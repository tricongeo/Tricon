package com.tricongeophysics;

import java.io.IOException;

/**
 * Class to decode simlpe XML
 * doesn't handle comments or parameters.
 * 
 * two kinds of XML items are handled, parents and children.
 * tags are assumed to be on separate lines from values
 * 
 * @author scott
 *
 */
public class SimpleXMLDecoder
{
    public static XMLItem makeXMLItem(String[] lines, Index i) {
        if (i.i+1 >= lines.length) {
            i.increment(1);
            return null;
        }
        String line0 = lines[i.getI()];
        String line1 = lines[i.getI()+1];
        
        if (line0.length() == 0) {
            i.increment(1);
            return null;
        }        
        
        String name = line0.substring(1, line0.length()-1);
        
        XMLItem item = null;
        
        if (line0.length() > 1 &&
                line0.charAt(0) == '<' &&
                line1.length() > 1 &&
                line1.charAt(0) == '<') {
            item = new XMLParent(name);
        } else {
            item = new XMLChild(name);
        }
        i.increment(1);
        if (line0.charAt(1) == '/') return null;
        item.parse(lines, i);
        return item;
    }
    
    public static void main (String[] args) {
        TriconFile file = new TriconFile("/apdata/request/L.s52.HDRH001.70030102.SHOT.geomshots.A.apu.6_4_10_5:37_PM.xml");
        String[] lines=null;
        try {
            lines = file.readFileFast().split("\n");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        XMLItem item = makeXMLItem(lines, new Index());
        item.print();
        
        String value = item.getValue("GPL");
        SUtil.print("value of GPL is: "+value);
        
        String[]values = item.getValues("Filename");
        SUtil.print("Filenames found:");
        SUtil.print(values);
    }

    public static XMLItem decodeFile(String filename) throws IOException
    {
        TriconFile file = new TriconFile(filename);
        if (!file.canRead() || file.isDirectory()) return null;
        String[] lines = file.readFileFast().split("\n");
        XMLItem item = makeXMLItem(lines, new Index());
        return item;
    }
}
