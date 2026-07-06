package com.tricongeophysics;

import java.awt.*;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class OBFileKeyDialogue extends FileKeyDialogue {
	
	private OBFileKey obFileKey;
    private JTextField sourceLineAddTextField;
    private JTextField recLineAddTextField;
    private JTextField ffidAddTextField;

    {
	    BUTTON_LABELS = new String[]{"First Row","Last Row","Key","FFID","SrcLine","SrcStn","FromChan","ToChan"
	            ,"RecLine","FromStn","ToStn", "Shot"};
	}
	
	public OBFileKeyDialogue(FileKey fk, Frame parent)
    {
        super(fk, parent);
        this.obFileKey = (OBFileKey)fk;
    }

    public OBFileKeyDialogue(Frame parent, OBFileKey obFileKey)
    {
        this(obFileKey, parent);
    }
    
    @Override
    public void setKeyFields () {
        obFileKey.setFirstLine(fieldGroups[0].getText());
        obFileKey.setLastLine(fieldGroups[1].getText());
        obFileKey.setCharKeyColumn(             ((CharacterKeyGroup)fieldGroups[2]).getColumn());
        obFileKey.setCharKeyChar(               ((CharacterKeyGroup)fieldGroups[2]).getChar());
        obFileKey.setFfidStartPos(               ((ColumnRangeGroup)fieldGroups[3]).getFrom());
        obFileKey.setFfidEndPos(                 ((ColumnRangeGroup)fieldGroups[3]).getTo());
        obFileKey.setSourceLineStart(            ((ColumnRangeGroup)fieldGroups[4]).getFrom());
        obFileKey.setSourceLineEnd(              ((ColumnRangeGroup)fieldGroups[4]).getTo());
        obFileKey.setSourceStationStart(         ((ColumnRangeGroup)fieldGroups[5]).getFrom());
        obFileKey.setSourceStationEnd(           ((ColumnRangeGroup)fieldGroups[5]).getTo());
        obFileKey.setFromChanStart(              ((ColumnRangeGroup)fieldGroups[6]).getFrom());
        obFileKey.setFromChanEnd(                ((ColumnRangeGroup)fieldGroups[6]).getTo());
        obFileKey.setToChanStart(                ((ColumnRangeGroup)fieldGroups[7]).getFrom());
        obFileKey.setToChanEnd(                  ((ColumnRangeGroup)fieldGroups[7]).getTo());
        obFileKey.setReceiverLineStart(          ((ColumnRangeGroup)fieldGroups[8]).getFrom());
        obFileKey.setReceiverLineEnd(            ((ColumnRangeGroup)fieldGroups[8]).getTo());
        obFileKey.setFromReceiverStationStartPos(((ColumnRangeGroup)fieldGroups[9]).getFrom());
        obFileKey.setFromReceiverStationEndPos(  ((ColumnRangeGroup)fieldGroups[9]).getTo());
        obFileKey.setToReceiverStationStartPos(  ((ColumnRangeGroup)fieldGroups[10]).getFrom());
        obFileKey.setToReceiverStationEndPos(    ((ColumnRangeGroup)fieldGroups[10]).getTo());
        obFileKey.setShotStartPos(               ((ColumnRangeGroup)fieldGroups[11]).getFrom());
        obFileKey.setShotEndPos(                 ((ColumnRangeGroup)fieldGroups[11]).getTo());
        obFileKey.setSourceLineNumberAdd(sourceLineAddTextField.getText());
        obFileKey.setReceiverLineNumberAdd(recLineAddTextField.getText());
        obFileKey.setFfidAdd(ffidAddTextField.getText());
    }
    
    @Override
    public void setTextFields() {
        fieldGroups[0].setText(""+obFileKey.getFirstLine());
        fieldGroups[1].setText(""+obFileKey.getLastLine());
        ((CharacterKeyGroup)fieldGroups[2]).setColumn(obFileKey.getCharKeyColumn());
        ((CharacterKeyGroup)fieldGroups[2]).setChar(""+obFileKey.getCharKeyChar());
        ((ColumnRangeGroup)fieldGroups[3]).setFrom(obFileKey.getFfidStartPos());
        ((ColumnRangeGroup)fieldGroups[3]).setTo(obFileKey.getFfidEndPos());
        ((ColumnRangeGroup)fieldGroups[4]).setFrom(obFileKey.getSourceLineStart());
        ((ColumnRangeGroup)fieldGroups[4]).setTo(obFileKey.getSourceLineEnd());
        ((ColumnRangeGroup)fieldGroups[5]).setFrom(obFileKey.getSourceStationStart());
        ((ColumnRangeGroup)fieldGroups[5]).setTo(obFileKey.getSourceStationEnd());
        ((ColumnRangeGroup)fieldGroups[6]).setFrom(obFileKey.getFromChanStart());
        ((ColumnRangeGroup)fieldGroups[6]).setTo(obFileKey.getFromChanEnd());
        ((ColumnRangeGroup)fieldGroups[7]).setFrom(obFileKey.getToChanStart());
        ((ColumnRangeGroup)fieldGroups[7]).setTo(obFileKey.getToChanEnd());
        ((ColumnRangeGroup)fieldGroups[8]).setFrom(obFileKey.getReceiverLineStart());
        ((ColumnRangeGroup)fieldGroups[8]).setTo(obFileKey.getReceiverLineEnd());
        ((ColumnRangeGroup)fieldGroups[9]).setFrom(obFileKey.getFromReceiverStationStartPos());
        ((ColumnRangeGroup)fieldGroups[9]).setTo(obFileKey.getFromReceiverStationEndPos());
        ((ColumnRangeGroup)fieldGroups[10]).setFrom(obFileKey.getToReceiverStationStartPos());
        ((ColumnRangeGroup)fieldGroups[10]).setTo(obFileKey.getToReceiverStationEndPos());
        ((ColumnRangeGroup)fieldGroups[11]).setFrom(obFileKey.getShotStartPos());
        ((ColumnRangeGroup)fieldGroups[11]).setTo(obFileKey.getShotEndPos());
        sourceLineAddTextField.setText(""+obFileKey.getSourceLineNumberAdd());
        recLineAddTextField.setText(""+obFileKey.getReceiverLineNumberAdd());
        ffidAddTextField.setText(""+obFileKey.getFfidAdd());
    }

    @Override
    public Component createLineAddPanel()
    {
        Box lineAddPanel = new Box(BoxLayout.Y_AXIS);
        
        ffidAddTextField = new JTextField();
        ffidAddTextField.setColumns(7);
        Box ffidAdd = new Box(BoxLayout.X_AXIS);
        ffidAdd.add(new JLabel("Shot Add: "));
        ffidAdd.add(ffidAddTextField);
        lineAddPanel.add(ffidAdd);
        ffidAddTextField.addActionListener(mbl);
        
        sourceLineAddTextField = new JTextField();
        sourceLineAddTextField.setColumns(7);
        Box srcAdd = new Box(BoxLayout.X_AXIS);
        srcAdd.add(new JLabel("Shot Line Add: "));
        srcAdd.add(sourceLineAddTextField);
        lineAddPanel.add(srcAdd);
        sourceLineAddTextField.addActionListener(mbl);
        
        recLineAddTextField = new JTextField();
        recLineAddTextField.setColumns(7);
        Box recAdd = new Box(BoxLayout.X_AXIS);
        recAdd.add(new JLabel("Rcvr Line Add: "));
        recAdd.add(recLineAddTextField);
        lineAddPanel.add(recAdd);
        recLineAddTextField.addActionListener(mbl);
        
        JPanel lineAddOK = new JPanel();
        lineAddOK.add(lineAddPanel);
        JPanel okPanel = new JPanel();
        okPanel.add(okButton);
        lineAddOK.add(okPanel);
        return lineAddOK;
    }
	
}