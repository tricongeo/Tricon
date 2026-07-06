package com.tricongeophysics;

import java.io.File;

import com.tricongeophysics.MapperInputFilesPane.DataChanged;

public class OpenOldProjectJob extends Job
{
    private File file;
    private MapperInputFilesPane inputFilesPane;

    OpenOldProjectJob(MapperInputFilesPane inputFilesPane, File file2)
    {
        this.inputFilesPane = inputFilesPane;
        this.file = file2;
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
        inputFilesPane.recoverProject(file); // read text file and reset combo file key lists
        inputFilesPane.updateKeyLists();
    }

    @Override
    public boolean getIndeterminate()
    {
        return true;
    }

}
