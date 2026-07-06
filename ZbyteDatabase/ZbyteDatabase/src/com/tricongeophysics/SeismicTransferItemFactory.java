package com.tricongeophysics;


public class SeismicTransferItemFactory extends ModelTransferItemFactory {

	@Override
	protected ModelTransferItem createItem(AbstractSpreadsheetModel model) {
		return new OriginalSeismicItem();
	}

}
