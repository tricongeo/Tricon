package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class SaveButton extends JButton {

	SaveButton() {
		super(SUtil.createImageIcon(SaveButton.class, "images/save_icon.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
	}
	
}
