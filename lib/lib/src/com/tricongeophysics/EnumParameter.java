package com.tricongeophysics;

public class EnumParameter extends Parameter {
	protected Object[] options;
	int selectedIndex=0;

	public EnumParameter(String name, String description, String help, String default_value, String[] options, int index) {
		super(name, description, help, default_value, index);
		this.options = options;
		setValue(default_value);
	}
	
	public EnumParameter()
    {
    }

	@Override
	public Type getType() {
		return Parameter.Type.ENUM;
	}

	@Override
	public boolean valueIsOk(String val) {
	    if (val == null || options == null) return false;
		for(int i=0;i<options.length;i++) {
			if (options[i].toString().equals(val)) {
				selectedIndex = i;
				return true;
			}
		}
		return false;
	}
	
	public Object[] getOptions() {
		return options;
	}

	public void setOptions(Object[] options) {
		this.options = options;
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	@Override
	public String toOutputString() {
		return String.format("%-70s : %s", super.toOutputString(), optionsToString());
	}

	private String optionsToString() {
		String opts="options(";
		for (int i=0;i<options.length;i++) {
			opts += " "+options[i]+",";
		}
		opts += ")";
		return opts;
	}


}
