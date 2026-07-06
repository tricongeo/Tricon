package com.tricongeophysics;

import java.util.ArrayList;

public class FocusGeometryLoader {

	private static final int HalfGig = (int) (0.5*1024*1024*1024);
	private String project;
	private String line;
	private FocusDbIO focusDbIO;
	private TriconGeometry tg;
	private String notes;
	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
	private boolean cancel = false;
	private Stopwatch shotTimer = new Stopwatch("Load shots from Focus");
	private Stopwatch recTimer = new Stopwatch("Load receivers from Focus");
	private OBRecord[] relationObs;
	private String pgSurveyRoot;
	
	FocusGeometryLoader(String projectName, String lineName, String pgSurveyRoot) {
		focusDbIO = new FocusDbIO();
		focusDbIO.initIDs();
		project = projectName.toUpperCase().trim();
		line = lineName.toUpperCase().trim();
		this.pgSurveyRoot = pgSurveyRoot;
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FocusGeometryLoader fgl = new FocusGeometryLoader("SCOTT3D", "PAULS3D", "/seisdata");
		//		fgl.project = "SCOTT3D";
		//		fgl.line = "PAULS3D";
		try {
			fgl.loadProject();
			fgl.loadLine();
		} catch (FocusDbIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		TriconGeometry tg2 = new TriconGeometry(fgl.project, fgl.line);
//		fgl.focusDbIO.loadAttrNames(tg2, fgl.line);
//		//FocusDbAttr atr = tg2.getShotModel().getAttr("SHOT", "ELEV");
//		FocusDbAttr atr = tg2.getShotModel().getAttr("CHAN", "LINE");
//		fgl.focusDbIO.loadAttr(atr);
		System.out.println("java done\n");
	}

	void loadLine() throws FocusDbIOException {
		tg = new TriconGeometry(project, line);
		focusDbIO.loadAttrNames(tg, line);
		loadShotModel();
		loadStationModel();
		try {
			loadBias();
		} catch (Exception e) {
//			e.printStackTrace();
		}
		try {
			loadCdpModel(); //don't crash if cdp model not found
		} catch (Exception e) {
//			e.printStackTrace();
		}
		try {
			notes = focusDbIO.readLongTextFile(line, "GEOMETRY NOTES"); //don't crash if notes not found
		} catch (Exception e) {
//			e.printStackTrace();
		}
		System.out.println("done loading line: " + line);
	}
	
	private void loadShotModel() throws FocusDbIOException {
		shotTimer.start();
		ArrayList<FocusDbAttr> attrs = tg.getShotModel().getAttributes();
		fireProgressChanged(new ProgressMaxChanged(this, "Loading Shot Data", attrs.size()));
		for (int i=0; i< attrs.size(); i++) {
			if (cancel) return;
			FocusDbAttr atr = attrs.get(i);
			if (atr.getEvent().contains("FBNET")) continue;
			if (atr.getEvent().equals("CHAN")) {
				if (atr.getAttribute().equals("LINE")) {
					focusDbIO.loadAttr(atr);
					float[] vals = atr.getVals();
					if (vals.length > HalfGig/4) { //each float takes up 4 bytes
						CompressedAttribute cAtr = new CompressedAttribute(atr);
						attrs.set(i, cAtr);
					}
					continue;
				}
			}
			focusDbIO.loadAttr(atr);
			fireProgressChanged(new ProgressEvent(this, "Loading "+atr, i));
		}
		shotTimer.stop();
	}

//	private void processRelationAttributeStation(FocusDbAttr atr) {
//		float[] vals = atr.getVals();
//		int vperloc = atr.getValsPerLoc();
//		int nShots = vals.length/vperloc;
//		if(relationObs == null) relationObs = new OBRecord[nShots];
//		for (int i=0; i<nShots; i++) {
//			if(relationObs[i] == null) relationObs[i] = new OBRecord();
//			setRelationStation(relationObs[i], i, atr);
//		}
//	}

//	private void setRelationStation(OBRecord ob, int shotID, FocusDbAttr stationAttr) {
//		int nlocs = stationAttr.getValsPerLoc();
//		if (nlocs < 2) {
//			System.err.println("ShotDbModel.setRelation() - Relation attributes not multivalued!");
//			return;
//		}
//		int oldStation = (int) stationAttr.getMultiVal(shotID, 0)-1;
//		if (oldStation != 0) {
//			ob.setFromReceiver(0, oldStation+1);
//		}
//		for (int r=0; r<nlocs; r++) {
//			int station = (int) stationAttr.getMultiVal(shotID, r);
//			if (Math.abs(station - oldStation) != 1) {
//				if (oldStation != 0) {
//					ob.getToReceiver().add(oldStation);
//				}
//				if (station != 0) {
//					ob.getFromReceiver().add(station);
//				}
//			}
//			oldStation = station;
//		}
//		if (oldStation != 0) {
//			ob.getToReceiver().add(oldStation);
//		}
//		ArrayList<Integer> tr = ob.getToReceiver();
//		ArrayList<Integer> tc = ob.getToChan();
//		ArrayList<Integer> fr = ob.getFromReceiver();
//		ArrayList<Integer> fc = ob.getFromChan();
//		int total = tr.size() + tc.size() + fr.size() + fc.size();
//		if (total != 4*tr.size()) {
//			System.err.println("ShotDbModel.setRelation() - from/to chan/receiver array sizes don't match!!");
//		}
//	}

	/**
	 * replace multival relation array with sparse array only containing real
	 * values. this will help us use less memory
	 * @param atr
	 */
//	private void processRelationAttributeLine(FocusDbAttr atr) {
//		float[] vals = atr.getVals();
//		int vperloc = atr.getValsPerLoc();
//		int nShots = vals.length/vperloc;
//		if(relationObs == null) relationObs = new OBRecord[nShots];
//		for (int i=0; i<nShots; i++) {
//			if(relationObs[i] == null) relationObs[i] = new OBRecord();
//			setRelationLine(relationObs[i], i, atr);
//		}
//	}

//	private void setRelationLine(OBRecord ob, int shotID, FocusDbAttr lineAttr) {
//		int nlocs = lineAttr.getValsPerLoc();
//		int oldLine = (int) lineAttr.getMultiVal(shotID, 0);
//		if (oldLine != 0) {
//			ob.setFromChan(0, 1);
//			ob.setReceiverLineNumber(0, oldLine);
//		}
//		int chan = 1;
//		for (int r=0; r<nlocs; r++) {
//			chan = r + 1;
//			int line = (int) lineAttr.getMultiVal(shotID, r);
//			if (line != oldLine) {
//				if (oldLine != 0) {
//					ob.getToChan().add(chan - 1);
//				}
//				if (line != 0) {
//					ob.getFromChan().add(chan);
//					ob.getReceiverLineNumber().add(line);
//				}
//			}
//			oldLine = line;
//		}
//		if (oldLine != 0) {
//			ob.getToChan().add(chan);
//		}
//	}

	private void loadStationModel() throws FocusDbIOException {
		recTimer.start();
		ArrayList<FocusDbAttr> attrs = tg.getRcvrModel().getAttributes();
		fireProgressChanged(new ProgressMaxChanged(this, "Loading Receiver Data", attrs.size()));
		//for (FocusDbAttr attr: attrs) {
		for (int i=0; i< attrs.size(); i++) {
			//focusDbIO.loadAttr(attr);
			if (cancel) return;
			FocusDbAttr atr = attrs.get(i);
			focusDbIO.loadAttr(atr);
			fireProgressChanged(new ProgressEvent(this, "Loading "+atr, i));
		}
		recTimer.stop();
	}

	private void loadCdpModel() throws FocusDbIOException {
		if (cancel) return;
		String geomFile = focusDbIO.readTextFile(line, FocusCDPModel.GeometryFile);
//		System.out.println(geomFile);
		FocusCDPModel cdpModel = tg.getCdpModel();
		cdpModel.loadGeometryFile(geomFile);
		String regularCdp = focusDbIO.readTextFile(line, FocusCDPModel.RegularCdp);
		cdpModel.loadRegularCdp(regularCdp);
	}

	void loadProject() throws FocusDbIOException {
		focusDbIO.initializeProject(project, pgSurveyRoot);
	}

	public TriconGeometry getGeometry() {
		return tg;
	}

	public boolean hasReceivers() {
		if (tg == null) return false;
		if (tg.rcvrModel == null) return false;
		if (tg.rcvrModel.attributes == null) return false;
		if (tg.rcvrModel.attributes.size() < 1) return false;
		return true;
	}

	public boolean hasShots() {
		if (tg == null) return false;
		if (tg.shotModel == null) return false;
		if (tg.shotModel.attributes == null) return false;
		if (tg.shotModel.attributes.size() < 1) return false;
		return true;
	}

	public boolean is3D() {
		if (tg == null) return false;
		if (tg.shotModel != null) {
			FocusDbAttr l = tg.shotModel.getAttr("SHOT", "LINE");
			if (l != null) return true;
		}
		if (tg.rcvrModel != null) {
			FocusDbAttr l = tg.rcvrModel.getAttr("RECEIVER", "LINE");
			if (l != null) return true;
		}
		return false;
	}

	public String getNotes() {
		return notes;
	}

	/**
	 * Get Bias from Focus. 
	 * Geometry.BIAS.COORD
	 * two integers for x and y bias.
	 * This is the number that needs to be added to raw x and y values in Focus database.
	 * Used for 8+ digit x/y coordinates (stored as floats which only has 7 digit accuracy).
	 * 
	 * @throws FocusDbIOException
	 */
	public void loadBias() throws FocusDbIOException {
		if (cancel) return;
		int[] bias = focusDbIO.readBinaryFile("GEOMETRY", FocusBias.Bias, FocusBias.Coord);
		
		FocusBias focusBias = tg.getBias();
		focusBias.loadBias(bias);
	}

	public void addProgressListener(ProgressListener l) {
		progressListeners  .add(l);
	}

	private void fireProgressChanged(ProgressEvent progressEvent) {
		for (ProgressListener l: progressListeners) l.progressChanged(progressEvent);
	}

	public void cancelJob() {
		cancel  = true;
	}

	public void printTimes() {
		shotTimer.printTime();
		recTimer.printTime();
	}

	public OBRecord[] getRelationObs() {
		return relationObs;
	}

}
