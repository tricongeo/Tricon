package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SelectInvoicePane extends AbstractWizardPage implements WizardPage {

	private ZbyteSelectSheet mergeSheet;
	private Object invoiceId;
	//private DatabaseModel model;
	private DbParms dbParms;
//	private EditDbRowPane editDbRowpane;
	private String tableName;
	private String fieldName;
	String[] tableList = new String[] {ZbyteDatabase.ZbyteMedia, ZbyteDatabase.DeliverableMedia, ZbyteDatabase.DeliverableScan, ZbyteDatabase.DeliverableZscan, ZbyteDatabase.DeliverableOther};
	private String[] columnList = new String[] {"id", "Line #", "Job:", "Date", "Type", "Invoice:", "Bill Code:", "Bill Qty", "Total"};
	private String[] mediaColList = new String[] {"id", null, "Job:",  "Creation Date", "Media:", "Invoice:", "Media:", null, null};
	private String[] seismicColList = new String[] {"id", "Line #", "Job:",  "Date Finished", "Seis Type:", "Invoice:", "Bill Code:", "Bill QTY", null};
	private String[] supportColList = new String[] {"id", "Line #", "Job:",  "Date Finished", "Support Type:", "Invoice:", "Bill Code:", "Bill QTY", null};
	private String[] zscanColList = new String[] {"id", "Line #", "Job:",  "Date Finished", "Seis Type:", "Invoice:", "Bill Code:", "Bill QTY", null};
	private String[] otherColList = new String[] {"id", null, "Job:",  "Date Finished", "Other Type:", "Invoice:", "Bill Code:", "Bill QTY", null};
	private Object client;
	public SelectInvoicePane(DbParms dbParms) {
		super();
		this.dbParms = dbParms;
		tableName = dbParms.dbTable;
		fieldName = tableName.replace("_", " ") + ":";
		setLayout(new BorderLayout());
		add(getCenterPane(), BorderLayout.CENTER);
		add(getTopPane(), BorderLayout.NORTH);
		enableSpreadsheets(false);
		setName("Start "+fieldName);
	}

	private Component getTopPane() {
		JPanel p = new JPanel();
		
		JLabel l = new JLabel("Select Items For This Invoice");
		
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
			DbMergeModel3 mergeModel = new DbMergeModel3(dbColumnMap);
			
			SelectDbSpreadsheetModelDecorator selectModel = new SelectDbSpreadsheetModelDecorator(mergeModel);
			
			//have to set ZbyteSelectSheet with the merge model
			mergeSheet = new ZbyteSelectSheet(dbParms);
			mergeSheet.setModel(selectModel);
			mergeSheet.setVisibleColumns(SUtil.arrayCat(new String[]{"Select"}, dbColumnMap.getMainColumnNames()));
			
			//tabbedPane.add(mergeSheet);
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e, "Failed to load spreadsheet", JOptionPane.ERROR_MESSAGE);
			return new JLabel("<html>Please fix database problems<br>" +e);
		}
	
		centerPane.add(mergeSheet);
		
		return centerPane;
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
	}

	public void setClient(Object client) {
		this.client = client;
		String clientid = getClientID(client);
		if (mergeSheet != null) mergeSheet.setBaseSearch("*-"+clientid+"-*", "Job:");
	}

	private String getClientID(Object client2) {
		String s = client2+"";
		int index = s.lastIndexOf("- ");
		if (index < 0) return ""+0;
		String id = s.substring(index).replace("- ", "");
		return id;
	}

	public void selectAll(boolean select) {
		if (mergeSheet != null) mergeSheet.selectAll(select);
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
	}

	public void saveId(Object id) {
		if (mergeSheet != null) mergeSheet.setSelectedItems("Invoice:", id);
	}

}
