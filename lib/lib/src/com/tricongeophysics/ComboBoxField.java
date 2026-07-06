package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class ComboBoxField extends SimpleField {

	protected Object[] vals;
	protected JComboBox comboBox;
	private JPanel buttonPane;

	public ComboBoxField(String label, Object value) {
		super(label, value);
	}

	public ComboBoxField(String label, Object[] vals, Object value) {
		super(label, value);
		setValues(vals);
	}
	
	void setValues(Object[] vals) {
		this.vals = vals;
		if (vals == null) return;
		comboBox.setModel(new DefaultComboBoxModel(vals));
	}

	@Override
	protected void addComponents() {
		add(l, BorderLayout.WEST);
		
		comboBox = new JComboBox();
		comboBox.setSelectedItem(v);
		comboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				//setValue(comboBox.getSelectedItem());
				fireAction(e);
				comboBox.setToolTipText(comboBox.getSelectedItem()+"");
			}});
		
		add(comboBox, BorderLayout.CENTER);
		buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		add(buttonPane, BorderLayout.EAST);
		
		if (bb != null) addButton(bb);
	}
	
	protected void addButton(Component component) {
		buttonPane.add(component);
	}

	@Override
	protected JButton getButton() {
		EditButton b = new EditButton();
		return b;
	}
	
	@Override
	protected void setValue(Object value) {
		//super.setValue(value);
		//tf.setText(value.toString());
		comboBox.setEditable(true);
		comboBox.setSelectedItem(value);
		comboBox.setEditable(false);
		comboBox.setToolTipText(value+"");
	}
	
	@Override
	public Object getValue() {
		return comboBox.getSelectedItem();
	}
}
