package com.tricongeophysics.view;

import com.tricongeophysics.SeismicTrace;
import com.tricongeophysics.model.HeaderFieldDef;
import com.tricongeophysics.model.HeaderSchema;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Editable JTable model backing a HeaderSchema: name / byte offset / type /
 * scalar / scale divisor, plus three read-only "sample value" columns
 * showing what each header actually decodes to for the first 3 traces of a
 * file - populated by setSampleTraces(), called after headers are (re)loaded
 * for a batch of traces.
 *
 * Rows 0..schema.getFields().size()-1 are the editable, offset-based schema
 * fields. Beyond that, extra READ-ONLY rows are shown for any header name
 * that actually appears on a sample trace but isn't in the schema at all -
 * this happens for SEG-D Rev 3.1, where SegdBufferedFileReader dynamically
 * appends fields (SHOT_YEAR/DAY/HOUR/MIN/SEC from the record-level General
 * Header timestamp or the per-trace §6.4.2 Timestamp block, and REC_X/REC_Y/
 * REC_ELEV/SHOT_X/SHOT_Y/SHOT_ELEV/FFID/SHOTLINE/SHOTSTN from walking
 * the Position/VP-ID/Source-Description blocks - see SegdBufferedFileReader's
 * class javadoc) that can't be represented as a fixed-offset schema entry at
 * all, since the blocks they come from sit after a variable-length,
 * variable-content extension chain. There's nothing to edit for these - no
 * single byte offset would be correct across traces - so they're name +
 * sample values only, with no offset/type/scalar/divisor and not
 * add/removable. For SEG-Y (and SEG-D Rev 1/2), every sample trace's headers
 * always match the schema names exactly, so this never produces extra rows.
 */
public class HeaderSchemaTableModel extends AbstractTableModel
{
    private static final String[] COLUMNS = {
        "Header Name", "Byte Offset (1-based)", "Type", "Scalar (SEG-Y only)", "Scale Divisor",
        "Trace 1", "Trace 2", "Trace 3"
    };
    private static final int FIRST_SAMPLE_COLUMN = 5;

    private final HeaderSchema schema;
    private SeismicTrace[] sampleTraces = new SeismicTrace[0];
    private List<String> extraHeaderNames = new ArrayList<String>();

    public HeaderSchemaTableModel(HeaderSchema schema)
    {
        this.schema = schema;
    }

    @Override public int getRowCount() { return schema.getFields().size() + extraHeaderNames.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }

    @Override
    public boolean isCellEditable(int row, int col)
    {
        return col < FIRST_SAMPLE_COLUMN && row < schema.getFields().size(); // extra (non-schema) rows aren't editable at all
    }

    @Override
    public Class<?> getColumnClass(int col)
    {
        switch (col)
        {
            case 0: return String.class;
            case 1: return Integer.class;
            case 2: return HeaderFieldDef.FieldType.class;
            case 3: return HeaderFieldDef.ScalarType.class;
            case 4: return Double.class;
            default: return String.class; // sample-value columns are display-only formatted text
        }
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        int schemaSize = schema.getFields().size();
        if (row >= schemaSize)
        {
            String name = extraHeaderNames.get(row - schemaSize);
            switch (col)
            {
                case 0: return name;
                case 1: case 2: case 3: case 4: return null; // no fixed offset/type applies - this field isn't schema-backed
                default: return sampleValueText(name, col - FIRST_SAMPLE_COLUMN);
            }
        }
        HeaderFieldDef f = schema.getFields().get(row);
        switch (col)
        {
            case 0: return f.getName();
            case 1: return f.getByteOffset() + 1;
            case 2: return f.getType();
            case 3: return f.getScalarType();
            case 4: return f.getScaleDivisor();
            default:
                int traceIndex = col - FIRST_SAMPLE_COLUMN;
                return sampleValueText(f.getName(), traceIndex);
        }
    }

    private String sampleValueText(String headerName, int traceIndex)
    {
        if (traceIndex < 0 || traceIndex >= sampleTraces.length) return "";
        return formatValue(sampleTraces[traceIndex].getHeaderValue(headerName));
    }

    private static String formatValue(double v)
    {
        if (Double.isNaN(v)) return "";
        if (!Double.isInfinite(v) && v == Math.rint(v)) return String.valueOf((long) v);
        return String.format("%.2f", v);
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        if (col >= FIRST_SAMPLE_COLUMN || row >= schema.getFields().size()) return; // sample-value and extra rows are read-only
        HeaderFieldDef f = schema.getFields().get(row);
        switch (col)
        {
            case 0: f.setName(String.valueOf(value)); break;
            case 1: f.setByteOffset(Math.max(1, ((Number) value).intValue()) - 1); break;
            case 2: f.setType((HeaderFieldDef.FieldType) value); break;
            case 3: f.setScalarType((HeaderFieldDef.ScalarType) value); break;
            case 4:
                double divisor = ((Number) value).doubleValue();
                f.setScaleDivisor(divisor == 0.0 ? 1.0 : divisor); // guard against divide-by-zero
                break;
            default: break;
        }
        fireTableCellUpdated(row, col);
    }

    /**
     * Sets the (up to 3) sample traces whose header values populate the "Trace 1/2/3" columns, and
     * recomputes extraHeaderNames from whatever header names these traces actually carry that aren't
     * already in the schema (see class javadoc) - so switching between an aux-channel-only batch and
     * a batch that reaches a Position block, for instance, updates which extra rows are shown.
     */
    public void setSampleTraces(SeismicTrace[] traces)
    {
        this.sampleTraces = traces == null ? new SeismicTrace[0] : traces;

        LinkedHashSet<String> schemaNames = new LinkedHashSet<String>();
        for (HeaderFieldDef f : schema.getFields()) schemaNames.add(f.getName());

        LinkedHashSet<String> extras = new LinkedHashSet<String>();
        for (SeismicTrace t : this.sampleTraces)
        {
            for (String name : t.getHeaderList())
            {
                if (name != null && !schemaNames.contains(name)) extras.add(name);
            }
        }
        this.extraHeaderNames = new ArrayList<String>(extras);

        fireTableDataChanged();
    }

    /** rows before this index are schema-backed and editable; rows at/after it are the extra (non-schema, read-only) rows */
    public int getEditableRowCount()
    {
        return schema.getFields().size();
    }

    public void addRow()
    {
        schema.getFields().add(new HeaderFieldDef("NEW_HEADER", 0, HeaderFieldDef.FieldType.INT32));
        int newRowIndex = schema.getFields().size() - 1; // schema rows always come before the extra (non-schema) rows
        fireTableRowsInserted(newRowIndex, newRowIndex);
    }

    public void removeRow(int row)
    {
        if (row < 0 || row >= schema.getFields().size()) return; // out of range, or an extra (non-schema) row - nothing to remove
        schema.getFields().remove(row);
        fireTableRowsDeleted(row, row);
    }
}
