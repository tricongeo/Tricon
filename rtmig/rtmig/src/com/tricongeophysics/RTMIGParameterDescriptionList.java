package com.tricongeophysics;

import java.util.ArrayList;

public class RTMIGParameterDescriptionList {
	
	public static final String PS = "PS";
	public static final String TenthOrder = "10th Order";
	public static final String EigthOrder = "8th Order";
	public static final String SixthOrder = "6th Order";
	public static final String FourthOrder = "4th Order";
	public static final String TraceHeader = "Trace Header";
	public static final String TwoByte = "2 Byte";
	public static final String FourByte = "4 Byte";
	public static final String Swap = "Swap";
	public static final String NoSwap = "No Swap";
	public static final String Vertical = "Vertical";
	public static final String SplitSpread = "Split-Spread";
	public static final String Streamer = "Streamer";
	public static final String Arbitrary = "Arbitrary";
	public static final String Segy = "SEG-Y";
	public static final String Ascii = "ASCII";
	public static final String Ibm = "IBM";
	public static final String Ieee = "32 IEEE";
	public static final String SingleFile = "Single File";
	public static final String MultiFile = "Multiple Files";
	public static final String Yes = "Yes";
	public static final String No = "No";
	public static final String MinimumTime = "Minimum Travel Time";
	public static final String MaximumEnergy = "Maximum Energy";
	public static final String CrossCorrelation = "Cross-Correlation";
	public static final String NormalizedCrossCorrelation = "Normalized Cross-Correlation";
	
	
	public static ArrayList<Parameter> getList() {
		//define parameters. declaration format is: name, description, help text, default value, other (filename ext, enum options, etc.), order index for output parm file
		Parameter inputSeismicParm  = new FileParameter("Input Seismic File",         "Input Seismic Data",              "Seismic Common Shot Gathers", "shot gathers",              new String[]{ "sgy" }, 0);
		Parameter velFileParm       = new FileParameter("P Vel File",                 "Velocity Filename",               "ASCII velocity file",  "velocity filename",                new String[]{ "sgy" }, 1);
		Parameter projNameParm      = new ProjectNameParameter ("Proj Name",          "Project Name",                    "Directory and name of all output files",  "project name"                        , 2);
		Parameter fdOrderParm       = new EnumParameter("FD Order",                   "Spatial Finite Difference Order", "FD order or Pseudo-Spectral", TenthOrder, new String[]{ PS, TenthOrder, EigthOrder, SixthOrder, FourthOrder}, 3);
		Parameter sumSRParm         = new IntParameter ("Summ Sample Rate",           "Time steps for summary file",     "How often summary file updates",                                           "100", 4);
		Parameter taperLengthParm   = new IntParameter ("Taper Length",               "Number of Absorbing Grids Points","Number of tapering grids to implement absorbing boundary conditions",       "30", 5);
		Parameter numTaperParm      = new IntParameter ("Num Taper",                  "Number of shots to be Tapered",   "Number of Seismograms to be Tapered",                                       "60", 6);
		Parameter velDepthSmoothParm = new IntParameter ("Num Depth Smooth",          "Velocity-smoothing in Depth",     "Number of velocity-smoothing in Depth",                                    "100", 7);
		Parameter velXLSmoothParm   = new IntParameter ("Num XL Smooth",              "Velocity-smoothing in XL",        "Number of velocity-smoothing in XL",                                       "100", 8);
		Parameter dzParm            = new FloatParameter ("Depth Grid Inc",           "Vel and Image Depth Interval",    "Enter Grid Z increment (ft or m)",                                        "18.0", 9);
		Parameter dxParm            = new FloatParameter ("Horizontal Grid Inc",      "Vel and Image XL Interval",       "Enter Grid XL increment (ft or m)",                                       "18.0", 10);
		Parameter inputByteSwapParm = new EnumParameter("Input Byte Swap",            "Seismic SEG-Y Byte Swap",         "Whether to swap input seismic SEG-Y bytes", Swap,   new String[]{ NoSwap, Swap }, 11);
		Parameter inputByteLengthParm = new EnumParameter("Input Byte Length",        "Seismic SEG-Y Header Length", "SEGY seismograms trace-header byte-length", FourByte,new String[]{FourByte, TwoByte}, 12);
		Parameter velByteSwapParm   = new EnumParameter("Vel Byte Swap",              "Velocity SEG-Y Byte Swap",        "Whether to swap input velocity bytes", Swap,        new String[]{ NoSwap, Swap }, 13);
		Parameter velByteLengthParm = new EnumParameter("Vel Byte Length",            "Velocity SEG-Y Header Length", "SEGY velocity trace-header byte-length",FourByte, new String[]{ FourByte, TwoByte }, 14);
		Parameter velXLLocParm      = new IntParameter ("Vel XL Byte",                "Velocity SEG-Y XL byte location", "Input SEGY velocity XL trace-header starting byte",                        "189", 15);
		Parameter velILLocParm      = new IntParameter ("Vel IL Byte",                "Velocity SEG-Y IL byte location", "Input SEGY velocity IL trace-header starting byte",                        "193", 16);
		Parameter outByteSwapParm   = new EnumParameter("Output Byte Swap",           "Output SEG-Y Byte Swap",          "Whether to swap output seismic SEG-Y bytes", Swap,  new String[]{ NoSwap, Swap }, 17);
		Parameter outByteLengthParm = new EnumParameter("Output Byte Length",         "Output SEG-Y Header Length", "SEGY seismograms trace-header byte-length", FourByte, new String[]{FourByte, TwoByte}, 18);
		Parameter outFloatTypeParm  = new EnumParameter("Output Float Type",          "Output SEG-Y Float Format",       "Output SEG-Y Floating-point format", Ibm,              new String[]{ Ibm, Ieee }, 19);
		Parameter outXLByteParm     = new IntParameter ("Out XL Byte",                "Output SEG-Y XL byte location",   "Output SEGY velocity XL trace-header starting byte",                       "189", 20);
		Parameter outILByteParm     = new IntParameter ("Out IL Byte",                "Output SEG-Y IL byte location",   "Output SEGY velocity IL trace-header starting byte",                       "193", 21);
		Parameter geomSourceParm    = new EnumParameter("Geom Source",                "Geometry Input Method",  "Get Geometry from ASCII file or Trace Headers", Ascii, new String[]{ Ascii, TraceHeader }, 22);
		Parameter geomSpreadTypeParm = new EnumParameter("Receiver Spread",           "Receiver Spread Configuration",   "Select Receiver Spread", Streamer, new String[]{ Streamer, Vertical, SplitSpread, Arbitrary }, 23);
		geomSpreadTypeParm.setEnabledDependsOn(geomSourceParm, Ascii);
		Parameter multiGathersParm  = new EnumParameter("Multi Gather Files",         "Multiple Input Files Option",  "Select whether input gathers are in multiple files", SingleFile, new String[]{ SingleFile, MultiFile }, 24);
		Parameter inputImgParm      = new EnumParameter("Input Migrated Option",      "Inputting reverse-time image?",   "Input additional reverse-time migrated file?",       No, new String[]{ No, Yes }, 25);
		Parameter imgCondParm       = new EnumParameter("Imaging Condition",          "Imaging Condition",       "Reverse-time imaging condition", MaximumEnergy, new String[]{ MinimumTime, MaximumEnergy, CrossCorrelation, NormalizedCrossCorrelation}, 26);
		Parameter bpFilterParm      = new EnumParameter("BP Filter",                  "Bandpass Filter?",                "Bandpass Filter Option",                            Yes, new String[]{ No, Yes }, 27);
		Parameter inputGeomFileParm = new FileParameter("Geom File",                  "Source/Receiver Geometry File",   "Geometry Filename",  "geometry filename",                 new String[]{ "geom" }, 28);
		inputGeomFileParm.setEnabledDependsOn(geomSourceParm, Ascii);
		Parameter inputImgFileParm  = new FileParameter("Input Image File",           "Reverse-Time Input File",         "Input RT Image Filename",  "image filename",               new String[]{ "sgy" }, 29);
		inputImgFileParm.setEnabledDependsOn(inputImgParm, Yes);
		Parameter bpF1Parm          = new FloatParameter ("BP Low End",               "Filter Low End Taper",            "1st freq. in trapezoidal filter",                                          "2.0", 30);
		bpF1Parm.setEnabledDependsOn(bpFilterParm, Yes);
		Parameter bpF2Parm          = new FloatParameter ("BP Low Cut",               "Filter Low Freq. Cut",            "2nd freq. in trapezoidal filter",                                          "4.0", 31);
		bpF2Parm.setEnabledDependsOn(bpFilterParm, Yes);
		Parameter bpF3Parm          = new FloatParameter ("BP High Cut",              "Filter Hi Freq. Cut",             "3rd freq. in trapezoidal filter",                                         "54.0", 32);
		bpF3Parm.setEnabledDependsOn(bpFilterParm, Yes);
		Parameter bpF4Parm          = new FloatParameter ("BP High End",              "Filter Hi End Taper",             "4th freq. in trapezoidal filter",                                         "60.0", 33);
		bpF4Parm.setEnabledDependsOn(bpFilterParm, Yes);
		
		//...Now, add to parameter list. Order added is order they will be placed in GUI (top to bottom)
		ArrayList<Parameter> parmList = new ArrayList<Parameter>();
		parmList.add(	inputSeismicParm  );
		parmList.add(	multiGathersParm  );
		parmList.add(	inputByteSwapParm );
		parmList.add(	inputByteLengthParm);
		parmList.add(	geomSourceParm    );
		parmList.add(	geomSpreadTypeParm);
		parmList.add(	inputGeomFileParm );
		parmList.add(	inputImgParm      ); 
		parmList.add(	inputImgFileParm  );
		parmList.add(	velFileParm       );
		parmList.add(	velDepthSmoothParm);
		parmList.add(	velXLSmoothParm   );
		parmList.add(	velByteSwapParm   );
		parmList.add(	velByteLengthParm );
		parmList.add(	velXLLocParm      );
		parmList.add(	velILLocParm      );
		parmList.add(	dzParm            );
		parmList.add(	dxParm            );
		parmList.add(	fdOrderParm   	  );   
		parmList.add(	imgCondParm       );  
		parmList.add(	sumSRParm     	  );  
		parmList.add(   taperLengthParm   );
		parmList.add(	numTaperParm      );
		parmList.add(	projNameParm  	  );   
		parmList.add(	outByteSwapParm   );
		parmList.add(	outByteLengthParm );
		parmList.add(	outFloatTypeParm  );
		parmList.add(	outXLByteParm     );
		parmList.add(	outILByteParm     );
		parmList.add(	bpFilterParm      );
		parmList.add(   bpF1Parm          );
		parmList.add(	bpF2Parm          );
		parmList.add(	bpF3Parm          );
		parmList.add(	bpF4Parm 	      );    
		return parmList;
	}
}
