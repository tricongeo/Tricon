package com.tricongeophysics;

import java.awt.*;

import javax.swing.*;
import java.awt.print.*;

public class PrintUtility implements Printable {
	protected Component componentToBePrinted;
	private boolean fitOnOnePage;
	protected double scale;

	public static void printComponent(Component c) {
		new PrintUtility(c, true).print();
	}

	public PrintUtility(Component componentToBePrinted,  boolean fitOnOnePage) {
		this.componentToBePrinted = componentToBePrinted;
		this.fitOnOnePage = fitOnOnePage;
	}

	public void print() {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);
		if (printJob.printDialog())
			try {
				printJob.print();
			} catch(PrinterException pe) {
				System.out.println("Error printing: " + pe);
				JOptionPane.showMessageDialog(componentToBePrinted, pe, "Print Error", JOptionPane.ERROR_MESSAGE);
			}
	}

	protected void calcImageScale(PageFormat pageFormat) {
		//    keep constant aspect ratio
		double xratio = pageFormat.getImageableWidth()/componentToBePrinted.getWidth();
		double yratio = pageFormat.getImageableHeight()/componentToBePrinted.getHeight();
		if (fitOnOnePage) {
			scale = Math.min(yratio, xratio);
		}
		else {
			scale = xratio;
		}
	}

	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		calcImageScale(pageFormat);
		int totalPages = calcTotalPages(pageFormat);
		double h = pageFormat.getImageableY();
		if (pageIndex >= totalPages ) {
			return(NO_SUCH_PAGE);
		} else {
			Graphics2D g2d = (Graphics2D)g;
			double ty = h;
			if (pageIndex > 0) ty = h - pageIndex* pageFormat.getImageableHeight();
			g2d.translate(pageFormat.getImageableX(), ty); //maybe we shouldn't be translating x on second page, but so far it's working
			g2d.scale(scale,scale);
			//disable double buffering and print!
			disableDoubleBuffering(componentToBePrinted);
			componentToBePrinted.paint(g2d);
			enableDoubleBuffering(componentToBePrinted);
			return(PAGE_EXISTS);
		}
	}

	protected int calcTotalPages(PageFormat pageFormat) {
		double totalHeight = pageFormat.getImageableHeight();
		int componentHeight = componentToBePrinted.getHeight();
		double usedHeight = componentHeight * scale;
		int pages = (int) Math.ceil (usedHeight / totalHeight);
		return pages;
	}

	public static void disableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(false);
	}

	public static void enableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(true);
	}

	public static void printComponent(Component c, boolean fitOnOnePage) {
		new PrintUtility(c, fitOnOnePage).print();
	}

	public static Printable getPrintable(Component c, boolean fitOnOnePage) {
		return new PrintUtility(c, fitOnOnePage);
	}
}
