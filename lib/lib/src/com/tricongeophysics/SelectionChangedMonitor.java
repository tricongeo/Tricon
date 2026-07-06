package com.tricongeophysics;

import java.util.ArrayList;

public class SelectionChangedMonitor {

	private static SelectionChangedMonitor selectionChangedMonitor;
	private ArrayList<SelectionListener> selectionListeners;

	private SelectionChangedMonitor() {
		selectionListeners = new ArrayList<SelectionListener>();
	}
	
	public static void addListener(SelectionListener l) {
		getSelectionChangedMonitor().addListener1(l);
	}

	public static SelectionChangedMonitor getSelectionChangedMonitor() {
		if(selectionChangedMonitor == null) selectionChangedMonitor = new SelectionChangedMonitor();
		return selectionChangedMonitor;
	}
	
	public void addListener1(SelectionListener l) {
		selectionListeners.add(l);
	}
	
	public static void fireSelectionChanged(SelectionChangedEvent e) {
		getSelectionChangedMonitor(); //make sure it exists first!!
		for(SelectionListener l: selectionChangedMonitor.selectionListeners) l.selectionChanged(e);
	}

}
