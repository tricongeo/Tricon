package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * 
 * @author scott
 * class for running a process on the operating system and displaying results to JDialog type window
 */

public class SpawnProcess extends JDialog implements Runnable {
	protected JTextArea textArea;
	protected JScrollPane scrollPane;
	protected JButton okButton;
	protected JButton cancelButton;
	protected Process process;
	protected String output = "";
	protected String input = "";
	protected String[] commandList; 
	protected File directory;
	
	public SpawnProcess(String title) {
		super();
		this.setTitle(title);
	}
	
	public SpawnProcess(String title, String[] commands, File dir) {
		super();
		this.setTitle(title);
		this.commandList = commands;
		this.directory = dir;
	}
	
	public static void main(String[] args) {
		ArrayList commands = new ArrayList();
		//commands.add("bash");
		//commands.add("which");
		//commands.add("gbuild");
		commands.add("gbuild");
		commands.add("-h");
		SpawnProcess sp = new SpawnProcess("Run GeoBuild");
		sp.showDialog();
		String gbuildcommand="";
		try {
			gbuildcommand=sp.runProcess(commands,new File("/export/home/scott"));
		}
		catch (Exception e) {
			System.err.println(e);
		}
		commands.clear();
		commands.add(gbuildcommand.trim());
		commands.add("-q");
		try {
		System.out.println(sp.runProcess(commands,new File("/export/home/scott")));
		}
		catch (Exception e) {
			System.err.println(e);
		}
	}
	
	public void showDialog(){
		JPanel background = new JPanel(new BorderLayout());
		background.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		//make text area
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("courier",Font.PLAIN,14));
		textArea.setText("executing command.... \n");
		
		//make OK Button
		okButton = new JButton("OK");
		okButton.setEnabled(false); //enable when process finishes
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		
//		make cancel Button
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				kill();
			}
		});
		
		//make scroll pane
		scrollPane = new JScrollPane(textArea);
		
		//put it all together
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		background.add(BorderLayout.CENTER, scrollPane);
		background.add(BorderLayout.SOUTH, buttonPanel);
		
		//display window
		this.getContentPane().add(background);
		this.setSize(new Dimension(600,500));
		this.setVisible(true);
	}
	
	/**
	   * Returns the output of a system process
	   */
	public String runProcess(ArrayList<String> commands,File dir)  {
		commandList = commands.toArray(new String[commands.size()]);
		return runProcess(commandList, dir);
	}
	
	/**
	   * Returns the output of a system process
	   */
	public String runProcess(String[] commands,File dir)  {
		directory = dir;
		this.commandList = commands;
		input = commandList[0];
		for (int i=1; i<commandList.length; i++) {
			input += " "+commandList[i];
		}
		
		return runProcess();
	}
	
	public String runProcess()  {		
		if (textArea != null) textArea.append("\ntrying \""+input+"\" ...");
		if (okButton != null) okButton.setEnabled(false); //ok button disabled until process finishes
		new Thread(this).start(); //run process in separate thread so that textArea updates real-time
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return output.trim(); //get rid of any unnecessary carriage returns at end of string
	}
	
	public void kill(){
		if (process!=null)
			process.destroy();
		setVisible(false);
		dispose();
	}

	public void run() {
		try {
			ProcessBuilder pb = new ProcessBuilder(commandList);
			pb.redirectErrorStream(true);
			pb.directory(directory);
			process = pb.start();
			
			//while process is running, send output to textArea
			BufferedReader outReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line="";
			while ( (line = outReader.readLine()) != null){
				output = output+line+"\n";
				if (textArea != null) textArea.append("\n"+line);
				if (scrollPane != null) scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());//scroll to end of page
			}
			
			//once process is done, re-enable OK button and return full string of output
			process.waitFor();
			if (textArea != null) textArea.append("\n\nDone...\n");
			if (okButton != null) okButton.setEnabled(true);  //process has finished, user can now exit cleanly w/ OK!
			return;
		}
		catch (Exception e) {
			output = output+e.toString()+"\n";
			if (textArea != null) textArea.append("\n"+e.toString());
			return;
		}
	}

	public void appendText(String string) {
		textArea.append(string + "\n");
	}
}
