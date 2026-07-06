package com.tricongeophysics;

public class XMLChild extends XMLItem
{
    String value;
    
    XMLChild(String name)
    {
        super(name);
    }

    @Override
    public void parse(String[] lines, Index i)
    {
        value = lines[i.i].trim();
        i.increment(2);
    }

    @Override
    public void print()
    {
        SUtil.print("Child name: "+name+" value: "+value);
    }
    
    public String toString() {
        return "Child name: "+name+" value: "+value;
    }

    /**
     * if the submitted name matches this child's name,
     * return value.
     * Otherwise, return null.
     */
    @Override
    public String getValue(String name)
    {
        if (name.equals(this.name)) return value;
        return null;
    }

    /**
     * Not implemented by child
     */
    @Override
    public String[] getValues(String name)
    {
        if (name.equals(this.name)) return new String[]{value};
        return null;
    }

}
