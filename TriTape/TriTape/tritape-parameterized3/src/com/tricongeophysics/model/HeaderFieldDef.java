package com.tricongeophysics.model;

/**
 * Describes one trace-header attribute the user wants decoded/encoded: its
 * name, its byte offset within the trace header block, how many bytes/what
 * numeric encoding it uses, and (SEG-Y only) whether a coordinate/elevation
 * scalar should be applied to it. A list of these forms a HeaderSchema, which
 * is user-editable from the GUI so the byte layout can be tuned per vendor.
 */
public class HeaderFieldDef
{
    public enum FieldType
    {
        INT8(1), UINT8(1), INT16(2), UINT16(2), INT32(4), UINT32(4), BCD2(2), BCD3(3), BCD4(4);

        public final int byteLength;

        FieldType(int byteLength) { this.byteLength = byteLength; }
    }

    /** SEG-Y only: which scalar (if any) to apply after decoding this field */
    public enum ScalarType { NONE, COORDINATE, ELEVATION }

    private String name;
    private int byteOffset; //0-based offset within the trace header block
    private FieldType type;
    private ScalarType scalarType;

    public HeaderFieldDef(String name, int byteOffset, FieldType type)
    {
        this(name, byteOffset, type, ScalarType.NONE);
    }

    public HeaderFieldDef(String name, int byteOffset, FieldType type, boolean applyCoordinateScalar)
    {
        this(name, byteOffset, type, applyCoordinateScalar ? ScalarType.COORDINATE : ScalarType.NONE);
    }

    public HeaderFieldDef(String name, int byteOffset, FieldType type, ScalarType scalarType)
    {
        this.name = name;
        this.byteOffset = byteOffset;
        this.type = type;
        this.scalarType = scalarType;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getByteOffset() { return byteOffset; }
    public void setByteOffset(int byteOffset) { this.byteOffset = byteOffset; }
    public FieldType getType() { return type; }
    public void setType(FieldType type) { this.type = type; }
    public ScalarType getScalarType() { return scalarType; }
    public void setScalarType(ScalarType scalarType) { this.scalarType = scalarType; }

    public HeaderFieldDef copy()
    {
        return new HeaderFieldDef(name, byteOffset, type, scalarType);
    }

    @Override
    public String toString() { return name; }
}
