package com.tricongeophysics;

public class TestJob extends Job {
	
	int max = 100000;
	boolean cancel = false;
	ProgressEvent e = new ProgressEvent(this,null,0);

	@Override
	public int getProgressMax() {
		return max;
	}

	@Override
	public void cancel() {
		SUtil.print("stopping");
		cancel = true;
	}

	@Override
	public void doJob() {
		cancel = false;
		SUtil.print("running");
		for(int i=0; i<max; i++) {
			if(cancel) return;
			e.setProgressVal(i);
			if (i%10 == 0)
				e.setMessage(i+"\n");
			this.fireProgressChanged(e);
		}
	}

    @Override
    public boolean getIndeterminate()
    {
        return false;
    }
}
