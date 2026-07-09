package com.tricongeophysics.view;

import com.tricongeophysics.SeismicTrace;
import com.tricongeophysics.model.AGCProcessor;
import com.tricongeophysics.model.BandpassFilterProcessor;
import com.tricongeophysics.model.GainProcessor;
import com.tricongeophysics.model.TraceProcessor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * MVC View. Displays a batch of SeismicTrace objects vertically (time
 * increasing downward), with toggleable AGC / bandpass filter / gain processing
 * and a choice of Variable Density or Variable Area Wiggle (VAWG) rendering.
 *
 * The TraceMonitor controller feeds trace batches in via setTraces()/
 * setSampleRateMicros() and, when the user clicks Submit, reads back
 * getActiveProcessors() so the exact same processing the user previewed is
 * applied to every trace written out during the batch reformat.
 */
public class TraceViewer extends JPanel {
	private static final int BASE_TRACE_SPACING = 4;
	private static final int MIN_HEADER_AREA_HEIGHT = 26;
	private static final int LEFT_MARGIN = 50;
	private static final double BASE_PIXELS_PER_SAMPLE = 3.0;
	private static final int HEADER_TEXT_ROW_HEIGHT = 11;
	private static final int HEADER_GRAPH_HEIGHT = 70;
	private static final int HEADER_GRAPH_LEGEND_HEIGHT = 12;
	private static final Color[] HEADER_COLORS = {
		new Color(200, 30, 30), new Color(30, 100, 200), new Color(30, 150, 30),
		new Color(200, 130, 0), new Color(150, 30, 180), new Color(0, 150, 150),
		new Color(140, 90, 40), new Color(90, 90, 90)
	};

	private SeismicTrace[] traces = new SeismicTrace[0];
	private int sampleRateMicros = 4000;

	private DisplayMode displayMode = DisplayMode.VAWG;
	private double zoom = 1.0;

	private final JCheckBox headerCheck = new JCheckBox("Show headers", true);
	private final JCheckBox headerTextCheck = new JCheckBox("Text", true);
	private final JCheckBox headerGraphCheck = new JCheckBox("Graph", false);
	private final JButton headersButton = new JButton("Choose Headers...");
	private String[] availableHeaderNames = new String[0];
	private final Set<String> selectedHeaderNames = new LinkedHashSet<String>();
	private final JCheckBox agcCheck = new JCheckBox("AGC");
	private final JSpinner agcWindowSpinner = new JSpinner(new SpinnerNumberModel(250.0, 10.0, 5000.0, 10.0));
	private final JCheckBox filterCheck = new JCheckBox("Filter");
	private final JSpinner filterLowSpinner = new JSpinner(new SpinnerNumberModel(5.0, 0.0, 500.0, 1.0));
	private final JSpinner filterHighSpinner = new JSpinner(new SpinnerNumberModel(80.0, 1.0, 1000.0, 1.0));
//    private final JCheckBox gainCheck = new JCheckBox("Gain");
	private final JSpinner gainSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 1000.0, 0.5));
	private final JComboBox<DisplayMode> modeCombo = new JComboBox<DisplayMode>(DisplayMode.values());
	private final JLabel zoomLabel = new JLabel("100%");

	private final TraceCanvas canvas = new TraceCanvas();
	private float[][] processedCache;
	private boolean cacheDirty = true;
	private double gain = 1;

	public TraceViewer() {
		super(new BorderLayout());
		add(buildToolBar(), BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(canvas);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		add(scrollPane, BorderLayout.CENTER);
	}

	private JToolBar buildToolBar() {
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);

		modeCombo.setSelectedItem(displayMode);
		modeCombo.addActionListener(e -> {
			displayMode = (DisplayMode) modeCombo.getSelectedItem();
			repaint();
		});
		bar.add(new JLabel("Display: "));
		bar.add(modeCombo);
		bar.addSeparator();

		headerCheck.addActionListener(e -> repaint());
		bar.add(headerCheck);
		headerTextCheck.addActionListener(e -> { canvas.revalidate(); canvas.repaint(); });
		bar.add(headerTextCheck);
		headerGraphCheck.addActionListener(e -> { canvas.revalidate(); canvas.repaint(); });
		bar.add(headerGraphCheck);
		headersButton.addActionListener(e -> openHeaderSelectionDialog());
		bar.add(headersButton);
		bar.addSeparator();

		agcCheck.addActionListener(e -> invalidateCacheAndRepaint());
		agcWindowSpinner.addChangeListener(e -> {
			if (agcCheck.isSelected())
				invalidateCacheAndRepaint();
		});
		bar.add(agcCheck);
		bar.add(new JLabel(" window(ms):"));
		agcWindowSpinner.setMaximumSize(new Dimension(70, 25));
		bar.add(agcWindowSpinner);
		bar.addSeparator();

		filterCheck.addActionListener(e -> invalidateCacheAndRepaint());
		filterLowSpinner.addChangeListener(e -> {
			if (filterCheck.isSelected())
				invalidateCacheAndRepaint();
		});
		filterHighSpinner.addChangeListener(e -> {
			if (filterCheck.isSelected())
				invalidateCacheAndRepaint();
		});
		bar.add(filterCheck);
		bar.add(new JLabel(" low(Hz):"));
		filterLowSpinner.setMaximumSize(new Dimension(60, 25));
		bar.add(filterLowSpinner);
		bar.add(new JLabel(" high(Hz):"));
		filterHighSpinner.setMaximumSize(new Dimension(60, 25));
		bar.add(filterHighSpinner);
		bar.addSeparator();

//        gainCheck.addActionListener(e -> invalidateCacheAndRepaint());
//        gainSpinner.addChangeListener(e -> { if (gainCheck.isSelected()) invalidateCacheAndRepaint(); });
		gainSpinner.addChangeListener(e -> {
			invalidateCacheAndRepaint();
		});
//        bar.add(gainCheck);
		bar.add(new JLabel("Gain"));
		bar.add(gainSpinner);
		bar.add(new JLabel(" x:"));
		gainSpinner.setMaximumSize(new Dimension(60, 25));
		bar.add(gainSpinner);
		bar.addSeparator();

		JButton zoomOut = new JButton("Zoom -");
		JButton zoomIn = new JButton("Zoom +");
		zoomOut.addActionListener(e -> setZoom(zoom * 0.8));
		zoomIn.addActionListener(e -> setZoom(zoom * 1.25));
		bar.add(zoomOut);
		bar.add(zoomLabel);
		bar.add(zoomIn);

		return bar;
	}

	private void setZoom(double newZoom) {
		zoom = Math.max(0.1, Math.min(newZoom, 10.0));
		zoomLabel.setText(Math.round(zoom * 100) + "%");
		canvas.revalidate();
		canvas.repaint();
	}

	/** replaces the batch of traces currently on screen (called by TraceMonitor) */
	public void setTraces(SeismicTrace[] traces) {
		this.traces = traces == null ? new SeismicTrace[0] : traces;
		refreshAvailableHeaderNames();
		invalidateCacheAndRepaint();
		canvas.revalidate();
	}

	/** rebuilds the list of header names available to pick from, based on the current batch of traces */
	private void refreshAvailableHeaderNames() {
		if (traces.length == 0) {
			availableHeaderNames = new String[0];
			return;
		}
		LinkedHashSet<String> uniq = new LinkedHashSet<String>();
		for (String n : traces[0].getHeaderList()) {
			if (n == null) continue;
			String trimmed = n.trim();
			if (!trimmed.isEmpty()) uniq.add(trimmed);
		}
		availableHeaderNames = uniq.toArray(new String[0]);
		selectedHeaderNames.retainAll(uniq);
		if (selectedHeaderNames.isEmpty()) {
			for (int i = 0; i < Math.min(2, availableHeaderNames.length); i++) {
				selectedHeaderNames.add(availableHeaderNames[i]);
			}
		}
	}

	/** opens a small modal dialog letting the user pick which trace headers to display along the top */
	private void openHeaderSelectionDialog() {
		Window owner = SwingUtilities.getWindowAncestor(this);
		JDialog dialog = new JDialog(owner, "Choose Headers to Display", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setLayout(new BorderLayout(4, 4));

		JPanel checkPanel = new JPanel();
		checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
		List<JCheckBox> boxes = new ArrayList<JCheckBox>();
		if (availableHeaderNames.length == 0) {
			checkPanel.add(new JLabel("Load a preview batch first."));
		}
		for (String name : availableHeaderNames) {
			JCheckBox cb = new JCheckBox(name, selectedHeaderNames.contains(name));
			boxes.add(cb);
			checkPanel.add(cb);
		}
		dialog.add(new JScrollPane(checkPanel), BorderLayout.CENTER);

		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");
		ok.addActionListener(e -> {
			selectedHeaderNames.clear();
			for (JCheckBox cb : boxes) {
				if (cb.isSelected()) selectedHeaderNames.add(cb.getText());
			}
			dialog.dispose();
			canvas.revalidate();
			canvas.repaint();
		});
		cancel.addActionListener(e -> dialog.dispose());
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(ok);
		buttons.add(cancel);
		dialog.add(buttons, BorderLayout.SOUTH);

		dialog.setSize(260, 400);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	/** height in pixels of the stacked per-trace header-value text rows, or 0 if that display is off */
	private int textRowsHeight() {
		if (!headerCheck.isSelected() || !headerTextCheck.isSelected() || selectedHeaderNames.isEmpty()) return 0;
		return 15 + selectedHeaderNames.size() * HEADER_TEXT_ROW_HEIGHT;
	}

	/** height in pixels of the multi-header line graph (legend row + plot), or 0 if that display is off */
	private int graphAreaHeight() {
		if (!headerCheck.isSelected() || !headerGraphCheck.isSelected() || selectedHeaderNames.isEmpty()) return 0;
		return HEADER_GRAPH_LEGEND_HEIGHT + HEADER_GRAPH_HEIGHT;
	}

	/** the header-strip height in pixels: graph (if on) stacked above text rows (if on), above the traces */
	private int headerAreaHeight() {
		int total = graphAreaHeight() + textRowsHeight();
		return total == 0 ? MIN_HEADER_AREA_HEIGHT : Math.max(MIN_HEADER_AREA_HEIGHT, total + 4);
	}

	/** assigns each selected header a stable color, by its position in the (ordered) selection */
	private static Color headerColor(int index) {
		return HEADER_COLORS[index % HEADER_COLORS.length];
	}

	public void setSampleRateMicros(int sampleRateMicros) {
		this.sampleRateMicros = sampleRateMicros;
		invalidateCacheAndRepaint();
	}

	private void invalidateCacheAndRepaint() {
		cacheDirty = true;
		canvas.repaint();
	}

	/**
	 * The processing chain currently selected in the UI, in the order it is
	 * applied: bandpass filter, then AGC, then a final scalar gain. Exposed so the
	 * controller can apply the exact same chain during the batch reformat.
	 */
	public List<TraceProcessor> getActiveProcessors() {
		List<TraceProcessor> list = new ArrayList<TraceProcessor>();
		if (filterCheck.isSelected()) {
			double low = ((Number) filterLowSpinner.getValue()).doubleValue();
			double high = ((Number) filterHighSpinner.getValue()).doubleValue();
			list.add(new BandpassFilterProcessor(low, high));
		}
		if (agcCheck.isSelected()) {
			double window = ((Number) agcWindowSpinner.getValue()).doubleValue();
			list.add(new AGCProcessor(window));
		}
//        if (gainCheck.isSelected())
//        {
		gain = ((Number) gainSpinner.getValue()).doubleValue();
		list.add(new GainProcessor(gain));
//        }
		return list;
	}

	private float[][] getProcessedSamples() {
		if (cacheDirty || processedCache == null) {
			List<TraceProcessor> chain = getActiveProcessors();
			processedCache = new float[traces.length][];
			for (int i = 0; i < traces.length; i++) {
				float[] samples = traces[i].getData();
				for (TraceProcessor p : chain) {
					samples = p.process(samples, sampleRateMicros);
				}
				processedCache[i] = samples;
			}
			cacheDirty = false;
		}
		return processedCache;
	}

	private static float computeMaxAbs(float[][] processed) {
		float max = 0;
		for (float[] trace : processed) {
			for (float v : trace) {
				float a = Math.abs(v);
				if (a > max)
					max = a;
			}
		}
		return max;
	}

	// ------------------------------------------------------------------
	// rendering canvas
	// ------------------------------------------------------------------

	private class TraceCanvas extends JPanel {
		TraceCanvas() {
			setBackground(Color.WHITE);
		}

		@Override
		public Dimension getPreferredSize() {
			int spacing = (int) Math.round(BASE_TRACE_SPACING * zoom);
			int width = LEFT_MARGIN * 2 + traces.length * spacing;
			int maxSamples = 0;
			for (SeismicTrace t : traces) {
				maxSamples = Math.max(maxSamples, t.getData().length);
			}
			int height = headerAreaHeight() + (int) Math.round(maxSamples * BASE_PIXELS_PER_SAMPLE * zoom) + 10;
			return new Dimension(Math.max(width, 200), Math.max(height, 200));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (traces.length == 0)
				return;

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			float[][] processed = getProcessedSamples();
			float maxAbs = computeMaxAbs(processed);
			double pixelsPerSample = BASE_PIXELS_PER_SAMPLE * zoom;
			int pixelsPerTrace = (int) Math.round(BASE_TRACE_SPACING * zoom);
			if (pixelsPerTrace == 0) {
				pixelsPerTrace = 1;
			}
			drawYAxis(g2, pixelsPerSample, traces[0]);
			if (headerCheck.isSelected() && headerGraphCheck.isSelected()) {
				drawHeaderGraph(g2, pixelsPerTrace);
			}

			for (int i = 0; i < processed.length; i++) {
				int x = LEFT_MARGIN + i * pixelsPerTrace + pixelsPerTrace / 2;
				if (headerCheck.isSelected() && headerTextCheck.isSelected()) {
					int gap = 50 / pixelsPerTrace;
					if (gap < 2) {
						gap = 2;
					}
					int mod = i % gap;
					if (mod == 0) {
						drawHeaderLabel(g2, x, traces[i]);
					}
				}
				if (displayMode == DisplayMode.VARIABLE_DENSITY) {
					drawDensityTrace(g2, x, pixelsPerTrace, processed[i], maxAbs, pixelsPerSample);
				} else {
					drawWiggleTrace(g2, x, pixelsPerTrace, processed[i], maxAbs, pixelsPerSample);
				}
			}
		}

		private void drawYAxis(Graphics2D g2, double pixelsPerSample, SeismicTrace trace) {
			Font old = g2.getFont();
			g2.setFont(old.deriveFont(9f));
			FontMetrics fm = g2.getFontMetrics();

			// one row per header currently being displayed as text, labeled with that header's actual
			// name (not just whatever happens to be at index 0), aligned with drawHeaderLabel's rows
			if (headerCheck.isSelected() && headerTextCheck.isSelected()) {
				String[] headerNameLabels = selectedHeaderNames.toArray(new String[0]);
				int rowY = graphAreaHeight() + 11;
				for (int i = 0; i < headerNameLabels.length; i++) {
					g2.setColor(headerColor(i));
					g2.drawString(headerNameLabels[i], 0, rowY);
					rowY += HEADER_TEXT_ROW_HEIGHT;
				}
			}

			g2.setColor(Color.DARK_GRAY);
			g2.setFont(old);
			int length = trace.getData().length;
			for (int i = 0; i < length; i += 50) {
				int ipixels = (int) (i * pixelsPerSample);
				int time = i * sampleRateMicros / 1000;
				String timeLabel = String.valueOf(time);
				g2.drawString(timeLabel, 0, headerAreaHeight() + ipixels + fm.getHeight() / 2);
			}

		}

		/**
		 * multi-header line graph: each selected header is plotted as its own
		 * colored polyline across the batch of traces, independently normalized
		 * to that header's own min/max so headers with very different ranges
		 * (e.g. CDP vs. OFFSET) can share the same vertical space without one
		 * flattening the other.
		 */
		private void drawHeaderGraph(Graphics2D g2, int pixelsPerTrace) {
			if (selectedHeaderNames.isEmpty() || traces.length == 0) return;
			String[] names = selectedHeaderNames.toArray(new String[0]);

			int legendTop = 2;
			int plotTop = HEADER_GRAPH_LEGEND_HEIGHT;
			int plotHeight = HEADER_GRAPH_HEIGHT - 4;
			int plotBottom = plotTop + plotHeight;
			int width = LEFT_MARGIN + traces.length * pixelsPerTrace;

			g2.setColor(new Color(248, 248, 248));
			g2.fillRect(0, 0, width, HEADER_GRAPH_LEGEND_HEIGHT + HEADER_GRAPH_HEIGHT);
			g2.setColor(new Color(215, 215, 215));
			g2.drawRect(0, plotTop, width - 1, plotHeight);

			Font old = g2.getFont();
			g2.setFont(old.deriveFont(9f));
			FontMetrics fm = g2.getFontMetrics();
			int legendX = LEFT_MARGIN;

			for (int h = 0; h < names.length; h++) {
				String name = names[h];
				double min = Double.POSITIVE_INFINITY;
				double max = Double.NEGATIVE_INFINITY;
				for (SeismicTrace t : traces) {
					double v = t.getHeaderValue(name);
					if (v < min) min = v;
					if (v > max) max = v;
				}
				double range = max - min;
				if (range < 1e-9) range = 1;

				Color color = headerColor(h);
				int[] xs = new int[traces.length];
				int[] ys = new int[traces.length];
				for (int i = 0; i < traces.length; i++) {
					double v = traces[i].getHeaderValue(name);
					double norm = (v - min) / range;
					xs[i] = LEFT_MARGIN + i * pixelsPerTrace + pixelsPerTrace / 2;
					ys[i] = plotBottom - (int) Math.round(norm * (plotHeight - 2)) - 1;
				}
				g2.setColor(color);
				g2.drawPolyline(xs, ys, traces.length);

				String legend = name + " [" + formatHeaderValue(min) + ".." + formatHeaderValue(max) + "]";
				g2.drawLine(legendX, legendTop + 5, legendX + 12, legendTop + 5);
				g2.drawString(legend, legendX + 15, legendTop + 9);
				legendX += 15 + fm.stringWidth(legend) + 12;
			}
			g2.setFont(old);
		}

		private void drawHeaderLabel(Graphics2D g2, int x, SeismicTrace trace) {
			if (selectedHeaderNames.isEmpty())
				return;
			Font old = g2.getFont();
			g2.setFont(old.deriveFont(9f));
			FontMetrics fm = g2.getFontMetrics();
			int y = graphAreaHeight() + 11;
			int i = 0;
			for (String name : selectedHeaderNames) {
				String label = formatHeaderValue(trace.getHeaderValue(name));
				g2.setColor(headerColor(i));
				g2.drawString(label, x - fm.stringWidth(label) / 2, y);
				y += HEADER_TEXT_ROW_HEIGHT;
				i++;
			}
			g2.setFont(old);
		}

		private String formatHeaderValue(double v) {
			if (!Double.isInfinite(v) && v == Math.rint(v)) return String.valueOf((long) v);
			return String.format("%.2f", v);
		}

		/**
		 * classic variable-area wiggle trace: positive excursions shaded solid to the
		 * right of the baseline
		 */
		private void drawWiggleTrace(Graphics2D g2, int x, int spacing, float[] samples, float maxAbs,
				double pixelsPerSample) {
			int n = samples.length;
			if (n == 0 || maxAbs <= 0)
				return;
			double halfWidth = spacing * gain * 2;

			int[] fillXs = new int[2 * n];
			int[] fillYs = new int[2 * n];
			int[] lineXs = new int[n];
			int[] lineYs = new int[n];

			for (int j = 0; j < n; j++) {
				double amp = samples[j] / maxAbs;
				int y = headerAreaHeight() + (int) Math.round(j * pixelsPerSample);
				double dx = amp * halfWidth;
				fillXs[j] = (int) Math.round(x + Math.max(dx, 0));
				fillYs[j] = y;
				lineXs[j] = (int) Math.round(x + dx);
				lineYs[j] = y;
			}
			for (int j = 0; j < n; j++) {
				int y = headerAreaHeight() + (int) Math.round((n - 1 - j) * pixelsPerSample);
				fillXs[n + j] = x;
				fillYs[n + j] = y;
			}

			g2.setColor(new Color(20, 20, 20));
			g2.fillPolygon(fillXs, fillYs, 2 * n);
			g2.setColor(Color.BLACK);
			g2.drawPolyline(lineXs, lineYs, n);
		}

		/**
		 * variable density raster: each sample rendered as a colored band
		 * (red=positive, blue=negative)
		 */
		private void drawDensityTrace(Graphics2D g2, int x, int spacing, float[] samples, float maxAbs,
				double pixelsPerSample) {
			int n = samples.length;
			if (n == 0)
				return;
			int bandHeight = Math.max(1, (int) Math.ceil(pixelsPerSample));
//			int bandWidth = Math.max(5, spacing - 4);
			int bandWidth = spacing;
			for (int j = 0; j < n; j++) {
				double amp = maxAbs > 0 ? samples[j] / maxAbs : 0;
				g2.setColor(amplitudeToColor(amp * gain));
				int y = headerAreaHeight() + (int) Math.round(j * pixelsPerSample);
				g2.fillRect(x - bandWidth / 2, y, bandWidth, bandHeight);
			}
		}

		private Color amplitudeToColor(double amp) {
			amp = Math.max(-1.0, Math.min(1.0, amp));
			if (amp >= 0) {
				int c = (int) Math.round(255 * (1 - amp));
				return new Color(255, c, c);
			} else {
				int c = (int) Math.round(255 * (1 + amp));
				return new Color(c, c, 255);
			}
		}
	}
}
