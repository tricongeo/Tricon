package com.tricongeophysics;

import java.util.ArrayList;
import java.util.Collections;

//import com.Sts.Types.PreStack.StsGatherData;
//import com.tricongeophysics.ParadigmDataSetIO;


public class Gather
{
    private DataSet dataSet;
    private ArrayList<SeismicTrace> traces;
    boolean gatherDebug = false;
    public static float INITIALMAX = 0 - Float.MAX_VALUE;  //initialize to wrong number
    public static float INITIALMIN = Float.MAX_VALUE;      //initialize to wrong number
    private float maxVal = INITIALMAX;
    private float minVal = INITIALMIN;

    public Gather(DataSet ds)
    {
        dataSet = ds;
        traces = new ArrayList<SeismicTrace>();
    }

    public int getHeaderIndex(String headerName)
    {
        int i = 0;
        for (i = 0; i < traces.get(0).getHeaderList().length; i++)
        {
            if (headerName.equals(traces.get(0).getHeaderList()[i].trim()))
            {
                return i;
            }
        }
        return -1;
    }

    public void sortTraces(String headerName)
    {
        SeismicTrace.sortKeyIndex = getHeaderIndex(headerName);
        if (SeismicTrace.sortKeyIndex >= 0)
        {
            Collections.sort(traces);
        }
        else
        {
            System.err.println("Gather: couldn't find header name " + headerName + " for sorting!!");
        }
    }

    public ArrayList<SeismicTrace> getTraces()
    {
        return traces;
    }

    public void setTraces(ArrayList<SeismicTrace> traces)
    {
        this.traces = traces;
    }

    public void addTrace(SeismicTrace trace)
    {
        if (trace != null)
        {
            SeismicTrace newTrace = new SeismicTrace(trace);
            traces.add(newTrace);
        }
    }

    public double getPkey()
    {
        return traces.get(0).getHeaders()[dataSet.getPkeyIndex()];
    }

    public String getPkeyName()
    {
        return traces.get(0).getHeaderList()[dataSet.getPkeyIndex()].trim();  //added trim to get rid of extra white space
    }

    public String printHeaders()
    {
        String hdrFormat = "Header[%03d]:, %10s,";
        String headers = "";
        String thisHeader = "";
        double headerVal = 0.0;
        String headerName = "";

        for (int hdrnum = 0; hdrnum < traces.get(0).getHeaderList().length; hdrnum++)
        {
            headerName = traces.get(0).getHeaderList()[hdrnum];
            thisHeader = String.format(hdrFormat, hdrnum, headerName.trim());
            for (int trnum = 0; trnum < traces.size(); trnum++)
            {
                headerVal = traces.get(trnum).getHeaders()[hdrnum];
                thisHeader = thisHeader + String.format(" %16.4f,", headerVal);
            }
            thisHeader = thisHeader.replace('\0', ' ');
            thisHeader = thisHeader + "\n";
            ;
            headers = headers + thisHeader;
        }
        return "\n" + headers;
    }

    public String printTraceData()
    {
        String sliceFormat = "Sample[%03d]:, %8dms,";
        String slices = "";
        String thisSlice = "";
        float sampleVal = 0.0f;
        int time = 0;

        for (int sampNum = 0; sampNum < traces.get(0).getData().length; sampNum++)
        {
            time = sampNum * (int) (dataSet.getSampleRate() / 1000.0);
            thisSlice = String.format(sliceFormat, sampNum, time);
            for (int trnum = 0; trnum < traces.size(); trnum++)
            {
                sampleVal = traces.get(trnum).getData()[sampNum];
                thisSlice = thisSlice + String.format(" %16.8e,", sampleVal);
            }
            thisSlice = thisSlice.replace('\0', ' ');
            thisSlice = thisSlice + "\n";
            ;
            slices = slices + thisSlice;
        }
        return "\n" + slices;
    }

    /** returns gather as single dimensioned array of floats instead of SeismicTrace objects */
    public float[] getTracesFloat()
    {
        if (traces.size() == 0) return null;  //end of dataset reached, so empty gather passed here

        float[] tracesFloat = new float[traces.size() * traces.get(0).getData().length];
        int samplenum = 0;
        for (int i = 0; i < traces.size(); i++)
        {
            for (int j = 0; j < traces.get(0).getData().length; j++)
            {
                tracesFloat[samplenum] = traces.get(i).getData()[j];
                samplenum++;
            }
        }
        return tracesFloat;
    }

    public double[] getHeadersDouble()
    {
        if (traces.size() == 0) return null;  //end of dataset reached, so empty gather passed here

        double[] headersDouble = new double[traces.size() * traces.get(0).getHeaders().length];
        int samplenum = 0;
        for (int i = 0; i < traces.size(); i++)
        {
            for (int j = 0; j < traces.get(0).getHeaders().length; j++)
            {
                headersDouble[samplenum] = traces.get(i).getHeaders()[j];
                samplenum++;
            }
        }
        return headersDouble;
    }

    public String toString()
    {
        String string = "\n";
        string += "gather: " + getPkeyName() + " " + getGatherId() + "\n";
        string += "total traces = " + traces.size() + "\n";
        string += traces.get(0).toString();
        return string;
    }

    public String toStringLong()
    {
        String string = "";
        string += this.printHeaders();
        string += this.printTraceData();
        return "\n" + string;
    }

    public double getGatherId()
    {
        if (traces != null && traces.size() > 0)
        {
            return traces.get(0).getHeaders()[getHeaderIndex(getPkeyName())];
        }
        else
        {
            return 0.0;
        }
    }

    /** method to see if headers contain zeroes for both cdp-x and cdp-y */
    public boolean zeroCDPXY()
    {
//		int indexX = traces.get(0).getIndexOfHeader(ParadigmDataSetIO.CDPX_HEADER);
//		int indexY = traces.get(0).getIndexOfHeader(ParadigmDataSetIO.CDPY_HEADER);
//		for (int i=0;i<traces.size();i++) {
//			if (traces.get(i).getHeaders()[indexX] == 0.0) {
//				return true;
//			}
//			if (traces.get(i).getHeaders()[indexY] == 0.0) {
//				return true;
//			}
//		}
        double x = traces.get(0).getHeaderValue(DataSet.CDPX_HEADER);
        double y = traces.get(0).getHeaderValue(DataSet.CDPY_HEADER);
        if (x == 0.0 && y == 0.0)
        {
            if (gatherDebug) {System.out.println("found zero x/y cdp:" + getCDP());}
            return true;
        }
        return false;
    }

    /** method to set cdp-x and cdp-y using CDPGrid in case they were left as 0 */
    public void fixCDPXY(CDPGrid grid)
    {
        double[] xys = grid.calcXYFromCDP(this.getCDP());
        double x = xys[0];
        double y = xys[1];
        int indexX = traces.get(0).getIndexOfHeader(DataSet.CDPX_HEADER);
        int indexY = traces.get(0).getIndexOfHeader(DataSet.CDPY_HEADER);
        for (int i = 0; i < traces.size(); i++)
        {
            traces.get(i).getHeaders()[indexX] = x;
            traces.get(i).getHeaders()[indexY] = y;
        }
    }

    public int getCDP()
    {
        return (int) traces.get(0).getHeaderValue(DataSet.CDP_HEADER);
    }

    private void findMinMaxVals()
    {
        maxVal = Gather.INITIALMAX;      //initialize to wrong number
        minVal = Gather.INITIALMIN;      //initialize to wrong number
        for (int i = 0; i < traces.size(); i++)
        {
            if (traces.get(i).getMaxVal() > maxVal) maxVal = traces.get(i).getMaxVal();
            if (traces.get(i).getMinVal() < minVal) minVal = traces.get(i).getMinVal();
        }
    }

    public float getMaxVal()
    {
        if (maxVal == INITIALMAX)
        {
            findMinMaxVals();
        }
        return maxVal;
    }

    public float getMinVal()
    {
        if (minVal == INITIALMIN)
        {
            findMinMaxVals();
        }
        return minVal;
    }

    public void clear()
    {
        traces.clear();
        maxVal = INITIALMAX;
        minVal = INITIALMIN;
    }

}
