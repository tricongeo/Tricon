package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a standard SEG-Y file (textual header + binary header + trace
 * headers/samples) in buffered batches of traces. The byte offsets used for
 * the binary-header values and the full trace-header field mapping are
 * driven by a SegyConfig, so a user can retarget this reader at files that
 * don't follow the exact rev-1 layout without touching code (see
 * SegySettingsPanel in the view package).
 */
public class SegyBufferedFileReader extends BufferedFileReader
{
    public static final int FORMAT_IBM_FLOAT = 1;
    public static final int FORMAT_INT32 = 2;
    public static final int FORMAT_INT16 = 3;
    public static final int FORMAT_IEEE_FLOAT = 5;
    public static final int FORMAT_INT8 = 8;

    private final SegyConfig config;
    private RandomAccessFile file;
    private String textualHeader;
    private byte[] binaryHeaderRaw;
    private int dataFormatCode;
    private long firstTraceOffset;

    public SegyBufferedFileReader(String filename)
    {
        this(filename, new SegyConfig());
    }

    public SegyBufferedFileReader(String filename, SegyConfig config)
    {
        super(filename);
        this.config = config;
    }

    public SegyConfig getConfig() { return config; }

    /**
     * Lightweight, validation-free peek at just a file's textual and binary
     * headers (no trace-header validation, unlike open()). Intended for a
     * settings/preview UI where the user is still tuning byte offsets and the
     * file may not yet parse cleanly enough for full reading.
     */
    public static SegyHeaderPreview peekHeaders(String filename, SegyConfig config) throws IOException
    {
        try (RandomAccessFile f = new RandomAccessFile(filename, "r"))
        {
            byte[] textRaw = new byte[config.textualHeaderBytes];
            f.seek(0);
            f.readFully(textRaw);
            boolean looksAscii = textRaw.length > 0 && textRaw[0] == 'C';
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < textRaw.length; i++)
            {
                int b = textRaw[i] & 0xFF;
                char c = looksAscii ? (char) b : SegyEbcdic.toAscii(b);
                sb.append(c);
                if ((i + 1) % 80 == 0) sb.append('\n');
            }

            byte[] binRaw = new byte[config.binaryHeaderBytes];
            f.seek(config.textualHeaderBytes);
            f.readFully(binRaw);
            int sampleRate = readUShort(binRaw, config.sampleRateByteOffset);
            int samplesPerTrace = readUShort(binRaw, config.samplesPerTraceByteOffset);
            int formatCode = readUShort(binRaw, config.formatCodeByteOffset);

            return new SegyHeaderPreview(sb.toString(), binRaw, sampleRate, samplesPerTrace, formatCode);
        }
    }

    @Override
    protected void doOpen() throws IOException
    {
        file = new RandomAccessFile(filename, "r");
        totalBytes = file.length();
        readTextualHeader();
        readBinaryHeader();
        firstTraceOffset = config.textualHeaderBytes + config.binaryHeaderBytes;
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
    public byte[] getBinaryHeaderRaw() { return binaryHeaderRaw; }
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
        byte[] raw = new byte[config.textualHeaderBytes];
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
        file.seek(config.textualHeaderBytes);
        byte[] raw = new byte[config.binaryHeaderBytes];
        file.readFully(raw);
        binaryHeaderRaw = raw;
        sampleRateMicros = readUShort(raw, config.sampleRateByteOffset);
        samplesPerTrace = readUShort(raw, config.samplesPerTraceByteOffset);
        dataFormatCode = readUShort(raw, config.formatCodeByteOffset);
        if (samplesPerTrace <= 0)
        {
            throw new IOException("SegyBufferedFileReader: invalid samples-per-trace in binary header");
        }
    }

    private SeismicTrace readOneTrace() throws IOException
    {
        byte[] hdr = new byte[config.traceHeaderBytes];
        file.readFully(hdr);

        double coordFactor = scalarToFactor((short) readShort(hdr, config.coordinateScalarByteOffset));
        double elevFactor = scalarToFactor((short) readShort(hdr, config.elevationScalarByteOffset));

        List<HeaderFieldDef> fields = config.traceHeaderSchema.getFields();
        String[] names = new String[fields.size()];
        double[] values = new double[fields.size()];
        for (int i = 0; i < fields.size(); i++)
        {
            HeaderFieldDef f = fields.get(i);
            double v = HeaderCodec.decode(hdr, f);
            if (f.getScalarType() == HeaderFieldDef.ScalarType.COORDINATE) v *= coordFactor;
            else if (f.getScalarType() == HeaderFieldDef.ScalarType.ELEVATION) v *= elevFactor;
            names[i] = f.getName();
            values[i] = v;
        }

        int traceSamples = readUShort(hdr, config.numSamplesThisTraceByteOffset);
        if (traceSamples <= 0) traceSamples = samplesPerTrace;

        float[] data = new float[traceSamples];
        byte[] samples = new byte[traceSamples * bytesPerSample()];
        file.readFully(samples);
        decodeSamples(samples, data);

        SeismicTrace trace = new SeismicTrace();
        trace.setHeaderList(names);
        trace.setHeaders(values);
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
