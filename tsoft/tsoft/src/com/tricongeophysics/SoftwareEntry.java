package com.tricongeophysics;

import java.util.ArrayList;

public class SoftwareEntry {

	private ArrayList<String> lines;
	private String name = "";
	private String[] text = {""};
	
	public enum Type { NORMAL, HEADER };
	private Type type;
	
	public SoftwareEntry(ArrayList<String> paragraph) {
		lines = new ArrayList<String>(paragraph);
		name = lines.get(0).trim().replace(":", "");
		 
		if (lines.size() > 1) {
			text = new String[lines.size()-1];
			for (int i=1; i<lines.size(); i++) {
				text[i-1] = lines.get(i);
			}
		}
		
		type = (text[0].trim().length() > 0) ? Type.NORMAL : Type.HEADER; //if there is text after the name, normal. otherwise, header.
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public String toString() {
		String string = "";
		for (int i=0;i<lines.size();i++) {
			string += lines.get(i)+"\n";
		}
		return string;
	}
	
	public String getHTML() {
		String string = "";
		string += "<h2>"+name+"</h2>";
		for (int i=0;i<text.length;i++) {
			string += text[i]+"<br>";
		}
		return string;
	}

	public String getName() {
		return name;
	}

	public String[] getText() {
		return text;
	}

	public Type getType() {
		return type;
	}

}
