package com.tricongeophysics;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.tricongeophysics.FileKeyDialogue.CharacterKeyGroup;
import com.tricongeophysics.FileKeyDialogue.ColumnRangeGroup;

public class StationFileKeyDialogue extends FileKeyDialogue
{

    private StationFileKey stationFileKey;
    private JTextField lineAddTextField;
    
    {
        BUTTON_LABELS = new String[] {"First Line","Last Line","Key","Line","Station","X","Y","Z"};
    }

    public StationFileKeyDialogue(FileKey fk, Frame parent)
    {
        super(fk, parent);
        this.stationFileKey = (StationFileKey) fk;
    }
    
    @Override
    public void setKeyFields () {
        stationFileKey.setFirstLine(fieldGroups[0].getText());
        stationFileKey.setLastLine(fieldGroups[1].getText());
        stationFileKey.setCharKeyColumn(((CharacterKeyGroup)fieldGroups[2]).getColumn());
        stationFileKey.setCharKeyChar(  ((CharacterKeyGroup)fieldGroups[2]).getChar());
        stationFileKey.setLineStart(    ((ColumnRangeGroup)fieldGroups[3]).getFrom());
        stationFileKey.setLineEnd(      ((ColumnRangeGroup)fieldGroups[3]).getTo());
        stationFileKey.setStationStart( ((ColumnRangeGroup)fieldGroups[4]).getFrom());
        stationFileKey.setStationEnd(   ((ColumnRangeGroup)fieldGroups[4]).getTo());
        stationFileKey.setXStart(       ((ColumnRangeGroup)fieldGroups[5]).getFrom());
        stationFileKey.setXEnd(         ((ColumnRangeGroup)fieldGroups[5]).getTo());
        stationFileKey.setYStart(       ((ColumnRangeGroup)fieldGroups[6]).getFrom());
        stationFileKey.setYEnd(         ((ColumnRangeGroup)fieldGroups[6]).getTo());
        stationFileKey.setZStart(       ((ColumnRangeGroup)fieldGroups[7]).getFrom());
        stationFileKey.setZEnd(         ((ColumnRangeGroup)fieldGroups[7]).getTo());
        stationFileKey.setLineNumberAdd(lineAddTextField.getText());
    }
    
    @Override
    public void setTextFields() {
        fieldGroups[0].setText(""+stationFileKey.getFirstLine());
        fieldGroups[1].setText(""+stationFileKey.getLastLine());
        ((CharacterKeyGroup)fieldGroups[2]).setColumn(stationFileKey.getCharKeyColumn());
        ((CharacterKeyGroup)fieldGroups[2]).setChar(""+stationFileKey.getCharKeyChar());
        ((ColumnRangeGroup)fieldGroups[3]).setFrom(stationFileKey.getLineStart());
        ((ColumnRangeGroup)fieldGroups[3]).setTo(stationFileKey.getLineEnd());
        ((ColumnRangeGroup)fieldGroups[4]).setFrom(stationFileKey.getStationStart());
        ((ColumnRangeGroup)fieldGroups[4]).setTo(stationFileKey.getStationEnd());
        ((ColumnRangeGroup)fieldGroups[5]).setFrom(stationFileKey.getXStart());
        ((ColumnRangeGroup)fieldGroups[5]).setTo(stationFileKey.getXEnd());
        ((ColumnRangeGroup)fieldGroups[6]).setFrom(stationFileKey.getYStart());
        ((ColumnRangeGroup)fieldGroups[6]).setTo(stationFileKey.getYEnd());
        ((ColumnRangeGroup)fieldGroups[7]).setFrom(stationFileKey.getZStart());
        ((ColumnRangeGroup)fieldGroups[7]).setTo(stationFileKey.getZEnd());
        lineAddTextField.setText(""+stationFileKey.getLineNumberAdd());
        
    }

    @Override
    public Component createLineAddPanel()
    {
        Box box = new Box(BoxLayout.Y_AXIS);
        box.setAlignmentX(LEFT_ALIGNMENT);
        
        JPanel okPanel = new JPanel();
        okPanel.add(okButton);
        box.add(okPanel);
        //box.add(okButton);
        
        JLabel label = new JLabel("Add to Line: ");
        JPanel lineAddPanel = new JPanel();
        lineAddPanel.add(label);
        lineAddTextField = new JTextField();
        lineAddTextField.setColumns(7);
        lineAddPanel.add(lineAddTextField);
        lineAddTextField.addActionListener(mbl);
        box.add(lineAddPanel);
        
        return box;
    }

}
