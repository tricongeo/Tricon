package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Writes traces out as a standard SEG-Y file: 3200 byte ASCII textual header,
 * 400 byte binary header, then 240 byte trace header + IEEE-float (format 5)
 * samples per trace.
 */
public class SegyWriter implements TraceWriter
{
    private RandomAccessFile file;
    private WriterConfig config;
    private int traceCounter = 0;

    @Override
    public void open(String filename, WriterConfig config) throws IOException
    {
        this.config = config;
        file = new RandomAccessFile(filename, "rw");
        file.setLength(0); //overwrite if it already exists
        writeTextualHeader(config.textualHeader);
        writeBinaryHeader(config);
    }

    private void writeTextualHeader(String text) throws IOException
    {
        byte[] raw = new byte[3200];
        java.util.Arrays.fill(raw, (byte) ' ');
        byte[] textBytes = text.getBytes();
        System.arraycopy(textBytes, 0, raw, 0, Math.min(textBytes.length, raw.length));
        file.write(raw);
    }

    private void writeBinaryHeader(WriterConfig config) throws IOException
    {
        byte[] raw = new byte[400];
        writeUShort(raw, 16, config.sampleRateMicros);
        writeUShort(raw, 20, config.samplesPerTrace);
        writeUShort(raw, 24, SegyBufferedFileReader.FORMAT_IEEE_FLOAT);
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
        byte[] hdr = new byte[SegyBufferedFileReader.TRACE_HEADER_BYTES];
        traceCounter++;
        writeInt(hdr, 0, traceCounter);
        writeInt(hdr, 4, traceCounter);

        String[] names = trace.getHeaderList();
        double[] values = trace.getHeaders();
        for (int i = 0; i < names.length && i < SegyBufferedFileReader.HEADER_NAMES.length; i++)
        {
            writeNamedHeader(hdr, names[i] == null ? "" : names[i].trim(), values[i]);
        }
        writeUShort(hdr, 114, trace.getData().length);
        file.write(hdr);

        float[] data = trace.getData();
        byte[] samples = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++)
        {
            writeInt(samples, i * 4, Float.floatToIntBits(data[i]));
        }
        file.write(samples);
    }

    private void writeNamedHeader(byte[] hdr, String name, double value)
    {
        int v = (int) Math.round(value);
        if (name.equals("FFID")) writeInt(hdr, 8, v);
        else if (name.equals("TRACENO")) writeInt(hdr, 12, v);
        else if (name.equals("CDP")) writeInt(hdr, 20, v);
        else if (name.equals("CDP_TR")) writeInt(hdr, 24, v);
        else if (name.equals("OFFSET")) writeInt(hdr, 36, v);
        else if (name.equals("SOURCE_X")) writeInt(hdr, 72, v);
        else if (name.equals("SOURCE_Y")) writeInt(hdr, 76, v);
        else if (name.equals("REC_X")) writeInt(hdr, 80, v);
        else if (name.equals("REC_Y")) writeInt(hdr, 84, v);
        else if (name.equals("CDP_X")) writeInt(hdr, 180, v);
        else if (name.equals("CDP_Y")) writeInt(hdr, 184, v);
        else if (name.equals("INLINE")) writeInt(hdr, 188, v);
        else if (name.equals("CROSSLINE")) writeInt(hdr, 192, v);
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

    private static void writeUShort(byte[] b, int offset, int v)
    {
        b[offset] = (byte) (v >>> 8);
        b[offset + 1] = (byte) v;
    }
}
