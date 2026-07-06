package com.tricongeophysics;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.event.TableModelListener;


public class DbMergeSheet extends DbSpreadsheetPanel {
	private DbColumnMap dbColumnMap;

	public DbMergeSheet(DbColumnMap dbColumnMap) throws SQLException {
		super(dbColumnMap.getDbParms());
		this.dbColumnMap = dbColumnMap;
		dbParms.dbTable = "merge";
		dbParms.query = getSQL(null, null, null);
		model = DatabaseModel.getDatabaseModel(dbParms);
		//setModel(model);
		setModel(new MergeModel(dbColumnMap.getMainColumnNames(), (DbModelInterface) model));
	}
	
	protected String getSQL(String cond1, String cond2, String string) {
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

	public class MergeModel extends LimitedDbSpreadsheetModel {

		private String sql;

		public MergeModel(String[] colNames, DbModelInterface model) throws SQLException {
			super(colNames, model);
			// TODO Auto-generated constructor stub
			//loadMergeModel();
			//tableName = "merge";
		}
		
		@Override
		public void addModelChangedListener(ModelChangedListener l) {
			//modelChangedListeners.add(l);
			dbModel.addModelChangedListener(l);
		}
		

		@Override
		public void addTableModelListener(TableModelListener l) {
			//model.addTableModelListener(l);
			listenerList.add(TableModelListener.class, l);
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
			
			sql = getSQL(cond1, cond2, null);
			
			PreparedStatement pstmt = connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
			for (int i=0; i<dbColumnMap.getTableNames().length; i++) {
				if (val1 != null) {
					val1 = val1.toString().replace("*", "%");
					pstmt.setString( i+1, val1+"");
				}
				if (val2 != null && val1 != null) {
					val2 = val2.toString().replace("*", "%");
					pstmt.setString( i+2, val2+"");
				}
				if (val2 != null && val1 == null) {
					val2 = val2.toString().replace("*", "%");
					pstmt.setString( i+1, val2+"");
				}
			}
			System.out.println("Executing Merge query: " + sql + " :: v1: " + val1 + " v2: " + val2 + " Table: " + getTableName() + " model: " + this);
//            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
//            for (StackTraceElement e: trace) {
//            	SUtil.print(e.toString());
//            }
			return pstmt.executeQuery();			
		}
		
		@Override
		public void setResultSet(ResultSet rs2) {
			((DatabaseModel) dbModel).setResultSet(rs2);
//			((DatabaseModel) LimitedDbSpreadsheetModel.model).rs = rs2;
//			((DatabaseModel) model).resetModel();
		}
		
		@Override
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
			
			sql = getSQL(cond1, null, " group by `" + column2 + "`");
			
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
		
//		@Override
//		public ResultSet executeQuery(String query) throws SQLException {
//			String newQuery = sql;
//			int index = query.toLowerCase().indexOf("where");
//			if (index >= 0) {
//				String condition = query.substring(index);
//				newQuery += condition;
//			}
//			
//			return super.executeQuery(newQuery);
//		}
	}
	
}
