package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class FeetMetersButton extends JButton
{
    FeetMetersButton() {
        super(SUtil.createImageIcon(RulerButton.class, "images/feet_meters.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
    }
}
