package com.tricongeophysics;

public class RcvrDbModel extends FocusDbModel {

	public RcvrDbModel(TriconGeometry tg) {
		super("STATION", tg);
//		attributes = new FocusDbAttr[attrlist.length];
//		for (int i=0; i< attributes.length; i++) {
//			String[] split = attrlist[i].split(":");
//			String event = split[0];
//			String attr = split[1];
//			attributes[i] = new FocusDbAttr(event, attr, this);
//		}
		attrlist = new String[] {
				"RECEIVER:LINE",
				"RECEIVER:STATION",
				"X:COORD",
				"Y:COORD",
				"SURFACE:ELEV"	};
	}

	public FocusDbAttr getX() {
		return this.getAttr("X", "COORD");
	}

	public FocusDbAttr getY() {
		return this.getAttr("Y", "COORD");
	}

	public FocusDbAttr getLine() {
		return this.getAttr("RECEIVER", "LINE");
	}

	public FocusDbAttr getStation() {
		return this.getAttr("RECEIVER", "STATION");
	}

	public FocusDbAttr getZ() {
		return this.getAttr("SURFACE", "ELEV");
	}
}
