package com.tricongeophysics;

import java.awt.Dimension;

public class IntField extends SimpleField {

	private int num;

	public IntField(String label, Object value) {
		super(label, value);
		// TODO Auto-generated constructor stub
	}

	public IntField(String label,Object value , Dimension labelDimension) {
		super(label, value, labelDimension);
	}

//	@Override
//	protected void setValue(Object object) {
//		num = 0;
//		if (object != null) {
//			num = (int) SUtil.sval(object.toString());
//		}
//		tf.setText(num+"");
//	}
	
	public int getIntValue() {
		num = (int) SUtil.sval(tf.getText());
		return num;
	}
	
}
