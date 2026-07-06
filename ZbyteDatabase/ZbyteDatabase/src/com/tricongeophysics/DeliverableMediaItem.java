package com.tricongeophysics;

import com.tricongeophysics.TableData.ColumnType;

public class DeliverableMediaItem extends AbstractTableData implements ModelTransferItem {
	
	public static final String[] RequiredColumns = { "Barcode"};
	Object barcode;
	
	public Object getBarcode() {
		return barcode;
	}

	public void setBarcode(Object barcode) {
		this.barcode = barcode;
	}
	
	@Override
	public boolean containsError() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsWarning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addRowColorChangedListener(RowColorChangedListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(TableData o) {
		return this.toString().compareTo(o.toString());
	}

	@Override
	public void setFromModel(AbstractSpreadsheetModel model, int row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setModelToItem(DatabaseModel model, int row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getRequiredColumns() {
		return RequiredColumns;
	}

	@Override
	public boolean[] getEditableCols() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ColumnType[] getColumnTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
