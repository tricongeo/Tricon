package com.tricongeophysics;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

//import com.Sts.Types.PreStack.StsDataLineSetFace;
//import com.Sts.Types.PreStack.StsGatherData;
//import com.Sts.Types.StsSEGYFormat;
//import com.Sts.Utilities.StsMath;

//public class DataSet  implements Comparable<DataSet>, StsDataLineSetFace
public class DataSet implements Comparable<DataSet> {
	static public final String CDPX_HEADER = "CDP-X";
	static public final String CDPY_HEADER = "CDP-Y";
	static public final String CDP_HEADER = "CDP";

	private String name;
	private String projectName;
	private String lineName;
	protected String path; // full path of file including filesystem, directories, and dataset identifier
							// string
	private int sampleRate; // (milliseconds per sample)
	private int samplesPerTrace;
	private int timeZero = 0; // time of first sample in trace (milliseconds) - not yet implemented
	private String dataType;

	public enum SortOrder {
		FFID, SHOT, CDP, OFFSET, REC_STAT, SHT_STAT, CDPLBLS, OTHER
	};

	private SortOrder primaryKey;
	private int pkeyIndex; // array index of primary key value
	private SortOrder secondaryKey; // not yet implemented
	private int maxntr;
	private int numTraces;
	private SeismicTrace trace;
	private CDPGrid cdpGrid;

	public enum LineType {
		INLINE, XLINE
	};

	private LineType lineType = LineType.INLINE; // not yet implemented
	private Gather gather;
//	private StsGatherData stsGatherData;
	private int sortKeyIndex;
	protected String[] alternateSortOrders = {};
	private float maxVal = Gather.INITIALMAX;
	private float minVal = Gather.INITIALMIN;

	final static String INDEX = ".index";
	final static String PDS = ".pds";

	public DataSet(String project, String line, String filename, String filepath) {
		trace = new SeismicTrace();
		cdpGrid = new CDPGrid();
		// pdsio = new ParadigmDataSetIO(this);
		projectName = project;
		lineName = line;
		name = filename;
		gather = new Gather(this);
//		stsGatherData = new StsGatherData();		
		path = filepath;
	}

	public String findSortOrder(SortOrder order) {
		for (int i = 0; i < alternateSortOrders.length; i++) {
			if (alternateSortOrders[i].contains(order.toString())) {
				return alternateSortOrders[i];
			}
		}
		return null;
	}

	// TODO fix so works without pdsio
	public boolean setSortOrder(SortOrder order) {
//		loadAlternateSortOrders();
		String matchingOrder = findSortOrder(order);
		if (matchingOrder == null) {
			return false;
		}
		// pdsio.setSortOrder(matchingOrder);
		return true;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getSamplesPerTrace() {
		return samplesPerTrace;
	}

	public void setSamplesPerTrace(int samplesPerTrace) {
		this.samplesPerTrace = samplesPerTrace;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public SortOrder getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(SortOrder primaryKey) {
		this.primaryKey = primaryKey;
	}

	// alternate version of this method allows string input to more easily
	// communicate with C code
	public void setPrimaryKey(String primaryKey) {
		if (primaryKey.toLowerCase().equals(SortOrder.CDP.toString().toLowerCase())) {
			setPrimaryKey(SortOrder.CDP);
		} else if (primaryKey.toLowerCase().equals(SortOrder.FFID.toString().toLowerCase())) {
			setPrimaryKey(SortOrder.FFID);
		} else if (primaryKey.toLowerCase().equals(SortOrder.SHOT.toString().toLowerCase())) {
			setPrimaryKey(SortOrder.SHOT);
		} else if (primaryKey.toLowerCase().equals(SortOrder.CDPLBLS.toString().toLowerCase())) {
			setPrimaryKey(SortOrder.CDPLBLS);
		} else {
			System.out.println("Warning: unsupported primary key encountered:" + primaryKey);
			setPrimaryKey(SortOrder.OTHER);
		}
	}

	public SortOrder getSecondaryKey() {
		return secondaryKey;
	}

	public void setSecondaryKey(SortOrder secondaryKey) {
		this.secondaryKey = secondaryKey;
	}

	public int getMaxntr() {
		return maxntr;
	}

	public void setMaxntr(int maxntr) {
		this.maxntr = maxntr;
	}

	public CDPGrid getCdpGrid() {
		return cdpGrid;
	}

	public void setCdpGrid(CDPGrid cdpGrid) {
		this.cdpGrid = cdpGrid;
	}

	public LineType getLineType() {
		return lineType;
	}

	public void setLineType(LineType lineType) {
		this.lineType = lineType;
	}

	public SeismicTrace getTrace() {
		return trace;
	}

	public void setTrace(SeismicTrace trace) {
		this.trace = trace;
		gather.addTrace(trace);
	}

	public void clearGather() {
		gather.clear();
	}

	public String toString() {
		if (filenameOK()) {
			return name;
		} else {
			return name + " * missing *";
		}
	}

	//TODO actually make this work
	private boolean filenameOK() {
		// TODO Auto-generated method stub
		return true;
	}

	public int getNumTraces() {
		return numTraces;
	}

	public void setNumTraces(int numTraces) {
		this.numTraces = numTraces;
	}

	public int getTimeZero() {
		return timeZero;
	}

	public void setTimeZero(int timeZero) {
		this.timeZero = timeZero;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getPkeyIndex() {
		return pkeyIndex;
	}

	public void setPkeyIndex(int pkeyIndex) {
		this.pkeyIndex = pkeyIndex;
	}

	// return names of headers (after converting Focus Header names to STS
	// equivalents)
	public String[] getAttributeNames() {
		boolean hasSOffset = false; // flag for whether focus header contains a signed offset
		String[] names = getTrace().getHeaderList();
//		for (int i=0;i<names.length;i++){
//			names[i] = names[i].trim(); //names come from FOCUS as always 8 chars (can be blank)
//			if (names[i].equals(ParadigmDataSetIO.OFFSET_HEADER)){ hasSOffset = true;}
//		}
//		for (int i=0;i<names.length;i++){
//			names[i] = names[i].trim(); //names come from FOCUS as always 8 chars (can be blank)
//			if (names[i].equals(ParadigmDataSetIO.CDPX_HEADER))  { names[i] = StsSEGYFormat.CDP_X;}
//			if (names[i].equals(ParadigmDataSetIO.CDPY_HEADER))  { names[i] = StsSEGYFormat.CDP_Y;}
//			if (names[i].equals(ParadigmDataSetIO.RECX_HEADER))  { names[i] = StsSEGYFormat.REC_X;}
//			if (names[i].equals(ParadigmDataSetIO.RECY_HEADER))  { names[i] = StsSEGYFormat.REC_Y;}
//			if (names[i].equals(ParadigmDataSetIO.SHOTX_HEADER)) { names[i] = StsSEGYFormat.SHT_X;}
//			if (names[i].equals(ParadigmDataSetIO.SHOTY_HEADER)) { names[i] = StsSEGYFormat.SHT_Y;}
//			if (names[i].equals(ParadigmDataSetIO.ABSOFFSET_HEADER) && hasSOffset){ 
//				names[i] = "ABSOFFSET";} //change name of absolute offset so S2S doesn't use it, will be using signed offset instead
//			if (names[i].equals(ParadigmDataSetIO.OFFSET_HEADER)){ names[i] = StsSEGYFormat.OFFSET;}
//			if (names[i].equals(ParadigmDataSetIO.INLINE_HEADER)){ names[i] = StsSEGYFormat.ILINE_NO;}
//			if (names[i].equals(ParadigmDataSetIO.XLINE_HEADER)) { names[i] = StsSEGYFormat.XLINE_NO;}
//		}
		return names;
	}

//	public byte getDepthUnits() {
//		// TODO Auto-generated method stub
//		return StsDataLineSetFace.DIST_FEET;
//	}

	public String getDescription() {
		String string;
		string = "name.........: " + name + "\n"
//		        +"pds file.....: "+pdsFile.getAbsolutePath()+"\n"
				+ "pkey           =" + primaryKey + "\n";
		loadAlternateSortOrders();
		if (alternateSortOrders.length > 0) {
			string += "sort orders: ";
			for (int i = 0; i < alternateSortOrders.length; i++) {
				string += alternateSortOrders[i] + ", ";
			}
			string += "\n";
		}
		return string;
	}

	private void loadAlternateSortOrders() {
		// TODO Auto-generated method stub

	}

	public String getDescriptionLong() {
		String string;
		string = "name           =" + name + "\n"
//		        +"pds file       ="+pdsFile.getAbsolutePath()+"\n"
				+ "path           =" + path + "\n" + "samplerate     =" + sampleRate + " microsec\n"
				+ "samplesPerTrace=" + samplesPerTrace + "\n" + "dataType       =" + dataType + "\n"
				+ "maxntr         =" + maxntr + "\n" + "numTraces      =" + numTraces + "\n" + "pkey           ="
				+ primaryKey + "\n" + "pkeyindex      =" + pkeyIndex + "\n";
		return string;
	}

	// TODO fix so works without pdsio
	public Iterator getGatherIterator() {
		// return pdsio;
		return null;
	}

//	public byte getHorizontalUnits() {
//		if (this.cdpGrid.getXUnits().equals(CDPGrid.Feet)) {
//			return StsDataLineSetFace.DIST_FEET;
//		}
//		return StsDataLineSetFace.DIST_METER;
//	}

	public int getNSamples(int row) {
		return getSamplesPerTrace();
	}

	// total traces for dataset
	public int getNTraces() {
		// return this.getCdpGrid().getNumInlines()*this.getCdpGrid().getNumXlines();
		// //rough estimate
		return this.getNumTraces(); // accurate now!! (gets directly from Focus)
	}

	public String getStemname() {
		return getName();
	}

//	public byte getTimeUnits() {
//		if (this.getCdpGrid().getZUnits().equals(CDPGrid.Microsecond)){
//			return StsDataLineSetFace.TIME_USECOND;
//		}
//		return StsDataLineSetFace.TIME_MSECOND;
//	}

	/** for sorting */
	public int compareTo(DataSet ds) {
		return name.toUpperCase().compareTo(ds.toString().toUpperCase());
	}

	public float getAngle() {
		return (float) getCdpGrid().getAngle();
	}

	/**
	 * get maximum data value
	 * 
	 * returns maximum value of current gather and previous gather (typically the
	 * first gather of an inline)
	 */
	public float getDataMax() {
		maxVal = Math.max(maxVal, gather.getMaxVal());
		return maxVal; // max sample amplitude
	}

	/**
	 * get minimum data value
	 * 
	 * returns minimum value of current gather and previous gather (typically the
	 * first gather of an inline)
	 */
	public float getDataMin() {
		minVal = Math.min(minVal, gather.getMinVal());
		return minVal; // min sample amplitude
	}

	public boolean getIsNMOed() {
		return false; // don't know how to get this from Paradigm just yet
	}

	public boolean getIsXLineCCW() {
		if (getCdpGrid().getInLineInterval() > 0) {
			return true;
		}
		return false;
	}

	public int getNAttributes() {
		return getTrace().getHeaderList().length;
	}

	public int getNSlices() {
		return this.getSamplesPerTrace(); // number of samples per trace
	}

	public float getXInc() {
		return (float) getCdpGrid().getXLineInterval();
	}

	public float getYInc() {
		return Math.abs((float) getCdpGrid().getInLineInterval()); // can be negative in Focus
	}

//	public byte getZDomain() {
//		if (getCdpGrid().getDataType().equals(CDPGrid.Time) || getCdpGrid().getDataType().equals(CDPGrid.TimeMigrated)) {
//			return StsDataLineSetFace.TIME;
//		}
//		return StsDataLineSetFace.DEPTH;
//	}

	/**
	 * loads first trace of Focus Dataset to get vital information (e.g. sort order)
	 * about dataset before continuing
	 */
	// TODO fix so works without pdsio
	public void initializeDataSet() {
		if (filenameOK()) {
			// pdsio.initializeDataSet();
//			setPDSFile();
		}
	}

	public String getProjectName() {
		return projectName;
	}

	public String getLineName() {
		return lineName;
	}

	/*
	 * public ParadigmDataSetIO getPdsio() { return pdsio; }
	 */

	public Gather getGather() {
		return gather;
	}

//	public StsGatherData getStsGatherData() {
//		gather.sortTraces(ParadigmDataSetIO.OFFSET_HEADER);  //STS wants traces sorted by signed offset
//		//gather.sortTraces("OFFSET"); //try sorting by absolute offset, didn't fix problems
//		if (gather.zeroCDPXY()) {
//			gather.fixCDPXY(cdpGrid);
//		}
//		stsGatherData.setNTraces(gather.getTraces().size());
//		stsGatherData.setTraceData(gather.getTracesFloat());
//		stsGatherData.setTraceOrderedAttributes(gather.getHeadersDouble());
//		stsGatherData.setNAttributes(gather.getTraces().get(0).getHeaderList().length);
//		return stsGatherData;
//	}

	public void setGather(Gather gather) {
		this.gather = gather;
	}

//	public void setStsGatherData(StsGatherData stsGatherData) {
//		this.stsGatherData = stsGatherData;
//	}

	// returns sample rate in milliseconds
	public float getZInc() {
//		if (this.getZDomain() == StsDataLineSetFace.TIME) {
//			if (this.getTimeUnits() == StsDataLineSetFace.TIME_USECOND){
//				return this.getSampleRate()/1000.0f;
//			}
//			if (this.getTimeUnits() == StsDataLineSetFace.TIME_MSECOND){
//				return this.getSampleRate();
//			}
//			if (this.getTimeUnits() == StsDataLineSetFace.TIME_SECOND){
//				return this.getSampleRate()*1000.0f;
//			}
//		}
//		if (this.getZDomain() == StsDataLineSetFace.DEPTH) {
//			return this.getSampleRate(); //??? don't really know what to do here!!
//		}
//		System.err.println("Data Set: getZinc(): returning 0 Z increment because ZDomain not set!!!");
//		return 0;
		return this.getSampleRate();
	}

	// nothing magical here.... Zmax = Zmin + number of samples X sample increment
	public float getZMax() {
		return this.getZInc() * this.getNSlices() + this.getZMin();
	}

	// minimum Z value... for right now just assuming it's 0 in Depth or Time
	// can't find this information in Focus headers
	public float getZMin() {
		return 0;
	}

	/**
	 * gets cross-line increment from CDP Grid - if CDP grid doesn't exist, just
	 * sets it to 1
	 */
	public float getColNumInc() {
		int inc = getCdpGrid().getXLineIncrement();
		if (inc < 1) { // make sure we don't get bogus increments just because it wasn't saved as 3D!!
			inc = 1;
		}
		return inc;
	}

	/**
	 * gets in-line increment from CDP Grid - if CDP grid doesn't exist, just sets
	 * it to 1
	 */
	public float getRowNumInc() {
		int inc = getCdpGrid().getInLineIncrement();
		if (inc < 1) { // make sure we don't get bogus increments just because it wasn't saved as 3D!!
			inc = 1;
		}
		return inc;
	}

	public int getSortKeyIndex() {
		return sortKeyIndex;
	}

	public void setSortKeyIndex(int sortKeyIndex) {
		this.sortKeyIndex = sortKeyIndex;
	}

	public boolean is3d() {
		return cdpGrid.is3d();
	}

}
