package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MultiSheetSelectWizardPane extends AbstractWizardPage implements SearchChangedListener, PasswordListener {

	private JTabbedPane tabbedPane;
	protected ArrayList<ZbyteSelectSheet> selectSheets;

	public MultiSheetSelectWizardPane(DbParms dbParms) throws SQLException {
		super();
		setName("select");
		
		selectSheets = new ArrayList<ZbyteSelectSheet>();

		tabbedPane = new JTabbedPane();
		
		tabbedPane.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				tabbedPane.getSelectedComponent().requestFocusInWindow();
			}});
		
		setLayout(new BorderLayout());
		add(getTopLabel(), BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
		add(getBottomPane(), BorderLayout.SOUTH);
	}

	private Component getBottomPane() {
		return new JLabel();
	}

	private Component getTopLabel() {
		return new JLabel("Select Original Items.");
	}

//	public ArrayList<TableData> loadSelectedItems(DataTransferItemFactory dataTransferItemFactory) {
//		return deliverableSheet.loadSelectedItems(dataTransferItemFactory);
//	}

//	public String getJobNum() {
//		return deliverableSheet.getJobNum();
//	}
	
	public void addSheet(ZbyteSelectSheet selectSheet) {
		selectSheets.add(selectSheet);
		tabbedPane.add(selectSheet);
		selectSheet.addSearchChangedListener(this);
		selectSheet.addPasswordListener(this);
	}
	
	public boolean getChanged() {
		for (ZbyteSelectSheet sheet:selectSheets ) {
			if (sheet.getChanged())
				return true;
		}
		return  false;
	}

	public void setChanged(boolean b) {
		for (ZbyteSelectSheet sheet:selectSheets ) {
			sheet.setChanged(b);
		}
	}
	
	public void setVisibleColumns(String[] colNames) {
		for (ZbyteSelectSheet sheet:selectSheets ) {
			sheet.setVisibleColumns(colNames);
		}
		
	}

	public void setPkeySearchField(Object fieldName) {
		for (ZbyteSelectSheet sheet:selectSheets ) {
			sheet.setPrimarySearchField(fieldName);
		}
	}

	@Override
	public void searchChanged(DatabaseModelSearchPane databaseModelSearchPane) {
		Object field = databaseModelSearchPane.getPrimaryKeyField();
		Object val = databaseModelSearchPane.getSelectedPkeyVal();
		setPkeySearchField(field);
		setPkeySearchValue(val);
	}

	public void setPkeySearchValue(Object val) {
		for (ZbyteSelectSheet sheet:selectSheets ) {
			sheet.setPrimarySearchValue(val);
		}
	}

	@Override
	public void passwordCorrect(boolean b) {
		for (ZbyteSelectSheet sheet:selectSheets ) {
			sheet.passwordCorrect(b);
		}
	}

	public ArrayList<TableData> loadSelectedItems(ModelTransferItemFactory stif) {
		ArrayList<TableData> items = new ArrayList<TableData>();
		for (ZbyteSelectSheet sheet:selectSheets ) {
			ArrayList<TableData> list = sheet.loadSelectedItems(stif);
			for (TableData td: list) {
				items.add(td);
			}
		}
		return items;
	}
}
