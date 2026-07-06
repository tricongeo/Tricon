package com.tricongeophysics;

public class DbSheetAddBarcodeItemDualFieldListener implements BarcodeListener {

	private String barcodeColumn;
	private ZbyteDbSpreadsheetPanel dbSheet;
	private String checkColumnName;
	private Object checkColumnVal;

	public DbSheetAddBarcodeItemDualFieldListener(String barcodeColumn, ZbyteDbSpreadsheetPanel dbSheet, String checkColumnName) {
		this.barcodeColumn = barcodeColumn;
		this.dbSheet = dbSheet;
		this.checkColumnName = checkColumnName;
	}

	@Override
	public void barcodeScanned(String barcode) {
		if (!dbSheet.isVisible()) return;
		int row = dbSheet.findRow(barcodeColumn, barcode, checkColumnName, checkColumnVal);
		if (row >= 0) {
			dbSheet.editRow(row);
		}
		else {
			dbSheet.addRowCheckCopy(0);
			int r = dbSheet.getRowCount();
			dbSheet.setValueAt(r-1, barcodeColumn, barcode);
			dbSheet.setValueAt(r-1, checkColumnName, checkColumnVal);
			dbSheet.editRow(r-1);
		}
	}

	public void setCheckColumnVal(Object value) {
		checkColumnVal = value;
	}

}
