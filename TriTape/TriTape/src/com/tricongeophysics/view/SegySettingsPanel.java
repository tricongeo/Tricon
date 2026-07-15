package com.tricongeophysics.view;

import com.tricongeophysics.SeismicTrace;
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
    private final boolean editableTextualHeader;

    private final JSpinner traceHeaderBytes;
    private final JSpinner sampleRateOffset;
    private final JSpinner samplesPerTraceOffset;
    private final JSpinner formatCodeOffset;
    private final JSpinner numSamplesThisTraceOffset;
    private final JSpinner coordScalarOffset;
    private final JSpinner elevScalarOffset;
    private final JSpinner outputSamplesPerTraceField; //writer-only; see SegyConfig.outputSamplesPerTrace
    private final HeaderSchemaEditorPanel schemaEditor;
    private final JPanel advancedFields; //the byte-offset rows, shown in a pop-up (see showAdvancedDialog()) rather than inline
    private JDialog advancedDialog; //built lazily on first "Advanced" click, once this panel has a real Window ancestor

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
     *                              if false (input tab), it's a read-only preview. Also
     *                              gates whether the "Output file length (samples)" field
     *                              is shown - it's writer-only, so only relevant for the
     *                              output tab's instance of this panel.
     */
    public SegySettingsPanel(SegyConfig config, Supplier<String> fileHintSupplier,
                              Consumer<SegyHeaderPreview> onHeadersLoaded, boolean editableTextualHeader)
    {
        super(new BorderLayout(4, 4));
        this.config = config;
        this.editableTextualHeader = editableTextualHeader;
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

        // large max (well beyond any plausible byte-offset spinner above) since this holds a sample
        // count, not an offset - continuous-mode recordings can run to very large sample counts
        outputSamplesPerTraceField = new JSpinner(new SpinnerNumberModel(config.outputSamplesPerTrace, 0, 100_000_000, 1));
        outputSamplesPerTraceField.addChangeListener(e -> config.outputSamplesPerTrace = (Integer) outputSamplesPerTraceField.getValue());

        JButton saveButton = new JButton("Save...");
        saveButton.addActionListener(e -> saveSettings());
        JButton loadButton = new JButton("Load...");
        loadButton.addActionListener(e -> loadSettings());
        JButton advancedButton = new JButton("Advanced...");
        advancedButton.setToolTipText("Byte offsets that rarely need to change (trace header length, binary-header field offsets)");
        advancedButton.addActionListener(e -> showAdvancedDialog());
        JPanel saveLoadRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saveLoadRow.add(saveButton);
        saveLoadRow.add(loadButton);
        saveLoadRow.add(advancedButton);

        // these rows rarely need to change day-to-day, so they live in the "Advanced..." pop-up
        // (showAdvancedDialog()) instead of taking up space in the main panel
        advancedFields = new JPanel(new GridLayout(0, 2, 4, 4));
        advancedFields.setBorder(BorderFactory.createTitledBorder("SEG-Y byte offsets (1-based)"));
        addRow(advancedFields, "Trace header length (bytes):", traceHeaderBytes);
        addRow(advancedFields, "Sample rate offset:", sampleRateOffset);
        addRow(advancedFields, "Samples/trace offset:", samplesPerTraceOffset);
        addRow(advancedFields, "Format code offset:", formatCodeOffset);
        addRow(advancedFields, "Samples-this-trace offset:", numSamplesThisTraceOffset);
        addRow(advancedFields, "Coordinate scalar offset:", coordScalarOffset);
        addRow(advancedFields, "Elevation scalar offset:", elevScalarOffset);

        JPanel fieldsAndButtons = new JPanel(new BorderLayout());
        fieldsAndButtons.add(saveLoadRow, BorderLayout.NORTH);

        if (editableTextualHeader)
        {
            // output tab only - every output trace gets padded/truncated to this length (see
            // SegyConfig.outputSamplesPerTrace); the header preview below defaults it to the input
            // file's actual max trace length, but the person can type a smaller value to truncate
            JPanel outputLengthRow = new JPanel(new GridLayout(0, 2, 4, 4));
            outputLengthRow.setBorder(BorderFactory.createTitledBorder(
                "Output trace length (0 = use input file's max automatically)"));
            addRow(outputLengthRow, "Output file length (samples):", outputSamplesPerTraceField);
            fieldsAndButtons.add(outputLengthRow, BorderLayout.CENTER);
        }

        JPanel left = new JPanel(new BorderLayout(4, 4));
        left.add(fieldsAndButtons, BorderLayout.NORTH);
        left.add(headerPreviewPanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, schemaEditor);
        split.setResizeWeight(0.35);
        split.setDividerLocation(360);

        add(split, BorderLayout.CENTER);
    }

    /**
     * Builds (once, lazily - needs a real Window ancestor, which this panel doesn't have yet at
     * construction time) and shows the modeless "Advanced" dialog containing the byte-offset rows
     * moved out of the main panel. Modeless so it can be left open alongside the rest of the UI;
     * edits still write straight through to the SegyConfig via the same spinners/listeners as before.
     */
    private void showAdvancedDialog()
    {
        if (advancedDialog == null)
        {
            Window owner = SwingUtilities.getWindowAncestor(this);
            advancedDialog = new JDialog(owner, "Advanced SEG-Y byte offsets", Dialog.ModalityType.MODELESS);
            advancedDialog.setLayout(new BorderLayout());
            advancedDialog.add(advancedFields, BorderLayout.CENTER);
            advancedDialog.pack();
            advancedDialog.setLocationRelativeTo(this);
        }
        advancedDialog.setVisible(true);
        advancedDialog.toFront();
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
        outputSamplesPerTraceField.setValue(config.outputSamplesPerTrace);
        schemaEditor.refresh();
    }

    /**
     * Called by TraceMonitor (output tab only) after a background scan of the input file finds its
     * max trace length, to fill in the "Output file length" field as a default the person can then
     * edit down (or leave alone) before Submit. Sets the field directly rather than going through
     * refresh()'s full resync, since this can be called from a background-scan completion callback
     * where only this one value is known to have changed. No-op if this instance isn't the output
     * tab's (editableTextualHeader == false), since the field doesn't exist there.
     */
    public void refreshOutputSamplesPerTraceDefault(int maxSamplesInInputFile)
    {
        if (editableTextualHeader)
        {
            outputSamplesPerTraceField.setValue(maxSamplesInInputFile);
        }
    }

    /** shows an already-fetched header preview (e.g. from the input tab) without this panel reading its own file */
    public void showMirroredPreview(SegyHeaderPreview preview, String sourceDescription)
    {
        headerPreviewPanel.showMirroredPreview(preview, sourceDescription);
    }

    /** sets the textual header area's default content programmatically (not counted as a user edit) - see SegyHeaderPreviewPanel.setTextualHeaderDefault() */
    public void setTextualHeaderDefault(String displayText, byte[] rawBytes)
    {
        headerPreviewPanel.setTextualHeaderDefault(displayText, rawBytes);
    }

    /**
     * Populates this panel's own schema table's "Trace 1/2/3" sample-value columns directly, bypassing
     * the header-preview panel's own file-read entirely - used by TraceMonitor to preview the OUTPUT
     * tab with real trace data read via whatever the INPUT format actually is (SEG-D or SEG-Y; the
     * header-preview panel's own readSampleTraces() only understands SEG-Y, so it can't be reused for
     * SEG-D input - see TraceMonitor.syncOutputSampleTracesFromInput()). The schema table looks values
     * up by NAME (HeaderSchemaTableModel.sampleValueText() -> SeismicTrace.getHeaderValue()), the
     * exact same mechanism SegyWriter itself uses at actual write time, so this shows precisely what
     * each output field would actually resolve to for these traces.
     */
    public void setSampleTraces(SeismicTrace[] traces)
    {
        schemaEditor.setSampleTraces(traces);
    }

    /**
     * Re-reads the header preview for whatever file is currently in the bound file field (input or
     * output, depending on which instance this is) - called by TraceMonitor whenever that file field
     * or the corresponding format combo changes, since there's no "Load Headers" button anymore to
     * trigger this manually. No-op if the file field is currently empty.
     */
    public void reloadHeaderPreview()
    {
        headerPreviewPanel.reloadIfFileSet();
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
