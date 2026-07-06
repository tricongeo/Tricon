package com.tricongeophysics;

import java.io.File;
import com.tricongeophysics.SUtil;

public class TSoft {

	private SoftwareSummaryFileLoader ssfl;
	private SoftwareUpdatesFileLoader sufl;
	private static final String CR = "\n";
	
	public TSoft (String summaryIn,String  summaryOut,String  updatesIn, String updatesOut) {
		SoftwareFileLoader.SummaryIn = new File(summaryIn);
		SoftwareFileLoader.SummaryOut = new File(summaryOut);
		SoftwareFileLoader.UpdatesIn = new File(updatesIn);
		SoftwareFileLoader.UpdatesOut = new File(updatesOut);
		ssfl = new SoftwareSummaryFileLoader();
		sufl = new SoftwareUpdatesFileLoader();
	}
	
	public static void main(String[] args) {
		if (argsOK(args)) {
			new TSoft(args[0], args[1], args[2], args[3]).go();
		}
	}
	
	public static boolean argsOK(String[] args) {
		if (args.length < 4) {
			if (args.length > 0) {
				SUtil.printErr("Not enough arguments, found "+args.length+" instead of 4.");
			} 
			printHelp();
			return false;
		}
		
		for (int i=0;i<4;i+=2) {
			File file = new File(args[i]);
			if (!file.canRead()) {
				SUtil.printErr("Cannot read from file: "+file.getAbsolutePath());
				return false;
			}
		}
		
		for (int i=1;i<4;i+=2) {
			File file = new File(args[i]);
			if (!file.canWrite()) {
				SUtil.printErr("Cannot write to file: "+file.getAbsolutePath());
				return false;
			}
		}
		
		return true;
	}

	private static void printHelp() {
		String string ="";
		string += "TSoft - converts tricon help text files to HTML for easier use."+CR;
		string += "Note: depends on file \"tricon_logo_small.png\" located in output folder."+CR;
		string += CR;
		string += "Usage:"+CR;
		string += "tsoft SummaryInFileName SummaryOutFileName UpdatesInFileName UpdatesOutFileName"+CR;
		string += CR;
		SUtil.print(string);
	}

	protected void go() {
		ssfl.convertHTML();
		sufl.convertHTML();
	}
}
	
