package com.tricongeophysics.view;

import com.tricongeophysics.model.ConfigXmlIO;
import com.tricongeophysics.model.SegdConfig;
import com.tricongeophysics.model.SegdVersion;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Inline (non-dialog) editor for a SegdConfig: which SEG-D revision to assume
 * (version), the key general-header/trace-header byte offsets for that
 * revision, a diagnostic header preview (SegdHeaderPreviewPanel), and the
 * full trace-header field mapping - embedded directly in TraceMonitor's
 * input/output panes. All edits write straight through to the SegdConfig
 * instance passed in (no OK/Cancel), and changing a byte offset automatically
 * reloads the header preview if a file is loaded.
 *
 * SEG-D varies significantly between revisions. REV3_1's offsets here follow
 * Sercel's "Nodal Data Format Manual" (SEG-D Rev 3.1) rather than a generic
 * assumption, and its fields are genuinely different (not just relocated)
 * from REV1_REV2's - see SegdBufferedFileReader's class javadoc for exactly
 * what each mode reads and from where. The version combo box switches which
 * set of offset fields is shown/used. If a file reads garbage/NaN samples,
 * use the header preview panel's raw hex dumps to check these offsets
 * against the actual file rather than assuming the defaults are right.
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
    private final SegdHeaderPreviewPanel headerPreviewPanel;

    private final JComboBox<SegdVersion> versionCombo = new JComboBox<SegdVersion>(SegdVersion.values());
    private final JSpinner traceHeaderBytes;
    private final JSpinner channelSetsOffset;
    private final JSpinner additionalHeaderBlocksOffset;
    private final JSpinner baseScanIntervalOffset;
    private final JSpinner traceHeaderExtCountOffset;
    private final JSpinner samplesFieldOffset;
    private final JSpinner extendedHeaderBlocksOffset2;
    private final JSpinner externalHeaderBlocksOffset2;
    private final JSpinner rev3AdditionalBlocksCountOffset;
    private final JSpinner rev3DominantSamplingIntervalOffset;
    private final JSpinner rev3ExtendedHeaderBlocksOffset;
    private final JSpinner rev3ExternalHeaderBlocksOffset;
    private final JSpinner rev3HeaderSizeOffset;
    private final JSpinner rev3TraceHeaderExtCountOffset;
    private final JSpinner rev3NumSamplesOffset;
    private final HeaderSchemaEditorPanel schemaEditor;

    private final CardLayout versionCardLayout = new CardLayout();
    private final JPanel versionCards = new JPanel(versionCardLayout);

    public SegdSettingsPanel(SegdConfig config)
    {
        this(config, () -> "");
    }

    public SegdSettingsPanel(SegdConfig config, Supplier<String> fileHintSupplier)
    {
        super(new BorderLayout(4, 4));
        this.config = config;
        this.schemaEditor = new HeaderSchemaEditorPanel(config.traceHeaderSchema);
        this.headerPreviewPanel = new SegdHeaderPreviewPanel(config, fileHintSupplier);

        traceHeaderBytes = boundSpinner(config.traceHeaderBytes, v -> config.traceHeaderBytes = v, false);
        channelSetsOffset = boundSpinner(config.channelSetsPerScanTypeByteOffset, v -> config.channelSetsPerScanTypeByteOffset = v, true);
        additionalHeaderBlocksOffset = boundSpinner(config.additionalGeneralHeaderBlocksByteOffset, v -> config.additionalGeneralHeaderBlocksByteOffset = v, true);
        baseScanIntervalOffset = boundSpinner(config.baseScanIntervalByteOffset, v -> config.baseScanIntervalByteOffset = v, true);
        traceHeaderExtCountOffset = boundSpinner(config.traceHeaderExtensionCountByteOffset, v -> config.traceHeaderExtensionCountByteOffset = v, true);
        samplesFieldOffset = boundSpinner(config.samplesFieldByteOffsetInChannelSetDescriptor, v -> config.samplesFieldByteOffsetInChannelSetDescriptor = v, true);
        extendedHeaderBlocksOffset2 = boundSpinner(config.extendedHeaderBlocksByteOffsetInHeader2, v -> config.extendedHeaderBlocksByteOffsetInHeader2 = v, true);
        externalHeaderBlocksOffset2 = boundSpinner(config.externalHeaderBlocksByteOffsetInHeader2, v -> config.externalHeaderBlocksByteOffsetInHeader2 = v, true);
        rev3AdditionalBlocksCountOffset = boundSpinner(config.rev3AdditionalBlocksCountByteOffsetInHeader2, v -> config.rev3AdditionalBlocksCountByteOffsetInHeader2 = v, true);
        rev3DominantSamplingIntervalOffset = boundSpinner(config.rev3DominantSamplingIntervalByteOffsetInHeader2, v -> config.rev3DominantSamplingIntervalByteOffsetInHeader2 = v, true);
        rev3ExtendedHeaderBlocksOffset = boundSpinner(config.rev3ExtendedHeaderBlocksByteOffsetInHeader2, v -> config.rev3ExtendedHeaderBlocksByteOffsetInHeader2 = v, true);
        rev3ExternalHeaderBlocksOffset = boundSpinner(config.rev3ExternalHeaderBlocksByteOffsetInHeader2, v -> config.rev3ExternalHeaderBlocksByteOffsetInHeader2 = v, true);
        rev3HeaderSizeOffset = boundSpinner(config.rev3HeaderSizeByteOffsetInHeader3, v -> config.rev3HeaderSizeByteOffsetInHeader3 = v, true);
        rev3TraceHeaderExtCountOffset = boundSpinner(config.rev3TraceHeaderExtensionCountByteOffset, v -> config.rev3TraceHeaderExtensionCountByteOffset = v, true);
        rev3NumSamplesOffset = boundSpinner(config.rev3NumSamplesByteOffsetInTraceHeaderExt1, v -> config.rev3NumSamplesByteOffsetInTraceHeaderExt1 = v, true);

        JButton saveButton = new JButton("Save Settings...");
        saveButton.addActionListener(e -> saveSettings());
        JButton loadButton = new JButton("Load Settings...");
        loadButton.addActionListener(e -> loadSettings());
        JPanel saveLoadRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saveLoadRow.add(saveButton);
        saveLoadRow.add(loadButton);

        JPanel versionRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        versionRow.add(new JLabel("SEG-D version:"));
        versionCombo.setSelectedItem(config.version);
        versionCombo.addActionListener(e ->
        {
            SegdVersion selected = (SegdVersion) versionCombo.getSelectedItem();
            config.version = selected;
            versionCardLayout.show(versionCards, selected.name());
            headerPreviewPanel.reloadIfFileSet();
        });
        versionRow.add(versionCombo);

        JPanel fields = new JPanel(new GridLayout(0, 2, 4, 4));
        fields.setBorder(BorderFactory.createTitledBorder("SEG-D byte offsets (1-based; General Header block 1)"));
        addRow(fields, "Trace header length (bytes):", traceHeaderBytes);
        addRow(fields, "Channel sets/scan type offset:", channelSetsOffset);
        addRow(fields, "Additional header blocks offset:", additionalHeaderBlocksOffset);
        addRow(fields, "Base scan interval offset:", baseScanIntervalOffset);
        addRow(fields, "Trace header ext. count offset (Rev 1/2):", traceHeaderExtCountOffset);
        addRow(fields, "Samples field offset (chan. set desc., Rev 1/2):", samplesFieldOffset);

        JPanel rev1Rev2Panel = new JPanel(new GridLayout(0, 2, 4, 4));
        rev1Rev2Panel.setBorder(BorderFactory.createTitledBorder("Rev 1/2: General Header block 2 offsets"));
        addRow(rev1Rev2Panel, "Extended header blocks offset:", extendedHeaderBlocksOffset2);
        addRow(rev1Rev2Panel, "External header blocks offset:", externalHeaderBlocksOffset2);
        versionCards.add(rev1Rev2Panel, SegdVersion.REV1_REV2.name());

        JPanel rev3Panel = new JPanel(new GridLayout(0, 2, 4, 4));
        rev3Panel.setBorder(BorderFactory.createTitledBorder(
            "Rev 3.1 (Sercel Nodal): General Header block 2/3 + trace header offsets"));
        addRow(rev3Panel, "GHB2: Additional blocks count offset:", rev3AdditionalBlocksCountOffset);
        addRow(rev3Panel, "GHB2: Dominant sampling interval offset:", rev3DominantSamplingIntervalOffset);
        addRow(rev3Panel, "GHB2: Extended header blocks offset:", rev3ExtendedHeaderBlocksOffset);
        addRow(rev3Panel, "GHB2: External header blocks offset:", rev3ExternalHeaderBlocksOffset);
        addRow(rev3Panel, "GHB3: Header size (first trace offset):", rev3HeaderSizeOffset);
        addRow(rev3Panel, "Trace header: extension count offset:", rev3TraceHeaderExtCountOffset);
        addRow(rev3Panel, "Trace hdr ext. #1: num samples offset:", rev3NumSamplesOffset);
        versionCards.add(rev3Panel, SegdVersion.REV3_1.name());

        versionCardLayout.show(versionCards, config.version.name());

        JPanel fieldsAndButtons = new JPanel(new BorderLayout(4, 4));
        fieldsAndButtons.add(saveLoadRow, BorderLayout.NORTH);
        JPanel fieldsMiddle = new JPanel(new BorderLayout(4, 4));
        fieldsMiddle.add(versionRow, BorderLayout.NORTH);
        fieldsMiddle.add(fields, BorderLayout.CENTER);
        fieldsMiddle.add(versionCards, BorderLayout.SOUTH);
        fieldsAndButtons.add(fieldsMiddle, BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout(4, 4));
        left.add(fieldsAndButtons, BorderLayout.NORTH);
        left.add(headerPreviewPanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, schemaEditor);
        split.setResizeWeight(0.35);
        split.setDividerLocation(360);

        add(split, BorderLayout.CENTER);
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
        versionCombo.setSelectedItem(config.version);
        versionCardLayout.show(versionCards, config.version.name());
        traceHeaderBytes.setValue(config.traceHeaderBytes);
        channelSetsOffset.setValue(config.channelSetsPerScanTypeByteOffset + 1);
        additionalHeaderBlocksOffset.setValue(config.additionalGeneralHeaderBlocksByteOffset + 1);
        baseScanIntervalOffset.setValue(config.baseScanIntervalByteOffset + 1);
        traceHeaderExtCountOffset.setValue(config.traceHeaderExtensionCountByteOffset + 1);
        samplesFieldOffset.setValue(config.samplesFieldByteOffsetInChannelSetDescriptor + 1);
        extendedHeaderBlocksOffset2.setValue(config.extendedHeaderBlocksByteOffsetInHeader2 + 1);
        externalHeaderBlocksOffset2.setValue(config.externalHeaderBlocksByteOffsetInHeader2 + 1);
        rev3AdditionalBlocksCountOffset.setValue(config.rev3AdditionalBlocksCountByteOffsetInHeader2 + 1);
        rev3DominantSamplingIntervalOffset.setValue(config.rev3DominantSamplingIntervalByteOffsetInHeader2 + 1);
        rev3ExtendedHeaderBlocksOffset.setValue(config.rev3ExtendedHeaderBlocksByteOffsetInHeader2 + 1);
        rev3ExternalHeaderBlocksOffset.setValue(config.rev3ExternalHeaderBlocksByteOffsetInHeader2 + 1);
        rev3HeaderSizeOffset.setValue(config.rev3HeaderSizeByteOffsetInHeader3 + 1);
        rev3TraceHeaderExtCountOffset.setValue(config.rev3TraceHeaderExtensionCountByteOffset + 1);
        rev3NumSamplesOffset.setValue(config.rev3NumSamplesByteOffsetInTraceHeaderExt1 + 1);
        schemaEditor.refresh();
    }

    /** commits any in-progress trace-header-schema table cell edit */
    public void commitEdits()
    {
        schemaEditor.commitEdits();
    }
}
