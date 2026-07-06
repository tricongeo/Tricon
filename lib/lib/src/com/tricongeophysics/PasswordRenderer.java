package com.tricongeophysics;

import java.awt.Component;

import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Element;
import javax.swing.text.PasswordView;

public class PasswordRenderer extends DefaultTableCellRenderer {

	private JPasswordField passwordView;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (passwordView == null) passwordView = new JPasswordField();
		passwordView.setText(value+"");
		return passwordView;
	}

}
