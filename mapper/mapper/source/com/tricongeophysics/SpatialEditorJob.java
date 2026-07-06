package com.tricongeophysics;

public class SpatialEditorJob extends Job
{
    private ReflectiveTableModel shotModel;
    private ReflectiveTableModel recModel;
    enum EditType {Smooth, Interpolate};
    private EditType type;
    private int maxProgress;
    private String columnName;
    private int progress = 0;
    private boolean canceled = false;

    public SpatialEditorJob(Mapper mapper, EditType type, String column)
    {
        super();
        shotModel = mapper.spList;
        recModel = mapper.receiverList;
        this.columnName = column;
        this.type = type;
        maxProgress = shotModel.size() + recModel.size();
    }

    @Override
    public int getProgressMax()
    {
        return maxProgress;
    }

    @Override
    public void cancel()
    {
        canceled = true;
        this.fireJobFinished();
    }

    @Override
    protected void doJob()
    {
        smooth(shotModel);
        smooth(recModel);
    }

    private void smooth(ReflectiveTableModel model)
    {
        for (int i=0; i<model.size(); i++) {
            if (canceled ) return;
            Station s = (Station) model.get(i);
            double v = 0;
            double weightFactor = 0;
            int total = 0;
            //...Loop through shot points
            for (int j=0; j<shotModel.size(); j++) {
                Object val = shotModel.getValueAt(j, columnName);
                if (val == null) continue;
                SP s2 = (SP) shotModel.get(j);
                double d = SUtil.getNumVal(val);
                double distance = SUtil.distance(s.x, s.y, s2.x, s2.y);
                if (distance > 0)  {
                    v += d/(distance*distance);
                    weightFactor += 1/(distance*distance);
                    total++;
                }
            }
            //...Loop through receivers
            for (int j=0; j<recModel.size(); j++) {
                Object val = recModel.getValueAt(j, columnName);
                if (val == null) continue;
                Station s2 = (Station) recModel.get(j);
                double d= SUtil.getNumVal(val);
                double distance = SUtil.distance(s.x, s.y, s2.x, s2.y);
                if (distance > 0)  {
                    v += d/(distance*distance);
                    weightFactor += 1/(distance*distance);
                    total++;
                }
            }
            if (total == 0) continue;
            v = v/total;
            weightFactor = weightFactor/total;
            v = v/weightFactor;
            Object old = model.getValueAt(i, columnName);
            Object out = SUtil.ObjectToType(old.getClass(), v);
//            Object out = old.getClass().cast(v);
            model.setValueAt(out, i, columnName);
            this.fireProgressChanged(new ProgressEvent(this, null, progress ++));
        }
    }

//    private Station[] getClosestStations(int i, ReflectiveTableModel model, double[] distances, Station[] closestStations)
//    {
//        
//        return closestStations;
//    }

    @Override
    public boolean getIndeterminate()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
