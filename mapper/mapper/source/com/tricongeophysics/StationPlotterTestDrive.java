package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;

public class StationPlotterTestDrive {
private StationPlotter stationPlotter;
private ArrayList<Station> stations = new ArrayList<Station>() ;
private final int numStas=5;

    public static void main (String[] args) {
	StationPlotterTestDrive sptd = new StationPlotterTestDrive();
	sptd.go();
    }

    public void go(){
	JFrame frame = new JFrame("plotter test");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	//make plot button
	JButton plotButton = new JButton("plot");
	plotButton.addActionListener( new ActionListener() {
	    public void actionPerformed(ActionEvent ev) {
		stationPlotter.repaint();
	    }
	});	    

	//make stations to plot
	for (int i=0;i<numStas;i++) {
	    stations.add(new Station());
	    stations.get(i).setX(i);
	    stations.get(i).setY(i);
	    stations.get(i).setZ(i);
	    stations.get(i).setStationNumber(i);
	    stations.get(i).setLineNumber(i);
	}

	//make station plotter
//	stationPlotter = new StationPlotter(stations,stations,stations);

	//add components to frame
	frame.getContentPane().add(BorderLayout.CENTER,stationPlotter);
	frame.getContentPane().add(BorderLayout.SOUTH,plotButton);
    }
}
