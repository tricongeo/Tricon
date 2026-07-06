package com.tricongeophysics;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;


public class SpreadsheetModel extends AbstractTableModel {

	public static int DEFAULT_ROW_COUNT = 1024;
	public static int DEFAULT_COLUMN_COUNT = 1024;

	//private Map sparseMatrix = new HashMap();
	private Vector denseMatrix = new Vector(new Vector());
	private int maxRow = 0;
	private int maxColumn = 0;

	private Point tmpIndex = new Point(0,0);
	
	public SpreadsheetModel() {
		((Vector)denseMatrix.get(0)).add("");
	}

	public int getRowCount() {
		return DEFAULT_ROW_COUNT;
	}

	public int getColumnCount() {
		return DEFAULT_COLUMN_COUNT;
	}
	public Object getValueAt(int row, int column) {
/*
		tmpIndex.y = row;
		tmpIndex.x = column;
		Object returnVal = sparseMatrix.get(tmpIndex);
		if (returnVal != null) {
			return returnVal;
		} else {
			return "";
		}
		*/
		if (row < 0 || column < 0) return "";
		if (row > maxRow || column > maxColumn) return "";
		Object returnVal = ((Vector)denseMatrix.get(row)).get(column);
		if (returnVal != null) {
			return returnVal;
		} else {
			return "";
		}
	}

	public void setValueAt(Object val, int row, int column) {
		/*
		if (val == null) {
			sparseMatrix.remove(new Point(column, row));
			return;
		}
		maxRow = Math.max(row, maxRow);
		maxColumn = Math.max(column, maxColumn);
		sparseMatrix.put(new Point(column, row), val);
		*/
		if (row < 0 || column < 0) return;
		maxRow = Math.max(row, maxRow);
		maxColumn = Math.max(column, maxColumn);
		denseMatrix.ensureCapacity(row);
		((Vector)denseMatrix.get(row)).ensureCapacity(column);
		((Vector)denseMatrix.get(row)).set(column, val);
	}

	public boolean isCellEditable( int row, int column ) {
		if (row < 0 || column < 0) return false;
		return true;
	}

	public int getMaxRow() {
		return maxRow;
	}

	public int getMaxColumn() {
		return maxColumn;
	}
}