package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class StartButton extends JButton {
	
	StartButton() {
		super(SUtil.createImageIcon(StartButton.class, "images/player_play.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
	}
	
}
