package com.tricongeophysics;

import java.util.*;
import java.awt.event.*;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

import com.tricongeophysics.MapperInputFilesPane.DataChanged;
import com.tricongeophysics.SpatialEditorJob.EditType;

//***************************************************************************
//Mapper
//
//Property of Tricon Geophysics
//
//Original Development: Scott Cook
//
//Overview: Program for reading SEGP-1 coordinate text files
//and displaying them on a map for interactive QC.
//
//Projected version plan: (outdated as of v2.0)
//v1.2 - multiple shot/receiver input files
//- colormap of stations (can show elevation, etc.)
//- user adjust symbol size
//- output SEGP-1 file (merged)
//v1.3 - add/remove & interpolate stations
//- find possible bad stations by dist > sigma along line or
//elevation difference > sigma for project
//v2.0 - can read in OB file (SPS)
//- can excecute GeoBuild in batch mode.
//- GeoBuild updated for silent mode, keeps track of channels.
//v2.1 - talks to Wayne's GeomQC program?
//
//
//1.2.3: Colorbar for elevation plots, adjust symbol size, calculate area - Scott Cook May 29, 2007
//2.0:   Added OB file input(OB button group, OB file key dialog, click to light up channels, etc.) - Scott Cook August, 2007
//2.1:   Added printing capability, LINKED TO GEOBUILD (wow! Finally) - Scott Cook August 15, 2007
//
// TODO:
//   1) bug - can't open files that don't have global write permissions and belong to another user
//            Need to open files as "read only" so java doesn't care about write permissions?
//
//2.1.1: Added FFID, SP, Receiver search menu - Scott Cook September 24, 2007
//***************************************************************************

public class Mapper extends JFrame implements FilesChangedListener, RunProgramListener, TableLoader, WindowListener, FileChangedListener, DataDepot, ChangeListener {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
   
    protected static String version = "3.3";
    
	static final int MAX_SYMBOL_SIZE = 10;
	protected StationPlotter stationPlotter;
	public static String file = "";
    public static String ProjectName;
    public static String LineName;

	protected MapperInputFilesPane inputFilesPane;
	protected MapperEditGeometryPane editGeomPane;
	protected MapperOutputPane outputPane;
	
	protected ReflectiveTableModel receiverList; // all receivers from all input files
	protected ReflectiveTableModel spList; // all shotpoints from all input files
	protected ReflectiveTableModel obList; // all OB records from all input files

	protected JLabel mouseHelpLabel;
	protected JLabel statisticsLabel;
	protected Station thisStation;
	protected JOptionPane pane;
    private boolean outputKills = false;
    private int killedShotPoints=0;
    private int killedReceivers=0;
    private SaveProjectJob saveProjectJob;
    private int killedShots;
    protected CdpGridPane cdpBinningPane;
    protected Object[] symbolSizes = { "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20" };
    NotesPane notesPane;
    public int maxChans;
	
	protected static final int DefaultWidth = 1000;
	protected static final int DefaultHeight = 910;
    private static final String Mapper2Suffix = ".mpr";
    static final String Mapper3Suffix = ".mpr3";
    static final String Mapper3Backup = ".mbck";
    static final String[] SPSFileTypes = {".rps", ".sps", ".xps", ".sum"};
    protected static String baseTitle = "Mapper - v";
    private static final String DefaultFileLoc = ".mapperLastFile";

    void processArgs(String[] args)
    {
	    System.out.println("args: " + args.length);
	    SUtil.print(args);
        if (args == null || args.length < 6) return;
        String project=null, line=null, pgRoot=null;
//        for (String arg:args) {
//            if (arg.contains("-project")) {
//                project = arg.replace("-p", "");
//            }
//            if (arg.contains("-l")) {
//                line = arg.replace("-l", "");
//            }
//        }
        String projFlag = args[0];
        if (projFlag.equals("-pgroot")) {
            pgRoot = args[1];
            project = args[3];
            line = args[5];
        }
        if (project != null && line != null) {
            FocusProjectBrowser.storeDefaults(pgRoot, project, line);
//            String pgRoot = System.getProperty(FocusDbIO.PgSurveyRoot);
//            System.out.print("Found PG_SURVEY_ROOT = " + pgRoot + "\n");
            loadFocusProject(project, line, pgRoot);
//            loadFocusProject(project, line, null);
        }
    }

    protected void loadFocusProject(final String project, final String line, String pgSurveyRoot)
    {
        Mapper.ProjectName = project;
        Mapper.LineName = line;
        Job openFocusJob = new OpenFocusJob(Mapper.this, project, line, pgSurveyRoot);
        openFocusJob.addJobFinishedListener(new JobFinishedListener(){
            @Override
            public void jobFinished(Job job)
            {
                //setModelChanged(false);
                Mapper.this.setProjectName("Focus - " + project + ":" + line);
            }});
        new JobProgressMonitor(Mapper.this, openFocusJob, "Openning Focus Project: "+project + " ," + line);
    }

    public Mapper() {
		super(baseTitle + version);
		receiverList = new ReflectiveTableModel();
		spList = new ReflectiveTableModel();
		obList = new ReflectiveTableModel();
		TriconFileChooser.addFileChangedListener(this);
	}

	public void go() {
		// make JFrame
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		JPanel background = new JPanel(new BorderLayout());
		background.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		

		// make plot area
		stationPlotter = new StationPlotter();
		stationPlotter.addMouseChangedListener(this);
		
		//stationPlotter.setPreferredSize(new Dimension(DefaultWidth, DefaultHeight));
		//stationPlotter.addMouseListener(stationPlotter.new NavigationModeMouseEventListener());
		//stationPlotter.addMouseMotionListener(stationPlotter.new NavigationModeMouseEventListener());
		JPanel plotterPane = new JPanel();
		plotterPane.setLayout(new BorderLayout());
		plotterPane.add(stationPlotter, BorderLayout.CENTER);
		/*
		JDialog plotterPane = new JDialog(this);
		plotterPane.setTitle(this.getTitle());
		plotterPane.add(stationPlotter);
		plotterPane.setSize(DefaultWidth, DefaultHeight);
		plotterPane.setLocation(DefaultWidth, 0);
		plotterPane.setVisible(true);
		*/

		// add menu bar
		JMenuBar menuBar = createMenuBar();
		setJMenuBar(menuBar);

		// make mouse help label
		mouseHelpLabel = new JLabel();
		mouseHelpLabel.setBorder(BorderFactory.createEtchedBorder());
		resetMouseHelpLabel();
		plotterPane.add(BorderLayout.SOUTH, mouseHelpLabel);

		// make statistics label
		statisticsLabel = new JLabel("Statistics>>");
		statisticsLabel.setBorder(BorderFactory.createEtchedBorder());

		// add input files tab (allows user to select shot, receiver, ob files)
		inputFilesPane = new MapperInputFilesPane(this);
		inputFilesPane.addFilesChangedListener(this);
		receiverList.setTableLoader(this);
		
		// add geometry edit tab
		editGeomPane = new MapperEditGeometryPane();
		editGeomPane.addTableModelListener(stationPlotter);
		
		// add output tab
		outputPane = new MapperOutputPane(this, this);
		
		// add CDP Binning tab
		cdpBinningPane = new CdpGridPane();
		stationPlotter.setCdpBinningPane(cdpBinningPane);
		cdpBinningPane.setStationPlotter(stationPlotter);
		
		// add Notes tab
        notesPane = new NotesPane();
		
		// add panes to tabbed pane..

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBorder(BorderFactory.createEtchedBorder());
		tabbedPane.add(inputFilesPane);
		tabbedPane.add(editGeomPane);
		tabbedPane.add(outputPane);
		tabbedPane.add(cdpBinningPane);
		tabbedPane.add(notesPane);
		
		background.add(BorderLayout.SOUTH, statisticsLabel);
		background.add(BorderLayout.CENTER, tabbedPane);
		//JScrollPane ascroller = new JScrollPane(background);
		//ascroller.setPreferredSize(new Dimension(DefaultWidth+200, DefaultHeight/2));
		//ascroller.setPreferredSize(new Dimension(DefaultWidth+200, DefaultHeight/2));
		//JScrollPane bscroller = new JScrollPane(plotterPane);
		
		//JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ascroller, bscroller);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, background, plotterPane);
		//splitPane.setPreferredSize(new Dimension(DefaultWidth/2, DefaultHeight/2));
		//splitPane.setDividerLocation(-1);
		splitPane.setDividerLocation(DefaultWidth);
		splitPane.setOneTouchExpandable(true);
		//getContentPane().add(scroller);
		getContentPane().add(splitPane);

		// make visible!
		setSize(DefaultWidth*2, DefaultHeight);
		//pack(); // let size be however big it needs to be to show everything
		setVisible(true);

	} // end go

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorder(BorderFactory.createLoweredBevelBorder());
		// add file menu
		JMenu fileMenu = new JMenu("File");
		menuBar.add(new JLabel(SUtil.createImageIcon(this.getClass(), "images/tricon_logo.gif",
				"Tricon Geophysics")));
		menuBar.add(fileMenu);
		
		JMenuItem recoverItem = new JMenuItem("Open Project");
		fileMenu.add(recoverItem);
		recoverItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
			    FileFilter[] filterList = getProjectFileFilters();
			    String file2 = TriconFileChooser.launchFilteredFileChooser( 
			            getFile(),
			            filterList, 
			            Mapper.this, "Open");
			    if (file2 == null) return;
			   // file = file2;'
			    if (Mapper.this.getModelChanged())
			        checkSaveOpen(file2);
			    else
			        openProject(new File(file2));
			}
		});

        JMenuItem openFocusItem = new JMenuItem("Open Focus Project");
        fileMenu.add(openFocusItem);
        openFocusItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                final FocusProjectBrowser fpb = new FocusProjectBrowser("Open");
                fpb.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e)
                    {
                        String project = fpb.getProject();
                        String line = fpb.getLine();
                        String pgSurveyRoot = fpb.getPgRoot();
                        if (project == null || line == null) return;
                        project = project.toUpperCase();
                        Mapper.this.loadFocusProject(project, line, pgSurveyRoot);
                    }
                });
            }
        });
        
		JMenuItem saveItem = new JMenuItem("Save Project");
		fileMenu.add(saveItem);
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				saveProject();
			}
		});
		
		JMenuItem saveFocusItem = new JMenuItem("Save to Focus Project");
        fileMenu.add(saveFocusItem);
        saveFocusItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                final FocusProjectBrowser fpb = new FocusProjectBrowser("Save");
                fpb.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e)
                    {
                        String project = fpb.getProject();
                        String line = fpb.getLine();
                        if (project == null || line == null) return;
                        project = project.toUpperCase();
                        Mapper.this.saveFocusProject(project, line, fpb.getPgRoot());
                    }
                });
            }
        });
        

        JMenuItem refreshItem = new JMenuItem("Refresh Project (from text files)");
        fileMenu.add(refreshItem);
        refreshItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                refreshData(DataChanged.All);
            }
        });
        
		fileMenu.addSeparator();
		JMenuItem selectShotItem = new JMenuItem("Select Shot");
		fileMenu.add(selectShotItem);
		selectShotItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (obList != null) {
					int[] shotList = new int[obList.size()];
					for (int i = 0; i < shotList.length; i++)
					    shotList[i] = ((OBRecord) obList.get(i)).getShot();
					final FfidSearchDialog fsd = new FfidSearchDialog(shotList, "Shot");
//					fsd.setStationPlotter(stationPlotter);
					fsd.addActionListener(new ActionListener(){
				            public void actionPerformed(ActionEvent e){
				                if (stationPlotter!=null){
				                    stationPlotter.setSelectedShotRecord(fsd.getSelectedItem());
				                    stationPlotter.repaint();
				                }
					}});
					fsd.go();
				} else {
					JOptionPane.showMessageDialog(Mapper.this,
							"You must first load OB file to select Shot",
							"Select Shot Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		JMenuItem selectFfidItem = new JMenuItem("Select FFID");
        fileMenu.add(selectFfidItem);
        selectFfidItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (obList != null) {
                    int[] ffidList = new int[obList.size()];
                    for (int i = 0; i < ffidList.length; i++)
                        ffidList[i] = ((OBRecord) obList.get(i)).getFfid();
                    final FfidSearchDialog fsd = new FfidSearchDialog(ffidList, "FFID");
//                    fsd.setStationPlotter(stationPlotter);
                    fsd.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e){
                            if (stationPlotter!=null){
                                stationPlotter.setSelectedFFID(fsd.getSelectedItem());
                                stationPlotter.repaint();
                            }
                }});
                    fsd.go();
                } else {
                    JOptionPane.showMessageDialog(Mapper.this,
                            "You must first load OB file to select FFID",
                            "Select FFID Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
		
		fileMenu.addSeparator();
		JMenuItem printItem = new JMenuItem("Print");
		fileMenu.add(printItem);
		printItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				print();
			}
		});
		JMenuItem segp1OutItem = new JMenuItem("Output rps, sps, xps files");
		fileMenu.add(segp1OutItem);
		segp1OutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
			    String file2 = TriconFileChooser.launchFilteredFileChooser(
			            getFile(), SPSFileTypes, Mapper.this, "Save");
                if (file2 == null) return;
                //file = file2;
                SpsFileWriter spswriter = new SpsFileWriter();
                spswriter.setOutputShotKills(true);
                spswriter.setOutputStationKills(true);
                spswriter.writeFile(new File(file2), Mapper.this);
			}
		});
		
		fileMenu.addSeparator();
		JMenuItem exitItem = new JMenuItem("Exit");
		fileMenu.add(exitItem);
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
			    checkSaveExit();
			}
		});

		// add Edit Menu
		JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        
        JMenuItem resetShotItem = new JMenuItem("Reset Shot Numbers");
        editMenu.add(resetShotItem);
        resetShotItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                editGeomPane.resetShots();
            }
        });
        
        JMenuItem smoothItem = new JMenuItem("Smooth");
        editMenu.add(smoothItem);
        smoothItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                final Object column = JOptionPane.showInputDialog(Mapper.this,
                        "Select Column For Smoothing",
                        "Select Column For Smoothing", JOptionPane.PLAIN_MESSAGE,
                        null, stationPlotter.getSymbolColorModeOptions(),
                        stationPlotter.getSymbolColorMode() + "");
                if (column == null) return;
//                Job smoothJob = new SpatialEditorJob(Mapper.this, EditType.Smooth, column.toString());
//                smoothJob.addJobFinishedListener(new JobFinishedListener(){
//                    @Override
//                    public void jobFinished(Job job)
//                    {
//                        //setModelChanged(false);
//                        stationPlotter.setSymbolColorMode(column.toString());
//                        stationPlotter.repaint();
//                    }});
//                new JobProgressMonitor(Mapper.this, smoothJob, "Smoothing: "+column);
            }
        });

        JMenuItem staticItem = new JMenuItem("Elevation Statics");
        editMenu.add(staticItem);
        staticItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                final ElevStaticsDialog elevDialog = new ElevStaticsDialog(Mapper.this);
                elevDialog.setVisible(true);
                elevDialog.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        if (elevDialog.isCanceled()) return;
                        Job elevStaticsJob = new ElevationStaticsJob(Mapper.this.spList, Mapper.this.receiverList, elevDialog);
                        elevStaticsJob.addJobFinishedListener(new JobFinishedListener(){
                            @Override
                            public void jobFinished(Job job)
                            {
                                //setModelChanged(false);
                                //                              stationPlotter.setSymbolColorMode(column.toString());
                                //                              stationPlotter.repaint();
                            }});
                        new JobProgressMonitor(Mapper.this, elevStaticsJob, "Calculating Statics");
                    }

                });
            }
        });

		// add display menu
		JMenu displayMenu = new JMenu("Display");
		menuBar.add(displayMenu);
		JMenuItem zoomMenuItem = new JMenuItem("Zoom");
		displayMenu.add(zoomMenuItem);
		zoomMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				Object factor = JOptionPane.showInputDialog(
						Mapper.this, "Zoom(%):", "Set Arbitrary zoom",
						JOptionPane.PLAIN_MESSAGE, null, null, "120");
				if (factor == null) return;
				double newFactor = 0.01 * SUtil.sval(factor.toString());
				if (newFactor == 0 || newFactor < 0) return;
				stationPlotter.zoomReset(newFactor);
				stationPlotter.repaint();
			}
		});
		final JMenuItem printerFriendlyItem = new JMenuItem("Printer Friendly");
		displayMenu.add(printerFriendlyItem);
		printerFriendlyItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (stationPlotter.isPrinterFriendlyMode()) {
					stationPlotter.setPrinterFriendlyMode(false);
					printerFriendlyItem.setText("Printer Friendly (No)");
				} else {
					stationPlotter.setPrinterFriendlyMode(true);
					printerFriendlyItem.setText("Printer Friendly (Yes)");
				}
			}
		});
		displayMenu.addSeparator();
		JMenuItem symbolSizeItem = new JMenuItem("Symbol Size");
		displayMenu.add(symbolSizeItem);
		symbolSizeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				Object symbolSize = JOptionPane.showInputDialog(
						Mapper.this, "Select Symbol Size:",
						"Select Symbol Size", JOptionPane.PLAIN_MESSAGE, null,
						symbolSizes, stationPlotter.getSymbolSize() + "");
				if (symbolSize == null) return;
				stationPlotter.setSymbolSize((int) SUtil.sval(symbolSize.toString()));
				stationPlotter.repaint();
			}
		});
		JMenuItem symbolColorModeItem = new JMenuItem("Map Color Axis");
		displayMenu.add(symbolColorModeItem);
		symbolColorModeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				Object symbolColorMode = JOptionPane.showInputDialog(Mapper.this,
						"Select Map Color Axis:",
						"Select Map Color Axis", JOptionPane.PLAIN_MESSAGE,
						null, stationPlotter.getSymbolColorModeOptions(),
						stationPlotter.getSymbolColorMode() + "");
				if (symbolColorMode == null) return;
				stationPlotter.setSymbolColorMode(symbolColorMode.toString());
				stationPlotter.repaint();
			}
		});
		
		JMenuItem channelLabelIncrementItem = new JMenuItem(
				"Channel Label Increment");
		displayMenu.add(channelLabelIncrementItem);
		channelLabelIncrementItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				Object increment = JOptionPane.showInputDialog(
						Mapper.this, "Channel Label Increment (0=none):",
						"Set Channel Label Increment:",
						JOptionPane.PLAIN_MESSAGE, null, null, "1");
				if (increment == null) return;
				int interval = (int) SUtil.sval(increment.toString());
				stationPlotter.setChannelLabelInterval(interval);
				stationPlotter.repaint();
			}
		});

        JMenuItem scatterIncrementItem = new JMenuItem(
                "Scatter Shot Increment");
        displayMenu.add(scatterIncrementItem);
        scatterIncrementItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                Object increment = JOptionPane.showInputDialog(
                        Mapper.this, "Scatter Shot Increment",
                        "Set Scatter Shot Increment:",
                        JOptionPane.PLAIN_MESSAGE, null, null, stationPlotter.getScatterIncrement());
                if (increment == null) return;
                int interval = (int) SUtil.sval(increment.toString());
                stationPlotter.setScatterIncrement(interval);
                stationPlotter.repaint();
            }
        });
        
		
		JMenu gbuildMenu = new JMenu("GeoBuild");
		menuBar.add(gbuildMenu);
		JMenuItem runItem = new JMenuItem("Run");
		gbuildMenu.add(runItem);
		runItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				Mapper.this.runGeoBuild();
			}
		});

		// JMenuItem mouseModeItem = new JMenuItem("Mouse Mode");
		// displayMenu.add(mouseModeItem);
		// mouseModeItem.addActionListener(new ActionListener(){
		// public void actionPerformed(ActionEvent ev) {
		// String mouseMode;
		// mouseMode =
		// JOptionPane.showInputDialog(Mapper.this,"Select Mouse Mode:",
		// "Select Mouse Mode",JOptionPane.PLAIN_MESSAGE,icon,
		// display.getMouseModeOptions(),display.getMouseMode()+"").toString();
		// display.setMouseMode(mouseMode);
		// mouseHelpLabel.setText(display.printHelp());
		// }
		// });

		// add help menu
		JMenu helpMenu = new JMenu("Help");
		// menuBar.setHelpMenu(helpMenu); //not yet implemented
		//menuBar.add(Box.createHorizontalGlue());
		menuBar.add(helpMenu);
		JMenuItem docItem = new JMenuItem("Step-By-Step Manual");
		helpMenu.add(docItem);
		docItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				/*JOptionPane
						.showMessageDialog(Mapper.this, MapperHelp.getHelp());
						*/
			    MapperHelp.showDialog();
			}
		});
		/*
		JMenuItem bugItem = new JMenuItem("Bug Fixes");
		helpMenu.add(bugItem);
		bugItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				MapperHelp.showDialog();
			}
		});
		*/
		return menuBar;
	}

    protected void saveFocusProject(String project, String line, String pgSurveyRoot)
    {
        Job saveFocusJob = new SaveFocusJob(Mapper.this, project, line, pgSurveyRoot);
        saveFocusJob.addJobFinishedListener(new JobFinishedListener(){
            @Override
            public void jobFinished(Job job)
            {
                //setModelChanged(false);
            }});
        new JobProgressMonitor(Mapper.this, saveFocusJob, "Saving Focus Project: "+project + " ," + line);
    }

    protected void print() {
	   MapperPrinter p = new MapperPrinter(this);
	   p.print();
	}

    protected FileFilter[] getProjectFileFilters()
    {
        FileFilter[] filters = new FileFilter[3];
        filters[2] = new DynamicFileFilter(Mapper3Suffix);
        ((DynamicFileFilter)filters[2]).setDescription("Mapper3 Project (*"+Mapper3Suffix+")");
        filters[1] = new DynamicFileFilter(Mapper2Suffix);
        ((DynamicFileFilter)filters[1]).setDescription("Old Mapper (*"+Mapper2Suffix+")");
        filters[0] = new DynamicFileFilter(Mapper3Backup);
        ((DynamicFileFilter)filters[0]).setDescription("Project Backup (*"+Mapper3Backup+")");
        return filters;
    }

    protected void exit()
    {
	    System.exit(0);
    }

    protected void checkSaveExit()
    {
        if (getModelChanged()) {
            int answer = JOptionPane.showConfirmDialog(this, "Project has changed.\nWould you like to save?");
            if (answer == JOptionPane.YES_OPTION) {
                saveProject();
                if (saveProjectJob == null) return;
                saveProjectJob.addJobFinishedListener(new JobFinishedListener(){
                    public void jobFinished(Job job)
                    {
                        exit(); //have to wait until done saving before exiting
                    }});
            }
            if (answer == JOptionPane.CANCEL_OPTION) return;
            if (answer == JOptionPane.NO_OPTION) exit();
        } else {
            exit();
        }
    }
    
    protected void checkSaveOpen(final String projectName)
    {
        int answer = JOptionPane.showConfirmDialog(this, "Previous project has changed.\nWould you like to save?");
        if (answer == JOptionPane.YES_OPTION) {
            saveProject();
            saveProjectJob.addJobFinishedListener(new JobFinishedListener(){
                public void jobFinished(Job job)
                {
                    openProject(new File(projectName)); //have to wait until done saving before exiting
                }});
        }
        if (answer == JOptionPane.CANCEL_OPTION) return;
        if (answer == JOptionPane.NO_OPTION) openProject(new File(projectName));
    }


    protected boolean getModelChanged()
    {
        if (editGeomPane.geomChanged()) return true;
        return false;
    }

    protected void saveProject()
    {
        String file2 = TriconFileChooser.launchFilteredFileChooser(
                getFile(), new String[]{Mapper3Suffix}, 
                Mapper.this, "Save");
        if (file2 == null) return;
        //file = file2;
        saveProject(new TriconFile(file2));
    }

    protected void openProject(File file2) {
	    TriconFile tf = new TriconFile(file2.getAbsolutePath());
	    setProjectName(tf.getAbsolutePath());
	    if (tf.getSuffix().equals(Mapper2Suffix)) {
            Job openOldProjectJob = new OpenOldProjectJob(inputFilesPane, file2);
            openOldProjectJob.addJobFinishedListener(new JobFinishedListener(){
                @Override
                public void jobFinished(Job job)
                {
                    refreshData(DataChanged.All);
                    repaint();
                }});
            new JobProgressMonitor(Mapper.this, openOldProjectJob, "Opening Project: "+file2.getName());
	    }
	    else if (tf.getSuffix().equals(Mapper3Suffix) || tf.getSuffix().equals(Mapper3Backup)) {
	        Job openProjectJob = new OpenProjectJob(Mapper.this, file2);
	        openProjectJob.addJobFinishedListener(new JobFinishedListener(){
	            public void jobFinished(Job job)
	            {
	                repaint();
	            }});
	        new JobProgressMonitor(Mapper.this, openProjectJob, "Opening Project: "+file2.getName());
	    }
    }

	private void setProjectName(String absolutePath)
    {
	    this.setTitle(baseTitle + version + " : " + absolutePath);
    }

    protected void saveProject(TriconFile file2) {
	    //MapperProject.toXML(file2, this);
        writeProject(file2.setExtension(Mapper2Suffix)); //we'll also make a backup of the mapper2 version
        checkBackupFile(file2);
        setProjectName(file2.getAbsolutePath());
        saveProjectJob = new SaveProjectJob(Mapper.this, file2);
        saveProjectJob.addJobFinishedListener(new JobFinishedListener(){
            @Override
            public void jobFinished(Job job)
            {
                setModelChanged(false);
            }});
        new JobProgressMonitor(this, saveProjectJob, "Saving Project: "+file2.getName());
	}

    private void checkBackupFile(TriconFile file2)
    {
        String backupName = file2.getAbsoluteFile()+Mapper3Backup;
        if (file2.exists()) {
            if (!file2.renameTo(new File(backupName))) {
                JOptionPane.showMessageDialog(this, "Failed to create file: "+backupName, "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setModelChanged(boolean b)
    {
        editGeomPane.setGeomChanged(b);
    }

    public void resetPlot() {
		stationPlotter.setReceivers(receiverList);
		stationPlotter.setShots(spList);
		stationPlotter.setObRecords(obList);
		stationPlotter.reset();
		stationPlotter.repaint();
        setStatisticsLabel();
	}

	// reload station information and show new plot
	public void refreshData(DataChanged dataChanged) {
		if (dataChanged == DataChanged.All || dataChanged == DataChanged.Receiver) {
	        final LoadStationsJob loadStationsJob = new LoadStationsJob(inputFilesPane.comboReceiverFileKeyList);
	        loadStationsJob.addJobFinishedListener(new JobFinishedListener(){
	            @Override
	            public void jobFinished(Job job)
	            {
	                ArrayList<TableData> sList = ((LoadStationsJob)job).getStations();
	                receiverList = new ReflectiveTableModel(sList);
	                receiverList.sort();
	                editGeomPane.setReceivers(receiverList);
	                receiverList.setTableLoader(Mapper.this);
	                resetPlot();
	            }});
	        new JobProgressMonitor(this, loadStationsJob, "Loading "+DataChanged.Receiver+" Files.");
		}
		if (dataChanged == DataChanged.All || dataChanged == DataChanged.Shot) {
		    final LoadStationsJob loadStationsJob = new LoadStationsJob(inputFilesPane.comboShotFileKeyList);
            loadStationsJob.addJobFinishedListener(new JobFinishedListener(){
                @Override
                public void jobFinished(Job job)
                {
                    ArrayList<TableData> sList = ((LoadStationsJob)job).getStations();
                    spList = new ReflectiveTableModel(sList);
                    spList.sort();
                    editGeomPane.setShotPoints(spList);
                    spList.setTableLoader(Mapper.this);
                    resetPlot();
                }});
            new JobProgressMonitor(this, loadStationsJob, "Loading "+DataChanged.Shot+" Files.");
		}
		if (dataChanged == DataChanged.All || dataChanged == DataChanged.OB) {
		    final LoadObsJob loadObsJob = new LoadObsJob(inputFilesPane.comboObFileKeyList);
		    loadObsJob.addJobFinishedListener(new JobFinishedListener(){
                @Override
                public void jobFinished(Job job)
                {
                    ArrayList<TableData> oList = ((LoadObsJob)job).getObs();
                    obList = new ReflectiveTableModel(oList);
                    obList.sort();
                    editGeomPane.setShotRecords(obList);
                    obList.setTableLoader(Mapper.this);
                    resetPlot();
                }});
            new JobProgressMonitor(this, loadObsJob, "Loading "+DataChanged.OB+" Files.");
		}
	}

	public void setStatisticsLabel() {
		statisticsLabel
				.setText("<HTML>Statistics>>  receiver points= <font color=00af00>"
						+ receiverList.size()
						+ "</font>, shot points= <font color=red>"
						+ spList.size()
						+ "</font>, shot records= <font color=blue>"
						+ obList.size()
						+ "</font>, receiver files= <font color=00af00>"
						+ inputFilesPane.getReceiverFileCount()
						+ "</font>, shot files= <font color=red>"
						+ inputFilesPane.getShotFileCount()
						+ "</font>, OB files= <font color=blue>"
						+ inputFilesPane.getObFileCount());
	}

	

	// write out Project Save file of all file keys
	protected void writeProject(File f) {
		try {
			FileWriter writer = new FileWriter(f);
			writer.write("H Project saved by Mapper v-" + version + "\n");
			for (int ii = 0; ii < inputFilesPane.getShotFileCount(); ii++)
				writer.write(inputFilesPane.getShotFileKey(ii).toStringDetailed(ShotFileKey.KEY_DESCRIPTION));
			for (int ii = 0; ii < inputFilesPane.getReceiverFileCount(); ii++)
				writer.write(inputFilesPane.getReceiverFileKey(ii).toStringDetailed(ReceiverFileKey.KEY_DESCRIPTION));
			for (int ii = 0; ii < inputFilesPane.getObFileCount(); ii++)
				writer.write(inputFilesPane.getObFileKey(ii).toStringDetailed(OBFileKey.KEY_DESCRIPTION));
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}


	public void runGeoBuild() {

		// ... Check if Geobuild exists
		ArrayList<String> gbuildCommands = new ArrayList<String>();
		SpawnProcess spawnProcess = new SpawnProcess("Run Geobuild");
		spawnProcess.showDialog();
		gbuildCommands.add("gbuild");
		gbuildCommands.add("-h"); // try running gbuild in help mode
		String output = spawnProcess.runProcess(gbuildCommands, null);
		if (output.indexOf("gbuild: version -") > 0) { // output should contain
														// beginning of help
														// text
			JOptionPane.showMessageDialog(Mapper.this,
					"Cannot find \"gbuild\" excecutable \n" + output,
					"GeoBuild excecution error", JOptionPane.ERROR_MESSAGE);
			spawnProcess.kill();
			return;
		}

		// check if input files (.rps, .sps, .xps) exist
		if (receiverList == null || spList == null || obList == null) {
			spawnProcess.kill();
			JOptionPane
					.showMessageDialog(
							Mapper.this,
							"Receiver, Shot Point, and OB files are necessary to run GeoBuild",
							"GeoBuild excecution error",
							JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Excecutable and input files exist, so go ahead and run gbuild
		spawnProcess.setVisible(false);
		
		String file2 = TriconFileChooser.launchFilteredFileChooser(
		        getFile(), SPSFileTypes, Mapper.this, "Open");
        if (file2 == null) return;
        //file = file2;
		runGbuild(spawnProcess, new TriconFile(file2));
	}

	public void runGbuild(SpawnProcess spawnProcess, TriconFile file2) {
		// make input files for geobuild, if successful, run geobuild
		// if (!file.getName().contains("_gb")) file =
		// new File
		// (file.getAbsolutePath().substring(0,file.getAbsolutePath().indexOf('.'))+"_gb");
		// //
		ArrayList<String> gbuildCommands = new ArrayList<String>();
		gbuildCommands.add("gbuild");
		gbuildCommands.add("-q"); // set to quiet mode
		SpsFileWriter filewriter = new SpsFileWriter();
		filewriter.setOutputShotKills(outputKills);
		filewriter.setOutputStationKills(outputKills);
		//spawnProcess.appendText("Writing coordinate files... ");
		if (filewriter.writeFile(file2, Mapper.this)) {
		    killedShotPoints = filewriter.getKilledShotPoints();
		    killedReceivers = filewriter.getKilledReceivers();
		    killedShots = filewriter.getKilledShots();
		    if  (writeParmFiles(file2)) {
		        // now, actually run geobuild and send output to text area
		        spawnProcess.setVisible(true);
		        spawnProcess.runProcess(gbuildCommands, file2.getParentFile());
		    }
		} else {
		    JOptionPane.showMessageDialog(Mapper.this,
		            "Failed to write out Receiver, Shot Point, and OB files",
					"GeoBuild excecution error", JOptionPane.ERROR_MESSAGE);
			spawnProcess.kill();
			return;
		}
	}

	public boolean writeParmFiles(File f) {
		if (receiverList == null || spList == null || obList == null) return false;
		String rootFileName = "";
		int index = f.getAbsolutePath().indexOf(".");
		if (index >= 0)
			rootFileName = f.getAbsolutePath().substring(0, index);
		else
			rootFileName = f.getAbsolutePath();
		File spsfile = new File(rootFileName + ".sps");
		File rpsfile = new File(rootFileName + ".rps");
		File xpsfile = new File(rootFileName + ".xps");
		try {
			// write receiver parm file
			FileWriter writer = new FileWriter(f.getParent() + File.separator
					+ "receiverparms.dat");
			writer.write("Parameters for reading receiver data.\n");
			writer.write(rpsfile.getName() + "% file name\n");
			writer
					.write("1%	 mode (1=key,2=no key,3=qc_file)" + "\n"
							+ "R%	 key used to find data (for mode=1)" + "\n"
							+ "0%	 column location of key (for mode=1)" + "\n"
							+ (receiverList.size() - killedReceivers)
							+ "%	 # of stations found in receiver file"
							+ "\n"
							+ "0%	 # of lines to skip at beginning of receiver file"
							+ "\n"
							+ "1%	 line column 0 (0=start,1=#digits)"
							+ "\n"
							+ "8%	 line column 1 (0=start,1=#digits)"
							+ "\n"
							+ "17%	 station column 0 (0=start,1=#digits)"
							+ "\n"
							+ "8%	 station column 1 (0=start,1=#digits)"
							+ "\n"
							+ "45%	 x column 0 (0=start,1=#digits)"
							+ "\n"
							+ "10%	 x column 1 (0=start,1=#digits)"
							+ "\n"
							+ "55%	 y column 0 (0=start,1=#digits)"
							+ "\n"
							+ "10%	 y column 1 (0=start,1=#digits)"
							+ "\n"
							+ "65%	 z column 0 (0=start,1=#digits)"
							+ "\n"
							+ "6%	 z column 1 (0=start,1=#digits)"
							+ "\n"
							+ "\n"
							+ "NOTE: Do NOT delete any lines. Only change values before \"%\" and LEAVE % !\n");
			writer.close();

			// write shot parm file
			writer = new FileWriter(f.getParent() + File.separator
					+ "shotparms.dat");
			writer.write("Parameters for reading shot data.\n");
			writer.write(spsfile.getName() + "% file name\n");
			writer
					.write("1%	 mode (1=key,2=no key,3=qc_file)" + "\n"
							+ "S%	 key used to find data (for mode=1)" + "\n"
							+ "0%	 column location of key (for mode=1)" + "\n"
							+ (spList.size() - killedShotPoints)
							+ "%	 # of stations found in shot file"
							+ "\n"
							+ "0%	 # of lines to skip at beginning of shot file"
							+ "\n"
							+ "1%	 line column 0 (0=start,1=#digits)"
							+ "\n"
							+ "8%	 line column 1 (0=start,1=#digits)"
							+ "\n"
							+ "17%	 station column 0 (0=start,1=#digits)"
							+ "\n"
							+ "8%	 station column 1 (0=start,1=#digits)"
							+ "\n"
							+ "45%	 x column 0 (0=start,1=#digits)"
							+ "\n"
							+ "10%	 x column 1 (0=start,1=#digits)"
							+ "\n"
							+ "55%	 y column 0 (0=start,1=#digits)"
							+ "\n"
							+ "10%	 y column 1 (0=start,1=#digits)"
							+ "\n"
							+ "65%	 z column 0 (0=start,1=#digits)"
							+ "\n"
							+ "6%	 z column 1 (0=start,1=#digits)"
							+ "\n"
							+ "\n"
							+ "NOTE: Do NOT delete any lines. Only change values before \"%\" and LEAVE % !\n");
			writer.close();

			// write obs
			String project = f.getName();
			if (project.indexOf(".") > 0)
				project = project.substring(0, project.indexOf(".")); // get rid  of suffix
			if (project.length() > 8)
				project = project.substring(0, 8);
			int nshots = obList.size() - killedShots;
			writer = new FileWriter(f.getParent() + File.separator
					+ "ob_parms.dat");
			writer
					.write("     ...GeoBuild parameters for reading in Observer Report data...%"
							+ "\n"
							+ "....:GENERAL PARAMETERS:....%"
							+ "\n"
							+ "7%	 OB Type (1=I/O IID; 2=I/O IIT; 3=Strip; 4=RSR; 5=Strucr; 6=FOCUS; 7=SPS)"
							+ "\n"
							+ xpsfile.getName()
							+ "%	 OB file name (or path)"
							+ "\n"
							+ project
							+ "%	 project name"
							+ "\n"
							+ // need to write code to take filename and give 8
								// characters
							project
							+ "%	 line name"
							+ "\n"
							+ // need to write code to take filename and give 8
								// characters
							nshots
							+ "%	 # of shot records (changing this value effects space allocation for shot records)"
							+ "\n"
							+ "n%	 use xkey file? (y/n)"
							+ "\n"
							+ "%	 xkey file name (or path)"
							+ "\n"
							+ "n%	 save nearest rec-stat as S.P.? (y/n)"
							+ "\n"
							+ "n%	 reduce # of patterns? (y/n)"
							+ "\n"
							+ "0%	 1st pattern number (type \"0\" to use ffid/xkey # when not reducing patterns)"
							+ "\n"
							+ "....:RSR AND I/O II DETAILED COMMON PARAMETERS:....%"
							+ "\n"
							+ "X%	 ffid key"
							+ "\n"
							+ "%	 uphole key"
							+ "\n"
							+ "%	 depth key"
							+ "\n"
							+ "%	 SP_line key"
							+ "\n"
							+ "%	 SP_station key"
							+ "\n"
							+ "0%	 factor to multipy SP_line by (if using SP_line & SP_station)"
							+ "\n"
							+ "%	 comment key"
							+ "\n"
							+ "%	 new_shot key (RSR) End_Shot key"
							+ "\n"
							+ "....:I/O SYSTEM II DETAILED ONLY PARAMETERS:....%"
							+ "\n"
							+ "%	 Rec_Line key"
							+ "\n"
							+ "%	 1st_Rec_Station key"
							+ "\n"
							+ "%	 Last_Rec_Station key"
							+ "\n"
							+ "....:RSR ONLY PARAMETERS:....%"
							+ "\n"
							+ "%	 pattern key"
							+ "\n"
							+ "0%	 ignore skips in pattern? (0=n/1=y)"
							+ "\n"
							+ "0%	 # of lines to skip after \"\" to find active spread"
							+ "\n"
							+ "0%	 # of characters used to search for each pattern value"
							+ "\n"
							+ "0%	 pos to start reading pattern value # 1 (1=line;2=1st_rec;3=last_rec)"
							+ "\n"
							+ "0%	 pos to start reading pattern value # 2 (1=line;2=1st_rec;3=last_rec)"
							+ "\n"
							+ "0%	 pos to start reading pattern value # 3 (1=line;2=1st_rec;3=last_rec)"
							+ "\n"
							+ "0%	 use script instead of SP_line & SP_station? (0=n/1=y)"
							+ "\n"
							+ "%	 script key"
							+ "\n"
							+ "....:FOCUS HEADER TABLE AND STRIP COMMON PARAMETERS:....%"
							+ "\n"
							+ "3%	 FFID column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 FFID column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "13%	 Shot_Line column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 Shot_Line column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "29%	 Shot_Station column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 Shot_Station column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "49%	 Rec_Line column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 Rec_Line column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "65%	 Rec_Station column data (From_Station for Strip)1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 Rec_Station column data (From_Station for Strip)2 (1=start/2=#digits)"
							+ "\n"
							+ "83%	 Uphole column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "1%	 Uphole column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "84%	 Depth column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "1%	 Depth column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "....:STRIP ONLY PARAMETERS:....%"
							+ "\n"
							+ "73%	 To_Station column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 To_Station column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "....:SPS AND I/O II TAPE LOG COMMON PARAMETERS:....%"
							+ "\n"
							+ "X%	 key indicative of shot data (only 1st letter used)"
							+ "\n"
							+ "0%	 1st column of shot data key"
							+ "\n"
							+ "3%	 FFID column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 FFID column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "13%	 Shot_Line column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 Shot_Line column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "29%	 Shot_Station column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 Shot_Station column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "49%	 Rec_Line column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 Rec_Line column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "65%	 Rec_Station column data (From_Station for Strip)1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 Rec_Station column data (From_Station for Strip)2 (1=start/2=#digits)"
							+ "\n"
							+ "73%	 To_Station column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "8%	 To_Station column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "83%	 Uphole column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "1%	 Uphole column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "84%	 Depth column data 1 (1=start/2=#digits)"
							+ "\n"
							+ "1%	 Depth column data 2 (1=start/2=#digits)"
							+ "\n"
							+ "\n"
							+ "\n"
							+ "NOTE: Do NOT delete any lines. Only change values before \"%\" and LEAVE % !"
							+ "\n" + "");
			writer.close();
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	// class ComboBoxRenderer extends JLabel
	// implements ListCellRenderer {
	// public ComboBoxRenderer() {
	// // super(20);
	// setPreferredSize(new Dimension(250,20));
	// setFont(new Font("Courier",Font.PLAIN, 12));
	// setHorizontalAlignment(TRAILING);
	// }
	// public Component getListCellRendererComponent(
	// JList list,
	// Object value,
	// int index,
	// boolean isSelected,
	// boolean cellHasFocus) {
	//	
	// if (isSelected) {
	// setBackground(list.getSelectionBackground());
	// setForeground(list.getSelectionForeground());
	// } else {
	// setBackground(list.getBackground());
	// setForeground(list.getForeground());
	// }
	// if (list.getSelectedValue() !=null) {
	// String text = new String();
	// text = ((FileKey)value).getInputFile().getPath();
	// list.setFont(new Font("COURIER",Font.PLAIN, 12));
	// // if (this.getColumns() < text.length())
	// // setText(text.substring(text.length()-this.getColumns()));
	// ////setText(this.getFont().toString());
	// // else setText(text);
	// setText(text);
	//	
	// }
	//	
	// // setFont(list.getFont());
	//	
	// return this;
	// }
	// }

	@Override
	public void inputFilesChanged(DataChanged dataChanged) {
		refreshData(dataChanged);
	}
	public static String getFile() {
	    if (file.equals("")) 
	        file = readDefaultFile();
		return file;
	}

	private static String readDefaultFile()
	{
	    String homeDir = System.getenv("HOME");
	    String defaultFileLoc = homeDir+File.separator+DefaultFileLoc;
	    TriconFile f = new TriconFile(defaultFileLoc);
	    String lastPath = "";
	    if (f.exists()) {
	        try {
	            lastPath = f.readFileFast().trim();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return lastPath;
	}

	private void writeDefaultFile()
	{
	    String homeDir = System.getenv("HOME");
        String defaultFileLoc = homeDir+File.separator+DefaultFileLoc;
        TriconFile f = new TriconFile(defaultFileLoc);
        try {
            f.write(new String[] {file});
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void setFile(String f) {
		if (f == null) return;
		file = f;
	}
	
	@Override
	public void runProgram() {
		SpawnProcess process = new SpawnProcess("Running GeoBuild");
		process.showDialog();
		runGbuild(process, new TriconFile(getFile()));
	}

	@Override
	public void reload(TableModel tableModel) {
		if (tableModel == spList) {
			this.refreshData(DataChanged.Shot);
			return;
		}
		if (tableModel == receiverList) {
			this.refreshData(DataChanged.Receiver);
			return;
		}
		if (tableModel == obList) {
			this.refreshData(DataChanged.OB);
			return;
		}
	}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e)
    {
        this.checkSaveExit();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void setOutputKills(boolean outputKills)
    {
        this.outputKills = outputKills;
    }

    @Override
    public void fileChanged(String fileName)
    {
        file = fileName;
        writeDefaultFile();
    }

    @Override
    public Object getData()
    {
        return new MapperProject(this);
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        resetMouseHelpLabel();
    }

    protected void resetMouseHelpLabel()
    {
        mouseHelpLabel.setText(stationPlotter.printHelp());
    }

    public CdpModel getCdpModel()
    {
        return cdpBinningPane.getCdpModel();
    }

    public String getNotes()
    {
        return notesPane.getText();
    }
} // close Mapper class
