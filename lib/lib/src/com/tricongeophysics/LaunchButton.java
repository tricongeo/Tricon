package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class LaunchButton extends JButton {

	LaunchButton() {
		super(SUtil.createImageIcon(LaunchButton.class, "images/launch.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
	}
	
}
