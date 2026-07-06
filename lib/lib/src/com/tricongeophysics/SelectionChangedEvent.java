package com.tricongeophysics;

import javax.swing.event.ChangeEvent;

public class SelectionChangedEvent extends ChangeEvent {

	private int index;

	public SelectionChangedEvent(Object source) {
		super(source);
		// TODO Auto-generated constructor stub
	}

	public SelectionChangedEvent(Object source, int index) {
		super(source);
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public Object getSource() {
		return this.source;
	}
}
