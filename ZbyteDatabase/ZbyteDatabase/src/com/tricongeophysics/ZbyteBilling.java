package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.sql.SQLException;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ZbyteBilling extends JApplet 
{
	@Override
	public void init() {
		Container c = this.getContentPane();
		try {
			ZbyteDbSpreadsheetPanel invoicePanel = (ZbyteDbSpreadsheetPanel) ZbyteDbSpreadsheetPanel.makePanel("ZbyteDatabase", "invoice");
			invoicePanel.setEditableColumns(new String[]{""});
			//out_transmittalPanel.passwordCorrect(false);
			
			JPanel transPane = new JPanel();
			//transPane.setLayout(new BoxLayout(transPane, BoxLayout.Y_AXIS));
			transPane.setLayout(new BorderLayout());
			transPane.setName("Invoice");
			transPane.add(invoicePanel);
			//transPane.add(out_transmittalPanel.getPasswordPane(), BorderLayout.SOUTH);
			
			JTabbedPane tabbedPane = new JTabbedPane();
		
			tabbedPane.add(transPane);
			
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
		JFrame frame = new JFrame("Billing");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		ZbyteBilling gj = new ZbyteBilling();
		gj.setPreferredSize(new Dimension(1000, 500));
		gj.setVisible(true);
		gj.init();
		
		frame.getContentPane().add(gj);
		
		frame.pack();
		frame.setVisible(true);
	}

}
