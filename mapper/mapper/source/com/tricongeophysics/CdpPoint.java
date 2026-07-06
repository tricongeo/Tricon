package com.tricongeophysics;

import java.io.Serializable;

public class CdpPoint implements Serializable
{
    private static final long serialVersionUID = 1L;
    int il;
    int xl;
    double x;
    double y;
    
    @Override
    public String toString() {
        return il + " " + xl + " x: " + x + " y: " + y;
    }

    public int getIl()
    {
        return il;
    }

    public void setIl(int il)
    {
        this.il = il;
    }

    public int getXl()
    {
        return xl;
    }

    public void setXl(int xl)
    {
        this.xl = xl;
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

    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }
}