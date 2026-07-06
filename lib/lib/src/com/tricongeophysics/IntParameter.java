package com.tricongeophysics;

public class IntParameter extends Parameter{
	int intValue;

	public IntParameter(String name, String description, String help,String default_value, int index) {
		super(name, description, help, default_value, index);
		setValue(default_value);
	}

	@Override
	public boolean valueIsOk(String val) {
		try {
			intValue = Integer.parseInt(val);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public Type getType() {
		return Parameter.Type.INT;
	}
	

}
