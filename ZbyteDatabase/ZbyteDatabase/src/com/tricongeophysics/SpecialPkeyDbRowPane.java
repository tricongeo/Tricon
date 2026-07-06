package com.tricongeophysics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class SpecialPkeyDbRowPane extends ZbyteEditDbRowPane implements ActionListener{

	protected static final String[] Letters = {
		"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v"
		, "w", "x", "y", "z"
	};
	protected String pkeyVal;
	
	public SpecialPkeyDbRowPane(String tableName, String pkeyName) {
		super(tableName, pkeyName);
	}

	public abstract void addActionListener();
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!getPkeyField().isEditable()) return;  //pkey not editable, so leave alone!
		if (!requiredFieldsOk()) return;
		setPkeyVal();
		if (pkeyVal == null) return;
		ensurePkeyUniq();
		getPkeyField().setValue(pkeyVal);
		ok.setEnabled(true);
	}

	protected abstract boolean requiredFieldsOk();

	protected void ensurePkeyUniq() {
		int count = getOccurences();
		if (count > 0) {
			pkeyVal += Letters[count];
		}
	}

	public abstract void setPkeyVal();
	
	protected int getOccurences() {
		int count = -1;
		try {
			model = DatabaseModel.getDatabaseModel(tableName);
			count = model.getRowCount(getPkeyName(), "*" + pkeyVal + "*");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return count;
	}
	
	@Override
	public void setVisible(boolean b) {
		if (b) okClicked = false;
		super.setVisible(b);
		if (getPkeyField().isEditable()) { //if pkey is editable, we're making new item, so disable OK until fields filled out
			ok.setEnabled(false);
		}
	}
	
	@Override
	public Component getCenterPane() {
		JPanel p = new JPanel();
		p.add(new JLabel(getRequiredFieldsString()));
		return p;
	}

	public abstract String getRequiredFieldsString();

}
