package com.tricongeophysics;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class DenseMatrixSpreadsheetModel extends DefaultTableModel {
	
	int MaxNumRows = 3000000;
	int MaxNumCols = 1024;
	@Override
	public void setValueAt(Object val, int row, int col) {
		
		if (row < 0 || col < 0) return;
		if (row >= getRowCount()) this.setRowCount(row+1);
		if (col >= getColumnCount()) this.setColumnCount(col+1);
		super.setValueAt(val, row, col);
	}
	
}
