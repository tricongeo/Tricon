package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class ReloadButton extends JButton {

	ReloadButton() {
		super(SUtil.createImageIcon(ReloadButton.class,"images/reload_20x20.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
	}
	
}
