package com.tricongeophysics;

import java.io.*;

public class ShotFileKey extends StationFileKey {
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    final static String KEY_DESCRIPTION = "Shot File Key";
	
	public ShotFileKey(TriconFile file) {
		super(file);
		this.setCharKeyChar('S');
	}
	
	public ShotFileKey(){
		this.setCharKeyChar('S');
	}

    @Override
    public Station getNewStation()
    {
        return new SurveyedSP();
    }
}
