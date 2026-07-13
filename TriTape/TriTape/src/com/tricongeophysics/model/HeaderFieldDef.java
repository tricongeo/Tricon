package com.tricongeophysics.model;

/**
 * Describes one trace-header attribute the user wants decoded/encoded: its
 * name, its byte offset within the trace header block, how many bytes/what
 * numeric encoding it uses, (SEG-Y only) whether a coordinate/elevation
 * scalar should be applied to it, and an optional fixed-point scale divisor
 * (e.g. 65536 for a 24.16 fixed-point value packed into a 5-byte integer -
 * this is how Sercel's SEG-D Rev 3.1 "Extended receiver line/point number"
 * fields work: divide the raw integer by 65536 to get the human-readable
 * line/point number). A list of these forms a HeaderSchema, which is
 * user-editable from the GUI so the byte layout can be tuned per vendor.
 */
public class HeaderFieldDef
{
    public enum FieldType
    {
        INT8(1), UINT8(1), INT16(2), UINT16(2), UINT24(3), INT32(4), UINT32(4), FLOAT32(4), UINT40(5), BCD1(1), BCD2(2), BCD3(3), BCD4(4), DOUBLE64(8), UINT64(8);

        public final int byteLength;

        FieldType(int byteLength) { this.byteLength = byteLength; }
    }

    /** SEG-Y only: which scalar (if any) to apply after decoding this field */
    public enum ScalarType { NONE, COORDINATE, ELEVATION }

    private String name;
    private int byteOffset; //0-based offset within the trace header block
    private FieldType type;
    private ScalarType scalarType;
    private double scaleDivisor = 1.0; //raw integer value is divided by this to get the displayed value (1.0 = no scaling)

    public HeaderFieldDef(String name, int byteOffset, FieldType type)
    {
        this(name, byteOffset, type, ScalarType.NONE, 1.0);
    }

    public HeaderFieldDef(String name, int byteOffset, FieldType type, boolean applyCoordinateScalar)
    {
        this(name, byteOffset, type, applyCoordinateScalar ? ScalarType.COORDINATE : ScalarType.NONE, 1.0);
    }

    public HeaderFieldDef(String name, int byteOffset, FieldType type, ScalarType scalarType)
    {
        this(name, byteOffset, type, scalarType, 1.0);
    }

    public HeaderFieldDef(String name, int byteOffset, FieldType type, double scaleDivisor)
    {
        this(name, byteOffset, type, ScalarType.NONE, scaleDivisor);
    }

    public HeaderFieldDef(String name, int byteOffset, FieldType type, ScalarType scalarType, double scaleDivisor)
    {
        this.name = name;
        this.byteOffset = byteOffset;
        this.type = type;
        this.scalarType = scalarType;
        this.scaleDivisor = scaleDivisor;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getByteOffset() { return byteOffset; }
    public void setByteOffset(int byteOffset) { this.byteOffset = byteOffset; }
    public FieldType getType() { return type; }
    public void setType(FieldType type) { this.type = type; }
    public ScalarType getScalarType() { return scalarType; }
    public void setScalarType(ScalarType scalarType) { this.scalarType = scalarType; }
    public double getScaleDivisor() { return scaleDivisor; }
    public void setScaleDivisor(double scaleDivisor) { this.scaleDivisor = scaleDivisor; }

    public HeaderFieldDef copy()
    {
        return new HeaderFieldDef(name, byteOffset, type, scalarType, scaleDivisor);
    }

    @Override
    public String toString() { return name; }
}
