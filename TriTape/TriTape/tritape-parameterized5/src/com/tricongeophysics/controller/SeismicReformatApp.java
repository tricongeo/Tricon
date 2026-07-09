package com.tricongeophysics.controller;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** application entry point */
public class SeismicReformatApp
{
    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ignored)
        {
            //fall back to the default look and feel
        }

        SwingUtilities.invokeLater(() -> new TraceMonitor().show());
    }
}
