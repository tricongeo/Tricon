package com.tricongeophysics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;

public class FileField extends SimpleField{

	private BrowseButton b;
	private String buttonText = "Select";

	public FileField(String label, Object object) {
		super(label, object);
	}
	
	public FileField(String label, Object object, Dimension labelDimension, String buttonText) {
		super(label, object, labelDimension);
		this.buttonText  = buttonText;
	}

	@Override
	protected JButton getButton(){
		b = new BrowseButton();
		b.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "";
				if (v != null) s = v.toString();
				v = TriconFileChooser.launchFilteredFileChooser(s, FileField.this,  buttonText);
				if (v == null) return;
				tf.setText(v.toString());
				fireAction(null);
			}
		});
		return b;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		b.setEnabled(enabled);
	}
}