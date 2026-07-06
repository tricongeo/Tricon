package com.tricongeophysics;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;

public class NewDbRowPane implements SQLErrorListener, ClickedCancelListener, ClickedOkListener {

	private EditDbRowPane newEditPane;

	public NewDbRowPane(EditDbRowPane editDbRowPane) {
		this.newEditPane = editDbRowPane;
		newEditPane.getPkeyField().setEditable(true);
		newEditPane.addSqlErrorListener(this);
		newEditPane.addClickedCancelListener(this);
		newEditPane.addClickedOKListener(this);
		newEditPane.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				newEditPane.removeClickedCancelListener(NewDbRowPane.this);
			}
		});
		newEditPane.addRow();
		newEditPane.setRow(newEditPane.getRowCount()-1);
		newEditPane.setVisible(true);
	}

	@Override
	public void handleException(Exception e) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(newEditPane, "<html>Failed to add row.<br>"+e.toString(), "Failed to Add Row", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void clickedCancel() {
		newEditPane.deleteRow(newEditPane.getRowCount()-1);
	}
	
	@Override
	public String toString() {
		return "new " + newEditPane;
	}

	@Override
	public void clickedOk() {
		//newEditPane.set
		//DbField f = newEditPane.getPkeyField();
//		f.setValue(f.getValue());
		//int row = newEditPane.getRowCount()-1;
		//newEditPane.model.setValueAt(f.getValue(), row, f.getColName());
	}
}
