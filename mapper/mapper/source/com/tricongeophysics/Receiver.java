package com.tricongeophysics;

public class Receiver extends Station {
	private static final long serialVersionUID = 1L;
	private static int numReceiverFiles;
	protected static final String INVERTED_TRIANGLE = "\u2207"; //unicode character for nabla
	//protected static final String INVERTED_TRIANGLE = "\u25bc"; //unicode character for upsidedown black triangle
	
    public static void setNumFiles(int n){
    	if (n>0)numReceiverFiles=n;
    }
    public static int getNumFiles(){
    	return numReceiverFiles;
    }
    public String toString(){
    	if (numReceiverFiles>1)
       		return "<HTML>"+"Receiver "+INVERTED_TRIANGLE+"<BR>"+super.toStringLong();
    	else
    		return "<HTML>"+"Receiver "+INVERTED_TRIANGLE+"<BR>"+super.toString();
    }  
    //...Outputs SEG-P1 format
    public String toSegp1() {
        String r = (kill) ? "K" : "R";
        return r+super.toSegp1();
    }
}
