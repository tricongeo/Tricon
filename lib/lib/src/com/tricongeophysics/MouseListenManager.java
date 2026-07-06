package com.tricongeophysics;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseListenManager implements MouseListener, MouseMotionListener
{
    MouseEventListener mel;
    
    public MouseListenManager(MouseEventListener mel) {
        this.mel = mel;
    }
    
    @Override
    public void mouseClicked(MouseEvent e)
    {
        mel.mouseClicked(e);
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        mel.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        mel.mouseExited(e);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        mel.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        mel.mouseReleased(e);
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        mel.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        mel.mouseMoved(e);
    }

    public void setListener(MouseEventListener mel)
    {
        this.mel = mel;
    }
    
    @Override
    public String toString() {
        return mel.toString();
    }

}
