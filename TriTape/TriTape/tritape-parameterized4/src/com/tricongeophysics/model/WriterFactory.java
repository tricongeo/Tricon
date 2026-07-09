package com.tricongeophysics.model;

/** factory pattern: creates the correct TraceWriter for a given output file format + config */
public final class WriterFactory
{
    private WriterFactory() {}

    public static TraceWriter create(FileFormat format)
    {
        return create(format, null, null);
    }

    public static TraceWriter create(FileFormat format, SegyConfig segyConfig, SegdConfig segdConfig)
    {
        switch (format)
        {
            case SEGY: return new SegyWriter(segyConfig != null ? segyConfig : new SegyConfig());
            case SEGD: return new SegdWriter(segdConfig != null ? segdConfig : new SegdConfig());
            default:   throw new IllegalArgumentException("unsupported file format: " + format);
        }
    }
}
