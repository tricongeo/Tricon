package com.tricongeophysics;

import java.sql.SQLException;
import java.util.ArrayList;

public class TransmittalTransferItemFactory extends ModelTransferItemFactory {


	@Override
	protected ModelTransferItem createItem(AbstractSpreadsheetModel model) {
		if (model.getTableName().equals(ZbyteDatabase.OriginalHardCopy)) {
			return new SupportTransmittalItem();
		}
		if (model.getTableName().equals(ZbyteDatabase.OriginalMedia)) {
			return new SeismicTransmittalItem();
		}
		if (model.getTableName().equals( ZbyteDatabase.ControlNum)) {
			return new ControlNumberTransmittalItem();
		}
		if (model.getTableName().equals(ZbyteDatabase.ZbyteMedia)) {
			return new DeliverableMediaTransmittalItem();
		}
		
		return new TransmittalItem();
	}
	
	public AbstractSpreadsheetModel addItemsToDatabase(DatabaseModel model, ArrayList<TableData> tableData) throws SQLException {
		for (TableData td: tableData) {
			ModelTransferItem mti = (ModelTransferItem) td;
			model.addRow(1);
			int row = model.getRowCount() - 1;
			mti.setModelToItem(model, row);
		}
		return model;
	}

}
