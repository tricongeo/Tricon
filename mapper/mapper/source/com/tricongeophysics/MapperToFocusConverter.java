package com.tricongeophysics;

import java.awt.Container;
import java.util.ArrayList;

import javax.swing.JOptionPane;


public class MapperToFocusConverter
{
    private static final String X = "X";
    private static final String COORD = "COORD";
    private static final String Y = "Y";
    private static final String LINE = "Line";
    private static final String RECEIVER = "RECEIVER";
    private static final String STATION = "Station";
    private static final String Z = "Z";
    private static final String SURFACE = "SURFACE";
    private static final String ELEV = "ELEV";
    private static final String ATRSPLIT = ":";
    static final String SHOT = "SHOT";
    static final String FFID = "FFID";
    private static final String CHAN = "CHAN";
    private static final String MAP = "MAP";
    private static final String UPHOLE = "Uphole";
    private static final Object DEPTH = "Depth";
    private static final String TIME = "TIME";
    private Mapper mapper;
    private String project;
    private String line;
    private TriconGeometry tg;
    private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
//    private int lineCount;
    private int lineMin;
//    private int lineInc;
    private int maxStnCount;
    private int stnMin;
    private int stationInc;
    private ArrayList<Integer> recLines;
    private int lineMax;
    private boolean cancel = false;

    public MapperToFocusConverter(Mapper mapper, String project, String line)
    {
        this.mapper = mapper;
        this.project = project;
        this.line = line;
        tg = new TriconGeometry(project, line);
        loadBias();
    }

    private void loadBias()
    {
        CdpModel cdpModel = mapper.getCdpModel();
        tg.initializeBias(cdpModel);
    }

    public RcvrDbModel getReceiverModel()
    {
        fireProgressChanged(new ProgressEvent(this, "Converting Receivers to Focus", 1));
        RcvrDbModel rModel = tg.getRcvrModel();
        ReflectiveTableModel rList = mapper.receiverList;
        String[] cols = rList.getColumnNames();
        if (cols == null) return null;
        int counter = 0;
        for (String colName: cols) {
//            String shortCol = colName.substring(0, Math.min(MaxString, colName.length()));
            String event = colName;
            String attribute = colName;
            if(colName.equals(X)) attribute = COORD;
            if(colName.equals(Y)) attribute = COORD;
            if(colName.equals(Z)){ event = SURFACE; attribute = ELEV; }
            if(colName.equals(LINE)) event = RECEIVER;
            if(colName.equals(STATION)) event = RECEIVER;
            if(colName.contains(ATRSPLIT)) {
                String[] parts = colName.split(ATRSPLIT);
                if (parts.length > 1) {
                    event = parts[0];
                    attribute = parts[1];
                }
            }
            rModel.addAttribute(event, attribute);
            FocusDbAttr atr = rModel.getAttr(event, attribute);
            Object[] vals = rList.getValues(colName);
            float[] fvals = new float[vals.length];
            for (int i=0; i<vals.length; i++) {
                Object v = vals[i];
                if (v == null) v = 0;
//                float f = (float) SUtil.sval(v.toString());
                float f = (float) SUtil.getNumVal(v);
                fvals[i] = f;
            }
            atr.setSize(vals.length);
            atr.setVals(fvals);
            tg.getBias().removeBias(atr);
            fireProgressChanged(new ProgressEvent(this, null, counter++));
//            System.gc();
        }
        System.gc();
        return rModel;
    }

    
    public FocusDbModel getShotModel() throws MapperToFocusException
    {
        ShotDbModel sModel = tg.getShotModel();
        ReflectiveTableModel oList = mapper.obList;
        ReflectiveTableModel sList = mapper.spList;
        SpIndexMap2 spIndexMap = new SpIndexMap2(sList);
        if (oList == null) return null;
        if (sList == null) return null;
        String[] cols = sList.getColumnNames();
        
        //...Set First SHOT
        OBRecord thisSHOT = (OBRecord) oList.get(0);
        int firstSHOT = thisSHOT.shot;
        sModel.setFirstID(firstSHOT);
        mapper.maxChans = 0;
        
        //...Loop through column names
        fireProgressChanged(new ProgressEvent(this, "Converting Shots to Focus", 1));
        int counter = 0;
//        Stopwatch totalTimer = new Stopwatch("Get Shot Total");
//        Stopwatch getAttrTimer = new Stopwatch("Get Attribute");
//        Stopwatch valLoop = new Stopwatch("Shot Val Loop");
//        Stopwatch findShot = new Stopwatch("Shot Find SP");
//        totalTimer.start();
        for (String colName: cols) {
//            getAttrTimer.start();
//            String shortCol = checkLength(colName);
//            String event = shortCol;
//            String attribute = shortCol;
            String event = colName;
            String attribute = colName;
            if(colName.equals(X)) attribute = COORD;
            if(colName.equals(Y)) attribute = COORD;
            if(colName.equals(Z)){ event = SHOT; attribute = ELEV; }
            if(colName.equals(LINE)) event = SHOT;
            if(colName.equals(STATION)) event = SHOT;
            if(colName.equals(UPHOLE)) { event = UPHOLE; attribute = TIME; }
            if(colName.equals(DEPTH)) event = SHOT;
            if(colName.contains(ATRSPLIT)) {
                String[] parts = colName.split(ATRSPLIT);
                if (parts.length > 1) {
                    event = parts[0];
                    attribute = parts[1];
                }
            }
            sModel.addAttribute(event, attribute);
            FocusDbAttr atr = sModel.getAttr(event.toUpperCase(), attribute.toUpperCase());
//            Object[] vals = oList.getValues(colName);
            float[] fvals = new float[oList.size()];
            
//            getAttrTimer.stop();
            
            //...Loop through shots, getting line/station from shot record, other info from associated SP
            int shotOld = firstSHOT - 1;
//            valLoop.start();
            for (int i=0; i<fvals.length; i++) {
                thisSHOT = (OBRecord) oList.get(i);
                mapper.maxChans = Math.max(thisSHOT.getLastChan(), mapper.maxChans);
                int shotInc = thisSHOT.shot - shotOld;
                if (shotInc != 1) {
                    throw new MapperToFocusException("SHOT must be sequential!\nJumped from: " + shotOld + " to: " + thisSHOT.shot +
                            "\n\nTo Fix:\n 1 - Edit->Reset Shot Numbers\n 2 - Insert Dummy Shots and Interpolate");
                }
                shotOld = thisSHOT.shot;
//                findShot.start();
//                int row = thisSHOT.getSPIndex(sList); //shot info comes from sp
                int row  = spIndexMap.getIndex(thisSHOT.sourceLineNumber, thisSHOT.sourceStationNumber);
//                findShot.stop();
                if (row < 0) continue;
                SP sp = (SP) sList.get(row);
                if (thisSHOT.sourceLineNumber != sp.lineNumber || thisSHOT.sourceStationNumber != sp.stationNumber) {
                	System.err.println("Found wrong SP!");
                }
                Object v = sList.getValueAt(row, colName);
                if (colName.equalsIgnoreCase("kill")) {
                    v = thisSHOT.getKill();
                }
                if (v == null) v = 0;
//                float f = (float) SUtil.sval(v.toString());
                float f = (float) SUtil.getNumVal(v);
                fvals[i] = f;
            }
//            valLoop.stop();
            atr.setSize(fvals.length);
            atr.setVals(fvals);
            tg.getBias().removeBias(atr);
            fireProgressChanged(new ProgressEvent(this, "Shot Column: "+colName, counter++));
//            System.gc();
        }
        System.gc();
//        totalTimer.stop();
//        totalTimer.printTime();
//        getAttrTimer.printTime();
//        valLoop.printTime(); 
//        findShot.printTime();

        
        //...Build relation table
        fireProgressChanged(new ProgressEvent(this, "Building Relation Table", 1));
        Stopwatch sw = new Stopwatch("Build Relation Table");
//        Stopwatch shotLoop = new Stopwatch("Shot loop");
//        Stopwatch cableLoop = new Stopwatch("Cable loop");
//        Stopwatch channelLoop = new Stopwatch("Channel loop");
//        Stopwatch getMemory = new Stopwatch("Get Memory");
//        Stopwatch toAttribute = new Stopwatch("Save To Attribute");
        Stopwatch gc = new Stopwatch("Garbage Collect");
        sw.start();
//        getMemory.start();
        int nChans = mapper.maxChans;
        int nShots = oList.size();
        double progressInc = (cols.length + 0.0)/nShots;
        int progress = 0;
        float[] stations = new float[nShots*nChans];
        float[] lines = new float[nShots*nChans];
        float[] ffids = new float[nShots];
        System.out.println("stations size is:" + stations.length);
        System.out.println("lines size is:" + lines.length);
        
        sModel.addAttribute(CHAN, STATION);
        sModel.addAttribute(CHAN, LINE);
        sModel.addAttribute(SHOT, FFID);
        FocusDbAttr satr = sModel.getAttr(CHAN, STATION);
        FocusDbAttr latr = sModel.getAttr(CHAN, LINE);
        FocusDbAttr fatr = sModel.getAttr(SHOT, FFID);
//        getMemory.stop();
        for (int shot=0; shot<nShots; shot++) {
//            shotLoop.start();
            OBRecord ob = (OBRecord) oList.get(shot);
            ffids[shot] = ob.ffid;
            ArrayList<Integer> rLines = ob.getReceiverLineNumber();
            ArrayList<Integer> fromRecs = ob.getFromReceiver();
            ArrayList<Integer> toRecs = ob.getToReceiver();
            ArrayList<Integer> fromChans = ob.getFromChan();
            ArrayList<Integer> toChans   = ob.getToChan();
//            shotLoop.stop();
            for (int cable = 0; cable<rLines.size(); cable++) {
//                cableLoop.start();
                int line = rLines.get(cable);
                int fromRec = fromRecs.get(cable);
                int toRec = toRecs.get(cable);
                int fromChan = fromChans.get(cable);
                int toChan = toChans.get(cable);
                int inc = 1;
                if (toChan != fromChan) inc = (toRec - fromRec)/(toChan - fromChan);
//                int shotIndex = shot*nChans;
//                cableLoop.stop();
                for (int chan = fromChan; chan <= toChan; chan++) {
//                    channelLoop.start();
                    int rec = fromRec + (chan - fromChan)*inc;
                    if (rec > Math.max(toRec, fromRec) || rec < Math.min(toRec, fromRec)) {
                        throw new MapperToFocusException("Bad Relation for SHOT: " + (shot + firstSHOT)
                                + "!\nChan: "+ chan + " Line: " + line + " Station: " + rec);
                    }
//                    lines[shotIndex + chan - 1] = line;
//                    stations[shotIndex + chan - 1] = rec;
                    lines[shot*nChans + chan - 1] = line;
                    stations[shot*nChans + chan - 1] = rec;
//                    channelLoop.stop();
                }
            }
//            if (shot%2 == 0){
//                gc.start();
                progress = (int) (shot*progressInc);
                fireProgressChanged(new ProgressEvent(this, null, progress));
//                System.gc();
//                gc.stop();
//            }
        }
//        toAttribute.start();
        fatr.setSize(nShots);
        fatr.setVals(ffids);
        satr.setSize(nShots);
        satr.setVals(stations);
        latr.setSize(nShots);
        latr.setVals(lines);
//        toAttribute.stop();
        gc.start();
        System.gc();
        gc.stop();
        sw.stop();
        sw.printTime();
//        shotLoop.printTime();
//        cableLoop.printTime();
//        channelLoop.printTime();
//        getMemory.printTime();
//        toAttribute.printTime();
        gc.printTime();
        
        return sModel;
    }

    private void fireProgressChanged(ProgressEvent progressEvent)
    {
        for (ProgressListener l: progressListeners) {
            l.progressChanged(progressEvent);
        }
    }

    public void addProgressChangedListener(ProgressListener l)
    {
        progressListeners .add(l);
    }

    /*
     * Station.Map is a matrix Focus uses to calculate Rec-Stat based off
     * line/station information. TEXT.REGULAR LABEL is used to determine
     * how many rows and columns there are. Each row is a receiver line,
     * while each column is a station. The file also contains what
     * the minimum line and station numbers are and what the increment
     * is. Based on this information, each cell in the matrix is assigned
     * a receiver line and station number, the contents of which are
     * the Rec-Stat number. Unused line/station numbers are set to zero.
     * All of this assumes the receivers were laid out in a grid-like
     * fashion. If not, this matrix can get quite large (for instance,
     * if both line and station number go up by one for each receiver).
     */
    public FocusDbModel getStationMapModel()
    {
        FocusDbModel rMapModel = new FocusDbModel(MAP, tg);
        rMapModel.addAttribute(STATION, MAP);
        FocusDbAttr sm = rMapModel.getAttr(STATION, MAP);
        recLines = new ArrayList<Integer>();
        
        
        ReflectiveTableModel rList = mapper.receiverList;
//        lineInc = 1;
        stationInc = 0;
//        lineCount = 0;
        maxStnCount = 0;
        int stnCount = 0;
        
        //...Scan for receiver layout grid
        fireProgressChanged(new ProgressEvent(this, "Building Station Map", 1));
        int count = 0;
        Receiver rec = (Receiver) rList.get(0);
        lineMin = rec.lineNumber;
        lineMax = rec.lineNumber;
        stnMin = rec.stationNumber;
        int stnMax = rec.stationNumber;
        int prevLine = Integer.MIN_VALUE;
        int prevStn = 0;
        for(TableData td: rList) {
            rec = (Receiver) td;
            int rLine = rec.lineNumber;
            int rStn = rec.stationNumber;
            lineMin = Math.min(lineMin, rLine);
            lineMax = Math.max(lineMax, rLine);
            stnMin = Math.min(stnMin, rStn);
            stnMax = Math.max(stnMax, rStn);
            stnCount ++;
            if (rLine == prevLine)stationInc = rStn - prevStn;
            else {
//                lineInc = rLine - prevLine;
//                lineCount ++;
                recLines.add(rLine);
                maxStnCount = Math.max(maxStnCount, stnCount);
                stnCount = 0;
                prevLine = rLine;
            }
            prevStn = rStn;
            count++;
            if (count%10 == 0) fireProgressChanged(new ProgressEvent(this, null, count));
        }
        System.out.println("\nReceiver Layout Grid:");
        System.out.println("Min Line: " + lineMin + " Max Line: " + lineMax);
        System.out.println("Min Stn:  " + stnMin  + " Max Stn:  " + stnMax);
        System.out.println("Stn Inc:  " + stationInc);//  + " Line Inc: " + lineInc);
        System.out.println("Stn Cnt:  " + maxStnCount + " Line Cnt: " + recLines.size() + "\n");
        
        //...Process Receiver Layout Grid
//        int lineCalcCount = (lineMax - lineMin)/lineInc + 1;
        int stnCalcCount = (stnMax - stnMin)/stationInc + 1;
//        if (lineCalcCount != lineCount) {
//            System.out.println("MapperToFocusConverter.getStationMapModel() - Receiver Lines not layed out on regular grid. Setting lineInc = 1");
//            lineInc = 1;
//            lineCount = lineMax - lineMin + 1;
//        }
        if (stnCalcCount != maxStnCount) {
            System.out.println("MapperToFocusConverter.getStationMapModel() - Receiver Stations not layed out on regular grid. Setting stationInc = 1");
            stationInc = 1;
            maxStnCount = stnMax - stnMin + 1;
        }
        
        //...Load Rec-Stat values into station map
//        MemoryWatch memWatch = new MemoryWatch();
//        memWatch.print();
        float[] rMap = new float[maxStnCount*recLines.size()];
        System.out.println("Float Size is:  " + rMap.length);
//        memWatch.print();
        fireProgressChanged(new ProgressEvent(this, "Loading Station Map", 1));
        for (int i=0; i<rList.size(); i++) {
            rec = (Receiver) rList.get(i);
//            int lineIndex = (rec.lineNumber - lineMin)/lineInc;
            int lineIndex = recLines.indexOf(rec.lineNumber);
            int stnIndex = (rec.stationNumber - stnMin)/stationInc;
            rMap[lineIndex*maxStnCount + stnIndex] = i + 1;
            if (i%10 == 0) fireProgressChanged(new ProgressEvent(this, null, i));
        }
        
//        sm.setSize(lineCount);
        sm.setSize(recLines.size());
        sm.setVals(rMap);
//        memWatch.print();
        System.gc();
//        memWatch.print();
        return rMapModel;
    }

    /**
     * run getStationMapModel first.
     * @return
     */
    public String getRegularLabelFile()
    {
        int lineCount = lineMax - lineMin + 1;
        String f = "";
        String nLines    = SUtil.stringResize(lineCount+"",10);
        String firstLine = SUtil.stringResize(lineMin+"",10);
//        String lInc      = SUtil.stringResize(lineInc+"",10);
        String lInc      = SUtil.stringResize(1+"",10);
        String nRecs     = SUtil.stringResize(maxStnCount+"",10);
        String firstRec  = SUtil.stringResize(stnMin+"",10);
        String sInc      = SUtil.stringResize(stationInc+"",10);
        f = nLines + firstLine + lInc + nRecs + firstRec + sInc;
        return f;
    }

    public FocusCDPModel getCdpModel()
    {
        CdpModel model = mapper.getCdpModel();
        tg.getBias().removeBias(model);
        FocusCDPModel fmodel = new FocusCDPModel(model, tg);
        
//        tg.getBias().applyBias(model); //reapply so user can keep using Mapper.
        return fmodel;
    }
    
    public void applyBias(FocusCDPModel model)
    {
        tg.getBias().applyBias(model.getMapperCdpModel()); //reapply so user can keep using Mapper.
    }

    public FocusBias getBias()
    {
       return tg.getBias();
    }

    public FocusStatKey getMapStatkey()
    {
        FocusStatKey key = new FocusStatKey(line, recLines);
        return key;
    }

    public void convertReceiversToMapper(FocusBias focusBias, ArrayList<TableData> rlist, RcvrDbModel rModel)
    {
        int nRecs = rModel.getNlocs();
        FocusDbAttr xAttr = rModel.getX();
        FocusDbAttr yAttr = rModel.getY();
        FocusDbAttr lineAttr = rModel.getLine();
        FocusDbAttr stationAttr = rModel.getStation();
        FocusDbAttr zAttr = rModel.getZ();
        FocusDbAttr[] optRecAttrs = rModel.getOptionalAttributes(); 
        rlist.clear();
        
        fireProgressChanged(new ProgressMaxChanged(this, "Converting Receivers", nRecs));
        for (int i=0 ; i<nRecs; i++) {
            if (cancel ) return;
            Receiver r = new SurveyedReceiver();
            int rcols = r.getRequiredColumns().length;
            if (lineAttr != null) r.lineNumber = (int) lineAttr.getVal(i);
            if (stationAttr != null) r.stationNumber = (int) stationAttr.getVal(i);
            if (xAttr != null) r.x = xAttr.getVal(i);
            if (yAttr != null) r.y = yAttr.getVal(i);
            if (zAttr != null) r.z = zAttr.getVal(i);
            for (int j=0; optRecAttrs != null && j<optRecAttrs.length; j++) {
                FocusDbAttr optAttr = optRecAttrs[j];
                String attrName = processOptAttrName(optAttr);
                r.addOptionalColumn(attrName, Float.class);
//                r.setOptionalValue(rcols+j, optAttr.getVal(i));
                r.setValue(attrName, optAttr.getVal(i));
            }
            focusBias.applyBias(r);
            rlist.add(r);
            if (i%10 == 0) fireProgressChanged(new ProgressEvent(this, null, i));
        }
    }
    
    public void convertShotsToMapper(FocusBias focusBias, ArrayList<TableData> slist, ShotDbModel sModel,  ArrayList<TableData> oblist, OBRecord[] relationObs)
    {
        int nSps = sModel.getNlocs();
        FocusDbAttr xAttr = sModel.getX();
        FocusDbAttr yAttr = sModel.getY();
        FocusDbAttr lineAttr = sModel.getLine();
        FocusDbAttr stationAttr = sModel.getStation();
        FocusDbAttr zAttr = sModel.getZ();
        FocusDbAttr killAttr = sModel.getKill();
        FocusDbAttr ffidAttr = sModel.getFfid();
        FocusDbAttr[] optShotAttrs = sModel.getOptionalAttributes(); 
        slist.clear();
        oblist.clear();
        
        fireProgressChanged(new ProgressMaxChanged(this, "Converting Shots", nSps));
        for (int i=0 ; i<nSps; i++) {
            if (cancel) return;
            SP s = new SurveyedSP();
            int nreq = s.getRequiredColumns().length;
            OBRecord ob = new OBRecord();
            if (lineAttr != null) s.lineNumber = (int) lineAttr.getVal(i);
            if (stationAttr != null) s.stationNumber = (int) stationAttr.getVal(i);
            if (xAttr != null) s.x = xAttr.getVal(i);
            if (yAttr != null) s.y = yAttr.getVal(i);
            if (zAttr != null) s.z = zAttr.getVal(i);
            ob.shot = i+sModel.getFirstID();
            ob.sourceLineNumber = s.lineNumber;
            ob.sourceStationNumber = s.stationNumber;
            if (ffidAttr != null) ob.ffid = (int) ffidAttr.getVal(i);
            sModel.setRelation(ob, i);
            for (int j=0; optShotAttrs != null && j<optShotAttrs.length; j++) {
                FocusDbAttr optAttr = optShotAttrs[j];
                String attrName = processOptAttrName(optAttr);
                s.addOptionalColumn(attrName, Float.class);
//                s.setOptionalValue(j+nreq, optAttr.getVal(i));
              //  s.setOptionalValue(attrName, optAttr.getVal(i));
                s.setValue(attrName, optAttr.getVal(i));
            }
            if (killAttr != null)
                ob.setKillInt((int)killAttr.getVal(i));
            focusBias.applyBias(s);
            slist.add(s);
            oblist.add(ob);
            if (i%10 == 0) fireProgressChanged(new ProgressEvent(this, null, i));
        }
    }

    private String processOptAttrName(FocusDbAttr optAttr)
    {
        String event = optAttr.getEvent();
        String attr = optAttr.getAttribute();
        if (event.equals(attr)) {
            if (event.equals("KILL")) return "Kill";
            if (event.equals("UNSURVEY")) return "UnSurveyed";
            if (event.equals("UNUSED")) return "UnSurveyed";
            if (event.equals("DUPLICAT")) return "Duplicate";
            if (event.equals("USEDBYSH")) return "UsedByShot";
            if (event.equals("DISTANCE")) return "Distance";
            if (event.equals("SURVEY")) return "Survey";
        }
        String attrName = event + ":" + attr;
        return attrName;
    }

    public void cancelJob()
    {
        cancel = true;
    }

}
