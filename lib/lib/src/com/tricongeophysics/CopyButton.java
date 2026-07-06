package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class CopyButton extends JButton
{
    CopyButton() {
        super(SUtil.createImageIcon(CopyButton.class, "images/copy.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setToolTipText("copy");
    }
}
