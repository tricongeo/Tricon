package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;

public class ZbyteDatabaseView extends JApplet 
{
	private ArrayList<DbSpreadsheetPanel> panels;
	private JPasswordField passwordField;

	@Override
	public void init() {
		Container c = this.getContentPane();
		try {
			
			String[] tableList = DatabaseModel.getTableList("ZbyteDatabase");
			panels = new ArrayList<DbSpreadsheetPanel>();
			for (String tableName: tableList) {
				panels.add(ZbyteDbSpreadsheetPanel.makePanel("ZbyteDatabase", tableName));
			}
			
//			DbSpreadsheetPanel boxPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", "box");
//			
//			DbSpreadsheetPanel clientPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", "client");
//
//			DbSpreadsheetPanel contactPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", "contact");
//
//			DbSpreadsheetPanel in_transmittalPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", "in_transmittal");
//
//			DbSpreadsheetPanel jobPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", "job");
//
//			DbSpreadsheetPanel out_transmittalPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", "out_transmittal");
//
//			DbSpreadsheetPanel ZbyteDatabase.OriginalHardCopyPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", ZbyteDatabase.OriginalHardCopy);
//
//			DbSpreadsheetPanel ZbyteDatabase.OriginalMediaPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", ZbyteDatabase.OriginalMedia);
//
//			DbSpreadsheetPanel codePanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", "bill_codes");
//			
//			DbSpreadsheetPanel deliverable_otherPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", "deliverable_other");
//			
//			DbSpreadsheetPanel deliverable_seismicPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", "deliverable_seismic");
//			
//			DbSpreadsheetPanel deliverable_supportPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", ZbyteDatabase.DeliverableScan);
//
//			DbSpreadsheetPanel deliverable_zscanPanel = DbSpreadsheetPanel.makePanel("ZbyteDatabase", "deliverable_zscan");

			
			JTabbedPane tabbedPane = new JTabbedPane();
			
			for (DbSpreadsheetPanel panel: panels) {
				tabbedPane.add(panel);
			}
			
//			tabbedPane.add(boxPanel);
//			tabbedPane.add(clientPanel);
//			tabbedPane.add(contactPanel);
//			tabbedPane.add(in_transmittalPanel);
//			tabbedPane.add(jobPanel);
//			tabbedPane.add(out_transmittalPanel);
//			tabbedPane.add(ZbyteDatabase.OriginalHardCopyPanel);
//			tabbedPane.add(ZbyteDatabase.OriginalMediaPanel);
//			tabbedPane.add(codePanel);
//			tabbedPane.add(deliverable_otherPanel);
//			tabbedPane.add(deliverable_seismicPanel);
//			tabbedPane.add(deliverable_supportPanel);
//			tabbedPane.add(deliverable_zscanPanel);
			
			
			JPanel p = new JPanel(new BorderLayout());
			p.add(tabbedPane);
			//p.add(BorderLayout.SOUTH, getPasswordPane());
			
			c.add(p);
			
		} catch (SQLException e) {
			
			//p.setText("Failed to connect to MySQL Server.\n"+e);
			c.add(new JLabel("<HTML>Failed to connect to MySQL Server<br>"+e.getMessage()));
		}
		
		//setSize(500,500);
		//passwordCorrect(false);
	}
	
//	Component getPasswordPane() {
//		passwordField = new JPasswordField(10);
//		//passwordField.setPreferredSize(new Dimension(100,30));
//		passwordField.addActionListener(new ActionListener(){
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				DbParms dbParms = DbParms.getParms("ZbyteDatabase");
//				if (passwordField.getText().equals(dbParms.adminPWord)) {
//					passwordCorrect(true);
//				} else {
//					passwordCorrect(false);
//				}
//			}});
//
//		JLabel label = new JLabel("Enter the password: ");
//		label.setLabelFor(passwordField);
//		
//		JPanel passwordPane = new JPanel();
//		passwordPane.setLayout(new BoxLayout(passwordPane, BoxLayout.X_AXIS));
//		passwordPane.add(label);
//		passwordPane.add(passwordField);
//		passwordPane.add(Box.createHorizontalGlue());
//		
//		JPanel outerPane = new JPanel();
//		outerPane.setLayout(new BorderLayout());
//		outerPane.add(passwordPane, BorderLayout.WEST);
//		outerPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//		return outerPane;
//	}

	protected void passwordCorrect(boolean b) {
		for (DbSpreadsheetPanel panel: panels) {
			panel.setEditable(b);
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
		JFrame frame = new JFrame("ZByte Database");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		ZbyteDatabaseView gj = new ZbyteDatabaseView();
		gj.setPreferredSize(new Dimension(1800, 1100));
		gj.setVisible(true);
		gj.init();
		
		frame.getContentPane().add(gj);
		
		frame.pack();
		frame.setVisible(true);
	}

}
