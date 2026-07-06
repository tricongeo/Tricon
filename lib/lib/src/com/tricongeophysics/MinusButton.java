package com.tricongeophysics;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;

public class MinusButton extends JButton {

	MinusButton() {
		super(SUtil.createImageIcon(MinusButton.class,"images/minus.png"));
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setToolTipText("Remove");
	}
	
}
