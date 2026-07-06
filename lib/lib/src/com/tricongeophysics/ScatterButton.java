package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class ScatterButton extends JButton
{
    ScatterButton() {
        super(SUtil.createImageIcon(ScatterButton.class, "images/scatter.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
    }
}
