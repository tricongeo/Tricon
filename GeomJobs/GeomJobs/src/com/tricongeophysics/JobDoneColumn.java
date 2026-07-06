package com.tricongeophysics;

import java.sql.Date;
import java.sql.ResultSet;

public class JobDoneColumn extends CheckWarningDateColumn {

	public JobDoneColumn(DatabaseModel model, String name,
			String checkColumnName) {
		super(model, name, checkColumnName);
		// TODO Auto-generated constructor stub
	}

	public JobDoneColumn(DatabaseModel model) {
		super(model, "Done", "Date Finished");
	}

}