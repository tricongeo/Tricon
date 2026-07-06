package com.tricongeophysics;

public class ControlNumberTransmittalItem extends TransmittalItem {

	@Override
	public void setFromModel(AbstractSpreadsheetModel model, int row) {
		super.setFromModel(model, row);
		barcode = model.getValueAt(row, "id"); 
		media = "Box";
		media_id = model.getValueAt(row, "Client Box#"); 
		box = barcode;
	}
}
