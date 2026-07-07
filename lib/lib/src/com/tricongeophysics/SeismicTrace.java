package com.tricongeophysics;

import java.util.Arrays;

public class SeismicTrace implements Comparable<SeismicTrace> {
	private String[] headerList;
	private double[] headers;
	private float[] data; // 32 bit float because that's how it's stored in SEG-Y
	private static String FORMAT = "Header[%03d]: %-8s %13.4f data[%03d]: %+12.7e\n";
	private float maxVal = Gather.INITIALMAX;
	private float minVal = Gather.INITIALMIN;
	static int sortKeyIndex = -1;
	private double sortKeyValue;

	public SeismicTrace() {
		headerList = new String[100];
		headers = new double[100];
		data = new float[100];
	}

	public SeismicTrace(SeismicTrace trace) {
		headerList = trace.headerList;
		headers = trace.headers;
		data = trace.data;
	}

	public String[] getHeaderList() {
		return headerList;
	}

	public void setHeaderList(String[] headerList) {
		this.headerList = headerList;
	}

	public void setHeaderList(char[] headerList) {
		int headerNameLength = 8; // each header name is 8 characters long?
		int numHeaders = (int) (headerList.length / (1.0 * headerNameLength));
		String stringHeaders[] = new String[numHeaders];
		for (int i = 0; i < numHeaders; i++) {
			stringHeaders[i] = String.copyValueOf(headerList, i * headerNameLength, headerNameLength);
		}
		this.headerList = stringHeaders;
	}

	// this version of set headers takes a long string as input and a separator
	// string.
	// by splitting on the separator, you get an array of header names
	public void setHeaderList(String headers, String separator) {
		// System.out.println("headers:"+headers);
		this.headerList = headers.split(separator);
	}

	public double[] getHeaders() {
		return headers;
	}

	public void setHeaders(double[] headers) {
		this.headers = headers;
	}

	public void setHeaders(int[] headers) {
		double[] dheaders = new double[headers.length];
		for (int i = 0; i < headers.length; i++) {
			// System.out.println("v:"+headers[i]+"\n");
			dheaders[i] = (double) headers[i];
		}
		this.headers = dheaders;
	}

	public void setHeaders(float[] headers) {
		double[] dheaders = new double[headers.length];
		for (int i = 0; i < headers.length; i++) {
			// System.out.println("v:"+headers[i]+"\n");
			dheaders[i] = (double) headers[i];
		}
		this.headers = dheaders;
	}

	public float[] getData() {
		return data;
	}

	public void setData(float[] data) {
		this.data = data;
	}

	public String toString() {
		int i = 0;
		String string = "";
		string += "num headers:" + headerList.length + "\n";
		for (i = 0; i < headerList.length - 1; i++) {
			string += String.format(FORMAT, i, headerList[i].trim(), headers[i], i, data[i]);
		}
		string += String.format(FORMAT, i, headerList[i].trim(), headers[i], data.length - 1, data[data.length - 1]);
		return string;
	}

	private void findMinMaxVals() {
		maxVal = Gather.INITIALMAX; // initialize to wrong number
		minVal = Gather.INITIALMIN; // initialize to wrong number
		for (int i = 0; i < data.length; i++) {
			if (data[i] > maxVal)
				maxVal = data[i];
			if (data[i] < minVal)
				minVal = data[i];
		}
	}

	public float getMaxVal() {
		if (maxVal == Gather.INITIALMAX) {
			findMinMaxVals();
		}
		return maxVal;
	}

	public float getMinVal() {
		if (minVal == Gather.INITIALMIN) {
			findMinMaxVals();
		}
		return minVal;
	}

	/**
	 * finds index of header name in header list. If string not found, returns -1
	 */
	public int getIndexOfHeader(String headerName) {
		for (int i = 0; i < headerList.length; i++) {
			if (headerName.equals(headerList[i].trim())) {
				return i;
			}
		}
		return -1;
	}

	/** returns value of named header or 0.0 if name does not exist */
	public double getHeaderValue(String headerName) {
		int index = getIndexOfHeader(headerName);
		if (index >= 0) {
			return headers[index];
		}
		return 0.0;
	}

	public int compareTo(SeismicTrace arg0) {
		return Double.compare(getHeaders()[sortKeyIndex], arg0.getHeaders()[sortKeyIndex]);
	}

	public int getLength() {
		if (data == null)
			return 0;
		return data.length;
	}
}
