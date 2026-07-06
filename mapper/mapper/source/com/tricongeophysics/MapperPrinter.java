package com.tricongeophysics;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class MapperPrinter extends PrintUtility
{


    private Mapper mapper;

    public MapperPrinter(Mapper mapper)
    {
        super(mapper.stationPlotter, false);
        this.mapper = mapper;
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex)
    {
        if (pageIndex >= 1 ) {
            return(NO_SUCH_PAGE);
        } else {
            Graphics2D g2d = (Graphics2D)g;
            componentToBePrinted = mapper.stationPlotter;
            calcImageScale(pageFormat);
            //disable double buffering and print!
            disableDoubleBuffering(mapper);
            
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2d.scale(scale,scale);
            
            JLabel l = new JLabel(mapper.getTitle());
            JFrame p = new JFrame();
            p.add(l);
            p.setVisible(true);
            p.pack();
            p.paint(g2d);
            p.setVisible(false);
            p.dispose();
            
            g2d.translate(0, p.getHeight());
            boolean pf = mapper.stationPlotter.isPrinterFriendlyMode();
            mapper.stationPlotter.setPrinterFriendlyMode(true);
            mapper.stationPlotter.paint(g2d);
            mapper.stationPlotter.setPrinterFriendlyMode(pf);
            
            g2d.translate(0, mapper.stationPlotter.getHeight());
            mapper.statisticsLabel.paint(g2d);
            
            g2d.translate(0, mapper.statisticsLabel.getHeight() + 10);
            mapper.cdpBinningPane.setCornersEnabled(true);
            mapper.cdpBinningPane.cornersPane.paint(g2d);
            
            g2d.translate(mapper.cdpBinningPane.cornersPane.getWidth() + 10, 0);
            mapper.cdpBinningPane.setAngleEnabled(true);
            mapper.cdpBinningPane.anglePane.paint(g2d);
            mapper.cdpBinningPane.cdpGridMethodChanged(mapper.cdpBinningPane.cdpGridMethod);
            
            g2d.translate(0 - mapper.cdpBinningPane.cornersPane.getWidth() - 10,  mapper.cdpBinningPane.cornersPane.getHeight() + 10);
            mapper.notesPane.paint(g2d);
            
            enableDoubleBuffering(mapper);
            return(PAGE_EXISTS);
        }
    }

}
