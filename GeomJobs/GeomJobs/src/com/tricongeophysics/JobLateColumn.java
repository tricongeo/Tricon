package com.tricongeophysics;

import java.sql.Date;
import java.sql.ResultSet;

public class JobLateColumn extends DbColumn {

	private DatabaseModel model;

	public JobLateColumn(ResultSet rs, int i, String name) {
		super(rs, i, name);
		// TODO Auto-generated constructor stub
	}

	public JobLateColumn(DatabaseModel model) {
		this(model.getResultSet(), 1, "Late");
		this.model = model;
	}
	
	/**
	 * returns whether a job is late based on the current date
	 * and expected completion date.
	 */
	public Object getValueAt(int row) throws Exception {
		resultSet = model.getResultSet();
		resultSet.absolute(row);
		Date done = resultSet.getDate("Date Finished");
		Date expected = resultSet.getDate("Finish By");
		Date today = new Date(System.currentTimeMillis());
		if (expected == null) return false; //no expectation date, so we're fine
		if (done != null) return false; //job done - it's not late
		if (today.after(expected)) return true; //job not done and today is later than expectation - it's late!!!
		return false;
	}

	/**
	 * does nothing
	 */
	public void setValueAt(int row, Object newVal) throws Exception {}
	
	/**
	 * make cells highlight red if job is late
	 */
	@Override
	public boolean containsError(int modelRow) {
		boolean late = false;
		try {
			late = getValueAt(modelRow).equals(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return late;
	}
}
