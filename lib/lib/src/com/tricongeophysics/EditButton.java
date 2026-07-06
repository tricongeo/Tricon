package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class EditButton extends JButton
{
    EditButton() {
        super(SUtil.createImageIcon(EditButton.class, "images/edit_icon.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setToolTipText("Edit");
    }
}
