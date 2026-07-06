package com.tricongeophysics;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.tricongeophysics.TableData.ColumnType;

public class Station extends AbstractTableData
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected int lineNumber;
    protected int stationNumber;
    protected double x = 0.0;
    protected double y = 0.0;
    protected double z = 0.0;
    // protected TriconFile file;
    String fileName = null;
    String dirName = null;
    protected boolean unUsed = false;
    protected boolean unSurveyed = true;
    protected boolean duplicate = false;
    protected boolean kill = false;
    protected int usedByShot = 0;
    protected int survey = 0;
    protected double distance = 0;

    protected static int MAX_FILENAME_LENGTH = 20;
    protected static DecimalFormat df2;
    protected static DecimalFormat df1;
    protected static int numFiles = 0;
    // public static final String[] variableList = { "X-Coordinate",
    // "Y-Coordinate", "Elevation", "Line Number", "Station Number"};
    public static final String[] RequiredColumns = { "Line", "Station", "X", "Y", "Z", "Kill", "UnSurveyed", "UnUsed", "Duplicate", "UsedByShot", "Distance", "Survey" };
    private static final ColumnType[] ColumnTypes = {ColumnType.Standard, ColumnType.Standard, ColumnType.Standard, ColumnType.Standard, ColumnType.Standard,
        ColumnType.Warning, ColumnType.Warning, ColumnType.Warning, ColumnType.Error, ColumnType.Standard, ColumnType.Standard, ColumnType.Standard};
    private boolean[] editableCols = { true, true, true, true, true, true, false, false, false, false, false, true };
    private transient ArrayList<RowColorChangedListener> rowColorChangedListeners = new ArrayList<RowColorChangedListener>();

    {
        init();
    }

    public Station()
    {
        // stationCount++;
        // file = new TriconFile("");
    }

    public void init()
    {
        df2 = new java.text.DecimalFormat("####0.00");
        df1 = new java.text.DecimalFormat("####0.0");
    }

    static void setNumFiles(int n)
    {
        if (n > 0)
            numFiles = n;
    }

    static int getNumFiles()
    {
        return numFiles;
    }

    /*
     * public double getSelectedValue(int variableIndex){ switch(variableIndex) { case Z: return getZ(); case X: return getX(); case Y: return getY(); case LINE_NUMBER: return
     * getLineNumber(); case STATION_NUMBER: return getStationNumber(); } return 0.0; }
     */

    public int getLineNumber()
    {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber)
    {
        this.lineNumber = lineNumber;
    }

    public int getStationNumber()
    {
        return stationNumber;
    }

    public void setStationNumber(int stationNumber)
    {
        this.stationNumber = stationNumber;
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public double getZ()
    {
        return z;
    }

    public void setZ(double z)
    {
        this.z = z;
    }

    public void setFile(String string)
    {
        TriconFile tf = new TriconFile(string);
        fileName = tf.getShortFileName(MAX_FILENAME_LENGTH);
        dirName = tf.getShortDirectoryName(MAX_FILENAME_LENGTH);
    }

    /*
     * public TriconFile getFile(){ return this.file; }
     */
    public String toStringLong()
    {
        return "Line: " + lineNumber + "<br>" + "Station: " + stationNumber + "<br>" + "x: " + df2.format(x) + "<br>" + "y: " + df2.format(y) + "<br>" + "z: " + df2.format(z)
                + "<br>" + "Input File:" + "<br>" + dirName + "/" + "<br>" + fileName + "</html>";
    }

    public boolean equals(Object o)
    {
        if (o == null) return false;
        Station s = (Station) o;
        if (this.getLineNumber() == s.getLineNumber())
            if (this.getStationNumber() == s.getStationNumber())
                return true;
        return false;
    }

    public int hashCode()
    {
        return (Integer.toString(this.getLineNumber())).hashCode();
    }

    public String toString()
    {
        if (df2 == null)
            init();
        return "Line: " + lineNumber + "<br>" + "Station: " + stationNumber + "<br>" + "x: " + df2.format(x) + "<br>" + "y: " + df2.format(y) + "<br>" + "z: " + df2.format(z)
                + "</html>";
    }

    public String toSegp1()
    {
        // ...Converts to SEG-P1 format, without leading S/R
        if (df2 == null || df1 == null)
            init();
        String xx = df1.format(x);
        String yy = df1.format(y);
        String ll = lineNumber + "";
        String ss = stationNumber + "";
        String zz = df2.format(z);
        String kk = SUtil.getNumVal(kill) + "";
        String us = SUtil.getNumVal(unSurveyed) + "";
        String su = survey + "";
        String blank = "                    ";

        // ...Resize Strings to proper length
        ll = SUtil.stringResize(ll, 8);
        ss = SUtil.stringResize(ss, 16);
        xx = SUtil.stringResize(xx, 10);
        yy = SUtil.stringResize(yy, 10);
        zz = SUtil.stringResize(zz, 7);
        kk = SUtil.stringResize(kk, 6);
        us = SUtil.stringResize(us, 11);
        su = SUtil.stringResize(su, 7);

        // ...Return formatted String
        String out = ll + ss + blank + xx + yy + zz + kk + us + su;
        if (optionalData != null) {
            for (Object o : optionalData) {
                if (o == null) continue; //shouldn't happen!! but just in case
                String s = o.toString();
                if (o instanceof Number)
                    s = df2.format(o);
                out += SUtil.stringResize(s, 10);
            }
        }
        return out + "\n";
    }

    @Override
    public String[] getRequiredColumns()
    {
        return RequiredColumns;
    }

    public int getUsedByShot()
    {
        return usedByShot;
    }

    public void setUsedByShot(int usedByShot)
    {
        this.usedByShot = usedByShot;
    }

    public String getSegp1Header()
    {
        String header = "H    Line         Station                       Easting  Northing   Elev  Kill unSurveyed Survey";
        String[] optionalNames = getOptionalColumnNames();
        if (optionalNames == null)
            return header + "\n";
        for (String name : optionalNames) {
            header += SUtil.stringResize(name, 10);
        }
        return header + "\n";
    }

    /**
     * this is dummy method. If this a surveyed station, use SurveyedReceiver or SurveyedSP classes
     * 
     * @param unSurveyed
     */
    public void setUnSurveyed(boolean unSurveyed)
    {
    }

    public double getDistance()
    {
        return distance;
    }

    public void setDistance(double distance)
    {
        this.distance = distance;
    }

    /*
     * always returns true. If this a surveyed station, use SurveyedReceiver or SurveyedSP classes
     */
    public boolean getUnSurveyed()
    {
        return unSurveyed;
    }

    public int getSurvey()
    {
        return survey;
    }

    public void setSurvey(int survey)
    {
        this.survey = survey;
    }

    public void setUnUsed(boolean unUsed)
    {
        this.unUsed = unUsed;
    }

    public boolean getUnUsed()
    {
        return unUsed;
    }

    public void setDuplicate(boolean duplicate)
    {
        this.duplicate = duplicate;
    }

    public boolean getDuplicate()
    {
        return duplicate;
    }

    @Override
    public boolean[] getEditableCols()
    {
        return this.editableCols;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getDirName()
    {
        return dirName;
    }

    public void setDirName(String dirName)
    {
        this.dirName = dirName;
    }
    
    public Station getCopy() {
        Class<? extends Station> sClass = this.getClass();
        Station station = null;
        try {
            station = sClass.newInstance();
            station.lineNumber = this.lineNumber;
            station.stationNumber = this.stationNumber;
            station.x = this.x;
            station.y = this.y;
            station.z = this.z;
            station.fileName = this.fileName;
            station.dirName = this.dirName;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return station;
    }

    public boolean getKill()
    {
        return kill;
    }

    public void setKill(boolean kill)
    {
        this.kill = kill;
        fireRowColorChanged();
    }

    @Override
    public int compareTo(TableData o)
    {
        if (o == null) return 0;
        Station s = (Station)o;
        if (lineNumber > s.lineNumber) return 1;
        if (lineNumber < s.lineNumber) return -1;
        if( stationNumber > s.stationNumber) return 1;
        if( stationNumber < s.stationNumber) return -11;
        return 0;
    }

    @Override
    public boolean containsError()
    {
        return (this.duplicate);
    }

    private void fireRowColorChanged()
    {
        for (RowColorChangedListener l: getRowColorChangedListeners()) {
            l.rowColorChanged();
        }
    }

    private  ArrayList<RowColorChangedListener> getRowColorChangedListeners()
    {
        if(rowColorChangedListeners == null) rowColorChangedListeners = new ArrayList<RowColorChangedListener>();
        return rowColorChangedListeners;
    }

    @Override
    public boolean containsWarning()
    {
        return (this.unSurveyed || this.unUsed || this.kill);
    }

    @Override
    public ColumnType[] getColumnTypes()
    {
        return ColumnTypes;
    }
    /*
    @Override
    public boolean rowChanged() {
        boolean changed = (kill == oldKill);
        oldKill = kill;
        return changed;
    }
    */
    
    @Override
    public void addRowColorChangedListener(RowColorChangedListener l)
    {
        getRowColorChangedListeners().add(l);
    }
    
    public int getLine() {
        return lineNumber;
    }
    
    public void setLine(int line) {
        this.lineNumber = line;
    }
    
    public int getStation() {
        return stationNumber;
    }
    
    public void setStation(int station) {
        this.stationNumber = station;
    }

    /**
     * Interpolate new x, y, z, etc. based on line and station number.
     * Assumes stations array already sorted!!
     * @param stations - array of stations (must be sorted by line/station)
     * @param station - station to be interpolated
     * @param i - index of station in array
     */
    public static void interp(ArrayList<Station> stations, Station station, int i)
    {
        if (stations == null || stations.size() == 0 || station == null) return;
        if (i >= stations.size() || i < 0) return;
        Station s0=null; //previous station;
        Station s1=null; //following station;
        int line = station.lineNumber;
        int length = stations.size();
        int i0=0;
        int i1=0;

        //first, find the closest surrounding stations that have survey
        for (int j=i-1; j>=0; j--) {
            s0 = stations.get(j);
            i0=j;
            if (!s0.unSurveyed && !s0.duplicate) break;
        }

        for (int j=i+1; j<length; j++) {
            s1 = stations.get(j);
            i1=j;
            if (!s1.unSurveyed) break;
        }

        //check to make sure same line number (otherwise, we can't use them)
        if (s0 != null && (s0.lineNumber != line || s0.unSurveyed)) s0=null;
        if (s1 != null && (s1.lineNumber != line || s1.unSurveyed)) s1=null;
        
        //if surrounding stations didn't work, we'll try 2 stations to either side of it (extrapolation case)
        if (s0 == null) {
            s0 = s1;
            s1=null;
            for (int j=i1+1; j<length; j++) {
                s1 = stations.get(j);
                if (!s1.unSurveyed) break;
            }
        }
        if (s1 == null) {
            s1 = s0;
            s0=null;
            for (int j=i0-1; j>=0; j--) {
                s0 = stations.get(j);
                if (!s0.unSurveyed && !s0.duplicate) break;
            }
        }
        
        //again, check to make sure we're on the same line...
        if (s0 != null && (s0.lineNumber != line || s0.unSurveyed)) s0=null;
        if (s1 != null && (s1.lineNumber != line || s1.unSurveyed)) s1=null;
        
        //hopefully we now have 2 stations for interpolation/extrapolation
        interp(station, s0, s1);
    }

    /**
     * interpolates station based on stations s0 and s1.
     * 
     * @param station
     * @param s0
     * @param s1
     */
    private static void interp(Station station, Station s0, Station s1)
    {
        if (station == null || s0 == null || s1 == null) return;//this is a singleton station, can't interpolate!!
        int line = station.lineNumber;
        if (s0.lineNumber != line || s1.lineNumber != line) return;
        int sDel = station.stationNumber - s0.stationNumber;
        int diff = s1.stationNumber - s0.stationNumber;
        double newX = s0.x + sDel*(s1.x - s0.x)/diff;
        double newY = s0.y + sDel*(s1.y - s0.y)/diff;
        double newZ = s0.z + sDel*(s1.z - s0.z)/diff;
        int newSurvey = s0.survey + sDel*(s1.survey - s0.survey)/diff;
        station.x = newX;
        station.y = newY;
        station.z = newZ;
        station.survey = newSurvey;
    }
}
