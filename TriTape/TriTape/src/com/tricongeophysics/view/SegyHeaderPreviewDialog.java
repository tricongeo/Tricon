package com.tricongeophysics.view;

import com.tricongeophysics.model.SegyBufferedFileReader;
import com.tricongeophysics.model.SegyConfig;
import com.tricongeophysics.model.SegyHeaderPreview;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Small, non-modal utility window that shows a SEG-Y file's actual textual
 * and binary header contents, decoded using whatever offsets are currently
 * set in the SegyConfig it was opened with. Since SegySettingsPanel binds its
 * spinners directly to the same live config object, "Load Headers" always
 * reflects the current settings, even if they were just changed.
 */
public class SegyHeaderPreviewDialog extends JDialog
{
    private final SegyConfig config;
    private final JTextField fileField = new JTextField(32);
    private final JTextArea textualHeaderArea = new JTextArea();
    private final JTextArea binarySummaryArea = new JTextArea();
    private final JTextArea binaryHexArea = new JTextArea();
    private final JLabel statusLabel = new JLabel(" ");

    public SegyHeaderPreviewDialog(Window owner, SegyConfig config, String defaultFile)
    {
        super(owner, "SEG-Y Header Preview", ModalityType.MODELESS);
        this.config = config;
        fileField.setText(defaultFile == null ? "" : defaultFile);
        buildUI();
        setSize(720, 560);
        setLocationRelativeTo(owner);
    }

    private void buildUI()
    {
        JPanel fileRow = new JPanel(new BorderLayout(4, 4));
        fileRow.add(new JLabel("File:"), BorderLayout.WEST);
        fileRow.add(fileField, BorderLayout.CENTER);
        JButton browse = new JButton("Browse...");
        browse.addActionListener(e -> browseFile());
        JButton load = new JButton("Load Headers");
        load.addActionListener(e -> loadHeaders());
        JPanel fileButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fileButtons.add(browse);
        fileButtons.add(load);
        fileRow.add(fileButtons, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout());
        top.add(fileRow, BorderLayout.NORTH);
        statusLabel.setForeground(Color.DARK_GRAY);
        top.add(statusLabel, BorderLayout.SOUTH);

        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        textualHeaderArea.setFont(mono);
        textualHeaderArea.setEditable(false);
        binarySummaryArea.setFont(mono);
        binarySummaryArea.setEditable(false);
        binarySummaryArea.setRows(5);
        binaryHexArea.setFont(mono);
        binaryHexArea.setEditable(false);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createTitledBorder("Textual (3200-byte) header"));
        textPanel.add(new JScrollPane(textualHeaderArea), BorderLayout.CENTER);

        JPanel binaryPanel = new JPanel(new BorderLayout());
        binaryPanel.setBorder(BorderFactory.createTitledBorder("Binary header"));
        binaryPanel.add(new JScrollPane(binarySummaryArea), BorderLayout.NORTH);
        binaryPanel.add(new JScrollPane(binaryHexArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textPanel, binaryPanel);
        split.setResizeWeight(0.5);

        setLayout(new BorderLayout(4, 4));
        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    private void browseFile()
    {
        JFileChooser chooser = new JFileChooser();
        if (!fileField.getText().trim().isEmpty())
        {
            File current = new File(fileField.getText().trim());
            if (current.getParentFile() != null) chooser.setCurrentDirectory(current.getParentFile());
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            fileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void loadHeaders()
    {
        String filename = fileField.getText().trim();
        if (filename.isEmpty() || !new File(filename).isFile())
        {
            statusLabel.setText("Choose a valid file first.");
            return;
        }
        try
        {
            SegyHeaderPreview preview = SegyBufferedFileReader.peekHeaders(filename, config);
            textualHeaderArea.setText(preview.textualHeader);
            textualHeaderArea.setCaretPosition(0);

            StringBuilder summary = new StringBuilder();
            summary.append("Sample rate (from offset ").append(config.sampleRateByteOffset).append("): ")
                .append(preview.sampleRateMicros).append(" microseconds\n");
            summary.append("Samples per trace (from offset ").append(config.samplesPerTraceByteOffset).append("): ")
                .append(preview.samplesPerTrace).append("\n");
            summary.append("Format code (from offset ").append(config.formatCodeByteOffset).append("): ")
                .append(preview.formatCode).append(" (").append(formatCodeName(preview.formatCode)).append(")\n");
            binarySummaryArea.setText(summary.toString());
            binarySummaryArea.setCaretPosition(0);

            binaryHexArea.setText(hexDump(preview.binaryHeaderRaw));
            binaryHexArea.setCaretPosition(0);

            statusLabel.setText("Loaded headers from " + filename + " using the current SEG-Y offsets.");
        }
        catch (IOException ex)
        {
            statusLabel.setText("Error reading headers: " + ex.getMessage());
            textualHeaderArea.setText("");
            binarySummaryArea.setText("");
            binaryHexArea.setText("");
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
}
