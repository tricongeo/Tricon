package com.tricongeophysics;

import java.io.File;

public class DirParameter extends Parameter {
	
	protected File dir;

	public DirParameter(String name, String description, String help, String default_value, int index) {
		super(name, description, help, default_value, index);
	}
	
	public DirParameter(){}

	@Override
	public Type getType() {
		return Parameter.Type.DIR;
	}

	@Override
	public boolean valueIsOk(String val) {
		dir = new File(val);
		if (dir.isDirectory()) {
			return true;
		}
		return false;
	}

}
