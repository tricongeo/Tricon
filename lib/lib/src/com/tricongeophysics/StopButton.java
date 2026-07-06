package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class StopButton extends JButton {
	
	StopButton(){
		super(SUtil.createImageIcon(StartButton.class, "images/stop.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
	}
	
}
