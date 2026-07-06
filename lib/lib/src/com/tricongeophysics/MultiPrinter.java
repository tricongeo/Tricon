package com.tricongeophysics;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public class MultiPrinter extends PrintUtility {

	private Printable[] printables;
	private int p_index = 0;
	private int pageCorrection = 0;

	public MultiPrinter(Printable[] printables) {
		super(null, false);
		this.printables = printables;
	}

	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		int status = 0;
		try {
			status = printables[p_index].print(g, pageFormat, pageIndex-pageCorrection);
			if (status == Printable.NO_SUCH_PAGE && p_index < (printables.length - 1)) {
				pageCorrection = pageIndex;
				p_index++;
				status = printables[p_index].print(g, pageFormat, pageIndex-pageCorrection);
			}
			
		} catch (PrinterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return status;
	}
}
