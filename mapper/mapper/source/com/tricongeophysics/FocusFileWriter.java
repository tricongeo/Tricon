package com.tricongeophysics;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class FocusFileWriter extends MapperAbstractFileWriter
{

    private static final String ShotExt = "_shtstat_edits.dat";
    private static final String RcvExt = "_recstat_edits.dat";
    private static final String RecordExt = "_shot_edits.dat";
    
    private File receiverfile;
    private File shotfile;
    private File recordfile;
    
    private enum Type { ShotStation, Receiver };

    @Override
    protected boolean writeData()
    {
        if (receiverList == null && spList == null && obList == null) return false;
        String rootFileName = "";
        int index = file.getAbsolutePath().indexOf(".");
        if (index >= 0)
            rootFileName = file.getAbsolutePath().substring(0, index);
        else
            rootFileName = file.getAbsolutePath();
        // String rootFileName = f.getName()f.(".",0)[0]; //get rid of anything
        // past the "." in the filename
        shotfile = new File(rootFileName + ShotExt);
        receiverfile = new File(rootFileName + RcvExt);
        recordfile = new File(rootFileName + RecordExt);
        FileWriter writer;

        // write receivers
        if (!writeStations(receiverList, receiverfile, Type.Receiver)) {
            return false;
        }

        // write shots
        if (!writeStations(spList, shotfile, Type.ShotStation)) {
            return false;
        }

        // write obs
        try {
            int counter = 0;
            if (obList != null) {
                writer = new FileWriter(recordfile);
                writer.write("*** SHOT Edit File created by Mapper v-" + version + "\n");
                writer.write("*CALL   EDIT    SHOT    chan    \n" +
                "ALL\n");
                for (TableData td: obList) {
                    OBRecord record = (OBRecord) td;
                    if (record.getKill()) {
                        String shot = record.getShot()+"";
                        shot = SUtil.stringResize(shot, 8);
                        writer.write(shot);
                        counter++;
                        if (counter%8 == 0) writer.write("\n");
                    }
                }
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Writes killed stations to EDIT job
     * @param stations
     * @param file
     * @param type
     * @return
     */
    private boolean writeStations(ArrayList<TableData> stations, File file, Type type)
    {
        int counter = 0;
        if (stations == null) return false;
        String s;
        if (type == Type.Receiver) s = "rec";
        else s = "sht";
        try {
            FileWriter writer = new FileWriter(file);
            writer.write("*** "+type+" Edit File created by Mapper v-" + version + "\n");
            writer.write("*CALL   HDRMATH AUX   \n"+  
                    "HCMUL   "+s+"line 10000   "+s+"ls   \n" +
                    "HHADD   "+s+"stn  "+s+"ls   "+s+"ls   \n" +
                    "*CALL   EDIT    "+s+"ls   chan    \n" +
            "ALL\n");

            for (TableData td: stations) {
                Station station = (Station) td;
                if (station.kill) {
                    String stn = station.lineNumber*10000+station.stationNumber+"";
                    stn = SUtil.stringResize(stn, 8);
                    writer.write(stn);
                    counter++;
                    if (counter%8 == 0) writer.write("\n");
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
