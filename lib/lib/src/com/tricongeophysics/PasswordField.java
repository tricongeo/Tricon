package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPasswordField;

public class PasswordField extends SimpleField {

	public PasswordField(String label, Object value) {
		super(label, value);
	}
	
	protected void addComponents() {
		tf = new JPasswordField();
		tf.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				//setValue(tf.getText());
				fireAction(e);
			}});
		if (v != null) tf.setText(v.toString());
		add(l, BorderLayout.WEST);
		add(tf, BorderLayout.CENTER);
		if (bb != null) add(bb, BorderLayout.EAST);
	}

}
