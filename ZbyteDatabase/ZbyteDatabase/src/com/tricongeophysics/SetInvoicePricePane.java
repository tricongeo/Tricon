package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SetInvoicePricePane extends AbstractWizardPage implements WizardPage {

	private ZbyteDbSpreadsheetPanel mergeSheet;
	private Object invoiceId;
	private DbParms dbParms;
	private String tableName;
	private String fieldName;

	String[] tableList = new String[] {ZbyteDatabase.ZbyteMedia, ZbyteDatabase.DeliverableMedia, ZbyteDatabase.DeliverableScan, ZbyteDatabase.DeliverableZscan, ZbyteDatabase.DeliverableOther};
	private String[] columnList = new String[] {"id", "Line #", "Job:", "Date", "Type", "Invoice:", "Bill Code:", "Bill Qty", "Total"};
	private String[] mediaColList = new String[] {"id", null, "Job:",  "Creation Date", "Media:", "Invoice:", "Bill Code:", "Bill Qty", "Total"};
	private String[] seismicColList = new String[] {"id", "Line #", "Job:",  "Date Finished", "Seis Type:", "Invoice:", "Bill Code:", "Bill Qty", "Total"};
	private String[] supportColList = new String[] {"id", "Line #", "Job:",  "Date Finished", "Support Type:", "Invoice:", "Bill Code:", "Bill Qty", "Total"};
	private String[] zscanColList = new String[] {"id", "Line #", "Job:",  "Date Finished", "Seis Type:", "Invoice:", "Bill Code:", "Bill Qty", "Total"};
	private String[] otherColList = new String[] {"id", null, "Job:",  "Date Finished", "Other Type:", "Invoice:", "Bill Code:", "Bill Qty", "Total"};
	private ZbyteDbSpreadsheetPanel codeSheet;
	private DbMergeModel3 mergeModel;
	private Object[] billCodes;
	private DatabaseModel codeModel;
	private JButton calcButton;
	private JTextField totalField;
	public SetInvoicePricePane(DbParms dbParms) {
		super();
		this.dbParms = dbParms;
		tableName = dbParms.dbTable;
		fieldName = tableName.replace("_", " ") + ":";
		setLayout(new BorderLayout());
		add(getCenterPane(), BorderLayout.CENTER);
		add(getTopPane(), BorderLayout.NORTH);
		enableSpreadsheets(true);
		//enableFields(true);
		setName("Set "+fieldName);
	}
	
	private Component getTopPane() {
		JPanel p = new JPanel();
		
		JLabel l = new JLabel("Set Billing Rate For Invoice Items");
		
		p.add(l);
		
		return p;
	}

	public Component getCenterPane() {
		JPanel centerPane = new JPanel();
		centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.Y_AXIS));
		

		try {
			dbParms.dbTable = ZbyteDatabase.DeliverableMedia;
			dbParms.query = "select * from " + ZbyteDatabase.DeliverableMedia;
			DbColumnMap dbColumnMap = new DbColumnMap(dbParms, columnList);
			dbColumnMap.addMap(ZbyteDatabase.ZbyteMedia, mediaColList);
			dbColumnMap.addMap(ZbyteDatabase.DeliverableMedia, seismicColList);
			dbColumnMap.addMap(ZbyteDatabase.DeliverableScan, supportColList);
			dbColumnMap.addMap(ZbyteDatabase.DeliverableZscan, zscanColList);
			dbColumnMap.addMap(ZbyteDatabase.DeliverableOther, otherColList);
			
			//merge model 
			mergeModel = new DbMergeModel3(dbColumnMap);
			
			//SelectDbSpreadsheetModelDecorator selectModel = new SelectDbSpreadsheetModelDecorator(mergeModel);
			
			//have to set ZbyteSelectSheet with the merge model
			mergeSheet = new ZbyteDbSpreadsheetPanel(dbParms);
			mergeSheet.setModel(mergeModel);
			mergeSheet.setVisibleColumns(dbColumnMap.getMainColumnNames());
			mergeSheet.setRowButtonsVisible(false);
			mergeSheet.setEditableColumns(new String[] {"Invoice:", "Bill Code:", "Bill Qty", "Total"});
			
			codeModel = DatabaseModel.getDatabaseModel( ZbyteDatabase.BillCode);
			codeSheet =  new ZbyteDbSpreadsheetPanel(dbParms);
			codeSheet.setModel(codeModel);
			codeSheet.setRowButtonsVisible(false);
			
			//tabbedPane.add(mergeSheet);
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e, "Failed to load spreadsheet", JOptionPane.ERROR_MESSAGE);
			return new JLabel("<html>Please fix database problems<br>" +e);
		}
		
		centerPane.add(mergeSheet);
		centerPane.add(codeSheet);
		centerPane.add(getCalculateField());
		
		return centerPane;
	}

	private Component getCalculateField() {
		JPanel p = new JPanel();
		calcButton = new JButton("Calculate");
		calcButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					calculateTotals();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}});
		
		totalField = new JTextField();
		totalField.setEditable(false);
		totalField.setColumns(20);
		
		JLabel l = new JLabel("Invoice Total:");
		
		p.add(calcButton);
		p.add(l);
		p.add(totalField);
		
		return p;
	}

	protected void calculateTotals() throws SQLException {
		if (mergeModel == null) return;
		//loop through invoice items
		int count = mergeModel.getRowCount();
		float grandTotal = 0;
		for (int i=0; i<count; i++) {
			Object code = mergeModel.getValueAt(i, "Bill Code:");
			Object qty = mergeModel.getValueAt(i, "Bill Qty");
			Object rate = codeModel.findRowValue("Bill Code", code, "Suggested Rate");
			double q = SUtil.sval(""+qty);
			double r = SUtil.sval(""+rate);
			double total = r*q;
			mergeModel.setValueAt(total, i, "Total");
			grandTotal += total;
		}
		totalField.setText(grandTotal+"");
		mergeModel.fireTableDataChanged();
	}

	protected void enableSpreadsheets(boolean b) {
		if (mergeSheet != null) mergeSheet.setEditable(b);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public String getId() {
		return getName();
	}

	public Object getInvoiceId() {
		return invoiceId;
	}

	public void refreshSearch() {
		if (mergeSheet != null) mergeSheet.executeSearch();
		loadBillCodes();
		try {
			setBillCodeRatesFromInvoiceTotals();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setBillCodeRatesFromInvoiceTotals() throws SQLException {
		if (mergeModel == null) return;
		if (billCodes == null) return;
		for (Object code: billCodes) {
			int row = mergeModel.findRow("Bill Code:", code);
			if (row < 0) {
				SUtil.printErr("SetInvoicePricePane.setBillCodeRatesFromInvoiceTotal() - Bill Code not Found!! " + code);
				continue;
			}
			Object total = mergeModel.getValueAt(row, "Total");
			Object qty = mergeModel.getValueAt(row, "Bill Qty");
			double t = SUtil.sval(""+total);
			double q = SUtil.sval(""+qty);
			double rate = (q < 1) ? 0 : t/q;
			int row2 = codeModel.findRow("Bill Code", code);
			if (row2 < 0) {
				SUtil.printErr("SetInvoicePricePane.setBillCodeRatesFromInvoiceTotal() - Bill Code not Found!! " + code);
				continue;
			}
			codeModel.setValueAt(rate, row2, "Suggested Rate");
		}
	}

	private void loadBillCodes() {
		if (mergeModel == null) return;
		try {
			billCodes = mergeModel.findCompoundColumnValues("Invoice:", invoiceId, "Bill Code:");
			codeSheet.setSearchValueList("Bill Code", billCodes);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void setDate(Object date) {
		if (date instanceof Date) {
			Date dd = (Date)date;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-*");
			String s = sdf.format(dd);
			if (mergeSheet != null) mergeSheet.setPrimarySearchField("Date");
			if (mergeSheet != null) mergeSheet.setPrimarySearchValue(s);
		}
		
	}

	public void setInvoiceId(Object id) {
		invoiceId = id;
		if (mergeSheet != null) mergeSheet.setBaseSearch(id, "Invoice:");
	}

	public DatabaseModel getModel() {
		return mergeModel;
	}

	
}
