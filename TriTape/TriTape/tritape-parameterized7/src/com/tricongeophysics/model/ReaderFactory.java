package com.tricongeophysics.model;

import java.util.List;

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

    /**
     * Same as create(format, filename, ...) but for one or more files that
     * should be read as a single continuous stream of traces (e.g. one file
     * per FFID). A single-element list behaves exactly like the single-file
     * overload; more than one is wrapped in a MultiFileBufferedFileReader.
     */
    public static BufferedFileReader create(FileFormat format, List<String> filenames, SegyConfig segyConfig, SegdConfig segdConfig)
    {
        if (filenames == null || filenames.isEmpty())
        {
            throw new IllegalArgumentException("no input files specified");
        }
        if (filenames.size() == 1)
        {
            return create(format, filenames.get(0), segyConfig, segdConfig);
        }
        return new MultiFileBufferedFileReader(filenames, format, segyConfig, segdConfig);
    }
}
