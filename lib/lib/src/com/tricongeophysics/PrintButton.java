package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class PrintButton extends JButton
{
    PrintButton() {
        super(SUtil.createImageIcon(PrintButton.class, "images/print.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setToolTipText("Print");
    }
}
