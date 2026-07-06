package com.tricongeophysics;

import java.awt.Component;
import java.sql.SQLException;

import javax.swing.JPanel;

public class MainDbPanel extends DbSpreadsheetPanel {
	
	MainDbPanel(DbParms dbParms) throws SQLException {
		super(dbParms);
		this.setName("Main");
	}
	
	@Override
	public Component getRowButtonPane() {
		return new JPanel();
	}

	@Override
	public String[] getEditableColumns() {
		return null;
	}
}
