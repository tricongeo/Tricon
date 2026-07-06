package com.tricongeophysics;

import java.io.File;
import java.io.InvalidClassException;

import javax.swing.JOptionPane;

public class OpenProjectJob extends Job
{

    private Mapper mapper;
    private File file;

    public OpenProjectJob(Mapper mapper, File file)
    {
        this.mapper = mapper;
        this.file = file;
    }

    @Override
    public void cancel()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int getProgressMax()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void doJob()
    {
        MapperProject project = null;
        try {
            project = MapperProject.unSerialize(file);
        } catch (InvalidClassException e) {
            JOptionPane.showMessageDialog(mapper, "Previous project is not compatible with this mapper version!\n"+e, 
                    "Open Project Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mapper, "Failed to open project file: " + file + "!\n"+e, 
                    "Open Project Error", JOptionPane.ERROR_MESSAGE);
        }
        if (project == null) return;
        project.restoreProject(mapper);
        System.gc();
    }

    @Override
    public boolean getIndeterminate()
    {
        return true;
    }

}
