package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class NewButton extends JButton
{
    NewButton() {
        super(SUtil.createImageIcon(NewButton.class, "images/new_icon.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setToolTipText("New");
    }
}
