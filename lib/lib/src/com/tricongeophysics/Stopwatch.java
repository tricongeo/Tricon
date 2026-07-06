package com.tricongeophysics;

import java.text.DecimalFormat;

/**
  * A class to help benchmark code snippits
  * It simulates a real stop watch
  */
public class Stopwatch {

    
    private long startTime = -1;
    private long stopTime = -1;
    private boolean running = false;
    private long totalTime = 0;
    private String name;
    private DecimalFormat df2;
    private DecimalFormat df1;

    public Stopwatch(String name)
    {
        this.name = name;
        df2 = new java.text.DecimalFormat("00");
        df1 = new java.text.DecimalFormat("00.000");
    }

   public Stopwatch start() {
      startTime = System.currentTimeMillis();
      running = true;
      return this;
   }
   public Stopwatch stop() {
      stopTime = System.currentTimeMillis();
      totalTime += stopTime - startTime;
      running = false;
      return this;
   }

   /** returns elapsed time in milliseconds 
     * if the watch has never been started then
     * return zero
     */
   public long getElapsedTime() {
      if (startTime == -1) {
         return 0;
      }
      if (running) {
         return System.currentTimeMillis()-startTime;
      } else {
         return stopTime - startTime; 
      }     
   }
  
   public Stopwatch reset() {
      startTime = -1;
      stopTime = -1;
      running = false;
      return this;
   }

   public long getAccumulatedTime()
   {
       return totalTime;
   }

   public void printTime()
   {
       SUtil.print(SUtil.formatTimeLong(totalTime)+" - "+name);
   }

}
