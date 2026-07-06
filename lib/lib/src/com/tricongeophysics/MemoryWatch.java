package com.tricongeophysics;

public class MemoryWatch {

	private long total;
	private long oldTotal;

	MemoryWatch() {
		total = Runtime.getRuntime().totalMemory()/1024/1024;
		oldTotal = total;
	}
	
	public void print() {
		total = Runtime.getRuntime().totalMemory()/1024/1024;
		long diff = total - oldTotal;
		oldTotal = total;
        System.out.println("Total memory allocated = " + total + "MB");
        System.out.println("Difference = " + diff + "MB\n");
	}

}
