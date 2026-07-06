package com.tricongeophysics;

public class SoftwareSummaryFileLoader extends SoftwareFileLoader {

	public SoftwareSummaryFileLoader() {
		super(SoftwareFileLoader.SummaryIn , SoftwareFileLoader.SummaryOut);
	}
	
	public String makeHTMLTitle() {
		String string="";
		string += "<body>"+CR;
		string += "<p><img src=\"tricon_logo_small.png\" align=\"center\"> Summary Page | <a href=\""+UpdatesOut.getName()+"\">Updates Page</a></p>"+CR;
		string += "<hr>"+CR;
		string += "<h1>"+title+"</h1>"+CR;
		return string;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SoftwareSummaryFileLoader ssfl = new SoftwareSummaryFileLoader();
		ssfl.test();
	}

}
