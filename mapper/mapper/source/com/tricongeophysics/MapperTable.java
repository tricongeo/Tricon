package com.tricongeophysics;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class MapperTable extends JTable
{
    private static final Color ErrorColor = new Color(255, 200, 200); //pink
    private static final Color WarningColor = new Color(255, 255, 150); //light yellow
    private ArrayList<TableData> tds;

    public MapperTable (TableModel model) {
        super(model);
        setModel(model);
    }
    
    public MapperTable()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void setModel(TableModel model) {
        super.setModel(model);
        if (model == null) return;
        if (!(model instanceof ReflectiveTableModel)) return;
        ReflectiveTableModel rtm = (ReflectiveTableModel) model;
        tds = rtm.getTableData();
    }
    
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        if (renderer == null) return null;
        Component c = super.prepareRenderer(renderer, row, column);
        if (tds == null) return c;
        int modelRow = this.convertRowIndexToModel(row);
        TableData td = tds.get(modelRow);
        if (this.isCellSelected(row, column)) return c;
        if (td instanceof Station) {
            Station station = (Station) td;
            if (station.unSurveyed || station.duplicate) {
                c.setBackground(ErrorColor);
                //c.setForeground(Color.red);
            }
            else if (station.unUsed) {
                c.setBackground(WarningColor);
            }
            else {
                c.setBackground(this.getBackground());
            }
        }
        if (td instanceof OBRecord) {
            OBRecord obr = (OBRecord) td;
            if (obr.getDuplicateShot() || obr.getDuplicateShotPoint() ) {
                c.setBackground(ErrorColor);
            }
            else {
                c.setBackground(this.getBackground());
            }
        }
        //c
        return c;
    }
}
