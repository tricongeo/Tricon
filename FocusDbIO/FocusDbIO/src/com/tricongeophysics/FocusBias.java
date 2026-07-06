package com.tricongeophysics;

public class FocusBias {

	public static final String Coord = "COORD";
	public static final String Bias = "BIAS";
	private static final String X = "X";
	private static final Object Y = "Y";
	public static final String Geometry = "GEOMETRY";
	private int xBias;
	private int yBias;
	

	public void loadBias(int[] bias2) {
		if (bias2 == null) {
			System.err.println("Trying to set Focus Bias to NULL");
			return;
		}
		if (bias2.length < 2) {
			System.err.println("Focus Bias array is wrong length = " + bias2.length);
			return;
		}
		xBias = bias2[0];
		yBias = bias2[1];
	}


	public void applyBias(Station s) {
		s.x = s.x + xBias;
		s.y = s.y + yBias;
	}


	public void applyBias(CdpModel m) {
		m.originX = m.originX + xBias;
		m.originY = m.originY + yBias;
	}


	public void initialize(CdpModel cdpModel) {
		double maxX = cdpModel.originX;
		double maxY = cdpModel.originY;
		CdpPoint[] p = cdpModel.getPoints();
		for(CdpPoint point: p) {
			maxX = Math.max(maxX, point.x);
			maxY = Math.max(maxY, point.y);
		}
		xBias = ((int) (maxX / 10000000))*10000000;
		yBias = ((int) (maxY / 10000000))*10000000;
	}


	public void removeBias(FocusDbAttr atr) {
		if (xBias == 0 && yBias == 0) return;
		if (!atr.getAttribute().equals(Coord)) return; //attribute not coordinate, so do nothing
		String event = atr.getEvent();
		int bias = 0;
		if (event.equals(X)) bias = xBias;
		if (event.equals(Y)) bias = yBias;
		if (bias == 0) return;
		float[] vals = atr.getVals();
		if (vals == null) {
			System.err.println("Trying to remove bias from null attribute "+atr);
			return;
		}
//		for (float v: vals) { iterator passes copy, not actual value
		for (int i=0;i<vals.length;i++) {
			vals[i] = vals[i] - bias;
		}
	}
	
	public void removeBias(CdpModel m) {
		m.originX = m.originX - xBias;
		m.originY = m.originY - yBias;
	}


	public int[] getData() {
		int[] data = new int[] {xBias, yBias};
		return data;
	}
}
