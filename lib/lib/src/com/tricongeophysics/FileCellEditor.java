package com.tricongeophysics;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

public class FileCellEditor extends DefaultCellEditor implements  MouseListener {

	private Component textField;
	private Desktop desktop;
	private ArrayList<MouseListener> mouseListeners = new ArrayList<MouseListener>();

	public FileCellEditor() {
		super(new JTextField());
		textField = this.getComponent();
		textField.addMouseListener(this);
		this.setClickCountToStart(2);
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() > 1){
			String val = ((JTextField)textField).getText();
			try {
				File file = new File(val);
				desktop.open(file);
				return;
			} catch (Exception e2) {
				try {
					URI uri = new URI("file://" + val);
					desktop.browse(uri);
					return;
				} catch (Exception e3) {}
			}
			try {
				String command = "firefox file://" + val.replace(" ", "%20");
				Process pc = Runtime.getRuntime().exec(command); 
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(textField, "<html>Error opening file:<br>" + ex, "File Open Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		fireMouseClicked(e);
	}

	private void fireMouseClicked(MouseEvent e) {
		for (MouseListener l: mouseListeners) {
			l.mouseClicked(e);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void addMouseListener(MouseListener l) {
		mouseListeners.add(l);
	}
}
