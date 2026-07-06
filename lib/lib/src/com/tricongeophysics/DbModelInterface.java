package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.table.TableModel;

import com.tricongeophysics.TableData.ColumnType;

public interface DbModelInterface extends TableModel {

	void addRowColorChangedListener(RowColorChangedListener l);

	void addColumn(String name, Class<?> colClass);

	boolean isOptionalColumn(int modelCol);

	void delColumn(int modelCol);

	void extrapUp(int[] modelRows, int c);

	void extrapDown(int[] modelRows, int c);

	void interpRows(int[] modelRows, int c);

	void reload();

	void addRow(int modelRow);

	void delRows(int[] modelRows);

	boolean isColEditable(int modelColumn);

	ColumnType getColumnType(int modelColumn);

	boolean containsError(int modelRow);

	boolean containsWarning(int modelRow);

	String[] getColumnNames();

	int getColumnIndex(String colName);

	void setEditableColumns(boolean[] editableColumns);

	void setEditableColumns(String[] editableColNames);

	void addModelChangedListener(ModelChangedListener l);

	boolean[] getEditableColumns();

	String getTableName();

	ResultSet getResultSet();

	void fireTableRowsUpdated(int i, int j);

	public String getSelectSQL();

	ResultSet executeCompoundQuery(String col1, Object val1, String col2,
			Object val2) throws SQLException;

}
