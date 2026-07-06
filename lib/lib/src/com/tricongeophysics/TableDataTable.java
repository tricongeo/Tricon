package com.tricongeophysics;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.TableCellRenderer;

public class TableDataTable extends JTable
{
    public static final Color ErrorColor = new Color(255, 200, 200); //pink
    public static final Color WarningColor = new Color(255, 255, 150); //light yellow
	private AbstractSpreadsheetModel asm;

    public TableDataTable (TableModel model) {
        super(model);
        setModel(model);
    }
    
    public TableDataTable()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void setModel(TableModel model) {
        super.setModel(model);
        if (model == null) return;
        if (!(model instanceof AbstractSpreadsheetModel)) return;
        asm = (AbstractSpreadsheetModel) model;
    }
    
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        if (renderer == null) return null;
        Component c = super.prepareRenderer(renderer, row, column);
        int modelRow = this.convertRowIndexToModel(row);
        if (this.isCellSelected(row, column)) return c;
        if (asm.containsError(modelRow)) {
            c.setBackground(ErrorColor);
            //c.repaint(); //for some reason, repaint() doesn't work???
            return c;
        }
        else if (asm.containsWarning(modelRow)) {
            c.setBackground(WarningColor);
            //c.repaint();//for some reason, repaint() doesn't work???
            return c;
        }
        else {
            c.setBackground(getBackground());
            //c.repaint();//for some reason, repaint() doesn't work???
            return c;
        }
    }
}
