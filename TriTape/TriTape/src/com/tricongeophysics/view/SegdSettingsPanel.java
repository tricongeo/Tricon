package com.tricongeophysics.view;

import com.tricongeophysics.model.ConfigXmlIO;
import com.tricongeophysics.model.SegdConfig;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Inline (non-dialog) editor for a SegdConfig: the key general-header/
 * channel-set-descriptor byte offsets plus the full trace-header field
 * mapping, embedded directly in TraceMonitor's input/output panes. All edits
 * write straight through to the SegdConfig instance passed in (no OK/Cancel).
 *
 * All byte offsets are shown and edited as 1-based (byte 1 = the first byte);
 * the SegdConfig/HeaderFieldDef objects underneath, and the file format
 * itself, still use 0-based offsets - the +1/-1 conversion happens only at
 * this UI boundary. Settings (offsets + trace-header field mapping) can be
 * saved to or loaded from an XML file via the buttons above the offset fields.
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
        this.schemaEditor = new HeaderSchemaEditorPanel(config.traceHeaderSchema);

        traceHeaderBytes = boundSpinner(config.traceHeaderBytes, v -> config.traceHeaderBytes = v, false);
        channelSetsOffset = boundSpinner(config.channelSetsPerScanTypeByteOffset, v -> config.channelSetsPerScanTypeByteOffset = v, true);
        additionalHeaderBlocksOffset = boundSpinner(config.additionalGeneralHeaderBlocksByteOffset, v -> config.additionalGeneralHeaderBlocksByteOffset = v, true);
        baseScanIntervalOffset = boundSpinner(config.baseScanIntervalByteOffset, v -> config.baseScanIntervalByteOffset = v, true);
        traceHeaderExtCountOffset = boundSpinner(config.traceHeaderExtensionCountByteOffset, v -> config.traceHeaderExtensionCountByteOffset = v, true);
        samplesFieldOffset = boundSpinner(config.samplesFieldByteOffsetInChannelSetDescriptor, v -> config.samplesFieldByteOffsetInChannelSetDescriptor = v, true);

        JButton saveButton = new JButton("Save Settings...");
        saveButton.addActionListener(e -> saveSettings());
        JButton loadButton = new JButton("Load Settings...");
        loadButton.addActionListener(e -> loadSettings());
        JPanel saveLoadRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saveLoadRow.add(saveButton);
        saveLoadRow.add(loadButton);

        JPanel fields = new JPanel(new GridLayout(0, 2, 4, 4));
        fields.setBorder(BorderFactory.createTitledBorder("SEG-D byte offsets (1-based; Gen. Header block 1, unless noted)"));
        addRow(fields, "Trace header length (bytes):", traceHeaderBytes);
        addRow(fields, "Channel sets/scan type offset:", channelSetsOffset);
        addRow(fields, "Additional header blocks offset:", additionalHeaderBlocksOffset);
        addRow(fields, "Base scan interval offset:", baseScanIntervalOffset);
        addRow(fields, "Trace header ext. count offset:", traceHeaderExtCountOffset);
        addRow(fields, "Samples field offset (chan. set desc.):", samplesFieldOffset);

        JPanel top = new JPanel(new BorderLayout());
        top.add(saveLoadRow, BorderLayout.NORTH);
        top.add(fields, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(schemaEditor, BorderLayout.CENTER);
    }

    private void saveSettings()
    {
        commitEdits();
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("XML files", "xml"));
        chooser.setSelectedFile(new File("segd-settings.xml"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = withXmlExtension(chooser.getSelectedFile());
        try
        {
            ConfigXmlIO.saveSegdConfig(config, file);
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
            SegdConfig loaded = ConfigXmlIO.loadSegdConfig(chooser.getSelectedFile());
            config.copyFrom(loaded);
            refresh();
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
        channelSetsOffset.setValue(config.channelSetsPerScanTypeByteOffset + 1);
        additionalHeaderBlocksOffset.setValue(config.additionalGeneralHeaderBlocksByteOffset + 1);
        baseScanIntervalOffset.setValue(config.baseScanIntervalByteOffset + 1);
        traceHeaderExtCountOffset.setValue(config.traceHeaderExtensionCountByteOffset + 1);
        samplesFieldOffset.setValue(config.samplesFieldByteOffsetInChannelSetDescriptor + 1);
        schemaEditor.refresh();
    }

    /** commits any in-progress trace-header-schema table cell edit */
    public void commitEdits()
    {
        schemaEditor.commitEdits();
    }
}
