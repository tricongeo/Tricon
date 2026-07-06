package com.tricongeophysics;

import java.applet.Applet;
import java.awt.Container;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

public class GeomJobs extends JApplet 
{
	@Override
	public void init() {
		Container c = this.getContentPane();

		GeomSpreadsheetPanel adminPanel;
		try {
			DbParms p = DbParms.getParms("GeomJobs");
			
			adminPanel = new AdminPanel(p);
			GeomSpreadsheetPanel procPanel = new ProcessorPanel(p);
			GeomSpreadsheetPanel techPanel = new TechPanel(p);
			DbSpreadsheetPanel   mainPanel = new MainDbPanel(p);
			GeomSpreadsheetPanel managerPanel = new ManagerPanel(p);
			
			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.add(mainPanel);
			tabbedPane.add(procPanel);
			tabbedPane.add(techPanel);
			tabbedPane.add(adminPanel);
			tabbedPane.add(managerPanel);
			
			c.add(tabbedPane);
			
		} catch (SQLException e) {
			
			//p.setText("Failed to connect to MySQL Server.\n"+e);
			c.add(new JLabel("<HTML>Failed to connect to MySQL Server<br>"+e));
		}
		
		setSize(500,500);
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
		JFrame frame = new JFrame("Geometry Jobs");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		GeomJobs gj = new GeomJobs();
		gj.setPreferredSize(new Dimension(1800, 1100));
		gj.setVisible(true);
		gj.init();
		
		frame.getContentPane().add(gj);
		
		frame.pack();
		frame.setVisible(true);
	}

}
