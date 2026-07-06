package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JToggleButton;

public class SpreadButton extends JToggleButton
{
    SpreadButton(){
        super(SUtil.createImageIcon(FButton.class, "images/spread4.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setPressedIcon(SUtil.createImageIcon(ScatterButton.class, "images/spread_selected.png"));
        this.setSelectedIcon(SUtil.createImageIcon(ScatterButton.class, "images/spread_selected.png"));
    }
}
