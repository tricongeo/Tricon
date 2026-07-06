package com.tricongeophysics;

import java.io.*;
import java.util.ArrayList;
import java.awt.*;

import com.tricongeophysics.FileKey.Key;

public abstract class FileKey implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    // Defaults are set to SPSOUT standard
    protected int firstLine = 0;
    protected int lastLine = 1000000;
    protected int charKeyColumn = 0;
    protected char charKeyChar = 'S';
    // transient protected TriconFile inputFile;
    protected String inputFile;
    protected boolean useCharKey = true;
    protected int lineNumberAdd = 0;
    protected int stationNumberAdd = 0;
    protected ArrayList<Key> optionalFileKeys = new ArrayList<Key>();

    public FileKey(TriconFile f)
    {
        setInputFile(f.getAbsolutePath());
    }

    public FileKey()
    {
    }

    // public String toString() {
    // return "fl"+firstLine+" ll"+lastLine+" ls"+lineStartPos+
    // " le"+lineEndPos+" ss"+stationStartPos+" se"+stationEndPos+
    // " xs"+xStartPos+" xe"+xEndPos+" ys"+yStartPos+" ye"+yEndPos+
    // " zs"+zStartPos+" ze"+zEndPos;
    // }
    public String toString()
    {
        // return inputFile.getPath();
        return inputFile;
    }

    public int getFirstLine()
    {
        return firstLine;
    }

    public int getLastLine()
    {
        return lastLine;
    }

    public int getCharKeyColumn()
    {
        return charKeyColumn;
    }

    public char getCharKeyChar()
    {
        return charKeyChar;
    }

    

    public String getInputFile()
    {
        return inputFile;
    }

    public boolean getUseCharKey()
    {
        return useCharKey;
    }

    public int getLineNumberAdd()
    {
        return lineNumberAdd;
    }

    public int getStationNumberAdd()
    {
        return stationNumberAdd;
    }

    public void setFirstLine(int num)
    {
        if (num >= 0)
            firstLine = num;
    }

    public void setLastLine(int num)
    {
        if (num >= firstLine)
            lastLine = num;
    }

    public void setCharKeyColumn(int num)
    {
        if (num >= 0)
            charKeyColumn = num;
    }

    public void setCharKeyChar(char c)
    {
        charKeyChar = c;
    }

    

    public void setLineNumberAdd(int num)
    {
        lineNumberAdd = num;
    }

    public void setStationNumberAdd(int num)
    {
        stationNumberAdd = num;
    }

    public void setFirstLine(String num)
    {
        setFirstLine((int) SUtil.sval(num, 0));
    }

    public void setLastLine(String num)
    {
        setLastLine((int) SUtil.sval(num, 0));
    }

    public void setCharKeyColumn(String num)
    {
        setCharKeyColumn((int) SUtil.sval(num, 0));
    }

    public void setCharKeyChar(String num)
    {
        if (num.length() > 0)
            setCharKeyChar(num.charAt(0));
    }

    

    public void setLineNumberAdd(String num)
    {
        setLineNumberAdd((int) SUtil.sval(num, 0));
    }

    public void setStationNumberAdd(String num)
    {
        setStationNumberAdd((int) SUtil.sval(num, 0));
    }

    public void setInputFile(String f)
    {
        inputFile = f;
    }

    public void setUseCharKey(boolean bool)
    {
        useCharKey = bool;
    }

    

    public abstract Station getNewStation();


    
    

    public static class Key implements Serializable
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private String name;
        private int to;
        private int from;

        public Key(String name)
        {
            this(name, 0, 0);
        }

        public Key(String name, int from, int to)
        {
            this.name = name;
            this.from  = from;
            this.to = to;
        }

        public String getName()
        {
            return name;
        }

        public void setTo(int to)
        {
            this.to = to;
        }

        public int getTo()
        {
            return to;
        }

        public void setFrom(int from)
        {
            this.from = from;
        }

        public int getFrom()
        {
            return from;
        }

    }


    public FileKey.Key addOptionalFileKey(String name)
    {
        return addOptionalFileKey(new FileKey.Key(name));
    }

    private Key findOptionalKey(String name)
    {
        for (Key key: optionalFileKeys) {
            if (key.name.equalsIgnoreCase(name)) return key;
        }
        return null;
    }

    public ArrayList<Key> getOptionalFileKeys()
    {
        return optionalFileKeys;
    }

    public abstract FileKey setFileKeyFromString(String s);

    public abstract String toStringDetailed(String description);

    public FileKey.Key addOptionalFileKey(FileKey.Key key)
    {
        if (optionalFileKeys == null) optionalFileKeys = new ArrayList<Key>();
        FileKey.Key oldKey = findOptionalKey(key.name);
        if (oldKey != null) return oldKey; //don't add same key twice!
        optionalFileKeys.add(key);
        return key; 
    }

}
