package com.tricongeophysics;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NewColumnDialog implements ActionListener {
	
	//Defaults are set to SPSOUT standard
	protected JTextField textField;
	protected JComboBox comboBox;
	protected enum SupportedClassNames {Float, Integer, Text, True_False};
	protected SupportedClassNames className = SupportedClassNames.Float;
	protected JButton okButton = new JButton("OK");
	protected JButton cancelButton = new JButton("Cancel");
	protected boolean clickedOK;
	private Component dialog;

	public NewColumnDialog(){
		super();
	}
	
	public void showDialog() {
		//ColumnInfo ci = ncd.showDialog();
		//dialog.show();
		//JPanel background = new JPanel(new BorderLayout());
		//background.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		textField = new JTextField();
		textField.setPreferredSize(new Dimension(100,25));
		comboBox = new JComboBox(SupportedClassNames.values());
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		JOptionPane pane = new JOptionPane("", JOptionPane.QUESTION_MESSAGE);
		JPanel nameField = new JPanel();
		nameField.add(new JLabel("Name for New Column:"));
		nameField.add(textField);
		JPanel classField = new JPanel();
		classField.add(new JLabel("Column Type:"));
		classField.add(comboBox);
		JPanel buttonsPane = new JPanel();
		buttonsPane.add(okButton);
		buttonsPane.add(cancelButton);
		JPanel allPane = new JPanel();
		allPane.setLayout(new BoxLayout(allPane, BoxLayout.Y_AXIS));
		//allPane.add(new JLabel("Name for New Column:"));
		allPane.add(nameField);
		allPane.add(classField);
		allPane.add(buttonsPane);
		//pane.setOptions(new Object[] {textField, comboBox, okButton, cancelButton});
		pane.setOptions(new Object[] {allPane});
		dialog = pane.createDialog("Name for New Column");
		dialog.setVisible(true);
		
		/*
		background.add(label,BorderLayout.WEST);
		background.add(textField,BorderLayout.CENTER);
		background.add(comboBox,BorderLayout.SOUTH);
		getContentPane().add(background);
		//setSize(800,800);
		pack();
		setVisible(true);
		
		
		ci.setName(textField.getText());
		ci.setClass(className);
		
		return ci;
		*/
		
	} // end gshowDialogue

	public Component getDialog() {
		return dialog;
	}

	public void setDialog(Component dialog) {
		this.dialog = dialog;
	}

	public boolean getClickedOK() {
		return clickedOK;
	}

	public String getColumnName() {
		if (textField == null) return null;
		return textField.getText();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(okButton))
				clickedOK = true;
		if (e.getSource().equals(cancelButton))
			clickedOK = false;
		dialog.setVisible(false);
	}

	public Class getColClass() {
		SupportedClassNames className = (SupportedClassNames) comboBox.getSelectedItem();
		if (className.equals(SupportedClassNames.True_False)) return Boolean.class;
		if (className.equals(SupportedClassNames.Float)) return Double.class;
		//if (className.equals(SupportedClassNames.Float)) return Float.class;
		if (className.equals(SupportedClassNames.Integer)) return Integer.class;
		if (className.equals(SupportedClassNames.Text)) return String.class;
		return null;
	}
	
}
