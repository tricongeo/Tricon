#include <stdlib.h>
#include <stdio.h>
#include <cnv-cfb.h>
#include <sdb.h>
//
//
////Here's where we ask Focus for attribute values from the database
//static void loadAttr(char* line_name, char* model, char* event, char* attr, INTEGER* vals, INTEGER* nlocs, INTEGER* ixval, INTEGER* ixcode)
//{
//	printf("inside loadAttr\n");
//	char line_pattern[16] = "*";
//	char model_pattern[16] = "*";
//	char event_pattern[16] = "*";
//	char attr_pattern[16] = "*";
//	INTEGER status = 9999;
//
////	char model[16];
////	char event[16];
////	char attr[16];
//	char interp[16];
//	char xunit[16];
//	char buftyp[16];
//
//	INTEGER xinc, xstart, xend, nvals;
//
//	char mdate[88];
//
//	INTEGER next_id;
//
//	line_pattern[1] = 0;
//	model_pattern[1] = 0;
//	event_pattern[1] = 0;
//	attr_pattern[1] = 0;
//
//	strcpy(buftyp, "USE");
//	printf("status %i\n", status);
//	printf("line:%s model:%s event:%s attr:%s\n", line_name, model, event, attr);
//
//	sdb_atrget_f(line_name, model, event, attr, buftyp, vals, interp, 10,
//			xunit, 10, &xinc, &xstart, &xend, nlocs, &nvals, ixval,
//			ixcode, &status);
//
//	printf("status:   %i\n", status);
//	if (status != SDB_NORMAL) {
//		fprintf(stderr, "FocusDbIO: Error loading %s:%s:%s\n", model, event,
//				attr);
//	}
//	if (status == SDB_ILLINE) {
//		fprintf(stderr, "Illegal syntax for LINE\n");
//	}
//	if (status == SDB_ILMODEL) {
//		fprintf(stderr, "Illegal syntax for MODEL\n");
//	}
//	if (status == SDB_ILEVENT) {
//		fprintf(stderr, "Illegal syntax for EVENT\n");
//	}
//	if (status == SDB_ILATTR) {
//		fprintf(stderr, "Illegal syntax for Attribute\n");
//	}
//	if (status == SDB_NOTFND) {
//		fprintf(stderr, "Model or attribute not found\n");
//	}
//	if (status == SDB_PROT) {
//		fprintf(stderr, "File protection precludes access\n");
//	}
//	if (status == SDB_NORMAL) {
//		printf("Attribute Get Success!\n");
//	}
//	//free(vals_memory);
//}
//
//static void getAttrList(char* line_name) {
//	INTEGER next_id, status, xinc, xstart, xend, nlocs, nvals;
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
//			printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, line, model, event, attr);
//		}
//	}
//	printf("id:%i line:%s model:%s event:%s attr:%s\n", next_id, line, model, event, attr);
//}


void processStatus(INTEGER status2)
{
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
}

int main( int argc, char ** argv )
{
	printf("hi\n");

	printf(" in C\n");
	//...Get project info
	printf("getting project\n");

	char* project = "SCOTT3D";
	char* line = "PAULS3D";

	printf( "project:       :%s:\n",project);
	printf( "line:          %s\n",line);

	INTEGER status = 9999;

	//InitLockClient ( argc, argv );

	printf("status:   %i\n", status);

	sdb_sdbopn_f((char*)project, &status); //segv (no compile errors)

	printf("status:   %i\n", status);
	if ( status != SDB_NORMAL  )
	{
		printf("Failed to open project: %s\n", project);
	}

	processStatus(status);

	if ( status == SDB_NORMAL  ) printf("Project Open Success\n");

//	getAttrList(line);

//	INTEGER* vals;
//	INTEGER nlocs, ixval, ixcode;
//	int model_size = 100000;
//	int value_and_code = 2;
//	printf("getting memory\n");
//	vals = (INTEGER *) malloc(sizeof(INTEGER) * model_size * value_and_code);
//	loadAttr(line, "SHOT", "CHAN", "LINE", vals, &nlocs, &ixval, &ixcode);
	return 0;



}
