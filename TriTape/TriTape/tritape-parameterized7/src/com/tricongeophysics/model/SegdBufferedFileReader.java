package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a SEG-D field file (rev 1/2 style fixed-length headers) in buffered
 * batches of traces. All structural byte offsets and the trace-header field
 * mapping come from a SegdConfig, so this can be retargeted at a different
 * vendor's layout from the GUI without touching code.
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
 */
public class SegdBufferedFileReader extends BufferedFileReader
{
    public static final int FORMAT_IEEE_FLOAT_8058 = 8058;

    private final SegdConfig config;
    private RandomAccessFile file;
    private int formatCode;
    private int additionalGeneralHeaderBlocks;
    private int traceHeaderExtensionBlocks;
    private int channelSetsPerScanType;
    private int fileNumber;
    private long firstTraceOffset;

    public SegdBufferedFileReader(String filename)
    {
        this(filename, new SegdConfig());
    }

    public SegdBufferedFileReader(String filename, SegdConfig config)
    {
        super(filename);
        this.config = config;
    }

    public SegdConfig getConfig() { return config; }

    @Override
    protected void doOpen() throws IOException
    {
        file = new RandomAccessFile(filename, "r");
        totalBytes = file.length();

        byte[] gh1 = new byte[config.generalHeaderBlockBytes];
        file.readFully(gh1);
        readGeneralHeader1(gh1);

        int extendedHeaderBlocks = 0;
        int externalHeaderBlocks = 0;
        if (additionalGeneralHeaderBlocks >= 1)
        {
            byte[] gh2 = new byte[config.generalHeaderBlockBytes];
            file.readFully(gh2);
            extendedHeaderBlocks = gh2[config.extendedHeaderBlocksByteOffsetInHeader2] & 0xFF;
            externalHeaderBlocks = gh2[config.externalHeaderBlocksByteOffsetInHeader2] & 0xFF;
        }
        for (int i = 1; i < additionalGeneralHeaderBlocks; i++)
        {
            file.skipBytes(config.generalHeaderBlockBytes);
        }
        file.skipBytes(extendedHeaderBlocks * config.generalHeaderBlockBytes);
        file.skipBytes(externalHeaderBlocks * config.generalHeaderBlockBytes);

        int nChannelSets = Math.max(1, channelSetsPerScanType);
        for (int i = 0; i < nChannelSets; i++)
        {
            byte[] csd = new byte[config.generalHeaderBlockBytes];
            file.readFully(csd);
            if (i == 0)
            {
                int off = config.samplesFieldByteOffsetInChannelSetDescriptor;
                samplesPerTrace = ((csd[off] & 0xFF) << 8) | (csd[off + 1] & 0xFF);
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
        fileNumber = HeaderCodec.bcdToInt(gh1, config.fileNumberByteOffset, 2);
        formatCode = HeaderCodec.bcdToInt(gh1, config.formatCodeByteOffset, 2);
        channelSetsPerScanType = gh1[config.channelSetsPerScanTypeByteOffset] & 0xFF;
        additionalGeneralHeaderBlocks = (gh1[config.additionalGeneralHeaderBlocksByteOffset] >> 4) & 0x0F;
        int baseScanInterval = gh1[config.baseScanIntervalByteOffset] & 0xFF; //value/16 = sample interval in ms
        sampleRateMicros = (int) Math.round((baseScanInterval / 16.0) * 1000.0);
        traceHeaderExtensionBlocks = gh1[config.traceHeaderExtensionCountByteOffset] & 0xFF;
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
        byte[] th = new byte[config.traceHeaderBytes];
        file.readFully(th);

        List<HeaderFieldDef> fields = config.traceHeaderSchema.getFields();
        String[] names = new String[fields.size()];
        double[] values = new double[fields.size()];
        for (int i = 0; i < fields.size(); i++)
        {
            HeaderFieldDef f = fields.get(i);
            names[i] = f.getName();
            values[i] = HeaderCodec.decode(th, f);
        }

        if (traceHeaderExtensionBlocks > 0)
        {
            file.skipBytes(traceHeaderExtensionBlocks * config.traceHeaderExtensionBytes);
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
        trace.setHeaderList(names);
        trace.setHeaders(values);
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
}
