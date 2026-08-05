package com.tricongeophysics.model;

/**
 * Tunable byte-layout parameters for the simplified SEG-D reader/writer: which
 * revision's general-header structure to assume (version), where the key
 * general-header/channel-set-descriptor values live, and the full
 * trace-header field mapping (HeaderSchema). See SegdBufferedFileReader's
 * class javadoc for the structural assumptions these offsets describe.
 *
 * The REV3_1 fields below follow the Sercel "Nodal Data Format Manual" (DCM
 * V5.0, SEG-D Rev 3.1, continuous receiver domain) rather than a generic
 * guess - Rev 3.1's actual layout differs substantially from Rev 1/2's, most
 * notably: General Header Block #2 carries the *true* additional-block count
 * and the dominant sampling interval directly in microseconds, and General
 * Header Block #3 gives the absolute byte offset to the first trace header
 * outright (so this reader can jump straight there instead of having to
 * enumerate every intervening header/channel-set-descriptor block, which for
 * Sercel's continuous-mode files can number in the thousands).
 */
public class SegdConfig
{
    public SegdVersion version = SegdVersion.REV1_REV2;

    public int generalHeaderBlockBytes = 32;
    public int traceHeaderBytes = 20;
    public int traceHeaderExtensionBytes = 32;

    /** offsets are within General Header block 1 (same positions assumed for every revision) */
    public int fileNumberByteOffset = 0;                    //BCD, 2 bytes
    public int formatCodeByteOffset = 2;                     //BCD, 2 bytes
    public int channelSetsPerScanTypeByteOffset = 9;         //1 byte
    public int additionalGeneralHeaderBlocksByteOffset = 11; //high nibble of this byte
    public int baseScanIntervalByteOffset = 27;              //1 byte, value/16 = ms
    public int traceHeaderExtensionCountByteOffset = 29;     //1 byte (Rev 1/2 only; Rev 3.1 reads this per-trace instead)

    /**
     * Record timestamp, within General Header block 1 (Rev 3.1). Year/hour/minute/second are
     * each a single BCD byte; the Julian day is a 3-BCD-digit field split across two bytes - the
     * hundreds digit is the LOW nibble of the SAME byte as additionalGeneralHeaderBlocksByteOffset
     * (whose HIGH nibble is the additional-block count), and the tens+ones digits are their own byte.
     */
    public int recordYearByteOffset = 10;
    public int recordJulianDayByteOffset = 12; //tens+ones digits; hundreds digit comes from the low nibble of additionalGeneralHeaderBlocksByteOffset's byte
    public int recordHourByteOffset = 13;
    public int recordMinuteByteOffset = 14;
    public int recordSecondByteOffset = 15;

    /** offsets are within General Header block 2 - used only when version == REV1_REV2 */
    public int extendedHeaderBlocksByteOffsetInHeader2 = 8;
    public int externalHeaderBlocksByteOffsetInHeader2 = 9;

    /**
     * Rev 3.1 only: offsets within General Header Block #2. Note the field widths differ
     * from the Rev 1/2 fields above (these are multi-byte, per the Sercel manual).
     */
    public int rev3AdditionalBlocksCountByteOffsetInHeader2 = 22;    //2-byte ubin, bytes 23-24 (1-based)
    public int rev3DominantSamplingIntervalByteOffsetInHeader2 = 24; //3-byte ubin, bytes 25-27 (1-based), microseconds
    public int rev3ExtendedHeaderBlocksByteOffsetInHeader2 = 5;      //3-byte ubin, bytes 6-8 (1-based)
    public int rev3ExternalHeaderBlocksByteOffsetInHeader2 = 27;     //3-byte ubin, bytes 28-30 (1-based)

    /**
     * Rev 3.1 only: General Header Block #3 (Timestamp and size header) gives the absolute
     * byte offset (from the start of the file) to the first trace header directly.
     */
    public int rev3HeaderSizeByteOffsetInHeader3 = 24; //4-byte ubin, bytes 25-28 (1-based)

    /** Rev 3.1 only: within each trace's own 20-byte Demultiplexed Trace Header */
    public int rev3TraceHeaderExtensionCountByteOffset = 9; //1 byte, byte 10 (1-based); per-trace extension block count

    /** Rev 3.1 only: within Trace Header Extension #1 (the first 32-byte extension block after the trace header) */
    public int rev3NumSamplesByteOffsetInTraceHeaderExt1 = 24; //4-byte ubin, bytes 25-28 (1-based)

    /**
     * Rev 2.1 only (SmartSolo/DTCC): offsets within General Header Block #1. Confirmed against a
     * real file - see SegdBufferedFileReader's class javadoc. Both are BCD (unlike the REV1_REV2
     * generic fields above, which read the equivalent-looking bytes as raw binary - these are NOT
     * interchangeable with channelSetsPerScanTypeByteOffset/baseScanIntervalByteOffset above, even
     * though they describe conceptually similar values).
     */
    public int rev21ChannelSetsPerScanTypeByteOffset = 28; //1-byte BCD, byte 29 (1-based): "Number of channel sets per record"
    public int rev21BaseScanIntervalByteOffset = 22;       //1-byte binary, byte 23 (1-based): value/16 = ms
    public int rev21ExtendedHeaderLengthByteOffset = 30;   //1-byte BCD, byte 31 (1-based): count of 32-byte Extended Header blocks
    public int rev21ExternalHeaderLengthByteOffset = 31;   //1-byte BCD, byte 32 (1-based): count of 32-byte External Header blocks

    /** Rev 2.1 only: within each trace's own 20-byte Demultiplexed Trace Header (byte 10, 1-based; always 7 per the manual, but read per-trace for robustness, same approach as REV3_1) */
    public int rev21TraceHeaderExtensionCountByteOffset = 9;

    /** Rev 2.1 only: within Trace Header Extension #1 - "Number of samples per trace", 3-byte binary */
    public int rev21NumSamplesByteOffsetInTraceHeaderExt1 = 7; //byte 8 (1-based)

    /** Rev 2.1 only: within Trace Header Extension #2 - "Extended Trace Number" (used here as CHAN), 4-byte binary */
    public int rev21ChanByteOffsetInTraceHeaderExt2 = 28; //byte 29 (1-based)

    /**
     * Rev 2.1 only: within Trace Header Extension #3 - Receiver/Source Easting/Northing/Elevation,
     * 4-byte binary. The manual states centimeters, but real-file evidence suggests these are
     * actually hundredths of a US survey foot (state-plane feet) - a very plausible convention for
     * onshore US surveys, and consistent with the magnitude of real decoded values (a plain cm/100
     * reading looked like an impossible ~10,380 km northing; the same raw value read as feet/100
     * lands in the range State Plane Northing commonly uses). The /100 divisor is unchanged either
     * way - only the resulting unit label changes from meters to feet - so if this ever turns out to
     * be wrong for a given file/zone, only the interpretation (not the division) needs revisiting.
     * A raw value of 0x80000000 (INT32_MIN) was confirmed on a real file to be a sentinel for "not
     * populated" (same convention SmartSolo/Sercel both use elsewhere), so that specific raw value
     * decodes to 0.0 ("unresolved") rather than a huge bogus coordinate.
     */
    public int rev21RecXByteOffsetInTraceHeaderExt3 = 0;     //byte 1 (1-based)
    public int rev21RecYByteOffsetInTraceHeaderExt3 = 4;     //byte 5 (1-based)
    public int rev21RecElevByteOffsetInTraceHeaderExt3 = 8;  //byte 9 (1-based) - the manual's own table says "8-12", which overlaps REC_Y's own bytes 5-8; corrected to the non-overlapping byte 9 start, consistent with the rest of the block's monotonic field layout
    public int rev21ShotXByteOffsetInTraceHeaderExt3 = 12;   //byte 13 (1-based)
    public int rev21ShotYByteOffsetInTraceHeaderExt3 = 16;   //byte 17 (1-based)
    public int rev21ShotElevByteOffsetInTraceHeaderExt3 = 20; //byte 21 (1-based)

    /**
     * Rev 2.1 only: within General Header Block #3 - "Source Line/Point Number" INTEGER parts (the
     * fraction bytes immediately after each, bytes 7-8/12-13 1-based, are NOT read here - only the
     * integer part was requested). Record-level (same value for every trace in the file), read once
     * at file-open - see rev21ShotLine/rev21ShotStn fields above.
     */
    public int rev21ShotLineByteOffsetInGh3 = 3; //byte 4 (1-based), 3-byte binary
    public int rev21ShotStnByteOffsetInGh3 = 8;  //byte 9 (1-based), 3-byte binary

    /**
     * Rev 2.1 only: within the Demultiplexed Trace Header itself (section 2.2.1, bytes 1-2 1-based)
     * - the plain four-digit BCD file number, used here as FFID. The manual documents that this reads
     * as the sentinel 0xFFFF when the real file number exceeds 9999, in which case the "Extended file
     * number" (bytes 18-20, 1-based, 3-byte binary, same trace header) should be used instead - see
     * decodeRev21ExtraFields(). (This is deliberately NOT the same field REV3_1 uses for FFID: that
     * revision's Sercel data was confirmed to always read this same byte position as an unrelated
     * FFFF sentinel with no fallback, so REV3_1 gets FFID from its own VP Identification Block
     * instead - see defaultSegdSchema()'s comment and decodePositionAndVpFields().)
     */
    public int rev21FfidByteOffsetInTraceHeader = 0;                 //byte 1 (1-based), 2-byte BCD
    public int rev21ExtendedFileNumberByteOffsetInTraceHeader = 17;  //byte 18 (1-based), 3-byte binary; sentinel fallback

    /** offset is within the 32-byte Channel Set Descriptor block (Rev 1/2 only; Rev 3.1 doesn't need this - see above) */
    public int samplesFieldByteOffsetInChannelSetDescriptor = 24; //2-byte binary

    public HeaderSchema traceHeaderSchema = HeaderSchema.defaultSegdSchema();

    public SegdConfig copy()
    {
        SegdConfig c = new SegdConfig();
        c.version = version;
        c.generalHeaderBlockBytes = generalHeaderBlockBytes;
        c.traceHeaderBytes = traceHeaderBytes;
        c.traceHeaderExtensionBytes = traceHeaderExtensionBytes;
        c.fileNumberByteOffset = fileNumberByteOffset;
        c.formatCodeByteOffset = formatCodeByteOffset;
        c.channelSetsPerScanTypeByteOffset = channelSetsPerScanTypeByteOffset;
        c.additionalGeneralHeaderBlocksByteOffset = additionalGeneralHeaderBlocksByteOffset;
        c.baseScanIntervalByteOffset = baseScanIntervalByteOffset;
        c.traceHeaderExtensionCountByteOffset = traceHeaderExtensionCountByteOffset;
        c.recordYearByteOffset = recordYearByteOffset;
        c.recordJulianDayByteOffset = recordJulianDayByteOffset;
        c.recordHourByteOffset = recordHourByteOffset;
        c.recordMinuteByteOffset = recordMinuteByteOffset;
        c.recordSecondByteOffset = recordSecondByteOffset;
        c.extendedHeaderBlocksByteOffsetInHeader2 = extendedHeaderBlocksByteOffsetInHeader2;
        c.externalHeaderBlocksByteOffsetInHeader2 = externalHeaderBlocksByteOffsetInHeader2;
        c.rev3AdditionalBlocksCountByteOffsetInHeader2 = rev3AdditionalBlocksCountByteOffsetInHeader2;
        c.rev3DominantSamplingIntervalByteOffsetInHeader2 = rev3DominantSamplingIntervalByteOffsetInHeader2;
        c.rev3ExtendedHeaderBlocksByteOffsetInHeader2 = rev3ExtendedHeaderBlocksByteOffsetInHeader2;
        c.rev3ExternalHeaderBlocksByteOffsetInHeader2 = rev3ExternalHeaderBlocksByteOffsetInHeader2;
        c.rev3HeaderSizeByteOffsetInHeader3 = rev3HeaderSizeByteOffsetInHeader3;
        c.rev3TraceHeaderExtensionCountByteOffset = rev3TraceHeaderExtensionCountByteOffset;
        c.rev3NumSamplesByteOffsetInTraceHeaderExt1 = rev3NumSamplesByteOffsetInTraceHeaderExt1;
        c.rev21ChannelSetsPerScanTypeByteOffset = rev21ChannelSetsPerScanTypeByteOffset;
        c.rev21BaseScanIntervalByteOffset = rev21BaseScanIntervalByteOffset;
        c.rev21ExtendedHeaderLengthByteOffset = rev21ExtendedHeaderLengthByteOffset;
        c.rev21ExternalHeaderLengthByteOffset = rev21ExternalHeaderLengthByteOffset;
        c.rev21TraceHeaderExtensionCountByteOffset = rev21TraceHeaderExtensionCountByteOffset;
        c.rev21NumSamplesByteOffsetInTraceHeaderExt1 = rev21NumSamplesByteOffsetInTraceHeaderExt1;
        c.rev21ChanByteOffsetInTraceHeaderExt2 = rev21ChanByteOffsetInTraceHeaderExt2;
        c.rev21RecXByteOffsetInTraceHeaderExt3 = rev21RecXByteOffsetInTraceHeaderExt3;
        c.rev21RecYByteOffsetInTraceHeaderExt3 = rev21RecYByteOffsetInTraceHeaderExt3;
        c.rev21RecElevByteOffsetInTraceHeaderExt3 = rev21RecElevByteOffsetInTraceHeaderExt3;
        c.rev21ShotXByteOffsetInTraceHeaderExt3 = rev21ShotXByteOffsetInTraceHeaderExt3;
        c.rev21ShotYByteOffsetInTraceHeaderExt3 = rev21ShotYByteOffsetInTraceHeaderExt3;
        c.rev21ShotElevByteOffsetInTraceHeaderExt3 = rev21ShotElevByteOffsetInTraceHeaderExt3;
        c.rev21ShotLineByteOffsetInGh3 = rev21ShotLineByteOffsetInGh3;
        c.rev21ShotStnByteOffsetInGh3 = rev21ShotStnByteOffsetInGh3;
        c.rev21FfidByteOffsetInTraceHeader = rev21FfidByteOffsetInTraceHeader;
        c.rev21ExtendedFileNumberByteOffsetInTraceHeader = rev21ExtendedFileNumberByteOffsetInTraceHeader;
        c.samplesFieldByteOffsetInChannelSetDescriptor = samplesFieldByteOffsetInChannelSetDescriptor;
        c.traceHeaderSchema = traceHeaderSchema.copy();
        return c;
    }

    /** overwrites this config's fields in place from another (used when loading settings from XML into a live, shared config) */
    public void copyFrom(SegdConfig other)
    {
        this.version = other.version;
        this.generalHeaderBlockBytes = other.generalHeaderBlockBytes;
        this.traceHeaderBytes = other.traceHeaderBytes;
        this.traceHeaderExtensionBytes = other.traceHeaderExtensionBytes;
        this.fileNumberByteOffset = other.fileNumberByteOffset;
        this.formatCodeByteOffset = other.formatCodeByteOffset;
        this.channelSetsPerScanTypeByteOffset = other.channelSetsPerScanTypeByteOffset;
        this.additionalGeneralHeaderBlocksByteOffset = other.additionalGeneralHeaderBlocksByteOffset;
        this.baseScanIntervalByteOffset = other.baseScanIntervalByteOffset;
        this.traceHeaderExtensionCountByteOffset = other.traceHeaderExtensionCountByteOffset;
        this.recordYearByteOffset = other.recordYearByteOffset;
        this.recordJulianDayByteOffset = other.recordJulianDayByteOffset;
        this.recordHourByteOffset = other.recordHourByteOffset;
        this.recordMinuteByteOffset = other.recordMinuteByteOffset;
        this.recordSecondByteOffset = other.recordSecondByteOffset;
        this.extendedHeaderBlocksByteOffsetInHeader2 = other.extendedHeaderBlocksByteOffsetInHeader2;
        this.externalHeaderBlocksByteOffsetInHeader2 = other.externalHeaderBlocksByteOffsetInHeader2;
        this.rev3AdditionalBlocksCountByteOffsetInHeader2 = other.rev3AdditionalBlocksCountByteOffsetInHeader2;
        this.rev3DominantSamplingIntervalByteOffsetInHeader2 = other.rev3DominantSamplingIntervalByteOffsetInHeader2;
        this.rev3ExtendedHeaderBlocksByteOffsetInHeader2 = other.rev3ExtendedHeaderBlocksByteOffsetInHeader2;
        this.rev3ExternalHeaderBlocksByteOffsetInHeader2 = other.rev3ExternalHeaderBlocksByteOffsetInHeader2;
        this.rev3HeaderSizeByteOffsetInHeader3 = other.rev3HeaderSizeByteOffsetInHeader3;
        this.rev3TraceHeaderExtensionCountByteOffset = other.rev3TraceHeaderExtensionCountByteOffset;
        this.rev3NumSamplesByteOffsetInTraceHeaderExt1 = other.rev3NumSamplesByteOffsetInTraceHeaderExt1;
        this.rev21ChannelSetsPerScanTypeByteOffset = other.rev21ChannelSetsPerScanTypeByteOffset;
        this.rev21BaseScanIntervalByteOffset = other.rev21BaseScanIntervalByteOffset;
        this.rev21ExtendedHeaderLengthByteOffset = other.rev21ExtendedHeaderLengthByteOffset;
        this.rev21ExternalHeaderLengthByteOffset = other.rev21ExternalHeaderLengthByteOffset;
        this.rev21TraceHeaderExtensionCountByteOffset = other.rev21TraceHeaderExtensionCountByteOffset;
        this.rev21NumSamplesByteOffsetInTraceHeaderExt1 = other.rev21NumSamplesByteOffsetInTraceHeaderExt1;
        this.rev21ChanByteOffsetInTraceHeaderExt2 = other.rev21ChanByteOffsetInTraceHeaderExt2;
        this.rev21RecXByteOffsetInTraceHeaderExt3 = other.rev21RecXByteOffsetInTraceHeaderExt3;
        this.rev21RecYByteOffsetInTraceHeaderExt3 = other.rev21RecYByteOffsetInTraceHeaderExt3;
        this.rev21RecElevByteOffsetInTraceHeaderExt3 = other.rev21RecElevByteOffsetInTraceHeaderExt3;
        this.rev21ShotXByteOffsetInTraceHeaderExt3 = other.rev21ShotXByteOffsetInTraceHeaderExt3;
        this.rev21ShotYByteOffsetInTraceHeaderExt3 = other.rev21ShotYByteOffsetInTraceHeaderExt3;
        this.rev21ShotElevByteOffsetInTraceHeaderExt3 = other.rev21ShotElevByteOffsetInTraceHeaderExt3;
        this.rev21ShotLineByteOffsetInGh3 = other.rev21ShotLineByteOffsetInGh3;
        this.rev21ShotStnByteOffsetInGh3 = other.rev21ShotStnByteOffsetInGh3;
        this.rev21FfidByteOffsetInTraceHeader = other.rev21FfidByteOffsetInTraceHeader;
        this.rev21ExtendedFileNumberByteOffsetInTraceHeader = other.rev21ExtendedFileNumberByteOffsetInTraceHeader;
        this.samplesFieldByteOffsetInChannelSetDescriptor = other.samplesFieldByteOffsetInChannelSetDescriptor;
        // mutate the existing schema's field list in place (rather than replacing the object) so
        // any HeaderSchemaEditorPanel/HeaderSchemaTableModel already bound to it picks up the change
        this.traceHeaderSchema.getFields().clear();
        for (HeaderFieldDef f : other.traceHeaderSchema.getFields())
        {
            this.traceHeaderSchema.getFields().add(f.copy());
        }
    }
}
