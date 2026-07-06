package com.tricongeophysics;

import java.util.ArrayList;

public class XMLParent extends XMLItem
{
    ArrayList<XMLItem> children = new ArrayList<XMLItem>();
    
    XMLParent(String name)
    {
        super(name);
    }

    @Override
    public void parse(String[] lines, Index i)
    {
        while (i.getI() < lines.length) {
            String line0 = lines[i.i];
            if (line0.contains("</"+name+">")) return;
            XMLItem child = SimpleXMLDecoder.makeXMLItem(lines, i);
            if (child == null) continue;
            children.add(child);
        }
    }

    @Override
    public void print()
    {
        SUtil.print("\nParent name: "+name+" children:\n");
        for (XMLItem child: children) {
            child.print();
        }
        SUtil.print("End Parent: "+name+"\n");
    }
    
    public String toString() {
        return name;
    }

    /**
     * searches all children for item named "name".
     * return the value of the first child encountered
     * with that name
     */
    @Override
    public String getValue(String name)
    {
        String value = null;
        for (XMLItem child: children) {
            value = child.getValue(name);
            if (value != null) 
                return value;
        }
        return value;
    }

    @Override
    public String[] getValues(String name)
    {
        ArrayList<String> values = new ArrayList<String>();
        String[] value = null;
        for (XMLItem child: children) {
            value = child.getValues(name);
            if (value != null) 
                for (String v: value)
                    values.add(v);  
        }
        if (value != null && value.length > 0 )
            return values.toArray(new String[]{""});
        return null;
    }

}
