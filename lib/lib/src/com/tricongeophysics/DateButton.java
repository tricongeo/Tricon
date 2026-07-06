package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class DateButton extends JButton
{
    DateButton() {
        super(SUtil.createImageIcon(DateButton.class, "images/date_picker.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setToolTipText("Select Date");
    }
}
