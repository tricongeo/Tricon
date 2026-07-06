package com.tricongeophysics;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Arrays;

public class CalculateScatterJob extends Job
{

    private static final int SizeGrow = 50000;
    private ArrayList<TableData> obs;
    private ArrayList<TableData> shots;
    private ArrayList<Receiver> receivers;
    private Point2D.Double[] scatter;
    private boolean cancel;
    private Stopwatch loadOb = new Stopwatch("load ob");
    private Stopwatch loadrec = new Stopwatch("load rec");
    private Stopwatch loadshot = new Stopwatch("load shot");
    private Stopwatch catscatter = new Stopwatch("cat scatter");
    private boolean debugTimer = false;
    private int scatterInc;
    
    public CalculateScatterJob(ArrayList<Receiver> receivers, ArrayList<TableData> shots, ArrayList<TableData> obs, int scatterInc)
    {
        this.receivers = receivers;
        this.shots = shots;
        this.obs = obs;
        this.scatterInc = scatterInc;
        if (scatterInc<1) scatterInc = 1;
    }

    @Override
    public void cancel()
    {
        cancel = true;
    }

    @Override
    protected void doJob()
    {
        if (receivers.isEmpty() || shots.isEmpty() || obs.isEmpty()) return;
        ShotRecord sr = new ShotRecord();
        int counter = 0;
        int totalTraces = 0;
        scatter = new Point2D.Double[SizeGrow];
        for (TableData td: obs) {
            if (cancel) {
                this.fireJobFinished();
                return;
            }
            counter++;
            if (counter%scatterInc == 0) {
                if (debugTimer) loadOb.start();
                OBRecord obr = (OBRecord) td;
                sr.loadOB(obr, false);
                if (debugTimer) loadOb.stop();
                if (debugTimer) loadrec.start();
                sr.loadReceiverXY(receivers);
                if (debugTimer) loadrec.stop();
                if (debugTimer) loadshot.start();
                sr.loadShotXY(shots);
                if (debugTimer) loadshot.stop();
                Point2D.Double[] thisScatter = sr.getScatter();
                totalTraces += thisScatter.length;
                if (debugTimer) catscatter.start();
                if (totalTraces > scatter.length) {
                    if(!growScatter())
                        return;
                }
                //scatter = SUtil.arrayCat(scatter, thisScatter);
                System.arraycopy(thisScatter, 0, scatter, totalTraces - thisScatter.length, thisScatter.length);
                if (debugTimer) catscatter.stop();
                fireProgressChanged(new ProgressEvent(this, null, counter));
            }
        }
        if (debugTimer) printTimers();
    }

    private boolean growScatter()
    {
        try {
        scatter = Arrays.copyOf(scatter, scatter.length + SizeGrow, scatter.getClass());
        return true;
        } catch (java.lang.OutOfMemoryError e) {
            e.printStackTrace();
            return false;
        }
    }

    private void printTimers()
    {
        loadOb.printTime();
        loadrec.printTime();
        loadshot.printTime();
        catscatter.printTime();
    }

    @Override
    public int getProgressMax()
    {
        return obs.size();
    }

    @Override
    public boolean getIndeterminate()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    public Point2D.Double[] getScatter()
    {
        return scatter;
    }

}
