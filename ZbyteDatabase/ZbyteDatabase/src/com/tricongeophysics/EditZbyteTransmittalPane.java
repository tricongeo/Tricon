package com.tricongeophysics;

import java.awt.Container;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JLabel;

public class EditZbyteTransmittalPane extends ZbyteEditDbRowPane implements PageFinishedListener 
{
	
	public EditZbyteTransmittalPane() {
		super(ZbyteDatabase.ZbyteTransmittal, null);
		// TODO Auto-generated constructor stub
	}

	private Wizard w;
	private TransmittalSelectWizardPane ot_select;
	private OutTransmittalSummaryWizardPane ot_summary;
	private PrintTransmittalWizardPane ot_print;
	private StartTransmittalPane ot_start;
	
	@Override
	public void setRow(int row) {
		if (ot_start == null) return;
		ot_start.setRow(row);
	}

	public void init() {
		Container c = this.getContentPane();
		try {		
			DbParms dbParms = DbParms.getParms("ZbyteDatabase");
			dbParms.dbTable = ZbyteDatabase.ZbyteTransmittal;
			dbParms.query = "select * from " + ZbyteDatabase.ZbyteTransmittal;
			ot_start = new StartTransmittalPane(dbParms);
			
			//...Original Select Panes
			dbParms.dbTable = ZbyteDatabase.ZbyteTransmittal;
			dbParms.query = "select * from " + ZbyteDatabase.ZbyteTransmittal;
			ot_select = new TransmittalSelectWizardPane(dbParms);
			
			//...Deliverable Summary Pane
			dbParms.dbTable = ZbyteDatabase.ZbyteTransmittal;
			dbParms.query = "select * from " + ZbyteDatabase.ZbyteTransmittal;
			ot_summary = new OutTransmittalSummaryWizardPane(dbParms);
			
			//...Deliverable Created Pane
			dbParms.dbTable = ZbyteDatabase.ZbyteTransmittal;
			dbParms.query = "select * from " + ZbyteDatabase.ZbyteTransmittal;
			ot_print = new PrintTransmittalWizardPane(dbParms);
			
			w = new Wizard("Inter-Office Transmittal");
			w.addWizardPage(ot_start);
			w.addWizardPage(ot_select);
			w.addWizardPage(ot_summary);
			w.addWizardPage(ot_print);
			w.setCancelText("Close");
			w.addPageFinishedListener(this);
			
			//deliverable_progress.addClickedCreateButtonListener(this);
			
//			JTabbedPane tabbedPane = new JTabbedPane();			
		
//			tabbedPane.add(deliverable_progress);
			
			//c.add(w);
			
			w.createAndShow();
			
			super.setVisible(false);
			//this.destroy();
			
//			c.add(tabbedPane);
			
		} catch (SQLException e) {
			c.add(new JLabel("<HTML>Failed to connect to MySQL Server<br>"+e));
		}
	}
	
	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
	
	@Override
	public void loadFields() {
		//super.loadFields();
	//	init();
	}

	protected static void createAndShowGUI() {
//		JFrame frame = new JFrame("Zbyte Progress");
//		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		
		EditZbyteTransmittalPane gj = new EditZbyteTransmittalPane();
//		gj.setPreferredSize(new Dimension(500, 500));
		//gj.setVisible(true);
		gj.init();
		
//		frame.getContentPane().add(gj);
//		
//		frame.pack();
//		frame.setVisible(true);
	}

	@Override
	public void pageFinished(WizardPage currentPage) {
		TransmittalTransferItemFactory stif = new TransmittalTransferItemFactory();
		if (currentPage == ot_start) {
			//ArrayList<TableData> selectedItems = ot_select.loadSelectedItems(stif);
			//deliverable_summary.setItems(selectedItems);
			ot_start.clickedOK();
			this.clickedOK();
			Object job = ot_start.getJobNum();
			ot_select.setPkeySearchField("Job:");
			ot_select.setPkeySearchValue(job);
			//deliverable_summary.setOutgoingJob(incomingJob);
		}
		if (currentPage == ot_select) {
			ArrayList<TableData> selectedItems = ot_select.loadSelectedItems(stif);
			ot_summary.setItems(selectedItems);
			//ot_start.clickedOK();
			//Object job = ot_start.getJobNum();
			//ot_select.setPkeySearchField("Job:");
			//deliverable_summary.setOutgoingJob(incomingJob);
			//liverable_summary.setFinishDate(getTodaysDate());
		}
		if (currentPage == ot_summary) {
//			if (ot_select.getChanged()) {
//				ot_select.setChanged(false);
//			}
			String ot_id = ot_start.getTransmittalId();
			
			ot_select.saveToDatabase(ot_id);			
			
			ot_start.refreshSearch();
			ArrayList<TableData> selectedItems = ot_start.loadTransmittalItems(stif);
			ot_print.setModel(selectedItems);
			//ot_print.setHtml(ot_start.getHtml());
			InterOfficeTransmittalHtmlFormatter thf = new InterOfficeTransmittalHtmlFormatter(ot_start.getEditDbRowPane());
			thf.setSheet(ot_print.getSheet());
			ot_print.setTransmittalID(ot_id);
			ot_print.setHtml(thf.getHtml());
		}
	}

	private Object getTodaysDate() {
		java.util.Date date = new java.util.Date();
		Date date2 = new java.sql.Date(date.getTime());
		return date2;
	}

	@Override
	public void setVisible(boolean b) {
		initialize();
		init();
		super.setVisible(false);
	}
}
