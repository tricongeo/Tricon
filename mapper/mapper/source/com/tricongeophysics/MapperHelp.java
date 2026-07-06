package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;


public class MapperHelp extends JFrame {
	public static void showDialog() {
		MapperHelp mh = new MapperHelp();
		mh.go();
	}
	public void go(){
	    java.net.URL imgURL = MapperHelp.class.getResource("docs/help.html");
	    String path = imgURL.toExternalForm();
        JScrollPane scroller = new JScrollPane(new HtmlViewer(path));
        getContentPane().add(scroller);
        scroller.setPreferredSize(new Dimension(1000, 1200));
        pack();
        addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
               // System.exit(0);
            }
        });
        setVisible(true);
	}
	
	public static void main (String Args[]) {
		MapperHelp mh = new MapperHelp();
		mh.go();
	}
    public static String getHelp() {
	return "<html>"+
	    "<font size=+2 color=blue>"+
	    "Getting Started:</font>"+"<br>"+
	    "<br>"+"<font size=+1 color=blue>"+
	    "Mapper v2.0 -</font> <font color=blue> seismic geometry text file QC."+"<br>"+
	    "<br></font>"+
	    "<font size=+1 color=blue>"+
	    "To Use:</font>"+"<br>"+
	    "1) Open a <font color=green>receiver</font> coordinate file by clicking the \"+\" button in the " +
	    "<font color=green>Receiver</font> button group."+"<br>"+
	    "2) Browse to find your file, then click \"Add\"."+"<br>"+
	    "3) The \"Select receiver station column ranges\" dialog pops up."+"<br>"+
	    "4) Click on the first line of text that contains the coordinates that you want to plot." +"<br>"+
	    "5) Click the \"Last Line\" button. It will put the row count for your file into the text field."+"<br>"+
	    "6) Click the \"Key\" button. Now click-and-drag over a text character that appears in the same column"+"<br>"+
	     " of every line containing data"+"<br>"+
	     " (if such a character does not exist, uncheck the \"Use Character Key\" box next to the\"OK\" button)"+"<br>"+
	    "7) Click on the \"Line\" button. Now click-and-drag to select the first and last column of each <font color=green>receiver</font> line."+"<br>"+
	    "8) Repeat step 7 for the remaining buttons (Station, X, Y, Z)"+"<br>"+
	    " Note: Double-Clicking instead of Click-and-Drag also works"+"<br>"+
	    "9) When you are done - Click OK"+"<br>"+
	    "<br>"+
	    "Now you should see your <font color=green>receiver</font> stations plotted as <font color=green>green</font> triangles."+"<br>"+
	    "To do the same with your <font color=red>shot</font> stations, go through steps 1-9 again, but"+"<br>"+
	    "this time use the <font color=red>\"Shots\"</font> button group."+"<br>"+
	    "To color by elevation instead of shot/receiver, go to Display->Symbol Color Mode->Elevation."+"<br>"+
	    "<br>"+
	    "10) To add <font color=green>receiver</font> spread information, click the \"+\" button in the <font color=blue>OB</font> button group."+"<br>"+
	    "11) Now, select the correct columns as before, except use <font color=green>receiver</font> spread columns instead" +
	    " of coordinates."+
	    "<br>"+
	    "12) Click OK. Now click on any <font color=red>shot</font> to see the associated <font color=blue>FFID</font> and channel information."+"<br>"+
	    "<br>"+
	    "<font size=+1 color=blue>"+
	    "Navigating the Plot:</font>"+"<br>"+
	    "<font color=blue>"+
	    "Nearest stations</font> - The nearest <font color=red>shot</font> (<font color=00ffff>cyan</font>) and <font color=green>receiver</font> (<font color=ff7f7f>pink</font>) to the mouse are highlighted at all times."+"<br>"+
	    "<font color=green>Receiver</font> information is displayed in the upper left-hand corner, while <font color=red>shot</font>"+"<br>"+
	    "information is displayed in the upper right-hand corner."+"<br>"+
	    "<br>"+
	    "<font color=blue>"+
	    "Zooming</font> - To zoom, right-click and drag across the area you want to inspect more closely. A <font color=ff00ff>magenta</font>"+"<br>"+
	    "rectangle will appear over the area (rectangle may not be visible unless you drag slowly on large 3Ds)."+"<br>"+
	    " To zoom back out, right-click (without the drag). This resets to fully zoomed out."+"<br>"+
	    " NOTE: To zoom out even farther, use Display->Zoom and type in something greater than 100%."+"<br>"+
	    "<br>"+
	    "<font color=blue>"+
	    "Panning</font> - Left-Click and drag to pull the map in any direction you wish."+"<br>"+
	    "<br>"+
	    "<font color=blue>"+
	    "Calculate Distance/Angle</font> - Middle-Click and drag to draw a white line across the screen. The length of"+"<br>"+
	    " this line in map units is displayed as well as the angle in degrees counter-clockwise from due East."+"<br>"+
	    " Double-Middle-Click to make the line disappear."+"<br>"+
	    "<br>"+
	    "<font color=blue>"+
	    "Calculate Area</font> - Single-Middle-Click to add points to the area polygon. The area is displayed to the right"+"<br>"+
	    " of the first point you click, so choose your first point wisely. Double-Middle-Click to hide the area polygon."+"<br>"+
	    "<br>"+
	    "<font color=blue>"+
	    "Select FFID/Receiver Spread</font> - Left-Click on any <font color=red>shot</font> to select an FFID. "+
	    "<font color=red>Shot</font>, <font color=green>Receiver</font>, and <font color=blue>OB</font> files must be loaded." +"<br>"+
	    " Active receivers for that FFID will be highlighted in white (Printer Friendly (No)) or <font color=black>black</font> (Printer Friendly (Yes))."+"<br>"+
	    "To change the channel numbering interval, go to Display->Channel Label Increment (the default is 10)."+"<br>"+
	    "<br>"+
	    "<font size=+1 color=blue>"+
	    "More Help:</font>"+"<br>"+
	    "For more help, send e-mail to <a href=\"mailto:scott.cook@tricongeophysics.com\">"+
	    "Scott.Cook@tricongeophysics.com</a>";
	    }
}
