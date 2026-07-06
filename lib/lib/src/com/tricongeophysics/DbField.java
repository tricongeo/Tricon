package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class DbField extends JPanel implements ClickedOkListener {

	protected DatabaseModel model;
	private int row;
	SimpleField simpleField;
	protected String colName;

	public DbField(DatabaseModel model, int row, String colName) {
		this.model = model;
		this.row = row;
		this.colName = colName;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.add(getDisplayField(), BorderLayout.CENTER);
	}

	/**
	 * Build display component based on type of data stored in database.
	 * In the data object is non-null, it's class type can be used,
	 * otherwise we have to fall back on the column Name.
	 * Also, we are assuming that primary keys in the database will have
	 * a ":" in the column name. If so, NewEditComboBoxField will be used.
	 * @return
	 */
	protected Component getDisplayField() {
		int c = model.getColumnIndex(colName);
		if (c < 0) {
			SUtil.printErr("column not found: " + colName + " in table: " + model.getTableName());
		}
		Class<? extends Object> klass = model.getColumnClass(c);
		Object type = model.getColumnDataType(colName);  
		if (colName.contains(":")){
			simpleField = new NewEditComboBoxField(colName);
		}
		else if (klass == Date.class) {
			simpleField = new DateField(colName, model.getValueAt(row, colName));
		}
		else if (klass == Number.class) {
			simpleField = new SimpleField(colName, model.getValueAt(row, colName));
		}
		else if (klass == Boolean.class) {
			//simpleField = new SimpleField(colName, model.getValueAt(row, colName));
			simpleField = new BooleanField(colName, model.getValueAt(row, colName));
		}
		else if (colName.toLowerCase().contains("file")){
			simpleField = new FileField(colName, model.getValueAt(row, colName));
		}
		else if (type.toString().toLowerCase().contains("date")){
			simpleField = new DateField(colName, model.getValueAt(row, colName));
		}
		else if (type.equals("CHAR")){
			simpleField = new EnumField(colName, model.getValueAt(row, colName), model);
		}
		else if (klass == Password.class){
			simpleField = new PasswordField(colName, model.getValueAt(row, colName));
		}
		else {
			simpleField = new SimpleField(colName, model.getValueAt(row, colName));
		}
		return simpleField;
	}

	public void setEditable(boolean b) {
		simpleField.setEditable(b);
	}

	@Override
	public void clickedOk() {
		if (isEditable())
			model.setValueAt(simpleField.getValue(), row, colName);
	}

	public void setRow(int row2) {
		row = row2;
		simpleField.setValue(model.getValueAt(row, colName));
	}

	public void setValue(Object val) {
		simpleField.setValue(val);
	}

	public void addActionListener(ActionListener l) {
		simpleField.addActionListener(l);
	}

	public boolean isEditable() {
		return simpleField.isEditable();
	}

	public String toString() {
		String v = "null";
		Object o = null;
		if (simpleField != null) o = simpleField.getValue();
		if (o != null) v = o.toString();
		return colName + ": " + v;
	}

	public String getColName() {
		return colName;
	}

	public String getHtml() {
		return simpleField.getHtml();
	}
	
	public String getName() {
		return colName;
	}
	
	public Object getValue() {
		return simpleField.getValue();
	}
}
