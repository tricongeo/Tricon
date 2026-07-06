package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class AdminPanel extends GeomSpreadsheetPanel {

	private JPasswordField passwordField;
	private Component addRowPane;
	private JLabel label;
	private JTextField textField;
	private PlusButton button;

	AdminPanel(DbParms p) throws SQLException {
		super(p);
		this.setName("Admin");
		//setBaseSearch("flightgear", "Processor");
		//searchPane.setBaseSearch(null);
	}

	@Override
	public String[] getEditableColumns() {
		return model.getColumnNames();
	}

	@Override
	public Component getBottomPane() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		
		pane.add(getPasswordPane());
		
		addRowPane = getAddRowPane();
		setAddRowPaneEnabled(false);
		pane.add(addRowPane);
		
		JPanel outerPane = new JPanel();
		outerPane.setLayout(new BorderLayout());
		outerPane.add(pane, BorderLayout.WEST);
		return outerPane;
	}

	private void setAddRowPaneEnabled(boolean b) {
		textField.setEditable(b);
		button.setEnabled(b);
		label.setEnabled(b);
		searchPane.setEnabled(b);
	}

	private Component getAddRowPane() {
		label = new JLabel("Add New Row (tab delimited text): ");
		textField = new JTextField();
		textField.setColumns(100);
		button = new PlusButton();
		
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String text = textField.getText();
				if (text == null) return;
				try {
					((DatabaseModel)model).addRowFromText(text);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(AdminPanel.this, "<HTML>Error while adding row.<br>"+e.toString());
					((DatabaseModel)model).fireExceptionOccured(e);
				}
				textField.setText("");
			}});
		
		
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
		pane.add(label);
		pane.add(textField);
		pane.add(button);
		pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return pane;
	}

	private Component getPasswordPane() {
		passwordField = new JPasswordField(10);
		//passwordField.setPreferredSize(new Dimension(100,30));
		passwordField.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (passwordField.getText().equals(dbParms.adminPWord)) {
					getData();
					setAddRowPaneEnabled(true);
				}
			}});

		JLabel label = new JLabel("Enter the password: ");
		label.setLabelFor(passwordField);
		
		JPanel passwordPane = new JPanel();
		passwordPane.setLayout(new BoxLayout(passwordPane, BoxLayout.X_AXIS));
		passwordPane.add(label);
		passwordPane.add(passwordField);
		passwordPane.add(Box.createHorizontalGlue());
		
		JPanel outerPane = new JPanel();
		outerPane.setLayout(new BorderLayout());
		outerPane.add(passwordPane, BorderLayout.WEST);
		outerPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return outerPane;
	}

	protected void getData() {
		ResultSet rs = ((DatabaseModel)model).executeQuery(dbParms.query);
		((DatabaseModel)model).setResultSet(rs);
	}
	
	@Override
	public Component getRowButtonPane() {
		Component cp = this.getCopyPastePane(BoxLayout.Y_AXIS);
		Component rp = super.getRowButtonPane();
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.add(cp);
		pane.add(Box.createVerticalGlue());
		pane.add(rp);
		pane.add(Box.createVerticalGlue());
		
		return pane;
	}
}
