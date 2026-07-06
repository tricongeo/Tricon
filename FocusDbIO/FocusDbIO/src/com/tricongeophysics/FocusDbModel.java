package com.tricongeophysics;

import java.util.ArrayList;

public class FocusDbModel {
	ArrayList<FocusDbAttr> attributes;
	private String name;
	private TriconGeometry tg;
	protected String[] attrlist;
	private int firstID = 1;
	private int inc = 1; //increment
//	private int end;
	protected int nlocs;
	private String status;

	public FocusDbModel(String name, TriconGeometry tg) {
		this.name = name;
		this.tg = tg;
		attributes = new ArrayList<FocusDbAttr>();
	}

	public FocusDbAttr getAttr(String event, String attribute) {
		if (event == null || attribute == null) return null;
		event = FocusDbAttr.clean(event);
		attribute = FocusDbAttr.clean(attribute);
		for (FocusDbAttr a: attributes) {
			if (a.getEvent().equals(event) && a.getAttribute().equals(attribute)) {
				return a;
			}
		}
		System.err.println("Didn't find model: " + name + " event: " + event + " attribute:" + attribute);
		return null;
	}
	
	public FocusDbAttr getFirstAttribute() {
		if (attributes.size() < 1 || attributes.get(0) == null) {
			attributes.add(new FocusDbAttr("dummy", "dummy", this));
		}
		return attributes.get(0);
	}

	public String getProjectName() {
		return tg.getProject();
	}

	public String getLineName() {
		return tg.getLine();
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return name + " model";
	}
	
	public ArrayList<FocusDbAttr> getAttributes() {
		return attributes;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public int getSize() {
//		if (attributes == null || attributes.size() < 1) return 0;
//		FocusDbAttr a = attributes.get(0);
//		return a.getSize();
//	}

	public void addAttribute(String event, String attribute) {
		FocusDbAttr attr = new FocusDbAttr(event, attribute, this);
		attributes.add(attr);
	}

	public FocusDbAttr[] getOptionalAttributes() {
		if (attributes.size() < 1) {
			System.err.println("No Attributes Found! Model: " + name);
			return null;
		}
		if (attributes.size() < attrlist.length) {
			System.err.println("No Optional Attributes Found! Model: " + name);
			return null;
		}
		FocusDbAttr[] attrs = new FocusDbAttr[attributes.size() - attrlist.length];
		int count = 0;
		for (int i=0; i< attributes.size(); i++) {
			String event1 = attributes.get(i).getEvent();
			String attr1 = attributes.get(i).getAttribute();
			if (isRequiredAttribute(event1, attr1)) {
				continue;
			}
			if (count >= attrs.length) {
				System.err.println("FocusDbModel.getOptionalAttributes() - Trying to add too many attributes to optional list. Length:" + attrs.length + " count:" + count + " attr:" + attributes.get(i));
			} else {
				attrs[count++] = attributes.get(i);
			}
		}
		return attrs;
	}

	private boolean isRequiredAttribute(String event1, String attr1) {
		for (String attr: attrlist) {
			String[] split = attr.split(":");
			String event = split[0];
			String attrname = split[1];
			if (event.equals(event1) && attrname.equals(attr1)) return true;
		}
		return false;
	}

	public void setFirstID(int firstID) {
		this.firstID = firstID;
	}
	
	public int getFirstID() {
		return firstID;
	}

	public int getInc() {
		return inc;
	}

	public void setInc(int inc) {
		this.inc = inc;
	}

	public int getNlocs() {
		return nlocs;
	}

	public void setNlocs(int nlocs) {
		this.nlocs = nlocs;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getEnd() {
		return firstID + (nlocs - 1)*inc;
	}

//	public void setEnd(int end) {
//		this.end = end;
//	}

	public TriconGeometry getTriconGeometry() {
		return tg;
	}

	public int getColumnCount() {
		if (attributes == null) return 0;
		return attributes.size();
	}
}
