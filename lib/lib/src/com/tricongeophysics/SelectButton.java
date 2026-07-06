package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JToggleButton;

public class SelectButton extends JToggleButton
{
    SelectButton() {
        super(SUtil.createImageIcon(SelectButton.class, "images/stock_draw-selection.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setPressedIcon(SUtil.createImageIcon(ScatterButton.class, "images/select_selected.png"));
        this.setSelectedIcon(SUtil.createImageIcon(ScatterButton.class, "images/select_selected.png"));

    }
}
