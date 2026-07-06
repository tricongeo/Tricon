package com.tricongeophysics;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;

/* ImageFilter.java is used by FileChooserDemo2.java. */
public class DynamicFileFilter extends FileFilter {
    
    protected ArrayList<String> suffixList;
    private String description;
    
    public DynamicFileFilter (String s) {
        this(new String[]{s});
    }
    
    public DynamicFileFilter(String[] strings)
    {
        super();
        suffixList = new ArrayList<String>();
        for (String s: strings)
            addSuffix(s);
    }

    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        //find current file extension, including "." part
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i).toLowerCase();
        }
        for (i=0;(i<suffixList.size() && ext != null);i++)
            if (ext.equals(suffixList.get(i))) 
                return true;
        return false;
    }

    //The description of this filter
    public String getDescription() {
        if (description != null) return description;
        if (suffixList == null || suffixList.size() == 0) return "";
        String s = suffixList.get(0);
        for (int i=1;i<suffixList.size();i++)
            s=s+", "+getSuffixList().get(i);
        return s;
    }

    public ArrayList<String> getSuffixList() {
        return suffixList;
    }

    public void addSuffix(String suffix) {
        this.suffixList.add(suffix);
    }
    
    public void removeSuffix(String suffix) {
        this.suffixList.remove(suffix);
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * returns first filter extension or blank.
     * Use "getDescription()" for more detailed information.
     */
    @Override
    public String toString() {
        if (suffixList == null || suffixList.size() == 0) return "";
        String s = suffixList.get(0);
        return s;
    }

}
