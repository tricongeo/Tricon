package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class PasteButton extends JButton
{
    PasteButton() {
        super(SUtil.createImageIcon(PasteButton.class, "images/paste.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setToolTipText("paste");
    }
}
