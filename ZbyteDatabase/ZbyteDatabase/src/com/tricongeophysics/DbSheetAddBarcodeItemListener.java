package com.tricongeophysics;

import java.sql.SQLException;

import javax.swing.JOptionPane;

public class DbSheetAddBarcodeItemListener implements BarcodeListener {

	private String barcodeColumn;
	private ZbyteDbSpreadsheetPanel dbSheet;
	private boolean barcodeIsPkey;

	public DbSheetAddBarcodeItemListener(String barcodeColumn, ZbyteDbSpreadsheetPanel dbSheet, boolean barcodeIsPkey) {
		this.barcodeColumn = barcodeColumn;
		this.dbSheet = dbSheet;
		this.barcodeIsPkey = barcodeIsPkey;
	}

	@Override
	public void barcodeScanned(String barcode) {
		if (!dbSheet.isVisible()) return;
		int row = dbSheet.findRow(barcodeColumn, barcode);
		if (row >= 0) {
			dbSheet.editRow(row);
		}
		else {
			if (barcodeIsPkey) {
				try {
					boolean exists = dbSheet.existsInDatabase(barcodeColumn, barcode);
					if (exists) {
						JOptionPane.showMessageDialog(dbSheet, "<html>Can\'t add barcode:<br>" + barcode + "<br> It already exists in table:<br>" +
								dbSheet.getName(), "Add Barcode Error", JOptionPane.ERROR_MESSAGE, null);
						return;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			dbSheet.addRowCheckCopy(0);
			int r = dbSheet.getRowCount();
			dbSheet.setValueAt(r-1, barcodeColumn, barcode);
			dbSheet.editRow(r-1);
		}
	}

}
