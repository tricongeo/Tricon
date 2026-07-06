package com.tricongeophysics;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Reads a standard SEG-Y file (textual header + binary header + trace headers/data)
 * and assembles the traces into Gather objects made up of SeismicTrace objects.
 *
 * Traces are read sequentially from the file and grouped into a Gather based on
 * a chosen "primary key" trace header (by default CDP ensemble number). Every
 * consecutive run of traces sharing the same primary key value becomes one Gather,
 * matching the way DataSet/Gather/SeismicTrace are used elsewhere in this project
 * (see DataSet.getGatherIterator(), Gather.addTrace(), SeismicTrace).
 *
 * Usage:
 *   SEGYReader reader = new SEGYReader("/path/to/file.sgy", dataSet);
 *   while (reader.hasNext())
 *   {
 *       Gather gather = reader.next();
 *       // ... process gather ...
 *   }
 *   reader.close();
 *
 * NOTE: exact byte offsets for trace header attributes follow the SEG-Y rev 1
 * standard layout. If this data was written by a specific vendor with a
 * non-standard header layout, the offsets in HEADER FIELD DEFINITIONS below may
 * need adjustment.
 */
public class SEGYReader implements Iterator<Gather>
{
    // ---- file layout constants ----
    private static final int TEXTUAL_HEADER_BYTES = 3200;
    private static final int BINARY_HEADER_BYTES = 400;
    private static final int TRACE_HEADER_BYTES = 240;

    // ---- SEG-Y data sample format codes ----
    private static final int FORMAT_IBM_FLOAT = 1;
    private static final int FORMAT_INT32 = 2;
    private static final int FORMAT_INT16 = 3;
    private static final int FORMAT_IEEE_FLOAT = 5;
    private static final int FORMAT_INT8 = 8;

    // ---- header names/order written into every SeismicTrace's headerList ----
    // index into this array == index into SeismicTrace.getHeaders()
    private static final String[] HEADER_NAMES =
    {
        "FFID",         // 0  original field record number
        "TRACENO",      // 1  trace number within field record
        "CDP",          // 2  CDP ensemble number
        "CDP_TR",       // 3  trace number within CDP ensemble
        "OFFSET",       // 4  source-to-receiver offset
        "SOURCE_X",     // 5
        "SOURCE_Y",     // 6
        "REC_X",        // 7
        "REC_Y",        // 8
        "CDP_X",        // 9
        "CDP_Y",        // 10
        "INLINE",       // 11
        "CROSSLINE",    // 12
        "ELEV_SOURCE",  // 13
        "ELEV_REC",     // 14
        "WATER_DEPTH_SOURCE", // 15
        "WATER_DEPTH_GROUP"   // 16
    };

    public static final int CDP_HEADER_INDEX = 2;
    public static final int OFFSET_HEADER_INDEX = 4;

    private RandomAccessFile file;
    private String textualHeader;
    private int sampleRateMicros;
    private int samplesPerTrace;
    private int dataFormatCode;
    private long firstTraceOffset;
    private long fileLength;

    private DataSet dataSet;
    private int pkeyIndex = CDP_HEADER_INDEX; //which header groups traces into a gather

    private SeismicTrace lookaheadTrace;    //next trace already read from disk, not yet consumed
    private boolean lookaheadValid = false;
    private boolean endOfFile = false;

    public SEGYReader(String filename, DataSet dataSet) throws IOException
    {
        this.dataSet = dataSet;
        this.file = new RandomAccessFile(filename, "r");
        this.fileLength = file.length();
        readTextualHeader();
        readBinaryHeader();
        firstTraceOffset = TEXTUAL_HEADER_BYTES + BINARY_HEADER_BYTES;

        if (dataSet != null)
        {
            dataSet.setSampleRate(sampleRateMicros);
            dataSet.setSamplesPerTrace(samplesPerTrace);
        }

        file.seek(firstTraceOffset);
        primeLookahead();
    }

    /** choose which header (see HEADER_NAMES) is used to decide where one gather ends and the next begins */
    public void setPrimaryKeyIndex(int pkeyIndex)
    {
        this.pkeyIndex = pkeyIndex;
    }

    public String getTextualHeader()
    {
        return textualHeader;
    }

    public int getSampleRateMicros()
    {
        return sampleRateMicros;
    }

    public int getSamplesPerTrace()
    {
        return samplesPerTrace;
    }

    // ------------------------------------------------------------------
    // Iterator<Gather> implementation
    // ------------------------------------------------------------------

    public boolean hasNext()
    {
        return lookaheadValid;
    }

    /** returns the next Gather: every consecutive trace sharing the same primary key value */
    public Gather next()
    {
        if (!lookaheadValid)
        {
            throw new NoSuchElementException("no more gathers in SEG-Y file");
        }

        Gather gather = new Gather(dataSet);
        double currentKeyValue = lookaheadTrace.getHeaders()[pkeyIndex];

        while (lookaheadValid && lookaheadTrace.getHeaders()[pkeyIndex] == currentKeyValue)
        {
            gather.addTrace(lookaheadTrace);
            advanceLookahead();
        }

        return gather;
    }

    public void remove()
    {
        throw new UnsupportedOperationException("remove() not supported by SEGYReader");
    }

    public void close()
    {
        try
        {
            if (file != null)
            {
                file.close();
            }
        }
        catch (IOException e)
        {
            System.err.println("SEGYReader: error closing file: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // header parsing
    // ------------------------------------------------------------------

    private void readTextualHeader() throws IOException
    {
        file.seek(0);
        byte[] raw = new byte[TEXTUAL_HEADER_BYTES];
        file.readFully(raw);

        //most SEG-Y textual headers are EBCDIC; detect ASCII (starts with "C 1" typically) vs EBCDIC
        boolean looksAscii = raw.length > 0 && raw[0] == 'C';
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length; i++)
        {
            int b = raw[i] & 0xFF;
            char c = looksAscii ? (char) b : ebcdicToAscii(b);
            sb.append(c);
            if ((i + 1) % 80 == 0)
            {
                sb.append('\n');
            }
        }
        textualHeader = sb.toString();
    }

    private void readBinaryHeader() throws IOException
    {
        file.seek(TEXTUAL_HEADER_BYTES);
        byte[] raw = new byte[BINARY_HEADER_BYTES];
        file.readFully(raw);

        sampleRateMicros = readUShort(raw, 16);   //bytes 3217-3218 (1-based) -> offset 16
        samplesPerTrace = readUShort(raw, 20);    //bytes 3221-3222 -> offset 20
        dataFormatCode = readUShort(raw, 24);     //bytes 3225-3226 -> offset 24

        if (samplesPerTrace <= 0)
        {
            throw new IOException("SEGYReader: invalid/zero samples-per-trace read from binary header");
        }
    }

    /** reads one full trace (240 byte header + samplesPerTrace samples) at the file's current position */
    private SeismicTrace readOneTrace() throws IOException
    {
        byte[] hdr = new byte[TRACE_HEADER_BYTES];
        file.readFully(hdr);

        double[] headers = new double[HEADER_NAMES.length];
        headers[0] = readInt(hdr, 8);    //FFID
        headers[1] = readInt(hdr, 12);   //trace number within field record
        headers[2] = readInt(hdr, 20);   //CDP
        headers[3] = readInt(hdr, 24);   //trace number within CDP ensemble
        headers[4] = readInt(hdr, 36);   //offset

        short elevScalar = (short) readShort(hdr, 68);
        short coordScalar = (short) readShort(hdr, 70);
        double elevFactor = scalarToFactor(elevScalar);
        double coordFactor = scalarToFactor(coordScalar);

        headers[5] = readInt(hdr, 72) * coordFactor;   //source X
        headers[6] = readInt(hdr, 76) * coordFactor;   //source Y
        headers[7] = readInt(hdr, 80) * coordFactor;   //group/receiver X
        headers[8] = readInt(hdr, 84) * coordFactor;   //group/receiver Y
        headers[13] = readInt(hdr, 44) * elevFactor;   //surface elevation at source
        headers[14] = readInt(hdr, 40) * elevFactor;   //receiver group elevation
        headers[15] = readInt(hdr, 60) * elevFactor;   //water depth at source
        headers[16] = readInt(hdr, 64) * elevFactor;   //water depth at group

        //rev-1 fields: CDP X/Y and inline/crossline, present only in SEG-Y rev1+ files
        headers[9] = readInt(hdr, 180) * coordFactor;  //CDP X
        headers[10] = readInt(hdr, 184) * coordFactor; //CDP Y
        headers[11] = readInt(hdr, 188);               //inline number
        headers[12] = readInt(hdr, 192);               //crossline number

        int traceSamples = readUShort(hdr, 114);
        if (traceSamples <= 0)
        {
            traceSamples = samplesPerTrace; //fall back to binary-header value
        }

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

    // ------------------------------------------------------------------
    // lookahead management (needed to know when a gather's key value changes)
    // ------------------------------------------------------------------

    private void primeLookahead() throws IOException
    {
        advanceLookahead();
    }

    private void advanceLookahead()
    {
        if (endOfFile)
        {
            lookaheadValid = false;
            return;
        }
        try
        {
            if (file.getFilePointer() >= fileLength)
            {
                endOfFile = true;
                lookaheadValid = false;
                return;
            }
            lookaheadTrace = readOneTrace();
            lookaheadValid = true;
        }
        catch (EOFException e)
        {
            endOfFile = true;
            lookaheadValid = false;
        }
        catch (IOException e)
        {
            System.err.println("SEGYReader: error reading trace: " + e.getMessage());
            endOfFile = true;
            lookaheadValid = false;
        }
    }

    // ------------------------------------------------------------------
    // low level byte / number decoding helpers
    // ------------------------------------------------------------------

    private int bytesPerSample()
    {
        switch (dataFormatCode)
        {
            case FORMAT_IBM_FLOAT:  return 4;
            case FORMAT_INT32:      return 4;
            case FORMAT_INT16:      return 2;
            case FORMAT_IEEE_FLOAT: return 4;
            case FORMAT_INT8:       return 1;
            default:
                System.err.println("SEGYReader: unsupported data format code " + dataFormatCode + ", assuming 4-byte IBM float");
                return 4;
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
                case FORMAT_IBM_FLOAT:
                    out[i] = ibmToFloat(readInt(raw, offset));
                    break;
                case FORMAT_INT32:
                    out[i] = readInt(raw, offset);
                    break;
                case FORMAT_INT16:
                    out[i] = (short) readShort(raw, offset);
                    break;
                case FORMAT_IEEE_FLOAT:
                    out[i] = Float.intBitsToFloat(readInt(raw, offset));
                    break;
                case FORMAT_INT8:
                    out[i] = raw[offset];
                    break;
                default:
                    out[i] = ibmToFloat(readInt(raw, offset));
                    break;
            }
        }
    }

    /** converts a 4-byte IBM System/360 floating point value (as raw bits) to an IEEE-754 float */
    private static float ibmToFloat(int ibm)
    {
        int sign = (ibm >> 31) & 0x1;
        int exponent = (ibm >> 24) & 0x7F;
        int mantissa = ibm & 0x00FFFFFF;

        if (mantissa == 0)
        {
            return 0.0f;
        }

        double value = mantissa / (double) (1L << 24);
        value *= Math.pow(16.0, exponent - 64);
        return (float) (sign == 1 ? -value : value);
    }

    /** SEG-Y coordinate/elevation scalar: positive multiplies, negative divides, 0 means factor of 1 */
    private static double scalarToFactor(short scalar)
    {
        if (scalar == 0) return 1.0;
        if (scalar > 0) return scalar;
        return 1.0 / (-scalar);
    }

    private static int readInt(byte[] b, int offset)
    {
        return ((b[offset] & 0xFF) << 24) | ((b[offset + 1] & 0xFF) << 16)
             | ((b[offset + 2] & 0xFF) << 8) | (b[offset + 3] & 0xFF);
    }

    private static int readShort(byte[] b, int offset)
    {
        return (short) (((b[offset] & 0xFF) << 8) | (b[offset + 1] & 0xFF));
    }

    private static int readUShort(byte[] b, int offset)
    {
        return ((b[offset] & 0xFF) << 8) | (b[offset + 1] & 0xFF);
    }

    private static char ebcdicToAscii(int ebcdic)
    {
        //minimal EBCDIC->ASCII table covering printable characters typically found in SEG-Y text headers
        byte[] table = EBCDIC_TO_ASCII;
        if (ebcdic >= 0 && ebcdic < table.length)
        {
            byte a = table[ebcdic];
            return a == 0 ? ' ' : (char) (a & 0xFF);
        }
        return ' ';
    }

    private static final byte[] EBCDIC_TO_ASCII = buildEbcdicTable();

    private static byte[] buildEbcdicTable()
    {
        byte[] t = new byte[256];
        for (int i = 0; i < 256; i++) t[i] = 0x20; //default to space
        // digits
        int[] digitCodes = {0xF0,0xF1,0xF2,0xF3,0xF4,0xF5,0xF6,0xF7,0xF8,0xF9};
        for (int i = 0; i < 10; i++) t[digitCodes[i]] = (byte) ('0' + i);
        // uppercase letters (EBCDIC has gaps, defined in three blocks)
        int[][] upperBlocks = { {0xC1,'A',9}, {0xD1,'J',9}, {0xE2,'S',8} };
        for (int[] blk : upperBlocks)
        {
            int start = blk[0]; char ch = (char) blk[1]; int len = blk[2];
            for (int i = 0; i < len; i++) t[start + i] = (byte) (ch + i);
        }
        // lowercase letters
        int[][] lowerBlocks = { {0x81,'a',9}, {0x91,'j',9}, {0xA2,'s',8} };
        for (int[] blk : lowerBlocks)
        {
            int start = blk[0]; char ch = (char) blk[1]; int len = blk[2];
            for (int i = 0; i < len; i++) t[start + i] = (byte) (ch + i);
        }
        t[0x40] = ' ';
        t[0x4B] = '.';
        t[0x6B] = ',';
        t[0x5B] = '$';
        t[0x60] = '-';
        t[0x61] = '/';
        t[0x7A] = ':';
        t[0x7D] = '\'';
        t[0x4E] = '+';
        return t;
    }
}