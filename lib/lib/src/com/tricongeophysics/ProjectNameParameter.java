package com.tricongeophysics;

import java.io.File;

/**
 * same as Directory Parameter, except that the parent directory, rather than the full path,
 * is all that must exist to be OK.
 * This is the stub name that all output files will use
 * 
 * @author scott
 *
 */
public class ProjectNameParameter extends Parameter {
	
	protected File projName;

	public ProjectNameParameter(String name, String description, String help, String default_value, int index) {
		super(name, description, help, default_value, index);
	}

	@Override
	public Type getType() {
		return Parameter.Type.PROJECTNAME;
	}

	@Override
	/**
	 * checks if parent directory exists
	 */
	public boolean valueIsOk(String val) {
		projName = new File(val);
		File parent = projName.getParentFile();
		if (parent != null && parent.isDirectory()) {
			return true;
		}
		return false;
	}
	
	

}
