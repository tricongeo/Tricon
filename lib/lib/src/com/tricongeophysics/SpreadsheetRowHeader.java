package com.tricongeophysics;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;


public class SpreadsheetRowHeader extends JTable {
	   TableCellRenderer render = new RowHeaderRenderer();

	   public SpreadsheetRowHeader(JTable table) {
	      super(new RowHeaderModel(table));
	      configure(table);
	      setEnabled(false);
	   }
	   protected void configure(JTable table) {
	      setRowHeight(table.getRowHeight());
	      setIntercellSpacing(new Dimension(0,0));
	      setShowHorizontalLines(false);
	      setShowVerticalLines(false);
	   }

	   public Dimension getPreferredScrollableViewportSize() {
	      return new Dimension(55, super.getPreferredSize().height); //need to accomodate higher row numbers
	   }
	   public TableCellRenderer getDefaultRenderer(Class c) {
	      return render;
	   }

	   static class RowHeaderModel extends AbstractTableModel {
	      JTable table;
	      protected RowHeaderModel(JTable tableToMirror) {
	         table = tableToMirror;
	      }
	      public int getRowCount() {
	         return table.getModel().getRowCount();
	      }
	      public int getColumnCount() {
	         return 1;
	      }
	      public Object getValueAt(int row, int column) {
	         return String.valueOf(row+1);
	      }
	   }
	   static class RowHeaderRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelect, boolean hasFocus, int row, int column)
        {
	         if (isSelect) {
                 setForeground(table.getSelectionForeground());
                 setBackground(table.getSelectionBackground());
             }
             else {
                 setBackground(UIManager.getColor("TableHeader.background"));
                 setForeground(UIManager.getColor("TableHeader.foreground"));
             }
	         setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	         setFont(UIManager.getFont("TableHeader.font"));
	         setValue(value);
	         return this;
	      }
	   }
	}
