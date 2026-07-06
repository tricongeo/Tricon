package com.tricongeophysics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;

public class JobProgressMonitor extends JDialog implements JobFinishedListener, ProgressListener, ActionListener
{

    public JProgressBar myBar;
    private boolean finished;
    private JPanel background;
    private JLabel label;
    private Job job;
    private StopButton cancelButton;
    private long startTime;

    public JobProgressMonitor(Component parentComponent, Job job, String message)
    {
        super();
        this.setLocationRelativeTo(parentComponent);
        this.job = job;
        
        myBar = new JProgressBar();
        myBar.setIndeterminate(job.getIndeterminate());
        if(!myBar.isIndeterminate()) {
            myBar.setStringPainted(true);
            myBar.setMaximum(job.getProgressMax());
            myBar.setMinimum(job.getProgressMin());
            job.addProgressListener(this);
            cancelButton = new StopButton();
            cancelButton.addActionListener(this);
        }
        
        job.addJobFinishedListener(this);
        
        label = new JLabel(message);
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        
        JPanel barPanel = new JPanel();
        barPanel.add(myBar);
        if (cancelButton != null) barPanel.add(cancelButton);
        
        background = new JPanel();
        background.setLayout(new BoxLayout(background, BoxLayout.Y_AXIS));
        background.add(label);
        background.add(barPanel);
        
        getContentPane().add(background);
        //setSize(800,800);
        pack();
        setVisible(true);
        startTime = System.currentTimeMillis();
        job.startJob();
    }
    
    public void finished() {
        finished = true;
        //this.setVisible(false);
        this.dispose();
    }

    public void setProgress(int progress)
    {
    	myBar.setMaximum(job.getProgressMax());
        myBar.setValue(progress);
        long tr = calcTimeRemaining(progress);
        setNote("Remaining: "+SUtil.formatTime(tr));
    }

    private long calcTimeRemaining(int progress)
    {
        if (progress < 1) return 0; //progress can't be zero
        long now = System.currentTimeMillis();
        long elapsed = now - startTime;
        long totalTime = elapsed*job.getProgressMax()/progress;
        return totalTime - elapsed;
    }

    public void setNote(String message)
    {
        myBar.setStringPainted(true);
        myBar.setString(message);
    }

    public boolean isCanceled()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void jobFinished(Job job)
    {
        finished();
    }

    @Override
    public void progressChanged(ProgressEvent e)
    {
        setProgress(e.getProgressVal());
        String text = e.getMessage();
        if (text != null) label.setText(text);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        job.cancel();
    }
}
