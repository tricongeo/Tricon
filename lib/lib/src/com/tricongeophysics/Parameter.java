package com.tricongeophysics;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public abstract class Parameter {
	
	protected String name;
	protected String description;
	protected String help;
	protected String value;
	protected String default_value;
	protected Parameter enabledDependsOnParm;
	protected String enabledDependsOnValue;
	public enum Type  { INT, FLOAT, TEXT, FILE, ENUM, DIR, PROJECTNAME };
	protected int index; //index of parameter for sorting when data is output
	
	public final static String NameParse = "Name:";
	public final static String DescParse = "Description:";
	public final static String HelpParse = "Help:";
	public final static String DefaultParse = "Default:";
	public final static String TypeParse = "Type:";
	
	private static final String NewExtension = ".parm";
	private static final String OldExtension = ".par";
	
	public final static String FieldSEP = ":"; //separator for fields in parameter files
	public final static String ValSEP = "\""; //separator for values in parameter files
	
	public Parameter (String name, String description, String help, String default_value, int index) {
		this.name = name;
		this.description = description;
		this.help = help;
		this.default_value = default_value;
		value = this.default_value; //go ahead and set default to value
		this.index = index;
	}
	
	public Parameter() {
	    
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public abstract boolean valueIsOk(String val);
	
	public abstract Type getType();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public String getValue() {
		return value;
	}

	/**
	 * sets value and checks if value is valid
	 * @param value
	 * @throws InvalidParameterException
	 */
	public void setValue(String value) {
//		if (valueIsOk(value)) {
//			this.value = value;
//		} else {
//			System.out.println("invalid value: " + value);
//		}
//		System.out.println("parm: \"" + name + "\" is now: " + this.value);
		if (value != null) {
			this.value = value.trim();
		}
	//	System.out.println("parm: \"" + name + "\" is now: " + this.value);
		if (!valueIsOk(value)) {
			System.out.println("invalid value: " + value);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public String toString() {
		String string = "";
		string += "name: " + name + "\n";
		string += "description: " + description + "\n";
		string += "help: " + help + "\n";
		string += "value: " + value + "\n";
		string += "default: " + default_value + "\n";
		
		return string;
	}

	public String getDefault_value() {
		return default_value;
	}

	public void setDefault_value(String default_value) {
		this.default_value = default_value;
	}

	/**
	 * loads parameter file based on file extension
	 * 
	 * if .par file, then runs LoadParameterValuesFromOldFile
	 * if .parm file, then runs LoadParameterValuesFromNewFile
	 * 
	 * @param filename
	 * @param parameterList
	 * @throws IOException 
	 */
	public static void LoadParameterValuesFromFile(String filename,	ArrayList<Parameter> parameterList, ParmFileConverter converter) throws IOException {
		TriconFile inputFile = new TriconFile(filename);
		if (!inputFile.exists()) {
			System.err.println("Parameter: LoadParameterValuesFromFile - file \""+ filename + "\" doesn't exist!!");
			return;
		}
		if (inputFile.getSuffix().equals(NewExtension)) {
			LoadParameterValuesFromNewFile(inputFile, parameterList);
			return;
		}
		if (inputFile.getSuffix().equals(OldExtension)) {
			LoadParameterValuesFromOldFile(inputFile, parameterList, converter);
			return;
		}
		System.err.println("Parameter: LoadParameterValuesFromFile - file \""+ filename + "\" uses unrecognized format!!");
	}

	/**
	 * Loads parameters using Turgut's original file format (just numbers, no text help)
	 * Assumes parameterList was built in exact same order as parameter File!!
	 * 
	 * @param inputFile
	 * @param parameterList
	 * @throws IOException 
	 */
	private static void LoadParameterValuesFromOldFile(TriconFile inputFile, ArrayList<Parameter> parameterList, ParmFileConverter converter) throws IOException {
		System.out.println("loading old format - file: \"" + inputFile.getAbsolutePath() + "\"");
		
		String contents[] = inputFile.getLines(); 
		//SC2DMODOldParmFileFormatConverter converter = new SC2DMODOldParmFileFormatConverter(parameterList);//converts old file format to new format
		try {
            parameterList = converter.toParameters(contents);
        } catch (InvalidParameterFileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	/**
	 * Reads New Format Parameter file and loads values into Parameter List
	 * 
	 * @param inputFile
	 * @param parameterList
	 * @throws IOException 
	 */
	private static void LoadParameterValuesFromNewFile(TriconFile inputFile, ArrayList<Parameter> parameterList) throws IOException {
		String contents = inputFile.readFileFast();
		String[] lines = contents.split("\n");
		
		for (int i=0;i<parameterList.size();i++) {
			parameterList.get(i).setValueFromTextLines(lines);
		}
		
		System.out.println("loading new format");
	}

	/**
	 * Takes lines from parameter text input file, finds the line matching the parameter's name, and then sets the value
	 * 
	 * takes the first value it finds that matches this parameter name
	 * 
	 * @param lines String array from .parm file
	 */
	private void setValueFromTextLines(String[] lines) {
		String name2="";
		String val="";
		
		//loop through lines. when name2 matches this parameters name, set the value
		for (int i=0;i<lines.length;i++) {
			if (lines[i].contains(FieldSEP)) {
				name2 = lines[i].split(FieldSEP)[0].trim();
				if (lines[i].contains(ValSEP)) {
					val = lines[i].split(ValSEP)[1];
					if (this.name.equals(name2)) {
						setValue(val);
						return; //we're done! no need to keep looping
					}
				}
			}
		}
	}

	public static String[] getFileExtensions() {
		return new String[] { NewExtension, OldExtension };
	}

	/**
	 * Saves parameter list to file (automatically sets file extension to correct type)
	 * 
	 * @param filename
	 * @param parameterList
	 * @param converter
	 * @throws IOException
	 */
	public static void SaveParameterValuesToFile(String filename, ArrayList<Parameter> parameterList, ParmFileConverter converter) throws IOException {
		TriconFile outputFile = new TriconFile(filename);
		SaveParameterValuesToNewFile(outputFile.setExtension(NewExtension), parameterList);
		SaveParameterValuesToOldFile(outputFile.setExtension(OldExtension), parameterList, converter);
	}

	private static void SaveParameterValuesToOldFile(TriconFile outputFile,ArrayList<Parameter> parameterList, ParmFileConverter converter) throws IOException {
		String[] lines = converter.toFileFormat();
		outputFile.write(lines);
	}

	public static void SaveParameterValuesToNewFile(TriconFile outputFile, ArrayList<Parameter> parameterList) throws IOException {
		String[] lines = new String[parameterList.size()+1];
		lines[0] = getParmFileHeader();
		for(int i=0;i<parameterList.size();i++) {
			lines[i+1] = parameterList.get(i).toOutputString();
		}
		outputFile.write(lines);
	}
	
	protected static String getParmFileHeader() {
		//String header = "";
		return "Parameter File - New Format - Created by SC2DMOD"+new Date();
	}

	public String toOutputString() {
		return String.format("%-30s: %-10s: \"%s\"", name, getType(), value);
	}

	public boolean valueIsOk() {
		return valueIsOk(value);
	}

	public void setEnabledDependsOn(Parameter parameter, String value) {
		this.enabledDependsOnParm = parameter;
		this.enabledDependsOnValue = value;
	}

	public boolean hasDependency() {
		if (enabledDependsOnParm == null) {
			return false;
		}
		return true;
	}

	public Parameter getEnabledDependsOnParm() {
		return enabledDependsOnParm;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

    public String toXML()
    {
        if (name == null || value == null) return null;
        return "<" + name + ">\n" + value + "\n" + "</" + name + ">\n";
    }

}
