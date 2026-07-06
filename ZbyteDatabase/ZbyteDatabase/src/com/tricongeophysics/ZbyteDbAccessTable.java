package com.tricongeophysics;

import com.tricongeophysics.ZbyteDatabase.SecurityCategory;

public class ZbyteDbAccessTable {

	private static ZbyteDbAccessTable accessTable;
	private Key key;
	
	private ZbyteDbAccessTable() {
		key = new Key();
		key.addKey(SecurityCategory.Transmittal, ZbyteDatabase.WorkOrder);
		key.addKey(SecurityCategory.Transmittal, ZbyteDatabase.OutTransmittal);
		key.addKey(SecurityCategory.Transmittal, ZbyteDatabase.ZbyteTransmittal);
		key.addKey(SecurityCategory.Manager,  ZbyteDatabase.BillCode);
		key.addKey(SecurityCategory.CreateJob,  ZbyteDatabase.Client);
		key.addKey(SecurityCategory.CreateJob,  ZbyteDatabase.Contact);
		key.addKey(SecurityCategory.Transmittal,  ZbyteDatabase.ControlNum);
		key.addKey(SecurityCategory.UpdateProgress, ZbyteDatabase.ZbyteMedia);
		key.addKey(SecurityCategory.UpdateProgress, ZbyteDatabase.DeliverableOther);
		key.addKey(SecurityCategory.UpdateProgress, ZbyteDatabase.DeliverableMedia);
		key.addKey(SecurityCategory.UpdateProgress, ZbyteDatabase.DeliverableScan);
		key.addKey(SecurityCategory.UpdateProgress, ZbyteDatabase.DeliverableZscan);
		key.addKey(SecurityCategory.Transmittal, ZbyteDatabase.Format);
		key.addKey(SecurityCategory.CreateJob, ZbyteDatabase.Job);
		key.addKey(SecurityCategory.Manager, ZbyteDatabase.Location);
		key.addKey(SecurityCategory.Transmittal, ZbyteDatabase.Media);
		key.addKey(SecurityCategory.CreateJob, ZbyteDatabase.Office);
		key.addKey(SecurityCategory.Manager, ZbyteDatabase.Operator);
		key.addKey(SecurityCategory.Transmittal, ZbyteDatabase.OriginalMedia);
		key.addKey(SecurityCategory.Transmittal, ZbyteDatabase.OriginalHardCopy);
		key.addKey(SecurityCategory.Transmittal, "other_type");
		key.addKey(SecurityCategory.Manager, ZbyteDatabase.SalesRep);
		key.addKey(SecurityCategory.Manager, ZbyteDatabase.ScanType);
		key.addKey(SecurityCategory.Manager, ZbyteDatabase.SeisType);
		key.addKey(SecurityCategory.Transmittal, ZbyteDatabase.ShippingMethod);
		key.addKey(SecurityCategory.Transmittal, ZbyteDatabase.SupportType);
		key.addKey(SecurityCategory.Administrator, ZbyteDatabase.User);
	}

	public String getAccessCategory(String tableName) {
		return key.getValue(tableName);
	}

	public static ZbyteDbAccessTable getAccessTable() {
		if(accessTable == null) accessTable = new ZbyteDbAccessTable();
		return accessTable;
	}

}
