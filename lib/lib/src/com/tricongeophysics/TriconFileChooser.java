package com.tricongeophysics;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TriconFileChooser {
	
	public static String lastFileSelection="";
    private static ArrayList<FileChangedListener> fileChangedListeners = new ArrayList<FileChangedListener>();

	/**
	 * launches file chooser and returns name of file selected
	 * 
	 * defaults to last file opened
	 * 
	 * if filterName or filterList are null, no file naming filter is applied
	 * 
	 * @param filterList
	 * @param c
	 * @return
	 */
	public static String launchFilteredFileChooser(String[] filterList, Component c) {
		return launchFilteredFileChooser(filterList, c, "Open");
	}
	
	public static String launchFilteredFileChooser(String[] filterList, Component c, String buttonText) {
		return launchFilteredFileChooser(lastFileSelection, filterList, null, c, buttonText);
	}
	
	public static String launchFilteredFileChooser(String defaultFile, String[] filterList, Component c) {
		return launchFilteredFileChooser(defaultFile, filterList, null, c, "Open");
	}
	
	
	/**
	 * launches file chooser and returns name of file selected
	 * 
	 * defaults to defaultFile (opens to that directory)
	 * 
	 * @param defaultFile = contains directory to start browsing at
	 * @param filterList = String[] of file extensions
	 * @param c = Component
	 * @return
	 */
	public static String launchFilteredFileChooser(String defaultFile, String[] filterList, FileFilter[] fileFilters, Component c, String buttonText) {
	    String firstGuess = defaultFile;
	    defaultFile = getDirName(defaultFile);
		if (!fileExists(defaultFile)) {
			defaultFile = lastFileSelection;
		}
		JFileChooser fileChooser = new JFileChooser(defaultFile);
		fileChooser.setApproveButtonText(buttonText);
		fileChooser.setDialogTitle(buttonText);
		fileChooser.setSelectedFile(new File(firstGuess));
		if (filterList != null) {
		    DynamicFileFilter filter = new DynamicFileFilter(filterList);
		    fileChooser.addChoosableFileFilter(filter);
		}
		if (fileFilters != null) {
		    for (FileFilter filter: fileFilters)
            fileChooser.addChoosableFileFilter(filter);
        }
		int returnVal = fileChooser.showOpenDialog(c);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	lastFileSelection = fileChooser.getSelectedFile().getAbsolutePath();
	    	TriconFile file = new TriconFile(lastFileSelection);
	    	if (!fileChooser.getFileFilter().accept(file)) {
	    		file = file.setExtension(fileChooser.getFileFilter().toString());
	    	}
	    	lastFileSelection = file.getAbsolutePath();
	    	fireSelectedFileChanged();
	       return lastFileSelection;
	    }
	    return null;
	}
	
	private static String getDirName(String path)
    {
	    File file = new File(path);
        File dir = file.getParentFile();
        if (dir == null) return "";
        return dir.getAbsolutePath();
    }

    private static boolean dirExists(String path)
    {
        File file = new File(path);
        File dir = file.getParentFile();
        if (dir == null) return false;
        return dir.exists();
    }

    private static void fireSelectedFileChanged()
    {
        for (FileChangedListener l: fileChangedListeners) l.fileChanged(lastFileSelection);
    }

    public static String launchDirChooser(String defaultDir, Component c) {
    	return launchDirChooser(defaultDir, c, "Open");
	}
	
	private static boolean fileExists(String fname) {
		if (fname == null) return false;
		File file = new File (fname);
		return file.exists();
	}

    public static void addFileChangedListener(FileChangedListener l)
    {
        if (l == null) return;
        fileChangedListeners.add(l);
    }

    public static String launchFilteredFileChooser(FileFilter[] filterList, Component c, String buttonText)
    {
        return launchFilteredFileChooser("", null, filterList, c, buttonText);
    }

    public static String launchFilteredFileChooser(String defaultFile, Component c, String buttonText)
    {
        return launchFilteredFileChooser(defaultFile, null, null, c, buttonText);
    }

    public static String launchFilteredFileChooser(String defaultFile, String[] filterList, Component c, String buttonText)
    {
        return launchFilteredFileChooser(defaultFile, filterList, null, c, buttonText);
    }

    public static String launchFilteredFileChooser(String defaultFile, FileFilter[] filterList, Component c, String buttonText)
    {
        return launchFilteredFileChooser(defaultFile, null, filterList, c, buttonText);
    }

	public static String launchDirChooser(String defaultDir, Component c, String buttonText) {
		if (!fileExists(defaultDir)) {
			defaultDir = lastFileSelection;
		}
		JFileChooser fileChooser = new JFileChooser(defaultDir);
		fileChooser.setApproveButtonText(buttonText);
		fileChooser.setDialogTitle(buttonText);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fileChooser.showOpenDialog(c);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	lastFileSelection = fileChooser.getSelectedFile().getAbsolutePath();
	       return lastFileSelection;
	    }
	    return null;
	}

	public static File[] launchDirsChooser(String defaultDir, Component c, String buttonText) {
		if (!fileExists(defaultDir)) {
			defaultDir = lastFileSelection;
		}
		JFileChooser fileChooser = new JFileChooser(defaultDir);
		fileChooser.setApproveButtonText(buttonText);
		fileChooser.setDialogTitle(buttonText);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		int returnVal = fileChooser.showOpenDialog(c);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	lastFileSelection = fileChooser.getSelectedFile().getAbsolutePath();
	       return fileChooser.getSelectedFiles();
	    }
	    return null;
	}
}
