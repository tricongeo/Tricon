package com.tricongeophysics;

import java.awt.Polygon;

public class Triangle {
	private static Polygon polygon;

	public static Polygon getTriangle(int x, int y, int height) {
		if (polygon == null) polygon = new Polygon();
		polygon.reset();
		double halfWidth = 0.577350269*height;
		polygon.addPoint(x,y); //bottom point
		polygon.addPoint((int) (x-halfWidth),y-height); // upper left
		polygon.addPoint((int) (x+halfWidth),y-height); // upper right

		return polygon;
	}
}
