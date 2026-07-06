package com.tricongeophysics;

public class TextParameter extends Parameter
{

    public TextParameter (String name, String description, String help, String default_value, int index) {
        super(name, description, help, default_value, index);
        setValue(default_value);
    }

    @Override
    public Parameter.Type getType()
    {
        return Parameter.Type.TEXT;
    }

    @Override
    public boolean valueIsOk(String val)
    {
        if (val == null) return false;
        return true;
    }

}
