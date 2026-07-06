package com.tricongeophysics;

import java.awt.Container;
import java.util.ArrayList;

import javax.swing.JOptionPane;


public class OpenFocusJob extends Job implements ProgressListener
{

    private Mapper mapper;
    private String project;
    private String line;
    private int progressMax = 10;
    private FocusGeometryLoader fgl;
    private Stopwatch recTimer = new Stopwatch("Convert receivers Time");
    private Stopwatch shotTimer = new Stopwatch("Convert shots Time");
    private Stopwatch openTimer = new Stopwatch("Total Open Time");
    private MapperToFocusConverter converter;
    private Stopwatch loaderTimer = new Stopwatch("FGL Total Time");
    private Stopwatch converterTimer = new Stopwatch("Converter Total Time");
    private Stopwatch restoreTimer = new Stopwatch("Mapper Restore Time");
    private String pgSurveyRoot;

    public OpenFocusJob(Mapper mapper, String project, String line, String pgSurveyRoot)
    {
        this.mapper = mapper;
        this.project = project;
        this.line = line;
        this.pgSurveyRoot = pgSurveyRoot;
    }

    @Override
    public void cancel()
    {
        if (converter != null) converter.cancelJob();
        fgl.cancelJob();
        fireJobFinished();
    }

    @Override
    public int getProgressMax()
    {
        return progressMax ;
    }

    @Override
    protected void doJob()
    {
        try {
            openTimer.start();
            fgl = new FocusGeometryLoader(project, line, pgSurveyRoot);
            fgl.addProgressListener(this);
            
            //...Open project
            fireProgressChanged(new ProgressEvent(this, "opening project", 1));
            fgl.loadProject();
            
            //...Load raw data from Focus
            loaderTimer.start();
            fireProgressChanged(new ProgressEvent(this, "Getting Raw Focus Data", 2));
            fgl.loadLine();
            loaderTimer.stop();
            
            //...Check if data was found
            if (!fgl.hasReceivers()) {
                showError("No Station Model Found!");
            }
            if (!fgl.hasShots()) {
                showError("No SHOT Model Found!");
            }
            if (!fgl.is3D()) {
                showError("Geometry Not 3D!");
                return;
            }
            
            //...Convert data to Mapper
            converterTimer.start();
            MapperProject mp = new MapperProject(mapper);
            loadData(fgl,mp);
            converterTimer.stop();
            
            //...Update Mapper Project with New Data
            restoreTimer.start();
            mp.restoreProject(mapper);
            restoreTimer.stop();
            
            //...Done
            openTimer.stop();
            printTimes();
            fgl.printTimes(); 
            restoreTimer.printTime();
            System.out.println("java done\n");
        }
        catch (Exception e) {
        	e.printStackTrace();
        	showError(e.getMessage());
            return;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            showError(e.getMessage());
            return;
        }
    }

    private void printTimes()
    {
        openTimer.printTime();
        converterTimer.printTime();
        shotTimer.printTime();
        recTimer.printTime();
        loaderTimer.printTime();
    }

    private void showError(String message)
    {
        JOptionPane.showMessageDialog(mapper, "Project Open Error!\n"+message, 
                "Open Project Error", JOptionPane.ERROR_MESSAGE);
    }

    private void loadData(FocusGeometryLoader fgl, MapperProject mp)
    {
        converter = new MapperToFocusConverter(mapper, project, line);
        converter.addProgressChangedListener(this);
        
        RcvrDbModel rModel = fgl.getGeometry().getRcvrModel();
        ShotDbModel sModel = fgl.getGeometry().getShotModel();
        FocusCDPModel cdpModel = fgl.getGeometry().getCdpModel();
        FocusBias focusBias = fgl.getGeometry().getBias();
        String notes = fgl.getNotes();
        
        //...Load Receivers
        recTimer.start();
        ArrayList<TableData> rlist = new ArrayList<TableData>();
        converter.convertReceiversToMapper(focusBias, rlist , rModel);
        mp.setReceiverList(rlist);
        recTimer.stop();
        
        //...Load Shots
        shotTimer.start();
        ArrayList<TableData> slist = new ArrayList<TableData>();
        ArrayList<TableData> oblist = new ArrayList<TableData>();
        OBRecord[] relationObs = fgl.getRelationObs();
        converter.convertShotsToMapper(focusBias, slist , sModel, oblist, relationObs);
        shotTimer.stop();
        
        //...Load CDPs
        progressMax = 10;
        fireProgressChanged(new ProgressEvent(this, "Loading CDPs", 1));
        CdpModel m = cdpModel.getMapperCdpModel();
        focusBias.applyBias(m);
        fireProgressChanged(new ProgressEvent(this, null, 10));
        
        mp.setSpList(slist);
        mp.setObList(oblist);
        mp.setCdpModel(m);
        mp.setNotes(notes);
    }

    @Override
    public boolean getIndeterminate()
    {
        return false;
    }

    @Override
    public void progressChanged(ProgressEvent e)
    {
        if (e instanceof ProgressMaxChanged) {
            progressMax = e.getProgressVal();
            e.setProgressVal(0);
        }
        fireProgressChanged(e);
    }

}
