package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;

public class StartTransmittalPane extends AbstractWizardPage implements WizardPage {

	private ZbyteDbSpreadsheetPanel boxSheet;
	private ZbyteDbSpreadsheetPanel supportSheet;
	private ZbyteDbSpreadsheetPanel seismicSheet;
	private ZbyteDbSpreadsheetPanel delivSheet;
	private Object transmittalId;
	//private DatabaseModel model;
	private DbParms dbParms;
	private EditDbRowPane editDbRowpane;
	private String tableName;
	private String fieldName;

	public StartTransmittalPane(DbParms dbParms) {
		super();
		this.dbParms = dbParms;
		tableName = dbParms.dbTable;
		fieldName = tableName.replace("_", " ") + ":";
		//JPanel p = new JPanel(new BorderLayout());
		setLayout(new BorderLayout());
//		p.add(getCenterPane(), BorderLayout.CENTER);
//		p.add(getTopPane(), BorderLayout.NORTH);
//		p.add(getBottomPane(), BorderLayout.SOUTH);
	//	add(p);
		add(getCenterPane(), BorderLayout.CENTER);
		add(getTopPane(), BorderLayout.NORTH);
		enableSpreadsheets(false);
		enableFields(false);
		setName("Start "+fieldName);
	}
	
	private Component getTopPane() {
		editDbRowpane = new ZbyteEditDbRowPane(tableName, "id");
		editDbRowpane.initialize();
		return editDbRowpane.getTopPane();
	}

	public Component getCenterPane() {
		JPanel centerPane = new JPanel();
		centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.Y_AXIS));
		
//		try {
//			dbParms.dbTable = tableName;
//			dbParms.query = "select * from " + tableName;
//			//model = DatabaseModel.getDatabaseModel(dbParms);
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		// transmittalId = model .getValueAt(row, "id");
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
		try {
			boxSheet = (ZbyteDbSpreadsheetPanel) ZbyteDbSpreadsheetPanel.makePanel("ZbyteDatabase",  ZbyteDatabase.ControlNum);
//			boxSheet.addRowAddedListener(this);
//			boxSheet.setPrimarySearchField("Out Transmittal:");
//			boxSheet.setPrimarySearchValue(transmittalId);
			//boxSheet.setHideColumns(new String[] {"Out Transmittal:", "Recycle Date"});
			boxSheet.setVisibleColumns(new String[]{"id", "Client Box#", "Client Item Count", "Actual Item Count", fieldName, "Comment",  "Doc. File", "Location:", "Job:"});
			tabbedPane.add(boxSheet);
			
			supportSheet = (ZbyteDbSpreadsheetPanel) ZbyteDbSpreadsheetPanel.makePanel("ZbyteDatabase", ZbyteDatabase.OriginalHardCopy);
			//supportSheet = BarcodeDbSpreadsheetPanel.makePanel("ZbyteDatabase", ZbyteDatabase.OriginalHardCopy);
//			supportSheet.addRowAddedListener(this);
//			supportSheet.setPrimarySearchField("Out Transmittal:");
//			supportSheet.setPrimarySearchValue(transmittalId);
			//supportSheet.setHideColumns(new String[] {"Out Transmittal:", "Copy Date", "File Path", "Operator:"});
			supportSheet.setVisibleColumns(new String[]{"id", "Barcode",  "Image File", "Control Num:", "Line #", "State", "County", "Support Type:", "Media:", fieldName, "Comment",  "Doc. File", "Job:"});
			tabbedPane.add(supportSheet);
			
			
			seismicSheet = (ZbyteDbSpreadsheetPanel) ZbyteDbSpreadsheetPanel.makePanel("ZbyteDatabase", ZbyteDatabase.OriginalMedia);
			//seismicSheet = BarcodeDbSpreadsheetPanel.makePanel("ZbyteDatabase", ZbyteDatabase.OriginalMedia);
//			seismicSheet.addRowAddedListener(this);
//			seismicSheet.setPrimarySearchField("Out Transmittal:");
//			supportSheet.setPrimarySearchValue(transmittalId);
			//seismicSheet.setHideColumns(new String[] {"Status", "FFID Range", "SP Range", "Copy Date", "File Path", "Operator:"});
			seismicSheet.setVisibleColumns(new String[]{"id", "Barcode",  "Image File", "Control Num:", "Line #", "State", "County", "Seis Type:", "Media:", "Media Id", fieldName, "Comment",  "Doc. File", "Job:"});
			tabbedPane.add(seismicSheet);
			
			
			delivSheet = (ZbyteDbSpreadsheetPanel) ZbyteDbSpreadsheetPanel.makePanel("ZbyteDatabase", ZbyteDatabase.ZbyteMedia);
			//delivSheet = BarcodeDbSpreadsheetPanel.makePanel("ZbyteDatabase", "deliverable_media");
//			delivSheet.addRowAddedListener(this);
//			delivSheet.setPrimarySearchField("Out Transmittal:");
//			delivSheet.setPrimarySearchValue(transmittalId);
			//supportSheet.setHideColumns(new String[] {"Out Transmittal:", "Copy Date", "File Path", "Operator:"});
			tabbedPane.add(delivSheet);
			
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e, "Failed to load spreadsheet", JOptionPane.ERROR_MESSAGE);
		}
		
		tabbedPane.setBorder(BorderFactory.createTitledBorder("Add Items To Transmittal"));
		
		centerPane.add(Box.createVerticalStrut(15));
		centerPane.add(tabbedPane);
		
		return centerPane;
	}

	protected void enableFields(boolean b) {
//		for (int i=1; i<dbFields.size(); i++) { //ignore first field, which is pkey
//			dbFields.get(i).setEditable(b);
//		}
	}

	protected void enableSpreadsheets(boolean b) {
		boxSheet.setEditable(b);
		supportSheet.setEditable(b);
		seismicSheet.setEditable(b);
		delivSheet.setEditable(b);
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
		editDbRowpane.setRow(row);
		transmittalId = editDbRowpane.getPkey();
		boxSheet.setBaseSearch(transmittalId, fieldName);
		supportSheet.setBaseSearch(transmittalId, fieldName);
		seismicSheet.setBaseSearch(transmittalId, fieldName);
		delivSheet.setBaseSearch(transmittalId, fieldName);
	}

	public Object getJobNum() {
		DbField f = editDbRowpane.getField("Job:");
		return f.getValue();
	}

	public void clickedOK() {
		editDbRowpane.clickedOK();
	}

	public String getTransmittalId() {
		return transmittalId.toString();
	}

	public void refreshSearch() {
		boxSheet.executeSearch();
		supportSheet.executeSearch();
		seismicSheet.executeSearch();
		delivSheet.executeSearch();
	}

	public ArrayList<TableData> loadTransmittalItems(TransmittalTransferItemFactory stif) {
		ArrayList<TableData> items = new ArrayList<TableData>();
		
		ArrayList<TableData> list = boxSheet.loadModelTransferItems(stif);
		for (TableData td: list) {
			items.add(td);
		}
		list = supportSheet.loadModelTransferItems(stif);
		for (TableData td: list) {
			items.add(td);
		}
		list = seismicSheet.loadModelTransferItems(stif);
		for (TableData td: list) {
			items.add(td);
		}
		list = delivSheet.loadModelTransferItems(stif);
		for (TableData td: list) {
			items.add(td);
		}
		
		return items;
	}

	public String getHtml() {
		return editDbRowpane.getHtml();
	}

	public EditDbRowPane getEditDbRowPane() {
		return editDbRowpane;
	}

}
