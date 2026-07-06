package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JToggleButton;

public class PanButton extends JToggleButton {

	PanButton() {
		super(SUtil.createImageIcon(PanButton.class,"images/pan_icon.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setToolTipText("Pan");
	}
	
}
