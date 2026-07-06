package com.tricongeophysics;

public interface DbRowCopier {

	void copyFromTo(int fromRow, int toRow, DatabaseModel model);

}
