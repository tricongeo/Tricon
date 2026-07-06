package com.tricongeophysics;

import com.tricongeophysics.FileKey.Key;

public abstract class StationFileKey extends FileKey
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected int lineStartPos = 5;
    protected int lineEndPos = 8;
    protected int stationStartPos = 21;
    protected int stationEndPos = 24;
    protected int xStartPos = 46;
    protected int xEndPos = 54;
    protected int yStartPos = 57;
    protected int yEndPos = 64;
    protected int zStartPos = 65;
    protected int zEndPos = 70;
    
    public StationFileKey() {
        super();
    }
    
    public StationFileKey(TriconFile file)
    {
        super(file);
    }

    @Override
    public abstract Station getNewStation();
    
    public int getLineStart()
    {
        return lineStartPos;
    }

    public int getLineEnd()
    {
        return lineEndPos;
    }

    public int getStationStart()
    {
        return stationStartPos;
    }

    public int getStationEnd()
    {
        return stationEndPos;
    }

    public int getXStart()
    {
        return xStartPos;
    }

    public int getXEnd()
    {
        return xEndPos;
    }

    public int getYStart()
    {
        return yStartPos;
    }

    public int getYEnd()
    {
        return yEndPos;
    }

    public int getZStart()
    {
        return zStartPos;
    }

    public int getZEnd()
    {
        return zEndPos;
    }

    public void setLineStart(int num)
    {
        if (num >= 0)
            lineStartPos = num;
    }

    public void setLineEnd(int num)
    {
        if (num >= lineStartPos)
            lineEndPos = num;
    }

    public void setStationStart(int num)
    {
        if (num >= 0)
            stationStartPos = num;
    }

    public void setStationEnd(int num)
    {
        if (num >= stationStartPos)
            stationEndPos = num;
    }

    public void setXStart(int num)
    {
        if (num >= 0)
            xStartPos = num;
    }

    public void setXEnd(int num)
    {
        if (num >= xStartPos)
            xEndPos = num;
    }

    public void setYStart(int num)
    {
        if (num >= 0)
            yStartPos = num;
    }

    public void setYEnd(int num)
    {
        if (num >= yStartPos)
            yEndPos = num;
    }

    public void setZStart(int num)
    {
        if (num >= 0)
            zStartPos = num;
    }

    public void setZEnd(int num)
    {
        if (num >= zStartPos)
            zEndPos = num;
    }
    
    public void setLineStart(String num)
    {
        setLineStart((int) SUtil.sval(num, 0));
    }

    public void setLineEnd(String num)
    {
        setLineEnd((int) SUtil.sval(num, 0));
    }

    public void setStationStart(String num)
    {
        setStationStart((int) SUtil.sval(num, 0));
    }

    public void setStationEnd(String num)
    {
        setStationEnd((int) SUtil.sval(num, 0));
    }

    public void setXStart(String num)
    {
        setXStart((int) SUtil.sval(num, 0));
    }

    public void setXEnd(String num)
    {
        setXEnd((int) SUtil.sval(num, 0));
    }

    public void setYStart(String num)
    {
        setYStart((int) SUtil.sval(num, 0));
    }

    public void setYEnd(String num)
    {
        setYEnd((int) SUtil.sval(num, 0));
    }

    public void setZStart(String num)
    {
        setZStart((int) SUtil.sval(num, 0));
    }

    public void setZEnd(String num)
    {
        setZEnd((int) SUtil.sval(num, 0));
    }
    
    public Station decipherLine(String line)
    {
        if (line == null || line.trim().length() == 0) return null;
        if (getUseCharKey())
            if ((line.length() <= getCharKeyColumn()))
                return null;
            else if (line.charAt(getCharKeyColumn()) != getCharKeyChar())
                return null;
        
        //if you made it here, either not using character key, or character key matches text
        Station station = getNewStation();
            station.setLineNumber(getLineNumberAdd()+(int)SUtil.sval(line,
                    getLineStart(),
                    getLineEnd()));
            station.setStationNumber(getStationNumberAdd()+(int)SUtil.sval(line,
                    getStationStart(),
                    getStationEnd()));
            station.setX(SUtil.sval(line,
                    getXStart(),
                    getXEnd()));
            station.setY(SUtil.sval(line,
                    getYStart(),
                    getYEnd()));
            station.setZ(SUtil.sval(line,
                    getZStart(),
                    getZEnd()));
            station.setFile(getInputFile());
            for (int i=0; i<optionalFileKeys.size(); i++) {
                Key key = optionalFileKeys.get(i);
                station.addOptionalColumn(key.getName(), Double.class);
                Double val = SUtil.sval(line, key.getFrom(), key.getTo());
                //station.setOptionalValue(station.getColumnNames().length + i - 1, val);
                station.setValue(key.getName(), val);
            }
            return station;
    }
    
    @Override
    public String toStringDetailed(String description)
    {
        return description + ":" + "\n" + this.getInputFile() + "% \t station input file name" + "\n" + this.getCharKeyChar() + "% \t character key character" + "\n"
                + this.getCharKeyColumn() + "% \t column location of character key" + "\n" + this.getFirstLine() + "% \t first line of text to read" + "\n" + this.getLastLine()
                + "% \t last line of text to read" + "\n" + this.getLineStart() + "% \t 1st column of line number" + "\n" + this.getLineEnd() + "% \t last column of line number"
                + "\n" + this.getStationStart() + "% \t 1st column of station number" + "\n" + this.getStationEnd() + "% \t last column of station number" + "\n"
                + this.getXStart() + "% \t 1st column of x coordinate" + "\n" + this.getXEnd() + "% \t last column of x coordinate" + "\n" + this.getYStart()
                + "% \t 1st column of y coordinate" + "\n" + this.getYEnd() + "% \t last column of y coordinate" + "\n" + this.getZStart() + "% \t 1st column of elevation" + "\n"
                + this.getZEnd() + "% \t last column of y coordinate" + "\n" + this.getLineNumberAdd() + "% \t constant to add to line number value" + "\n"
                + this.getStationNumberAdd() + "% \t constant to add to station number value" + "\n" + this.getUseCharKey() + "% \t use character key? (1=true,0=false)" + "\n";
    }
    
 // Set FileKey based on "%" parsed text string. First segment is garbage - remaining segments must match the following...
    @Override
    public FileKey setFileKeyFromString(String s)
    {
        String[] percentParsedString = s.split("%");

        //this.setInputFile(new TriconFile(percentParsedString[1]));
        this.setInputFile(percentParsedString[1]);
        this.setCharKeyChar((percentParsedString[2]).charAt(0));
        this.setCharKeyColumn((int) SUtil.sval(percentParsedString[3]));
        this.setFirstLine((int) SUtil.sval(percentParsedString[4]));
        this.setLastLine((int) SUtil.sval(percentParsedString[5]));
        this.setLineStart((int) SUtil.sval(percentParsedString[6]));
        this.setLineEnd((int) SUtil.sval(percentParsedString[7]));
        this.setStationStart((int) SUtil.sval(percentParsedString[8]));
        this.setStationEnd((int) SUtil.sval(percentParsedString[9]));
        this.setXStart((int) SUtil.sval(percentParsedString[10]));
        this.setXEnd((int) SUtil.sval(percentParsedString[11]));
        this.setYStart((int) SUtil.sval(percentParsedString[12]));
        this.setYEnd((int) SUtil.sval(percentParsedString[13]));
        this.setZStart((int) SUtil.sval(percentParsedString[14]));
        this.setZEnd((int) SUtil.sval(percentParsedString[15]));
        this.setLineNumberAdd((int) SUtil.sval(percentParsedString[16]));
        this.setStationNumberAdd((int) SUtil.sval(percentParsedString[17]));
        this.setUseCharKey(percentParsedString[18].equals("true"));

        return this;
    }

}
