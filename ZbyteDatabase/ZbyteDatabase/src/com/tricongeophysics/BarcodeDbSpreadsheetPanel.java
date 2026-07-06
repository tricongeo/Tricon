package com.tricongeophysics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class BarcodeDbSpreadsheetPanel extends ZbyteDbSpreadsheetPanel {

	private BrowseButton browseButton;

	public BarcodeDbSpreadsheetPanel(DbParms dbParms) throws SQLException {
		super(dbParms);
	}
	
	public static DbSpreadsheetPanel makePanel(String appName, String tableName) throws SQLException {
		DbParms p = DbParms.read(appName);
		p.dbTable = tableName;
		p.query = "select * from " + tableName;
		p.pkeyIndex = 0;
		DbSpreadsheetPanel panel = new BarcodeDbSpreadsheetPanel(p);
		panel.setName(tableName);
		return panel;
	}
	
	@Override
	protected void addRowPaneButtons(JPanel panel) {
		super.addRowPaneButtons(panel);
		panel.add(getBrowseButton());
	}
	
	protected Component getBrowseButton() {
		browseButton = new BrowseButton();
		browseButton.setToolTipText("import rows from file/directory");
		browseButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
//				if (table.getRowCount() < 1) return;
//				int row = table.getSelectedRow();
//				if (row < 0) return;
//				int modelRow = table.convertRowIndexToModel(row);
//				editRow(modelRow);
				File[] dirs = TriconFileChooser.launchDirsChooser("", BarcodeDbSpreadsheetPanel.this, "Import Barcode Directory(s)");
				if (dirs == null) return;
				for (File dir: dirs)
					addRowsFromDir(dir);
			}}
		);
		return browseButton;
	}

	protected void addRowsFromDir(File dir) {
		if (dir == null) return;
		if (!dir.isDirectory()) return;
		String[] files = dir.list();
		String box = dir.getName();
		ArrayList<String> barcodes = new ArrayList<String>();
		ArrayList<String> files2 = new ArrayList<String>();
		for (String file: files) {
			File f = new File(dir.getAbsolutePath() + File.separator + file);
			if (TriconFile.isImage(file)) {
				String barcode = TriconFile.removeSuffix(file);
				barcodes.add(barcode);
				files2.add(f.getAbsolutePath());
			}
		}
		
		String first = "";
		String last = "";
		if (barcodes.size() > 0) {
			first = barcodes.get(0);
			last =  barcodes.get(barcodes.size() - 1);
		}
		String message = "<html>Add \"" + barcodes.size() + "\"" +
		" barcodes <br>from box \"" + box + "\" ?<br><br>Barcode Range:<br>\"" + 
		first + "\" - \"" + last + "\"";
		int answer = JOptionPane.showConfirmDialog(this, message, "Barcode Import", JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			for (int i=0; i< barcodes.size(); i++) {
				String barcode = barcodes.get(i);
				if (checkBarcodeBox(barcode, box)) {
					model.addRow(1);
					int r = model.getRowCount();
					model.setValueAt(barcode, r-1, "Barcode");
					model.setValueAt(box, r-1, "Control Num:");
					model.setValueAt(files2.get(i), r-1, "Image File");
					fireRowAdded(model, r-1);
				}
			}
		}
	}
	
	@Override
	public void setRowButtonsEnabled(boolean enabled) {
		super.setRowButtonsEnabled(enabled);
		if (browseButton == null) return;
		browseButton.setEnabled(enabled);
	}

	/**
	 * todo
	 * needs sql injection protection!!!!!!!!!!!
	 * 
	 * 
	 * @param barcode
	 * @param box
	 * @return
	 */
	private boolean checkBarcodeBox(String barcode, String box) {
		try {
			int count = ((DatabaseModel)model).getCompoundRowCount("Barcode", barcode, "Control Num:", box);
			if (count > 0) {
				int ans = JOptionPane.showConfirmDialog(this, "<html>Warning:<br>Tape with barcode \"" + barcode + "\"<br>" +
						"and box \"" + box + "\"<br>" +
						"already exists!!<br><br>" +
						"Are you sure you want to add it again?", "Duplicate Item", JOptionPane.YES_NO_OPTION);
				if (ans == JOptionPane.YES_OPTION) {
					return true;
				} else {
					return false;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}
