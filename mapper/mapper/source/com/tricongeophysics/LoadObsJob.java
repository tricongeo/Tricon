package com.tricongeophysics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.tricongeophysics.MapperInputFilesPane.MapperComboBox;

public class LoadObsJob extends Job
{

    private MapperComboBox fileKeyList;
    private ArrayList<TableData> obList;

    public LoadObsJob(MapperComboBox fileKeyList)
    {
        this.fileKeyList = fileKeyList;
    }

    @Override
    public void cancel() { }

    @Override
    protected void doJob()
    {
        if (fileKeyList == null) return;
        obList = new ArrayList<TableData>();
        obList.ensureCapacity(5000);
        int i;
        for (i = 0; i < fileKeyList.getItemCount(); i++) {
            OBFileKey obFileKey = (OBFileKey) fileKeyList.getItemAt(i);
            int linesRead = -1;
            if (obFileKey.getInputFile() == null) return;
            if (new File(obFileKey.getInputFile()).exists()) {
                try {
                    //Cursor oldCursor = getCursor();
                    //setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    //String line = new String();
                    String line = null;
                    BufferedReader reader = new BufferedReader(new FileReader(
                            obFileKey.getInputFile()));
                    OBRecord oldRecord = new OBRecord();
                    while ((line = reader.readLine()) != null) {
                        linesRead++;
                        if ((linesRead >= obFileKey.getFirstLine()) && (linesRead <= obFileKey.getLastLine())) {
                            OBRecord obr = obFileKey.decipherLineNewShot(line); // 1st try new shot
                            if(oldRecord.equals(obr)) {// if not new shot, add more spread (new shot determined if either FFID or ShotPoint are different)
                                obFileKey.decipherLineMoreSpread(line, oldRecord);
                                //obList.remove(obList.size() - 1);
                                //obList.add(obr);
                            } else if (obr != null) {
                                if (obr.survey == 0) obr.setSurvey(i+1);
                                obList.add(obr);
                                oldRecord = obr;
                            }
                        }
                    }
                    //obList.add(oldRecord);
                    reader.close();
                    //setCursor(oldCursor);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        obList.trimToSize(); // get rid of any unused elements that may be there
    }

    @Override
    public boolean getIndeterminate()
    {
        return true;
    }

    @Override
    public int getProgressMax()
    {
        return 10;
    }

    public ArrayList<TableData> getObs()
    {
        return obList;
    }

}
