package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.TableModel;

public class MapperInputFilesPane extends JPanel implements ActionListener {

	private Frame parent;
	
	//private TriconFile file;
	
	protected MapperComboBox comboShotFileKeyList;
	protected MapperComboBox comboReceiverFileKeyList;
	protected MapperComboBox comboObFileKeyList;
	protected ComboBoxModel obFileKeyList;
	protected ComboBoxModel shotFileKeyList;
	protected ComboBoxModel receiverFileKeyList;
	protected JButton obFileKeyButton;
	protected JButton receiverFileKeyButton;
	protected JButton shotFileKeyButton;
	protected JButton obAddFileButton;
	protected JButton receiverAddFileButton;
	protected JButton shotAddFileButton;
	protected JButton obDeleteFileButton;
	protected JButton receiverDeleteFileButton;
	protected JButton shotDeleteFileButton;
	protected JPanel shotBrowsePanel;
	
	public enum DataChanged { Shot, Receiver, OB, All, None };

	private ArrayList<FilesChangedListener> filesChangedListeners;

	private JPanel obBrowsePanel;

	private JPanel receiverBrowsePanel;

	
	public MapperInputFilesPane (final Frame parent) {
		super();
		this.setName("1. Input");
		this.parent = parent; 
		
		filesChangedListeners = new ArrayList<FilesChangedListener>();
		rebuildPane(new ReceiverFileKey[0], new ShotFileKey[0], new OBFileKey[0]);
		
	}


	private void initializeButtons()
    {
	    
        shotFileKeyList.addListDataListener(new ListDataListener(){
            public void intervalAdded(ListDataEvent e) {
                SP.setNumFiles(shotFileKeyList.getSize());
            }
            public void intervalRemoved(ListDataEvent e) {
                SP.setNumFiles(shotFileKeyList.getSize());
            }
            public void contentsChanged(ListDataEvent e) {
                SP.setNumFiles(shotFileKeyList.getSize());
            }                       
        });
        receiverFileKeyList.addListDataListener(new ListDataListener(){
            public void intervalAdded(ListDataEvent e) {
                Receiver.setNumFiles(receiverFileKeyList.getSize());
            }
            public void intervalRemoved(ListDataEvent e) {
                Receiver.setNumFiles(receiverFileKeyList.getSize());
            }
            public void contentsChanged(ListDataEvent e) {
                Receiver.setNumFiles(receiverFileKeyList.getSize());
            }                       
        });
        obFileKeyList.addListDataListener(new ListDataListener(){
            public void intervalAdded(ListDataEvent e) {
                OBRecord.setNumFiles(obFileKeyList.getSize());
            }
            public void intervalRemoved(ListDataEvent e) {
                OBRecord.setNumFiles(obFileKeyList.getSize());
            }
            public void contentsChanged(ListDataEvent e) {
                OBRecord.setNumFiles(obFileKeyList.getSize());
            }                       
        });
        comboShotFileKeyList = new MapperComboBox(shotFileKeyList);
        //comboShotFileKeyList.setPreferredSize(new Dimension(200,20));
        comboReceiverFileKeyList = new MapperComboBox(receiverFileKeyList);
       // comboReceiverFileKeyList.setPreferredSize(new Dimension(200,20));
        comboObFileKeyList = new MapperComboBox(obFileKeyList);
       // comboObFileKeyList.setPreferredSize(new Dimension(200,20));
        
        // make ReceiverFileKey button
        receiverFileKeyButton = new KeyButton();
        receiverFileKeyButton.setEnabled(false); //must hit Add button first!
        receiverFileKeyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                int selectedIndex = comboReceiverFileKeyList.getSelectedIndex();
                ReceiverFileDialogue receiverFileDialogue = 
                    new ReceiverFileDialogue(parent,(ReceiverFileKey)comboReceiverFileKeyList.getSelectedItem());
                receiverFileDialogue.setTitle("Select receiver station column ranges");
                comboReceiverFileKeyList.removeItemAt(selectedIndex);
                comboReceiverFileKeyList.insertItemAt(receiverFileDialogue.showDialogue(),selectedIndex);
                comboReceiverFileKeyList.setSelectedIndex(selectedIndex);
                if (receiverFileDialogue.clickedOK()) {
                    fireFilesChanged(DataChanged.Receiver); //go ahead and show new plot made by file key changes
                }
            }
        }); 
        receiverFileKeyButton.setToolTipText("Set column ranges for reading receiver" + 
        " coordinates");
        
        // make ShotFileKey button
        shotFileKeyButton = new KeyButton();
        shotFileKeyButton.setEnabled(false); //must hit Browse button first!
        shotFileKeyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                int selectedIndex = comboShotFileKeyList.getSelectedIndex();
                ShotFileKeyDialogue shotFileDialogue = 
                    new ShotFileKeyDialogue(parent, (ShotFileKey)(comboShotFileKeyList.getSelectedItem()));
                shotFileDialogue.setTitle("Select shot point column ranges");
                comboShotFileKeyList.removeItemAt(selectedIndex);
                comboShotFileKeyList.insertItemAt(shotFileDialogue.showDialogue(),selectedIndex);
                comboShotFileKeyList.setSelectedIndex(selectedIndex);
                if (shotFileDialogue.clickedOK()) {
                    fireFilesChanged(DataChanged.Shot);
                }
            }
        });
        shotFileKeyButton.setToolTipText("Set column ranges for reading shot point" + 
        " coordinates");
        
        // make OBFileKey button
        obFileKeyButton = new KeyButton();
        obFileKeyButton.setEnabled(false); //must hit Browse button first!
        obFileKeyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                int selectedIndex = comboObFileKeyList.getSelectedIndex();
                OBFileKeyDialogue obFileDialogue = 
                    new OBFileKeyDialogue(parent, (OBFileKey)(comboObFileKeyList.getSelectedItem()));
                obFileDialogue.setTitle("Select OB column ranges");
                comboObFileKeyList.removeItemAt(selectedIndex);
                comboObFileKeyList.insertItemAt(obFileDialogue.showDialogue(),selectedIndex);
                comboObFileKeyList.setSelectedIndex(selectedIndex);
                if (obFileDialogue.clickedOK()) {
                    fireFilesChanged(DataChanged.OB);
                }
            }
        });
        obFileKeyButton.setToolTipText("Set column ranges for reading OB");
        
        // make receiver file add button
        receiverAddFileButton = new PlusButton();
        receiverAddFileButton.addActionListener(this);
        receiverAddFileButton.setActionCommand(DataChanged.Receiver.toString());
        receiverAddFileButton.setToolTipText("Find and Add Receiver Coordinate File");
        
        // make shot file add button
        shotAddFileButton = new PlusButton();
        shotAddFileButton.addActionListener(this);
        shotAddFileButton.setActionCommand(DataChanged.Shot.toString());
        shotAddFileButton.setToolTipText("Find and Add Shot Point Coordinate File");
        
        // make shot record file add button
        obAddFileButton = new PlusButton();
        obAddFileButton.addActionListener(this);
        obAddFileButton.setActionCommand(DataChanged.OB.toString());
        obAddFileButton.setToolTipText("Find and Add OB File");
        
        // make receiver file delete button
        receiverDeleteFileButton = new MinusButton();
        receiverDeleteFileButton.addActionListener(new DeleteButtonListener());
        receiverDeleteFileButton.setActionCommand(DataChanged.Receiver.toString());
        receiverDeleteFileButton.setToolTipText("Remove Receiver Coordinate File");
        receiverDeleteFileButton.setEnabled(false);
        
        // make shot file browse button
        shotDeleteFileButton = new MinusButton();
        shotDeleteFileButton.addActionListener(new DeleteButtonListener());
        shotDeleteFileButton.setActionCommand(DataChanged.Shot.toString());
        shotDeleteFileButton.setToolTipText("Remove Shot Point Coordinate File");
        shotDeleteFileButton.setEnabled(false);
        
        // make ob file browse button
        obDeleteFileButton = new MinusButton();
        obDeleteFileButton.addActionListener(new DeleteButtonListener());
        obDeleteFileButton.setActionCommand(DataChanged.OB.toString());
        obDeleteFileButton.setToolTipText("Remove OB File");
        obDeleteFileButton.setEnabled(false);
    }


    private void setPanels() {
		this.removeAll();
		JPanel background = new JPanel();
		background.setLayout(new BoxLayout(background, BoxLayout.Y_AXIS));
		background.add(receiverBrowsePanel);
		background.add(shotBrowsePanel);
		background.add(obBrowsePanel);
		background.add(getWorkflowLabel());
		background.add(Box.createVerticalGlue());
		this.setLayout(new BorderLayout());
		this.add(background, BorderLayout.CENTER);
	}


	private Component getWorkflowLabel()
    {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(JLabel.LEFT);
        String plusPath = "";
        String runPath = "";
        String reloadPath = "";
        try {
            plusPath = MapperInputFilesPane.class.getResource("docs/plus.gif").toExternalForm();
            runPath = MapperInputFilesPane.class.getResource("docs/run_button.png").toExternalForm();
            reloadPath = MapperInputFilesPane.class.getResource("docs/reload.png").toExternalForm();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String text = "";
        text += "<html><font size=6>Workflow<br></font><font size=4>"+
        "<ol>" +
        "<li><font size=5>Input</li>" +
        "<ul>" +
        "<li>Add <font color=green>Receiver</font>, <font color=red>Shot Point</font>, and " +
        "<font color=blue>Relation</font> files by clicking the <img src=\""+plusPath+"\"> buttons.</li>" +
        "<li>Files should be SPS format compatible (column separated).</li>" +
        "</ul>"+
        "<li><font size=5>Edit Tab</li>" +
        "<ol>" +
        "<li><b>Run QC (first pass)</b></li>" +
        "Click <img src=\""+runPath+"\"> to start (leave the \"Interpolate UnSurveyed Stations\" box unchecked)."+
        "<li><b><font color=blue>Relation</font></b></li>" +
        "Fix \"DuplicateShotPoint\" column by either killing Void/Test shots or fixing incorrect Line/Station numbers." +
        "<li><b>Click <img src=\""+reloadPath+"\"> button for both <font color=green>Receiver</font> and <font color=red>Shot Point</font> tabs</b>" +
        " to remove any superfluous UnSurveyed stations added by Void/Test Shots.</li>" +
        "<li><b>Run QC (second pass)</b></li>" +
        "Click \"Interpolate UnSurveyed Stations\" checkbox. Now that the <font color=blue>Relation</font> spreadsheet is fixed, all UnSurveyed stations are real and should be interpolated." +
        "<li><b>Fix <font color=green>Receiver</font> and <font color=red>Shot Point</font> spreadsheets.</b></li>" +
        "<ul>" +
        "<li>Kill unecessary Duplicate stations.</li>" +
        "<li>Check positions of UnSurveyed stations in Map to make sure interpolated/extrapolated positions are correct." +
        "<li>QC Geometry by viewing the Kill, UnUsed, Duplicate, UnSurveyed, etc., columns in the Map (use the \"Display:\" combo-box to do this).</li>" +
        "</ul>" +
        "<li><b>Run QC (final pass)</b></li>" +
        "You may want to keep running this until DuplicateShotPoint, DuplicateShot, UnUsed, and Surveyed-Twice totals are zero." +
        "</ol>" +
        "<li><font size=5>Output</li>" +
        "<ol>" +
        "<li>SPS Tab (Output geometry for input to Focus).</li>" +
        "<ol>" +
        "<li>Browse to select output file root name.</li>" +
        "<li>Click \"Save\".</li>" +
        "</ol>" +
        "<li>Focus Tab (Output Shot & Station kills to Focus).</li>" +
        "<ol>" +
        "<li>Browse to select output file root name.</li>" +
        "<li>Click \"Save\".</li>" +
        "</ol>";
        
        label.setText(text);
       // JPanel panel = new JPanel();
       // panel.add(label);
        JScrollPane scroller = new JScrollPane(label);
       // scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroller;
    }


    protected void fireFilesChanged(DataChanged dataChanged) {
		for (FilesChangedListener listener: filesChangedListeners) {
			listener.inputFilesChanged(dataChanged);
		}
	}


	public void updateKeyLists() {
		//If combo file key lists have changed, reset plot and buttons
		if (comboReceiverFileKeyList.getItemCount()>0) {
			comboReceiverFileKeyList.setSelectedIndex(comboReceiverFileKeyList.getItemCount()-1);
			receiverFileKeyButton.setEnabled(true);
			receiverDeleteFileButton.setEnabled(true);
		}
		if (comboShotFileKeyList.getItemCount()>0) {
			comboShotFileKeyList.setSelectedIndex(comboShotFileKeyList.getItemCount()-1);
			shotFileKeyButton.setEnabled(true);
			shotDeleteFileButton.setEnabled(true);
		}
		if (comboObFileKeyList.getItemCount()>0) {
			comboObFileKeyList.setSelectedIndex(comboObFileKeyList.getItemCount()-1);
			obFileKeyButton.setEnabled(true);
			obDeleteFileButton.setEnabled(true);
		}
		
	}


	public int getReceiverFileCount() {
		return comboReceiverFileKeyList.getItemCount();
	}


	public int getShotFileCount() {
		return comboShotFileKeyList.getItemCount();
	}


	public int getObFileCount() {
		return comboObFileKeyList.getItemCount();
	}
	
	public class DeleteButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			JComboBox fileKeyList;
			JButton deleteButton;
			JButton keyButton;
			DataChanged dataChanged = DataChanged.valueOf(ev.getActionCommand());
			if (dataChanged == DataChanged.Receiver) {
				fileKeyList = comboReceiverFileKeyList;
				deleteButton = receiverDeleteFileButton;
				keyButton = receiverFileKeyButton;
			}
			else if (dataChanged == DataChanged.Shot) {
				fileKeyList = comboShotFileKeyList;
				deleteButton = shotDeleteFileButton;
				keyButton = shotFileKeyButton;
			}
			else {
				fileKeyList = comboObFileKeyList;
				deleteButton = obDeleteFileButton;
				keyButton = obFileKeyButton;
			}
			
			if (fileKeyList.getItemCount() > 0) {
				fileKeyList.removeItemAt(fileKeyList.getSelectedIndex());
				fireFilesChanged(dataChanged); //reset plot to show data from remaining files
			}
			if (fileKeyList.getItemCount() == 0) {
				deleteButton.setEnabled(false);
				keyButton.setEnabled(false);
				//fireFilesChanged(); //set plot to blank
			}
		}
	}

	/*
	protected ArrayList<TableData> loadStations(JComboBox fileKeyList) {
		ArrayList<TableData> sList = new ArrayList<TableData>();
		String line = null;
		int i;
		for (i = 0; i < fileKeyList.getItemCount(); i++) {
			FileKey fileKey = (FileKey) fileKeyList.getItemAt(i);
			int linesRead = -1;
			if (new File(fileKey.getInputFile()).exists()) {
				try {
					//Cursor oldCursor = getCursor();
					//setCursor(new Cursor(Cursor.WAIT_CURSOR));
					BufferedReader reader = new BufferedReader(new FileReader(
							fileKey.getInputFile()));
					while ((line = reader.readLine()) != null) {
						linesRead++;
						if ((linesRead >= fileKey.getFirstLine())
								&& (linesRead <= fileKey.getLastLine())) {
							Station thisStation = fileKey.decipherLine(line);
							if (thisStation != null) {
							    thisStation.setSurvey(i+1);
								sList.add(thisStation);
							}
						}
						//setCursor(oldCursor);
					}
				} catch (Exception ex) {
					System.out.println("couldn't read the xy file"
							+ fileKey.getInputFile());
					ex.printStackTrace();
				}
			}
		}
		return sList;
	}
	 */

	/*
	protected ArrayList<TableData> loadOBs(JComboBox fileKeyList) {
		ArrayList<TableData> obList = new ArrayList<TableData>();
		obList.ensureCapacity(5000);
		int i;
		for (i = 0; i < fileKeyList.getItemCount(); i++) {
			OBFileKey obFileKey = (OBFileKey) fileKeyList.getItemAt(i);
			int linesRead = -1;
			if (obFileKey.getInputFile().exists()) {
				try {
					//Cursor oldCursor = getCursor();
					//setCursor(new Cursor(Cursor.WAIT_CURSOR));
					String line = new String();
					line = null;
					BufferedReader reader = new BufferedReader(new FileReader(
							obFileKey.getInputFile()));
					while ((line = reader.readLine()) != null) {
						linesRead++;
						if ((linesRead >= obFileKey.getFirstLine())
								&& (linesRead <= obFileKey.getLastLine())) {
							OBRecord obr = obFileKey.decipherLineNewShot(line); // 1st
																				// try
																				// new
																				// shot
							if (obList.size() > 0
									&& (obr != null)
									&& (obr.getFfid() == ((OBRecord) obList
											.get(obList.size() - 1)).getFfid())) {// if
																					// not
																					// new
																					// shot,
																					// add
																					// more
																					// spread
								obr = obFileKey.decipherLineMoreSpread(line,
										(OBRecord) obList
												.get(obList.size() - 1));
								obList.remove(obList.size() - 1);
								obList.add(obr);
							} else if (obr != null)
								obList.add(obr);
						}
					}
					reader.close();
					//setCursor(oldCursor);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		obList.trimToSize(); // get rid of any unused elements that may be there
		return obList;
	}
	*/

	public String getReceiverFileName(int ii) {
		return receiverFileKeyList.getElementAt(ii).toString();
	}

	public String getShotFileName(int ii) {
		return shotFileKeyList.getElementAt(ii).toString();
	}


	public String getObFileName(int ii) {
		return obFileKeyList.getElementAt(ii).toString();
	}


	public ShotFileKey getShotFileKey(int ii) {
		return (ShotFileKey) shotFileKeyList.getElementAt(ii);
	}

	public ReceiverFileKey getReceiverFileKey(int ii) {
		return (ReceiverFileKey) receiverFileKeyList.getElementAt(ii);
	}


	public OBFileKey getObFileKey(int ii) {
		return (OBFileKey) obFileKeyList.getElementAt(ii);
	}
	
	// read Project file keys from text file
	protected void recoverProject(File f) {
		String lines = null;

		// read file key information from text file
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line = new String();
			line = null;
			while ((line = reader.readLine()) != null) {
				lines = lines + line.split("%")[0] + "%"; // only take text
															// before the "%"
															// sign, the add
															// another one
			}
			reader.close();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Couldn't read Project File:"
					+ f, "File Read Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}

		// create new file keys from text
		if (lines != null) {
			// Parse input text by receiver and shot information.
			// When parsed by ShotFileKey.KEY_DESCRIPTION, strings will begin
			// with shot information (except
			// for the first string, which is ignored)
			String[] shotParsedLines = lines.split(ShotFileKey.KEY_DESCRIPTION);
			if (shotParsedLines.length > 0)
				comboShotFileKeyList.removeAllItems();
			// When parsed by ReceiverFileKey.KEY_DESCRIPTION, strings will
			// begin with receiver information (except
			// for the first string, which is ignored)
			String[] receiverParsedLines = lines
					.split(ReceiverFileKey.KEY_DESCRIPTION);
			if (receiverParsedLines.length > 0)
				comboReceiverFileKeyList.removeAllItems();
			// When parsed by ReceiverFileKey.KEY_DESCRIPTION, strings will
			// begin with receiver information (except
			// for the first string, which is ignored)
			String[] obParsedLines = lines.split(OBFileKey.KEY_DESCRIPTION);
			if (obParsedLines.length > 0)
				comboObFileKeyList.removeAllItems();

			if (!((shotParsedLines.length > 0)
					&& (receiverParsedLines.length > 0) && (obParsedLines.length > 0)))
				JOptionPane.showMessageDialog(this,
						"No Shot/Receiver/OB information found in file:" + f,
						"File Read Error", JOptionPane.ERROR_MESSAGE);

			// new file keys can be created by parsing text by "%" (handled
			// within file key objects)
			for (int i = 1; i < shotParsedLines.length; i++)
				comboShotFileKeyList.insertItemAt(new ShotFileKey()
						.setFileKeyFromString(shotParsedLines[i]), i - 1);
			for (int i = 1; i < receiverParsedLines.length; i++)
				comboReceiverFileKeyList.insertItemAt(new ReceiverFileKey()
						.setFileKeyFromString(receiverParsedLines[i]), i - 1);
			for (int i = 1; i < obParsedLines.length; i++)
				comboObFileKeyList.insertItemAt(new OBFileKey()
						.setFileKeyFromString(obParsedLines[i]), i - 1);
		}
	}

	/*
	public ArrayList<TableData> loadReceivers() {
		return loadStations(comboReceiverFileKeyList);
	}


	public ArrayList<TableData> loadSPs() {
		return loadStations(comboShotFileKeyList);
	}

	public ArrayList<TableData> loadOBs() {
		return loadOBs(comboObFileKeyList);
	}
	 */


	public void addFilesChangedListener(FilesChangedListener listener) {
	    filesChangedListeners.add(listener);
	}

	class MapperComboBox extends JComboBox { // same as JComboBox except ToolTip
		// shows currently selected File
		// Path
		MapperComboBox(ComboBoxModel cbm) {
			super(cbm);
		}
		
		MapperComboBox() {
			super();
		}

		public MapperComboBox(Object[] fileKeys) {
			this(new DefaultComboBoxModel(fileKeys));
		}

		public void setSelectedItem(Object o) {
			super.setSelectedItem(o);
			super.setToolTipText(o.toString());
		}
	}
	
	public MapperComboBox getComboShotFileKeyList() {
		return comboShotFileKeyList;
	}

	public MapperComboBox getComboReceiverFileKeyList() {
		return comboReceiverFileKeyList;
	}

	public MapperComboBox getComboObFileKeyList() {
		return comboObFileKeyList;
	}
	public void rebuildPane(FileKey[] receiverKeys,
			FileKey[] shotKeys,
			OBFileKey[] obFileKeys) {
	    receiverFileKeyList = new DefaultComboBoxModel(receiverKeys);
        shotFileKeyList = new DefaultComboBoxModel(shotKeys);
        obFileKeyList = new DefaultComboBoxModel(obFileKeys);
        initializeButtons();
		JPanel panel = createShotBrowsePanel(new MapperComboBox(shotFileKeyList));
		if (panel != null)
			shotBrowsePanel = panel;
		panel = createReceiverBrowsePanel(new MapperComboBox(receiverFileKeyList));
		if (panel != null)
			receiverBrowsePanel = panel;
		panel = createObBrowsePanel(new MapperComboBox(obFileKeyList));
		if (panel != null)
			obBrowsePanel = panel;
		this.setPanels();
	}


	private JPanel createObBrowsePanel(MapperComboBox mcb) {
		if (mcb == null) return null;
		return createBrowsePanel(mcb, obAddFileButton, obDeleteFileButton, obFileKeyButton, Color.blue, "Relation Files");
	}


	private JPanel createReceiverBrowsePanel(MapperComboBox mcb) {
		if (mcb == null) return null;
		return createBrowsePanel(mcb, receiverAddFileButton, receiverDeleteFileButton, receiverFileKeyButton, Color.GREEN.darker(), "Receiver Files");
	}


	private JPanel createShotBrowsePanel(MapperComboBox mcb) 
	{
		if (mcb == null) return null;
		return createBrowsePanel(mcb, shotAddFileButton, shotDeleteFileButton, shotFileKeyButton, Color.red, "Shot Point Files");
	}


	private JPanel createBrowsePanel(MapperComboBox comboFileKeyList, JButton addFileButton, JButton deleteFileButton, JButton fileKeyButton, Color color, String title) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(comboFileKeyList);
		panel.add(addFileButton);
		panel.add(deleteFileButton);
		panel.add(fileKeyButton);
		panel.
		setBorder(BorderFactory.
				createTitledBorder(BorderFactory.createLineBorder(color),
						title,
						TitledBorder.DEFAULT_JUSTIFICATION,
						TitledBorder.DEFAULT_POSITION,
						new Font("lucida",Font.BOLD,12),
						color));
		panel.setPreferredSize(new Dimension(700,50));
		if (comboFileKeyList.getItemCount() > 0) {
		    deleteFileButton.setEnabled(true);
		    fileKeyButton.setEnabled(true);
		}
		return panel;
	}


	public OBFileKey[] getObFileKeys() {
		ArrayList<OBFileKey> keys = new ArrayList<OBFileKey>();
		ComboBoxModel model = comboObFileKeyList.getModel();
		for (int i=0; i<model.getSize(); i++)
			keys.add((OBFileKey) model.getElementAt(i));
		return keys.toArray(new OBFileKey[]{});
	}


	public FileKey[] getReceiverFileKeys() {
		ArrayList<FileKey> keys = new ArrayList<FileKey>();
		ComboBoxModel model = comboReceiverFileKeyList.getModel();
		for (int i=0; i<model.getSize(); i++)
			keys.add((FileKey) model.getElementAt(i));
		return keys.toArray(new FileKey[]{});
	}


	public FileKey[] getShotFileKeys() {
		ArrayList<FileKey> keys = new ArrayList<FileKey>();
		ComboBoxModel model = comboShotFileKeyList.getModel();
		for (int i=0; i<model.getSize(); i++)
			keys.add((FileKey) model.getElementAt(i));
		return keys.toArray(new FileKey[]{});
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
        JButton keyButton;
        JButton deleteButton;
        String title;
        DataChanged dataChanged = DataChanged.valueOf(ev.getActionCommand());
//      receiverFile = ((FileKey)(comboReceiverFileKeyList.getSelectedItem())).getInputFile();
//      shotFile = ((FileKey)(comboShotFileKeyList.getSelectedItem())).getInputFile();
        //set references so that they change the right object
        if (dataChanged == DataChanged.Receiver) {
            keyButton = receiverFileKeyButton;
            deleteButton = receiverDeleteFileButton;
            title = "Select Receiver Coordinate File";
        } else if (dataChanged == DataChanged.Shot){
            keyButton = shotFileKeyButton;
            deleteButton = shotDeleteFileButton;
            title = "Select Shot Point Coordinate File";
        } else {
            keyButton = obFileKeyButton;
            deleteButton = obDeleteFileButton;
            title = "Select OB File";
        }
        
        String file2 = TriconFileChooser.launchFilteredFileChooser(Mapper.getFile(), this, "Add");
        if (file2 == null) return;
        Mapper.setFile(file2);
        TriconFile file = new TriconFile(file2);
        // get file from user, assign to file reference if valid
        if (file.exists()){
            //we have valid file- save it,show it, and enable key button
            keyButton.setEnabled(true);
            deleteButton.setEnabled(true);
            if (dataChanged == DataChanged.Receiver) {
                comboReceiverFileKeyList.addItem(new ReceiverFileKey(file));
                comboReceiverFileKeyList.setSelectedIndex(comboReceiverFileKeyList.getItemCount()-1);
                receiverFileKeyButton.doClick(); //go directly to File Key window for file just added
            }
            else if (dataChanged == DataChanged.Shot){
                comboShotFileKeyList.addItem(new ShotFileKey(file));
                comboShotFileKeyList.setSelectedIndex(comboShotFileKeyList.getItemCount()-1);
                shotFileKeyButton.doClick(); //go directly to File Key window for file just added
            }
            else if (dataChanged == DataChanged.OB){
                comboObFileKeyList.addItem(new OBFileKey(file));
                comboObFileKeyList.setSelectedIndex(comboObFileKeyList.getItemCount()-1);
                obFileKeyButton.doClick(); //go directly to File Key window for file just added
            }
        } else { 
            keyButton.setEnabled(false);
        }
    }


    public void resetNumberInputFiles()
    {
        Receiver.setNumFiles(receiverFileKeyList.getSize());
        SP.setNumFiles(shotFileKeyList.getSize());
        OBRecord.setNumFiles(obFileKeyList.getSize());
    }
}
