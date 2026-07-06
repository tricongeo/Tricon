package com.tricongeophysics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.util.ArrayList;


public class ElevStaticsDialog extends JDialog implements ActionListener
{
    private static final String OK = "OK";
    private static final String Cancel = "Cancel";
    private final JButton okButton;
    private JTextField textField;
    private JButton btnCancel;
    private boolean canceled;
    private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
    private IntField elevField;
    private IntField velField;
    private IntField nameField;
    private BooleanField uhField;
    private BooleanField depthField;

    {
        SimpleField.preferredFieldWidth = 150;
        SimpleField.preferredLabelWidth = 100;
    }
    
    public ElevStaticsDialog(Frame parent)
    {
        super(parent, "Elevation Statics");
        
        JPanel buttonPane = new JPanel();
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        okButton = new JButton(OK);
        okButton.setActionCommand(OK);
        okButton.addActionListener(this);
        buttonPane.add(okButton);
        
        btnCancel = new JButton(Cancel);
        btnCancel.setActionCommand(Cancel);
        btnCancel.addActionListener(this);
        buttonPane.add(btnCancel);
        
        JLabel lblElevationStatics = new JLabel("Elevation Statics");
        getContentPane().add(lblElevationStatics, BorderLayout.NORTH);
        
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setPreferredSize(new Dimension(300,200));
        JPanel surroundPanel = new JPanel();
        surroundPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        surroundPanel.add(fieldsPanel);
        getContentPane().add(surroundPanel, BorderLayout.CENTER);
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        
        elevField = new IntField("Datum Elev.", 0);
        velField = new IntField("Repl. Vel.", 10000);
        nameField = new IntField("Statics Name", "ElevStat");
        uhField = new BooleanField("Use Upholes", false);
        depthField = new BooleanField("Use Shot Depths", false);
        
        fieldsPanel.add(elevField);
        fieldsPanel.add(velField);
        fieldsPanel.add(nameField);
        fieldsPanel.add(uhField);
        fieldsPanel.add(depthField);
        
        //this.setSize(400, 250);
        this.pack();
        this.setVisible(true);
    }

    public void addActionListener(ActionListener actionListener)
    {
       listeners.add(actionListener);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String cmnd = e.getActionCommand();
        if (cmnd == Cancel) canceled = true;
        else canceled = false;
        this.setVisible(false);
        this.dispose();
        fireActionPerformed(e);
    }

    private void fireActionPerformed(ActionEvent e)
    {
        for (ActionListener l: listeners) l.actionPerformed(e);
    }

    public boolean isCanceled()
    {
        return canceled;
    }

    public int getDatumElevation()
    {
       return this.elevField.getIntValue();
    }

    public int getCorrectionalVelocity()
    {
        return this.velField.getIntValue();
    }

    public String getStaticsName()
    {
        return this.nameField.getValue() + "";
    }

    public boolean getUseUpholes()
    {
        return this.uhField.getBooleanValue();
    }

    public boolean getUseShotDepth()
    {
        return this.depthField.getBooleanValue();
    }

}
