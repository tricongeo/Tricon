package com.tricongeophysics;

import java.io.File;
import java.util.ArrayList;

public abstract class MapperAbstractFileWriter implements TriconFileWriter
{

    protected Object dataDepot;
    protected File file;
    
    private MapperProject mapperProject;
    protected ArrayList<TableData> obList;
    protected  ArrayList<TableData> spList;
    protected ArrayList<TableData> receiverList;
    protected String version;
    protected boolean outputKills;
    protected OBFileKey[] obFileKeyList;
    protected FileKey[] shotFileKeyList;
    protected FileKey[] receiverFileKeyList;

    @Override
    public boolean writeFile(File f, DataDepot dataDepot)
    {
        this.file = f;
        getData(dataDepot);
        return writeData();
    }

    protected abstract boolean writeData();

    private void getData(DataDepot dataDepot)
    {
        this.dataDepot = dataDepot.getData();
        loadProject();
    }
    private void loadProject()
    {
        this.mapperProject = (MapperProject) this.dataDepot;
        this.obList = mapperProject.getObList();
        this.spList = mapperProject.getSpList();
        this.receiverList = mapperProject.getReceiverList();
        this.version = mapperProject.getVersion();
        this.obFileKeyList = mapperProject.getObFileKeyList();
        this.shotFileKeyList = mapperProject.getShotFileKeyList();
        this.receiverFileKeyList = mapperProject.getReceiverFileKeyList();
    }

}
