package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Writes traces out as a SEG-Y file. Byte offsets for the binary-header
 * values and the trace-header field mapping come from a SegyConfig so the
 * output layout matches whatever the user configured (defaults to the SEG-Y
 * rev 1 standard layout). Coordinate/elevation values in SeismicTrace headers
 * are assumed already descaled (as produced by SegyBufferedFileReader), so
 * this writer always writes a scalar of 1 (no re-scaling on output).
 */
public class SegyWriter implements TraceWriter
{
    private final SegyConfig config;
    private RandomAccessFile file;
    private int traceCounter = 0;

    public SegyWriter()
    {
        this(new SegyConfig());
    }

    public SegyWriter(SegyConfig config)
    {
        this.config = config;
    }

    @Override
    public void open(String filename, WriterConfig writerConfig) throws IOException
    {
        file = new RandomAccessFile(filename, "rw");
        file.setLength(0); //overwrite if it already exists
        writeTextualHeader(writerConfig.textualHeader);
        writeBinaryHeader(writerConfig);
    }

    private void writeTextualHeader(String text) throws IOException
    {
        byte[] raw = new byte[config.textualHeaderBytes];
        java.util.Arrays.fill(raw, (byte) ' ');
        byte[] textBytes = text.getBytes();
        System.arraycopy(textBytes, 0, raw, 0, Math.min(textBytes.length, raw.length));
        file.write(raw);
    }

    private void writeBinaryHeader(WriterConfig writerConfig) throws IOException
    {
        byte[] raw = new byte[config.binaryHeaderBytes];
        writeUShort(raw, config.sampleRateByteOffset, writerConfig.sampleRateMicros);
        writeUShort(raw, config.samplesPerTraceByteOffset, writerConfig.samplesPerTrace);
        writeUShort(raw, config.formatCodeByteOffset, SegyBufferedFileReader.FORMAT_IEEE_FLOAT);
        file.write(raw);
    }

    @Override
    public void writeTraces(SeismicTrace[] traces) throws IOException
    {
        for (SeismicTrace trace : traces)
        {
            writeOneTrace(trace);
        }
    }

    private void writeOneTrace(SeismicTrace trace) throws IOException
    {
        byte[] hdr = new byte[config.traceHeaderBytes];
        traceCounter++;
        writeInt(hdr, 0, traceCounter);
        writeInt(hdr, 4, traceCounter);
        writeShort(hdr, config.coordinateScalarByteOffset, (short) 1);
        writeShort(hdr, config.elevationScalarByteOffset, (short) 1);

        String[] names = trace.getHeaderList();
        double[] values = trace.getHeaders();
        List<HeaderFieldDef> fields = config.traceHeaderSchema.getFields();
        for (HeaderFieldDef f : fields)
        {
            double value = findHeaderValue(names, values, f.getName());
            HeaderCodec.encode(hdr, f, value);
        }
        writeUShort(hdr, config.numSamplesThisTraceByteOffset, trace.getData().length);
        file.write(hdr);

        float[] data = trace.getData();
        byte[] samples = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++)
        {
            writeInt(samples, i * 4, Float.floatToIntBits(data[i]));
        }
        file.write(samples);
    }

    private static double findHeaderValue(String[] names, double[] values, String name)
    {
        for (int i = 0; i < names.length; i++)
        {
            String n = names[i] == null ? "" : names[i].trim();
            if (name.equals(n)) return values[i];
        }
        return 0.0;
    }

    @Override
    public void close() throws IOException
    {
        if (file != null) file.close();
    }

    private static void writeInt(byte[] b, int offset, int v)
    {
        b[offset] = (byte) (v >>> 24);
        b[offset + 1] = (byte) (v >>> 16);
        b[offset + 2] = (byte) (v >>> 8);
        b[offset + 3] = (byte) v;
    }

    private static void writeShort(byte[] b, int offset, short v)
    {
        b[offset] = (byte) (v >> 8);
        b[offset + 1] = (byte) v;
    }

    private static void writeUShort(byte[] b, int offset, int v)
    {
        b[offset] = (byte) (v >>> 8);
        b[offset + 1] = (byte) v;
    }
}
