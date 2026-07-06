package com.tricongeophysics;

import java.io.*;
import java.awt.*;
import java.util.*;

import com.tricongeophysics.FileKey.Key;

public class OBFileKey extends FileKey implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    //Defaults are set to SPSOUT standard
    protected int ffidStartPos = 6;
    protected int ffidEndPos = 10;
    protected int sourceLineStartPos = 17;
    protected int sourceLineEndPos = 20;
    protected int sourceStationStartPos = 33;
    protected int sourceStationEndPos = 36;
    protected int fromChanStartPos = 38;
    protected int fromChanEndPos = 42;
    protected int toChanStartPos = 43;
    protected int toChanEndPos = 47;
    protected int receiverLineStartPos = 53;
    protected int receiverLineEndPos = 56;
    protected int fromReceiverStationStartPos = 69;
    protected int fromReceiverStationEndPos = 72;
    protected int toReceiverStationStartPos = 77;
    protected int toReceiverStationEndPos = 80; 
    protected int shotStartPos = 81;
    protected int shotEndPos = 89;
    protected int sourceLineNumberAdd = 0;
    protected int sourceStationNumberAdd = 0;
    protected int receiverLineNumberAdd = 0;
    protected int receiverStationNumberAdd = 0;
    protected int ffidAdd = 0;
    public final static String KEY_DESCRIPTION = "OB File Key";

    public OBFileKey(File f) {
        setInputFile(f);
        this.setCharKeyChar('X');
    }

    public OBFileKey(){
        this.setCharKeyChar('X');
    }

    public String toString() {
        return inputFile;
    }

    public int getFirstLine() {
        return firstLine;
    }
    public int getLastLine() {
        return lastLine;
    }
    public int getCharKeyColumn() {
        return charKeyColumn;
    }
    public char getCharKeyChar() {
        return charKeyChar;
    }
    public int getSourceLineStart() {
        return sourceLineStartPos;
    }
    public int getSourceLineEnd() {
        return sourceLineEndPos;
    }
    public int getSourceStationStart() {
        return sourceStationStartPos;
    }
    public int getSourceStationEnd() {
        return sourceStationEndPos;
    }
    public int getFromChanStart() {
        return fromChanStartPos;
    }
    public int getFromChanEnd() {
        return fromChanEndPos;
    }
    public int getToChanStart() {
        return toChanStartPos;
    }
    public int getToChanEnd() {
        return toChanEndPos;
    }
    public int getReceiverLineStart() {
        return receiverLineStartPos;
    }
    public int getReceiverLineEnd() {
        return receiverLineEndPos;
    }
    public String getInputFile() {
        return inputFile;
    }
    public boolean getUseCharKey() {
        return useCharKey;
    }
    public int getSourceLineNumberAdd() {
        return sourceLineNumberAdd;
    }
    public int getSourceStationNumberAdd() {
        return sourceStationNumberAdd;
    }
    public int getReceiverLineNumberAdd() {
        return receiverLineNumberAdd;
    }
    public int getReceiverStationNumberAdd() {
        return receiverStationNumberAdd;
    }
    public void setFirstLine(int num) {
        if (num >= 0)
            firstLine = num;
    }
    public void setLastLine(int num) {
        if (num >= firstLine)
            lastLine = num;
    }
    public void setCharKeyColumn(int num) {
        if (num >= 0)
            charKeyColumn = num;
    }
    public void setCharKeyChar(char c) {
        charKeyChar = c;
    }
    public void setSourceLineStart(int num) {
        if (num >= 0)
            sourceLineStartPos = num;
    }
    public void setSourceLineEnd(int num) {
        if (num >= sourceLineStartPos)
            sourceLineEndPos = num;
    }
    public void setSourceStationStart(int num) {
        if (num >= 0)
            sourceStationStartPos = num;
    }
    public void setSourceStationEnd(int num) {
        if (num >= sourceStationStartPos)
            sourceStationEndPos = num;
    }
    public void setFromChanStart(int num) {
        if (num >= 0)
            fromChanStartPos = num;
    }
    public void setFromChanEnd(int num) {
        if (num >= fromChanStartPos)
            fromChanEndPos = num;
    }
    public void setToChanStart(int num) {
        if (num >= 0)
            toChanStartPos = num;
    }
    public void setToChanEnd(int num) {
        if (num >= toChanStartPos)
            toChanEndPos = num;
    }
    public void setReceiverLineStart(int num) {
        if (num >= 0)
            receiverLineStartPos = num;
    }
    public void setReceiverLineEnd(int num) {
        if (num >= receiverLineStartPos)
            receiverLineEndPos = num;
    }
    public void setSourceLineNumberAdd(int num) {
        sourceLineNumberAdd = num;
    }
    public void setSourceStationNumberAdd(int num) {
        sourceStationNumberAdd = num;
    }
    public void setReceiverLineNumberAdd(int num) {
        receiverLineNumberAdd = num;
    }
    public void setReceiverStationNumberAdd(int num) {
        receiverStationNumberAdd = num;
    }
    public void setFirstLine(String num) {
        setFirstLine((int)SUtil.sval(num,0));
    }
    public void setLastLine(String num) {
        setLastLine((int)SUtil.sval(num,0));
    }
    public void setCharKeyColumn(String num) {
        setCharKeyColumn((int)SUtil.sval(num,0));
    }
    public void setCharKeyChar(String num) {
        if(num.length()>0)
            setCharKeyChar(num.charAt(0));
    }
    public void setSourceLineStart(String num) {
        setSourceLineStart((int)SUtil.sval(num,0));
    }
    public void setSourceLineEnd(String num) {
        setSourceLineEnd((int)SUtil.sval(num,0));
    }
    public void setSourceStationStart(String num) {
        setSourceStationStart((int)SUtil.sval(num,0));
    }
    public void setSourceStationEnd(String num) {
        setSourceStationEnd((int)SUtil.sval(num,0));
    }
    public void setFromChanStart(String num) {
        setFromChanStart((int)SUtil.sval(num,0));
    }
    public void setFromChanEnd(String num) {
        setFromChanEnd((int)SUtil.sval(num,0));
    }
    public void setToChanStart(String num) {
        setToChanStart((int)SUtil.sval(num,0));
    }
    public void setToChanEnd(String num) {
        setToChanEnd((int)SUtil.sval(num,0));
    }
    public void setReceiverLineStart(String num) {
        setReceiverLineStart((int)SUtil.sval(num,0));
    }
    public void setReceiverLineEnd(String num) {
        setReceiverLineEnd((int)SUtil.sval(num,0));
    }
    public void setSourceLineNumberAdd(String num) {
        setSourceLineNumberAdd((int)SUtil.sval(num,0));
    }
    public void setSourceStationNumberAdd(String num) {
        setSourceStationNumberAdd((int)SUtil.sval(num,0));
    }
    public void setReceiverLineNumberAdd(String num) {
        setReceiverLineNumberAdd((int)SUtil.sval(num,0));
    }
    public void setReceiverStationNumberAdd(String num) {
        setReceiverStationNumberAdd((int)SUtil.sval(num,0));
    }
    public void setInputFile(File f) {
        inputFile = f.getAbsolutePath();
    }
    public void setUseCharKey(boolean bool) {
        useCharKey = bool;
    }
    public OBRecord decipherLineNewShot(String line) {
        if (!useCharKey || 
                ((line.length() > charKeyColumn) && (line.charAt(charKeyColumn) == charKeyChar))) {
            OBRecord obRecord = new OBRecord();
            obRecord.setFfid(ffidAdd+(int)SUtil.sval(line,
                    getFfidStartPos(),
                    getFfidEndPos()));
            obRecord.setSourceLineNumber(sourceLineNumberAdd+(int)SUtil.sval(line,
                    getSourceLineStart(),
                    getSourceLineEnd()));
            obRecord.setSourceStationNumber(sourceStationNumberAdd+(int)SUtil.sval(line,
                    getSourceStationStart(),
                    getSourceStationEnd()));
            obRecord.setFromChan(0,(int)SUtil.sval(line,
                    getFromChanStart(),
                    getFromChanEnd()));
            obRecord.setToChan(0,(int)SUtil.sval(line,
                    getToChanStart(),
                    getToChanEnd()));
            obRecord.setReceiverLineNumber(0,receiverLineNumberAdd+(int)SUtil.sval(line,
                    getReceiverLineStart(),
                    getReceiverLineEnd()));
            obRecord.setFromReceiver(0,receiverStationNumberAdd+(int)SUtil.sval(line,
                    getFromReceiverStationStartPos(),
                    getFromReceiverStationEndPos()));
            obRecord.setToReceiver(0,receiverStationNumberAdd+(int)SUtil.sval(line,
                    getToReceiverStationStartPos(),
                    getToReceiverStationEndPos()));
            obRecord.setShot((int)SUtil.sval(line,
                    getShotStartPos(),
                    getShotEndPos()));
            for (int i=0; i<optionalFileKeys.size(); i++) {
                Key key = optionalFileKeys.get(i);
                obRecord.addOptionalColumn(key.getName(), Double.class);
                Double val = SUtil.sval(line, key.getFrom(), key.getTo());
                obRecord.setOptionalValue(obRecord.getColumnNames().length + i - 1, val);
            }
            return obRecord;
        }
        else
            return null;
    }

    public OBRecord decipherLineMoreSpread(String line, OBRecord obr) {
        if (!useCharKey || 
                ((line.length() > charKeyColumn) && (line.charAt(charKeyColumn) == charKeyChar))) {
            obr.getFromChan().add(new Integer((int)SUtil.sval(line,
                    getFromChanStart(),
                    getFromChanEnd())));
            obr.getToChan().add(new Integer((int)SUtil.sval(line,
                    getToChanStart(),
                    getToChanEnd())));
            obr.getReceiverLineNumber().add(new Integer(receiverLineNumberAdd+(int)SUtil.sval(line,
                    getReceiverLineStart(),
                    getReceiverLineEnd())));
            obr.getFromReceiver().add(new Integer(receiverStationNumberAdd+(int)SUtil.sval(line,
                    getFromReceiverStationStartPos(),
                    getFromReceiverStationEndPos())));
            obr.getToReceiver().add(new Integer(receiverStationNumberAdd+(int)SUtil.sval(line,
                    getToReceiverStationStartPos(),
                    getToReceiverStationEndPos())));
            return obr;
        }
        else
            return obr;
    }

    public String toStringDetailed(String description) {
        return description+":"+"\n"+
        this.getInputFile()+"% \t station input file name"+"\n"+
        this.getCharKeyChar()+"% \t character key character"+"\n"+
        this.getCharKeyColumn()+"% \t column location of character key"+"\n"+
        this.getFirstLine()+"% \t first line of text to read"+"\n"+
        this.getLastLine()+"% \t last line of text to read"+"\n"+
        this.getFfidStartPos()+"% \t 1st column of FFID"+"\n"+
        this.getFfidEndPos()+"% \t last column of FFID"+"\n"+
        this.getSourceLineStart()+"% \t 1st column of shot line number"+"\n"+
        this.getSourceLineEnd()+"% \t last column of shot line number"+"\n"+
        this.getSourceStationStart()+"% \t 1st column of shot station number"+"\n"+
        this.getSourceStationEnd()+"% \t last column of shot station number"+"\n"+
        this.getFromChanStart()+"% \t 1st column of from channel"+"\n"+
        this.getFromChanEnd()+"% \t last column of from channel"+"\n"+
        this.getToChanStart()+"% \t 1st column of to channel"+"\n"+
        this.getToChanEnd()+"% \t last column of to channel"+"\n"+
        this.getReceiverLineStart()+"% \t 1st column of receiver line"+"\n"+
        this.getReceiverLineEnd()+"% \t last column of receiver line"+"\n"+
        this.getFromReceiverStationStartPos()+"% \t 1st column of from receiver station"+"\n"+
        this.getFromReceiverStationEndPos()+"% \t last column of from receiver station"+"\n"+
        this.getToReceiverStationStartPos()+"% \t 1st column of to receiver station"+"\n"+
        this.getToReceiverStationEndPos()+"% \t last column of to receiver station"+"\n"+
        this.getSourceLineNumberAdd()+"% \t constant to add to shot line number value"+"\n"+
        this.getSourceStationNumberAdd()+"% \t constant to add to shot station number value"+"\n"+
        this.getReceiverLineNumberAdd()+"% \t constant to add to receiver line number value"+"\n"+
        this.getReceiverStationNumberAdd()+"% \t constant to add to receiver station number value"+"\n"+
        this.getUseCharKey()+"% \t use character key? (1=true,0=false)"+"\n";
    }

    //Set FileKey based on "%" parsed text string. First segment is garbage - remaining segments must match the following...
    public OBFileKey setFileKeyFromString(String s){
        String[] percentParsedString = s.split("%");

        this.setInputFile(new File(percentParsedString[1]));
        this.setCharKeyChar((percentParsedString[2]).charAt(0));
        this.setCharKeyColumn((int)SUtil.sval(percentParsedString[3]));
        this.setFirstLine((int)SUtil.sval(percentParsedString[4]));
        this.setLastLine((int)SUtil.sval(percentParsedString[5]));
        this.setFfidStartPos((int)SUtil.sval(percentParsedString[6]));
        this.setFfidEndPos((int)SUtil.sval(percentParsedString[7]));
        this.setSourceLineStart((int)SUtil.sval(percentParsedString[8]));
        this.setSourceLineEnd((int)SUtil.sval(percentParsedString[9]));
        this.setSourceStationStart((int)SUtil.sval(percentParsedString[10]));
        this.setSourceStationEnd((int)SUtil.sval(percentParsedString[11]));
        this.setFromChanStart((int)SUtil.sval(percentParsedString[12]));
        this.setFromChanEnd((int)SUtil.sval(percentParsedString[13]));
        this.setToChanStart((int)SUtil.sval(percentParsedString[14]));
        this.setToChanEnd((int)SUtil.sval(percentParsedString[15]));
        this.setReceiverLineStart((int)SUtil.sval(percentParsedString[16]));
        this.setReceiverLineEnd((int)SUtil.sval(percentParsedString[17]));
        this.setFromReceiverStationStartPos((int)SUtil.sval(percentParsedString[18]));
        this.setFromReceiverStationEndPos((int)SUtil.sval(percentParsedString[19]));
        this.setToReceiverStationStartPos((int)SUtil.sval(percentParsedString[20]));
        this.setToReceiverStationEndPos((int)SUtil.sval(percentParsedString[21]));
        this.setSourceLineNumberAdd((int)SUtil.sval(percentParsedString[22]));
        this.setSourceStationNumberAdd((int)SUtil.sval(percentParsedString[23]));
        this.setReceiverLineNumberAdd((int)SUtil.sval(percentParsedString[24]));
        this.setReceiverStationNumberAdd((int)SUtil.sval(percentParsedString[25]));
        this.setUseCharKey(percentParsedString[26].equals("true"));

        return this;
    }

    public int getFfidEndPos() {
        return ffidEndPos;
    }

    public int getFfidStartPos() {
        return ffidStartPos;
    }

    public int getFromReceiverStationEndPos() {
        return fromReceiverStationEndPos;
    }

    public int getFromReceiverStationStartPos() {
        return fromReceiverStationStartPos;
    }

    public int getToReceiverStationEndPos() {
        return toReceiverStationEndPos;
    }

    public int getToReceiverStationStartPos() {
        return toReceiverStationStartPos;
    }

    public void setFfidEndPos(int num) {
        if (num>=0)ffidEndPos = num;
    }

    public void setFfidStartPos(int num) {
        if (num>=0)ffidStartPos = num;
    }
    public void setFfidEndPos(String num) {
        setFfidEndPos((int)SUtil.sval(num,0));
    }

    public void setFfidStartPos(String num) {
        setFfidStartPos((int)SUtil.sval(num,0));
    }

    public void setFromReceiverStationEndPos(String num) {
        setFromReceiverStationEndPos((int)SUtil.sval(num,0));
    }

    public void setFromReceiverStationStartPos(String num) {
        setFromReceiverStationStartPos((int)SUtil.sval(num,0));
    }

    public void setToReceiverStationEndPos(String num) {
        setToReceiverStationEndPos((int)SUtil.sval(num,0));
    }

    public void setToReceiverStationStartPos(String num) {
        setToReceiverStationStartPos((int)SUtil.sval(num,0));
    }
    public void setFromReceiverStationEndPos(int num) {
        if(num>=0)fromReceiverStationEndPos=num;
    }

    public void setFromReceiverStationStartPos(int num) {
        if(num>=0)fromReceiverStationStartPos=num;
    }

    public void setToReceiverStationEndPos(int num) {
        if(num>=0)toReceiverStationEndPos=num;
    }

    public void setToReceiverStationStartPos(int num) {
        if(num>=0)toReceiverStationStartPos=num;
    }

    public int getFfidAdd() {
        return ffidAdd;
    }

    public void setFfidAdd(int ffidAdd) {
        this.ffidAdd = ffidAdd;
    }

    public void setFfidAdd(String num) {
        this.ffidAdd = (int)SUtil.sval(num,0);
    }

    /**
     * Don't use this method!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */
    @Override
    public Station getNewStation()
    {
        System.err.println("OBFileKey.getNewStation() Illegal call to dysfunctional method!");
        return null;
    }

    public int getShotStartPos()
    {
        return shotStartPos;
    }

    public void setShotStartPos(int shotStartPos)
    {
        this.shotStartPos = shotStartPos;
    }

    public int getShotEndPos()
    {
        return shotEndPos;
    }

    public void setShotEndPos(int shotEndPos)
    {
        this.shotEndPos = shotEndPos;
    }

    public void setShotEndPos(String to)
    {
        this.shotEndPos = (int)SUtil.sval(to,0);
    }

    public void setShotStartPos(String from)
    {
        this.shotStartPos = (int)SUtil.sval(from,0);
    }


}