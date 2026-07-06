package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.DocumentEvent;

public class MainWindow extends JFrame implements ParameterValueChangedListener {
	private JPanel background;
	private JPanel foreground;
	private ParameterPanel parameterPanel;
	private NodeSelectionPanel nodeSelectionPanel;
	private JobStatusPanel jobStatusPanel;
	private ParmFileConverter converter;
	private JTabbedPane tabbedPane;
	//private SpawnProcess spawnProcess;
	private String parmFileName;
	private String[] commands;
	private boolean parameterValueChanged = false;
	private String mainTitle;
	private MessageLabel messageLabel;
	private String nodeListFileName;
	
	public final static String TriconLogoFile = "/apdata/rtmig/sc2dmod/tricon_logo.gif";
	
	public MainWindow(ArrayList<Parameter> parmList, String title, ParmFileConverter converter, String[] commands) {
		super();		
		parameterPanel = new ParameterPanel();
		parameterPanel.setParameterList(parmList);
		parameterPanel.addParameterValueChangedListener(this);
		
		nodeSelectionPanel = new NodeSelectionPanel(new String[]{"default node"});
		
		//jobStatusPanel = new JobStatusPanel();
		
		tabbedPane = new JTabbedPane();
		
		messageLabel = new MessageLabel("File Not Loaded");
		
		this.converter = converter;
		this.setTitle(title);
		mainTitle = title;
		this.commands = commands;
	}
	
	public void showMain() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		background = new JPanel(new BorderLayout());
		//background.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		foreground = new JPanel();
		foreground.setLayout(new BoxLayout(foreground,BoxLayout.Y_AXIS));
		foreground.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		//Make menu bar and add
		this.setJMenuBar(makeMenuBar());
		
		//Make logo pane
		foreground.add(makeLogoPane());
		
		//Make Tabbed Pane
		tabbedPane.add("Parameters", parameterPanel);
		tabbedPane.add("Nodes", nodeSelectionPanel);
		tabbedPane.add("Jobs", jobStatusPanel);
		foreground.add(tabbedPane);		
		
		foreground.add(new JLabel(" ")); //add blank space
		
		foreground.add(messageLabel);
		
		//set so if window gets bigger, panel doesn't change. if smaller engage scrollbars
		JPanel panelLockEW = new JPanel(new BorderLayout());
		panelLockEW.add(foreground, BorderLayout.WEST);
		background.add(panelLockEW, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane(background);
		scrollPane.setPreferredSize(new Dimension(background.getPreferredSize().width+5, background.getPreferredSize().height+5)); 
		getContentPane().add(scrollPane);
		pack();
		setVisible(true);
	}
	


	private Component makeLogoPane() {
		JPanel logoPane = new JPanel();
		ImageIcon logo = new ImageIcon(TriconLogoFile);
		logoPane.add(new JLabel(logo));
		logoPane.setBorder(BorderFactory.createEtchedBorder());
		//logoPane.setPreferredSize(new Dimension(ParameterField.PanelWidth,logo.getIconHeight()));
		return logoPane;
	}

	private JMenuBar makeMenuBar() {
		//make menu bar
		JMenuBar menuBar = new JMenuBar();
		
		//add File menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		
		//put items in File Menu - (open item)
		JMenuItem openItem = new JMenuItem("Open Parm File");
		openItem.setMnemonic(KeyEvent.VK_O);
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow.this.lauchOpenDialog();
			}
			
		});
		fileMenu.add(openItem);
		
		//put items in File Menu - (save item)
		JMenuItem saveItem = new JMenuItem("Save Parm File");
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow.this.launchSaveDialog();
			}
			
		});
		fileMenu.add(saveItem);
		
		fileMenu.addSeparator();
		
		//put items in File Menu - (open node list)
		JMenuItem openNodeList = new JMenuItem("Open Node List");
		openNodeList.setMnemonic(KeyEvent.VK_P);
		openNodeList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow.this.lauchOpenNodeListDialog();
			}
			
		});
		fileMenu.add(openNodeList);
		
		//put items in File Menu - (save node list)
		JMenuItem saveNodeList = new JMenuItem("Save Node List");
		saveNodeList.setMnemonic(KeyEvent.VK_A);
		saveNodeList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow.this.lauchSaveNodeListDialog();
			}
			
		});
		fileMenu.add(saveNodeList);
		
		fileMenu.addSeparator();
		
		//add Exit Item to File Menu
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setMnemonic(KeyEvent.VK_X);
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (parameterValueChanged) MainWindow.this.launchSaveYesNoDialog();
				System.exit(EXIT_ON_CLOSE);
			}
			
		});
		fileMenu.add(exitItem);
		
		//***RUN Menu***
		JMenu runMenu = new JMenu("Run");
		fileMenu.setMnemonic(KeyEvent.VK_R);
		
		//put items in Run Menu - (run item)
		JMenuItem runItem = new JMenuItem("Run");
		runItem.setMnemonic(KeyEvent.VK_R);
		runItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ( parameterValueChanged ) MainWindow.this.launchSaveYesNoDialog();
				MainWindow.this.launchRunDialog();
			}
			
		});
		runMenu.add(runItem);
		menuBar.add(runMenu);
		
		return menuBar;
	}

	protected void lauchSaveNodeListDialog() {
		setNodeListFileName(TriconFileChooser.launchFilteredFileChooser(NodeSelectionPanel.getFileExtensions(), MainWindow.this, "Save"));
		if (nodeListFileName !=null) {
			try {
				nodeSelectionPanel.SaveNodeListToFile(nodeListFileName);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(MainWindow.this,
						"Can't write to file: "+nodeListFileName+".\n message: "+e1.toString(),
						"File Save Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	protected void lauchOpenNodeListDialog() {
		setNodeListFileName(TriconFileChooser.launchFilteredFileChooser(NodeSelectionPanel.getFileExtensions(), MainWindow.this));
		if (nodeListFileName !=null) {
			try {
				nodeSelectionPanel.LoadNodeListFromFile(nodeListFileName);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(MainWindow.this,
						"Can't read from file: "+nodeListFileName+".\n message: "+e1.toString(),
						"File Open Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void setNodeListFileName(String fileName) {
		this.nodeListFileName = fileName;
	}

	protected void launchSaveYesNoDialog() {
		int n = JOptionPane.showConfirmDialog(
			    this,
			    "Parameter values have changed!\nWould you like to save?",
			    "Save Parameters",
			    JOptionPane.YES_NO_OPTION);
		if (n==JOptionPane.YES_OPTION) {
			this.launchSaveDialog();
		}
	}

	protected void launchRunDialog() {
		if (MainWindow.this.parmFileName == null) {
			JOptionPane.showMessageDialog(this, "Must Save Parm File Before Running!!",
					"Job Execution Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		SpawnProcess spawnProcess = new SpawnProcess(commands[0]);
		ArrayList<String> comandList = new ArrayList<String>();
		for (int i=0;i<commands.length;i++) {
			comandList.add(commands[i]);
		}
		comandList.add(parmFileName);
		spawnProcess.showDialog();
		spawnProcess.runProcess(comandList, new File(parmFileName).getParentFile());
	}

	protected void lauchOpenDialog() {
		setParmFileName(TriconFileChooser.launchFilteredFileChooser(Parameter.getFileExtensions(), MainWindow.this));
		if (parmFileName !=null) {
			try {
				parameterPanel.LoadParameterValuesFromFile(parmFileName, converter);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(MainWindow.this,
						"Can't read from file: "+parmFileName+".\n message: "+e1.toString(),
						"File Open Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		MainWindow.this.setParameterValueChanged(false);
	}

	protected void launchSaveDialog() {
		setParmFileName(TriconFileChooser.launchFilteredFileChooser(Parameter.getFileExtensions(), MainWindow.this, "Save"));
		if (parmFileName !=null) {
			try {
				parameterPanel.SaveParameterValuesToFile(parmFileName, converter);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(MainWindow.this,
					    "Can't write to file: "+parmFileName+".\n message: "+e1.toString(),
					    "File Save Error",
					    JOptionPane.ERROR_MESSAGE);
			}
		}
		MainWindow.this.setParameterValueChanged(false);
	}

	public ParameterPanel getParameterPanel() {
		return parameterPanel;
	}

	public void setParameterPanel(ParameterPanel parameterPanel) {
		this.parameterPanel = parameterPanel;
	}

	public static void main(String[] args) {
		//MainWindow mw = new MainWindow();
		//mw.showMain();
	}

	public boolean isParameterValueChanged() {
		return parameterValueChanged;
	}

	public void setParameterValueChanged(boolean parameterValueChanged) {
		this.parameterValueChanged = parameterValueChanged;
		if (parameterValueChanged) {
			this.setTitle(mainTitle+"  ************Changed************");
		} else {
			this.setTitle(mainTitle);
		}
	}
	
	/**
	 * This is how main window knows when user has changed a parameter value
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		setParameterValueChanged(true);
		System.out.println("MainWindow: parameter change detected");
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		actionPerformed(null);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		actionPerformed(null);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		actionPerformed(null);
	}
	
	private class MessageLabel extends JPanel {
		String message;
		JLabel label;
		
		public MessageLabel(String message) {
			this.message = message;
			label = new JLabel(message);
			this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
			this.add(label);
			this.setBorder(BorderFactory.createEtchedBorder());
			//label.setBorder(BorderFactory.createEmptyBorder());
			this.add(Box.createHorizontalGlue()); //keep text on the left instead of centering
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
			label.setText(message);
		}
		
	}

	public String getParmFileName() {
		return parmFileName;
	}

	public void setParmFileName(String parmFileName) {
		this.parmFileName = parmFileName;
		this.messageLabel.setMessage(parmFileName);
	}
}
