package com.tricongeophysics.model;

import java.util.ArrayList;
import java.util.List;

/** an ordered, user-editable list of trace-header field definitions for a reader/writer */
public class HeaderSchema
{
    private List<HeaderFieldDef> fields;

    public HeaderSchema()
    {
        this(new ArrayList<HeaderFieldDef>());
    }

    public HeaderSchema(List<HeaderFieldDef> fields)
    {
        this.fields = fields;
    }

    public List<HeaderFieldDef> getFields() { return fields; }
    public void setFields(List<HeaderFieldDef> fields) { this.fields = fields; }

    public String[] getNames()
    {
        String[] names = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) names[i] = fields.get(i).getName();
        return names;
    }

    public HeaderSchema copy()
    {
        List<HeaderFieldDef> copyList = new ArrayList<HeaderFieldDef>();
        for (HeaderFieldDef f : fields) copyList.add(f.copy());
        return new HeaderSchema(copyList);
    }

    /**
     * Generates a human-readable SEG-Y textual header describing this schema's own fields (name,
     * 1-based byte offset, type) - used by SegyWriter as the output's textual header when there's no
     * real source textual header to carry through byte-for-byte (SEG-D has no equivalent concept at
     * all, so this is always the case for SEG-D -> SEG-Y). Since this schema is the WRITER's own (the
     * output SEG-Y file's actual trace-header layout, not the input's), the generated header
     * documents exactly how to interpret each trace's header bytes in the file being produced - which
     * is what a SEG-Y textual header is conventionally for anyway.
     * <p>
     * Formatted as standard SEG-Y "C" records: one field per line, each padded/truncated to exactly
     * 80 characters and prefixed with its 1-based line number ("C 1 ", "C 2 ", ...), for as many
     * lines as textualHeaderBytes actually allows (3200 bytes = the SEG-Y rev 1 standard 40 lines;
     * unused trailing lines are left blank). If there are more fields than fit, the remaining ones are
     * simply not listed rather than overflowing - this is documentation, not the authoritative source
     * of the layout (the trace headers' own bytes are), so a truncated list isn't a correctness issue.
     * <p>
     * Returns plain text (one String, not yet split into 80-char lines as an array) - the caller is
     * responsible for encoding it to whatever the output actually needs (SegyWriter uses EBCDIC, via
     * SegyEbcdic, matching the SEG-Y standard's own convention for textual headers).
     */
    public String describeAsTextualHeader(int textualHeaderBytes)
    {
        final int LINE_WIDTH = 80;
        int maxLines = Math.max(1, textualHeaderBytes / LINE_WIDTH);

        List<String> content = new ArrayList<String>();
        content.add("REFORMATTED TO SEG-Y - NO SOURCE TEXTUAL HEADER WAS AVAILABLE, E.G. SEG-D INPUT");
        content.add("TRACE HEADER FIELD MAP FOR THIS FILE - NAME, 1-BASED BYTE OFFSET, TYPE:");
        for (HeaderFieldDef f : fields)
        {
            if (content.size() >= maxLines) break;
            content.add(String.format("%-24s BYTE %-6d %s", f.getName(), f.getByteOffset() + 1, f.getType().name()));
        }

        StringBuilder sb = new StringBuilder(maxLines * LINE_WIDTH);
        for (int i = 0; i < maxLines; i++)
        {
            String body = i < content.size() ? content.get(i) : "";
            String numbered = String.format("C%2d %s", i + 1, body);
            if (numbered.length() > LINE_WIDTH) numbered = numbered.substring(0, LINE_WIDTH);
            sb.append(String.format("%-" + LINE_WIDTH + "s", numbered));
        }
        return sb.toString();
    }

    /**
     * describeAsTextualHeader(), reformatted for on-screen display: a '\n' inserted after every
     * 80-character record, so a JTextArea shows it as 40 separate lines instead of one 3200-character
     * blob - confirmed as a real problem on a real file: showing it as one unbroken line made it look
     * (and be) essentially impossible for a person to hand-edit without misaligning the underlying
     * 80-byte record grid, since there was no visual indication of where each record boundary was.
     * <p>
     * NEVER pass this to SegyEbcdic.toEbcdic()/write it to a file directly - the inserted '\n'
     * characters aren't part of the actual SEG-Y textual header content (which is raw fixed-width
     * text with NO embedded record delimiters at all - a reader is expected to just chop it into
     * 80-byte chunks positionally), and EBCDIC-encoding them would eat a content byte per line for a
     * character that isn't in SegyEbcdic's conversion table, silently shifting every later record's
     * byte alignment by one. SegyHeaderPreviewPanel already strips '\n'/'\r' back out before encoding
     * a person's edits (see getEffectiveTextualHeaderRaw()), which is exactly why that round-trips
     * correctly - this method's output should only ever reach a JTextArea, never SegyEbcdic directly.
     * describeAsTextualHeaderEbcdic() correctly uses the newline-free describeAsTextualHeader()
     * instead, precisely to avoid this trap.
     */
    public String describeAsTextualHeaderForDisplay(int textualHeaderBytes)
    {
        final int LINE_WIDTH = 80;
        String raw = describeAsTextualHeader(textualHeaderBytes);
        StringBuilder sb = new StringBuilder(raw.length() + raw.length() / LINE_WIDTH + 1);
        for (int i = 0; i < raw.length(); i += LINE_WIDTH)
        {
            sb.append(raw, i, Math.min(i + LINE_WIDTH, raw.length()));
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * describeAsTextualHeader(), EBCDIC-encoded (via SegyEbcdic) into raw bytes ready to write - the
     * single source of truth for this schema's generated default textual header, used both by
     * SegyWriter (the actual written bytes) and by the output tab's SegyHeaderPreviewPanel (so the
     * person can see, and edit, the same default before submitting - see TraceMonitor's
     * syncOutputSegyDefaultsFromInput()) rather than it only being generated invisibly at write time.
     */
    public byte[] describeAsTextualHeaderEbcdic(int textualHeaderBytes)
    {
        return SegyEbcdic.toEbcdic(describeAsTextualHeader(textualHeaderBytes), textualHeaderBytes);
    }

    /**
     * Default trace-header field mapping covering the full SEG-Y rev 1
     * standard trace header (bytes 1-240 per the standard, offsets here
     * 0-based). This is essentially every field in the spec that maps to a
     * single numeric value; the one exception is "Source Energy Direction"
     * (bytes 219-224), a packed 3-component compound field that doesn't fit
     * this schema's one-value-per-field model, so it's omitted. Several
     * fields are named to line up exactly with defaultSegdSchema() (FFID,
     * CHAN, REC_X, REC_Y already matched; SOURCE_X/Y -> SHOT_X/Y,
     * ELEV_SOURCE -> SHOT_ELEV, ELEV_REC -> REC_ELEV, INLINE -> RECLINE,
     * CROSSLINE -> RECSTN, SHOTPOINT -> SHOTSTN, YEAR/DAY_OF_YEAR/HOUR/
     * MINUTE/SECOND -> SHOT_YEAR/SHOT_DAY/SHOT_HOUR/SHOT_MIN/SHOT_SEC), so a
     * SEG-D -> SEG-Y reformat carries these straight through by name without
     * any extra configuration - the writer looks up each output field by
     * name in whatever headers the input trace actually has. SHOTLINE uses
     * previously-unassigned tail bytes since rev1 has no natural slot for it.
     */
    public static HeaderSchema defaultSegySchema()
    {
        List<HeaderFieldDef> f = new ArrayList<HeaderFieldDef>();
        HeaderFieldDef.FieldType I16 = HeaderFieldDef.FieldType.INT16;
        HeaderFieldDef.FieldType I32 = HeaderFieldDef.FieldType.INT32;
        HeaderFieldDef.FieldType U16 = HeaderFieldDef.FieldType.UINT16;
        HeaderFieldDef.ScalarType COORD = HeaderFieldDef.ScalarType.COORDINATE;
        HeaderFieldDef.ScalarType ELEV = HeaderFieldDef.ScalarType.ELEVATION;

        f.add(new HeaderFieldDef("TRACE_SEQ_LINE", 0, I32));
        f.add(new HeaderFieldDef("TRACE_SEQ_FILE", 4, I32));
        f.add(new HeaderFieldDef("FFID", 8, I32));
        f.add(new HeaderFieldDef("CHAN", 12, I32));
        f.add(new HeaderFieldDef("ESPNUM", 16, I32));
        f.add(new HeaderFieldDef("CDP", 20, I32));
        f.add(new HeaderFieldDef("CDP_TR", 24, I32));
        f.add(new HeaderFieldDef("TRACE_ID_CODE", 28, I16));
        f.add(new HeaderFieldDef("NSUM_VERT", 30, I16));
        f.add(new HeaderFieldDef("NSUM_HORZ", 32, I16));
        f.add(new HeaderFieldDef("DATA_USE", 34, I16));
        f.add(new HeaderFieldDef("OFFSET", 36, I32));
        f.add(new HeaderFieldDef("REC_ELEV", 40, I32, ELEV));
        f.add(new HeaderFieldDef("SHOT_ELEV", 44, I32, ELEV));
        f.add(new HeaderFieldDef("SOURCE_DEPTH", 48, I32, ELEV));
        f.add(new HeaderFieldDef("DATUM_ELEV_REC", 52, I32, ELEV));
        f.add(new HeaderFieldDef("DATUM_ELEV_SOURCE", 56, I32, ELEV));
        f.add(new HeaderFieldDef("WATER_DEPTH_SOURCE", 60, I32, ELEV));
        f.add(new HeaderFieldDef("WATER_DEPTH_GROUP", 64, I32, ELEV));
        f.add(new HeaderFieldDef("ELEV_SCALAR", 68, I16));
        f.add(new HeaderFieldDef("COORD_SCALAR", 70, I16));
        f.add(new HeaderFieldDef("SHOT_X", 72, I32, COORD));
        f.add(new HeaderFieldDef("SHOT_Y", 76, I32, COORD));
        f.add(new HeaderFieldDef("REC_X", 80, I32, COORD));
        f.add(new HeaderFieldDef("REC_Y", 84, I32, COORD));
        f.add(new HeaderFieldDef("COORD_UNITS", 88, I16));
        f.add(new HeaderFieldDef("WEATHERING_VEL", 90, I16));
        f.add(new HeaderFieldDef("SUBWEATHERING_VEL", 92, I16));
        f.add(new HeaderFieldDef("UPHOLE_TIME_SOURCE", 94, I16));
        f.add(new HeaderFieldDef("UPHOLE_TIME_REC", 96, I16));
        f.add(new HeaderFieldDef("SOURCE_STATIC", 98, I16));
        f.add(new HeaderFieldDef("GROUP_STATIC", 100, I16));
        f.add(new HeaderFieldDef("TOTAL_STATIC", 102, I16));
        f.add(new HeaderFieldDef("LAG_TIME_A", 104, I16));
        f.add(new HeaderFieldDef("LAG_TIME_B", 106, I16));
        f.add(new HeaderFieldDef("DELAY_TIME", 108, I16));
        f.add(new HeaderFieldDef("MUTE_START", 110, I16));
        f.add(new HeaderFieldDef("MUTE_END", 112, I16));
        f.add(new HeaderFieldDef("NUM_SAMPLES", 114, U16));
        f.add(new HeaderFieldDef("SAMPLE_INTERVAL", 116, U16));
        f.add(new HeaderFieldDef("GAIN_TYPE", 118, I16));
        f.add(new HeaderFieldDef("GAIN_CONST", 120, I16));
        f.add(new HeaderFieldDef("INITIAL_GAIN", 122, I16));
        f.add(new HeaderFieldDef("CORRELATED", 124, I16));
        f.add(new HeaderFieldDef("SWEEP_FREQ_START", 126, I16));
        f.add(new HeaderFieldDef("SWEEP_FREQ_END", 128, I16));
        f.add(new HeaderFieldDef("SWEEP_LENGTH", 130, I16));
        f.add(new HeaderFieldDef("SWEEP_TYPE", 132, I16));
        f.add(new HeaderFieldDef("SWEEP_TAPER_START", 134, I16));
        f.add(new HeaderFieldDef("SWEEP_TAPER_END", 136, I16));
        f.add(new HeaderFieldDef("TAPER_TYPE", 138, I16));
        f.add(new HeaderFieldDef("ALIAS_FILT_FREQ", 140, I16));
        f.add(new HeaderFieldDef("ALIAS_FILT_SLOPE", 142, I16));
        f.add(new HeaderFieldDef("NOTCH_FILT_FREQ", 144, I16));
        f.add(new HeaderFieldDef("NOTCH_FILT_SLOPE", 146, I16));
        f.add(new HeaderFieldDef("LOW_CUT_FREQ", 148, I16));
        f.add(new HeaderFieldDef("HIGH_CUT_FREQ", 150, I16));
        f.add(new HeaderFieldDef("LOW_CUT_SLOPE", 152, I16));
        f.add(new HeaderFieldDef("HIGH_CUT_SLOPE", 154, I16));
        f.add(new HeaderFieldDef("SHOT_YEAR", 156, I16));
        f.add(new HeaderFieldDef("SHOT_DAY", 158, I16));
        f.add(new HeaderFieldDef("SHOT_HOUR", 160, I16));
        f.add(new HeaderFieldDef("SHOT_MIN", 162, I16));
        f.add(new HeaderFieldDef("SHOT_SEC", 164, I16));
        f.add(new HeaderFieldDef("TIME_BASIS_CODE", 166, I16));
        f.add(new HeaderFieldDef("TRACE_WEIGHT", 168, I16));
        f.add(new HeaderFieldDef("GEOPHONE_GRP_ROLL1", 170, I16));
        f.add(new HeaderFieldDef("GEOPHONE_GRP_FIRST", 172, I16));
        f.add(new HeaderFieldDef("GEOPHONE_GRP_LAST", 174, I16));
        f.add(new HeaderFieldDef("GAP_SIZE", 176, I16));
        f.add(new HeaderFieldDef("OVER_TRAVEL", 178, I16));
        f.add(new HeaderFieldDef("CDP_X", 180, I32, COORD));
        f.add(new HeaderFieldDef("CDP_Y", 184, I32, COORD));
        f.add(new HeaderFieldDef("RECLINE", 188, I32));
        f.add(new HeaderFieldDef("RECSTN", 192, I32));
        f.add(new HeaderFieldDef("SHOTSTN", 196, I32));
        f.add(new HeaderFieldDef("SHOTPOINT_SCALAR", 200, I16));
        f.add(new HeaderFieldDef("TRACE_VALUE_UNIT", 202, I16));
        f.add(new HeaderFieldDef("TRANSDUCTION_MANTISSA", 204, I32));
        f.add(new HeaderFieldDef("TRANSDUCTION_EXPONENT", 208, I16));
        f.add(new HeaderFieldDef("TRANSDUCTION_UNITS", 210, I16));
        f.add(new HeaderFieldDef("DEVICE_TRACE_ID", 212, I16));
        f.add(new HeaderFieldDef("TIME_SCALAR", 214, I16));
        f.add(new HeaderFieldDef("SOURCE_TYPE", 216, I16));
        // bytes 219-224 (offset 218): Source Energy Direction - packed 3-component compound field, not representable
        // as a single value in this schema, so intentionally omitted
        f.add(new HeaderFieldDef("SOURCE_MEASURE_MANTISSA", 224, I32));
        f.add(new HeaderFieldDef("SOURCE_MEASURE_EXPONENT", 228, I16));
        f.add(new HeaderFieldDef("SOURCE_MEASURE_UNIT", 230, I16));
        // bytes 233-236 (offset 232) were unassigned in the standard - used here for SHOTLINE so a SEG-D
        // reformat has somewhere to put it by default; move it if you need those bytes for something else
        f.add(new HeaderFieldDef("SHOTLINE", 232, I32));
        return new HeaderSchema(f);
    }

    /**
     * Default trace-header field mapping, per Sercel's "Nodal Data Format Manual"
     * (SEG-D Rev 3.1): the 20-byte Demultiplexed Trace Header, plus (for Rev 3.1)
     * fields from the 32-byte Trace Header Extension #1 that follows it, addressed
     * here as offsets 20-51 within the combined 52-byte buffer SegdBufferedFileReader
     * builds for Rev 3.1 (see its class javadoc). The Rev 1/2 code path only ever
     * has the 20-byte header available, so fields at offset >=20 simply won't
     * resolve there (SegdBufferedFileReader.readOneTrace() skips any field whose
     * offset+width falls outside whatever buffer is actually available, rather
     * than erroring). Also a reasonable starting point for Rev 1/2 files for the
     * first six fields, since the core concepts (file number, scan type, channel
     * set) carry the same meaning even where exact byte packing differs by
     * revision/vendor.
     */
    public static HeaderSchema defaultSegdSchema()
    {
        List<HeaderFieldDef> f = new ArrayList<HeaderFieldDef>();
        // Demultiplexed Trace Header (20 bytes). NOTE: like RECLINE/RECSTN below, the plain "File number"
        // (offset 0, BCD) and "Channel Set Number" (offset 3, BCD) fields are Sercel sentinels that always
        // read FFFF/FF - confirmed against a real file (old FFID mapping decoded as garbage 16665, since FF
        // isn't a valid BCD digit). The "Extended file number" at offset 17 (bytes 18-20, 1-based) that
        // used to live here as FFID was confirmed unused (always reads as sentinel/garbage) for REV3_1 -
        // FFID is instead decoded dynamically for REV3_1 from the SERCEL VP Identification Block's VP
        // uuid (§6.4.3), replacing what used to be exposed separately as SHOT_VPID - see
        // SegdBufferedFileReader.decodePositionAndVpFields() and readOneTrace(). REV1_REV2 files don't
        // reach that block at all (position/VP-ID fields are REV3_1-only - see the class javadoc), so
        // they won't get an FFID from this schema at all now; add a fixed-offset "FFID" field back via
        // the schema editor UI if a specific REV1/REV2 file has one at a known offset.
        // CHAN_SET points at the real "Extended channel set number" - this identifies which channel SET
        // (a group of channels sharing acquisition parameters) a trace belongs to, NOT which individual
        // channel/receiver it is, so it deliberately does NOT match a SEG-Y output field name (unlike
        // CHAN below, which does): a SEG-D->SEG-Y reformat should not carry CHAN_SET through as CHAN.
        f.add(new HeaderFieldDef("SCAN_TYPE", 2, HeaderFieldDef.FieldType.BCD1));
        f.add(new HeaderFieldDef("TRACE_HDR_EXT_COUNT", 9, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("SAMPLE_SKEW", 10, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("TRACE_EDIT", 11, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("CHAN_SET", 15, HeaderFieldDef.FieldType.UINT16));
        // Trace Header Extension #1 (32 bytes, Rev 3.1 only; offsets are 20 + the manual's own 0-based offset).
        // NOTE: the plain "Receiver line/point number" fields (offset 20/23, 3 bytes) are Sercel sentinels
        // that always read 0xFFFFFF - the real, per-trace values live in the "Extended receiver line/point
        // number" fields (5 bytes each) instead, which is what REC_LINE/REC_POINT point at below. Confirmed
        // against a real file that these are 24.16 fixed-point (24-bit integer part + 16-bit fraction packed
        // into the 5-byte integer), so the raw value is divided by 65536 (2^16) to get the human-readable
        // line/point number (e.g. raw 458752000 / 65536 = 7000.0).
        f.add(new HeaderFieldDef("RECLINE", 30, HeaderFieldDef.FieldType.UINT40, 65536.0));
        f.add(new HeaderFieldDef("RECSTN", 35, HeaderFieldDef.FieldType.UINT40, 65536.0));
        f.add(new HeaderFieldDef("REC_POINT_INDEX", 26, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("RESHOOT_INDEX", 27, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("GROUP_INDEX", 28, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("DEPTH_INDEX", 29, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("SENSOR_TYPE", 40, HeaderFieldDef.FieldType.UINT8));
        // §6.4.2 bytes 22-24 (1-based) of Extension #1 - this trace's own channel number, which IS what
        // should map to SEG-Y's CHAN field on output (unlike CHAN_SET above); named EXT_TRACE_NUM until
        // this rename made that mapping explicit and matched to defaultSegySchema()'s "CHAN" field name.
        f.add(new HeaderFieldDef("CHAN", 41, HeaderFieldDef.FieldType.UINT24));
        f.add(new HeaderFieldDef("NUM_SAMPLES", 44, HeaderFieldDef.FieldType.UINT32));
        f.add(new HeaderFieldDef("SENSOR_MOVING", 48, HeaderFieldDef.FieldType.UINT8));
        return new HeaderSchema(f);
    }
}
