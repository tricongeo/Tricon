package com.tricongeophysics;

import java.sql.SQLException;

public class DeliverableSelectSheet extends ZbyteSelectSheet {

	private DbSheetSelectEditBarcodeItemListener deliverableBarcodeListener;
	private DualSearchPane dsp;

	DeliverableSelectSheet(DbParms p) throws SQLException {
		super(p);
		// TODO Auto-generated constructor stub
		deliverableBarcodeListener = new DbSheetSelectEditBarcodeItemListener("Barcode", this);
		this.addBarcodeListener(deliverableBarcodeListener);
	}

	@Override
	protected DatabaseModelSearchPane makeSearchPane() {
		dsp = new DualSearchPane((DatabaseModel) model, dbParms.dbTable, "Job:");
		dsp.setPrimarySearchField("Control Num:");
		dsp.setPrimaryFieldLabel("Incoming Job:");
		return dsp;
	}

	public String getJobNum() {
		return dsp.getPrimarySearchValue() +"";
	}
	
}
