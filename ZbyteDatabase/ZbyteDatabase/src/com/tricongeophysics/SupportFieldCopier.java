package com.tricongeophysics;

public class SupportFieldCopier extends AbstractDbRowCopier {

	@Override
	public String[] getColumnNamesToCopy() {
		return new String[] { "Control Num:", "County", "State", "Support Type:", "Media:", "Location:", "Operator:"};
	}

}
