package com.tricongeophysics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class EditJobPane extends SpecialPkeyDbRowPane {

	private static final Object[] offices = {"", "Golden", "Denver", "Houston", "Caracas", "Bogota"};
	private Object date;
	private DbField dateField;
	private DbField billToField;
	private ComboBoxField officeField;
	private Object billToAlias;
	private Object office;

	public EditJobPane(String tableName, String pkeyName) {
		super(tableName, pkeyName);
	}
	
	@Override
	public void loadFields() {
		if (dbFields.size() > 1) return; //check if fields already loaded
		super.loadFields();
		dateField = this.getField("Date Received");
		billToField = this.getField("Bill To:");
		officeField = new ComboBoxField(ZbyteDatabase.Office, offices, "");
		addActionListener();
		//if (officeField == null) officeField = new ComboBoxField(ZbyteDatabase.Office, offices, "");
	}

	@Override
	public void addActionListener() {
		dateField.addActionListener(this);
		billToField.addActionListener(this);
		officeField.addActionListener(this);
	}
	
	@Override
	protected Component getTopPane() {
		Component c = super.getTopPane();
		gridPane.add(officeField);
		return c;
	}

	public void setPkeyVal() {
		DecimalFormat df = new DecimalFormat("0000");
		int clientID = getClientID();
		if (clientID < 0) {
			pkeyVal = null;
			return;
		}
		String officeChar = office.toString().substring(0, 1);
		//String dateString = formatDate(date.toString());
		SimpleDateFormat ddf = new SimpleDateFormat("yyMMdd");
		String dateString = ddf.format(date);
		pkeyVal = "Z" + officeChar + "-" + df.format(clientID) + "-" + dateString;
	}

//	private String formatDate(String date2) {
//		String[] parts = date2.split("/");
//		if (parts.length < 3) return null;
//		String month = parts[0];
//		String day = parts[1];
//		String year = parts[2];
//		for (int i=0; i< 2-month.length(); i++) month = "0" + month;
//		for (int i=0; i< 2-day.length(); i++) day = "0" + day;
//		for (int i=0; i< 2-year.length(); i++) year = "0" + year;
//		return year + month + day;
//	}
	
	private int getClientID() {
		int clientID = -1;
		try {
			//DatabaseModel contactModel = DatabaseModel.getDatabaseModel( ZbyteDatabase.Contact);
			//Object client = contactModel.findRowValue("Alias", contactAlias, "Bill To:");
			DatabaseModel clientModel = DatabaseModel.getDatabaseModel( ZbyteDatabase.Client);
			Object id = clientModel.findRowValue("Alias", billToAlias, "id");
			clientID = (int) SUtil.sval("" + id);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return clientID;
	}

	@Override
	protected boolean requiredFieldsOk() {
		office = officeField.getValue();
		billToAlias = billToField.getValue();
		date = dateField.getValue();
		if (office == "" || date.equals("null")) return false;
		return (office != null && billToAlias != null && date != null);
	}
	

	@Override
	public String getRequiredFieldsString() {
		return "Must supply Date Received, Office, and Bill To: before clicking OK";
	}
}
