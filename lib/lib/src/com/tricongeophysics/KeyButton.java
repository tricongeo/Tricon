package com.tricongeophysics;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;

public class KeyButton extends JButton {

	KeyButton() {
		super(SUtil.createImageIcon(KeyButton.class, "images/key_clear.png","key icon"));
		this.setMargin(new Insets(0, 0, 0, 0));
	}
	
}
