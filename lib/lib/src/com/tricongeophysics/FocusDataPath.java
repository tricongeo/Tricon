package com.tricongeophysics;

import java.io.File;


public class FocusDataPath
{
    private File dataPath;
    private String pathAlias;

    public File getDataPath()
    {
        return dataPath;
    }

    public String name()
    {
        return dataPath.getAbsolutePath();
    }

    public String getPathAlias()
    {
        return pathAlias;
    }

    public FocusDataPath(File path, String alias)
    {
        dataPath = path;
        pathAlias = alias;
    }

    public boolean exists()
    {
        if (dataPath != null)
        {
            return dataPath.isDirectory();
        }
        else
        {
            return false;
        }
    }

    public String toString()
    {
        return dataPath.getAbsolutePath() + " = " + pathAlias;
    }
}
