package com.tricongeophysics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RTMIGOldParmFileFormatConverter extends com.tricongeophysics.ParmFileConverter {
	
	protected String seisfile;    /*1*/  
	protected String velofile;    /*2*/  
	protected String jobfname;    /*3*/  
	protected String spor;        /*4*/  
	protected String ipr;         /*5*/ 
	protected String lt;          /*6*/  
	protected String lts;         /*7*/  
	protected String nsm_zz;      /*8*/  
	protected String nsm_xline;   /*9*/  
	protected String dz;          /*10*/
	protected String dx;          /*11*/
	protected String seswap;      /*12*/
	protected String se2or4;      /*13*/
	protected String vswap;       /*14*/
	protected String v2or4;       /*15*/
	protected String vxl_sbyte;   /*16*/
	protected String vil_sbyte;   /*17*/
	protected String rtswap;      /*18*/
	protected String rt2or4;      /*19*/
	protected String rt_ibm_ieee; /*20*/
	protected String rt_xl_sbyte; /*21*/
	protected String rt_il_sbyte; /*22*/
	protected String geomop;      /*23*/
	protected String srgop;       /*24*/
	protected String inpfileop;   /*25*/
	protected String inpimgop;    /*26*/
	protected String imgcon;      /*27*/
	protected String bpassop;     /*28*/
	protected String geomfile;    /*29*/
	protected String inpimgfile;  /*30*/
	protected String bpf1;        /*31*/
	protected String bpf2;        /*32*/
	protected String bpf3;        /*33*/
	protected String bpf4;        /*34*/


	protected Key spatialOrderKey;
	protected Key rickerWaveletKey;
	protected Key byteLengthKey;
	protected Key boundaryConditionKey;
	protected Key byteSwapKey;
	protected Key receiverSpreadKey;
	protected Key dataFormatKey;
	protected Key floatingPointKey;
	protected Key sourceDelayKey;
	protected Key velHeadersKey;
	protected Key yesNoKey;
	protected Key muliFileKey;
	protected Key imageConditionKey;
	
	public final static int TotalParms = 34;
	
	
	public RTMIGOldParmFileFormatConverter(ArrayList<Parameter> parameterList) {
		super(parameterList);
	}
	
	/**
	 * loads parameter values from string array, then sets Parameter values accordingly
	 * 
	 * @return
	 * @throws IOException 
	 */
	@Override
	public ArrayList<Parameter> toParameters(String[] parmStrings) throws InvalidParameterFileException {
		loadParms(parmStrings);
		copyToParmList(parameterList);
		return parameterList;
	}
	
	
	protected void loadKeys() {
		spatialOrderKey = new Key();
		spatialOrderKey.addKey("0", RTMIGParameterDescriptionList.PS);
		spatialOrderKey.addKey("1", RTMIGParameterDescriptionList.TenthOrder);
		spatialOrderKey.addKey("2", RTMIGParameterDescriptionList.EigthOrder);
		spatialOrderKey.addKey("3", RTMIGParameterDescriptionList.SixthOrder);
		spatialOrderKey.addKey("4", RTMIGParameterDescriptionList.FourthOrder);
		
		byteLengthKey = new Key();
		byteLengthKey.addKey("0", RTMIGParameterDescriptionList.TwoByte);
		byteLengthKey.addKey("1", RTMIGParameterDescriptionList.FourByte);
		
		byteSwapKey = new Key();
		byteSwapKey.addKey("1", RTMIGParameterDescriptionList.Swap);
		byteSwapKey.addKey("0", RTMIGParameterDescriptionList.NoSwap);
		
		receiverSpreadKey = new Key();
		receiverSpreadKey.addKey("0", RTMIGParameterDescriptionList.Vertical);
		receiverSpreadKey.addKey("1", RTMIGParameterDescriptionList.Streamer);
		receiverSpreadKey.addKey("2", RTMIGParameterDescriptionList.SplitSpread);
		receiverSpreadKey.addKey("3", RTMIGParameterDescriptionList.Arbitrary);
		
		dataFormatKey = new Key();
		dataFormatKey.addKey("0", RTMIGParameterDescriptionList.Segy);
		dataFormatKey.addKey("1", RTMIGParameterDescriptionList.Ascii);
		
		floatingPointKey = new Key();
		floatingPointKey.addKey("0", RTMIGParameterDescriptionList.Ibm);
		floatingPointKey.addKey("1", RTMIGParameterDescriptionList.Ieee);
		
		yesNoKey = new Key();
		yesNoKey.addKey("1", RTMIGParameterDescriptionList.Yes);
		yesNoKey.addKey("0", RTMIGParameterDescriptionList.No);
		
		muliFileKey = new Key();
		muliFileKey.addKey("0", RTMIGParameterDescriptionList.SingleFile);
		muliFileKey.addKey("1", RTMIGParameterDescriptionList.MultiFile);
		
		imageConditionKey = new Key();
		imageConditionKey.addKey("0", RTMIGParameterDescriptionList.MinimumTime);
		imageConditionKey.addKey("1", RTMIGParameterDescriptionList.MaximumEnergy);
		imageConditionKey.addKey("2", RTMIGParameterDescriptionList.CrossCorrelation);
		imageConditionKey.addKey("3", RTMIGParameterDescriptionList.NormalizedCrossCorrelation);
		
	}

	protected void copyToParmList(ArrayList<Parameter> parameterList) {
		parameterList.get(0).setValue(                               seisfile     );  
		parameterList.get(1).setValue(                               velofile     );
		parameterList.get(2).setValue(                               jobfname     );
		parameterList.get(3).setValue(    spatialOrderKey.getLabel(  spor         ));
		parameterList.get(4).setValue(                               ipr          );
		parameterList.get(5).setValue(                               lt           );
		parameterList.get(6).setValue(                               lts          );
		parameterList.get(7).setValue(                               nsm_zz       );
		parameterList.get(8).setValue(                               nsm_xline    );
		parameterList.get(9).setValue(                               dz           );
		parameterList.get(10).setValue(                              dx           );
		parameterList.get(11).setValue(   byteSwapKey.getLabel(      seswap       ));
		parameterList.get(12).setValue(   byteLengthKey.getLabel(    se2or4       ));
		parameterList.get(13).setValue(   byteSwapKey.getLabel(      vswap        ));
		parameterList.get(14).setValue(   byteLengthKey.getLabel(    v2or4        ));
		parameterList.get(15).setValue(                              vxl_sbyte    );
		parameterList.get(16).setValue(                              vil_sbyte    );
		parameterList.get(17).setValue(   byteSwapKey.getLabel(      rtswap       ));
		parameterList.get(18).setValue(   byteLengthKey.getLabel(    rt2or4       ));
		parameterList.get(19).setValue(   floatingPointKey.getLabel( rt_ibm_ieee  ));
		parameterList.get(20).setValue(                              rt_xl_sbyte  );
		parameterList.get(21).setValue(                              rt_il_sbyte  );
		parameterList.get(22).setValue(   dataFormatKey.getLabel(    geomop       ));
		parameterList.get(23).setValue(   receiverSpreadKey.getLabel(srgop        ));
		parameterList.get(24).setValue(   muliFileKey.getLabel(      inpfileop    ));
		parameterList.get(25).setValue(   yesNoKey.getLabel(         inpimgop     ));
		parameterList.get(26).setValue(   imageConditionKey.getLabel(imgcon       ));
		parameterList.get(27).setValue(   yesNoKey.getLabel(         bpassop      ));
		parameterList.get(28).setValue(                              geomfile     );
		parameterList.get(29).setValue(                              inpimgfile   );
		parameterList.get(30).setValue(                              bpf1         );
		parameterList.get(31).setValue(                              bpf2         );
		parameterList.get(32).setValue(                              bpf3         );
		parameterList.get(33).setValue(                              bpf4         );
	}

	protected void loadParms(String[] parmStrings) throws InvalidParameterFileException   {
		int indexAdjust=0;
		
		try {
			seisfile    = parmStrings[0+indexAdjust];       /*1*/ 
			velofile    = parmStrings[1+indexAdjust];       /*2*/ 
			jobfname    = parmStrings[2+indexAdjust];       /*3*/ 
			spor        = parmStrings[3+indexAdjust];       /*4*/ 
			ipr         = parmStrings[4+indexAdjust];       /*5*/
			lt          = parmStrings[5+indexAdjust];       /*6*/ 
			lts         = parmStrings[6+indexAdjust];       /*7*/ 
			nsm_zz      = parmStrings[7+indexAdjust];       /*8*/ 
			nsm_xline   = parmStrings[8+indexAdjust];       /*9*/ 
			dz          = parmStrings[9+indexAdjust];       /*10*/
			dx          = parmStrings[10+indexAdjust];      /*11*/
			seswap      = parmStrings[11+indexAdjust];      /*12*/
			se2or4      = parmStrings[12+indexAdjust];      /*13*/
			vswap       = parmStrings[13+indexAdjust];      /*14*/
			v2or4       = parmStrings[14+indexAdjust];      /*15*/
			vxl_sbyte   = parmStrings[15+indexAdjust];      /*16*/
			vil_sbyte   = parmStrings[16+indexAdjust];      /*17*/
			rtswap      = parmStrings[17+indexAdjust];      /*18*/
			rt2or4      = parmStrings[18+indexAdjust];      /*19*/
			rt_ibm_ieee = parmStrings[19+indexAdjust];      /*20*/
			rt_xl_sbyte = parmStrings[20+indexAdjust];      /*21*/
			rt_il_sbyte = parmStrings[21+indexAdjust];      /*22*/
			geomop      = parmStrings[22+indexAdjust];      /*23*/

			if ( geomop.equals(dataFormatKey.getValue(RTMIGParameterDescriptionList.Ascii)) ) {
				srgop       = parmStrings[23+indexAdjust];  /*24*/
			} else {
				indexAdjust += -1; // geometry options not written out, indexes adjusted by 1 accordingly
			}

			inpfileop   = parmStrings[24+indexAdjust];      /*25*/
			inpimgop    = parmStrings[25+indexAdjust];      /*26*/

			imgcon      = parmStrings[26+indexAdjust];      /*27*/
			bpassop     = parmStrings[27+indexAdjust];      /*28*/

			if ( geomop.equals(dataFormatKey.getValue(RTMIGParameterDescriptionList.Ascii)) ) {
				geomfile    = parmStrings[28+indexAdjust];  /*29*/
			} else {
				indexAdjust += -1; // geometry options not written out, indexes adjusted by 1 accordingly
			}

			if ( inpimgop.equals(yesNoKey.getValue(RTMIGParameterDescriptionList.Yes) ) ) {
				inpimgfile  = parmStrings[29+indexAdjust];  /*30*/
			} else {
				indexAdjust += -1; // geometry options not written out, indexes adjusted by 1 accordingly
			}

			if ( bpassop.equals(yesNoKey.getValue(RTMIGParameterDescriptionList.Yes) ) ) {
				bpf1        = parmStrings[30+indexAdjust];  /*31*/
				bpf2        = parmStrings[31+indexAdjust];  /*32*/
				bpf3        = parmStrings[32+indexAdjust];  /*33*/
				bpf4        = parmStrings[33+indexAdjust];  /*34*/
			}
			
		} catch (Exception e) {
			System.err.println("RTMIGOldParmFileFormat: incorrect number of parameters! " + parmStrings.length);
			throw new InvalidParameterFileException("\n\nIncorrect number of parameters! " + parmStrings.length + " instead of " + TotalParms
					+ "\n\n" + e.toString());
		}
	}

	@Override
	public String[] toFileFormat() {
		copyParmsToVariables();
		return variablesToOldFormat();
	}

	
	protected String[] variablesToOldFormat() {
		ArrayList<String> strings = new ArrayList<String>();
		strings.add(seisfile        ); /*1*/  
		strings.add(velofile        ); /*2*/  
		strings.add(jobfname        ); /*3*/  
		strings.add(spor            ); /*4*/  
		strings.add(ipr             ); /*5*/ 
		strings.add(lt              ); /*6*/  
		strings.add(lts             ); /*7*/  
		strings.add(nsm_zz          ); /*8*/  
		strings.add(nsm_xline       ); /*9*/  
		strings.add(dz              ); /*10*/ 
		strings.add(dx              ); /*11*/ 
		strings.add(seswap          ); /*12*/ 
		strings.add(se2or4          ); /*13*/ 
		strings.add(vswap           ); /*14*/ 
		strings.add(v2or4           ); /*15*/ 
		strings.add(vxl_sbyte       ); /*16*/ 
		strings.add(vil_sbyte       ); /*17*/ 
		strings.add(rtswap          ); /*18*/ 
		strings.add(rt2or4          ); /*19*/ 
		strings.add(rt_ibm_ieee     ); /*20*/ 
		strings.add(rt_xl_sbyte     ); /*21*/ 
		strings.add(rt_il_sbyte     ); /*22*/ 
		strings.add(geomop          ); /*23*/ 

		if ( geomop.equals(dataFormatKey.getValue(RTMIGParameterDescriptionList.Ascii)) ) {
			strings.add(srgop         ); /*24*/ 
		} 

		strings.add(inpfileop       ); /*25*/ 
		strings.add(inpimgop        ); /*26*/ 
		strings.add(imgcon          ); /*27*/ 
		strings.add(bpassop         ); /*28*/ 

		if ( geomop.equals(dataFormatKey.getValue(RTMIGParameterDescriptionList.Ascii)) ) {
			strings.add(geomfile      ); /*29*/ 
		} 

		if ( inpimgop.equals(yesNoKey.getValue(RTMIGParameterDescriptionList.Yes) ) ) {
			strings.add(inpimgfile    ); /*30*/ 
		} 

		if ( bpassop.equals(yesNoKey.getValue(RTMIGParameterDescriptionList.Yes) ) ) {
			strings.add(bpf1          ); /*31*/ 
			strings.add(bpf2          ); /*32*/ 
			strings.add(bpf3          ); /*33*/ 
			strings.add(bpf4          ); /*34*/ 
		}

		return strings.toArray(new String[0]);
	}

	protected void copyParmsToVariables() {
		seisfile     =                            parameterList.get(0).getValue( );  
		velofile     =                            parameterList.get(1).getValue( );
		jobfname     =                            parameterList.get(2).getValue( );
		spor         = spatialOrderKey.getValue(  parameterList.get(3).getValue( ));
		ipr          =                            parameterList.get(4).getValue( );
		lt           =                            parameterList.get(5).getValue( );
		lts          =                            parameterList.get(6).getValue( );
		nsm_zz       =                            parameterList.get(7).getValue( );
		nsm_xline    =                            parameterList.get(8).getValue( );
		dz           =                            parameterList.get(9).getValue( );
		dx           =                            parameterList.get(10).getValue();
		seswap       = byteSwapKey.getValue(      parameterList.get(11).getValue());
		se2or4       = byteLengthKey.getValue(    parameterList.get(12).getValue());
		vswap        = byteSwapKey.getValue(      parameterList.get(13).getValue());
		v2or4        = byteLengthKey.getValue(    parameterList.get(14).getValue());
		vxl_sbyte    =                            parameterList.get(15).getValue();
		vil_sbyte    =                            parameterList.get(16).getValue();
		rtswap       = byteSwapKey.getValue(      parameterList.get(17).getValue());
		rt2or4       = byteLengthKey.getValue(    parameterList.get(18).getValue());
		rt_ibm_ieee  = floatingPointKey.getValue( parameterList.get(19).getValue());
		rt_xl_sbyte  =                            parameterList.get(20).getValue();
		rt_il_sbyte  =                            parameterList.get(21).getValue();
		geomop       = dataFormatKey.getValue(    parameterList.get(22).getValue());
		srgop        = receiverSpreadKey.getValue(parameterList.get(23).getValue());
		inpfileop    = muliFileKey.getValue(      parameterList.get(24).getValue());
		inpimgop     = yesNoKey.getValue(         parameterList.get(25).getValue());
		imgcon       = imageConditionKey.getValue(parameterList.get(26).getValue());
		bpassop      = yesNoKey.getValue(         parameterList.get(27).getValue());
		geomfile     =                            parameterList.get(28).getValue();
		inpimgfile   =                            parameterList.get(29).getValue();
		bpf1         =                            parameterList.get(30).getValue();
		bpf2         =                            parameterList.get(31).getValue();
		bpf3         =                            parameterList.get(32).getValue();
		bpf4         =                            parameterList.get(33).getValue();
	}


}
