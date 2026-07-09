package com.tricongeophysics.model;

/**
 * Tunable byte-layout parameters for the simplified SEG-D reader/writer: where
 * the key general-header/channel-set-descriptor values live, and the full
 * trace-header field mapping (HeaderSchema). See SegdBufferedFileReader's
 * class javadoc for the structural assumptions these offsets describe.
 */
public class SegdConfig
{
    public int generalHeaderBlockBytes = 32;
    public int traceHeaderBytes = 20;
    public int traceHeaderExtensionBytes = 32;

    /** offsets are within General Header block 1 */
    public int fileNumberByteOffset = 0;                    //BCD, 2 bytes
    public int formatCodeByteOffset = 2;                     //BCD, 2 bytes
    public int channelSetsPerScanTypeByteOffset = 9;         //1 byte
    public int additionalGeneralHeaderBlocksByteOffset = 11; //high nibble of this byte
    public int baseScanIntervalByteOffset = 27;              //1 byte, value/16 = ms
    public int traceHeaderExtensionCountByteOffset = 29;     //1 byte

    /** offsets are within General Header block 2 (only read if additional header blocks >= 1) */
    public int extendedHeaderBlocksByteOffsetInHeader2 = 8;
    public int externalHeaderBlocksByteOffsetInHeader2 = 9;

    /** offset is within the 32-byte Channel Set Descriptor block */
    public int samplesFieldByteOffsetInChannelSetDescriptor = 24; //2-byte binary

    public HeaderSchema traceHeaderSchema = HeaderSchema.defaultSegdSchema();

    public SegdConfig copy()
    {
        SegdConfig c = new SegdConfig();
        c.generalHeaderBlockBytes = generalHeaderBlockBytes;
        c.traceHeaderBytes = traceHeaderBytes;
        c.traceHeaderExtensionBytes = traceHeaderExtensionBytes;
        c.fileNumberByteOffset = fileNumberByteOffset;
        c.formatCodeByteOffset = formatCodeByteOffset;
        c.channelSetsPerScanTypeByteOffset = channelSetsPerScanTypeByteOffset;
        c.additionalGeneralHeaderBlocksByteOffset = additionalGeneralHeaderBlocksByteOffset;
        c.baseScanIntervalByteOffset = baseScanIntervalByteOffset;
        c.traceHeaderExtensionCountByteOffset = traceHeaderExtensionCountByteOffset;
        c.extendedHeaderBlocksByteOffsetInHeader2 = extendedHeaderBlocksByteOffsetInHeader2;
        c.externalHeaderBlocksByteOffsetInHeader2 = externalHeaderBlocksByteOffsetInHeader2;
        c.samplesFieldByteOffsetInChannelSetDescriptor = samplesFieldByteOffsetInChannelSetDescriptor;
        c.traceHeaderSchema = traceHeaderSchema.copy();
        return c;
    }

    /** overwrites this config's fields in place from another (used when loading settings from XML into a live, shared config) */
    public void copyFrom(SegdConfig other)
    {
        this.generalHeaderBlockBytes = other.generalHeaderBlockBytes;
        this.traceHeaderBytes = other.traceHeaderBytes;
        this.traceHeaderExtensionBytes = other.traceHeaderExtensionBytes;
        this.fileNumberByteOffset = other.fileNumberByteOffset;
        this.formatCodeByteOffset = other.formatCodeByteOffset;
        this.channelSetsPerScanTypeByteOffset = other.channelSetsPerScanTypeByteOffset;
        this.additionalGeneralHeaderBlocksByteOffset = other.additionalGeneralHeaderBlocksByteOffset;
        this.baseScanIntervalByteOffset = other.baseScanIntervalByteOffset;
        this.traceHeaderExtensionCountByteOffset = other.traceHeaderExtensionCountByteOffset;
        this.extendedHeaderBlocksByteOffsetInHeader2 = other.extendedHeaderBlocksByteOffsetInHeader2;
        this.externalHeaderBlocksByteOffsetInHeader2 = other.externalHeaderBlocksByteOffsetInHeader2;
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
