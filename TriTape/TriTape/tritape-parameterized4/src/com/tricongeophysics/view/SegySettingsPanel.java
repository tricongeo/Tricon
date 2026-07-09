package com.tricongeophysics.view;

import com.tricongeophysics.model.SegyConfig;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * Inline (non-dialog) editor for a SegyConfig: the key binary/trace-header
 * byte offsets, a live preview of the actual textual/binary header contents
 * of a chosen file (SegyHeaderPreviewPanel), and the full trace-header field
 * mapping - all embedded directly in one of TraceMonitor's Input/Output tabs.
 * All edits write straight through to the SegyConfig instance passed in (no
 * OK/Cancel) so the reader/writer always see the latest values, and changing
 * a byte offset automatically reloads the header preview if a file is loaded.
 *
 * The reader and writer each have their own independent SegyConfig, so each
 * tab gets its own SegySettingsPanel instance bound to its own config - no
 * cross-panel syncing needed.
 */
public class SegySettingsPanel extends JPanel
{
    private final SegyConfig config;
    private final SegyHeaderPreviewPanel headerPreviewPanel;

    private final JSpinner traceHeaderBytes;
    private final JSpinner sampleRateOffset;
    private final JSpinner samplesPerTraceOffset;
    private final JSpinner formatCodeOffset;
    private final JSpinner numSamplesThisTraceOffset;
    private final JSpinner coordScalarOffset;
    private final JSpinner elevScalarOffset;
    private final HeaderSchemaEditorPanel schemaEditor;

    public SegySettingsPanel(SegyConfig config)
    {
        this(config, () -> "");
    }

    public SegySettingsPanel(SegyConfig config, Supplier<String> fileHintSupplier)
    {
        super(new BorderLayout(4, 4));
        this.config = config;
        this.schemaEditor = new HeaderSchemaEditorPanel(config.traceHeaderSchema);
        this.headerPreviewPanel = new SegyHeaderPreviewPanel(config, fileHintSupplier, traces -> schemaEditor.setSampleTraces(traces));

        traceHeaderBytes = boundSpinner(config.traceHeaderBytes, v -> config.traceHeaderBytes = v);
        sampleRateOffset = boundSpinner(config.sampleRateByteOffset, v -> config.sampleRateByteOffset = v);
        samplesPerTraceOffset = boundSpinner(config.samplesPerTraceByteOffset, v -> config.samplesPerTraceByteOffset = v);
        formatCodeOffset = boundSpinner(config.formatCodeByteOffset, v -> config.formatCodeByteOffset = v);
        numSamplesThisTraceOffset = boundSpinner(config.numSamplesThisTraceByteOffset, v -> config.numSamplesThisTraceByteOffset = v);
        coordScalarOffset = boundSpinner(config.coordinateScalarByteOffset, v -> config.coordinateScalarByteOffset = v);
        elevScalarOffset = boundSpinner(config.elevationScalarByteOffset, v -> config.elevationScalarByteOffset = v);

        JPanel fields = new JPanel(new GridLayout(0, 2, 4, 4));
        fields.setBorder(BorderFactory.createTitledBorder("SEG-Y byte offsets"));
        addRow(fields, "Trace header length (bytes):", traceHeaderBytes);
        addRow(fields, "Sample rate offset:", sampleRateOffset);
        addRow(fields, "Samples/trace offset:", samplesPerTraceOffset);
        addRow(fields, "Format code offset:", formatCodeOffset);
        addRow(fields, "Samples-this-trace offset:", numSamplesThisTraceOffset);
        addRow(fields, "Coordinate scalar offset:", coordScalarOffset);
        addRow(fields, "Elevation scalar offset:", elevScalarOffset);

        JPanel left = new JPanel(new BorderLayout(4, 4));
        left.add(fields, BorderLayout.NORTH);
        left.add(headerPreviewPanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, schemaEditor);
        split.setResizeWeight(0.5);

        add(split, BorderLayout.CENTER);
    }

    private interface IntSetter { void set(int value); }

    private JSpinner boundSpinner(int initial, IntSetter setter)
    {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initial, 0, 65535, 1));
        spinner.addChangeListener(e ->
        {
            setter.set((Integer) spinner.getValue());
            headerPreviewPanel.reloadIfFileSet();
        });
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
        sampleRateOffset.setValue(config.sampleRateByteOffset);
        samplesPerTraceOffset.setValue(config.samplesPerTraceByteOffset);
        formatCodeOffset.setValue(config.formatCodeByteOffset);
        numSamplesThisTraceOffset.setValue(config.numSamplesThisTraceByteOffset);
        coordScalarOffset.setValue(config.coordinateScalarByteOffset);
        elevScalarOffset.setValue(config.elevationScalarByteOffset);
        schemaEditor.refresh();
    }

    /** commits any in-progress trace-header-schema table cell edit */
    public void commitEdits()
    {
        schemaEditor.commitEdits();
    }
}
