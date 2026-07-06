package com.tricongeophysics;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class DbSecurityManager {

	private static User user;
	private static DbSecurityManager manager;
	public enum AccessLevel{Denied, ReadOnly, ReadWrite};
	
	private DbSecurityManager(){}

	public boolean attemptLogin(DatabaseModel model, String username, char[] pword) throws SQLException {
		user = new User(model, username, pword);
		return user.isLoggedIn();
	}

	public static DbSecurityManager getManager() {
		if (manager == null)
			manager = new DbSecurityManager();
		return manager;
	}

	public class User {

		private String name;
		private String[] accessCategories;
		private Object[] accessValues;
		private boolean loggedIn;
		private String message;

		public User(DatabaseModel model, String username, char[] pword) {
			this.name = username;
			String idColName = model.getPkeyName();
			int row = model.findRow(idColName, name);
			if (row < 0) {
				message = "User \"" + name + "\" not found";
				SUtil.printErr(message);
				loggedIn = false;
				return;
			}
			String passwordTry = String.valueOf(pword);
			String password = model.getValueAt(row, "Password")+"";
			if (password.equals(passwordTry)) {
				loggedIn = true;
			} else {
				message = "Incorrect password for user \"" + name + "\"";
				SUtil.printErr(message);
				loggedIn = false;
				return;
			}
			String[] columns = model.getColumnDbNames();
			accessCategories = new String[columns.length-2];
			accessValues = new Object[columns.length-2];
			for (int i=2; i<columns.length; i++) {
				accessCategories[i-2] = columns[i];
				accessValues[i-2] = model.getValueAt(row, i);
			}			
		}
		
		@Override
		public String toString() {
			String s = "<html>User: " + name + " <br>Permissions:<br>";
			//String s = "<html>User: " + name + "<br>";
			if (loggedIn)
				for (int i=0; i<accessCategories.length; i++) {
					s += accessCategories[i] + " = " + accessValues[i]+"<br>";
				}
			else
				s += "logged out.";
			return s;
		}

		public boolean isLoggedIn() {
			return loggedIn;
		}

		public Object getAccessLevel(String accessCategory) {
			for (int i=0; i<accessCategories.length; i++) {
				String cat = accessCategories[i].replace(" ", "");
				if (cat.equals(accessCategory)) {
					return accessValues[i];
				}
			}
			SUtil.printErr("Access Category: " + accessCategory + " not found!");
			return null;
		}

		public void setPassword(String newPassword, DatabaseModel model) {
			String idColName = model.getPkeyName();
			int row = model.findRow(idColName, name);
			if (row < 0) {
				message = "User \"" + name + "\" not found";
				SUtil.printErr(message);
				return;
			}
			model.setValueAt(newPassword, row, "Password");
		}
	}

	public User getUser() {
		return user;
	}

	public AccessLevel getAccessLevel(String accessCategory) {
		if (user == null || !user.isLoggedIn()) return AccessLevel.Denied;
		Object level = user.getAccessLevel(accessCategory);
		if (level == null) return AccessLevel.Denied;
		String s = level.toString().replace("-", "").replace("/", "");
		return AccessLevel.valueOf(s.toString());
	}

	public String getLoginMessage() {
		if (user == null) return "Logged out.";
		if (user.isLoggedIn()) {
			return user.name + " logged in.";
		}
		else {
			return "Failed Login: " + user.message;
		}
	}

	public void logout() {
		user = null;
	}

	public String getUserDescription() {
		if (user == null) 
			return "Logged out";
		else
			return user.toString();
	}

	public void launchChangePasswordDialog(final DatabaseModel model, Frame owner) {
		if (user == null || !user.isLoggedIn()) {
			JOptionPane.showMessageDialog(null, "Must Log in First!", "Change Password Failed", JOptionPane.ERROR_MESSAGE, null);
			return;
		}
		final JPasswordField oldpword = new JPasswordField();
		final JPasswordField newpword1 = new JPasswordField();
		final JPasswordField newpword2 = new JPasswordField();
		JLabel oldLabel = new JLabel("Verify old password:");
		JLabel newLabel1 = new JLabel("Type new password:");
		JLabel newLabel2 = new JLabel("Re-Type new password:");
		final JLabel messageLabel = new JLabel(" ");
		
		JPanel gridPane = new JPanel();
		gridPane.setLayout(new GridLayout(3,2));
		gridPane.add(oldLabel);
		gridPane.add(oldpword);
		gridPane.add(newLabel1);
		gridPane.add(newpword1);
		gridPane.add(newLabel2);
		gridPane.add(newpword2);
		
		newpword1.setEnabled(false);
		newpword2.setEnabled(false);
		
		oldpword.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					if (attemptLogin(model, user.name, oldpword.getPassword())) {
						newpword1.setEnabled(true);
						newpword2.setEnabled(true);
					} else {
						newpword1.setEnabled(false);
						newpword2.setEnabled(false);
					}
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				messageLabel.setText(getLoginMessage());
			}});
		
		newpword2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String pword1 = newpword1.getText();
				String pword2 = newpword2.getText();
				if (pword1.equals(pword2)) {
					user.setPassword(pword2, model);
					messageLabel.setText("Password Changed.");
				} else {
					messageLabel.setText("Passwords did not match.");
				}
			}});
		
//		JButton ok = new JButton("OK");
//		ok.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent e) {
//				dialog.
//			}});
		
		JPanel background = new JPanel();
		background.setLayout(new BoxLayout(background, BoxLayout.Y_AXIS));
		background.add(gridPane);
		background.add(messageLabel);
		
		JDialog dialog = new JDialog(owner, "Set Password", true);
		dialog.setContentPane(background);
		dialog.pack();
		dialog.setVisible(true);
	}
	
}
