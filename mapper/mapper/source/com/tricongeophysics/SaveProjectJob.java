package com.tricongeophysics;

public class SaveProjectJob extends Job
{

    private Mapper mapper;
    private TriconFile file;

    public SaveProjectJob(Mapper mapper, TriconFile file)
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
        MapperProject.serialize(file, mapper);
    }

    @Override
    public boolean getIndeterminate()
    {
        return true;
    }

}
