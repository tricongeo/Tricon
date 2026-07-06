package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class OutTransmittalSummaryWizardPane extends AbstractWizardPage {

	private SummarySpreadsheetPanel summarySheet;
	private DbParms dbParms;
	Object date;

	OutTransmittalSummaryWizardPane(DbParms dbParms) {
		super();
		setName("summary");

		this.dbParms = dbParms;

		summarySheet = new SummarySpreadsheetPanel(new ReflectiveTableModel());
		summarySheet.setBorder(BorderFactory.createEtchedBorder());

		setLayout(new BorderLayout());
		add(getTopLabel(), BorderLayout.NORTH);
		add(summarySheet, BorderLayout.CENTER);
	}

	private Component getTopLabel() {
		return new JLabel("The Following Items Will Be Added To This Transmittal By Clicking \"Next\".");
	}

	public void setItems(ArrayList<TableData> selectedItems) {
		ReflectiveTableModel items = new ReflectiveTableModel(selectedItems);
		summarySheet.setModel(items);
	}

	public class SummarySpreadsheetPanel extends SpreadsheetPanel {

		public SummarySpreadsheetPanel(AbstractSpreadsheetModel model2) {
			super(model2);
			//add(getBottomPane(), BorderLayout.SOUTH);
		}

		@Override
		public Component getColButtonPane() {
			return new JPanel();
		}

		public AbstractSpreadsheetModel createDeliverableModel(ModelTransferItemFactory dataTransferItemFactory) {
			try {
				dbParms.dbTable = ZbyteDatabase.DeliverableScan;
				dbParms.query = "select * from " + ZbyteDatabase.DeliverableScan;
				DatabaseModel delivModel = DatabaseModel.getDatabaseModel(dbParms);
				return dataTransferItemFactory.addItemsToDatabase(delivModel, ((ReflectiveTableModel)this.model).tableData);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		public void setFinishDate(Object date) {
			for (int i=0; i < model.getRowCount(); i++) {
				model.setValueAt(date, i, "date");
				model.fireTableDataChanged();
			}
		}
	}
	
	public void setFinishDate(Object object) {
		summarySheet.setFinishDate(object);
		this.date = object;
	}

	public AbstractSpreadsheetModel getModel() {
		return summarySheet.model;
	}
}
