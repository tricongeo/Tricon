package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JToggleButton;

public class RulerButton extends JToggleButton
{
    RulerButton() {
        super(SUtil.createImageIcon(RulerButton.class, "images/ruler.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setPressedIcon(SUtil.createImageIcon(ScatterButton.class, "images/ruler_selected.png"));
        this.setSelectedIcon(SUtil.createImageIcon(ScatterButton.class, "images/ruler_selected.png"));

    }
}
