package com.tricongeophysics.model;

/**
 * Result of a lightweight, validation-free peek at a SEG-Y file's textual and
 * binary headers (see SegyBufferedFileReader.peekHeaders()). Used by
 * SegyHeaderPreviewPanel to let the user see the actual header contents
 * while they tune byte offsets, without requiring the file to fully validate
 * as openable for trace reading.
 */
public class SegyHeaderPreview
{
    public final String textualHeader;
    public final byte[] binaryHeaderRaw;
    public final int sampleRateMicros;
    public final int samplesPerTrace;
    public final int formatCode;

    public SegyHeaderPreview(String textualHeader, byte[] binaryHeaderRaw,
                              int sampleRateMicros, int samplesPerTrace, int formatCode)
    {
        this.textualHeader = textualHeader;
        this.binaryHeaderRaw = binaryHeaderRaw;
        this.sampleRateMicros = sampleRateMicros;
        this.samplesPerTrace = samplesPerTrace;
        this.formatCode = formatCode;
    }
}
