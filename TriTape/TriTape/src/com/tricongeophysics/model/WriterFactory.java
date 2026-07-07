package com.tricongeophysics.model;

/** factory pattern: creates the correct TraceWriter for a given output file format */
public final class WriterFactory
{
    private WriterFactory() {}

    public static TraceWriter create(FileFormat format)
    {
        switch (format)
        {
            case SEGY: return new SegyWriter();
            case SEGD: return new SegdWriter();
            default:   throw new IllegalArgumentException("unsupported file format: " + format);
        }
    }
}
