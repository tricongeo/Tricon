package com.tricongeophysics.view;

import com.tricongeophysics.SeismicTrace;
import com.tricongeophysics.model.HeaderFieldDef;
import com.tricongeophysics.model.HeaderSchema;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable editor for a HeaderSchema: an add/remove-able JTable where each
 * row is one trace-header field (name, byte offset, numeric type, and
 * SEG-Y-only scalar handling). Embedded directly inside SegySettingsPanel and
 * SegdSettingsPanel, which are shown inline in TraceMonitor's input/output
 * panes.
 */
public class HeaderSchemaEditorPanel extends JPanel
{
    private final HeaderSchemaTableModel model;
    private final JTable table;

    public HeaderSchemaEditorPanel(HeaderSchema schema)
    {
        super(new BorderLayout(4, 4));
        model = new HeaderSchemaTableModel(schema);
        table = new JTable(model);
        table.setRowHeight(22);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // let columns keep their own width; scroll horizontally instead of squeezing them all into the available space
        table.getColumnModel().getColumn(2).setCellEditor(
            new DefaultCellEditor(new JComboBox<HeaderFieldDef.FieldType>(HeaderFieldDef.FieldType.values())));
        table.getColumnModel().getColumn(3).setCellEditor(
            new DefaultCellEditor(new JComboBox<HeaderFieldDef.ScalarType>(HeaderFieldDef.ScalarType.values())));
        setColumnWidths();

        add(new JLabel("Trace header field mapping (name, 1-based byte offset within the trace header, type, scalar, scale divisor):"),
            BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addButton = new JButton("Add Field");
        JButton removeButton = new JButton("Remove Selected");
        addButton.addActionListener(e -> model.addRow());
        removeButton.addActionListener(e ->
        {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            model.removeRow(table.getSelectedRow());
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addButton);
        buttons.add(removeButton);
        add(buttons, BorderLayout.SOUTH);
    }

    private void setColumnWidths()
    {
        int[] widths = {130, 130, 90, 130, 90, 90, 90, 90}; // name, offset, type, scalar, scale, trace1, trace2, trace3
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++)
        {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /** commits any in-progress cell edit (call before reading the schema back out) */
    public void commitEdits()
    {
        if (table.isEditing())
        {
            table.getCellEditor().stopCellEditing();
        }
    }

    /** re-renders the table from the underlying schema (e.g. after switching format cards) */
    public void refresh()
    {
        model.fireTableDataChanged();
    }

    /** populates the "Trace 1/2/3" sample-value columns from a freshly-read batch of traces */
    public void setSampleTraces(SeismicTrace[] traces)
    {
        model.setSampleTraces(traces);
    }
}
