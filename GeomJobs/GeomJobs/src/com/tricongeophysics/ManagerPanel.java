package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class ManagerPanel extends GeomSpreadsheetPanel {

	private JPasswordField passwordField;
	private ReloadButton button;

	ManagerPanel(DbParms p) throws SQLException {
		super(p);
		this.setName("Manager");
		//setBaseSearch("flightgear", "Processor");
	}
	
	@Override
	public String[] getEditableColumns() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * does nothing
	 */
	@Override
	public void addOptionalColumns(DatabaseModel model) {
		
	}
	
	/**
	 * does nothing
	 */
	@Override
	public Component getRowButtonPane() {
		return new JPanel();
	}
	
	@Override
	public Component getBottomPane() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		
		pane.add(getPasswordPane());
		
		JPanel outerPane = new JPanel();
		outerPane.setLayout(new BorderLayout());
		outerPane.add(pane, BorderLayout.WEST);
		return outerPane;
	}
	
	private Component getPasswordPane() {
		passwordField = new JPasswordField(10);
		//passwordField.setPreferredSize(new Dimension(100,30));
		passwordField.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (passwordField.getText().equals(dbParms.managerPWord)) {
					getData();
					button.setEnabled(true);
				}
			}
		});
		
		button = new ReloadButton();
		button.setEnabled(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getData();
			}
		});

		JLabel label = new JLabel("Enter the password: ");
		label.setLabelFor(passwordField);
		
		JPanel passwordPane = new JPanel();
		passwordPane.setLayout(new BoxLayout(passwordPane, BoxLayout.X_AXIS));
		passwordPane.add(label);
		passwordPane.add(passwordField);
		passwordPane.add(button);
		passwordPane.add(Box.createHorizontalGlue());
		
		JPanel outerPane = new JPanel();
		outerPane.setLayout(new BorderLayout());
		outerPane.add(passwordPane, BorderLayout.WEST);
		outerPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return outerPane;
	}

	protected void getData() {
		String query = "select technician, " +
		"count(*) as 'Total Jobs', " +
		"count(`Date Finished`) as 'Finished', " +
		"(count(*) - count(`Date Finished`)) as 'Incomplete', " +
		"count(case when ( (now() > `Finish By`) and (`Date Finished` is NULL) ) then 1 else NULL END) as 'Late'" +
		" from " + dbParms.dbTable + 
		" group by technician";
		ResultSet rs;
		rs = ((DatabaseModel)model).executeQuery(query);
		((DatabaseModel)model).setResultSet(rs);
	}
	
	@Override
	public DatabaseModelSearchPane makeSearchPane() {
		return null;
	}
}
