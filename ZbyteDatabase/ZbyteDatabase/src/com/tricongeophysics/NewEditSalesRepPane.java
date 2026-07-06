package com.tricongeophysics;

public class NewEditSalesRepPane extends NewEditDbPane {

	NewEditSalesRepPane(){
		super();
		setTitle("New/Edit Sales Representative");
	}
	
	@Override
	protected String getTableName() {
		return ZbyteDatabase.SalesRep;
	}

	@Override
	protected String getPkeyName() {
		return "Alias";
	}

}
