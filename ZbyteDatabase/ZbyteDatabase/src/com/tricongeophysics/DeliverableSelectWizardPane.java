package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class DeliverableSelectWizardPane extends AbstractWizardPage {

	private DeliverableSelectSheet deliverableSheet;
	public Object date;

	public DeliverableSelectWizardPane(DbParms dbParms) throws SQLException {
		super();
		setName("select");

		deliverableSheet = new DeliverableSelectSheet(dbParms);
		deliverableSheet.setBorder(BorderFactory.createEtchedBorder());

		setLayout(new BorderLayout());
		add(getTopLabel(), BorderLayout.NORTH);
		add(deliverableSheet, BorderLayout.CENTER);
		add(getBottomPane(), BorderLayout.SOUTH);
	}

	private Component getBottomPane() {
		return new JLabel();
	}

	private Component getTopLabel() {
		return new JLabel("Select Original Items.");
	}

	public ArrayList<TableData> loadSelectedItems(ModelTransferItemFactory dataTransferItemFactory) {
		return deliverableSheet.loadSelectedItems(dataTransferItemFactory);
	}

	public String getJobNum() {
		return deliverableSheet.getJobNum();
	}
	
	public boolean getChanged() {
		return  deliverableSheet.getChanged();
	}

	public void setChanged(boolean b) {
		deliverableSheet.setChanged(false);
	}
	
	public void setVisibleColumns(String[] colNames) {
		deliverableSheet.setVisibleColumns(colNames);
	}

	public void setFinishDate(Object date) {
		this.date = date;
		deliverableSheet.setSelectedItems("Copy Date", date);
	}
}
