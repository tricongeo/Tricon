package com.tricongeophysics;

import java.util.ArrayList;

/**
 * Class for storing paired labels and values. An arbitrary number of
 * labels and values can be assigned to a single key.
 * 
 * @author scott
 *
 */
public class Key {
	
	ArrayList<KeyElement> keys;
	
	public ArrayList<KeyElement> getKeys()
    {
        return keys;
    }

    public void setKeys(ArrayList<KeyElement> keys)
    {
        this.keys = keys;
    }

    public Key() {
		keys = new ArrayList<KeyElement>();
	}
	
	public void addKey(String v, String l) {
		keys.add(new KeyElement(v,l));
	}
	
	public String getLabel(String v) {
		for (int i=0;i<keys.size();i++) {
			if (keys.get(i).value.equals(v)) {
				return keys.get(i).label;
			}
		}
		System.out.println("KEY:getLabel - warning! Couldn't find label for \"" + v + "\"");
		return null;
	}
	
	public String getValue(String l) {
		for (int i=0;i<keys.size();i++) {
			if (keys.get(i).label.equals(l)) {
				return keys.get(i).value;
			}
		}
		return null;
	}

	public void addKey(Object v, String l) {
		addKey(v+"", l);
	}
	
}
