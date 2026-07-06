package com.tricongeophysics;

import java.sql.SQLException;

import javax.swing.JOptionPane;

public class DbSheetSelectEditBarcodeItemListener implements BarcodeListener {

	private String barcodeColumn;
	private ZbyteDbSpreadsheetPanel dbSheet;

	public DbSheetSelectEditBarcodeItemListener(String barcodeColumn, ZbyteDbSpreadsheetPanel dbSheet) {
		this.barcodeColumn = barcodeColumn;
		this.dbSheet = dbSheet;
	}

	@Override
	public void barcodeScanned(String barcode) {
		if (!dbSheet.isVisible()) return;
		int row = dbSheet.findRow(barcodeColumn, barcode);
		if (row >= 0) {
			dbSheet.setValueAt(row, "Select", true);
			dbSheet.editRow(row);
		}
		else {
			JOptionPane.showMessageDialog(dbSheet, "<html>Barcode Not Found!:<br>" + barcode,
					"Select Barcode Error", JOptionPane.ERROR_MESSAGE, null);
			return;

		}
	}

}
