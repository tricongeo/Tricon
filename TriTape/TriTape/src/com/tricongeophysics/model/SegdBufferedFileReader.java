package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Reads a SEG-D field file (rev 1/2 style fixed-length headers) in buffered
 * batches of traces.
 *
 * SEG-D varies significantly by revision and recording-system vendor. This is
 * a best-effort, simplified implementation covering the common structure:
 *   General Header block 1 (32 bytes)
 *   General Header block 2..N (32 bytes each, count from header 1)
 *   Extended header blocks + External header blocks (32 bytes each,
 *     counts from general header block 2)
 *   Channel set descriptor blocks (32 bytes each, one per channel set)
 *   repeating Trace blocks: Trace Header (20 bytes)
 *     + Trace Header Extension blocks (32 bytes each, count from header 1)
 *     + trace samples
 *
 * If a file uses a header layout that differs from this, the offsets in
 * readGeneralHeader1()/readGeneralHeader2()/readChannelSetDescriptor() will
 * need to be adjusted for that vendor's exact format.
 */
public class SegdBufferedFileReader extends BufferedFileReader
{
    private static final int GENERAL_HEADER_BLOCK_BYTES = 32;
    private static final int TRACE_HEADER_BYTES = 20;
    private static final int TRACE_HEADER_EXTENSION_BYTES = 32;
    public static final int FORMAT_IEEE_FLOAT_8058 = 8058;

    public static final String[] HEADER_NAMES =
    {
        "FFID", "SCAN_TYPE", "CHANNEL_SET", "CHANNEL_NUMBER", "TRACE_EDIT"
    };

    private RandomAccessFile file;
    private int formatCode;
    private int additionalGeneralHeaderBlocks;
    private int traceHeaderExtensionBlocks;
    private int channelSetsPerScanType;
    private int fileNumber;
    private long firstTraceOffset;

    public SegdBufferedFileReader(String filename)
    {
        super(filename);
    }

    @Override
    protected void doOpen() throws IOException
    {
        file = new RandomAccessFile(filename, "r");
        totalBytes = file.length();

        byte[] gh1 = new byte[GENERAL_HEADER_BLOCK_BYTES];
        file.readFully(gh1);
        readGeneralHeader1(gh1);

        int extendedHeaderBlocks = 0;
        int externalHeaderBlocks = 0;
        if (additionalGeneralHeaderBlocks >= 1)
        {
            byte[] gh2 = new byte[GENERAL_HEADER_BLOCK_BYTES];
            file.readFully(gh2);
            extendedHeaderBlocks = gh2[8] & 0xFF;
            externalHeaderBlocks = gh2[9] & 0xFF;
        }
        //skip any further general header blocks beyond block 2
        for (int i = 1; i < additionalGeneralHeaderBlocks; i++)
        {
            file.skipBytes(GENERAL_HEADER_BLOCK_BYTES);
        }
        file.skipBytes(extendedHeaderBlocks * GENERAL_HEADER_BLOCK_BYTES);
        file.skipBytes(externalHeaderBlocks * GENERAL_HEADER_BLOCK_BYTES);

        //channel set descriptor blocks -> pull sample count / sample interval from the first one
        int nChannelSets = Math.max(1, channelSetsPerScanType);
        for (int i = 0; i < nChannelSets; i++)
        {
            byte[] csd = new byte[GENERAL_HEADER_BLOCK_BYTES];
            file.readFully(csd);
            if (i == 0)
            {
                readChannelSetDescriptor(csd);
            }
        }

        if (samplesPerTrace <= 0)
        {
            throw new IOException("SegdBufferedFileReader: could not determine samples-per-trace from headers");
        }

        firstTraceOffset = file.getFilePointer();
    }

    private void readGeneralHeader1(byte[] gh1)
    {
        fileNumber = bcdToInt(gh1, 0, 2);
        formatCode = bcdToInt(gh1, 2, 2);
        channelSetsPerScanType = gh1[9] & 0xFF;
        additionalGeneralHeaderBlocks = (gh1[11] >> 4) & 0x0F;
        int baseScanInterval = gh1[27] & 0xFF;      //value/16 = sample interval in ms
        double sampleIntervalMs = baseScanInterval / 16.0;
        sampleRateMicros = (int) Math.round(sampleIntervalMs * 1000.0);
        traceHeaderExtensionBlocks = gh1[29] & 0xFF;
    }

    private void readChannelSetDescriptor(byte[] csd)
    {
        samplesPerTrace = ((csd[24] & 0xFF) << 8) | (csd[25] & 0xFF);
    }

    @Override
    protected SeismicTrace[] doReadNextTraces(int batchSize) throws IOException
    {
        ArrayList<SeismicTrace> list = new ArrayList<SeismicTrace>(batchSize);
        for (int i = 0; i < batchSize; i++)
        {
            if (file.getFilePointer() >= totalBytes) break;
            try
            {
                list.add(readOneTrace());
            }
            catch (EOFException eof)
            {
                break;
            }
        }
        return list.toArray(new SeismicTrace[0]);
    }

    private SeismicTrace readOneTrace() throws IOException
    {
        byte[] th = new byte[TRACE_HEADER_BYTES];
        file.readFully(th);

        double[] headers = new double[HEADER_NAMES.length];
        headers[0] = bcdToInt(th, 0, 3);          //field record number
        headers[1] = th[3] & 0xFF;                //scan type number
        headers[2] = th[4] & 0xFF;                //channel set number
        headers[3] = ((th[5] & 0xFF) << 8) | (th[6] & 0xFF); //channel number
        headers[4] = th[10] & 0xFF;               //trace edit code

        if (traceHeaderExtensionBlocks > 0)
        {
            file.skipBytes(traceHeaderExtensionBlocks * TRACE_HEADER_EXTENSION_BYTES);
        }

        float[] data = new float[samplesPerTrace];
        byte[] samples = new byte[samplesPerTrace * 4]; //assume 4-byte samples (IEEE float, format 8058)
        file.readFully(samples);
        for (int i = 0; i < samplesPerTrace; i++)
        {
            int bits = SegyBufferedFileReader.readInt(samples, i * 4);
            data[i] = (formatCode == FORMAT_IEEE_FLOAT_8058)
                ? Float.intBitsToFloat(bits)
                : SegyBufferedFileReader.ibmToFloat(bits); //best-effort fallback for other format codes
        }

        SeismicTrace trace = new SeismicTrace();
        trace.setHeaderList(HEADER_NAMES.clone());
        trace.setHeaders(headers);
        trace.setData(data);
        return trace;
    }

    @Override
    public boolean hasMoreTraces()
    {
        try
        {
            return file != null && file.getFilePointer() < totalBytes;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    @Override
    public void rewindToFirstTrace() throws IOException
    {
        file.seek(firstTraceOffset);
        tracesRead = 0;
    }

    @Override
    public long getBytesRead()
    {
        try
        {
            return file == null ? 0 : file.getFilePointer();
        }
        catch (IOException e)
        {
            return 0;
        }
    }

    public int getFormatCode() { return formatCode; }
    public int getFileNumber() { return fileNumber; }

    @Override
    public void close() throws IOException
    {
        if (file != null) file.close();
    }

    /** decodes numBytes of packed binary-coded-decimal (2 digits per byte) into an int */
    static int bcdToInt(byte[] b, int offset, int numBytes)
    {
        int value = 0;
        for (int i = 0; i < numBytes; i++)
        {
            int by = b[offset + i] & 0xFF;
            int hi = (by >> 4) & 0x0F;
            int lo = by & 0x0F;
            value = value * 100 + hi * 10 + lo;
        }
        return value;
    }
}
