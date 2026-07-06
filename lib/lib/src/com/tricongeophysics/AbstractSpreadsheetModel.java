package com.tricongeophysics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.tricongeophysics.TableData.ColumnType;


public abstract class AbstractSpreadsheetModel extends AbstractTableModel {

    protected Class<? extends Object>[] columnClasses;
	protected ArrayList<ModelChangedListener> modelChangedListeners = new ArrayList<ModelChangedListener>();

	public abstract void addRowColorChangedListener(RowColorChangedListener l);

    public abstract void addColumn(String name, Class<?> colClass);

    public abstract boolean isOptionalColumn(int modelCol);

    public abstract void delColumn(int modelCol);

    public abstract void extrapUp(int[] modelRows, int c);

    public abstract void extrapDown(int[] modelRows, int c);

    public abstract void interpRows(int[] modelRows, int c);

    public abstract void reload();

    public abstract void addRow(int modelRow);

    public abstract void delRows(int[] modelRows);

    public abstract boolean isColEditable(int modelColumn);

    public abstract ColumnType getColumnType(int modelColumn);
    
    @Override
    public abstract Object getValueAt(int row, int col);
	
    @Override
    public abstract void setValueAt(Object newVal, int row, int col);
    
    @Override
    public abstract boolean isCellEditable(int row, int col);

	public String[] getColumnNames() {
		String[] names = new String[getColumnCount()];
		for (int i=0; i<names.length; i++) {
			names[i] = getColumnName(i);
		}
		return names;
	}
	
	@Override
	public Class<? extends Object> getColumnClass(int c) {
		//Store classes in array for faster access
		if (columnClasses == null) {
			columnClasses = new Class[getColumnCount()];
		}
		if (c >= columnClasses.length || c < 0) return Object.class;
		
		//Check if class already loaded
		Class<? extends Object> klass = columnClasses[c];
		if (klass != null) return klass;
		
		//Class not loaded yet, find class from first non-null value
		Object val = getValueAt(0, c);
		int i=1;
		while (i<getRowCount() && val == null) {
			val = getValueAt(i, c);
			i++;
		}
		if (val == null) 
			columnClasses[c] = Object.class; //no non-null values, use Object class
		else
			columnClasses[c] = val.getClass();
		return columnClasses[c];
	}

	public void addModelChangedListener(ModelChangedListener l) {
		modelChangedListeners .add(l);
	}

	public void fireModelChanged() {
		for (ModelChangedListener l: modelChangedListeners) {
			l.modelChanged();
		}
	}

	public abstract boolean containsError(int modelRow);

	public abstract boolean containsWarning(int modelRow);
	
	public void setValueAt(Object newVal, int row, String colName) {
		int col = getColumnIndex(colName);
		setValueAt(newVal, row, col);
	}

	public int getColumnIndex(String colName) {
		String[] names = getColumnNames();
		for (int i=0; i<names.length; i++) {
			if (names[i] == null && colName == null) return i;  
			if (names[i] == null || colName == null) continue;  
			if (names[i].toLowerCase().equals(colName.toLowerCase()))
				return i;
		}
		SUtil.printErr("AbstractSpreadhsheetModel.getColumnIndex() - column not found - " + colName + " (table: " + getTableName() + ")");
		return -1;
	}
	
	public Object getValueAt(int row, String colName) {
		int col = getColumnIndex(colName);
		return getValueAt(row,col);
	}
	
	public Object[] getValues(String colName) {
		int c = getColumnIndex(colName);
		return getValues(c);
	}

	public Object[] getValues(int c) {
		if (c < 0 || c >= getColumnCount()) return null;
		int rc = getRowCount();
		Object[] vals = new Object[rc];
		for (int i=0; i<rc; i++) {
			vals[i] = getValueAt(i, c);
		}
		return vals;
	}

	public abstract String getTableName();
	
	/**
	 * Finds row number in model that matches value val in column with name colName
	 * 
	 * @param colName
	 * @param val
	 * @return modelRow or -1 if column or row not found
	 */
	public int findRow(String colName, Object val) {
		int nRows = getRowCount();
		int col = getColumnIndex(colName);
		if (col < 0) {
			SUtil.printErr("AbstractSpreadsheetModel.findRow() - Column \"" + colName + "\" not found in table \"" + getTableName());
			return -1;
		}
		for (int i=0; i< nRows; i++) {
			Object v = getValueAt(i, col);
			if (v == null) continue;
			if (v.equals(val))
				return i;
		}
		SUtil.printErr("AbstractSpreadsheetModel.findRow() - Row \"" + colName + "\" = \"" + val + "\" not found in table \"" + getTableName());
		return -1;
	}

}