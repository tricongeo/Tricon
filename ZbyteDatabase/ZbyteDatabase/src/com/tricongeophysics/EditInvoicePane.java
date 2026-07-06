package com.tricongeophysics;

import java.awt.Container;


public class EditInvoicePane extends ZbyteEditDbRowPane implements PageFinishedListener 
{
	
	public EditInvoicePane() {
		super("invoice", null);
		// TODO Auto-generated constructor stub
	}

	private Wizard w;
	private PrintInvoiceWizardPane iv_print;
	private StartInvoicePane iv_start;
	private SelectInvoicePane iv_select;
	private SetInvoicePricePane iv_set;
	
	@Override
	public void setRow(int row) {
		if (iv_start == null) return;
		iv_start.setRow(row);
	}

	public void init() {
		Container c = this.getContentPane();
		DbParms dbParms = DbParms.getParms("ZbyteDatabase");
		
		//..Start Invoice Pane
		dbParms.dbTable = "invoice";
		dbParms.query = "select * from invoice";
		iv_start = new StartInvoicePane(dbParms);
		
		
		//..Select Invoice Pane
		dbParms.dbTable = "invoice";
		dbParms.query = "select * from invoice";
		iv_select = new SelectInvoicePane(dbParms);
		
		//..Set Invoice Price Pane
		dbParms.dbTable = "invoice";
		dbParms.query = "select * from invoice";
		iv_set = new SetInvoicePricePane(dbParms);
		
		//...Deliverable Created Pane
		dbParms.dbTable = "invoice";
		dbParms.query = "select * from invoice";
		iv_print = new PrintInvoiceWizardPane(dbParms);
		
		w = new Wizard("Invoice");
		w.addWizardPage(iv_start);
		w.addWizardPage(iv_select);
		w.addWizardPage(iv_set);
		w.addWizardPage(iv_print);
		w.setCancelText("Close");
		w.addPageFinishedListener(this);
		
		w.createAndShow();
		
		super.setVisible(false);
		//this.destroy();
	}
	
	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
	
	@Override
	public void loadFields() {
		super.loadFields();
	//	init();
	}

	protected static void createAndShowGUI() {
//		JFrame frame = new JFrame("Zbyte Progress");
//		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		
		EditInvoicePane gj = new EditInvoicePane();
//		gj.setPreferredSize(new Dimension(500, 500));
		//gj.setVisible(true);
		gj.init();
		
//		frame.getContentPane().add(gj);
//		
//		frame.pack();
//		frame.setVisible(true);
	}

	@Override
	public void pageFinished(WizardPage currentPage) {
		TransmittalTransferItemFactory stif = new TransmittalTransferItemFactory();
		if (currentPage == iv_start) {
			//ArrayList<TableData> selectedItems = ot_select.loadSelectedItems(stif);
			//deliverable_summary.setItems(selectedItems);
			iv_start.clickedOK();
			//this.clickedOK();
			Object client = iv_start.getClient();
			iv_select.setClient(client);
			iv_select.selectAll(true);
			
			Object date = iv_start.getDate();
			iv_select.setDate(date);
		}
		if (currentPage == iv_select) {
			Object id = iv_start.getInvoiceId();
			iv_select.setInvoiceId(id);
			iv_select.saveId(id);
			iv_set.setInvoiceId(id);
			iv_set.refreshSearch();
			iv_start.refreshSearch();
		}
		if (currentPage == iv_set) {
			Object id = iv_start.getInvoiceId();
			
			iv_print.setModel(iv_set.getModel());
			
			InvoiceHtmlFormatter ihf = new InvoiceHtmlFormatter(iv_start.getEditDbRowPane());
			ihf.setSheet(iv_print.getSheet());
			iv_print.setHtml(ihf.getHtml());
			iv_print.setInvoiceId(id);
		}
	}

	@Override
	public void setVisible(boolean b) {
		initialize();
		init();
		super.setVisible(false);
	}
}
