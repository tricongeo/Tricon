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
