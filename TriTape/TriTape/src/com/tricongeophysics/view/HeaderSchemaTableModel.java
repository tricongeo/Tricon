package com.tricongeophysics.view;

import com.tricongeophysics.SeismicTrace;
import com.tricongeophysics.model.HeaderFieldDef;
import com.tricongeophysics.model.HeaderSchema;

import javax.swing.table.AbstractTableModel;

/**
 * Editable JTable model backing a HeaderSchema: name / byte offset / type /
 * scalar, plus three read-only "sample value" columns showing what each
 * header actually decodes to for the first 3 traces of a file - populated by
 * setSampleTraces(), called after "Load Headers" reads a batch of traces.
 */
public class HeaderSchemaTableModel extends AbstractTableModel
{
    private static final String[] COLUMNS = {
        "Header Name", "Byte Offset (1-based)", "Type", "Scalar (SEG-Y only)",
        "Trace 1", "Trace 2", "Trace 3"
    };
    private static final int FIRST_SAMPLE_COLUMN = 4;

    private final HeaderSchema schema;
    private SeismicTrace[] sampleTraces = new SeismicTrace[0];

    public HeaderSchemaTableModel(HeaderSchema schema)
    {
        this.schema = schema;
    }

    @Override public int getRowCount() { return schema.getFields().size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }
    @Override public boolean isCellEditable(int row, int col) { return col < FIRST_SAMPLE_COLUMN; }

    @Override
    public Class<?> getColumnClass(int col)
    {
        switch (col)
        {
            case 0: return String.class;
            case 1: return Integer.class;
            case 2: return HeaderFieldDef.FieldType.class;
            case 3: return HeaderFieldDef.ScalarType.class;
            default: return String.class; // sample-value columns are display-only formatted text
        }
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        HeaderFieldDef f = schema.getFields().get(row);
        switch (col)
        {
            case 0: return f.getName();
            case 1: return f.getByteOffset() + 1;
            case 2: return f.getType();
            case 3: return f.getScalarType();
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
        if (col >= FIRST_SAMPLE_COLUMN) return; // sample-value columns are read-only
        HeaderFieldDef f = schema.getFields().get(row);
        switch (col)
        {
            case 0: f.setName(String.valueOf(value)); break;
            case 1: f.setByteOffset(Math.max(1, ((Number) value).intValue()) - 1); break;
            case 2: f.setType((HeaderFieldDef.FieldType) value); break;
            case 3: f.setScalarType((HeaderFieldDef.ScalarType) value); break;
            default: break;
        }
        fireTableCellUpdated(row, col);
    }

    /** sets the (up to 3) sample traces whose header values populate the "Trace 1/2/3" columns */
    public void setSampleTraces(SeismicTrace[] traces)
    {
        this.sampleTraces = traces == null ? new SeismicTrace[0] : traces;
        fireTableDataChanged();
    }

    public void addRow()
    {
        schema.getFields().add(new HeaderFieldDef("NEW_HEADER", 0, HeaderFieldDef.FieldType.INT32));
        int last = getRowCount() - 1;
        fireTableRowsInserted(last, last);
    }

    public void removeRow(int row)
    {
        if (row < 0 || row >= getRowCount()) return;
        schema.getFields().remove(row);
        fireTableRowsDeleted(row, row);
    }
}
