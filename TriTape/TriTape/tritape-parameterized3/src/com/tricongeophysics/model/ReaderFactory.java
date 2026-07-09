package com.tricongeophysics.model;

/** factory pattern: creates the correct BufferedFileReader for a given file format + config */
public final class ReaderFactory
{
    private ReaderFactory() {}

    public static BufferedFileReader create(FileFormat format, String filename)
    {
        return create(format, filename, null, null);
    }

    public static BufferedFileReader create(FileFormat format, String filename, SegyConfig segyConfig, SegdConfig segdConfig)
    {
        switch (format)
        {
            case SEGY: return new SegyBufferedFileReader(filename, segyConfig != null ? segyConfig : new SegyConfig());
            case SEGD: return new SegdBufferedFileReader(filename, segdConfig != null ? segdConfig : new SegdConfig());
            default:   throw new IllegalArgumentException("unsupported file format: " + format);
        }
    }
}
