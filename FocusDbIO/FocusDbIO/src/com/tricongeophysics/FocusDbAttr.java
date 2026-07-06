package com.tricongeophysics;

import java.util.ArrayList;

public class FocusDbAttr {

	private String event;
	private String attribute;
	private float[] vals;
	private FocusDbModel dbModel;
	protected int size;
	protected int valsPerLoc;

    private static final int MaxString = 8;

	public FocusDbAttr(String event, String attribute, FocusDbModel dbModel) {
//		this.event = event.toUpperCase();
//		this.attribute = attribute.toUpperCase();
		this.event = clean(event);
		this.attribute = clean(attribute);
		this.dbModel = dbModel;
	}

	public float[] getVals() {
		return vals;
	}

	static String clean(String string)
	{
		int newLength = Math.min(MaxString, string.length());
		return string.toUpperCase().substring(0, newLength).trim();
	}

	public void setVals(float[] vals) {
		this.vals = vals;
		valsPerLoc = vals.length/size;
		if (valsPerLoc > 1) {
			System.out.println("multival Attribute: " + this + " valsPerLoc: " + valsPerLoc);
		}
		if (valsPerLoc * size != vals.length) {
			System.err.println("Incorrect number of values!!: " + this);
		}
	}

	public String getProjectName() {
		return dbModel.getProjectName();
	}
	
	public String getLineName() {
		return dbModel.getLineName();
	}

	public String getModelName() {
		return dbModel.getName();
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	public String toString() {
		String v;
		if (vals != null && vals.length > 0) v = vals[0] + "";
		else v = "none";
		return "Model: " + dbModel.getName() + " Event: " + event + " Attribute: " + attribute + " v[0]=" + v;
	}

	public int getSize() {
//		if (vals == null) return 0;
//		return vals.length;
		return size;
	}

	/**
	 * Sets size of attribute (number of locations).
	 * Call this before setVals().
	 * If size is less than vals.length, it's assumed this is 2d array
	 * (more than one value per location).
	 * @param size
	 */
	public void setSize(int size) {
		this.size = size;
		dbModel.setNlocs(size);
	}

	public float getVal(int i) {
		if (vals == null || i >= vals.length) return -1;
		return vals[i*valsPerLoc];
	}
	
	public float getMultiVal(int id, int loc) {
		if (vals == null || id >= vals.length) return -1;
		return vals[id*valsPerLoc + loc];
	}

	public int getValsPerLoc() {
		return valsPerLoc;
	}
	
	/**
	 * Sets firstID. Focus requires sequential ID counters in a model, but it doesn't have to start with 1.
	 * Focus also lets the ID increment by more than 1, as long as it's consistent, but we're assuming
	 * increment by 1.
	 * @param size
	 */
	public void setFirstID(int firstID) {
		dbModel.setFirstID(firstID);
	}
	
	public int getFirstID() {
		return dbModel.getFirstID();
	}

	public void delete() {
		vals = null;
		size = 0;
		valsPerLoc = 0;
	}

	public FocusDbModel getModel() {
		return dbModel;
	}
	
}
