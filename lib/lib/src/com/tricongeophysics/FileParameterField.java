package com.tricongeophysics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.plaf.metal.MetalIconFactory;

public class FileParameterField extends ParameterField {
	
	protected JButton fileChooserButton;
	private String buttonText = "Open";

	public FileParameterField(Parameter parameter) {
		super(parameter);
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
				String file = TriconFileChooser.launchFilteredFileChooser(parameter.value, ((FileParameter)parameter).getFilterList(), FileParameterField.this, buttonText);
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

	public void setButtonText(String text) {
		buttonText  = text;
	}

}
