package com.tricongeophysics;

import java.util.ArrayList;

public class CompressedAttribute extends FocusDbAttr{

	private FocusDbAttr attr;
	private ArrayList smallVals;
	private ArrayList indices;
	private int lastIndex = 0;

	public CompressedAttribute(FocusDbAttr atr) {
		super(atr.getEvent(), atr.getAttribute(), atr.getModel());
		this.attr = atr;
		size = atr.getSize();
		valsPerLoc = atr.getValsPerLoc();
		initialize();
	}

	private void initialize() {
		float[] vals = attr.getVals();
		smallVals = new ArrayList();
		indices = new ArrayList();
		float oldVal = vals[0]-1;
		float v = 0;
		for (int i=0; i<vals.length; i++) {
			v = vals[i];
			float diff = Math.abs(v - oldVal);
			if (diff > 0) {
				smallVals.add(v);
				indices.add(i);
			}
			oldVal = v;
		}
		int finalIndex = vals.length - 1;
		if ((Integer)indices.get(indices.size()-1) != finalIndex) {
			indices.add(finalIndex);
			smallVals.add(v);
		}
		attr.delete();
		System.gc();
	}
	
	@Override
	public void delete() {
		super.delete();
		indices = null;
		smallVals = null;
	}
	
	@Override
	public float getVal(int index) {
		for (int i=lastIndex; i<indices.size(); i++) {
			int j = (Integer) indices.get(i);
			if (j > index) {
				if (i > 0) {
					lastIndex = i-1;
					return (Float)smallVals.get(i-1);
				}
				else {
					System.err.println("CompressedAttribute:getVal() - trying to access value before beginning of array!");
					return -1;
				}
			}
			if (j == index) {
				return (Float)smallVals.get(i);
			}
		}
		for (int i=0; i<=lastIndex; i++) {
			int j = (Integer) indices.get(i);
			if (j > index) {
				if (i > 0) {
					lastIndex = i-1;
					return (Float)smallVals.get(i-1);
				}
				else {
					System.err.println("CompressedAttribute:getVal() - trying to access value before beginning of array!");
					return -1;
				}
			}
			if (j == index) {
				return (Float)smallVals.get(i);
			}
		}
		System.err.println("CompressedAttribute:getVal() - index: " + index + " not found!");
		return -1;
	}
	
	@Override
	public float[] getVals() {
		System.err.println("CompressedAttribute:getVals() - not supported!");
		return null;
	}
	
	@Override
	public void setVals(float[] vals) {
		System.err.println("CompressedAttribute:setVals() - not supported!");
	}
	
	@Override
	public float getMultiVal(int id, int loc) {
		return getVal(id*valsPerLoc + loc);
	}

}
