package com.tricongeophysics.model;

/**
 * Encodes/decodes a single trace-header field's raw bytes according to its
 * FieldType. Shared by the SEG-Y and SEG-D readers/writers so the byte-level
 * logic for each numeric encoding lives in exactly one place, and every
 * reader/writer honors whatever HeaderSchema the user configured.
 */
final class HeaderCodec
{
    private HeaderCodec() {}

    static double decode(byte[] buf, HeaderFieldDef field)
    {
        int off = field.getByteOffset();
        switch (field.getType())
        {
            case INT8:   return buf[off];
            case UINT8:  return buf[off] & 0xFF;
            case INT16:  return (short) (((buf[off] & 0xFF) << 8) | (buf[off + 1] & 0xFF));
            case UINT16: return ((buf[off] & 0xFF) << 8) | (buf[off + 1] & 0xFF);
            case INT32:  return readInt32(buf, off);
            case UINT32: return readInt32(buf, off) & 0xFFFFFFFFL;
            case BCD2:   return bcdToInt(buf, off, 2);
            case BCD3:   return bcdToInt(buf, off, 3);
            case BCD4:   return bcdToInt(buf, off, 4);
            default:     return 0;
        }
    }

    static void encode(byte[] buf, HeaderFieldDef field, double value)
    {
        int off = field.getByteOffset();
        long v = Math.round(value);
        switch (field.getType())
        {
            case INT8:
            case UINT8:  buf[off] = (byte) v; break;
            case INT16:
            case UINT16: buf[off] = (byte) (v >> 8); buf[off + 1] = (byte) v; break;
            case INT32:
            case UINT32: writeInt32(buf, off, (int) v); break;
            case BCD2:   writeBcd(buf, off, 2, (int) v); break;
            case BCD3:   writeBcd(buf, off, 3, (int) v); break;
            case BCD4:   writeBcd(buf, off, 4, (int) v); break;
        }
    }

    static int readInt32(byte[] b, int off)
    {
        return ((b[off] & 0xFF) << 24) | ((b[off + 1] & 0xFF) << 16) | ((b[off + 2] & 0xFF) << 8) | (b[off + 3] & 0xFF);
    }

    static void writeInt32(byte[] b, int off, int v)
    {
        b[off] = (byte) (v >>> 24); b[off + 1] = (byte) (v >>> 16); b[off + 2] = (byte) (v >>> 8); b[off + 3] = (byte) v;
    }

    /** decodes numBytes of packed binary-coded-decimal (2 digits per byte) into an int */
    static int bcdToInt(byte[] b, int off, int n)
    {
        int value = 0;
        for (int i = 0; i < n; i++)
        {
            int by = b[off + i] & 0xFF;
            value = value * 100 + ((by >> 4) & 0x0F) * 10 + (by & 0x0F);
        }
        return value;
    }

    static void writeBcd(byte[] b, int off, int n, int value)
    {
        for (int i = n - 1; i >= 0; i--)
        {
            int pair = value % 100;
            value /= 100;
            int hi = pair / 10, lo = pair % 10;
            b[off + i] = (byte) ((hi << 4) | lo);
        }
    }
}
