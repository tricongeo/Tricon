package com.tricongeophysics;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] commands = new String[] {"/apdata/rtmig/sc2dmod/src/sc2dmod.e"};
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.redirectErrorStream(true);
		Process process=null;
		try {
			process = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		PrintStream ps = new PrintStream(new BufferedOutputStream(process.getOutputStream()));
		TriconFile file = new TriconFile("/apdata/rtmig/sc2dmod/pars/test3.par");
		String[] lines = null;
		try {
			lines = file.getLines();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedReader outReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
		for (int i=0;i<lines.length;i++) {
			ps.println(lines[i]);
		}
		String line="";
		try {
			while ( (line = outReader.readLine()) != null){
				System.out.println(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//once process is done, re-enable OK button and return full string of output
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done\n");
	}

}
