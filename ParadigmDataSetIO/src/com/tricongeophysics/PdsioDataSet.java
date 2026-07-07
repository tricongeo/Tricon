package com.tricongeophysics;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.tricongeophysics.DataSet.SortOrder;

public class PdsioDataSet extends DataSet {

	private ParadigmDataSetIO pdsio;
	private String pgSurveyRoot; // directory that FOCUS projects are found in
	private String pgSurveyDir; // directory that GeoDepth projects are found in
	private File pdsFile;

	public PdsioDataSet(String project, String line, String filename, String pdsPath, String psr, String psd) {
		super(project, line, filename, pdsPath);
		this.pdsio = new ParadigmDataSetIO(this);
		pgSurveyRoot = psr;
		pgSurveyDir = psd;
	}

	public Iterator getGatherIterator() {
		return pdsio;
	}

	public void initializeDataSet() {
		if (pdsFileOK()) {
			pdsio.initializeDataSet();
			setPDSFile();
		}
	}

	public ParadigmDataSetIO getPdsio() {
		return pdsio;
	}

	public boolean setSortOrder(SortOrder order) {
		loadAlternateSortOrders();
		String matchingOrder = findSortOrder(order);
		if (matchingOrder == null) {
			return false;
		}
		pdsio.setSortOrder(matchingOrder);
		return true;
	}

	public File getPdsFile() {
		return pdsFile;
	}

	public void loadAlternateSortOrders() {
		String SEP = ".";
		ArrayList<String> orders = new ArrayList<String>();
		if (!pdsFileOK()) {
			return;
		}
		File dir = new File(pdsFile.getParent());
		String[] contents = dir.list();
		for (int i = 0; i < contents.length; i++) {
			String[] parts = contents[i].split("\\" + SEP);
			int nParts = parts.length;
			if (nParts > 2) {
				String ext = SEP + parts[parts.length - 1];
				String baseName = parts[0] + SEP + parts[1] + SEP + parts[2]; // first 3 segments of filename are
																				// basename
				String order = contents[i].replaceFirst(baseName, "").replaceFirst(ext, "").replaceFirst(SEP, ""); // order
																													// is
																													// rest
																													// of
																													// file
																													// name
				if (pdsFile.getName().contains(baseName) && ext.equals(INDEX)) { // test if this is index file for this
																					// dataset
					orders.add(order);
				}
			}
		}
		alternateSortOrders = orders.toArray(new String[0]);
	}

	public String[] getAlternateSortOrders() {
		loadAlternateSortOrders();
		return alternateSortOrders;
	}

	public String getPgSurveyRoot() {
		return pgSurveyRoot;
	}

	public String getPgSurveyDir() {
		return pgSurveyDir;
	}

	public boolean setPDSFile() {
		pdsFile = new File(path + PDS);
		return pdsFile.isFile();
	}

	// checks to see if pds file can be found
	public boolean pdsFileOK() {
		return setPDSFile();
	}

}
