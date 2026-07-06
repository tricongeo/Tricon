package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public abstract class NewEditDbPane extends JFrame{

	protected DatabaseModel model;
	protected int row;
	private JButton ok;
	private JButton cancel;
	private ArrayList<ActionListener> clickedOKListeners = new ArrayList<ActionListener>();

	public NewEditDbPane() {
		super();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel background = new JPanel(new BorderLayout());
		
		try {
			this.model = getModel();
			background.add(getTopPane(), BorderLayout.NORTH);
			background.add(getCenterPane(), BorderLayout.CENTER);
			background.add(getBottomPane(), BorderLayout.SOUTH);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		getContentPane().add(background);
		pack();
	}

	private DatabaseModel getModel() throws Exception {
		DbParms dbParms = DbParms.getParms("ZbyteDatabase");
		dbParms.dbTable = getTableName();
		dbParms.query = "select * from " + getTableName();
		model = DatabaseModel.getDatabaseModel(dbParms);
		return model;
	}

	protected abstract String getTableName();

	private void showError(String m) {
		JOptionPane.showMessageDialog(this, m, "Database Connection Error", JOptionPane.ERROR_MESSAGE);
	}

	protected Component getBottomPane() {
		JPanel p = new JPanel();
		
		ok = new JButton("OK");
		ok.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clickedOK();
				NewEditDbPane.this.dispose();
			}});
		p.add(ok);
		
		cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				NewEditDbPane.this.dispose();
			}});
		//p.add(cancel);
		
		return p;
	}

	/**
	 * Hook for notifying listeners that the user clicked OK before the frame is disposed of.
	 */
	protected void clickedOK() {
		for (ActionListener l:clickedOKListeners) {
			l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Clicked OK"));
		}
	}

	protected Component getCenterPane() {
		return new JPanel();
	}

	protected Component getTopPane() {
		return new JPanel();
	}
	
	protected void addClickedOKListener(ActionListener l) {
		clickedOKListeners.add(l);
	}

	public Object[] getValues() {
		return model.getValues(getPkeyName());
	}

	public Object getValue() {
		return model.getValueAt(row, getPkeyName());
	}

	protected abstract String getPkeyName();

	public void setRow(int selectedIndex) {
		row = selectedIndex;
		rowChanged();
	}

	protected void rowChanged() {
	}
	
	protected SimpleField makeSimpleDbField(final String colName) {
		Object d =  model.getValueAt(row, colName);
		final SimpleField field = new SimpleField(colName+":", d);
		addClickedOKListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				model.setValueAt(field.getValue(), row, colName);
			}});
		return field;
	}
}
