package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class BrowseButton extends JButton {

	BrowseButton() {
		super(SUtil.createImageIcon(BrowseButton.class, "images/folder_blue_open.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
	}
	
}
