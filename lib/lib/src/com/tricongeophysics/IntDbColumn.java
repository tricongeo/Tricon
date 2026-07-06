package com.tricongeophysics;

import java.sql.ResultSet;

public class IntDbColumn extends DbColumn
{

	
	
    public IntDbColumn(ResultSet rs, int i, String name)
    {
       super(rs, i, name);
    }

    @Override
    public Object getValueAt(int row) throws Exception {
		resultSet.absolute(row);
		//if (resultSet.getConcurrency() == ResultSet.CONCUR_UPDATABLE)
			//resultSet.refreshRow();  //make sure data is synced w/ database before showing!!
		int val = resultSet.getInt(columnIndex);
		return val;
	}
	
}
