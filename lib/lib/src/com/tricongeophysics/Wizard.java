package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Wizard extends JFrame implements ActionListener {

	private static final String NEXT = "Next";
	private static final String PREVIOUS = "Previous";
	private String FINISH = "Finish";
	private String CANCEL = "Cancel";
	private JPanel background;
	private JPanel wizardPagesPanel;
	private JButton nextButton;
	private JButton prevButton;
	private JButton cancelButton;
	private CardLayout cardLayout;
	private int pageCount = 0;
	private int pageId = 1;
	private ArrayList<WizardPage> wizardPages = new ArrayList<WizardPage>();
	//private ListIterator<WizardPage> pageIterator;
	private WizardPage currentPage;
	private ArrayList<PageFinishedListener> pageFinishedListeners = new ArrayList<PageFinishedListener>();

	Wizard(String name) {
		super(name);
		cardLayout = new CardLayout();
		wizardPagesPanel = new JPanel(cardLayout);
		background = new JPanel(new BorderLayout());
		
		nextButton = new JButton(NEXT);
		prevButton  = new JButton(PREVIOUS);
		cancelButton = new JButton(CANCEL);
		//pageIterator = wizardPages.listIterator();
	}
	
	public Wizard() {
		this("");
	}

	private Component getButtonPane() {
		nextButton.setActionCommand(NEXT);
		nextButton.addActionListener(this);
		
		prevButton.setActionCommand(PREVIOUS);
		prevButton.addActionListener(this);
		
		cancelButton.setActionCommand(CANCEL);
		cancelButton.addActionListener(this);	
		
		Box buttonBox = new Box(BoxLayout.X_AXIS);
		buttonBox.add(prevButton);
		buttonBox.add(Box.createHorizontalStrut(4));
		buttonBox.add(nextButton);
		buttonBox.add(Box.createHorizontalStrut(8));
		buttonBox.add(cancelButton);
		
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
		p.add(buttonBox, BorderLayout.EAST);
		
		return p;
	}

	public static void main (String[] args) {
		Wizard w = new Wizard();
		w.addWizardPage(new AbstractWizardPage(new JLabel("first"), "first"));
		w.addWizardPage(new AbstractWizardPage(new JLabel("second"), "second"));
		w.addWizardPage(new AbstractWizardPage(new JLabel("third"),"third" ));
		
		w.createAndShow();
	}

	void createAndShow() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		background.add(getButtonPane(), BorderLayout.SOUTH);
		background.add(wizardPagesPanel, BorderLayout.CENTER);
		
		getContentPane().add(background);
		
		//pageIterator.next();  //have to set iterator to first page;
		
		updateButtons();
		
		cardLayout.first(wizardPagesPanel);
		
		pack();
		setVisible(true);
	}

	void addWizardPage(WizardPage page) {
		pageCount += 1;
		wizardPagesPanel.add(page.getComponent(), page.getId());
		wizardPages.add(page);
		//pageIterator = wizardPages.listIterator();
		currentPage = wizardPages.get(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		if (command == NEXT) {
			if (pageId < pageCount) {
				pageId += 1;
				cardLayout.next(wizardPagesPanel);
				firePageFinished(currentPage);
			}
			//if (pageIterator.hasNext())currentPage = pageIterator.next();
		}
		else if (command == PREVIOUS) {
			if (pageId > 1) {
				pageId -= 1;
				cardLayout.previous(wizardPagesPanel);
			}
			//if (pageIterator.hasPrevious())currentPage = pageIterator.previous();
		}
		else if (command == CANCEL) {
			this.dispose();
			return;
		}
		//cardLayout.show(wizardPagesPanel, currentPage.getId());
		currentPage = wizardPages.get(pageId-1);
		updateButtons();
	}

	private void firePageFinished(WizardPage currentPage2) {
		for (PageFinishedListener l: pageFinishedListeners) {
			l.pageFinished(currentPage2);
		}
	}

	private void updateButtons() {
		prevButton.setEnabled(true);
		nextButton.setEnabled(true);
		if (pageId <= 1) {
		//if (pageIterator.hasPrevious()) {
			prevButton.setEnabled(false);
		}
		if (pageId >= pageCount) {
		//if (pageIterator.hasNext()) {
			nextButton.setEnabled(false);
			setFinishText();
		} else {
			setCancelText();
		}
	}

	private void setCancelText() {
		cancelButton.setText(CANCEL);
	}

	private void setFinishText() {
		cancelButton.setText(FINISH);
	}

	public void setCancelText(String text) {
		CANCEL = text;
		cancelButton.setText(text);
	}

	public void addPageFinishedListener(PageFinishedListener l) {
		pageFinishedListeners.add(l);
	}
}
