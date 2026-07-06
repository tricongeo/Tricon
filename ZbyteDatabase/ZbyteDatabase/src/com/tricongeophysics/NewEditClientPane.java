package com.tricongeophysics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class NewEditClientPane extends NewEditDbPane {

	private SimpleField aliasField;
	private SimpleField nameField;
	private SimpleField streetField;
	private SimpleField cityField;
	private SimpleField stateField;
	private SimpleField zipField;
	private SimpleField countryField;
	private SimpleField phoneField;
	private SimpleField faxField;
	private SimpleField commentField;

	public NewEditClientPane() {
		super();
		setTitle("New/Edit Client");
	}
	
	@Override
	protected Component getTopPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JPanel a = new JPanel();
		a.setLayout(new BoxLayout(a, BoxLayout.X_AXIS));
		a.add(getAliasField());
		a.add(getNameField());
		
		JPanel b = new JPanel();
		b.setLayout(new BoxLayout(b, BoxLayout.X_AXIS));
		b.add(getStreetField());
		
		JPanel c = new JPanel();
		c.setLayout(new BoxLayout(c, BoxLayout.X_AXIS));
		c.add(getCityField());
		c.add(getStateField());
		c.add(getZipField());
		c.add(getCountryField());
		
		JPanel d = new JPanel();
		d.setLayout(new BoxLayout(d, BoxLayout.X_AXIS));
		d.add(getPhoneField());
		d.add(getFaxField());
		d.add(getCommentField());
		
		panel.add(a);
		panel.add(b);
		panel.add(c);
		panel.add(d);
		
		return panel;
	}
	
	private Component getCommentField() {
		commentField = makeSimpleDbField("Comment");
		return commentField;
	}

	private Component getFaxField() {
		faxField = makeSimpleDbField("Fax#");
		return faxField;
	}

	private Component getPhoneField() {
		phoneField = makeSimpleDbField("Phone#");
		return phoneField;
	}

	private Component getCountryField() {
		countryField = makeSimpleDbField("Zip");
		return countryField;
	}

	private Component getZipField() {
		zipField = makeSimpleDbField("Zip");
		return zipField;
	}

	private Component getStateField() {
		stateField = makeSimpleDbField("State");
		return stateField;
	}

	private Component getCityField() {
		cityField = makeSimpleDbField("City");
		return cityField;
	}

	private Component getStreetField() {
		streetField = makeSimpleDbField("Street Address");
		return streetField;
	}

	private Component getNameField() {
		nameField = makeSimpleDbField("Name");
		return nameField;
	}

	private Component getAliasField() {
		aliasField = makeSimpleDbField("Alias");
		return aliasField;
	}

	@Override
	protected String getTableName() {
		return  ZbyteDatabase.Client;
	}

	@Override
	protected String getPkeyName() {
		return "Alias";
	}
	
	@Override
	protected void rowChanged() {
		aliasField.setValue(model.getValueAt(row, "Alias"));
		nameField.setValue(model.getValueAt(row, "Name"));
		streetField.setValue(model.getValueAt(row, "Street Address"));
		cityField.setValue(model.getValueAt(row, "City"));
		stateField.setValue(model.getValueAt(row, "State"));
		zipField.setValue(model.getValueAt(row, "Zip"));
		countryField.setValue(model.getValueAt(row, "Country"));
		phoneField.setValue(model.getValueAt(row, "Phone#"));
		faxField.setValue(model.getValueAt(row, "Fax#"));
		commentField.setValue(model.getValueAt(row, "Comment"));
	}
}
