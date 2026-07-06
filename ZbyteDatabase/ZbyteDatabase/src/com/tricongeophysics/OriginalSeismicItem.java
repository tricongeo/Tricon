package com.tricongeophysics;

public class OriginalSeismicItem extends OriginalDataItem {
	
	
	private static String[] thisRequiredColumns = {ZbyteDatabase.SeisType, ZbyteDatabase.Format};
	private Object seis_type;
	private Object format;

	@Override
	public String[] getRequiredColumns() {
		String[] moreRequiredColumns = SUtil.arrayCat(RequiredColumns, thisRequiredColumns );
		return moreRequiredColumns;
	}
	
	@Override
	public void setFromModel(AbstractSpreadsheetModel model, int row) {
		super.setFromModel(model, row);
		seis_type = model.getValueAt(row, "Seis Type:");
		format = model.getValueAt(row, "Format:");
	}
	
	@Override
	public void setModelToItem(DatabaseModel model, int row) {
		super.setModelToItem(model, row);
		model.setValueAt(seis_type, row, "Seis Type:");
		model.setValueAt(id, row, ZbyteDatabase.Original_Media);
		model.setValueAt(format, row, "Format:");
	}

	public Object getFormat() {
		return format;
	}

	public void setFormat(Object format) {
		this.format = format;
	}

	public Object getSeis_type() {
		return seis_type;
	}

	public void setSeis_type(Object seis_type) {
		this.seis_type = seis_type;
	}
}
