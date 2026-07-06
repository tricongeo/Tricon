package com.tricongeophysics;

import java.awt.Container;
import java.awt.Dimension;
import java.sql.SQLException;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

public class WorkOrders extends JApplet 
{
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
			
			DbSpreadsheetPanel in_transmittalPanel = ZbyteDbSpreadsheetPanel.makePanel("ZbyteDatabase", ZbyteDatabase.WorkOrder);
			in_transmittalPanel.setEditableColumns(new String[]{""});
			
			
			JTabbedPane tabbedPane = new JTabbedPane();
			
		
			tabbedPane.add(in_transmittalPanel);
			
			c.add(tabbedPane);
			
		} catch (SQLException e) {
			
			//p.setText("Failed to connect to MySQL Server.\n"+e);
			c.add(new JLabel("<HTML>Failed to connect to MySQL Server<br>"+e));
		}
		
		//setSize(500,500);
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
		JFrame frame = new JFrame("Work Orders");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		WorkOrders gj = new WorkOrders();
		gj.setPreferredSize(new Dimension(900, 500));
		gj.setVisible(true);
		gj.init();
		
		frame.getContentPane().add(gj);
		
		frame.pack();
		frame.setVisible(true);
	}

}
