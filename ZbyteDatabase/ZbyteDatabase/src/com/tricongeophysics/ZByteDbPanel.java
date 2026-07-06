package com.tricongeophysics;

import java.sql.SQLException;

public class ZByteDbPanel extends DbSpreadsheetPanel {

	public ZByteDbPanel(DbParms dbParms) throws SQLException {
		super(dbParms);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String[] getEditableColumns() {
		// TODO Auto-generated method stub
		return model.getColumnNames();
	}
}
