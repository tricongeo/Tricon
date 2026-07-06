package com.tricongeophysics;

import java.util.ArrayList;

public class RTMIGMainWindow extends com.tricongeophysics.MainWindow {

	public RTMIGMainWindow(ArrayList<Parameter> parmList, String title, ParmFileConverter converter, String[] commands) {
		super(parmList, title, converter, commands);
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		ArrayList<Parameter> parmList = RTMIGParameterDescriptionList.getList();
		RTMIGOldParmFileFormatConverter converter = new RTMIGOldParmFileFormatConverter(parmList);
		//SpawnProcess spawnProcess = new SpawnProces("SC2DMOD", new String[] { "sc2dmod.e"+parmList.get(2).getValue() });
		MainWindow mw = new RTMIGMainWindow(parmList,"RTMIG", converter, new String[] {"/tips/bin/tips_run", "rtmig2d"});
		mw.showMain();
	}
}
