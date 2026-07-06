package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class StartInvoicePane extends AbstractWizardPage implements WizardPage {

	private DbSpreadsheetPanel mergeSheet;
	private Object invoiceId;
	//private DatabaseModel model;
	private DbParms dbParms;
	private EditDbRowPane editDbRowpane;
	private String tableName;
	private String fieldName;
	private DbField clientField;
	private DbField contactField;
	//private ComboBoxField jobField;
	private DateField fromDateField;
	private DateField toDateField;
	String[] tableList = new String[] {ZbyteDatabase.ZbyteMedia, ZbyteDatabase.DeliverableMedia, ZbyteDatabase.DeliverableScan, ZbyteDatabase.DeliverableZscan, ZbyteDatabase.DeliverableOther};
	private String[] columnList = new String[] {"id", "Line #", "Job:", "Date", "Type", "Invoice:", "Bill Code:", "Bill Qty", "Total"};
	private String[] mediaColList = new String[] {"id", null, "Job:",  "Creation Date", "Media:", "Invoice:", "Media:", null, null};
	private String[] seismicColList = new String[] {"id", "Line #", "Job:",  "Date Finished", "Seis Type:", "Invoice:", "Bill Code:", "Bill QTY", null};
	private String[] supportColList = new String[] {"id", "Line #", "Job:",  "Date Finished", "Support Type:", "Invoice:", "Bill Code:", "Bill QTY", null};
	private String[] zscanColList = new String[] {"id", "Line #", "Job:",  "Date Finished", "Seis Type:", "Invoice:", "Bill Code:", "Bill QTY", null};
	private String[] otherColList = new String[] {"id", null, "Job:",  "Date Finished", "Other Type:", "Invoice:", "Bill Code:", "Bill QTY", null};
	private DbField dateField;

	public StartInvoicePane(DbParms dbParms) {
		super();
		this.dbParms = dbParms;
		tableName = dbParms.dbTable;
		fieldName = "Invoice:";//tableName.replace("_", " ") + ":";
		setLayout(new BorderLayout());
		add(getCenterPane(), BorderLayout.CENTER);
		add(getTopPane(), BorderLayout.NORTH);
		enableSpreadsheets(false);
		enableFields(false);
		setName("Start "+fieldName);
		//initializeDate();
	}
	
	private void initializeDate() {
		dateField = editDbRowpane.getField("Invoice Date");
		dateField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				initializeDateRange();
			}});
		Object date = dateField.getValue();
		if (date == null || date.equals("")) {
			Date today = SUtil.getTodaysDate();
			dateField.setValue(today);
		}
	}

	@SuppressWarnings("deprecation")
	protected void initializeDateRange() {
		Date from = (Date) fromDateField.getValue();
		Date to = (Date) toDateField.getValue();
		if (from != null && to != null) return;
		Date ivDate = (Date) dateField.getValue();
		if (ivDate == null) return;
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(ivDate);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			Date beg = cal.getTime();
			fromDateField.setValue(beg);
			cal.set(Calendar.DAY_OF_MONTH,  cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			Date end = cal.getTime();
			toDateField.setValue(end);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Component getTopPane() {
		editDbRowpane = new ZbyteEditDbRowPane(tableName, "id");
		if (editDbRowpane == null) return new JLabel("failed to load table: " + tableName);
		editDbRowpane.initialize();
		
		JPanel topPane = new JPanel();
		
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.Y_AXIS));
		topPane.add(editDbRowpane.getTopPane());
		topPane.add(Box.createVerticalStrut(10));
		topPane.add(getSelectPane());
		
		//initializeDate();
		
		return topPane;
	}

	private Component getSelectPane() {
		clientField = editDbRowpane.getField("Client:");
		clientField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setPossibleContacts();
				//setJobs();
			}});
		contactField = editDbRowpane.getField("Contact:");
		
		//jobField = new ComboBoxField("Job:", new String[]{"All"}, null);
		fromDateField = new DateField("From Date:", null);
		toDateField = new DateField("To Date:", null);
		
		JPanel p = new JPanel();
		//p.add(jobField);
		p.add(fromDateField);
		p.add(toDateField);
		
		p.setBorder(BorderFactory.createTitledBorder("Invoice Parameters"));
		
		return p;
	}

//	protected void setJobs() {
//		Object client = clientField.getValue();
//		if (client == null) return;
//		try {
//			dbParms.dbTable = ZbyteDatabase.Job;
//			dbParms.query = "select * from job";
//			DatabaseModel jobModel = DatabaseModel.getDatabaseModel(dbParms);
////			ResultSet rs = jobModel.executeQuery("Bill To:", client.toString());
////			Object[] jobs = new ArrayList<String>();
////			jobs.add("All");
////			while(rs.next()) {
////				jobs.add(rs.getString("Job#"));
////			}
//			Object[] jobs = jobModel.findCompoundColumnValues("Bill To:", client, "Job#");
//			jobField.setValues(jobs);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	protected void setPossibleContacts() {
		Object client = clientField.getValue();
		if (client == null) return;
		try {
			dbParms.dbTable =  ZbyteDatabase.Contact;
			dbParms.query = "select * from contact";
			DatabaseModel contactModel = DatabaseModel.getDatabaseModel(dbParms);
//			ResultSet rs = contactModel.executeQuery("Client:", client.toString());
//			ArrayList<String> contacts = new ArrayList<String>();
//			while(rs.next()) {
//				contacts.add(rs.getString("Alias"));
//			}
			Object[] contacts = contactModel.findCompoundColumnValues("Client:", client, "Alias");
			((ComboBoxField)contactField.simpleField).setValues(contacts);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Component getCenterPane() {
		JPanel centerPane = new JPanel();
		centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.Y_AXIS));
		
//		try {
//			dbParms.dbTable = tableName;
//			dbParms.query = "select * from " + tableName;
//			model = DatabaseModel.getDatabaseModel(dbParms);
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		try {
			dbParms.dbTable = ZbyteDatabase.DeliverableMedia;
			dbParms.query = "select * from " + ZbyteDatabase.DeliverableMedia;
			DbColumnMap dbColumnMap = new DbColumnMap(dbParms, columnList);
			dbColumnMap.addMap(ZbyteDatabase.ZbyteMedia, mediaColList);
			dbColumnMap.addMap(ZbyteDatabase.DeliverableMedia, seismicColList);
			dbColumnMap.addMap(ZbyteDatabase.DeliverableScan, supportColList);
			dbColumnMap.addMap(ZbyteDatabase.DeliverableZscan, zscanColList);
			dbColumnMap.addMap(ZbyteDatabase.DeliverableOther, otherColList);
			
			mergeSheet = new ZbyteDbSpreadsheetPanel(dbColumnMap.dbParms);
			
			DbMergeModel3 mergeModel = new DbMergeModel3(dbColumnMap);
			
			mergeSheet.setModel(mergeModel);
			mergeSheet.setVisibleColumns(dbColumnMap.getMainColumnNames());
			
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e, "Failed to load spreadsheet", JOptionPane.ERROR_MESSAGE);
			return new JLabel("<html> Please fix database<br>" + e);
		}
		
		centerPane.add(Box.createVerticalStrut(15));
		centerPane.add(mergeSheet);
		
		return centerPane;
	}

	protected void enableFields(boolean b) {
//		for (int i=1; i<dbFields.size(); i++) { //ignore first field, which is pkey
//			dbFields.get(i).setEditable(b);
//		}
	}

	protected void enableSpreadsheets(boolean b) {
		if (mergeSheet == null) return;
		mergeSheet.setEditable(b);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public String getId() {
		return getName();
	}

	public void setRow(int row) {
		if (mergeSheet == null) return;
		if (editDbRowpane == null) return;
		editDbRowpane.setRow(row);
		invoiceId = editDbRowpane.getPkey();
		mergeSheet.setBaseSearch(invoiceId, fieldName);
		mergeSheet.modelChanged();
		initializeDate();
		this.initializeDateRange();
	}

	public Object getClient() {
		if (editDbRowpane == null) return null;
		DbField f = editDbRowpane.getField("Client:");
		return f.getValue();
	}

	public void clickedOK() {
		if (editDbRowpane == null) return;
		editDbRowpane.clickedOK();
	}

	public Object getInvoiceId() {
		return invoiceId;
	}

	public void refreshSearch() {
		if (mergeSheet == null) return;
		mergeSheet.executeSearch();
	}

	public String getHtml() {
		if (editDbRowpane == null) return null;
		return editDbRowpane.getHtml();
	}

	public EditDbRowPane getEditDbRowPane() {
		return editDbRowpane;
	}

	public Object getDate() {
		if (dateField == null) return null;
		return dateField.getValue();
	}

}
