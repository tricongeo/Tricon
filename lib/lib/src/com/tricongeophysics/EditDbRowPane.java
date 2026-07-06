
package com.tricongeophysics;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class EditDbRowPane extends JFrame {

	protected DatabaseModel model;
	protected int row;
	protected JButton ok;
	private ArrayList<ClickedOkListener> clickedOKListeners = new ArrayList<ClickedOkListener>();
	protected String tableName;
	private String pkeyName;
	protected String[] colNames;
	protected ArrayList<DbField> dbFields;
	private JButton cancel;
	private String niceTableName;
	protected JPanel gridPane;
	private ArrayList<ClickedCancelListener> clickedCancelListeners = new ArrayList<ClickedCancelListener>();
	protected boolean okClicked = false;
	protected JPanel background;
	private boolean initialized = false;

	public EditDbRowPane(String tableName, String pkeyName2) {
		super();
		niceTableName = tableName.replace("_", " ").toUpperCase(Locale.US);
		setTitle(niceTableName);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		background = new JPanel(new BorderLayout());
		addWindowListener(new WindowAdapter(){
			@Override
			/**
			 * Make sure that the window acts like a cancel if the user didn't click OK
			 */
			public void windowClosing(WindowEvent e) {
				if (!okClicked ) {
					clickedCancel();
				}
				closing();
			}
		});
		
		this.tableName = tableName;
		this.pkeyName = pkeyName2;
		
		try {
			this.model = getModel();
			colNames = model.getColumnNames();
			if (pkeyName == null) pkeyName = colNames[0];
			model.setPkeyName(pkeyName);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		
		loadPkey();
		//initialize();
	}

	private void loadPkey() {
		dbFields = new ArrayList<DbField>();
		DbField dbf = new DbField(model, row, colNames[0]);
		addClickedOKListener(dbf);
		dbFields.add(dbf);
		dbf.setEditable(false);
	}

	protected void closing() {
		setVisible(false);
		EditDbRowPane.this.dispose();
//		dbFields = null;
//		clickedOKListeners = null;
//		clickedCancelListeners = null;
//		//model.clear();
//		//model = null;
//		cancel = null;
//		ok = null;
		System.gc();
	}

	public Component getCenterPane() {
		return new JPanel();
	}

	/**
	 * Lazy gui creation!
	 * We don't actually get the information for the different display fields until:
	 * A) - user asks for information (or other code needs it), or
	 * B) - gui actually showing on the screen.
	 * 
	 * loadFields() method gets called just before gui gets displayed (setVisible(true))
	 */
	protected void loadFields() {
		if (dbFields.size() > 1) return; //check if fields already loaded
		//dbFields = new ArrayList<DbField>();
		for (int i=1; i<colNames.length; i++) {
			DbField dbf = new DbField(model, row, colNames[i]);
			addClickedOKListener(dbf);
			dbFields.add(dbf);
		}
	}

	private DatabaseModel getModel() throws Exception {
//		DbParms dbParms = DbParms.getParms("ZbyteDatabase");
//		dbParms.dbTable = getTableName();
//		dbParms.query = "select * from `" + getTableName() + "`";
		model = DatabaseModel.getDatabaseModel(tableName);
		return model;
	}

	protected String getTableName() {
		return tableName;
	}

	private void showError(String m) {
		JOptionPane.showMessageDialog(this, m, "Database Connection Error", JOptionPane.ERROR_MESSAGE);
	}

	protected Component getBottomPane() {
		JPanel p = new JPanel();

		cancel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clickedCancel();
				EditDbRowPane.this.dispose();
				EditDbRowPane.this.dispatchEvent(new WindowEvent(EditDbRowPane.this, WindowEvent.WINDOW_CLOSING));
			}});
		p.add(cancel);
		
		ok.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clickedOK();
				closing();
				EditDbRowPane.this.dispose();
				EditDbRowPane.this.dispatchEvent(new WindowEvent(EditDbRowPane.this, WindowEvent.WINDOW_CLOSING));
			}});
		p.add(ok);
		
		JPanel p2 = new JPanel(new BorderLayout());
		p2.add(p, BorderLayout.EAST);
		
		return p2;
	}

	protected void clickedCancel() {
		okClicked = false;
		for (ClickedCancelListener l:clickedCancelListeners) {
			l.clickedCancel();
		}
		closing();
	}

	/**
	 * Hook for notifying listeners that the user clicked OK before the frame is disposed of.
	 */
	protected void clickedOK() {
		okClicked = true;
		for (ClickedOkListener l:clickedOKListeners) {
			l.clickedOk();
		}
		//closing();
		model.fireTableDataChanged();
	}
	
	protected Component getTopPane() {
		
		gridPane = new JPanel();
		gridPane.setLayout(new GridLayout(0, 3));
		for (int i=1; i<dbFields.size(); i++) {
			gridPane.add(dbFields.get(i));
		}
		
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.Y_AXIS));
		topPane.add(dbFields.get(0));
		topPane.add(gridPane);
		return topPane;
	}
	
	protected void addClickedOKListener(ClickedOkListener l) {
		clickedOKListeners.add(l);
	}

	/**
	 * returns all possible pkey values from database
	 * @return
	 */
	public Object[] getPkeys() {
		if (model == null) return null;
		return model.getPkeys();
	}

	/**
	 * returns pkey value of currently selected row
	 * @return
	 */
	public Object getPkey() {
		return model.getValueAt(row, getPkeyName());
	}

	protected String getPkeyName() {
		return pkeyName;
	}

	public void setRow(int selectedIndex) {
		if (selectedIndex < 0) {
			SUtil.printErr("Can't set to negative row! " + selectedIndex + " " + this.toString());
			return;
		}
		loadFields();
		row = selectedIndex;
		rowChanged();
	}

	protected void rowChanged() {
		for (DbField dbf: dbFields) {
			dbf.setRow(row);
		}
	}
	
	protected SimpleField makeSimpleDbField(final String colName) {
		Object d =  model.getValueAt(row, colName);
		final SimpleField field = new SimpleField(colName+":", d);
		addClickedOKListener(new ClickedOkListener(){
			public void clickedOk() {
				model.setValueAt(field.getValue(), row, colName);
			}});
		return field;
	}

	public DbField getPkeyField() {
		return dbFields.get(0);
	}

	

	public void addRow() {
		model.addRow(1);
	}

	public int getRowCount() {
		return model.getRowCount();
	}

	public void reloadModel() {
		model.reload();
	}

	public void addSqlErrorListener(SQLErrorListener l) {
		model.addSQLErrorListener(l);
	}
	
	public String toString() {
		return tableName + ":" + row;
	}

	public String getHtml() {
		loadFields();
		String html = "";
		html += "<br>" + niceTableName + "<br>";
		html += "<table><tr>";
		int count = 0;
		ArrayList<DbField> comboFields = new ArrayList<DbField>();
		for (DbField f: dbFields) {
			if (f.simpleField instanceof NewEditComboBoxField) {
				comboFields.add(f);
				continue;
			}
			html +="<td>" + f.getHtml() + "</td>";
//			if (f.getColName().equals("job")) {
//				jobField = f;
//			}
			if (count%2 == 0) {
				html += "</tr><tr>";
			}
			count++;
		}
		html += "</tr></table>";
		for (DbField f:comboFields ) {
			html += f.getHtml();
		}
		return html;
	}
	
	protected DbField getField(String name) {
		if (!name.equals(getPkeyName())) loadFields();
		for (DbField field: dbFields) {
			if (field.getName().endsWith(name)) {
				return field;
			}
		}
		SUtil.printErr("Field: "+ name + " not found in " + model.getTableName());
		return null;
	}

	public void addClickedCancelListener(ClickedCancelListener l) {
		clickedCancelListeners.add(l);
	}

	public void deleteRow(int row) {
		model.delRow(row);
	}

	public void removeClickedCancelListener(ClickedCancelListener l) {
		clickedCancelListeners.remove(l);
	}

	public void initialize() {
		if (initialized) return;
		loadFields();
		background.add(getTopPane(), BorderLayout.NORTH);
		background.add(getCenterPane(), BorderLayout.CENTER);
		background.add(getBottomPane(), BorderLayout.SOUTH);
		getContentPane().add(background);
		pack();
		initialized = true;
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) initialize(); //trying to show gui, better load displays now
		super.setVisible(visible);
	}

	/**
	 * set row based on pkey val
	 * @param contact
	 */
	public void setRow(Object pkeyVal) {
		int row = model.findRow(pkeyName, pkeyVal);
		setRow(row);
	}
}
