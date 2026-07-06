package com.tricongeophysics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SpsFileWriter extends MapperAbstractFileWriter
{

    private int killedReceivers;
    private int killedShotsPoints;
    private boolean outputShotKills;
    private int killedShots;

    @Override
    protected boolean writeData()
    {
        
     // write out Segp-1 file of all shot/receiver stations
        /**
         * 
         */
       
            if (receiverList == null && spList == null && obList == null) return false;
            killedShotsPoints = 0; killedReceivers = 0;
            String rootFileName = "";
            int index = file.getAbsolutePath().indexOf(".");
            if (index >= 0)
                rootFileName = file.getAbsolutePath().substring(0, index);
            else
                rootFileName = file.getAbsolutePath();
            // String rootFileName = f.getName()f.(".",0)[0]; //get rid of anything
            // past the "." in the filename
            File spsfile = new File(rootFileName + ".sps");
            File rpsfile = new File(rootFileName + ".rps");
            File xpsfile = new File(rootFileName + ".xps");
            File sumfile = new File(rootFileName + ".sum");
            try {
                FileWriter writer;
                
                // write receivers
                if (receiverList != null) {
                    writer = new FileWriter(rpsfile);
                    writer.write("H SEGP-1 File created by Mapper v-" + version + "\n");
                    writer.write("H Receiver Station Files:\n");
                    for (int ii = 0; ii < receiverFileKeyList.length; ii++)
                        writer.write("H " + receiverFileKeyList[ii].getInputFile() + "\n");
                    if ( receiverList.size()>0 ) {
                        writer.write( ((Station) receiverList.get(0)).getSegp1Header() );
                    }
                    for (int ii = 0; ii < receiverList.size(); ii++) {
                        Receiver station = (Receiver) receiverList.get(ii);
                        if (station.kill) {
                            killedReceivers++;
                            if (!outputKills)
                                continue;
                        }
                        writer.write(station.toSegp1());
                    }
                    writer.close();
                }

                // write shots
                if (spList != null) {
                    writer = new FileWriter(spsfile);
                    writer.write("H SEGP-1 File created by Mapper v-" + version + "\n");
                    writer.write("H Shot Point Files:\n");
                    for (int ii = 0; ii < shotFileKeyList.length; ii++)
                        writer.write("H " + shotFileKeyList[ii].getInputFile() + "\n");
                    if ( spList.size()>0 ) {
                        writer.write( ((Station) spList.get(0)).getSegp1Header() );
                    }
                    for (int ii = 0; ii < spList.size(); ii++) {
                        SP station = (SP) spList.get(ii);
                        if (station.kill) {
                            killedShotsPoints++;
                            if (!outputKills)
                                continue;
                        }
                        writer.write(station.toSegp1());
                    }
                    writer.close();
                }

                // write obs
                if (obList != null) {
                    writer = new FileWriter(xpsfile);
                    writer.write("H Relation File created by Mapper v-" + version + "\n");
                    writer.write("H OB Files:\n");
                    for (int ii = 0; ii < obFileKeyList.length; ii++)
                        writer.write("H " + obFileKeyList[ii].getInputFile() + "\n");
                    if ( obList.size()>0 ) {
                        writer.write( ((OBRecord) obList.get(0)).getSegp1Header() );
                    }
                    for (int ii = 0; ii < obList.size(); ii++) {
                        OBRecord record = (OBRecord) obList.get(ii);
                        if (record.getKill()) {
                            killedShots++;
                            if (!outputShotKills){
                                continue;
                            }
                        }
                        writer.write(record.toSegp1());
                    }
                    writer.close();
                }

                // write sum file
                if (obList != null) {
                    writer = new FileWriter(sumfile);
                    writer.write("H OB Files:\n");
                    for (int ii = 0; ii < obFileKeyList.length; ii++)
                        writer.write("H " + obFileKeyList[ii].getInputFile() + "\n");
                    if ( obList.size()>0 ) {
                        writer.write( ((OBRecord) obList.get(0)).getSumHeader() );
                    }
                    for (int ii = 0; ii < obList.size(); ii++) {
                        OBRecord record = (OBRecord) obList.get(ii);
                        if (record.getKill() && !outputShotKills) continue;
                        writer.write(record.toSumFile());
                    }
                    writer.close();
                }
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }

    /**
     * sets whether killed stations are output
     * @param outputKills
     */
    public void setOutputStationKills(boolean outputKills)
    {
        this.outputKills = outputKills;
    }

    public int getKilledShotPoints()
    {
        return killedShotsPoints;
    }

    public int getKilledReceivers()
    {
        return killedReceivers;
    }

    /**
     * sets whether killed shot records are output
     * @param outputShotKills
     */
    public void setOutputShotKills(boolean outputShotKills)
    {
        this.outputShotKills = outputShotKills;
    }

    public int getKilledShots()
    {
        return this.killedShots;
    }

}
