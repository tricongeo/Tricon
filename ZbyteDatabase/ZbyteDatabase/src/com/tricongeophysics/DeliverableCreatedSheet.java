package com.tricongeophysics;

import java.sql.SQLException;
import java.util.Date;

public class DeliverableCreatedSheet extends ZbyteDbSpreadsheetPanel {
	
	DeliverableCreatedSheet(DbParms p) throws SQLException {
		super(p);
		//this.setName("Deliverable Support");
	}
	
	@Override
	protected DatabaseModelSearchPane makeSearchPane() {
		DualSearchPane dsp = new DualSearchPane((DatabaseModel) model, dbParms.dbTable, "Job:");
		dsp.setPrimarySearchField("Date Finished");
		dsp.setPrimaryFieldLabel("Outgoing Job:");
		return dsp;
	}

	public void setDate(Object date) {
		this.setSecondarySearchValue(date);
	}
	
//	@Override
//	public Component getRowButtonPane() {
//		Component cp = this.getCopyPastePane(BoxLayout.Y_AXIS);
//		Component rp = this.getEditRowButton();
//		JPanel pane = new JPanel();
//		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
//		pane.add(cp);
//		pane.add(Box.createVerticalGlue());
//		pane.add(rp);
//		pane.add(Box.createVerticalGlue());
//		
//		return pane;
//	}

}
