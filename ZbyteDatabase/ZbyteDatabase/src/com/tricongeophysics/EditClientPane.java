package com.tricongeophysics;

import java.awt.Component;
import java.text.DecimalFormat;

public class EditClientPane extends SpecialPkeyDbRowPane {
	
	private DbField nameField;
	private Object name;
	private DbField idField;
	private Object id;

	public EditClientPane(String tableName, String pkeyName) {
		super(tableName, pkeyName);
	}

	@Override
	public void loadFields() {
		if (dbFields.size() > 1) return; //check if fields already loaded
		super.loadFields();
		nameField = this.getField("Name");
		idField = this.getField("id");

		addActionListener();
	}
	
	
	@Override
	public void addActionListener() {
		nameField.addActionListener(this);
	}

	@Override
	public void setPkeyVal() {
		DecimalFormat df = new DecimalFormat("0000");
		int clientID = Integer.parseInt(id.toString());
		pkeyVal = name + " - " + df.format(clientID);
	}

	@Override
	protected boolean requiredFieldsOk() {
		id = idField.getValue();
		name = nameField.getValue();
		return (name != null && id != null);
	}

	@Override
	public String getRequiredFieldsString() {
		return "Must supply ID and Name (Hit Enter) before clicking OK";
	}
}
