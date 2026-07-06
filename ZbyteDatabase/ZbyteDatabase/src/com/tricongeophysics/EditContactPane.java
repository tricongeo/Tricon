package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class EditContactPane extends SpecialPkeyDbRowPane {
	
	private DbField firstField;
	private Object first;
	private DbField lastField;
	private Object last;
	private DbField clientField;
	private Object client;

	public EditContactPane(String tableName, String pkeyName) {
		super(tableName, pkeyName);
	}
	
	@Override
	public void loadFields() {
		if (dbFields.size() > 1) return; //check if fields already loaded
		super.loadFields();
		firstField = this.getField("First");
		lastField = this.getField("Last");
		clientField = this.getField("Client:");
		addActionListener();
	}

	@Override
	public void addActionListener() {
		firstField.addActionListener(this);
		lastField.addActionListener(this);
		clientField.addActionListener(this);
	}

	@Override
	public void setPkeyVal() {
		DecimalFormat df = new DecimalFormat("0000");
		int clientID = getClientID();
		pkeyVal = last + ", " + first + " - " + df.format(clientID);
	}

	@Override
	protected boolean requiredFieldsOk() {
		first = firstField.getValue();
		last = lastField.getValue();
		client = clientField.getValue();
		return (first != null && last != null && client != null);
	}
	
	private int getClientID() {
		int clientID = -1;
		try {
			DatabaseModel clientModel = DatabaseModel.getDatabaseModel( ZbyteDatabase.Client);
			Object val = clientModel.findRowValue("Alias", client, "id");
			clientID = (int) SUtil.sval("" + val);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return clientID;
	}
	
	@Override
	protected void ensurePkeyUniq() {};
	

	@Override
	public String getRequiredFieldsString() {
		return "Must supply First, Last (Hit Enter) and Client: before clicking OK";
	}
}
