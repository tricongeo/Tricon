package com.tricongeophysics.model;

/**
 * Result of a lightweight, validation-free peek at a SEG-Y file's textual and
 * binary headers (see SegyBufferedFileReader.peekHeaders()). Used by
 * SegyHeaderPreviewPanel to let the user see the actual header contents
 * while they tune byte offsets, without requiring the file to fully validate
 * as openable for trace reading. Also used by TraceMonitor to default an
 * output file's textual/binary headers to whatever was physically read from
 * the input file when both are SEG-Y (textualHeaderRaw/binaryHeaderRaw carry
 * the exact original bytes, so they can be written back through unchanged -
 * textualHeader is the decoded, human-readable form for display only).
 */
public class SegyHeaderPreview
{
    public final String textualHeader;
    public final byte[] textualHeaderRaw;
    public final byte[] binaryHeaderRaw;
    public final int sampleRateMicros;
    public final int samplesPerTrace;
    public final int formatCode;

    public SegyHeaderPreview(String textualHeader, byte[] textualHeaderRaw, byte[] binaryHeaderRaw,
                              int sampleRateMicros, int samplesPerTrace, int formatCode)
    {
        this.textualHeader = textualHeader;
        this.textualHeaderRaw = textualHeaderRaw;
        this.binaryHeaderRaw = binaryHeaderRaw;
        this.sampleRateMicros = sampleRateMicros;
        this.samplesPerTrace = samplesPerTrace;
        this.formatCode = formatCode;
    }
}
