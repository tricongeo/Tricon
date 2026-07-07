package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Reads a standard SEG-Y file (3200 byte textual header, 400 byte binary header,
 * 240 byte trace headers + samples) in buffered batches of traces.
 *
 * NOTE: trace header byte offsets follow the SEG-Y rev 1 standard layout. Files
 * from a vendor with a non-standard layout may need the offsets in HEADER_NAMES /
 * readOneTrace() adjusted.
 */
public class SegyBufferedFileReader extends BufferedFileReader
{
    private static final int TEXTUAL_HEADER_BYTES = 3200;
    private static final int BINARY_HEADER_BYTES = 400;
    public static final int TRACE_HEADER_BYTES = 240;

    public static final int FORMAT_IBM_FLOAT = 1;
    public static final int FORMAT_INT32 = 2;
    public static final int FORMAT_INT16 = 3;
    public static final int FORMAT_IEEE_FLOAT = 5;
    public static final int FORMAT_INT8 = 8;

    /** header names/order written into every SeismicTrace's headerList & headers array */
    public static final String[] HEADER_NAMES =
    {
        "FFID", "TRACENO", "CDP", "CDP_TR", "OFFSET",
        "SOURCE_X", "SOURCE_Y", "REC_X", "REC_Y",
        "CDP_X", "CDP_Y", "INLINE", "CROSSLINE",
        "ELEV_SOURCE", "ELEV_REC", "WATER_DEPTH_SOURCE", "WATER_DEPTH_GROUP"
    };

    private RandomAccessFile file;
    private String textualHeader;
    private int dataFormatCode;
    private long firstTraceOffset;

    public SegyBufferedFileReader(String filename)
    {
        super(filename);
    }

    @Override
    protected void doOpen() throws IOException
    {
        file = new RandomAccessFile(filename, "r");
        totalBytes = file.length();
        readTextualHeader();
        readBinaryHeader();
        firstTraceOffset = TEXTUAL_HEADER_BYTES + BINARY_HEADER_BYTES;
        file.seek(firstTraceOffset);
    }

    @Override
    protected SeismicTrace[] doReadNextTraces(int batchSize) throws IOException
    {
        ArrayList<SeismicTrace> list = new ArrayList<SeismicTrace>(batchSize);
        for (int i = 0; i < batchSize; i++)
        {
            if (file.getFilePointer() >= totalBytes)
            {
                break;
            }
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

    public String getTextualHeader() { return textualHeader; }
    public int getDataFormatCode() { return dataFormatCode; }

    @Override
    public void close() throws IOException
    {
        if (file != null) file.close();
    }

    // ------------------------------------------------------------------

    private void readTextualHeader() throws IOException
    {
        file.seek(0);
        byte[] raw = new byte[TEXTUAL_HEADER_BYTES];
        file.readFully(raw);
        boolean looksAscii = raw.length > 0 && raw[0] == 'C';
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length; i++)
        {
            int b = raw[i] & 0xFF;
            char c = looksAscii ? (char) b : SegyEbcdic.toAscii(b);
            sb.append(c);
            if ((i + 1) % 80 == 0) sb.append('\n');
        }
        textualHeader = sb.toString();
    }

    private void readBinaryHeader() throws IOException
    {
        file.seek(TEXTUAL_HEADER_BYTES);
        byte[] raw = new byte[BINARY_HEADER_BYTES];
        file.readFully(raw);
        sampleRateMicros = readUShort(raw, 16);
        samplesPerTrace = readUShort(raw, 20);
        dataFormatCode = readUShort(raw, 24);
        if (samplesPerTrace <= 0)
        {
            throw new IOException("SegyBufferedFileReader: invalid samples-per-trace in binary header");
        }
    }

    private SeismicTrace readOneTrace() throws IOException
    {
        byte[] hdr = new byte[TRACE_HEADER_BYTES];
        file.readFully(hdr);

        double[] headers = new double[HEADER_NAMES.length];
        headers[0] = readInt(hdr, 8);
        headers[1] = readInt(hdr, 12);
        headers[2] = readInt(hdr, 20);
        headers[3] = readInt(hdr, 24);
        headers[4] = readInt(hdr, 36);

        double elevFactor = scalarToFactor((short) readShort(hdr, 68));
        double coordFactor = scalarToFactor((short) readShort(hdr, 70));

        headers[5] = readInt(hdr, 72) * coordFactor;
        headers[6] = readInt(hdr, 76) * coordFactor;
        headers[7] = readInt(hdr, 80) * coordFactor;
        headers[8] = readInt(hdr, 84) * coordFactor;
        headers[9] = readInt(hdr, 180) * coordFactor;
        headers[10] = readInt(hdr, 184) * coordFactor;
        headers[11] = readInt(hdr, 188);
        headers[12] = readInt(hdr, 192);
        headers[13] = readInt(hdr, 44) * elevFactor;
        headers[14] = readInt(hdr, 40) * elevFactor;
        headers[15] = readInt(hdr, 60) * elevFactor;
        headers[16] = readInt(hdr, 64) * elevFactor;

        int traceSamples = readUShort(hdr, 114);
        if (traceSamples <= 0) traceSamples = samplesPerTrace;

        float[] data = new float[traceSamples];
        byte[] samples = new byte[traceSamples * bytesPerSample()];
        file.readFully(samples);
        decodeSamples(samples, data);

        SeismicTrace trace = new SeismicTrace();
        trace.setHeaderList(HEADER_NAMES.clone());
        trace.setHeaders(headers);
        trace.setData(data);
        return trace;
    }

    private int bytesPerSample()
    {
        switch (dataFormatCode)
        {
            case FORMAT_IBM_FLOAT:  return 4;
            case FORMAT_INT32:      return 4;
            case FORMAT_INT16:      return 2;
            case FORMAT_IEEE_FLOAT: return 4;
            case FORMAT_INT8:       return 1;
            default:                return 4;
        }
    }

    private void decodeSamples(byte[] raw, float[] out)
    {
        int bps = bytesPerSample();
        for (int i = 0; i < out.length; i++)
        {
            int offset = i * bps;
            switch (dataFormatCode)
            {
                case FORMAT_IBM_FLOAT:  out[i] = ibmToFloat(readInt(raw, offset)); break;
                case FORMAT_INT32:      out[i] = readInt(raw, offset); break;
                case FORMAT_INT16:      out[i] = (short) readShort(raw, offset); break;
                case FORMAT_IEEE_FLOAT: out[i] = Float.intBitsToFloat(readInt(raw, offset)); break;
                case FORMAT_INT8:       out[i] = raw[offset]; break;
                default:                out[i] = ibmToFloat(readInt(raw, offset)); break;
            }
        }
    }

    static float ibmToFloat(int ibm)
    {
        int sign = (ibm >> 31) & 0x1;
        int exponent = (ibm >> 24) & 0x7F;
        int mantissa = ibm & 0x00FFFFFF;
        if (mantissa == 0) return 0.0f;
        double value = mantissa / (double) (1L << 24);
        value *= Math.pow(16.0, exponent - 64);
        return (float) (sign == 1 ? -value : value);
    }

    static double scalarToFactor(short scalar)
    {
        if (scalar == 0) return 1.0;
        if (scalar > 0) return scalar;
        return 1.0 / (-scalar);
    }

    static int readInt(byte[] b, int offset)
    {
        return ((b[offset] & 0xFF) << 24) | ((b[offset + 1] & 0xFF) << 16)
             | ((b[offset + 2] & 0xFF) << 8) | (b[offset + 3] & 0xFF);
    }

    static int readShort(byte[] b, int offset)
    {
        return (short) (((b[offset] & 0xFF) << 8) | (b[offset + 1] & 0xFF));
    }

    static int readUShort(byte[] b, int offset)
    {
        return ((b[offset] & 0xFF) << 8) | (b[offset + 1] & 0xFF);
    }
}
