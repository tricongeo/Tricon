package com.tricongeophysics;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MapperInputFilesPaneTest extends JPanel {
	
	String[] receiverFiles;
	String[] shotFiles;
	String[] obFiles;
	
	MapperInputFilesPaneTest() {
		receiverFiles = new String[1];
		ReceiverBox receiverBox = new ReceiverBox(receiverFiles);
		add(receiverBox);
		
	}
	
	class ReceiverBox extends InputFilesBox {

		public ReceiverBox(String[] receiverFiles) {
			super(receiverFiles);
		}
		
	}
	
	class InputFilesBox extends JPanel {
		
		String[] files;
		JComboBox comboBox;
		JButton addButton;
		JButton delButton;

		public InputFilesBox(String[] files) {
			this.files = files;
			comboBox = new JComboBox(files);
			comboBox.setPreferredSize(new Dimension(700,25));
			addButton = new JButton("+");
			delButton = new JButton("-");
			add(comboBox);
			add(addButton);
			add(delButton);
		}
		
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MapperInputFilesPaneTest mifpt = new MapperInputFilesPaneTest();
		frame.getContentPane().add(mifpt);
		frame.pack();
		frame.setVisible(true);
	}

}
