package com.tricongeophysics;

import javax.swing.JOptionPane;

public class ElevationStaticsJob extends Job
{
    private ReflectiveTableModel shots;
    private ReflectiveTableModel recs;
    private int da;
    private int vc;
    private String name;
    private boolean useUh;
    private boolean useDepth;
    private int nShots;
    private int nRecs;
    private int progressMax;

    public ElevationStaticsJob(ReflectiveTableModel spList, ReflectiveTableModel receiverList, ElevStaticsDialog elevDialog)
    {
        shots = spList;
        recs = receiverList;
        da = elevDialog.getDatumElevation();
        vc = elevDialog.getCorrectionalVelocity();
        name = elevDialog.getStaticsName();
        useUh = elevDialog.getUseUpholes();
        useDepth = elevDialog.getUseShotDepth();
        
        nShots = shots.size();
        nRecs = recs.size();
        progressMax = nShots;
    }

    @Override
    public int getProgressMax()
    {
       return progressMax;
    }

    @Override
    public void cancel()
    {
        fireJobFinished();
    }

    @Override
    protected void doJob()
    {
        Object[] zs = shots.getValues("Z");
        Object[] depths = shots.getValues("Depth");
        Object[] uhs = shots.getValues("Uphole");
        if (zs == null) {
        	fireInsufficientData("Elevations");
        	return;
        }
        if (depths == null && useDepth) {
        	fireInsufficientData("Depths");
            return;
        }
        if (uhs == null && useUh) {
            fireInsufficientData("Upholes");
            return;
        }
        shots.addColumn(name, Double.class);
        double d = 0;
        double uh = 0;
        
        //...Loop over shots
        progressMax = nShots;
        fireProgressChanged(new ProgressEvent(this, "Calculating Shot Statics", 1));
        for (int i=0; i<nShots; i++) {
        	Object o = shots.get(i);
        	SP sp = (SP) o;
        	if (sp.kill) continue;
            double elev = (Double) zs[i];
            if (depths != null) d = (Double) depths[i];
            if (uhs != null) uh = (Double) uhs[i];
            double statik = calcStatic(elev, d, uh);
            shots.setValueAt(statik, i, name);
            if (i%10 == 0) fireProgressChanged(new ProgressEvent(this, null, i));
        }
        //...Loop over Receivers
        progressMax = nRecs;
        fireProgressChanged(new ProgressEvent(this, "Calculating Receiver Statics", 1));
        recs.addColumn(name, Double.class);
        for (int i=0; i<nRecs; i++) {
            Object o = recs.get(i);
            Receiver r = (Receiver) o;
            if (r.kill) continue;
            double statik = calcStatic(r.z, 0, 0);
            recs.setValueAt(statik, i, name);
            if (i%10 == 0) fireProgressChanged(new ProgressEvent(this, null, i));
        }
    }

    private void fireInsufficientData(String dataName)
    {
    	JOptionPane.showMessageDialog(null, "Missing " + dataName,
                "Calculate statics error", JOptionPane.ERROR_MESSAGE);
    	cancel();
    }

    private double calcStatic(double elev, double d, double uh)
    {
        double statik = (da - elev)/vc;
        if (this.useDepth) statik = statik - d/vc;
        if (this.useUh) statik = statik + uh;
        return statik*1000.0;  //vc is in m/sec, so need to convert to milliseconds
    }

    @Override
    public boolean getIndeterminate()
    {
        return false;
    }

}
