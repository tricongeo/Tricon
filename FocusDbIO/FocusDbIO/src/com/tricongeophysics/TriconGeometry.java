package com.tricongeophysics;

public class TriconGeometry {
	private static final String SHOT = "SHOT";
	private static final String STATION = "STATION";
	private static final String CDP = "CDP";
	String project;
	String line;
    ShotDbModel shotModel;
    RcvrDbModel rcvrModel;
    FocusCDPModel cdpModel;
	private FocusBias bias;
    
	public TriconGeometry(String project2, String line2) {
		project = project2;
		line = line2;
		shotModel = new ShotDbModel(this);
		rcvrModel = new RcvrDbModel(this);
		cdpModel = new FocusCDPModel(null, this);
		bias = new FocusBias();
	}
	
	public TriconGeometry() {
		this("dummy", "dummy");
	}

	public String getProject() {
		return project;
	}
	
	public void setProject(String project) {
		this.project = project;
	}
	
	public String getLine() {
		return line;
	}
	
	public void setLine(String line) {
		this.line = line;
	}

	public ShotDbModel getShotModel() {
		return shotModel;
	}

	public void setShotModel(ShotDbModel shotModel) {
		this.shotModel = shotModel;
	}

	public RcvrDbModel getRcvrModel() {
		return rcvrModel;
	}

	public void setRcvrModel(RcvrDbModel rcvrModel) {
		this.rcvrModel = rcvrModel;
	}

	public FocusCDPModel getCdpModel() {
		return cdpModel;
	}

	public void setCdpModel(FocusCDPModel cdpModel) {
		this.cdpModel = cdpModel;
	}
	
	public String toString() {
		return "project: " + project + " line: " +line;
	}
	
	public void addAttribute(String line, String model, String event, String attribute) {
		if (line == null || model == null || event == null || attribute == null) {
			System.err.println("TriconGeometry:addAttribute() - Can't add null attribute information");
			return;
		}
		System.out.println("adding "+line+","+model+","+event+","+attribute);
		if (!line.equals(this.line)) {
			System.err.println("TriconGeometry:addAttribute() - Trying add attribute from wrong line! " + line);
			return;
		}
		if (model.equals(SHOT)) {
			shotModel.addAttribute(event, attribute);
		}
		if (model.equals(STATION)) {
			rcvrModel.addAttribute(event, attribute);
		}
		if (model.equals(CDP)) {
			cdpModel.addAttribute(event, attribute);
		}
	}

	public void addAttribute2(char[] cline, char[] cmodel, char[] cevent, char[] cattribute) {
		if (cline == null || cmodel == null || cevent == null || cattribute == null) {
			System.err.println("TriconGeometry:addAttribute() - Can't add null attribute information");
			return;
		}
		String line = String.valueOf(SUtil.cleanChars(cline));
		String model = String.valueOf(SUtil.cleanChars(cmodel));
		String event = String.valueOf(SUtil.cleanChars(cevent));
		String attribute = String.valueOf(SUtil.cleanChars(cattribute));
		addAttribute(line, model, event, attribute);
	}
	
	public FocusBias getBias() {
		return bias;
	}

	public void initializeBias(CdpModel cdpModel2) {
		bias.initialize(cdpModel2);
	}
}
