package com.tricongeophysics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ParameterListXMLEncoder
{

    private File file;

    public ParameterListXMLEncoder(File file)
    {
        this.file = file;
    }

    public void writeList(ArrayList<Parameter> parameterList, String listName) throws IOException
    {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write("<"+listName+">\n");
            if (parameterList == null) return;
            for (Parameter p:parameterList) {
                if (p == null) continue;
                writer.write(p.toXML());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (writer != null){
                    writer.write("</"+listName+">\n");
                    writer.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void close()
    {
        // TODO Auto-generated method stub
        
    }

    public String getFilename()
    {
        return file.getAbsolutePath();
    }

}
