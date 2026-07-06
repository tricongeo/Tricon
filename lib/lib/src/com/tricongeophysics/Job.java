package com.tricongeophysics;

import java.util.ArrayList;

/**
 * class to run process in separate thread.
 * process happens in doJob() method that subclass overrides
 * 
 * designed to be monitorable by some kind of progress meter
 * 
 * process
 * @author scott
 *
 */
public abstract class Job implements Runnable {

	protected boolean finished = false;
	protected ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
	protected ArrayList<JobFinishedListener> jobFinishedListeners = new ArrayList<JobFinishedListener>();
	
	/**
	 * call this method to make Job launch in a new thread
	 */
	public final void startJob() {
		new Thread(this).start(); //run process in separate thread
	}
	
	/**
	 * you can call this to check if a Job has finished yet
	 * @return
	 */
	public boolean isFinished() {
		return finished;
	}
	
	/**
	 * some Jobs allow you to check on their progress. If you're interested,
	 * add yourself to this list.
	 * @param l
	 */
	public final void addProgressListener(ProgressListener l) {
		progressListeners.add(l);
	}
	
	/** 
	 * subclasses call this to tell listeners that progress has changed
	 * @param e
	 */
	protected final void fireProgressChanged(ProgressEvent e) {
		for (ProgressListener l: progressListeners)
			l.progressChanged(e);
	}
	
	/** 
	 * all Jobs allow others to listen for a JobFinished event
	 * @param l
	 */
	public final void addJobFinishedListener(JobFinishedListener l) {
		jobFinishedListeners.add(l);
	}
	
	/**
	 * subclasses fire this method to let listeners know that the Job is done
	 */
	protected final void fireJobFinished() {
		finished = true;
		for (JobFinishedListener l: jobFinishedListeners)
			l.jobFinished(this);
	}

	/**
	 * Some jobs can tell you what the minimum progress is.
	 * Default is zero.
	 */
	public int getProgressMin() {
		return 0;
	}

	/**
	 * Some jobs can tell you what the maximum progress is
	 * @return
	 */
	public abstract int getProgressMax();

	/**
	 * Call this to tell a job to stop running (not always supported).
	 */
	public abstract void cancel();
	
	/**
	 * Don't call this method!! Call startJob() instead.
	 * (Otherwise, job won't start on another thread, which is against the whole point).
	 */
	@Override
	public void run()
    {
	    doJob();
	    fireJobFinished();
    }

	/**
	 * this is where subclass should do work in the Job
	 */
    protected abstract void doJob();

    public abstract boolean getIndeterminate();

    /**
     * extend this message if you would like to give the user some information about how the job went
     * @return
     */
    public String printSummary()
    {
        return "";
    }
}
