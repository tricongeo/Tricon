package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SimpleField extends JPanel{

	public static int hAlignment = JLabel.LEFT;
	public static int preferredFieldWidth = 300;
	public static int preferredLabelWidth = 100;
	protected JTextField tf;
	protected JButton bb;
	protected Object v;
	protected ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	protected JLabel l;

	public SimpleField(String label, Object value) {
		this(label, value, new Dimension(preferredLabelWidth, 15));
	}

	public SimpleField(String label, Object value, Dimension labelDimension) {
		setName(label);
		l = new JLabel("<html>"+label+"</html>");
		v = value;
		tf = new JTextField();
		if (value != null) tf.setText(value.toString());
		bb = getButton();
		
		tf.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				fireAction(e);
			}});

		setLayout(new BorderLayout());
		l.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		l.setPreferredSize(labelDimension);
		l.setHorizontalAlignment(hAlignment);
		this.setPreferredSize(new Dimension(preferredFieldWidth, 25));
		addComponents();
	}

	protected void addComponents() {
		add(l, BorderLayout.WEST);
		add(tf, BorderLayout.CENTER);
		if (bb != null) add(bb, BorderLayout.EAST);
	}

	protected void setValue(Object object) {
		//v = object;
		if (object == null){
			tf.setText(null);
			return;
		}
		tf.setText(object.toString());
	}

	protected JButton getButton() {
		return null;
	}

	protected void fireAction(ActionEvent e) {
		for (ActionListener l: actionListeners) {
			l.actionPerformed(e);
		}
	}

	public void addActionListener(ActionListener l) {
		actionListeners.add(l);
		tf.addActionListener(l);
	}

	public Object getValue() {
		//return v;
		return tf.getText();
	}
	
	public void addButtonListener(ActionListener actionListener) {
		if (bb!=null) bb.addActionListener(actionListener);
	}

	public void setEditable(boolean b) {
		tf.setEditable(b);
		if(bb != null) bb.setEnabled(b);
	}
	
	public String toString() {
		return l.getText() + ": " + v;
	}

	public boolean isEditable() {
		return tf.isEditable();
	}

	public String getHtml() {
		v= getValue();
		if (v!=null)
			return l.getText() + ": " +v;
		else
			return l.getText() + ": ";
	}

	@Override
	public void setEnabled(boolean b) {
		tf.setEnabled(b);
		//l.setEnabled(b);
	}
}
