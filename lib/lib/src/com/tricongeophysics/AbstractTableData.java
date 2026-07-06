package com.tricongeophysics;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import com.tricongeophysics.TableData.ColumnType;

public abstract class AbstractTableData implements TableData, Serializable, Comparable<TableData> {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected ArrayList<Object> optionalData;
	protected ArrayList<String> columnNames;
    private transient ArrayList<Method> setMethods;
    private transient ArrayList<Method> getMethods;
	
	{
		columnNames = new ArrayList<String>();
		optionalData = new ArrayList<Object>();
		for (String col: getRequiredColumns()) columnNames.add(col);
	}
	

	@Override
	public void addOptionalColumn(String name, Class colClass) {
	    if (indexOfCol(name) >= 0) return; //first, check that column doesn't already exist before adding
		Object defaultObject = null;
		String className = colClass.getSimpleName();
		if (className.equals("Double")) defaultObject = 0.0;
		if (className.equals("String")) defaultObject = " ";
		if (className.equals("Integer")) defaultObject = (int)0;
		if (className.equals("Float")) defaultObject = 0f;
		if (className.equals("Boolean")) defaultObject = false;
		
		optionalData.add(defaultObject);
		columnNames.add(name);
	}

	public abstract String[] getRequiredColumns(); 

	@Override
	public Object getOptionalValue(int i) {
	    int optionalIndex = i - this.getRequiredColumns().length;
		if (optionalIndex < optionalData.size() && optionalIndex >= 0) return optionalData.get(optionalIndex);
		return null;
	}

	@Override
	public void setOptionalValue(int i, Object val) {
	    int optionalIndex = i - this.getRequiredColumns().length;
		if (optionalIndex < optionalData.size()) {
			optionalData.set(optionalIndex, val);
			return;
		}
		while(optionalData.size() < optionalIndex+1) optionalData.add("");
		optionalData.set(optionalIndex, val);
		//SUtil.print("tried to set data past end of optional values...");
	}
	
	public void setValue(String name, Object val)
    {
	    if (columnNames == null || name == null) return;
        int index = indexOfCol(name);
        int nStandardCols = getRequiredColumns().length;
        if (index >= nStandardCols) {
            setOptionalValue(index, val);
            return;
        }
        setStandardValue(name, val);
    }

	/**
	 * finds index of column with name "name"
	 * not case sensitive
	 * @param name
	 * @return index or -1
	 */
	private int indexOfCol(String name)
    {
	    for (int i=0; i<columnNames.size(); i++) {
	        if (name.equalsIgnoreCase(columnNames.get(i))) return i;
	    }
	    return -1;
    }

    /**
	 * Warning! this method can be slow (has to reflectively find set method)
	 * @param name (not case sensitive)
	 * @param val
	 */
	private void setStandardValue(String name, Object val)
	{
	    ArrayList<Method> methods= getSetMethods();
	    for (Method m: methods) {
	        if (m.getName().equalsIgnoreCase("set"+name)) {
	            try {
	                Type[] types = m.getGenericParameterTypes();
	                Type type = types[0];
	                Object val2 = SUtil.ObjectToType(type, val);
	                m.invoke(this, val2);
	                return;
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    SUtil.printErr("Couldn't find set method for: "+name);
	    return;
	}

	/**
	 * if already loaded, won't reload.
	 * if not, calls initializeMethods()
	 * @return
	 */
    private ArrayList<Method> getSetMethods()
    {
        if (setMethods != null) return setMethods;
        initializeMethods();
        return setMethods;
    }

    @Override
	public void delOptionalColumn(int i) {
		int nStandardCols = columnNames.size() - optionalData.size();
		if (i < optionalData.size()) {
			optionalData.remove(i);
			columnNames.remove(nStandardCols+i);
			return;
		}
		SUtil.print("tried to delete data past end of optional values...");
	}
	
	@Override
	public String[] getColumnNames() {
		return columnNames.toArray(new String[]{""});
	}
	
	public String[] getOptionalColumnNames() {
		if (optionalData == null || columnNames == null) return null;
		if (optionalData.size() == 0) return null;
		int nOptionalColumns = optionalData.size();
		int nColumns = columnNames.size();
		return Arrays.copyOfRange(getColumnNames(), nColumns-nOptionalColumns, nColumns);
	}
	
	@Override
	public boolean isColEditable(int col) {
		boolean[] editableCols = getEditableCols();
		if(editableCols == null || editableCols.length <= col) return true;
		return getEditableCols()[col];
	}

	public abstract boolean[] getEditableCols();
	
	/**
	 * gets columnTypes[] from subclass.
	 * If column is greater than columnTypes.length,
	 * or columnTypes == null, returns ColumnType.Standard.
	 * 
	 * Otherwise, returns ColumnType from columnTypes[] array.
	 */
	@Override
	public ColumnType getColumnType(int column) {
	    ColumnType[] types = getColumnTypes();
	    if (types == null || column >= types.length) return ColumnType.Standard;
	    return types[column];
	}

	/**
	 * ColumnTypes are used for coloring the header of a column in the spreadsheet.
	 * If a column's contents effect the return of containsError() it is an Error type column.
	 * If the column effects the return of containsWarning(), it is a Warning type column.
	 * Otherwise, the column is a Standard column.
	 * @return ColumnType - Standard, Warning, or Error
	 */
    public abstract ColumnType[] getColumnTypes();
    
    /**
     * load get/set methods into arrays
     */
    private void initializeMethods() {
        getMethods = new ArrayList<Method>();
        setMethods = new ArrayList<Method>();
        Method[] unsortedMethods = this.getClass().getMethods();

        for (int i=0; i<columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            for (Method m: unsortedMethods) {
                String methodName = m.getName();
                if (methodName.equalsIgnoreCase("get"+columnName)) {
                    getMethods.add(m);
                }
                if (methodName.equalsIgnoreCase("set"+columnName)) {
                    setMethods.add(m);
                }
            }
        }
    }

}
