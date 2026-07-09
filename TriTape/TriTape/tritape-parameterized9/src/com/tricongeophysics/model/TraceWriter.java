package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.IOException;

/** output-format strategy used by the batch reformat pass to write traces to disk */
public interface TraceWriter extends AutoCloseable
{
    void open(String filename, WriterConfig config) throws IOException;

    void writeTraces(SeismicTrace[] traces) throws IOException;

    @Override
    void close() throws IOException;
}
