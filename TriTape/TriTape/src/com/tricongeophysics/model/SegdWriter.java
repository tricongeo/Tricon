package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Writes traces out as a simplified SEG-D file matching the structure that
 * SegdBufferedFileReader can read back: General Header block 1 + block 2,
 * one Channel Set Descriptor block, then repeating 20-byte Trace Headers
 * (no trace header extensions) followed by 32-bit IEEE float samples
 * (format code 8058). See SegdBufferedFileReader's class javadoc for the
 * same caveats about SEG-D revision/vendor variability.
 */
public class SegdWriter implements TraceWriter
{
    private RandomAccessFile file;
    private int fileNumberCounter = 1;

    @Override
    public void open(String filename, WriterConfig config) throws IOException
    {
        file = new RandomAccessFile(filename, "rw");
        file.setLength(0);

        byte[] gh1 = new byte[32];
        writeBcd(gh1, 0, 2, 1);                       //file number
        writeBcd(gh1, 2, 2, SegdBufferedFileReader.FORMAT_IEEE_FLOAT_8058); //format code 8058
        gh1[9] = 1;                                    //channel sets per scan type
        gh1[11] = (byte) (1 << 4);                      //1 additional general header block
        int baseScanInterval = (int) Math.round((config.sampleRateMicros / 1000.0) * 16.0);
        gh1[27] = (byte) baseScanInterval;
        gh1[29] = 0;                                    //0 trace header extension blocks
        file.write(gh1);

        byte[] gh2 = new byte[32];
        gh2[8] = 0; //extended header blocks
        gh2[9] = 0; //external header blocks
        file.write(gh2);

        byte[] csd = new byte[32];
        csd[24] = (byte) ((config.samplesPerTrace >> 8) & 0xFF);
        csd[25] = (byte) (config.samplesPerTrace & 0xFF);
        file.write(csd);
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
        byte[] th = new byte[20];
        String[] names = trace.getHeaderList();
        double[] values = trace.getHeaders();
        int ffid = fileNumberCounter, scanType = 1, chanSet = 1, chanNum = 1, edit = 0;
        for (int i = 0; i < names.length && i < SegdBufferedFileReader.HEADER_NAMES.length; i++)
        {
            String n = names[i] == null ? "" : names[i].trim();
            if (n.equals("FFID")) ffid = (int) values[i];
            else if (n.equals("SCAN_TYPE")) scanType = (int) values[i];
            else if (n.equals("CHANNEL_SET")) chanSet = (int) values[i];
            else if (n.equals("CHANNEL_NUMBER")) chanNum = (int) values[i];
            else if (n.equals("TRACE_EDIT")) edit = (int) values[i];
        }
        writeBcd(th, 0, 3, ffid);
        th[3] = (byte) scanType;
        th[4] = (byte) chanSet;
        th[5] = (byte) ((chanNum >> 8) & 0xFF);
        th[6] = (byte) (chanNum & 0xFF);
        th[10] = (byte) edit;
        file.write(th);

        float[] data = trace.getData();
        byte[] samples = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++)
        {
            int bits = Float.floatToIntBits(data[i]);
            samples[i * 4] = (byte) (bits >>> 24);
            samples[i * 4 + 1] = (byte) (bits >>> 16);
            samples[i * 4 + 2] = (byte) (bits >>> 8);
            samples[i * 4 + 3] = (byte) bits;
        }
        file.write(samples);
        fileNumberCounter++;
    }

    @Override
    public void close() throws IOException
    {
        if (file != null) file.close();
    }

    private static void writeBcd(byte[] b, int offset, int numBytes, int value)
    {
        for (int i = numBytes - 1; i >= 0; i--)
        {
            int digitsPair = value % 100;
            value /= 100;
            int hi = digitsPair / 10;
            int lo = digitsPair % 10;
            b[offset + i] = (byte) ((hi << 4) | lo);
        }
    }
}
