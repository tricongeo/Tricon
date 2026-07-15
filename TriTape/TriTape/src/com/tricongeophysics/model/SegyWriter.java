package com.tricongeophysics.model;

import com.tricongeophysics.SeismicTrace;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Writes traces out as a SEG-Y file. Byte offsets for the binary-header
 * values and the trace-header field mapping come from a SegyConfig so the
 * output layout matches whatever the user configured (defaults to the SEG-Y
 * rev 1 standard layout). When the input file was also SEG-Y, TraceMonitor
 * supplies the input's actual textual/binary header bytes via WriterConfig,
 * and this writer uses them as the starting point (see writeTextualHeader()/
 * writeBinaryHeader()) rather than generating generic defaults; for any other
 * input (no equivalent header bytes exist to carry through - SEG-D has no
 * textual header concept at all) the textual header instead documents this
 * writer's own trace-header schema (name/offset/type per field), EBCDIC-
 * encoded per the SEG-Y standard's own convention - see
 * HeaderSchema.describeAsTextualHeader(). Either way, sample rate, samples/
 * trace, and format-code fields are still always overwritten at this
 * writer's own configured offsets, since those must match the output's
 * actual data regardless of what the input said. Coordinate/elevation
 * values in SeismicTrace headers are assumed already descaled (as produced
 * by SegyBufferedFileReader), so this writer always writes a scalar of 1 (no
 * re-scaling on output).
 */
public class SegyWriter implements TraceWriter
{
    private final SegyConfig config;
    private RandomAccessFile file;
    private int traceCounter = 0;
    // fixed samples/trace every output trace is padded/truncated to - see open(); SEG-Y rev 1 (which
    // is what this writer produces) assumes fixed-length trace records, so writing traces of
    // differing lengths (as SEG-D Rev 3.1 input can supply - see SegdBufferedFileReader) corrupts
    // the file for any reader beyond the first trace whose actual length differs from what the
    // binary header declares
    private int fixedSamplesPerTrace;

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
        if (writerConfig.samplesPerTrace <= 0)
        {
            throw new IOException("SegyWriter: samplesPerTrace must be a positive, fully-resolved value "
                + "(the fixed length every output trace will be padded/truncated to) - got "
                + writerConfig.samplesPerTrace + ". This should have been resolved by the caller "
                + "(e.g. TraceMonitor scanning the input file, or an explicit output-length override) "
                + "before calling open().");
        }
        file = new RandomAccessFile(filename, "rw");
        file.setLength(0); //overwrite if it already exists
        fixedSamplesPerTrace = writerConfig.samplesPerTrace;
        writeTextualHeader(writerConfig);
        writeBinaryHeader(writerConfig);
    }

    /**
     * Writes the raw textual header bytes physically read from the input file when
     * available (byte-for-byte, preserving whatever encoding - EBCDIC or ASCII - the
     * input used), padding/truncating to config.textualHeaderBytes if the lengths
     * differ. Falls back to a generated textual header describing this writer's own
     * trace-header schema (name/offset/type per field - see
     * HeaderSchema.describeAsTextualHeader()) when no raw bytes were supplied - this is
     * always the case for non-SEG-Y input (SEG-D has no equivalent header concept at
     * all), and the generated text is encoded as EBCDIC (via SegyEbcdic), matching the
     * SEG-Y standard's own convention for textual headers rather than writing plain
     * ASCII bytes into a field every real SEG-Y reader will try to decode as EBCDIC.
     */
    private void writeTextualHeader(WriterConfig writerConfig) throws IOException
    {
        byte[] raw = new byte[config.textualHeaderBytes];
        if (writerConfig.textualHeaderRaw != null)
        {
            byte[] src = writerConfig.textualHeaderRaw;
            java.util.Arrays.fill(raw, (byte) ' ');
            System.arraycopy(src, 0, raw, 0, Math.min(src.length, raw.length));
        }
        else
        {
            System.arraycopy(config.traceHeaderSchema.describeAsTextualHeaderEbcdic(config.textualHeaderBytes), 0, raw, 0, raw.length);
        }
        file.write(raw);
    }

    /**
     * Starts from the input file's raw binary header bytes when available (preserving
     * every field this writer doesn't otherwise know about - job/line/reel numbers,
     * etc.), falling back to an all-zero header when not. Either way, sample rate,
     * samples/trace, and format code are always overwritten at this writer's configured
     * offsets afterward, since those three must be correct for the output file to be
     * readable regardless of what the input's raw bytes said.
     */
    private void writeBinaryHeader(WriterConfig writerConfig) throws IOException
    {
        byte[] raw = new byte[config.binaryHeaderBytes];
        if (writerConfig.binaryHeaderRaw != null)
        {
            byte[] src = writerConfig.binaryHeaderRaw;
            System.arraycopy(src, 0, raw, 0, Math.min(src.length, raw.length));
        }
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
        // every trace record must be the same size (see fixedSamplesPerTrace javadoc above), so the
        // header always declares fixedSamplesPerTrace here - NOT trace.getData().length, which is
        // this trace's own actual (possibly different) sample count before padding/truncation below
        writeUShort(hdr, config.numSamplesThisTraceByteOffset, fixedSamplesPerTrace);
        file.write(hdr);

        float[] data = trace.getData();
        byte[] samples = new byte[fixedSamplesPerTrace * 4];
        int samplesToCopy = Math.min(data.length, fixedSamplesPerTrace);
        for (int i = 0; i < samplesToCopy; i++)
        {
            writeInt(samples, i * 4, Float.floatToIntBits(data[i]));
        }
        // any remaining bytes (data.length < fixedSamplesPerTrace) are already zero from the fresh
        // byte[] allocation above - that's the zero-padding; data.length > fixedSamplesPerTrace is a
        // silent truncation, per the person's own request to allow a shorter output length than the
        // input's longest trace
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
