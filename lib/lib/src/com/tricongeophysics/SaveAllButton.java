package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class SaveAllButton extends JButton {

	SaveAllButton() {
		super(SUtil.createImageIcon(SaveAllButton.class, "images/save_all.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
	}
	
}
