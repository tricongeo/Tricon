package com.tricongeophysics;

import java.util.ArrayList;

public class ShotDbModel extends FocusDbModel {
	
	public ShotDbModel(TriconGeometry tg) {
		super("SHOT", tg);
//		attributes = new FocusDbAttr[attrlist.length];
//		for (int i=0; i< attributes.length; i++) {
//			String[] split = attrlist[i].split(":");
//			String event = split[0];
//			String attr = split[1];
//			attributes[i] = new FocusDbAttr(event, attr, this);
//		}
		attrlist = new String[] {
				"SHOT:FFID",
				"SHOT:LINE",
				"SHOT:STATION",
				"X:COORD",
				"Y:COORD",
				//"SHOT:DEPTH",
				//"UPHOLE:TIME",
				"SHOT:ELEV",
				"CHAN:LINE",
				"CHAN:STATION"};
	}

	public FocusDbAttr getX() {
		return this.getAttr("X", "COORD");
	}

	public FocusDbAttr getY() {
		return this.getAttr("Y", "COORD");
	}

	public FocusDbAttr getLine() {
		return this.getAttr("SHOT", "LINE");
	}

	public FocusDbAttr getStation() {
		return this.getAttr("SHOT", "STATION");
	}

	public FocusDbAttr getZ() {
		return this.getAttr("SHOT", "ELEV");
	}

	/**
	 * takes CHAN:LINE and CHAN:STATION from Focus and converts it to
	 * from/to channel and station pairs (more like SPS style relation).
	 * Focus uses zeroed placeholders for channel gaps in the patterns.
	 * These are ignored (SPS format handles channel gaps as a separate
	 * receiver line).
	 * 
	 * Assumes channel starts at 1 and increases by 1 per CHAN:LINE/
	 * CHAN:STATION.
	 * 
	 * @param ob
	 * @param shotID sequential shotID (starts at zero)
	 */
	public void setRelation(OBRecord ob, int shotID) {
		FocusDbAttr lineAttr = getAttr("CHAN", "LINE");
		FocusDbAttr stationAttr = getAttr("CHAN", "STATION");
		if (lineAttr == null || stationAttr == null) {
			System.err.println("ShotDbModel.setRelation() - Relation attributes not found!");
			return;
		}
		int nlocs = lineAttr.getValsPerLoc();
		if (nlocs < 2) {
			System.err.println("ShotDbModel.setRelation() - Relation attributes not multivalued!");
			return;
		}
		int oldLine = (int) lineAttr.getMultiVal(shotID, 0);
		int oldStation = (int) stationAttr.getMultiVal(shotID, 0)-1;
		if (oldLine != 0) {
			ob.setFromChan(0, 1);
			ob.setFromReceiver(0, oldStation+1);
			ob.setReceiverLineNumber(0, oldLine);
		}
		int chan = 1;
		for (int r=0; r<nlocs; r++) {
			chan = r + 1;
			int line = (int) lineAttr.getMultiVal(shotID, r);
			int station = (int) stationAttr.getMultiVal(shotID, r);
			if (line != oldLine || Math.abs(station - oldStation) != 1) {
//			if (line != oldLine) {
				if (oldStation != 0) {
					ob.getToReceiver().add(oldStation);
					ob.getToChan().add(chan - 1);
				}
				if (station != 0) {
					ob.getFromChan().add(chan);
					ob.getFromReceiver().add(station);
					ob.getReceiverLineNumber().add(line);
				}
			}
			oldLine = line;
			oldStation = station;
		}
		if (oldStation != 0) {
			ob.getToReceiver().add(oldStation);
			ob.getToChan().add(chan);
		}
		ArrayList<Integer> tr = ob.getToReceiver();
		ArrayList<Integer> tc = ob.getToChan();
		ArrayList<Integer> fr = ob.getFromReceiver();
		ArrayList<Integer> fc = ob.getFromChan();
		int total = tr.size() + tc.size() + fr.size() + fc.size();
		if (total != 4*tr.size()) {
			System.err.println("ShotDbModel.setRelation() - from/to chan/receiver array sizes don't match!!");
		}
	}

	public FocusDbAttr getFfid() {
		return this.getAttr(MapperToFocusConverter.SHOT, MapperToFocusConverter.FFID);
	}

	public FocusDbAttr getKill() {
		return this.getAttr("KILL", "KILL");
	}
}
