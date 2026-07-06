package com.tricongeophysics;

import java.sql.SQLException;
import java.util.ArrayList;

public class TransmittalSelectWizardPane extends MultiSheetSelectWizardPane {

	private String tableName;
	private String fieldName;

	public TransmittalSelectWizardPane(DbParms dbParms) throws SQLException {
		super(dbParms);
		tableName = dbParms.dbTable;
		fieldName = tableName.replace("_", " ") + ":";
		dbParms.dbTable = ZbyteDatabase.OriginalHardCopy;
		dbParms.query = "select * from " + ZbyteDatabase.OriginalHardCopy;
		ZbyteSelectSheet originalSupportSheet = new ZbyteSelectSheet(dbParms);
		originalSupportSheet.setVisibleColumns(new String[] {"Select", "Barcode", "Image File", "Line #", "Media:", "Support Type:", "Job:", "State", "County", "id", "Control Num:", "Comment",
				fieldName});
		DbSheetSelectBarcodeListener originalSupportBarcodeListener = new DbSheetSelectBarcodeListener("Barcode", originalSupportSheet);
		originalSupportSheet.addBarcodeListener(originalSupportBarcodeListener);
		addSheet(originalSupportSheet);
		
		dbParms.dbTable = ZbyteDatabase.OriginalMedia;
		dbParms.query = "select * from " + ZbyteDatabase.OriginalMedia;
		ZbyteSelectSheet originalSeismicSheet = new ZbyteSelectSheet(dbParms);
		originalSeismicSheet.setVisibleColumns(new String[] {"Select", "Barcode", "Image File", "Line #", "Media:", "Media Id", "Seis Type:", "Job:", "State", "County", "id", "Control Num:", "Comment",
				fieldName});
		DbSheetSelectBarcodeListener originalSeismicBarcodeListener = new DbSheetSelectBarcodeListener("Barcode", originalSeismicSheet);
		originalSeismicSheet.addBarcodeListener(originalSeismicBarcodeListener);
		addSheet(originalSeismicSheet);
		
		dbParms.dbTable =  ZbyteDatabase.ControlNum;
		dbParms.query = "select * from  " + ZbyteDatabase.ControlNum;
		ZbyteSelectSheet controlNumSheet = new ZbyteSelectSheet(dbParms);
		controlNumSheet.setVisibleColumns(new String[] {"Select", "id", "Client Box#", "Client Item Count", "Actual Item Count", "Location:", "Job:", 
				fieldName , "Doc. File", "Comment"});
		DbSheetSelectBarcodeListener controlNumBarcodeListener = new DbSheetSelectBarcodeListener("id", controlNumSheet);
		controlNumSheet.addBarcodeListener(controlNumBarcodeListener);
		addSheet(controlNumSheet);
		
		dbParms.dbTable = ZbyteDatabase.ZbyteMedia;
		dbParms.query = "select * from " + ZbyteDatabase.ZbyteMedia;
		ZbyteSelectSheet delivMediaSheet = new ZbyteSelectSheet(dbParms);
		delivMediaSheet.setVisibleColumns(new String[] {"Select", "id", "Media:", "Description", fieldName, "Job:", "Creation Date", "Comment"});
		DbSheetSelectBarcodeListener delivMediaBarcodeListener = new DbSheetSelectBarcodeListener("id", delivMediaSheet);
		delivMediaSheet.addBarcodeListener(delivMediaBarcodeListener);
		addSheet(delivMediaSheet);
		
		setPkeySearchField("Job:");
	}

	public void saveToDatabase(String transmittalID) {
		for (ZbyteSelectSheet sheet:selectSheets ) {
			sheet.setSelectedItems(fieldName, transmittalID);
		}
		
	}
}
