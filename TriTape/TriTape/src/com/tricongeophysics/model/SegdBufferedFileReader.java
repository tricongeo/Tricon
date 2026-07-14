package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a SEG-D field file in buffered batches of traces. All structural byte
 * offsets and the trace-header field mapping come from a SegdConfig, so this
 * can be retargeted at a different vendor's layout from the GUI without
 * touching code. config.version selects which revision's general-header
 * structure to assume - REV1_REV2 and REV3_1 differ enough (see below) that
 * this reader handles them as genuinely different code paths, not just
 * different offsets into the same logic.
 *
 * REV1_REV2 (generic, simplified):
 *   General Header block 1 (32 bytes)
 *   General Header block 2 (32 bytes, present if header 1 says >=1 additional
 *     block), which carries the extended/external header block counts;
 *     further additional blocks are skipped unread
 *   Extended header blocks + External header blocks (32 bytes each, counts
 *     from General Header block 2)
 *   Channel set descriptor blocks (32 bytes each, one per channel set) - the
 *     first one's "number of samples" field gives samplesPerTrace for the
 *     whole file
 *   repeating Trace blocks: Trace Header (20 bytes) + Trace Header Extension
 *     blocks (32 bytes each, count from General Header block 1) + samples
 *
 * REV3_1 (modeled on Sercel's "Nodal Data Format Manual", DCM V5.0, SEG-D
 * Rev 3.1, continuous receiver domain - not the generic SEG-D Rev 3.1
 * standard, since vendor implementations of Rev 3.x vary):
 *   General Header block 1 (32 bytes)
 *   General Header block 2 (32 bytes, present if header 1 says >=1
 *     additional block), which carries the *true* additional-block count
 *     (overriding header 1's count, which Sercel always sets to the sentinel
 *     0xF) and the dominant sampling interval directly in microseconds
 *   General Header block 3 "Timestamp and size header" (32 bytes, present if
 *     the true additional-block count is >=2), which gives the absolute
 *     byte offset (from the start of the file) to the first trace header
 *     directly - this reader seeks straight there, rather than trying to
 *     enumerate every other general-header block, channel-set-descriptor
 *     (of which Sercel's continuous-mode files can have thousands - one per
 *     trace), extended-header, and external-header block individually
 *   Demultiplexed Trace Header (20 bytes) - byte 10 (1-based) gives *this
 *     trace's own* count of trace header extension blocks that follow
 *   All of this trace's own Trace Header Extension blocks (32 bytes each),
 *     concatenated onto the trace header into one combined buffer that the
 *     schema decodes against - so fields can address any block in the chain
 *     by its absolute offset (Extension #1's own bytes 25-28, 1-based, give
 *     *this trace's own* sample count, read fresh for every trace since it's
 *     confirmed to vary trace-to-trace on a real Sercel file - not assumed
 *     constant for the whole file, only used as an initial estimate via
 *     getSamplesPerTrace() before the first trace is read). Everything up
 *     through Extension #1 is decoded via the static, user-editable schema
 *     (see HeaderSchema.defaultSegdSchema()), but REC_X/REC_Y/REC_ELEV,
 *     SHOT_X/SHOT_Y/SHOT_ELEV, SHOT_VPID, and SHOT_YEAR/DAY/HOUR/MIN/
 *     SEC cannot be, since the Position (§6.4.7/6.4.8), VP-ID (§6.4.3), and
 *     Timestamp (§6.4.2) blocks that carry them sit after a variable-length,
 *     variable-content extension chain whose layout is NOT constant across
 *     traces or channel types - readOneTrace() instead walks that chain
 *     block-by-block, dispatching on each block's own type byte; see
 *     decodePositionAndVpFields() and decodeShotTimestamp() for the full
 *     details and the real-file evidence behind them. "aux" channel traces
 *     have a much shorter extension chain but were confirmed on a real file
 *     to still carry their own Timestamp block; they never reach a Position
 *     block at all though, so REC_/SHOT_X/Y/ELEV/SHOT_VPID simply read back
 *     as 0.0 (unresolved) for them - expected, not an error.
 *   trace samples (thisTraceSamples * 4 bytes, per this trace's own count)
 *
 * SEG-D varies significantly by revision and recording-system vendor, and
 * Sercel's Rev 3.1 implementation itself goes well beyond what's modeled
 * above (its own general-header blocks for vessel/job/line identification,
 * SERCEL-specific blocks, position/measurement trace header extensions,
 * etc.) that this simplified reader doesn't attempt to decode - every offset
 * it does use is configurable via SegdConfig/SegdSettingsPanel so it can be
 * corrected for a specific file.
 */
public class SegdBufferedFileReader extends BufferedFileReader
{
    public static final int FORMAT_IEEE_FLOAT_8058 = 8058;

    private final SegdConfig config;
    private RandomAccessFile file;
    private int formatCode;
    private int additionalGeneralHeaderBlocks;
    private int traceHeaderExtensionBlocks; //REV1_REV2 only: file-wide count from General Header block 1
    private int channelSetsPerScanType;
    private int fileNumber;
    private long firstTraceOffset;
    // REV1/REV2 only: this is the correct (and only) source for shot timing on those simpler
    // revisions, decoded once from General Header block 1 at file-open. REV3_1 must NOT use these
    // for SHOT_YEAR/DAY/HOUR/MIN/SEC - §6.4.2's Timestamp Header block (type 0x42, present in
    // every trace's own extension chain - confirmed on real files, including short "aux"-channel
    // chains) carries this trace's own actual GPS timestamp, which can genuinely differ trace-to-
    // trace within a long continuous recording; the General Header value is file/record-level only
    // and was previously (incorrectly) reused for every trace regardless of version. These fields are
    // kept only as REV3_1's fallback for the rare trace whose chain doesn't contain a Timestamp block
    // at all (see decodeShotTimestamp()) - see readOneTrace() for the actual per-trace decode.
    private int recordYear;
    private int recordJulianDay;
    private int recordHour;
    private int recordMinute;
    private int recordSecond;

    public SegdBufferedFileReader(String filename)
    {
        this(filename, new SegdConfig());
    }

    public SegdBufferedFileReader(String filename, SegdConfig config)
    {
        super(filename);
        this.config = config;
    }

    public SegdConfig getConfig() { return config; }

    /**
     * Lightweight, validation-free diagnostic peek at a file's key header
     * blocks and first trace - does NOT throw on implausible values (unlike
     * open()), so it can show raw bytes even when the current SegdConfig
     * offsets are wrong for this particular file. Used by
     * SegdHeaderPreviewPanel.
     */
    public static SegdHeaderPreview peekHeaders(String filename, SegdConfig config) throws IOException
    {
        try (RandomAccessFile f = new RandomAccessFile(filename, "r"))
        {
            byte[] gh1 = new byte[config.generalHeaderBlockBytes];
            f.readFully(gh1);

            int fileNumber = HeaderCodec.bcdToInt(gh1, config.fileNumberByteOffset, 2);
            int formatCode = HeaderCodec.bcdToInt(gh1, config.formatCodeByteOffset, 2);
            int channelSets = gh1[config.channelSetsPerScanTypeByteOffset] & 0xFF;
            int additionalNibble = (gh1[config.additionalGeneralHeaderBlocksByteOffset] >> 4) & 0x0F;
            int baseScanInterval = gh1[config.baseScanIntervalByteOffset] & 0xFF;
            int sampleRateFromGh1 = (int) Math.round((baseScanInterval / 16.0) * 1000.0);

            byte[] gh2 = null;
            byte[] gh3 = null;
            int trueAdditionalBlocks = -1;
            int dominantSamplingInterval = -1;
            int extendedHeaderBlocks = -1;
            int externalHeaderBlocks = -1;
            long headerSizeOffset = -1;
            byte[] firstTraceHeader = null;
            byte[] firstTraceExt1 = null;
            int firstTraceExtCount = -1;
            int firstTraceNumSamples = -1;

            if (additionalNibble >= 1 && f.length() >= f.getFilePointer() + config.generalHeaderBlockBytes)
            {
                gh2 = new byte[config.generalHeaderBlockBytes];
                f.readFully(gh2);

                if (config.version == SegdVersion.REV3_1)
                {
                    trueAdditionalBlocks = readUShort(gh2, config.rev3AdditionalBlocksCountByteOffsetInHeader2);
                    dominantSamplingInterval = readUInt24(gh2, config.rev3DominantSamplingIntervalByteOffsetInHeader2);
                    extendedHeaderBlocks = readUInt24(gh2, config.rev3ExtendedHeaderBlocksByteOffsetInHeader2);
                    externalHeaderBlocks = readUInt24(gh2, config.rev3ExternalHeaderBlocksByteOffsetInHeader2);

                    if (trueAdditionalBlocks >= 2 && f.length() >= f.getFilePointer() + config.generalHeaderBlockBytes)
                    {
                        gh3 = new byte[config.generalHeaderBlockBytes];
                        f.readFully(gh3);
                        headerSizeOffset = SegyBufferedFileReader.readInt(gh3, config.rev3HeaderSizeByteOffsetInHeader3) & 0xFFFFFFFFL;
                    }
                }
                else
                {
                    extendedHeaderBlocks = gh2[config.extendedHeaderBlocksByteOffsetInHeader2] & 0xFF;
                    externalHeaderBlocks = gh2[config.externalHeaderBlocksByteOffsetInHeader2] & 0xFF;
                }
            }

            if (config.version == SegdVersion.REV3_1 && headerSizeOffset > 0
                && headerSizeOffset + config.traceHeaderBytes + config.traceHeaderExtensionBytes <= f.length())
            {
                f.seek(headerSizeOffset);
                firstTraceHeader = new byte[config.traceHeaderBytes];
                f.readFully(firstTraceHeader);
                firstTraceExtCount = firstTraceHeader[config.rev3TraceHeaderExtensionCountByteOffset] & 0xFF;
                if (firstTraceExtCount > 0)
                {
                    firstTraceExt1 = new byte[config.traceHeaderExtensionBytes];
                    f.readFully(firstTraceExt1);
                    firstTraceNumSamples = SegyBufferedFileReader.readInt(firstTraceExt1, config.rev3NumSamplesByteOffsetInTraceHeaderExt1);
                }
            }

            return new SegdHeaderPreview(gh1, gh2, gh3, firstTraceHeader, firstTraceExt1,
                fileNumber, formatCode, additionalNibble, channelSets, sampleRateFromGh1,
                trueAdditionalBlocks, dominantSamplingInterval, extendedHeaderBlocks, externalHeaderBlocks,
                headerSizeOffset, firstTraceExtCount, firstTraceNumSamples);
        }
    }

    @Override
    protected void doOpen() throws IOException
    {
        file = new RandomAccessFile(filename, "r");
        totalBytes = file.length();

        byte[] gh1 = new byte[config.generalHeaderBlockBytes];
        file.readFully(gh1);
        readGeneralHeader1(gh1);

        if (config.version == SegdVersion.REV3_1)
        {
            doOpenRev3();
        }
        else
        {
            doOpenRev1Rev2();
        }

        if (samplesPerTrace <= 0)
        {
            throw new IOException("SegdBufferedFileReader: could not determine samples-per-trace from headers");
        }
        if ((long) samplesPerTrace * 4 > totalBytes)
        {
            throw new IOException("SegdBufferedFileReader: samples-per-trace (" + samplesPerTrace
                + ") is larger than the whole file - the header offsets are almost certainly wrong for this "
                + "file. Use the header preview panel to check the raw bytes against the current settings.");
        }

        firstTraceOffset = file.getFilePointer();
    }

    private void readGeneralHeader1(byte[] gh1)
    {
        fileNumber = HeaderCodec.bcdToInt(gh1, config.fileNumberByteOffset, 2);
        formatCode = HeaderCodec.bcdToInt(gh1, config.formatCodeByteOffset, 2);
        channelSetsPerScanType = gh1[config.channelSetsPerScanTypeByteOffset] & 0xFF;
        additionalGeneralHeaderBlocks = (gh1[config.additionalGeneralHeaderBlocksByteOffset] >> 4) & 0x0F;
        int baseScanInterval = gh1[config.baseScanIntervalByteOffset] & 0xFF; //value/16 = sample interval in ms
        sampleRateMicros = (int) Math.round((baseScanInterval / 16.0) * 1000.0);
        traceHeaderExtensionBlocks = gh1[config.traceHeaderExtensionCountByteOffset] & 0xFF;

        int yearTwoDigit = HeaderCodec.bcdToInt(gh1, config.recordYearByteOffset, 1);
        recordYear = 2000 + yearTwoDigit; //reasonable for the foreseeable future; adjust if this ever needs to read pre-2000 data
        int julianDayHundreds = gh1[config.additionalGeneralHeaderBlocksByteOffset] & 0x0F; //low nibble of the same byte as the additional-blocks count
        int julianDayTensOnes = HeaderCodec.bcdToInt(gh1, config.recordJulianDayByteOffset, 1);
        recordJulianDay = julianDayHundreds * 100 + julianDayTensOnes;
        recordHour = HeaderCodec.bcdToInt(gh1, config.recordHourByteOffset, 1);
        recordMinute = HeaderCodec.bcdToInt(gh1, config.recordMinuteByteOffset, 1);
        recordSecond = HeaderCodec.bcdToInt(gh1, config.recordSecondByteOffset, 1);
    }

    /** REV1_REV2: General Header block 2 (if present) carries the extended/external header block counts directly. */
    private void doOpenRev1Rev2() throws IOException
    {
        int extendedHeaderBlocks = 0;
        int externalHeaderBlocks = 0;
        if (additionalGeneralHeaderBlocks >= 1)
        {
            byte[] gh2 = new byte[config.generalHeaderBlockBytes];
            file.readFully(gh2);
            extendedHeaderBlocks = gh2[config.extendedHeaderBlocksByteOffsetInHeader2] & 0xFF;
            externalHeaderBlocks = gh2[config.externalHeaderBlocksByteOffsetInHeader2] & 0xFF;
        }
        for (int i = 1; i < additionalGeneralHeaderBlocks; i++)
        {
            file.skipBytes(config.generalHeaderBlockBytes);
        }
        file.skipBytes(extendedHeaderBlocks * config.generalHeaderBlockBytes);
        file.skipBytes(externalHeaderBlocks * config.generalHeaderBlockBytes);

        int nChannelSets = Math.max(1, channelSetsPerScanType);
        for (int i = 0; i < nChannelSets; i++)
        {
            byte[] csd = new byte[config.generalHeaderBlockBytes];
            file.readFully(csd);
            if (i == 0)
            {
                int off = config.samplesFieldByteOffsetInChannelSetDescriptor;
                samplesPerTrace = ((csd[off] & 0xFF) << 8) | (csd[off + 1] & 0xFF);
            }
        }
    }

    /**
     * REV3_1: General Header block 2 gives the true additional-block count and the sample
     * rate; General Header block 3 gives the absolute file offset to the first trace header
     * directly, so we seek straight there instead of enumerating everything in between.
     * samplesPerTrace is set here from the first trace's own Trace Header Extension #1 purely
     * as an initial estimate for getSamplesPerTrace() (e.g. before any traces are read); actual
     * reads always re-derive each trace's own sample count fresh - see readOneTrace().
     */
    private void doOpenRev3() throws IOException
    {
        long headerSizeOffset = -1;

        if (additionalGeneralHeaderBlocks >= 1)
        {
            byte[] gh2 = new byte[config.generalHeaderBlockBytes];
            file.readFully(gh2);
            int trueAdditionalBlocks = readUShort(gh2, config.rev3AdditionalBlocksCountByteOffsetInHeader2);
            int dominantSamplingIntervalUs = readUInt24(gh2, config.rev3DominantSamplingIntervalByteOffsetInHeader2);
            if (dominantSamplingIntervalUs > 0)
            {
                sampleRateMicros = dominantSamplingIntervalUs;
            }

            if (trueAdditionalBlocks >= 2)
            {
                byte[] gh3 = new byte[config.generalHeaderBlockBytes];
                file.readFully(gh3);
                headerSizeOffset = SegyBufferedFileReader.readInt(gh3, config.rev3HeaderSizeByteOffsetInHeader3) & 0xFFFFFFFFL;
            }
        }

        if (headerSizeOffset > 0 && headerSizeOffset < totalBytes)
        {
            file.seek(headerSizeOffset);
        }
        //else: General Header block 3 wasn't present/usable - leave the file pointer where it is
        //(right after general header block 2) as a best-effort fallback

        //peek the first trace's own header + Trace Header Extension #1 to learn samplesPerTrace, then rewind
        long tracesStart = file.getFilePointer();
        if (tracesStart + config.traceHeaderBytes + config.traceHeaderExtensionBytes <= totalBytes)
        {
            byte[] th = new byte[config.traceHeaderBytes];
            file.readFully(th);
            int extCount = th[config.rev3TraceHeaderExtensionCountByteOffset] & 0xFF;
            if (extCount > 0)
            {
                byte[] ext1 = new byte[config.traceHeaderExtensionBytes];
                file.readFully(ext1);
                int n = SegyBufferedFileReader.readInt(ext1, config.rev3NumSamplesByteOffsetInTraceHeaderExt1);
                if (n > 0) samplesPerTrace = n;
            }
        }
        file.seek(tracesStart);
    }

    @Override
    protected SeismicTrace[] doReadNextTraces(int batchSize) throws IOException
    {
        ArrayList<SeismicTrace> list = new ArrayList<SeismicTrace>(batchSize);
        for (int i = 0; i < batchSize; i++)
        {
            if (file.getFilePointer() >= totalBytes) break;
            try
            {
                list.add(readOneTrace());
            }
            catch (EOFException eof)
            {
                break;
            }
        }
        return list.toArray(new SeismicTrace[0]);
    }

    private SeismicTrace readOneTrace() throws IOException
    {
        byte[] th = new byte[config.traceHeaderBytes];
        file.readFully(th);

        byte[] allExtensions = null; //REV3_1: every extension block for this trace, concatenated in order
        int thisTraceSamples;
        if (config.version == SegdVersion.REV3_1)
        {
            // Rev 3.1: samples-per-trace can vary trace-to-trace (confirmed against a real Sercel
            // file - trace 1 had 10000 samples, trace 2 had 19999), so it's read from *this trace's
            // own* Trace Header Extension #1 rather than assumed constant for the whole file.
            int extensionBlocksThisTrace = th[config.rev3TraceHeaderExtensionCountByteOffset] & 0xFF;
            thisTraceSamples = samplesPerTrace; //fallback if there's no extension #1 to read it from
            if (extensionBlocksThisTrace > 0)
            {
                // read every extension block for this trace (not just #1), so the schema can address
                // fields anywhere in the chain - e.g. Position/Source blocks that follow Extension #1 -
                // by their absolute offset within this combined buffer, per the file's actual, observed
                // block order (see HeaderSchema.defaultSegdSchema() for what's mapped where and why)
                allExtensions = new byte[extensionBlocksThisTrace * config.traceHeaderExtensionBytes];
                file.readFully(allExtensions);
                int n = SegyBufferedFileReader.readInt(allExtensions, config.rev3NumSamplesByteOffsetInTraceHeaderExt1);
                if (n > 0) thisTraceSamples = n;
            }
        }
        else
        {
            thisTraceSamples = samplesPerTrace;
            if (traceHeaderExtensionBlocks > 0)
            {
                file.skipBytes(traceHeaderExtensionBlocks * config.traceHeaderExtensionBytes);
            }
        }

        // decode the schema against the 20-byte trace header plus (Rev 3.1) every extension block
        // that follows it, so schema fields can address any offset within that combined buffer
        byte[] combined = allExtensions == null ? th : concat(th, allExtensions);
        List<HeaderFieldDef> fields = config.traceHeaderSchema.getFields();
        int recordFieldCount = config.version == SegdVersion.REV3_1 ? 5 : 0; //SHOT_YEAR/DAY/HOUR/MIN/SEC (field named SHOT_DAY, not SHOT_JULIAN_DAY - must match defaultSegySchema()'s name for SEG-D->SEG-Y pass-through), appended below
        // REC_X/REC_Y/REC_ELEV, SHOT_X/SHOT_Y/SHOT_ELEV, SHOT_VPID, SHOTLINE, SHOTSTN - see
        // decodePositionAndVpFields(); these can't be static-offset schema fields since the
        // Position/VP-ID/Source-Description blocks that carry them sit after a variable-length,
        // variable-content extension chain (see class javadoc and decodePositionAndVpFields() below),
        // so they're always appended in this fixed order/count for REV3_1 (0.0 default when
        // unresolved, e.g. aux-channel traces that never reach these blocks) so every trace in a file
        // gets the same header set regardless of channel type.
        int positionFieldCount = config.version == SegdVersion.REV3_1 ? 9 : 0;
        String[] names = new String[fields.size() + recordFieldCount + positionFieldCount];
        double[] values = new double[fields.size() + recordFieldCount + positionFieldCount];
        for (int i = 0; i < fields.size(); i++)
        {
            HeaderFieldDef f = fields.get(i);
            names[i] = f.getName();
            values[i] = (f.getByteOffset() + f.getType().byteLength <= combined.length)
                ? HeaderCodec.decode(combined, f)
                : 0; //field falls outside whatever buffer is actually available for this trace/version - skip rather than crash
        }
        if (recordFieldCount > 0)
        {
            // §6.4.2 Timestamp Header block, from THIS trace's own extension chain - the correct
            // source for REV3_1 (see decodeShotTimestamp() and the class javadoc for why the
            // record-level General Header timestamp below is wrong for this version: it's the same
            // single value for every trace in the file, but Sercel's continuous-recording files carry
            // a genuine, distinct GPS timestamp per trace). Falls back to the record-level fields
            // only if this particular trace's chain doesn't contain a Timestamp block at all.
            int[] shotTime = decodeShotTimestamp(allExtensions);
            int i = fields.size();
            names[i] = "SHOT_YEAR";        values[i] = shotTime != null ? shotTime[0] : recordYear;
            names[i + 1] = "SHOT_DAY";      values[i + 1] = shotTime != null ? shotTime[1] : recordJulianDay;
            names[i + 2] = "SHOT_HOUR";     values[i + 2] = shotTime != null ? shotTime[2] : recordHour;
            names[i + 3] = "SHOT_MIN";      values[i + 3] = shotTime != null ? shotTime[3] : recordMinute;
            names[i + 4] = "SHOT_SEC";      values[i + 4] = shotTime != null ? shotTime[4] : recordSecond;
        }
        if (positionFieldCount > 0)
        {
            double[] pos = decodePositionAndVpFields(allExtensions);
            int i = fields.size() + recordFieldCount;
            names[i] = "REC_X";         values[i] = pos[0];
            names[i + 1] = "REC_Y";     values[i + 1] = pos[1];
            names[i + 2] = "REC_ELEV";  values[i + 2] = pos[2];
            names[i + 3] = "SHOT_X";    values[i + 3] = pos[3];
            names[i + 4] = "SHOT_Y";    values[i + 4] = pos[4];
            names[i + 5] = "SHOT_ELEV"; values[i + 5] = pos[5];
            names[i + 6] = "SHOT_VPID"; values[i + 6] = pos[6];
            names[i + 7] = "SHOTLINE";  values[i + 7] = pos[7];
            names[i + 8] = "SHOTSTN";   values[i + 8] = pos[8];
        }

        float[] data = new float[thisTraceSamples];
        byte[] samples = new byte[thisTraceSamples * 4]; //assume 4-byte samples (IEEE float, format 8058)
        file.readFully(samples);
        for (int i = 0; i < thisTraceSamples; i++)
        {
            int bits = SegyBufferedFileReader.readInt(samples, i * 4);
            data[i] = (formatCode == FORMAT_IEEE_FLOAT_8058)
                ? Float.intBitsToFloat(bits)
                : SegyBufferedFileReader.ibmToFloat(bits); //best-effort fallback for other format codes
        }

        SeismicTrace trace = new SeismicTrace();
        trace.setHeaderList(names);
        trace.setHeaders(values);
        trace.setData(data);
        return trace;
    }

    /**
     * Walks this trace's own extension-block chain (everything after Extension #1, i.e. the buffer
     * passed here is the SAME allExtensions array read in readOneTrace() - Extension #1 is block 0
     * of it) looking for Position blocks (§6.4.7/6.4.8: a 0x50/0x51/0x52 triple - three consecutive
     * 32-byte blocks, self-identified by their own last byte) and the SERCEL VP identification Block
     * (§6.4.3, type 0xBB). Position/type-byte offsets after Extension #1 are NOT constant across
     * traces or channel types (confirmed against real files: a 25-extension "live" channel trace and
     * a 6-extension "aux" channel trace both start with the same block order but diverge completely
     * after that), so this walks block-by-block dispatching on each block's own type byte rather than
     * assuming any fixed offset - see the class javadoc and HeaderSchema.defaultSegdSchema() for why
     * everything up through Extension #1 CAN safely use fixed offsets but nothing after it can.
     * <p>
     * Occurrence order of the 0x50/51/52 triples is significant and was confirmed against a real
     * file: 1st triple = receiver/sensor position, 2nd = source position planned (§6.4.7), 3rd =
     * source position measured (§6.4.8). Only occurrences 1-3 are used; a 4th (which shouldn't occur
     * per the manual, but the format is vendor-messy enough not to assume) is ignored rather than
     * overwriting anything, and the walk never runs past the end of the buffer.
     * <p>
     * Each triple's middle block (type 0x51, "coordinate tuple 1") and last block (type 0x52,
     * "coordinate tuple 2", a backup/secondary solution) each carry three big-endian IEEE-754 doubles
     * (X/Y/Z) at their own offsets 0/8/16. Confirmed against a real file that either tuple can be the
     * one actually populated - e.g. a source-planned triple where tuple 1 held a valid UTM solution
     * and tuple 2 held the same point in decimal lat/lon, but also a source-measured triple where
     * BOTH tuples were entirely unpopulated (every coordinate IEEE-754 +Infinity). So each tuple is
     * checked for finiteness before use: tuple 1 is preferred when finite, tuple 2 is used as a
     * fallback when finite, and the triple is left unresolved (0.0, same "unknown" convention as the
     * rest of this reader) only when neither tuple is usable. This means a "measured" triple that
     * turned out to be empty correctly falls back to whatever the "planned" triple already resolved,
     * rather than blindly overwriting a good planned position with an empty measured one.
     * <p>
     * The VP-ID block's 16-byte "VP uuid" field (§6.4.3) was confirmed on a real file to be almost
     * entirely zero-padded with the meaningful VP/vibrator-point number in the low bytes (e.g. raw
     * ...00000006fd = 1789 decimal), so only the low 8 bytes are decoded, as an unsigned integer -
     * comfortably exact as a double for any realistic VP count.
     * <p>
     * The Source Description block (§6.4.5 documents it as type 0x17, "Source Air Gun Block" -
     * confirmed on a real vibroseis file's live-channel trace that Sercel also emits it as type
     * 0x15 for vibrator sources, with an IDENTICAL byte layout otherwise, right down to the three
     * "offset cross-line/in-line/depth" fields reading back the documented 0x8000 "unknown" sentinel)
     * carries Source Line Number and Source Point Number as two 5-byte fields (3-byte integer part +
     * 2-byte fraction part, same 24.16 fixed-point packing as RECLINE/RECSTN - see
     * HeaderSchema.defaultSegdSchema()), at the block's own offsets 3 and 8. Both 0x15 and 0x17 are
     * treated identically here and decoded into SHOTLINE/SHOTSTN.
     *
     * @param allExtensions this trace's full concatenated extension-block chain (may be null if the
     *                       trace has zero extension blocks, e.g. a malformed/truncated trace)
     * @return double[9]: {REC_X, REC_Y, REC_ELEV, SHOT_X, SHOT_Y, SHOT_ELEV, SHOT_VPID, SHOTLINE,
     *         SHOTSTN}, with 0.0 in any slot that couldn't be resolved from this trace's chain (e.g.
     *         aux-channel traces, which never reach these blocks at all)
     */
    private static double[] decodePositionAndVpFields(byte[] allExtensions)
    {
        double[] result = new double[9]; //all default to 0.0 = unresolved
        if (allExtensions == null) return result;

        final int BLOCK = 32;
        final int TRIPLE = 3 * BLOCK;
        int positionTripleCount = 0;
        int i = 0;
        while (i + BLOCK <= allExtensions.length)
        {
            int type = allExtensions[i + BLOCK - 1] & 0xFF;
            if (type == 0x50 && positionTripleCount < 3 && i + TRIPLE <= allExtensions.length)
            {
                double[] tuple1 = readCoordinateTuple(allExtensions, i + BLOCK);
                double[] tuple2 = readCoordinateTuple(allExtensions, i + 2 * BLOCK);
                double[] chosen = isFiniteTuple(tuple1) ? tuple1 : (isFiniteTuple(tuple2) ? tuple2 : null);
                positionTripleCount++;
                if (chosen != null)
                {
                    if (positionTripleCount == 1)
                    {
                        result[0] = chosen[0]; result[1] = chosen[1]; result[2] = chosen[2]; //REC_X/Y/ELEV
                    }
                    else
                    {
                        // 2nd (planned) and 3rd (measured) triples both target SHOT_X/Y/ELEV; measured
                        // only overwrites planned if it actually resolved to something - see javadoc above
                        result[3] = chosen[0]; result[4] = chosen[1]; result[5] = chosen[2]; //SHOT_X/Y/ELEV
                    }
                }
                i += TRIPLE;
            }
            else if (type == 0xBB && i + BLOCK <= allExtensions.length)
            {
                result[6] = HeaderCodec.readInt64(allExtensions, i + 8) & Long.MAX_VALUE; //low 8 bytes of the 16-byte VP uuid; SHOT_VPID
                i += BLOCK;
            }
            else if ((type == 0x15 || type == 0x17) && i + BLOCK <= allExtensions.length)
            {
                // Source Description block (§6.4.5): Source Line/Point Number, each a 5-byte
                // 24.16 fixed-point field (3-byte integer + 2-byte fraction) at the block's own
                // offsets 3 and 8 - confirmed against a real file down to the 0x8000 "unknown"
                // sentinels in the unused offset fields. 0x17 is the manual's documented "Air Gun"
                // type; 0x15 was confirmed empirically as Sercel's vibrator equivalent, same layout.
                result[7] = HeaderCodec.readInt40(allExtensions, i + 3) / 65536.0; //SHOTLINE
                result[8] = HeaderCodec.readInt40(allExtensions, i + 8) / 65536.0; //SHOTSTN
                i += BLOCK;
            }
            else
            {
                i += BLOCK;
            }
        }
        return result;
    }

    /** reads three consecutive big-endian IEEE-754 doubles (X/Y/Z) at a coordinate-tuple sub-block's own offsets 0/8/16 */
    private static double[] readCoordinateTuple(byte[] buf, int off)
    {
        return new double[] {
            Double.longBitsToDouble(HeaderCodec.readInt64(buf, off)),
            Double.longBitsToDouble(HeaderCodec.readInt64(buf, off + 8)),
            Double.longBitsToDouble(HeaderCodec.readInt64(buf, off + 16))
        };
    }

    /** an unpopulated coordinate tuple reads back as IEEE-754 +Infinity (confirmed on real files) or NaN */
    private static boolean isFiniteTuple(double[] xyz)
    {
        return Double.isFinite(xyz[0]) && Double.isFinite(xyz[1]) && Double.isFinite(xyz[2]);
    }

    private static final Instant GPS_EPOCH = Instant.parse("1980-01-06T00:00:00Z");
    // current cumulative GPS-UTC leap second offset; GPS time itself never inserts leap seconds, so
    // this only needs to change if the international timekeeping bodies schedule a new one (none
    // since 2016) - a one-second-per-leap-second error here doesn't affect which trace a shot belongs
    // to, only the displayed SHOT_SEC value, so it's not worth making this runtime-configurable
    private static final int GPS_UTC_LEAP_SECONDS = 18;

    /**
     * Walks this trace's own extension-block chain for the Timestamp Header block (§6.4.2, type
     * 0x42) and decodes its GPS time (bytes 1-8, big-endian microseconds since the GPS epoch) into
     * calendar components. Confirmed on real files (both a 25-extension "live" channel trace and a
     * 6-extension "aux" channel trace) that this block is present and is the SECOND block in the
     * chain, right after Extension #1 - but this still walks type-byte-by-type-byte rather than
     * assuming that fixed position, consistent with decodePositionAndVpFields() and for the same
     * reason: block order past Extension #1 isn't guaranteed constant.
     * <p>
     * This is what REV3_1 must use for SHOT_YEAR/DAY/HOUR/MIN/SEC - unlike REV1_REV2, where
     * General Header block 1's record-level timestamp (recordYear/recordJulianDay/etc., read once at
     * file-open) is the only timestamp SEG-D provides at all. Using that same file/record-level value
     * for every trace in a REV3_1 file (as this reader incorrectly did previously) silently discards
     * the fact that Sercel's continuous-recording files carry a genuine, distinct GPS timestamp per
     * trace in this block.
     *
     * @param allExtensions this trace's full concatenated extension-block chain (may be null)
     * @return {year, dayOfYear, hour, minute, second} in UTC, or null if this trace's chain doesn't
     *         contain a Timestamp block at all (caller should fall back to the record-level fields
     *         in that case, per the class javadoc)
     */
    private static int[] decodeShotTimestamp(byte[] allExtensions)
    {
        if (allExtensions == null) return null;

        final int BLOCK = 32;
        int i = 0;
        while (i + BLOCK <= allExtensions.length)
        {
            int type = allExtensions[i + BLOCK - 1] & 0xFF;
            if (type == 0x42)
            {
                long gpsMicros = HeaderCodec.readInt64(allExtensions, i);
                long gpsSeconds = Math.floorDiv(gpsMicros, 1_000_000L);
                long nanosRemainder = Math.floorMod(gpsMicros, 1_000_000L) * 1_000L;
                Instant utc = GPS_EPOCH.plusSeconds(gpsSeconds - GPS_UTC_LEAP_SECONDS).plusNanos(nanosRemainder);
                ZonedDateTime z = utc.atZone(ZoneOffset.UTC);
                return new int[] { z.getYear(), z.getDayOfYear(), z.getHour(), z.getMinute(), z.getSecond() };
            }
            i += BLOCK;
        }
        return null; //no Timestamp block in this trace's chain - caller falls back to the record-level value
    }

    private static byte[] concat(byte[] a, byte[] b)
    {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    @Override
    public boolean hasMoreTraces()
    {
        try
        {
            return file != null && file.getFilePointer() < totalBytes;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    @Override
    public void rewindToFirstTrace() throws IOException
    {
        file.seek(firstTraceOffset);
        tracesRead = 0;
    }

    @Override
    public long getBytesRead()
    {
        try
        {
            return file == null ? 0 : file.getFilePointer();
        }
        catch (IOException e)
        {
            return 0;
        }
    }

    public int getFormatCode() { return formatCode; }
    public int getFileNumber() { return fileNumber; }

    @Override
    public void close() throws IOException
    {
        if (file != null) file.close();
    }

    private static int readUShort(byte[] b, int off)
    {
        return ((b[off] & 0xFF) << 8) | (b[off + 1] & 0xFF);
    }

    private static int readUInt24(byte[] b, int off)
    {
        return ((b[off] & 0xFF) << 16) | ((b[off + 1] & 0xFF) << 8) | (b[off + 2] & 0xFF);
    }
}
