package com.tricongeophysics;

public abstract class AbstractDbRowCopier implements DbRowCopier {
	
	@Override
	public void copyFromTo(int fromRow, int toRow, DatabaseModel model) {
		String[] fieldNames = getColumnNamesToCopy();
		for (String fieldName: fieldNames) {
			Object val = model.getValueAt(fromRow, fieldName);
			model.setValueAt(val, toRow, fieldName);
		}
	}

	public abstract String[] getColumnNamesToCopy();

}
