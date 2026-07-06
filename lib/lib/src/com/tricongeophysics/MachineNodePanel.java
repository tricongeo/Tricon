package com.tricongeophysics;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MachineNodePanel extends JPanel implements ActionListener, MouseListener {
	
	protected MachineNode node;
	protected JLabel label;
	protected JCheckBox checkBox;
	protected boolean selected;
	private boolean nodeDebug = true;
	
	public MachineNodePanel (MachineNode node) {
		super();
		this.node = node;
		label = new JLabel(node.getName());
		checkBox = new JCheckBox();
		checkBox.addActionListener(this);
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(checkBox);
		this.add(Box.createHorizontalGlue());
		this.add(label);
		this.setBorder(BorderFactory.createEtchedBorder());
		this.addMouseListener(this); //make it so that you just have to click in panel (not just check box)
	}
	
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		selected = checkBox.isSelected();
		if (nodeDebug ) System.out.println("node " + node + " is selected?: " + selected);
	}

	public MachineNode getNode() {
		return node;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		checkBox.doClick();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
