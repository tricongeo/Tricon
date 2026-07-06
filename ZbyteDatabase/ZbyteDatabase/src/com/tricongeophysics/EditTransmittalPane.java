package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class EditTransmittalPane extends ZbyteEditDbRowPane implements RowAddedListener {

	private ZbyteDbSpreadsheetPanel boxSheet;
	private ZbyteDbSpreadsheetPanel supportSheet;
	private ZbyteDbSpreadsheetPanel seismicSheet;
	private JButton printPreviewButton;
	//private JPasswordField passwordField;
	private DbSheetAddBarcodeItemListener boxSheetBarcodeListener;
	private DbSheetAddBarcodeItemDualFieldListener supportSheetBarcodeListener;
	private DbField jobField;
	private DbSheetAddBarcodeItemDualFieldListener seismicSheetBarcodeListener;
	private JTabbedPane tabbedPane;

	public EditTransmittalPane(String tableName, String pkeyName) {
		super(tableName, pkeyName);
		
		tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
		try {
			boxSheet = (ZbyteDbSpreadsheetPanel) BarcodeDbSpreadsheetPanel.makePanel("ZbyteDatabase",  ZbyteDatabase.ControlNum);
			boxSheet.addRowAddedListener(this);
			boxSheetBarcodeListener = new DbSheetAddBarcodeItemListener("id", boxSheet, true);
			((ZbyteDbSpreadsheetPanel)boxSheet).addBarcodeListener(boxSheetBarcodeListener);
			
			supportSheet = (ZbyteDbSpreadsheetPanel) BarcodeDbSpreadsheetPanel.makePanel("ZbyteDatabase", ZbyteDatabase.OriginalHardCopy);
			supportSheet.addRowAddedListener(this);
			supportSheet.setVisibleColumns(new String[]{"id", "Barcode", "Image File", "Control Num:", "Line #", "Orig. Line #", "County", "State",
					"Support Type:", "Media:", "Job:", "Location:", "In Transmittal:", "Data Owner:", "Comment"});
			supportSheetBarcodeListener = new DbSheetAddBarcodeItemDualFieldListener("Barcode", supportSheet, "Job:");
			((ZbyteDbSpreadsheetPanel)supportSheet).addBarcodeListener(supportSheetBarcodeListener);
			supportSheet.setCopier(new SupportFieldCopier());
			
			seismicSheet = (ZbyteDbSpreadsheetPanel) BarcodeDbSpreadsheetPanel.makePanel("ZbyteDatabase", ZbyteDatabase.OriginalMedia);
			seismicSheet.addRowAddedListener(this);
			seismicSheet.setVisibleColumns(new String[]{"id", "Barcode", "Image File", "Control Num:", "Line #", "Orig. Line #", "County", "State",
					"Seis Type:", "Media:", "Media Id", "Job:", "Location:", "In Transmittal:", "Data Owner:", "Comment"});
			seismicSheetBarcodeListener = new DbSheetAddBarcodeItemDualFieldListener("Barcode", seismicSheet, "Job:");
			((ZbyteDbSpreadsheetPanel)seismicSheet).addBarcodeListener(seismicSheetBarcodeListener);
			seismicSheet.setCopier(new SeismicFieldCopier());
			
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e, "Failed to load spreadsheet", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	@Override
	public Component getCenterPane() {
		JPanel centerPane = new JPanel();
		centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.Y_AXIS));
		
		tabbedPane.add(boxSheet);
		tabbedPane.add(supportSheet);
		tabbedPane.add(seismicSheet);
		tabbedPane.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				tabbedPane.getSelectedComponent().requestFocusInWindow();
			}});
		
		BarcodeScannerManager scanManager = new BarcodeScannerManager();
//		tabbedPane.addKeyListener(scanManager);
//		centerPane.addKeyListener(scanManager);
		tabbedPane.getComponent(0).addKeyListener(scanManager);
		
		jobField = this.getField("Job:");
		supportSheetBarcodeListener.setCheckColumnVal(jobField.getValue());
		seismicSheetBarcodeListener.setCheckColumnVal(jobField.getValue());
		jobField.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				supportSheetBarcodeListener.setCheckColumnVal(jobField.getValue());
				seismicSheetBarcodeListener.setCheckColumnVal(jobField.getValue());
				setEditable(true);
				boxSheet.requestFocus();
			}});
		
		tabbedPane.setBorder(BorderFactory.createTitledBorder("Add Items To Transmittal"));
		
		centerPane.add(Box.createVerticalStrut(15));
		centerPane.add(tabbedPane);
		
		//enableSpreadsheets(false);
		//enableFields(false);j
		
		return centerPane;
	}
	
	/**
	 * Let all the sub-spreadsheets know that the row has changed.
	 * We want them to only show rows from the current transmittal (no the entire database)
	 */
	@Override
	public void setRow (int row) {
		super.setRow(row);
		boxSheet.setBaseSearch(getPkey(), "In Transmittal:");
		seismicSheet.setBaseSearch(getPkey(), "In Transmittal:");
		supportSheet.setBaseSearch(getPkey(), "In Transmittal:");
		boxSheet.requestFocus();
	}

	/**
	 * We know that some columns should be defaulted to current transmittal values.
	 * That action happens here.
	 */
	@Override
	public void rowAdded(AbstractSpreadsheetModel model2, int modelRow) {
		Object transmittalId = model.getValueAt(row, "id");
		//Object job = model.getValueAt(row, "Job:");
		NewEditComboBoxField jobComboField = (NewEditComboBoxField)jobField.simpleField;
		int row = jobComboField.getSelectedItemIndex();
		EditDbRowPane jobPane = jobComboField.newEditPane;
		jobPane.loadFields();
		jobPane.setRow(row);
		Object dataOwner = jobPane.getField("Data Owner:").getValue();
		
		model2.setValueAt(transmittalId, modelRow, "In Transmittal:");
		model2.setValueAt(jobField.getValue(), modelRow, "Job:");
		model2.setValueAt(dataOwner, modelRow, "Data Owner:");
	}

	@Override
	public Component getBottomPane() {
		Component p = super.getBottomPane();
		
		printPreviewButton = new JButton("Printer Friendly");
		
		JPanel p2 = new JPanel();
		p2.add(printPreviewButton);
		printPreviewButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showPrintPreview();
			}});
		
		//p2.add(getPasswordPane());
		
		//((JPanel)p).add(printPreviewButton, BorderLayout.WEST);
		((JPanel)p).add(p2,  BorderLayout.WEST);
		return p;
	}

	protected void showPrintPreview() {
		HtmlViewer v = new HtmlViewer();
		String html = "<html>"+getHtml();
		html += boxSheet.getHtml();
		html += supportSheet.getHtml();
		html += seismicSheet.getHtml();
		html += "</html>";
		v.setHtml(html);
		v.show();
	}

//	private Component getPasswordPane() {
//		final DbParms dbParms = DbParms.read("ZbyteDatabase");
//		passwordField = new JPasswordField(10);
//		//passwordField.setPreferredSize(new Dimension(100,30));
//		passwordField.addActionListener(new ActionListener(){
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				if (passwordField.getText().equals(dbParms.adminPWord)) {
//					enableSpreadsheets(true);
//					enableFields(true);
//				} else {
//					enableSpreadsheets(false);
//					enableFields(false);
//				}
//				//EditTransmittalPane.this.transferFocusBackward();
//				//boxSheet.requestFocus();
//				tabbedPane.getSelectedComponent().requestFocus();
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

	protected void enableFields(boolean b) {
		loadFields();
		for (int i=1; i<dbFields.size(); i++) { //ignore first field, which is pkey
			dbFields.get(i).setEditable(b);
		}
	}

	@Override
	public void setEditable(boolean b) {
		if (jobField == null) jobField = this.getField("Job:");
		super.setEditable(b);
		boxSheet.setEditable(b);
		supportSheet.setEditable(b);
		seismicSheet.setEditable(b);
		if (b) {
			Object job = jobField.getValue();
			if (job == null) {
				boxSheet.setEditable(false);
				supportSheet.setEditable(false);
				seismicSheet.setEditable(false);
				tabbedPane.setEnabled(false);
			} else {
				tabbedPane.setEnabled(true);
			}
		}
	}

}
