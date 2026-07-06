package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class PlusCopyButton extends JButton
{
    PlusCopyButton() {
        super(SUtil.createImageIcon(PlusCopyButton.class, "images/plus_copy.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setToolTipText("Add Same");
    }
}
