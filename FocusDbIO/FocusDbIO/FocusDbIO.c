#include <string.h>
#include <jni.h>
#include <stdio.h>
#include "com_tricongeophysics_FocusDbIO.h"
#include <sdb.h>
#include <cnv-cfb.h>
//#include <tri_memory.h>
#include <stdlib.h>

//keep tricon geometry struct as global
//tricon_geometry C_tg;                   //struct containing information about current project geometry

//method and field ID's for java methods and fields are stored as global variables
//this way, the JVM only needs to look them up once per instance of FocusDbIO object
jmethodID MID_FocusDbIO_getTg;
jmethodID MID_FocusDbIO_setStatus;
jmethodID MID_FocusDbAttr_setVals;
jmethodID MID_FocusDbAttr_getVals;
jmethodID MID_TriconGeometry_getShotModel;
jmethodID MID_FocusDbModel_getFirstAttribute;
jmethodID MID_FocusDbModel_getProjectName;
jmethodID MID_FocusDbModel_getLineName;
jmethodID MID_FocusDbModel_getName;
jmethodID MID_FocusDbModel_setInc;
jmethodID MID_FocusDbModel_getInc;
jmethodID MID_FocusDbModel_setFirstID;
jmethodID MID_FocusDbModel_getFirstID;
//jmethodID MID_FocusDbModel_setEnd;
jmethodID MID_FocusDbModel_setNLocs;
jmethodID MID_FocusDbModel_setStatus;
jmethodID MID_FocusDbModel_getEnd;
jmethodID MID_FocusDbAttr_getProjectName;
jmethodID MID_FocusDbAttr_getLineName;
jmethodID MID_FocusDbAttr_getModelName;
jmethodID MID_FocusDbAttr_getEvent;
jmethodID MID_FocusDbAttr_getAttribute;
jmethodID MID_FocusDbAttr_setSize;
jmethodID MID_FocusDbAttr_getSize;
jmethodID MID_FocusDbAttr_getValsPerLoc;
jmethodID MID_FocusDbAttr_getFirstID;
jmethodID MID_TriconGeometry_addAttribute;
jmethodID MID_TriconGeometry_addAttribute2;
jmethodID MID_FocusDbAttr_setFirstID;


#define testing 1
#define MaxValsPerLoc 10000  //not best way to do this, but have to limit memory allocation to avoid crashing (large first-break array, etc.)
int maxshotlocs;
int maxshotvals;
int maxreclocs;
int maxrecvals;
int maxcdplocs;
int maxcdpvals;
INTEGER status;

//This call loads all Java methods into C globals so that they can be called without the expense of looking them
// up later in the Java Virtual Machine.
JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_initIDs (JNIEnv *env, jobject dbioObject)
{
	if (testing) printf("\ninit ids\n");
	jclass dbioClass = (*env)->GetObjectClass(env, dbioObject);
	if (dbioClass == NULL) {
		return; //exception thrown
	}

	//load TriconGeometry getter method
	if (testing) printf("\ninit Geometry\n");
	MID_FocusDbIO_getTg     = (*env)->GetMethodID(env, dbioClass, "getTg",     "()Lcom/tricongeophysics/TriconGeometry;");
	MID_FocusDbIO_setStatus = (*env)->GetMethodID(env, dbioClass, "setStatus", "(I)V");
	if (MID_FocusDbIO_getTg == NULL) {
		fprintf( stderr, "failed to find getTg\n");
		return; //exception thrown
	}

	//Get TriconGeometry Object
	jobject tgObject = (*env)->CallObjectMethod(env, dbioObject, MID_FocusDbIO_getTg);
	if (tgObject == NULL) {
		fprintf( stderr, "failed to find tgObject\n");
		return; //exception thrown
	}

	//get TriconGeometry Class
	jclass tgClass = (*env)->GetObjectClass(env, tgObject);
	if (tgClass == NULL) {
		fprintf( stderr, "failed to find tgClass\n");
		return; //exception thrown
	}

	//load FocusDbModel getter method
	if (testing) printf("\ninit FocusDbModel\n");
	MID_TriconGeometry_getShotModel  = (*env)->GetMethodID(env, tgClass, "getShotModel",  "()Lcom/tricongeophysics/ShotDbModel;");
	MID_TriconGeometry_addAttribute  = (*env)->GetMethodID(env, tgClass, "addAttribute",  "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
	MID_TriconGeometry_addAttribute2 = (*env)->GetMethodID(env, tgClass, "addAttribute2", "([C[C[C[C)V");
	if (MID_TriconGeometry_getShotModel == NULL) {
		fprintf( stderr, "failed to find getShotModel\n");
		return; //exception thrown
	}

	//get FocusDbModel object
	jobject dbModelObject = (*env)->CallObjectMethod(env, tgObject, MID_TriconGeometry_getShotModel);
	if (dbModelObject == NULL) {
		fprintf( stderr, "failed to find dbModelObject\n");
		return; //exception thrown
	}

	//get FocusDbModel class
	jclass dbModelClass = (*env)->GetObjectClass(env, dbModelObject);
	if (dbModelClass == NULL) {
		fprintf( stderr, "failed to find dbModelClass\n");
		return; //exception thrown
	}

	//load FocusDbAttr getter method
	if (testing) printf("\ninit FocusDbAttr\n");
	MID_FocusDbModel_getFirstAttribute = (*env)->GetMethodID(env, dbModelClass, "getFirstAttribute", "()Lcom/tricongeophysics/FocusDbAttr;");
	MID_FocusDbModel_getProjectName    = (*env)->GetMethodID(env, dbModelClass, "getProjectName",    "()Ljava/lang/String;");
	MID_FocusDbModel_getLineName       = (*env)->GetMethodID(env, dbModelClass, "getLineName",       "()Ljava/lang/String;");
	MID_FocusDbModel_getName           = (*env)->GetMethodID(env, dbModelClass, "getName",           "()Ljava/lang/String;");
	MID_FocusDbModel_setInc            = (*env)->GetMethodID(env, dbModelClass, "setInc",            "(I)V");
	MID_FocusDbModel_getInc            = (*env)->GetMethodID(env, dbModelClass, "getInc",            "()I");
	MID_FocusDbModel_setFirstID        = (*env)->GetMethodID(env, dbModelClass, "setFirstID",        "(I)V");
	MID_FocusDbModel_getFirstID        = (*env)->GetMethodID(env, dbModelClass, "getFirstID",        "()I");
//	MID_FocusDbModel_setEnd            = (*env)->GetMethodID(env, dbModelClass, "setEnd",            "(I)V");
	MID_FocusDbModel_setNLocs          = (*env)->GetMethodID(env, dbModelClass, "setNlocs",          "(I)V");
	MID_FocusDbModel_getEnd            = (*env)->GetMethodID(env, dbModelClass, "getEnd",            "()I");
	MID_FocusDbModel_setStatus         = (*env)->GetMethodID(env, dbModelClass, "setStatus",         "(Ljava/lang/String;)V");
	if (MID_FocusDbModel_getFirstAttribute == NULL) {
		fprintf( stderr, "failed to find getFirstAttribute\n");
		return; //exception thrown
	}

	//get FocusDbAttr object
	jobject dbAttrObject = (*env)->CallObjectMethod(env, dbModelObject, MID_FocusDbModel_getFirstAttribute);
	if (dbAttrObject == NULL) {
		fprintf( stderr, "failed to find dbAttrObject\n");
		return; //exception thrown
	}
	
	//get FocusDbAttr class
	jclass dbAttrClass = (*env)->GetObjectClass(env, dbAttrObject);
	if (dbAttrClass == NULL) {
		fprintf( stderr, "failed to find dbAttrClass\n");
		return; //exception thrown
	}

	//get setVals method
	if (testing) printf("\nfinding setVals method\n");
	MID_FocusDbAttr_setVals        = (*env)->GetMethodID(env, dbAttrClass, "setVals",        "([F)V");
	MID_FocusDbAttr_getVals        = (*env)->GetMethodID(env, dbAttrClass, "getVals",        "()[F");
	MID_FocusDbAttr_getProjectName = (*env)->GetMethodID(env, dbAttrClass, "getProjectName", "()Ljava/lang/String;");
	MID_FocusDbAttr_getLineName    = (*env)->GetMethodID(env, dbAttrClass, "getLineName",    "()Ljava/lang/String;");
	MID_FocusDbAttr_getModelName   = (*env)->GetMethodID(env, dbAttrClass, "getModelName",   "()Ljava/lang/String;");
	MID_FocusDbAttr_getEvent       = (*env)->GetMethodID(env, dbAttrClass, "getEvent",       "()Ljava/lang/String;");
	MID_FocusDbAttr_getAttribute   = (*env)->GetMethodID(env, dbAttrClass, "getAttribute",   "()Ljava/lang/String;");
	MID_FocusDbAttr_setSize        = (*env)->GetMethodID(env, dbAttrClass, "setSize",        "(I)V");
	MID_FocusDbAttr_getSize        = (*env)->GetMethodID(env, dbAttrClass, "getSize",        "()I");
	MID_FocusDbAttr_getValsPerLoc  = (*env)->GetMethodID(env, dbAttrClass, "getValsPerLoc",  "()I");
	MID_FocusDbAttr_setFirstID     = (*env)->GetMethodID(env, dbAttrClass, "setFirstID",     "(I)V");
	MID_FocusDbAttr_getFirstID     = (*env)->GetMethodID(env, dbAttrClass, "getFirstID",     "()I");
	if (MID_FocusDbAttr_setVals == NULL) {
		fprintf( stderr, "failed to find setVals\n");
		return; //exception thrown
	}

	if (testing) printf("\nfound all methods\n\n");
}

/**
 * Get attribute values from Focus
 */
static void loadAttr(char* line_name, char* model, char* event, char* attr, INTEGER* vals, INTEGER* nlocs, INTEGER* nvals, INTEGER* ixval, INTEGER* ixcode, INTEGER* xstart)
{
	if (testing) printf("inside loadAttr\n");

	char interp[16];
	char xunit[16];
	char buftyp[16];
	INTEGER xinc, xend;

	strcpy(buftyp, "USE");
	status = 9999;

	if (testing) printf("status %i\n", status);
	if (testing) printf("line:%s model:%s event:%s attr:%s\n", line_name, model, event, attr);

	sdb_atrget_f(line_name, model, event, attr, buftyp, vals, interp, 10,
			xunit, 10, &xinc, xstart, &xend, nlocs, nvals, ixval,
			ixcode, &status);

	if (testing) printf("status:   %i  xstart:  %i\n", status, *xstart);
	if (status != SDB_NORMAL) {
		fprintf(stderr, "FocusDbIO: Error loading %s:%s:%s\n", model, event,
				attr);
	}
	if (status == SDB_NORMAL) {
		if (testing) printf("Attribute Get Success!\n");
	}
}

/**
 * Save attribute values to Focus
 */
static void saveAttr(char* line_name, char* model, char* event, char* attr, INTEGER* vals, INTEGER nlocs, INTEGER nvals, INTEGER xstart)
{
	if (testing) printf("inside saveAttr\n");
	status = 9999;

	char interp[16];
	char xunit[16];

	INTEGER xinc;
	strcpy(interp, "");
	strcpy(xunit, "");
	xinc = 1;

	if (testing) printf("status %i\n", status);
	if (testing) printf("line:%s model:%s event:%s attr:%s\n", line_name, model, event, attr);

	sdb_atrputs_f(line_name, model, event, attr, interp,
			xunit, xinc, xstart, nlocs, nvals, vals, &status);

	if (testing) printf("status:   %i  xstart:  %i\n", status, xstart);

	if (status != SDB_NORMAL) {
		fprintf(stderr, "FocusDbIO: Error saving %s:%s:%s\n", model, event, attr);
	}
	if (status == SDB_MISM) {
		fprintf(stderr, "Values of XINC, XSTART, or NLOCS do not match values defined for the model. xinc: %i, xstart: %i, nlocs: %i\n", xinc, xstart, nlocs);
	}
	if (status == SDB_NORMAL) {
		if (testing) printf("Successfully saved attribute %s:%s:%s\n", model, event, attr);
	}
}


static int initializeProject(char* project)
{
	status = 9999;
	if (testing){
		const char* pgarch;
		const char *name = "PG_ARCH";
		pgarch = getenv(name);
		printf("pg_arch:       '%s'\n", pgarch);

		const char* ldkern;
		const char* name2 = "LD_ASSUME_KERNEL";
		ldkern = getenv(name2);
		printf("ld_assume_kernel: '%s'\n", ldkern);

		const char* pgroot;
		const char* name3 = "PG_ROOT";
		pgroot = getenv(name3);
		printf("pg_root: '%s'\n", pgroot);

		const char* ldpath;
		const char* name4 = "LD_LIBRARY_PATH";
		ldpath = getenv(name4);
		printf("ld_library_path: '%s'\n", ldpath);

		printf("status:   %i\n", status);
	}

	sdb_sdbopn_f(project, &status); //segv (no compile errors)

	if (testing) printf("status:   %i\n", status);
	if (status != SDB_NORMAL) {
		const char* psurveygroot;
		const char *name = "PG_SURVEY_ROOT";
		psurveygroot = getenv(name);
		fprintf(stderr, "Failed to open project: %s. PG_SURVEY_ROOT=%s\n", project, psurveygroot);
		//		fprintf(stderr, "Failed to open project: %s\n", project);
	}

	if (status == SDB_NORMAL) {
		if (testing) printf("Successfully opened project %s!\n", project);
		return 1;
	}
	return 0;
}

void processStatus(JNIEnv * env, INTEGER status2, jobject dbioObject)
{
//	if (testing) printf("SDB_NORMAL     is: %i\n", SDB_NORMAL   );
//	if (testing) printf("SDB_ILATTR     is: %i\n", SDB_ILATTR   );
//	if (testing) printf("SDB_ILCLASS    is: %i\n", SDB_ILCLASS  );
//	if (testing) printf("SDB_ILEVENT    is: %i\n", SDB_ILEVENT  );
//	if (testing) printf("SDB_ILKEY      is: %i\n", SDB_ILKEY    );
//	if (testing) printf("SDB_ILLINE     is: %i\n", SDB_ILLINE   );
//	if (testing) printf("SDB_ILMODEL    is: %i\n", SDB_ILMODEL  );
//	if (testing) printf("SDB_ILPRJTYP   is: %i\n", SDB_ILPRJTYP );
//	if (testing) printf("SDB_ILPROJ     is: %i\n", SDB_ILPROJ   );
//	if (testing) printf("SDB_LCLCLS     is: %i\n", SDB_LCLCLS   );
//	if (testing) printf("SDB_LCLCONN    is: %i\n", SDB_LCLCONN  );
//	if (testing) printf("SDB_LENERR     is: %i\n", SDB_LENERR   );
//	if (testing) printf("SDB_MDLEXIST   is: %i\n", SDB_MDLEXIST );
//	if (testing) printf("SDB_MISM       is: %i\n", SDB_MISM     );
//	if (testing) printf("SDB_NMF        is: %i\n", SDB_NMF      );
//	if (testing) printf("SDB_NOTFND     is: %i\n", SDB_NOTFND   );
//	if (testing) printf("SDB_PROT       is: %i\n", SDB_PROT     );
//	if (testing) printf("SDB_READ       is: %i\n", SDB_READ     );
//	if (testing) printf("SDB_REMCLS     is: %i\n", SDB_REMCLS   );
//	if (testing) printf("SDB_REMCONN    is: %i\n", SDB_REMCONN  );

	if (status2 == SDB_ILLINE) {
		fprintf(stderr, "Illegal syntax for LINE\n");
	}
	if (status2 == SDB_ILMODEL) {
		fprintf(stderr, "Illegal syntax for MODEL\n");
	}
	if (status2 == SDB_ILEVENT) {
		fprintf(stderr, "Illegal syntax for EVENT\n");
	}
	if (status2 == SDB_ILATTR) {
		fprintf(stderr, "Illegal syntax for Attribute\n");
	}
	if (status2 == SDB_NOTFND) {
		fprintf(stderr, "Model, Attribute, or Database not found\n");
	}
	if (status2 == SDB_PROT) {
		fprintf(stderr, "File protection precludes access\n");
	}
	if (status2 == SDB_MDLEXIST) {
		fprintf(stderr, "Model definition already exists\n");
	}
	if (status2 == SDB_MISM) {
		fprintf(stderr, "First/Last ID mismatch. Must be outside current min/max range to extend\n");
	}
	if (status2 == SDB_ILKEY) {
		fprintf(stderr, "Illegal syntax for KEY\n");
	}
	if (status2 == SDB_READ) {
		fprintf(stderr, "Read Access Only\n");
	}
	if (status2 == SDB_ILCLASS) {
		fprintf(stderr, "Illegal syntax for CLASS\n");
	}
	if (status2 == SDB_LENERR) {
		fprintf(stderr, "Array supplied too short for vector\n");
	}
	if (status2 == SDB_NMF) {
		fprintf(stderr, "No more matching entries\n");
	}
	if (status2 == SDB_ILPROJ) {
		fprintf(stderr, "Illegal syntax for PROJECT NAME\n");
	}
	if (status2 == SDB_LCLCONN) {
		fprintf(stderr, "Failed to connect to local server\n");
	}
	if (status2 == SDB_REMCONN) {
		fprintf(stderr, "Failed to connect to remote server\n");
	}
	if (status2 == SDB_LCLCLS) {
		fprintf(stderr, "Failed to disconnect from local server\n");
	}
	if (status2 == SDB_REMCLS) {
		fprintf(stderr, "Failed to disconnect from remote server\n");
	}
	if (status2 == SDB_ILPRJTYP) {
		fprintf(stderr, "Cannot determine current project status\n");
	}

	//...Send status to Java
	(*env)->CallVoidMethod(env, dbioObject, MID_FocusDbIO_setStatus, (jint)status);
}

/**
 * This method uses PG_SURVEY_ROOT passed from arguments
 */
JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeInitializeProjectWithSurveyRoot
(JNIEnv * env, jobject dbioObject, jstring jproject, jstring jpgSurveyRoot)
{
	if (testing) printf("getting project\n");

	//...convert java strings to C strings
	const jbyte *project;
	const jbyte *pgRoot;
	project = (jbyte*) (*env)->GetStringUTFChars(env, jproject, NULL); //project names are only 8 characters
	pgRoot  = (jbyte*) (*env)->GetStringUTFChars(env, jpgSurveyRoot, NULL);

	if (testing) printf("project:       '%s'\n", project);
	if (testing) printf("pgSurveyRoot:  '%s'\n", pgRoot);

	char* cproject = (char*) project;
	char* cpgRoot  = (char*) pgRoot;

	const char *name = "PG_SURVEY_ROOT";
	if (setenv(name, cpgRoot, 1) + 1) { //returns 0 if successful, -1 if no success
		initializeProject(cproject);
	} else {
		fprintf(stderr, "Failed to set %s to: %s\n", name,  cpgRoot);
//		initializeProject(cproject);
	}

	processStatus(env, status, dbioObject);
}

/**
 * This method uses PG_SURVEY_ROOT from the environment
 */
JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeInitializeProject
(JNIEnv * env, jobject dbioObject, jstring jproject)
{
	if (testing) printf("getting project\n");

	//...convert java strings to C strings
	const jbyte *project;
	project = (jbyte*) (*env)->GetStringUTFChars(env, jproject, NULL); //project names are only 8 characters
	if (testing) printf("project:       '%s'\n", project);

	char* cproject = (char*) project;

	initializeProject(cproject);

	processStatus(env, status, dbioObject);
}


//static void getAttrList(char* line_name) {
//	INTEGER next_id, xinc, xstart, xend, nlocs, nvals;
//	printf("get attr list\n");
//
//	char line[10];
//	char model[10];
//	char event[10];
//	char attr[10];
//	char interp[10];
//	char xunit[10];
//	char mdate[10];
//
//	strcpy(line, line_name);
//	strcpy(model, "STATION");
//	strcpy(event, "X");
//	strcpy(attr, "COORD");
//	strcpy(interp, "yo");
//	strcpy(xunit, "yo");
//	strcpy(mdate, "today");
//
//	//atrmsk to get iterator pointer for attributes
//	status = 9999;
//	printf("status %i\n", status);
//	sdb_atrmsk_f("*", "*", "*", "*", &next_id, &status);
//	printf("status %i\n", status);
//
//	printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, line, model, event, attr);
//	while (status == SDB_NORMAL) {
//		sdb_atrnxt_f(&next_id, line, 8, model, 8, event, 8, attr, 8, interp,
//				8, xunit, 8, &xinc, &xstart, &xend, &nlocs, &nvals, mdate, 8, &status);
//		//find attributes for selected line_name
//		if (strcmp( line, line_name) == 0) {
//			if (testing) printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, line, model, event, attr);
//		}
//	}
//	printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, line, model, event, attr);
//}

/**
 * Stores attribute values from Focus into Java attribute "jattr"
 */
JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeLoadAttr (JNIEnv * env, jobject dbioObject, jobject jattr)
{
	if (testing) printf("loading attr\n");
	jstring jproject = (*env)->CallObjectMethod(env, jattr, MID_FocusDbAttr_getProjectName);
	jstring jline    = (*env)->CallObjectMethod(env, jattr, MID_FocusDbAttr_getLineName);
	jstring jmodel   = (*env)->CallObjectMethod(env, jattr, MID_FocusDbAttr_getModelName);
	jstring jevent   = (*env)->CallObjectMethod(env, jattr, MID_FocusDbAttr_getEvent);
	jstring jatrname = (*env)->CallObjectMethod(env, jattr, MID_FocusDbAttr_getAttribute);

	const jbyte *project = (jbyte*) (*env)->GetStringUTFChars(env, jproject, NULL);
	const jbyte *line    = (jbyte*) (*env)->GetStringUTFChars(env, jline, NULL);
	const jbyte *model   = (jbyte*) (*env)->GetStringUTFChars(env, jmodel, NULL);
	const jbyte *event   = (jbyte*) (*env)->GetStringUTFChars(env, jevent, NULL);
	const jbyte *attr    = (jbyte*) (*env)->GetStringUTFChars(env, jatrname, NULL);

	if (testing) {
		printf("Project :%s\n", project);
		printf("Line    :%s\n", line);
		printf("Model   :%s\n", model);
		printf("Event   :%s\n", event);
		printf("Attr    :%s\n", attr);
	}

	char* cproject = (char*) project;
	char* cline    = (char*) line;
	char* cmodel   = (char*) model;
	char* cevent   = (char*) event;
	char* cattr    = (char*) attr;

	if (!initializeProject(cproject)) {
		return;
	}

	INTEGER* vals;
	INTEGER nlocs, ixval, ixcode, nvals, xstart;

	//int model_size = 10000000;
	int maxVals = 0;
	int maxLocs = 0;
	if (strcmp( cmodel, "SHOT") == 0) {
		maxVals = maxshotvals;
		maxLocs = maxshotlocs;
	}
	if (strcmp( cmodel, "STATION") == 0) {
		//model_size = maxrecvals * maxreclocs;
		maxVals = maxrecvals;
		maxLocs = maxreclocs;
	}
	if (strcmp( cmodel, "CDP") == 0) {
		//model_size = maxcdpvals * maxcdplocs;
		maxVals = maxcdpvals;
		maxLocs = maxcdplocs;
	}
	//if (maxVals > 10000) maxVals = 10000;
	int model_size = maxVals * (maxLocs + 1);
	if (testing) printf("model size: %i maxVals: %i maxLocs: %i\n", model_size, maxVals, maxLocs);
	if (model_size < 1) {
		fprintf(stderr, "maxsvals 0r maxlocs < 1. Need to load run loadAttrNames first!!\n");
		return;
	}

	int value_and_code = 2;
	if (testing) printf("getting memory\n");
	vals = (INTEGER *) malloc(sizeof(INTEGER) * model_size * value_and_code);

	if (testing) printf("status   :%i\n", status);
	if (testing) printf("calling loadAttr()\n");
	loadAttr(cline, cmodel, cevent, cattr, vals, &nlocs, &nvals, &ixval, &ixcode, &xstart);

	processStatus(env, status, dbioObject);

	if (vals == NULL) {
		fprintf(stderr, "vals is null for %s:%s:%s\n", model, event, attr);
		return;
	}
	if (nlocs < 1) {
		fprintf(stderr, "nlocs less than 1 for %s:%s:%s\n", model, event, attr);
		return;
	}

	float* fvals = (float*) vals; //focus array is actually floats

	if (testing) printf("vals[0]->%f\n",fvals[0]);
	if (testing) printf("nlocs: %i nvals: %i\n",nlocs, nvals);

//	jfloatArray jvals = (*env)->NewFloatArray(env, nlocs);
//	(*env)->SetFloatArrayRegion(env, jvals, 0, nlocs, fvals+ixval-1);
	jfloatArray jvals = (*env)->NewFloatArray(env, nlocs*nvals);
	(*env)->SetFloatArrayRegion(env, jvals, 0, nlocs*nvals, fvals+ixval-1);

	(*env)->CallVoidMethod(env, jattr, MID_FocusDbAttr_setFirstID, (jint)xstart);
	(*env)->CallVoidMethod(env, jattr, MID_FocusDbAttr_setSize, (jint)nlocs);
	(*env)->CallVoidMethod(env, jattr, MID_FocusDbAttr_setVals, jvals);
	if (testing) printf("done loading vals\n");

	if (testing) printf("freeing memory\n\n\n");
	free(vals);
//	free(fvals);
	(*env)->ReleaseStringUTFChars(env, jproject, cproject);
	(*env)->ReleaseStringUTFChars(env, jline,    cline);
	(*env)->ReleaseStringUTFChars(env, jmodel,   cmodel);
	(*env)->ReleaseStringUTFChars(env, jevent,   cevent);
	(*env)->ReleaseStringUTFChars(env, jatrname, cattr);
}


jcharArray getJCharArray(JNIEnv * env, char* chars, int len) {
	unsigned short shorts[len];
	int i=0;
	for (i=0; i<len; i++) {
		shorts[i] = chars[i];
	}
	jcharArray jchars = (*env)->NewCharArray(env, len);
	(*env)->SetCharArrayRegion(env, jchars, 0, len, shorts);
	return jchars;
}

void copyJCharArray(JNIEnv * env, char* chars, jcharArray jchars, int len) {
	unsigned short shorts[len];
	int i=0;
	for (i=0; i<len; i++) {
		shorts[i] = chars[i];
	}
	(*env)->SetCharArrayRegion(env, jchars, 0, len, shorts);
}

/**
 * Sends list of attribute names from Focus into Java by adding attributes to TriconGeometry Java object
 */
JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeLoadAttrNames (JNIEnv * env, jobject jdbioObject, jobject jtriconGeometry, jstring jline)
{
	if (testing) printf("inside loadAttrNames\n");

	const jbyte *line    = (jbyte*) (*env)->GetStringUTFChars(env, jline, NULL);\
	char* cline    = (char*) line;

	if (testing) printf("Line    :%s\n", line);

	INTEGER next_id, xinc, xstart, xend, nlocs, nvals;

	char line2[12];
	char model[12];
	char event[12];
	char attr[12];
	char interp[12];
	char xunit[12];
	char mdate[12];

	strcpy(line2, cline);

	//atrmsk to get iterator pointer for attributes
	status = 9999;
	if (testing) printf("status %i\n", status);
	sdb_atrmsk_f("*", "*", "*", "*", &next_id, &status);
	if (testing) printf("status %i\n", status);

	if (testing) printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, line, model, event, attr);
	maxshotlocs = maxshotvals = 0;
	maxreclocs = maxrecvals = 0;
	maxcdplocs = maxcdpvals = 0;
	while (status == SDB_NORMAL) {
		sdb_atrnxt_f(&next_id, line2, 10, model, 10, event, 10, attr, 10, interp,
				10, xunit, 10, &xinc, &xstart, &xend, &nlocs, &nvals, mdate, 10, &status);
		//find attributes for selected line_name
		if (strcmp( cline, line2) == 0) {
			if (testing) printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, line, model, event, attr);
			jstring jmodel = (*env)->NewStringUTF(env, model);
			jstring jevent = (*env)->NewStringUTF(env, event);
			jstring jattr = (*env)->NewStringUTF(env, attr);
			(*env)->CallVoidMethod(env, jtriconGeometry, MID_TriconGeometry_addAttribute, jline, jmodel, jevent, jattr); //here's where attribute gets sent to Java

			if (nvals > MaxValsPerLoc) {
				fprintf(stderr, "Max Vals Per Loc exceeded! %i for %s:%s:%s\n", nvals, model, event, attr);
				nvals = 0;
			}

			//...Keep track of attribute sizes for later memory allocation when we actually load the attributes
			if (strcmp( model, "SHOT") == 0) {
				if (nlocs > maxshotlocs) maxshotlocs = nlocs;
				if (nvals > maxshotvals) maxshotvals = nvals;
			}
			if (strcmp( model, "STATION") == 0) {
				if (nlocs > maxreclocs) maxreclocs = nlocs;
				if (nvals > maxrecvals) maxrecvals = nvals;
			}
			if (strcmp( model, "CDP") == 0) {
				if (nlocs > maxcdplocs) maxcdplocs = nlocs;
				if (nvals > maxcdpvals) maxcdpvals = nvals;
			}
		}
	}
	//printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, line, model, event, attr);
	(*env)->ReleaseStringUTFChars(env, jline, cline);

	if (status == SDB_NMF) status = SDB_NORMAL;  //no more attributes found, which is normal at this point
	processStatus(env, status, jdbioObject);
}



/**
 * Sends list of attribute names from Focus into Java by adding attributes to TriconGeometry Java object
 */
JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeLoadAttrNames2 (JNIEnv * env, jobject jdbioObject, jobject jtriconGeometry, jstring jline)
{
	if (testing) printf("inside loadAttrNames\n");

	const jbyte *line    = (jbyte*) (*env)->GetStringUTFChars(env, jline, NULL);\
	char* cline    = (char*) line;

	if (testing) printf("Line    :%s\n", cline);

	INTEGER next_id, xinc, xstart, xend, nlocs, nvals;

	char line2[12];
	char model[12];
	char event[12];
	char attr[12];
	char interp[12];
	char xunit[12];
	char mdate[12];

	strcpy(line2, cline);
	if (testing) printf("Line2   :%s\n", line2);

	//atrmsk to get iterator pointer for attributes
	status = 9999;
	if (testing) printf("status %i\n", status);
	sdb_atrmsk_f("*", "*", "*", "*", &next_id, &status);
	if (testing) printf("status %i\n", status);

	if (testing) printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, cline, model, event, attr);
	maxshotlocs = maxshotvals = 0;
	maxreclocs = maxrecvals = 0;
	maxcdplocs = maxcdpvals = 0;

	jcharArray jline2  = (*env)->NewCharArray(env, 12);
	jcharArray jmodel = (*env)->NewCharArray(env, 12);
	jcharArray jevent = (*env)->NewCharArray(env, 12);
	jcharArray jattr = (*env)->NewCharArray(env, 12);
	while (status == SDB_NORMAL) {
		if (env == NULL) {
			printf("env null");
			return;
		}
		if (jtriconGeometry == NULL ||  MID_TriconGeometry_addAttribute2 == 0 || jline2 == NULL) {
			printf("went to crap");
			return;
		}
		sdb_atrnxt_f(&next_id, line2, 10, model, 10, event, 10, attr, 10, interp,
				10, xunit, 10, &xinc, &xstart, &xend, &nlocs, &nvals, mdate, 10, &status);
		//find attributes for selected line_name
		if (strcmp( event, "FBNET") == 0) continue;  //skip first break picks
		if (strcmp( cline, line2) == 0) {
			if (testing) printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, cline, model, event, attr);
			if(event == NULL || attr == NULL) {
				printf("trying to load null event or attribute");
				continue;
			}
//			char model2[12];
//			char event2[12];
//			char attr2[12];
//			char cline2[12];
//			strcpy(model2, model);
//			strcpy(event2, event);
//			strcpy(attr2, attr);
//			strcpy(cline2, cline);
//			 jline2 = getJCharArray(env, cline2, 12);
//			 jmodel = getJCharArray(env, model2, 12);
//			 jevent = getJCharArray(env, event2, 12);
////			 jattr  = getJCharArray(env, attr2, 12);
//			copyJCharArray(env, cline2, jline2, 12);
//			copyJCharArray(env, model2, jmodel, 12);
//			copyJCharArray(env, event2, jevent, 12);
//			copyJCharArray(env, attr2, jattr, 12);

			copyJCharArray(env, cline, jline2, 12);
			copyJCharArray(env, model, jmodel, 12);
			copyJCharArray(env, event, jevent, 12);
			copyJCharArray(env, attr, jattr, 12);


			//			(*env)->SetCharArrayRegion(env, jmodel, 0, 12, model2);
			//			jline2 = (*env)->NewStringUTF(env, cline2);
//			jmodel = (*env)->NewStringUTF(env, model2);
//			jevent = (*env)->NewStringUTF(env, event2);
//			jattr  = (*env)->NewStringUTF(env, attr2);
//			if (testing) printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, cline2, model2, event2, attr2);
//			jstring jline2 = (*env)->NewStringUTF(env, cline);
//			jstring jmodel = (*env)->NewStringUTF(env,model);
//			jstring jevent = (*env)->NewStringUTF(env, event);
//			jstring jattr  = (*env)->NewStringUTF(env, attr);
//			jintArray jline2 = (*env)->NewIntArray(env, 12);
//			(*env)->SetIntArrayRegion(env, jline2, 0, 12, (jint*)cline);
//			jintArray jmodel = (*env)->NewIntArray(env, 12);
//			(*env)->SetIntArrayRegion(env, jmodel, 0, 12, (jint*)model);
//			jshortArray jline2 = (*env)->NewShortArray(env, 12);
//			(*env)->SetShortArrayRegion(env, jline2, 0, 12, (jshort*)cline);
//			jshortArray jmodel = (*env)->NewShortArray(env, 12);
//			(*env)->SetShortArrayRegion(env, jmodel, 0, 12, (jshort*)model);
			//			(*env)->CallVoidMethod(env, jtriconGeometry, MID_TriconGeometry_addAttribute, jline2, jmodel, jevent, jattr); //here's where attribute gets sent to Java
//			(*env)->CallVoidMethod(env, jtriconGeometry, MID_TriconGeometry_addAttribute2, jline2, jmodel, jevent, jattr); //here's where attribute gets sent to Java

//			    (*env)->CallVoidMethod(env, jtriconGeometry, MID_TriconGeometry_addAttribute2, jline2, NULL, NULL, NULL); //here's where attribute gets sent to Java
//			(*env)->CallVoidMethod(env, jtriconGeometry, MID_TriconGeometry_addAttribute, NULL, jmodel, jevent, jattr); //here's where attribute gets sent to Java
//			(*env)->CallVoidMethod(env, jtriconGeometry, MID_TriconGeometry_addAttribute, cline2, model2, event2, attr2); //here's where attribute gets sent to Java
//			(*env)->CallVoidMethod(env, jtriconGeometry, MID_TriconGeometry_addAttribute, cline2); //here's where attribute gets sent to Java
//			(*env)->CallVoidMethod(env, jtriconGeometry, MID_TriconGeometry_addAttribute, NULL, NULL, NULL, NULL); //here's where attribute gets sent to Java
//			status = 9999;
//			(*env)->ReleaseStringUTFChars(env, jattr, attr);

			if (nvals > MaxValsPerLoc) {
				fprintf(stderr, "Max Vals Per Loc exceeded! %i for %s:%s:%s\n", nvals, model, event, attr);
				nvals = 0;
			}

			//...Keep track of attribute sizes for later memory allocation when we actually load the attributes
			if (strcmp( model, "SHOT") == 0) {
				if (nlocs > maxshotlocs) maxshotlocs = nlocs;
				if (nvals > maxshotvals) maxshotvals = nvals;
			}
			if (strcmp( model, "STATION") == 0) {
				if (nlocs > maxreclocs) maxreclocs = nlocs;
				if (nvals > maxrecvals) maxrecvals = nvals;
			}
			if (strcmp( model, "CDP") == 0) {
				if (nlocs > maxcdplocs) maxcdplocs = nlocs;
				if (nvals > maxcdpvals) maxcdpvals = nvals;
			}
		}
	}
	//printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, line, model, event, attr);

	if (status == SDB_NMF) status = SDB_NORMAL;  //no more attributes found, which is normal at this point
	processStatus(env, status, jdbioObject);

	if (testing) printf("status %i\n", status);
	if (status == SDB_NORMAL) {
		if (testing) printf("Successfully Loaded Attributes for Line: %s", cline);
	} else {
		fprintf(stderr, "Failed to Load Attributes for Line: %s\n", cline);
	}
	(*env)->ReleaseStringUTFChars(env, jline, cline);
}

/**
 * Save attribute data from Java attribute to Focus
 */
JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeSaveAttribute (JNIEnv * env, jobject jdbioObject, jobject jattr)
{
	if (testing) printf("inside saveAttribute\n");

	jstring jproject  = (*env)->CallObjectMethod(env, jattr, MID_FocusDbAttr_getProjectName);
	jstring jline     = (*env)->CallObjectMethod(env, jattr, MID_FocusDbAttr_getLineName);
	jstring jmodel    = (*env)->CallObjectMethod(env, jattr, MID_FocusDbAttr_getModelName);
	jstring jevent    = (*env)->CallObjectMethod(env, jattr, MID_FocusDbAttr_getEvent);
	jstring jatrname  = (*env)->CallObjectMethod(env, jattr, MID_FocusDbAttr_getAttribute);
	jfloatArray jvals = (*env)->CallObjectMethod(env, jattr, MID_FocusDbAttr_getVals); //attribute values
	jint        jsize = (*env)->CallIntMethod   (env, jattr, MID_FocusDbAttr_getSize);
	jint  jValsPerLoc = (*env)->CallIntMethod   (env, jattr, MID_FocusDbAttr_getValsPerLoc);
	jint     jFirstID = (*env)->CallIntMethod   (env, jattr, MID_FocusDbAttr_getFirstID);

	const jbyte *project = (jbyte*) (*env)->GetStringUTFChars(env, jproject, NULL);
	const jbyte *line    = (jbyte*) (*env)->GetStringUTFChars(env, jline, NULL);
	const jbyte *model   = (jbyte*) (*env)->GetStringUTFChars(env, jmodel, NULL);
	const jbyte *event   = (jbyte*) (*env)->GetStringUTFChars(env, jevent, NULL);
	const jbyte *attr    = (jbyte*) (*env)->GetStringUTFChars(env, jatrname, NULL);

	if (testing) {
		printf("Project :%s\n", project);
		printf("Line    :%s\n", line);
		printf("Model   :%s\n", model);
		printf("Event   :%s\n", event);
		printf("Attr    :%s\n", attr);
		printf("Size    :%i\n", jsize);
		printf("vperloc :%i\n", jValsPerLoc);
		printf("1stid   :%i\n", jFirstID);
	}

	char* cproject = (char*) project;
	char* cline    = (char*) line;
	char* cmodel   = (char*) model;
	char* cevent   = (char*) event;
	char* cattr    = (char*) attr;

	if (!initializeProject(cproject)) {
		return;
	}

	//...Calculate array size
	int size = (int) jsize;
	int vperloc = (int) jValsPerLoc;
	int model_size = size * vperloc + size; //values plus codes
	if (model_size < 1) {
		fprintf(stderr, "NativeSaveAttribute: maxsvals 0r maxlocs < 1. Need to load run loadAttrNames first!!\n");
		return;
	}

	//...Allocate memory
	if (testing) printf("getting memory\n");
	float* vals = (float *) malloc(sizeof(float) * model_size);  //float version of pointer for storing attribute values
	INTEGER* ivals = (INTEGER *) vals;                    //integer version of pointer is for storing code values

	jfloat *jfloats = (*env)->GetFloatArrayElements(env, jvals, NULL); //c-accessible version of java floats
	if (jfloats == NULL) {
		fprintf(stderr, "Failed to get floats from java!! %s:%s:%s\n", model, event, attr);
		return;
	}

	//...Copy java floats int c array with codes at end
	int i=0, j=0;
	for (i=0; i<size; i++) {
		for (j=0; j<vperloc; j++) {
			vals[i*vperloc + j] = (float) jfloats[i*vperloc + j];
		}
		ivals[i + size*vperloc] = (INTEGER) 90; //set code
	}

	//...Save into FOCUS!
	INTEGER nlocs, nvals, xstart;
	nlocs = (INTEGER) size;
	nvals = (INTEGER) vperloc;
	xstart = (INTEGER) jFirstID;

	if (testing) printf("status   :%i\n", status);printf("calling saveAttr()\n");
	saveAttr(cline, cmodel, cevent, cattr, ivals, nlocs, nvals, xstart);
	processStatus(env, status, jdbioObject);

	if (status == SDB_NORMAL) {
		if (testing) printf("Save attribute success!\n");
	}
	else {
		fprintf(stderr, "Failed to save attribute %s:%s:%s\n", model, event, attr);
	}

	if (testing) printf("freeing memory\n\n\n");
	free(vals);
	(*env)->ReleaseStringUTFChars(env, jproject, cproject);
	(*env)->ReleaseStringUTFChars(env, jline,    cline);
	(*env)->ReleaseStringUTFChars(env, jmodel,   cmodel);
	(*env)->ReleaseStringUTFChars(env, jevent,   cevent);
	(*env)->ReleaseStringUTFChars(env, jatrname, cattr);
}

JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeGetModel (JNIEnv * env, jobject jdbioObject, jobject jmodel)
{
	if (testing) printf("inside getModel\n");

	jstring jproject = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getProjectName);
	jstring jline    = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getLineName);
	jstring jname    = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getName);

	const jbyte *project = (jbyte*) (*env)->GetStringUTFChars(env, jproject, NULL);
	const jbyte *line    = (jbyte*) (*env)->GetStringUTFChars(env, jline, NULL);
	const jbyte *model   = (jbyte*) (*env)->GetStringUTFChars(env, jname, NULL);

	if (testing) {
		printf("Project :%s\n", project);
		printf("Line    :%s\n", line);
		printf("Model   :%s\n", model);
	}

	char* cproject = (char*) project;
	char* cline    = (char*) line;
	char* cmodel   = (char*) model;

	if (!initializeProject(cproject)) {
		return;
	}

	INTEGER xinc, xstart, xend, nlocs;
	xinc = xstart = xend = nlocs = 0;
	status = 9999;
	char interp[12];
	char xunit[12];
	char description[88];
	strcpy(interp, "yo");
	strcpy(xunit, "yo");
	strcpy(description, "yo");

	if (testing) printf("status   :%i\n", status);

	sdb_mdlget_f (cline, cmodel, interp, 10, xunit, 10, &xinc, &xstart, &xend, &nlocs,
			description, 80, &status);

	if (testing) printf("status   :%i\n", status);
	processStatus(env, status, jdbioObject);

	if (status != SDB_NORMAL) {
		fprintf(stderr, "Failed to get Model! project: %s, line: %s, model: %s\n", project, line, model);
	}
	else {
		if (testing) printf("Model get success! project: %s, line: %s, model: %s\n\n", project, line, model);
		(*env)->CallVoidMethod(env, jmodel, MID_FocusDbModel_setInc, xinc);
		(*env)->CallVoidMethod(env, jmodel, MID_FocusDbModel_setFirstID, xstart);
		(*env)->CallVoidMethod(env, jmodel, MID_FocusDbModel_setNLocs, nlocs);
	}
}

JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeDeleteModel (JNIEnv * env, jobject jdbioObject, jobject jmodel)
{
	if (testing) printf("inside deleteModel\n");

	jstring jproject = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getProjectName);
	jstring jline    = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getLineName);
	jstring jname    = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getName);

	const jbyte *project = (jbyte*) (*env)->GetStringUTFChars(env, jproject, NULL);
	const jbyte *line    = (jbyte*) (*env)->GetStringUTFChars(env, jline, NULL);
	const jbyte *model   = (jbyte*) (*env)->GetStringUTFChars(env, jname, NULL);

	if (testing) {
		printf("Project :%s\n", project);
		printf("Line    :%s\n", line);
		printf("Model   :%s\n", model);
	}

	char* cproject = (char*) project;
	char* cline    = (char*) line;
	char* cmodel   = (char*) model;

	if (!initializeProject(cproject)) {
		return;
	}

	status = 9999;
	if (testing) printf("status   :%i\n", status);

	sdb_mdldel_f(cline, cmodel, &status);

	if (testing) printf("status   :%i\n", status);
	processStatus(env, status, jdbioObject);

	if (status != SDB_NORMAL) {
		fprintf(stderr, "Failed to delete Model! project: %s, line: %s, model: %s\n", project, line, model);
	}
	if (status == SDB_NORMAL) {
		if (testing) printf("Model delete success! project: %s, line: %s, model: %s\n\n", project, line, model);
	}
}

JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeCreateModel (JNIEnv * env, jobject jdbioObject, jobject jmodel)
{
	if (testing) printf("inside createModel\n");

	jstring jproject = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getProjectName);
	jstring jline    = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getLineName);
	jstring jname    = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getName);
	jint    jinc     = (*env)->CallIntMethod   (env, jmodel, MID_FocusDbModel_getInc);
	jint    jfirstID = (*env)->CallIntMethod   (env, jmodel, MID_FocusDbModel_getFirstID);
	jint    jend     = (*env)->CallIntMethod   (env, jmodel, MID_FocusDbModel_getEnd);

	const jbyte *project = (jbyte*) (*env)->GetStringUTFChars(env, jproject, NULL);
	const jbyte *line    = (jbyte*) (*env)->GetStringUTFChars(env, jline, NULL);
	const jbyte *model   = (jbyte*) (*env)->GetStringUTFChars(env, jname, NULL);

	if (testing) {
		printf("Project :%s\n", project);
		printf("Line    :%s\n", line);
		printf("Model   :%s\n", model);
		printf("End: %i, FirstID: %i, Inc: %i\n", jend, jfirstID, jinc);
	}

	char* cproject = (char*) project;
	char* cline    = (char*) line;
	char* cmodel   = (char*) model;

	if (!initializeProject(cproject)) {
		return;
	}

	status = 9999;
	char interp[12];
	char xunit[12];
	char description[88];
	INTEGER xinc, xstart, xend;

	strcpy(interp, "LINEAR");
	strcpy(xunit, cmodel);
	strcpy(description, "Made by FocusDbIO using JAVA");
	xinc = (INTEGER) jinc;
	xstart = (INTEGER) jfirstID;
	xend = jend;

	if (testing) printf("status   :%i\n", status);

	sdb_mdlput_f(cline, cmodel, interp,  xunit, xinc, xstart, xend, description, &status);

	if (testing) printf("status   :%i\n", status);
	processStatus(env, status, jdbioObject);

	if (status != SDB_NORMAL) {
		fprintf(stderr, "Failed to create Model! project: %s, line: %s, model: %s\n", project, line, model);
	}
	if (status == SDB_NORMAL) {
		if (testing) printf("Model create success! project: %s, line: %s, model: %s\n\n", project, line, model);
	}
}

JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeExtendModel (JNIEnv * env, jobject dbioObject, jobject jmodel, jint jiflag)
{
	if (testing) printf("inside extendModel\n");

	jstring jproject = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getProjectName);
	jstring jline    = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getLineName);
	jstring jname    = (*env)->CallObjectMethod(env, jmodel, MID_FocusDbModel_getName);
	jint    jinc     = (*env)->CallIntMethod   (env, jmodel, MID_FocusDbModel_getInc);
	jint    jfirstID = (*env)->CallIntMethod   (env, jmodel, MID_FocusDbModel_getFirstID);
	jint    jend     = (*env)->CallIntMethod   (env, jmodel, MID_FocusDbModel_getEnd);

	const jbyte *project = (jbyte*) (*env)->GetStringUTFChars(env, jproject, NULL);
	const jbyte *line    = (jbyte*) (*env)->GetStringUTFChars(env, jline, NULL);
	const jbyte *model   = (jbyte*) (*env)->GetStringUTFChars(env, jname, NULL);

	if (testing) {
		printf("Project :%s\n", project);
		printf("Line    :%s\n", line);
		printf("Model   :%s\n", model);
		printf("End: %i, FirstID: %i, Inc: %i\n", jend, jfirstID, jinc);
	}

	char* cproject = (char*) project;
	char* cline    = (char*) line;
	char* cmodel   = (char*) model;

	if (!initializeProject(cproject)) {
		return;
	}

	status = 9999;
	INTEGER iflag = jiflag;
	INTEGER xstart = (INTEGER) jfirstID;
	INTEGER xend = jend;

	if (testing) printf("status   :%i\n", status);

	sdb_mdlextend_f(cline, cmodel, iflag, xstart, xend, &status);

	if (testing) printf("status   :%i\n", status);
	processStatus(env, status, dbioObject);

	if (status != SDB_NORMAL) {
		fprintf(stderr, "Failed to extend Model! project: %s, line: %s, model: %s\n", project, line, model);
	}
	if (status == SDB_NORMAL) {
		if (testing) printf("Model extend success! project: %s, line: %s, model: %s\n\n", project, line, model);
	}
}

JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeChrPut(JNIEnv * env, jobject jdbioObject, jstring jtext, jstring jkey, jstring jlineName)
{
	if (testing) printf("inside chrPut\n");

	const jbyte *text     = (jbyte*) (*env)->GetStringUTFChars(env, jtext, NULL);
	const jbyte *key      = (jbyte*) (*env)->GetStringUTFChars(env, jkey, NULL);
	const jbyte *lineName = (jbyte*) (*env)->GetStringUTFChars(env, jlineName, NULL);

	if (testing) {
		printf("line    :%s\n", lineName);
		printf("key     :%s\n", key);
		printf("text    :%s\n", text);
	}

	char* cline    = (char*) lineName;
	char* ckey     = (char*) key;
	char* ctext    = (char*) text;

	status = 9999;
	if (testing) printf("status   :%i\n", status);

	sdb_chrput_f(cline, ckey, ctext, &status);

	if (testing) printf("status   :%i\n", status);
	processStatus(env, status, jdbioObject);

	if (status != SDB_NORMAL) {
		fprintf(stderr, "Failed store text! line: %s, key: %s\n", cline, key);
	}
	if (status == SDB_NORMAL) {
		if (testing) printf("Store text success! line: %s, key: %s\n\n", cline, key);
	}
}
JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeAscPut(JNIEnv * env, jobject jdbioObject, jstring jtext, jstring jkey, jstring jlineName)
{
	if (testing) printf("inside chrPut\n");

	const jbyte *text     = (jbyte*) (*env)->GetStringUTFChars(env, jtext, NULL);
	const jbyte *key      = (jbyte*) (*env)->GetStringUTFChars(env, jkey, NULL);
	const jbyte *lineName = (jbyte*) (*env)->GetStringUTFChars(env, jlineName, NULL);
	const jsize  length   = (jsize)  (*env)->GetStringLength  (env, jtext);

	if (testing) {
		printf("line    :%s\n", lineName);
		printf("key     :%s\n", key);
		printf("text    :%s\n", text);
		printf("length  :%i\n", length);
	}

	char* cline    = (char*) lineName;
	char* ckey     = (char*) key;
	char* ctext    = (char*) text;
	int   clength  = (int) length;
	char  record[127] = "";

	status = 9999;
	if (testing) printf("status   :%i\n", status);

	INTEGER iunit;
	sdb_ascnew_f(cline, "TEXT", ckey, "written by java", &iunit, &status); //open file
	if (status == SDB_NORMAL) {
		int i = 0;
		//		for (i = 0; i<length; i+=127) {
		while (i < length) { //write text to file, one line at a time (Focus automatically puts return characters at the end of each record
			int nextR = strchr(ctext+i, '\n') - (ctext+i) ; //find distance to next return character
			int nextN = strchr(ctext+i, '\0') - (ctext+i) ; //find distance to next null character

			int next = 0;
			if (nextR < nextN) next = nextR;
			else next = nextN;

			if (next > 127) next = 127;
//			if (next == 0) next = 1;
			if (next < 0) next = 0;
			if (testing) printf("i is: %i  next is   :%i\n\n", i, next);

			strncpy(record, ctext+i, next);
			if (next < 127) record[next] = '\0';

			sdb_ascput_f(iunit, record, &status); //write to file (function only writes 127 chars at a time)

			i += next + 1; //skip the return character
//			i += next;
		}
		sdb_asccls_f(iunit, "KEEP", &status); //close file
	}

	if (testing) printf("status   :%i\n", status);
	processStatus(env, status, jdbioObject);

	if (status != SDB_NORMAL) {
		fprintf(stderr, "Failed store text! line: %s, key: %s\n", cline, key);
	}
	if (status == SDB_NORMAL) {
		if (testing) printf("Store text success! line: %s, key: %s\n\n", cline, key);
	}
}

JNIEXPORT jstring JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeChrGet (JNIEnv * env, jobject jdbioObject, jstring jlineName, jstring jkey)
{
	if (testing) printf("inside chrGet\n");

	const jbyte *key      = (jbyte*) (*env)->GetStringUTFChars(env, jkey, NULL);
	const jbyte *lineName = (jbyte*) (*env)->GetStringUTFChars(env, jlineName, NULL);

	if (testing) {
		printf("line    :%s\n", lineName);
		printf("key     :%s\n", key);
	}

	char* cline      = (char*) lineName;
	char* ckey       = (char*) key;
	char  ctext[132] = "";

	status = 9999;
	if (testing) printf("status   :%i\n", status);

	sdb_chrget_f(cline, ckey, ctext, 132, &status);

	if (testing) printf("status   :%i\n", status);
	processStatus(env, status, jdbioObject);

	if (status != SDB_NORMAL) {
		fprintf(stderr, "Failed read text! line: %s, key: %s\n", cline, key);
	}
	if (status == SDB_NORMAL) {
		if (testing) printf("Read text success! line: %s, key: %s\n\n", cline, key);
	}

	jstring jtext = (*env)->NewStringUTF(env, ctext);
	return jtext;
}

JNIEXPORT jstring JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeAscGet (JNIEnv * env, jobject jdbioObject, jstring jlineName, jstring jkey)
{
	if (testing) printf("inside chrGet\n");

	const jbyte *key      = (jbyte*) (*env)->GetStringUTFChars(env, jkey, NULL);
	const jbyte *lineName = (jbyte*) (*env)->GetStringUTFChars(env, jlineName, NULL);

	if (testing) {
		printf("line    :%s\n", lineName);
		printf("key     :%s\n", key);
	}

	char* cline      = (char*) lineName;
	char* ckey       = (char*) key;
	char  ctext[10000] = ""; //total text from file
	char  filename[127] = ""; //actual filename on dis
	char  record[127]  = ""; //line of text from file
	int   recordSize = 127;
	int   mode       = 0;     // 0 = search database for file, 1 = use given file name;
	INTEGER iunit;           //lun of file to read from

	status = 9999;
	if (testing) printf("status   :%i\n", status);

//	sdb_chrget_f(cline, ckey, ctext, 132, &status);
	sdb_ascold_f(cline, "TEXT", ckey, mode, filename, 127, SDB_READ, &iunit, &status);
	if (status != SDB_NORMAL) {
		if (testing) printf("status   :%i\n", status);
		processStatus(env, status, jdbioObject);
		return;
	}

	int index = 0;
	while (status == SDB_NORMAL) {
//		printf("reading record\n");
		sdb_ascget_f(iunit, record, 127, &status);
		int i=0;
		for (i=0; i<recordSize; i++) {
			int cc = record[i];
			if (cc == '\0') {
				//printf("found terminator\n");
				break;
			}
			ctext[index++] = record[i];
		}
		if (ctext[index] != '\n') ctext[index++] = '\n';
//		printf("copying text \"%s\"\n", record);
//		printf("index is %i\n", index);
//		printf("text is `%s`\n\n", ctext);

		if ((index + recordSize) >= 10000) {
			fprintf(stderr, "Reached max text limit! line: %s, key: %s\n", cline, key);
			break;
		}
	}

//	printf("text is %s\n", ctext);

	sdb_asccls_f(iunit, "KEEP", &status);

	if (testing) printf("status   :%i\n", status);
	processStatus(env, status, jdbioObject);

	if (status != SDB_NORMAL) {
		fprintf(stderr, "Failed read text! line: %s, key: %s\n", cline, key);
	}
	if (status == SDB_NORMAL) {
		if (testing) printf("Read text success! line: %s, key: %s\n\n", cline, key);
	}

	jstring jtext = (*env)->NewStringUTF(env, ctext);
	return jtext;
}

JNIEXPORT jintArray JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeBinGet  (JNIEnv * env, jobject jdbioObject, jstring jlineName, jstring jklass, jstring jkey)
{
	if (testing) printf("inside binGet\n");

	const jbyte *key      = (jbyte*) (*env)->GetStringUTFChars(env, jkey, NULL);
	const jbyte *lineName = (jbyte*) (*env)->GetStringUTFChars(env, jlineName, NULL);
	const jbyte *klass    = (jbyte*) (*env)->GetStringUTFChars(env, jklass, NULL);

	if (testing) {
		printf("line    :%s\n", lineName);
		printf("key     :%s\n", key);
		printf("klass   :%s\n", klass);
	}

	char* cline      = (char*) lineName;
	char* ckey       = (char*) key;
	char* cklass     = (char*) klass;
	INTEGER leni = 132;
	INTEGER* buff;
	INTEGER leno = 0;
	INTEGER ix = 0;

	status = 9999;
	if (testing) printf("status   :%i\n", status);

	if (testing) printf("getting memory\n");
	buff = (INTEGER *) malloc(sizeof(INTEGER) * leni);

	sdb_binget_f(cline, cklass, ckey, buff, leni-1, "USE", &ix, &leno, &status);

	if (testing) printf("status   :%i\n", status);
	processStatus(env, status, jdbioObject);

	if (status != SDB_NORMAL) {
		fprintf(stderr, "Failed read binary file! line: %s, class: %s, key: %s\n", cline, cklass, ckey);
	}
	if (status == SDB_NORMAL) {
		if (testing) printf("Read binary success! line: %s, class: %s, key: %s\n", cline, cklass, ckey);
	}

	jintArray jvals = (*env)->NewIntArray(env, (int)leno);
	(*env)->SetIntArrayRegion(env, jvals, 0, (int)leno, buff);

	free(buff);
	return jvals;
}

JNIEXPORT void JNICALL
Java_com_tricongeophysics_FocusDbIO_nativeBinPut (JNIEnv * env, jobject jdbioObject, jstring jlineName, jstring jklass, jstring jkey, jintArray jbuffer)
{
	if (testing) printf("inside binPut\n");

	const jbyte *key      = (jbyte*) (*env)->GetStringUTFChars(env, jkey, NULL);
	const jbyte *lineName = (jbyte*) (*env)->GetStringUTFChars(env, jlineName, NULL);
	const jbyte *klass    = (jbyte*) (*env)->GetStringUTFChars(env, jklass, NULL);
	const jint  *buffer   = (jint*)  (*env)->GetIntArrayElements(env, jbuffer, NULL);
	const jsize  size     = (jsize) (*env)->GetArrayLength(env, jbuffer);

	if (testing) {
		printf("line    :%s\n", lineName);
		printf("key     :%s\n", key);
		printf("klass   :%s\n", klass);
		printf("size    :%i\n", size);
	}

	char* cline      = (char*) lineName;
	char* ckey       = (char*) key;
	char* cklass     = (char*) klass;
	int*  cbuffer    = (int*)  buffer;
	int   csize      = (int)   size;

	status = 9999;
	if (testing) printf("status   :%i\n", status);

	sdb_binput_f(cline, cklass, ckey, cbuffer, csize, &status);

	if (testing) printf("status   :%i\n", status);
	processStatus(env, status, jdbioObject);

	if (status != SDB_NORMAL) {
		fprintf(stderr, "Failed to write binary file! line: %s, class: %s, key: %s\n", cline, cklass, ckey);
	}
	if (status == SDB_NORMAL) {
		if (testing) printf("Write binary success! line: %s, class: %s, key: %s\n", cline, cklass, ckey);
	}
}


int main(int argc, char ** argv) {
	printf("hi");
	return 0;
}
