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

    /** default trace-header field mapping matching the SEG-Y rev 1 standard layout */
    public static HeaderSchema defaultSegySchema()
    {
        List<HeaderFieldDef> f = new ArrayList<HeaderFieldDef>();
        f.add(new HeaderFieldDef("FFID", 8, HeaderFieldDef.FieldType.INT32));
        f.add(new HeaderFieldDef("TRACENO", 12, HeaderFieldDef.FieldType.INT32));
        f.add(new HeaderFieldDef("CDP", 20, HeaderFieldDef.FieldType.INT32));
        f.add(new HeaderFieldDef("CDP_TR", 24, HeaderFieldDef.FieldType.INT32));
        f.add(new HeaderFieldDef("OFFSET", 36, HeaderFieldDef.FieldType.INT32));
        f.add(new HeaderFieldDef("SOURCE_X", 72, HeaderFieldDef.FieldType.INT32, HeaderFieldDef.ScalarType.COORDINATE));
        f.add(new HeaderFieldDef("SOURCE_Y", 76, HeaderFieldDef.FieldType.INT32, HeaderFieldDef.ScalarType.COORDINATE));
        f.add(new HeaderFieldDef("REC_X", 80, HeaderFieldDef.FieldType.INT32, HeaderFieldDef.ScalarType.COORDINATE));
        f.add(new HeaderFieldDef("REC_Y", 84, HeaderFieldDef.FieldType.INT32, HeaderFieldDef.ScalarType.COORDINATE));
        f.add(new HeaderFieldDef("CDP_X", 180, HeaderFieldDef.FieldType.INT32, HeaderFieldDef.ScalarType.COORDINATE));
        f.add(new HeaderFieldDef("CDP_Y", 184, HeaderFieldDef.FieldType.INT32, HeaderFieldDef.ScalarType.COORDINATE));
        f.add(new HeaderFieldDef("INLINE", 188, HeaderFieldDef.FieldType.INT32));
        f.add(new HeaderFieldDef("CROSSLINE", 192, HeaderFieldDef.FieldType.INT32));
        f.add(new HeaderFieldDef("ELEV_SOURCE", 44, HeaderFieldDef.FieldType.INT32, HeaderFieldDef.ScalarType.ELEVATION));
        f.add(new HeaderFieldDef("ELEV_REC", 40, HeaderFieldDef.FieldType.INT32, HeaderFieldDef.ScalarType.ELEVATION));
        f.add(new HeaderFieldDef("WATER_DEPTH_SOURCE", 60, HeaderFieldDef.FieldType.INT32, HeaderFieldDef.ScalarType.ELEVATION));
        f.add(new HeaderFieldDef("WATER_DEPTH_GROUP", 64, HeaderFieldDef.FieldType.INT32, HeaderFieldDef.ScalarType.ELEVATION));
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
