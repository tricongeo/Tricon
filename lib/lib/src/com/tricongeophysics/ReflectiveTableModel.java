package com.tricongeophysics;

import java.awt.Container;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import com.tricongeophysics.SUtil.PrecisionLevels;
import com.tricongeophysics.TableData.ColumnType;

public class ReflectiveTableModel extends AbstractSpreadsheetModel implements Iterable<TableData>, Serializable{

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected String[] columnNames;
	transient protected Method[] getMethods;
	transient protected Method[] setMethods;
	
	protected ArrayList<TableData> tableData;
	
	protected Class<? extends TableData> tableClass;
	transient private TableLoader tableLoader;
    private int nStandardCols;
	
	public ReflectiveTableModel(ArrayList<TableData> tableData) {
		this.tableData = tableData;
		if( tableData == null ) {
			//SUtil.printErr("ReflectiveTableModel: null input found... aborting!");
			return;
		}
		if (getRowCount() == 0) {
			//SUtil.printErr("ReflectiveTableModel: no rows found... aborting!");
			return;
		}
		tableClass = tableData.get(0).getClass();
		resetColumnNames();
		int nCols = getColumnCount();
		if (nCols == 0) {
			SUtil.printErr("ReflectiveTableModel: no columns found... aborting!");
			return;
		}
		getMethods = new Method[nCols];
		setMethods = new Method[nCols];
		initializeColumnMethods();
	}
	
	public ReflectiveTableModel() {
	    this(null);
	}

	private void initializeColumnMethods() {
		Method[] unsortedMethods = tableClass.getMethods();
		boolean foundGet = false;
		boolean foundSet = false;
		
		for (int i=0; i<columnNames.length; i++) {
			String columnName = columnNames[i];
			foundGet = false;
			foundSet = false;
			for (Method m: unsortedMethods) {
				String methodName = m.getName();
				if (methodName.equalsIgnoreCase("get"+columnName)) {
					getMethods[i] = m;
					foundGet = true;
				}
				if (methodName.equalsIgnoreCase("set"+columnName)) {
					setMethods[i] = m;
					foundSet = true;
				}
			}
			nStandardCols = 0;
			for (Method m: setMethods) {
			    if (m != null) nStandardCols++;
			}
			if(!foundGet) SUtil.print("ReflectiveTableModel.initializeColumnMethods: no get method found for field: "+columnName+". Could be optional column."); //no need to shout! This is now common for re-openning projects
			if(!foundSet) SUtil.print("ReflectiveTableModel.initializeColumnMethods: no set method found for field: "+columnName+". Could be optional column."); //no need to shout! This is now common for re-openning projects
		}

	}

	@Override
	public int getColumnCount() {
		if (columnNames == null) return 0;
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		if (tableData == null) return 0;
		return tableData.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < 0    || rowIndex >= getRowCount()) return null;
		if (columnIndex < 0 || columnIndex >= getColumnCount()) return null;
		TableData o = tableData.get(rowIndex);
		if (o == null) return null;
		
		//First, try standard column methods
		if (columnIndex < getMethods.length) {
		    Method m = getMethods[columnIndex];
		    if (m != null) {
		        try {
		            return m.invoke(o);
		        } catch (Exception e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		            return null;
		        } 
		    }
		}
		
		//must be optional column
		Object val = o.getOptionalValue(columnIndex);
		val = (val == null) ? "" : val;
		return val;
	}
	
	@Override
	public void setValueAt(Object newVal, int rowIndex, int columnIndex) {
		if (rowIndex < 0    || rowIndex >= getRowCount()) return;
		if (columnIndex < 0 || columnIndex >= getColumnCount()) return;
		TableData o = tableData.get(rowIndex);
		if (o == null) return;
		
		// First, try standard methods
		if (columnIndex < setMethods.length) {
		    Method m = setMethods[columnIndex];
		    if (m != null) {
		        try {
		            m.invoke(o, newVal);
		            return;
		        } catch (Exception e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		            return;
		        } 
		    }
		}
		
		//must be optional column
		o.setOptionalValue(columnIndex, newVal);
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		if (tableData == null) return false;
		return tableData.get(0).isColEditable(col);
	}

	@Override
	public String getColumnName(int c) {
		if (columnNames == null || c >= getColumnCount()) return null;
		return columnNames[c];
	}

	public void addColumn(String name, Class colClass) {
		if (colNameExists(name)) return;
		for (TableData td: tableData) {
			td.addOptionalColumn(name, colClass);
		}
		resetColumnNames();
		this.fireTableStructureChanged();
	}

	private boolean colNameExists(String name) {
		if (columnNames == null) return false;
		for (String n: columnNames) {
			if (n.equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	public void delRow(int row) {
		if (row < 0 || row >= tableData.size()) return;
		try {
			tableData.remove(row);
			this.fireTableRowsDeleted(row, row);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public void addRow(int row) {
		row = (row < 0) ? 0 : row;
		try {
			tableData.add(row, tableClass.newInstance());
			//this.fireTableRowsInserted(row, row); //seems logical to use this, but it causes the whole table to re-sort, putting the new row at the top because it's filled with zeroes
			this.fireTableRowsInserted(0, 0);
			//this.fireTableDataChanged();
			//this.fireTableRowsUpdated(row, row); //seems to have desired effect, table redraws w/out resorting
		} catch (Exception e) {
			// TODO Auto-generated catch block
			SUtil.printErr("error while adding row: "+row);
			e.printStackTrace();
		} 
	}

	public void delColumn(int selectedCol) {
		if (setMethods == null || setMethods.length == 0) {
			SUtil.printErr("ReflectiveTableModel.delColumn(): setMethods not loaded - error deleting column " + selectedCol);
			return;
		}
		//int nStandardCols = setMethods.length;
		int delCol = selectedCol - nStandardCols;
		if (delCol < 0) return; //can't delete standard columns, only optional
		for (TableData td: tableData) {
			td.delOptionalColumn(delCol);
		}
		resetColumnNames();
		this.fireTableStructureChanged();
	}

	private void resetColumnNames() {
		if (tableData == null) return;
		if (tableData.size() == 0 || tableData.get(0) == null) return;
		columnNames = tableData.get(0).getColumnNames();
	}

	public void interpRows(int[] rows, int col) {
		int nRows = rows.length;
		if (nRows < 3) return;
		setValsOnSlope(rows, col, 0, nRows-1);
	}

	public void extrapUp(int[] rows, int col) {
		int nRows = rows.length;
		if (nRows < 3) return;
		setValsOnSlope(rows, col, nRows-1, nRows-2);
	}

	public void extrapDown(int[] rows, int col) {
		int nRows = rows.length;
		if (nRows < 3) return;
		setValsOnSlope(rows, col, 0, 1);
	}

	/**
	 * Handles interpolation and extrapolation.
	 * Slope is determined by the values in rows[] located at index1 and index2.
	 * All other values are calculated based on that slope and their distance
	 * in location from index1.
	 * 
	 * Backwards and forwards extrapolation are both supported.
	 * 
	 * @param rows
	 * @param col
	 * @param index1
	 * @param index2
	 */
	private void setValsOnSlope(int[] rows, int col, int index1, int index2) {
		if (rows == null || rows.length < 3) return;
		int nRows = rows.length;
		if (index1 < 0 || index1 >= nRows) return;
		if (index2 < 0 || index2 >= nRows) return;
		Object firstObject = getValueAt(rows[index1], col);
		Object secondObject = getValueAt(rows[index2], col);
		PrecisionLevels precisionLevel = SUtil.getPrecisionLevel(firstObject);
		if (precisionLevel == PrecisionLevels.NAN) return;  //can't interp if not a number!!
		double firstVal = SUtil.getNumVal(firstObject);//Double.parseDouble(firstObject.toString());
		double secondVal = SUtil.getNumVal(secondObject);//Double.parseDouble(secondObject.toString());
		double slope = (secondVal - firstVal)/(index2 - index1);
		for (int i=0; i<nRows; i++) {
			double newVal = firstVal + (i-index1) * slope;
			if (precisionLevel.equals(PrecisionLevels.INT))
				setValueAt((int)newVal, rows[i], col);
			if (precisionLevel.equals(PrecisionLevels.FLOAT))
				setValueAt((float)newVal, rows[i], col);
			if (precisionLevel.equals(PrecisionLevels.DOUBLE))
				setValueAt((double)newVal, rows[i], col);
			if (precisionLevel.equals(PrecisionLevels.BOOL))
                setValueAt(SUtil.getBoolVal(newVal), rows[i], col);
		}
	}

	public void delRows(int[] rows) {
		if (rows == null || rows.length == 0) return;
		Arrays.sort(rows);
		for (int i=0; i<rows.length; i++) {
			int row = rows[i] - i;  //array indexes update immediately for ArrayList.remove(), will be off by one after first delete, etc.
			delRow(row);
		}
	}

	public boolean isOptionalColumn(int col) {
		//int nStandardCols = setMethods.length; //setMethods.length doesn't help for saved projects that have optional columns initially in the columnNames array(which is how long setMethods is)
		if (col >= nStandardCols) return true;
		if (setMethods[col] == null) return true;
		return false;
	}
	
	public ArrayList getTableData() {
		return tableData;
	}

	public boolean isEmpty() {
		if (tableData == null) return true;
		return tableData.isEmpty();
	}

	public int size() {
		if (tableData == null) return 0;
		return tableData.size();
	}

	public Object get(int index) {
		if (tableData == null) return null;
		if (index < 0 || index >= tableData.size()) return null;
		return tableData.get(index);
	}

	public int indexOf(Object item) {
		if (tableData == null) return -1;
		return tableData.indexOf(item);
	}

	@Override
	public Iterator<TableData> iterator() {
		if (tableData == null) return null;
		return tableData.iterator();
	}

	/**
	 * returns maximum value (if String
	 * @param col
	 * @return
	 */
	public double getMax(int col) {
		if (tableData == null || col < 0) return 0;
		double max = SUtil.getNumVal(getValueAt(0,col));
		double val = 0;
		for (int i=0; i<size(); i++) {
			val = SUtil.getNumVal(getValueAt(i,col));
			max = Math.max(val, max);
		}
		return max;
	}

	public double getMin(int col) {
		if (tableData == null || col < 0) return 0;
		double min = SUtil.getNumVal(getValueAt(0,col));
		double val = 0;
		for (int i=0; i<size(); i++) {
			val = SUtil.getNumVal(getValueAt(i,col));
			min = Math.min(val, min);
		}
		return min;
	}

	/**
	 * Returns index of column name or -1 if not found.
	 */
	public int getColumnIndex(String name) {
		if (columnNames == null) return -1;
		for (int i=0; i<columnNames.length; i++) {
			if (name.equals(columnNames[i])) return i;
		}
		return -1;
	}

	public String[] getColumnNames() {
		resetColumnNames();
		return columnNames;
	}

	public void add(TableData row) {
		tableData.add(row);
	}

	public void reload() {
		if (tableLoader == null) return;
		tableLoader.reload(this);
	}

	public void setTableLoader(TableLoader tablerLoader) {
		this.tableLoader = tablerLoader;
	}
	
	public void setTableData(ArrayList<TableData> tableData) {
		this.tableData = tableData;
	}

	public Method[] getGetMethods() {
		return getMethods;
	}

	public void setGetMethods(Method[] getMethods) {
		this.getMethods = getMethods;
	}

	public Method[] getSetMethods() {
		return setMethods;
	}

	public void setSetMethods(Method[] setMethods) {
		this.setMethods = setMethods;
	}

	public Class<? extends TableData> getTableClass() {
		return tableClass;
	}

	public void setTableClass(Class<? extends TableData> tableClass) {
		this.tableClass = tableClass;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

    public void sort()
    {
        Object[] array = tableData.toArray();
        Arrays.sort(array);
        for (int i=0; i<tableData.size(); i++) {
            tableData.set(i, (TableData) array[i]);
        }

    }

    public void addRowColorChangedListener(RowColorChangedListener l)
    {
        if (tableData == null) return;
        for (TableData td: tableData) {
            td.addRowColorChangedListener(l);
        }
    }

    @Override
    public boolean isColEditable(int modelColumn)
    {
        return tableData.get(0).isColEditable(modelColumn);
    }

    @Override
    public ColumnType getColumnType(int modelColumn)
    {
        return tableData.get(0).getColumnType(modelColumn);
    }

	@Override
	public boolean containsError(int modelRow) {
		if (tableData == null) return false;
		return tableData.get(modelRow).containsError();
	}

	@Override
	public boolean containsWarning(int modelRow) {
		return tableData.get(modelRow).containsWarning();
	}

	@Override
	public String getTableName() {
		return tableData.get(0).getClass().getSimpleName();
	}

}
