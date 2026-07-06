package com.tricongeophysics;

public class SeismicFieldCopier extends AbstractDbRowCopier {

	@Override
	public String[] getColumnNamesToCopy() {
		return new String[] { "Control Num:", "County", "State", "Seis Type:", "Media:", "Location:", "Format:", "Operator:"};
	}

}
