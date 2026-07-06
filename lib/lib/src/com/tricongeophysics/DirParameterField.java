package com.tricongeophysics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.metal.MetalIconFactory;

public class DirParameterField extends ParameterField {
	
	protected JButton fileChooserButton;
	
	public DirParameterField(Parameter parameter) {
		super(parameter);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void buildFieldPanel() {
		this.add(label); //all fields add label first
		this.add(textField);
		
		//build and add browse button
		fileChooserButton = new JButton(new MetalIconFactory.FolderIcon16());
		this.add(fileChooserButton);
		fileChooserButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String file = TriconFileChooser.launchDirChooser(parameter.getValue(), DirParameterField.this);
				if (file != null) {
					parameter.setValue(file);
					updateField();
				}
			}

		});
	}

	@Override
	public void setValueFromField() {
		parameter.setValue(textField.getText());
		updateField();
	}

	@Override
	public void addParameterValueChangedListener(ParameterValueChangedListener listener) {
		textField.getDocument().addDocumentListener(listener);
	}


}
