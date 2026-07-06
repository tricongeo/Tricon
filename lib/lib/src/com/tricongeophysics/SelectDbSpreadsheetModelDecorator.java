package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.event.TableModelListener;

import com.tricongeophysics.TableData.ColumnType;

/**
 * Class to decorate spreadsheet model by adding a select column.
 * Uses decorator pattern so column won't be added to actual model
 * (which can then still be used by other spreadsheets without the select column)
 * 
 * @author scott
 *
 */
public class SelectDbSpreadsheetModelDecorator extends DatabaseModel implements DbModelInterface {

	private static final int SelectIndex = 99999;
	private DbModelInterface model;
	private SelectItemColumn selectColumn;

	public SelectDbSpreadsheetModelDecorator(DbModelInterface model) {
		this.model = model;
		selectColumn = new SelectItemColumn(model);
		((DatabaseModel) model).setWarningColumn(selectColumn);
	}

	@Override
	public int getRowCount() {
		return model.getRowCount();
	}

	@Override
	public int getColumnCount() {
		//return model.getColumnCount()+1;
		return model.getColumnCount();
	}
	
	@Override
	public int getColumnIndex(String colName) {
		if (colName == null || colName.equals("Select")) return SelectIndex;
		return model.getColumnIndex(colName);
	}
	
	public void addModelChangedListener(ModelChangedListener l) {
		model.addModelChangedListener(l);
		modelChangedListeners.add(l);
	}

	@Override
	public void addRowColorChangedListener(RowColorChangedListener l) {
		model.addRowColorChangedListener(l);
	}

	@Override
	public void addColumn(String name, Class<?> colClass) {
		SUtil.printErr("SelectSpreadsheetModelDecorator.addColumn() not implemented");
	}

	@Override
	public boolean isOptionalColumn(int modelCol) {
		if (modelCol == SelectIndex) return false;
		return model.isOptionalColumn(modelCol);
	}

	@Override
	public void delColumn(int modelCol) {
		SUtil.printErr("SelectSpreadsheetModelDecorator.delColumn() not implemented");
	}

	@Override
	public void extrapUp(int[] modelRows, int c) {
		if (c == SelectIndex) {
			SUtil.printErr("SelectSpreadsheetModelDecorator.extrapUp() not implemented");
			return;
		}
		model.extrapUp(modelRows, c);
	}

	@Override
	public void extrapDown(int[] modelRows, int c) {
		if (c == SelectIndex) {
			SUtil.printErr("SelectSpreadsheetModelDecorator.extrapDown() not implemented");
			return;
		}
		model.extrapDown(modelRows, c);
	}

	@Override
	public void interpRows(int[] modelRows, int c) {
		if (c == SelectIndex) {
			SUtil.printErr("SelectSpreadsheetModelDecorator.interpRows() not implemented");
			return;
		}
		model.interpRows(modelRows, c);
	}

	@Override
	public void reload() {
		model.reload();
	}

	@Override
	public void addRow(int modelRow) {
		model.addRow(modelRow);
	}

	@Override
	public void delRows(int[] modelRows) {
		model.delRows(modelRows);
	}

	@Override
	public boolean isColEditable(int modelColumn) {
		if (modelColumn == SelectIndex) return true;
		return model.isColEditable(modelColumn);
	}

	@Override
	public ColumnType getColumnType(int modelColumn) {
		if (modelColumn == SelectIndex) return model.getColumnType(0);
		return model.getColumnType(modelColumn);
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == SelectIndex)
			try {
				return selectColumn.getValueAt(row);
				//SUtil.print("col zero");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return model.getValueAt(row, col);
	}

	@Override
	public void setValueAt(Object newVal, int row, int col) {
		if (col == SelectIndex)
			try {
				selectColumn.setValueAt(row, newVal);
				return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		model.setValueAt(newVal, row, col);
	}

	@Override
	public Object[] findCompoundColumnValues(String column1, Object val1, String column2) throws SQLException {
		return ((DatabaseModel) model).findCompoundColumnValues(column1, val1, column2);
	}
	
	@Override
	public ResultSet executeCompoundQuery(String col1, Object val1, String col2, Object val2) throws SQLException {
		return model.executeCompoundQuery(col1, val1, col2, val2);
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		if (col == SelectIndex) return true;// model.isCellEditable(row, 0);
		return model.isCellEditable(row, col);
	}
	
	@Override
	public void setResultSet(ResultSet rs) {
		((DatabaseModel) model).setResultSet(rs);
	}

	@Override
	public boolean containsError(int modelRow) {
		return model.containsError(modelRow);
	}

	@Override
	public boolean containsWarning(int modelRow) {
		return model.containsWarning(modelRow);
	}

	@Override
	public String[] getColumnNames() {
		String[] names = model.getColumnNames();
		String[] names2 = SUtil.arrayCat(new String[] {"Select"}, names);
		return names2;
	}
	
	@Override
	public String[] getColumnDbNames() {
		return ((DatabaseModel) model).getColumnDbNames();
	}
	
	@Override
	public String getColumnName(int c) {
		if (c == SelectIndex) return "Select";
		return model.getColumnName(c);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == SelectIndex) return Boolean.class;
		return model.getColumnClass(columnIndex);
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		model.addTableModelListener(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		model.removeTableModelListener(l);
	}

	@Override
	public void setEditableColumns(boolean[] editableColumns) {
		((DatabaseModel)model).setEditableColumns(editableColumns);
	}

	@Override
	public void setEditableColumns(String[] editableColNames) {
		((DatabaseModel)model).setEditableColumns(editableColNames);
	}

	@Override
	public boolean[] getEditableColumns() {
		return ((DatabaseModel)model).getEditableColumns();
	}

	@Override
	public String getTableName() {
		return ((DatabaseModel)model).getTableName();
	}

	@Override
	public ResultSet getResultSet() {
		return model.getResultSet();
	}
	
	@Override
	public String getSelectSQL() {
		return model.getSelectSQL();
	}

	@Override
	public void addSQLErrorListener(SQLErrorListener l) {
		((DatabaseModel)model).addSQLErrorListener(l);
	}
}
