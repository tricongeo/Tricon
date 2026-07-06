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
public class Mapper3d extends Mapper {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private final static String version3d = "3D 3.3";

    public static void main(final String[] args) {
        Mapper.version = version3d;
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
                Mapper3d mapper = new Mapper3d();
                mapper.go();
                mapper.processArgs(args);
            }
        });
        
        SUtil.print("Running Mapper version - " + version);
        SUtil.print("\nJava version - " + System.getProperty("java.version"));
    }

    public Mapper3d() {
        super();
    }
    
    

} // close Mapper class
