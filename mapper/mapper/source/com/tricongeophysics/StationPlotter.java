package com.tricongeophysics;


import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*; //1.4 code

public class StationPlotter extends JPanel implements SelectionListener, TableModelListener {

    protected ReflectiveTableModel receivers;
    protected ReflectiveTableModel shots;
    protected ReflectiveTableModel obRecords;
    protected ShotRecord selectedShotRecord;
    protected Receiver selectedReceiver;
    protected SP selectedShot;
    protected int symbolSize=8;
    protected int channelLabelInterval=10;
    protected double maxStationX=0;
    protected double maxStationY=0;
    protected double minStationX=0;
    protected double minStationY=0;
    protected double minStationZ=0;
    protected double maxStationZ=0;
    protected double mouseX=0; //mouse X position in map coordinates
    protected double mouseY=0; //mouse Y position in map coordinates
    protected double originX=0; //smallest x coordinate to be drawn
    protected double originY=0; //smallest y coordinate to be drawn
    protected double mapHeight=0; //vertical distance to be drawn (minus margins)
    protected double mapWidth=0; //horizontal distance to be drawn (minus margins)
    protected double mapScaleFactor=0; // map scale (pixels/(ft or m))
    protected JLabel shotLabel; //selected shot information
    protected JLabel receiverLabel; //selected receiver information
    protected JLabel mouseLabel; //mouse position label
    protected int xMargin=0; //shift for centering plot in x direction
    protected int yMargin=0; //shift for centering plot in y direction
    protected Rectangle zoomRectangle;
    protected Rectangle oldZoomRectangle;
    protected Color selectedReceiverColor=Color.pink;
    protected Color selectedShotColor=Color.cyan;
    protected Color foregroundColor=Color.WHITE;
    protected Color backgroundColor=Color.BLACK;
    protected Color shotColor=Color.RED;
    protected Color receiverColor=Color.GREEN;
    protected Color areaPolygonColor = Color.YELLOW;
    private Color zoomRectangleColor = Color.magenta;
    private Color cdpGridColor = selectedReceiverColor.darker().darker();
    protected boolean printerFriendlyMode = false;
    protected Point2D.Double distLineBegin;
    protected Point2D.Double distLineEnd;
    protected Point2D.Double oldDistLineEnd;
    protected ArrayList<Point2D.Double> areaPoints; //points used to define polygon for area calculation
    protected double mouseX0=0; //initial mouse x position for drag events
    protected double mouseY0=0; //initial mouse y position for drag events
    protected DecimalFormat df; // 1.4 code
    protected DecimalFormat df2; // 1.4 code
    protected boolean dragging = false;
    protected Point zoomStart;
    protected Rectangle2D.Double drawingArea; 
    protected static final String RECEIVER_VS_SHOT = "Receiver vs. Shot";
    protected String symbolColorMode = RECEIVER_VS_SHOT; //mode used for determining symbol color
    //protected int selectedVariableIndex = 0; //index of station variable used for station color map
    protected final static String MOUSE_MODE_HELP = "/Pan  &ensp&ensp&ensp Middle-Mouse: Polygon Area/Distance Line &ensp&ensp&ensp Right-Mouse: Zoom Reset/Zoom";
    protected ColorBar colorBar;

    private boolean debugTimer = false;
    private Stopwatch sw;
    public int selectedReceiverIndex;
    public int selectedShotIndex;
    private JToggleButton selectButton;
    private JToggleButton ffidButton;
    private JToggleButton measureButton;
    private JToggleButton areaButton;
    protected MouseListenManager mouseListenManager;
    private ArrayList<ChangeListener> mouseChangedListeners = new ArrayList<ChangeListener>();
    enum Units {Meters, Feet};
    protected Units units = Units.Feet;
    protected Point2D.Double[] scatter;
    protected Point2D.Double[] oldScatter;
    private int scatterInc = 4;
    private JComboBox symbolColorModeBox;
    protected boolean showReceivers = true;
    protected boolean showShotpoints = true;
    private CdpGridPane cdpBinningPane;
    private Image backPane;
    private Receiver oldSelectedReceiver;
    private SP oldSelectedShot;
    protected static final String RIGHT_ARROW = ">>"; //\u22b3contains as normal subgroup symbol (right-pointing-triangle)
    protected static final String ANGLE_SYMBOL = "\u2220"; //29a8?
    protected static final String DEGREE_SYMBOL = "\u00B0";
    public static final String Receivers = "<html><font color=green>Receivers";
    public static final String ShotPoints = "<html><font color=red>Shots Points";
    public static final String ShotRecords = "<html><font color=blue>Relation";
    private static final Double SquareMile = 5280.0*5280.0;
    private static final Double SquareKilometer = 1000.0*1000.0;
    private static final Color Transparent = new Color(0,0,0,0);

    {
        SelectionChangedMonitor.addListener(this);
        mouseListenManager = new MouseListenManager(new SelectMouseListener());
        this.addMouseListener(mouseListenManager);
        this.addMouseMotionListener(mouseListenManager);
    }

    public StationPlotter(ReflectiveTableModel r, ReflectiveTableModel s, ReflectiveTableModel o) {
        receivers = r;
        shots = s;
        obRecords = o;
        stationMinMax(r.getTableData());
        stationMinMax(s.getTableData());
        setLayout();
    }

    public StationPlotter(){
        receivers = new ReflectiveTableModel();
        shots = new ReflectiveTableModel();
        obRecords = new ReflectiveTableModel();
        selectedShotRecord = new ShotRecord();
        oldSelectedReceiver = selectedReceiver= new Receiver();
        oldSelectedShot = selectedShot = new SP();
        setLayout();
    }

    public StationPlotter(ArrayList<TableData> receivers, ArrayList<TableData> shotPoints, ArrayList<TableData> shotRecords) {
        this(new ReflectiveTableModel(receivers), 
                new ReflectiveTableModel(shotPoints),
                new ReflectiveTableModel(shotRecords));
    }

    public void setLayout() {
        //make selected shot and receiver legend labels and add to JPanel
        shotLabel = new JLabel();
        receiverLabel = new JLabel();
        shotLabel.setForeground(selectedShotColor);
        shotLabel.setBackground(backgroundColor);
        shotLabel.setOpaque(true);
        shotLabel.setBorder(BorderFactory.createLineBorder(shotColor));
        shotLabel.setToolTipText("Information about selected Shot Station");
        receiverLabel.setForeground(selectedReceiverColor);
        receiverLabel.setBackground(backgroundColor);
        receiverLabel.setOpaque(true);
        receiverLabel.setBorder(BorderFactory.createLineBorder(receiverColor));
        receiverLabel.setToolTipText("Information about selected Receiver Station");
        JPanel labels = new JPanel(new BorderLayout());
        labels.setBackground(Transparent); //set label background to clear
        labels.add(new JPanel().add(shotLabel),BorderLayout.EAST);
        labels.add(new JPanel().add(receiverLabel),BorderLayout.WEST);
        this.setLayout(new BorderLayout());
        this.add(BorderLayout.NORTH,labels);  

        //make symbolColorMode comboBox
        symbolColorModeBox = new JComboBox(this.getSymbolColorModeOptions());
        symbolColorModeBox.setBackground(backgroundColor);
        symbolColorModeBox.setForeground(foregroundColor);
        symbolColorModeBox.setToolTipText("Select column to display in color bar");
        symbolColorModeBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                setSymbolColorMode(symbolColorModeBox.getSelectedItem().toString());
                StationPlotter.this.repaint();
            }});
        JPanel colorModePanel = new JPanel();
        JLabel label = new JLabel("Display:");
        label.setBackground(Transparent);
        label.setForeground(foregroundColor);
        colorModePanel.add(label);
        colorModePanel.add(symbolColorModeBox);
        colorModePanel.setBackground(Transparent);

        //make mouse position label and add at lower left
        mouseLabel = new JLabel();
        mouseLabel.setForeground(foregroundColor);

        // make southern border
        JPanel southPane = new JPanel();
        southPane.setLayout(new BorderLayout());
        southPane.add(mouseLabel, BorderLayout.WEST);
        southPane.add(colorModePanel, BorderLayout.EAST);
        southPane.setBackground(Transparent);
        this.add(BorderLayout.SOUTH, southPane);

        //make mouseMode Button pane
        JPanel mouseButtonPane = createMouseButtonPane();
        this.add(BorderLayout.WEST, mouseButtonPane);

        //make displayButtonPane
        JPanel displayButtonPane = createDisplayButtonPane();
        this.add(BorderLayout.EAST, displayButtonPane);

        //set decimal formatting
        df = new java.text.DecimalFormat("##,##0.00");
        df2 = new java.text.DecimalFormat("####0.00");
    }

    private JPanel createDisplayButtonPane()
    {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.setBackground(Transparent);

        JButton receiverButton = new ReceiverButton();
        receiverButton.setBackground(Transparent);
        receiverButton.setToolTipText("Toggle Receiver Display");
        receiverButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                showReceivers = !showReceivers;
                setColorBar();
                repaint();
            }});
        pane.add(receiverButton);

        JButton shotButton = new ShotPointButton();
        shotButton.setToolTipText("Toggle Shot Point Display");
        shotButton.setBackground(Transparent);
        shotButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                showShotpoints  = !showShotpoints;
                setColorBar();
                repaint();
            }});
        pane.add(shotButton);

        ScatterButton scatterButton = new ScatterButton();
        scatterButton.setBackground(Transparent);
        scatterButton.setToolTipText("Toggle Scatter View");
        scatterButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                checkCalcScatter();
            }});
        pane.add(scatterButton);

        FeetMetersButton feetMetersButton = new FeetMetersButton();
        feetMetersButton.setBackground(Transparent);
        feetMetersButton.setToolTipText("Toggle Units (ft/meters)");
        feetMetersButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                if (units == Units.Feet) {
                    units = Units.Meters;
                }
                else {
                    units = Units.Feet;
                }
                repaint();
            }});
        pane.add(feetMetersButton);

        return pane;
    }

    private JPanel createMouseButtonPane()
    {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.setBackground(Transparent);
        selectButton = new SelectButton();
        selectButton.setBackground(Transparent);
        selectButton.setToolTipText("Select Shot Point/Receiver Mouse Mode");
        selectButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                setMouseListener(new SelectMouseListener());
            }});

        ffidButton = new SpreadButton();
        ffidButton.setBackground(Transparent);
        ffidButton.setToolTipText("Select Shot Mouse Mode (Double-click to clear)");
        ffidButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                setMouseListener(new SelectFFIDMouseListener());
            }});

        measureButton = new RulerButton();
        measureButton.setBackground(Transparent);
        measureButton.setToolTipText("Calculate Distance/Angle Between Two Points");
        measureButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                clearDistanceLine();
                setMouseListener(new MeasureMouseListener());
            }});

        areaButton = new PolyLineButton();
        areaButton.setBackground(Transparent);
        areaButton.setToolTipText("Calculate Area Mouse Mode (Double-click to clear)");
        areaButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                clearAreaPolygon();
                setMouseListener(new AreaMouseListener());
            }});

        ButtonGroup group = new ButtonGroup();
        group.add(selectButton);
        group.add(ffidButton);
        group.add(measureButton);
        group.add(areaButton);

        pane.add(selectButton);
        pane.add(ffidButton);
        pane.add(measureButton);
        pane.add(areaButton);
        return pane;
    }

    protected void clearDistanceLine()
    {
        distLineBegin=null;
        distLineEnd=null;
        oldDistLineEnd=null;
    }

    protected void clearAreaPolygon()
    {
        areaPoints = null;
    }

    protected void checkCalcScatter()
    {
        if (scatter == null) { //turning scatter on
            if (oldScatter == null) {
                if (obRecords == null || obRecords.getTableData() == null || obRecords.getTableData().size() == 0) return;
                final CalculateScatterJob scatterJob = new CalculateScatterJob(receivers.getTableData(), shots.getTableData(), obRecords.getTableData(), scatterInc);
                scatterJob.addJobFinishedListener(new JobFinishedListener(){
                    public void jobFinished(Job job)
                    {
                        scatter = scatterJob.getScatter();
                        repaint();
                        return;
                    }});
                new JobProgressMonitor(StationPlotter.this, scatterJob, "Calculating Scatter");
            }
            else
                scatter = oldScatter;
            repaint();
            return;
        }
        else { //turning scatter off
            oldScatter = scatter;
            scatter = null;
            repaint();
        }
    }

    protected void setMouseListener(MouseEventListener mel)
    {
        mouseListenManager.setListener(mel);
        fireMouseListenerChanged();
        repaint();
    }

    private void fireMouseListenerChanged()
    {
        for (ChangeListener cl: mouseChangedListeners ) {
            cl.stateChanged(null);
        }
    }

    public void setReceivers(ReflectiveTableModel r) {
        receivers = r;
        stationMinMax(r.getTableData());
        zoomReset();
    }

    public void setShots(ReflectiveTableModel s) {
        if (s == null) return;
        shots = s;
        stationMinMax(s.getTableData());
        zoomReset();
    }    

    public void setObRecords(ReflectiveTableModel o) {
        obRecords = o;
    }    

    /*
    public ArrayList getObRecords() {
        return obRecords;
    }
     */

    private class SelectMouseListener extends PlotterMouseEventListener {
        @Override
        public void mouse1Clicked(MouseEvent e)
        {
            fireSelectionChanged(new SelectionChangedEvent(selectedReceiver, selectedReceiverIndex));
            fireSelectionChanged(new SelectionChangedEvent(selectedShot, selectedShotIndex));
        }

        @Override
        public String getMouseAction()
        {
            return "Select Station";
        }
    }

    private class MeasureMouseListener extends PlotterMouseEventListener {
        @Override
        public void mouse1Clicked(MouseEvent e)
        {
            if (distLineBegin == null) {  //first or fourth time clicking time clicking (start distance line)
                distLineBegin = new Point2D.Double(mouseX,mouseY);
                distLineEnd = null;
                oldDistLineEnd = null;
            } else if (oldDistLineEnd == null) { //second time clicking (preserve distance line)
                oldDistLineEnd = distLineEnd; //oldDistLineEnd is used as flag to indicate user is done drawing
            } else {                             //third time clicking (clear distance line
                distLineBegin = null;
                distLineEnd = null;
                oldDistLineEnd = null;
            } 
        }
        @Override
        public void mouseMoved(MouseEvent e) {
            getMouseXY(e);
            if (oldDistLineEnd ==  null) //track distance line with current mouse position until user clicks second time
                distLineEnd = new Point2D.Double(mouseX,mouseY);
            //mouse moved, find nearest station
            if (receivers.size()>0) {
                int index = findClosestStation(mouseX,mouseY,receivers.getTableData());
                selectedReceiver = (Receiver)receivers.get(index);
                selectedReceiverIndex = index;
                //fireSelectionChanged(new SelectionChangedEvent(selectedReceiver, index));
            }
            if (shots.size()>0) {
                int index = findClosestStation(mouseX,mouseY,shots.getTableData());
                selectedShot = (SP)shots.get(index);
                selectedShotIndex = index;
                //fireSelectionChanged(new SelectionChangedEvent(selectedShot, index));
            }
            resetLabel(e);
            repaint();
        }
        @Override
        public String getMouseAction()
        {
            return "Draw Distance Line";
        }
    }

    private class AreaMouseListener extends PlotterMouseEventListener {
        @Override
        public void mouse1Clicked(MouseEvent e)
        {
            if (e.getClickCount()==1) {
                if (areaPoints == null) areaPoints = new ArrayList<Point2D.Double>();
                //add new point
                areaPoints.add(new Point2D.Double(mouseX,mouseY));
            }
            else if (e.getClickCount()>1) {
                areaPoints = null;
            }
        }

        @Override
        public String getMouseAction()
        {
            return "Draw Area Polygon";
        }
    }

    private class SelectFFIDMouseListener extends PlotterMouseEventListener {
        @Override
        public void mouse1Clicked(MouseEvent e)
        {
            fireSelectionChanged(new SelectionChangedEvent(selectedReceiver, selectedReceiverIndex));
            fireSelectionChanged(new SelectionChangedEvent(selectedShot, selectedShotIndex));
            if (e.getClickCount()==1) {
                if (!receivers.isEmpty() && !shots.isEmpty() && !obRecords.isEmpty()) {
                    if (selectedShotRecord == null) selectedShotRecord = new ShotRecord();
                    //                      find selected shot point in obs
                    int i=0;
                    for (i=0;i<obRecords.size();i++)
                        if ( (((OBRecord)obRecords.get(i)).getSourceLineNumber() == selectedShot.getLineNumber())
                                && (((OBRecord)obRecords.get(i)).getSourceStationNumber() == selectedShot.getStationNumber()))
                            break;
                    if (i<obRecords.size()) {
//                        loadSelectedShotRecord((OBRecord)obRecords.get(i));
                        fireSelectionChanged(new SelectionChangedEvent(selectedShotRecord, i));
                    }
                    else {
                        selectedShotRecord = null;
                        JOptionPane.showMessageDialog(StationPlotter.this,"<HTML>Couldn't find Shot for selected shot point:\n"+selectedShot+
                                "in OB file(s)",
                                "OB Search Error",JOptionPane.ERROR_MESSAGE);
                    }
                }
                else {
                    JOptionPane.showMessageDialog(StationPlotter.this,"Receiver, Shot Point, and OB files are required"+
                            "to select Shot Record",
                            "OB Search Error",JOptionPane.ERROR_MESSAGE);
                    selectedShotRecord = null;
                }
            }
            //mouse 1 double clicked = unselect ffid
            else if (e.getClickCount()>1)
                selectedShotRecord=null;
        }

        @Override
        public String getMouseAction()
        {
            return "Select Shot";
        }

    }

    // reset zoom to arbitrary factor (1 = no zoom)
    public void zoomReset(double factor){
        //find max/min for station x/y values
        zoomRectangle = null;
        oldZoomRectangle = null;
        minStationX=0; //force station Min/Max to initialize to real values
        minStationY=0; //          in case user has selected new input files
        maxStationX=0;
        maxStationY=0;
        stationMinMax(shots.getTableData());
        stationMinMax(receivers.getTableData());

        //initialize width, height, and origin to 10% greater than survey extent
        mapWidth = factor*(maxStationX-minStationX);
        mapHeight = factor*(maxStationY-minStationY);
        originX = minStationX - 0.5*(factor-1)*(maxStationX-minStationX);
        originY = minStationY - 0.5*(factor-1)*(maxStationY-minStationY);

        calcScaleFactor(mapWidth,mapHeight);
    } // end zoomReset

    public void zoomReset(){
        zoomReset(1.2);
    }

    public void setZoom(){
        //get new map origin from the current zoom rectangle
        //  (add height to origin y because coordinates are 
        //   flipped vertically)
        originX = pixelToCoordConvertX((int)zoomRectangle.getX());
        originY = pixelToCoordConvertY((int)(zoomRectangle.getY()
                +zoomRectangle.getHeight()));
        //current scale factor used to calculate what the new
        //   height & width of the map will be in distance units.
        mapWidth = Math.abs(zoomRectangle.getWidth())/mapScaleFactor;
        mapHeight = Math.abs(zoomRectangle.getHeight())/mapScaleFactor;
        zoomRectangle = null; //hide zoom rectangle
        oldZoomRectangle = null;
        //using new height & width, calculate what the new
        //   scale factor will be (pixels/(ft or m))
        calcScaleFactor(mapWidth,mapHeight);        
    }

    public void stationMinMax(ArrayList<Station> stations) {
        // If max/min are all zeroes, get real ones first
        if (stations == null || stations.size() == 0) return;
        if (maxStationX-minStationX+maxStationY-minStationY == 0) {
            Station s1 = stations.get(0);
            int j=0;
            while (s1.kill) s1 = stations.get(j++);
            maxStationX = s1.getX();
            maxStationY = s1.getY(); 
            minStationX = s1.getX();
            minStationY = s1.getY();
        }

        //now, find actual max/min values
        for (int i=0; i<stations.size();i++ ) { 
            Station s1 = stations.get(i);
            if (s1.kill) continue; //ignore killed stations
            if (s1.getX() > maxStationX) 
                maxStationX = s1.getX();
            if (s1.getY() > maxStationY) 
                maxStationY = s1.getY();
            if (s1.getX() < minStationX) 
                minStationX = s1.getX();
            if (s1.getY() < minStationY) 
                minStationY = s1.getY(); 
        }
    } // end stationMinMax

    //Calculate map scale(pixels/(m or ft)) using window size and 
    //  desired x & y distances to be viewed.
    public void calcScaleFactor(
            double xDist, // horizontal distance to be viewed on screen
            double yDist)  // vertical distance to be viewed on screen
    {
        //Prevent division by zero.
        if (xDist < Double.MIN_VALUE) xDist = Double.MIN_VALUE;
        if (yDist < Double.MIN_VALUE) yDist = Double.MIN_VALUE;
        double xScaleFactor = (this.getWidth()/Math.abs(xDist));
        double yScaleFactor = (this.getHeight()/Math.abs(yDist));

        //final map scale will take scale that allows both xDist & yDist to be visible
        mapScaleFactor = Math.min(xScaleFactor, yScaleFactor);

        //calculate new margins to center zoom area on screen
        xMargin = (int)(0.5*(this.getWidth()-mapScaleFactor*Math.abs(xDist)));
        yMargin = (int)(0.5*(this.getHeight()-mapScaleFactor*Math.abs(yDist)));
    }// end calcScaleFactor

    public abstract class PlotterMouseEventListener extends MouseEventListener {

        public void getMouseXY(MouseEvent e) {
            //no matter what happens, keep mouseX/mouseY current
            mouseX = pixelToCoordConvertX(e.getX());
            mouseY = pixelToCoordConvertY(e.getY());
        }
        public void mouseClicked(MouseEvent e) {
            getMouseXY(e); //always get mouse XY and reset label
            resetLabel(e);
            switch (e.getButton()) {
                case MouseEvent.BUTTON1:
                    mouse1Clicked(e);
                    repaint();
                    break;
                case MouseEvent.BUTTON2:
                    //mouse 2 click = add polygon point
                    if (e.getClickCount()==1) {
                        if (areaPoints == null) areaPoints = new ArrayList<Point2D.Double>();
                        //add new point
                        areaPoints.add(new Point2D.Double(mouseX,mouseY));
                        distLineBegin=null;
                        distLineEnd=null;
                        oldDistLineEnd=null;
                    }
                    //if double clicked (or higher) remove area points and distance line
                    else if (e.getClickCount()>1) {
                        clearAreaPolygon();
                        clearDistanceLine();
                    }
                    repaint();
                    break;
                case MouseEvent.BUTTON3:
                    //mouse 3 click = zoom reset
                    if (e.getClickCount()==1) {
                        zoomReset();
                        zoomStart = null;
                    }
                    //mouse 3 double click = zoom out
                    else if (e.getClickCount()>1) {
                        System.out.println("zoom out");
                        zoomStart = null;
                        zoomReset();
                    }
                    repaint();
                    break;
            } // end switch
        }
        public abstract void mouse1Clicked(MouseEvent e);
        public void mouseExited(MouseEvent e) {
            getMouseXY(e);
        }
        public void mouseEntered(MouseEvent e) {
            getMouseXY(e);
        }
        public void mouseReleased(MouseEvent e) { 
            getMouseXY(e);
            switch (e.getButton()) {
                case MouseEvent.BUTTON1:
                    //finished panning
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    break;
                case MouseEvent.BUTTON2:
                    //finished drawing distance line
                    dragging = false;
                    distLineEnd.setLocation(mouseX,mouseY);
                    oldDistLineEnd = null;
                    repaint();
                    break;
                case MouseEvent.BUTTON3:
                    //finished drawing zoom box
                    dragging = false;
                    zoomStart = null;
                    setZoom();  
                    repaint();
                    break;
            } // end switch
            resetLabel(e);
        }
        public void mousePressed(MouseEvent e) {
            getMouseXY(e);
            switch (e.getButton()) {
                case MouseEvent.BUTTON1:
                    //possibly starting to pan,
                    // reset mouse initial position.
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    mouseX0 = mouseX;  //set initial mouse X
                    mouseY0 = mouseY;  //set initial mouse X
                    break;
                case MouseEvent.BUTTON2:
                    //posibly starting to draw distance line, new points jic
                    distLineBegin = new Point2D.Double(mouseX,mouseY);
                    distLineEnd = new Point2D.Double(mouseX,mouseY);
                    break;
                case MouseEvent.BUTTON3:
                    //possibly starting to zoom, new rectangle just in case.
                    zoomStart = new Point(e.getX(),e.getY());
                    zoomRectangle = new Rectangle(e.getX(),e.getY(),1,1);
                    break;
            } // end switch
            resetLabel(e); 
        }
        public void mouseMoved(MouseEvent e) {
            getMouseXY(e);
            //mouse moved, find nearest station
            if (receivers.size()>0) {
                int index = findClosestStation(mouseX,mouseY,receivers.getTableData());
                selectedReceiver = (Receiver)receivers.get(index);
                selectedReceiverIndex = index;
                //fireSelectionChanged(new SelectionChangedEvent(selectedReceiver, index));
            }
            if (shots.size()>0) {
                int index = findClosestStation(mouseX,mouseY,shots.getTableData());
                selectedShot = (SP)shots.get(index);
                selectedShotIndex = index;
                //fireSelectionChanged(new SelectionChangedEvent(selectedShot, index));
            }
            resetLabel(e);
            repaint();
        }
        public void mouseDragged(MouseEvent e) {
            getMouseXY(e);
            //System.out.print("dragging");
            switch (e.getModifiersEx()) {
                case MouseEvent.BUTTON1_DOWN_MASK:
                    //we're panning now! 
                    originX=originX+(mouseX0-mouseX); //adjust origin by x mouse movement
                    originY=originY+(mouseY0-mouseY); //adjust origin by y mouse movement
                    repaint();
                    break;
                case MouseEvent.BUTTON2_DOWN_MASK:
                    //we're drawing distance line!
                    Point2D.Double tmpPt = new Point2D.Double(mouseX,mouseY);
                    if (!distLineEnd.equals(tmpPt)){
                        oldDistLineEnd = distLineEnd;
                        distLineEnd = tmpPt;
                        tmpPt = null;
                    }
                    dragging = true;
                    repaint();
                    break;
                case MouseEvent.BUTTON3_DOWN_MASK:
                    //we're drawing zoom box!
                    Rectangle tmp = new Rectangle();
                    tmp.setRect(
                            Math.min(zoomStart.getX(),e.getX()),
                            Math.min(zoomStart.getY(),e.getY()),
                            Math.abs(e.getX()-zoomStart.getX()),
                            Math.abs(e.getY()-zoomStart.getY()));
                    //check to see if zoom rectangle is changing, then set old zoom
                    // so that we can paint over it with background color
                    if (!zoomRectangle.equals(tmp)){
                        oldZoomRectangle = zoomRectangle;
                        zoomRectangle = tmp;
                        tmp = null;
                    }
                    dragging = true;
                    repaint();
                    break;
            }
            resetLabel(e);
        }
        public void resetLabel(MouseEvent e) {
            if (!dragging) { //don't reset when dragging, since background not refreshing
                mouseLabel.setText("x: "+df2.format(mouseX)+"  y: "+df2.format(mouseY)); 
                updateReceiverLabel();
                updateShotPointLabel();
            } //end if
        }

        @Override
        public String toString() {
            return "<html>Mouse Help &ensp&ensp "+RIGHT_ARROW+" &ensp&ensp Left Mouse: "+getMouseAction()+MOUSE_MODE_HELP;
        }
        public abstract String getMouseAction();
    } // end class NavigationModeMouseEventListener

    //Coordinate conversion methods
    public double pixelToCoordConvertX(int mapX) {
        double coordX = 1.0*(mapX - xMargin)/mapScaleFactor + originX;
        return coordX;
    }

    public void fireSelectionChanged(SelectionChangedEvent e) {
        SelectionChangedMonitor.fireSelectionChanged(e);
    }

    public void loadSelectedShotRecord(OBRecord obRecord) {
        if (selectedShotRecord == null) selectedShotRecord = new ShotRecord();
        selectedShotRecord.loadOB(obRecord, true);
        // selectedShotRecord.setSp(selectedShot);
        selectedShotRecord.loadReceiverXY(receivers.getTableData());
        shotLabel.setText(selectedShotRecord.toString());
    }

    public void updateShotPointLabel() {
        if (shots.size()>0 && selectedShot != null)
            shotLabel.setText(selectedShot.toString());
    }

    public void updateReceiverLabel() {
        if (receivers.size()>0 && selectedReceiver != null)
            receiverLabel.setText(selectedReceiver.toString());
    }

    public double pixelToCoordConvertY(int mapY) {
        //y-axis is flipped, so have to subtract y-position from window height
        double coordY = 1.0*(this.getHeight() - mapY - yMargin)/mapScaleFactor + originY;
        return coordY;
    }

    public int coordToPixelConvertX(double x) {
        int mapX = (int)((x-(double)originX)*mapScaleFactor+(double)xMargin);
        return mapX;
    }

    public int coordToPixelConvertY(double y) {
        //y-axis is flipped, so have to subtract y-position from window height
        int mapY = this.getHeight()-(int)((y-(double)originY)*mapScaleFactor+(double)yMargin);
        return mapY;
    }

    //make polygon from an array of Point2D.Double points
    public Polygon makePolygon(ArrayList<Point2D.Double> a) {
        int[] x = new int[a.size()];
        int[] y = new int[a.size()];
        for (int i=0;i<a.size();i++) {
            x[i] = coordToPixelConvertX(a.get(i).getX());
            y[i] = coordToPixelConvertY(a.get(i).getY());
        }
        return new Polygon(x,y,a.size());
    }
    //returns index of closest station in station array to mouse position
    public int findClosestStation(double x, double y, ArrayList<Station> stations) {
        double shortestThisDistance = Double.MAX_VALUE;
        double thisDistance=0;
        int shortestIndex=0;

        for (int i=0;i<stations.size();i++){
            thisDistance = (double)SUtil.distance(x,y,
                    stations.get(i).getX(),
                    stations.get(i).getY());
            if (thisDistance<shortestThisDistance){
                shortestThisDistance = thisDistance;
                shortestIndex = i;
            }
        }

        return shortestIndex;
    } // end findClosestStation

    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;

        drawingArea = new Rectangle2D.Double(
                originX - xMargin/mapScaleFactor,
                originY - yMargin/mapScaleFactor,
                getWidth()/mapScaleFactor,
                getHeight()/mapScaleFactor);

        //only paint stations when not zooming or drawing distance line (helps refresh rate)
        if (!dragging && !selectionChanged()) {
            //set background
            backPane = createImage(this.getWidth(), this.getHeight());
            Graphics2D g2d_backpane = (Graphics2D) backPane.getGraphics();
            g2d_backpane.setColor(backgroundColor);
            g2d_backpane.fillRect(0,0,this.getWidth(),this.getHeight());
            drawScatter(g2d_backpane);
            drawCdpGrid(g2d_backpane);
            drawShotRecStations(g2d_backpane);
        }

        g2d.drawImage(backPane, 0, 0, this);

        g2d.setFont(new Font("arial",Font.BOLD,12));

        //draw scale bar
        if (mapScaleFactor!=0)
        {
            g2d.setColor(foregroundColor);
            g2d.fillRect(getWidth()*2/3,getHeight()-10,70,3);
            g2d.drawString(df.format(70/mapScaleFactor)+" "+units,getWidth()*2/3,getHeight()-15);
        }

        //draw color bar
        if (colorBar != null) {
            colorBar.setForegroundColor(foregroundColor);
            colorBar.drawVertColorBar(g2d,10,(int)(this.getHeight()*0.425),(int)(this.getHeight()*0.15),10);
        }

        if (zoomRectangle != null) drawZoomRectangle(g2d);

        // overdraw old distance line, if exists
        //      if (distLineBegin != null && oldDistLineEnd != null) {
        //          drawDistanceLine(distLineBegin.x, distLineBegin.y, oldDistLineEnd.x, oldDistLineEnd.y, backgroundColor, g2d);
        //      }

        // draw distance line, if exists
        if (distLineBegin != null && distLineEnd != null) {
            drawDistanceLine(distLineBegin.x, distLineBegin.y, distLineEnd.x, distLineEnd.y, foregroundColor, g2d);
        }

        // draw area calculation polygon, if exists
        if (areaPoints != null) {
            drawAreaPolygon(areaPoints, g2d);
        }

        // draw selected shot record w/ spread, if exists
        if (selectedShotRecord != null && mouseListenManager.mel instanceof SelectFFIDMouseListener) {
            drawShotRecord(selectedShotRecord, g2d);
        }

        // fill in triangle of selected receiver
        if (selectedReceiver != null) {
            drawReceiver(selectedReceiverColor, selectedReceiver, g2d, symbolSize+3, true);
        }

        // fill in circle of selected shot
        if (selectedShot != null) {
            drawShotPoint(selectedShotColor, selectedShot, g2d, symbolSize+3, true);
        }
        oldSelectedReceiver = selectedReceiver;
        oldSelectedShot = selectedShot;
    }

    private boolean selectionChanged()
    {
        if (selectedShot == null || selectedReceiver == null) return true;
        if (selectedShot.equals(oldSelectedShot)) return false;
        if (selectedReceiver.equals(oldSelectedReceiver)) return false;
        return true;
    }

    private void drawCdpGrid(Graphics2D g2d)
    {
        boolean drawGrid = cdpBinningPane.isGridEnabled();
        if (!drawGrid) return;

        CdpModel cdpModel = cdpBinningPane.getCdpModel();
        int il0 = cdpModel.getOriginIL();
        int xl0 = cdpModel.getOriginXL();
        int numInlines = cdpModel.getNumInlines();
        int numXlines = cdpModel.getNumXlines();
        int xl1 = xl0 + numXlines-1;
        int il1 = il0 + numInlines-1;
        int inlineLabelInc = cdpBinningPane.getDisplayInlineLabelInc();
        int xlineLabelInc = cdpBinningPane.getDisplayXlineLabelInc();

        if (numInlines == 0 || numXlines == 0) return;

        g2d.setColor(cdpGridColor);

        //draw inlines
        int ilCount = 0;
        for (int i=0; i < numInlines; i += cdpBinningPane.getInlineDisplayInc()) {
            int il = il0 + i;
            CdpPoint p1 = cdpModel.getCdpPoint(il - 0.5, xl0 - 0.5); 
            CdpPoint p2 = cdpModel.getCdpPoint(il - 0.5, xl1 + 0.5);

            drawCdpLine(p1, p2, g2d);

            if(ilCount%inlineLabelInc == 0 && cdpBinningPane.isInlineLabelDrawEnabled()) {
                if (p2.y > p1.y || p2.x > p1.x) {
                    double angle = SUtil.azimuth(p1.x, p1.y, p2.x, p2.y);
                    drawLabel(" IL "+il, p2.x, p2.y, g2d, angle);
                }
                else {
                    double angle = SUtil.azimuth(p2.x, p2.y, p1.x, p1.y);
                    drawLabel(" IL "+il, p1.x, p1.y, g2d, angle);
                }
            }

            ilCount++;
        }
        CdpPoint p1 = cdpModel.getCdpPoint(il1 + 0.5, xl0 - 0.5); 
        CdpPoint p2 = cdpModel.getCdpPoint(il1 + 0.5, xl1 + 0.5);
        drawCdpLine(p1, p2, g2d);

        //draw xlines
        int xlCount = 0;
        for (int i=0; i < numXlines; i += cdpBinningPane.getXlineDisplayInc()) {
            int xl = xl0 + i;
            
            //... Draw cdp bin centers
            for (int j=0; j < numInlines; j += cdpBinningPane.getInlineDisplayInc()) {
                int il = il0 + j;
                CdpPoint p = cdpModel.getCdpPoint(il, xl);
                drawCdpPoint(p, g2d);
            }
            
            p1 = cdpModel.getCdpPoint(il0 - 0.5, xl - 0.5);
            p2 = cdpModel.getCdpPoint(il1 + 0.5, xl - 0.5);

            drawCdpLine(p1, p2, g2d);

            if(xlCount%xlineLabelInc == 0 && cdpBinningPane.isXlineLabelDrawEnabled()) {
                if (p2.x > p1.x || p2.y > p1.y) {
                    double angle = SUtil.azimuth(p1.x, p1.y, p2.x, p2.y);
                    drawLabel(" XL "+xl, p2.x, p2.y, g2d, angle);
                }
                else {
                    double angle = SUtil.azimuth(p2.x, p2.y, p1.x, p1.y);
                    drawLabel(" XL "+xl, p1.x , p1.y, g2d, angle);
                }
            }

            xlCount++;
        }
        p1 = cdpModel.getCdpPoint(il0 - 0.5, xl1 + 0.5);
        p2 = cdpModel.getCdpPoint(il1 + 0.5, xl1 + 0.5);
        drawCdpLine(p1, p2, g2d);

        //draw inlineLabels

    }

    private void drawCdpPoint(CdpPoint p, Graphics2D g2d)
    {
        int x = coordToPixelConvertX(p.x);
        int y = coordToPixelConvertY(p.y);

        if (drawingArea.contains(p.x, p.y)) {
            drawX(g2d, x, y, symbolSize/2+1);
        }
    }

    private void drawLabel(String string, double x, double y, Graphics2D g2d, double angle)
    {

        // while (angle > 90) angle -= 90; 

        int x1 = coordToPixelConvertX(x);
        int y1 = coordToPixelConvertY(y);

        g2d.translate(x1, y1);
        g2d.rotate(0-Math.toRadians(angle)); //angle is backwards of what you would think, y axis points down

        g2d.drawString(string, 0, 0);

        g2d.rotate(Math.toRadians(angle));
        g2d.translate(0-x1, 0-y1);

    }

    private void drawCdpLine(CdpPoint p1, CdpPoint p2, Graphics g2d)
    {
        int x1 = coordToPixelConvertX(p1.x);
        int y1 = coordToPixelConvertY(p1.y);
        int x2 = coordToPixelConvertX(p2.x);
        int y2 = coordToPixelConvertY(p2.y);

        if (drawingArea.intersectsLine(p1.x, p1.y, p2.x, p2.y)) {
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawAreaPolygon(ArrayList<Point2D.Double> areaPoints2, Graphics2D g2d)
    {
        g2d.setColor(areaPolygonColor);
        // draw polygon
        g2d.drawPolygon(makePolygon(areaPoints2));
        // write area

        double rightMostX = areaPoints.get(0).x;
        double rightMostY = areaPoints.get(0).y;
        for (Point2D.Double point: areaPoints2) {
            if (rightMostX < point.x) {
                rightMostX = point.x;
                rightMostY = point.y;
            }
        }

        double area = calcPolygonArea(areaPoints2);
        g2d.drawString(" Area= "+formatArea(area),
                coordToPixelConvertX(rightMostX),
                coordToPixelConvertY(rightMostY));
    }

    private void drawScatter(Graphics2D g2d)
    {
        if (scatter == null) return;
        g2d.setColor(foregroundColor.darker());
        for (Point2D.Double point: scatter) {
            if (point == null) return; //stop drawing at end of scatter (scatter not guaranteed to be non-null)
            if (drawingArea.contains(point.getX(), point.getY())) {
                int x = this.coordToPixelConvertX(point.x);
                int y = this.coordToPixelConvertY(point.y);
                g2d.drawLine(x-1, y, x+1, y);
                g2d.drawLine(x, y-1, x, y+1);
            }
        }
    }

    private String formatArea(double area)
    {
        String s = "";
        Double areaFactor = 0.0;
        if (units == units.Feet) {
            areaFactor = SquareMile;
            s = "square miles";
        } else {
            areaFactor = SquareKilometer;
            s = "square km";
        }
        return df.format(area/areaFactor)+" "+s;
    }

    private void drawShotRecord(ShotRecord shotRecord, Graphics2D g2d) {
        Receiver[] receivers = shotRecord.getReceivers();
        ArrayList<Integer> chans = shotRecord.getChans();
        if(chans.size() < 1) return;
        int firstChan = chans.get(0);
        int lastChan = chans.get(chans.size()-1);
        int counter = 0;
        for (int chan: shotRecord.getChans()) {
            Receiver r = receivers[counter++];
            drawReceiver(foregroundColor, r, g2d, symbolSize, true);
            if (channelLabelInterval > 0) {
                int mod = chan%channelLabelInterval;
                if (mod == 0 || chan == firstChan || chan == lastChan) {
                    g2d.setColor(selectedReceiverColor);
                    g2d.drawString(""+chan,
                            coordToPixelConvertX(r.getX()),
                            coordToPixelConvertY(r.getY()));
                }
            }
        }
        if (channelLabelInterval > 0) {
            g2d.setColor(selectedShotColor.darker());
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            g2d.drawString("Shot: "+shotRecord.getFfid(),
                    coordToPixelConvertX(shotRecord.getSp().getX()),
                    coordToPixelConvertY(shotRecord.getSp().getY()));
        }
    }

    private void drawDistanceLine(double x, double y, double x2, double y2, Color color, Graphics2D g2d) {
        int pixX1 = coordToPixelConvertX(x);
        int pixY1 = coordToPixelConvertY(y);
        int pixX2 = coordToPixelConvertX(x2);
        int pixY2 = coordToPixelConvertY(y2);
        int pixX = 0;
        int pixY = 0;

        // draw line
        g2d.setColor(color);
        g2d.drawLine(pixX1, pixY1, pixX2, pixY2);

        // calculate distance
        double dist = SUtil.distance(x, y, x2, y2);
        double angle = SUtil.azimuth(x, y, x2, y2);

        // write distance to right of line
        if (pixX1 > pixX2) {
            pixX = pixX1;
            pixY = pixY1;
        }
        else {
            pixX = pixX2;
            pixY = pixY2;
        }
        g2d.drawString(df.format(dist) + " " + units + " "+ANGLE_SYMBOL+" = "+df.format(angle)+DEGREE_SYMBOL, pixX, pixY);
    }

    private void drawZoomRectangle(Graphics2D g2d) {
        // reset old zoom rectangle to background color
        if (oldZoomRectangle != null) {
            g2d.setColor(backgroundColor);
            g2d.drawRect((int) oldZoomRectangle.getX(),
                    (int) oldZoomRectangle.getY(), (int) oldZoomRectangle.getWidth(),
                    (int) oldZoomRectangle.getHeight());
        }

        // draw zoom rectangle if zooming
        if (zoomRectangle != null) {
            g2d.setColor(zoomRectangleColor);
            g2d.drawRect((int) zoomRectangle.getX(),
                    (int) zoomRectangle.getY(), (int) zoomRectangle.getWidth(),
                    (int) zoomRectangle.getHeight());
        }
    }

    private void drawShotRecStations(Graphics2D g2d) {

        //if no map to draw, give help message
        if (receivers.size()==0 && shots.size()==0) {
            g2d.setColor(foregroundColor);
            g2d.drawString("Click on a Receiver or Shot \"+\" button to begin.",
                    (int)(getWidth()*0.5)-100,(int)(getHeight()*0.5));
            return;
        }

        Color color = null;
        //start painting receivers
        if (showReceivers) {
            Receiver receiver = null;
            int recVariableIndex = receivers.getColumnIndex(symbolColorMode);
            for (int r=0;r<receivers.size();r++) {
                receiver = (Receiver)receivers.get(r);
                //draw triangles if they land in the current zoom area (drawing area)
                if (drawingArea.contains(receiver.getX(), receiver.getY())) {
                    if (symbolColorMode.equals(RECEIVER_VS_SHOT) || colorBar == null)
                        color = receiverColor;
                    else if (recVariableIndex > -1)
                        color = colorBar.assignColor(SUtil.getNumVal(receivers.getValueAt(r, recVariableIndex)));
                    else
                        color = ColorBar.DEFAULT_COLOR;
                    drawReceiver(color, receiver, g2d);
                }
            }
        }

        //start painting shots
        if (showShotpoints) {
            SP sp = null;
            int spVariableIndex = shots.getColumnIndex(symbolColorMode);
            for (int s=0;s<shots.size();s++) {
                //draw circlesif they land in the current zoom area (drawing area)
                sp = (SP)shots.get(s);
                if (drawingArea.contains(sp.getX(), sp.getY())) {
                    if (symbolColorMode.equals(RECEIVER_VS_SHOT) || colorBar == null)
                        color = shotColor;
                    else if (spVariableIndex > -1)
                        color = colorBar.assignColor(SUtil.getNumVal(shots.getValueAt(s, spVariableIndex)));
                    else
                        color = ColorBar.DEFAULT_COLOR;
                    drawShotPoint(color, sp, g2d);
                }
            }
        }
    }

    /**
     * draw SP sp as circle centered around sp.x and sp.y of default size and not filled
     * @param color
     * @param sp
     * @param g2d
     */
    private void drawShotPoint(Color color, SP sp, Graphics2D g2d) {
        drawShotPoint(color, sp, g2d, symbolSize, false);
    }

    private void drawShotPoint(Color color, SP sp, Graphics2D g2d, int size, boolean filled) {
        int x = (int) (coordToPixelConvertX(sp.getX()));
        int y = (int)(coordToPixelConvertY(sp.getY()));
        g2d.setColor(color);
        if (filled) g2d.fillOval((int)(x-size/2.0),(int)(y-size/2.0), size, size);
        else {
            g2d.drawOval((int)(x-size/2.0), (int)(y-size/2.0), size, size);
            if (sp.kill) drawX(g2d, x, y, size);
        }
    }

    private void drawX(Graphics g2d, int x, int y, int size)
    {
        size = (int) (size/2.0);
        g2d.drawLine(x-size, y-size, x+size, y+size);
        g2d.drawLine(x-size, y+size, x+size, y-size);
    }

    /**
     * Draw receiver with default symbol size and no fill
     * 
     * @param color
     * @param receiver
     * @param g2d
     */
    private void drawReceiver(Color color, Receiver receiver, Graphics2D g2d) {
        drawReceiver(color, receiver, g2d, symbolSize, false);
    }

    /**
     * draw receiver as a triangle centered around receiver coordinates using the given color.
     * 
     * @param color
     * @param receiver
     * @param g2d
     * @param size
     * @param filled
     */
    private void drawReceiver(Color color, Receiver receiver, Graphics g2d, int size, boolean filled) {
        g2d.setColor(color);
        int x=coordToPixelConvertX(receiver.getX());
        int y=(int) (coordToPixelConvertY(receiver.getY())); //center triangle over coordinate
        if (filled) g2d.fillPolygon(Triangle.getTriangle(x,(int) (y+size/2.0),size));
        else {
            g2d.drawPolygon(Triangle.getTriangle(x,(int) (y+size/2.0),size));
            if (receiver.kill) drawX(g2d, x, y, size);
        }
    }

    public void setSymbolSize(int n) {
        if (n>0)
            symbolSize = n;
    }

    public int getSymbolSize() {
        return symbolSize;
    }

    public Object[] getSymbolColorModeOptions() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(RECEIVER_VS_SHOT); 
        String[] receiverList = null;
        String[] spList = null;
        if (receivers != null && receivers.size() > 0)
            receiverList = receivers.getColumnNames();
        if (shots != null && shots.size() > 0)
            spList = shots.getColumnNames();
        String[] totalList = SUtil.arrayCat(receiverList, spList);
        if (totalList == null) return list.toArray();
        Arrays.sort(totalList);
        totalList = SUtil.uniq(totalList);
        for (int i=0;i<totalList.length;i++)
            list.add(totalList[i]);
        return list.toArray();
    }

    public void setSymbolColorMode(String s) {
        symbolColorMode = s;
        /*
        if(receivers == null) return;
        String[] list = ((Station)receivers.get(0)).getColumnNames();
        int i;
        for (i=0;i<list.length;i++) 
            if (list[i].equals(s)) break;
        selectedVariableIndex = i; //index of symbol color mode in station variable list.
                                   //will be > variable list length if it's RECEIVER_VS_SHOT
         */
        setColorBar();
    }

    public String getSymbolColorMode() {
        return symbolColorMode;
    }

    public void setColorBar() {
        colorBar = null;
        //if not shot vs. receiver, find min max values and set colorbar
        if ( !symbolColorMode.equals(RECEIVER_VS_SHOT) ) { //&& (selectedVariableIndex < Station.variableList.length)) {
            int sColIndex = shots.getColumnIndex(symbolColorMode);
            double shotMax = shots.getMax(sColIndex);
            double shotMin = shots.getMin(sColIndex);
            int rColIndex = receivers.getColumnIndex(symbolColorMode);
            double recMax = receivers.getMax(rColIndex);
            double recMin = receivers.getMin(rColIndex);

            if (sColIndex < 0 || !showShotpoints) shotMax = shotMin = recMax; //ignore min/max of shot if index not available
            if (rColIndex < 0 || !showReceivers) recMax = recMin = shotMax;//ignore min/max of receiver if index not available

            double minVal=Math.min(shotMin, recMin);
            double maxVal=Math.max(shotMax, recMax);
            //once max and min found, set colorbar for value range and selected variable name
            colorBar = new ColorBar(symbolColorMode,minVal, maxVal);
        }
    }

    public String printHelp() {
        return mouseListenManager.toString();
    }

    //calculate polygon area using an array list of Point2D.Double points
    public double calcPolygonArea(ArrayList<Point2D.Double> areaPoints2) {
        double area=0;

        //more than 2 points needed for area
        if (areaPoints2.size()>2) {
            //program to calculate area using Meister's 1769 formula:
            //A = (1/2)*(x1y2 - x2y1 + x2y3 - x3y2 + .... + xny1 - x1yn)
            // for area of arbitrary polygon in cartesian coordinates -
            // x/y points must go in counter-clockwise direction 
            //(taken from: http://en.wikipedia.org/wiki/Polygon#Area)
            for (int i=0;i<areaPoints2.size()-1;i++)
                area = 0.5*( 
                        areaPoints2.get(i).x * areaPoints2.get(i+1).y -
                        areaPoints2.get(i+1).x * areaPoints2.get(i).y ) +
                        area;
            area = 0.5*( 
                    areaPoints2.get(areaPoints2.size()-1).x * areaPoints2.get(0).y -
                    areaPoints2.get(0).x * areaPoints2.get(areaPoints2.size()-1).y ) +
                    area;
        }
        return Math.abs(area); //taking absolute value for case when user clicks in clockwise direction
    }

    public boolean isPrinterFriendlyMode() {
        return printerFriendlyMode;
    }

    public void setPrinterFriendlyMode(boolean printerFriendlyMode) {
        if (printerFriendlyMode) {
            backgroundColor = Color.white;
            foregroundColor = Color.black;
            receiverColor = Color.magenta;
            shotColor = Color.CYAN;
            selectedReceiverColor = new Color(0,128,128); //sea green?
            selectedShotColor = Color.red;
            areaPolygonColor = Color.BLUE;
        }
        else {
            backgroundColor = Color.black;
            foregroundColor = Color.white;
            receiverColor = Color.GREEN;
            shotColor = Color.RED;
            selectedReceiverColor = Color.PINK;
            selectedShotColor = Color.CYAN;
            areaPolygonColor = Color.YELLOW;
        }
        receiverLabel.setBackground(backgroundColor);
        receiverLabel.setForeground(selectedReceiverColor);
        shotLabel.setBackground(backgroundColor);
        shotLabel.setForeground(selectedShotColor);
        mouseLabel.setForeground(foregroundColor);
        this.printerFriendlyMode = printerFriendlyMode;
        this.repaint();
    }

    public int getChannelLabelInterval() {
        return channelLabelInterval;
    }

    public void setChannelLabelInterval(int channelLabelInterval) {
        if (channelLabelInterval >= 0)
            this.channelLabelInterval = channelLabelInterval;
    }

    public ShotRecord getSelectedShotRecord() {
        return selectedShotRecord;
    }

    /**
     * called by Select Shot Record combobox in Mapper.
     * calls fireSelectionChanged() to update spreadsheet in EditPane.
     * (don't call this method from EditPane, otherwise there will be infinite loop)
     * @param shot
     */
    public void setSelectedShotRecord(int shot) {
        int i=0;
        if (selectedShotRecord == null) selectedShotRecord = new ShotRecord();
        for (i=0;i<obRecords.size();i++)
            if ( ((OBRecord)obRecords.get(i)).getShot() == shot)
                break;
        if (i<obRecords.size()) {
            loadSelectedShotRecord((OBRecord)obRecords.get(i));
        }
        else {
            selectedShotRecord = null;
            JOptionPane.showMessageDialog(StationPlotter.this,"<HTML>Couldn't find selected Shot:\n"+shot+
                    "in OB file(s)",
                    "OB Search Error",JOptionPane.ERROR_MESSAGE);
        }
        int spIndex = shots.indexOf(selectedShotRecord.getSp());
        if (spIndex < 0) {
            JOptionPane.showMessageDialog(StationPlotter.this,"<HTML>Couldn't find Shot Point for selected Shot:\n"+shot+
                    "Shot Point:\n"+selectedShotRecord.getSp(),
                    "OB Search Error",JOptionPane.ERROR_MESSAGE);
        } else {
            selectedShot = (SP) shots.get(spIndex);
            ffidButton.doClick();
            fireSelectionChanged(new SelectionChangedEvent(selectedShotRecord, i));
        }
    }

    private void setSelectedShotPoint(int lineNumber, int stationNumber) {
        if (shots == null || shots.size() == 0) return;
        for (Object o: shots) {
            SP sp = (SP)o;
            if (sp.lineNumber == lineNumber && sp.stationNumber == stationNumber) {
                selectedShot = sp;
                return;
            }
        }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent e) {
        Object source = e.getSource();
        int i = e.getIndex();
        if (source == Receivers) {
            selectedReceiver = (Receiver) receivers.get(i);
            updateReceiverLabel();
        }
        else if (source == ShotPoints) {
            selectedShot = (SP) shots.get(i);
            updateShotPointLabel();
        }
        else if (source == ShotRecords) {
            OBRecord obr = (OBRecord) obRecords.get(i);
            setSelectedShotPoint(obr.getSourceLineNumber(), obr.getSourceStationNumber());
            loadSelectedShotRecord(obr);
        }
        else {
            //SUtil.printErr("StationPlotter.setSelectedItemIndex() - unknown item name - " + source);
            return;
        }
        repaint();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        setSymbolColorModeBox();
        setColorBar();
        this.repaint();
    }

    private void setSymbolColorModeBox()
    {
        int i = symbolColorModeBox.getSelectedIndex();
        symbolColorModeBox.setModel(new DefaultComboBoxModel(this.getSymbolColorModeOptions()));
        if (getSymbolColorModeOptions().length>i) symbolColorModeBox.setSelectedIndex(i);
    }

    public void addMouseChangedListener(ChangeListener l)
    {
        mouseChangedListeners.add(l);
    }

    public void reset()
    {
        setSymbolColorModeBox();
        zoomReset();
        setColorBar();
        scatter = null;
        oldScatter = null;
        selectedShotRecord = null;
    }

    public void setScatterIncrement(int inc)
    {
        if (this.scatterInc != inc) {
            this.scatterInc = inc;
            scatter = null;
            oldScatter = null;
        }
    }

    public int getScatterIncrement()
    {
        return scatterInc;
    }

    public void setCdpBinningPane(CdpGridPane cdpBinningPane)
    {
        this.cdpBinningPane = cdpBinningPane;
    }

    public ArrayList<TableData> getReceivers()
    {
        return receivers.tableData;
    }

    public ArrayList<TableData> getShotPoints()
    {
        return shots.tableData;
    }

    public void setAdjustOriginMouseListener()
    {
        this.setMouseListener(new AdjustOriginMouseListener());
    }

    public class AdjustOriginMouseListener extends PlotterMouseEventListener
    {

        private double startDragX;
        private double startDragY;

        @Override
        public void mouse1Clicked(MouseEvent e)
        {

        }

        @Override
        public String getMouseAction()
        {
            return "Select Station";
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            CdpModel cdpModel = cdpBinningPane.getCdpModel();
            startDragX = cdpModel.originX;
            startDragY = cdpModel.originY;
        }

        /**
         * For left-mouse drag, pan CDP grid
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            getMouseXY(e);
            //System.out.print("dragging");
            switch (e.getModifiersEx()) {
                case MouseEvent.BUTTON1_DOWN_MASK:
                    CdpModel cdpModel = cdpBinningPane.getCdpModel();
                    cdpModel.setOriginX(startDragX + mouseX - mouseX0);
                    cdpModel.setOriginY(startDragY + mouseY - mouseY0);
                    cdpModel.calcCornersFromAngle();
                    cdpBinningPane.setFieldsFromModel();
                    repaint();
                    break;
                case MouseEvent.BUTTON2_DOWN_MASK:
                    //we're drawing distance line!
                    Point2D.Double tmpPt = new Point2D.Double(mouseX,mouseY);
                    if (!distLineEnd.equals(tmpPt)){
                        oldDistLineEnd = distLineEnd;
                        distLineEnd = tmpPt;
                        tmpPt = null;
                    }
                    dragging = true;
                    repaint();
                    break;
                case MouseEvent.BUTTON3_DOWN_MASK:
                    //we're drawing zoom box!
                    Rectangle tmp = new Rectangle();
                    tmp.setRect(
                            Math.min(zoomStart.getX(),e.getX()),
                            Math.min(zoomStart.getY(),e.getY()),
                            Math.abs(e.getX()-zoomStart.getX()),
                            Math.abs(e.getY()-zoomStart.getY()));
                    //check to see if zoom rectangle is changing, then set old zoom
                    // so that we can paint over it with background color
                    if (!zoomRectangle.equals(tmp)){
                        oldZoomRectangle = zoomRectangle;
                        zoomRectangle = tmp;
                        tmp = null;
                    }
                    dragging = true;
                    repaint();
                    break;
            }
            resetLabel(e);
        }

        /**
         * Go back to normal pan mode when done adjusting CDP grid
         */
        public void mouseReleased(MouseEvent e) {
            cdpBinningPane.setNormalPanMouseMode();
        }

    }

    public void setNormalPanMouseListener()
    {
        this.setMouseListener(new SelectMouseListener());
    }

    public void setAdjustAngleMouseListener()
    {
        this.setMouseListener(new AdjustAngleMouseListener());
    }

    public class AdjustAngleMouseListener extends PlotterMouseEventListener
    {

        private double startAngle;
        private double mouseSensitivity = 0.001;

        @Override
        public void mouse1Clicked(MouseEvent e){}

        @Override
        public String getMouseAction()
        {
            return "Select Station";
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            CdpModel cdpModel = cdpBinningPane.getCdpModel();
            startAngle = cdpModel.angle;
        }

        /**
         * For left-mouse drag, rotate CDP grid
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            getMouseXY(e);
            //System.out.print("dragging");
            switch (e.getModifiersEx()) {
                case MouseEvent.BUTTON1_DOWN_MASK:
                    CdpModel cdpModel = cdpBinningPane.getCdpModel();
                    cdpModel.setAngle(startAngle + (mouseX0 - mouseX)*mouseSensitivity );
                    cdpModel.calcCornersFromAngle();
                    cdpBinningPane.setFieldsFromModel();
                    repaint();
                    break;
                case MouseEvent.BUTTON2_DOWN_MASK:
                    //we're drawing distance line!
                    Point2D.Double tmpPt = new Point2D.Double(mouseX,mouseY);
                    if (!distLineEnd.equals(tmpPt)){
                        oldDistLineEnd = distLineEnd;
                        distLineEnd = tmpPt;
                        tmpPt = null;
                    }
                    dragging = true;
                    repaint();
                    break;
                case MouseEvent.BUTTON3_DOWN_MASK:
                    //we're drawing zoom box!
                    Rectangle tmp = new Rectangle();
                    tmp.setRect(
                            Math.min(zoomStart.getX(),e.getX()),
                            Math.min(zoomStart.getY(),e.getY()),
                            Math.abs(e.getX()-zoomStart.getX()),
                            Math.abs(e.getY()-zoomStart.getY()));
                    //check to see if zoom rectangle is changing, then set old zoom
                    // so that we can paint over it with background color
                    if (!zoomRectangle.equals(tmp)){
                        oldZoomRectangle = zoomRectangle;
                        zoomRectangle = tmp;
                        //tmp = null;
                    }
                    dragging = true;
                    repaint();
                    break;
            }
            resetLabel(e);
        }

        /**
         * Go back to normal pan mode when done adjusting CDP grid
         */
        //        @Override
        //        public void mouseReleased(MouseEvent e) {
        //            cdpBinningPane.setNormalPanMouseMode();
        //        }

    }

    public void setSelectedFFID(Object ffid)
    {
        int iffid = (int)SUtil.sval(ffid+"");
        int row = obRecords.findRow("FFID", iffid);
        Object shotID = obRecords.getValueAt(row, "Shot");
        setSelectedShotRecord(shotID);
    }

    public void setSelectedShotRecord(Object shot)
    {
        int ishot = (int)SUtil.sval(shot+"");
        setSelectedShotRecord(ishot);
    }
}
