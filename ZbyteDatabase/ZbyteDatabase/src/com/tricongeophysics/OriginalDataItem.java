package com.tricongeophysics;

public class OriginalDataItem extends AbstractTableData implements ModelTransferItem {

	Object id;
	Object control_num;
	Object line_num;
	private Object job;
	private Object date;
	private Object operator;
	private Object comment;
	
	public Object getJob() {
		return job;
	}

	public void setJob(Object job) {
		this.job = job;
	}

	public static final String[] RequiredColumns = { "id", "control_num", "line_num", "operator", "job", "date", "comment"};

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

	public static String[] getRequiredcolumns() {
		return RequiredColumns;
	}

	public void setDate(Object date) {
		this.date = date;
	}

	public Object getDate() {
		return date;
	}

	@Override
	public void setFromModel(AbstractSpreadsheetModel model, int row) {
		id = model.getValueAt(row, "id"); 
		control_num = model.getValueAt(row, "Control Num:");
		line_num = model.getValueAt(row, "Line #");
		job = model.getValueAt(row, "Job:");
		operator = model.getValueAt(row, "Operator:");
		comment = model.getValueAt(row, "Comment");
		date = model.getValueAt(row, "Copy Date");
	}

	@Override
	public void setModelToItem(DatabaseModel model, int row) {
		//model.setValueAt(id, row, "Original Support:");
		model.setValueAt(line_num, row, "Line #");
		model.setValueAt(job, row, "Job:");
		model.setValueAt(date, row, "Date Finished");
		model.setValueAt(comment, row, "Comment");
		model.setValueAt(operator, row, "Operator:");
	}

	public Object getOperator() {
		return operator;
	}

	public void setOperator(Object operator) {
		this.operator = operator;
	}

	public Object getComment() {
		return comment;
	}

	public void setComment(Object comment) {
		this.comment = comment;
	}
	
}
