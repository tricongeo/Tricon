package com.tricongeophysics;

public class SP extends Station {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private static int numShotFiles;
    //protected static final String FILLED_CIRCLE = "\u25cf"; //25be? small black circle
    //protected static final String FILLED_CIRCLE = "\u26ab"; //medium black circle
    protected static final String FILLED_CIRCLE = "O"; //capital o
    
    public static void setNumFiles(int n){
    	if (n>0)numShotFiles=n;
    }
    static int getNumFiles(){
    	return numShotFiles;
    }

    public String toString(){
    	if (numShotFiles>1)
       		return "<HTML>"+"Shot Point "+FILLED_CIRCLE+"<BR>"+super.toStringLong();
    	else
    		return "<HTML>"+"Shot Point "+FILLED_CIRCLE+"<BR>"+super.toString();
    }
    //...Outputs SEG-P1 format
    public String toSegp1() {
        String s = (kill) ? "K" : "S";
        return s + super.toSegp1().replace("\n", "") + SUtil.stringResize(""+usedByShot, 8) + "\n";
    }
    

    public String getSegp1Header()
    {
        return super.getSegp1Header().trim() + " usedByShot" + "\n";
    }

 
}
