package com.tricongeophysics;

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

class DoubleRenderer extends DefaultTableCellRenderer {
	NumberFormat formatter;
	public DoubleRenderer() { 
		super();
		this.setHorizontalAlignment(RIGHT);
	}

	public void setValue(Object value) {
        if (formatter==null) {
            formatter = new DecimalFormat("#,###,###,##0.00");
        }
        if (value instanceof Number)
        	setText((value == null) ? "" : formatter.format(value));
        else
        	setText("NaN");
    }
}
