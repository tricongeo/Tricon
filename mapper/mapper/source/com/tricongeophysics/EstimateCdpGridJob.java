package com.tricongeophysics;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

public class EstimateCdpGridJob extends Job
{
   
    private static final int AngleSkip = 5;
    private ArrayList<TableData> receivers;
    private double angle;
    private CdpModel cdpModel;
    private ArrayList<TableData> shotpoints;
    private int buffer;
    private CdpPoint matchPoint;

    public EstimateCdpGridJob(ArrayList<TableData> receivers, ArrayList<TableData> shotpoints, CdpModel cdpModel)
    {
        this.receivers = receivers;
        this.shotpoints = shotpoints;
        this.cdpModel = cdpModel;
        buffer = cdpModel.getBuffer();
    }

    @Override
    public void cancel()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int getProgressMax()
    {
        // TODO Auto-generated method stub
        return 3;
    }

    @Override
    protected void doJob()
    {
        if (receivers == null) {
            JOptionPane.showMessageDialog(null, "Receivers are required to create CDP Grid!", "Insufficient Data", JOptionPane.ERROR_MESSAGE, null);
            return;
        }
        matchPoint = cdpModel.getMatchPoint();
        if (matchPoint == null) {
            findAngle();
            findOrigin();
            nudgeOrigin();
        }
        else {
            findMatchOrigin();
            //nudgeOrigin();
        }
    }

    private void findMatchOrigin()
    {
        if (receivers == null) return;
        if (receivers.size() < 2) return;
        
        Station s1 = (Station) receivers.get(0);
        int i=0;
        while(s1.kill) s1 = (Station) receivers.get(i++); //find first non-killed station
        
        //... Just set up some dummy initial coordinates
        cdpModel.setOriginX(matchPoint.x);
        cdpModel.setOriginY(matchPoint.y);
        cdpModel.setOriginIL(matchPoint.il);
        cdpModel.setOriginXL(matchPoint.xl);

        //... Get min/max fake inline/xline numbers
        CdpPoint p = cdpModel.getCdpPointFromXY(s1.x, s1.y);
        int minInlineZ = p.il;
        int minXlineZ = p.xl;
        int maxInlineZ = p.il;
        int maxXlineZ = p.xl;
        
        for (TableData td: receivers) {
            s1 = (Station) td;
            if(s1.kill) continue; //don't use killed stations!
            p = cdpModel.getCdpPointFromXY(s1.x, s1.y);
            minInlineZ = Math.min(minInlineZ, p.il);
            minXlineZ = Math.min(minXlineZ, p.xl);
            maxInlineZ = Math.max(maxInlineZ, p.il);
            maxXlineZ = Math.max(maxXlineZ, p.xl);
        }

        for (TableData td: shotpoints) {
            s1 = (Station) td;
            if(s1.kill) continue; //don't use killed stations!
            p = cdpModel.getCdpPointFromXY(s1.x, s1.y);
            minInlineZ = Math.min(minInlineZ, p.il);
            minXlineZ = Math.min(minXlineZ, p.xl);
            maxInlineZ = Math.max(maxInlineZ, p.il);
            maxXlineZ = Math.max(maxXlineZ, p.xl);
        }
        
        //... get true origin
        p = cdpModel.getCdpPoint(minInlineZ - buffer, minXlineZ - buffer); //true origin point
        cdpModel.setOriginX(p.x);
        cdpModel.setOriginY(p.y);
        cdpModel.setOriginIL(p.il);
        cdpModel.setOriginXL(p.xl);
        
        //...set Number Inlines and Xlines
        int nInlines = (maxInlineZ - minInlineZ)/cdpModel.ilIncrement + 2 + 2*buffer;
        int nXlines  = (maxXlineZ  - minXlineZ )/cdpModel.xlIncrement + 2 + 2*buffer;
        cdpModel.setNumInlines(nInlines);
        cdpModel.setNumXlines(nXlines);
    }

    /**
     * nudge origin as necessary to get the majority of the stations to land
     * on grid corners
     */
    private void nudgeOrigin()
    {
        if (receivers == null) return;
        if (receivers.size() < 2) return;
        
        double[] xShifts = new double[receivers.size()/AngleSkip];
        double[] yShifts = new double[receivers.size()/AngleSkip];
        
        for (int i=0; i<receivers.size()/AngleSkip; i++) {
            Station s1 = (Station) receivers.get(i*AngleSkip);
            if(s1.kill) continue; //don't use killed stations!
            
            CdpPoint p1 = cdpModel.getCdpPointFromXY(s1.x, s1.y); //find nearest CDP to station
            CdpPoint p2 = cdpModel.getCdpPoint(p1.il + 0.5, p1.xl + 0.5); //find xy of upper right corner of cdp
            
            xShifts[i] = p2.x - p1.x; // shift necessary to put corner on top of station
            yShifts[i] = p2.y - p1.y; // shift necessary to put corner on top of station
        }
        
        double xShift = SUtil.median(xShifts);
        double yShift = SUtil.median(yShifts);
        Arrays.sort(yShifts);
        
        cdpModel.setOriginX(cdpModel.originX + xShift);
        cdpModel.setOriginY(cdpModel.originY + yShift);
    }

    /**
     * estimate origin by first assigning a fake origin with correct angle.
     * Using this origin, we can loop through all shots and receivers to
     * find the minimum and maximum inline/xline used by the fake system.
     * We then find the x/y of the minimum inline/xline. This will be
     * close to the true origin (within one cdp of the correct origin,
     * slight shifting might be necessary to get the scatter to land in the center).
     */
    private void findOrigin()
    {
        if (receivers == null) return;
        if (receivers.size() < 2) return;
        
        Station s1 = (Station) receivers.get(0);
        int i=0;
        while(s1.kill) s1 = (Station) receivers.get(i++); //find first non-killed station
        
        //... Just set up some dummy initial coordinates
        cdpModel.setOriginX(s1.x);
        cdpModel.setOriginY(s1.y);

        //... Get min/max fake inline/xline numbers
        CdpPoint p = cdpModel.getCdpPointFromXY(s1.x, s1.y);
        int minInlineZ = p.il;
        int minXlineZ = p.xl;
        int maxInlineZ = p.il;
        int maxXlineZ = p.xl;
        
        for (TableData td: receivers) {
            s1 = (Station) td;
            if(s1.kill) continue; //don't use killed stations!
            p = cdpModel.getCdpPointFromXY(s1.x, s1.y);
            minInlineZ = Math.min(minInlineZ, p.il);
            minXlineZ = Math.min(minXlineZ, p.xl);
            maxInlineZ = Math.max(maxInlineZ, p.il);
            maxXlineZ = Math.max(maxXlineZ, p.xl);
        }

        if (shotpoints != null) {
            for (TableData td: shotpoints) {
                s1 = (Station) td;
                if(s1.kill) continue; //don't use killed stations!
                p = cdpModel.getCdpPointFromXY(s1.x, s1.y);
                minInlineZ = Math.min(minInlineZ, p.il);
                minXlineZ = Math.min(minXlineZ, p.xl);
                maxInlineZ = Math.max(maxInlineZ, p.il);
                maxXlineZ = Math.max(maxXlineZ, p.xl);
            }
        }
        
        //... get first guess at origin true x/y
        p = cdpModel.getCdpPoint(minInlineZ-0.5 - buffer, minXlineZ-0.5 - buffer); //true origin point
        cdpModel.setOriginX(p.x);
        cdpModel.setOriginY(p.y);
        
        //...set Number Inlines and Xlines
        int nInlines = (maxInlineZ - minInlineZ)/cdpModel.ilIncrement + 2 + 2*buffer;
        int nXlines  = (maxXlineZ  - minXlineZ )/cdpModel.xlIncrement + 2 + 2*buffer;
        cdpModel.setNumInlines(nInlines);
        cdpModel.setNumXlines(nXlines);
    }

    /**
     * get grid angle by taking the median of the station-to-station azimuth throughout
     * the survey.
     * 
     * Updated - 8/26/2011 - now uses first/last stations per receiver line for more accurate angle - SWC
     */
    private void findAngle()
    {
        if (receivers == null) return;
        if (receivers.size() < 2) return;
        
        //angles = new double[receivers.size()/AngleSkip];
        ArrayList<Double> angles1 = new ArrayList<Double>();
        angles1.ensureCapacity(receivers.size()/AngleSkip);
        
        Station s1 = (Station) receivers.get(0);
        int i=0;
        while(s1.kill) s1 = (Station) receivers.get(i++); //find first non-killed station
        Station s2, s3;
        s3 = s1;
        for (i=1; i<receivers.size()/AngleSkip; i++) {
            s2 = (Station) receivers.get(i*AngleSkip);
            if(s2.kill || s1.kill) {
//                s1 = s2;
                continue; //don't use killed stations!
            }
            if (s1.lineNumber == s2.lineNumber) {
//                double angle = SUtil.azimuth(s1.x, s1.y, s2.x, s2.y);
               // angles[angleCount++] = angle;
//                angles1.add(angle);
                s3 = s2;
            } else {
                double angle = SUtil.azimuth(s1.x, s1.y, s3.x, s3.y);
                angles1.add(angle);
                s1 = s2;
            }
//            s1 = s2;
        }
        angle = (Double) SUtil.median(angles1.toArray());
        cdpModel.angle = angle;
    }

    @Override
    public boolean getIndeterminate()
    {
        return true;
    }

}
