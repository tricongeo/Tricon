package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JToggleButton;

public class PanGridButton extends JToggleButton {

	PanGridButton() {
		super(SUtil.createImageIcon(PanGridButton.class,"images/pan_grid.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setToolTipText("Pan Grid");
	}
	
}
