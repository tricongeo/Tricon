package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class OriginalItem2 extends AbstractTableData {
	
	public static final String[] RequiredColumns = { };

	//public static final String[] RequiredColumns = { "id", "control_num", "line_num", "filename", "job", "date"};
	
	public static OriginalItem2 create(AbstractSpreadsheetModel model, int row) {
		OriginalItem2 item = new OriginalItem2();
		
		int count = model.getColumnCount();
		for (int i=0; i<count ; i++) {
			String name = model.getColumnName(i);
			Object val = model.getValueAt(row, i);
			item.addOptionalColumn(name, model.getColumnClass(i));
			item.setValue(name, val);
		}
		
		return item;
	}

	@Override
	public boolean containsError() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsWarning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addRowColorChangedListener(RowColorChangedListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(TableData o)
	{
		return o.toString().compareTo(this.toString());
	}

	@Override
	public String[] getRequiredColumns() {
		return RequiredColumns;
	}

	@Override
	public boolean[] getEditableCols() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ColumnType[] getColumnTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public static String[] getRequiredcolumns() {
		return RequiredColumns;
	}

	public static AbstractSpreadsheetModel addDeliverablesToDatabase(
			DatabaseModel model, ArrayList<TableData> deliverableArray) throws SQLException {
		int rows = deliverableArray.size();
		String[] names = deliverableArray.get(0).getColumnNames();
		int cols = deliverableArray.get(0).getColumnNames().length;
		for (int i=0; i<rows; i++) {
			OriginalItem2 oi = (OriginalItem2) deliverableArray.get(i);
			model.addRow(1);
			int newRow = model.getRowCount() - 1;
			for (int j=0; j<cols; j++) {
				String colName = names[j];
				Object val = oi.getOptionalValue(j);
				model.setValueAt(val, newRow, colName);
			}
		}
		return model;
	}

}
