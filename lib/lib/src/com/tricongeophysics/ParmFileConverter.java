package com.tricongeophysics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public abstract class ParmFileConverter {

	ArrayList<Parameter> parameterList;

	/**
	 * goes back and forth between file format and paramater array
	 * 
	 * @return
	 */
	public ParmFileConverter( ArrayList<Parameter> parameterList ) {
		this.parameterList = new ArrayList<Parameter>(parameterList); //makes new copy, so when we sort it or alter it the real one isn't effected.
		sortParms();
		loadKeys();
	}

	/**
	 * sort parameters based on their index
	 * this ensures that IO to and from files will match order of info in files
	 */
	protected void sortParms() {
		Collections.sort(parameterList, new Comparator<Parameter>(){
			public int compare(Parameter o1, Parameter o2) {
				Integer i1 = new Integer(o1.getIndex());
				Integer i2 = new Integer(o2.getIndex());
				return i1.compareTo(i2);
			}
		});
	}

	/**
	 * parses string input and loads the values it finds into the correct parameter
	 * 
	 * @param parmStrings
	 * @return
	 * @throws InvalidParameterFileException 
	 */
	public abstract ArrayList<Parameter> toParameters(String[] parmStrings) throws InvalidParameterFileException;


	/**
	 * loads Keys to define relationship between parameter values and file values
	 * (eg. file has numbers where parameters have strings - string/number relationship)
	 */
	protected abstract void loadKeys();

	/**
	 * converts parameter array to file format
	 * 
	 * @return String array
	 */
	public abstract String[] toFileFormat();
}

