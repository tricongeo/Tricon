package com.tricongeophysics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DualSearchPane extends DatabaseModelSearchPane{

	private String primaryField;
	private JComboBox cBox;
	private JLabel primaryKeyLabel;

	public DualSearchPane(DatabaseModel dbModel, String table, String primaryField) {
		super(dbModel, table);
		this.primaryField = primaryField;
		updatePrimarySearchFields();
	}
	
	private void updatePrimarySearchFields() {
		if (primaryField == null) return;
		primaryKeyLabel.setText(primaryField.replace(":", "") + ":");
		Object[] vals = getPrimarySearchValues();
		if (vals == null) vals = new String[]{""};
		cBox.setModel(new DefaultComboBoxModel(vals));
	}

	protected Component getButtonsPane() {
		Component c = super.getButtonsPane();
		
		JPanel panel = new JPanel();
		
		cBox = new JComboBox();
		cBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object val = cBox.getSelectedItem();
				setBaseSearch(val, primaryField);
				DualSearchPane.super.updateBoxLists();
				excecuteSearchOnModel();
			}});
		
		primaryKeyLabel = new JLabel();
		
		panel.add(primaryKeyLabel);
		panel.add(cBox);
		panel.add(c);
		return panel;
	}
	
	private Object[] getPrimarySearchValues() {
		Object[] vals = null;
		if (primaryField == null) return null;
		if (primaryField == "") return null;
		try {
			model = DatabaseModel.getDatabaseModel(dbTable);
			vals = model.findColumnValues(primaryField);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vals;
	}

	public void setPrimaryFieldLabel(String text) {
		primaryKeyLabel.setText(text);
	}
	
	@Override
	public Object getSelectedPkeyVal() {
		return cBox.getSelectedItem();
	}
	
	@Override
	public void setPrimarySearchValue(Object val) {
		cBox.setSelectedItem(val);
	}
	
	@Override
	protected void updateBoxLists() {
		super.updateBoxLists();
		updatePrimarySearchFields();
	}
	
	@Override
	public void setSecondarySearchValue(Object val) {
		valueBox.setSelectedItem(val);
	}
	
	@Override
	public Object getPrimarySearchValue() {
		return cBox.getSelectedItem();
	}
}
