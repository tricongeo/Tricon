package com.tricongeophysics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;

import com.qt.datapicker.DatePicker;

public class DateField extends SimpleField implements Observer {

	private DatePicker dp;
	private DateFormat sdf;

	public DateField(String label, Object value) {
		super(label, value);
		dp = new DatePicker(DateField.this, Locale.US);
		sdf = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.US);
		tf.setColumns(8);
	}
	
	@Override
	protected JButton getButton() {
		DateButton b = new DateButton();
		b.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
                // previously selected date
                Date selectedDate = dp.parseDate(tf.getText());
                dp.setSelectedDate(selectedDate);
                dp.start(DateField.this);
			}});
		return b;
	}

	@Override
	public void update(Observable o, Object arg) {
		Calendar calendar = (Calendar) arg;
        DatePicker dp = (DatePicker) o;
        
        String s = dp.formatDate(calendar);
        setValue(s);
        //tf.setText(s);
        fireAction(null);
	}
	
	@Override
	public void setValue(Object value) {
		v = value;
		if (value == null) {
			tf.setText(null);
			return;
		}
		try {
			java.sql.Date dd = java.sql.Date.valueOf(value.toString());
			value = new Date(dd.getTime());
		} catch (Exception e) {}

		try {
			String s = sdf.format(value);
			tf.setText(s);
			return;
		} catch(Exception e) {
		}
        tf.setText(value.toString());
	}
	
	@Override
	public Object getValue() {
		v = tf.getText();
		v = dp.parseDate(v.toString());
		return v;
	}

}
