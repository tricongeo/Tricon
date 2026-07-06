package com.tricongeophysics;

public class MapperBugList {
	   public static String getList() {
			return "<html>"+
			    "<font size=+2 color=0000ff>"+
			    "Bug Fixes: </font>"+"<br>"+
			    "<br>"+
			    "<font size=+1 color=0000ff>"+
			    "Version 1.1.1:</font>"+"<br>"+
			    "1) Only plots stations located in zoom window - eliminates plot-wrap bug when zoomed-in on SGI."+"<br>"+
			    "2) Sped up zoom rectangle and distance line animation, screen only updates when done drawing."+"<br>"+
			    "3) Added Bugs Fixed menu"+"<br>"+
			    "<br>"+
			    "<font size=+1 color=0000ff>"+
			    "Version 1.1:</font>"+"<br>"+
			    "1) First Non-Beta version."+"<br>"+
			    "2) Added ability to read separate shot and receiver files (shots red, receivers green)"+"<br>"+
			    "3) Added Help menu"+ "<br>"+
			    "<br>"+
			    "<font size=+1 color=0000ff>"+
			    "Coming next version:</font>"+"<br>"+
			    "1) Multiple shot & receiver files"+"<br>"+
			    "2) Outputs SEGP-1 file with merged shots and receivers (renumbered if need be)"+"<br>"+
			    "3) Plot elevation as station color"+"<br>"+
			    "<br>"+
			    "<font size=+1 color=0000ff>"+
			    "Found a Bug?:</font>"+"<br>"+
			    "To submit a bug, send e-mail to <a href=\"mailto:scott.cook@tricongeophysics.com\">"+
			    "Scott.Cook@tricongeophysics.com</a>"+
			    "</html>";
			    }
}
