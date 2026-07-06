package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JToggleButton;

public class GridButton extends JToggleButton {

	GridButton() {
		super(SUtil.createImageIcon(GridButton.class,"images/grid.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setToolTipText("Show Grid");
	}
	
}
