package com.tricongeophysics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JTextArea;

public class TextAreaReadFileJob extends Job
{

    private String file;
    private JTextArea textArea;

    public TextAreaReadFileJob(JTextArea textArea2, String inputFile)
    {
        this.textArea = textArea2;
        this.file = inputFile;
    }

    @Override
    public void cancel()
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void doJob()
    {
        try {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        textArea.read(reader,file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getProgressMax()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getIndeterminate()
    {
        return true;
    }

}
