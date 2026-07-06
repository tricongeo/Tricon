package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class DatabaseModelSearchPane extends JPanel {

	private static final Object NoSearch = "No Search";
	private JComboBox fieldBox;
	protected JComboBox valueBox;
	protected DatabaseModel model;
	protected String dbTable;
	//private String baseSearch;
	private JLabel label;
	private SearchButton button;
	private ArrayList<SearchChangedListener> searchChangedListeners = new ArrayList<SearchChangedListener>();
	private String baseSearchField;
	private Object baseSearchVal;

	public DatabaseModelSearchPane(DatabaseModel dbModel, String table) {
		this.model = dbModel;
		this.dbTable = table;
		
		setLayout(new BorderLayout());
		add(getButtonsPane(), BorderLayout.WEST);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		updateBoxLists();
	}

	protected Component getButtonsPane() {
		label = new JLabel("Search: ");
		valueBox = new JComboBox();
		button = new SearchButton();
		fieldBox = new JComboBox();
		
		//fieldBox.setSelectedIndex(0);
		fieldBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				//SUtil.print("\n field selected : " + fieldBox.getSelectedItem() + " \n");
				setPossibleValues();
				fireSearchChanged();
				DatabaseModelSearchPane.this.requestFocusInWindow();
			}});
		
		valueBox.setEditable(true);
		valueBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				excecuteSearchOnModel();
				fireSearchChanged();
				DatabaseModelSearchPane.this.requestFocusInWindow();
			}});
		
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				excecuteSearchOnModel();
			}});
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(label);
		panel.add(fieldBox);
		panel.add(new JLabel(" = "));
		panel.add(valueBox);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(button);
		panel.add(Box.createHorizontalGlue());
		
		return panel;
	}

	protected void fireSearchChanged() {
		for (SearchChangedListener l: searchChangedListeners) {
			l.searchChanged(this);
		}
	}

	protected void excecuteSearchOnModel() {
		String query = "";
		try {
			//model = DatabaseModel.getDatabaseModel(dbTable);  //scary!! I'm not sure why this is needed, but LimitedDbSpreadsheetModel appears to be broken without this
			Object field = fieldBox.getSelectedItem();
			if (field == null)
				throw new SQLException("Can't do search on null field.");
			Object value = valueBox.getSelectedItem();
			ResultSet rs;
			if (field.equals(NoSearch)) {
				rs = model.executeQuery(baseSearchField, baseSearchVal);
			} else {
				rs = model.executeCompoundQuery(baseSearchField, baseSearchVal, field.toString(), value);
			}

			
//			query = "select * from " + dbTable;
//			ResultSet rs = null;
//			if (field.equals(NoSearch)) {
//				if (baseSearch != null) query += " where " + baseSearch;
//				rs = model.executeQuery(query);
//			} else {
//				String v;
//				if (value == null) {
//					v = "` IS NULL";
//				} else {
//					v = "` = ?";
//					//v = "` = '" + value + "'";
//					value = value.toString().replace('*', '%');
//					if (value.toString().contains("%"))
//						v = v.replace("=", "LIKE");
//				}		
//				
//				java.sql.Connection con = model.getConnection();
//				query += " where `" + field + v;
//				if (baseSearch != null) query += " && " + baseSearch;
//				PreparedStatement pstmt = con.prepareStatement(query,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
//				SUtil.print("executing query: " + query + ". ? == \"" + value + "\"");
//				if (value != null) pstmt.setString( 1, ""+value);
//				rs = pstmt.executeQuery();
//			}
			
			//ResultSet rs = model.executeQuery(query);
			model.setResultSet(rs);
		} catch (SQLException e2) {
			JOptionPane.showMessageDialog(this, e2, "Database Error", JOptionPane.ERROR_MESSAGE);
			System.out.println("failed SQL: " + query + "\n");
			e2.printStackTrace();
		}
	}

	private void setPossibleValues() {
		if (fieldBox == null || fieldBox.getSelectedItem() == null) return;
		String field = fieldBox.getSelectedItem().toString();
		if (field.equals(NoSearch)) return;
		Object[] vals = null;
		String query = "";
		try {
//			query = "select `" + field + "` from " + dbTable;
//			if (baseSearch != null) query += " where " + baseSearch;
//			query += " group by `" + field + "`";
//			ResultSet rs = model.executeQuery(query);
			vals = model.findCompoundColumnValues(baseSearchField, baseSearchVal, field);
//			int nVals = 0;
//			while(rs.next()) {
//				//if (rs.getObject(1) == null) continue;
//				nVals++;
//			}
//			vals = new String[nVals];
//			int i=0;
//			rs.beforeFirst();
//			while(rs.next()) {
//				if (rs.getObject(1) == null) {
//					vals[i++] = null;
//				} else {
//					vals[i++] = rs.getObject(1).toString();
//				}
//			}
		} catch (Exception e) {
			System.out.println("failed SQL: " + query + "\n");
			e.printStackTrace();
		}
		if (vals == null) vals = new String[]{};
		valueBox.setModel(new DefaultComboBoxModel(vals));
	}

//	public void setBaseSearch(String baseSearch) {
//		this.baseSearch = baseSearch;
//		setPossibleValues();
//	}
	
	@Override
	public void setEnabled(boolean b) {
		label.setEnabled(b);
		valueBox.setEnabled(b);
		fieldBox.setEnabled(b);
		button.setEnabled(b);
	}
	
	protected void setBaseSearch(Object value, String field) {
//		String v;
//		if (value == null) {
//			v = "` IS NULL";
//		} else {
//			v = "` = '" + value + "'";
//		}				
//		String baseSearch = "`" + field + v;
	//	setBaseSearch(baseSearch);
		baseSearchField = field;
		baseSearchVal = value;
	}
	
	public Object getSelectedPkeyVal() {
		return valueBox.getSelectedItem();
	}
	
	protected void updateBoxLists() {
		Object selectedField = fieldBox.getSelectedItem();
		
		String[] fields = model.getColumnDbNames();
		if (fields == null) {
			//add(new JLabel("no database columns found"));
			return;
		}
		fieldBox.setModel(new DefaultComboBoxModel(fields));
		fieldBox.insertItemAt(NoSearch, 0);
		
		if (selectedField != null)
			fieldBox.setSelectedItem(selectedField);
		else
			fieldBox.setSelectedIndex(0);
		
		setPossibleValues();
	}

	public void setSecondarySearchValue(Object val) {
		SUtil.printErr("Invalid method! Must be dual search pane!!");
	}

	public void setPrimarySearchField(Object fieldName) {
		Object oldField = fieldBox.getSelectedItem();
		if (fieldName.equals(oldField)) return;
		fieldBox.setSelectedItem(fieldName);
	}
	
	public void setPrimarySearchValue(Object val) {
		Object oldVal = valueBox.getSelectedItem();
		if (oldVal == val) return;
		if (val != null && val.equals(oldVal)) return;
		valueBox.setSelectedItem(val);
	}
	
	public void addSearchChangedListener(SearchChangedListener l) {
		 searchChangedListeners.add(l);
	}

	public Object getPrimaryKeyField() {
		return fieldBox.getSelectedItem();
	}

	public void setModel(DatabaseModel model2) {
		if (model2 == null) return;
		dbTable = model2.getTableName();
		this.model = model2;
	}
	
	public Object getPrimarySearchValue() {
		return valueBox.getSelectedItem();
	}
}
