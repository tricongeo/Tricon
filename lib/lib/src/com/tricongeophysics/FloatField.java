package com.tricongeophysics;

import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.JTextField;

public class FloatField extends SimpleField {

	private Number num;
	private DecimalFormat df = new DecimalFormat("####0.00");

	public FloatField(String label, Object value) {
		super(label, value);
		// TODO Auto-generated constructor stub
	}

	public FloatField(String label, Object val, Dimension labelDimension) {
		super(label, val, labelDimension);
	}

	@Override
	protected void setValue(Object object) {
		num = 0;
		if (object instanceof Number) {
			num = (Number) object;
		}
		else if (object != null) {
			num = SUtil.sval(object.toString());
		}
		v = num;
		tf.setText(df.format(num));
		tf.setHorizontalAlignment(JTextField.LEFT);
	}
	
	public double getDoubleValue() {
		num = SUtil.sval(tf.getText());
		return (Double) num;
	}
	
}
