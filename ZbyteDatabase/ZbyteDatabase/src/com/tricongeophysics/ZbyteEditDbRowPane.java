package com.tricongeophysics;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.tricongeophysics.DbSecurityManager.AccessLevel;

public class ZbyteEditDbRowPane extends EditDbRowPane {

	private boolean editable = true;;

	public ZbyteEditDbRowPane(String tableName, String pkeyName) {
		super(tableName, pkeyName);
		// TODO Auto-generated constructor stub
	}

	protected void loadFields() {
		if (dbFields.size() > 1) return;
		//dbFields = new ArrayList<DbField>();
		for (int i=1; i<colNames.length; i++) {
			DbField dbf = new ZbyteDbField(model, row, colNames[i]);
			addClickedOKListener(dbf);
			dbFields.add(dbf);
		}
		
	}

	public static ZbyteEditDbRowPane createPane(String tableName) {
		ZbyteEditDbRowPane p = null;
		if (tableName.equals(ZbyteDatabase.WorkOrder)) {
			p = new EditTransmittalPane(ZbyteDatabase.WorkOrder, null);
		} 
		else if (tableName.equals(ZbyteDatabase.Job)) {
			p = new EditJobPane(ZbyteDatabase.Job, null);
		}
		else if (tableName.equals( ZbyteDatabase.Client)) {
			p = new EditClientPane( ZbyteDatabase.Client, null);
		}
		else if (tableName.equals( ZbyteDatabase.Contact)) {
			p = new EditContactPane( ZbyteDatabase.Contact, null);
		}
		else if (tableName.equals(ZbyteDatabase.OutTransmittal)) {
			p = new EditOutTransmittalPane();
		}
		else if (tableName.equals(ZbyteDatabase.ZbyteTransmittal)) {
			p = new EditZbyteTransmittalPane();
		}
		else if (tableName.equals("invoice")) {
			p = new EditInvoicePane();
		}
		else {
			p = new ZbyteEditDbRowPane(tableName, null);
		}
		return p;
	}
	
	
	private boolean checkSecurity() {
		DbSecurityManager manager = DbSecurityManager.getManager();
		String category = ZbyteDbAccessTable.getAccessTable().getAccessCategory(model.getTableName());
		AccessLevel level;
		if (category == null) {
			SUtil.print("Model: " + model.getTableName() + " has no access category");
			level = AccessLevel.ReadWrite;
		} else {
			level = manager.getAccessLevel(category);
		}
		if (level == AccessLevel.ReadWrite && editable) {
			setEditable(true);
		} else { //Read-Only case
			if (dbFields.get(0).isEditable()) { //this is pkeyField. if editable, then we're making new row
				JOptionPane.showMessageDialog(this, "<html>You Don't Have Write Access To:<br>" +
						"Table: " + model.getTableName() +
						"<br>Category: " + category, "Access Denied", JOptionPane.WARNING_MESSAGE, null);
				setEditable(false);
				this.dispose();
				return false;
			}
			setEditable(false);
		}
		return true;
	}

	public void setEditable(boolean b) {
		for (int i=1; i<dbFields.size(); i++) {
			dbFields.get(i).setEditable(b);
		}
		this.editable  = b;
	}
	
	@Override 
	public void setVisible(boolean visible) {
		if (visible) {
			if (checkSecurity()) {
				super.setVisible(true);
			} else {
				super.setVisible(false);
			}
		}
		else {
			super.setVisible(false);
		}
	}
}
