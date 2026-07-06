package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ProcessorPanel extends GeomSpreadsheetPanel {

	ProcessorPanel(DbParms p) throws SQLException {
		super(p);
		this.setName("Processor");
		//setBaseSearch(cBox.getSelectedItem(), "Processor");
	}

	@Override
	public Component getRowButtonPane() {
		return new JPanel();
	}
	
	@Override
	protected DatabaseModelSearchPane makeSearchPane() {
		return new DualSearchPane((DatabaseModel) model, dbParms.dbTable, "Processor");
	}
	
	@Override
	public String[] getEditableColumns() {
		return null;
	}
}
