package com.tricongeophysics;

import java.util.ArrayList;

public class SpIndexMap
{
    private ArrayList<Integer> lineNumbers;
    private ArrayList<Integer> minSta;
    private ArrayList<Integer> maxSta;
    private ArrayList<Integer> firstIndex;

    public SpIndexMap(ReflectiveTableModel sList)
    {
        initialize(sList);
    }

    /**
     * initialize index. Assumes sList is sorted.
     * @param sList
     */
    private void initialize(ReflectiveTableModel sList)
    {
        lineNumbers = new ArrayList<Integer>();
        minSta = new ArrayList<Integer>();
        maxSta = new ArrayList<Integer>();
        firstIndex = new ArrayList<Integer>();
        
        int oldLine = -1;
        int oldStation = -1;
        for(int i=0; i<sList.size(); i++) {
            SP sp = (SP) sList.get(i);
            int line = sp.lineNumber;
            int station = sp.stationNumber;
            int stnInc = station - oldStation;
            if (line != oldLine || stnInc != 1) {
                lineNumbers.add(line);
                minSta.add(station);
                firstIndex.add(i);
                if (oldLine != -1) maxSta.add(oldStation);
            }
            oldLine = line;
            oldStation = station;
        }
        maxSta.add(oldStation);
    }

    public int getIndex(int line, int station)
    {
        for (int i=0; i<lineNumbers.size(); i++) {
            int thisLine = lineNumbers.get(i);
            if (thisLine == line) {
                int min = minSta.get(i);
                int max = maxSta.get(i);
                if (station <= max && station >= min) {
                    int index0 = firstIndex.get(i);
                    return index0 + station - min;
                }
            }
        }
        System.err.println("Failed to find SP for Line: " + line + " Station: " + station);
        return -1;
    }

}