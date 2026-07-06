package com.tricongeophysics;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Locale;

public class DateDbColumn extends DbColumn {

	public DateDbColumn(ResultSet rs, int i, String name) {
		super(rs, i, name);
	}

	@Override
	public void setValueAt(int row, Object newVal) throws Exception {
		if (newVal == null || newVal.equals("")) {
			resultSet.updateObject(columnIndex, null);
			resultSet.updateRow();
		} else {
			resultSet.absolute(row);
			Date date = null;
			try {
				date = Date.valueOf(newVal.toString());
			} catch (IllegalArgumentException e) {
				//java.util.Date d = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.US).parse(newVal.toString());
				long d = java.util.Date.parse(newVal.toString());
				date = new Date(d);
			}
			//resultSet.updateDate(columnIndex, date);
			resultSet.updateObject(columnIndex, date);
			resultSet.updateRow();
			//System.out.println(getValueAt(row));
		}
	}

}
