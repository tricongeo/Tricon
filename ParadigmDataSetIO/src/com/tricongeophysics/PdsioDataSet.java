package com.tricongeophysics;

import java.util.Iterator;

import com.tricongeophysics.DataSet.SortOrder;

public class PdsioDataSet extends DataSet {
	
	private ParadigmDataSetIO pdsio;

	public PdsioDataSet(String project, String line, String filename, String pdsPath, String psr, String psd){
		super(project, line, filename, pdsPath, psr, psd);
		this.pdsio = new ParadigmDataSetIO(this);
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
	public boolean setSortOrder (SortOrder order) {
		loadAlternateSortOrders();
		String matchingOrder = findSortOrder(order);
		if (matchingOrder == null) {
			return false;
		}
		pdsio.setSortOrder(matchingOrder);
		return true;
	}
}
