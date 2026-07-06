package com.tricongeophysics;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;


public class SaveFocusJob extends Job implements ProgressListener
{
    private Mapper mapper;
    private String project;
    private String line;
    private RcvrDbModel rModel;
    private FocusDbModel sModel;
    private int progressMax = 10;
    private FocusDbModel rMapModel;
    private MapperToFocusConverter converter;
    private FocusCDPModel cdpModel;
    private String notes;
    
    private Stopwatch receiverTimer = new Stopwatch("Get Receivers");
    private Stopwatch shotTimer = new Stopwatch("Get Shots");
    private Stopwatch mapTimer = new Stopwatch("Get Map");
    private Stopwatch totalTimer = new Stopwatch("Total Save Job");
    private Stopwatch loadData = new Stopwatch("Load Data");
    private Stopwatch saveData = new Stopwatch("Save Data");
    private Stopwatch saveReceivers = new Stopwatch("Save Receivers");
    private Stopwatch saveShots = new Stopwatch("Save Shots");
    private Stopwatch saveMap = new Stopwatch("Save Map");
    private String pgSurveyRoot;
    
    public SaveFocusJob(Mapper mapper, String project, String line, String pgSurveyRoot)
    {
        this.mapper = mapper;
        this.project = project;
        this.line = line;
        this.pgSurveyRoot = pgSurveyRoot;
    }

    @Override
    public void cancel()
    {
        // TODO Auto-generated method stub

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
            totalTimer.start();
            
            FocusGeometrySaver fgs = new FocusGeometrySaver(project, line, pgSurveyRoot);
            fgs.addProgressListener(this);
            fireProgressChanged(new ProgressEvent(this, "opening project", 1));
            fgs.loadProject();
            
            loadData.start();
            loadData(fgs);
            loadData.stop();
            
            saveData.start();
            saveData(fgs);
            saveData.stop();
            
            totalTimer.stop();
            printTimes();
        }
        catch (Exception e) {
            e.printStackTrace();
            showError(e.getMessage());
            releaseMemory();
            return;
        }
        releaseMemory();
    }

    private void printTimes()
    {
        totalTimer.printTime();
        loadData.printTime();
        receiverTimer.printTime();
        shotTimer.printTime();
        mapTimer.printTime();
        saveData.printTime();
        saveReceivers.printTime();
        saveShots.printTime();
        saveMap.printTime();
    }

    private void saveData(FocusGeometrySaver fgs) throws FocusDbIOException
    {
        if (rModel != null) {
            progressMax = rModel.getColumnCount();
            saveReceivers.start();
            fgs.saveModel(rModel, mapper);
            saveReceivers.stop();
        }
        if (sModel != null) {
            progressMax = sModel.getColumnCount();
            saveShots.start();
            fgs.saveModel(sModel, mapper);
            saveShots.stop();
        }
        if (rMapModel != null) {
            progressMax = rMapModel.getColumnCount();
            saveMap.start();
            if (fgs.saveModel(rMapModel, mapper)) {
                fgs.saveFile(converter.getRegularLabelFile(), "REGULAR LABEL");
                fgs.saveMapStatKey(converter.getMapStatkey());
            }
            saveMap.stop();
        }
        if (cdpModel != null) {
            if (fgs.saveModel(cdpModel, mapper)) {
                fgs.saveFile(cdpModel.getRegularCDP(), "REGULAR CDP");
                fgs.saveFile(cdpModel.getGeometryFile(), "GEOMETRY FILE");
                converter.applyBias(cdpModel); //reapply bias
            }
        }
        if (notes != null)
            fgs.saveLongFile(notes, "GEOMETRY NOTES");
        fgs.saveBias(converter.getBias());
    }

    private void releaseMemory()
    {
        converter = null;
        rModel = null;
        sModel = null;
        rMapModel = null;
        cdpModel = null;
        notes = null;
        System.gc();
    }

    private void showError(String message)
    {
        JOptionPane.showMessageDialog(mapper, "Save to Focus Error!\n"+message, 
                "Save to Focus Error", JOptionPane.ERROR_MESSAGE);
    }

    private void loadData(FocusGeometrySaver fgs)
    {
        converter = new MapperToFocusConverter(mapper, project, line);
        converter.addProgressChangedListener(this);
//        TriconGeometry tg2 = new TriconGeometry(project, line);
        
        try {
            progressMax = mapper.receiverList.getColumnCount();
            receiverTimer.start();
            rModel = converter.getReceiverModel();
            receiverTimer.stop();
            
            progressMax = mapper.spList.getColumnCount();
            shotTimer.start();
            sModel = converter.getShotModel();
            shotTimer.stop();
            
            progressMax= mapper.receiverList.size();
            mapTimer.start();
            rMapModel = converter.getStationMapModel();
            mapTimer.stop();
            
            cdpModel = converter.getCdpModel();
            notes = mapper.getNotes();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mapper, e.getMessage(), "Failed to save to Focus", JOptionPane.ERROR_MESSAGE);
        }
//        ShotDbModel sModel = fgs.getGeometry().getShotModel();
        
//        ReflectiveTableModel slist = mapper.spList;
//        String[] sattrs = slist.columnNames;
        System.gc();
    }

    @Override
    public boolean getIndeterminate()
    {
        return false;
    }

    static public void main (String[] args) {
        Mapper m = new Mapper();
        m.go();
        m.openProject(new File("/home/scott/pauls3d.mpr3"));
        m.saveFocusProject("SCOTT3D", "TEST", "/seisdata");
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
