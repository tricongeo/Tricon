# This definition makes JAVA_HOME refer to the JDK specified in your PATH.
# You can change this to refer to a specific directory
JAVA_HOME=/pdata/apps/java/jdk1.6.0_25
CDIR=$(BASE_DIR)/java/ParadigmDataSetIO/
JDIR=$(BASE_DIR)/java/ParadigmDataSetIO/src
CFILE=ParadigmDataSetIO
JFILE=com/tricongeophysics/ParadigmDataSetIO
JPKG=com.tricongeophysics.ParadigmDataSetIO
CLASS_DIR=$(BASE_DIR)/java/ParadigmDataSetIO/bin
OBJS_DIR=$(BASE_DIR)/interactive/src/standalone_library
CLASSPATHS=$(BASE_DIR)/java/ParadigmDataSetIO/src/JOGL-magicianFonts.jar:$(BASE_DIR)/java/ParadigmDataSetIO/src/linux-amd64/lib/jogl.jar:$(BASE_DIR)/java/ParadigmDataSetIO/src/linux-amd64/lib/gluegen-rt.jar:$(BASE_DIR)/java/ParadigmDataSetIO/src/jama.jar:$(BASE_DIR)/java/ParadigmDataSetIO/src/JChart2D.jar:$(BASE_DIR)/java/ParadigmDataSetIO/src/jhall.jar:$(BASE_DIR)/java/ParadigmDataSetIO/src/jh.jar:$(BASE_DIR)/java/ParadigmDataSetIO/src/jmf.jar:$(BASE_DIR)/java/ParadigmDataSetIO/src/junit.jar:$(BASE_DIR)/java/ParadigmDataSetIO/src/OpenSpirit.jar:$(BASE_DIR)/java/ParadigmDataSetIO/src/ParadigmDataSetIOUsage.jar

OBJS=#${OBJS_DIR}/tri_memory.o \
    #${PG_ROOT}/p2.0/arch/Linux_em64_2.3.2_i9/lib/libPGHlbr.so 
    #${PG_ROOT}/p2.0/arch/Linux_em64_2.3.2_i9/lib/libPGHpds.so
    # ${OBJS_DIR}/tri_array.o \
    # ${OBJS_DIR}/tri_error.o \
    # ${OBJS_DIR}/tricon_linked_list.o \
    # ${OBJS_DIR}/tricon_dbfile.o \
    # /apdata/tf/tricon/interactive/linux/epos54/obj/library/tricon_geometry.o \
    # /apdata/tf/tricon/interactive/linux/epos54/obj/library/tri_utilities.o \
    # ${OBJS_DIR}/tri_utilities.o \



$(CFILE).so: $(JFILE).h
	gcc -Wl,--export-dynamic -O -lm -g -fPIC -shared -I../../interactive/src/include  -L../../linux/epos54/lib  -I/disco/Epos3TE/Epos3/p2.0/include  -I/disco/Epos3TE/Epos3/base/include -L/disco/Epos3TE/Epos3/p2.0/arch/Linux_em64_2.3.2_i9/lib -lPGILockClient -L/disco/Epos3TE/Epos3/p2.0/arch/Linux_em64_2.3.2_i9/lib   /disco/Epos3TE/Epos3/p2.0/arch/Linux_em64_2.3.2_i9/lib/mpp.o /disco/Epos3TE/Epos3/p2.0/arch/Linux_em64_2.3.2_i9/lib/C64bit.o  -lPGHplt -lPGHsim -lPGHmth  -lPGHlbr -lPGHpds -lPGHbase   -lGTunix -lPGIFileMan -lPGIda_tools -lGTda_geo -lGTda -lPGIFileMan -lGTda -lGTda_geo -lGTFileView -lGTFileRecFilter -lGTfw -lGTGDUtil -lGTpacDA -lGTTraceFile -lGTxt -lGTunix -lGTvisix -lPGHplt -lPGHmth -lPGHlbr -lPGHpds -lPGHbase  -lGTunix -lPGIFileMan -lPGIda_tools -lGTda_geo -lGTda -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -o $(CDIR)lib$(CFILE).so  $(CDIR)$(CFILE).c $(OBJS) 



$(JFILE).h: #$(JFILE).class
	$(JAVA_HOME)/bin/javah -jni -classpath $(CLASS_DIR) $(JPKG)

$(JFILE).class:
	#cd $(JDIR) && $(JAVA_HOME)/bin/javac -classpath $(CLASSPATHS) -d $(CLASS_DIR)  $(JFILE).java	

run:
	$(JAVA_HOME)/bin/java -Xmx3072m -DLD_LIBRARY_PATH=$(CDIR):/opt/intel/cce/9.0/lib/ -cp $(CLASS_DIR) $(JPKG)


