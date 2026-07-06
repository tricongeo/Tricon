package com.tricongeophysics;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class FastStringRenderer extends DefaultTableCellRenderer {

   Component stubRenderer = new NothingComponent();

   public Component getTableCellRendererComponent(JTable table, Object value,
                                                  boolean isSelected, boolean hasFocus, int row, int column) {
      if ( ((String)value).length() == 0 && !isSelected && !hasFocus) {
         return stubRenderer;
      }
      return super.getTableCellRendererComponent(table, value, 
                                                 isSelected, hasFocus,
                                                 row, column);
   }

   class NothingComponent extends JComponent {
      public void paint(Graphics g) {
         // Do Nothing
      }
   }
}

