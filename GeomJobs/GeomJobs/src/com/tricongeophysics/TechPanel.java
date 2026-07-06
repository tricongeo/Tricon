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

public class TechPanel extends GeomSpreadsheetPanel {

	TechPanel(DbParms p) throws SQLException {
		super(p);
		this.setName("Tech");
		setBaseSearch("", "Technician");
	}

	@Override
	protected DatabaseModelSearchPane makeSearchPane() {
		return new DualSearchPane((DatabaseModel) model, dbParms.dbTable, "Technician");
	}
	
	@Override
	public String[] getEditableColumns() {
		return new String[]{"Date Finished", "Focus Project", "Focus Line", "Comments", "Done"};
	}
	
	@Override
	public Component getRowButtonPane() {
		Component cp = this.getCopyPastePane(BoxLayout.Y_AXIS);
		//Component rp = super.getRowButtonPane();
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.add(cp);
		pane.add(Box.createVerticalGlue());
		//pane.add(rp);
		pane.add(Box.createVerticalGlue());
		
		return pane;
	}
}
