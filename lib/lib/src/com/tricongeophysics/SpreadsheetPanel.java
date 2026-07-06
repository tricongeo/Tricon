package com.tricongeophysics;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;

public class SpreadsheetPanel extends JPanel implements ListSelectionListener, RowColorChangedListener, ModelChangedListener {
	
	protected JTable table;
	private JScrollPane scroller;
	protected AbstractSpreadsheetModel model;
	protected JButton addRowButton;
	protected JButton delRowButton;
	private JButton interpButton;
	private JButton extrapDownButton;
	private JButton extrapUpButton;
	protected JButton delColButton;
	protected JButton addColButton;
	private JButton reloadButton;
    private boolean changed = false;
	private JButton copyButton;
	private JButton pasteButton;
	//private Object[][] copyClipboard;
	public JMenuItem copyItem;
	public JMenuItem pasteItem;
	private ArrayList<RowAddedListener> rowAddedListeners = new ArrayList<RowAddedListener>();
	private MouseListener mouseListener;
	protected static final int MaxRowDelete = 50;
	private static final int ColSizeMinDefault = 50;
	private static final int ColSizeMaxDefault = 300;  //in pixels
	private static final int MaxRowSearch = 1000;

	public SpreadsheetPanel(AbstractSpreadsheetModel model2) {
	    if (model2 == null) model2 = new ReflectiveTableModel();
	    table = new TableDataTable();
		this.model = (AbstractSpreadsheetModel) model2;
		model2.addRowColorChangedListener(this);
		
		setName("");
		
		table.setModel(model2);
		//table.setDefaultRenderer(Object.class, new FastStringRenderer());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setCellSelectionEnabled(true);
		table.setRowSelectionAllowed(true);
		table.setAutoCreateRowSorter(true);
		table.setDefaultEditor(Date.class, new DefaultCellEditor(new JTextField()));
		table.setDefaultRenderer(Double.class, new DoubleRenderer());
		table.setDefaultRenderer(Float.class, new DoubleRenderer());
		table.setDefaultRenderer(Boolean.class, new BoolRenderer());
		table.setDefaultRenderer(Password.class, new PasswordRenderer());
		FileCellEditor fileEditor = new FileCellEditor();
		mouseListener = new SpreadsheetMouseListener();
		fileEditor.addMouseListener(mouseListener);
		table.setDefaultEditor(File.class, fileEditor);
		table.getTableHeader().setDefaultRenderer(new TableHeaderRenderer());
		table.getSelectionModel().addListSelectionListener(this);
		scroller = new JScrollPane(table);
		setLayout(new BorderLayout());
		add(scroller, BorderLayout.CENTER);
		
		//this.addMouseListener(new SpreadsheetMouseListener());
		table.addMouseListener(mouseListener);

		add(getRowButtonPane(), BorderLayout.WEST);
		add(getColButtonPane(), BorderLayout.NORTH);
		
		model.addModelChangedListener(this);
		
		modelChanged();
		setDeleteRowColEnabled(false);
		setEnableInterp(false);
	}
	
    void initColumnSizes() {
		int nCols = table.getColumnCount();
		//int pixelsPerChar = 8;
		
		for(int i=0; i<nCols; i++) {
//			String name = model.getColumnName(i);
//			int nameWidth = name.length();
			TableColumn column = table.getColumnModel().getColumn(i);
			Object headerVal = column.getHeaderValue();
			TableCellRenderer headerRenderer = column.getHeaderRenderer();
			if (headerRenderer == null){
				headerRenderer = table.getTableHeader().getDefaultRenderer();
			}
			Component c = headerRenderer.getTableCellRendererComponent(table, headerVal, false, false, -1, i);
			int headerWidth = c.getPreferredSize().width + 5;
			int maxFromRows = 0;
			int rowCount = getRowCount();
			rowCount = Math.min(rowCount, MaxRowSearch);
			for (int j=0; j< rowCount; j++) {
				Object v = model.getValueAt(j, i);
				TableCellRenderer cellRenderer = table.getCellRenderer(j, i);
				if (v == null) continue;
				//rowWidth = Math.max(rowWidth, v.toString().length());
				c = cellRenderer.getTableCellRendererComponent(table, v, false, false, j, i);
				maxFromRows = Math.max(maxFromRows, c.getPreferredSize().width+5);
			}
			int allMax = Math.max(headerWidth, maxFromRows);
			int bestMax = Math.max(ColSizeMinDefault, allMax);
			bestMax = Math.min(ColSizeMaxDefault, allMax);
			column.setPreferredWidth(bestMax);
		}
	}

	public void modelChanged() {
//		if (model == null)
//			setRowButtonsEnabled(false);
//		else
//			setRowButtonsEnabled(true);
		if (model == null) return;
		model.fireTableStructureChanged();
		//table = new TableDataTable();
		table.setModel(model);
		initColumnSizes();
		//scroller.setRowHeader(new JViewport());
	//	JViewport vp = new JViewport();
	//	vp.setView(new SpreadsheetRowHeader(table));
	//	scroller.setRowHeader(vp);
		scroller.setRowHeaderView(new SpreadsheetRowHeader(table)); 
		//this.invalidate();
	//	model.fireTableDataChanged();
	//	scroller.setSize(scroller.getWidth()-1, scroller.getHeight()-1);
	//	this.repaint();
	//	scroller.getRowHeader().revalidate();
	//	scroller.revalidate();
		//table.setModel(model);
	}

	public Component getColButtonPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		panel.add(getCopyPastePane(BoxLayout.X_AXIS));
		
		//panel.add(Box.createHorizontalStrut(50));
		panel.add(Box.createHorizontalGlue());
		
		panel.add(new JLabel("Columns "));
		panel.add(Box.createHorizontalStrut(5));
		
		addColButton = new PlusButton();
		panel.add(addColButton);
		addColButton.setToolTipText("add new column");
		addColButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				NewColumnDialog ncd = new NewColumnDialog();
				ncd.showDialog();
				String name = ncd.getColumnName();
				Class<?> colClass = ncd.getColClass();
				if (ncd.getClickedOK()) model.addColumn(name, colClass);
				initColumnSizes();
			}}
		);

		delColButton = new MinusButton();
		panel.add(delColButton);
		delColButton.setToolTipText("delete selected column");
		delColButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int selectedCol = table.getSelectedColumn();
				int modelCol = table.convertColumnIndexToModel(selectedCol);
				if (model.isOptionalColumn(modelCol)) model.delColumn(modelCol);
				else {
					String colName = model.getColumnName(selectedCol);
					JOptionPane.showMessageDialog(SpreadsheetPanel.this, "\""+colName + "\" is a required column \n and therefore can't be deleted.");
				}
			}}
		);
		
		panel.add(Box.createHorizontalStrut(10));
		
		panel.add(new JLabel("Extrap/Interp "));
		extrapUpButton = new JButton(SUtil.createImageIcon(this.getClass(),"images/uparrow.png"));
		extrapUpButton.setMargin(new Insets(0, 0, 0, 0));
		extrapUpButton.setToolTipText("Extrapolate Up");
		panel.add(extrapUpButton);
		extrapUpButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int[] cols = table.getSelectedColumns();
				int[] modelCols = convertColIndicesToModel(cols);
				int[] rows = table.getSelectedRows();
				int[] modelRows = convertRowIndicesToModel(rows);
				if (modelRows.length<3) return;
				for (int i=0; i<cols.length; i++)
					model.extrapUp(modelRows, modelCols[i]);
				//Arrays.sort(rows);
				//model.fireTableRowsUpdated(rows[0], rows[rows.length-1]);
				//model.fireTableRowsUpdated(0, table.getRowCount()-1);
				try {
	                model.fireTableRowsUpdated(0, model.getRowCount()-1);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}}
		);
		
		extrapDownButton = new JButton(SUtil.createImageIcon(this.getClass(),"images/downarrow.png"));
		extrapDownButton.setMargin(new Insets(0, 0, 0, 0));
		extrapDownButton.setToolTipText("Extrapolate Down");
		panel.add(extrapDownButton);
		extrapDownButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int[] cols = table.getSelectedColumns();
				int[] modelCols = convertColIndicesToModel(cols);
				int[] rows = table.getSelectedRows();
				int[] modelRows = convertRowIndicesToModel(rows);
				if (modelRows.length<3) return;
				for (int i=0; i<cols.length; i++)
					model.extrapDown(modelRows, modelCols[i]);
				//Arrays.sort(rows);
				//model.fireTableRowsUpdated(rows[0], rows[rows.length-1]);
				//model.fireTableRowsUpdated(0, table.getRowCount()-1);
				try {
				model.fireTableRowsUpdated(0, model.getRowCount()-1);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}}
		);
		
		interpButton = new JButton(SUtil.createImageIcon(this.getClass(),"images/interpolate.png"));
		interpButton.setMargin(new Insets(0, 0, 0, 0));
		interpButton.setToolTipText("Interpolate Rows");
		panel.add(interpButton);
		interpButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int[] cols = table.getSelectedColumns();
				int[] modelCols = convertColIndicesToModel(cols);
				int[] rows = table.getSelectedRows();
				int[] modelRows = convertRowIndicesToModel(rows);
				if (modelRows.length<3) return;
				for (int i=0; i<cols.length; i++)
					model.interpRows(modelRows, modelCols[i]);
				//Arrays.sort(rows);
				//model.fireTableRowsUpdated(rows[0], rows[rows.length-1]);
				try {
				model.fireTableRowsUpdated(0, model.getRowCount()-1);
				} catch (Exception e1) {
					e1.printStackTrace();
				} //catch exception when rowCount higher than rowSorter count (because we're not using fireRowsInserted)
			}}
		);
		
		panel.add(Box.createHorizontalStrut(10));
		
		panel.add(new JLabel("Reload "));
		reloadButton = new ReloadButton();
		reloadButton.setToolTipText("Reload Spreadsheet from Text File");
		panel.add(reloadButton);
		reloadButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				fireReloadSheet();
			}
		});
		
		panel.add(Box.createHorizontalGlue());
		return panel;
	}

	/**
	 * JPanel containing copy and paste buttons
	 * @param boxLayoutAxis x or y axis
	 * @return
	 */
	public Component getCopyPastePane(int boxLayoutAxis) {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, boxLayoutAxis));

		copyButton = new CopyButton();
		copyButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				copyCells();
				setCopyPasteEnabled(true);
			}});
		
		
		pasteButton = new PasteButton();
		pasteButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteCells();
				setCopyPasteEnabled(false);
			}});
		
		setCopyPasteEnabled(false);
		
		if (boxLayoutAxis == BoxLayout.X_AXIS)
			pane.add(Box.createHorizontalStrut(30));
		else
			pane.add(Box.createVerticalStrut(30));
		pane.add(copyButton);
		pane.add(pasteButton);
		
		return pane;
	}

	protected void pasteCells() {
		String cb = getClipboard();
		if (cb == null) return;
		String[][] copyClipboard = sysClipBoardToArray(cb);
		if (copyClipboard.length == 0) return;
		if (copyClipboard.length > 1 || copyClipboard[0].length > 1) {
			pasteMany();
		} else {
			pasteOne();
		}
		
		//model.fireTableRowsUpdated(rows[0], rows[rows.length-1]);
		model.fireTableRowsUpdated(0, table.getRowCount()-1);
		
		//copyClipboard = null;
	}

	/**
	 * converts string of text to 2D array of Strings
	 * lines (rows) are separated by return characters.
	 * columns are separated by tab characters.
	 * 
	 * @param text
	 * @return
	 */
	private String[][] sysClipBoardToArray(String text) {
		if (text == null) return null;
		String[] lines = text.split("\n");
		if (lines == null) return null;
		
		int nLines = lines.length;
		int maxCols = 0;
		
		//count max number of columns
		for (int i=0; i<nLines; i++) {
			String[] cols = lines[i].split("\t");
			int nCols = cols.length;
			maxCols = Math.max(nCols, maxCols);
		}
		
		//now populate array
		String[][] array = new String[nLines][maxCols];
		for (int i=0; i<nLines; i++) {
			String[] cols = lines[i].split("\t");
			for (int j=0; j<maxCols; j++) {
				if (j < cols.length)
					array[i][j] = cols[j];
				else
					array[i][j] = null;
			}
		}
		
		return array;
	}

	private void pasteOne() {
		int[] rows = table.getSelectedRows();
		int[] cols = table.getSelectedColumns();
		
		Arrays.sort(rows);
		Arrays.sort(cols);
		
		String[][] copyClipboard = getClipBoardArray();
		//if (copyClipboard == null || copyClipboard.length == 0 || copyClipboard[0].length == 0) return;
		if (copyClipboard == null || copyClipboard.length == 0) return;
		
		Object o = null;
		if (copyClipboard[0].length > 0)
			o = copyClipboard[0][0];

		int maxRow=rows[0], maxCol=cols[0];
		
		for (int i=0; i<cols.length; i++) {
			for (int j=0; j<rows.length; j++) {
				try {
					paste(o, rows[j], cols[i]);
					maxRow = rows[j];
					maxCol=cols[i];
				} catch (Exception e) {
					e.printStackTrace();
				}
			}			
		}
		table.addRowSelectionInterval(rows[0], maxRow);
		table.addColumnSelectionInterval(cols[0], maxCol);
	}

	private String[][] getClipBoardArray() {
		String cb = getClipboard();
		String[][] array = sysClipBoardToArray(cb);
		return array;
	}

	private void pasteMany() {
		int[] rows = table.getSelectedRows();
		int col = table.getSelectedColumn();
		
		Arrays.sort(rows);
		
		int row = rows[0];
		
		String[][] copyClipboard = getClipBoardArray();
		if (copyClipboard == null || copyClipboard.length == 0 || copyClipboard[0].length == 0) return;
		
		int maxRow=row, maxCol=col;
		for (int i=0; i<copyClipboard.length; i++) {
			for (int j=0; j<copyClipboard[0].length; j++) {
				Object o = copyClipboard[i][j];
				if (row+i < table.getRowCount() && col+j < table.getColumnCount()) {
					try {
						paste(o, row+i, col+j);
						maxRow = row+i;
						maxCol = col+j;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}			
		}
		table.addRowSelectionInterval(row, maxRow);
		table.addColumnSelectionInterval(col, maxCol);
	}

	/**
	 * Attempts to "cast" object to new type by converting to string and using string constructor.
	 * If that fails, tries converting to an integer using SUtil.getNumVal()
	 * @param o object to paste
	 * @param row to paste into
	 * @param col to paste into
	 * @throws Exception
	 */
	private void paste(Object o, int row, int col) throws Exception {
		Constructor<? extends Object> constructor=null;
		int modCol = table.convertColumnIndexToModel(col);
		if (!model.isColEditable(modCol)) return;;
		Class<? extends Object> classTo = table.getColumnClass(col);
		Object newObject = o;
		if (classTo.isInstance(o)) //... first, see if same class and use direct assignment
			table.setValueAt(o, row, col);
		else {
			try { //...now, try casting
				newObject = classTo.cast(o);
				table.setValueAt(newObject, row, col);
			} catch (Exception e1) {
				try {  //...Next, try using string accepting constructor
					//Object newObject = classTo.cast(o);
					constructor = classTo.getConstructor(o.toString().getClass());
					newObject = constructor.newInstance(o.toString());
					table.setValueAt(newObject, row, col);

				} catch (Exception e2) {  //...Finally, try setting to an integer with string constructor
					int val = (int) SUtil.getNumVal(o);
					//Object newObject = classTo.cast(val);
					//Object newObject = constructor.newInstance(val);
					if (constructor != null) newObject = constructor.newInstance(val+"");
					table.setValueAt(newObject, row, col);
				}
			}
		} 
	}

	protected void copyCells() {
		int[] rows = table.getSelectedRows();
		int[] cols = table.getSelectedColumns();
		
		String toSystemClipboard = "";
		
		Arrays.sort(rows);
		
		if (rows == null || cols == null) return;
		
	//	copyClipboard = new Object[cols.length][rows.length];

		for (int j=0; j<rows.length; j++) {
			for (int i=0; i<cols.length; i++) {
				Object o = table.getValueAt(rows[j], cols[i]);
			//	copyClipboard[i][j] = o;
				if (o == null)
					toSystemClipboard += "\t";
				else
					toSystemClipboard += o+"\t";
			}			
			toSystemClipboard += "\n";
		}
		setCopyPasteEnabled(true);
		
		StringSelection ss = new StringSelection(toSystemClipboard); 
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
	}

	protected void fireReloadSheet() {
	    if (model == null) return;
		int answer = 
			JOptionPane.showConfirmDialog(this, "Really Reload Table?\nThis will undo all of your edits.",
					"Reload Table?", JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			model.reload();
			model.fireTableDataChanged();
		}
	}

	protected int[] convertColIndicesToModel(int[] cols) {
		if (cols == null) return null;
		int[] modelCols = new int[cols.length];
		for (int i=0; i<cols.length; i++) {
			modelCols[i] = table.convertColumnIndexToModel(cols[i]);
		}
		return modelCols;
	}


	protected int[] convertRowIndicesToModel(int[] rows) {
		if (rows == null) return null;
		int[] modelRows = new int[rows.length];
		for (int i=0; i<rows.length; i++) {
			modelRows[i] = table.convertRowIndexToModel(rows[i]);
		}
		return modelRows;
	}

	public Component getRowButtonPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		//panel.add(Box.createVerticalStrut(30));
		panel.add(Box.createVerticalGlue());
		
		panel.add(new JLabel("R"));
		panel.add(new JLabel("o"));
		panel.add(new JLabel("w"));
		panel.add(new JLabel("s"));
		panel.add(Box.createVerticalStrut(5));
		
		addRowPaneButtons(panel);
		
		panel.add(Box.createVerticalGlue());
		
		return panel;
	}

	protected void addRowPaneButtons(JPanel panel) {
		panel.add(getAddRowButton());
		panel.add(getDelRowButton());
	}

	protected Component getDelRowButton() {
		delRowButton = new MinusButton();
		delRowButton.setToolTipText("delete selected row(s)");
		delRowButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (table.getRowCount() < 1) return;
				int[] rows = table.getSelectedRows();
				if (rows.length > MaxRowDelete) {
					JOptionPane.showMessageDialog(SpreadsheetPanel.this, 
							"Sorry...\nCan't delete more than "+MaxRowDelete+" Rows at a time.",
							"Row Delete Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int yesno = JOptionPane.showConfirmDialog(SpreadsheetPanel.this, 
						"OK to delete "+rows.length+" rows?",
						"Delete Rows", JOptionPane.YES_NO_OPTION);
				if (yesno == JOptionPane.YES_OPTION) {
					int[] modelRows = convertRowIndicesToModel(rows);
					model.delRows(modelRows);
					scroller.setRowHeaderView(new SpreadsheetRowHeader(table));
				}
			}}
		);
		return delRowButton;
	}

	protected Component getAddRowButton() {
		addRowButton = new PlusButton();
		addRowButton.setToolTipText("add new row");
		addRowButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				addRow();
			}}
		);
		return addRowButton;
	}

	protected void addRow() {
		int selectedRow = table.getSelectedRow();
		int modelRow = -1;
		if (table.getRowCount() > 0) {
			if (selectedRow < 0) selectedRow = table.getRowCount() - 1;
			modelRow = table.convertRowIndexToModel(selectedRow);
		}
		//table.getRowSorter().setSortKeys(null); //turn off sorting
		//table.getRowSorter().rowsInserted(0, 0);
		//RowSorter<? extends TableModel> tmp = table.getRowSorter();
		//table.setRowSorter(null);
		String ans = JOptionPane.showInputDialog(SpreadsheetPanel.this, "How many rows would you like to add?", "1");
		if (ans == null) return;
		int rows = 0;
		try {
			rows = Integer.parseInt(ans);
		} catch (Exception e2) {
			e2.printStackTrace();
		};
		if (rows == 0) return;
		
		for (int i=0; i<rows; i++) {
			model.addRow(modelRow+i);
			//fireRowAdded(model, modelRow+1+i);
			fireRowAdded(model, modelRow+1+i);
		}
		scroller.setRowHeaderView(new SpreadsheetRowHeader(table));  
		//table.setRowSorter(tmp);
	}

	protected void fireRowAdded(AbstractSpreadsheetModel model2, int modelRow) {
		for (RowAddedListener l: rowAddedListeners) {
			l.rowAdded(model2, modelRow);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int nSelectedRows = 0;
		int[] rows = table.getSelectedRows();
		nSelectedRows = (rows == null) ? 0 : rows.length;
		if (nSelectedRows > 2) setEnableInterp(true);
		else setEnableInterp(false);
		if (nSelectedRows > 0) {
			setDeleteRowColEnabled(true);
			setCopyPasteEnabled(true);
			fireRowColSelectedChanged(rows[0], table.getSelectedColumn());
		}
		else {
			setDeleteRowColEnabled(false);
			setCopyPasteEnabled(false);
		}
		setChanged(true);
		if (rows == null || rows.length == 0) return;
		((SpreadsheetRowHeader)scroller.getRowHeader().getView()).getSelectionModel().setSelectionInterval(rows[0], rows[rows.length-1]);
	}
	
	private void setCopyPasteEnabled(boolean b) {
		//if (copyButton == null) return;
		//if (pasteButton == null) return;
		if (copyButton != null) copyButton.setEnabled(b);
		copyItem.setEnabled(b);
		String copyClipboard = getClipboard();
		if (copyClipboard != null){
			if (pasteButton != null) pasteButton.setEnabled(true);
			pasteItem.setEnabled(true);
		} else {
			if (pasteButton != null) pasteButton.setEnabled(false);
			pasteItem.setEnabled(false);
		}
	}
	
	public String getClipboard() { 
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null); 
		try { 
			if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) { 
				String text = (String)t.getTransferData(DataFlavor.stringFlavor); 
				return text; 
			} 
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null; 
	} 
	
	private void fireRowColSelectedChanged(int row, int col) {
		if (row < 0) return;
		row = table.convertRowIndexToModel(row);
		SelectionChangedMonitor.fireSelectionChanged(new SelectionChangedEvent(getName(), row));
	}

	public void setDeleteRowColEnabled(boolean enabled) {
		if (delColButton != null) delColButton.setEnabled(enabled);
		if (delRowButton != null) delRowButton.setEnabled(enabled);
	}

	public void setEnableInterp(boolean enabled) {
		if (interpButton     != null) interpButton.setEnabled(enabled);
		if (extrapDownButton != null) extrapDownButton.setEnabled(enabled);
		if (extrapUpButton   != null) extrapUpButton.setEnabled(enabled);
	}
	
	public void setRowButtonsEnabled(boolean enabled) {
		if (addColButton != null) addColButton.setEnabled(enabled);
		if (addRowButton != null) addRowButton.setEnabled(enabled);
		if (delRowButton != null) delRowButton.setEnabled(enabled);
	}

	public void setModel(AbstractSpreadsheetModel model) {
		if (model == null) return;
		this.model = model;
		model.addRowColorChangedListener(this);
		table.setModel(model);
		modelChanged();
	}
	
	public void addTableModelListener(TableModelListener l) {
		if( model == null ) return;
		model.addTableModelListener(l);
	}

    public void setChanged(boolean changed)
    {
        this.changed = changed;
    }

    public boolean getChanged()
    {
        return changed;
    }

    @Override
    public void rowColorChanged()
    {
        table.repaint();
    }

    public class SpreadsheetMouseListener implements MouseListener {

    	private JPopupMenu popup;
		private MouseEvent me;
    	
    	private SpreadsheetMouseListener(){
    		popup = new JPopupMenu();
    		copyItem = new JMenuItem("Copy");
    		copyItem.setEnabled(false);
    		copyItem.addActionListener(new ActionListener(){
    			@Override
    			public void actionPerformed(ActionEvent e) {
    				copyCells();
    			}});
    		popup.add(copyItem);

    		pasteItem = new JMenuItem("Paste");
    		pasteItem.setEnabled(false);
    		pasteItem.addActionListener(new ActionListener(){
    			@Override
    			public void actionPerformed(ActionEvent e) {
    				pasteCells();
    			}});
    		popup.add(pasteItem);
    	}

		@Override
    	public void mouseClicked(MouseEvent e) {
    		doClick(e);
    	}

		@Override
    	public void mousePressed(MouseEvent e) {
    		doClick(e);
    	}
    	@Override
    	public void mouseReleased(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		
		private void doClick(MouseEvent e) {
			me = e;
			if (e.getButton() == MouseEvent.BUTTON3) {
				
				Point point = new Point(me.getX(), me.getY());
				int row = table.rowAtPoint(point);
				int col = table.columnAtPoint(point);
				if (!table.isCellSelected(row, col)) {
					table.setRowSelectionInterval(row, row);
					table.setColumnSelectionInterval(col, col);
				}
				
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
    
    public void addRowAddedListener(RowAddedListener l) {
		rowAddedListeners.add(l);
	}

	public Component getTable() {
		return table;
	}

	public int getRowCount() {
		return table.getRowCount();
	}

	public void addSheetKeyListener(KeyListener l) {
		table.addKeyListener(l);
	}

	public Object getValueAt(int row, String colName) {
		return model.getValueAt(row, colName);
	}
	
	public void setValueAt(int modelRow, String colName, Object val) {
		if (modelRow < 0 || modelRow >= model.getRowCount()) {
			SUtil.printErr("SpreadsheetPane.setValueAt() - modelRow outside bounds: " + modelRow);
			return;
		}
		int col = model.getColumnIndex(colName);
		if (col < 0) {
			SUtil.printErr("SpreadsheetPane.setValueAt() - colName not found: " + colName);
			return;
		}
		model.setValueAt(val, modelRow, col);
	}
	
	public void setRowButtonsVisible(boolean visible) {
		addRowButton.setVisible(visible);
		delRowButton.setVisible(visible);
	}

	public void extrapDown(String colName, int fromRow, int toRow) {
		int col = model.getColumnIndex(colName);
		if (col < 0) return;
		int[] rows = new int[toRow - fromRow + 1];
		for (int i=fromRow; i<=toRow; i++) rows[i] = i;
//		int[] modelRows = convertRowIndicesToModel(rows);
//		model.extrapDown(modelRows, col);
		model.extrapDown(rows, col);
	}
}


