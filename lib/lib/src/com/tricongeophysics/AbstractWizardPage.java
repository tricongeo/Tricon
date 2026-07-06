package com.tricongeophysics;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AbstractWizardPage extends JPanel implements WizardPage {

	public AbstractWizardPage() {
		super();
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	public AbstractWizardPage(Component c, String id) {
		this();
		add(c);
		setName(id);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public String getId() {
		return getName();
	}

}
