package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class NotesPane extends JPanel
{

    private JTextPane textPane;

    NotesPane() {
        this.setName("Notes");
        this.setLayout(new BorderLayout());
        textPane = new JTextPane();
        //textPane.setPreferredSize(new Dimension(600,600));
        
        JScrollPane scroller = new JScrollPane(textPane);
        this.add(scroller);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initialize();
    }

    private String getUserName()
    {
        return SUtil.getUserName();
    }

    private String getDate()
    {
        return SUtil.getTodaysDate().toString();
    }

    private void initialize()
    {
        textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        textPane.setText(getDefaultText() );
    }

    private String getDefaultText()
    {
        String defaultText = 
                "Geom Tech: " + getUserName() + "\t\t\t\t\t\t\t\tDate: " + getDate() + "\n" +
                "Line Name:    \t\t\t\t\t\t\t\tClient:\n" +
                "State:        \t\t\t\t\t\t\t\tCounty:\n" +
                "Focus Project: " + Mapper.ProjectName + "\t\t\t\t\t\t\tFocus Line: " + Mapper.LineName + "\n" +
                "Source Type:  \t\t\t\t\t\t\t\tSweep Frequency:\n" +
                "Record Length:\t\t\t\t\t\t\t\tSample Rate:\n" +
                "Segd/Segy:\n\n" +
                "Shot Interval(ft/m):\n" +
                "Recv Interval(ft/m):\n" +
                "CDP Bin Size:\n\n" +
                "Total Shots After GIN    :\n" +
                "Total Shots From OB      :\n" +
                "Total Shots After PROSHOT:\n" +
                "\n*******************************************************\n" +
                "\nNotes:\n" +
                "\n";
        return defaultText;
    }

    public String getText()
    {
       return textPane.getText();
    }

    public void setText(String notes)
    {
        if (notes != null) {
            textPane.setText(notes);
        } else {
            textPane.setText(this.getDefaultText());
        }
    }
    
}
