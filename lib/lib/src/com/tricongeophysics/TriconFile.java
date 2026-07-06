package com.tricongeophysics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * File class that adds useful methods to the original Java File class
 * 
 * @author scott
 *
 */

public class TriconFile extends File {
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final String NewLine = "\n";
	public String fileName;
	
	public static String DOT = ".";


	public TriconFile(String name) {
		super(name);
		fileName = this.getAbsolutePath();
	}
	
	public TriconFile() {
		super("");
	}
	
	/**
	 * get filename extension
	 * 
	 * @param file
	 * 
	 * @return
	 * String containing extension or "" if no extension
	 * 
	 * based on code from Jeff Albertson - JavaRanch
	 */
	public String getSuffix() {  
		return getSuffix(getName());
	}  
	
	

	/**
	 * File reader that reads a file 5,000,000 characters at a time and returns the file as a String
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public String readFileFast () throws IOException {
		char[] charArray = new char[5000000]; //character array buffer
		int numChars = 0; //number of characters read per attempt
		String fileText = "";
		//Read text file and insert into text area
		BufferedReader reader = new BufferedReader(new FileReader(this));
		while ((numChars=reader.read(charArray,0,charArray.length)) != -1) {
			fileText = fileText+String.copyValueOf(charArray,0,numChars);
		}
		reader.close();			
		return fileText;
	}
	
	public String[] getLines() throws IOException {
		String text = readFileFast();
		return text.split(NewLine);
	}

	/**
	 * Writes the given set of lines to the file.
	 * Closes the file when done.
	 * 
	 * @param lines
	 * @throws IOException
	 */
	public void write(String[] lines) throws IOException {
		if (lines == null) return;
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(this));
		for (int i=0;i<lines.length;i++) {
			writer.write(lines[i]+NewLine);
		}
		writer.close();
	}

	/**
	 * Sets everything in filename past last "." found to the characters found in "new Extension"
	 * NOTE: extension is changed only for the returned TriconFile object! Not the current object!!
	 * 
	 * @param newExtension
	 * @return new TriconFile with the filename extension set accordingly
	 */
	public TriconFile setExtension(String newExtension) {
		if (newExtension == null) newExtension = "";
		String name = this.getAbsolutePath();
		int index = name.lastIndexOf(DOT);
		if ( index >= 0 ) {
			name = name.substring(0,index);
		}
		String newFileName = name + newExtension;
		return new TriconFile(newFileName);
	}

	public String getShortFileName(int maxNumChars) {
		String name = getName();
		if (name == null) name = "null";
		if (name.length() <= maxNumChars ) return name;
		return "..."+name.substring(name.length()-maxNumChars);
	}

	public String getShortDirectoryName(int maxNumChars) {
		String dir = getParent();
		if (dir == null) dir = "/null";
		if (dir.length() <= maxNumChars ) return dir;
		return "..."+dir.substring(dir.length()-maxNumChars);
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Writes one line of text to the file
	 * @param string
	 * @throws IOException
	 */
    public void write(String string) throws IOException
    {
        this.write(new String[]{string});
    }

    /**
     * Returns text after last period in String (including the period).
     * @param file
     * @return if String is null or contains no periods, a blank String is returned
     */
	public static String getSuffix(String file) {
		if (file == null) return "";
		int dot = file.lastIndexOf(DOT);  
		return dot == -1 ? "" : DOT+file.substring(dot+1);  
	}

	public static String removeSuffix(String file) {
		int dot = file.lastIndexOf(DOT);  
		return dot == -1 ? file : file.substring(0, dot);  
	}

	public static boolean isImage(String file) {
		String[] imageSuffixes = ImageIO.getReaderFileSuffixes();
		
		String suffix = getSuffix(file).replace(".", "");
		for (String s: imageSuffixes) {
			if (s.toLowerCase().equals(suffix.toLowerCase()))
				return true;
		}
		return false;
	}

}
