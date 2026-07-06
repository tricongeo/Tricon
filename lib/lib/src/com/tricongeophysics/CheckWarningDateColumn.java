package com.tricongeophysics;

import java.sql.Date;
import java.sql.ResultSet;

public class CheckWarningDateColumn extends DbColumn {

	private DatabaseModel model;
	private String checkColumnName;

	public CheckWarningDateColumn(ResultSet rs, int i, String name) {
		super(rs, i, name);
		// TODO Auto-generated constructor stub
	}
	
	public CheckWarningDateColumn(DatabaseModel model, String name, String checkColumnName) {
		this(model.getResultSet(), 1, name);
		this.model = model;
		this.checkColumnName = checkColumnName;
	}
	
	/**
	 * returns whether a job is done
	 */
	public Object getValueAt(int row) throws Exception {
		resultSet = model.getResultSet();
		resultSet.absolute(row);
		Date done = resultSet.getDate(checkColumnName);
		if (done == null) return false;
		return true;
	}

	/**
	 * sets Date Finished to current day or null
	 */
	public void setValueAt(int row, Object newVal) throws Exception {
		//boolean done = !Boolean.getBoolean(newVal.toString());
		boolean done = newVal.toString().equals("true");
		resultSet = model.getResultSet();
		resultSet.absolute(row);
		if (done) 
			resultSet.updateDate(checkColumnName, new Date(System.currentTimeMillis()));
		else
			resultSet.updateDate(checkColumnName, null);
		resultSet.updateRow();
		model.fireTableRowsUpdated(row-1, row-1);
	}
	
	/**
	 * make cell highlight yellow for incomplete jobs
	 */
	@Override
	public boolean containsWarning(int modelRow) {
		boolean done = false;
		try {
			done = getValueAt(modelRow).equals(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return !done;
	}
}
