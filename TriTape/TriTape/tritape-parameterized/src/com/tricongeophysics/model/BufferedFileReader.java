package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.IOException;

/**
 * Template-method base class for reading seismic trace files in buffered chunks.
 * Subclasses (SegyBufferedFileReader, SegdBufferedFileReader) implement the
 * format-specific file/trace header parsing and sample decoding; this class
 * defines the "read N traces at a time" contract used by TraceMonitor to feed
 * both the preview (TraceViewer) and the batch reformat pass.
 */
public abstract class BufferedFileReader implements AutoCloseable
{
    public static final int DEFAULT_BATCH_SIZE = 10000;

    protected final String filename;
    protected int sampleRateMicros;
    protected int samplesPerTrace;
    protected long totalBytes = -1;
    protected int tracesRead = 0;
    protected boolean initialized = false;

    protected BufferedFileReader(String filename)
    {
        this.filename = filename;
    }

    /** opens the file and parses the file-level (reel/general) headers. must be called first. */
    public final void open() throws IOException
    {
        doOpen();
        initialized = true;
    }

    /** reads up to batchSize traces from the current file position.
     *  returns fewer than batchSize (possibly zero) traces at end of file. */
    public final SeismicTrace[] readNextTraces(int batchSize) throws IOException
    {
        if (!initialized)
        {
            throw new IllegalStateException("open() must be called before reading traces");
        }
        SeismicTrace[] traces = doReadNextTraces(batchSize);
        tracesRead += traces.length;
        return traces;
    }

    public final SeismicTrace[] readNextTraces() throws IOException
    {
        return readNextTraces(DEFAULT_BATCH_SIZE);
    }

    /** returns true if there is at least one more trace to read */
    public abstract boolean hasMoreTraces();

    /** rewinds the file back to the first trace record (used before the batch reformat pass) */
    public abstract void rewindToFirstTrace() throws IOException;

    /** approximate progress indicator: bytes consumed so far in the file */
    public abstract long getBytesRead();

    public long getTotalBytes() { return totalBytes; }
    public int getSampleRateMicros() { return sampleRateMicros; }
    public int getSamplesPerTrace() { return samplesPerTrace; }
    public int getTracesRead() { return tracesRead; }
    public String getFilename() { return filename; }

    protected abstract void doOpen() throws IOException;

    protected abstract SeismicTrace[] doReadNextTraces(int batchSize) throws IOException;

    public abstract void close() throws IOException;
}
