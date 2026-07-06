package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

public abstract class GeomSpreadsheetPanel extends DbSpreadsheetPanel {

	public GeomSpreadsheetPanel(DbParms dbParms) throws SQLException {
		super(dbParms);
	}
	
	@Override
	public void addOptionalColumns(DatabaseModel model){
		DbColumn jobDoneColumn = new JobDoneColumn(model);
		DbColumn jobLateColumn = new JobLateColumn(model);
		model.addOptionalColumn(jobDoneColumn);
		model.addOptionalColumn(jobLateColumn);
		model.setErrorColumn(jobLateColumn);
		model.setWarningColumn(jobDoneColumn);
	}
	
}