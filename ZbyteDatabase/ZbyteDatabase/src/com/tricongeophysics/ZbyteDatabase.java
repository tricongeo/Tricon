package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.tricongeophysics.DbSecurityManager.AccessLevel;

public class ZbyteDatabase extends JApplet 
{
	private static final String Launch = "Launch";
	private static final String Login = "Login";
	private JPasswordField passwordField;
	private JTextField usernameField;
	private ArrayList<DbLaunchButton> buttonList;
	private DbSecurityManager manager;
	private JLabel messageLabel;
	private CardLayout cardLayout;
	private JPanel launchPane;
	private JPanel loginPane;
	private JButton logout;
	private JPanel cardPanel;
	private JLabel userLabel;
	public enum SecurityCategory {Transmittal, UpdateProgress, CreateJob, Manager, Administrator};
	
	public static final String ZbyteMedia = "zbyte_media";
	public static final String DeliverableMedia = "deliverable_media";
	public static final String OriginalHardCopy = "original_hardcopy";
	public static final String OriginalMedia = "original_media";
	public static final String DeliverableScan = "deliverable_scan";
	public static final String Original_Media = "Original Media:";
	public static final String Original_HardCopy = "Original HardCopy:";
	public static final String WorkOrder = "work_order";
	public static final String OutTransmittal = "out_transmittal";
	public static final String ZbyteTransmittal = "zbyte_transmittal";
	public static final String BillCode = "bill_code";
	public static final String Client = "client";
	public static final String Contact = "contact";
	public static final String ControlNum = "control_num";
	public static final String DeliverableOther = "deliverable_other";
	public static final String DeliverableZscan = "deliverable_zscan";
	public static final String Format = "format";
	public static final String Job = "job";
	public static final String Location = "location";
	public static final String Media = "media";
	public static final String Office = "office";
	public static final String Operator = "operator";
	public static final String SalesRep = "sales_rep";
	public static final String ScanType = "scan_type";
	public static final String SeisType = "seis_type";
	public static final String ShippingMethod = "shipping_method";
	public static final String SupportType = "support_type";
	public static final String User = "user";
	public static final String InTransmittal = "in_transmittal";

	@Override
	public void init() {
		Container c = this.getContentPane();
		
		cardLayout = new CardLayout();

		manager = DbSecurityManager.getManager();
		
		buttonList = new ArrayList<DbLaunchButton>();
		
		try {
			String test = this.getParameter("Test");
			DbParms.test = test;
		} catch (Exception e) { 
			//e.printStackTrace();
		};
		
		SUtil.print("Test is: " + DbParms.test);		
		
		messageLabel = new JLabel();
		
		JPanel outerpane = new JPanel();
		outerpane.setLayout(new BoxLayout(outerpane, BoxLayout.Y_AXIS));
		//outerpane.add(getPasswordPane());
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(getButtonPane());
		tabbedPane.add(getAccountPane());
		
		outerpane.add(tabbedPane);
		outerpane.add(getLogoutPane());
		//outerpane.add(messageLabel);
		
		launchPane = new JPanel();
		launchPane.add(outerpane);
		
		loginPane = new JPanel();
		loginPane.setLayout(new BoxLayout(loginPane, BoxLayout.Y_AXIS));
		loginPane.add(getPasswordPane());
		loginPane.add(messageLabel);
		
		cardPanel = new JPanel();
		cardPanel.setLayout(cardLayout);
		JPanel buffer = new JPanel();
		buffer.add(loginPane);
		cardPanel.add(buffer, Login);
		cardPanel.add(launchPane, Launch);
		
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(getIconPane());
		c.add(cardPanel);
		
		enableButtons();		
		cardLayout.show(cardPanel, Login);
	}

	private Component getAccountPane() {
		JPanel p = new JPanel();
		p.setName("Account Settings");
		
		userLabel = new JLabel();
		
		p.add(userLabel);
		
		JButton b = new JButton("Change Password");
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				DbParms dbParms = DbParms.getParms("ZbyteDatabase");
				dbParms.dbTable = ZbyteDatabase.User;
				dbParms.query = "Select * from " + ZbyteDatabase.User;
				DatabaseModel model;
				try {
					model = DatabaseModel.getDatabaseModel(dbParms);
					manager.launchChangePasswordDialog(model, null);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}});
		
		p.add(b);
		
		return p;
	}

	private Component getButtonPane() {
		buttonList.add(new DbLaunchButton("Work Orders", SecurityCategory.Transmittal, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				WorkOrders.createAndShowGUI();			
			}}));
		buttonList.add(new DbLaunchButton("Out Transmittals", SecurityCategory.Transmittal, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				OutTransmittals.createAndShowGUI();			
			}}));
		buttonList.add(new DbLaunchButton("Inter-Office Transmittal", SecurityCategory.Transmittal, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				com.tricongeophysics.ZbyteTransmittal.createAndShowGUI();
			}}));
		buttonList.add(new DbLaunchButton("HardCopy Progress", SecurityCategory.UpdateProgress, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				ZbyteSupportProgress.createAndShowGUI();
			}}));
		buttonList.add(new DbLaunchButton("Media/Tape Progress", SecurityCategory.UpdateProgress, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				ZbyteSeismicProgress.createAndShowGUI();
			}}));
		buttonList.add(new DbLaunchButton("ZScan Progress", SecurityCategory.UpdateProgress, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				ZbyteZscanProgress.createAndShowGUI();
			}}));
		buttonList.add(new DbLaunchButton("Billing", SecurityCategory.Manager, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				ZbyteBilling.createAndShowGUI();
			}}));
		buttonList.add(new DbLaunchButton("Job Management", SecurityCategory.Manager, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(ZbyteDatabase.this, "Job Management under construction");
			}}));
		buttonList.add(new DbLaunchButton("Database Administration", SecurityCategory.Administrator, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				ZbyteDatabaseView.createAndShowGUI();
			}}));
		
		JPanel buttonPane = new JPanel();
		buttonPane.setName("Launch Database");
		buttonPane.setLayout(new GridLayout(3,3));
		for (JButton b: buttonList) {
			buttonPane.add(b);
		}
		
		((GridLayout)buttonPane.getLayout()).setHgap(2);
		((GridLayout)buttonPane.getLayout()).setVgap(2);
		
		return buttonPane;
	}

	private Component getIconPane() {
		Icon zbase = (ImageIcon) SUtil.createImageIcon(ZbyteDatabase.class, "images/zbase2.png");
		Icon zbyte = (ImageIcon) SUtil.createImageIcon(ZbyteDatabase.class, "images/zbyte_small.png");
		JLabel lbase = new JLabel(zbase);
		JLabel lbyte = new JLabel(zbyte);
//		JPanel p = new JPanel(new BorderLayout());
//		p.add(lbase, BorderLayout.WEST);
//		p.add(lbyte, BorderLayout.EAST);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(lbase);
		p.add(lbyte);
		return p;
	}

	private Component getLogoutPane() {
		logout = new JButton("Logout");
		logout.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				logoutUser();
			}		
		});
		JPanel p = new JPanel();
		p.add(logout);
		return p;
	}

	protected void logoutUser() {
		manager.logout();
		passwordField.setText("");
		usernameField.setText("");
		messageLabel.setText(manager.getLoginMessage());
		cardLayout.show(cardPanel, Login);
	}

	Component getPasswordPane() {
		passwordField = new JPasswordField(10);
		//passwordField.setPreferredSize(new Dimension(100,30));
		passwordField.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				attemptLogin();
			}});
		JLabel plabel = new JLabel("Password: ");
		plabel.setLabelFor(passwordField);

		usernameField = new JTextField(10);
		usernameField.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				attemptLogin();
			}});
		JLabel nlabel = new JLabel("User Name: ");
		nlabel.setLabelFor(usernameField);
		
		JPanel passwordPane = new JPanel();
		passwordPane.setLayout(new BoxLayout(passwordPane, BoxLayout.X_AXIS));
		passwordPane.add(nlabel);
		passwordPane.add(usernameField);
		passwordPane.add(Box.createHorizontalStrut(20));
		passwordPane.add(plabel);
		passwordPane.add(passwordField);
		passwordPane.add(Box.createHorizontalGlue());
		
		JPanel outerPane = new JPanel();
		outerPane.setLayout(new BorderLayout());
		outerPane.add(passwordPane, BorderLayout.WEST);
		outerPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return outerPane;
	}

	protected void attemptLogin() {
		DbParms dbParms = DbParms.getParms("ZbyteDatabase");
		dbParms.dbTable = ZbyteDatabase.User;
		dbParms.query = "Select * from " + ZbyteDatabase.User;
		DatabaseModel model;
		try {
			model = DatabaseModel.getDatabaseModel(dbParms);
			if (manager.attemptLogin(model, usernameField.getText(), passwordField.getPassword())) {
				//login success!!
				cardLayout.show(cardPanel, Launch);
			}
			enableButtons();
			setMessageLabel();
		} catch (SQLException e1) {
			JOptionPane.showMessageDialog(ZbyteDatabase.this, e1.getMessage(), "Failed Login", JOptionPane.ERROR_MESSAGE, null);
			e1.printStackTrace();
		}
	}

	private void setMessageLabel() {
		messageLabel.setText(manager.getLoginMessage());
		userLabel.setText(manager.getUserDescription());
	}

	protected void enableButtons() {
		for (DbLaunchButton b: buttonList) {
			AccessLevel level = manager.getAccessLevel(b.getAccessCategory().toString());
			if (level == AccessLevel.Denied) {
				b.setEnabled(false);
			}
			else {
				b.setEnabled(true);
			}
		}
	}

	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
		
		if (args.length > 0) {
			DbParms.test = "true";
		}
		
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

	protected static void createAndShowGUI() {
		JFrame frame = new JFrame("ZByte Database");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ZbyteDatabase gj = new ZbyteDatabase();
		gj.setPreferredSize(new Dimension(900, 500));
		gj.setVisible(true);
		gj.init();
		
		frame.getContentPane().add(gj);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	public class DbLaunchButton extends JButton {

		private SecurityCategory accessCategory;

		public DbLaunchButton(String name, SecurityCategory transmittal, ActionListener actionListener) {
			super(name);
			this.accessCategory = transmittal;
			this.addActionListener(actionListener);
		}

		public SecurityCategory getAccessCategory() {
			return accessCategory;
		}

		
	}


}
