package com.tricongeophysics;

public class InvalidParameterFileException extends Exception {

	private String message;

	public InvalidParameterFileException(String string) {
		super();
		message = string;
	}
	
	@Override
	public String getMessage() {
		return message;
	}

}
