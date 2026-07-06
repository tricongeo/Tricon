package com.tricongeophysics;

import java.awt.Component;

public class ZbyteDbField extends DbField {

	public ZbyteDbField(DatabaseModel model, int row, String colName) {
		super(model, row, colName);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Component getDisplayField() {
		if (colName.contains(":")){
			if (colName.contains("Bill To")) {
				simpleField = new NewZbyteEditComboBoxField(colName,  ZbyteDatabase.Client);
			}
			else if (colName.contains("Data Owner")) {
				simpleField = new NewZbyteEditComboBoxField(colName,  ZbyteDatabase.Client);
			}
			else if (colName.contains("Transmitted By")) {
				simpleField = new NewZbyteEditComboBoxField(colName, ZbyteDatabase.Operator);
			}
			else if (colName.contains(ZbyteDatabase.Office)) {
				simpleField = new NewZbyteEditComboBoxField(colName, ZbyteDatabase.Office);
			}
			else simpleField = new NewZbyteEditComboBoxField(colName);
			return simpleField;
		} else {
			return super.getDisplayField();
		}
	}
}
