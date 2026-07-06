package com.tricongeophysics;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;

import javax.swing.event.*;

import com.tricongeophysics.FileKey.Key;

import java.io.*;
import java.nio.CharBuffer;
import java.util.ArrayList;


public abstract class FileKeyDialogue extends JDialog {

    //Defaults are set to SPSOUT standard
	protected static String[] BUTTON_LABELS = {"First Line","Last Line","Key","Line","Station","X","Y","Z"};
	protected FileKey fileKey;
	//protected JToggleButton[] buttons;
	//protected JTextField[] textFields;
	protected int caretColumn1=0;
	protected int caretColumn2=0;
	protected int caretRow=0;
	protected JLabel rowColLabel;
	protected JTextArea textArea;
	protected ButtonGroup bGroup; //button toggle group
	protected MyButtonListener mbl;
	protected int nLinesRead=0; //counter for automatically setting last line number
    private boolean clickedOK = false;
    protected FieldGroup[] fieldGroups;
    private PlusButton optionalColumnButton;
    private Box columnButtons;
    protected JButton okButton;
    
    private ArrayList<OptionalColumnGroup> optionalColumnGroups = new ArrayList<OptionalColumnGroup>();
	
	public FileKeyDialogue(FileKey fk, Frame parent){
		super(parent);
		setFileKey(fk);
		this.setModal(true);
	}
	
	public FileKeyDialogue(Frame parent, FileKey fk){
		this(fk, parent);
	}
	
	public void setFileKey(FileKey fk){
		fileKey = fk;
	}
	
	public FileKey showDialogue() {
	    if (fileKey == null) return null;
		JPanel background = new JPanel(new BorderLayout());
		background.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		//Make Row & Column label
		rowColLabel = new JLabel("row:"+caretRow+" from column:"
				+caretColumn1+" to column:"
				+caretColumn2);
		
		//Make file text area
		textArea = new JTextArea(10,20);
		readFile(new TriconFile(fileKey.getInputFile()));
		textArea.setCaretPosition(0);//start user at beginning of file
		textArea.addCaretListener(new MyCaretListener());
		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		textArea.setEditable(false);
		
		//make text fields(2 fields/button except for 1st 2 buttons (first/last row buttons))
		fieldGroups = new FieldGroup[BUTTON_LABELS.length];
		fieldGroups[0] = new SingleFieldGroup(BUTTON_LABELS[0]);
		fieldGroups[0].addActionListener(mbl);
		fieldGroups[1] = new SingleFieldGroup(BUTTON_LABELS[1]);
		fieldGroups[1].addActionListener(mbl);
		fieldGroups[2] = new CharacterKeyGroup(BUTTON_LABELS[2]);
        fieldGroups[2].addActionListener(mbl);
		for (int i=3;i<fieldGroups.length;i++) {
		    fieldGroups[i] = new ColumnRangeGroup(BUTTON_LABELS[i]);
		    fieldGroups[i].addActionListener(mbl);
		}
		
		fieldGroups[0].setSelected(true); //start user at "First Line" button
		
		//set Last Line button to default to total lines when selected
		fieldGroups[1].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				fileKey.setLastLine(nLinesRead);
				setTextFields();
			}
		});		
		
		//make first/last button display
		Box firstLast1 = new Box(BoxLayout.Y_AXIS);
		//firstLast1.setBorder(BorderFactory.createTitledBorder("Select First/Last Row"));
		firstLast1.add(new JPanel().add(fieldGroups[0]));
		firstLast1.add(fieldGroups[1]);
		//firstLast1.add(Box.createVerticalGlue());
		JPanel firstLast = new JPanel();
		firstLast.add(firstLast1);
		firstLast.setBorder(BorderFactory.createTitledBorder("Select First/Last Row"));
		
		//make character key check box
        JCheckBox charKeyBox = new JCheckBox();
        charKeyBox.setSelected(fileKey.getUseCharKey());
        fieldGroups[2].setEnabled(fileKey.getUseCharKey());
        charKeyBox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    fileKey.setUseCharKey(false);
                    fieldGroups[2].setEnabled(false);
                }
                else {
                    fileKey.setUseCharKey(true);
                    fieldGroups[2].setEnabled(true);
                }
            }
        });
        
		//make character key button display
		JPanel charKeyPanel = new JPanel();
		fieldGroups[2].setToolTipText(
				"Set cursor mode to select column and character of Character Key");
		charKeyPanel.add(fieldGroups[2]);
		charKeyPanel.add(charKeyBox);
        charKeyPanel.setBorder(BorderFactory.createTitledBorder("Character Key"));
		
		//make column buttons display
		columnButtons = new Box(BoxLayout.X_AXIS);
		JPanel columnButtons1 = new JPanel();
        columnButtons1 .setBorder(BorderFactory.createTitledBorder("Select column ranges for reading"+
		" coordinates"));
		for (int i=3; i<fieldGroups.length; i++) {
		    columnButtons.add(fieldGroups[i]);
		    columnButtons.add(Box.createHorizontalStrut(3));
		}
		
		//set button group
        bGroup = new ButtonGroup();
        for (FieldGroup fg: fieldGroups) {
            bGroup.add(fg.getButton());
        }
		
		//make optional column button
		optionalColumnButton = new PlusButton();
		optionalColumnButton.setToolTipText("Add Optional Column");
		optionalColumnButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                String name = JOptionPane.showInputDialog("Name of New Column:");
                if (name == null) return;
                FileKey.Key newKey = fileKey.addOptionalFileKey(name);
                addOptionalColumn(newKey);
                FileKeyDialogue.this.pack();
            }});
		columnButtons1.add(columnButtons);
		initializeOptionalColumns();
		columnButtons1.add(optionalColumnButton);
		//columnButtons.add(optionalColumnButton);
		
		//make OK button
		okButton = new JButton("OK");
		okButton.setToolTipText("Use current column/row selection settings");
		okButton.setForeground(Color.GREEN.darker().darker());
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			    clickedOK = true;
				setKeyFields(); //grab all user input and get outta here!!
				setVisible(false);
				dispose();
			}
		});
		okButton.setFont(new Font("arial",Font.BOLD,14));
		
		//make line add text fields
        Component lineAddPanel = createLineAddPanel();
		
		//add text area to scroll pane
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(600,600));
		
		//set defaults
		setTextFields();
		
		//put it all together
		Box buttonPanel = new Box(BoxLayout.X_AXIS);
		buttonPanel.add(firstLast);
		buttonPanel.add(charKeyPanel);
		buttonPanel.add(columnButtons1);
		JPanel okLinePanel = new JPanel(new BorderLayout());
		okLinePanel.add(lineAddPanel, BorderLayout.CENTER);
		okLinePanel.setBorder(BorderFactory.createTitledBorder("Done"));
		//okBox.add(Box.createVerticalGlue());
		//JPanel okPanel = new JPanel();
		//okPanel.add(okButton);
		//okLinePanel.add( okPanel, BorderLayout.NORTH);
		buttonPanel.add(okLinePanel);
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		JPanel top = new JPanel(new BorderLayout());
		top.add(buttonPanel,BorderLayout.WEST);
		top.add(new JLabel("Filename:"+fileKey.getInputFile()),BorderLayout.PAGE_END);
		background.add(top,BorderLayout.NORTH);
		background.add(scrollPane,BorderLayout.CENTER);
		background.add(rowColLabel,BorderLayout.SOUTH);
		getContentPane().add(background);
		//setSize(800,800);
		pack();
		setVisible(true);
		
		return fileKey;
		
	} // end gshowDialogue
	
	public abstract Component createLineAddPanel();

    private void initializeOptionalColumns()
    {
        ArrayList<Key> keys = fileKey.getOptionalFileKeys();
        if (keys == null) return;
        for (Key key: keys) {
            addOptionalColumn(key);
        }
    }

    protected void addOptionalColumn(Key key)
    {
	    FieldGroup group = findSelectedGroup(key.getName());
	    if (group != null) return; //already exists!!! don't make any more with this name
        OptionalColumnGroup ocg = new OptionalColumnGroup(key);
        optionalColumnGroups.add(ocg);
        columnButtons.add(ocg);
        bGroup.add(ocg.getButton());
        bGroup.setSelected(ocg.getButton().getModel(), true);
    }

    
	
	public class MyButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			//update fileKey whenever action is performed
			setKeyFields();	
		}
	}
	
	public void readFile(File file) {
		//		Read text file and insert into text area
		int lineCountX = 0;
		char[] cbuf = new char[1000000];
		String t="";
		try {
			//			 BufferedReader reader = new BufferedReader(new FileReader(fileKey.getInputFile()));
			//			 textArea.read(reader,fileKey.getInputFile());
			BufferedReader reader = new BufferedReader(new FileReader(fileKey.getInputFile()));
			int chunks = 0; //number of chunks read
			int chars = 1; //number of characters read
			while (chars > 0) {
				chunks++;
				chars = reader.read(cbuf);  ///read from text file
				if (chunks < 10) {
					t = String.valueOf(cbuf);
					textArea.append(t);
				}
				for (int i=0; i<chars; i++) {
					char c = cbuf[i];
					if (c == '\n') lineCountX++;
				}
				if (chunks == 10) {
					textArea.append("\n\n !!! End of file not shown !!!!\n");
				}
			}
		}
		catch (Exception ex) {
			System.out.println("couldn't read the xy file" + file);
			ex.printStackTrace();
		}
		nLinesRead = (lineCountX > 0) ? lineCountX : textArea.getLineCount();
		fileKey.setLastLine(nLinesRead);
	}
	
	public abstract void setKeyFields();

    public class MyCaretListener implements CaretListener {
		public void caretUpdate(CaretEvent e) {
			try {
				caretRow=textArea.getLineOfOffset(e.getDot());
			} catch(Exception ex){}
			//get columns 1&2 from selected text range (Column1 must be less than Column2)
			if (e.getMark()<e.getDot()){
				try {
					caretColumn1=e.getMark()
					-textArea.getLineStartOffset(textArea.getLineOfOffset(e.getMark()));
				} catch(Exception ex){}
				try {
					caretColumn2=e.getDot()-textArea.getLineStartOffset(caretRow)-1;
				} catch(Exception ex){}
			} else {
				try {
					caretColumn2=e.getMark()
					-textArea.getLineStartOffset(textArea.getLineOfOffset(e.getMark()))-1;
				} catch(Exception ex){}
				try {
					caretColumn1=e.getDot()-textArea.getLineStartOffset(caretRow);
				} catch(Exception ex){}
			}
			rowColLabel.setText("row:"+caretRow+" from column:"
					+caretColumn1+" to column:"
					+caretColumn2);
			updateSelectedFieldGroup();
		}
	}
	
	public boolean clickedOK() {
	    return clickedOK ;
	}
	
	public void updateSelectedFieldGroup()
    {
	    String gName = bGroup.getSelection().getActionCommand();
	    FieldGroup selectedGroup = findSelectedGroup(gName);
	    if (selectedGroup == null) return;
	    selectedGroup.processUserSelection();
        setKeyFields();
        setTextFields();
    }

    public abstract void setTextFields();

    private FieldGroup findSelectedGroup(String gName)
    {
        if (fieldGroups == null) return null;
        for (FieldGroup fg: fieldGroups) {
            if(gName.equals(fg.getName())) {
                return fg;
            }
        }
        for (FieldGroup fg: optionalColumnGroups) {
            if(gName.equals(fg.getName())) {
                return fg;
            }
        }
        return null;
    }

    abstract class FieldGroup extends JPanel {

	    JToggleButton button;
	    JTextField textField;
	    
        public FieldGroup(String string)
        {
            setName(string);
            button = new JToggleButton(string);
            button.setActionCommand(string);
            button.setToolTipText("Set cursor mode to select \""+string+"\" column range");
            textField = new JTextField();
        }

        public abstract void processUserSelection();

        public AbstractButton getButton()
        {
            return button;
        }

        public void setText(String string)
        {
            textField.setText(string);
        }

        public String getText()
        {
            return textField.getText();
        }

        public void setToolTipText(String string)
        {
            button.setToolTipText(string);
        }

        public void setSelected(boolean b)
        {
            button.setSelected(b);
        }

        public void addActionListener(ActionListener actionListener)
        {
            textField.addActionListener(actionListener);
        }
        
        @Override
        public void setEnabled(boolean b) {
            button.setEnabled(b);
            textField.setEnabled(b);
            super.setEnabled(b);
        }
	    
	}
	
	private class SingleFieldGroup extends FieldGroup
    {

        public SingleFieldGroup(String string)
        {
            super(string);
            textField.setColumns(10);
            button.setToolTipText("Set cursor mode to select row of \""+string+"\"");
            button.setPreferredSize(new Dimension(100,25));
            
            setLayout(new BorderLayout());
            add(button, BorderLayout.WEST);
            add(textField, BorderLayout.CENTER);
        }

        @Override
        public void processUserSelection()
        {
            setText(""+caretRow); 
        }

    }

	class ColumnRangeGroup extends DoubleFieldGroup
	{
        public ColumnRangeGroup(String string)
	    {
	        super(string);
	        fieldName1.setText("From");
	        fieldName2.setText("To");
	    }

        public void setTo(int i)
        {
            textField2.setText(""+i);
        }

        public void setFrom(int i)
        {
            textField.setText(""+i);
        }

        public String getTo()
        {
            return textField2.getText();
        }

        public String getFrom()
        {
            return textField.getText();
        }

        @Override
        public void processUserSelection()
        {
            setFrom(caretColumn1);
            setTo(caretColumn2);
        }

	}

	class CharacterKeyGroup extends DoubleFieldGroup
	{

        public CharacterKeyGroup(String string)
        {
            super(string);
            fieldName1.setText("Col.");
            fieldName2.setText("Char");
        }

        public void setChar(String string)
        {
            textField2.setText(string);
        }

        public void setColumn(int column)
        {
            textField.setText(""+column);
        }

        public String getChar()
        {
            return textField2.getText();
        }

        public String getColumn()
        {
            return textField.getText();
        }

        @Override
        public void processUserSelection()
        {
            setColumn(caretColumn1);
            setChar(textArea.getSelectedText());
        }
	}
	
	private abstract class DoubleFieldGroup extends FieldGroup
    {
	    protected JLabel fieldName1 = new JLabel();
        protected JLabel fieldName2 = new JLabel();

        protected JTextField textField2;

        public DoubleFieldGroup(String string)
        {
            super(string);
            textField2 = new JTextField();

            setLayout(new BorderLayout());
            add(button, BorderLayout.CENTER);
            
            Box fromPane = new Box(BoxLayout.Y_AXIS);
            fromPane.add(fieldName1);
            fromPane.add(textField);

            Box toPane = new Box(BoxLayout.Y_AXIS);
            toPane.add(fieldName2);
            toPane.add(textField2);
            
            Box bottom = new Box(BoxLayout.X_AXIS);
            bottom.add(fromPane);
            bottom.add(toPane);
            add(bottom, BorderLayout.SOUTH);
        }
        
        @Override
        public void addActionListener(ActionListener actionListener)
        {
            textField.addActionListener(actionListener);
            textField2.addActionListener(actionListener);
        }


        @Override
        public void setEnabled(boolean b) {
            textField2.setEnabled(b);
            fieldName1.setEnabled(b);
            fieldName2.setEnabled(b);
            super.setEnabled(b);
        }
    }


    private class OptionalColumnGroup extends ColumnRangeGroup
    {

        private Key key;

        public OptionalColumnGroup(String string)
        {
            super(string);
            // TODO Auto-generated constructor stub
        }

        public OptionalColumnGroup(Key key)
        {
            super(key.getName());
            this.key = key;
            textField.setText(key.getFrom()+"");
            textField2.setText(key.getTo()+"");
        }
        
        @Override
        public void setTo(int to) {
            super.setTo(to);
            key.setTo(to);
        }

        @Override
        public String getTo() {
            return key.getTo()+"";
        }
        

        @Override
        public void setFrom(int from) {
            super.setFrom(from);
            key.setFrom(from);
        }

        @Override
        public String getFrom() {
            return key.getFrom()+"";
        }
    }


}
