package com.tricongeophysics;

import java.io.Serializable;

public class CdpModel implements Serializable, DataDepot
{
    private static final long serialVersionUID = 1L;
    int firstCDP = 1;
    int originIL = 1001;
    int originXL = 1001;
    double ilInterval = 110;
    double xlInterval = 110;
    int ilIncrement = 1;
    int xlIncrement = 1;
    double originX = 0;
    double originY = 0;
    double angle = 0;
    int numInlines = 50;
    int numXlines = 50;
    public int realModel = 1; //tests to see if this is just empty crap model from unSerialize()
    
    enum Corner {
        UpperLeft, UpperRight, LowerLeft, LowerRight};
    
    CdpPoint p1 = new CdpPoint();
    CdpPoint p2 = new CdpPoint();
    CdpPoint p3 = new CdpPoint();
    CdpPoint p4 = new CdpPoint();
    private int buffer = 1;
    private CdpPoint matchPoint; //point from previous cdp model that we're supposed to match
    


    public CdpModel() {
       
    }

    public int getFirstCDP()
    {
        return firstCDP;
    }

    public void setFirstCDP(int firstCDP)
    {
        this.firstCDP = firstCDP;
    }

    public int getOriginIL()
    {
        return originIL;
    }

    public void setOriginIL(int originIL)
    {
        this.originIL = originIL;
    }

    public int getOriginXL()
    {
        return originXL;
    }

    public void setOriginXL(int originXL)
    {
        this.originXL = originXL;
    }

    public double getIlInterval()
    {
        return ilInterval;
    }

    public void setIlInterval(double ilInterval)
    {
        this.ilInterval = ilInterval;
    }

    public double getXlInterval()
    {
        return xlInterval;
    }

    public void setXlInterval(double xlInterval)
    {
        this.xlInterval = xlInterval;
    }

    public int getIlIncrement()
    {
        return ilIncrement;
    }

    public void setIlIncrement(int ilIncrement)
    {
        this.ilIncrement = ilIncrement;
    }

    public int getXlIncrement()
    {
        return xlIncrement;
    }

    public void setXlIncrement(int xlIncrement)
    {
        this.xlIncrement = xlIncrement;
    }

    public double getOriginX()
    {
        return originX;
    }

    public void setOriginX(double originX)
    {
        this.originX = originX;
    }

    public double getOriginY()
    {
        return originY;
    }

    public void setOriginY(double originY)
    {
        this.originY = originY;
    }

    public double getAngle()
    {
        return angle;
    }

    /**
     * Angle from x axis, counter-clockwise, to first in-line, measured in degrees.
     * Must be between 0 and 360.
     * @param angle
     */
    public void setAngle(double angle)
    {
        this.angle = (angle >= 360 || angle < 0) ? 0 : angle;
    }

    public int getNumInlines()
    {
        return numInlines;
    }

    public void setNumInlines(int numInlines)
    {
        this.numInlines = numInlines;
    }

    public int getNumXlines()
    {
        return numXlines;
    }

    public void setNumXlines(int numXlines)
    {
        this.numXlines = numXlines;
    }

    public void calcCornersFromAngle()
    {
         int il0 = originIL; //first inline number
         int xl0 = originXL; //first xline number
         int il1 = il0 + (numInlines - 1) * ilIncrement; //last inline number
         int xl1 = xl0 + (numXlines - 1) * xlIncrement; //last xline number
//         double dil = (numInlines - 1) * ilInterval; //distance across inlines
//         double dxl = (numXlines - 1) * xlInterval; //distance across xlines
//         double a = angle*Math.PI/180;  //angle in radians counter-clockwise from x axix
         
         p1 = getCdpPoint(il0, xl0);
         p2 = getCdpPoint(il0, xl1);
         p3 = getCdpPoint(il1, xl1);
         p4 = getCdpPoint(il1, xl0);
         
//         p1.il = il0;
//         p1.xl = xl0;
//         p1.x = originX;
//         p1.y = originY;
//         
//         p2.il = il0;
//         p2.xl = xl1;
//         p2.x = originX + dxl * Math.cos(a);
//         p2.y = originY + dxl * Math.sin(a);
//         
//         p4.il = il1;
//         p4.xl = xl0;
//         p4.x = originX - dil * Math.sin(a);
//         p4.y = originY + dil * Math.cos(a);
//         
//         p3.il = il1;
//         p3.xl = xl1;
//         p3.x = p4.x + dxl * Math.cos(a);
//         p3.y = p4.y + dxl * Math.sin(a);
//         double d = p2.x - p1.x;
//         double e = p2.y - p1.y;
//         p3.x = p4.x + d;
//         p3.y = p4.y + e;
    }

    public void calcAngleFromCorners()
    {
        angle = SUtil.azimuth(p1.x, p1.y, p2.x, p2.y);
        originIL = p1.il;
        originXL = p1.xl;
        originX = p1.x;
        originY = p1.y;
        
        numInlines = p4.il - p1.il + 1;
        numXlines = p2.xl - p1.xl + 1;
        
        ilIncrement = 1;
        xlIncrement = 1;
        
        //firstCDP = 1;
        
        double ilDistance = SUtil.distance(p1.x, p1.y, p4.x, p4.y);
        ilInterval = ilDistance/(numInlines-1);
        double xlDistance = SUtil.distance(p1.x, p1.y, p2.x, p2.y);
        xlInterval = xlDistance/(numXlines-1);
        
        //check for ilInterval sign
        CdpPoint p = getCdpPointFromXY(p4.x, p4.y);
        if (p.il < p1.il) {
            ilInterval = (-1) * ilInterval;
        }
        p = getCdpPointFromXY(p4.x, p4.y);
        if (p.il < 0) {
           SUtil.print("wtf??? having trouble getting cdp model from corners");
        }
        
        
        //sanity check
        p2.il = p1.il;
        p4.xl = p1.xl;
        p3 = getCdpPoint(p4.il, p2.xl);
    }

    public CdpPoint getPoint(Corner corner)
    {
        double xavg = (p1.x + p2.x + p3.x + p4.x)/4;
        double yavg = (p1.y + p2.y + p3.y + p4.y)/4;
        
//        if ((angle <45 && angle >= 0) || (angle <360 && angle >= 325)) {
//            if (corner == Corner.LowerRight) return p2;
//            if (corner == Corner.LowerLeft) return p1;
//            if (corner == Corner.UpperRight) return p3;
//            if (corner == Corner.UpperLeft) return p4;
//        }
//        if (angle <135 && angle >= 45) {
//            if (corner == Corner.LowerRight) return p1;
//            if (corner == Corner.LowerLeft) return p4;
//            if (corner == Corner.UpperRight) return p2;
//            if (corner == Corner.UpperLeft) return p3;
//        }
//        if (angle <225 && angle >= 135) {
//            if (corner == Corner.LowerRight) return p4;
//            if (corner == Corner.LowerLeft) return p3;
//            if (corner == Corner.UpperRight) return p1;
//            if (corner == Corner.UpperLeft) return p2;
//        }
//        if (angle <325 && angle >= 225) {
//            if (corner == Corner.LowerRight) return p3;
//            if (corner == Corner.LowerLeft) return p2;
//            if (corner == Corner.UpperRight) return p4;
//            if (corner == Corner.UpperLeft) return p1;
//        }
        
        CdpPoint[] points = new CdpPoint[]{ p1, p2, p3, p4};
        CdpPoint lowestPoint = p1; //take first guess at lowest point
        int index = 0;
        for (int i=0; i< points.length; i++) {
            if (points[i].y < lowestPoint.y) {
                lowestPoint = points[i];
                index = i;
            }
        }
        
        CdpPoint lowerLeft;
        CdpPoint lowerRight;
        CdpPoint upperRight;
        CdpPoint upperLeft;
        int shiftby = (ilInterval > 0 && xlInterval > 0) ? 1 : -1;
        if (lowestPoint.x < xavg) {
            lowerLeft = lowestPoint;
            int shift = (index + shiftby)%4;
            if (shift < 0) shift += 4;
            lowerRight = points[shift];
            shift = (index + 2*shiftby)%4;
            if (shift < 0) shift += 4;
            upperRight = points[shift];
            shift = (index + 3*shiftby)%4;
            if (shift < 0) shift += 4;
            upperLeft = points[shift];
        } else {
            lowerRight = lowestPoint;
            int shift = (index + shiftby)%4;
            if (shift < 0) shift += 4;
            upperRight = points[shift];
            shift = (index + 2*shiftby)%4;
            if (shift < 0) shift += 4;
            upperLeft = points[shift];
            shift = (index + 3*shiftby)%4;
            if (shift < 0) shift += 4;
            lowerLeft = points[shift];
        }
        
        if (corner == Corner.LowerLeft) {
           return lowerLeft;
        }
        if (corner == Corner.LowerRight) {
           return lowerRight;
        }
        if (corner == Corner.UpperRight) {
            return upperRight;
        }
        if (corner == Corner.UpperLeft) {
           return upperLeft;
        }
        
        if (corner == Corner.LowerLeft) {
           return lowerLeft;
        }
        if (corner == Corner.LowerRight) {
           return lowerRight;
        }
        if (corner == Corner.UpperRight) {
           return upperRight;
        }
        if (corner == Corner.UpperLeft) {
           return upperLeft;
        }
        
        return null;
    }

    /**
     * set corners from an array of CdpPoints.
     * assumes lowest inline and crossline is p1 (origin).
     * works counter-clockwise from there.
     * @param corners
     */
    public void setCorners(CdpPoint[] corners)
    {
        if (corners == null || corners.length < 1) return;
        int minIL = (corners[0].il != 0) ? corners[0].il : Integer.MAX_VALUE;
        int minXL = (corners[0].xl != 0) ? corners[0].xl : Integer.MAX_VALUE;
        for (CdpPoint p: corners) {
            if (p.il != 0) minIL = Math.min(minIL, p.il);
            if (p.xl != 0) minXL = Math.min(minXL, p.xl);
        }
        for (CdpPoint p: corners) {
           if (p.il == minIL && p.xl == minXL) {
               p1 = p;
           }
           if (p.il == minIL && p.xl != minXL) {
               p2 = p;
           }
           if (p.il != minIL && p.xl == minXL) {
               p4 = p;
           }
           if (p.il != minIL && p.xl != minXL) {
               p3 = p;
           }
        }
    }

    /**
     * returns point from arbitrary inline and crossline location.
     * il and xl can be fractions.
     * @param il
     * @param xl
     * @return
     */
    public CdpPoint getCdpPoint(double il, double xl)
    {       
       double inLineDeltaX = getInlineDeltaX();
       double inLineDeltaY = getInlineDeltaY();
       double xLineDeltaX  = getXLineDeltaX();
       double xLineDeltaY  = getXLineDeltaY();
       
       CdpPoint p = new CdpPoint();
       p.il = (int) Math.round(il);
       p.xl = (int) Math.round(xl);
       double dInline = il - originIL;
       double dXline =  xl- originXL;
       
       p.x = originX + dInline * inLineDeltaX + dXline * xLineDeltaX;
       p.y = originY + dInline * inLineDeltaY + dXline * xLineDeltaY;
       
       return p;
    }

    double getXLineDeltaY()
    {
        double a = Math.toRadians(angle);
        return Math.sin(a) * xlInterval;
    }

    double getXLineDeltaX()
    {
        double a = Math.toRadians(angle);
        return Math.cos(a) * xlInterval;
    }

    double getInlineDeltaY()
    {
        double b = Math.toRadians(90 + angle);
        return Math.sin(b) * ilInterval;
    }

    double getInlineDeltaX()
    {
        double b = Math.toRadians(90 + angle);
        return Math.cos(b) * ilInterval;
    }

    public CdpPoint getP1()
    {
        return p1;
    }

    public void setP1(CdpPoint p1)
    {
        this.p1 = p1;
    }

    public CdpPoint getP2()
    {
        return p2;
    }

    public void setP2(CdpPoint p2)
    {
        this.p2 = p2;
    }

    public CdpPoint getP3()
    {
        return p3;
    }

    public void setP3(CdpPoint p3)
    {
        this.p3 = p3;
    }

    public CdpPoint getP4()
    {
        return p4;
    }

    public void setP4(CdpPoint p4)
    {
        this.p4 = p4;
    }

    /**
     * Get nearest CDP point to x and y;
     * returns point with correct il and xl, but with supplied x and y values
     * @param x
     * @param y
     * @return
     */
    public CdpPoint getCdpPointFromXY(double x, double y)
    {
        double a = Math.toRadians(angle);

        double xR = (x - originX) * Math.cos(a) + (y - originY) * Math.sin(a); //rotated x difference
        double yR = (y - originY) * Math.cos(a) - (x - originX) * Math.sin(a); //rotated y difference

        int numInlines = (int) (yR/ilInterval); //number of inlines from origin
        int il = originIL + numInlines * ilIncrement;
        
        int numXlines = (int) (xR/xlInterval); //number of xlines from origin
        int xl = originXL + numXlines * xlIncrement;
        
        //CdpPoint p = this.getCdpPoint(il, xl);
        CdpPoint p = new CdpPoint();
        p.il = il;
        p.xl = xl;
        p.x = x;
        p.y = y;
        return p;
    }

    public void setBuffer(int buffer)
    {
        this.buffer = buffer;
    }

    public int getBuffer()
    {
        return buffer;
    }

    public int getRealModel()
    {
        return realModel;
    }

    public void setRealModel(int realModel)
    {
        this.realModel = realModel;
    }

    @Override
    public Object getData()
    {
        return this;
    }

    public void setMatchPoint(CdpPoint p)
    {
        this.matchPoint = p;
    }

    public CdpPoint getMatchPoint()
    {
        return matchPoint;
    }

    public int getLastCDP()
    {
        int cdpl = firstCDP + getNumCdps() - 1;
        return cdpl;
    }

    public int getNumCdps()
    {
        return numInlines * numXlines;
    }

    public CdpPoint[] getPoints()
    {
        CdpPoint[] p = new CdpPoint[4];
        p[0] = p1;
        p[1] = p2;
        p[2] = p3;
        p[3] = p4;
        return p;
    }
}
