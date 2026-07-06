package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

public class DbSpreadsheetPanel extends SpreadsheetPanel implements SQLErrorListener, ClickedOkListener {
	
	protected DatabaseModelSearchPane searchPane;
	protected DbParms dbParms;
	private EditButton editRowButton;
	private boolean[] editableColumns;
	private PlusCopyButton addSameButton;
	protected DbRowCopier copier;
	private JPanel rowButtonPane;

	public DbSpreadsheetPanel(DbParms dbParms) throws SQLException {
		super(null);
		
		this.dbParms = dbParms;
//		dbParms.url = "jdbc:mysql://127.0.0.1";
//		dbParms.db = "geomJobs";
//		dbParms.dbTable = "test2";
//		dbParms.query = "select * from " + dbParms.dbTable;
//		dbParms.user = "geom";
//		dbParms.pword = "geom;123";
//		dbParms.PASSWORD = "adminadmin";
//		dbParms.pkeyIndex = 6;
//		dbParms.write();
		
		setName(dbParms.dbTable);
		
		DatabaseModel model = null;
		
		model = DatabaseModel.getDatabaseModel(dbParms);
		addOptionalColumns(model);

		this.model = model;
		this.table.setModel(model);
		model.addModelChangedListener(this);
		this.modelChanged();
		searchPane = makeSearchPane();

		if (model != null) {
			model.setEditableColumns(getEditableColumns());
			model.addSQLErrorListener(this);
		}
		
		JPanel southPane = new JPanel();
		southPane.setLayout(new BoxLayout(southPane, BoxLayout.Y_AXIS));
		Component p = getBottomPane();
		if (p != null) southPane.add(p);
		if (searchPane != null) southPane.add(searchPane);
		southPane.setBorder(BorderFactory.createEtchedBorder());
		
		JPanel padding = new JPanel(new BorderLayout());
		padding.add(southPane);
		padding.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		add(padding, BorderLayout.SOUTH);
		
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	protected DatabaseModelSearchPane makeSearchPane() {
		return new DatabaseModelSearchPane((DatabaseModel) model, dbParms.dbTable);
	}

	public void setSearchPane(DatabaseModelSearchPane searchPane) {
		this.searchPane = searchPane;
	}
	
	/**
	 * override in subclass
	 * @param model
	 */
	public void addOptionalColumns(DatabaseModel model){};

	protected Component getBottomPane() {
		return new JPanel();
	}

	/**
	 * override in sublcass with array of column names the user can edit
	 * @return
	 */
	public String[] getEditableColumns() {
		return model.getColumnNames();
	}

	@Override
	public Component getColButtonPane() {
//		JPanel errorpanel = new JPanel();
//		errorLabel = new JLabel("messages:");
//		errorpanel.add(errorLabel);
//		return errorpanel;
		return new JPanel();
	}
	
	/**
	 * does nothing
	 */
	@Override
	public void setEnableInterp(boolean enabled) {
	}
	
	@Override
	public void handleException(Exception e) {
//		if (e == null) return;
//		String msg = e.getMessage();
//		if (msg == null) msg = e.toString();
//		errorLabel.setText("Database Error: "+e.toString());
//		errorLabel.setForeground(Color.red);
		JOptionPane.showMessageDialog(this, e, "Database Error", JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
	}
	
	@Override
	public void setRowButtonsEnabled(boolean enabled) {
		super.setRowButtonsEnabled(enabled);
		if (editRowButton == null) return;
		editRowButton.setEnabled(enabled);
	}

	@Override
	public void setDeleteRowColEnabled(boolean enabled) {}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		//errorLabel.setText("OK");
		//errorLabel.setForeground(Color.black);
		super.valueChanged(e);
	}
	
	protected void setBaseSearch(Object value, String field) {
			if (searchPane != null) {
				searchPane.setBaseSearch(value, field);
				searchPane.excecuteSearchOnModel();
			}
	}

	public static DbSpreadsheetPanel makePanel(String appName, String tableName) throws SQLException {
		DbParms p = DbParms.read(appName);
		p.dbTable = tableName;
		p.query = "select * from "+tableName;
		p.pkeyIndex = 0;
		DbSpreadsheetPanel panel = new DbSpreadsheetPanel(p);
		panel.setName(tableName);
		return panel;
	}
	
	@Override
	protected void addRowPaneButtons(JPanel panel) {
		rowButtonPane = panel;
		super.addRowPaneButtons(panel);
		panel.add(getEditRowButton());
	}
	
	protected Component getEditRowButton() {
		editRowButton  = new EditButton();
		editRowButton.setToolTipText("edit selected row(s)");
		editRowButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (table.getRowCount() < 1) return;
				int row = table.getSelectedRow();
				if (row < 0) return;
				int modelRow = table.convertRowIndexToModel(row);
				editRow(modelRow);
			}}
		);
		return editRowButton;
	}

	/**
	 * subclass this method to edit row
	 * @param modelRow
	 */
	protected void editRow(int modelRow) {
		EditDbRowPane p = new EditDbRowPane(dbParms.dbTable, ((DatabaseModel)model).getPkeyName());
		p.setRow(modelRow);
		p.setVisible(true);
		p.addClickedOKListener(this);
	}

	@Override
	public void clickedOk() {
		//this.model.fireTableDataChanged();
		this.modelChanged();
	}

	public String getHtml() {
		int nRows = model.getRowCount();
		String html = "<br>" + dbParms.dbTable.toUpperCase().replace("_"," ") + "<br>";
		html += "<table border=\"1\"><tr>";
		String[] cNames = model.getColumnNames();
		for (String name: cNames) {
			html += "<th>" + name + "</th>";
		}
		html += "</tr>";
		for (int i=0; i<nRows; i++) {
			html += "<tr>";
			for (String name: cNames) {
				Object v = model.getValueAt(i, name);
				if ( v == null ) v = "";
				html += "<td>" + v + "</td>";
			}
			html += "</tr>";
		}
		html += "</table>";
		return html;
	}

//	public void setHideColumns(String[] cols) {
//		//((DatabaseModel)model).setHideColumns(cols);
//		for (String col: cols) {
//			this.table.getColumn(col).setm
//		}
//	}

	public void setEditableColumns(String[] editableColNames) {
		((DbModelInterface)model).setEditableColumns(editableColNames);
		editableColumns = null;
	}

	public void setEditable(boolean editable) {
		if (editableColumns == null)
			editableColumns = ((DbModelInterface)model).getEditableColumns();
		if (editable) {
			((DbModelInterface)model).setEditableColumns(editableColumns);
		} else {
			boolean[] nullarray = null;
			((DbModelInterface)model).setEditableColumns(nullarray);
		}
		this.modelChanged();
		
//		addRowButton.setEnabled(editable);
//		delRowButton.setEnabled(editable);
		setRowButtonsEnabled(editable);
	}
	
	public void setPrimarySearchValue(Object val) {
		searchPane.setPrimarySearchValue(val);
	}
	
	public void setPrimarySearchField(Object fieldName) {
		searchPane.setPrimarySearchField(fieldName);
	}
	
	public void setSecondarySearchValue(Object val) {
		searchPane.setSecondarySearchValue(val);
	}
	
	public void setModel(AbstractSpreadsheetModel model) {
		super.setModel(model);
		searchPane.setModel((DatabaseModel) model);
		searchPane.updateBoxLists();
		model.addModelChangedListener(this);
		((DatabaseModel)model).addSQLErrorListener(this);
	}
	
	public void setVisibleColumns(String[] colNames) {
		AbstractSpreadsheetModel subModel = new LimitedDbSpreadsheetModel(colNames, (DbModelInterface) model);
		setModel(subModel);
	}

	public void executeSearch() {
		searchPane.excecuteSearchOnModel();
	}
	
	public int findRow(String colName, Object val) {
		return model.findRow(colName, val);
	}

	public void addRowCheckCopy(int modelRow) {
		model.addRow(modelRow);
		int rc = model.getRowCount();
		fireRowAdded(model, rc-1);
		checkCopyToAddedRow();
	}

	private void checkCopyToAddedRow() {
		if (copier == null) return;
		int addedRow = table.getRowCount()-1;
		int selectedRow = table.getSelectedRow();
		if (addedRow < 0) return;
		if (selectedRow < 0) return;
		int modelRow = table.convertRowIndexToModel(selectedRow);
		if (modelRow < 0) return;
		copier.copyFromTo(modelRow, addedRow, (DatabaseModel) model);
	}

	public int findRow(String col1, Object val1, String col2, Object val2) {
		int nRows = model.getRowCount();
		int c1 = model.getColumnIndex(col1);
		int c2 = model.getColumnIndex(col2);
		if (c1 < 0 ) {
			SUtil.printErr("DbSpreadsheetPane.findRow() - Column \"" + col1 + "\" not found in table \"" + model.getTableName());
			return -1;
		}
		if (c2 < 0 ) {
			SUtil.printErr("DbSpreadsheetPane.findRow() - Column \"" + col2 + "\" not found in table \"" + model.getTableName());
			return -1;
		}
		for (int i=0; i< nRows; i++) {
			Object v1 = model.getValueAt(i, c1);
			Object v2 = model.getValueAt(i, c2);
			if (v1 == null || v2 == null) continue;
			if (v1.equals(val1) && v2.equals(val2))
				return i;
		}
		
		return -1;
	}
	
	public boolean existsInDatabase(String colName, String val) throws SQLException {
		int count = ((DatabaseModel)model).getRowCount(colName, val);
		return count > 0;
	}

	public AbstractSpreadsheetModel getModel() {
		return model;
	}
	
	@Override
	public void setRowButtonsVisible(boolean visible) {
		editRowButton.setVisible(visible);
		super.setRowButtonsVisible(visible);
	}
	
	public void setSearchValueList(String colName, Object[] vals) throws SQLException {
		ResultSet rs2 = ((DatabaseModel)model).executeValueListSearch(colName, vals);
		((DatabaseModel)model).setResultSet(rs2);
	}
	
	private Component getAddSameButton() {
		addSameButton = new PlusCopyButton();
		addSameButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				addRowCheckCopy(0);
				int addedRow = getRowCount()-1;
				editRow(addedRow);
			}});
		
		return addSameButton;
	}

	public void setCopier(DbRowCopier copier) {
		if (addSameButton == null) {
			rowButtonPane.add(getAddSameButton(), 5);
			rowButtonPane.validate();
		}
		this.copier = copier;
	}
	
}
