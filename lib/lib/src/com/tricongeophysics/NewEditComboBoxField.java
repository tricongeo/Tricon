package com.tricongeophysics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class NewEditComboBoxField extends ComboBoxField implements ModelChangedListener {
	protected EditDbRowPane newEditPane;
	private JButton newButton;

	public NewEditComboBoxField(String label, Object[] values, Object value) {
		super(label, values, value);
		String tableName = label.replace(":", "").replace(" ", "_").toLowerCase();
		newEditPane = makeNewEditPane(tableName, null);
		setNewEditPane(newEditPane);
		//setNewEditPane(tableName, null);
	}

	void setNewEditPane(String tableName, String pkeyName) {
		newEditPane = makeNewEditPane(tableName, pkeyName);
		setNewEditPane(newEditPane);
		//setNewEditPane(new EditDbRowPane(tableName, pkeyName));
	}

	public NewEditComboBoxField(String label) {
		this(label, new String[] {""}, "");
	}
	
	public NewEditComboBoxField(String colName, String tableName) {
		super(colName, new String[] {""}, "");
//		setName(tableName);  //this is so NewZbyteEditComboBox can get correct tableName if it has to recreate editPane
//		setNewEditPane(new EditDbRowPane(tableName, null));
		newEditPane = makeNewEditPane(tableName, null);
		setNewEditPane(newEditPane);
	}

	protected EditDbRowPane makeNewEditPane(String tableName, String pkeyName) {
		return new EditDbRowPane(tableName, pkeyName);
	}

	public void setNewEditPane(EditDbRowPane newEditPane1) {
		newEditPane = newEditPane1;
		setValues(newEditPane.getPkeys());
		//setValue(newEditPane.getValue());
		addButtonListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Object item = comboBox.getSelectedItem();
				int index = comboBox.getSelectedIndex();
				if (index < 0) {
					JOptionPane.showMessageDialog(NewEditComboBoxField.this, "\"" +item + "\" is not valid input for \"" + getName() + "\"", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				newEditPane.setRow(index);
				newEditPane.setVisible(true);
				newEditPane.getPkeyField().setEditable(false); //this is edit mode, we're not creating new pkey here
			}});
		newEditPane.addClickedOKListener(new ClickedOkListener(){
			@Override
			public void clickedOk() {
				setValues(newEditPane.getPkeys());
				setValue(newEditPane.getPkey());
			}});
		newEditPane.addClickedCancelListener(new ClickedCancelListener(){
			@Override
			public void clickedCancel() {
				setValues(newEditPane.getPkeys());
				setValue(newEditPane.getPkey());
			}});
		
		//newEditPane.model.addTableModelListener(this);
		newEditPane.model.addModelChangedListener(this);
	}

	@Override
	public void addComponents() {
		super.addComponents();
		addButton(getNewButton());
	}

	private Component getNewButton() {
		newButton = new NewButton();
		newButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				new NewDbRowPane(newEditPane);
			}});
		return newButton;
	}

	@Override
	public void modelChanged() {
		setValues(newEditPane.getPkeys());
	}
	
	@Override
	public String getHtml() {
		newEditPane.setRow(comboBox.getSelectedIndex());
		return newEditPane.getHtml();
	}
	
	@Override
	public void setEditable(boolean b) {
		super.setEditable(b);
		comboBox.setEnabled(b);
		newButton.setEnabled(b);
	}

	public int getSelectedItemIndex() {
		return comboBox.getSelectedIndex();
	}

	public void setSelectedItem(Object item) {
		comboBox.setSelectedItem(item);
	}
}
