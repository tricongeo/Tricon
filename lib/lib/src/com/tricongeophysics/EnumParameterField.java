package com.tricongeophysics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class EnumParameterField extends ParameterField {
	
	protected JComboBox comboBox;
	

	public EnumParameterField(EnumParameter parameter) {
		super(parameter);
	}

	@Override
	protected void buildFieldPanel() {
	    if (((EnumParameter)parameter).getOptions() == null) {
	        ((EnumParameter)parameter).setOptions(new String[]{""});
        }
		comboBox = new JComboBox(((EnumParameter)parameter).getOptions());
		comboBox.addActionListener(this);
		comboBox.setToolTipText(parameter.getHelp());
		updateField();
		this.add(label);
		this.add(comboBox);
	}

	@Override
	public void updateField() {
		parameter.valueIsOk(); //this sets the selected index
		comboBox.setSelectedIndex(((EnumParameter)parameter).getSelectedIndex()); 
	}

	@Override
	public void setValueFromField() {
		parameter.setValue(comboBox.getSelectedItem().toString());
		updateField();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		comboBox.setEnabled(enabled);
		label.setEnabled(enabled);
	}

	@Override
	public void addParameterValueChangedListener(ParameterValueChangedListener listener) {
		comboBox.addActionListener(listener);
	}

    public void updateOptions()
    {
        comboBox.setModel(new DefaultComboBoxModel(((EnumParameter)parameter).getOptions()));
    }

	public void setOptions(Object[] validGroups) {
		((EnumParameter)parameter).setOptions(validGroups);
		updateOptions();
	}

	public Object getSelectedItem() {
		return comboBox.getSelectedItem();
	}

}
