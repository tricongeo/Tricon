package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.tricongeophysics.TableData.ColumnType;

public class TableHeaderRenderer implements TableCellRenderer
{

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        int modelColumn = table.convertColumnIndexToModel(column);
        Icon icon = SUtil.createImageIcon(TableHeaderRenderer.class, "images/sort_none.png");
        //if (table.isColumnSelected(column)) {
            RowSorter<? extends TableModel> sorter = table.getRowSorter();
            if (sorter != null) {
                List<? extends SortKey> keys = sorter.getSortKeys();
                if (keys != null && keys.size() > 0) {
                    if(keys.get(0).getColumn() == modelColumn) {
                        SortOrder order = keys.get(0).getSortOrder();
                        if (order == SortOrder.ASCENDING) icon = SUtil.createImageIcon(this.getClass(), "images/sort_down.png");
                        else icon = SUtil.createImageIcon(this.getClass(), "images/sort_up.png");
                    }
                }
            }
      // }
        JLabel label = new JLabel(""+value);
        label.setHorizontalAlignment(JLabel.CENTER);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(label, BorderLayout.CENTER);
        panel.add(new JLabel(icon), BorderLayout.EAST);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.gray));
        AbstractSpreadsheetModel rtm = getRTM(table);
        panel.setBackground(getBackground(rtm, modelColumn));
        label.setEnabled(getEnabled(rtm, modelColumn));
        if (column == 0) panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
        //label.setHorizontalTextPosition(JLabel.LEFT);
        panel.setToolTipText("click to sort column: "+table.getColumnName(column));
        return panel;
    }

    private boolean getEnabled(AbstractSpreadsheetModel rtm, int modelColumn)
    {
        return rtm.isColEditable(modelColumn);
    }

    private AbstractSpreadsheetModel getRTM(JTable table)
    {
        if (table == null) return null;
        return (AbstractSpreadsheetModel) table.getModel();
    }

    private Color getBackground(AbstractSpreadsheetModel rtm, int modelColumn)
    {
        Color defaultColor = UIManager.getColor("TableHeader.background");
        if (rtm == null ) return defaultColor;
        ColumnType type = rtm.getColumnType(modelColumn);
        if (type == ColumnType.Standard) return defaultColor;
        if (type == ColumnType.Error) return TableDataTable.ErrorColor;
        if (type == ColumnType.Warning) return TableDataTable.WarningColor;
        return defaultColor;
    }

}
