package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DeliverableCreatedWizardPane extends AbstractWizardPage {

	private DeliverableCreatedSheet deliverableSheet;
	private JLabel topLabel;

	public DeliverableCreatedWizardPane(DbParms dbParms) throws SQLException {
		super();
		setName("created");

		deliverableSheet = new DeliverableCreatedSheet(dbParms);
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
		topLabel = new JLabel("These Deliverable Items Were Created.");
		return topLabel;
	}

	public void setModel(AbstractSpreadsheetModel m) {
		deliverableSheet.setModel(m);
	}

	public void setBaseSearch(Object value, String field) {
		deliverableSheet.setBaseSearch(value, field);
	}

	public void setOutgoingJob(String outgoingJob) {
		deliverableSheet.setPrimarySearchValue(outgoingJob);
	}

	public void setDate(Object date) {
		deliverableSheet.setDate(date);
	}

	public void setLabel(String string) {
		topLabel.setText(string);
	}

}
