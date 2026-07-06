package com.tricongeophysics;

import java.sql.SQLException;

public class InvoiceSelectWizardPane extends MultiSheetSelectWizardPane {

	private String tableName;
	private String fieldName;

	public InvoiceSelectWizardPane(DbParms dbParms) throws SQLException {
		super(dbParms);
		tableName = dbParms.dbTable;
		fieldName = tableName.replace("_", " ") + ":";
		dbParms.dbTable = ZbyteDatabase.DeliverableScan;
		dbParms.query = "select * from " + ZbyteDatabase.DeliverableScan;
		ZbyteSelectSheet deliverableSupportSheet = new ZbyteSelectSheet(dbParms);
		//deliverableSupportSheet.setVisibleColumns(new String[] {"Select", "Barcode", "Image File", "Line #", "Media:", "Media Id", "Support Type:", "Job:", "State", "County", "id", "Control Num:", "Comment",
		//		fieldName});
		addSheet(deliverableSupportSheet);
		
		dbParms.dbTable = ZbyteDatabase.DeliverableMedia;
		dbParms.query = "select * from " + ZbyteDatabase.DeliverableMedia;
		ZbyteSelectSheet deliverableSeismicSheet = new ZbyteSelectSheet(dbParms);
		//deliverableSeismicSheet.setVisibleColumns(new String[] {"Select", "Barcode", "Image File", "Line #", "Media:", "Media Id", "Seis Type:", "Job:", "State", "County", "id", "Control Num:", "Comment",
		//		fieldName});
		addSheet(deliverableSeismicSheet);
		
		dbParms.dbTable = ZbyteDatabase.DeliverableZscan;
		dbParms.query = "select * from " + ZbyteDatabase.DeliverableZscan;
		ZbyteSelectSheet zscanSheet = new ZbyteSelectSheet(dbParms);
//		zscanSheet.setVisibleColumns(new String[] {"Select", "id", "Client Box#", "Client Item Count", "Actual Item Count", "Location:", "Job:", 
//				fieldName , "Doc. File", "Comment"});
		addSheet(zscanSheet);
		
		dbParms.dbTable = ZbyteDatabase.ZbyteMedia;
		dbParms.query = "select * from " + ZbyteDatabase.ZbyteMedia;
		ZbyteSelectSheet delivMediaSheet = new ZbyteSelectSheet(dbParms);
		//delivMediaSheet.setVisibleColumns(new String[] {"Select", "id", "Media:", "Description", fieldName, "Job:", "Creation Date", "Comment"});
		addSheet(delivMediaSheet);
		
		dbParms.dbTable = ZbyteDatabase.DeliverableOther;
		dbParms.query = "select * from " + ZbyteDatabase.DeliverableOther;
		ZbyteSelectSheet delivOtherSheet = new ZbyteSelectSheet(dbParms);
		//delivMediaSheet.setVisibleColumns(new String[] {"Select", "id", "Media:", "Description", fieldName, "Job:", "Creation Date", "Comment"});
		addSheet(delivOtherSheet);
		
		setPkeySearchField("Job:");
	}

	public void saveToDatabase(String transmittalID) {
		for (ZbyteSelectSheet sheet:selectSheets ) {
			sheet.setSelectedItems(fieldName, transmittalID);
		}
		
	}
}
