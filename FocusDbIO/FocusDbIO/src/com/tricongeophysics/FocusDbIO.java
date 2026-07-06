package com.tricongeophysics;

import java.util.ArrayList;

import javax.swing.JOptionPane;

public class FocusDbIO {
	public static final String SDB_MSG_UNKNOWN   = "Unkown Error                            ";
	public static final String SDB_MSG_NORMAL    = "Normal Completion                       ";
	public static final String SDB_MSG_ILATTR    = "Illegal syntax for Attribute            ";
	public static final String SDB_MSG_ILCLASS   = "Illegal syntax for CLASS                ";
	public static final String SDB_MSG_ILEVENT   = "Illegal syntax for EVENT                ";
	public static final String SDB_MSG_ILKEY     = "Illegal syntax for KEY                  ";
	public static final String SDB_MSG_ILLINE    = "Illegal syntax for LINE                 ";
	public static final String SDB_MSG_ILMODEL   = "Illegal syntax for MODEL                ";
	public static final String SDB_MSG_ILPRJTYP  = "Cannot determine current project status ";
	public static final String SDB_MSG_ILPROJ    = "Illegal syntax for PROJECT NAME         ";
	public static final String SDB_MSG_LCLCLS    = "Failed to disconnect from local server  ";
	public static final String SDB_MSG_LCLCONN   = "Failed to connect to local server       ";
	public static final String SDB_MSG_LENERR    = "Array supplied too short for vector     ";
	public static final String SDB_MSG_MDLEXIST  = "Model definition already exists         ";
	public static final String SDB_MSG_MISM      = "First/Last ID mismatch.                 ";
	public static final String SDB_MSG_NMF       = "No more matching entries                ";
	public static final String SDB_MSG_NOTFND    = "Model, Attribute, or Database not found ";
	public static final String SDB_MSG_PROT      = "File protection precludes access        ";
	public static final String SDB_MSG_READ      = "Read Access Only                        ";
	public static final String SDB_MSG_REMCLS    = "Failed to disconnect from remote server ";
	public static final String SDB_MSG_REMCONN   = "Failed to connect to remote server      ";
	
	public static final int SDB_NORMAL     = 0   ;
	public static final int SDB_ILATTR     = 12  ;
	public static final int SDB_ILCLASS    = 333 ;
	public static final int SDB_ILEVENT    = 10  ;
	public static final int SDB_ILKEY      = 555 ;
	public static final int SDB_ILLINE     = 6   ;
	public static final int SDB_ILMODEL    = 8   ;
	public static final int SDB_ILPRJTYP   = 119 ;
	public static final int SDB_ILPROJ     = 33  ;
	public static final int SDB_LCLCLS     = 126 ;
	public static final int SDB_LCLCONN    = 125 ;
	public static final int SDB_LENERR     = 888 ;
	public static final int SDB_MDLEXIST   = 16  ;
	public static final int SDB_MISM       = 28  ;
	public static final int SDB_NMF        = 999 ;
	public static final int SDB_NOTFND     = 444 ;
	public static final int SDB_PROT       = 222 ;
	public static final int SDB_READ       = 111 ;
	public static final int SDB_REMCLS     = 128 ;
	public static final int SDB_REMCONN    = 127 ;
	static final String PgSurveyRoot = "PG_SURVEY_ROOT";
	
	
	private TriconGeometry tg;
	private int status;
	
	FocusDbIO() {
		tg = new TriconGeometry();
	}
	
	static {
		System.out.println("FocusDbIO library path is: "+ System.getenv("LD_LIBRARY_PATH"));
		System.loadLibrary("FocusDbIO");
		//System.loadLibrary("PGHpds");
	}

	public TriconGeometry getTg() {
		return tg;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FocusDbIO fdbio = new FocusDbIO();
		
		fdbio.initIDs();
		
//		TriconGeometry tg = new TriconGeometry("SCOTT3D", "PAULS3D");	
		try {
			fdbio.initializeProject("SCOTT3D", "/seisdata");
			fdbio.saveTextFile("This is just a test", "KEY", "TEST");
			
			TriconGeometry tg2 = new TriconGeometry("SCOTT3D", "TEST3");
			//fdbio.loadAttrNames(tg2, tg2.line );
//			ArrayList<FocusDbAttr> attrs = tg2.getShotModel().getAttributes();
//			for (int i=0; i< attrs.size(); i++) {
//				FocusDbAttr atr = attrs.get(i);
//				fdbio.loadAttr(atr);
//			}
			//FocusDbAttr atr = tg2.getRcvrModel().getAttr("X", "COORD");
			//fdbio.loadAttr(atr);
			
			fdbio.saveLongTextFile(getTestText(), "GEOMETRY NOTES", "TEST3");
			
			String text = fdbio.readLongTextFile("TEST3", "GEOMETRY NOTES");
			System.out.print("text is:\n" + text);
		} catch (FocusDbIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("java done");
	}

	private static String getTestText() {
		String text =
				"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" + "\n" +
						"000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999" + "\n" +
						"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
		return text;
	}

	//call to initialize method and class ID's in C
	public native void initIDs();

	public void initializeProject(String project, String pgSurveyRoot) throws FocusDbIOException {
//		System.setProperty(PgSurveyRoot, pgSurveyRoot);
//		System.out.println("using pgroot: " + pgSurveyRoot);
		if (pgSurveyRoot == null) {
			nativeInitializeProject(project);
		} else {
			nativeInitializeProjectWithSurveyRoot(project, pgSurveyRoot);
		}
//		nativeInitializeProject(project);
		processStatus(status, "Error Opening Project - " + project);
	}

	private native void nativeInitializeProject(String project);

	private native void nativeInitializeProjectWithSurveyRoot(String project, String pgSurveyRoot2);

	public void loadAttr(FocusDbAttr attr) throws FocusDbIOException {
		if (attr == null) {
			System.err.println("Trying to load null attribute!!");
			return;
		}
		nativeLoadAttr(attr);
		processStatus(status, "Error Loading Attribute - " + attr);
	}

	private native void nativeLoadAttr(FocusDbAttr attr);

	public void loadAttrNames(TriconGeometry tg2, String line) throws FocusDbIOException {
		nativeLoadAttrNames(tg2, line);
//		nativeLoadAttrNames2(tg2, line);
		processStatus(status, "Error Getting Attribute Names for Line - " + line);
	}

	private native void nativeLoadAttrNames2(TriconGeometry tg2, String line);

	private native void nativeLoadAttrNames(TriconGeometry tg2, String line);

	public void saveAttribute(FocusDbAttr attr) throws FocusDbIOException {
		nativeSaveAttribute(attr);
		processStatus(status, "Error Saving Attribute -\n" + attr.toString());
	}

	private native void nativeSaveAttribute(FocusDbAttr attr);

	public void getModel(FocusDbModel m) {
		nativeGetModel(m);
	}

	private native void nativeGetModel(FocusDbModel m);

	public void deleteModel(FocusDbModel m) throws FocusDbIOException {
		nativeDeleteModel(m);
		processStatus(status, "Error deleting model - " + m.getName());
	}

	private native void nativeDeleteModel(FocusDbModel m);

	public void createModel(FocusDbModel m) throws FocusDbIOException {
		nativeCreateModel(m);
		processStatus(status, "Error creating model - " + m.getName());
	}

	private native void nativeCreateModel(FocusDbModel m);

	public void checkExtendModel(FocusDbModel model) throws FocusDbIOException {
		FocusDbModel oldModel = new FocusDbModel(model.getName(), model.getTriconGeometry());
		nativeGetModel(oldModel);
		int iflag = 0; //flag for extending model (must tell Focus if extending from beginning or from end)
		if (model.getFirstID() < oldModel.getFirstID()) {
			iflag += 10;
		}
		if (model.getEnd() > oldModel.getEnd()) {
			iflag += 1;
		}
		if (iflag == 0) {
			System.out.println("FocusDbIO.extendModel - Model doesn't need extending: " + model);
			return;
		}
		System.out.println("FocusDbIO.extendModel - Extending Model: " + model);
		nativeExtendModel(model, iflag);
		processStatus(status, "Error extending model - " + model.getName());
	}

	private native void nativeExtendModel(FocusDbModel model, int iflag);

	public void saveTextFile(String text, String key, String lineName) throws FocusDbIOException {
		if (text == null) throw new FocusDbIOException(SDB_LENERR, "Can't save null text to file-" + key);
		if (key == null) throw new FocusDbIOException(SDB_ILKEY, "Can't save to null key");
		if (lineName == null) throw new FocusDbIOException(SDB_ILLINE, "Can't save -"+key+"- to null line name");
		nativeChrPut(text, key, lineName);
		processStatus(status, "Error Saving Text to File - " + lineName + "-" + key);
	}

	private native void nativeChrPut(String text, String key, String lineName);

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
//		processStatus(status);
	}

	private void processStatus(int status2, String messageIntro) throws FocusDbIOException {
		String msg = SDB_MSG_UNKNOWN;
		switch(status2) {
		case SDB_NORMAL  : msg = SDB_MSG_NORMAL   ; break;
		case SDB_ILATTR  : msg = SDB_MSG_ILATTR   ; break;
		case SDB_ILCLASS : msg = SDB_MSG_ILCLASS  ; break;
		case SDB_ILEVENT : msg = SDB_MSG_ILEVENT  ; break;
		case SDB_ILKEY   : msg = SDB_MSG_ILKEY    ; break;
		case SDB_ILLINE  : msg = SDB_MSG_ILLINE   ; break;
		case SDB_ILMODEL : msg = SDB_MSG_ILMODEL  ; break;
		case SDB_ILPRJTYP: msg = SDB_MSG_ILPRJTYP ; break;
		case SDB_ILPROJ  : msg = SDB_MSG_ILPROJ   ; break;
		case SDB_LCLCLS  : msg = SDB_MSG_LCLCLS   ; break;
		case SDB_LCLCONN : msg = SDB_MSG_LCLCONN  ; break;
		case SDB_LENERR  : msg = SDB_MSG_LENERR   ; break;
		case SDB_MDLEXIST: msg = SDB_MSG_MDLEXIST ; break;
		case SDB_MISM    : msg = SDB_MSG_MISM     ; break;
		case SDB_NMF     : msg = SDB_MSG_NMF      ; break;
		case SDB_NOTFND  : msg = SDB_MSG_NOTFND   ; break;
		case SDB_PROT    : msg = SDB_MSG_PROT     ; break;
		case SDB_READ    : msg = SDB_MSG_READ     ; break;
		case SDB_REMCLS  : msg = SDB_MSG_REMCLS   ; break;
		case SDB_REMCONN : msg = SDB_MSG_REMCONN  ; break;
		}
//		if (msg != null && status2 != SDB_NORMAL) JOptionPane.showMessageDialog(null, msg);
		if (status2 != SDB_NORMAL) {
			throw new FocusDbIOException(status2, messageIntro + ":\n\n" + msg);
		}
	}

	public boolean normalCompletion() {
		return (status == SDB_NORMAL);
	}

	public String readTextFile(String line, String key) throws FocusDbIOException {
		String text = nativeChrGet(line, key);
		processStatus(status, "Error Reading Text File - " + line + "-" + key);
		return text;
	}

	private native String nativeChrGet(String line, String key);

	public int[] readBinaryFile(String line, String klass, String key) throws FocusDbIOException {
		int[] data = nativeBinGet(line,klass,key);
		processStatus(status, "Error Reading Binary File - " + line + "." + klass + "." + key);
		return data;
	}

	private native int[] nativeBinGet(String line, String klass, String key);

	public void saveBinaryFile(String line, String klass, String key, int[] data) throws FocusDbIOException {
		nativeBinPut(line, klass, key, data);
		processStatus(status, "Error Writing Binary File - " + line + "." + klass + "." + key);
	}

	private native void nativeBinPut(String line, String klass, String key, int[] data);

	public void saveLongTextFile(String text, String key, String lineName) throws FocusDbIOException {
		if (text == null) throw new FocusDbIOException(SDB_LENERR, "Can't save null text to file-" + key);
		if (key == null) throw new FocusDbIOException(SDB_ILKEY, "Can't save to null key");
		if (lineName == null) throw new FocusDbIOException(SDB_ILLINE, "Can't save -"+key+"- to null line name");
		nativeAscPut(text, key, lineName);
		processStatus(status, "Error Saving Text to File - " + lineName + "-" + key);
	}

	private native void nativeAscPut(String text, String key, String lineName);

	public String readLongTextFile(String line, String key) throws FocusDbIOException {
		String text = nativeAscGet(line, key);
		processStatus(status, "Error Reading Text File - " + line + "-" + key);
		return text;
	}

	private native String nativeAscGet(String line, String key);
}


