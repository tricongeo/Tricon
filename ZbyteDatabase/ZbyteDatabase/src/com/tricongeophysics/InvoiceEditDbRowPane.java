package com.tricongeophysics;

public class InvoiceEditDbRowPane{

	private String contactName;
	private Object clientName;
	private Object streetAddress;
	private Object city;
	private Object state;
	private Object zip;
	private Object country;
	private Object phone;
	private Object invoiceID;
	private Object invoiceDate;
	private Object comment;

	public InvoiceEditDbRowPane(EditDbRowPane editDbRowPane) {
		invoiceID = editDbRowPane.getPkey();
		invoiceDate = editDbRowPane.getField("Invoice Date").getValue();
		comment = editDbRowPane.getField("Comment").getValue();
		
		DbField contactField = editDbRowPane.getField("Contact:");
		NewEditComboBoxField contactComboField = (NewEditComboBoxField)contactField.simpleField;
		//int row = contactComboField.getSelectedItemIndex();
		Object contact = contactField.getValue();
		EditDbRowPane contactPane = contactComboField.newEditPane;
		contactPane.loadFields();
		contactPane.setRow(contact);
		Object first = contactPane.getField("First").getValue();
		Object last = contactPane.getField("Last").getValue();
		setContactName(last + ", " + first);
		
	    DbField clientField = editDbRowPane.getField("Client:");
		NewEditComboBoxField clientComboField = (NewEditComboBoxField)clientField.simpleField;
		//int row = clientComboField.getSelectedItemIndex();
		Object client = clientField.getValue();
		EditDbRowPane clientPane = clientComboField.newEditPane;
		clientPane.loadFields();
		clientPane.setRow(client);
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

	public Object getInvoiceID() {
		return invoiceID;
	}

	public Object getInvoiceDate() {
		return invoiceDate;
	}

	public Object getComment() {
		return comment;
	}

}
