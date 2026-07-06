package com.tricongeophysics;

public class ZscanTransferItemFactory extends ModelTransferItemFactory {

	@Override
	protected ModelTransferItem createItem(AbstractSpreadsheetModel model) {
		return new OriginalZscanItem();
	}

}
