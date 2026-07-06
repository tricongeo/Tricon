package com.tricongeophysics;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.*;

public class ShotRecord {
	//protected ArrayList<Receiver> receivers;
	protected ArrayList<Integer> chans;
	protected SP sp;
	protected int ffid;
	protected int shot;
	protected int stationInc;  //receiver station increment (#stations/channel)
	private OBRecord obRecord;
    private Receiver[] receivers;
    private boolean debugTimer = false;
    Stopwatch sw;
    private int counter;
    private static int rold;
	
	public ShotRecord() {
        chans = new ArrayList<Integer>();
        //receivers = new ArrayList<Receiver>();
        receivers = new Receiver[50000];
        chans.ensureCapacity(5000);
        //receivers.ensureCapacity(5000);
	}
	
	public int getFfid() {
		return ffid;
	}

	public void setFfid(int ffid) {
		if (ffid>0) this.ffid = ffid;
	}

	public Receiver[] getReceivers() {
		return receivers;
	}

	public void setReceivers(Receiver[] receivers) {
		this.receivers = receivers;
	}
	
	public void loadOB(OBRecord obr, boolean calcChans) {
	    if (debugTimer) {
	        sw = new Stopwatch("load ob");
	        sw.start();
	    }
		obRecord = obr;
		sp = new SP();
		setFfid(obr.getFfid());
		setShot(obr.getShot());
		sp.setLineNumber(obr.getSourceLineNumber());
		sp.setStationNumber(obr.getSourceStationNumber());
		int numStations = 0;
		int numChans = 0;
		int recLine = 0;
		int fromReceiver = 0;
		int fromChan = 0;
		counter = 0;
		chans.clear();
		
		//load receivers
		int numCables = obr.getFromChan().size();
		for (int cable=0; cable<numCables; cable++){ //loop through cables (spread lines)
		    fromChan = obr.getFromChan(cable);
			numChans = obr.getToChan(cable) - fromChan;
			
			recLine = obr.getReceiverLineNumber(cable);
			fromReceiver = obr.getFromReceiver(cable);
			numStations = obr.getToReceiver(cable) - fromReceiver;
			int chanInc = 1;
			if (numChans == 0) {
			    stationInc = 1;
			} else {
			    stationInc = numStations/Math.abs(numChans);
			    chanInc = numChans/Math.abs(numChans);
			}
			numStations = Math.abs(numStations);
			if (stationInc == 0) stationInc = 1;
			for (int s=0;s<=numStations/Math.abs(stationInc);s++) { //loop through stations on cable
			    /*
			    if (counter >= receivers.size()) {
			        Receiver r = new Receiver();
			        receivers.add(r);
			    }
			    */
			    if (receivers[counter] == null) receivers[counter] = new Receiver();
			    Receiver r = receivers[counter];
				r.setStationNumber(fromReceiver+s*stationInc);
				r.setLineNumber(recLine);
				if (calcChans) {
				    if (counter >= chans.size()) {
	                    chans.add(0);
	                }
				    chans.set(counter, fromChan + s*chanInc);
				}
				counter++;
			}
		}
		if (counter < receivers.length - 1) receivers[counter] = null;
		if (debugTimer) {
		    sw.stop();
		    sw.printTime();
		}
	}
	
    public void loadReceiverXY(ArrayList<Receiver> recs) {
	    for (Receiver r: receivers){
	        if (r == null) return; //null marks last live receiver
	        //int index = recs.indexOf(r);
	        int index = -1;
	        for (int i = rold; i<recs.size(); i++) {
	            if (r.equals(recs.get(i))) {
	                index = i;
	                break;
	            }
	        }
	        if (index < 0) {
	            for (int i=0; i<rold; i++) {
	                if (r.equals(recs.get(i))) {
	                    index = i;
	                    break;
	                }
	            }
	        }
	        if (index>=0) {
	            r.x = recs.get(index).x;
	            r.y = recs.get(index).y;
	            rold = index;
	        }
	        else {
	            r.x = 0;
	            r.y = 0;
	        }
	    }
	}

	public void loadShotXY(ArrayList s) {
	    int index = s.indexOf(sp);
	    if (index>=0) sp = (SP)s.get(index);
	}

	public SP getSp() {
		return sp;
	}

	public void setSp(SP sp) {
		this.sp = sp;
	}

    public String toString() {
        if (receivers == null || receivers.length == 0 || receivers[0] == null) return "Shot: "+ffid;
    	return "<HTML>"+
    	"FFID: "+ffid+" SHOT: "+shot+"<br>"+
    	"shot line: "+sp.getLineNumber()+"<br>"+
    	"shot station: "+sp.getStationNumber()+"<br>"+
    	"channel 1: "+receivers[0].getLineNumber()+", "+receivers[0].getStationNumber()+"<br>"+
    	"channel "+chans.get(chans.size()-1)+": "+receivers[chans.size()-1].getLineNumber()+", "+
    	receivers[chans.size()-1].getStationNumber()+"<br>"+
    	"</html>";
    }

	public ArrayList<Integer> getChans() {
		return chans;
	}

	public void setChans(ArrayList<Integer> chans) {
		this.chans = chans;
	}

	public OBRecord getObRecord() {
		return obRecord;
	}

    public Point2D.Double[] getScatter()
    {
        if (receivers == null || sp == null) return null;
        Double[] scatter = new Point2D.Double[counter];
        for (int i=0; i<counter; i++) {
            scatter[i] = new Point2D.Double();
            scatter[i].x = (receivers[i].x + sp.x)/2;
            scatter[i].y = (receivers[i].y + sp.y)/2;
        }
        return scatter;
    }

    public int getShot()
    {
        return shot;
    }

    public void setShot(int shot)
    {
        this.shot = shot;
    }
}
