package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JToggleButton;

public class RotateGridButton extends JToggleButton {

	RotateGridButton() {
		super(SUtil.createImageIcon(RotateGridButton.class,"images/rotate_grid.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setToolTipText("Rotate Grid");
	}
	
}
