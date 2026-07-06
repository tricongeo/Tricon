package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SelectItemColumn extends DbColumn {

	private DbModelInterface model;
	private int rowCount;
	//private boolean[] vals;
	private ArrayList<Boolean> vals = new ArrayList<Boolean>(); //model may change number of rows, need to be adjustable
	
	private void initializeVals() {
		//vals = new boolean[rowCount+1]; //row numbers actuall start at 1
//		for (int i=0; i< vals.length; i++) {
//			//vals[i] = false;
//			
//		}
	}

//	private int countRows() {
//		try {
//			while (resultSet.next()) rowCount++;
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return rowCount;
//	}

	public SelectItemColumn(DbModelInterface model2) {
		super(model2.getResultSet(), 1, "Select");
		this.model = model2;
		rowCount = model2.getRowCount();
		initializeVals();
	}
	
	public Object getValueAt(int row) throws Exception {
		//if (row < 0 || row >= vals.length) return null;
		//return vals[row];
		//if (row < 0 || row >= vals.size()) return null;
		if (row < 0) return null;
		while (vals.size() <= row) {
			vals.add(false);
		}
		return vals.get(row);
	}
	
	public void setValueAt(int row, Object newVal) throws Exception {
		//if (row < 0 || row >= vals.length) return;
		if (row < 0) return;
		while (vals.size() <= row) {
			vals.add(false);
		}
		String v = newVal.toString();
		if (v.equals("true"))
			//vals[row] = true;
			vals.set(row, true);
		else if (v.equals("false"))
			//vals[row] = false;
			vals.set(row, false);
		else
			SUtil.printErr("SelectItemColumn.setValueAt() - Object: \"" + newVal + "\" is not boolean");
		model.fireTableRowsUpdated(row-1, row-1);
	}
	
	/**
	 * make cell highlight yellow for incomplete jobs
	 */
	@Override
	public boolean containsWarning(int modelRow) {
		int row = modelRow -1;
		//if (row >= vals.length) return false;
		if (row >= vals.size()) return false;
		//return !vals[row];
		return !vals.get(row);
	}
}
