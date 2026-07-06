package com.tricongeophysics;

import java.io.FileNotFoundException;
import java.util.Iterator;

class ParadigmDataSetIO implements Iterator
{
	private DataSet dataSet;
	private boolean endOfGather=false;
	private boolean endOfDataSet=false;
	private boolean firstGatherLoaded=false;
	private SeismicTrace trace;
	
	static public final String INLINE_HEADER = "CDPLBLS";
    static public final String XLINE_HEADER = "CDPLBLX";
    static public final String OFFSET_HEADER = "SOFFSET";
    static public final String SHOTX_HEADER = "SHT-X";
    static public final String SHOTY_HEADER = "SHT-Y";
    static public final String RECX_HEADER = "REC-X";
    static public final String RECY_HEADER = "REC-Y";
    static public final String CDPX_HEADER = "CDP-X";
    static public final String CDPY_HEADER = "CDP-Y";
	static public final String CDP_HEADER = "CDP";
	static public final String ABSOFFSET_HEADER = "OFFSET";
	private static final String pg_in_pgver = "pg_in_pgver";
    
    private final boolean debug = false;
	
	static {
		String pig = System.getenv(pg_in_pgver);
		System.out.println(pg_in_pgver + " is" + pig);
		if (pig != null && pig == "1") {
			System.out.println("ParadigmDataSetIO library path is: "+ System.getenv("LD_LIBRARY_PATH"));
			System.loadLibrary("ParadigmDataSetIO");
		}
	}

	public ParadigmDataSetIO(DataSet ds){
		dataSet = ds;
		trace = new SeismicTrace();
	}	

	public static void main(String[] args) {
		//DataSet dataSet = new DataSet("pastunit", "TEST", "zwirn_geom_shts", "/apdata/survey/pastunit_gd/Line_AAAA/0000400600e00bc1.000000.00000003","/apdata/seisdata", "/apdata/seisdata/hds");
		DataSet dataSet = new DataSet("2droseta", "300", "signalshots", "/isdata/survey/2droseta_gd/Line_AAAB/0000408500a00bc1.000000.0000000b","/ginge/seisdata", "/ginge/seisdata/hds");
//		pdsio.dataSelector.setFileName("cdps");
//		pdsio.dataSelector.setLineName("TEST");
//		pdsio.dataSelector.setProjectName("SCOTT2");
		long start = System.currentTimeMillis();
		dataSet.initializeDataSet();
		//dataSet.getGatherIterator().hasNext();
		//dataSet.getGatherIterator().next();
		dataSet.getPdsio().setSortOrder("CDP.OFFSET");
		int count=0;
		while (dataSet.getGatherIterator().hasNext() && count <20) {
			for (int i=0;i<dataSet.getGather().getTraces().size();i++) {
				System.out.println("cdp: "+dataSet.getGather().getCDP()+" inline:"+dataSet.getGather().getTraces().get(i).getHeaderValue(INLINE_HEADER)+
						" xline:"+dataSet.getGather().getTraces().get(i).getHeaderValue(XLINE_HEADER));
			}
			count++;
		}
//		long stop = System.currentTimeMillis();
//		System.out.println("gather load took "+ (stop-start) +" milliseconds");
//		System.out.println(dataSet.getGather().toString());
//		System.out.println(dataSet.getCdpGrid().description());
		
		/*
		try {
			//write receivers
			FileWriter writer = new FileWriter("dataout.txt");
			writer.write(pdsio.getGather().toStringLong());
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("done writing out gather...\n");
		*/
	}
		

	//call to C Paradigm IO library through JNI - loads background information about dataset
	private native DataSet nativeInitializeDataSet(DataSet dataSet);

	//call to C Paradigm IO library through JNI - loads first trace of particular gather
	private native SeismicTrace nativeLoadGatherIDFirstTrace(int gatherID, SeismicTrace trace);

	//call to C Paradigm IO library through JNI - loads first trace of entire dataset
	private native SeismicTrace nativeLoadDataSetFirstTrace(SeismicTrace trace);
	
	//call to C Paradigm IO library through JNI - loads first trace of entire dataset
	private native SeismicTrace nativeLoadNextGatherFirstTrace(SeismicTrace trace);

	//call to C Paradigm IO library through JNI - loads next trace in gather/dataset
	// returns null if no traces are left in current gather
	private native SeismicTrace nativeLoadNextTrace(SeismicTrace trace);

	//call to initialize method and class ID's in C
	public native void initIDs();
	
	/** call to C Paradigm IO library through JNI - changes order that data is read from disk */
	/** "order" is of format major.minor */
	/** requires pointer file on disk created by Paradigm (sort data in Focus First) */
	private native void nativeChangeSortOrder(String order);

	public void initializeDataSet() {
		//first, set method ID's in C
		initIDs();
		
		//now load dataset in C
		if (dataSet.getProjectName().length() > 0) {
			dataSet = nativeInitializeDataSet(dataSet);
		}
		else
		{
			System.err.println("can't initialize dataset! Project name not set!!");
		}
		
		//now, try to get CDP grid
		try {
			dataSet.getCdpGrid().loadFromPDS(dataSet.getPath()+".pds");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//now load first trace
		loadDataSetFirstTrace();
		//System.gc();
	}

	public void loadDataSetFirstTrace(){
		dataSet.clearGather();
		dataSet.setTrace(nativeLoadDataSetFirstTrace(dataSet.getTrace()));
		//System.gc();
	}
	
	public void loadGatherIDFirstTrace(int id){
		dataSet.clearGather();
		dataSet.setTrace(nativeLoadGatherIDFirstTrace(id, dataSet.getTrace()));
		//System.gc();
	}

	public void loadNextGatherFirstTrace(){
		dataSet.clearGather();
		this.endOfGather = true;
		trace = nativeLoadNextGatherFirstTrace(dataSet.getTrace());
		if (trace != null) {
			dataSet.setTrace(trace);
			this.endOfGather = false;
		}
	}
	
	public void loadNextTraceInGather(){
		this.endOfGather = true;
		trace = nativeLoadNextTrace(dataSet.getTrace());
		if (trace != null) {
			dataSet.setTrace(trace);
			this.endOfGather = false;
		}
	}
	
	public void loadNextTrace(){
		SeismicTrace trace = dataSet.getTrace();
		loadNextTraceInGather();
		if (this.endOfGather) {
			dataSet.setTrace(trace);
			loadNextGatherFirstTrace();
		}
		//System.gc();
	}

	public void loadGather(int id) {
		this.endOfGather = false;
		loadGatherIDFirstTrace(id);
		while(!this.endOfGather) {
			loadNextTraceInGather();
		}
	}
	
	public void loadFirstGather() {
		this.endOfGather = false;
		loadDataSetFirstTrace();
		while(!this.endOfGather) {
			loadNextTraceInGather();
		}
		firstGatherLoaded = true;
	}

	public void loadNextGather() {
		this.endOfGather = false;
		loadNextGatherFirstTrace();
		while(!this.endOfGather) {
			loadNextTraceInGather();
		}
		if (dataSet.getGather().getTraces().size() > 0) {
			this.endOfDataSet = false;
		}
		else {
			this.endOfDataSet = true;
		}
		if (debug) System.out.println("finished gather: "+dataSet.getGather().getGatherId());
	}

	public DataSet getDataSet() {
		return dataSet;
	}

	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	public boolean isEndOfGather() {
		return endOfGather;
	}

	public void setEndOfGather(boolean endOfGather) {
		this.endOfGather = endOfGather;
	}

	public boolean hasNext() {
		if (!firstGatherLoaded) {
			loadFirstGather();
		}
		else {
			loadNextGather();
		}
		return !endOfDataSet;
	}

	public Object next() {
		return dataSet.getGather();
	}

	public void remove() {
		// TODO Auto-generated method stub
		
	}

	public void setSortOrder(String matchingOrder) {
		nativeChangeSortOrder(matchingOrder);
	}


}