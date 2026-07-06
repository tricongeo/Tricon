package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.event.TableModelListener;

import com.tricongeophysics.TableData.ColumnType;

public class LimitedDbSpreadsheetModel extends DatabaseModel implements DbModelInterface {

	private String[] colNames;
	protected DbModelInterface dbModel;

	public LimitedDbSpreadsheetModel(String[] colNames, DbModelInterface model) {
		super();
		this.colNames = colNames;
		this.dbModel = model;
	}

	@Override
	public int getRowCount() {
		return dbModel.getRowCount();
	}

	@Override
	public int getColumnCount() {
		return colNames.length;
	}
	
	public void addModelChangedListener(ModelChangedListener l) {
		dbModel.addModelChangedListener(l);
	}

	@Override
	public void addRowColorChangedListener(RowColorChangedListener l) {
		dbModel.addRowColorChangedListener(l);
	}

	@Override
	public void addColumn(String name, Class<?> colClass) {
		SUtil.printErr("LimitedSpreadsheetModel.addColumn() not implemented");
	}

	@Override
	public boolean isOptionalColumn(int modelCol) {
		int c = calcFullModelColumn(modelCol);
		return dbModel.isOptionalColumn(c);
	}

	@Override
	public void delColumn(int modelCol) {
		SUtil.printErr("LimitedSpreadsheetModel.delColumn() not implemented");
	}

	@Override
	public void extrapUp(int[] modelRows, int c) {
		int c2 = calcFullModelColumn(c);
		dbModel.extrapUp(modelRows, c2);
	}

	@Override
	public void extrapDown(int[] modelRows, int c) {
		int c2 = calcFullModelColumn(c);
		dbModel.extrapDown(modelRows, c2);
	}

	@Override
	public void interpRows(int[] modelRows, int c) {
		int c2 = calcFullModelColumn(c);
		dbModel.interpRows(modelRows, c2);
	}

	@Override
	public void reload() {
		dbModel.reload();
	}

	@Override
	public void addRow(int modelRow) {
		dbModel.addRow(modelRow);
	}

	@Override
	public void delRows(int[] modelRows) {
		dbModel.delRows(modelRows);
	}

	@Override
	public boolean isColEditable(int modelColumn) {
		int c2 = calcFullModelColumn(modelColumn);
		return dbModel.isColEditable(c2);
	}

	@Override
	public ColumnType getColumnType(int modelColumn) {
		int c2 = calcFullModelColumn(modelColumn);
		return dbModel.getColumnType(c2);
	}

	@Override
	public Object getValueAt(int row, int col) {
		int c2 = calcFullModelColumn(col);
		return dbModel.getValueAt(row, c2);
	}

	@Override
	public void setValueAt(Object newVal, int row, int col) {
		int c2 = calcFullModelColumn(col);
		dbModel.setValueAt(newVal, row, c2);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		int c2 = calcFullModelColumn(col);
		return dbModel.isCellEditable(row, c2);
	}

	@Override
	public boolean containsError(int modelRow) {
		return dbModel.containsError(modelRow);
	}

	@Override
	public boolean containsWarning(int modelRow) {
		return dbModel.containsWarning(modelRow);
	}

	@Override
	public int getColumnIndex(String colName) {
//		return model.getColumnIndex(colName);
		int i=0;
		for (String name: colNames) {
			if (name.equalsIgnoreCase(colName))
				return i;
			i++;
		}
		SUtil.printErr("AbstractSpreadhsheetModel.getColumnIndex() - column not found - " + colName + " (table: " + getTableName() + ")");
		return -1;
	}
	
	@Override
	public String[] getColumnNames() {
		return colNames;
	}
	
	@Override
	public String[] getColumnDbNames() {
		return ((DatabaseModel) dbModel).getColumnDbNames();
	}
	
	@Override
	public String getColumnName(int c) {
		if (c >= colNames.length || c < 0) return null;
		return colNames[c];
	}
	
	protected int calcFullModelColumn(int modelCol) {
		String colName = getColumnName(modelCol);
		int fullColNumber = dbModel.getColumnIndex(colName);
		return fullColNumber;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		int c2 = calcFullModelColumn(columnIndex);
		return dbModel.getColumnClass(c2);
	}

	@Override
	public Object[] findCompoundColumnValues(String column1, Object val1, String column2) throws SQLException {
		return ((DatabaseModel) dbModel).findCompoundColumnValues(column1, val1, column2);
	}
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		dbModel.addTableModelListener(l);
	}
	
	@Override
	public String getSelectSQL() {
		return dbModel.getSelectSQL();
	}
	
	@Override
	public ResultSet executeCompoundQuery(String col1, Object val1, String col2, Object val2) throws SQLException {
		return dbModel.executeCompoundQuery(col1, val1, col2, val2);
	}
	
	@Override
	public void setResultSet(ResultSet rs) {
		((DatabaseModel) dbModel).setResultSet(rs);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		dbModel.removeTableModelListener(l);
	}

	@Override
	public void setEditableColumns(boolean[] editableColumns) {
		((DatabaseModel)dbModel).setEditableColumns(editableColumns);
	}

	@Override
	public void setEditableColumns(String[] editableColNames) {
		((DatabaseModel)dbModel).setEditableColumns(editableColNames);
	}

	@Override
	public boolean[] getEditableColumns() {
		return ((DatabaseModel)dbModel).getEditableColumns();
	}

	@Override
	public String getTableName() {
		return ((DatabaseModel)dbModel).getTableName();
	}

	@Override
	public ResultSet getResultSet() {
		return dbModel.getResultSet();
	}
	
	@Override
	public void addSQLErrorListener(SQLErrorListener l) {
		((DatabaseModel)dbModel).addSQLErrorListener(l);
	}
}
