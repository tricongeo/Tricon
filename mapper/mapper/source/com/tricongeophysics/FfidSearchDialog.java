package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.*;

public class FfidSearchDialog extends JDialog{

//	protected StationPlotter stationPlotter;
	protected JComboBox comboFfidList;
	protected String ffidMessage;
	//protected ComboBoxModel ffidList;
	protected Object[] ffidList;
	/**
	 * @param colName 
	 * @param args
	 */
	public FfidSearchDialog(int[] a, String colName) {
		super();
		Arrays.sort(a);
		ffidList = new Object[a.length];
		for (int i=0;i<ffidList.length;i++)ffidList[i]=a[i]+"";
		this.setTitle("Select "+ colName);
		ffidMessage="Select "+colName+": ("+a[0]+"-"+a[a.length-1]+")";
		
		//make combobox
        comboFfidList = new JComboBox(ffidList);
        comboFfidList.setMaximumRowCount(20);
	}
	
	public static void main(String[] args) {
		int[] list = {1,2,5,4,5,6,11,8,9};
		//String[] list = new String[1000];
		//for (int i=0;i<list.length;i++) list[i]=i+"";
		FfidSearchDialog fsd = new FfidSearchDialog(list,"FFID");
		fsd.go();
	}
	
	public void go() {
		JPanel background = new JPanel(new BorderLayout());
		background.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		//make OK button
		JButton okButton = new JButton("OK");
		okButton.setToolTipText("Close Window");
		okButton.setForeground(Color.GREEN.darker().darker());
		okButton.setFont(new Font("arial",Font.BOLD,14));
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		
		//put it together
		background.add(BorderLayout.NORTH,new JLabel(ffidMessage));
		background.add(BorderLayout.CENTER,comboFfidList);
		JPanel okPanel = new JPanel();
		okPanel.add(okButton);
		background.add(BorderLayout.SOUTH,okPanel);
		this.getContentPane().add(background);
		
		this.setModal(false); //allow station stationPlotter to still function while dialog is visible
		//this.pack();
		this.setSize(new Dimension(200,125));
		this.setVisible(true);
	}

    public void addActionListener(ActionListener actionListener)
    {
        comboFfidList.addActionListener(actionListener);
    }

    public Object getSelectedItem()
    {
        return comboFfidList.getSelectedItem();
    }
	
//	public void setStationPlotter(StationPlotter sp){
//		stationPlotter = sp;
//	}

}
