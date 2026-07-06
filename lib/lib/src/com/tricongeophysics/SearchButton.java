package com.tricongeophysics;

import java.awt.Insets;

import javax.swing.JButton;

public class SearchButton extends JButton
{
    SearchButton() {
        super(SUtil.createImageIcon(SearchButton.class, "images/search.png"));
        this.setMargin(new Insets(0, 0, 0, 0));
    }
}
