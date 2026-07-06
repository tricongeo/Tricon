package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JToggleButton;

public class PolyLineButton extends JToggleButton
{
    PolyLineButton() {
        super(SUtil.createImageIcon(PolyLineButton.class, "images/yellow_polyline.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setPressedIcon(SUtil.createImageIcon(ScatterButton.class, "images/yellow_polyline_selected.png"));
        this.setSelectedIcon(SUtil.createImageIcon(ScatterButton.class, "images/yellow_polyline_selected.png"));

    }
}
