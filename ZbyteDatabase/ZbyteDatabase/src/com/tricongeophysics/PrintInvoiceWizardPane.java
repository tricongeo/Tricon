package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTable.PrintMode;


public class PrintInvoiceWizardPane extends AbstractWizardPage {


	private DbSpreadsheetPanel summarySheet;
	private DbParms dbParms;
	Object date;
	private HtmlViewer htmlViewer;
	private PrintButton printButton;
	private JSplitPane splitPane;
	private Object invoiceID;

	PrintInvoiceWizardPane(DbParms dbParms) {
		super();
		setName("print");

		this.dbParms = dbParms;

		try {
			summarySheet = new PrintSheet(dbParms);
			summarySheet.setEnabled(false);
			summarySheet.setBorder(BorderFactory.createEtchedBorder());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		setLayout(new BorderLayout());
//		add(getTopPane(), BorderLayout.NORTH);
//		add(summarySheet, BorderLayout.CENTER);
//		add(getBottomPane(), BorderLayout.SOUTH);
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.add(getTopPane());
		splitPane.add(summarySheet);
		
//		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//		add(getTopPane());
//		add(summarySheet);
		add(splitPane, BorderLayout.CENTER);
		add(getBottomPane(), BorderLayout.SOUTH);
	}

	private Component getBottomPane() {
		JPanel p = new JPanel();
		printButton = new PrintButton();
		printButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				print();
			}});
		p.add(printButton);
		return p;
	}

	private Component getTopPane() {
		htmlViewer = new HtmlViewer();
		JScrollPane scroller = new JScrollPane(htmlViewer);
		htmlViewer.setBackground(Color.white);
		htmlViewer.setBorder(BorderFactory.createEmptyBorder());
		return scroller;
	}
	
	public class PrintSheet extends DbSpreadsheetPanel {

		public PrintSheet(DbParms dbParms) throws SQLException {
			super(dbParms);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public Component getRowButtonPane() {
			return new JPanel();
		}
		
		@Override
		public Component getColButtonPane() {
			return new JPanel();
		}
		

	}
	
	protected void print() {
	//	PrintUtility.printComponent(htmlViewer, false);
		//PrintUtility.printComponent(summarySheet.getTable(), false);
		Printable a = PrintUtility.getPrintable(htmlViewer, false);
	
		JTable t = (JTable) summarySheet.getTable();
		//MessageFormat header = new MessageFormat("Z BYTE Transmittal: " + transmittalID);
		MessageFormat footer = new MessageFormat("Z BYTE Invoice "+ invoiceID + "  - Page {0}");
		Printable b = t.getPrintable(PrintMode.FIT_WIDTH, null, footer);
		
		Printable[] printables = new Printable[]{a, b};
		
		PrintUtility mp = new MultiPrinter(printables);
		mp.print();
		
//		try {
//			t.print();
//		} catch (PrinterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
//	public void setTransmittalID(String transmittalID) {
//		this.transmittalID = transmittalID;
//	}

	public void setHtml(String html) {
		htmlViewer.setHtml(html);
		splitPane.setDividerLocation(0.5);
	}

	public SpreadsheetPanel getSheet() {
		return summarySheet;
	}

	public void setInvoiceId(Object id) {
		invoiceID = id;
	}

	public void setModel(DatabaseModel model) {
		if (model == null) return;
		summarySheet.setModel(model);
		summarySheet.setVisibleColumns(new String[] {"id", "Line #", "Type", "Bill Code:", "Bill Qty", "Total"});
	}
}
