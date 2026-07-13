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
        // isn't a valid BCD digit). FFID below points at the real "Extended file number" instead; CHAN
        // already pointed at the real "Extended channel set number".
        f.add(new HeaderFieldDef("FFID", 17, HeaderFieldDef.FieldType.UINT24));
        f.add(new HeaderFieldDef("SCAN_TYPE", 2, HeaderFieldDef.FieldType.BCD1));
        f.add(new HeaderFieldDef("TRACE_HDR_EXT_COUNT", 9, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("SAMPLE_SKEW", 10, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("TRACE_EDIT", 11, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("CHAN", 15, HeaderFieldDef.FieldType.UINT16));
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
        f.add(new HeaderFieldDef("EXT_TRACE_NUM", 41, HeaderFieldDef.FieldType.UINT24));
        f.add(new HeaderFieldDef("NUM_SAMPLES", 44, HeaderFieldDef.FieldType.UINT32));
        f.add(new HeaderFieldDef("SENSOR_MOVING", 48, HeaderFieldDef.FieldType.UINT8));
        return new HeaderSchema(f);
    }
}
