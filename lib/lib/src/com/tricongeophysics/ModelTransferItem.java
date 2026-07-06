package com.tricongeophysics;

public interface ModelTransferItem extends TableData {

	void setFromModel(AbstractSpreadsheetModel model, int row);

	void setModelToItem(DatabaseModel model, int row);

}
