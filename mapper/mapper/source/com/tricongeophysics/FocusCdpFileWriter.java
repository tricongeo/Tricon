package com.tricongeophysics;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class FocusCdpFileWriter implements TriconFileWriter
{

    private CdpModel model;

    @Override
    public boolean writeFile(File f, DataDepot cdpModel)
    {
        this.model = (CdpModel) cdpModel;
        
        FileWriter writer;
        
        DecimalFormat df = new DecimalFormat("########");
        
        try {
            if (cdpModel != null) {
                writer = new FileWriter(f);
                writer.write("*** LINE3D CDP File created by Mapper v-" + Mapper.version + "\n");
                writer.write("*CALL   DUMIN\n");
                writer.write("*CALL   LINE3D  CDPS\n");
                writer.write("LOCN    ");
                writer.write(SUtil.stringResize(model.getFirstCDP()+"", 8));
                writer.write("        ");
                writer.write(SUtil.stringResize(df.format(model.getOriginX()), 8));
                writer.write(SUtil.stringResize(df.format(model.getOriginY()), 8));
                writer.write("        ");
                writer.write(SUtil.stringResize(model.getOriginXL()+"", 8));
                writer.write(SUtil.stringResize(model.getOriginIL()+"", 8));
                writer.write("\n");
                
                writer.write("AREA    ");
                writer.write(SUtil.stringResize(model.getNumXlines()+"", 8));
                writer.write(SUtil.stringResize(model.getXlInterval()+"", 8));
                writer.write(SUtil.stringResize(model.getNumInlines()+"", 8));
                writer.write(SUtil.stringResize(model.getIlInterval()+"", 8));
                writer.write(SUtil.stringResize(model.getXlIncrement()+"", 8));
                writer.write(SUtil.stringResize(model.getIlIncrement()+"", 8));
                writer.write(SUtil.stringResize(model.getAngle()+"", 8));
                writer.write("\n*END\n");
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    

}
