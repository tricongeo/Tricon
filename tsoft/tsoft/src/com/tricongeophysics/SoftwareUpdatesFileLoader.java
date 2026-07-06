package com.tricongeophysics;

import java.util.ArrayList;

public class SoftwareUpdatesFileLoader extends SoftwareFileLoader {

	public SoftwareUpdatesFileLoader() {
		super(SoftwareFileLoader.UpdatesIn, SoftwareFileLoader.UpdatesOut);
	}
	
	public String makeHTMLTitle() {
		String string="";
		string += "<body>"+CR;
		string += "<p><img src=\"tricon_logo_small.png\" align=\"center\"> Updates Page | <a href=\""+SummaryOut.getName()+"\">Summary Page</a></p>"+CR;
		string += "<hr>"+CR;
		string += "<h1>"+title+"</h1>"+CR;
		return string;
	}


	public static void main(String[] args) {
		SoftwareUpdatesFileLoader sufl = new SoftwareUpdatesFileLoader();
		sufl.test();
	}

}

	