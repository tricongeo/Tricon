package com.tricongeophysics.controller;

import com.tricongeophysics.SeismicTrace;
import com.tricongeophysics.model.BufferedFileReader;
import com.tricongeophysics.model.FileFormat;
import com.tricongeophysics.model.ReaderFactory;
import com.tricongeophysics.model.TraceProcessor;
import com.tricongeophysics.model.TraceWriter;
import com.tricongeophysics.model.WriterConfig;
import com.tricongeophysics.model.WriterFactory;
import com.tricongeophysics.view.TraceViewer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * MVC Controller. Owns the file-selection UI, uses the model reader classes to
 * pull a batch of traces (default 10000) at a time and hands them to the
 * TraceViewer for preview. When the user is happy with the preview and clicks
 * "Submit", the entire input file is streamed through the same reader/writer
 * pair in batches, applying whatever processing chain is currently active in
 * the viewer, with a ProgressMonitor showing progress.
 */
public class TraceMonitor
{
    private final JFrame frame;
    private final TraceViewer viewer;

    private final JTextField inputFileField = new JTextField(28);
    private final JTextField outputFileField = new JTextField(28);
    private final JComboBox<FileFormat> inputFormatCombo = new JComboBox<FileFormat>(FileFormat.values());
    private final JComboBox<FileFormat> outputFormatCombo = new JComboBox<FileFormat>(FileFormat.values());
    private final JSpinner batchSizeSpinner = new JSpinner(
        new SpinnerNumberModel(BufferedFileReader.DEFAULT_BATCH_SIZE, 1, 1_000_000, 1000));
    private final JButton previewButton = new JButton("Load Preview");
    private final JButton submitButton = new JButton("Submit (Reformat File)");
    private final JLabel statusLabel = new JLabel("Select an input file and click Load Preview.");

    private BufferedFileReader previewReader;

    public TraceMonitor()
    {
        viewer = new TraceViewer();
        frame = new JFrame("TriTape");
        buildUI();
        inputFileField.setText("/home/scott/Projects/develop/tritape/jetson_test_shots.sgy");
        previewButton.doClick();
    }

    private void buildUI()
    {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(buildFileSelectionPanel(), BorderLayout.NORTH);
        frame.add(viewer, BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);

        previewButton.addActionListener(e -> loadPreview());
        submitButton.addActionListener(e -> submitReformat());
        submitButton.setEnabled(false);

        frame.setSize(1400, 800);
        frame.setLocationRelativeTo(null);
    }

    private JPanel buildFileSelectionPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.WEST;

        JButton browseInput = new JButton("Browse...");
        browseInput.addActionListener(e -> browseFor(inputFileField, false));
        JButton browseOutput = new JButton("Browse...");
        browseOutput.addActionListener(e -> browseFor(outputFileField, true));

        int row = 0;
        c.gridx = 0; c.gridy = row; panel.add(new JLabel("Input file:"), c);
        c.gridx = 1; panel.add(inputFileField, c);
        c.gridx = 2; panel.add(browseInput, c);
        c.gridx = 3; panel.add(new JLabel("Input format:"), c);
        c.gridx = 4; panel.add(inputFormatCombo, c);

        row++;
        c.gridx = 0; c.gridy = row; panel.add(new JLabel("Output file:"), c);
        c.gridx = 1; panel.add(outputFileField, c);
        c.gridx = 2; panel.add(browseOutput, c);
        c.gridx = 3; panel.add(new JLabel("Output format:"), c);
        c.gridx = 4; panel.add(outputFormatCombo, c);
        outputFormatCombo.setSelectedItem(FileFormat.SEGY);

        row++;
        c.gridx = 0; c.gridy = row; panel.add(new JLabel("Batch size:"), c);
        c.gridx = 1; panel.add(batchSizeSpinner, c);
        c.gridx = 2; panel.add(previewButton, c);
        c.gridx = 3; c.gridwidth = 2; panel.add(submitButton, c);
        c.gridwidth = 1;

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

    // ------------------------------------------------------------------
    // preview: read one batch of traces and hand them to the view
    // ------------------------------------------------------------------

    private void loadPreview()
    {
        String inputFile = inputFileField.getText().trim();
        if (inputFile.isEmpty() || !new File(inputFile).isFile())
        {
            JOptionPane.showMessageDialog(frame, "Please choose a valid input file first.",
                "No input file", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
                    reader = ReaderFactory.create(format, inputFile);
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
        String inputFile = inputFileField.getText().trim();
        String outputFile = outputFileField.getText().trim();
        if (outputFile.isEmpty())
        {
            JOptionPane.showMessageDialog(frame, "Please choose an output file first.",
                "No output file", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FileFormat inputFormat = (FileFormat) inputFormatCombo.getSelectedItem();
        FileFormat outputFormat = (FileFormat) outputFormatCombo.getSelectedItem();
        int batchSize = (Integer) batchSizeSpinner.getValue();
        List<TraceProcessor> processors = viewer.getActiveProcessors();

        setControlsEnabled(false);
        ProgressMonitor progressMonitor = new ProgressMonitor(frame,
            "Reformatting " + new File(inputFile).getName(), "Starting...", 0, 100);
        progressMonitor.setMillisToDecideToPopup(0);
        progressMonitor.setMillisToPopup(0);

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>()
        {
            private Exception error;
            private boolean cancelled;

            @Override
            protected Void doInBackground()
            {
                try (BufferedFileReader reader = ReaderFactory.create(inputFormat, inputFile))
                {
                    reader.open();
                    WriterConfig config = new WriterConfig(reader.getSampleRateMicros(), reader.getSamplesPerTrace());
                    TraceWriter writer = WriterFactory.create(outputFormat);
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
                            applyProcessors(batch, processors, reader.getSampleRateMicros());
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

    /** applies the view's currently-active processing chain to every trace in place, in order */
    private static void applyProcessors(SeismicTrace[] traces, List<TraceProcessor> processors, int sampleRateMicros)
    {
        if (processors.isEmpty()) return;
        for (SeismicTrace trace : traces)
        {
            float[] samples = trace.getData();
            for (TraceProcessor p : processors)
            {
                samples = p.process(samples, sampleRateMicros);
            }
            trace.setData(samples);
        }
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
