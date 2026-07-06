package com.tricongeophysics;

public class Index
{
    int i=0;

    public int getI()
    {
        return i;
    }

    public void setI(int i)
    {
        this.i = i;
    }

    public void increment(int j)
    {
        i += j;
    }
    
    public String toString() {
        return i+"";
    }
}
