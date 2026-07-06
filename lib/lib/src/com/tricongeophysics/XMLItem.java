package com.tricongeophysics;

public abstract class XMLItem
{
    String name;
    
    XMLItem(String name) {
        this.name = name;
    }

    /**
     * Parses an array of text lines in order to populate the
     * "name" and "value" fields of an XMLItem.
     * 
     * Parsing starts at index "i" in the text array.
     * 
     * @param lines
     * @param i
     */
    public abstract void parse(String[] lines, Index i);
    
    /**
     * Prints all children and parents beneath this XMLItem.
     * 
     */
    public abstract void print();

    /**
     * Search XML tree for the first child instance having the name
     * "name". The value associated with that child is returned
     * (or null if it does not exist).
     * @param name
     * @return
     */
    public abstract String getValue(String name);

    /**
     * Search XML tree for any children having the name "name".
     * All values of such children are returned in String[] array.
     * 
     * null is returned if that name does not exist.
     * 
     * @param name
     * @return
     */
    public abstract String[] getValues(String name);
    
}
