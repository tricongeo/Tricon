package com.tricongeophysics.view;

import com.tricongeophysics.model.SegdConfig;

import javax.swing.*;
import java.awt.*;

/**
 * Inline (non-dialog) editor for a SegdConfig: the key general-header/
 * channel-set-descriptor byte offsets plus the full trace-header field
 * mapping, embedded directly in TraceMonitor's input/output panes. All edits
 * write straight through to the SegdConfig instance passed in (no OK/Cancel).
 *
 * The reader and writer each have their own independent SegdConfig, so each
 * pane gets its own SegdSettingsPanel instance bound to its own config - no
 * cross-panel syncing needed.
 */
public class SegdSettingsPanel extends JPanel
{
    private final SegdConfig config;

    private final JSpinner traceHeaderBytes;
    private final JSpinner channelSetsOffset;
    private final JSpinner additionalHeaderBlocksOffset;
    private final JSpinner baseScanIntervalOffset;
    private final JSpinner traceHeaderExtCountOffset;
    private final JSpinner samplesFieldOffset;
    private final HeaderSchemaEditorPanel schemaEditor;

    public SegdSettingsPanel(SegdConfig config)
    {
        super(new BorderLayout(4, 4));
        this.config = config;

        traceHeaderBytes = boundSpinner(config.traceHeaderBytes, v -> config.traceHeaderBytes = v);
        channelSetsOffset = boundSpinner(config.channelSetsPerScanTypeByteOffset, v -> config.channelSetsPerScanTypeByteOffset = v);
        additionalHeaderBlocksOffset = boundSpinner(config.additionalGeneralHeaderBlocksByteOffset, v -> config.additionalGeneralHeaderBlocksByteOffset = v);
        baseScanIntervalOffset = boundSpinner(config.baseScanIntervalByteOffset, v -> config.baseScanIntervalByteOffset = v);
        traceHeaderExtCountOffset = boundSpinner(config.traceHeaderExtensionCountByteOffset, v -> config.traceHeaderExtensionCountByteOffset = v);
        samplesFieldOffset = boundSpinner(config.samplesFieldByteOffsetInChannelSetDescriptor, v -> config.samplesFieldByteOffsetInChannelSetDescriptor = v);

        JPanel fields = new JPanel(new GridLayout(0, 2, 4, 4));
        fields.setBorder(BorderFactory.createTitledBorder("SEG-D byte offsets (Gen. Header block 1, unless noted)"));
        addRow(fields, "Trace header length (bytes):", traceHeaderBytes);
        addRow(fields, "Channel sets/scan type offset:", channelSetsOffset);
        addRow(fields, "Additional header blocks offset:", additionalHeaderBlocksOffset);
        addRow(fields, "Base scan interval offset:", baseScanIntervalOffset);
        addRow(fields, "Trace header ext. count offset:", traceHeaderExtCountOffset);
        addRow(fields, "Samples field offset (chan. set desc.):", samplesFieldOffset);

        schemaEditor = new HeaderSchemaEditorPanel(config.traceHeaderSchema);

        add(fields, BorderLayout.NORTH);
        add(schemaEditor, BorderLayout.CENTER);
    }

    private interface IntSetter { void set(int value); }

    private JSpinner boundSpinner(int initial, IntSetter setter)
    {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initial, 0, 65535, 1));
        spinner.addChangeListener(e -> setter.set((Integer) spinner.getValue()));
        return spinner;
    }

    private static void addRow(JPanel panel, String label, JComponent field)
    {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    /** re-reads the bound config's current values into the UI (e.g. after switching format cards) */
    public void refresh()
    {
        traceHeaderBytes.setValue(config.traceHeaderBytes);
        channelSetsOffset.setValue(config.channelSetsPerScanTypeByteOffset);
        additionalHeaderBlocksOffset.setValue(config.additionalGeneralHeaderBlocksByteOffset);
        baseScanIntervalOffset.setValue(config.baseScanIntervalByteOffset);
        traceHeaderExtCountOffset.setValue(config.traceHeaderExtensionCountByteOffset);
        samplesFieldOffset.setValue(config.samplesFieldByteOffsetInChannelSetDescriptor);
        schemaEditor.refresh();
    }

    /** commits any in-progress trace-header-schema table cell edit */
    public void commitEdits()
    {
        schemaEditor.commitEdits();
    }
}
