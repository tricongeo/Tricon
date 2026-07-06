package com.tricongeophysics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class SegyReader {

	private String filepath;
	private int sampleRate;
	private int dataLength;
	private int nSamps;
	private File file;
	private byte[] ebcdicHeader;
	private byte[] binaryHeader;
	private byte[] traceHeader;
	private byte[] traceData;
	private ArrayList<int[]> traceHeaders;
	private ArrayList<float[]> traceDataFloats;
	private int nTraces;

	public SegyReader(String filepath, int sampleRate, int dataLength) {
		this.filepath = filepath;
		this.sampleRate = sampleRate;
		this.dataLength = dataLength;
		nSamps = (int)(dataLength/sampleRate);
		ebcdicHeader = new byte[3200];
		binaryHeader = new byte[400];
		traceHeader = new byte[240];
		traceData = new byte[nSamps*4];
		traceHeaders = new ArrayList<int[]>();
		traceDataFloats = new ArrayList<float[]>();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			SUtil.printErr("segyreader: program to read in segy file and output as csv file" +
					"\n usage: \n segyreader segyfilename samplerate(ms) datalength(ms)");
			return;
		}
		String file = args[0];
		String outFile = TriconFile.removeSuffix(file) + ".csv";
		
		int sr = (int) SUtil.sval(args[1]);
		int len = (int) SUtil.sval(args[2]);
		SegyReader sgr = new SegyReader(file, sr, len);
		sgr.read();
		try {
			sgr.dumpAscii(outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * output segy data into text file
	 * 
	 * 1 column per trace
	 * 
	 * @param filename
	 * @throws IOException
	 */
	private void dumpAscii(String filename) throws IOException {
		SUtil.print("writing data to: "+filename);
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		
		writer.write("segy data from input file: "+filepath+"\n");
		writer.write("sample rate              : "+sampleRate+"\n");
		writer.write("samples per trace        : "+nSamps+"\n");
		writer.write("data length              : "+dataLength+"\n");
		writer.write("number of traces         : "+nTraces+"\n");
		
		writer.write("\n");
		String line = "";
		for (int i=0; i<60; i++) {
			line = "Header: "+SUtil.stringResize(i+1+"", 4);
			for (int j=0; j<nTraces; j++) {
				line += ","+SUtil.stringResize(traceHeaders.get(j)[i]+"", 12);
			}
			writer.write(line+"\n");
		}
		for (int i=0; i<nSamps; i++) {
			line = "Sample: "+SUtil.stringResize(i+1+"", 4);
			line += SUtil.stringResize(i*sampleRate+" ms", 12);
			for (int j=0; j<nTraces; j++) {
				line += ","+SUtil.stringResize(traceDataFloats.get(j)[i]+"", 12);
			}
			writer.write(line+"\n");
		}
		writer.write("\n");
		writer.close();
		SUtil.print("Done...");
	}

	/**
	 * Scan through Seg-y file, load trace data and headers into memory
	 */
	private void read() {
		SUtil.print("readin segy file: "+filepath);
		openFile();
		FileInputStream fis;
		try {
			fis = (new FileInputStream(file));
			fis.read(ebcdicHeader);
			fis.read(binaryHeader);
			while (fis.read(traceHeader) > 0) {
				fis.read(traceData);
				loadHeaders();
				loadTraceData();				
			}
			nTraces = traceHeaders.size();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		SUtil.print("finished reading: "+filepath);
	}

	/**
	 * Convert trace bytes to floats
	 */
	private void loadTraceData() {
		float[] data = new float[nSamps];
		ByteBuffer buf = ByteBuffer.wrap(traceData);  
		
		for (int i=0; i<data.length; i++) {
			data[i] = buf.getFloat();
		}
		
		traceDataFloats.add(data);
	}

	/**
	 * Convert header bytes to floats
	 */
	private void loadHeaders() {
		int[] headers = new int[60];
		ByteBuffer buf = ByteBuffer.wrap(traceHeader);  
		
		for (int i=0; i<headers.length; i++) {
			headers[i] = buf.getInt();
		}
		
		traceHeaders.add(headers);
	}

	private void openFile() {
		file = new File(filepath);
	}

}
