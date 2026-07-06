package com.tricongeophysics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.BoxLayout;
import java.awt.FlowLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.tricongeophysics.FocusProject.Status;

public class FocusProjectBrowser extends JDialog {
	private static final String DefaultFileLoc = ".focusDefaults";
	private static final String PgRoot = "PgRoot";
	private static final String ProjectName = "ProjectName";
	private static final String LineName = "LineName";
	private JTextField textField;
	private JButton browseButton = new BrowseButton();
	private JList projects;
	private JList lines;
	private final JPanel panel_1 = new JPanel();
	private String pgRoot;
	private JButton cancelButton;
	private ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private JButton openButton;
	private FocusProject project;
	private String line;
	private JPanel background;
//	private DefaultTableModel projectModel;

	/**
	 * Create the panel.
	 * @param buttonText 
	 */
	public FocusProjectBrowser(boolean show, String buttonText) {
		background = new JPanel();
		background.setBorder(new EmptyBorder(5, 5, 5, 5));
		background.setLayout(new BorderLayout(5, 5));
		background.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		Component horizontalGlue = Box.createHorizontalGlue();
		panel_1.add(horizontalGlue);
		
		cancelButton = new JButton("Cancel");
		panel_1.add(cancelButton);
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				fireCanceled();
			}});
		
		openButton = new JButton(buttonText);
		panel_1.add(openButton);
		openButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				fireOpened();
			}});
		openButton.setEnabled(false);
		
		JPanel panel = new JPanel();
		background.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JLabel pgRootLabel = new JLabel("PGROOT:");
		panel.add(pgRootLabel);
		
		textField = new JTextField();
		textField.setEditable(false);
		panel.add(textField);
		textField.setColumns(10);
		
		panel.add(browseButton);
		browseButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String root = TriconFileChooser.launchDirChooser(null, FocusProjectBrowser.this);
				if (root != null) {
					setPgRoot(root);
				}
			}});
		
		JSplitPane splitPane = new JSplitPane();
		background.add(splitPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		projects = new JList();
		projects.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				updateProject(projects.getSelectedValue()+"");
			}});
		
		//splitPane.setLeftComponent(projects);
		splitPane.setLeftComponent(scrollPane);
		
		scrollPane.setViewportView(projects);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		splitPane.setRightComponent(scrollPane_1);
		
		lines = new JList();
		scrollPane_1.setViewportView(lines);
		lines.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				updateLine(lines.getSelectedValue()+"");
			}});
		
		getContentPane().add(background);
		if (show) {
			pack();
			setVisible(true);
		}
		loadDefaults();
	}

	public FocusProjectBrowser(String buttonText) {
		this(true, buttonText);
		openButton.setText(buttonText);
	}

	private void loadDefaults() {
		String homeDir = System.getenv("HOME");
        String defaultFileLoc = homeDir+File.separator+DefaultFileLoc;
        Properties p = new Properties();
        String pgSurveyRoot = getPgRootEnvVar();
        try {
            p.load(new FileInputStream(defaultFileLoc));
            setPgRoot(pgSurveyRoot);
        } catch (Exception e1) {
//            e1.printStackTrace();
            return;
        } 
        pgRoot = p.getProperty(PgRoot);
        if (pgRoot == null) pgRoot = pgSurveyRoot;
        String projectName = p.getProperty(ProjectName);
        String lineName = p.getProperty(LineName);
        updateProjectList();
//        updateProject(projectName);
//        updateLine(lineName);
        projects.setSelectedValue(projectName.toLowerCase(), true);
        lines.setSelectedValue(lineName, true);
        textField.setText(pgRoot);
	}

	protected void updateLine(String lineName) {
		if (project == null ) return;
		project.setSelectedLine(lineName);
		//line = project.getSelectedLine()+"";
		line = lineName;
		openButton.setEnabled(true);
	}

	protected void updateProject(String projectName) {
		project = new FocusProject(projectName, pgRoot);
		Status status = project.getFocusStatus();
		if (status == Status.OK)
			lines.setListData(project.getSeismicLineNames());
		else
			lines.setListData(new String[]{"ERROR: Corrupt Project"});
	}

	protected void fireOpened() {
		String pname = (project == null) ? null : project.getName();
	    storeDefaults(pgRoot,pname, line);
		for (ActionListener l: actionListeners) {
			l.actionPerformed(null);
		}
		this.setVisible(false);
		this.dispose();
	}

	protected void fireCanceled() {
		project = null;
		line = null;
		fireOpened();
	}

	protected void setPgRoot(String root) {
		pgRoot = root;
		textField.setText(pgRoot);
		updateProjectList();
	}

	private void updateProjectList() {
		String[] list = FocusProject.getProjectList(pgRoot);
		if (list == null) {
			System.err.println("No Projects Found! PG_ROOT="+pgRoot);
			return;
		}
		Arrays.sort(list);
//		String[][] list2 = new String[list.length][1];
//		for (int i=0; i<list.length; i++) {
//			list2[i][0] = list[i];
//		}
////		projectModel = new DefaultTableModel(list2, new String[]{"Project"});
//		projects.setModel(projectModel);
		projects.setListData(list);
	}

	static public void main(String[] args) {
		//JFrame frame = new JFrame();
		final FocusProjectBrowser fpb = new FocusProjectBrowser(true, "Select");
		fpb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.println("you selected project: " + fpb.project + " line: " + fpb.line);
				System.exit(0);
			}});
		
//		frame.getContentPane().add(fpb);
//		frame.pack();
//		frame.setVisible(true);
		
	}

	public void addActionListener(ActionListener l) {
		actionListeners.add(l);
	}

	public String getProject() {
		return project+"";
	}

	public String getLine() {
		return line;
	}
	
	public static void storeDefaults(String pgRoot, String project, String line)
    {
		if (project == null || line == null) return;
		if (pgRoot == null) {
			pgRoot = getPgRootEnvVar();
		}
	    String homeDir = System.getenv("HOME");
        String defaultFileLoc = homeDir+File.separator+DefaultFileLoc;
        Properties p = new Properties();
        p.put(PgRoot, pgRoot);
        p.put(ProjectName, project);
        p.put(LineName, line);
        try {
            p.store(new FileOutputStream(defaultFileLoc), null);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

	private static String getPgRootEnvVar() {
		return System.getenv("PG_SURVEY_ROOT");
	}

	public JPanel getBackgroundPane() {
		return background;
	}

	public String getPgRoot() {
		return pgRoot;
	}

}
