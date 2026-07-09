package com.tricongeophysics.controller;

import com.tricongeophysics.SeismicTrace;
import com.tricongeophysics.model.BufferedFileReader;
import com.tricongeophysics.model.FileFormat;
import com.tricongeophysics.model.ReaderFactory;
import com.tricongeophysics.model.SegdConfig;
import com.tricongeophysics.model.SegyBufferedFileReader;
import com.tricongeophysics.model.SegyConfig;
import com.tricongeophysics.model.SegyHeaderPreview;
import com.tricongeophysics.model.TraceWriter;
import com.tricongeophysics.model.WriterConfig;
import com.tricongeophysics.model.WriterFactory;
import com.tricongeophysics.view.SegdSettingsPanel;
import com.tricongeophysics.view.SegySettingsPanel;
import com.tricongeophysics.view.TraceViewer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MVC Controller. Owns the file-selection UI, uses the model reader classes to
 * pull a batch of traces (default 10000) at a time and hands them to the
 * TraceViewer for preview. When the user is happy with the preview and clicks
 * "Submit", the entire input file (or files - the input Browse button allows
 * selecting several, e.g. one file per FFID as some field systems produce;
 * they're read as one continuous stream via MultiFileBufferedFileReader) is
 * streamed through the same reader/writer pair in batches, applying whatever
 * processing chain is currently active in the viewer, with a ProgressMonitor
 * showing progress.
 *
 * The window is laid out as two horizontal panes: a left pane holding a
 * tabbed "Input"/"Output" panel (file, format, and the reader's or writer's
 * byte-offset/header-schema settings + live SEG-Y header preview, all shown
 * inline and swapped via CardLayout as the format changes) and a right pane
 * with batch size / Load Preview / the TraceViewer (unchanged).
 */
public class TraceMonitor
{
    private final JFrame frame;
    private final TraceViewer viewer;

    private final JTextField inputFileField = new JTextField(20);
    private final JTextField outputFileField = new JTextField(20);
    private final JComboBox<FileFormat> inputFormatCombo = new JComboBox<FileFormat>(FileFormat.values());
    private final JComboBox<FileFormat> outputFormatCombo = new JComboBox<FileFormat>(FileFormat.values());
    private final JSpinner batchSizeSpinner = new JSpinner(
        new SpinnerNumberModel(BufferedFileReader.DEFAULT_BATCH_SIZE, 1, 1_000_000, 1000));
    private final JButton previewButton = new JButton("Load Preview");
    private final JButton submitButton = new JButton("Submit (Reformat File)");
    private final JLabel statusLabel = new JLabel("Select an input file and click Load Preview.");

    private BufferedFileReader previewReader;
    private final SegyConfig readerSegyConfig = new SegyConfig();
    private final SegdConfig readerSegdConfig = new SegdConfig();
    private final SegyConfig writerSegyConfig = new SegyConfig();
    private final SegdConfig writerSegdConfig = new SegdConfig();

    // inline settings panels: input pane's are bound to the reader configs,
    // output pane's are bound to the writer configs - fully independent, no cross-syncing needed
    private final SegySettingsPanel inputSegySettingsPanel;
    private final SegdSettingsPanel inputSegdSettingsPanel;
    private final SegySettingsPanel outputSegySettingsPanel;
    private final SegdSettingsPanel outputSegdSettingsPanel;

    public TraceMonitor()
    {
        viewer = new TraceViewer();

        outputSegySettingsPanel = new SegySettingsPanel(writerSegyConfig, () -> outputFileField.getText().trim(),
            preview -> { }, true);
        outputSegdSettingsPanel = new SegdSettingsPanel(writerSegdConfig);
        inputSegySettingsPanel = new SegySettingsPanel(readerSegyConfig, this::firstInputFile, preview ->
        {
            if (outputFormatCombo.getSelectedItem() == FileFormat.SEGY)
            {
                outputSegySettingsPanel.showMirroredPreview(preview, "input");
            }
        });
        inputSegdSettingsPanel = new SegdSettingsPanel(readerSegdConfig);

        frame = new JFrame("TriTape");
        buildUI();
        syncOutputSegyDefaultsFromInput();
        inputFileField.setText("/home/scott/Projects/develop/tritape/jetson_test_shots.sgy");
        previewButton.doClick();
    }

    private void buildUI()
    {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(buildTwoPaneLayout(), BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);

        previewButton.addActionListener(e -> loadPreview());
        submitButton.addActionListener(e -> submitReformat());
        submitButton.setEnabled(false);

        frame.setSize(1600, 900);
        frame.setLocationRelativeTo(null);
    }

    /** two horizontal panes: a tabbed Input/Output pane on the left, the TraceViewer pane on the right */
    private JSplitPane buildTwoPaneLayout()
    {
        JTabbedPane ioTabs = new JTabbedPane();
        ioTabs.addTab("Input", buildInputPane());
        ioTabs.addTab("Output", buildOutputPane());
        ioTabs.setPreferredSize(new Dimension(760, 0));

        JPanel viewerPane = buildViewerPane();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ioTabs, viewerPane);
        split.setResizeWeight(0.0); // extra space on resize goes to the viewer
        split.setOneTouchExpandable(true);
        return split;
    }

    private JPanel buildInputPane()
    {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel fileControls = new JPanel(new GridBagLayout());
        JButton browseInput = new JButton("Browse...");
        browseInput.setToolTipText("Select one file, or multiple files (e.g. one file per FFID) to read as a single continuous input");
        browseInput.addActionListener(e -> browseInputFiles());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;

        int row = 0;
        c.gridy = row++; fileControls.add(new JLabel("Input file(s):"), c);
        c.gridy = row++; fileControls.add(inputFileField, c);
        c.gridy = row++; fileControls.add(browseInput, c);
        c.gridy = row++; fileControls.add(new JLabel("Input format:"), c);
        c.gridy = row++; fileControls.add(inputFormatCombo, c);

        CardLayout cardLayout = new CardLayout();
        JPanel settingsCards = new JPanel(cardLayout);
        settingsCards.add(inputSegySettingsPanel, FileFormat.SEGY.name());
        settingsCards.add(inputSegdSettingsPanel, FileFormat.SEGD.name());
        inputFormatCombo.addActionListener(e ->
        {
            FileFormat fmt = (FileFormat) inputFormatCombo.getSelectedItem();
            cardLayout.show(settingsCards, fmt.name());
            if (fmt == FileFormat.SEGY) inputSegySettingsPanel.refresh(); else inputSegdSettingsPanel.refresh();
            syncOutputSegyDefaultsFromInput();
        });

        panel.add(fileControls, BorderLayout.NORTH);
        panel.add(settingsCards, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildOutputPane()
    {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel fileControls = new JPanel(new GridBagLayout());
        JButton browseOutput = new JButton("Browse...");
        browseOutput.addActionListener(e -> browseFor(outputFileField, true));
        outputFormatCombo.setSelectedItem(FileFormat.SEGY);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;

        int row = 0;
        c.gridy = row++; fileControls.add(new JLabel("Output file:"), c);
        c.gridy = row++; fileControls.add(outputFileField, c);
        c.gridy = row++; fileControls.add(browseOutput, c);
        c.gridy = row++; fileControls.add(new JLabel("Output format:"), c);
        c.gridy = row++; fileControls.add(outputFormatCombo, c);
        c.gridy = row++; fileControls.add(Box.createVerticalStrut(8), c);
        c.gridy = row++; fileControls.add(submitButton, c);

        CardLayout cardLayout = new CardLayout();
        JPanel settingsCards = new JPanel(cardLayout);
        settingsCards.add(outputSegySettingsPanel, FileFormat.SEGY.name());
        settingsCards.add(outputSegdSettingsPanel, FileFormat.SEGD.name());
        outputFormatCombo.addActionListener(e ->
        {
            FileFormat fmt = (FileFormat) outputFormatCombo.getSelectedItem();
            cardLayout.show(settingsCards, fmt.name());
            if (fmt == FileFormat.SEGY) outputSegySettingsPanel.refresh(); else outputSegdSettingsPanel.refresh();
            syncOutputSegyDefaultsFromInput();
        });

        panel.add(fileControls, BorderLayout.NORTH);
        panel.add(settingsCards, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildViewerPane()
    {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Preview"));

        JPanel previewControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        previewControls.add(new JLabel("Batch size:"));
        previewControls.add(batchSizeSpinner);
        previewControls.add(previewButton);
        panel.add(previewControls, BorderLayout.NORTH);
        panel.add(viewer, BorderLayout.CENTER);

        return panel;
    }

    private void browseFor(JTextField field, boolean saveDialog)
    {
        JFileChooser chooser = new JFileChooser();
        int result = saveDialog ? chooser.showSaveDialog(frame) : chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /** lets the user pick one or more input files (e.g. one file per FFID); joins the paths into inputFileField */
    private void browseInputFiles()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Select one or more input files");
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        File[] files = chooser.getSelectedFiles();
        if (files.length == 0) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.length; i++)
        {
            if (i > 0) sb.append("; ");
            sb.append(files[i].getAbsolutePath());
        }
        inputFileField.setText(sb.toString());
    }

    /** splits inputFileField's text (semicolon-separated) into individual, trimmed, non-empty file paths */
    private static List<String> splitFiles(String text)
    {
        List<String> result = new ArrayList<String>();
        if (text == null) return result;
        for (String part : text.split(";"))
        {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }

    private List<String> currentInputFiles()
    {
        return splitFiles(inputFileField.getText());
    }

    /** the first selected input file, or "" if none - used to seed the SEG-Y header preview */
    private String firstInputFile()
    {
        List<String> files = currentInputFiles();
        return files.isEmpty() ? "" : files.get(0);
    }

    /** commits any in-progress trace-header-schema table edits across all four inline settings panels */
    private void commitSettingsEdits()
    {
        inputSegySettingsPanel.commitEdits();
        inputSegdSettingsPanel.commitEdits();
        outputSegySettingsPanel.commitEdits();
        outputSegdSettingsPanel.commitEdits();
    }

    /**
     * When both input and output are SEG-Y, defaults the output's byte-offset settings
     * (binary header offsets, textual/binary/trace header lengths) and trace-header
     * schema to match the input's - a reasonable starting point for reformatting a file
     * with the same layout, since the trace data itself is copied through unchanged.
     * The user can still edit the output's settings independently afterward; this only
     * runs when the input or output format combo changes, not on every keystroke, so it
     * never overwrites edits the user has already made.
     */
    private void syncOutputSegyDefaultsFromInput()
    {
        FileFormat inFmt = (FileFormat) inputFormatCombo.getSelectedItem();
        FileFormat outFmt = (FileFormat) outputFormatCombo.getSelectedItem();
        if (inFmt == FileFormat.SEGY && outFmt == FileFormat.SEGY)
        {
            writerSegyConfig.copyFrom(readerSegyConfig);
            outputSegySettingsPanel.refresh();
        }
    }

    // ------------------------------------------------------------------
    // preview: read one batch of traces and hand them to the view
    // ------------------------------------------------------------------

    private void loadPreview()
    {
        List<String> inputFiles = currentInputFiles();
        if (inputFiles.isEmpty())
        {
            JOptionPane.showMessageDialog(frame, "Please choose at least one valid input file first.",
                "No input file", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (String path : inputFiles)
        {
            if (!new File(path).isFile())
            {
                JOptionPane.showMessageDialog(frame, "Not a valid file:\n" + path,
                    "No input file", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        commitSettingsEdits();
        setControlsEnabled(false);
        statusLabel.setText("Loading preview batch...");
        FileFormat format = (FileFormat) inputFormatCombo.getSelectedItem();
        int batchSize = (Integer) batchSizeSpinner.getValue();

        SwingWorker<SeismicTrace[], Void> worker = new SwingWorker<SeismicTrace[], Void>()
        {
            private BufferedFileReader reader;
            private Exception error;

            @Override
            protected SeismicTrace[] doInBackground()
            {
                try
                {
                    if (previewReader != null) previewReader.close();
                    reader = ReaderFactory.create(format, inputFiles, readerSegyConfig, readerSegdConfig);
                    reader.open();
                    return reader.readNextTraces(batchSize);
                }
                catch (Exception ex)
                {
                    error = ex;
                    return new SeismicTrace[0];
                }
            }

            @Override
            protected void done()
            {
                setControlsEnabled(true);
                if (error != null)
                {
                    statusLabel.setText("Error loading preview: " + error.getMessage());
                    JOptionPane.showMessageDialog(frame, "Failed to read input file:\n" + error.getMessage(),
                        "Read error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                previewReader = reader;
                try
                {
                    SeismicTrace[] traces = get();
                    viewer.setSampleRateMicros(previewReader.getSampleRateMicros());
                    viewer.setTraces(traces);
                    statusLabel.setText("Loaded " + traces.length + " trace(s) for preview. Adjust display, then Submit to reformat the whole file.");
                    submitButton.setEnabled(traces.length > 0);
                }
                catch (Exception ex)
                {
                    statusLabel.setText("Error loading preview: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ------------------------------------------------------------------
    // batch reformat: stream the whole file through reader -> processors -> writer
    // ------------------------------------------------------------------

    private void submitReformat()
    {
        List<String> inputFiles = currentInputFiles();
        String outputFile = outputFileField.getText().trim();
        if (inputFiles.isEmpty())
        {
            JOptionPane.showMessageDialog(frame, "Please choose at least one valid input file first.",
                "No input file", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (outputFile.isEmpty())
        {
            JOptionPane.showMessageDialog(frame, "Please choose an output file first.",
                "No output file", JOptionPane.WARNING_MESSAGE);
            return;
        }

        commitSettingsEdits();
        FileFormat inputFormat = (FileFormat) inputFormatCombo.getSelectedItem();
        FileFormat outputFormat = (FileFormat) outputFormatCombo.getSelectedItem();
        int batchSize = (Integer) batchSizeSpinner.getValue();
        // captured on the EDT since it reads a Swing text component; passed into the background worker below
        byte[] outputTextualHeaderOverride = outputSegySettingsPanel.getEffectiveTextualHeaderRaw();

        setControlsEnabled(false);
        String progressTitle = inputFiles.size() == 1
            ? new File(inputFiles.get(0)).getName()
            : inputFiles.size() + " files";
        ProgressMonitor progressMonitor = new ProgressMonitor(frame,
            "Reformatting " + progressTitle, "Starting...", 0, 100);
        progressMonitor.setMillisToDecideToPopup(0);
        progressMonitor.setMillisToPopup(0);

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>()
        {
            private Exception error;
            private boolean cancelled;

            @Override
            protected Void doInBackground()
            {
                try (BufferedFileReader reader = ReaderFactory.create(inputFormat, inputFiles, readerSegyConfig, readerSegdConfig))
                {
                    reader.open();
                    WriterConfig config = new WriterConfig(reader.getSampleRateMicros(), reader.getSamplesPerTrace());
                    if (inputFormat == FileFormat.SEGY && outputFormat == FileFormat.SEGY && !inputFiles.isEmpty())
                    {
                        try
                        {
                            SegyHeaderPreview inputHeaders = SegyBufferedFileReader.peekHeaders(inputFiles.get(0), readerSegyConfig);
                            config.binaryHeaderRaw = inputHeaders.binaryHeaderRaw;
                            // the textual header is user-editable on the Output tab; use whatever's there
                            // (edited or still the loaded/mirrored default) rather than re-peeking the input,
                            // falling back to the input's raw bytes only if nothing was ever loaded there
                            config.textualHeaderRaw = outputTextualHeaderOverride != null
                                ? outputTextualHeaderOverride : inputHeaders.textualHeaderRaw;
                        }
                        catch (IOException ignored)
                        {
                            // fall back to the writer's generic default textual/binary header
                        }
                    }
                    TraceWriter writer = WriterFactory.create(outputFormat, writerSegyConfig, writerSegdConfig);
                    try
                    {
                        writer.open(outputFile, config);
                        while (reader.hasMoreTraces())
                        {
                            if (progressMonitor.isCanceled())
                            {
                                cancelled = true;
                                break;
                            }
                            SeismicTrace[] batch = reader.readNextTraces(batchSize);
                            if (batch.length == 0) break;
                            writer.writeTraces(batch);

                            long total = Math.max(1, reader.getTotalBytes());
                            int percent = (int) Math.min(100, (100L * reader.getBytesRead()) / total);
                            publish(percent);
                        }
                    }
                    finally
                    {
                        writer.close();
                    }
                }
                catch (Exception ex)
                {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks)
            {
                int latest = chunks.get(chunks.size() - 1);
                progressMonitor.setProgress(latest);
                progressMonitor.setNote("Reformatted " + latest + "% of file...");
            }

            @Override
            protected void done()
            {
                progressMonitor.close();
                setControlsEnabled(true);
                if (error != null)
                {
                    statusLabel.setText("Reformat failed: " + error.getMessage());
                    JOptionPane.showMessageDialog(frame, "Reformat failed:\n" + error.getMessage(),
                        "Reformat error", JOptionPane.ERROR_MESSAGE);
                }
                else if (cancelled)
                {
                    statusLabel.setText("Reformat cancelled by user.");
                }
                else
                {
                    statusLabel.setText("Reformat complete: " + outputFile);
                    JOptionPane.showMessageDialog(frame, "Reformatting complete:\n" + outputFile,
                        "Done", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void setControlsEnabled(boolean enabled)
    {
        previewButton.setEnabled(enabled);
        submitButton.setEnabled(enabled && previewReader != null);
        inputFileField.setEnabled(enabled);
        outputFileField.setEnabled(enabled);
        inputFormatCombo.setEnabled(enabled);
        outputFormatCombo.setEnabled(enabled);
        batchSizeSpinner.setEnabled(enabled);
    }

    public void show()
    {
        frame.setVisible(true);
    }
}
