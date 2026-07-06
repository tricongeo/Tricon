package com.tricongeophysics;

import javax.swing.JOptionPane;

public class DbSheetSelectBarcodeListener implements BarcodeListener {

	private String barcodeColumn;
	private ZbyteDbSpreadsheetPanel dbSheet;

	public DbSheetSelectBarcodeListener(String barcodeColumn, ZbyteDbSpreadsheetPanel dbSheet) {
		this.barcodeColumn = barcodeColumn;
		this.dbSheet = dbSheet;
	}

	@Override
	public void barcodeScanned(String barcode) {
		if (!dbSheet.isVisible()) return;
		int row = dbSheet.findRow(barcodeColumn, barcode);
		if (row >= 0) {
			Object v = dbSheet.getValueAt(row, "Select");
			if (v == null) v = "";
			if (v.toString().equals("true")) {
				JOptionPane.showMessageDialog(dbSheet, "<html>Barcode Already Selected!:<br>" + barcode,
						"Select Barcode Warning", JOptionPane.WARNING_MESSAGE, null);
				return;
			}
			dbSheet.setValueAt(row, "Select", true);
		}
		else {
			JOptionPane.showMessageDialog(dbSheet, "<html>Barcode Not Found!:<br>" + barcode,
					"Select Barcode Error", JOptionPane.ERROR_MESSAGE, null);
			return;
		}
	}

}
