package com.tricongeophysics;

import java.util.ArrayList;
import java.util.Arrays;

public class ProcessShotRecordsJob extends Job {
	
    boolean cancel = false;
	ProgressEvent e = new ProgressEvent(this,null,0);
	private ReflectiveTableModel obList;
	private ArrayList<Station> receiverList;
	private ArrayList<Station> spList;
	private ArrayList<Integer> usedFfids = new ArrayList<Integer>();
	private ArrayList<SP> usedSPs = new ArrayList<SP>();
	private int prevFfid;
	private ArrayList<OBRecord> uniqSpFfids = new ArrayList<OBRecord>();
	private  ArrayList<OBRecord> uniqFfidRecords = new ArrayList<OBRecord>();
	private Stopwatch receiverTimer = null;
	private Stopwatch shotTimer = null;
	private Stopwatch obTimer = null;
	private Stopwatch totalTimer = null;
	private boolean debugTimer = false;
    private Stopwatch loadSRTimer;
    private Stopwatch loopingReceivers = null;
    private Stopwatch addingSurveyedReceiver = null;
    private int i;
    private ArrayList<StationStatistic> receiverLines = new ArrayList<StationStatistic>();
    private ArrayList<StationStatistic> spLines = new ArrayList<StationStatistic>();
    private int dupShotPoints;
    private Integer unSurveyedSPs = 0;
    private Integer unUsedSPs = 0;
    private Integer unSurveyedRecs = 0;
    private Integer unUsedRecs = 0;
    private int spsSurveyedTwice = 0;
    private int recsSurveyedTwice = 0;
    private int dupFFIDs = 0;
    private boolean interpUnsurveyed = true;
   
	private static final int minCapacity = 5000;
	

	public ProcessShotRecordsJob(ReflectiveTableModel obList,
			ReflectiveTableModel receiverList,
			ReflectiveTableModel spList) {
		this.obList = obList;
		/*
		this.receiverList = new ArrayList<Station>();
		for (int i=0;i<receiverList.size();i++) this.receiverList.set(i, (Receiver) receiverList.get(i));
		this.spList = new ArrayList<Station>();
		for (int i=0;i<spList.size();i++) this.spList.set(i, (SP) spList.get(i));
		*/
		if (receiverList == null) return;
		if (spList == null) return;
		this.receiverList = receiverList.getTableData();
		this.spList = spList.getTableData();
	}

	@Override
	public int getProgressMax() {
		if (obList == null) return 0;
		return obList.size();
	}

	@Override
	public void cancel() {
		SUtil.print("stopping");
		cancel = true;
	}

	@Override
	public void doJob() {
	    if (debugTimer == true) {
	        obTimer = new Stopwatch("Ob");
	        shotTimer = new Stopwatch("shots");
	        receiverTimer = new Stopwatch("receiver spread");
	        totalTimer = new Stopwatch("total");
	        loadSRTimer = new Stopwatch("load shot record");
	        addingSurveyedReceiver = new Stopwatch("add surveyed stations");
	        loopingReceivers = new Stopwatch("looping stations");
	        totalTimer.start();
	    }
	    cancel = false;
		
		if (receiverList == null || receiverList.size() == 0) 
			abort("Missing Receivers");
		if (obList == null || obList.size() == 0) 
			abort("Missing Shot Records");
		if (spList == null || spList.size() == 0) 
			abort("Missing Shot Points");
		if(cancel) {
			fireJobFinished();
			return;
		}
		
		int counter = 0;
		ShotRecord sr = new ShotRecord();
		OBRecord obr = (OBRecord) obList.get(0);
		prevFfid = obr.getFfid()-1;
		initializeJob();
		setProgress(0, "\nProcessing Observers Report(s)\n");
		surveyMinMax(spList, spLines);
        surveyMinMax(receiverList, receiverLines);
		for(TableData td: obList) {
			obr = (OBRecord) td;
			if(cancel) break;
			if (obr.getKill()) continue;
			if (debugTimer) loadSRTimer.start();
			sr.loadOB(obr, false);
			if (debugTimer) loadSRTimer.stop();
			Receiver[] receivers = sr.getReceivers();
			if (debugTimer) receiverTimer.start();
			processReceiverSpread(receivers, obr);
			if (debugTimer) receiverTimer.stop();
			SP sp = sr.getSp();
			if (debugTimer) shotTimer.start();
			processStation(sp, spList, obr);
			if (debugTimer) shotTimer.stop();
			if (debugTimer) obTimer.start();
			processShotRecord(sr);
			if (debugTimer) obTimer.stop();
			if (counter%10 == 0) {
				setProgress(counter, null);
			}
			counter++;
		}
		if (!cancel) {
		    setProgress(0, "\nProcessing Shot Points\n");
		    SUtil.sort(spList);
		    checkSurveyedTwice(spList);
		    SUtil.sort(spLines);
		    surveyStatistics(spList);
		    if (interpUnsurveyed) interpUnSurveyed(spList);
		    calcDistances(spList);
		    setProgress(0, "\nProcessing Receivers\n");
		    SUtil.sort(receiverList);
		    checkSurveyedTwice(receiverList);
		    SUtil.sort(receiverLines);
		    surveyStatistics(receiverList);
		    if (interpUnsurveyed) interpUnSurveyed(receiverList);
		    calcDistances(receiverList);
		}
		if (debugTimer) {
		    totalTimer.stop();
		    printTimerInfo();
		}
	}

	/**
	 * interpolate new xyz for unsurveyed stations based on line and station number
	 * @param stations
	 */
	private void interpUnSurveyed(ArrayList<Station> stations)
    {
        for (int i=0; i<stations.size(); i++) {
            Station station = stations.get(i);
            if (station.unSurveyed) {
                Station.interp(stations, station, i);
            }
        }
    }

	/**
	 * Count how many unsurveyed SP/Rec, unused SP/Rec, 
	 * and surveyed-twice SP/Rec.
	 * @param stations
	 */
    private void surveyStatistics(ArrayList<Station> stations)
	{
	    for (Station station: stations) {
	        if (station.unSurveyed) {
	            if (station instanceof SP) {
	                unSurveyedSPs++;
	            } else
	                unSurveyedRecs++;
	        }
	        if (station.unUsed) {
	            if (station instanceof SP) {
	                unUsedSPs++;
	            } else
	                unUsedRecs++;
	        }
	        if (station.duplicate) {
	            if (station instanceof SP) {
	                spsSurveyedTwice ++;
	            } else
	                recsSurveyedTwice ++;
	        }
	    }
	}

    private void surveyMinMax(ArrayList<Station> stations, ArrayList<StationStatistic> lines)
    {
        for (Station s: stations) {
            processSurveyStats(lines, s);
        }
    }
    
    private void calcDistances(ArrayList<Station> stations)
    {
	    double distance = 0;
	    int i=0;
        for (; i<stations.size()-1; i++) {
            Station s1 = stations.get(i);
            Station s2 = stations.get(i+1);
            int line1 = s1.getLineNumber();
            int line2 = s2.getLineNumber();
            if (line1 == line2) {
                distance = SUtil.distance(s1.x, s1.y, s2.x, s2.y);
            }  
            s1.setDistance(distance); //if next station is from different line, go ahead and use previous distance
        }
        stations.get(i).setDistance(distance);
    }
    
    private void printTimerInfo()
    {
        totalTimer.printTime();
        loadSRTimer.printTime();
        receiverTimer.printTime();
        shotTimer.printTime();
        obTimer.printTime();
        loopingReceivers.printTime();
        addingSurveyedReceiver.printTime();
    }

    private void checkSurveyedTwice(ArrayList<Station> stations) {
        setProgress(0, null); //reset timer
        double factor = obList.size()*1.0/stations.size(); //adjust progress to number of shotrecords for progress meter
		ArrayList<Station> usedStations = new ArrayList<Station>();
		int counter = 0;
		for (TableData td: stations) {
			Station station = (Station) td;
			if (station.kill) continue;
			int index = usedStations.indexOf(station);
			if (index >= 0) {
				station.setDuplicate(true);
				usedStations.get(index).setDuplicate(true);
			}
			else {
				usedStations.add(station);
			}
			if (counter%10 == 0) {
				setProgress((int) (factor*counter), null);
			}
			counter++;
		}
	}

    private void initializeJob() {
		resetStations(spList);
		resetStations(receiverList);
		resetShotRecords();
		
		usedFfids.clear();
		usedSPs.clear();
		uniqFfidRecords.clear();
		uniqSpFfids.clear();
		
		usedFfids.ensureCapacity(minCapacity);
        usedSPs.ensureCapacity(minCapacity);
        uniqFfidRecords.ensureCapacity(minCapacity);
        uniqSpFfids.ensureCapacity(minCapacity);
        
        dupShotPoints = 0;
        dupFFIDs = 0;
        unUsedSPs = 0;
        unSurveyedSPs = 0;
        unUsedRecs = 0;
        unSurveyedRecs = 0;
        spsSurveyedTwice = 0;
        recsSurveyedTwice = 0;
	}

	private void resetShotRecords() {
		for (Object o: obList) {
			OBRecord obr = (OBRecord) o;
			obr.setDuplicateShotPoint(false);
			obr.setDuplicateShot(false);
		}
	}

	private void processShotRecord(ShotRecord sr) {
		OBRecord obr = sr.getObRecord();
		int ffid = sr.getFfid();
		checkDuplicateFfid(obr);
		
		int gap = Math.abs(ffid - prevFfid)-1;
		if (gap > 0)
			setProgress(-1, "FFID " + SUtil.stringResize(ffid+"", 6) + " found after " + 
					SUtil.stringResize(prevFfid+"", 6) + ". Skipped " + 
					SUtil.stringResize(gap+"", 4) + " record(s).");
		prevFfid = ffid;
		
		checkDuplicateShotPoint(obr);
		processOBStats(spLines, sr.getSp());
	}

	/**
	 * Checks if two or more FFID's use the same SP.
	 * uses iObr as a first guess for the duplication location to speed things up a bit
	 * @param thisObr
	 */
	private void checkDuplicateShotPoint(OBRecord thisObr) {
		for (OBRecord otherObr: uniqSpFfids) {
			if (thisObr.getSourceLineNumber() == otherObr.getSourceLineNumber() &&
					thisObr.getSourceStationNumber() == otherObr.getSourceStationNumber()) {
				thisObr.setDuplicateShotPoint(true);
				thisObr.setShotFromDupSP(otherObr.getShot());
				dupShotPoints += 1;
				if (otherObr.getDuplicateShotPoint() == false) {
					otherObr.setDuplicateShotPoint(true);
					otherObr.setShotFromDupSP(thisObr.getShot());
					dupShotPoints += 1;
				}
				return;
			}
		}
		uniqSpFfids.add(thisObr);
	}

	private void checkDuplicateFfid(OBRecord thisObr) {
		for (OBRecord otherObr: uniqFfidRecords) {
			if (thisObr.getFfid() == otherObr.getFfid()) {
				thisObr.setDuplicateShot(true);
				dupFFIDs += 1;
				if (!otherObr.getDuplicateShot()) {
				    otherObr.setDuplicateShot(true);
				    dupFFIDs += 1;
				}
				return;
			}
		}
		uniqFfidRecords.add(thisObr);
	}

	/**
	 * sets defaults (unSurveyed = false, unUsed = true, duplicate = false)
	 * to what they should be before processing.
	 * @param stations
	 */
	private void resetStations(ArrayList<Station> stations) {
		for (TableData td: stations) {
			Station s = (Station) td;
//			s.setUnSurveyed(false);  //this method doesn't do anything!!
			s.setUnUsed(true);
			s.setDuplicate(false);
		}
	}

	/**
	 * compares a particular station against the list of surveyed stations
	 */
	private void processStation(Station thisStation, ArrayList<Station> stations, OBRecord obr) {
		if (thisStation == null|| stations == null) return;
		Station surveyedStation = null;
		i = Math.max(0, i);
		i = Math.min(i, stations.size()-1);
		int size = stations.size();

		//if (debugTimer) loopingReceivers.start();
		//.. Find thisStation in stations list
		int iOld = i;
		for (; i<size; i++) { //use previous position (i) as first guess for next position
		    Station tmpStation = stations.get(i);
		    if (tmpStation.equals(thisStation)) {
		       // if(!tmpStation.kill) {  //this was causing killed stations to get added again
		            surveyedStation = tmpStation;
		            break;
		       // }
		    }
		}
		if (surveyedStation == null) {
		    for (i=0; i<iOld; i++) { //search first half of array
		        Station tmpStation = stations.get(i);
		        if (tmpStation.equals(thisStation)) {
		          //  if(!tmpStation.kill) { //this was causing killed stations to get added again
		                surveyedStation = tmpStation;
		                break;
		         //   }
		        }
		    }
		}
		//if (debugTimer) loopingReceivers.stop();
		//if (debugTimer) addingSurveyedReceiver.start();
		//.. If found, set it to "used", otherwise create new one
		if (surveyedStation == null) { //Station Not Surveyed!!!
		    surveyedStation = thisStation.getCopy(); //don't trust pointer to thisStation! It can point to something else later.
//		    surveyedStation.setUnSurveyed(true); //this method doesn't do anything!!
		    surveyedStation.setUnUsed(false);
		    stations.add(surveyedStation);
		}
		else
			surveyedStation.setUnUsed(false);
		//if (debugTimer) addingSurveyedReceiver.stop();
		surveyedStation.setUsedByShot(obr.getShot());
	}

	private void processReceiverSpread(Receiver[] receivers, OBRecord obr) {
		if (receivers == null) return;
		for(Receiver rec: receivers) {
		    if (rec == null) return; //null receiver indicates end of receiver spread
			processStation(rec, receiverList, obr);
			processOBStats(receiverLines, rec);
		}
	}

	private void processOBStats(ArrayList<StationStatistic> stats, Station station)
    {
        int line = station.lineNumber;
        StationStatistic tmp = new StationStatistic(line);
        int index = stats.indexOf(tmp);
        int stationNumber = station.stationNumber;
        if (index < 0) {
            StationStatistic ss = new StationStatistic(line);
            ss.setMinSpread(stationNumber);
            ss.setMaxSpread(stationNumber);
            stats.add(ss);
        } else {
            StationStatistic ss = stats.get(index);
            int min = Math.min(ss.getMinSpread(), stationNumber);
            int max = Math.max(ss.getMaxSpread(), stationNumber);
            if (min == 0) min = stationNumber;
            ss.setMinSpread(min);
            ss.setMaxSpread(max);
        }
    }
	
	private void processSurveyStats(ArrayList<StationStatistic> stats, Station station)
    {
	    int line = station.lineNumber;
	    StationStatistic tmp = new StationStatistic(line);
        int index = stats.indexOf(tmp);
        int stationNumber = station.stationNumber;
        if (index < 0) {
            StationStatistic ss = new StationStatistic(line);
            ss.setMinSurvey(stationNumber);
            ss.setMaxSurvey(stationNumber);
            stats.add(ss);
        } else {
            StationStatistic ss = stats.get(index);
            int min = Math.min(ss.getMinSurvey(), stationNumber);
            int max = Math.max(ss.getMaxSurvey(), stationNumber);
            ss.setMinSurvey(min);
            ss.setMaxSurvey(max);
        }
    }


    private void abort(String message) {
		setProgress(-1, message);
		setProgress(-1, "Aborting!!");
		cancel = true;
	}

	private void setProgress(int val, String message) {
		e.setMessage(message);
		e.setProgressVal(val);
		this.fireProgressChanged(e);
	}

    @Override
    public boolean getIndeterminate()
    {
        return false;
    }
    
    public class StationStatistic implements Comparable<StationStatistic>
    {
        private int line;
        private int minSpread;
        private int maxSpread;
        private int minSurvey;
        private int maxSurvey;

        public StationStatistic(int line)
        {
            this.setLine(line);
        }

        public void setMaxSpread(int station)
        {
            this.maxSpread = station;
        }

        public int getMaxSurvey()
        {
            return maxSurvey;
        }

        public void setMaxSurvey(int maxSurvey)
        {
            this.maxSurvey = maxSurvey;
        }

        public int getMinSpread()
        {
            return minSpread;
        }

        public int getMaxSpread()
        {
            return maxSpread;
        }

        public void setMinSpread(int station)
        {
            this.minSpread = station;
        }

        public void setLine(int line)
        {
            this.line = line;
        }

        public int getLine()
        {
            return line;
        }

        public void setMinSurvey(int minSurvey)
        {
            this.minSurvey = minSurvey;
        }

        public int getMinSurvey()
        {
            return minSurvey;
        }
        
        @Override
        public boolean equals(Object o) {
            StationStatistic ss = (StationStatistic) o;
            return (this.line == ss.line);
        }
        
        @Override
        public String toString() {
            String m="";
            if (minSpread > minSurvey || maxSpread < maxSurvey) m=" *";
            if (minSpread < minSurvey || maxSpread > maxSurvey) m=" !";
            return SUtil.stringResize(line+"", 8)
            +SUtil.stringResize(minSurvey+"", 8)
            +SUtil.stringResize(maxSurvey+"", 8)
            +SUtil.stringResize(minSpread+"", 8)
            +SUtil.stringResize(maxSpread+"", 8) + m;
        }

        @Override
        public int compareTo(StationStatistic o)
        {
            if (o == null) return 0;
            if (this.line < o.line) return -1;
            if (this.line == o.line) return 0;
            if (this.line > o.line) return 1;
            return 0;
        }
    }
    
    @Override
    public String printSummary() {
        if (receiverList == null || spList == null) return "";
        String 
        sum =  "\n    **************SUMMARY***************\n\n";
        sum += "    -------------Receivers--------------- \n\n";
        sum += "           |---Survey---|  |--Relation--|\n";
        sum += "    Line     Min     Max     Min     Max\n";
        for (StationStatistic ss: receiverLines) {
            sum += ss.toString() + "\n";
        }
        sum += "    ! = station(s) outside survey bounds\n    * = unused surveyed station(s)\n\n";
        sum += "    -------------Shot Points------------- \n\n";
        sum += "           |---Survey---|  |--Relation--|\n";
        sum += "    Line     Min     Max     Min     Max\n";
        for (StationStatistic ss: spLines) {
            sum += ss.toString() + "\n";
        }
        sum += "    ! = station(s) outside survey bounds\n    * = unused surveyed station(s)\n\n";
        sum += "Relation:   ";
        sum += "Total DuplicateSP Duplicate-FFIDs \n";
        sum += SUtil.stringResize(obList.size()+"", 17) + 
        SUtil.stringResize(dupShotPoints+"", 8) +
        SUtil.stringResize(dupFFIDs +"", 8) + 
        "\n\n";
        sum += "Receivers:  ";
        sum += "Total UnSurveyed Unused Surveyed-Twice(Duplicate)\n";
        sum += SUtil.stringResize(receiverList.size()+"", 17) + 
        SUtil.stringResize(unSurveyedRecs+"", 8) +
        SUtil.stringResize(unUsedRecs+"", 8) + 
        SUtil.stringResize(recsSurveyedTwice+"", 8) +
        "\n\n";
        sum += "Shot Points:";
        sum += "Total UnSurveyed Unused Surveyed-Twice(Duplicate)\n";
        sum += SUtil.stringResize(spList.size()+"", 17) + 
        SUtil.stringResize(unSurveyedSPs+"", 8) +
        SUtil.stringResize(unUsedSPs+"", 8) + 
        SUtil.stringResize(spsSurveyedTwice+"", 8) +
        "\n\n";
        
        
        return sum;
    }

    public boolean getInterpUnsurveyed()
    {
        return interpUnsurveyed;
    }

    public void setInterpUnsurveyed(boolean interpUnsurveyed)
    {
        this.interpUnsurveyed = interpUnsurveyed;
    }

}