package com.tricongeophysics.view;

import com.tricongeophysics.model.HeaderFieldDef;
import com.tricongeophysics.model.HeaderSchema;

import javax.swing.table.AbstractTableModel;

/** editable JTable model backing a HeaderSchema: name / byte offset / type / scalar */
public class HeaderSchemaTableModel extends AbstractTableModel
{
    private static final String[] COLUMNS = {"Header Name", "Byte Offset", "Type", "Scalar (SEG-Y only)"};
    private final HeaderSchema schema;

    public HeaderSchemaTableModel(HeaderSchema schema)
    {
        this.schema = schema;
    }

    @Override public int getRowCount() { return schema.getFields().size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }
    @Override public boolean isCellEditable(int row, int col) { return true; }

    @Override
    public Class<?> getColumnClass(int col)
    {
        switch (col)
        {
            case 0: return String.class;
            case 1: return Integer.class;
            case 2: return HeaderFieldDef.FieldType.class;
            case 3: return HeaderFieldDef.ScalarType.class;
            default: return Object.class;
        }
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        HeaderFieldDef f = schema.getFields().get(row);
        switch (col)
        {
            case 0: return f.getName();
            case 1: return f.getByteOffset();
            case 2: return f.getType();
            case 3: return f.getScalarType();
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        HeaderFieldDef f = schema.getFields().get(row);
        switch (col)
        {
            case 0: f.setName(String.valueOf(value)); break;
            case 1: f.setByteOffset(((Number) value).intValue()); break;
            case 2: f.setType((HeaderFieldDef.FieldType) value); break;
            case 3: f.setScalarType((HeaderFieldDef.ScalarType) value); break;
            default: break;
        }
        fireTableCellUpdated(row, col);
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
