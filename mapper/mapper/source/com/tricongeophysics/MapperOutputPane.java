package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MapperOutputPane extends JTabbedPane implements ActionListener, ChangeListener, FileChangedListener {
	
	private static final String Browse = "Browse";
	private static final String Output = "Output";
	private static final String Run = "Run";
    private static final String OutputKills = "OutputKills";
    private static final String Focus = "Focus";
    private static final String OutputShotKills = "OutputShotKills";
	private boolean outputKills = false;
	JPanel spsOutputPane;
	JPanel focusOutputPane;
	JPanel gbuildOutputPane;
	JLabel infoLabel;
	
	JTextField textField;
	JButton browseButton;
	JButton outputButton;
	JButton runButton;
	private RunProgramListener runGbuild;
    private JCheckBox outputKillsButton;
    private String file="";
    private SaveAllButton focusButton;
    private DataDepot dataDepot;
    private JCheckBox outputShotKillsButton;
    private boolean outputShotKills = false;

	MapperOutputPane(DataDepot dataDepot, RunProgramListener runGbuild) {
	    this.dataDepot = dataDepot;
		this.runGbuild = runGbuild;
		this.setName("3. Output");
		this.setTabPlacement(LEFT);
		this.addChangeListener(this);

		textField = new JTextField(Mapper.getFile());
		textField.setEditable(false);
		
		outputKillsButton = new JCheckBox("Output Killed Stations", outputKills);
        outputKillsButton.addActionListener(this);
        outputKillsButton.setActionCommand(OutputKills);
        outputKillsButton.setToolTipText("Select whether killed stations are output to text files.");
        outputKillsButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        outputShotKillsButton = new JCheckBox("Output Killed Shot Records", outputShotKills);
        outputShotKillsButton.addActionListener(this);
        outputShotKillsButton.setActionCommand(OutputShotKills);
        outputShotKillsButton.setToolTipText("Select whether killed shot records are output to text files.");
        outputShotKillsButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));

		browseButton = new BrowseButton();
		browseButton.addActionListener(this);
		browseButton.setActionCommand(Browse);
		browseButton.setToolTipText("Browse for output root file name");

		infoLabel = new JLabel();
		setInfoLabelText(Mapper.getFile());
		
		spsOutputPane = makeSpsPane(new JPanel());
		focusOutputPane = makeFocusPane(new JPanel());
		gbuildOutputPane = makeGeoBuildPane(new JPanel());
		
		TriconFileChooser.addFileChangedListener(this);
		
		this.add(spsOutputPane);
		this.add(focusOutputPane);
		this.add(gbuildOutputPane);
	}

	private JPanel makeGeoBuildPane(JPanel panel) {
		runButton = new LaunchButton();
		runButton.addActionListener(this);
		runButton.setActionCommand(Run);
		runButton.setToolTipText("Launch GeoBuild");
		
		panel.setName("GeoBuild");
		JPanel buttonsPane = new JPanel();
		buttonsPane.setLayout(new BoxLayout(buttonsPane, BoxLayout.X_AXIS));
		buttonsPane.add(textField);
		buttonsPane.add(browseButton);
		buttonsPane.add(runButton);
		buttonsPane.setPreferredSize(new Dimension(300,28));
		
		outputKillsButton.setText("Output Killed Stations/Records");
		outputKillsButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JPanel topPane = new JPanel();
		topPane.setLayout(new BorderLayout());
		topPane.add(buttonsPane, BorderLayout.NORTH);
		topPane.add(outputKillsButton, BorderLayout.CENTER);
		topPane.add(infoLabel, BorderLayout.SOUTH);
		
		panel.setLayout(new BorderLayout());
		panel.add(topPane, BorderLayout.NORTH);
		return panel;
	}

	private JPanel makeFocusPane(JPanel panel) {
		panel.setName("Focus");
		focusButton = new SaveAllButton();
		focusButton.addActionListener(this);
		focusButton.setActionCommand(Focus);
		focusButton.setToolTipText("Save geometry edits to focus EDIT module files");
		
		JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new BoxLayout(buttonsPane, BoxLayout.X_AXIS));
        buttonsPane.add(textField);
        //buttonsPane.add(browseButton);  //put this functionality into the focusButton (one-step instead of two-step process)
        buttonsPane.add(focusButton);
        buttonsPane.setPreferredSize(new Dimension(300,28));
        
        JPanel editsPane = new JPanel();
        editsPane.setLayout(new BorderLayout());
//        editsPane.add(new JLabel("Output Focus Edit Jobs"), BorderLayout.NORTH);
        editsPane.add(buttonsPane, BorderLayout.CENTER);
        editsPane.add(infoLabel, BorderLayout.SOUTH);
//        editsPane.setBorder(BorderFactory.createEtchedBorder());
        editsPane.setBorder(BorderFactory.createTitledBorder("Output Focus Edit Jobs"));
        
        panel.setLayout(new BorderLayout());
        panel.add(editsPane, BorderLayout.SOUTH);
        
        
        JPanel browserPane = new JPanel();
        browserPane.setLayout(new BorderLayout());
        final FocusProjectBrowser fpb = new FocusProjectBrowser(false, "Save");
        fpb.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                String project = fpb.getProject();
                String line = fpb.getLine();
                if (project == null || line == null) return;
                project = project.toUpperCase();
                ((Mapper)dataDepot).saveFocusProject(project, line, fpb.getPgRoot());
            }
        });
        browserPane.add(fpb.getBackgroundPane());
//        browserPane.add(new JLabel("Save to Focus Database"), BorderLayout.NORTH);
//        browserPane.setBorder(BorderFactory.createEtchedBorder());
        browserPane.setBorder(BorderFactory.createTitledBorder("Save to Focus Database"));
        
        panel.add(browserPane, BorderLayout.CENTER);
		/*
		JLabel label = new JLabel();
		label.setText("Send geometry directly to Focus. (Coming soon!)");
		
		panel.add(label);
		*/
		return panel;
	}

	private JPanel makeSpsPane(JPanel panel) {
		outputButton = new SaveAllButton();
		outputButton.addActionListener(this);
		outputButton.setActionCommand(Output);
		outputButton.setToolTipText("Save to *.xps, *.sps, *.rps, *.sum files");
		
		panel.setName("SPS");
		JPanel buttonsPane = new JPanel();
		buttonsPane.setLayout(new BoxLayout(buttonsPane, BoxLayout.X_AXIS));
		buttonsPane.add(textField);
		//buttonsPane.add(browseButton); //put this functionality into the outputButton (one-step instead of two-step process)
		buttonsPane.add(outputButton);
		buttonsPane.setPreferredSize(new Dimension(300,28));
		
		JPanel killPane = new JPanel();
		killPane.setLayout(new BoxLayout(killPane, BoxLayout.Y_AXIS));
		outputKillsButton.setText("Output Killed Stations");
		outputKillsButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
		killPane.add(outputShotKillsButton);
        killPane.add(outputKillsButton);
		
		JPanel topPane = new JPanel();
		topPane.setLayout(new BorderLayout());
		topPane.add(buttonsPane, BorderLayout.NORTH);
		topPane.add(killPane, BorderLayout.CENTER);
		topPane.add(infoLabel, BorderLayout.SOUTH);
		
		panel.setLayout(new BorderLayout());
		panel.add(topPane, BorderLayout.NORTH);
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == Browse) {
		    browse("Save");
            //Mapper.setFile(file2);
			return;
		}
		if (e.getActionCommand() == OutputKills) {
            outputKills = !outputKills;
            return;
        }
		if (e.getActionCommand() == OutputShotKills) {
            outputShotKills = !outputShotKills;
            return;
        }
		if (e.getActionCommand() == Output) {
		    if (browse("Save") != null) {
		        SpsFileWriter spsWriter = new SpsFileWriter();
		        spsWriter.setOutputStationKills(outputKills);
		        spsWriter.setOutputShotKills(outputShotKills);
		        if (!spsWriter.writeFile(new File(file), dataDepot)) {
		            JOptionPane.showMessageDialog(this, "Failed to output file: "+file, "File Save Error", JOptionPane.ERROR_MESSAGE);
		            return;
		        }
		        infoLabel.setText(infoLabel.getText() + "<br><font size=6>Saved</font>");
		    }
		    return;
		}
		if (e.getActionCommand() == Run) {
		    runGbuild.setOutputKills(outputKills);
			runGbuild.runProgram();
			return;
		}
		if (e.getActionCommand() == Focus) {
		    if (browse("Save") != null) {
		        FocusFileWriter focusWriter = new FocusFileWriter();
		        if (!focusWriter.writeFile(new File(file), dataDepot)) {
		            JOptionPane.showMessageDialog(this, "Failed to output file: "+file, "File Save Error", JOptionPane.ERROR_MESSAGE);
		            return;
		        }
		        infoLabel.setText(infoLabel.getText() + "<br><font size=6>Saved</font>");
		    }
		    return;
		}
	}

	private String browse(String buttonText)
    {
	    return TriconFileChooser.launchFilteredFileChooser(Mapper.getFile(),
                Mapper.SPSFileTypes, 
                MapperOutputPane.this, buttonText);
    }

    private void setInfoLabelText(String name) {
		String rootName = name;
		int index = name.lastIndexOf(".");
		if (index > 0)
			rootName = name.substring(0, name.lastIndexOf("."));
		String recordName = rootName + ".xps";
		String shotName = rootName + ".sps";
		String rcvName = rootName + ".rps";
		String sumName = rootName + ".sum";
		String description = "";
		if (this.getSelectedComponent() == focusOutputPane) {
		    recordName = rootName + "_shot_edits.dat";
	        shotName = rootName + "_shtstat_edits.dat";
	        rcvName = rootName + "_recstat_edits.dat";
	        sumName = "";
	        description = getFocusDescription();
        } else if (this.getSelectedComponent() == gbuildOutputPane) {
            description = getGeoBuildDescription();
        } else {
            description = getSPSDescription();
        }
		infoLabel.setText("<html>Output files will be: <br>" +
				recordName + " <br>" +
				shotName + " <br>" +
				rcvName + " <br>" +
				sumName + 
				description);
		
	}

	private String getSPSDescription()
    {
	    return
        "<br><br><font size=4>" +
        "Output geometry information into SPS formatted files.<br>" +
        "Any changes made to the geometry using the \"Edit\" tab spreadsheets will be reflected in the output files.<br>" +
        "Any columns created using the \"+\" button will also be output.<br>" +
        "<br>" +
        "<font color=green> These are the files you will want to import into the Focus spreadsheet using the \"Input Textfile\" feature.<br></font>" ;
    }

    private String getFocusDescription()
    {
        return
        "<br><br><font size=4>" +
        "<font color=green>Killed Shots, Shot Points, and Receivers are output as DISCO EDIT jobs. </font><br>" +
        "Shots are edited using the SHOT header.<br>" +
        "Shot Points are edited using the SHTLS header created to combine line and station information.<br>" +
        "Receivers are edited using the RECLS header created to combine line and station information.<br>" +
        "<br>" +
        "Note: This tab may contain options for directly storing geometry information into the Focus database in the future.<br></font>" ;
    }

    private String getGeoBuildDescription()
    {
        return
        "<br><br>" +
        "<font color=green>Select output root filename and click the launch button to begin.</font><br>" +
        "The above \"Output\" files will be used as input for GeoBuild."+
        "<br><br><font size=4>" +
        "GeoBuild is a geometry QC program similar to Mapper3. <br>" +
        "Primarily a command-line program, GeoBuild outputs potential geometry problems to a \"warning\" file. <br>" +
        "Mapper3 is intended to replace GeoBuild, but it is still included for your convenience. <br>" +
        "<br>" +
        "Files created by GeoBuild:<br>" +
        " - receiverparms.dat (parameters for reading the *.rps file) <br>" +
        " - shotparms.dat     (parameters for reading the *.sps file) <br>" +
        " - ob_parms.dat      (parameters for reading the *.xps file) <br>" +
        " - qc_receiver.dat   (file containing the Receiver information collected by GeoBuild) <br>" +
        " - qc_shot.dat       (file containing the Shot Point information collected by GeoBuild) <br>" +
        " - ob_out.xps        (file containing the relation information collected by GeoBuild - <font color=red>WARNING: channels are renumbered as a sequential counter!!</font>) <br>" +
        " - ob_out.sum        (file containing shot record information - one line per shot) <br>" +
        " - *line.dat         (contains DISCO LINE job)<br>" +
        " - *pat.dat          (contains DISCO PATN3D job)<br>" +
        " - *src.dat          (contains DISCO SHOT3D job)<br>" +
        " - *testorvoid.dat   (contains comments from OB file - only valid for RSR and IO type OB files)<br>" +
        " - *warning.dat <font color=green>   (contains geometry QC information and statistics) </font> <br>" +
        "<br>" +
        "Note: As Mapper3 is intended to replace GeoBuild, support for GeoBuild may end in the near future.<br>" +
        "If there is some feature in GeoBuild that you would like to add to Mapper3, please write <font color=blue>scott.cook@tricongeophysics.com</font>.</font>";
    }

    @Override
	public void stateChanged(ChangeEvent e) {
	    Component c = this.getSelectedComponent();
	    if (c == spsOutputPane) {
	        spsOutputPane.removeAll();
	        spsOutputPane = makeSpsPane(spsOutputPane);
	    }
	    else if (c == gbuildOutputPane) {
	        gbuildOutputPane.removeAll();
	        gbuildOutputPane = makeGeoBuildPane(gbuildOutputPane);
	    }
	    else {
	        focusOutputPane.removeAll();
	        focusOutputPane = makeFocusPane(focusOutputPane);
	    }
	    setInfoLabelText(file);
	}

	@Override
	public void fileChanged(String filename)
	{
	    file = filename;
	    setInfoLabelText(file);
	    textField.setText(file);
	}

	/*
    public void resetDefaultFile()
    {
        setInfoLabelText(Mapper.getFile());
        textField.setText(Mapper.getFile());
    }
    */
}
