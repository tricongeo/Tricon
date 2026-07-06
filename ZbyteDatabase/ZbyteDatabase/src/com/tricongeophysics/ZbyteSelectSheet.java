package com.tricongeophysics;

import java.awt.Component;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class ZbyteSelectSheet extends ZbyteDbSpreadsheetPanel {

	ZbyteSelectSheet(DbParms p) throws SQLException {
		super(p);
		//this.setName("Deliverable Support");
		//this.passwordCorrect(false);
		model = new SelectDbSpreadsheetModelDecorator(DatabaseModel.getDatabaseModel(p));
		setModel(model);
	}
	
	@Override
	public void setModel(AbstractSpreadsheetModel model) {
		int selectCol = model.getColumnIndex("Select");
		if (selectCol >= 0 ) {
			super.setModel(model);
		} else {
			AbstractSpreadsheetModel model2 = new SelectDbSpreadsheetModelDecorator((DbModelInterface) model);
			super.setModel(model2);
		}
	}

	@Override
	public Component getRowButtonPane() {
		Component cp = this.getCopyPastePane(BoxLayout.Y_AXIS);
		Component rp = this.getEditRowButton();
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.add(cp);
		pane.add(Box.createVerticalGlue());
		pane.add(rp);
		pane.add(Box.createVerticalGlue());

		return pane;
	}

//	@Override
//	public void addOptionalColumns(DatabaseModel model){
//		DbColumn selectItem = new SelectItemColumn(model);
//		model.addOptionalColumn(selectItem);
//		model.setWarningColumn(selectItem);
//		//model.setTableName(model.getTableName() + "select");
//	}

	public ArrayList<TableData> loadSelectedItems(ModelTransferItemFactory dtif) {
		ArrayList<TableData> selectedItems = new ArrayList<TableData>();

		int c = model.getColumnIndex("Select");
		int rc = model.getRowCount();
		for (int i=0; i< rc; i++) {
			Object v = model.getValueAt(i, c);
			if (v == null) continue;
			if ( v.toString().equals("true") ) {
				selectedItems.add(dtif.create(model, i));
			}
		}
		return selectedItems;
	}

//	public String getJobNum() {
//		return searchPane.getSelectedPkeyVal()+"";
//	}

	@Override
	public Component getBottomPane() {
		Component p = super.getBottomPane();

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(p);
		//panel.add(getPasswordPane());

		return panel;
	}

	public void addSearchChangedListener(SearchChangedListener l) {
		searchPane.addSearchChangedListener(l);
	}

	public AbstractSpreadsheetModel getModel() {
		return model;
	}

	public void setSelectedItems(String columnName, Object val) {
		int c = model.getColumnIndex("Select");
		int rc = model.getRowCount();
		for (int i=0; i< rc; i++) {
			Object v = model.getValueAt(i, c);
			if (v == null) continue;
			if ( v.toString().equals("true") ) {
				model.setValueAt(val, i, columnName);
			}
		}
	}

	public void selectAll(boolean select) {
		int c = model.getColumnIndex("Select");
		int rc = model.getRowCount();
		for (int i=0; i< rc; i++) {
			model.setValueAt(select, i, c);
		}
	}
}
