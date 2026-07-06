package com.tricongeophysics;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ZbyteJob {

	private String jobNum;
	private ResultSet rs;
	private Date dateReceived;
	private String salesRep;

	public ZbyteJob(Object job) {
		DbParms p = DbParms.read("ZbyteDatabase");
		p.dbTable = ZbyteDatabase.Job;
		p.query = "select * from job";
		p.pkeyIndex = 0;
		jobNum = job.toString();
		try {
			DatabaseModel model = DatabaseModel.getDatabaseModel(p);
			//rs = model.executeQuery("select * from job where id = "+jobNum);
			loadFields();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void loadFields() throws SQLException {
			dateReceived = rs.getDate("Date Received");
			salesRep = rs.getString("Sales Rep");
			
	}

}
