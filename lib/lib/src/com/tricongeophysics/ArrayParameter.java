package com.tricongeophysics;

public class ArrayParameter extends EnumParameter
{
    public ArrayParameter(String name, String description, String help, String itemName, String[] array, int index) {
        super(name, description, help, itemName, array, index);
    }

    public String toXML()
    {
        if (name == null || value == null) return null;
        String s = "";
        s += "<"+name+">\n";
        for (Object o: options) {
            s += "<"+value+">\n";
            s += "  "+o+"\n";
            s += "</"+value+">\n";
        }
        s += "</"+name+">\n";
        return s;
    }
    
    public void setValue(String value) {};

}
