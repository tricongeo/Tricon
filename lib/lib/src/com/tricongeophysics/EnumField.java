package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EnumField extends ComboBoxField {

	public EnumField(String label, Object value) {
		super(label, value);
		// TODO Auto-generated constructor stub
	}

	public EnumField(String colName, Object value, DatabaseModel model) {
		super(colName, value);
		try {
			String type = model.getColumnTrueType(colName);
			if (!type.contains("enum")) {
				SUtil.printErr("EnumField() - column is not of type Enum");
			}
			int beg = type.indexOf("(");
			int end = type.indexOf(")");
			String listString = type.substring(beg+1, end);
			String[] vals2 = listString.split(",");
			for (int i=0; i<vals2.length; i++) {
				vals2[i] = vals2[i].replace("'", "");
			}
			this.setValues(vals2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
