package com.tricongeophysics;

import java.sql.SQLException;
import java.util.ArrayList;

public abstract class ModelTransferItemFactory {

	public TableData create(AbstractSpreadsheetModel model, int row) {
		ModelTransferItem item = createItem(model);
		
		item.setFromModel(model,row );
		
		return item;
	}

	protected abstract ModelTransferItem createItem(AbstractSpreadsheetModel model);

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
