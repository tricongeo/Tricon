package com.tricongeophysics;

public class OriginalSupportItem extends OriginalDataItem {
	
	
	private static String[] thisRequiredColumns = {ZbyteDatabase.SupportType};
	private Object support_type;

	@Override
	public String[] getRequiredColumns() {
		String[] moreRequiredColumns = SUtil.arrayCat(RequiredColumns, thisRequiredColumns );
		return moreRequiredColumns;
	}
	
	@Override
	public void setFromModel(AbstractSpreadsheetModel model, int row) {
		super.setFromModel(model, row);
		support_type = model.getValueAt(row, "Support Type:");
	}
	
	@Override
	public void setModelToItem(DatabaseModel model, int row) {
		super.setModelToItem(model, row);
		model.setValueAt(support_type, row, "Support Type:");
		model.setValueAt(id, row, ZbyteDatabase.Original_HardCopy);
	}

	public Object getSupport_type() {
		return support_type;
	}

	public void setSupport_type(Object support_type) {
		this.support_type = support_type;
	}
}
