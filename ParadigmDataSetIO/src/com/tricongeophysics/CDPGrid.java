package com.tricongeophysics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CDPGrid
{
    private int numInlines;
    private int numXlines;
    private double xOrigin;
    private double yOrigin;
    private double inLineInterval; //distance between inlines
    private double xLineInterval; //distance between xlines
    private double angle;  //angle in degrees counter-clockwise from due East to first inline
    private double inlineDeltaX;
    private double xlineDeltaX;
    private double xlineDeltaY;
    private double inlineDeltaY;
    private int firstInline;
    private int firstXline;
    private int inLineIncrement; //inline label increment
    private int xLineIncrement;  //xline label increment
    private String xUnits;       //horizontal distance unit name
    private String zUnits;       //vertical axis unit name
    private String dataType;
    private int firstCDP;
    public static final String FirstCDP = "DaNsurveyFirstCmp";
    public static final String SurveyTypeText = "DsNsurveyType";
    public static final String XOriginText = "DaNsurveyX0";
    public static final String YOriginText = "DaNsurveyY0";
    public static final String AngleText = "DaNsurveyRotation";
    public static final String InlineIntervalText = "DaNsurveyDsl";
    public static final String FirstInlineText = "DaNsurveyFirstSl";
    public static final String InlineIncrementText = "DaNsurveyIncSl";
    public static final String NumInlinesText = "DaNsurveyNSl";
    public static final String XlineIntervalText = "DaNsurveyDxl";
    public static final String FirstXlineText = "DaNsurveyFirstXl";
    public static final String XlineIncrementText = "DaNsurveyIncXl";
    public static final String NumXlinesText = "DaNsurveyNXl";
    public static final String AxisNameText = "DaNaxis ";
    public static final String UnitsNameText = "DaNunits";
    public static final String DataTypeText = "DaNdataType";
    public static final String Feet = "Feet";
    public static final String Microsecond = "Microsecond";
    public static final String Time = "Time";
    public static final String TimeMigrated = "TimeMigrated";
    private File pdsFile;

    public enum SurveyType
    {
        _2D, _3D
    }

    ;
    private SurveyType surveyType;


    public String description()
    {
        String string = "cdp grid:" + "\n" +
            "from file:" + pdsFile + "\n" +
            "type:" + surveyType + "\n" +
            "xunits:" + xUnits + "\n" +
            "zunits:" + zUnits + "\n" +
            "x0:" + xOrigin + "\n" +
            "y0:" + yOrigin + "\n" +
            "angle:" + angle + "\n" +
            "inline0:" + firstInline + "\n" +
            "xline0:" + firstXline + "\n" +
            "DsInline:" + inLineInterval + "\n" +
            "DsXline:" + xLineInterval + "\n" +
            "NumInlines:" + numInlines + "\n" +
            "NumXlines:" + numXlines + "\n" +
            "InLineInc:" + inLineIncrement + "\n" +
            "XLineInc:" + xLineIncrement + "\n";
        return string;
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

    public double getInLineInterval()
    {
        return inLineInterval;
    }

    public void setInLineInterval(double inLineInterval)
    {
        this.inLineInterval = inLineInterval;
    }

    public double getXLineInterval()
    {
        return xLineInterval;
    }

    public void setXLineInterval(double lineInterval)
    {
        xLineInterval = lineInterval;
    }

    public double getAngle()
    {
        return angle;
    }

    public void setAngle(double angle)
    {
        this.angle = angle;
    }

    public int getFirstInline()
    {
        return firstInline;
    }

    public void setFirstInline(int firstInline)
    {
        this.firstInline = firstInline;
    }

    public int getFirstXline()
    {
        return firstXline;
    }

    public void setFirstXline(int firstXline)
    {
        this.firstXline = firstXline;
    }

    //returns 7th field when text is split by spaces, this seems to be where the values are placed
    // in the file
    private int getPDSInt(String textline)
    {
        String[] array = textline.trim().split(" ");
        if (array.length > 3 && !array[4].equals(""))
        {
            String c = array[4];
            return Integer.parseInt(c);
        }
        else
        {
            return 0;
        }
    }

    private double getPDSDouble(String textline)
    {
        String[] array = textline.trim().split(" ");
        if (array.length > 3 && !array[4].equals(""))
        {
            String c = array[4];
            return Double.parseDouble(c);
        }
        else
        {
            return 0.0;
        }
    }

    private String getPDSString(String textline)
    {
        return textline.trim().split(" ")[4].replace("`", " ").trim();
    }

    public void loadFromPDS(String filename) throws FileNotFoundException
    {
        pdsFile = new File(filename);
        System.out.println("\nReading CDP Grid from file \"" + filename + "\"...\n");
        numInlines = 0;
        numXlines = 0;
        xOrigin = 0.0;
        yOrigin = 0.0;
        inLineInterval = 0; //distance between inlines
        xLineInterval = 0; //distance between xlines
        angle = 0;  //angle in degrees counter-clockwise from due East to first inline
        firstInline = 0;
        firstXline = 0;
        inLineIncrement = 0; //inline label increment
        xLineIncrement = 0;  //xline label increment
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(pdsFile));
            String line;
            String currentAxis = "";
            while ((line = reader.readLine()) != null)
            {
                if (line.contains(CDPGrid.AxisNameText))
                {
                    currentAxis = getPDSString(line);
                }
                if (line.contains(CDPGrid.UnitsNameText) && currentAxis.contains("Z"))
                {
                    zUnits = getPDSString(line);
                }
                if (line.contains(CDPGrid.DataTypeText) && currentAxis.contains("Z"))
                {
                    dataType = getPDSString(line);
                }
                if (line.contains(CDPGrid.UnitsNameText) && currentAxis.contains("X"))
                {
                    xUnits = getPDSString(line);
                }
                if (line.contains(CDPGrid.SurveyTypeText))
                {
                    String type = getPDSString(line);
                    if (type.contains("3D"))
                    {
                        surveyType = SurveyType._3D;
                    }
                    else
                    {
                        surveyType = SurveyType._2D;
                    }
                }
                if (line.contains(NumInlinesText))
                {
                    numInlines = getPDSInt(line);
                }
                if (line.contains(NumXlinesText))
                {
                    numXlines = getPDSInt(line);
                }
                if (line.contains(XOriginText))
                {
                    xOrigin = getPDSDouble(line);
                }
                if (line.contains(YOriginText))
                {
                    yOrigin = getPDSDouble(line);
                }
                if (line.contains(InlineIntervalText))
                {
                    inLineInterval = getPDSDouble(line);
                }
                if (line.contains(XlineIntervalText))
                {
                    xLineInterval = getPDSDouble(line);
                }
                if (line.contains(AngleText))
                {
                	if (getPDSString(line).equals("")) angle = -1; //flag for bad angle
                	else angle = getPDSDouble(line);
                }
                if (line.contains(FirstInlineText))
                {
                    firstInline = getPDSInt(line);
                }
                if (line.contains(FirstXlineText))
                {
                    firstXline = getPDSInt(line);
                }
                if (line.contains(InlineIncrementText))
                {
                    inLineIncrement = getPDSInt(line);
                }
                if (line.contains(XlineIncrementText))
                {
                    xLineIncrement = getPDSInt(line);
                }
                if (line.contains(FirstCDP))
                {
                    firstCDP = getPDSInt(line);
                }
            }
            reader.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            System.err.println(filename + "not found! Can't load CDP Grid!");
            throw e;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("error reading \"" + filename + "\". Can't load CDP Grid!");
            e.printStackTrace();
        }
        calcDeltaXDeltaY();
    }

    /** calculate inline and xline delta-x and delta-y using the rotation angle and bin intervals */
    public void calcDeltaXDeltaY()
    {
        double radians = angle * (2 * Math.PI) / 360.0;
        inlineDeltaX = inLineInterval * Math.cos(radians + 0.5 * Math.PI); //add 90 degrees to angle
        inlineDeltaY = inLineInterval * Math.sin(radians + 0.5 * Math.PI); //add 90 degrees to angle
        xlineDeltaX = xLineInterval * Math.cos(radians); 
        xlineDeltaY = xLineInterval * Math.sin(radians); 
    }

    public String getXUnits()
    {
        return xUnits;
    }


    public String getZUnits()
    {
        return zUnits;
    }

    public String getDataType()
    {
        return dataType;
    }

    public double getXOrigin()
    {
        return xOrigin;
    }

    public int getInLineIncrement()
    {
        return inLineIncrement;
    }

    public int getXLineIncrement()
    {
        return xLineIncrement;
    }

    public int[] getInlineXlineFromCDP(int cdp)
    {
        int inline = (int) ((1.0 * (cdp - firstCDP)) / numXlines) + firstInline;
        int xline = (int) ((1.0 * (cdp - firstCDP)) % numXlines) + firstXline;
        return new int[]{inline, xline};
    }

    public double[] calcXYFromCDP(int cdp)
    {
        int[] inlineXline = getInlineXlineFromCDP(cdp);
        return calcXYFromInlineXline(inlineXline);
    }

    private double[] calcXYFromInlineXline(int[] inlineXline)
    {
    	int inline = inlineXline[0];
        int xline = inlineXline[1];
        int deltaInline = inline - firstInline;
        int deltaXline = xline - firstXline;
        double x = deltaInline * inlineDeltaX + deltaXline * xlineDeltaX + xOrigin;
        double y = deltaInline * inlineDeltaY + deltaXline * xlineDeltaY + yOrigin;
        return new double[]{x, y};
    }

    public double getInlineDeltaX()
    {
        return inlineDeltaX;
    }

    public double getXlineDeltaX()
    {
        return xlineDeltaX;
    }

    public int getFirstCDP()
    {
        return firstCDP;
    }

    public boolean is3d()
    {
        if (surveyType.equals(SurveyType._3D))
        {
            return true;
        }
        return false;
    }

}
