package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public abstract class DbColumn
{

	protected ResultSet resultSet;
	protected int columnIndex;
	protected String name;

	public DbColumn(ResultSet rs, int i, String name) {
		this.resultSet = rs;
		this.columnIndex = i;
		this.name = name;
	}

	public static DbColumn create(ResultSet rs, int i)
	{
		try {
			ResultSetMetaData md = rs.getMetaData();
			String klass = md.getColumnClassName(i);
			String name = md.getColumnName(i);
			return create(rs, i, klass, name);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static DbColumn create(ResultSet rs, int i, String klass, String name)
	{
		if (klass.equals("java.lang.Integer")) {
			return new IntDbColumn(rs, i, name);
		}
		if (klass.equals("java.lang.String")) {
			return new StringDbColumn(rs, i, name);
		}
		if (klass.equals("java.sql.Date")) {
			return new DateDbColumn(rs, i, name);
		}
		if (klass.equals("java.lang.Float")) {
			return new DoubleDbColumn(rs, i, name);
		}
		if (klass.equals("java.lang.Double")) {
			return new DoubleDbColumn(rs, i, name);
		}
		if (klass.equals("java.lang.Boolean")) {
			return new BoolDbColumn(rs, i, name);
		}
		return new DefaultDbColumn(rs, i, name);
	}

	/**
	 * gets value from database for this column and row (row).
	 * Note: row numbering starts at 1, not zero.
	 * 
	 * @param row
	 * @return
	 * @throws SQLException 
	 */
	public Object getValueAt(int row) throws Exception {
		resultSet.absolute(row);
	//	if (resultSet.getConcurrency() == ResultSet.CONCUR_UPDATABLE)
			//resultSet.refreshRow();  //make sure data is synced w/ database before showing!! //this is really slowing down database SWC 10/2/2010
		Object val = resultSet.getObject(columnIndex);
		return val;
	}

	/**
	 * sets value in database for this column and row (row).
	 * Note: row numbering starts at 1, not zero.
	 * 
	 * @param row
	 * @return
	 * @throws SQLException 
	 */
	public void setValueAt(int row, Object newVal) throws Exception {
		resultSet.absolute(row);
		resultSet.updateObject(columnIndex, newVal);
		resultSet.updateRow();
	}

	public String getName() {
		return name;
	}

	/**
	 * hook to return error status based on cell contents.
	 * Used to paint spreadsheet red if data is in error or should be of concern to user.
	 * @param modelRow
	 * @return
	 */
	public boolean containsError(int modelRow) {
		return false;
	}
	
	/**
	 * hook to return warning status based on cell contents.
	 * Used to paint spreadsheet red if data is in unexpected or should be of concern to user.
	 * @param modelRow
	 * @return
	 */
	public boolean containsWarning(int modelRow) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
