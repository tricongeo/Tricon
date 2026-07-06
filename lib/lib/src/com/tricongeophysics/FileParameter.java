package com.tricongeophysics;

import java.io.File;


public class FileParameter extends Parameter {
	File file;
	private String[] filterList;

	public FileParameter(String name, String description, String help, String default_value, String[] filterList, int index) {
		super(name, description, help, default_value, index);
		this.filterList = filterList;
		//setValue(default_value); //sometimes, default value may contain message, so no need to check it
	}

	@Override
	public Type getType() {
		return Parameter.Type.FILE;
	}

	@Override
	public boolean valueIsOk(String val) {
		if (val == null) return false;
		file = new File(val);
		if (file.exists()) {
			return true;
		}
		return false;
	}

	public String[] getFilterList() {
		return filterList;
	}

}
