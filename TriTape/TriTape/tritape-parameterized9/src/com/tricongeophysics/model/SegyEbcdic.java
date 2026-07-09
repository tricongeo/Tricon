package com.tricongeophysics.model;

/** minimal EBCDIC &lt;-&gt; ASCII conversion table for SEG-Y textual headers */
final class SegyEbcdic
{
    private static final byte[] TO_ASCII = build();

    private SegyEbcdic() {}

    static char toAscii(int ebcdic)
    {
        if (ebcdic < 0 || ebcdic >= TO_ASCII.length) return ' ';
        byte a = TO_ASCII[ebcdic];
        return a == 0 ? ' ' : (char) (a & 0xFF);
    }

    static byte toEbcdic(char ascii)
    {
        for (int i = 0; i < TO_ASCII.length; i++)
        {
            if ((TO_ASCII[i] & 0xFF) == ascii) return (byte) i;
        }
        return 0x40; //EBCDIC space
    }

    private static byte[] build()
    {
        byte[] t = new byte[256];
        for (int i = 0; i < 256; i++) t[i] = 0x20;
        int[] digitCodes = {0xF0,0xF1,0xF2,0xF3,0xF4,0xF5,0xF6,0xF7,0xF8,0xF9};
        for (int i = 0; i < 10; i++) t[digitCodes[i]] = (byte) ('0' + i);
        int[][] upperBlocks = { {0xC1,'A',9}, {0xD1,'J',9}, {0xE2,'S',8} };
        for (int[] blk : upperBlocks)
        {
            int start = blk[0]; char ch = (char) blk[1]; int len = blk[2];
            for (int i = 0; i < len; i++) t[start + i] = (byte) (ch + i);
        }
        int[][] lowerBlocks = { {0x81,'a',9}, {0x91,'j',9}, {0xA2,'s',8} };
        for (int[] blk : lowerBlocks)
        {
            int start = blk[0]; char ch = (char) blk[1]; int len = blk[2];
            for (int i = 0; i < len; i++) t[start + i] = (byte) (ch + i);
        }
        t[0x40] = ' '; t[0x4B] = '.'; t[0x6B] = ','; t[0x5B] = '$';
        t[0x60] = '-'; t[0x61] = '/'; t[0x7A] = ':'; t[0x7D] = '\''; t[0x4E] = '+';
        return t;
    }
}
