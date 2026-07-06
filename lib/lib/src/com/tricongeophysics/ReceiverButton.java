package com.tricongeophysics;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;


public class ReceiverButton extends JButton
{
    boolean selected = false;
    private Icon selectedIcon;
    private Icon unselectedIcon;
    
    ReceiverButton() {
        super(SUtil.createImageIcon(ReceiverButton.class, "images/receiver.png"));
        setMargin(new Insets(0, 0, 0, 0));
        selectedIcon = SUtil.createImageIcon(ReceiverButton.class, "images/receiver_off.png");
        unselectedIcon = getIcon();
        addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e)
            {
                selected = !selected;
                setIcon();
            }});
    }

    protected void setIcon()
    {
        if (selected) {
            setIcon(selectedIcon);
        }
        else {
            setIcon(unselectedIcon);
        }
    }
}
