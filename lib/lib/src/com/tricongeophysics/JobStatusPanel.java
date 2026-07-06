package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class JobStatusPanel extends JPanel implements JobFinishedListener, ProgressListener, ActionListener {
	
	private JProgressBar progressBar;
    private StartButton startButton;
    private StopButton stopButton;
    private JTextArea jobMessages;
    private Job job;
	private int min;
	private int max;
	private JScrollPane scrollPane;
    private ArrayList<JobFinishedListener> jobFinishedListeners = new ArrayList<JobFinishedListener>();
    private long startTime;
    private long oldStartTime;
    
    JobStatusPanel(Job job) {
    	super();
    	this.job = job;
    	min = job.getProgressMin();
    	max = job.getProgressMax();
    	job.addJobFinishedListener(this);
    	job.addProgressListener(this);
    	progressBar = new JProgressBar(min, max);
    	progressBar.setStringPainted(true);
    	progressBar.setValue(min);
    	
    	startButton = new StartButton();
    	startButton.addActionListener(this);
    	startButton.setToolTipText("Start Job");
    	startButton.setActionCommand("Start");
    	
    	stopButton = new StopButton();
    	stopButton.addActionListener(this);
    	stopButton.setToolTipText("Cancel Job");
    	stopButton.setActionCommand("Stop");

        jobMessages = new JTextArea(5, 20);
        //taskOutput.setMargin(new Insets(5,5,5,5));
        jobMessages.setEditable(false);
        jobMessages.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(startButton);
        panel.add(stopButton);
        panel.add(progressBar);
        //progressBar.setPreferredSize(new Dimension(300,25));

        this.setLayout(new BorderLayout());
        add(panel, BorderLayout.SOUTH);
        scrollPane = new JScrollPane(jobMessages);
        add(scrollPane, BorderLayout.CENTER);
        //setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }
    
	public static void main(String[] args) {
		JFrame test = new JFrame("test");
		test.setVisible(true);
		test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Job testJob = new TestJob();
		
		JobStatusPanel jobStatusPanel = new JobStatusPanel(testJob);
		
		test.getContentPane().add(jobStatusPanel);
		test.pack();
	}

	@Override
	public void jobFinished(Job job) {
		progressBar.setValue(max);
		startTime = oldStartTime;
		progressBar.setString("Job Finished. Elapsed Time: "+SUtil.formatTime(getElapsedTime()));
		stopButton.setEnabled(false);
		startButton.setEnabled(true);
		addJobMessage("Finished\n\n");
		addJobMessage(job.printSummary());
		fireJobFinished(job);
	}

	private long getElapsedTime()
    {
	    long now = System.currentTimeMillis();
        long elapsed = now - startTime;
        return elapsed;
    }

    private void fireJobFinished(Job job)
    {
        for (JobFinishedListener l: jobFinishedListeners) l.jobFinished(job);
    }

    @Override
	public void progressChanged(ProgressEvent e) {
		int progress = e.getProgressVal();
		String message = e.getMessage();
		if (progress >= 0) {
		    if (progress == 0) startTime = System.currentTimeMillis();
			progressBar.setValue(progress);
			long tr = calcTimeRemaining(progress);
			progressBar.setStringPainted(true);
			progressBar.setString("Time Remaining: "+SUtil.formatTime(tr));
		} 
		addJobMessage(message);
	}

    private long calcTimeRemaining(int progress)
    {
        if (progress < 1) return 0; //progress can't be zero
        long elapsed = getElapsedTime();
        long totalTime = (long) (elapsed*max*1.0/progress);
        return totalTime - elapsed;
    }

    private void addJobMessage(String message) {
		if (message != null) {
			jobMessages.append(message);
			JScrollBar bar = scrollPane.getVerticalScrollBar();
			bar.setValue(bar.getMaximum());
		}
	}

	public void resetProgressBar() {
		min = job.getProgressMin();
    	max = job.getProgressMax();
    	progressBar.setMaximum(max);
    	progressBar.setMinimum(min);
	}

	public void setJob(Job job) {
		this.job = job;
		job.addJobFinishedListener(this);
		job.addProgressListener(this);
		resetProgressBar();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "Start") {
			job.startJob();
			stopButton.setEnabled(true);
			startButton.setEnabled(false);
			progressBar.setValue(min);
			jobMessages.setText("Start\n\n");
			startTime = System.currentTimeMillis();
			oldStartTime = startTime;
		}
		if (e.getActionCommand() == "Stop") {
			job.cancel();
			stopButton.setEnabled(false);
			startButton.setEnabled(true);
			addJobMessage("Cancelled\n");
		}
	}

    public void addJobFinishedListener(JobFinishedListener l)
    {
        if (l == null) return;
        jobFinishedListeners.add(l);
    }

    public Job getJob()
    {
        return job;
    }
}
