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


public class DeliverableSummaryWizardPane extends AbstractWizardPage {

	private SummarySpreadsheetPanel summarySheet;
	private JComboBox outgoingJobBox;
	protected String outgoingJob;
	private DbParms dbParms;
	//Object date;

	DeliverableSummaryWizardPane(DbParms dbParms) {
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
		return new JLabel("The Following Deliverables Will Be Created By Clicking \"Next\".");
	}

	public void setItems(ArrayList<TableData> selectedItems) {
		ReflectiveTableModel items = new ReflectiveTableModel(selectedItems);
		summarySheet.setModel(items);
	}

	public class SummarySpreadsheetPanel extends SpreadsheetPanel {

		public SummarySpreadsheetPanel(AbstractSpreadsheetModel model2) {
			super(model2);
			add(getBottomPane(), BorderLayout.SOUTH);
		}

		@Override
		public Component getColButtonPane() {
			return new JPanel();
		}

		private Component getOutGoingJobBox() {
			Object[] jobs = getJobNumbers();
			outgoingJobBox = new JComboBox(jobs);
			outgoingJob = outgoingJobBox.getSelectedItem().toString();
			outgoingJobBox.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					outgoingJob = outgoingJobBox.getSelectedItem().toString();
					updateOutgoingJob();
				}});
			return outgoingJobBox;
		}

		protected void updateOutgoingJob() {
			for (int i=0; i < model.getRowCount(); i++) {
				model.setValueAt(outgoingJob, i, "job");
				model.fireTableDataChanged();
			}
		}

		private Object[] getJobNumbers() {
			Object[] vals = null;
			try {
				DatabaseModel model = DatabaseModel.getDatabaseModel(ZbyteDatabase.Job);
				vals = model.findColumnValues("Job#");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return vals;
		}

		protected Component getBottomPane() {
			JPanel pane = new JPanel();
			pane.setLayout(new BorderLayout());
			//pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));

			//pane.add(c);
			JPanel pane2 = new JPanel();
			pane2.add(new JLabel("Outgoing Job:"));
			pane2.add(getOutGoingJobBox());
			//pane.add(Box.createHorizontalGlue());

			pane.add(pane2, BorderLayout.WEST);

			return pane;
		}

		public AbstractSpreadsheetModel createDeliverableModel(ModelTransferItemFactory dataTransferItemFactory, DatabaseModel delivModel) {
			try {
//				dbParms.dbTable = ZbyteDatabase.DeliverableScan;
//				dbParms.query = "select * from " + ZbyteDatabase.DeliverableScan;
//				DatabaseModel delivModel = DatabaseModel.getDatabaseModel(dbParms);
				return dataTransferItemFactory.addItemsToDatabase(delivModel, ((ReflectiveTableModel)this.model).tableData);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

//		public void setFinishDate(Object date) {
//			for (int i=0; i < model.getRowCount(); i++) {
//				model.setValueAt(date, i, "date");
//				model.fireTableDataChanged();
//			}
//		}
	}
	
	public AbstractSpreadsheetModel buildCreatedDeliverableModel(ModelTransferItemFactory dataTransferItemFactory, DatabaseModel delivModel) {
		return summarySheet.createDeliverableModel(dataTransferItemFactory, delivModel);
	}

	public void setOutgoingJob(String job) {
		outgoingJob = job;
		outgoingJobBox.setSelectedItem(job);
		summarySheet.updateOutgoingJob();
	}
	
//	public void setFinishDate(Object object) {
//		summarySheet.setFinishDate(object);
//		this.date = object;
//	}
}
