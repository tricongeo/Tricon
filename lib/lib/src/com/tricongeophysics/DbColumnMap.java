package com.tricongeophysics;

import java.util.ArrayList;

public class DbColumnMap {

	DbParms dbParms;
	private String[] mainColumnList;
	private ArrayList<String[]> columnLists = new ArrayList<String[]>();
	private ArrayList<String> tableNames= new ArrayList<String>();

	public DbColumnMap(DbParms dbParms, String[] columnList) {
		this.dbParms = dbParms;
		this.mainColumnList = columnList;
	}

	public void addMap(String tableName, String[] columnList) {
		columnLists.add(columnList);
		tableNames.add(tableName);
	}

	public DbParms getDbParms() {
		return dbParms;
	}

	public String[] getTableNames() {
		return tableNames.toArray(new String[]{});
	}

	public String[] getMainColumnNames() {
		return mainColumnList;
	}

	public String[] getColumnNames(String table) {
		int index = tableNames.indexOf(table);
		if (index < 0) return null;
		return columnLists.get(index);
	}

	public String getColumnName(String tableName, String columnName) {
		String[] cols = getColumnNames(tableName);
		for (int i=0; i< mainColumnList.length; i++) {
			if (mainColumnList[i].equals(columnName)) {
				return cols[i];
			}
		}
		return null;
	}

}
