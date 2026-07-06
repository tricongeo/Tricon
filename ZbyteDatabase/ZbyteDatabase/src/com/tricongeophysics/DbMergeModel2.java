package com.tricongeophysics;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.event.TableModelListener;

import com.tricongeophysics.TableData.ColumnType;

public class DbMergeModel2 extends DatabaseModel
{

	private DbColumnMap dbColumnMap;
	private String sql;

	public ResultSet executeQuery() throws SQLException {
		return executeQuery(null, null);
	}

	public DbMergeModel2(DbColumnMap dbColumnMap) throws SQLException {
		//super(dbColumnMap.getMainColumnNames(), getSubModel(dbColumnMap));
		//this(dbColumnMap.dbParms.url , dbColumnMap.dbParms.db , dbColumnMap.dbParms.dbTable , dbColumnMap.dbParms.query, dbColumnMap.dbParms.user , dbColumnMap.dbParms.pword, dbColumnMap.dbParms.pkeyIndex );
		this.dbColumnMap = dbColumnMap;
		this.dbParms2 = dbColumnMap.dbParms;
		this.connectionURL = dbParms2.url+"/"+dbParms2.db;
		this.query = dbParms2.query;
		this.tableName = dbParms2.dbTable;
		properties = new Properties();
		properties.setProperty("user", dbParms2.user);
		properties.setProperty("password", dbParms2.pword);
		properties.setProperty("useServerPrepStmts", "false");
		properties.setProperty("holdResultsOpenOverStatementClose", "true");
		properties.setProperty("autoReconnect", "true");
		properties.setProperty("jdbcCompliantTruncation", "false");
		this.pkeyIndex = pkeyIndex;
		
		connection = getConnection(connectionURL, properties);

		rs = executeQuery();
		resetModel();
	}

	private static Connection getConnection(String url, Properties prop) throws SQLException {
    	if (connection == null) {
    		SUtil.print("Openning new connection: " + url);
    		connection = DriverManager.getConnection(url, prop);
    	}
    	if (connection.isClosed()) {
    		SUtil.print("Re-openning connection: " + url);
    		connection = DriverManager.getConnection(url, prop);
    	} else {
    		SUtil.print("Connection already open: " + url);
    	}
    	return connection;
	}

	@Override
    public void addRowColorChangedListener(RowColorChangedListener l)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addColumn(String name, Class<?> colClass)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isOptionalColumn(int modelCol)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void delColumn(int modelCol)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void extrapUp(int[] modelRows, int i)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void extrapDown(int[] modelRows, int i)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void interpRows(int[] modelRows, int i)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void reload()
    {
    	try {
			//rs = statement.executeQuery(query);
    		rs = executeQuery();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
        //resetModel();
        try {
			String n = rs.getMetaData().getTableName(1);
		//	System.out.println(n + ":" +rowCount);
		} catch (SQLException e) {
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
    public void delRows(int[] rows)
    {
		SUtil.printErr("DbMergeModel.delRows() not supported");
    }

    void delRow(int row) {
    	SUtil.printErr("DbMergeModel.delRow() not supported");
	}

	@Override
    public void setValueAt(Object newVal, int rowIndex, int columnIndex) {
		SUtil.printErr("DbMergeModel.setValueAt() not supported");
    }

	@Override
    public boolean isColEditable(int modelColumn)
    {
       return false;
    }
    
    @Override
	public boolean isCellEditable(int row, int col) {
		return false;
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

	public void setEditableColumns(String[] editableColNames) {
		
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
		return rs;
	}

	/**
	 * returns how many times val occurs in column colName within the table
	 * @param colName
	 * @param val
	 * @return
	 * @throws SQLException
	 */
	public int getRowCount(String colName, Object val) throws SQLException {
		SUtil.printErr("DbMergeModel.getRowCount() not supported");
		return 0;
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
		String sql = "";
		if (colName == null) return null;
		sql += "`" + colName + "`";
		if (value == null) {
			sql += " is NULL";
		} else {
			value = value.toString().replace("*", "%");
			if (value.toString().contains("%")) {
				sql += " like ?";
			} else {
				sql += " = ?";
			}
		}
		return sql;
	}
	
	protected String getSelectSQL(String column2) {
		return "select `" + column2 + "` from " + getTableName();
	}

	public String getSelectSQL() {
		//return "select * from " + getTableName();
		if (query == null) {
			query = "select * from " + getTableName();
		}
		return query;
	}

	/**
	 * Returns first value in table for column column2 where column1 = value1
	 * @param string
	 * @param contactAlias
	 * @param string2
	 * @return
	 * @throws SQLException 
	 */
	public Object findRowValue(String column1, Object val1, String column2) throws SQLException {
		Object[] vals = findCompoundColumnValues(column1, val1, column2);
		if (vals == null || vals.length < 1) return null;
		return vals[0];
	}

	public Object[] findColumnValues(String colName) throws SQLException {
		return findCompoundColumnValues(null, null, colName);
	}

	public int getCompoundRowCount(String colName1, Object val1, String colName2, Object val2) throws SQLException {
		if (colName1 == null)
			return getRowCount(colName2, val2);
		if (colName2 == null)
			return getRowCount(colName1, val1);
		
		String condition1 = prepareCondition(colName1, val1);
		String condition2 = prepareCondition(colName2, val2);
		String sql = "select count(*) from `" + getTableName() + "` where " + condition1 
		    + " && " + condition2;
		ResultSet rs2 = executeSpecialQuery(sql);
		rs2.first();
		Object o = rs2.getObject(1);
		if (o == null) return 0;
		int count = (int) SUtil.sval(o.toString());
		return count;
	}

	protected static String getSQL(String cond1, String cond2, String string, DbColumnMap dbColumnMap) {
		String sql = "";
		String[] tables = dbColumnMap.getTableNames();
		String[] mainColNames = dbColumnMap.getMainColumnNames();
		for (int i=0; i<tables.length; i++){
			String table = tables[i];
			String[] colNames = dbColumnMap.getColumnNames(table);
			if (i>0) sql += " union ";
			sql += "(select ";
			for (int j=0; j<colNames.length; j++) {
				String col = colNames[j];
				col = (col == null) ? " NULL" : " `" + col + "`";
				sql += col + " as `" + mainColNames[j] + "`";
				if (j<colNames.length-1) sql += ",";
			}
			sql += " from " + table;
			if (cond1 == null) {

			} else {
				sql += " where " + cond1;
			}
			if (cond2 == null) {

			} else {
				if (cond1 != null) {
					sql += " && ";
				} else {
					sql += " where ";
				}
				sql += cond2;
			}
			if (string != null) sql += string;
			sql += ")";
		}
		return sql;
	}

	@Override
	public ResultSet executeCompoundQuery(String column1, Object val1, String column2, Object val2) throws SQLException {
		SUtil.print("Col1: " + column1 + " val1: " + val1 + " Col2: " + column2 + " val2: " + val2);
		if (connection == null) {
			SUtil.printErr("DatabaseModel.executeQuery(): null connection!!");
			return null;
		}
		String cond1 = prepareCondition(column1, val1);
		String cond2 = prepareCondition(column2, val2);

		sql = getSQL(cond1, cond2, null, dbColumnMap);

		PreparedStatement pstmt = connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		int nCond = 1;
		if (val1 != null && val2 != null) nCond = 2;
		for (int i=0; i<dbColumnMap.getTableNames().length; i++) {
			if (val1 != null) {
				val1 = val1.toString().replace("*", "%");
				pstmt.setString( nCond*i+1, val1+"");
			}
			if (val2 != null && val1 != null) {
				val2 = val2.toString().replace("*", "%");
				pstmt.setString( nCond*i+2, val2+"");
			}
			if (val2 != null && val1 == null) {
				val2 = val2.toString().replace("*", "%");
				pstmt.setString( nCond*i+1, val2+"");
			}
		}
		System.out.println("Executing Merge query: " + sql + " :: v1: " + val1 + " v2: " + val2 + " Table: " + getTableName() + " model: " + this);
		//            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		//            for (StackTraceElement e: trace) {
		//            	SUtil.print(e.toString());
		//            }
		return pstmt.executeQuery();			
	}

	public Object[] findCompoundColumnValues(String column1, Object val1, String column2) throws SQLException {
		if (connection == null) {
			SUtil.printErr("DatabaseModel.executeQuery(): null connection!!");
			return null;
		}
		if (column2 == null) {
			SUtil.printErr("DatabaseModel.findCompoundFieldValues - Can't find values of null column!");
			return null;
		}
		String cond1 = prepareCondition(column1, val1);

		sql = getSQL(cond1, null, " group by `" + column2 + "`", dbColumnMap);

		PreparedStatement pstmt = connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		for (int i=0; i<dbColumnMap.getTableNames().length; i++) {
			if (val1 != null) {
				val1 = val1.toString().replace("*", "%");
				pstmt.setString( i+1, val1+"");
			}
		}

		System.out.println("Executing Merge query: " + sql + " :: v1: " + val1 + " Table: " + getTableName());

		ResultSet rs2 = pstmt.executeQuery();
		int nVals = 0;
		while(rs2.next()) {
			//if (rs.getObject(1) == null) continue;
			nVals++;
		}
		Object[] vals = new Object[nVals];
		int i=0;
		rs2.beforeFirst();
		while(rs2.next()) {
			//if (rs2.getObject(1) == null) {
			if (rs2.getObject(column2) == null) {
				vals[i++] = null;
			} else {
				//vals[i++] = rs2.getObject(1);
				vals[i++] = rs2.getObject(column2);
			}
		}
		return vals;
	}
	
	public String[] getColumnNames() {
		return dbColumnMap.getMainColumnNames();
	}
}
