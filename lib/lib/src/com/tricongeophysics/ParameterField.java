package com.tricongeophysics;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class ParameterField extends JPanel implements ParameterValueChangedListener{
//public abstract class ParameterField extends JPanel implements ActionListener {
	
	protected Parameter parameter;
	protected JLabel label;
	protected JTextField textField;
	public ActionListener listener;
	protected ParameterField dependsOnField;
    private ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	public static int PanelWidth = 640;
	public static int PanelHeight = 30;
	public static int LabelWidth = 240;
	
	//public static Color TextFieldBackground = new Color(137, 165, 187);
	public static Color TextFieldBackground = new Color(180, 198, 211);
	public static Color TextFieldBackgroundError = new Color(255, 100, 100);
	
	public ParameterField(Parameter parameter) {
		super();
		this.parameter = parameter;
		label = new JLabel(parameter.getDescription());
		label.setPreferredSize(new Dimension(LabelWidth,PanelHeight));
		
		//most parameters will use text field, go ahead and build that here
		textField = new JTextField(parameter.default_value);
		textField.setBackground(TextFieldBackground);
		textField.setToolTipText(parameter.getHelp());
		textField.setDisabledTextColor(Color.LIGHT_GRAY);
		textField.setActionCommand(parameter.name);
		
		buildFieldPanel();
		
		addParameterValueChangedListener(this);
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setPreferredSize(new Dimension(PanelWidth,PanelHeight));
		this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		this.setForeground(Color.BLUE);
	}
	
	/**
	 * update gui display to current value of parameter (in case of reload from file, etc.)
	 */
	public void updateField() {
		try { //changing text while document loops through changeListeners throws an exception, which we will ignore!!!
		textField.setText(parameter.getValue()); //reset text to show if value was accepted
		} 
		catch (java.lang.IllegalStateException e) {
			//e.printStackTrace();    //it's going to complain about this, but who cares!!;
		}
		
		if (parameter.valueIsOk()) {
			textField.setBackground(TextFieldBackground);
		} else {
			textField.setBackground(TextFieldBackgroundError);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		setValueFromField();
		fireActionPerformed(e);
	}
	
	private void fireActionPerformed(ActionEvent e)
    {
        for (ActionListener l: actionListeners) {
            l.actionPerformed(e);
        }
    }

    public void changedUpdate(DocumentEvent e) {
		//setValueFromField();
		actionPerformed(null);
	}

	public void insertUpdate(DocumentEvent e) {
		//setValueFromField();
		actionPerformed(null);
	}

	public void removeUpdate(DocumentEvent e) {
		//setValueFromField();
		actionPerformed(null);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IntParameter parm = new IntParameter("test", "test thing", "help", "0", 0);
		ParameterField pf = new IntParameterField(parm);
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(new JLabel(pf.parameter.toString()));
		frame.pack();
		frame.setVisible(true);
	}

	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	/**
	 * Creates appropriate parameter field based on parameter2.getType()
	 * 
	 * @param parameter2
	 * @return ParameterField
	 */
	public static ParameterField newFromParameter(Parameter parameter2) {
		if (parameter2.getType().equals(Parameter.Type.INT)) {
			return new IntParameterField((IntParameter)parameter2);
		}
		if (parameter2.getType().equals(Parameter.Type.FLOAT)) {
			return new FloatParameterField((FloatParameter)parameter2);
		}
		if (parameter2.getType().equals(Parameter.Type.ENUM)) {
			return new EnumParameterField((EnumParameter)parameter2);
		}
		if (parameter2.getType().equals(Parameter.Type.FILE)) {
			return new FileParameterField((FileParameter)parameter2);
		}
		if (parameter2.getType().equals(Parameter.Type.DIR)) {
			return new DirParameterField((DirParameter)parameter2);
		}
		if (parameter2.getType().equals(Parameter.Type.PROJECTNAME)) {
			return new ProjectNameParameterField((ProjectNameParameter)parameter2);
		}
		if (parameter2.getType().equals(Parameter.Type.TEXT)) {
            return new TextParameterField((TextParameter)parameter2);
        }
		System.err.println("ParameterField: Error - unrecognized parameter type encountered!!!!");
		return null;
	}

	public void setDependsOnField(ParameterField parmField) {
		//System.out.println("setting "+this.getParameter().getName()+" depends on "+parmField.getParameter().getName());
		this.dependsOnField = parmField;
		dependsOnField.addParameterValueChangedListener(new ParameterValueChangedListener() {
			public void actionPerformed(ActionEvent e) {
				//System.out.println(dependsOnField.getParameter().getName() + " changing!");
				dependsOnField.setValueFromField(); //make sure value updates first before doing this;

				String dependsFieldValue = dependsOnField.getParameter().getValue();
				String enablingValue = ParameterField.this.getParameter().enabledDependsOnValue;

				if (dependsFieldValue.equals(enablingValue)) {
					//System.out.println(ParameterField.this.getParameter().getName() + " enabled");
					ParameterField.this.setEnabled(true);
				} else {
					//System.out.println(ParameterField.this.getParameter().getName() + " disabled");
					ParameterField.this.setEnabled(false);
				}
			}
			public void changedUpdate(DocumentEvent e) {
				actionPerformed(null);
			}
			public void insertUpdate(DocumentEvent e) {
				actionPerformed(null);
			}
			public void removeUpdate(DocumentEvent e) {
				actionPerformed(null);
			}
		});
		dependsOnField.actionPerformed(null); //now that listener set up, go ahead and fire action event so that this field is initialized correctly to enabled/disabled
	}

	@Override
	public void setEnabled(boolean enabled) {
		textField.setEditable(enabled); //so you can still see the text
		label.setEnabled(enabled);
	}
	
    protected void buildFieldPanel() {
        this.add(label);
        this.add(textField);
    }

    public void setValueFromField() {
        parameter.setValue(textField.getText());
        updateField();
    }

    public void addParameterValueChangedListener(ParameterValueChangedListener listener) {
        textField.getDocument().addDocumentListener(listener);
    }

    public String getValue()
    {
        if (parameter == null) return null;
        return parameter.value;
    }

    public void setValue(String value)
    {
        if (value == null || parameter == null) return;
        parameter.setValue(value);
        updateField();
    }

    public void addActionListener(ActionListener l)
    {
        actionListeners .add(l);
    }

    public JTextField getTextField()
    {
        return textField;
    }
    
    @Override
    public String toString() {
    	return getValue();
    }
}
