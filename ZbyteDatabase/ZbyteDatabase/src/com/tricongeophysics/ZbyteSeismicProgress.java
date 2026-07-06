package com.tricongeophysics;

import java.awt.Container;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JApplet;
import javax.swing.JLabel;

public class ZbyteSeismicProgress extends JApplet implements PageFinishedListener 
{
	
	private Wizard w;
	private DeliverableSelectWizardPane deliverable_select;
	private DeliverableSummaryWizardPane deliverable_summary;
	private DeliverableCreatedWizardPane deliverable_created;
	private DbParms dbParms;

	@Override
	public void init() {
		Container c = this.getContentPane();
		try {
			try {
				String test = this.getParameter("Test");
				DbParms.test = test;
			} catch (Exception e) { 
				//e.printStackTrace();
			};
			
			//...Deliverable Select Pane
			dbParms = DbParms.getParms("ZbyteDatabase");
			dbParms.dbTable = ZbyteDatabase.OriginalMedia;
			dbParms.query = "select * from " + ZbyteDatabase.OriginalMedia;
			deliverable_select = new DeliverableSelectWizardPane(dbParms);
			deliverable_select.setVisibleColumns(new String[] {"Select", "id", "Barcode", "Image File", "Line #", "Copy Date", "Operator:","Seis Type:", "Format:", "Status", "Media:", "Media Id", "Job:", "Control Num:", "Comment"});
			
			//...Deliverable Summary Pane
			deliverable_summary = new DeliverableSummaryWizardPane(dbParms);
			
			//...Deliverable Created Pane
			dbParms.dbTable = ZbyteDatabase.DeliverableMedia;
			dbParms.query = "select * from " + ZbyteDatabase.DeliverableMedia;
			deliverable_created = new DeliverableCreatedWizardPane(dbParms);
			
			w = new Wizard("Seismic Progress");
			w.addWizardPage(deliverable_select);
			w.addWizardPage(deliverable_summary);
			w.addWizardPage(deliverable_created);
			w.setCancelText("Close");
			w.addPageFinishedListener(this);
			
			//deliverable_progress.addClickedCreateButtonListener(this);
			
//			JTabbedPane tabbedPane = new JTabbedPane();			
		
//			tabbedPane.add(deliverable_progress);
			
			//c.add(w);
			
			w.createAndShow();
			
			this.setVisible(false);
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

	protected static void createAndShowGUI() {
//		JFrame frame = new JFrame("Zbyte Progress");
//		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		
		ZbyteSeismicProgress gj = new ZbyteSeismicProgress();
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
		SeismicTransferItemFactory stif = new SeismicTransferItemFactory();
		if (currentPage == deliverable_select) {
			deliverable_select.setFinishDate(getTodaysDate());
			ArrayList<TableData> selectedItems = deliverable_select.loadSelectedItems(stif);
			deliverable_summary.setItems(selectedItems);
			String incomingJob = deliverable_select.getJobNum();
			deliverable_summary.setOutgoingJob(incomingJob);
			//deliverable_summary.setFinishDate(getTodaysDate());
		}
		if (currentPage == deliverable_summary) {
			if (deliverable_select.getChanged()) {
				DatabaseModel model;
				try {
					dbParms.dbTable = ZbyteDatabase.DeliverableMedia;
					dbParms.query = "select * from " + ZbyteDatabase.DeliverableMedia;
					model = DatabaseModel.getDatabaseModel(dbParms);
					AbstractSpreadsheetModel m = deliverable_summary.buildCreatedDeliverableModel(stif, model);
					deliverable_created.setModel(m);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String outgoingJob = deliverable_summary.outgoingJob;
				deliverable_created.setOutgoingJob(outgoingJob);
				Object date = deliverable_select.date;
				deliverable_created.setDate(date);
				deliverable_created.setLabel("The following items were created " + date + " for job " + outgoingJob);
				deliverable_select.setChanged(false);
			}
		}
	}

	private Object getTodaysDate() {
		java.util.Date date = new java.util.Date();
		Date date2 = new java.sql.Date(date.getTime());
		return date2;
	}

}
