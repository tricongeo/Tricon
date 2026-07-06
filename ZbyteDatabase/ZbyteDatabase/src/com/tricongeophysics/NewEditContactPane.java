package com.tricongeophysics;

public class NewEditContactPane extends NewEditDbPane {

	NewEditContactPane() {
		super();
		setTitle("New/Edit Contact");
	}
	
	@Override
	protected String getTableName() {
		return  ZbyteDatabase.Contact;
	}

	@Override
	protected String getPkeyName() {
		return "Alias";
	}

}
