package com.tricongeophysics;

import java.util.ArrayList;

public class FocusStatKey {

	public static final String Map = "MAP";
	public static final String Statkey = "STATKEY";
	private String lineName;
	private ArrayList<Integer> recLines;

	public FocusStatKey(String line, ArrayList<Integer> recLines) {
		lineName = line;
		this.recLines = recLines;
	}

	public String getLine() {
		return lineName;
	}

	public int[] getData() {
		if (recLines == null) return null;
		int[] data = new int[recLines.size()];
		for (int i=0; i<data.length; i++) {
			data[i] = recLines.get(i);
		}
		return data;
	}

}
