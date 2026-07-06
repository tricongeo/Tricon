package com.tricongeophysics;

public class DeliverableMediaTransmittalItem extends TransmittalItem {

	@Override
	public void setFromModel(AbstractSpreadsheetModel model, int row) {
		super.setFromModel(model, row);
		barcode = model.getValueAt(row, "id"); 
		media =  model.getValueAt(row, "Media:"); 
		type = model.getValueAt(row, "Description"); 
		//box = model.getValueAt(row, "Control Num:"); 
		box = "";
	}
}
