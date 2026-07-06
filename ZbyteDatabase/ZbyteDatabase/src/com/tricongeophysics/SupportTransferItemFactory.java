package com.tricongeophysics;

import java.sql.SQLException;
import java.util.ArrayList;

public class SupportTransferItemFactory extends ModelTransferItemFactory {

	@Override
	protected ModelTransferItem createItem(AbstractSpreadsheetModel model) {
		return new OriginalSupportItem();
	}

	

}
