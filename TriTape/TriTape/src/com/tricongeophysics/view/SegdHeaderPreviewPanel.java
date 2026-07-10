package com.tricongeophysics.view;

import com.tricongeophysics.model.SegdBufferedFileReader;
import com.tricongeophysics.model.SegdConfig;
import com.tricongeophysics.model.SegdHeaderPreview;
import com.tricongeophysics.model.SegdVersion;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Inline diagnostic view of a SEG-D file's key header blocks (General Header
 * 1/2/3, and the first trace's own header + Trace Header Extension #1),
 * shown as both a decoded summary and raw hex. SEG-D varies enough by
 * revision and vendor that SegdConfig's default offsets are a best-effort
 * starting point, not a guarantee - this panel is how to check them against
 * an actual file when something looks wrong (e.g. traces reading as NaN,
 * which usually means the reader landed on the wrong bytes somewhere
 * upstream of the sample data).
 */
public class SegdHeaderPreviewPanel extends JPanel
{
    private final SegdConfig config;
    private final Supplier<String> fileHintSupplier;

    private final JLabel fileLabel = new JLabel("File: (none)");
    private final JTextArea summaryArea = new JTextArea();
    private final JTextArea hexArea = new JTextArea();
    private final JComboBox<String> hexBlockCombo = new JComboBox<String>();
    private final JLabel statusLabel = new JLabel(" ");

    private SegdHeaderPreview lastPreview;

    public SegdHeaderPreviewPanel(SegdConfig config, Supplier<String> fileHintSupplier)
    {
        super(new BorderLayout(4, 4));
        this.config = config;
        this.fileHintSupplier = fileHintSupplier;
        setBorder(BorderFactory.createTitledBorder("Header preview (diagnostic)"));
        buildUI();
    }

    private void buildUI()
    {
        JPanel fileRow = new JPanel(new BorderLayout(4, 4));
        fileRow.add(fileLabel, BorderLayout.CENTER);
        JButton load = new JButton("Load Headers");
        load.addActionListener(e -> loadHeaders());
        fileRow.add(load, BorderLayout.EAST);

        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 11);
        summaryArea.setFont(mono);
        summaryArea.setEditable(false);
        summaryArea.setRows(11);
        hexArea.setFont(mono);
        hexArea.setEditable(false);
        hexArea.setRows(11);

        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Decoded summary"));
        summaryPanel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);

        JPanel hexPanel = new JPanel(new BorderLayout(2, 2));
        hexPanel.setBorder(BorderFactory.createTitledBorder("Raw bytes"));
        JPanel hexTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hexTop.add(new JLabel("Block:"));
        hexBlockCombo.addActionListener(e -> updateHexDisplay());
        hexTop.add(hexBlockCombo);
        hexPanel.add(hexTop, BorderLayout.NORTH);
        hexPanel.add(new JScrollPane(hexArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, summaryPanel, hexPanel);
        split.setResizeWeight(0.4);

        statusLabel.setForeground(Color.DARK_GRAY);

        add(fileRow, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    /** re-loads headers for the current file (from fileHintSupplier) using the config's latest offsets; no-op if none set */
    public void reloadIfFileSet()
    {
        if (!fileHintSupplier.get().trim().isEmpty())
        {
            loadHeaders();
        }
    }

    private void loadHeaders()
    {
        String filename = fileHintSupplier.get().trim();
        fileLabel.setText("File: " + (filename.isEmpty() ? "(none)" : filename));
        if (filename.isEmpty() || !new File(filename).isFile())
        {
            statusLabel.setText("Choose a valid file in the field above first.");
            return;
        }
        try
        {
            SegdHeaderPreview preview = SegdBufferedFileReader.peekHeaders(filename, config);
            lastPreview = preview;

            StringBuilder sb = new StringBuilder();
            sb.append("File number (GH1): ").append(preview.fileNumber).append('\n');
            sb.append("Format code (GH1): ").append(preview.formatCode).append('\n');
            sb.append("Channel sets/scan type (GH1): ").append(preview.channelSetsPerScanType).append('\n');
            sb.append("Additional gen. header blocks nibble (GH1): ").append(preview.additionalGeneralHeaderBlocksNibble).append('\n');
            sb.append("Sample rate from GH1 base scan interval: ").append(preview.sampleRateMicrosFromHeader1).append(" us\n");

            if (config.version == SegdVersion.REV3_1)
            {
                sb.append("\n--- Rev 3.1 (from GHB2/GHB3) ---\n");
                sb.append("True additional blocks count (GHB2): ").append(preview.trueAdditionalBlocks).append('\n');
                sb.append("Dominant sampling interval (GHB2): ").append(preview.dominantSamplingIntervalMicros).append(" us\n");
                sb.append("Extended header blocks (GHB2): ").append(preview.extendedHeaderBlocks).append('\n');
                sb.append("External header blocks (GHB2): ").append(preview.externalHeaderBlocks).append('\n');
                sb.append("Header size / first trace offset (GHB3): ").append(preview.headerSizeOffset).append('\n');
                sb.append("First trace: extension block count: ").append(preview.firstTraceExtensionCount).append('\n');
                sb.append("First trace: num samples (from Ext #1): ").append(preview.firstTraceNumSamples).append('\n');
                if (preview.generalHeader3Raw == null)
                {
                    sb.append("\n(General Header Block #3 wasn't reached - either the additional-block\n");
                    sb.append(" count is <2, or the GHB2 offsets above are wrong for this file.)\n");
                }
                if (preview.firstTraceHeaderRaw == null)
                {
                    sb.append("\n(Couldn't reach the first trace - Header Size above is likely wrong,\n");
                    sb.append(" or GHB3 wasn't reached at all. Compare the GHB3 hex dump against\n");
                    sb.append(" the manual's byte 25-28 field by hand if needed.)\n");
                }
            }
            else
            {
                sb.append("\n--- Rev 1/2 (from GHB2) ---\n");
                sb.append("Extended header blocks (GHB2): ").append(preview.extendedHeaderBlocks).append('\n');
                sb.append("External header blocks (GHB2): ").append(preview.externalHeaderBlocks).append('\n');
            }
            summaryArea.setText(sb.toString());
            summaryArea.setCaretPosition(0);

            hexBlockCombo.removeAllItems();
            hexBlockCombo.addItem("General Header Block #1");
            if (preview.generalHeader2Raw != null) hexBlockCombo.addItem("General Header Block #2");
            if (preview.generalHeader3Raw != null) hexBlockCombo.addItem("General Header Block #3");
            if (preview.firstTraceHeaderRaw != null) hexBlockCombo.addItem("First Trace Header (20 bytes)");
            if (preview.firstTraceHeaderExt1Raw != null) hexBlockCombo.addItem("First Trace Header Extension #1");
            hexBlockCombo.setSelectedIndex(0);
            updateHexDisplay();

            statusLabel.setText("Loaded diagnostic headers from " + filename + ".");
        }
        catch (IOException ex)
        {
            statusLabel.setText("Error: " + ex.getMessage());
            summaryArea.setText("");
            hexArea.setText("");
            hexBlockCombo.removeAllItems();
        }
    }

    private void updateHexDisplay()
    {
        if (lastPreview == null) return;
        String selected = (String) hexBlockCombo.getSelectedItem();
        if (selected == null) return;
        byte[] data;
        if ("General Header Block #1".equals(selected)) data = lastPreview.generalHeader1Raw;
        else if ("General Header Block #2".equals(selected)) data = lastPreview.generalHeader2Raw;
        else if ("General Header Block #3".equals(selected)) data = lastPreview.generalHeader3Raw;
        else if ("First Trace Header (20 bytes)".equals(selected)) data = lastPreview.firstTraceHeaderRaw;
        else if ("First Trace Header Extension #1".equals(selected)) data = lastPreview.firstTraceHeaderExt1Raw;
        else data = null;

        hexArea.setText(hexDump(data));
        hexArea.setCaretPosition(0);
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
}
