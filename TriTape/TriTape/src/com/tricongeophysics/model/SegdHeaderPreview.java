package com.tricongeophysics.model;

/**
 * Result of a lightweight diagnostic peek at a SEG-D file's key header blocks
 * (General Header 1/2/3, and the first trace's header + Trace Header
 * Extension #1), used by SegdHeaderPreviewPanel to show raw bytes alongside
 * decoded values so the byte-offset assumptions in SegdConfig can be checked
 * against an actual file - important for SEG-D since revisions and vendor
 * implementations vary so much that any fixed set of offsets is a best-effort
 * starting point, not a guarantee. Fields that aren't applicable/reachable for
 * a given file/version are left at their default (-1 for ints/longs, null for
 * byte arrays) rather than causing an error.
 */
public class SegdHeaderPreview
{
    public final byte[] generalHeader1Raw;
    public final byte[] generalHeader2Raw;
    public final byte[] generalHeader3Raw;
    public final byte[] firstTraceHeaderRaw;
    public final byte[] firstTraceHeaderExt1Raw;

    public final int fileNumber;
    public final int formatCode;
    public final int additionalGeneralHeaderBlocksNibble;
    public final int channelSetsPerScanType;
    public final int sampleRateMicrosFromHeader1;

    /** Rev 3.1 only; -1 if not applicable/available */
    public final int trueAdditionalBlocks;
    public final int dominantSamplingIntervalMicros;
    public final int extendedHeaderBlocks;
    public final int externalHeaderBlocks;
    public final long headerSizeOffset;
    public final int firstTraceExtensionCount;
    public final int firstTraceNumSamples;

    public SegdHeaderPreview(byte[] generalHeader1Raw, byte[] generalHeader2Raw, byte[] generalHeader3Raw,
                              byte[] firstTraceHeaderRaw, byte[] firstTraceHeaderExt1Raw,
                              int fileNumber, int formatCode, int additionalGeneralHeaderBlocksNibble,
                              int channelSetsPerScanType, int sampleRateMicrosFromHeader1,
                              int trueAdditionalBlocks, int dominantSamplingIntervalMicros,
                              int extendedHeaderBlocks, int externalHeaderBlocks, long headerSizeOffset,
                              int firstTraceExtensionCount, int firstTraceNumSamples)
    {
        this.generalHeader1Raw = generalHeader1Raw;
        this.generalHeader2Raw = generalHeader2Raw;
        this.generalHeader3Raw = generalHeader3Raw;
        this.firstTraceHeaderRaw = firstTraceHeaderRaw;
        this.firstTraceHeaderExt1Raw = firstTraceHeaderExt1Raw;
        this.fileNumber = fileNumber;
        this.formatCode = formatCode;
        this.additionalGeneralHeaderBlocksNibble = additionalGeneralHeaderBlocksNibble;
        this.channelSetsPerScanType = channelSetsPerScanType;
        this.sampleRateMicrosFromHeader1 = sampleRateMicrosFromHeader1;
        this.trueAdditionalBlocks = trueAdditionalBlocks;
        this.dominantSamplingIntervalMicros = dominantSamplingIntervalMicros;
        this.extendedHeaderBlocks = extendedHeaderBlocks;
        this.externalHeaderBlocks = externalHeaderBlocks;
        this.headerSizeOffset = headerSizeOffset;
        this.firstTraceExtensionCount = firstTraceExtensionCount;
        this.firstTraceNumSamples = firstTraceNumSamples;
    }
}
