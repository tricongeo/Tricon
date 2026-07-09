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
     * this schema's one-value-per-field model, so it's omitted, along with
     * the 8 unassigned bytes at the end of the header (233-240).
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
        f.add(new HeaderFieldDef("TRACENO", 12, I32));
        f.add(new HeaderFieldDef("ENERGY_SOURCE_PT", 16, I32));
        f.add(new HeaderFieldDef("CDP", 20, I32));
        f.add(new HeaderFieldDef("CDP_TR", 24, I32));
        f.add(new HeaderFieldDef("TRACE_ID_CODE", 28, I16));
        f.add(new HeaderFieldDef("NSUM_VERT", 30, I16));
        f.add(new HeaderFieldDef("NSUM_HORZ", 32, I16));
        f.add(new HeaderFieldDef("DATA_USE", 34, I16));
        f.add(new HeaderFieldDef("OFFSET", 36, I32));
        f.add(new HeaderFieldDef("ELEV_REC", 40, I32, ELEV));
        f.add(new HeaderFieldDef("ELEV_SOURCE", 44, I32, ELEV));
        f.add(new HeaderFieldDef("SOURCE_DEPTH", 48, I32, ELEV));
        f.add(new HeaderFieldDef("DATUM_ELEV_REC", 52, I32, ELEV));
        f.add(new HeaderFieldDef("DATUM_ELEV_SOURCE", 56, I32, ELEV));
        f.add(new HeaderFieldDef("WATER_DEPTH_SOURCE", 60, I32, ELEV));
        f.add(new HeaderFieldDef("WATER_DEPTH_GROUP", 64, I32, ELEV));
        f.add(new HeaderFieldDef("ELEV_SCALAR", 68, I16));
        f.add(new HeaderFieldDef("COORD_SCALAR", 70, I16));
        f.add(new HeaderFieldDef("SOURCE_X", 72, I32, COORD));
        f.add(new HeaderFieldDef("SOURCE_Y", 76, I32, COORD));
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
        f.add(new HeaderFieldDef("YEAR", 156, I16));
        f.add(new HeaderFieldDef("DAY_OF_YEAR", 158, I16));
        f.add(new HeaderFieldDef("HOUR", 160, I16));
        f.add(new HeaderFieldDef("MINUTE", 162, I16));
        f.add(new HeaderFieldDef("SECOND", 164, I16));
        f.add(new HeaderFieldDef("TIME_BASIS_CODE", 166, I16));
        f.add(new HeaderFieldDef("TRACE_WEIGHT", 168, I16));
        f.add(new HeaderFieldDef("GEOPHONE_GRP_ROLL1", 170, I16));
        f.add(new HeaderFieldDef("GEOPHONE_GRP_FIRST", 172, I16));
        f.add(new HeaderFieldDef("GEOPHONE_GRP_LAST", 174, I16));
        f.add(new HeaderFieldDef("GAP_SIZE", 176, I16));
        f.add(new HeaderFieldDef("OVER_TRAVEL", 178, I16));
        f.add(new HeaderFieldDef("CDP_X", 180, I32, COORD));
        f.add(new HeaderFieldDef("CDP_Y", 184, I32, COORD));
        f.add(new HeaderFieldDef("INLINE", 188, I32));
        f.add(new HeaderFieldDef("CROSSLINE", 192, I32));
        f.add(new HeaderFieldDef("SHOTPOINT", 196, I32));
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
        // bytes 233-240 (offset 232): unassigned
        return new HeaderSchema(f);
    }

    /** default trace-header field mapping for the simplified SEG-D layout used by SegdBufferedFileReader */
    public static HeaderSchema defaultSegdSchema()
    {
        List<HeaderFieldDef> f = new ArrayList<HeaderFieldDef>();
        f.add(new HeaderFieldDef("FFID", 0, HeaderFieldDef.FieldType.BCD3));
        f.add(new HeaderFieldDef("SCAN_TYPE", 3, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("CHANNEL_SET", 4, HeaderFieldDef.FieldType.UINT8));
        f.add(new HeaderFieldDef("CHANNEL_NUMBER", 5, HeaderFieldDef.FieldType.UINT16));
        f.add(new HeaderFieldDef("TRACE_EDIT", 10, HeaderFieldDef.FieldType.UINT8));
        return new HeaderSchema(f);
    }
}
