package com.tricongeophysics;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class ParameterPanel extends JPanel {
	protected ArrayList<Parameter> parameterList;
	protected ArrayList<ParameterField> parameterFieldList;
	private ArrayList<ActionListener> actionListeners;
	
	public ParameterPanel(){
		super(); 
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); //add stuff vertically instead of horizontally
		parameterList = new ArrayList<Parameter>();
		parameterFieldList = new ArrayList<ParameterField>();
		this.setBorder(BorderFactory.createEtchedBorder());
	}

	public ArrayList<Parameter> getParameterList() {
		return parameterList;
	}

	public void setParameterList(ArrayList<Parameter> parameterList) {
	    if (parameterList == null) return;
		this.parameterList = parameterList;
		loadParameterFieldList();
		setFieldDependencies();
		addFieldsToPanel();
	}
	
	private void setFieldDependencies() {
		Parameter parm;
		ParameterField parmField;
		for (int i=0;i<parameterFieldList.size();i++) {
			parmField = parameterFieldList.get(i);
			parm = parmField.getParameter();
			if (parm.hasDependency()) {
				parmField.setDependsOnField(findFieldWithName(parm.getEnabledDependsOnParm().getName()));
			}
		}
	}

	public ParameterField findFieldWithName(String name) {
		for (int i=0;i<parameterFieldList.size();i++) {
			if (parameterFieldList.get(i).getParameter().getName().equals(name)) {
				return parameterFieldList.get(i);
			}
		}
		System.err.println("ParameterPanel: findFieldWithName() - couldn't find field with name \"" + name + "\"");
		return null;
	}

	public void loadParameterFieldList() {
		for (int i=0;i<parameterList.size();i++) {
			parameterFieldList.add(ParameterField.newFromParameter(parameterList.get(i)));
		}
	}
	
	public void addFieldsToPanel() {
		for (int i=0;i<parameterFieldList.size();i++) {
			this.add(parameterFieldList.get(i));
		}
	}

	/**
	 * adds given Parameter to parm list, creates ParameterField associated with this parm,
	 * and adds the ParameterField to the Panel
	 * 
	 * @param parm
	 */
	public void addParameter(Parameter parm) {
		parameterList.add(parm);
		parameterFieldList.add(ParameterField.newFromParameter(parm));
		this.add(parameterFieldList.get(parameterFieldList.size()-1));
	}

	public void LoadParameterValuesFromFile(String filename, ParmFileConverter converter) throws IOException {
		Parameter.LoadParameterValuesFromFile(filename,parameterList, converter);
		updateFields();
	}

	public void updateFields() {
		for (int i=0;i<parameterFieldList.size();i++) {
			parameterFieldList.get(i).updateField();
		}
		
	}

	public void SaveParameterValuesToFile(String filename, ParmFileConverter converter) throws IOException {
		Parameter.SaveParameterValuesToFile(filename,parameterList, converter);
	}

	public void addParameterValueChangedListener(ParameterValueChangedListener listener) {
		for (int i=0;i<parameterFieldList.size();i++) {
			parameterFieldList.get(i).addParameterValueChangedListener(listener);
		}
		
	}

	public void addActionListener(ActionListener l) {
		for (int i=0;i<parameterFieldList.size();i++) {
			parameterFieldList.get(i).getTextField().addActionListener(l);
		}
	}
}
