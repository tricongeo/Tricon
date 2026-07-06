package com.tricongeophysics;

import java.text.DecimalFormat;

public class FocusCDPModel extends FocusDbModel{

	private static final String CDP = "CDP";
	public static final String GeometryFile = "GEOMETRY FILE";
	public static final String RegularCdp = "REGULAR CDP";
	private CdpModel mapperCdpModel;

	public FocusCDPModel(CdpModel cdpModel, TriconGeometry tg) {
		super(CDP, tg);
		if (cdpModel == null) cdpModel = new CdpModel();
		this.mapperCdpModel = cdpModel;
		nlocs = cdpModel.getNumCdps();
	}

	public String getRegularCDP() {
		if (mapperCdpModel == null) return "";
		
		DecimalFormat df = new DecimalFormat("######.00##");
		
		String fcdp  = SUtil.stringResize(mapperCdpModel.getFirstCDP()+""    , 10); 
		String lcdp  = SUtil.stringResize(mapperCdpModel.getLastCDP()+""     , 10); 
		String nxl   = SUtil.stringResize(mapperCdpModel.getNumXlines()+""   , 10);
		String nsl   = SUtil.stringResize(mapperCdpModel.getNumInlines()+""  , 10);
		String xori  = SUtil.stringResize(df.format(mapperCdpModel.getOriginX())     , 15);
		String yori  = SUtil.stringResize(df.format(mapperCdpModel.getOriginY())     , 15);
		String xxinc = SUtil.stringResize(df.format(mapperCdpModel.getXLineDeltaX()) , 15);
		String yxinc = SUtil.stringResize(df.format(mapperCdpModel.getXLineDeltaY()) , 15);
		String xsinc = SUtil.stringResize(df.format(mapperCdpModel.getInlineDeltaX()), 15);
		String ysinc = SUtil.stringResize(df.format(mapperCdpModel.getInlineDeltaY()), 15);
		
		String text = fcdp + lcdp + nxl + nsl + xori + yori + xxinc + yxinc + xsinc + ysinc;
		return text;		
	}

	public String getGeometryFile() {
		if (mapperCdpModel == null) return "";
		
		DecimalFormat df = new DecimalFormat("######.00##");

		String fcdp  = SUtil.stringResize(mapperCdpModel.getFirstCDP()+""    , 10); 
		String lcdp  = SUtil.stringResize(mapperCdpModel.getLastCDP()+""     , 10); 
		String nxl   = SUtil.stringResize(mapperCdpModel.getNumXlines()+""   , 10);
		String nsl   = SUtil.stringResize(mapperCdpModel.getNumInlines()+""  , 10);
		String fsl   = SUtil.stringResize(mapperCdpModel.getOriginIL()+  ""  , 10);
		String incs  = SUtil.stringResize(mapperCdpModel.getIlIncrement()+"" , 10);
		String fxl   = SUtil.stringResize(mapperCdpModel.getOriginXL()+  ""  , 10);
		String incx  = SUtil.stringResize(mapperCdpModel.getXlIncrement()+"" , 10);
		String dxlin = SUtil.stringResize(df.format(mapperCdpModel.getXlInterval()), 13);
		String dslin = SUtil.stringResize(df.format(mapperCdpModel.getIlInterval()), 13);
		
		String text = nxl + nsl + fsl + incs + fxl +incx + fcdp + lcdp + dxlin + dslin;
		return text;		
	}
	

	public CdpModel getMapperCdpModel() {
		return mapperCdpModel;
	}

	public void loadGeometryFile(String geomFile) {
		int nxl  = (int) SUtil.sval(geomFile,  0,  9);
		int nsl  = (int) SUtil.sval(geomFile, 10, 19);
		int fsl  = (int) SUtil.sval(geomFile, 20, 29);
		int incs = (int) SUtil.sval(geomFile, 30, 39);
		int fxl  = (int) SUtil.sval(geomFile, 40, 49);
		int incx = (int) SUtil.sval(geomFile, 50, 59);
		int fcdp = (int) SUtil.sval(geomFile, 60, 69);
		int lcdp = (int) SUtil.sval(geomFile, 70, 79);
		double dxlin =   SUtil.toDouble(geomFile, 80, 93);
		double dslin =   SUtil.toDouble(geomFile, 93, 106);
		
		mapperCdpModel.setFirstCDP(fcdp);
		mapperCdpModel.setNumInlines(nsl);
		mapperCdpModel.setNumXlines(nxl);
		mapperCdpModel.setOriginIL(fsl);
		mapperCdpModel.setOriginXL(fxl);
		mapperCdpModel.setIlIncrement(incs);
		mapperCdpModel.setXlIncrement(incx);
		mapperCdpModel.setXlInterval(dxlin);
		mapperCdpModel.setIlInterval(dslin);
	}

	public void loadRegularCdp(String regularCdp) {
		int fcdp = (int) SUtil.sval(regularCdp,  0,  9);
		int lcdp = (int) SUtil.sval(regularCdp, 10, 19);
		int nxl  = (int) SUtil.sval(regularCdp, 20, 29);
		int nsl  = (int) SUtil.sval(regularCdp, 30, 39);
		double xori  =   SUtil.toDouble(regularCdp, 40, 55);
		double yori  =   SUtil.toDouble(regularCdp, 55, 70);
		double xxinc =   SUtil.toDouble(regularCdp, 70, 85);
		double yxinc =   SUtil.toDouble(regularCdp, 85, 100);
		double xsinc =   SUtil.toDouble(regularCdp, 100, 115);
		double ysinc =   SUtil.toDouble(regularCdp, 115, 130);
		
		mapperCdpModel.setFirstCDP(fcdp);
		mapperCdpModel.setNumInlines(nsl);
		mapperCdpModel.setNumXlines(nxl);
		mapperCdpModel.setOriginX(xori);
		mapperCdpModel.setOriginY(yori);
//		double angle = Math.atan(yxinc/xxinc);
//		double angle = Math.asin(yxinc/mapperCdpModel.getIlInterval());
		double angle = SUtil.azimuth(0, 0, xxinc, yxinc); //this seems to be backwards, xxinc is actually inline-x interval
//		double degrees = Math.toDegrees(angle);
		mapperCdpModel.setAngle(angle);
		mapperCdpModel.calcCornersFromAngle();
	}
}
