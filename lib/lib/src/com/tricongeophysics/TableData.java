package com.tricongeophysics;

public interface TableData {
    public enum ColumnType { Standard, Warning, Error };

	String[] getColumnNames();

	void addOptionalColumn(String string, Class colClass);

	/**
	 * Index i is index of column within total column list (not just optional columns)
	 * @param i
	 * @return value
	 */
	Object getOptionalValue(int i);

	/**
     * Index i is index of column within total column list (not just optional columns)
     * @param i
     */
	void setOptionalValue(int i, Object val);

	void delOptionalColumn(int i);

	boolean isColEditable(int col);

    boolean containsError();

    boolean containsWarning();
    
    ColumnType getColumnType(int column);

    void addRowColorChangedListener(RowColorChangedListener l);

   // boolean rowChanged();
}
