package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.tricongeophysics.DbSecurityManager.AccessLevel;

public class ZbyteDbSpreadsheetPanel extends DbSpreadsheetPanel implements BarcodeListener {

	private JPasswordField passwordField;
	private ArrayList<PasswordListener> passwordListeners = new ArrayList<PasswordListener>();
	private BarcodeScannerManager barcodeScannerManager;
	private ArrayList<BarcodeListener> barcodeListeners = new ArrayList<BarcodeListener>();
	private boolean editable = true;
	private PlusCopyButton addSameButton;
	protected DbRowCopier copier;

	public ZbyteDbSpreadsheetPanel(DbParms dbParms) throws SQLException {
		super(dbParms);
//		passwordCorrect(false);
		barcodeScannerManager = new BarcodeScannerManager();
		barcodeScannerManager.setBarcodeListener(this);
		this.addSheetKeyListener(barcodeScannerManager);
		this.addKeyListener(barcodeScannerManager);
	}
	
	public static DbSpreadsheetPanel makePanel(String appName, String tableName) throws SQLException {
		DbParms p = DbParms.read(appName);
		p.dbTable = tableName;
		p.query = "select * from "+tableName;
		p.pkeyIndex = 0;
		DbSpreadsheetPanel panel = new ZbyteDbSpreadsheetPanel(p);
		panel.setName(tableName);
		return panel;
	}
	
	@Override
	protected void editRow(int modelRow) {
		if (!editable) return;
		String tableName = ((DbModelInterface)model).getTableName();
		ZbyteEditDbRowPane p = ZbyteEditDbRowPane.createPane(tableName);
		Stopwatch sw = new Stopwatch("load editDbRowPane");
		sw.start();
		p.setVisible(true);
		p.addClickedOKListener(this);
		sw.stop();
		sw.printTime();
		p.setRow(modelRow);
	}
	
	@Override
	public void addRowCheckCopy(int modelRow) {
		if (!editable) return;
		super.addRowCheckCopy(modelRow);
	}
	
	Component getPasswordPane() {
		passwordField = new JPasswordField(10);
		//passwordField.setPreferredSize(new Dimension(100,30));
		passwordField.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (passwordField.getText().equals(dbParms.adminPWord)) {
					passwordCorrect(true);
					firePasswordCorrect(true);
				} else {
					passwordCorrect(false);
					firePasswordCorrect(false);
				}
				ZbyteDbSpreadsheetPanel.this.requestFocusInWindow();
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
	
//	@Override
//	public Component getBottomPane() {
//		return getPasswordPane();
//	}

	protected void passwordCorrect(boolean b) {
		setEditable(b);
	}

	private void firePasswordCorrect(boolean b) {
		for (PasswordListener l: passwordListeners) {
			l.passwordCorrect(b);
		}
	}

	public void addPasswordListener(PasswordListener l) {
		passwordListeners.add(l);
	}

	public ArrayList<TableData> loadModelTransferItems(ModelTransferItemFactory stif) {
		ArrayList<TableData> items = new ArrayList<TableData>();

		int rc = model.getRowCount();
		for (int i=0; i< rc; i++) {
			items.add(stif.create(model, i));
		}
		return items;
	}

	/**
	 * Override this method to do something with it.
	 * @param barcode
	 */
	@Override
	public void barcodeScanned(String barcode) {
		fireBarcodeScanned(barcode);
	}
	
	private void fireBarcodeScanned(String barcode) {
		for (BarcodeListener l: barcodeListeners)
			l.barcodeScanned(barcode);
	}

	public void addBarcodeListener(BarcodeListener l) {
		barcodeListeners.add(l);
	}
	
	@Override 
	public void setVisible(boolean visible) {
		//super.setVisible(visible);
		if (visible) {
			//barcodeScannerManager = BarcodeScannerManager.getScanManager();
//			this.addSheetKeyListener(barcodeScannerManager);
//			barcodeScannerManager.addBarcodeListener(this);
			if (checkSecurity()) {
				super.setVisible(true);
			} else {
				super.setVisible(false);
			}
		}
		else {
			super.setVisible(false);
		}
	}
	
	private boolean checkSecurity() {
		DbSecurityManager manager = DbSecurityManager.getManager();
		String category = ZbyteDbAccessTable.getAccessTable().getAccessCategory(model.getTableName());
		AccessLevel level;
		if (category == null) {
			SUtil.print("Model: " + model.getTableName() + " has no access category");
			level = AccessLevel.ReadWrite;
		} else {
			level = manager.getAccessLevel(category);
		}
//		if (level == AccessLevel.Denied) {
//			JOptionPane.showMessageDialog(this, "<html>You Don't Have Access To:<br>" +
//					"Table: " + model.getTableName() +
//					"<br>Category: " + category, "Access Denied", JOptionPane.WARNING_MESSAGE, null);
//			//this.setVisible(false);
//			return false;
//		}
		if (level == AccessLevel.ReadWrite && editable) {
			super.setEditable(true);
		} else {
			super.setEditable(false);
		}
		return true;
	}

	@Override
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		this.editable = editable;
	}	
}
