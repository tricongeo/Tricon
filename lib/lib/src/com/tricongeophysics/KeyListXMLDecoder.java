package com.tricongeophysics;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This is not a true XML Decoder. Just a simple one I wrote pretty quickly.
 * It assumes that tags are on a separate line as values, which XML does not require.
 * It is designed to read output from Parameter.toXML();
 * @author scott
 *
 */
public class KeyListXMLDecoder
{

    private TriconFile file;

    public KeyListXMLDecoder(String filename)
    {
        file = new TriconFile(filename);
        
    }

    public Key decodeKeyList() throws IOException
    {
        if (file == null) return null;
        
        String[] lines = file.readFileFast().split("\n");
        Key list = new Key();
        for (int i=1; i<lines.length-1; i++) { //ignore first and last line containing root field
            String line0 = lines[i];
            String line1 = lines[i+1];
           // SUtil.print(line0);
            if (line0.length() > 1 && line0.charAt(0) == '<' && line0.charAt(1) != '/') { //beginning of new parameter
                String name = line0.substring(1, line0.length()-1);
               // SUtil.print("adding "+name+"\n");
                list.addKey(line1.trim(), name);
            }
        }
        
        return list;
    }

}
