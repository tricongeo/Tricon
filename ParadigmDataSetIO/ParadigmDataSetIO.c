#include <jni.h>
#include <stdio.h>
#include "com_tricongeophysics_ParadigmDataSetIO.h"
#include <tri_utilities.h>
#include <tri_memory.h>
#include <tri_array.h>
#include <tri_error.h>
#include <tricon_dsin.h>
#include <tricon_dbfile.h>

//keep tricon dataset struct as global so that I always know what the next trace is (keeps track of current trace ID)
triDataset C_TriconDataSet;                   //struct containing information about current dataset
triDataSelector C_TriconDataSelector;         //struct describing how to find dataset (Focus project, line, filename, etc.)


//method and field ID's for java methods and fields are stored as global variables
//this way, the JVM only needs to look them up once per instance of ParadigmDataSetIO object
jmethodID MID_ParadigmDataSetIO_getDataSet ;
jmethodID MID_DataSet_setName       ;
jmethodID MID_DataSet_getName       ;
jmethodID MID_DataSet_setPath       ;
jmethodID MID_DataSet_getPath       ;
jmethodID MID_DataSet_setSampleRate ;
jmethodID MID_DataSet_getSampleRate ;
jmethodID MID_DataSet_setSamplesPerTrace;
jmethodID MID_DataSet_getSamplesPerTrace;
jmethodID MID_DataSet_setTimeZero;
jmethodID MID_DataSet_getTimeZero;
jmethodID MID_DataSet_setDataType;
jmethodID MID_DataSet_getDataType;
jmethodID MID_DataSet_setPrimaryKey;
jmethodID MID_DataSet_getPrimaryKey;
jmethodID MID_DataSet_setPkeyIndex ;
jmethodID MID_DataSet_getPkeyIndex ;
jmethodID MID_DataSet_setSecondaryKey;
jmethodID MID_DataSet_getSecondaryKey;
jmethodID MID_DataSet_setMaxntr     ;
jmethodID MID_DataSet_getMaxntr     ;
jmethodID MID_DataSet_setNumTraces  ;
jmethodID MID_DataSet_getNumTraces  ;
jmethodID MID_DataSet_setTrace      ;
jmethodID MID_DataSet_getTrace      ;
jmethodID MID_DataSet_setCdpGrid    ;
jmethodID MID_DataSet_getCdpGrid    ;
jmethodID MID_DataSet_setLineType   ;
jmethodID MID_DataSet_getLineType   ;
jmethodID MID_DataSet_getProjectName;
jmethodID MID_DataSet_getLineName   ;
jmethodID MID_DataSet_getPgSurveyRoot;
jmethodID MID_DataSet_getPgSurveyDir;
jmethodID MID_CDPGrid_setNumInlines     ;
jmethodID MID_CDPGrid_getNumInlines     ;
jmethodID MID_CDPGrid_setNumXlines      ;
jmethodID MID_CDPGrid_getNumXlines      ;
jmethodID MID_CDPGrid_setInLineInterval ; 
jmethodID MID_CDPGrid_getInLineInterval ; 
jmethodID MID_CDPGrid_setXLineInterval  ; 
jmethodID MID_CDPGrid_getXLineInterval  ; 
jmethodID MID_CDPGrid_setAngle          ; 
jmethodID MID_CDPGrid_getAngle          ; 
jmethodID MID_CDPGrid_setFirstInline    ; 
jmethodID MID_CDPGrid_getFirstInline    ; 
jmethodID MID_CDPGrid_setFirstXline     ; 
jmethodID MID_CDPGrid_getFirstXline     ; 
jmethodID MID_SeismicTrace_setHeaderList ; 
jmethodID MID_SeismicTrace_getHeaderList ; 
jmethodID MID_SeismicTrace_setHeaders    ; 
jmethodID MID_SeismicTrace_getHeaders    ; 
jmethodID MID_SeismicTrace_setData       ; 
jmethodID MID_SeismicTrace_getData       ; 

#define testing 0

void trace_data_to_java( JNIEnv * env, jobject seismic_trace, float * data_array);
void trace_headers_to_java( JNIEnv * env, jobject seismic_trace, int * header_array);

//This call loads all Java methods into C globals so that they can be called without the expense of looking them
// up later in the Java Virtual Machine.
JNIEXPORT void JNICALL Java_com_tricongeophysics_ParadigmDataSetIO_initIDs  (JNIEnv *env, jobject dsioObject)
{
  // printf("\ninit ids\n");
  jclass dsioClass = (*env)->GetObjectClass(env, dsioObject);
  if (dsioClass == NULL) {
    return; //exception thrown
  } 

  //load Data Set getter/setter methods
  // printf("\ninit Data Set\n");
  MID_ParadigmDataSetIO_getDataSet = (*env)->GetMethodID(env, dsioClass, "getDataSet", "()Lcom/tricongeophysics/DataSet;");
  if (MID_ParadigmDataSetIO_getDataSet == NULL) {
    return; //exception thrown
  }
  jobject dataSetObject = (*env)->CallObjectMethod(env, dsioObject, MID_ParadigmDataSetIO_getDataSet);
  if (dataSetObject == NULL) {
    return; //exception thrown
  }
  jclass dataSetClass = (*env)->GetObjectClass(env, dataSetObject);
  if (dataSetClass == NULL) {
    return; //exception thrown
  }
  MID_DataSet_setName        = (*env)->GetMethodID(env, dataSetClass, "setName"       , "(Ljava/lang/String;)V");
  MID_DataSet_getName        = (*env)->GetMethodID(env, dataSetClass, "getName"       , "()Ljava/lang/String;");
  MID_DataSet_setPath        = (*env)->GetMethodID(env, dataSetClass, "setPath"       , "(Ljava/lang/String;)V");
  MID_DataSet_getPath        = (*env)->GetMethodID(env, dataSetClass, "getPath"       , "()Ljava/lang/String;");
  MID_DataSet_setSampleRate  = (*env)->GetMethodID(env, dataSetClass, "setSampleRate" , "(I)V");
  MID_DataSet_getSampleRate  = (*env)->GetMethodID(env, dataSetClass, "getSampleRate" , "()I");
  MID_DataSet_setSamplesPerTrace = (*env)->GetMethodID(env, dataSetClass, "setSamplesPerTrace", "(I)V");
  MID_DataSet_getSamplesPerTrace = (*env)->GetMethodID(env, dataSetClass, "getSamplesPerTrace", "()I");
  MID_DataSet_setTimeZero    = (*env)->GetMethodID(env, dataSetClass, "setTimeZero"   , "(I)V");
  MID_DataSet_getTimeZero    = (*env)->GetMethodID(env, dataSetClass, "getTimeZero"   , "()I");
  MID_DataSet_setDataType    = (*env)->GetMethodID(env, dataSetClass, "setDataType"   , "(Ljava/lang/String;)V");
  MID_DataSet_getDataType    = (*env)->GetMethodID(env, dataSetClass, "getDataType"   , "()Ljava/lang/String;");
  MID_DataSet_setPrimaryKey  = (*env)->GetMethodID(env, dataSetClass, "setPrimaryKey" , "(Ljava/lang/String;)V");
  MID_DataSet_getPrimaryKey  = (*env)->GetMethodID(env, dataSetClass, "getPrimaryKey" , "()Lcom/tricongeophysics/DataSet$SortOrder;");
  MID_DataSet_setPkeyIndex   = (*env)->GetMethodID(env, dataSetClass, "setPkeyIndex"  , "(I)V");
  MID_DataSet_getPkeyIndex   = (*env)->GetMethodID(env, dataSetClass, "getPkeyIndex"  , "()I");
  MID_DataSet_setSecondaryKey = (*env)->GetMethodID(env, dataSetClass, "setSecondaryKey", "(Lcom/tricongeophysics/DataSet$SortOrder;)V");
  MID_DataSet_getSecondaryKey = (*env)->GetMethodID(env, dataSetClass, "getSecondaryKey", "()Lcom/tricongeophysics/DataSet$SortOrder;");
  MID_DataSet_setMaxntr      = (*env)->GetMethodID(env, dataSetClass, "setMaxntr"     , "(I)V");
  MID_DataSet_getMaxntr      = (*env)->GetMethodID(env, dataSetClass, "getMaxntr"     , "()I");
  MID_DataSet_setNumTraces   = (*env)->GetMethodID(env, dataSetClass, "setNumTraces"  , "(I)V");
  MID_DataSet_getNumTraces   = (*env)->GetMethodID(env, dataSetClass, "getNumTraces"  , "()I");
  MID_DataSet_setTrace       = (*env)->GetMethodID(env, dataSetClass, "setTrace"      , "(Lcom/tricongeophysics/SeismicTrace;)V");
  MID_DataSet_getTrace       = (*env)->GetMethodID(env, dataSetClass, "getTrace"      , "()Lcom/tricongeophysics/SeismicTrace;");
  MID_DataSet_setCdpGrid     = (*env)->GetMethodID(env, dataSetClass, "setCdpGrid"    , "(Lcom/tricongeophysics/CDPGrid;)V");
  MID_DataSet_getCdpGrid     = (*env)->GetMethodID(env, dataSetClass, "getCdpGrid"    , "()Lcom/tricongeophysics/CDPGrid;");
  MID_DataSet_setLineType    = (*env)->GetMethodID(env, dataSetClass, "setLineType"   , "(Lcom/tricongeophysics/DataSet$LineType;)V");
  MID_DataSet_getLineType    = (*env)->GetMethodID(env, dataSetClass, "getLineType"   , "()Lcom/tricongeophysics/DataSet$LineType;");
  MID_DataSet_getProjectName = (*env)->GetMethodID(env, dataSetClass, "getProjectName", "()Ljava/lang/String;");
  MID_DataSet_getLineName    = (*env)->GetMethodID(env, dataSetClass, "getLineName"   , "()Ljava/lang/String;");
  MID_DataSet_getPgSurveyRoot = (*env)->GetMethodID(env, dataSetClass, "getPgSurveyRoot", "()Ljava/lang/String;");
  MID_DataSet_getPgSurveyDir = (*env)->GetMethodID(env, dataSetClass, "getPgSurveyDir", "()Ljava/lang/String;");
  
  //load CDPGrid getter/setter methods
  /*
  printf("\ninit cdp grid\n");
  printf("%10s%10i\n","mid setSampleRate     ", MID_DataSet_setSampleRate       );
  printf("%10s%10i\n","mid getSampleRate     ", MID_DataSet_getSampleRate       );
  printf("%10s%10i\n","mid setSamplesPerTrace", MID_DataSet_setSamplesPerTrace  );
  printf("%10s%10i\n","mid getSamplesPerTrace", MID_DataSet_getSamplesPerTrace  );
  printf("%10s%10i\n","mid setTimeZero       ", MID_DataSet_setTimeZero         );
  printf("%10s%10i\n","mid getTimeZero       ", MID_DataSet_getTimeZero         );
  printf("%10s%10i\n","mid setDataType       ", MID_DataSet_setDataType         );
  printf("%10s%10i\n","mid getDataType       ", MID_DataSet_getDataType         );
  printf("%10s%10i\n","mid setPrimaryKey     ", MID_DataSet_setPrimaryKey       );
  printf("%10s%10i\n","mid getPrimaryKey     ", MID_DataSet_getPrimaryKey       );
  printf("%10s%10i\n","mid setSecondaryKey   ", MID_DataSet_setSecondaryKey     );
  printf("%10s%10i\n","mid getSecondaryKey   ", MID_DataSet_getSecondaryKey     );
  printf("%10s%10i\n","mid setMaxntr         ", MID_DataSet_setMaxntr           );
  printf("%10s%10i\n","mid getMaxntr         ", MID_DataSet_getMaxntr           );
  printf("%10s%10i\n","mid setNumTraces      ", MID_DataSet_setNumTraces        );
  printf("%10s%10i\n","mid getNumTraces      ", MID_DataSet_getNumTraces        );
  printf("%10s%10i\n","mid setTrace          ", MID_DataSet_setTrace            );
  printf("%10s%10i\n","mid getTrace          ", MID_DataSet_getTrace            );
  printf("%10s%10i\n","mid setCdpGrid        ", MID_DataSet_setCdpGrid          );
  printf("%10s%10i\n","mid getCdpGrid        ", MID_DataSet_getCdpGrid          );
  printf("%10s%10i\n","mid setLineType       ", MID_DataSet_setLineType         );
  printf("%10s%10i\n","mid getLineType       ", MID_DataSet_getLineType         );
  */
                                                                                 
  jobject cdpGridObject = (*env)->CallObjectMethod(env, dataSetObject, MID_DataSet_getCdpGrid);
  //printf("\npast grid object call\n");
  if (cdpGridObject == NULL) {
    //printf("\ngrid object is null\n");
    return; //exception thrown
  }
  //printf("\ngot grid object\n");
  jclass cdpGridClass = (*env)->GetObjectClass(env, cdpGridObject);
  if (cdpGridClass == NULL) {
    return; //exception thrown
  }
  //printf("\ngot grid class\n");
  MID_CDPGrid_setNumInlines     = (*env)->GetMethodID(env, cdpGridClass, "setNumInlines"     , "(I)V");
  MID_CDPGrid_getNumInlines     = (*env)->GetMethodID(env, cdpGridClass, "getNumInlines"     , "()I");
  MID_CDPGrid_setNumXlines      = (*env)->GetMethodID(env, cdpGridClass, "setNumXlines"      , "(I)V");
  MID_CDPGrid_getNumXlines      = (*env)->GetMethodID(env, cdpGridClass, "getNumXlines"      , "()I");
  MID_CDPGrid_setInLineInterval = (*env)->GetMethodID(env, cdpGridClass, "setInLineInterval" , "(D)V");
  MID_CDPGrid_getInLineInterval = (*env)->GetMethodID(env, cdpGridClass, "getInLineInterval" , "()D");
  MID_CDPGrid_setXLineInterval  = (*env)->GetMethodID(env, cdpGridClass, "setXLineInterval"  , "(D)V");
  MID_CDPGrid_getXLineInterval  = (*env)->GetMethodID(env, cdpGridClass, "getXLineInterval"  , "()D");
  MID_CDPGrid_setAngle          = (*env)->GetMethodID(env, cdpGridClass, "setAngle"          , "(D)V");
  MID_CDPGrid_getAngle          = (*env)->GetMethodID(env, cdpGridClass, "getAngle"          , "()D");
  MID_CDPGrid_setFirstInline    = (*env)->GetMethodID(env, cdpGridClass, "setFirstInline"    , "(I)V");
  MID_CDPGrid_getFirstInline    = (*env)->GetMethodID(env, cdpGridClass, "getFirstInline"    , "()I");
  MID_CDPGrid_setFirstXline     = (*env)->GetMethodID(env, cdpGridClass, "setFirstXline"     , "(I)V");
  MID_CDPGrid_getFirstXline     = (*env)->GetMethodID(env, cdpGridClass, "getFirstXline"     , "()I");

  //load Seismic Trace getter/setter methods
  //printf("\ninit seismic trace\n");
  jobject seismicTraceObject = (*env)->CallObjectMethod(env, dataSetObject, MID_DataSet_getTrace);
  if (seismicTraceObject == NULL) {
    return; //exception thrown
  }
  jclass seismicTraceClass = (*env)->GetObjectClass(env, seismicTraceObject);
  if (seismicTraceClass == NULL) {
    return; //exception thrown
  }
  MID_SeismicTrace_setHeaderList = (*env)->GetMethodID(env, seismicTraceClass, "setHeaderList", "([C)V");
  MID_SeismicTrace_getHeaderList = (*env)->GetMethodID(env, seismicTraceClass, "getHeaderList", "()[Ljava/lang/String;");
  MID_SeismicTrace_setHeaders    = (*env)->GetMethodID(env, seismicTraceClass, "setHeaders"   , "([F)V");
  MID_SeismicTrace_getHeaders    = (*env)->GetMethodID(env, seismicTraceClass, "getHeaders"   , "()[D");
  MID_SeismicTrace_setData       = (*env)->GetMethodID(env, seismicTraceClass, "setData"      , "([F)V");
  MID_SeismicTrace_getData       = (*env)->GetMethodID(env, seismicTraceClass, "getData"      , "()[F");

}


// JNI C code to load dataset pointers into C memory and return essential information about the dataset to Java
// This should be called before trying to access any actual seismic traces
JNIEXPORT jobject JNICALL
Java_com_tricongeophysics_ParadigmDataSetIO_nativeInitializeDataSet (JNIEnv *env,
								     jobject dsioObject,
								     jobject jdataSet)
{
  //printf(" in C\n");
  //...Get project info
  //printf("getting project\n");
  //printf("method id", MID_DataSelector_getProjectName);
  jstring jproject  = (*env)->CallObjectMethod(env, jdataSet, MID_DataSet_getProjectName);
  //printf("getting line\n");
  jstring jline     = (*env)->CallObjectMethod(env, jdataSet, MID_DataSet_getLineName);
  //printf("getting file\n");
  jstring jfilename = (*env)->CallObjectMethod(env, jdataSet, MID_DataSet_getName);
  //printf("got jstrings\n");
  jstring jpgsurveyroot = (*env)->CallObjectMethod(env, jdataSet, MID_DataSet_getPgSurveyRoot);
  //printf("got jstrings\n");
  jstring jpgsurveydir = (*env)->CallObjectMethod(env, jdataSet, MID_DataSet_getPgSurveyDir);
  //printf("got jstrings\n");

  //...convert java strings to C strings
  const jbyte *project;
  const jbyte *line;
  const jbyte *dataset;
  const jbyte *pg_survey_root;
  const jbyte *pg_survey_dir;

  project = (jbyte*)(*env)->GetStringUTFChars(env, jproject , NULL); //project names are only 8 characters
  line    = (jbyte*)(*env)->GetStringUTFChars(env, jline    , NULL);    //line names are only 8 characters
  dataset = (jbyte*)(*env)->GetStringUTFChars(env, jfilename, NULL);
  pg_survey_root = (jbyte*)(*env)->GetStringUTFChars(env, jpgsurveyroot, NULL);
  pg_survey_dir  = (jbyte*)(*env)->GetStringUTFChars(env, jpgsurveydir, NULL);

  printf( "project:       %s\n",project);
  printf( "line:          %s\n",line);
  printf( "dataset:       %s\n",dataset);
  printf( "pg_survey_root:%s\n",pg_survey_root);
  printf( "pg_survey_dir: %s\n",pg_survey_dir);

  C_TriconDataSelector = triselect_initialize_env_override( (char*)project, (char*)line, (char*)pg_survey_root, (char*)pg_survey_dir );
  C_TriconDataSet = tridata_initialize( (char*)dataset, C_TriconDataSelector, NULL );
  tridata_load_first_ensemble( C_TriconDataSet );  //needed to get total_traces and MaxNtr?
  

  printf("\nTricon Dataset...\n");
  printf( "DataSet:    %s\n", C_TriconDataSet->name );
  printf( "Path:       %s\n", C_TriconDataSet->path );
  printf( "Thdrlen:    %d\n", C_TriconDataSet->mg->thdrlen );
  printf( "NumHead:    %d\n", C_TriconDataSet->mg->num_headers );
  printf( "DataType:   %s\n", C_TriconDataSet->data_type );
  printf( "SampRate:   %d\n", C_TriconDataSet->sample_rate );
  printf( "Samples:    %d\n", C_TriconDataSet->mg->samples );
  printf( "MaxNtr:     %d\n", C_TriconDataSet->maxntr );
  printf( "tottraces:  %d\n", C_TriconDataSet->total_traces );
  printf( "PrimaryKey: %s\n..\n", C_TriconDataSet->primary_key );

  

  //Load essential dataset info into java DataSet object
  (*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setName           , (*env)->NewStringUTF(env, C_TriconDataSet->name));
  (*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setPath           , (*env)->NewStringUTF(env, C_TriconDataSet->path));
  (*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setDataType       , (*env)->NewStringUTF(env, C_TriconDataSet->data_type));
  (*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setMaxntr         , C_TriconDataSet->maxntr);
  (*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setNumTraces      , C_TriconDataSet->total_traces);
  (*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setPrimaryKey     , (*env)->NewStringUTF(env, C_TriconDataSet->primary_key));
  (*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setSampleRate     , C_TriconDataSet->sample_rate );
  (*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setSamplesPerTrace, C_TriconDataSet->mg->samples );
  (*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setPkeyIndex      , C_TriconDataSet->mg->pkey_index );

  //(*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setLineType       , NewString(env, //not yet implemented
  //(*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setCdpGrid        ,  //not yet implemented
  //(*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setSecondaryKey   , //not yet implemented
  //(*env)->CallVoidMethod(env, jdataSet, MID_DataSet_setTimeZero       , //not yet implemented
                  
  return jdataSet;  
}                   
                


// JNI call to load first trace of a particular gather in dataset (using GatherID)
// uses pre-loaded C_TriconDataSet and C_TriconDataSelector structs
// Java_com_tricongeophysics_ParadigmDataSetIO_nativeInitializeDataSet should be run first
JNIEXPORT jobject JNICALL Java_com_tricongeophysics_ParadigmDataSetIO_nativeLoadGatherIDFirstTrace(JNIEnv *env, 
												 jobject dsioObject, 
												 jint gatherID,
												 jobject jseismicTrace)
{
  //get first data trace of gather in C
  float *data;
  int   *iheaders; //header version that tridata_first_trace wants
  Bool loaded = tridata_load_ensemble( C_TriconDataSet, gatherID );
  if ( loaded ) {
    tridata_first_trace( C_TriconDataSet, &data, &iheaders );

    //load trace data into Java
    trace_data_to_java(env, jseismicTrace, data);
    
    //load trace headers into Java
    trace_headers_to_java(env, jseismicTrace, iheaders);

    //printf("jseismicTrace is:%08X\n",jseismicTrace);

    if (testing) printf("done loading first trace of gather %i\n...\n", gatherID);

    return jseismicTrace;
  }
  else {
    printf("failed to load first trace of gather %i\n...\n", gatherID);
    return NULL;  //dataset failed to load for some reason
  }
}


                  
// JNI call to load first trace of the first gather in dataset
// uses pre-loaded C_TriconDataSet and C_TriconDataSelector structs
// Java_com_tricongeophysics_ParadigmDataSetIO_nativeInitializeDataSet should be run first
//
// header name list is sent to Java as an array of 8 character strings
// header value array is first converted to floats, then sent to Java
JNIEXPORT jobject JNICALL Java_com_tricongeophysics_ParadigmDataSetIO_nativeLoadDataSetFirstTrace(JNIEnv *env,
												  jobject dsioObject,
												  jobject jseismicTrace)
{
  //get first data trace in C
  float *data;
  int   *iheaders; //header version that tridata_first_trace wants

  if (testing) printf("begin loading first dataset trace\n...\n");

  Bool loaded = tridata_load_first_ensemble( C_TriconDataSet );
  if ( loaded ) {

    if (testing) printf("loaded first ensemble\n...\n");

    tridata_first_trace( C_TriconDataSet, &data, &iheaders );

    if (testing) printf("found first trace of dataset\n...\n");
    
    //load trace headers into Java
    trace_headers_to_java(env, jseismicTrace, iheaders);

    if (testing) printf("sent trace headers to java\n...\n");

    //load trace data into Java
    trace_data_to_java(env, jseismicTrace, data);
  
    if (testing) printf("sent trace data to java\n...\n");

    return jseismicTrace;
  }
  else {
    printf("failed to load first dataset trace\n...\n");
    return NULL;  //dataset failed to load for some reason
  }
 }



// JNI call to load first trace of the next gather in dataset
// uses pre-loaded C_TriconDataSet and C_TriconDataSelector structs
// Java_com_tricongeophysics_ParadigmDataSetIO_nativeInitializeDataSet should be run first
//
// returns NULL if next gather not loaded (already at end of dataset?)
//
// header name list is sent to Java as an array of 8 character strings
// header value array is first converted to floats, then sent to Java
JNIEXPORT jobject JNICALL Java_com_tricongeophysics_ParadigmDataSetIO_nativeLoadNextGatherFirstTrace(JNIEnv *env,
												  jobject dsioObject,
												  jobject jseismicTrace)
{
  //get first data trace in C
  float *data;
  int   *iheaders; //header version that tridata_first_trace wants

  Bool loaded = tridata_load_next_ensemble( C_TriconDataSet );
  if ( loaded ) {
    if (tridata_first_trace( C_TriconDataSet, &data, &iheaders )) {
      
      //load trace data into Java
      trace_data_to_java(env, jseismicTrace, data);
      
      //load trace headers into Java
      trace_headers_to_java(env, jseismicTrace, iheaders);

      //printf("jseismicTrace is:%08X\n",jseismicTrace);
      
      if (testing) printf("done loading next gather first trace\n...\n");

      //if (iheaders != NULL) printf("tot %i gather# %i \n",C_TriconDataSet->total_traces, iheaders[C_TriconDataSet->mg->pkey_index]);
      
      return jseismicTrace;
    }
    else {
      printf("failed loading next gather first trace\n...\n");
      return NULL;
    }
  }
  else {
    printf("failed loading next gather first trace\n...\n");
    return NULL;
  }
}


// JNI call to load next trace of the current gather in dataset
// uses pre-loaded C_TriconDataSet and C_TriconDataSelector structs
// Java_com_tricongeophysics_ParadigmDataSetIO_nativeInitializeDataSet should be run first
//
// returns NULL if end of ensemble encountered
//
// header name list is sent to Java as an array of 8 character strings
// header value array is first converted to floats, then sent to Java
JNIEXPORT jobject JNICALL Java_com_tricongeophysics_ParadigmDataSetIO_nativeLoadNextTrace(JNIEnv *env, 
											  jobject dsioObject, 
											  jobject jseismicTrace)
{
  //printf("in load next trace\n");

  //get next data trace in C
  float *data;
  int   *iheaders; //header version that tridata_first_trace wants

  if ( C_TriconDataSet) {

    if ( tridata_next_trace( C_TriconDataSet, &data, &iheaders ) ) {
      //load trace data into Java
      trace_data_to_java(env, jseismicTrace, data);

      //load trace headers into Java
      trace_headers_to_java(env, jseismicTrace, iheaders);

      //printf("jseismicTrace is:%08X\n",jseismicTrace);

      if (testing) printf("done loading next trace\n...\n");
      return jseismicTrace;
    }
    else {
      //printf("loading next trace hit end of ensemble: \n....\n");
      return NULL; //end of ensemble already reached, can't pass next trace until next ensemble loaded
    }
  }
  else {
    // printf("loading next trace hit end of ensemble: gather# ");
    return NULL; //end of ensemble already reached, can't pass next trace until next ensemble loaded
  }  
}


//This function takes a float array of data samples as input and loads them into the java
// trace data
// Java_com_tricongeophysics_ParadigmDataSetIO_nativeInitializeDataSet should be run first
void trace_data_to_java( JNIEnv *env,           //pointer to all kinds of JNI stuff!
			 jobject jseismicTrace, //java SeismicTrace object
			 float *data_array)     //array containing seismic sample amplitudes
{
  //load trace data into Java
  //printf("in trace data to java\n");
  jfloatArray jdata = (*env)->NewFloatArray(env, C_TriconDataSet->mg->samples);
  //printf("made jdata - size is:%i data_array[0]is:%f\n",C_TriconDataSet->mg->samples, *data_array);
  (*env)->SetFloatArrayRegion(env, jdata, 0, C_TriconDataSet->mg->samples, data_array);
  // printf("copied data array into jdata\n");
  (*env)->CallVoidMethod(env, jseismicTrace, MID_SeismicTrace_setData, jdata);
  //printf("sent jdata to java\n");
  
}


//This function takes an integer array of header values as input and loads them into the java
// trace headers.
// Also takes Header Map from C_TriconDataSet and extracts a list of header names which is sent
// to Java.
// Java_com_tricongeophysics_ParadigmDataSetIO_nativeInitializeDataSet should be run first
void trace_headers_to_java( JNIEnv *env,
			    jobject jseismicTrace, //java SeismicTrace object
			    long4 *iheaders)         //array containing header values (has both floats and ints init)
{

  float *fheaders; //version to be sent to Java
  int headerNameSize = 8; //each header name can only be 8 characters (FOCUS limitation)
  int arraySize = headerNameSize*C_TriconDataSet->mg->num_headers; //number of characters for storing all header names
  jchar *header_list;
  int i=0; int j=0;
  if (testing) printf("num headers is:%i\n",C_TriconDataSet->mg->num_headers);
  if (testing) printf("array size:%i\n",arraySize);
  
  //initialize fheaders as direct copy of iheaders (later will truly convert integers into floats)
  fheaders = (float*)iheaders;

  //dynamically allocate memory for header name list
  header_list = malloc(arraySize*sizeof(jchar)); 
  if (testing) {
    printf( "Data Set: %08X\n", C_TriconDataSet );
    printf( "Merge: %08X\n", C_TriconDataSet->mg );
    printf( "Map: %08X\n", C_TriconDataSet->mg->header_map );
    printf( "Elements: %d\n", (long4) C_TriconDataSet->mg->header_map->total_elements );
    printf( "Header: %08X\n", iheaders );
  }
  //load header name list into character array and convert integer values to float (loading in float array)
  struct header_entry *head = (struct header_entry *) tri_array_first_element( C_TriconDataSet->mg->header_map );
  while( head )
    {
      if (testing) printf ("\nheader num:%i\n", i);
      //convert integer header values to float so that they are all one data type
      if (*(head->type) == 'I') {
	if (testing) printf("i is:%i iheaders[i] is:%i\n",i,*(iheaders+head->index));
	fheaders[i] = (float)iheaders[i];
      }

      if (testing) printf("%-10s %5s     %3d     %4d    %8f\n", 
			  head->name, head->type, head->length, head->index, fheaders[head->index]);

      //load name of header into header list
      for(j=0;j<headerNameSize;j++) {
	header_list[i*headerNameSize+j] = head->name[j];
	//(header_list+i*headerNameSize+j) = (head->name+j);
      }
      
      if (testing) printf("header name loaded into header_list\n");

      head = (struct header_entry *) tri_array_next_element( C_TriconDataSet->mg->header_map );
      if (testing) printf("loaded next header\n");     
 
      i++;
    }
  
  if (testing) printf("name[0]->%c\n",*header_list);

  //load character array name list into Java
  jcharArray jheader_list = (*env)->NewCharArray(env, arraySize);
  (*env)->SetCharArrayRegion(env, jheader_list, 0, arraySize, header_list);
  (*env)->CallVoidMethod(env, jseismicTrace, MID_SeismicTrace_setHeaderList,jheader_list);

  //load header value array into Java
  if (testing) printf("fheaders[0]->%f\n",fheaders[0]);
  jfloatArray jheaders = (*env)->NewFloatArray(env, C_TriconDataSet->mg->num_headers);
   (*env)->SetFloatArrayRegion(env, jheaders, 0, C_TriconDataSet->mg->num_headers, fheaders);
  (*env)->CallVoidMethod(env, jseismicTrace, MID_SeismicTrace_setHeaders, jheaders);
  if (testing) printf("done loading headers\n");

  //trimem_free(header_list);  //already being freed somehow???
  free(header_list);
}


//JNI call to change sort order that traces are read in
JNIEXPORT void JNICALL Java_com_tricongeophysics_ParadigmDataSetIO_nativeChangeSortOrder (JNIEnv *env, 
										    jobject dsioObject, 
										    jstring jorder)
{
  float *data;
  int   *iheaders; //header version that tridata_first_trace wants
  
  //...First, convert Java String jorder to C String order
  const jbyte *order;
  order = (jbyte*)(*env)->GetStringUTFChars(env, jorder, NULL); 
  
  printf("nativeChangeSortOrder: Changing sort order to -> %s\n", order);

  struct tridata_order_ident *order_id;  
  triconArray orders;
  tridata_sort_orders_available(C_TriconDataSet , &orders );
  order_id = (struct tridata_order_ident *)tri_array_first_element( orders );

  //...Loop over available sort orders 
  while( order_id )
    {
      if ( strcmp( order_id->ident, order ) == 0 ) {
	//...Found desired sort order, set dataset to this order
	tridata_set_sort_order( C_TriconDataSet, order_id );
	if ( tridata_load_first_ensemble( C_TriconDataSet ) ) {
	  if (tridata_first_trace( C_TriconDataSet, &data, &iheaders ) ) {
	    printf("order changed? %s\n", C_TriconDataSet->data_reordered ? "yes" : "no");
	    printf(" -- Order successfully changed to %s --\n", order_id->ident);
	    tri_array_destroy( orders );
	    return;
	  }
	  printf("nativeChangeSortOrder: Error - Failed to load first trace\n");
	  tri_array_destroy( orders );
	  return;
	}
	printf("nativeChangeSortOrder: Error - Failed to load first ensemble\n");
	tri_array_destroy( orders );
	return;
      }
      order_id = (struct tridata_order_ident *)tri_array_next_element( orders );
    }
  
  //...If here, desired sort order not found
  printf("\nnativeChangeSortOrder: Error - Sort Order \"%s\" Not Available!!\n", order);
  tri_array_destroy( orders );
  return;
}
