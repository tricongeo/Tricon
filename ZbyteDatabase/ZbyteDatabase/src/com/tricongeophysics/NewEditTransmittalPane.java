package com.tricongeophysics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class NewEditTransmittalPane extends NewEditDbPane {

	private ResultSet rs;
	private JLabel idLabel;
	private FileField fileField;
	private DateField arrivalDateField;
	private NewEditComboBoxField jobField;

	public NewEditTransmittalPane() {
		super();
		setTitle("New/Edit Transmittal");
	}
	
	@Override
	protected Component getTopPane() {
		JPanel panel = new JPanel();
		
		panel.add(getIDField());
		panel.add(getFileField());
		panel.add(getJobField());
		panel.add(getArrivalDateField());
		
		return panel;
	}

	private Component getArrivalDateField() {
		//... Make Arrival Date picker field
		Object d = model.getValueAt(row, "Arrival Date");
		arrivalDateField = new DateField("Arrival Date:", d);
		arrivalDateField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
			model.setValueAt(arrivalDateField.getValue(), row, "Arrival Date");
		}});
		return arrivalDateField;
	}

	private Component getJobField() {
		//...Make Job # combobox field
		jobField = new NewEditComboBoxField("Job#:");
		//jobField.setNewEditPane(new NewEditJobPane());
		jobField.setNewEditPane(new EditDbRowPane(ZbyteDatabase.Job, "id"));
		jobField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				model.setValueAt(jobField.getValue(), row, "Job#");
			}});
		return jobField;
	}

	private Component getFileField() {
		//... Make Transmittal file chooser field
		String filepath = "Transmittal File";
		if (model.getValueAt(row, "File Path") != null) {
			filepath = model.getValueAt(row, "File Path").toString();
		}
		
		fileField = new FileField("File Path:", filepath);
		fileField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
			model.setValueAt(fileField.getValue(), row, "File Path");
		}});
		return fileField;
	}

	private Component getIDField() {
		//... Make ID Label
		Object id = model.getValueAt(row, "id");
		idLabel = new JLabel("Transmittal ID: " + id);
		return idLabel;
	}

	@Override
	protected String getTableName() {
		return ZbyteDatabase.InTransmittal;
	}

	@Override
	protected String getPkeyName() {
		return "id";
	}
	
	@Override
	protected void rowChanged() {
		Object id = model.getValueAt(row, "id");
		idLabel.setText("Transmittal ID: " + id);
		fileField.setValue(model.getValueAt(row, "File Path"));
		arrivalDateField.setValue(model.getValueAt(row, "Arrival Date"));
		jobField.setValue(model.getValueAt(row, "Job#"));
	}

}
