package com.tricongeophysics;

import java.util.ArrayList;

/**
 * Creates an index so that SP can quickly be found using Line and Station values.
 * Assumes Line/Station are in a grid. Will be fast if lots of SP per Shot Line,
 * but slow if every station has its own line number.
 * 
 * @author scott
 *
 */
public class SpIndexMap2
{

    private ArrayList<Integer> lineNumbers;
    private int minSta;
    private int maxSta;
    private int[][] indexMap;

    public SpIndexMap2(ReflectiveTableModel sList)
    {
        initialize(sList);
        loadIndices(sList);
    }

    /**
     * creates 2D array of SP indices. Size is
     * #lines X (maxStation - minStation + 1).
     * Many indices will be blank, but no problem
     * with memory issues so far, so won't resort
     * to using Sparse Matrix
     * @param sList
     */
    private void loadIndices(ReflectiveTableModel sList)
    {
        if (lineNumbers == null) return;
        int stationRange = maxSta - minSta + 1;
        indexMap = new int[lineNumbers.size()][stationRange];
        for (int i=0; i<sList.size(); i++) {
            SP sp = (SP) sList.get(i);
            int line = sp.lineNumber;
            int station = sp.stationNumber;
            int lineIndex = lineNumbers.indexOf(line); 
            int stationIndex = station - minSta;
            indexMap[lineIndex][stationIndex] = i;
        }
    }

    /**
     * initialize index. Assumes sList is sorted by line number.
     * 
     * @param sList
     */
    private void initialize(ReflectiveTableModel sList)
    {
        if (sList == null || sList.size() < 1) return;
        lineNumbers = new ArrayList<Integer>();
        SP sp = (SP) sList.get(0);
        minSta = sp.stationNumber;
        maxSta = sp.stationNumber;
        int oldLine = sp.lineNumber;
        lineNumbers.add(oldLine);
        for(int i=1; i<sList.size(); i++) {
            sp = (SP) sList.get(i);
            int line = sp.lineNumber;
            int station = sp.stationNumber;
            minSta = Math.min(minSta, station);
            maxSta = Math.max(maxSta, station);
            if (line != oldLine ) {
                lineNumbers.add(line);
            }
            oldLine = line;
        }
    }

    public int getIndex(int line, int station)
    {
        int lineIndex = lineNumbers.indexOf(line); 
        int stationIndex = station - minSta;
        if (lineIndex >= 0 && stationIndex >= 0)
            return indexMap[lineIndex][stationIndex];
        System.err.println("Failed to find SP for Line: " + line + " Station: " + station);
        return -1;
    }

}