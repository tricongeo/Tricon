package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JToggleButton;

public class FButton extends JToggleButton
{
    FButton() {
        super(SUtil.createImageIcon(FButton.class, "images/f.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
    }
}
