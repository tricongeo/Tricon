package com.tricongeophysics;

public class OriginalZscanItem extends OriginalDataItem {
	
	
	private static String[] thisRequiredColumns = {"county", "state"};
	private Object county;
	private Object state;
	

	public Object getCounty() {
		return county;
	}

	public void setCounty(Object county) {
		this.county = county;
	}

	public Object getState() {
		return state;
	}

	public void setState(Object state) {
		this.state = state;
	}

	@Override
	public String[] getRequiredColumns() {
		String[] moreRequiredColumns = SUtil.arrayCat(RequiredColumns, thisRequiredColumns );
		return moreRequiredColumns;
	}
	
	@Override
	public void setFromModel(AbstractSpreadsheetModel model, int row) {
		super.setFromModel(model, row);
		county = model.getValueAt(row, "County");
		state = model.getValueAt(row, "State");
	}
	
	@Override
	public void setModelToItem(DatabaseModel model, int row) {
		super.setModelToItem(model, row);
		model.setValueAt(county, row, "County");
		model.setValueAt(state, row, "State");
		model.setValueAt(id, row, ZbyteDatabase.Original_HardCopy);
	}
}
