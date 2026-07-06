package com.tricongeophysics;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class FocusGeometrySaver {

	private String project;
	private String line;
	private FocusDbIO focusDbIO;
	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
	private String pgSurveyRoot;
	
	FocusGeometrySaver(String projectName, String lineName, String pgSurveyRoot) {
		project = projectName.toUpperCase();
		line = lineName.toUpperCase();
		this.pgSurveyRoot = pgSurveyRoot;
		focusDbIO = new FocusDbIO();
		focusDbIO.initIDs();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FocusGeometrySaver fgs = new FocusGeometrySaver("SCOTT3D", "test", "/seisdata");
		String event = "TEST";
		String attr = "THING";
		TriconGeometry tg2 = new TriconGeometry(fgs.project, fgs.line);
		tg2.shotModel.addAttribute(event, attr);
		FocusDbAttr t = tg2.shotModel.getAttr(event, attr);
//		float[] vals = new float[] { 1, 2, 3, 4, 5, 6};
		float[] vals = new float[] { 11, 22, 33, 44};
//		t.setSize(vals.length/2);
		t.setSize(vals.length);
		t.setVals(vals);
		try {
		//fgs.saveShots(tg2);
//		fgs.focusDbIO.deleteModel(tg2.shotModel);
//		fgs.focusDbIO.getModel(tg2.shotModel);
		tg2.shotModel.setFirstID(3);
//		fgs.focusDbIO.createModel(tg2.shotModel);
		fgs.focusDbIO.checkExtendModel(tg2.shotModel);
		fgs.focusDbIO.saveAttribute(t);
//		fgs.focusDbIO.initializeProject("SCOTT3D");
//		fgs.focusDbIO.loadAttrNames(tg2, fgs.line);
//		//FocusDbAttr atr = tg2.getShotModel().getAttr("SHOT", "ELEV");
		
//		fgs.focusDbIO.loadAttr(t);
//		FocusDbAttr atr = tg2.getShotModel().getAttr(event, attr);
//		float[] v = t.getVals();
//		for (float f: v) System.out.print(f + ", ");
//		fgl.focusDbIO.loadAttr(atr);
		
//		FocusGeometryLoader fgl = new FocusGeometryLoader("SCOTT3D", "Pauls3d");
//		fgl.loadProject();
//		fgl.loadLine();
//		TriconGeometry g = fgl.getGeometry();
//		g.line = "TEST";
//		ShotDbModel m = g.shotModel;
//		fgs.saveModel(g.shotModel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("java done\n");
	}

	/**
	 * Save FocusDbModel to Focus database by calling FocusDbIO.saveAttribute on all
	 * attributes in model.<br>
	 * <br>
	 * Safety features include:<br>
	 * 1) Check if model exists. If so, ask user if he wants to overwrite. <br>
	 * 2) If model doesn't exist, create before writing.<br>
	 * 3) If model is too small, extend before writing.<br>
	 * 4) If problems are encountered, a FocusDbIOException is thrown.<br>
	 * <br>
	 * @param model
	 * @param parent
	 * @return successful completion flag (boolean).
	 * @throws FocusDbIOException
	 */
	public boolean saveModel(FocusDbModel model, Component parent) throws FocusDbIOException {
		if (model == null) return false;
		FocusDbModel oldModel = new FocusDbModel(model.getName(), model.getTriconGeometry());
		focusDbIO.getModel(oldModel);
		int oldSize = oldModel.getNlocs();
		if (oldSize > 0) {
			System.out.print("Model exists! " + model);
			int answer = JOptionPane.showConfirmDialog(parent, model + "\nModel Exists!\n Overwrite model?");
			if (answer != JOptionPane.OK_OPTION) return false;
			focusDbIO.checkExtendModel(model);
		}
		else {
			System.out.print("Model not found! " + model);
			System.out.print("Creating Model");
			focusDbIO.createModel(model);
		}
		ArrayList<FocusDbAttr> atrs = model.getAttributes();
		fireProgressChanged(new ProgressEvent(this, "Saving Model " + model.getName(), 1));
		int count = 0;
		for (FocusDbAttr attr: atrs) {
			String event = attr.getEvent();
			if (event.contains("FBNET")) continue;  //don't save first break picks until we get this figured out.
			focusDbIO.saveAttribute(attr);
			fireProgressChanged(new ProgressEvent(this, null, count++));
		}
		return focusDbIO.normalCompletion();
	}

	private void fireProgressChanged(ProgressEvent progressEvent) {
		for (ProgressListener l: progressListeners) l.progressChanged(progressEvent);
	}

	/**
	 * Initializes project in C code. This is not necessary
	 * (all other methods make sure project is initialized before
	 * continuing). 
	 * @throws FocusDbIOException if project not found, can't find pns server, etc.
	 */
	public void loadProject() throws FocusDbIOException {
		focusDbIO.initializeProject(project, pgSurveyRoot);
	}

	public void addProgressListener(ProgressListener l) {
		progressListeners .add(l);
	}

	/**
	 * Save "text" to file "key" in Focus database.
	 * This is considered a "TEXT" file in the Focus database,
	 * as opposed to a "USER" file. 
	 * @param text
	 * @param key
	 * @throws FocusDbIOException
	 */
	public void saveFile(String text, String key) throws FocusDbIOException {
		focusDbIO.saveTextFile(text, key, line);
	}

	public void saveBinary(String lineName, String klass, String key, int[] data) throws FocusDbIOException {
		focusDbIO.saveBinaryFile(lineName, klass, key, data);
	}

	public void saveBias(FocusBias bias) throws FocusDbIOException {
		saveBinary(FocusBias.Geometry, FocusBias.Bias, FocusBias.Coord, bias.getData()); 
	}

	public void saveMapStatKey(FocusStatKey mapStatkey) throws FocusDbIOException {
		saveBinary(mapStatkey.getLine(), FocusStatKey.Map, FocusStatKey.Statkey, mapStatkey.getData()); 
	}

	public void saveLongFile(String text, String key) throws FocusDbIOException {
		focusDbIO.saveLongTextFile(text, key, line);
	}
}
