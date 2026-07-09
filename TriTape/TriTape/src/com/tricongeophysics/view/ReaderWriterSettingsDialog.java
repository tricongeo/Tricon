package com.tricongeophysics.view;

import com.tricongeophysics.model.SegdConfig;
import com.tricongeophysics.model.SegyBufferedFileReader;
import com.tricongeophysics.model.SegyConfig;
import com.tricongeophysics.model.SegyHeaderPreview;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Lets the user parameterize the SEG-Y and SEG-D readers/writers: the key
 * binary/general-header byte offsets, and the full trace-header field mapping
 * (which header lives at which byte, with what numeric encoding). Also has a
 * "SEG-Y Header Preview" tab that shows the actual textual and binary header
 * contents of a chosen file, decoded using whatever offsets are currently set
 * in the SEG-Y tab, so the user can verify their settings against real data.
 * Edits a working copy of each config; changes only take effect if the user
 * presses OK (see isCommitted()/getSegyConfig()/getSegdConfig()).
 */
public class ReaderWriterSettingsDialog extends JDialog
{
    private boolean committed = false;

    private final SegyConfig workingSegyConfig;
    private final SegdConfig workingSegdConfig;

    private final HeaderSchemaEditorPanel segySchemaEditor;
    private final HeaderSchemaEditorPanel segdSchemaEditor;

    private final JSpinner segySampleRateOffset;
    private final JSpinner segySamplesPerTraceOffset;
    private final JSpinner segyFormatCodeOffset;
    private final JSpinner segyCoordScalarOffset;
    private final JSpinner segyElevScalarOffset;
    private final JSpinner segyNumSamplesThisTraceOffset;
    private final JSpinner segyTraceHeaderBytes;

    private final JSpinner segdBaseScanIntervalOffset;
    private final JSpinner segdChannelSetsOffset;
    private final JSpinner segdAdditionalHeaderBlocksOffset;
    private final JSpinner segdTraceHeaderExtCountOffset;
    private final JSpinner segdSamplesFieldOffset;
    private final JSpinner segdTraceHeaderBytes;

    private final JTextField previewFileField = new JTextField(32);
    private final JTextArea textualHeaderArea = new JTextArea();
    private final JTextArea binaryHeaderSummaryArea = new JTextArea();
    private final JTextArea binaryHeaderHexArea = new JTextArea();
    private final JLabel previewStatusLabel = new JLabel(" ");

    public ReaderWriterSettingsDialog(Frame owner, SegyConfig segyConfig, SegdConfig segdConfig)
    {
        this(owner, segyConfig, segdConfig, "");
    }

    public ReaderWriterSettingsDialog(Frame owner, SegyConfig segyConfig, SegdConfig segdConfig, String defaultPreviewFile)
    {
        super(owner, "Reader / Writer Settings", true);
        this.workingSegyConfig = segyConfig.copy();
        this.workingSegdConfig = segdConfig.copy();

        segySampleRateOffset = spinner(workingSegyConfig.sampleRateByteOffset);
        segySamplesPerTraceOffset = spinner(workingSegyConfig.samplesPerTraceByteOffset);
        segyFormatCodeOffset = spinner(workingSegyConfig.formatCodeByteOffset);
        segyCoordScalarOffset = spinner(workingSegyConfig.coordinateScalarByteOffset);
        segyElevScalarOffset = spinner(workingSegyConfig.elevationScalarByteOffset);
        segyNumSamplesThisTraceOffset = spinner(workingSegyConfig.numSamplesThisTraceByteOffset);
        segyTraceHeaderBytes = spinner(workingSegyConfig.traceHeaderBytes);

        segdBaseScanIntervalOffset = spinner(workingSegdConfig.baseScanIntervalByteOffset);
        segdChannelSetsOffset = spinner(workingSegdConfig.channelSetsPerScanTypeByteOffset);
        segdAdditionalHeaderBlocksOffset = spinner(workingSegdConfig.additionalGeneralHeaderBlocksByteOffset);
        segdTraceHeaderExtCountOffset = spinner(workingSegdConfig.traceHeaderExtensionCountByteOffset);
        segdSamplesFieldOffset = spinner(workingSegdConfig.samplesFieldByteOffsetInChannelSetDescriptor);
        segdTraceHeaderBytes = spinner(workingSegdConfig.traceHeaderBytes);

        segySchemaEditor = new HeaderSchemaEditorPanel(workingSegyConfig.traceHeaderSchema);
        segdSchemaEditor = new HeaderSchemaEditorPanel(workingSegdConfig.traceHeaderSchema);

        previewFileField.setText(defaultPreviewFile == null ? "" : defaultPreviewFile);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("SEG-Y", buildSegyPanel());
        tabs.addTab("SEG-Y Header Preview", buildSegyHeaderPreviewPanel());
        tabs.addTab("SEG-D", buildSegdPanel());

        setLayout(new BorderLayout(4, 4));
        add(tabs, BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        setSize(800, 600);
        setLocationRelativeTo(owner);
    }

    private static JSpinner spinner(int value)
    {
        return new JSpinner(new SpinnerNumberModel(value, 0, 65535, 1));
    }

    private JPanel buildSegyPanel()
    {
        JPanel fields = new JPanel(new GridLayout(0, 2, 4, 4));
        fields.setBorder(BorderFactory.createTitledBorder("Byte offsets"));
        fields.add(new JLabel("Trace header length (bytes):"));
        fields.add(segyTraceHeaderBytes);
        fields.add(new JLabel("Binary header: sample rate offset:"));
        fields.add(segySampleRateOffset);
        fields.add(new JLabel("Binary header: samples/trace offset:"));
        fields.add(segySamplesPerTraceOffset);
        fields.add(new JLabel("Binary header: format code offset:"));
        fields.add(segyFormatCodeOffset);
        fields.add(new JLabel("Trace header: samples-this-trace offset:"));
        fields.add(segyNumSamplesThisTraceOffset);
        fields.add(new JLabel("Trace header: coordinate scalar offset:"));
        fields.add(segyCoordScalarOffset);
        fields.add(new JLabel("Trace header: elevation scalar offset:"));
        fields.add(segyElevScalarOffset);

        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(fields, BorderLayout.NORTH);
        panel.add(segySchemaEditor, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSegyHeaderPreviewPanel()
    {
        JPanel filePanel = new JPanel(new BorderLayout(4, 4));
        JPanel fileRow = new JPanel(new BorderLayout(4, 4));
        fileRow.add(new JLabel("File:"), BorderLayout.WEST);
        fileRow.add(previewFileField, BorderLayout.CENTER);
        JButton browse = new JButton("Browse...");
        browse.addActionListener(e -> browsePreviewFile());
        JButton load = new JButton("Load Headers");
        load.addActionListener(e -> loadHeaderPreview());
        JPanel fileButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fileButtons.add(browse);
        fileButtons.add(load);
        fileRow.add(fileButtons, BorderLayout.EAST);
        filePanel.add(fileRow, BorderLayout.NORTH);
        previewStatusLabel.setForeground(Color.DARK_GRAY);
        filePanel.add(previewStatusLabel, BorderLayout.SOUTH);

        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        textualHeaderArea.setFont(mono);
        textualHeaderArea.setEditable(false);
        textualHeaderArea.setLineWrap(false);

        binaryHeaderSummaryArea.setFont(mono);
        binaryHeaderSummaryArea.setEditable(false);
        binaryHeaderSummaryArea.setRows(5);

        binaryHeaderHexArea.setFont(mono);
        binaryHeaderHexArea.setEditable(false);

        JPanel textPanel = new JPanel(new BorderLayout(2, 2));
        textPanel.setBorder(BorderFactory.createTitledBorder("Textual (3200-byte) header"));
        textPanel.add(new JScrollPane(textualHeaderArea), BorderLayout.CENTER);

        JPanel binaryPanel = new JPanel(new BorderLayout(2, 2));
        binaryPanel.setBorder(BorderFactory.createTitledBorder("Binary header"));
        JPanel binaryTop = new JPanel(new BorderLayout());
        binaryTop.add(new JScrollPane(binaryHeaderSummaryArea), BorderLayout.NORTH);
        binaryPanel.add(binaryTop, BorderLayout.NORTH);
        binaryPanel.add(new JScrollPane(binaryHeaderHexArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textPanel, binaryPanel);
        split.setResizeWeight(0.5);

        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(filePanel, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void browsePreviewFile()
    {
        JFileChooser chooser = new JFileChooser();
        if (!previewFileField.getText().trim().isEmpty())
        {
            File current = new File(previewFileField.getText().trim());
            if (current.getParentFile() != null) chooser.setCurrentDirectory(current.getParentFile());
        }
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            previewFileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void loadHeaderPreview()
    {
        String filename = previewFileField.getText().trim();
        if (filename.isEmpty() || !new File(filename).isFile())
        {
            previewStatusLabel.setText("Choose a valid file first.");
            return;
        }

        segySchemaEditor.commitEdits();
        SegyConfig snapshot = workingSegyConfig.copy();
        writeSegySpinnersInto(snapshot);

        try
        {
            SegyHeaderPreview preview = SegyBufferedFileReader.peekHeaders(filename, snapshot);
            textualHeaderArea.setText(preview.textualHeader);
            textualHeaderArea.setCaretPosition(0);

            StringBuilder summary = new StringBuilder();
            summary.append("Sample rate (from offset ").append(snapshot.sampleRateByteOffset).append("): ")
                .append(preview.sampleRateMicros).append(" microseconds\n");
            summary.append("Samples per trace (from offset ").append(snapshot.samplesPerTraceByteOffset).append("): ")
                .append(preview.samplesPerTrace).append("\n");
            summary.append("Format code (from offset ").append(snapshot.formatCodeByteOffset).append("): ")
                .append(preview.formatCode).append(" (").append(formatCodeName(preview.formatCode)).append(")\n");
            binaryHeaderSummaryArea.setText(summary.toString());
            binaryHeaderSummaryArea.setCaretPosition(0);

            binaryHeaderHexArea.setText(hexDump(preview.binaryHeaderRaw));
            binaryHeaderHexArea.setCaretPosition(0);

            previewStatusLabel.setText("Loaded headers from " + filename + " using the current SEG-Y offsets above.");
        }
        catch (IOException ex)
        {
            previewStatusLabel.setText("Error reading headers: " + ex.getMessage());
            textualHeaderArea.setText("");
            binaryHeaderSummaryArea.setText("");
            binaryHeaderHexArea.setText("");
        }
    }

    private static String formatCodeName(int code)
    {
        switch (code)
        {
            case SegyBufferedFileReader.FORMAT_IBM_FLOAT:  return "IBM float";
            case SegyBufferedFileReader.FORMAT_INT32:      return "4-byte integer";
            case SegyBufferedFileReader.FORMAT_INT16:      return "2-byte integer";
            case SegyBufferedFileReader.FORMAT_IEEE_FLOAT: return "IEEE float";
            case SegyBufferedFileReader.FORMAT_INT8:       return "1-byte integer";
            default:                                       return "unknown";
        }
    }

    private static String hexDump(byte[] data)
    {
        if (data == null) return "";
        StringBuilder sb = new StringBuilder();
        int bytesPerLine = 16;
        for (int offset = 0; offset < data.length; offset += bytesPerLine)
        {
            sb.append(String.format("%04X  ", offset));
            StringBuilder ascii = new StringBuilder();
            for (int i = 0; i < bytesPerLine; i++)
            {
                int idx = offset + i;
                if (idx < data.length)
                {
                    int b = data[idx] & 0xFF;
                    sb.append(String.format("%02X ", b));
                    ascii.append(b >= 32 && b < 127 ? (char) b : '.');
                }
                else
                {
                    sb.append("   ");
                }
                if (i == bytesPerLine / 2 - 1) sb.append(' ');
            }
            sb.append(" ").append(ascii).append('\n');
        }
        return sb.toString();
    }

    private JPanel buildSegdPanel()
    {
        JPanel fields = new JPanel(new GridLayout(0, 2, 4, 4));
        fields.setBorder(BorderFactory.createTitledBorder("Byte offsets (within General Header block 1, unless noted)"));
        fields.add(new JLabel("Trace header length (bytes):"));
        fields.add(segdTraceHeaderBytes);
        fields.add(new JLabel("Channel sets per scan type offset:"));
        fields.add(segdChannelSetsOffset);
        fields.add(new JLabel("Additional general header blocks offset:"));
        fields.add(segdAdditionalHeaderBlocksOffset);
        fields.add(new JLabel("Base scan interval offset:"));
        fields.add(segdBaseScanIntervalOffset);
        fields.add(new JLabel("Trace header extension count offset:"));
        fields.add(segdTraceHeaderExtCountOffset);
        fields.add(new JLabel("Samples field offset (in channel set descriptor):"));
        fields.add(segdSamplesFieldOffset);

        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(fields, BorderLayout.NORTH);
        panel.add(segdSchemaEditor, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtonPanel()
    {
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e ->
        {
            segySchemaEditor.commitEdits();
            segdSchemaEditor.commitEdits();
            writeSegySpinnersInto(workingSegyConfig);
            writeSegdSpinnersInto(workingSegdConfig);
            committed = true;
            setVisible(false);
        });
        cancel.addActionListener(e -> setVisible(false));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(ok);
        panel.add(cancel);
        return panel;
    }

    private void writeSegySpinnersInto(SegyConfig c)
    {
        c.traceHeaderBytes = (Integer) segyTraceHeaderBytes.getValue();
        c.sampleRateByteOffset = (Integer) segySampleRateOffset.getValue();
        c.samplesPerTraceByteOffset = (Integer) segySamplesPerTraceOffset.getValue();
        c.formatCodeByteOffset = (Integer) segyFormatCodeOffset.getValue();
        c.numSamplesThisTraceByteOffset = (Integer) segyNumSamplesThisTraceOffset.getValue();
        c.coordinateScalarByteOffset = (Integer) segyCoordScalarOffset.getValue();
        c.elevationScalarByteOffset = (Integer) segyElevScalarOffset.getValue();
    }

    private void writeSegdSpinnersInto(SegdConfig c)
    {
        c.traceHeaderBytes = (Integer) segdTraceHeaderBytes.getValue();
        c.channelSetsPerScanTypeByteOffset = (Integer) segdChannelSetsOffset.getValue();
        c.additionalGeneralHeaderBlocksByteOffset = (Integer) segdAdditionalHeaderBlocksOffset.getValue();
        c.baseScanIntervalByteOffset = (Integer) segdBaseScanIntervalOffset.getValue();
        c.traceHeaderExtensionCountByteOffset = (Integer) segdTraceHeaderExtCountOffset.getValue();
        c.samplesFieldByteOffsetInChannelSetDescriptor = (Integer) segdSamplesFieldOffset.getValue();
    }

    public boolean isCommitted() { return committed; }
    public SegyConfig getSegyConfig() { return workingSegyConfig; }
    public SegdConfig getSegdConfig() { return workingSegdConfig; }
}
