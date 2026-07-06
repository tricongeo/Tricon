package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class QCGeomPanel extends JPanel implements JobFinishedListener {

	private JobStatusPanel processShotRecordsPanel;
	private JLabel label;
	private ReflectiveTableModel receiverList;
	private ReflectiveTableModel spList;
	private ReflectiveTableModel obList;
    private JCheckBox interpUnsurveyedBox;
    protected boolean interpUnsurveyed = false;
    private ProcessShotRecordsJob processShotRecordsJob;

	QCGeomPanel() {
		this.setName(" Run QC");
		label = new JLabel("Geometry Analysis");
		
		processShotRecordsJob = new ProcessShotRecordsJob(obList, receiverList, spList);
		
		processShotRecordsPanel = new JobStatusPanel(processShotRecordsJob);
		processShotRecordsPanel.addJobFinishedListener(this);
		
		interpUnsurveyedBox = new JCheckBox("Interpolate UnSurveyed Stations");
		interpUnsurveyedBox.setSelected(interpUnsurveyed);
		interpUnsurveyedBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                interpUnsurveyed = !interpUnsurveyed;
                setInterp();
            }});
		
		this.setLayout(new BorderLayout());
		this.add(label, BorderLayout.NORTH);
		this.add(processShotRecordsPanel, BorderLayout.CENTER);
		this.add(interpUnsurveyedBox, BorderLayout.SOUTH);
	}

	protected void setInterp()
    {
	    processShotRecordsJob = (ProcessShotRecordsJob) processShotRecordsPanel.getJob();
        processShotRecordsJob.setInterpUnsurveyed(interpUnsurveyed);
    }

    public void setReceivers(ReflectiveTableModel receiverList) {
		this.receiverList = receiverList;
		processShotRecordsPanel.setJob(new ProcessShotRecordsJob(obList, receiverList, spList));
		setInterp();
	}
	
	public void setShotPoints(ReflectiveTableModel spList) {
		this.spList = spList;
		processShotRecordsPanel.setJob(new ProcessShotRecordsJob(obList, receiverList, spList));
		setInterp();
	}

	public void setShotRecords(ReflectiveTableModel obList) {
		this.obList = obList;
		processShotRecordsPanel.setJob(new ProcessShotRecordsJob(obList, receiverList, spList));
		setInterp();
	}

    @Override
    public void jobFinished(Job job)
    {
        if (receiverList != null)receiverList.fireTableDataChanged();
        if (spList != null)spList.fireTableDataChanged();
        if (obList != null)obList.fireTableDataChanged();
        writeLogFile();
    }

    private void writeLogFile()
    {
        String file = Mapper.getFile();
        //TODO should figure out how to do this some time - but there won't necessarily be a project name at this point.
    }

	
}
