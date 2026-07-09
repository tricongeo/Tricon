package com.tricongeophysics.model;

/**
 * Tunable byte-layout parameters for SEG-Y reading/writing: where the key
 * binary-header values live, where the coordinate/elevation scalars live in
 * the trace header, and the full trace-header field mapping (HeaderSchema).
 * Edited from the GUI via SegySettingsPanel (embedded inline in TraceMonitor)
 * and passed into
 * ReaderFactory/WriterFactory so every SegyBufferedFileReader/SegyWriter uses
 * the same, user-specified layout.
 */
public class SegyConfig
{
    public int textualHeaderBytes = 3200;
    public int binaryHeaderBytes = 400;
    public int traceHeaderBytes = 240;

    /** offsets are within the 400-byte binary header */
    public int sampleRateByteOffset = 16;
    public int samplesPerTraceByteOffset = 20;
    public int formatCodeByteOffset = 24;

    /** offsets are within each 240-byte trace header */
    public int numSamplesThisTraceByteOffset = 114;
    public int coordinateScalarByteOffset = 70;
    public int elevationScalarByteOffset = 68;

    public HeaderSchema traceHeaderSchema = HeaderSchema.defaultSegySchema();

    public SegyConfig copy()
    {
        SegyConfig c = new SegyConfig();
        c.textualHeaderBytes = textualHeaderBytes;
        c.binaryHeaderBytes = binaryHeaderBytes;
        c.traceHeaderBytes = traceHeaderBytes;
        c.sampleRateByteOffset = sampleRateByteOffset;
        c.samplesPerTraceByteOffset = samplesPerTraceByteOffset;
        c.formatCodeByteOffset = formatCodeByteOffset;
        c.numSamplesThisTraceByteOffset = numSamplesThisTraceByteOffset;
        c.coordinateScalarByteOffset = coordinateScalarByteOffset;
        c.elevationScalarByteOffset = elevationScalarByteOffset;
        c.traceHeaderSchema = traceHeaderSchema.copy();
        return c;
    }

    /** overwrites this config's fields in place from another (used when loading settings from XML into a live, shared config) */
    public void copyFrom(SegyConfig other)
    {
        this.textualHeaderBytes = other.textualHeaderBytes;
        this.binaryHeaderBytes = other.binaryHeaderBytes;
        this.traceHeaderBytes = other.traceHeaderBytes;
        this.sampleRateByteOffset = other.sampleRateByteOffset;
        this.samplesPerTraceByteOffset = other.samplesPerTraceByteOffset;
        this.formatCodeByteOffset = other.formatCodeByteOffset;
        this.numSamplesThisTraceByteOffset = other.numSamplesThisTraceByteOffset;
        this.coordinateScalarByteOffset = other.coordinateScalarByteOffset;
        this.elevationScalarByteOffset = other.elevationScalarByteOffset;
        // mutate the existing schema's field list in place (rather than replacing the object) so
        // any HeaderSchemaEditorPanel/HeaderSchemaTableModel already bound to it picks up the change
        this.traceHeaderSchema.getFields().clear();
        for (HeaderFieldDef f : other.traceHeaderSchema.getFields())
        {
            this.traceHeaderSchema.getFields().add(f.copy());
        }
    }
}
