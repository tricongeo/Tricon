package com.tricongeophysics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.JComboBox;

public class LoadStationsJob extends Job
{

    private JComboBox fileKeyList;
    private ArrayList<TableData> sList;

    public LoadStationsJob(JComboBox fileKeyList)
    {
        this.fileKeyList = fileKeyList;
    }

    @Override
    public void cancel() { }

    @Override
    protected void doJob()
    {
        sList = new ArrayList<TableData>();
        String line = null;
        int i;
        for (i = 0; i < fileKeyList.getItemCount(); i++) {
            StationFileKey fileKey = (StationFileKey) fileKeyList.getItemAt(i);
            int linesRead = -1;
            if (new File(fileKey.getInputFile()).exists()) {
                try {
                    //Cursor oldCursor = getCursor();
                    //setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    BufferedReader reader = new BufferedReader(new FileReader(
                            fileKey.getInputFile()));
                    while ((line = reader.readLine()) != null) {
                        linesRead++;
                        if ((linesRead >= fileKey.getFirstLine())
                                && (linesRead <= fileKey.getLastLine())) {
                            Station thisStation = fileKey.decipherLine(line);
                            if (thisStation != null) {
                                if (thisStation.survey == 0) thisStation.setSurvey(i+1);
                                sList.add(thisStation);
                            }
                        }
                        //setCursor(oldCursor);
                    }
                } catch (Exception ex) {
                    System.out.println("couldn't read the xy file"
                            + fileKey.getInputFile());
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean getIndeterminate()
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public int getProgressMax()
    {
        // TODO Auto-generated method stub
        return 10;
    }

    public ArrayList<TableData> getStations()
    {
        return sList;
    }
    
}
