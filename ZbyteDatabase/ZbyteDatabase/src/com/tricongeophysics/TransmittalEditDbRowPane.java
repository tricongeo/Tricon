package com.tricongeophysics;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransmittalEditDbRowPane{

	private String contactName;
	private Object clientName;
	private Object streetAddress;
	private Object city;
	private Object state;
	private Object zip;
	private Object country;
	private Object phone;
	private Object transmittalID;
	private Object transmittedBy;
	private Object shipDate;
	private Object shipMethod;
	private Object comment;

	public TransmittalEditDbRowPane(EditDbRowPane editDbRowPane) {
		transmittalID = editDbRowPane.getPkey();
		transmittedBy = editDbRowPane.getField("Transmitted By:").getValue();
		shipDate = editDbRowPane.getField("Ship Date").getValue();
		shipMethod = editDbRowPane.getField("Shipping Method:").getValue();
		comment = editDbRowPane.getField("Comment").getValue();
		
		DbField contactField = editDbRowPane.getField("Contact:");
		Object contact = contactField.getValue();
		NewEditComboBoxField contactComboField = (NewEditComboBoxField)contactField.simpleField;
		int row = contactComboField.getSelectedItemIndex();
		EditDbRowPane contactPane = contactComboField.newEditPane;
		contactPane.loadFields();
		contactPane.setRow(row);
		Object first = contactPane.getField("First").getValue();
		Object last = contactPane.getField("Last").getValue();
		setContactName(last + ", " + first);
		
		DbField clientField = contactPane.getField("Client:");
		NewEditComboBoxField clientComboField = (NewEditComboBoxField)clientField.simpleField;
		row = clientComboField.getSelectedItemIndex();
		EditDbRowPane clientPane = clientComboField.newEditPane;
		clientPane.loadFields();
		clientPane.setRow(row);
		clientName = clientPane.getField("Name").getValue();
		streetAddress = clientPane.getField("Street Address").getValue();
		city = clientPane.getField("City").getValue();
		state = clientPane.getField("State").getValue();
		zip = clientPane.getField("Zip").getValue();
		country = clientPane.getField("Country").getValue();
		phone = clientPane.getField("Phone#").getValue();
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactName() {
		return contactName;
	}

	public Object getClientName() {
		return clientName;
	}

	public void setClientName(Object clientName) {
		this.clientName = clientName;
	}

	public Object getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(Object streetAddress) {
		this.streetAddress = streetAddress;
	}

	public Object getCity() {
		return city;
	}

	public void setCity(Object city) {
		this.city = city;
	}

	public Object getState() {
		return state;
	}

	public void setState(Object state) {
		this.state = state;
	}

	public Object getZip() {
		return zip;
	}

	public void setZip(Object zip) {
		this.zip = zip;
	}

	public Object getCountry() {
		return country;
	}

	public void setCountry(Object country) {
		this.country = country;
	}

	public Object getPhone() {
		return phone;
	}

	public void setPhone(Object phone) {
		this.phone = phone;
	}

	public Object getTransmittalID() {
		return transmittalID;
	}

	public Object getTransmittedBy() {
		return transmittedBy;
	}

	public Object getShipDate() {
		return shipDate;
	}

	public Object getShipMethod() {
		return shipMethod;
	}

	public Object getComment() {
		return comment;
	}

}
