package com.tricongeophysics;

public class SupportTransmittalItem extends TransmittalItem {

	@Override
	public void setFromModel(AbstractSpreadsheetModel model, int row) {
		super.setFromModel(model, row);
		line_number = model.getValueAt(row, "Line #"); 
		state = model.getValueAt(row, "State"); 
		county = model.getValueAt(row, "County"); 
		barcode = model.getValueAt(row, "Barcode"); 
		media =  model.getValueAt(row, "Media:"); 
		//media_id =  model.getValueAt(row, "Media Id"); 
		type = model.getValueAt(row, "Support Type:"); 
		box = model.getValueAt(row, "Control Num:"); 
	}
}
