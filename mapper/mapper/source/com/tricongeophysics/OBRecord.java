package com.tricongeophysics;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import com.tricongeophysics.TableData.ColumnType;

public class OBRecord extends AbstractTableData {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int shot;
	protected int ffid;
	protected int sourceLineNumber;
	protected int sourceStationNumber;
	private ArrayList<Integer> fromChan = null;
	private ArrayList<Integer> toChan = null;
	private ArrayList<Integer> receiverLineNumber = null;
	private ArrayList<Integer> fromReceiver = null;
	private ArrayList<Integer> toReceiver = null;
	private File file;
	private transient DecimalFormat df2;
	private boolean duplicateShotPoint = false;
	private boolean duplicateShot;
	private int shotFromDupSP;
	private boolean[] editableCols = {true, true, true, false, false, false, true, true};
	private boolean kill = false;
	
	protected static int MAX_FILENAME_LENGTH = 15;
	protected static int numFiles=0;
	public transient final static String[] RequiredColumns = {"Shot", "ShotLine", "ShotStation", "DuplicateShotPoint", "ShotFromDupSP", "DuplicateShot", "Kill", "Survey", "FFID"};
    private transient static final ColumnType[] ColumnTypes = {ColumnType.Standard, ColumnType.Standard, ColumnType.Standard, ColumnType.Error,
        ColumnType.Error, ColumnType.Error, ColumnType.Warning, ColumnType.Standard};
    private static int lastIndex = 0;
    private transient ArrayList<RowColorChangedListener> rowColorChangedListeners = new ArrayList<RowColorChangedListener>();
    int survey = 0; //index of text file that ob's came from

	{
		init();
	}
	
	public OBRecord() {
		fromChan = new ArrayList<Integer>();
		toChan = new ArrayList<Integer>();
		receiverLineNumber = new ArrayList<Integer>();
		fromReceiver = new ArrayList<Integer>();
		toReceiver = new ArrayList<Integer>();
	}
	
	public void init()
    {
	    df2 = new java.text.DecimalFormat("####0.00");
    }

    static void setNumFiles(int n){
		if (n>0)numFiles=n;
	}
	static int getNumFiles(){
		return numFiles;
	}
	
	
	public String toString() {
		return 
		"Shot: "+shot+" FFID: " + ffid + "<br>"+
		"Shot Line: "+sourceLineNumber+"<br>"+
		"Shot Station: "+sourceStationNumber+"<br>"+
		"</html>";
	}
	
	public String toSegp1() {
		//...Converts to SEG-P1 format, without leading S/R
	    if (df2 == null) init();
		String shotRecord = "";
		String s  = SUtil.stringResize(shot+""                      ,10);
		String sl = SUtil.stringResize(sourceLineNumber+""          ,10);
		String ss = SUtil.stringResize(sourceStationNumber+""       ,16);
		String kk = SUtil.stringResize(SUtil.getNumVal(getKill())+"",5);
		String su = SUtil.stringResize(survey+"",7);
		String ff = SUtil.stringResize(ffid+"",7);
		String x = (getKill()) ? "K" : "X";
		
		//...Loop through receiver spread
		for (int i=0;i<getFromChan().size();i++) {
			String fc = SUtil.stringResize(getFromChan(i)+""          ,6);
			String tc = SUtil.stringResize(getToChan(i)+""            ,5);
			String rl = SUtil.stringResize(getReceiverLineNumber(i)+"",9);
			String fr = SUtil.stringResize(getFromReceiver(i)+""      ,16);
			String tr = SUtil.stringResize(getToReceiver(i)+""        ,8);
			shotRecord = shotRecord + x + ff + sl + ss + fc + tc + rl + fr + tr + s + kk + su;
			//add optional data
	    	if (optionalData != null) {
	    		for (Object o: optionalData) {
	    		    if (o == null) continue; //shouldn't happen!! but just in case
	    			String st = o.toString();
	    			if (o instanceof Number)
	    				st = df2.format(o);
	    			shotRecord += SUtil.stringResize(st, 10);
	    		}
	    	}
	    	if ( Math.abs(Integer.parseInt(this.getToChan().get(i).toString()) - Integer.parseInt(this.getFromChan().get(i).toString())) !=
                Math.abs(Integer.parseInt(this.getToReceiver().get(i).toString())-Integer.parseInt(this.getFromReceiver().get(i).toString())))
            shotRecord = shotRecord+" channel range differs from receiver range";
			shotRecord = shotRecord+"\n";
		}
		return shotRecord;
	}
	
		
	public int getShot() {
		return shot;
	}
	public void setShot(int shot) {
		this.shot = shot;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public ArrayList<Integer> getFromChan() {
		return fromChan;
	}
	public void setFromChan(int i, int num) {
		if (i<fromChan.size() && i>=0)
			this.fromChan.set(i,new Integer(num));
		else if (i==fromChan.size())
			this.fromChan.add(new Integer(num));
	}
	public ArrayList<Integer> getFromReceiver() {
		return fromReceiver;
	}
	public void setFromReceiver(int i, int num) {
		if (i<fromReceiver.size() && i>=0)
			this.fromReceiver.set(i,new Integer(num));
		else if (i==fromReceiver.size())
			this.fromReceiver.add(new Integer(num));
	}
	public ArrayList<Integer> getReceiverLineNumber() {
		return receiverLineNumber;
	}
	public void setReceiverLineNumber(int i, int num) {
		if (i<receiverLineNumber.size() && i>=0)
			this.receiverLineNumber.set(i,new Integer(num));
		else if (i==receiverLineNumber.size())
			this.receiverLineNumber.add(new Integer(num));
	}
	public int getSourceLineNumber() {
		return sourceLineNumber;
	}
	public void setSourceLineNumber(int sourceLineNumber) {
		this.sourceLineNumber = sourceLineNumber;
	}
	public int getSourceStationNumber() {
		return sourceStationNumber;
	}
	public void setSourceStationNumber(int sourceStationNumber) {
		this.sourceStationNumber = sourceStationNumber;
	}
	public ArrayList<Integer> getToChan() {
		return toChan;
	}
	public void setToChan(int i, int num) {
		if (i<toChan.size() && i>=0)
			this.toChan.set(i,new Integer(num));
		else if (i==toChan.size())
			this.toChan.add(new Integer(num));
	}
	public ArrayList<Integer> getToReceiver() {
		return toReceiver;
	}
	public void setToReceiver(int i, int num) {
		if (i<toReceiver.size() && i>=0)
			this.toReceiver.set(i,new Integer(num));
		else if (i==toReceiver.size())
			this.toReceiver.add(new Integer(num));
	}

	@Override
	public String[] getRequiredColumns() {
		return RequiredColumns;
	}

	public String getSegp1Header() {
		String header = "H     FFID  ShotLine     ShotStation  Chan Chan   RcvrLine        RcvrStn RcvrStn   SHOT  Kill  Survey";
		String[] optionalNames = getOptionalColumnNames();
		if (optionalNames == null) return header+"\n";
		for (String name: optionalNames) {
			header += SUtil.stringResize(name, 10);
		}
		return header+"\n";
	}

	public int getToReceiver(int cable) {
		return getToReceiver().get(cable);
	}

	public int getFromReceiver(int cable) {
		return getFromReceiver().get(cable);
	}

	public int getToChan(int cable) {
	    if (cable >= getToChan().size()) {
	        System.err.println("OBRecord.getToChan() - cable number greater than toChan array: " + cable);
	    }
		return getToChan().get(cable);
	}

	public int getFromChan(int cable) {
		return getFromChan().get(cable);
	}

	public int getReceiverLineNumber(int cable) {
		return getReceiverLineNumber().get(cable);
	}

	public int getSurvey()
    {
        return survey;
    }

    public void setDuplicateShotPoint(boolean duplicateShotPoint) {
		this.duplicateShotPoint = duplicateShotPoint;
	}
	
	public boolean getDuplicateShotPoint() {
		return duplicateShotPoint;
	}

	public void setDuplicateShot(boolean duplicateFFID) {
		this.duplicateShot = duplicateFFID;
	}
	
	public boolean getDuplicateShot() {
		return this.duplicateShot;
	}

	@Override
	public boolean[] getEditableCols() {
		return editableCols;
	}

	public void setKill(boolean kill) {
		this.kill = kill;
		fireRowColorChanged();
	}

	private void fireRowColorChanged()
    {
	    for (RowColorChangedListener l: getRowColorChangedListeners()) {
            l.rowColorChanged();
        }
    }

    public boolean getKill() {
		return kill;
	}

    public void setFromChan(ArrayList<Integer> fromChan)
    {
        this.fromChan = fromChan;
    }

    public void setToChan(ArrayList<Integer> toChan)
    {
        this.toChan = toChan;
    }

    public void setReceiverLineNumber(ArrayList<Integer> receiverLineNumber)
    {
        this.receiverLineNumber = receiverLineNumber;
    }

    public void setFromReceiver(ArrayList<Integer> fromReceiver)
    {
        this.fromReceiver = fromReceiver;
    }

    public void setToReceiver(ArrayList<Integer> toReceiver)
    {
        this.toReceiver = toReceiver;
    }

    @Override
    public int compareTo(TableData o)
    {
        if (o == null) return 0;
        OBRecord or = (OBRecord) o;
        if (this.shot > or.shot) return 1;
        if (this.shot < or.shot) return -1;
        return 0;
    }

    @Override
    public boolean containsError()
    {
        return (this.duplicateShot || this.duplicateShotPoint);
    }

    @Override
    public boolean containsWarning()
    {
        return this.kill;
    }
    
    private  ArrayList<RowColorChangedListener> getRowColorChangedListeners()
    {
        if(rowColorChangedListeners == null) rowColorChangedListeners = new ArrayList<RowColorChangedListener>();
        return rowColorChangedListeners;
    }

    @Override
    public ColumnType[] getColumnTypes()
    {
        return ColumnTypes;
    }
    
    @Override
    public void addRowColorChangedListener(RowColorChangedListener l)
    {
        getRowColorChangedListeners().add(l);
    }

    public void setShotFromDupSP(Integer shotFromDupSP)
    {
        this.shotFromDupSP = shotFromDupSP;
    }

    public Integer getShotFromDupSP()
    {
        return shotFromDupSP;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        OBRecord oRecord = (OBRecord) o;
        if (shot != oRecord.shot) return false;
        if (ffid != oRecord.ffid) return false;
        if (sourceLineNumber != oRecord.sourceLineNumber) return false;
        if (sourceStationNumber != oRecord.sourceStationNumber) return false;
        return true;
    }

    public void setSurvey(int survey)
    {
        this.survey  = survey;
    }
    
    public int getShotLine() {
        return this.sourceLineNumber;
    }
    
    public void setShotLine(int shotLine) {
        this.sourceLineNumber = shotLine;
    }
    
    public int getShotStation() {
        return this.sourceStationNumber;
    }
    
    public void setShotStation(int shotStation) {
        this.sourceStationNumber = shotStation;
    }

    public String toSumFile()
    {
      //...Converts to SEG-P1 format, without leading S/R
        if (df2 == null) init();
        String shotRecord = "";
        String ff = SUtil.stringResize(shot+""                      ,10);
        String sl = SUtil.stringResize(sourceLineNumber+""          ,10);
        String ss = SUtil.stringResize(sourceStationNumber+""       ,16);
        String kk = SUtil.stringResize(SUtil.getNumVal(getKill())+"",5);
        String su = SUtil.stringResize(survey+"",7);
        String fid = SUtil.stringResize(ffid+"",7);
        String x = (getKill()) ? "K" : "M";

        shotRecord = shotRecord + x + ff + sl + ss + fid + kk + su;
        //add optional data
        if (optionalData != null) {
            for (Object o: optionalData) {
                if (o == null) continue; //shouldn't happen!! but just in case
                String s = o.toString();
                if (o instanceof Number)
                    s = df2.format(o);
                shotRecord += SUtil.stringResize(s, 10);
            }
        }
        shotRecord = shotRecord+"\n";
        return shotRecord;
    }

    public String getSumHeader()
    {
        String header = "H      SHOT  ShotLine     ShotStation  Kill Survey";
        String[] optionalNames = getOptionalColumnNames();
        if (optionalNames == null) return header+"\n";
        for (String name: optionalNames) {
            header += SUtil.stringResize(name, 10);
        }
        return header+"\n";
    }

    public int getSPIndex(ReflectiveTableModel sList)
    {
        if (lastIndex > sList.size()) {
            System.out.println("OBRecord.getSPIndex - lastIndex greater than slist.size!!! " + lastIndex);
            lastIndex = 0;
        }
        for (int i=lastIndex; i<sList.size(); i++) {
            SP sp = (SP) sList.get(i);
            if (sp.lineNumber == this.sourceLineNumber && sp.stationNumber == this.sourceStationNumber) {
                lastIndex = i;
                return i;
            }
        }
        for (int i=0; i<lastIndex; i++) {
            SP sp = (SP) sList.get(i);
            if (sp.lineNumber == this.sourceLineNumber && sp.stationNumber == this.sourceStationNumber) {
                lastIndex = i;
                return i;
            }
        }
        System.err.println("OBRecord.getSPIndex - SP not found! line:" + this.sourceLineNumber + " station: " + this.sourceStationNumber);
        return -1;
    }

    public int getLastChan()
    {
        if (toChan == null) return 0;
        if (toChan.size() == 0) return 0;
        return toChan.get(toChan.size() - 1);
    }

    public int getFfid()
    {
        return ffid;
    }

    public void setFfid(int ffid)
    {
        this.ffid = ffid;
    }

    public void setKillInt(int val)
    {
        kill = (val > 0);
    }
}
