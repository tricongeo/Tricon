package com.tricongeophysics;

public class ReflectiveTableModelTestDrive {
	
	static ShotRecord[] shotRecords;

	public static void main(String[] args) {
		SP sp = new SP();
		sp.lineNumber = 1001;
		sp.stationNumber = 5001;
		
		shotRecords = new ShotRecord[5];
		for (int i=0; i< shotRecords.length; i++) {
			shotRecords[i] = new ShotRecord();
			shotRecords[i].ffid = i;
			shotRecords[i].sp = sp;
			shotRecords[i].stationInc = i;
		}
		
		String[] names = {"ffid", "sp", "stationInc"};
		
		//ReflectiveTableModel rtm = new ReflectiveTableModel(shotRecords);
		//Object val = rtm.getValueAt(0, 2);
		//SUtil.print("found val: "+val);
	}
}
