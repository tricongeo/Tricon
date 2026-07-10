package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
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
 *   Trace Header Extension #1 (32 bytes, always the first extension when any
 *     are present) - bytes 25-28 (1-based) give *this trace's own* sample
 *     count, which is read fresh for every trace (confirmed against a real
 *     Sercel file that samples-per-trace genuinely varies trace-to-trace -
 *     it is not assumed constant for the file, only used as an initial
 *     estimate via getSamplesPerTrace() before the first trace is read)
 *   remaining Trace Header Extension blocks (skipped, not decoded)
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

        byte[] ext1 = null;
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
                ext1 = new byte[config.traceHeaderExtensionBytes];
                file.readFully(ext1);
                int n = SegyBufferedFileReader.readInt(ext1, config.rev3NumSamplesByteOffsetInTraceHeaderExt1);
                if (n > 0) thisTraceSamples = n;
                int remainingExtensions = extensionBlocksThisTrace - 1; //extension #1 already consumed above
                if (remainingExtensions > 0)
                {
                    file.skipBytes(remainingExtensions * config.traceHeaderExtensionBytes);
                }
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

        // decode the schema against the 20-byte trace header plus (Rev 3.1, when present) Trace
        // Header Extension #1 appended right after it, so schema fields can address offsets 20-51
        // to reach into the extension - see HeaderSchema.defaultSegdSchema() for what's mapped there
        byte[] combined = ext1 == null ? th : concat(th, ext1);
        List<HeaderFieldDef> fields = config.traceHeaderSchema.getFields();
        String[] names = new String[fields.size()];
        double[] values = new double[fields.size()];
        for (int i = 0; i < fields.size(); i++)
        {
            HeaderFieldDef f = fields.get(i);
            names[i] = f.getName();
            values[i] = (f.getByteOffset() + f.getType().byteLength <= combined.length)
                ? HeaderCodec.decode(combined, f)
                : 0; //field falls outside whatever buffer is actually available for this trace/version - skip rather than crash
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
