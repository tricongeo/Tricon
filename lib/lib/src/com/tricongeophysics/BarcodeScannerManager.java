package com.tricongeophysics;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JFrame;

/**
 * Provides support for barcode scanners that act like keyboards.
 * Listens to a Component with keyboard focus for a special character that indicates
 * the beginning of a barcode scan. The remaining barcode characters
 * are handled by this class and not passed to the Component.
 * Once the barcode has finished (indicated by another special character),
 * a barcode scan event is passed to all listeners with the barcode number
 * passed as a String.
 * <br><br>
 * To use:<br>
 * Get a BarcodeScannerManager instance and add it as a KeyListener to the
 * component that's in keyboard focus. Next, add your object that cares
 * about barcodes as a BarcodeListener to the BarcodeScannerManager
 * instance.
 * 
 * @author scott
 *
 */
public class BarcodeScannerManager implements KeyListener {

	private static final char BarcodeStartCharacter = KeyEvent.VK_ESCAPE;
	private static final char BarcodeEndCharacter = KeyEvent.VK_ENTER;
	private static final char EmptyCharacter = KeyEvent.VK_CLEAR;
	private static BarcodeScannerManager barcodeScannerManager;
	private boolean readingBarcode;
	private String barcode;
	private ArrayList<BarcodeListener> barcodeListeners = new ArrayList<BarcodeListener>();
	private BarcodeListener barcodeListener;

	BarcodeScannerManager() {};  //Singleton constructor pattern

	public static void main(String[] args) {
		ArrayList<TableData> array = new ArrayList<TableData>();

		int itemCount = 4;

		for(int i=0; i<itemCount; i++) {
			TestData td = new TestData();
			td.i = i;
			td.f = i;
			td.d = Math.random()*100000000;
			td.b = (i%2 == 0) ? true : false;
			array.add(td);
		}

		ReflectiveTableModel rtm = new ReflectiveTableModel(array);
		//Object val = rtm.getValueAt(0, 2);
		//SUtil.print("found val: "+val);=

		JFrame f= new JFrame("Spreadsheet");
		SpreadsheetPanel sheet = new SpreadsheetPanel(rtm);
		f.setContentPane(sheet);
		f.setSize(800,800);
		f.addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.setVisible(true);

		sheet.addSheetKeyListener(new BarcodeScannerManager());
	}


	@Override
	public void keyTyped(KeyEvent e) {
		//SUtil.print(e.paramString());
		char keychar = e.getKeyChar();
		
		if (keychar == BarcodeStartCharacter) {
			readingBarcode = true;
			clearBarcode();
		}
		else if (keychar == BarcodeEndCharacter && readingBarcode == true) {
			fireBarcodeFinished();
			readingBarcode = false;
			clearBarcode();
		} else if (readingBarcode == true) {
			addBarcodeChar(keychar);
		} else {
			return; //we aren't reading barcode, let text field handle characters
		}
		e.setKeyChar(EmptyCharacter); //suppress barcode printing to text field by replacing characters with empty character
	}

	private void addBarcodeChar(char keychar) {
		barcode += keychar;
	}


	private void clearBarcode() {
		barcode = "";
	}

	private void fireBarcodeFinished() {
		SUtil.print("\nBarcode is: " + barcode);
		Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		SUtil.print("Focus owner is: " + owner.getName()+ " :: " + owner + "\n");
		for (BarcodeListener l: barcodeListeners) {
			l.barcodeScanned(barcode);
		}
		if (barcodeListener != null)barcodeListener.barcodeScanned(barcode);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

//	public void addBarcodeListener(BarcodeListener l) {
//		barcodeListeners.add(l);
//	}

	public void setBarcodeListener(BarcodeListener l) {
		barcodeListener = l;
	}

	/**
	 * BarcodeScannerManager uses singleton pattern.
	 * There can only be one open at a time.
	 * To get an instance, use this method instead of trying to use constructor.
	 * @return
	 */
//	public static BarcodeScannerManager getScanManager() {
//		if (barcodeScannerManager == null)
//			barcodeScannerManager = new BarcodeScannerManager();
//		return barcodeScannerManager;
//	}
}
