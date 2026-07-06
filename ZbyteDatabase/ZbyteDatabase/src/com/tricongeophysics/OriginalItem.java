package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class OriginalItem extends AbstractTableData {

	private Object id;
	private Object control_num;
	private Object line_num;
	private Object filename;
	private Object job;
	private Object date;
	
	public Object getJob() {
		return job;
	}

	public void setJob(Object job) {
		this.job = job;
	}

	public static final String[] RequiredColumns = { "id", "control_num", "line_num", "filename", "job", "date"};
	
	public static OriginalItem create(AbstractSpreadsheetModel model, int row) {
		OriginalItem item = new OriginalItem();
		
		item.id = model.getValueAt(row, "id"); 
		item.control_num = model.getValueAt(row, "Control Num:");
		item.line_num = model.getValueAt(row, "Line #");
		item.filename = model.getValueAt(row, "Filename");
		
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

	public String toString() {
		return id.toString();
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Object getControl_num() {
		return control_num;
	}

	public void setControl_num(Object control_num) {
		this.control_num = control_num;
	}

	public Object getLine_num() {
		return line_num;
	}

	public void setLine_num(Object line_num) {
		this.line_num = line_num;
	}

	public Object getFilename() {
		return filename;
	}

	public void setFilename(Object filename) {
		this.filename = filename;
	}

	public static String[] getRequiredcolumns() {
		return RequiredColumns;
	}

	public static AbstractSpreadsheetModel addDeliverablesToDatabase(
			DatabaseModel model, ArrayList<TableData> deliverableArray) throws SQLException {
		for (TableData td: deliverableArray) {
			OriginalItem oi = (OriginalItem) td;
			model.addRow(1);
			int row = model.getRowCount() - 1;
			model.setValueAt(oi.id, row, "Original Support:");
			model.setValueAt(oi.control_num, row, "Control Num:");
			model.setValueAt(oi.line_num, row, "Line #");
			model.setValueAt(oi.filename, row, "Filename");
			model.setValueAt(oi.job, row, "Job:");
			model.setValueAt(oi.date, row, "Date Finished");
		}
		return model;
	}

	public void setDate(Object date) {
		this.date = date;
	}

	public Object getDate() {
		return date;
	}
	
}
