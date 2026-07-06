package com.tricongeophysics;

public class FloatParameter extends Parameter {
	double floatValue;

	public FloatParameter(String name, String description, String help, String default_value, int index) {
		super(name, description, help, default_value, index);
		setValue(default_value);
	}

	@Override
	public boolean valueIsOk(String val) {
		try {
			floatValue = Double.parseDouble(val);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
	
	@Override
	public void setValue(String val) {
		if (valueIsOk(val)) {
			val = ""+Double.parseDouble(val); //reformat to float before setting
		}
		super.setValue(val);
	}

	@Override
	public Type getType() {
		return Parameter.Type.FLOAT;
	}

}
