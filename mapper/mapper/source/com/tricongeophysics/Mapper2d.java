package com.tricongeophysics;

import java.util.*;
import java.awt.event.*;

import javax.management.RuntimeErrorException;
import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.TableModel;

import com.tricongeophysics.MapperInputFilesPane.DataChanged;
import com.tricongeophysics.SpatialEditorJob.EditType;

/**
 * Mapper2d
 * 
 * @author scott
 *
 */
public class Mapper2d extends Mapper {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private final static String version2d = "2D 0.1";

    public static void main(final String[] args) {
        Mapper.version = version2d;
        try { // set Nimbus look and feel (CDE/Motif look and feel is lame!)
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            defaults.remove("Table.alternateRowColor");
            defaults.put("Table.showGrid", Boolean.TRUE);
            defaults.put("Table.gridColor", new ColorUIResource(203, 209, 216));
            defaults.put("Table.intercellSpacing", new DimensionUIResource(1,1));

        } catch (Exception e) {
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() { // encapsulate in "run" method for thread safety
                Mapper2d mapper = new Mapper2d();
                mapper.go();
                mapper.processArgs(args);
            }
        });
        
        SUtil.print("Running Mapper version - " + version);
        SUtil.print("\nJava version - " + System.getProperty("java.version"));
    }

    public Mapper2d() {
        super();
    }
    
    public void setStatisticsLabel() {
        statisticsLabel
                .setText("<HTML>Statistics>>  receiver points= <font color=00af00>"
                        + receiverList.size()
                        + "</font>, shot points= <font color=red>"
                        + spList.size()
                        + "</font>, shot records= <font color=blue>"
                        + obList.size()
                        + "</font>, receiver files= <font color=00af00>"
                        + inputFilesPane.getReceiverFileCount()
                        + "</font>, shot files= <font color=red>"
                        + inputFilesPane.getShotFileCount()
                        + "</font>, OB files= <font color=blue>"
                        + inputFilesPane.getObFileCount());
    }

    /**
     * Not supported in 2d!!
     */
    public void runGeoBuild() {
       throw new RuntimeErrorException(null);
    }

    /**
     * Not supported in 2d!!
     * 
     * @param spawnProcess
     * @param file2
     */
    public void runGbuild(SpawnProcess spawnProcess, TriconFile file2) {
        throw new RuntimeErrorException(null);
    }

    /**
     * Not supported in 2d!!
     * 
     * @param f
     * @return
     */
    public boolean writeParmFiles(File f) {
        throw new RuntimeErrorException(null);
    }
   
    /**
     * Not supported in 2d!!
     */
    @Override
    public void runProgram() {
        throw new RuntimeErrorException(null);
    }

} // close Mapper class
