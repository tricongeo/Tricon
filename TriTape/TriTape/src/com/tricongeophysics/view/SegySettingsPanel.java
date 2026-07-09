package com.tricongeophysics.view;

import com.tricongeophysics.model.ConfigXmlIO;
import com.tricongeophysics.model.SegyConfig;
import com.tricongeophysics.model.SegyHeaderPreview;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
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
 * All byte offsets are shown and edited as 1-based (byte 1 = the first byte)
 * since that's how most people think and talk about byte positions; the
 * SegyConfig/HeaderFieldDef objects underneath, and the file format itself,
 * still use 0-based offsets - the +1/-1 conversion happens only at this UI
 * boundary. Settings (offsets + trace-header field mapping) can be saved to
 * or loaded from an XML file via the buttons above the offset fields.
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
        this(config, fileHintSupplier, preview -> { });
    }

    public SegySettingsPanel(SegyConfig config, Supplier<String> fileHintSupplier, Consumer<SegyHeaderPreview> onHeadersLoaded)
    {
        this(config, fileHintSupplier, onHeadersLoaded, false);
    }

    /**
     * @param editableTextualHeader if true (used for the output tab), the header
     *                              preview's textual header text area is user-editable
     *                              and getEffectiveTextualHeaderRaw() will reflect edits;
     *                              if false (input tab), it's a read-only preview.
     */
    public SegySettingsPanel(SegyConfig config, Supplier<String> fileHintSupplier,
                              Consumer<SegyHeaderPreview> onHeadersLoaded, boolean editableTextualHeader)
    {
        super(new BorderLayout(4, 4));
        this.config = config;
        this.schemaEditor = new HeaderSchemaEditorPanel(config.traceHeaderSchema);
        this.headerPreviewPanel = new SegyHeaderPreviewPanel(config, fileHintSupplier,
            traces -> schemaEditor.setSampleTraces(traces), onHeadersLoaded, editableTextualHeader);

        traceHeaderBytes = boundSpinner(config.traceHeaderBytes, v -> config.traceHeaderBytes = v, false);
        sampleRateOffset = boundSpinner(config.sampleRateByteOffset, v -> config.sampleRateByteOffset = v, true);
        samplesPerTraceOffset = boundSpinner(config.samplesPerTraceByteOffset, v -> config.samplesPerTraceByteOffset = v, true);
        formatCodeOffset = boundSpinner(config.formatCodeByteOffset, v -> config.formatCodeByteOffset = v, true);
        numSamplesThisTraceOffset = boundSpinner(config.numSamplesThisTraceByteOffset, v -> config.numSamplesThisTraceByteOffset = v, true);
        coordScalarOffset = boundSpinner(config.coordinateScalarByteOffset, v -> config.coordinateScalarByteOffset = v, true);
        elevScalarOffset = boundSpinner(config.elevationScalarByteOffset, v -> config.elevationScalarByteOffset = v, true);

        JButton saveButton = new JButton("Save Settings...");
        saveButton.addActionListener(e -> saveSettings());
        JButton loadButton = new JButton("Load Settings...");
        loadButton.addActionListener(e -> loadSettings());
        JPanel saveLoadRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saveLoadRow.add(saveButton);
        saveLoadRow.add(loadButton);

        JPanel fields = new JPanel(new GridLayout(0, 2, 4, 4));
        fields.setBorder(BorderFactory.createTitledBorder("SEG-Y byte offsets (1-based)"));
        addRow(fields, "Trace header length (bytes):", traceHeaderBytes);
        addRow(fields, "Sample rate offset:", sampleRateOffset);
        addRow(fields, "Samples/trace offset:", samplesPerTraceOffset);
        addRow(fields, "Format code offset:", formatCodeOffset);
        addRow(fields, "Samples-this-trace offset:", numSamplesThisTraceOffset);
        addRow(fields, "Coordinate scalar offset:", coordScalarOffset);
        addRow(fields, "Elevation scalar offset:", elevScalarOffset);

        JPanel fieldsAndButtons = new JPanel(new BorderLayout());
        fieldsAndButtons.add(saveLoadRow, BorderLayout.NORTH);
        fieldsAndButtons.add(fields, BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout(4, 4));
        left.add(fieldsAndButtons, BorderLayout.NORTH);
        left.add(headerPreviewPanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, schemaEditor);
        split.setResizeWeight(0.5);

        add(split, BorderLayout.CENTER);
    }

    private void saveSettings()
    {
        commitEdits();
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("XML files", "xml"));
        chooser.setSelectedFile(new File("segy-settings.xml"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = withXmlExtension(chooser.getSelectedFile());
        try
        {
            ConfigXmlIO.saveSegyConfig(config, file);
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Failed to save settings:\n" + ex.getMessage(),
                "Save Settings", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSettings()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("XML files", "xml"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try
        {
            SegyConfig loaded = ConfigXmlIO.loadSegyConfig(chooser.getSelectedFile());
            config.copyFrom(loaded);
            refresh();
            headerPreviewPanel.reloadIfFileSet();
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Failed to load settings:\n" + ex.getMessage(),
                "Load Settings", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static File withXmlExtension(File file)
    {
        if (file.getName().toLowerCase().endsWith(".xml")) return file;
        return new File(file.getParentFile(), file.getName() + ".xml");
    }

    private interface IntSetter { void set(int value); }

    /** binds a spinner to a config field; if isOffset, the spinner shows/accepts 1-based values (min 1) */
    private JSpinner boundSpinner(int initial, IntSetter setter, boolean isOffset)
    {
        int display = isOffset ? initial + 1 : initial;
        int min = isOffset ? 1 : 0;
        int max = isOffset ? 65536 : 65535;
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(display, min, max, 1));
        spinner.addChangeListener(e ->
        {
            int shown = (Integer) spinner.getValue();
            setter.set(isOffset ? shown - 1 : shown);
            headerPreviewPanel.reloadIfFileSet();
        });
        return spinner;
    }

    private static void addRow(JPanel panel, String label, JComponent field)
    {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    /** re-reads the bound config's current values into the UI (e.g. after switching format cards or loading settings) */
    public void refresh()
    {
        traceHeaderBytes.setValue(config.traceHeaderBytes);
        sampleRateOffset.setValue(config.sampleRateByteOffset + 1);
        samplesPerTraceOffset.setValue(config.samplesPerTraceByteOffset + 1);
        formatCodeOffset.setValue(config.formatCodeByteOffset + 1);
        numSamplesThisTraceOffset.setValue(config.numSamplesThisTraceByteOffset + 1);
        coordScalarOffset.setValue(config.coordinateScalarByteOffset + 1);
        elevScalarOffset.setValue(config.elevationScalarByteOffset + 1);
        schemaEditor.refresh();
    }

    /** shows an already-fetched header preview (e.g. from the input tab) without this panel reading its own file */
    public void showMirroredPreview(SegyHeaderPreview preview, String sourceDescription)
    {
        headerPreviewPanel.showMirroredPreview(preview, sourceDescription);
    }

    /** the textual header bytes to actually write: the user's edits if any, otherwise the loaded/mirrored default, or null if neither has happened yet */
    public byte[] getEffectiveTextualHeaderRaw()
    {
        return headerPreviewPanel.getEffectiveTextualHeaderRaw();
    }

    /** commits any in-progress trace-header-schema table cell edit */
    public void commitEdits()
    {
        schemaEditor.commitEdits();
    }
}
