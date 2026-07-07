package com.tricongeophysics.model;

/** factory pattern: creates the correct BufferedFileReader for a given file format */
public final class ReaderFactory
{
    private ReaderFactory() {}

    public static BufferedFileReader create(FileFormat format, String filename)
    {
        switch (format)
        {
            case SEGY: return new SegyBufferedFileReader(filename);
            case SEGD: return new SegdBufferedFileReader(filename);
            default:   throw new IllegalArgumentException("unsupported file format: " + format);
        }
    }
}
