package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Writes traces out as a simplified SEG-D file matching the structure that
 * SegdBufferedFileReader can read back. All structural byte offsets and the
 * trace-header field mapping come from a SegdConfig. See
 * SegdBufferedFileReader's class javadoc for the same caveats about SEG-D
 * revision/vendor variability.
 */
public class SegdWriter implements TraceWriter
{
    private final SegdConfig config;
    private RandomAccessFile file;

    public SegdWriter()
    {
        this(new SegdConfig());
    }

    public SegdWriter(SegdConfig config)
    {
        this.config = config;
    }

    @Override
    public void open(String filename, WriterConfig writerConfig) throws IOException
    {
        file = new RandomAccessFile(filename, "rw");
        file.setLength(0);

        byte[] gh1 = new byte[config.generalHeaderBlockBytes];
        HeaderCodec.writeBcd(gh1, config.fileNumberByteOffset, 2, 1);
        HeaderCodec.writeBcd(gh1, config.formatCodeByteOffset, 2, SegdBufferedFileReader.FORMAT_IEEE_FLOAT_8058);
        gh1[config.channelSetsPerScanTypeByteOffset] = 1;
        gh1[config.additionalGeneralHeaderBlocksByteOffset] = (byte) (1 << 4);
        int baseScanInterval = (int) Math.round((writerConfig.sampleRateMicros / 1000.0) * 16.0);
        gh1[config.baseScanIntervalByteOffset] = (byte) baseScanInterval;
        gh1[config.traceHeaderExtensionCountByteOffset] = 0;
        file.write(gh1);

        byte[] gh2 = new byte[config.generalHeaderBlockBytes];
        gh2[config.extendedHeaderBlocksByteOffsetInHeader2] = 0;
        gh2[config.externalHeaderBlocksByteOffsetInHeader2] = 0;
        file.write(gh2);

        byte[] csd = new byte[config.generalHeaderBlockBytes];
        int off = config.samplesFieldByteOffsetInChannelSetDescriptor;
        csd[off] = (byte) ((writerConfig.samplesPerTrace >> 8) & 0xFF);
        csd[off + 1] = (byte) (writerConfig.samplesPerTrace & 0xFF);
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
        byte[] th = new byte[config.traceHeaderBytes];
        String[] names = trace.getHeaderList();
        double[] values = trace.getHeaders();
        List<HeaderFieldDef> fields = config.traceHeaderSchema.getFields();
        for (HeaderFieldDef f : fields)
        {
            double value = findHeaderValue(names, values, f.getName());
            HeaderCodec.encode(th, f, value);
        }
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
}
