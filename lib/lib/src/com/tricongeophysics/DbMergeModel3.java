package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.tricongeophysics.TableData.ColumnType;

public class DbMergeModel3 extends DatabaseModel
{

	private DbColumnMap dbColumnMap;
	private String sql;
	private ArrayList<DatabaseModel> models;
	private String[] columnNames;
	private ArrayList<Object[]> data;
	private int OriginalRowIndex;
    //private ReflectiveTableModel data;
	private int OriginalModelIndex;
	
	
	public ResultSet executeQuery() throws SQLException {
		return executeQuery(null, null);
	}

	public DbMergeModel3(DbColumnMap dbColumnMap) throws SQLException {
		this.dbColumnMap = dbColumnMap;
		resetModel();
	}

    private void addRows(ArrayList<Object[]> data2, DatabaseModel model) {
		int rows = model.getRowCount();
		int cols = columnNames.length;
		String[] modelNames = dbColumnMap.getColumnNames(model.getTableName());
		int length = columnNames.length+2;
		OriginalRowIndex = length - 2;
		OriginalModelIndex = length -1;
		for (int i=0; i<rows; i++) {
			Object[] row = new Object[length];
			for (int j=0; j<cols; j++) {
				String colName = modelNames[j];
				if (colName != null) {
					row[j] = model.getValueAt(i, colName);
				} else {
					row[j] = null;
				}
			}
			row[OriginalRowIndex] = i;
			row[OriginalModelIndex] = model;
			data.add(row);
		}
	}

	@Override
    public void reload()
    {
    	try {
			resetModel();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override
    public void resetModel() throws SQLException {
    	if (models == null) {
    		models = new ArrayList<DatabaseModel>();
    		String[] modelList = dbColumnMap.getTableNames();
    		for (String tableName: modelList) {
    			models.add(DatabaseModel.getDatabaseModel(tableName));
    		}
    	}
    	//data = new ReflectiveTableModel();
    	columnNames = dbColumnMap.getMainColumnNames();
    	if (data == null) data = new ArrayList<Object[]>();
    	data.clear();
    	for (DatabaseModel model: models) {
    		addRows(data, model);
    	}
    	columnClasses = null;
		rowCount = 0;
        getRowCount();
        fireTableDataChanged();
        fireTableStructureChanged();
        fireModelChanged();   
    }
    
    @Override
    public int getColumnCount() {
    	return dbColumnMap.getMainColumnNames().length;
    }

    @Override
    public void setResultSet(ResultSet rs) {
    	SUtil.printErr("DbMergeModel.setResultSet() not supported");
    	try {
			resetModel();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override
    public void addRow(int row)
    {
       SUtil.printErr("DbMergeModel.addRow() not supported");
    }


	public String getMaxVal(String colName) throws SQLException {
		 SUtil.printErr("DbMergeModel.getMaxVal() not supported");
		 return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (row < 0 || col < 0) return null;
		if (row >= data.size() || col >= columnNames.length) return null;
		return data.get(row)[col];
	}
	
	@Override
    public void delRows(int[] rows)
    {
		SUtil.printErr("DbMergeModel.delRows() not supported");
    }

    void delRow(int row) {
    	SUtil.printErr("DbMergeModel.delRow() not supported");
	}

	@Override
    public void setValueAt(Object newVal, int rowIndex, int columnIndex) {
		//SUtil.printErr("DbMergeModel.setValueAt() not supported");
		data.get(rowIndex)[columnIndex] = newVal;
		DatabaseModel mod = (DatabaseModel) data.get(rowIndex)[OriginalModelIndex];
		int row0 = (Integer) data.get(rowIndex)[OriginalRowIndex];
		String cname = getColumnName(columnIndex);
		String c0 = dbColumnMap.getColumnName(mod.getTableName(), cname);
		mod.setValueAt(newVal, row0, c0);
    }

    @Override
    public ColumnType getColumnType(int modelColumn)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getColumnName(int c) {
    	//return super.getColumnName(c);
    	return this.dbColumnMap.getMainColumnNames()[c];
    }

	/**
	 * try not to use this if you can.
	 * No SQL Injection protection!!
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	protected ResultSet executeSpecialQuery(String sql) throws SQLException {
		SUtil.printErr("DbMergeModel.executeSpecialQuery() not supported: " + sql);
		return null;
	}

	public ResultSet getResultSet() {
		SUtil.printErr("DbMergeModel.getResultSet() not supported");
		return null;
	}

	/**
	 * returns how many times val occurs in column colName within the table
	 * @param colName
	 * @param val
	 * @return
	 * @throws SQLException
	 */
	public int getRowCount(String colName, Object val) throws SQLException {
		int count = 0;
		for (DatabaseModel model: models) {
			String c1 = dbColumnMap.getColumnName(model.getTableName(), colName);
			if (c1 == null && colName != null) continue;
			count += model.getRowCount(c1, val);
		}
		return count;
	}
	
	@Override
    public int getRowCount()
    {
    	//if (rowCount > 0) return rowCount;
		if (data == null) return 0;
        rowCount = data.size();
        return rowCount;
    }

	public String getColumnTrueType(String colName) throws SQLException {
		SUtil.printErr("DbMergeModel.getColumnTrueType() not supported");
		return null;
	}

	@Override
	public String getTableName() {
		return "Merge";
	}
	
	protected String prepareCondition(String colName, Object value) {
		SUtil.printErr("DbMergeModel.prepareCondition() not supported");
		return null;
	}
	
	protected String getSelectSQL(String column2) {
		SUtil.printErr("DbMergeModel.getSelectSQL() not supported");
		return null;
	}

	public String getSelectSQL() {
		SUtil.printErr("DbMergeModel.getSelectSQL() not supported");
		return null;
	}

	/**
	 * Returns first value in table for column2 where column1 = value1
	 * @param string
	 * @param contactAlias
	 * @param string2
	 * @return
	 * @throws SQLException 
	 */
	public Object findRowValue(String column1, Object val1, String column2) throws SQLException {
		for (DatabaseModel model: models) {
			String c1 = dbColumnMap.getColumnName(model.getTableName(), column1);
			String c2 = dbColumnMap.getColumnName(model.getTableName(), column2);
			if (c1 == null && column1 != null) continue;
			if (c2 == null && column2 != null) continue;
			Object val = model.findRowValue(c1, val1, c2);
			if (val != null) return val;
		}
		return null;
	}

	public int getCompoundRowCount(String colName1, Object val1, String colName2, Object val2) throws SQLException {
		int count = 0;
		for (DatabaseModel model: models) {
			String c1 = dbColumnMap.getColumnName(model.getTableName(), colName1);
			String c2 = dbColumnMap.getColumnName(model.getTableName(), colName2);
			if (c1 == null && colName1 != null) continue;
			if (c2 == null && colName2 != null) continue;
			count += model.getCompoundRowCount(c1, val1, c2, val2);
		}
		return count;
	}

	protected static String getSQL(String cond1, String cond2, String string, DbColumnMap dbColumnMap) {
		SUtil.printErr("DbMergeModel.getSQL() not supported");
		return null;
	}

	@Override
	public ResultSet executeCompoundQuery(String column1, Object val1, String column2, Object val2) throws SQLException {
		for (DatabaseModel model: models) {
			String c1 = dbColumnMap.getColumnName(model.getTableName(), column1);
			String c2 = dbColumnMap.getColumnName(model.getTableName(), column2);
			if ((c1 == null && column1 != null) || (c2 == null && column2 != null)) {
				model.setResultSet(null);
				continue;
			}
			ResultSet rs2 = model.executeCompoundQuery(c1, val1, c2, val2);
			model.setResultSet(rs2);
		}
		reload();
		return null;
	}

	@Override
	public Object[] findCompoundColumnValues(String column1, Object val1, String column2) throws SQLException {
		//Object[] vals2 = new Object[]{"1", "2"};
		Object[] vals2 = null;
		//SUtil.print("findcompoundcolumnvalues col1 " + column1 + " v1 " + val1 + " c2 " + column2 );
		for (DatabaseModel model: models) {
			String c1 = dbColumnMap.getColumnName(model.getTableName(), column1);
			String c2 = dbColumnMap.getColumnName(model.getTableName(), column2);
			if (c1 == null && column1 != null) continue;
			if (c2 == null && column2 != null) continue;
			Object[] vals = model.findCompoundColumnValues(c1, val1, c2);
			//Object[] vals = new Object[]{"3", "4"};
			//SUtil.print("vals");
			//SUtil.print(vals);
			vals2 = SUtil.arrayCat(vals2, vals);
		}
		//Arrays.sort(vals2);
		//SUtil.print("before sort");
		//SUtil.print(vals2);
		vals2 = SUtil.sort(vals2);
		//SUtil.print("after sort");
		//SUtil.print(vals2);
		vals2 = SUtil.uniq(vals2);
		//SUtil.print("after uniq");
		//SUtil.print(vals2);
		return vals2;
	}
	
	@Override
	public String[] getColumnNames() {
		return dbColumnMap.getMainColumnNames();
	}
	
	@Override
	public String[] getColumnDbNames() {
		return dbColumnMap.getMainColumnNames();
	}
}
