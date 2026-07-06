package com.tricongeophysics;

public class InterOfficeTransmittalEditDbRowPane{

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
	private Object officeName;
	private Object fromOffice;
	private Object job;

	public InterOfficeTransmittalEditDbRowPane(EditDbRowPane editDbRowPane) {
		transmittalID = editDbRowPane.getPkey();
		transmittedBy = editDbRowPane.getField("Transmitted By:").getValue();
		shipDate = editDbRowPane.getField("Ship Date").getValue();
		shipMethod = editDbRowPane.getField("Shipping Method:").getValue();
		comment = editDbRowPane.getField("Comment").getValue();
		fromOffice = editDbRowPane.getField("From Office:").getValue();
		job = editDbRowPane.getField("Job:").getValue();
		
		DbField officeField = editDbRowPane.getField("To Office:");
		officeName = officeField.getValue();
		
		NewEditComboBoxField officeComboField = (NewEditComboBoxField)officeField.simpleField;
		int row = officeComboField.getSelectedItemIndex();
		EditDbRowPane officePane = officeComboField.newEditPane;
		officePane.loadFields();
		officePane.setRow(row);
		streetAddress = officePane.getField("Street Address").getValue();
		city = officePane.getField("City").getValue();
		state = officePane.getField("State").getValue();
		zip = officePane.getField("Zip").getValue();
		country = officePane.getField("Country").getValue();
		phone = officePane.getField("Phone#").getValue();
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

	public Object getOfficeName() {
		return officeName;
	}

	public Object getFromOffice() {
		return fromOffice;
	}

	public Object getJob() {
		return job;
	}

}
