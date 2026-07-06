package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;

public class BooleanField extends SimpleField{

	private AbstractButton cb;

	public BooleanField(String label, Object object) {
		super(label, object);
		v = object;
		if (v == null) v = "false";
		if (v.toString().equals("true")) {
			cb.setSelected(true);
		}
		else {
			cb.setSelected(false);
		}
	}
	
	@Override
	protected void addComponents(){
		cb = new JCheckBox();
		cb.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				fireAction(e);
				v = !cb.isSelected();
			}
		});
		add(l, BorderLayout.WEST);
		add(cb, BorderLayout.CENTER);
	}
	
	@Override
	public void addActionListener(ActionListener l) {
		actionListeners.add(l);
		cb.addActionListener(l);
	}

	public Object getValue() {
		//return v;
		return cb.isSelected();
	}
	
	protected void setValue(Object object) {
		v = object;
		if (v == null) v = "";
		if (v.toString().equals("true")) {
			cb.setSelected(true);
		}
		else {
			cb.setSelected(false);
		}
	}
	
	public void addButtonListener(ActionListener actionListener) {
		if (cb!=null) cb.addActionListener(actionListener);
	}

	public void setEditable(boolean b) {
		if(cb != null) cb.setEnabled(b);
	}
	
	public String toString() {
		return l.getText() + ": " + cb.isSelected();
	}

	public boolean isEditable() {
		return cb.isEnabled();
	}

	public boolean getBooleanValue() {
		return cb.isSelected();
	}
}