package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wraps a sequence of same-format files - e.g. field acquisition systems that
 * write one file per FFID/shot - as a single continuous BufferedFileReader.
 * Reads exhaust the current file's traces, then transparently move on to the
 * next file, using a fresh per-file reader (built via ReaderFactory, with the
 * same format/config for every file in the set). Sample rate and
 * samples/trace are taken from the first file and assumed consistent across
 * the whole set; progress (getBytesRead()/getTotalBytes()) is reported across
 * all files combined.
 */
public class MultiFileBufferedFileReader extends BufferedFileReader
{
    private final List<String> filenames;
    private final FileFormat format;
    private final SegyConfig segyConfig;
    private final SegdConfig segdConfig;

    private int currentIndex = -1;
    private BufferedFileReader currentReader;
    private long bytesReadInCompletedFiles = 0;

    public MultiFileBufferedFileReader(List<String> filenames, FileFormat format, SegyConfig segyConfig, SegdConfig segdConfig)
    {
        super(filenames.isEmpty() ? "" : filenames.get(0));
        this.filenames = new ArrayList<String>(filenames);
        this.format = format;
        this.segyConfig = segyConfig;
        this.segdConfig = segdConfig;
    }

    /** the full list of files this reader will read through, in order */
    public List<String> getFilenames()
    {
        return Collections.unmodifiableList(filenames);
    }

    @Override
    public String getFilename()
    {
        return filenames.size() == 1
            ? filenames.get(0)
            : filenames.size() + " files (starting with " + filenames.get(0) + ")";
    }

    @Override
    protected void doOpen() throws IOException
    {
        totalBytes = 0;
        for (String name : filenames)
        {
            totalBytes += new File(name).length();
        }
        openFileAt(0);
    }

    private void openFileAt(int index) throws IOException
    {
        if (currentReader != null)
        {
            bytesReadInCompletedFiles += currentReader.getBytesRead();
            currentReader.close();
            currentReader = null;
        }
        currentIndex = index;
        if (currentIndex >= filenames.size())
        {
            return; // no more files
        }
        currentReader = ReaderFactory.create(format, filenames.get(currentIndex), segyConfig, segdConfig);
        currentReader.open();
        if (currentIndex == 0)
        {
            sampleRateMicros = currentReader.getSampleRateMicros();
            samplesPerTrace = currentReader.getSamplesPerTrace();
        }
    }

    @Override
    protected SeismicTrace[] doReadNextTraces(int batchSize) throws IOException
    {
        List<SeismicTrace> collected = new ArrayList<SeismicTrace>(batchSize);
        while (collected.size() < batchSize && currentReader != null)
        {
            int remaining = batchSize - collected.size();
            SeismicTrace[] batch = currentReader.readNextTraces(remaining);
            for (SeismicTrace t : batch) collected.add(t);

            if (batch.length < remaining)
            {
                // current file exhausted; move on to the next one, if any
                if (currentIndex + 1 < filenames.size())
                {
                    openFileAt(currentIndex + 1);
                }
                else
                {
                    bytesReadInCompletedFiles += currentReader.getBytesRead();
                    currentReader.close();
                    currentReader = null;
                }
            }
        }
        return collected.toArray(new SeismicTrace[0]);
    }

    @Override
    public boolean hasMoreTraces()
    {
        if (currentReader != null && currentReader.hasMoreTraces()) return true;
        return currentIndex + 1 < filenames.size();
    }

    @Override
    public void rewindToFirstTrace() throws IOException
    {
        bytesReadInCompletedFiles = 0;
        tracesRead = 0;
        openFileAt(0);
    }

    @Override
    public long getBytesRead()
    {
        long current = currentReader == null ? 0 : currentReader.getBytesRead();
        return bytesReadInCompletedFiles + current;
    }

    @Override
    public void close() throws IOException
    {
        if (currentReader != null)
        {
            currentReader.close();
            currentReader = null;
        }
    }
}
