package com.tricongeophysics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.tricongeophysics.SUtil;

public abstract class SoftwareFileLoader {

	protected File inputFile;
	protected File outputFile;
	protected ArrayList<SoftwareEntry> entries;
	protected String fileText;
	protected String title;
	
	protected static final String CR = "\n";
	protected static final String BodyColor = "rgb(0,50,90)";
	protected static final String HeadingColor = "rgb(0,60,0)";
	protected static final String ListColor = "rgb(55,0,25)";
	protected static final String BackgroundColor = "rgb(232,230,230)";
	protected static final String Font = "times";
	public static File SummaryIn;
	public static File SummaryOut;
	public static File UpdatesIn;
	public static File UpdatesOut;
	
	
	public SoftwareFileLoader (File inFile, File outFile) {
		inputFile = inFile;
		outputFile = outFile;
	}
	
	
	/**
	 * reads input text file and interprets text as a list of software entries
	 * these entries are stored in array "entries".
	 * 
	 * @param inputFile = file to be read
	 * @return  boolean = whether file was read successfully or not
	 */
	public boolean readFile(File file) {
		fileText = SUtil.readFileFast(file);
		return true;
	}
	
	public boolean readFile() {
		fileText = SUtil.readFileFast(inputFile);
		return true;
	}
	
	public boolean writeHTMLFile(File file) {
		FileWriter writer;
		try {
			writer = new FileWriter(file);
			writer.write(getHTMLText());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void loadEntries() {
		readFile();
		processText();
	}
	
	public String getFormattedText() {
		loadEntries();
		String string = "";
		for (int i=0; i<entries.size(); i++) {
			string = string+entries.get(i).toString()+CR;
		}
		return string;
	}
	
	public String getHTMLText() {
		loadEntries();
		String string = "";
		string += makeHTMLOpenning();
		string += makeHTMLTitle();
		string += makeTableOfContentsTable();
		string += "<hr>"+CR;
		string += makeDescriptionLists();
		string += "<hr>"+CR;
		string += makeHTMLClosing();
		return string;
	}
	
	protected abstract String makeHTMLTitle();

	public String makeHTMLOpenning() {
		String string="";
		title = entries.get(0).getName();
		string += "<html>"+CR;
		string += "<style type=\"text/css\">"+CR;
		string += "  body { color: "+BodyColor+"; "+CR;
		string += "  font-family: "+Font+"; "+CR;
		string += "  background-color: "+BackgroundColor+"; }"+CR;
		string += "  h2 { color: "+HeadingColor+";  }"+CR;
		string += "  dt { color: "+ListColor+";  }"+CR;
		string += "</style>"+CR;
		string += "<head>"+CR;
		string += "<title>"+title+"</title>"+CR;
		string += "</head>"+CR;
		return string;
	}
	
	public String makeTableOfContentsList() {
		String string="";
		
		for (int i=1;i<entries.size();i++) { //start w/ i=1 to skip title
			String name = entries.get(i).getName();
			if (entries.get(i).getType() == SoftwareEntry.Type.HEADER) {
				string += "</ul><h2>"+name+"</h2><ul>"+CR;
			} 
			if (entries.get(i).getType() == SoftwareEntry.Type.NORMAL) {
				string += "<li><a href=\"#"+name+"\">"+name+"</a></li>"+CR;
			} 
		}
		return string;
	}
	
	public String makeTableOfContentsTable() {
		String string="";
		int nrows = 8;
		int counter = 0;
		
		for (int i=1;i<entries.size();i++) { //start w/ i=1 to skip title
			String name = entries.get(i).getName();
			if (entries.get(i).getType() == SoftwareEntry.Type.HEADER) {
				string += "</table>"+CR+CR+"<h2>"+name+"</h2>"+CR+"<table border=\"1\" cellpadding=\"2\" width=\"50%\">"+CR+"<tr>";
				counter = 0;
			} 
			if (entries.get(i).getType() == SoftwareEntry.Type.NORMAL) {
				counter++;
				if (counter <= nrows) {
					string += "<td><a href=\"#"+name+"\">"+name+"</a></td>"+CR;
				}
				else {
					string += "</tr>"+CR+"<tr><td><a href=\"#"+name+"\">"+name+"</a></td>";
					counter = 1;
				}
			} 
		}
		string += "</table>"+CR;
		return string;
	}
	
	
	public String makeDescriptionLists() {
		String string="";
		
		for (int i=1;i<entries.size();i++) { //start w/ i=1 to skip title
			String name = entries.get(i).getName();
			String[] text = entries.get(i).getText();
			if (entries.get(i).getType() == SoftwareEntry.Type.HEADER) {
				string += "</dl>"+CR+CR+"<h2>"+name+"</h2><dl>"+CR;
			} 
			if (entries.get(i).getType() == SoftwareEntry.Type.NORMAL) {
				string += "<dt id=\""+name+"\">"+name+"</dt>"+CR+"<dd>"+CR;
				for (int j=0;j<text.length;j++) {
					string += text[j]+CR;
				}
				string += "</dd>"+CR;
			} 
		}
		string += "</dl>"+CR;
		
		return string;
	}
	
	public String makeHTMLClosing() {
		String string="";
		string += "</body>"+CR;
		string += "</html>"+CR;
		return string;
	}
	
	public void processText() {
		entries = new ArrayList<SoftwareEntry>();
		int length = 0;
		String[] lines = fileText.split(CR);
		ArrayList<String> paragraph = new ArrayList<String>();
		for (int i=0;i<lines.length;i++) {
			length = lines[i].trim().length();
			if (length>0) paragraph.add(lines[i]);
			if (length == 0 && paragraph.size()>0) { //end of paragraph reached, add to entry list
				entries.add(new SoftwareEntry(paragraph));
				paragraph.clear();
			}
		}
	}
	
	public void test() {
		SUtil.print("reading file"+inputFile);
		readFile();
		processText();
		SUtil.print("here's what we found");
		SUtil.print(getHTMLText());
		writeHTMLFile(outputFile);
	}
	
	public void convertHTML() {
		SUtil.print("reading file"+inputFile+CR);
		readFile();
		processText();
		SUtil.print("writing file"+outputFile+CR);
		writeHTMLFile(outputFile);
	}


}
