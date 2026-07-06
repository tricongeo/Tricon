package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BoolDbColumn extends DbColumn {

	/**
	 * @param rs
	 * @param i
	 * @param name
	 */
	public BoolDbColumn(ResultSet rs, int i, String name) {
		super(rs, i, name);
	}
	
	/**
	 * doesn't work. we'll try to fix it later
	 * @throws Exception 
	 */
	@Override
	public void setValueAt(int row,  Object newVal) throws Exception {
		resultSet.absolute(row);
		Boolean b = Boolean.valueOf(newVal.toString());
		resultSet.updateBoolean(columnIndex, b);
		resultSet.updateRow();
	}
	
	@Override
	public Object getValueAt(int row) throws Exception {
		resultSet.absolute(row);
		resultSet.refreshRow();  //make sure data is synced w/ database before showing!!
		
		Object val = resultSet.getObject(columnIndex);
	//	boolean val = resultSet.getBoolean(columnIndex);
	//	Byte bb = resultSet.getByte(columnIndex);
	//	boolean val = false;
	//	if (bb == 1) val = true;
		return val;
	}

}
