package com.tricongeophysics;

import java.io.*;

public class ReceiverFileKey extends StationFileKey {
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    final static String KEY_DESCRIPTION = "Receiver File Key";
	
	public ReceiverFileKey (TriconFile file) {
		super(file);
		this.setCharKeyChar('R');
	}
	
	public ReceiverFileKey(){
		this.setCharKeyChar('R');
	}

    @Override
    public Station getNewStation()
    {
        return new SurveyedReceiver();
    }
	
}
