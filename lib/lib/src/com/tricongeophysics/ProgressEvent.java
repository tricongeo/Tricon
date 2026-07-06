package com.tricongeophysics;

public class ProgressEvent {

	private Object source;
	private String message;
	private int progressVal;
	private boolean messageSent = false;

	public ProgressEvent(Object source, String message, int progressVal) {
		this.source = source;
		this.message = message;
		this.progressVal = progressVal;
		
	}

	public void setMessage(String message) {
		messageSent = false;
		if (message != null) message += "\n";
		this.message = message;
	}

	public void setProgressVal(int progressVal) {
		this.progressVal = progressVal;
	}

	public Object getSource() {
		return source;
	}

	/**
	 * if message has already been sent, returns null. 
	 * otherwise returns message
	 * @return
	 */
	public String getMessage() {
		if (messageSent) return null;
		messageSent = true;
		return message;
	}

	public int getProgressVal() {
		return progressVal;
	}

}
