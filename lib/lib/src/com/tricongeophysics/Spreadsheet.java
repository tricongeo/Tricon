package com.tricongeophysics;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

/**
  * This class is just a shell to bootstrap the app.
  * The core of the functionality is in SpreadsheetPanel.
  *
  * @see SpreadsheetPanel
  * @author Steve Wilson
  */
public class Spreadsheet {

   SpreadsheetPanel sheet;
 
   public Spreadsheet() {
      JFrame f= new JFrame("SheetMetal");
      sheet = new SpreadsheetPanel(null);
      f.setContentPane(sheet);
      f.setSize(400,400);
      f.addWindowListener( new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            quit();
         }
      });
      f.setVisible(true);
   }

   protected void quit() {
      System.exit(0);
   }

   public static void main(String[] args) {
      new Spreadsheet();
   }
}
