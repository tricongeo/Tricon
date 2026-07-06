package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringDbColumn extends DbColumn {

	public StringDbColumn(ResultSet rs, int i, String name) {
		super(rs, i, name);
	}

	@Override
	public void setValueAt(int row, Object newVal) throws Exception {
		if (newVal == null) newVal = "";
		resultSet.absolute(row);
		resultSet.updateString(columnIndex, newVal.toString());
		resultSet.updateRow();
	}

}
