package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class TriconDb extends JApplet 
{
	private ArrayList<DbSpreadsheetPanel> panels;

	@Override
	public void init() {
		Container c = this.getContentPane();
		try {
			
			String[] tableList = DatabaseModel.getTableList("TriconDb");
			panels = new ArrayList<DbSpreadsheetPanel>();
			for (String tableName: tableList) {
				panels.add(DbSpreadsheetPanel.makePanel("TriconDb", tableName));
			}
			
			JTabbedPane tabbedPane = new JTabbedPane();
			
			for (DbSpreadsheetPanel panel: panels) {
				tabbedPane.add(panel);
			}
			JPanel p = new JPanel(new BorderLayout());
			p.add(tabbedPane);
			//p.add(BorderLayout.SOUTH, getPasswordPane());
			
			c.add(p);
			
		} catch (SQLException e) {
			
			//p.setText("Failed to connect to MySQL Server.\n"+e);
			c.add(new JLabel("<HTML>Failed to connect to MySQL Server<br>"+e.getMessage()));
		}
		
		
	}

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
		JFrame frame = new JFrame("Tricon Database");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		TriconDb gj = new TriconDb();
		gj.setPreferredSize(new Dimension(1800, 1100));
		gj.setVisible(true);
		gj.init();
		
		frame.getContentPane().add(gj);
		
		frame.pack();
		frame.setVisible(true);
	}

}
