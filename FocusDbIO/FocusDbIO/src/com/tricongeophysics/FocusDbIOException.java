package com.tricongeophysics;

public class FocusDbIOException extends Exception {

	private int status;
	private String message;

	public FocusDbIOException(int status2, String msg) {
		super();
		this.status = status2;
		this.message = msg;
	}

	@Override
    public
    String getMessage() {
        return message;
    }

	public void setMessage(String msg) {
		message = msg;
	}
}
