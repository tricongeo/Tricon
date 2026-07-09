package com.tricongeophysics.view;

import com.tricongeophysics.SeismicTrace;
import com.tricongeophysics.model.AGCProcessor;
import com.tricongeophysics.model.BandpassFilterProcessor;
import com.tricongeophysics.model.GainProcessor;
import com.tricongeophysics.model.TraceProcessor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * MVC View. Displays a batch of SeismicTrace objects vertically (time
 * increasing downward), with toggleable AGC / bandpass filter / gain processing
 * and a choice of Variable Density or Variable Area Wiggle (VAWG) rendering.
 *
 * The trace data lives in its own scrollable/pannable/zoomable canvas, while
 * the header rows + graph (X-axis) and the time axis (Y-axis) are rendered in
 * JScrollPane's column-header/row-header views. Those views only scroll along
 * with their corresponding axis of the main viewport and are otherwise fixed,
 * so the axis displays stay visible no matter how the trace data is panned or
 * scrolled - including via the scrollbars, since JScrollPane keeps the header
 * views synced to the main viewport automatically.
 *
 * The TraceMonitor controller feeds trace batches in via setTraces()/
 * setSampleRateMicros() and, when the user clicks Submit, reads back
 * getActiveProcessors() so the exact same processing the user previewed is
 * applied to every trace written out during the batch reformat.
 */
public class TraceViewer extends JPanel {
	private static final int BASE_TRACE_SPACING = 4;
	private static final int MIN_HEADER_AREA_HEIGHT = 26;
	private static final int LEFT_MARGIN = 50; // width of the fixed Y-axis (row header) column
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
	private double zoomX = 1.0;
	private double zoomY = 1.0;
	private MouseMode mouseMode = MouseMode.PAN;
	private final Deque<ViewState> zoomHistory = new ArrayDeque<ViewState>();
	private static final Cursor ZOOM_CURSOR = createZoomCursor();
	private int hoveredTraceIndex = -1;
	private int hoveredY = -1;

	/** a saved zoom level (both axes) + scroll position, pushed before each rectangle-zoom so a click can undo it */
	private static final class ViewState {
		final double zoomX;
		final double zoomY;
		final int viewX;
		final int viewY;

		ViewState(double zoomX, double zoomY, int viewX, int viewY) {
			this.zoomX = zoomX;
			this.zoomY = zoomY;
			this.viewX = viewX;
			this.viewY = viewY;
		}
	}

	private static Cursor createZoomCursor() {
		int size = 24;
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(2f));
		g.drawOval(1, 1, 12, 12);
		g.drawLine(11, 11, 19, 19);
		g.dispose();
		return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(1, 1), "zoom");
	}

	private final JCheckBox headerCheck = new JCheckBox("Show headers", true);
	private final JCheckBox headerTextCheck = new JCheckBox("Text", true);
	private final JCheckBox headerGraphCheck = new JCheckBox("Graph", true);
	private final JButton headersButton = new JButton("Choose Headers...");
	private String[] availableHeaderNames = new String[0];
	private final Set<String> selectedHeaderNames = new LinkedHashSet<String>();
	private final JCheckBox agcCheck = new JCheckBox("AGC", true);
	private final JSpinner agcWindowSpinner = new JSpinner(new SpinnerNumberModel(250.0, 10.0, 5000.0, 10.0));
	private final JCheckBox filterCheck = new JCheckBox("Filter");
	private final JSpinner filterLowSpinner = new JSpinner(new SpinnerNumberModel(5.0, 0.0, 500.0, 1.0));
	private final JSpinner filterHighSpinner = new JSpinner(new SpinnerNumberModel(80.0, 1.0, 1000.0, 1.0));
//    private final JCheckBox gainCheck = new JCheckBox("Gain");
	private final JSpinner gainSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 1000.0, 0.5));
	private final JComboBox<DisplayMode> modeCombo = new JComboBox<DisplayMode>(DisplayMode.values());
	private final JLabel zoomLabel = new JLabel("X:100%  Y:100%");

	private final HeaderColumnPanel headerColumnPanel = new HeaderColumnPanel();
	private final YAxisPanel yAxisPanel = new YAxisPanel();
	private final CornerPanel cornerPanel = new CornerPanel();
	private final TraceCanvas canvas = new TraceCanvas();
	private JScrollPane scrollPane;
	private float[][] processedCache;
	private boolean cacheDirty = true;
	private double gain = 1;

	public TraceViewer() {
		super(new BorderLayout());
		add(buildToolBar(), BorderLayout.NORTH);
		scrollPane = new JScrollPane(canvas);
		scrollPane.setColumnHeaderView(headerColumnPanel);
		scrollPane.setRowHeaderView(yAxisPanel);
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, cornerPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		add(scrollPane, BorderLayout.CENTER);
		setMouseMode(mouseMode);
	}

	private JPanel buildToolBar() {
		JPanel bar = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 4));

		JToggleButton panButton = new JToggleButton("Pan", true);
		JToggleButton zoomModeButton = new JToggleButton("Zoom");
		ButtonGroup mouseModeGroup = new ButtonGroup();
		mouseModeGroup.add(panButton);
		mouseModeGroup.add(zoomModeButton);
		panButton.setToolTipText("Click and drag to pan the display");
		zoomModeButton.setToolTipText("Drag a rectangle to zoom in on it; single-click to zoom back out");
		panButton.addActionListener(e -> setMouseMode(MouseMode.PAN));
		zoomModeButton.addActionListener(e -> setMouseMode(MouseMode.ZOOM));
		bar.add(new JLabel("Mouse: "));
		bar.add(panButton);
		bar.add(zoomModeButton);
		addSeparator(bar);

		modeCombo.setSelectedItem(displayMode);
		modeCombo.addActionListener(e -> {
			displayMode = (DisplayMode) modeCombo.getSelectedItem();
			canvas.repaint();
		});
		bar.add(new JLabel("Display: "));
		bar.add(modeCombo);
		addSeparator(bar);

		headerCheck.addActionListener(e -> refreshHeaderDisplay());
		bar.add(headerCheck);
		headerTextCheck.addActionListener(e -> refreshHeaderDisplay());
		bar.add(headerTextCheck);
		headerGraphCheck.addActionListener(e -> refreshHeaderDisplay());
		bar.add(headerGraphCheck);
		headersButton.addActionListener(e -> openHeaderSelectionDialog());
		bar.add(headersButton);
		addSeparator(bar);

		agcCheck.addActionListener(e -> invalidateCacheAndRepaint());
		agcWindowSpinner.addChangeListener(e -> {
			if (agcCheck.isSelected())
				invalidateCacheAndRepaint();
		});
		bar.add(agcCheck);
		bar.add(new JLabel(" window(ms):"));
		agcWindowSpinner.setMaximumSize(new Dimension(70, 25));
		bar.add(agcWindowSpinner);
		addSeparator(bar);

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
		addSeparator(bar);

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
		addSeparator(bar);

		JButton zoomOut = new JButton("Zoom -");
		JButton zoomIn = new JButton("Zoom +");
		zoomOut.addActionListener(e -> setZoomBoth(0.8));
		zoomIn.addActionListener(e -> setZoomBoth(1.25));
		bar.add(zoomOut);
		bar.add(zoomLabel);
		bar.add(zoomIn);

		JButton zoomXOut = new JButton("X -");
		JButton zoomXIn = new JButton("X +");
		zoomXOut.setToolTipText("Zoom out horizontally (trace spacing) only");
		zoomXIn.setToolTipText("Zoom in horizontally (trace spacing) only");
		zoomXOut.addActionListener(e -> setZoomX(zoomX * 0.8));
		zoomXIn.addActionListener(e -> setZoomX(zoomX * 1.25));
		bar.add(zoomXOut);
		bar.add(zoomXIn);

		JButton zoomYOut = new JButton("Y -");
		JButton zoomYIn = new JButton("Y +");
		zoomYOut.setToolTipText("Zoom out vertically (time scale) only");
		zoomYIn.setToolTipText("Zoom in vertically (time scale) only");
		zoomYOut.addActionListener(e -> setZoomY(zoomY * 0.8));
		zoomYIn.addActionListener(e -> setZoomY(zoomY * 1.25));
		bar.add(zoomYOut);
		bar.add(zoomYIn);

		return bar;
	}

	/** adds a short vertical divider between groups of toolbar controls */
	private static void addSeparator(JPanel bar) {
		JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
		sep.setPreferredSize(new Dimension(2, 24));
		bar.add(sep);
	}

	private void setZoomX(double newZoom) {
		zoomX = clampZoomValue(newZoom);
		updateZoomLabel();
		refreshAllPanels();
	}

	private void setZoomY(double newZoom) {
		zoomY = clampZoomValue(newZoom);
		updateZoomLabel();
		refreshAllPanels();
	}

	/** scales both axes by the same factor, e.g. 0.8 to zoom out 20%, 1.25 to zoom in 25% */
	private void setZoomBoth(double factor) {
		zoomX = clampZoomValue(zoomX * factor);
		zoomY = clampZoomValue(zoomY * factor);
		updateZoomLabel();
		refreshAllPanels();
	}

	private void updateZoomLabel() {
		zoomLabel.setText("X:" + Math.round(zoomX * 100) + "%  Y:" + Math.round(zoomY * 100) + "%");
	}

	private static double clampZoomValue(double z) {
		return Math.max(0.1, Math.min(z, 20.0));
	}

	/** switches between Pan (hand cursor, drag to scroll) and Zoom (magnifying-glass cursor, drag/click to zoom) */
	private void setMouseMode(MouseMode mode) {
		mouseMode = mode;
		canvas.setCursor(mode == MouseMode.PAN ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : ZOOM_CURSOR);
		canvas.resetDrag();
	}

	/** revalidates+repaints every panel (canvas size and/or content may have changed) */
	private void refreshAllPanels() {
		canvas.revalidate();
		canvas.repaint();
		headerColumnPanel.repaint();
		yAxisPanel.repaint();
		cornerPanel.repaint();
	}

	/** headers on/off, text rows on/off, or graph on/off: affects header-column-panel/corner sizing and content */
	private void refreshHeaderDisplay() {
		refreshAllPanels();
	}

	/** replaces the batch of traces currently on screen (called by TraceMonitor) */
	public void setTraces(SeismicTrace[] traces) {
		this.traces = traces == null ? new SeismicTrace[0] : traces;
		refreshAvailableHeaderNames();
		invalidateCacheAndRepaint();
		refreshAllPanels();
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
			String[] preferredDefaults = {"FFID", "CHAN", "OFFSET"};
			for (String name : preferredDefaults) {
				if (uniq.contains(name)) selectedHeaderNames.add(name);
			}
			if (selectedHeaderNames.isEmpty()) {
				for (int i = 0; i < Math.min(2, availableHeaderNames.length); i++) {
					selectedHeaderNames.add(availableHeaderNames[i]);
				}
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
			refreshAllPanels();
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

	/** the header-strip height in pixels: graph (if on) stacked above text rows (if on) */
	private int headerAreaHeight() {
		int total = graphAreaHeight() + textRowsHeight();
		return total == 0 ? MIN_HEADER_AREA_HEIGHT : Math.max(MIN_HEADER_AREA_HEIGHT, total + 4);
	}

	/** assigns each selected header a stable color, by its position in the (ordered) selection */
	private static Color headerColor(int index) {
		return HEADER_COLORS[index % HEADER_COLORS.length];
	}

	private static String formatHeaderValue(double v) {
		if (!Double.isInfinite(v) && v == Math.rint(v)) return String.valueOf((long) v);
		return String.format("%.2f", v);
	}

	/** the (processed, i.e. as-displayed) sample amplitude at the currently hovered trace/time, or "--" if nothing is hovered */
	private String hoveredAmplitudeText() {
		if (hoveredTraceIndex < 0 || hoveredY < 0) return "--";
		float[][] processed = getProcessedSamples();
		if (hoveredTraceIndex >= processed.length) return "--";
		float[] samples = processed[hoveredTraceIndex];
		double pixelsPerSample = BASE_PIXELS_PER_SAMPLE * zoomY;
		int sampleIndex = (int) Math.round(hoveredY / pixelsPerSample);
		if (sampleIndex < 0 || sampleIndex >= samples.length) return "--";
		return String.format("%.3e", samples[sampleIndex]);
	}

	public void setSampleRateMicros(int sampleRateMicros) {
		this.sampleRateMicros = sampleRateMicros;
		invalidateCacheAndRepaint();
		yAxisPanel.repaint();
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

	private int currentPixelsPerTrace() {
		int p = (int) Math.round(BASE_TRACE_SPACING * zoomX);
		return p <= 0 ? 1 : p;
	}

	// ------------------------------------------------------------------
	// main content: the trace wiggle/density plots (scrolls in both axes; supports pan/zoom-drag)
	// ------------------------------------------------------------------

	private class TraceCanvas extends JPanel {
		private Point lastDragPoint;
		private Point zoomDragStart;
		private Point zoomDragCurrent;
		private boolean dragged;

		TraceCanvas() {
			setBackground(Color.WHITE);
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					boolean changed = false;
					int idx = traceIndexAtX(e.getX());
					if (idx != hoveredTraceIndex) {
						hoveredTraceIndex = idx;
						headerColumnPanel.repaint();
						changed = true;
					}
					int y = e.getY();
					if (y != hoveredY) {
						hoveredY = y;
						yAxisPanel.repaint();
						changed = true;
					}
					if (changed) cornerPanel.repaint();
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					if (mouseMode == MouseMode.PAN) {
						panBy(new Point(e.getXOnScreen(), e.getYOnScreen()));
					} else if (zoomDragStart != null) {
						zoomDragCurrent = e.getPoint();
						int dx = Math.abs(zoomDragCurrent.x - zoomDragStart.x);
						int dy = Math.abs(zoomDragCurrent.y - zoomDragStart.y);
						if (dx > 4 || dy > 4) dragged = true;
						repaint();
					}
				}
			});
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(MouseEvent e) {
					boolean changed = false;
					if (hoveredTraceIndex != -1) {
						hoveredTraceIndex = -1;
						headerColumnPanel.repaint();
						changed = true;
					}
					if (hoveredY != -1) {
						hoveredY = -1;
						yAxisPanel.repaint();
						changed = true;
					}
					if (changed) cornerPanel.repaint();
				}

				@Override
				public void mousePressed(MouseEvent e) {
					if (!SwingUtilities.isLeftMouseButton(e)) return;
					dragged = false;
					if (mouseMode == MouseMode.PAN) {
						lastDragPoint = new Point(e.getXOnScreen(), e.getYOnScreen());
					} else {
						zoomDragStart = e.getPoint();
						zoomDragCurrent = e.getPoint();
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (!SwingUtilities.isLeftMouseButton(e)) return;
					if (mouseMode == MouseMode.PAN) {
						lastDragPoint = null;
					} else {
						if (dragged && zoomDragStart != null) {
							zoomToRectangle(zoomDragStart, e.getPoint());
						} else {
							zoomOutOneStep();
						}
						zoomDragStart = null;
						zoomDragCurrent = null;
						dragged = false;
						repaint();
					}
				}
			});
		}

		/** clears any in-progress pan/zoom-rectangle drag (called when the mouse mode is switched mid-drag) */
		void resetDrag() {
			lastDragPoint = null;
			zoomDragStart = null;
			zoomDragCurrent = null;
			dragged = false;
			repaint();
		}

		/**
		 * scrolls the viewport by the delta since the last drag event, clamped so it stops at the edges.
		 * Takes an absolute screen-coordinate point (not canvas-local): canvas-local coordinates shift
		 * every time we reposition the viewport ourselves (JViewport moves the canvas to scroll it), so
		 * using them here would fight against our own scrolling and make the drag jump/oscillate.
		 */
		private void panBy(Point currentScreen) {
			if (lastDragPoint == null) {
				lastDragPoint = currentScreen;
				return;
			}
			int dx = currentScreen.x - lastDragPoint.x;
			int dy = currentScreen.y - lastDragPoint.y;
			Point vp = scrollPane.getViewport().getViewPosition();
			int newX = vp.x - dx;
			int newY = vp.y - dy;
			Dimension viewSize = scrollPane.getViewport().getExtentSize();
			Dimension canvasSize = getPreferredSize();
			int maxX = Math.max(0, canvasSize.width - viewSize.width);
			int maxY = Math.max(0, canvasSize.height - viewSize.height);
			newX = Math.max(0, Math.min(maxX, newX));
			newY = Math.max(0, Math.min(maxY, newY));
			scrollPane.getViewport().setViewPosition(new Point(newX, newY));
			lastDragPoint = currentScreen;
		}

		/** zooms in so the dragged rectangle's data exactly fills the viewport (X and Y independently), remembering the prior view for undo */
		private void zoomToRectangle(Point p1, Point p2) {
			int x1 = Math.min(p1.x, p2.x);
			int y1 = Math.min(p1.y, p2.y);
			int rectW = Math.abs(p2.x - p1.x);
			int rectH = Math.abs(p2.y - p1.y);
			if (rectW < 8 || rectH < 8) return;

			Dimension viewSize = scrollPane.getViewport().getExtentSize();
			double factorX = (double) viewSize.width / rectW;
			double factorY = (double) viewSize.height / rectH;

			Point curView = scrollPane.getViewport().getViewPosition();
			zoomHistory.push(new ViewState(zoomX, zoomY, curView.x, curView.y));

			double oldPixelsPerTrace = BASE_TRACE_SPACING * zoomX;
			double oldPixelsPerSample = BASE_PIXELS_PER_SAMPLE * zoomY;
			double traceIndexAtX1 = x1 / oldPixelsPerTrace;
			double sampleIndexAtY1 = y1 / oldPixelsPerSample;

			zoomX = clampZoomValue(zoomX * factorX);
			zoomY = clampZoomValue(zoomY * factorY);
			updateZoomLabel();
			refreshAllPanels();

			double newPixelsPerTrace = BASE_TRACE_SPACING * zoomX;
			double newPixelsPerSample = BASE_PIXELS_PER_SAMPLE * zoomY;
			int targetX = (int) Math.round(traceIndexAtX1 * newPixelsPerTrace);
			int targetY = (int) Math.round(sampleIndexAtY1 * newPixelsPerSample);

			SwingUtilities.invokeLater(() -> {
				setViewPositionClamped(targetX, targetY);
				repaint();
			});
		}

		/** a single (non-drag) click in Zoom mode: pop back to the previous view, or reset to 100%/100% if none saved */
		private void zoomOutOneStep() {
			if (!zoomHistory.isEmpty()) {
				ViewState prev = zoomHistory.pop();
				zoomX = prev.zoomX;
				zoomY = prev.zoomY;
				updateZoomLabel();
				refreshAllPanels();
				SwingUtilities.invokeLater(() -> {
					setViewPositionClamped(prev.viewX, prev.viewY);
					repaint();
				});
			} else {
				zoomX = 1.0;
				zoomY = 1.0;
				updateZoomLabel();
				refreshAllPanels();
				SwingUtilities.invokeLater(() -> {
					scrollPane.getViewport().setViewPosition(new Point(0, 0));
					repaint();
				});
			}
		}

		private void setViewPositionClamped(int x, int y) {
			Dimension canvasSize = getPreferredSize();
			Dimension viewSize = scrollPane.getViewport().getExtentSize();
			int maxX = Math.max(0, canvasSize.width - viewSize.width);
			int maxY = Math.max(0, canvasSize.height - viewSize.height);
			int vx = Math.max(0, Math.min(maxX, x));
			int vy = Math.max(0, Math.min(maxY, y));
			scrollPane.getViewport().setViewPosition(new Point(vx, vy));
		}

		/** the index of the trace column nearest a given x pixel, or -1 if no traces are loaded */
		private int traceIndexAtX(int mouseX) {
			if (traces.length == 0) return -1;
			int pixelsPerTrace = currentPixelsPerTrace();
			int idx = (int) Math.round((mouseX - pixelsPerTrace / 2.0) / pixelsPerTrace);
			if (idx < 0) idx = 0;
			if (idx >= traces.length) idx = traces.length - 1;
			return idx;
		}

		@Override
		public Dimension getPreferredSize() {
			int spacing = currentPixelsPerTrace();
			int width = traces.length * spacing;
			int maxSamples = 0;
			for (SeismicTrace t : traces) {
				maxSamples = Math.max(maxSamples, t.getData().length);
			}
			int height = (int) Math.round(maxSamples * BASE_PIXELS_PER_SAMPLE * zoomY) + 10;
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
			double pixelsPerSample = BASE_PIXELS_PER_SAMPLE * zoomY;
			int pixelsPerTrace = currentPixelsPerTrace();

			for (int i = 0; i < processed.length; i++) {
				int x = i * pixelsPerTrace + pixelsPerTrace / 2;
				if (displayMode == DisplayMode.VARIABLE_DENSITY) {
					drawDensityTrace(g2, x, pixelsPerTrace, processed[i], maxAbs, pixelsPerSample);
				} else {
					drawWiggleTrace(g2, x, pixelsPerTrace, processed[i], maxAbs, pixelsPerSample);
				}
			}

			if (mouseMode == MouseMode.ZOOM && zoomDragStart != null && zoomDragCurrent != null) {
				int rx = Math.min(zoomDragStart.x, zoomDragCurrent.x);
				int ry = Math.min(zoomDragStart.y, zoomDragCurrent.y);
				int rw = Math.abs(zoomDragCurrent.x - zoomDragStart.x);
				int rh = Math.abs(zoomDragCurrent.y - zoomDragStart.y);
				if (rw > 0 && rh > 0) {
					g2.setColor(new Color(0, 90, 200, 60));
					g2.fillRect(rx, ry, rw, rh);
					g2.setColor(new Color(0, 90, 200));
					g2.drawRect(rx, ry, rw, rh);
				}
			}
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
				int y = (int) Math.round(j * pixelsPerSample);
				double dx = amp * halfWidth;
				fillXs[j] = (int) Math.round(x + Math.max(dx, 0));
				fillYs[j] = y;
				lineXs[j] = (int) Math.round(x + dx);
				lineYs[j] = y;
			}
			for (int j = 0; j < n; j++) {
				int y = (int) Math.round((n - 1 - j) * pixelsPerSample);
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
			int bandWidth = spacing;
			for (int j = 0; j < n; j++) {
				double amp = maxAbs > 0 ? samples[j] / maxAbs : 0;
				g2.setColor(amplitudeToColor(amp * gain));
				int y = (int) Math.round(j * pixelsPerSample);
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

	// ------------------------------------------------------------------
	// column header: header text rows + multi-header graph (scrolls horizontally with the
	// canvas, stays fixed vertically - i.e. always visible regardless of vertical pan/scroll)
	// ------------------------------------------------------------------

	private class HeaderColumnPanel extends JPanel {
		HeaderColumnPanel() {
			setBackground(Color.WHITE);
		}

		@Override
		public Dimension getPreferredSize() {
			int width = traces.length * currentPixelsPerTrace();
			return new Dimension(Math.max(width, 1), headerAreaHeight());
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (traces.length == 0 || !headerCheck.isSelected()) return;

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int pixelsPerTrace = currentPixelsPerTrace();

			if (headerGraphCheck.isSelected()) {
				drawHeaderGraph(g2, pixelsPerTrace);
			}
			if (headerTextCheck.isSelected()) {
				int gap = Math.max(2, 50 / pixelsPerTrace);
				for (int i = 0; i < traces.length; i++) {
					if (i % gap != 0) continue;
					int x = i * pixelsPerTrace + pixelsPerTrace / 2;
					drawHeaderLabel(g2, x, traces[i]);
				}
			}
		}

		/**
		 * multi-header line graph: each selected header is plotted as its own
		 * colored polyline across the batch of traces, independently normalized
		 * to that header's own min/max so headers with very different ranges
		 * (e.g. CDP vs. OFFSET) can share the same vertical space without one
		 * flattening the other. The legend shows each header's value for the
		 * trace nearest the mouse pointer (a vertical crosshair marks it),
		 * rather than the min/max used only to scale the lines.
		 */
		private void drawHeaderGraph(Graphics2D g2, int pixelsPerTrace) {
			if (selectedHeaderNames.isEmpty() || traces.length == 0) return;
			String[] names = selectedHeaderNames.toArray(new String[0]);

			int legendTop = 2;
			int plotTop = HEADER_GRAPH_LEGEND_HEIGHT;
			int plotHeight = HEADER_GRAPH_HEIGHT - 4;
			int plotBottom = plotTop + plotHeight;
			int width = traces.length * pixelsPerTrace;

			g2.setColor(new Color(248, 248, 248));
			g2.fillRect(0, 0, width, HEADER_GRAPH_LEGEND_HEIGHT + HEADER_GRAPH_HEIGHT);
			g2.setColor(new Color(215, 215, 215));
			g2.drawRect(0, plotTop, width - 1, plotHeight);

			Font old = g2.getFont();
			g2.setFont(old.deriveFont(9f));
			FontMetrics fm = g2.getFontMetrics();
			int legendX = 4;

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
					xs[i] = i * pixelsPerTrace + pixelsPerTrace / 2;
					ys[i] = plotBottom - (int) Math.round(norm * (plotHeight - 2)) - 1;
				}
				g2.setColor(color);
				g2.drawPolyline(xs, ys, traces.length);

				String hoveredValue = (hoveredTraceIndex >= 0 && hoveredTraceIndex < traces.length)
					? formatHeaderValue(traces[hoveredTraceIndex].getHeaderValue(name))
					: "--";
				String legend = name + ": " + hoveredValue;
				g2.drawLine(legendX, legendTop + 5, legendX + 12, legendTop + 5);
				g2.drawString(legend, legendX + 15, legendTop + 9);
				legendX += 15 + fm.stringWidth(legend) + 12;
			}

			if (hoveredTraceIndex >= 0 && hoveredTraceIndex < traces.length) {
				int hoverX = hoveredTraceIndex * pixelsPerTrace + pixelsPerTrace / 2;
				g2.setColor(new Color(110, 110, 110));
				g2.drawLine(hoverX, plotTop, hoverX, plotBottom);
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
	}

	// ------------------------------------------------------------------
	// row header: time axis (scrolls vertically with the canvas, stays fixed
	// horizontally - i.e. always visible regardless of horizontal pan/scroll)
	// ------------------------------------------------------------------

	private class YAxisPanel extends JPanel {
		YAxisPanel() {
			setBackground(Color.WHITE);
		}

		@Override
		public Dimension getPreferredSize() {
			int maxSamples = 0;
			for (SeismicTrace t : traces) {
				maxSamples = Math.max(maxSamples, t.getData().length);
			}
			int height = (int) Math.round(maxSamples * BASE_PIXELS_PER_SAMPLE * zoomY) + 10;
			return new Dimension(LEFT_MARGIN, Math.max(height, 1));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (traces.length == 0) return;

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			double pixelsPerSample = BASE_PIXELS_PER_SAMPLE * zoomY;
			Font old = g2.getFont();
			g2.setFont(old.deriveFont(9f));
			g2.setColor(Color.DARK_GRAY);
			FontMetrics fm = g2.getFontMetrics();

			int length = traces[0].getData().length;
			for (int i = 0; i < length; i += 50) {
				int ipixels = (int) (i * pixelsPerSample);
				int time = i * sampleRateMicros / 1000;
				String timeLabel = String.valueOf(time);
				g2.drawString(timeLabel, 2, ipixels + fm.getHeight() / 2);
			}

			if (hoveredY >= 0) {
				int hoveredTime = (int) Math.round((hoveredY / pixelsPerSample) * sampleRateMicros / 1000.0);
				g2.setColor(new Color(110, 110, 110));
				g2.drawLine(0, hoveredY, getWidth(), hoveredY);
				g2.setColor(Color.BLACK);
				String hoveredLabel = String.valueOf(hoveredTime);
				int labelY = hoveredY - 3;
				if (labelY < fm.getHeight()) labelY = hoveredY + fm.getHeight() + 3;
				g2.drawString(hoveredLabel, 2, labelY);
			}

			g2.setFont(old);
		}
	}

	// ------------------------------------------------------------------
	// corner: header row NAMES (fixed in both axes - the one spot where the
	// X-axis header rows and the Y-axis column visually intersect)
	// ------------------------------------------------------------------

	private class CornerPanel extends JPanel {
		CornerPanel() {
			setBackground(Color.WHITE);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Font old = g2.getFont();
			g2.setFont(old.deriveFont(9f));

			g2.setColor(Color.BLACK);
			g2.drawString("Amp:", 2, 10);
			g2.drawString(hoveredAmplitudeText(), 2, 21);

			if (headerCheck.isSelected() && headerTextCheck.isSelected() && !selectedHeaderNames.isEmpty()) {
				String[] headerNameLabels = selectedHeaderNames.toArray(new String[0]);
				int rowY = graphAreaHeight() + 11;
				for (int i = 0; i < headerNameLabels.length; i++) {
					g2.setColor(headerColor(i));
					g2.drawString(headerNameLabels[i], 2, rowY);
					rowY += HEADER_TEXT_ROW_HEIGHT;
				}
			}
			g2.setFont(old);
		}
	}
}
