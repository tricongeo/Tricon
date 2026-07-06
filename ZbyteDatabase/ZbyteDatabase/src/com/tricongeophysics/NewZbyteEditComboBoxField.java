package com.tricongeophysics;

public class NewZbyteEditComboBoxField extends NewEditComboBoxField {

	public NewZbyteEditComboBoxField(String label, Object[] values, Object value) {
		super(label, values, value);
	}
	
	@Override
	protected EditDbRowPane makeNewEditPane(String tableName, String pkeyName) {
		//String tableName = l.getText().replace("<html>", "").replace("</html>", "");
		String tableName1 = tableName.replace(":", "").toLowerCase().replace(" ", "_");
		ZbyteEditDbRowPane p = ZbyteEditDbRowPane.createPane(tableName1);
		return p;
	}

	public NewZbyteEditComboBoxField(String label) {
		this(label, new String[] {""}, "");
	}

	public NewZbyteEditComboBoxField(String colName, String tableName) {
		super(colName, tableName);
	}

	@Override
	void setNewEditPane(String tableName, String pkeyName) {
		setNewEditPane(new ZbyteEditDbRowPane(tableName, pkeyName));
	}
	
//	@Override
//	public void setNewEditPane(EditDbRowPane newEditPane1) {
//		newEditPane = makeNewEditPane();
//		super.setNewEditPane(newEditPane);
////		super.setNewEditPane(newEditPane1);
//	}
}
