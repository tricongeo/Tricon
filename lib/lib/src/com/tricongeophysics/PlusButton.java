package com.tricongeophysics;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;

public class PlusButton extends JButton {

	PlusButton() {
		super(SUtil.createImageIcon(PlusButton.class,"images/plus.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setToolTipText("Add");
	}
	
}
