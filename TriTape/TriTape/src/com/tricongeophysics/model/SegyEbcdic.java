package com.tricongeophysics.model;

/** minimal EBCDIC &lt;-&gt; ASCII conversion table for SEG-Y textual headers */
public final class SegyEbcdic
{
    private static final byte[] TO_ASCII = build();

    private SegyEbcdic() {}

    public static char toAscii(int ebcdic)
    {
        if (ebcdic < 0 || ebcdic >= TO_ASCII.length) return ' ';
        byte a = TO_ASCII[ebcdic];
        return a == 0 ? ' ' : (char) (a & 0xFF);
    }

    public static byte toEbcdic(char ascii)
    {
        for (int i = 0; i < TO_ASCII.length; i++)
        {
            if ((TO_ASCII[i] & 0xFF) == ascii) return (byte) i;
        }
        return 0x40; //EBCDIC space
    }

    /**
     * Encodes an entire string to EBCDIC, into a byte array of exactly length bytes - truncated if
     * text is longer, padded with EBCDIC space (0x40 - NOT ASCII space 0x20, which would leave a SEG-Y
     * textual header with a mix of EBCDIC content and ASCII padding, confusing any reader that isn't
     * lenient about it) if shorter. Public specifically so callers outside this package (e.g.
     * SegyHeaderPreviewPanel, encoding a person's hand-edited textual header text) can produce
     * correctly EBCDIC-encoded bytes too, not just this package's own readers/writers.
     */
    public static byte[] toEbcdic(String text, int length)
    {
        byte[] raw = new byte[length];
        java.util.Arrays.fill(raw, toEbcdic(' '));
        for (int i = 0; i < text.length() && i < raw.length; i++)
        {
            raw[i] = toEbcdic(text.charAt(i));
        }
        return raw;
    }

    private static byte[] build()
    {
        // NOTE: deliberately NOT pre-filled with a placeholder character (e.g. ' ') - byte[] is
        // already zero-initialized by Java, and toEbcdic(char) linearly searches this table for the
        // first index whose value matches, so any placeholder value here that happens to equal a real
        // character (space was tried and is a real, extremely common one) makes toEbcdic() find that
        // unmapped index INSTEAD of the correct one (index 0's leftover placeholder would always win
        // the search before reaching t[0x40], the actual EBCDIC space) - confirmed on a real file:
        // every space silently became EBCDIC NUL (0x00) instead of space (0x40), and since many
        // string/display routines treat 0x00 as a C-style terminator, everything after the first
        // space in each line appeared to vanish. Leaving unmapped entries at 0 relies on toAscii()'s
        // own explicit `a == 0 ? ' ' : ...` check to still decode them as space; toEbcdic() only ever
        // gets called with real, explicitly-mapped characters below (or falls through to its own 0x40
        // default for anything genuinely unmapped), so 0 never gets found as a false match there.
        byte[] t = new byte[256];
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
        t[0x60] = '-'; t[0x61] = '/'; t[0x7A] = ':'; t[0x7D] = '\''; t[0x4E] = '+'; t[0x6D] = '_';
        return t;
    }
}
