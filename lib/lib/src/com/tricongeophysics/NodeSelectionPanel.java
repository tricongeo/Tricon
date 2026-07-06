package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


@SuppressWarnings("serial")
public class NodeSelectionPanel extends JPanel {

	private static String[] Extensions = new String[]{"nodes"};
	ArrayList <MachineNodePanel> nodePanels;
	JPanel nodesPane;
	JPanel buttonPane;
	JButton addButton;
	JButton removeButton;
	private boolean nodeDebug = true;
	
	public NodeSelectionPanel(String[] names) {
		//...Setup nodesPane
		nodesPane = new JPanel();
		nodesPane.setLayout(new BoxLayout(nodesPane, BoxLayout.Y_AXIS));
		nodesPane.setBorder(BorderFactory.createEtchedBorder());
		setNodePanels(names);
		
		//...Setup buttonsPanel
		buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.add(Box.createHorizontalGlue());
		
		//...Add Button
		addButton = new JButton("+");
		addButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String node = (String)JOptionPane.showInputDialog(
	                    NodeSelectionPanel.this,
	                    "Node Name",
	                    "Add Node Name",
	                    JOptionPane.QUESTION_MESSAGE);
				if (node != null) {
					NodeSelectionPanel.this.addNode(node);
				}
			}
		});
		buttonPane.add(addButton);
		
		//...Remove Button
		removeButton = new JButton("-");
		removeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(NodeSelectionPanel.this, "doesn't work");
			}
		});
		buttonPane.add(removeButton);
		
		//...Put it together
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(nodesPane), BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.NORTH);
	}
	
	private void addNode(String node) {
		nodePanels.add(new MachineNodePanel(new MachineNode(node)));
		refreshPanels();
	}

	private void setNodePanels(String[] names) {
		MachineNode[] nodes = new MachineNode[names.length];
		for (int i=0; i< nodes.length; i++ ) {
			nodes[i] = new MachineNode(names[i]);
		}
		
		setNodePanels(nodes);
	}

	private void setNodePanels(MachineNode[] nodes) {
		nodePanels = new ArrayList <MachineNodePanel>();
		for (int i=0; i< nodes.length; i++ ) {
			nodePanels.add(new MachineNodePanel(nodes[i]));
		}
		
		refreshPanels();
	}
	
	private void refreshPanels() {
		nodesPane.removeAll();
		for (int i=0; i<nodePanels.size(); i++) {
			nodesPane.add(nodePanels.get(i));
		}
		nodesPane.revalidate();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		NodeSelectionPanel nsp = new NodeSelectionPanel(new String[]{"ted", "fred"});
		frame.getContentPane().add(nsp);
		frame.pack();
		frame.setVisible(true);

	}
	
	public MachineNode[] getNodes() {
		MachineNode[] nodes = new MachineNode[nodePanels.size()];
		for (int i=0; i< nodes.length; i++) {
			nodes[i] = nodePanels.get(i).getNode();
		}
		return nodes;
	}

	public void LoadNodeListFromFile(String nodeListFileName) throws IOException {
		if (nodeDebug ) System.out.println("reading file " + nodeListFileName);
	}

	public static String[] getFileExtensions() {
		return Extensions ;
	}

	public void SaveNodeListToFile(String nodeListFileName) throws IOException {
		if (nodeDebug ) System.out.println("writing to file " + nodeListFileName);
		TriconFile file = new TriconFile(nodeListFileName);
		String[] names = new String[nodePanels.size()];
		
		for (int i=0; i< nodePanels.size(); i++ ) {
			names[i] = nodePanels.get(i).getNode().getName();
		}
		file.write(names);
	}

}
