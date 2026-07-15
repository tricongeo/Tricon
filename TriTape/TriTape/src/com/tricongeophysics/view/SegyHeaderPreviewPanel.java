package com.tricongeophysics.view;

import com.tricongeophysics.SeismicTrace;
import com.tricongeophysics.model.SegyBufferedFileReader;
import com.tricongeophysics.model.SegyConfig;
import com.tricongeophysics.model.SegyEbcdic;
import com.tricongeophysics.model.SegyHeaderPreview;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
 * Always reads whichever file its fileHintSupplier currently points at - there's
 * no file picker or filename label here (the Browse section above already
 * shows the chosen file), and no "Load Headers" button either: TraceMonitor
 * calls reloadIfFileSet() automatically whenever the relevant file field or
 * format combo changes, so this panel just reflects whatever's currently
 * selected. The Input tab's instance is seeded from the input file field;
 * the Output tab's instance is ALSO seeded from the input file(s) (see
 * TraceMonitor's construction of outputSegySettingsPanel), not the output
 * file field - the point of this preview is to show what's about to be
 * written, which should never depend on whatever might already exist on
 * disk at the chosen output path from an earlier run.
 *
 * showMirroredPreview() lets another instance's already-loaded result be
 * displayed here without doing its own file read - used so that reloading
 * the Input tab's preview also updates the Output tab's preview to show what
 * the output file's headers will default to.
 *
 * When editableTextualHeader is true (used for the output tab), the textual
 * header text area can be edited by the user: getEffectiveTextualHeaderRaw()
 * then returns their edited text (re-encoded to bytes) instead of the raw
 * default bytes, so a value/binary header is expected to just pass through
 * unchanged, but the textual header can be interactively customized before
 * the file is actually written.
 */
public class SegyHeaderPreviewPanel extends JPanel
{
    private final SegyConfig config;
    private final Supplier<String> fileHintSupplier;
    private final Consumer<SeismicTrace[]> onTracesLoaded;
    private final Consumer<SegyHeaderPreview> onHeadersLoaded;
    private final boolean editableTextualHeader;

    private final JTextArea textualHeaderArea = new JTextArea();
    private final JTextArea binarySummaryArea = new JTextArea();
    private final JTextArea binaryHexArea = new JTextArea();
    private final JLabel statusLabel = new JLabel(" ");

    private byte[] lastRawTextualHeaderBytes;
    private boolean textualHeaderUserEdited = false;
    private boolean suppressEditTracking = false;

    public SegyHeaderPreviewPanel(SegyConfig config, Supplier<String> fileHintSupplier)
    {
        this(config, fileHintSupplier, traces -> { }, preview -> { }, false);
    }

    public SegyHeaderPreviewPanel(SegyConfig config, Supplier<String> fileHintSupplier, Consumer<SeismicTrace[]> onTracesLoaded)
    {
        this(config, fileHintSupplier, onTracesLoaded, preview -> { }, false);
    }

    public SegyHeaderPreviewPanel(SegyConfig config, Supplier<String> fileHintSupplier,
                                   Consumer<SeismicTrace[]> onTracesLoaded, Consumer<SegyHeaderPreview> onHeadersLoaded)
    {
        this(config, fileHintSupplier, onTracesLoaded, onHeadersLoaded, false);
    }

    public SegyHeaderPreviewPanel(SegyConfig config, Supplier<String> fileHintSupplier, Consumer<SeismicTrace[]> onTracesLoaded,
                                   Consumer<SegyHeaderPreview> onHeadersLoaded, boolean editableTextualHeader)
    {
        super(new BorderLayout(4, 4));
        this.config = config;
        this.fileHintSupplier = fileHintSupplier;
        this.onTracesLoaded = onTracesLoaded;
        this.onHeadersLoaded = onHeadersLoaded;
        this.editableTextualHeader = editableTextualHeader;
        setBorder(BorderFactory.createTitledBorder("Header preview"));
        buildUI();
    }

    private void buildUI()
    {
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 11);
        textualHeaderArea.setFont(mono);
        textualHeaderArea.setEditable(editableTextualHeader);
        textualHeaderArea.setRows(11);
        if (editableTextualHeader)
        {
            textualHeaderArea.getDocument().addDocumentListener(new DocumentListener()
            {
                @Override public void insertUpdate(DocumentEvent e) { markEdited(); }
                @Override public void removeUpdate(DocumentEvent e) { markEdited(); }
                @Override public void changedUpdate(DocumentEvent e) { markEdited(); }
            });
        }
        binarySummaryArea.setFont(mono);
        binarySummaryArea.setEditable(false);
        binarySummaryArea.setRows(3);
        binaryHexArea.setFont(mono);
        binaryHexArea.setEditable(false);
        binaryHexArea.setRows(11);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createTitledBorder(
            editableTextualHeader ? "Textual header (editable - this is what gets written)" : "Textual header"));
        textPanel.add(new JScrollPane(textualHeaderArea), BorderLayout.CENTER);

        JPanel binaryPanel = new JPanel(new BorderLayout(2, 2));
        binaryPanel.setBorder(BorderFactory.createTitledBorder("Binary header"));
        binaryPanel.add(new JScrollPane(binarySummaryArea), BorderLayout.NORTH);
        binaryPanel.add(new JScrollPane(binaryHexArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textPanel, binaryPanel);
        split.setResizeWeight(0.5);

        statusLabel.setForeground(Color.DARK_GRAY);

        add(split, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void markEdited()
    {
        if (!suppressEditTracking)
        {
            textualHeaderUserEdited = true;
        }
    }

    /**
     * Replaces the textual header area's content programmatically, without counting it as a user
     * edit - public so TraceMonitor can populate the output tab's default for non-SEG-Y input (see
     * HeaderSchema.describeAsTextualHeaderEbcdic()), the same way showMirroredPreview() already does
     * for SEG-Y input.
     */
    public void setTextualHeaderDefault(String displayText, byte[] rawBytes)
    {
        suppressEditTracking = true;
        textualHeaderArea.setText(displayText);
        textualHeaderArea.setCaretPosition(0);
        suppressEditTracking = false;
        lastRawTextualHeaderBytes = rawBytes;
        textualHeaderUserEdited = false;
    }

    /**
     * The textual header bytes that should actually be written: the user's edited text, EBCDIC-
     * encoded (via SegyEbcdic, matching the SEG-Y standard's own convention - a third-party reader
     * expecting EBCDIC will otherwise silently misread hand-edited text that got encoded as plain
     * ASCII/platform-default bytes instead) and sized to exactly config.textualHeaderBytes, if
     * they've changed the text area since it was last set programmatically; otherwise the exact raw
     * bytes of whatever default was loaded/mirrored/generated in (already correctly encoded whichever
     * way its own source used - see setTextualHeaderDefault() callers). Returns null if nothing has
     * been loaded/mirrored/generated here yet and the user hasn't typed anything.
     */
    public byte[] getEffectiveTextualHeaderRaw()
    {
        if (textualHeaderUserEdited)
        {
            String concatenated = textualHeaderArea.getText().replace("\n", "").replace("\r", "");
            return SegyEbcdic.toEbcdic(concatenated, config.textualHeaderBytes);
        }
        return lastRawTextualHeaderBytes;
    }

    /** re-loads headers for the current file (from fileHintSupplier) using the config's latest offsets; no-op if none set */
    public void reloadIfFileSet()
    {
        if (!fileHintSupplier.get().trim().isEmpty())
        {
            loadHeaders();
        }
    }

    /**
     * Displays an already-fetched preview (from another panel's loadHeaders() call) without
     * reading any file itself - used to mirror the input file's actual headers into the output
     * tab's preview, showing what the output will actually default to.
     */
    public void showMirroredPreview(SegyHeaderPreview preview, String sourceDescription)
    {
        setTextualHeaderDefault(preview.textualHeader, preview.textualHeaderRaw);

        StringBuilder summary = new StringBuilder();
        summary.append("Sample rate: ").append(preview.sampleRateMicros).append(" microseconds\n");
        summary.append("Samples/trace: ").append(preview.samplesPerTrace).append("\n");
        summary.append("Format code: ").append(preview.formatCode)
            .append(" (").append(formatCodeName(preview.formatCode)).append(")\n");
        binarySummaryArea.setText(summary.toString());
        binarySummaryArea.setCaretPosition(0);

        binaryHexArea.setText(hexDump(preview.binaryHeaderRaw));
        binaryHexArea.setCaretPosition(0);

        statusLabel.setText("Defaulted from the " + sourceDescription + " file."
            + (editableTextualHeader ? " Edit the textual header above if you'd like to change it." : ""));
    }

    private void loadHeaders()
    {
        String filename = fileHintSupplier.get().trim();

        if (filename.isEmpty() || !new File(filename).isFile())
        {
            statusLabel.setText("Choose a valid file in the field above first.");
            return;
        }
        try
        {
            SegyHeaderPreview preview = SegyBufferedFileReader.peekHeaders(filename, config);
            setTextualHeaderDefault(preview.textualHeader, preview.textualHeaderRaw);

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
            onHeadersLoaded.accept(preview);

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
