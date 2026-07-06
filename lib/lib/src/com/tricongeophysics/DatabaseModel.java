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

import com.mysql.jdbc.NotUpdatable;
import com.tricongeophysics.TableData.ColumnType;

public class DatabaseModel extends AbstractSpreadsheetModel implements DbModelInterface
{
    private static ArrayList<DatabaseModel> loadedModels = new ArrayList<DatabaseModel>();
	protected String connectionURL;
    protected static Connection connection;
	protected static DbParms dbParms2;
    protected ResultSet rs;
    private ResultSetMetaData md;
    protected DbColumn[] dbColumns;
	protected int rowCount;
	private boolean[] editableColumns;
	private ArrayList<SQLErrorListener> sqlErrorListeners = new ArrayList<SQLErrorListener>();
	private ArrayList<DbColumn> optionalCols = new ArrayList<DbColumn>();
	private DbColumn errorColumn;
	private DbColumn warningColumn;
	protected int pkeyIndex;
	//private Statement statement;
	protected String query;
	protected String tableName;
	//private String[] hideColumns = new String[] {};
	protected Properties properties;
    
	/**
	 * private constructor. use getDatabaseModel()
	 * @param connectionURL2
	 * @param dbName
	 * @param query
	 * @param user
	 * @param passwd
	 * @param pkeyIndex
	 * @throws SQLException
	 */
	private DatabaseModel(String connectionURL2, String dbName, String tableName, String query, String user, String passwd, int pkeyIndex) throws SQLException
	{
		this.connectionURL = connectionURL2+"/"+dbName;
		this.query = query;
		this.tableName = tableName;
		properties = new Properties();
		properties.setProperty("user", user);
		properties.setProperty("password", passwd);
		properties.setProperty("useServerPrepStmts", "false");
		properties.setProperty("holdResultsOpenOverStatementClose", "true");
		properties.setProperty("autoReconnect", "true");
		properties.setProperty("jdbcCompliantTruncation", "false");
		this.pkeyIndex = pkeyIndex;
		
		connection = getConnection(connectionURL, properties);

//		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
//		System.out.println("Excecuting query: " + query);
//		rs = statement.executeQuery(query);
		//rs = executeQuery();
		rs = this.executeSpecialQuery("select * from " + tableName + " limit 100"); //just get the first row
		resetModel();
	}

	public ResultSet executeQuery() throws SQLException {
		return executeQuery(null, null);
	}

	public DatabaseModel() {
		// TODO Auto-generated constructor stub
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
    	if (connection == null) {
    		SUtil.print("\nnull Connection! \n Connection \""+url+"\" failed!\n");
    	}
    	return connection;
	}

	protected void resetModel() throws SQLException {
    	if (rs == null) {
    		SUtil.printErr("can't reset model - no result set!!");
    		rowCount = 0;
    		return;
    	}
    	try {
			md = rs.getMetaData();
		} catch (Exception e) {
			fireExceptionOccured(e);
			md = null;
		}
		columnClasses = null;
		rowCount = 0;
        loadColumns();
        getRowCount();
        fireTableDataChanged();
        fireTableStructureChanged();
        fireModelChanged();   
	}

	/**
	 * TODO - get rid of hidden columns
	 * hidden columns are bad idea as many views use same model and same time
	 * and may want different columns visible. This should be handled at view/controller
	 * level, not model.
	 */
    private void loadColumns()
    {
    	if (rs == null) return;
    	int nCols=0;
    	try {
    		nCols = md.getColumnCount();
    	} catch (SQLException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	dbColumns = new DbColumn[nCols];
    	for (int i=0; i<nCols; i++) {  //columns actually start at 1, not zero
    		DbColumn col = DbColumn.create(rs, i+1);
    		dbColumns[i] = col; 
    	}
    }

    @Override
    public int getColumnCount()
    {
    	if (md == null) return 0;
        try {
            return md.getColumnCount() + optionalCols.size();
        } catch (SQLException e) {
            fireExceptionOccured(e);;
            return 0;
        }
    }

    @Override
    public int getRowCount()
    {
    	if (rowCount > 0) return rowCount;
        rowCount = 0;
        if (rs == null) return 0;
        try {
            rs.beforeFirst();
            while (rs.next()) rowCount++;
            rs.beforeFirst();
        } catch (Exception e) {
            fireExceptionOccured(e);;
        }
        return rowCount;
    }

    public void fireExceptionOccured(Exception e) {
		for (SQLErrorListener l: sqlErrorListeners) {
			l.handleException(e);
		}
	}

	@Override
    public Object getValueAt(int row, int col)
    {
    	if (dbColumns == null) return null;
    	if (row >= rowCount) return null;
    	if (row < 0) return null;
    	if (col < 0) return null;

    	try {
    		if (col >= dbColumns.length) 
    			return getOptionalValueAt(row+1, col - dbColumns.length) ;
    		DbColumn c = dbColumns[col];
    		if (c == null) return null;
    		try {
    			return c.getValueAt(row+1);
    		} catch (NotUpdatable e) { //serious error = we have a table that can't be updated!
    			e.printStackTrace();
    			return null;
    		} catch (Exception e) {
    			reconnect();
    			return c.getValueAt(row+1);
    		}
    	} catch (Exception e) {
    		fireExceptionOccured(e);;
    		return null;
    	}
    	//        try {
    	//            rs.absolute(row+1);
    	//            String val = rs.getString(col+1);
    	//            return val;
    	//        } catch (Exception e) {
    	//            // TODO Auto-generated catch block
    	//            e.printStackTrace();
    	//            return "";
    	//        }
    }

	private void reconnect() throws SQLException {
		connection = getConnection(this.connectionURL, properties);

//		statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
//		System.out.println("Excecuting query: " + query);
		rs = executeQuery();
	}

	private Object getOptionalValueAt(int row, int col) throws Exception {
		if (col >= optionalCols.size()) return null;
		DbColumn c = optionalCols.get(col);
		if (c == null) return null;
		return c.getValueAt(row);
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
        try {
        	resetModel();
			String n = rs.getMetaData().getTableName(1);
		//	System.out.println(n + ":" +rowCount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    @Override
    public void addRow(int row)
    {
        try {
            //rs.absolute(modelRow+1);
        	Object newVal = calcNewPkeyVal();
        	rs.first();
        	rs.moveToInsertRow();
        	rs.updateObject(1, newVal);
        	rs.insertRow();
//        	rs.absolute(getRowCount());
//        	for (int i=0; i<md.getColumnCount(); i++) {
//        		//if (i == pkeyIndex)
//        		if (i == pkeyIndex)
//        			rs.updateObject(i+1, calcNewPkeyVal());
//        		else 
//        			rs.updateObject(i+1, null);
//        	}
//            rs.updateRow();
        //	rs.moveToInsertRow();
        //	rs.insertRow();
            rowCount++;
          //  this.fireTableDataChanged();
         //   this.fireTableRowsInserted(0, 0);
            this.fireModelChanged();
        } catch (Exception e) {
            fireExceptionOccured(e);;
        }
    }
    
    /**
	 * uses sort to find highest current pkey value.
	 * afterwards, it adds 1 to the previous value,
	 * assuming that the first two characters are non-numerical
	 * @return
	 */
	public Object calcNewPkeyVal() {
//		Object[] keys = getAllModelPkeys();
//		if (keys == null || keys. == 0) return 0;
//		Arrays.sort(keys);
//		String biggestVal = keys[keys.length-1].toString();
		String biggestVal = "";
		try {
			biggestVal = getMaxVal(getPkeyName());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		String prefix = "";
		if (biggestVal.length() > 1) {
			prefix = biggestVal.substring(0,2);
		}
		String suffix = biggestVal.replaceFirst(prefix, "");
		NumberFormat nf = new DecimalFormat();
		nf.setMinimumIntegerDigits(suffix.length());
		nf.setGroupingUsed(false);
		int num = 0;
		try {
			num = Integer.parseInt(suffix);
		}
		catch (Exception e) {
			return biggestVal + "0"; //non-numerical pkey, just attempt to make unique
		}
		String newPkey = prefix + nf.format((num+1));
		return newPkey;
	}

	public String getMaxVal(String colName) throws SQLException {
		ResultSet rs2 = executeSpecialQuery("select max(`" + colName + "`) from `" + getTableName() + "`");
		rs2.first();
		Object o = rs2.getObject(1);
		if (o == null) return "";
		return o.toString();
	}

	public String getPkeyName() {
		return getColumnNames()[pkeyIndex];
	}

	@Override
    public void delRows(int[] rows)
    {
    	if (rows == null || rows.length == 0) return;
		Arrays.sort(rows);
		for (int i=0; i<rows.length; i++) {
			int row = rows[i] - i;  //array indexes update immediately for ArrayList.remove(), will be off by one after first delete, etc.
			delRow(row);
		}
    }

    void delRow(int row) {
    	if (row < 0 || row >= rowCount) return;
		try {
			rs.absolute(row+1);
			rs.deleteRow();
			rowCount--;
			this.fireTableRowsDeleted(row, row);
		} catch (Exception e) {
			fireExceptionOccured(e);;
		} 
	}

	@Override
    public void setValueAt(Object newVal, int rowIndex, int columnIndex) {
    	if (dbColumns == null) return;
        if (rowIndex < 0    || rowIndex > rowCount) return;
        if (columnIndex < 0 ) return;

        try {
        	if (columnIndex >= dbColumns.length) 
        		setOptionalValueAt(newVal, rowIndex+1, columnIndex - dbColumns.length);
        	else {
        		dbColumns[columnIndex].setValueAt(rowIndex+1, newVal);
        	}
        } catch (Exception e) {
			fireExceptionOccured(e);
		}
		if (columnIndex == pkeyIndex) {
			this.fireModelChanged();
		}
        
//        try {
//            rs.absolute(rowIndex+1);
//           // rs.u
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void executeUpdate(String sql) throws SQLException {
    	if (connection == null) {
			SUtil.printErr("DatabaseModel.executeQuery(): null connection!!");
			return;
		}
		System.out.println("Executing update: " + sql + " Table: " + getTableName());
		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		statement.executeUpdate(sql);
	}

	private void setOptionalValueAt(Object newVal, int row, int col) throws Exception {
    	if (col > optionalCols.size()) return;
		optionalCols.get(col).setValueAt(row, newVal);
	}

	@Override
    public boolean isColEditable(int modelColumn)
    {
        if (editableColumns == null) return false;
        if (editableColumns.length <= modelColumn) return true;
        if (modelColumn < 0) return false;
        return editableColumns[modelColumn];
    }
    
    @Override
	public boolean isCellEditable(int row, int col) {
		return isColEditable(col);
	}

    @Override
    public ColumnType getColumnType(int modelColumn)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getColumnName(int c) {
    	if (dbColumns == null) return null;
    	//if (c < 0) return null;
    	if (c < dbColumns.length) {
    		DbColumn cc = dbColumns[c];
    		if (cc == null) return "";
    		return dbColumns[c].getName();
    	}
    	int c2 = c - dbColumns.length;
    	if (c2 < optionalCols.size())
    		return optionalCols.get(c2).getName();
    	else
    		return null;
    }

	public void setEditableColumns(String[] editableColNames) {
		if (editableColNames == null) {
			editableColumns = null;
			return;
		}
		editableColumns = new boolean[getColumnCount()];
		for (int i=0; i<editableColumns.length; i++) {
			editableColumns[i] = false;
			for (int j=0; j<editableColNames.length; j++) {
				if (getColumnName(i).equals(editableColNames[j])) {
					editableColumns[i] = true;
				}
			}
		}
	}

	public void addSQLErrorListener(SQLErrorListener l) {
		if (l == null) return;
		sqlErrorListeners .add(l);
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
		if (connection == null) {
			SUtil.printErr("DatabaseModel.executeQuery(): null connection!!");
			return null;
		}
		System.out.println("Executing SPECIAL query: " + sql + " Table: " + getTableName());
		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		return statement.executeQuery(sql);
	}

	public ResultSet executeQuery(String colName, Object value) throws SQLException {
//		if (connection == null) {
//			SUtil.printErr("DatabaseModel.executeQuery(): null connection!!");
//			return null;
//		}
//		String sql = "select * from " + getTableName();
//		if (colName != null) {
//			sql += " where `" + colName + "`";
//			if (value == null) {
//				sql += " is NULL";
//			} else {
//				value = value.toString().replace("*", "%");
//				if (value.toString().contains("%")) {
//					sql += " like ?";
//				} else {
//					sql += " = ?";
//				}
//			}
//		}
//		PreparedStatement pstmt = connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
//		if (value != null) pstmt.setString( 1, value+"");
//
//		System.out.println("Executing query: " + sql + " :: value: " + value + " Table: " + getTableName());
//
//		return pstmt.executeQuery();
		return executeCompoundQuery(null, null, colName, value);
	}

	public void setResultSet(ResultSet rs2) {
		rs = rs2;
		try {
			resetModel();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addOptionalColumn(DbColumn column) {
		if (!columnExists(column))
			optionalCols.add(column);
	}

	private boolean columnExists(DbColumn column) {
		if (optionalCols == null) return false;
		for (DbColumn col: optionalCols) {
			if (col.name.equals(column.name)) {
				return true;
			}
		}
		return false;
	}

	public ResultSet getResultSet() {
		return rs;
	}

	/**
	 * creates new row in database using text.
	 * Fields in text are assumed to be tab delimited.
	 * @param text
	 * @throws Exception 
	 */
	public void addRowFromText(String text) throws Exception {
		if (rs == null || md == null) return;
		if (text == null) return;
		String[] lines = text.split("\n");
		if (lines == null) return;
		for (String line:lines) {
			String[] fields = line.split("\t");
			if (fields == null || fields.length < 2) {
				throw new Exception("DatabaseModel.addRowFromText() - no tabs in text");
			}

			int length = Math.min(fields.length, md.getColumnCount());
			rs.moveToInsertRow();
			for (int i=0; i<length; i++) {
				rs.updateObject(i+1, fields[i].trim());
			}
			rs.insertRow();
			this.fireTableDataChanged();
			//this.fireTableRowsInserted(0, 1);
			this.fireModelChanged();
			rowCount++;
		}
	}

	public String[] getColumnDbNames() {
		if (dbColumns == null) return null;
		String[] names = new String[dbColumns.length];
		for (int i=0; i<dbColumns.length; i++) {
			names[i] = dbColumns[i].getName();
		}
		return names;
	}

	@Override
	public boolean containsError(int modelRow) {
		if (errorColumn == null) return false;
		return errorColumn.containsError(modelRow+1);
	}

	@Override
	public boolean containsWarning(int modelRow) {
		if (warningColumn == null) return false;
		return warningColumn.containsWarning(modelRow+1);
	}

	public void setErrorColumn(DbColumn c) {
		errorColumn = c;
	}

	public void setWarningColumn(DbColumn c) {
		warningColumn = c;
	}

	public Object[] getPkeys() {
		if (getRowCount() < 2) reload(); //perhaps we haven't loaded data yet (model only initialized)
		Object[] keys = getValues(pkeyIndex);
		//Arrays.sort(keys);
		return keys;
	}

	public void setPkeyName(String pkeyName2) {
		//pkeyIndex = this.getColumnIndex(pkeyName2);
	}

	/**
	 * Uses Singleton Pattern to make sure only one version of every table gets loaded.
	 * Uses lazy loading style. Database tables are only loaded when asked for and
	 * kept in memory.
	 * 
	 * Important: dbParms should be explicitly set before running! dbParms tends to be
	 * a singleton (only one pointer) and constantly getting reset by other objects.
	 * In order to get the model you want, you should make sure dbParms hasn't been
	 * set by another object before executing this method!.
	 * @param dbParms
	 * @return
	 * @throws SQLException
	 */
	public static DatabaseModel getDatabaseModel(DbParms dbParms) throws SQLException {
		for(DatabaseModel m:loadedModels) {
			if (m.getTableName().equals(dbParms.dbTable))
				return m;
		}
		return loadTable(dbParms);
	}

	private static DatabaseModel loadTable(DbParms dbParms) throws SQLException {
		String cp = System.getenv("CLASSPATH");
		DatabaseModel.dbParms2 = dbParms;
		//SUtil.print("Classpath is: " + cp);
		DatabaseModel model;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			model = new DatabaseModel(dbParms.url , dbParms.db , dbParms.dbTable , dbParms.query, dbParms.user , dbParms.pword, dbParms.pkeyIndex );
			model.setTableName(dbParms.dbTable);
			loadedModels .add(model);
		} catch (SQLException e) {
			String m = "URL: "+dbParms.url + " \n" + e.getMessage();
			throw new SQLException(m);
		} catch (Exception e) {
			SUtil.printErr("Failed to find MySQL Connector/J driver");
			throw new SQLException();
		}
		return model;
	}

	public String getTableName() {
		return tableName;
	}

	void setTableName(String dbTable) {
		tableName = dbTable;
	}

	public static String[] getTableList(String applicationName) throws SQLException {
		DbParms p = DbParms.read(applicationName);
		DatabaseModel model = DatabaseModel.getDatabaseModel(p);
		ResultSet rs2 = model.executeSpecialQuery("show tables");
		int nRows = 0;
		rs2.beforeFirst();
		while (rs2.next())
			nRows++;
		String[] tableList = new String[nRows];
		
		rs2.beforeFirst();
		int i=0;
		while (rs2.next()) {
			tableList[i++] = rs2.getString(1);
		}
		
		return tableList;
	}

	public Object getColumnDataType(String colName) {
		int c = this.getColumnIndex(colName);
		try {
			Object s = md.getColumnTypeName(c+1);
			return s;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

//	public void setHideColumns(String[] cols) {
//		hideColumns = cols;
//		resetModel();
//	}
	
	@Override
	public Class getColumnClass(int col) {
		String colName = getColumnName(col);
		if (colName == null) { 
			SUtil.printErr("No Column Name for Column Index: " + col);
			return null;
		}
		if (colName.toLowerCase().contains("file")){
			return File.class;
		}
//		else if (colName.toLowerCase().contains("date")){
//			return Date.class;
//		}
		else if (colName.toLowerCase().contains("password")){
			return Password.class;
		}
		return super.getColumnClass(col);
	}

	public boolean[] getEditableColumns() {
		return editableColumns;
	}

	public void setEditableColumns(boolean[] editableColumns2) {
		editableColumns = editableColumns2;
		//this.fireTableDataChanged();
	}

	public Connection getConnection() {
		return connection;
	}

	/**
	 * returns how many times val occurs in column colName within the table
	 * @param colName
	 * @param val
	 * @return
	 * @throws SQLException
	 */
	public int getRowCount(String colName, Object val) throws SQLException {
		String condition = prepareCondition(colName, val);
		String sql = "select count(*) from `" + getTableName() + "` where " + condition;
		PreparedStatement pstmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		if (val != null) {
			val = val.toString().replace("*", "%");
			pstmt.setObject(1, val);
		}
		SUtil.print("Executing query: " + sql + " :: value: " + val + " Table: " + getTableName());
		ResultSet rs2 = pstmt.executeQuery();
		rs2.first();
		Object o = rs2.getObject(1);
		if (o == null) return 0;
		int count = (int) SUtil.sval(o.toString());
		SUtil.print("found " + o + " rows");
		return count;
	}

	public String getColumnTrueType(String colName) throws SQLException {
		ResultSet rs2 = executeSpecialQuery("show columns from " + getTableName() + " where `Field` = '" + colName + "'");
		rs2.next();
		Object type = rs2.getObject("Type");
		return type.toString();
	}

	public ResultSet executeCompoundQuery(String column1, Object val1, String column2, Object val2) throws SQLException {
		//SUtil.print("Col1: " + column1 + " val1: " + val1 + " Col2: " + column2 + " val2: " + val2);
		if (connection == null) {
			SUtil.printErr("DatabaseModel.executeQuery(): null connection!!");
			return null;
		}
		String sql = getSelectSQL();
		String cond1 = prepareCondition(column1, val1);
		String cond2 = prepareCondition(column2, val2);
		
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
		PreparedStatement pstmt = connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		if (val1 != null) {
			val1 = val1.toString().replace("*", "%");
			pstmt.setString( 1, val1+"");
		}
		if (val2 != null && val1 != null) {
			val2 = val2.toString().replace("*", "%");
			pstmt.setString( 2, val2+"");
		}
		if (val2 != null && val1 == null) {
			val2 = val2.toString().replace("*", "%");
			pstmt.setString( 1, val2+"");
		}
		System.out.println("Executing query: " + sql + " :: v1: " + val1 + " v2: " + val2 + " Table: " + getTableName() + " model: " + this);
//		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
//        for (StackTraceElement e: trace) {
//        	SUtil.print(e.toString());
//        }
		return pstmt.executeQuery();
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

	/**
	 * Finds all of the values of column2 where column1 equals val1
	 * 
	 * @param column1
	 * @param val1
	 * @param column2
	 * @return
	 * @throws SQLException
	 */
	public Object[] findCompoundColumnValues(String column1, Object val1, String column2) throws SQLException {
		if (connection == null) {
			SUtil.printErr("DatabaseModel.executeQuery(): null connection!!");
			return null;
		}
		if (column2 == null) {
			SUtil.printErr("DatabaseModel.findCompoundFieldValues - Can't find values of null column!");
			return null;
		}
		//String sql = getSelectSQL(column2);
		String sql = getSelectSQL();
		String cond1 = prepareCondition(column1, val1);
		
		if (cond1 == null) {
			
		} else {
			sql += " where " + cond1;
		}
		
		sql += " group by `" + column2 + "`";
		PreparedStatement pstmt = connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		if (val1 != null) {
			val1 = val1.toString().replace("*","%");
			pstmt.setString( 1, val1+"");
		}
		
		System.out.println("Executing query: " + sql + " v1: " + val1 + " Table: " + getTableName());

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
				//SUtil.print("found " + rs2.getObject(column2));
			}
		}
		//SUtil.print(" found " + vals.length + " values");
		return vals;
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

//	public ResultSet executeQuery(String tableName, String colName, String value) throws SQLException {
//		String q = "select * from `" + tableName + "` where `" + colName + "` = '" + value + "'";
//		return executeQuery(q);
//	}

	public static DatabaseModel getDatabaseModel(DbParms dbParms, String tableName) throws SQLException {
		dbParms.dbTable = tableName;
		dbParms.query = "select * from " + tableName;
		return getDatabaseModel(dbParms);
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

	public static DatabaseModel getDatabaseModel(String tableName2) throws SQLException {
		if (dbParms2 == null) {
			SUtil.printErr("DatabaseModel.getDatabaseModel - Failed to load \"" + tableName2 + "\" - dbParms2 not set!!");
			return null;
		}
		dbParms2.dbTable = tableName2;
		dbParms2.query = "select * from " + tableName2;
		return getDatabaseModel(dbParms2);
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

	public ResultSet executeValueListSearch(String colName, Object[] vals) throws SQLException {
		String sql = "select * from " + getTableName() + " where ";
		for (int i=0; i<vals.length; i++) {
			if (vals[i] == null)
				sql += " `" + colName +"` is NULL ";
			else
				sql += "`" + colName +"` = '" + vals[i] + "'";
			if (i < vals.length -1) sql += " or ";
		}
		
		ResultSet rs2 = executeSpecialQuery(sql);
		
		return rs2;
	}

	public ResultSet executeQuery(String query2) {
		// TODO Auto-generated method stub
		try {
			this.executeQuery(query2,null);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
