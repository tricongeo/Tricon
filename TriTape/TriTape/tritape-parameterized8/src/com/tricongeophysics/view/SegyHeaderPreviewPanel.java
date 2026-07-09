package com.tricongeophysics.view;

import com.tricongeophysics.SeismicTrace;
import com.tricongeophysics.model.SegyBufferedFileReader;
import com.tricongeophysics.model.SegyConfig;
import com.tricongeophysics.model.SegyHeaderPreview;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Inline view of a SEG-Y file's actual textual and binary header contents,
 * decoded using whatever offsets are currently set in the SegyConfig it's
 * bound to. Embedded directly inside SegySettingsPanel, right alongside the
 * byte-offset fields that decode it, so there's no separate window to open.
 *
 * Always reads whichever file is currently in TraceMonitor's input (or
 * output) file field, via fileHintSupplier - there's no separate file picker
 * here, it just follows the file the user already chose for reading/writing.
 */
public class SegyHeaderPreviewPanel extends JPanel
{
    private final SegyConfig config;
    private final Supplier<String> fileHintSupplier;
    private final Consumer<SeismicTrace[]> onTracesLoaded;

    private final JLabel fileLabel = new JLabel("File: (none)");
    private final JTextArea textualHeaderArea = new JTextArea();
    private final JTextArea binarySummaryArea = new JTextArea();
    private final JTextArea binaryHexArea = new JTextArea();
    private final JLabel statusLabel = new JLabel(" ");

    public SegyHeaderPreviewPanel(SegyConfig config, Supplier<String> fileHintSupplier)
    {
        this(config, fileHintSupplier, traces -> { });
    }

    public SegyHeaderPreviewPanel(SegyConfig config, Supplier<String> fileHintSupplier, Consumer<SeismicTrace[]> onTracesLoaded)
    {
        super(new BorderLayout(4, 4));
        this.config = config;
        this.fileHintSupplier = fileHintSupplier;
        this.onTracesLoaded = onTracesLoaded;
        setBorder(BorderFactory.createTitledBorder("Header preview"));
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
        textualHeaderArea.setFont(mono);
        textualHeaderArea.setEditable(false);
        textualHeaderArea.setRows(11);
        binarySummaryArea.setFont(mono);
        binarySummaryArea.setEditable(false);
        binarySummaryArea.setRows(3);
        binaryHexArea.setFont(mono);
        binaryHexArea.setEditable(false);
        binaryHexArea.setRows(11);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createTitledBorder("Textual header"));
        textPanel.add(new JScrollPane(textualHeaderArea), BorderLayout.CENTER);

        JPanel binaryPanel = new JPanel(new BorderLayout(2, 2));
        binaryPanel.setBorder(BorderFactory.createTitledBorder("Binary header"));
        binaryPanel.add(new JScrollPane(binarySummaryArea), BorderLayout.NORTH);
        binaryPanel.add(new JScrollPane(binaryHexArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textPanel, binaryPanel);
        split.setResizeWeight(0.5);

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
            SegyHeaderPreview preview = SegyBufferedFileReader.peekHeaders(filename, config);
            textualHeaderArea.setText(preview.textualHeader);
            textualHeaderArea.setCaretPosition(0);

            StringBuilder summary = new StringBuilder();
            summary.append("Sample rate (offset ").append(config.sampleRateByteOffset).append("): ")
                .append(preview.sampleRateMicros).append(" microseconds\n");
            summary.append("Samples/trace (offset ").append(config.samplesPerTraceByteOffset).append("): ")
                .append(preview.samplesPerTrace).append("\n");
            summary.append("Format code (offset ").append(config.formatCodeByteOffset).append("): ")
                .append(preview.formatCode).append(" (").append(formatCodeName(preview.formatCode)).append(")\n");
            binarySummaryArea.setText(summary.toString());
            binarySummaryArea.setCaretPosition(0);

            binaryHexArea.setText(hexDump(preview.binaryHeaderRaw));
            binaryHexArea.setCaretPosition(0);

            SeismicTrace[] sampleTraces = readSampleTraces(filename, 3);
            onTracesLoaded.accept(sampleTraces);

            statusLabel.setText("Loaded headers" + (sampleTraces.length > 0
                ? " and " + sampleTraces.length + " sample trace(s)." : "."));
        }
        catch (IOException ex)
        {
            statusLabel.setText("Error: " + ex.getMessage());
            textualHeaderArea.setText("");
            binarySummaryArea.setText("");
            binaryHexArea.setText("");
            onTracesLoaded.accept(new SeismicTrace[0]);
        }
    }

    /** best-effort read of the first few trace headers/samples, for the schema table's sample-value columns */
    private SeismicTrace[] readSampleTraces(String filename, int count)
    {
        try (SegyBufferedFileReader reader = new SegyBufferedFileReader(filename, config))
        {
            reader.open();
            return reader.readNextTraces(count);
        }
        catch (IOException ex)
        {
            return new SeismicTrace[0];
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
