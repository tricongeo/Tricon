package com.tricongeophysics.view;

import com.tricongeophysics.SeismicTrace;
import com.tricongeophysics.model.AGCProcessor;
import com.tricongeophysics.model.BandpassFilterProcessor;
import com.tricongeophysics.model.GainProcessor;
import com.tricongeophysics.model.TraceProcessor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
	private static final int HEADER_AREA_HEIGHT = 26;
	private static final int LEFT_MARGIN = 50;
	private static final double BASE_PIXELS_PER_SAMPLE = 3.0;

	private SeismicTrace[] traces = new SeismicTrace[0];
	private int sampleRateMicros = 4000;

	private DisplayMode displayMode = DisplayMode.VAWG;
	private double zoom = 1.0;

	private final JCheckBox headerCheck = new JCheckBox("Show headers", true);
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
		invalidateCacheAndRepaint();
		canvas.revalidate();
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
			int height = HEADER_AREA_HEIGHT + (int) Math.round(maxSamples * BASE_PIXELS_PER_SAMPLE * zoom) + 10;
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

			for (int i = 0; i < processed.length; i++) {
				int x = LEFT_MARGIN + i * pixelsPerTrace + pixelsPerTrace / 2;
				if (headerCheck.isSelected()) {
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
			String label = trace.getHeaderList()[0];
			Font old = g2.getFont();
			g2.setFont(old.deriveFont(9f));
			g2.setColor(Color.DARK_GRAY);
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(label, 0, HEADER_AREA_HEIGHT - 14);
			g2.setFont(old);
			int length = trace.getLength();
			for (int i = 0; i < length; i += 50) {
				int ipixels = (int) (i * pixelsPerSample);
				int time = i * sampleRateMicros / 1000;
				label = String.valueOf(time);
				g2.drawString(label, 0, HEADER_AREA_HEIGHT + ipixels + fm.getHeight() / 2);
			}

		}

		private void drawHeaderLabel(Graphics2D g2, int x, SeismicTrace trace) {
			double[] headers = trace.getHeaders();
			if (headers.length == 0)
				return;
			String label = String.valueOf((long) headers[0]);
			Font old = g2.getFont();
			g2.setFont(old.deriveFont(9f));
			g2.setColor(Color.DARK_GRAY);
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(label, x - fm.stringWidth(label) / 2, HEADER_AREA_HEIGHT - 4);
			g2.setFont(old);
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
				int y = HEADER_AREA_HEIGHT + (int) Math.round(j * pixelsPerSample);
				double dx = amp * halfWidth;
				fillXs[j] = (int) Math.round(x + Math.max(dx, 0));
				fillYs[j] = y;
				lineXs[j] = (int) Math.round(x + dx);
				lineYs[j] = y;
			}
			for (int j = 0; j < n; j++) {
				int y = HEADER_AREA_HEIGHT + (int) Math.round((n - 1 - j) * pixelsPerSample);
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
				int y = HEADER_AREA_HEIGHT + (int) Math.round(j * pixelsPerSample);
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
