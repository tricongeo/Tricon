package com.tricongeophysics;

import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import javax.swing.border.LineBorder;
import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import java.awt.Component;
import javax.swing.Box;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.ButtonGroup;

import com.tricongeophysics.CdpModel.Corner;

public class CdpGridPane extends JPanel
{
    public enum CdpGridMethod {
        Estimate, Corners, Angle

    }
    private static final int LeftPaneWidth = 700;
    public Object val;
    private FloatField ilSpacingField;
    private FloatField xlSpacingField;
    private IntField ilLabelIncrementField;
    private IntField xlLabelIncrementField;
    private IntField cornersUR_ILField;
    private IntField cornersUR_XLField;
    private FloatField cornersUR_XField;
    private FloatField cornersUR_YField;
    private IntField cornersLL_ILField;
    private IntField cornersLL_XLField;
    private FloatField cornersLL_XField;
    private FloatField cornersLL_YField;
    private IntField cornersUL_ILField;
    private IntField cornersUL_XLField;
    private FloatField cornersUL_XField;
    private FloatField cornersUL_YField;
    private IntField cornersLR_ILField;
    private IntField cornersLR_XLField;
    private FloatField cornersLR_XField;
    private FloatField cornersLR_YField;
    private IntField matchInlineField;
    private IntField matchXLineField;
    private FloatField matchXField;
    private FloatField matchYField;
    private IntField displayILIncField;
    private IntField scatterShotIncField;
    //private IntField scatterChanIncField;
    private FloatField matchAngleField;
    private IntField bufferSizeField;
    private IntField angleILField;
    private IntField angleXLField;
    private IntField firstCDPField;
    private FloatField angleField;
    private FloatField angleXField;
    private FloatField angleYField;
    private JSplitPane splitPane;
    private FileField line3dFileOutField;
    private IntField ilOriginField;
    private IntField xlOriginField;
    private IntField displayIL_LabelIncField;
    private IntField displayXL_LabelIncField;
    private IntField angleNumILField;
    private IntField angleNumXLField;
    private final ButtonGroup cdpMethodButtonGroup = new ButtonGroup();
    private final ButtonGroup mouseActionButtonGroup = new ButtonGroup();
    private JPanel panel;
    private JCheckBox estimateMethodCheckBox;
    private JPanel estimateMethodPane;
    private JButton estimateButton;
    private IntField displayXLIncField;
    private JCheckBox cornersMethodCheckBox;
    private JCheckBox angleMethodCheckBox;
    private JPanel cornerMethodPane;
    private JPanel angleMethodPane;
    private JCheckBox matchGridCheckbox;
    private JButton refreshButton;
    CdpGridMethod cdpGridMethod;
    private CdpModel cdpModel;
    private StationPlotter stationPlotter;
    private GridButton showGridButton;
    private JButton scatterButton;
    private JCheckBox showXLLabelsCheckBox;
    private JCheckBox showILLabelsCheckBox;
    private PanGridButton adjustOriginButton;
    private RotateGridButton adjustAngleButton;
    private EstimateCdpGridJob estimateCdpGridJob;
    private PanButton panNormalButton;
    JPanel anglePane;
    JPanel cornersPane;
    
    {
        SimpleField.preferredFieldWidth = 150;
        SimpleField.preferredLabelWidth = 50;
    }

    /**
     * Create the panel.
     */
    public CdpGridPane()
    {        
        setName("CDP Binning");
        setLayout(new BorderLayout(0, 0));
        
        initializeFields();
        cdpModel = new CdpModel();
        
        JPanel titlePane = new JPanel();
        add(titlePane, BorderLayout.NORTH);
        titlePane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        JLabel lblCdpBinning = new JLabel("CDP Binning");
        titlePane.add(lblCdpBinning);
        
        JPanel centerPane = new JPanel();
        centerPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        centerPane.setLayout(new BorderLayout());
        add(centerPane, BorderLayout.CENTER);
        
        splitPane = new JSplitPane();
        splitPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        centerPane.add(splitPane);
        
        JPanel leftPane = new JPanel();
        JScrollPane scrollPane = new JScrollPane(leftPane);
        leftPane.setPreferredSize(new Dimension(LeftPaneWidth,600));
        splitPane.setLeftComponent(scrollPane);
        leftPane.setBorder(null);
       // leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
        leftPane.setLayout(new BorderLayout());
        
        JPanel requiredParmPane = new JPanel();
        leftPane.add(requiredParmPane, BorderLayout.NORTH);
        requiredParmPane.setBorder(new TitledBorder(null, "Required Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        requiredParmPane.setLayout(new BoxLayout(requiredParmPane, BoxLayout.X_AXIS));
        
        JPanel panel_62 = new JPanel();
        requiredParmPane.add(panel_62);
        panel_62.setLayout(new BorderLayout(0, 0));
        
        JPanel panel_15 = new JPanel();
        panel_62.add(panel_15, BorderLayout.NORTH);
        panel_15.setLayout(new BoxLayout(panel_15, BoxLayout.X_AXIS));
        
        panel_15.add(firstCDPField);
        
        JPanel panel_92 = new JPanel();
        requiredParmPane.add(panel_92);
        panel_92.setLayout(new BoxLayout(panel_92, BoxLayout.Y_AXIS));
        
        JPanel panel_56 = new JPanel();
        panel_92.add(panel_56);
        panel_56.setLayout(new BoxLayout(panel_56, BoxLayout.X_AXIS));
        
        JLabel l1 = new JLabel("Spacing");
        panel_56.add(l1);
        panel_56.add(ilSpacingField);
        panel_56.add(xlSpacingField);
        
        JPanel panel_57 = new JPanel();
        panel_92.add(panel_57);
        panel_57.setLayout(new BoxLayout(panel_57, BoxLayout.X_AXIS));
        
        JLabel l2 = new JLabel("Label Increment");
        panel_57.add(l2);
        panel_57.add(ilLabelIncrementField);
        panel_57.add(xlLabelIncrementField);
        
        JPanel panel_12 = new JPanel();
        requiredParmPane.add(panel_12);
        
        JPanel panel_16 = new JPanel();
        requiredParmPane.add(panel_16);
        
        JPanel panel_17 = new JPanel();
        requiredParmPane.add(panel_17);
        
        JPanel gridMethodPane = new JPanel();
        gridMethodPane.setBorder(new TitledBorder(null, "Grid Definition Methods", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        leftPane.add(gridMethodPane, BorderLayout.CENTER);
        gridMethodPane.setLayout(new BoxLayout(gridMethodPane, BoxLayout.Y_AXIS));
        
        estimateMethodPane = new JPanel();
        gridMethodPane.add(estimateMethodPane);
        estimateMethodPane.setLayout(new BoxLayout(estimateMethodPane, BoxLayout.X_AXIS));
        
        estimateMethodCheckBox = new JCheckBox("");
        cdpMethodButtonGroup.add(estimateMethodCheckBox);
        estimateMethodCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               cdpGridMethodChanged(CdpGridMethod.Estimate);
            }
        });
        estimateMethodCheckBox.setSelected(true); //default to estimate method
        estimateMethodPane.add(estimateMethodCheckBox);
        
        panel = new JPanel();
        estimateMethodPane.add(panel);
        panel.setBorder(new TitledBorder(null, "Estimate", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        JPanel panel_80 = new JPanel();
        panel.add(panel_80);
        panel_80.setBorder(null);
        panel_80.setLayout(new BoxLayout(panel_80, BoxLayout.Y_AXIS));
        
        estimateButton = new JButton("Estimate");
        panel_80.add(estimateButton);
        estimateButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                runEstimateJob();
            }});
        
        Component verticalStrut_7 = Box.createVerticalStrut(10);
        panel_80.add(verticalStrut_7);
        
        JPanel panel_82 = new JPanel();
        panel_80.add(panel_82);
        panel_82.setLayout(new BoxLayout(panel_82, BoxLayout.X_AXIS));
        
        panel_82.add(bufferSizeField);
        
        JLabel label_17 = new JLabel("Bins");
        panel_82.add(label_17);
        
        Component verticalStrut_11 = Box.createVerticalStrut(10);
        panel_80.add(verticalStrut_11);
        
        JPanel panel_77 = new JPanel();
        panel_80.add(panel_77);
        panel_77.setLayout(new BorderLayout(0, 0));
        
       // panel_77.add(new JLabel("Origin"));
        panel_77.add(ilOriginField, BorderLayout.WEST);
        panel_77.add(xlOriginField, BorderLayout.EAST);
        
        Component verticalStrut_6 = Box.createVerticalStrut(10);
        panel_80.add(verticalStrut_6);
        
        Component horizontalStrut = Box.createHorizontalStrut(10);
        panel.add(horizontalStrut);
        
        JPanel panel_14 = new JPanel();
        panel.add(panel_14);
        panel_14.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panel_14.setLayout(new BoxLayout(panel_14, BoxLayout.Y_AXIS));
        
        Component verticalStrut_8 = Box.createVerticalStrut(10);
        panel_14.add(verticalStrut_8);
        
        JPanel panel_20 = new JPanel();
        panel_14.add(panel_20);
        panel_20.setLayout(new BoxLayout(panel_20, BoxLayout.X_AXIS));
        
        matchGridCheckbox = new JCheckBox("Match Existing Grid:");
        panel_20.add(matchGridCheckbox);
       // matchGridCheckbox.setEnabled(false);
        matchGridCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               cdpGridMethodChanged(CdpGridMethod.Estimate);
            }});
       
        panel_20.add(matchInlineField);
        panel_20.add(matchXLineField);
        
        Component verticalStrut_1 = Box.createVerticalStrut(20);
        panel_14.add(verticalStrut_1);
        
        JPanel panel_91 = new JPanel();
        panel_14.add(panel_91);
        panel_91.setLayout(new BoxLayout(panel_91, BoxLayout.X_AXIS));
        
        panel_91.add(matchAngleField);
        
        panel_91.add(matchXField);
        panel_91.add(matchYField);
        
        Component verticalStrut_5 = Box.createVerticalStrut(10);
        panel_14.add(verticalStrut_5);
       
        
        Component verticalStrut_2 = Box.createVerticalStrut(10);
        gridMethodPane.add(verticalStrut_2);
        
        cornerMethodPane = new JPanel();
        gridMethodPane.add(cornerMethodPane);
        cornerMethodPane.setLayout(new BoxLayout(cornerMethodPane, BoxLayout.X_AXIS));
        
        cornersMethodCheckBox = new JCheckBox("");
        cornersMethodCheckBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                cdpGridMethodChanged(CdpGridMethod.Corners);
            }});
        cdpMethodButtonGroup.add(cornersMethodCheckBox);
        cornerMethodPane.add(cornersMethodCheckBox);
        
        cornersPane = new JPanel();
        cornerMethodPane.add(cornersPane);
        cornersPane.setBorder(new TitledBorder(null, "Corners", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        cornersPane.setLayout(new GridLayout(0, 3, 0, 0));
        
        JPanel panel_7 = new JPanel();
        cornersPane.add(panel_7);
        panel_7.setLayout(new GridLayout(2, 4, 2, 2));
        
        JPanel panel_21 = new JPanel();
        panel_7.add(panel_21);
        panel_21.setLayout(new BoxLayout(panel_21, BoxLayout.X_AXIS));
        
        panel_21.add(cornersUL_ILField);
        
        JPanel panel_22 = new JPanel();
        panel_7.add(panel_22);
        panel_22.setLayout(new BoxLayout(panel_22, BoxLayout.X_AXIS));
        
        panel_22.add(cornersUL_XLField);
        
        JPanel panel_23 = new JPanel();
        panel_7.add(panel_23);
        panel_23.setLayout(new BoxLayout(panel_23, BoxLayout.X_AXIS));
        
        panel_23.add(cornersUL_XField);
        
        JPanel panel_24 = new JPanel();
        panel_7.add(panel_24);
        panel_24.setLayout(new BoxLayout(panel_24, BoxLayout.X_AXIS));
        
        panel_24.add(cornersUL_YField);
        
        JLabel lblNewLabel_8 = new JLabel("Grid Map");
        lblNewLabel_8.setHorizontalAlignment(SwingConstants.CENTER);
        cornersPane.add(lblNewLabel_8);
        
        JPanel panel_5 = new JPanel();
        cornersPane.add(panel_5);
        panel_5.setLayout(new GridLayout(2, 4, 2, 2));
        
        JPanel panel_44 = new JPanel();
        panel_5.add(panel_44);
        panel_44.setLayout(new BoxLayout(panel_44, BoxLayout.X_AXIS));
        
        panel_44.add(cornersUR_ILField);
        
        JPanel panel_45 = new JPanel();
        panel_5.add(panel_45);
        panel_45.setLayout(new BoxLayout(panel_45, BoxLayout.X_AXIS));
        
        panel_45.add(cornersUR_XLField);
        
        JPanel panel_46 = new JPanel();
        panel_5.add(panel_46);
        panel_46.setLayout(new BoxLayout(panel_46, BoxLayout.X_AXIS));
        
        panel_46.add(cornersUR_XField);
        
        JPanel panel_47 = new JPanel();
        panel_5.add(panel_47);
        panel_47.setLayout(new BoxLayout(panel_47, BoxLayout.X_AXIS));
        
        panel_47.add(cornersUR_YField);
        
        JLabel label_12 = new JLabel("");
        cornersPane.add(label_12);
        
        JPanel panel_9 = new JPanel();
        panel_9.setBorder(new LineBorder(new Color(0, 0, 0), 4));
        cornersPane.add(panel_9);
        
        JLabel label_13 = new JLabel("");
        cornersPane.add(label_13);
        
        JPanel panel_6 = new JPanel();
        cornersPane.add(panel_6);
        panel_6.setLayout(new GridLayout(2, 4, 2, 2));
        
        JPanel panel_48 = new JPanel();
        panel_6.add(panel_48);
        panel_48.setLayout(new BoxLayout(panel_48, BoxLayout.X_AXIS));
        
        panel_48.add(cornersLL_ILField);
        
        JPanel panel_49 = new JPanel();
        panel_6.add(panel_49);
        panel_49.setLayout(new BoxLayout(panel_49, BoxLayout.X_AXIS));
        
        panel_49.add(cornersLL_XLField);
        
        JPanel panel_50 = new JPanel();
        panel_6.add(panel_50);
        panel_50.setLayout(new BoxLayout(panel_50, BoxLayout.X_AXIS));
        
        panel_50.add(cornersLL_XField);
        
        JPanel panel_51 = new JPanel();
        panel_6.add(panel_51);
        panel_51.setLayout(new BoxLayout(panel_51, BoxLayout.X_AXIS));
        
        panel_51.add(cornersLL_YField);
        
        JLabel label_14 = new JLabel("");
        cornersPane.add(label_14);
        
        JPanel panel_8 = new JPanel();
        cornersPane.add(panel_8);
        panel_8.setLayout(new GridLayout(2, 4, 2, 2));
        
        JPanel panel_52 = new JPanel();
        panel_8.add(panel_52);
        panel_52.setLayout(new BoxLayout(panel_52, BoxLayout.X_AXIS));
        
        panel_52.add(cornersLR_ILField);
        
        JPanel panel_53 = new JPanel();
        panel_8.add(panel_53);
        panel_53.setLayout(new BoxLayout(panel_53, BoxLayout.X_AXIS));
        
        panel_53.add(cornersLR_XLField);
        
        JPanel panel_54 = new JPanel();
        panel_8.add(panel_54);
        panel_54.setLayout(new BoxLayout(panel_54, BoxLayout.X_AXIS));
        
        panel_54.add(cornersLR_XField);
        
        JPanel panel_55 = new JPanel();
        panel_8.add(panel_55);
        panel_55.setLayout(new BoxLayout(panel_55, BoxLayout.X_AXIS));
        
        panel_55.add(cornersLR_YField);
        
        Component verticalStrut_3 = Box.createVerticalStrut(10);
        gridMethodPane.add(verticalStrut_3);
        
        angleMethodPane = new JPanel();
        gridMethodPane.add(angleMethodPane);
        angleMethodPane.setLayout(new BoxLayout(angleMethodPane, BoxLayout.X_AXIS));
        
        angleMethodCheckBox = new JCheckBox("");
        angleMethodCheckBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                cdpGridMethodChanged(CdpGridMethod.Angle);
            }});
        cdpMethodButtonGroup.add(angleMethodCheckBox);
        angleMethodPane.add(angleMethodCheckBox);
        
        anglePane = new JPanel();
        angleMethodPane.add(anglePane);
        anglePane.setBorder(new TitledBorder(null, "Angle", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        anglePane.setLayout(new BoxLayout(anglePane, BoxLayout.Y_AXIS));
        
        anglePane.add(angleField);
        
        Component verticalStrut_9 = Box.createVerticalStrut(10);
        anglePane.add(verticalStrut_9);

        JPanel panel_65 = new JPanel();
        anglePane.add(panel_65);
        panel_65.setLayout(new BoxLayout(panel_65, BoxLayout.X_AXIS));

        JLabel lblNewLabel = new JLabel("Origin");
        panel_65.add(lblNewLabel);

        panel_65.add(angleILField);
        panel_65.add(angleXLField);

        panel_65.add(angleNumILField);
        Component verticalStrut_4 = Box.createVerticalStrut(10);
        anglePane.add(verticalStrut_4);

        JPanel panel_66 = new JPanel();
        anglePane.add(panel_66);
        panel_66.setLayout(new BoxLayout(panel_66, BoxLayout.X_AXIS));

        JLabel label = new JLabel("Origin");
        panel_66.add(label);
        panel_66.add(angleXField);
        panel_66.add(angleYField);
        
        panel_66.add(angleNumXLField);
        
        Component verticalStrut_10 = Box.createVerticalStrut(10);
        anglePane.add(verticalStrut_10);
        
        JPanel panel_1 = new JPanel();
        gridMethodPane.add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
        
        Component verticalGlue = Box.createVerticalGlue();
        panel_1.add(verticalGlue);
        
        Component verticalGlue_1 = Box.createVerticalGlue();
        panel_1.add(verticalGlue_1);
        
        Component verticalStrut = Box.createVerticalStrut(200);
        panel_1.add(verticalStrut);
        
        JPanel rightPane = new JPanel();
        splitPane.setRightComponent(rightPane);
        rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
        
        JPanel showGridPane = new JPanel();
        showGridPane.setBorder(new TitledBorder("Display Grid"));
        rightPane.add(showGridPane);
        showGridPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        JPanel panel_2 = new JPanel();
        showGridPane.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
        
        JPanel panel_29 = new JPanel();
        panel_2.add(panel_29);
        panel_29.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        //panel_29.setLayout(new BoxLayout(panel_29, BoxLayout.X_AXIS));
        
        JLabel lblNewLabel_12 = new JLabel("Show Grid");
        panel_29.add(lblNewLabel_12);
        
        showGridButton = new GridButton();
        showGridButton.setSelected(true);
        showGridButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                refreshPlot();
            }});
        panel_29.add(showGridButton);
        
        JPanel panel_30 = new JPanel();
        panel_2.add(panel_30);
        panel_30.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        panel_30.add(displayILIncField);
        
        
        
        JPanel panel_31 = new JPanel();
        panel_2.add(panel_31);
        panel_31.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        panel_31.add(displayXLIncField);
        
        
        
        JPanel panel_13 = new JPanel();
        panel_2.add(panel_13);
        panel_13.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        showILLabelsCheckBox = new JCheckBox("");
        showILLabelsCheckBox.setSelected(true);
        panel_13.add(showILLabelsCheckBox);
        
//        Component horizontalStrut = Box.createHorizontalStrut(10);
//        panel_13.add(horizontalStrut);
        
        panel_13.add(displayIL_LabelIncField);
        
        JPanel panel_83 = new JPanel();
        panel_2.add(panel_83);
        panel_83.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        showXLLabelsCheckBox = new JCheckBox("");
        showXLLabelsCheckBox.setSelected(true);
        panel_83.add(showXLLabelsCheckBox);
        
//        Component horizontalStrut_1 = Box.createHorizontalStrut(10);
//        panel_83.add(horizontalStrut_1);
        
        panel_83.add(displayXL_LabelIncField);
        
        JPanel panel_76 = new JPanel();
        panel_2.add(panel_76);
        panel_76.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        JLabel lblNewLabel_26 = new JLabel("Refresh Grid");
        panel_76.add(lblNewLabel_26);
        
        refreshButton = new ReloadButton();
        refreshButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                doCdpModelUpdate();
                refreshPlot();
            }});
        panel_76.add(refreshButton);
        
        JPanel showScatterPane = new JPanel();
        showScatterPane.setBorder(new TitledBorder(null, "Scatter Display", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        rightPane.add(showScatterPane);
        
        JPanel panel_32 = new JPanel();
        showScatterPane.add(panel_32);
        panel_32.setLayout(new BoxLayout(panel_32, BoxLayout.Y_AXIS));
        
        JPanel panel_33 = new JPanel();
        panel_32.add(panel_33);
        
        JLabel lblNewLabel_14 = new JLabel("Show Scatter");
        panel_33.add(lblNewLabel_14);
        
        scatterButton = new ScatterButton();
        scatterButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                stationPlotter.setScatterIncrement(scatterShotIncField.getIntValue());
                stationPlotter.checkCalcScatter();
            }});
        panel_33.add(scatterButton);
        
        JPanel panel_34 = new JPanel();
        panel_32.add(panel_34);
        
        panel_34.add(scatterShotIncField);
        
        JPanel panel_35 = new JPanel();
        panel_32.add(panel_35);
        
       // panel_35.add(scatterChanIncField);
        
        JPanel mouseActionPane = new JPanel();
        mouseActionPane.setBorder(new TitledBorder(null, "Mouse Action", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(59, 59, 59)));
        rightPane.add(mouseActionPane);
        mouseActionPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        JPanel panel_36 = new JPanel();
        mouseActionPane.add(panel_36);
        panel_36.setLayout(new BoxLayout(panel_36, BoxLayout.Y_AXIS));
        
        JPanel panel_3 = new JPanel();
        panel_36.add(panel_3);
        
        JLabel lblNewLabel_1 = new JLabel("Click and Drag to....");
        panel_3.add(lblNewLabel_1);
        
        JPanel panel_41 = new JPanel();
        panel_36.add(panel_41);
        
        JLabel lblNewLabel_9 = new JLabel("Pan Map (normal)");
        panel_41.add(lblNewLabel_9);
        
        panNormalButton = new PanButton();
        panNormalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                stationPlotter.setNormalPanMouseListener();
            }});
        mouseActionButtonGroup.add(panNormalButton);
        panel_41.add(panNormalButton);
        
        JPanel panel_42 = new JPanel();
        panel_36.add(panel_42);
        
        JLabel lblNewLabel_10 = new JLabel("Adjust Origin");
        panel_42.add(lblNewLabel_10);
        
        adjustOriginButton = new PanGridButton();
        adjustOriginButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                stationPlotter.setAdjustOriginMouseListener();
            }});
        mouseActionButtonGroup.add(adjustOriginButton);
        //adjustOriginButton.setEnabled(false);
        panel_42.add(adjustOriginButton);
        
        JPanel panel_43 = new JPanel();
        panel_36.add(panel_43);
        
        JLabel lblNewLabel_17 = new JLabel("Adjust Angle");
        panel_43.add(lblNewLabel_17);
        
        adjustAngleButton = new RotateGridButton();
        mouseActionButtonGroup.add(adjustAngleButton);
        adjustAngleButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
               stationPlotter.setAdjustAngleMouseListener();
            }});
        //adjustAngleButton.setEnabled(false);
        panel_43.add(adjustAngleButton);
        
        JPanel fileIOPane = new JPanel();
        fileIOPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        add(fileIOPane, BorderLayout.SOUTH);
        fileIOPane.setLayout(new BoxLayout(fileIOPane, BoxLayout.X_AXIS));
        
//        fileIOPane.add(line3dFileInField);
//        line3dFileInField.setEnabled(false);
        fileIOPane.add(line3dFileOutField); //doesn't work yet
        line3dFileOutField.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                String file = line3dFileOutField.getValue()+"";
                FocusCdpFileWriter focusWriter = new FocusCdpFileWriter();
                if (!focusWriter.writeFile(new File(file), cdpModel)) {
                    JOptionPane.showMessageDialog(CdpGridPane.this, "Failed to output file: "+file, "File Save Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(CdpGridPane.this, "Saved to Line3D File: " + file, "Save Success!", JOptionPane.INFORMATION_MESSAGE);
            }});
        
        cdpGridMethodChanged(CdpGridMethod.Estimate);
        
       
        //this.setPreferredSize(new Dimension(900, 900));
        splitPane.setDividerLocation(750);
    }
    
    protected void runEstimateJob()
    {
        setModelByEstimateFields();
        estimateCdpGridJob = new EstimateCdpGridJob(stationPlotter.getReceivers(),stationPlotter.getShotPoints(), cdpModel);
        estimateCdpGridJob.addJobFinishedListener(new JobFinishedListener(){
            @Override
            public void jobFinished(Job job)
            {
                doCdpModelUpdate();
                refreshPlot();
            }});
        new JobProgressMonitor(this, estimateCdpGridJob, "Estimating Grid...");
    }

    protected void refreshPlot()
    {
        stationPlotter.repaint();
    }

    protected void doCdpModelUpdate()
    {
        if (cdpGridMethod == CdpGridMethod.Estimate) {
            //setModelByEstimateFields();
            setFieldsFromModel();
            cdpModel.calcCornersFromAngle();
        }
        if (cdpGridMethod == CdpGridMethod.Angle) {
            setModelByAngleFields();
            cdpModel.calcCornersFromAngle();
        }
        if (cdpGridMethod == CdpGridMethod.Corners) {
            setModelByCornerFields();
            cdpModel.calcAngleFromCorners();
        }   
        setFieldsFromModel();
    }

    private void setModelByEstimateFields()
    {
        setModelToRequiredFields();
        cdpModel.setOriginIL(ilOriginField.getIntValue());
        cdpModel.setOriginXL(xlOriginField.getIntValue());
        cdpModel.setBuffer(bufferSizeField.getIntValue());
        if (this.matchGridCheckbox.isSelected()) {
            CdpPoint p = new CdpPoint();
            p.il = this.matchInlineField.getIntValue();
            p.xl = this.matchXLineField.getIntValue();
            p.x = this.matchXField.getDoubleValue();
            p.y = this.matchYField.getDoubleValue();
            cdpModel.setMatchPoint(p);
            cdpModel.setAngle(this.matchAngleField.getDoubleValue());
        }
        else {
            cdpModel.setMatchPoint(null);
        }
    }

    private void setModelByCornerFields()
    {
        setModelToRequiredFields();
        CdpPoint pll = new CdpPoint();
        pll.il = cornersLL_ILField.getIntValue();
        pll.xl = cornersLL_XLField.getIntValue();
        pll.x  = cornersLL_XField. getDoubleValue();
        pll.y  = cornersLL_YField. getDoubleValue();
        CdpPoint plr = new CdpPoint();
        plr.il = cornersLR_ILField.getIntValue();
        plr.xl = cornersLR_XLField.getIntValue();
        plr.x  = cornersLR_XField. getDoubleValue();
        plr.y  = cornersLR_YField. getDoubleValue();
        CdpPoint pur = new CdpPoint();
        pur.il = cornersUR_ILField.getIntValue();
        pur.xl = cornersUR_XLField.getIntValue();
        pur.x  = cornersUR_XField. getDoubleValue();
        pur.y  = cornersUR_YField. getDoubleValue();
        CdpPoint pul = new CdpPoint();
        pul.il = cornersUL_ILField.getIntValue();
        pul.xl = cornersUL_XLField.getIntValue();
        pul.x  = cornersUL_XField. getDoubleValue();
        pul.y  = cornersUL_YField. getDoubleValue();
        CdpPoint[] corners = new CdpPoint[]{ pll, plr, pur, pul };
        cdpModel.setCorners(corners);
    }

    private void setModelToRequiredFields()
    {
        cdpModel.setFirstCDP(firstCDPField.getIntValue());
        cdpModel.setIlIncrement(ilLabelIncrementField.getIntValue());
        cdpModel.setXlIncrement(xlLabelIncrementField.getIntValue());
        cdpModel.setIlInterval(ilSpacingField.getDoubleValue());
        cdpModel.setXlInterval(xlSpacingField.getDoubleValue());
    }

    private void setModelByAngleFields()
    {
        setModelToRequiredFields();
        cdpModel.setAngle(SUtil.sval(angleField.getValue()+""));
        cdpModel.setOriginIL((int) SUtil.sval(angleILField.getValue()+""));
        cdpModel.setOriginXL((int) SUtil.sval(angleXLField.getValue()+""));
        cdpModel.setOriginX(SUtil.sval(angleXField.getValue()+""));
        cdpModel.setOriginY(SUtil.sval(angleYField.getValue()+""));
        cdpModel.setNumInlines((int) SUtil.sval(angleNumILField.getValue()+""));
        cdpModel.setNumXlines((int) SUtil.sval(angleNumXLField.getValue()+""));
    }

    void setFieldsFromModel()
    {
        firstCDPField.setValue(cdpModel.getFirstCDP());
        ilSpacingField.setValue(cdpModel.getIlInterval());
        xlSpacingField.setValue(cdpModel.getXlInterval());
        ilLabelIncrementField.setValue(cdpModel.getIlIncrement());
        xlLabelIncrementField.setValue(cdpModel.getXlIncrement());
        ilOriginField.setValue(cdpModel.getOriginIL());
        xlOriginField.setValue(cdpModel.getOriginXL());
        bufferSizeField.setValue(cdpModel.getBuffer());
        
        CdpPoint p = cdpModel.getPoint(Corner.UpperLeft);
        cornersUL_ILField.setValue(p.il);
        cornersUL_XLField.setValue(p.xl);
        cornersUL_XField.setValue(p.x);
        cornersUL_YField.setValue(p.y);
        p = cdpModel.getPoint(Corner.UpperRight);
        cornersUR_ILField.setValue(p.il);
        cornersUR_XLField.setValue(p.xl);
        cornersUR_XField.setValue(p.x);
        cornersUR_YField.setValue(p.y);
        p = cdpModel.getPoint(Corner.LowerLeft);
        cornersLL_ILField.setValue(p.il);
        cornersLL_XLField.setValue(p.xl);
        cornersLL_XField.setValue(p.x);
        cornersLL_YField.setValue(p.y);
        p = cdpModel.getPoint(Corner.LowerRight);
        cornersLR_ILField.setValue(p.il);
        cornersLR_XLField.setValue(p.xl);
        cornersLR_XField.setValue(p.x);
        cornersLR_YField.setValue(p.y);
        
        angleField.setValue(cdpModel.getAngle());
        angleILField.setValue(cdpModel.getOriginIL());
        angleXLField.setValue(cdpModel.getOriginXL());
        angleXField.setValue(cdpModel.getOriginX());
        angleYField.setValue(cdpModel.getOriginY());
        angleNumILField.setValue(cdpModel.getNumInlines());
        angleNumXLField.setValue(cdpModel.getNumXlines());
        
        p = cdpModel.getMatchPoint();
        if (p != null) {
            matchAngleField.setValue(cdpModel.getAngle());
            matchInlineField.setValue(p.il);
            matchXLineField.setValue(p.xl);
            matchXField.setValue(p.x);
            matchYField.setValue(p.y);
        }
    }

    protected void cdpGridMethodChanged(CdpGridMethod method)
    {
        cdpGridMethod = method;
        setEstimateEnabled(method == CdpGridMethod.Estimate);
        setCornersEnabled(method == CdpGridMethod.Corners);
        setAngleEnabled(method == CdpGridMethod.Angle);
    }
    
    void setAngleEnabled(boolean b)
    {
        angleField.setEnabled(b);
        angleILField.setEnabled(b);
        angleXLField.setEnabled(b);
        angleXField.setEnabled(b);
        angleYField.setEnabled(b);
        angleNumILField.setEnabled(b);
        angleNumXLField.setEnabled(b);
    }

    void setCornersEnabled(boolean b)
    {
        cornersUL_ILField.setEnabled(b);
        cornersUL_XLField.setEnabled(b);
        cornersUL_XField.setEnabled(b);
        cornersUL_YField.setEnabled(b);
        cornersUR_ILField.setEnabled(b);
        cornersUR_XLField.setEnabled(b);
        cornersUR_XField.setEnabled(b);
        cornersUR_YField.setEnabled(b);
        cornersLL_ILField.setEnabled(b);
        cornersLL_XLField.setEnabled(b);
        cornersLL_XField .setEnabled(b);
        cornersLL_YField .setEnabled(b);
        cornersLR_ILField.setEnabled(b);
        cornersLR_XLField.setEnabled(b);
        cornersLR_XField.setEnabled(b);
        cornersLR_YField.setEnabled(b);
    }

    private void setEstimateEnabled(boolean b)
    {
        estimateButton.setEnabled(b);
        ilOriginField.setEnabled(b);
        xlOriginField.setEnabled(b);
        bufferSizeField.setEnabled(b);
        matchGridCheckbox.setEnabled(b);
        setMatchGridEnabled(b && matchGridCheckbox.isSelected());
    }
    
    private void setMatchGridEnabled(boolean b)
    {
        matchAngleField.setEnabled(b);
        matchInlineField.setEnabled(b);
        matchXLineField.setEnabled(b);
        matchXField.setEnabled(b);
        matchYField.setEnabled(b);
    }

    private void initializeFields()
    {
        SimpleField.preferredFieldWidth = 150; //set default sizes
        SimpleField.preferredLabelWidth = 30; //set default sizes
        SimpleField.hAlignment = SwingConstants.RIGHT;
        Dimension longLabelSize = new Dimension(80,25);
        Dimension longerLabelSize = new Dimension(150,25);
        firstCDPField = new IntField("1st CDP:", 1, longLabelSize);
        ilSpacingField = new FloatField("IL:", 110);
        xlSpacingField = new FloatField("XL:", 110);
        ilLabelIncrementField = new IntField("IL:", 1);
        xlLabelIncrementField = new IntField("XL:", 1);
        bufferSizeField = new IntField("Buffer:", 1, longLabelSize);
        ilOriginField = new IntField("Origin IL:", 1001, longLabelSize);
        xlOriginField = new IntField("XL:", 1001, longLabelSize);
        cornersUL_ILField = new IntField("IL:", 0);
        cornersUL_XLField = new IntField("XL:", 0);
        cornersUL_XField = new FloatField("X:", 0);
        cornersUL_YField = new FloatField("Y:", 0);
        cornersUR_ILField = new IntField("IL:", 0);
        cornersUR_XLField = new IntField("XL:", 0);
        cornersUR_XField = new FloatField("X:", 0);
        cornersUR_YField = new FloatField("Y:", 0);
        cornersLL_ILField = new IntField("IL:", 0);
        cornersLL_XLField = new IntField("XL:", 0);
        cornersLL_XField = new FloatField("X:", 0);
        cornersLL_YField = new FloatField("Y:", 0);
        cornersLR_ILField = new IntField("IL:", 0);
        cornersLR_XLField = new IntField("XL:", 0);
        cornersLR_XField = new FloatField("X:", 0);
        cornersLR_YField = new FloatField("Y:", 0);
        scatterShotIncField = new IntField("Shot Inc.:", 4, longLabelSize);
        //scatterChanIncField = new IntField("Chan Inc.:", 1, longLabelSize);
        line3dFileOutField = new FileField("Output LINE3D File:", null, longerLabelSize, "Save");
        angleField = new FloatField("Angle: ", 0, longLabelSize);
        angleNumXLField = new IntField("# XLines:", 50, longLabelSize);
        angleXField = new FloatField("X:", 0);
        angleYField = new FloatField("Y:", 0);
        angleNumILField = new IntField("# InLines:", 50, longLabelSize);
        angleILField = new IntField("IL:", 1001);
        angleXLField = new IntField("XL:", 1001);
        matchAngleField = new FloatField("Angle:", 0, longLabelSize);
        matchXField = new FloatField("X:", 0);
        matchYField = new FloatField("Y:", 0);
        matchInlineField = new IntField("IL:", 1001);
        matchXLineField = new IntField("XL:", 1001);
        displayILIncField = new IntField("Inline Inc.:", 10, longLabelSize);
        displayXLIncField = new IntField("Xline Inc.:", 10, longLabelSize);
        displayIL_LabelIncField = new IntField("IL Label Freq.:", 1, longLabelSize);
        displayXL_LabelIncField = new IntField("XL Label Freq.:", 1, longLabelSize);
        
    }
    public JPanel getPanel_10() {
        return panel;
    }
    public JCheckBox estimateMethodCheckBox() {
        return estimateMethodCheckBox;
    }

    public CdpModel getCdpModel()
    {
        return cdpModel;
    }

    public void setStationPlotter(StationPlotter stationPlotter)
    {
        this.stationPlotter = stationPlotter;
    }

    public int getInlineDisplayInc()
    {
        return displayILIncField.getIntValue();
    }

    public int getXlineDisplayInc()
    {
        return displayXLIncField.getIntValue();
    }

    public void setCdpModel(CdpModel cdpModel)
    {
        if (cdpModel == null) return;
        if (cdpModel.realModel == 0) cdpModel = new CdpModel();
        this.cdpModel = cdpModel;
        setFieldsFromModel();
    }

    public boolean isGridEnabled()
    {
        return this.showGridButton.isSelected();
    }

    public int getDisplayInlineLabelInc()
    {
        return displayIL_LabelIncField.getIntValue();
    }
    
    public int getDisplayXlineLabelInc()
    {
        return displayXL_LabelIncField.getIntValue();
    }

    public boolean isInlineLabelDrawEnabled()
    {
       return this.showILLabelsCheckBox.isSelected();
    }

    public boolean isXlineLabelDrawEnabled()
    {
        return this.showXLLabelsCheckBox.isSelected();
    }

    public void setNormalPanMouseMode()
    {
        panNormalButton.doClick();
    }
}
