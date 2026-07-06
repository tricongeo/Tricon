package com.tricongeophysics;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import java.awt.Color;

import java.awt.Graphics2D;

import java.text.*;

public class ColorBar extends JPanel {
	
	protected double minVal;
	protected double maxVal;
	protected String title;
	static final Color DEFAULT_COLOR = Color.GRAY;
	protected Color foregroundColor = Color.WHITE;
	protected DecimalFormat df = new java.text.DecimalFormat("##,##0.00"); // 1.4 code
	
	public ColorBar(String t, double min, double max) {
		super(); //is member of JPanel so that I can have it draw itself later if I want to
		title = t;
		minVal = min;
		maxVal = max;
	}

	public double getMaxVal() {
		return maxVal;
	}

	public void setMaxVal(double maxVal) {
		this.maxVal = maxVal;
	}

	public double getMinVal() {
		return minVal;
	}

	public void setMinVal(double minVal) {
		this.minVal = minVal;
	}
	
	/**
	 * set the minimum value to lavender and the max color to red 
	 * w/ green and yellow inbetween (based on hue/saturation/brightness scale)
	 * @param v
	 * @return
	 */
	public Color assignColor(double v) {
		double range = maxVal - minVal;
		if (range == 0) return DEFAULT_COLOR;
		v = Math.max(v, minVal);
		v = Math.min(v, maxVal);
		return Color.getHSBColor(0.7f-(float)(0.7*((v-minVal)/range)),1.0f,1.0f);
	}

	public void drawVertColorBar(Graphics2D g2d, int x, int y, int len, int w){
		double range = maxVal - minVal;
		double inc = range/len;
		g2d.setColor(foregroundColor);
		g2d.drawString(title,x,y);
		g2d.drawString(df.format(maxVal),x,y+15);
		for (int i=0; i<len; i++) {
			g2d.setColor(assignColor(maxVal-i*inc));
			g2d.drawLine(x,y+i+17,x+w,y+i+17);
		}
		g2d.setColor(foregroundColor);
		g2d.drawString(df.format(minVal),x,y+len+30);
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}
}
	