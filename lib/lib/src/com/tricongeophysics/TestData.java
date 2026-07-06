package com.tricongeophysics;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import javax.swing.JFrame;

import com.tricongeophysics.TableData.ColumnType;

public class TestData extends AbstractTableData {

	private static final ColumnType[] ColumnTypes = {ColumnType.Standard, ColumnType.Warning, ColumnType.Standard, ColumnType.Error};
    int i;
	float f;
	double d;
	boolean b;
	String yoyomamabigcolumnname="";
    private boolean oldb;
    private ArrayList<RowColorChangedListener> rowColorChangedListeners = new ArrayList<RowColorChangedListener>();
	public String getYoyomamabigcolumnname() {
		return yoyomamabigcolumnname;
	}
	
	public static String[] RequiredColumns = {"i", "f", "d", "b", "yoyomamabigcolumnname"};
	private static boolean[] editableCols = {false};

	public void setYoyomamabigcolumnname(String yoyomamabigcolumnname) {
		this.yoyomamabigcolumnname = yoyomamabigcolumnname;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public float getF() {
		return f;
	}

	public void setF(float f) {
		this.f = f;
	}

	public boolean getB() {
		return b;
	}

	public void setB(boolean b) {
		this.b = b;
		fireRowColorChanged();
	}
	
	private void fireRowColorChanged()
    {
        for (RowColorChangedListener l: rowColorChangedListeners) {
            l.rowColorChanged();
        }
    }

    public static void main(String[] args) {
		ArrayList<TableData> array = new ArrayList<TableData>();
		
		int itemCount = 4;
		
		for(int i=0; i<itemCount; i++) {
			TestData td = new TestData();
			td.i = i;
			td.f = i;
			td.d = Math.random()*100000000;
			td.b = (i%2 == 0) ? true : false;
			array.add(td);
		}
		
		ReflectiveTableModel rtm = new ReflectiveTableModel(array);
		//Object val = rtm.getValueAt(0, 2);
		//SUtil.print("found val: "+val);=
		
		JFrame f= new JFrame("Spreadsheet");
		SpreadsheetPanel sheet = new SpreadsheetPanel(rtm);
		f.setContentPane(sheet);
		f.setSize(800,800);
		f.addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.setVisible(true);
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	@Override
	public boolean[] getEditableCols() {
		return this.editableCols;
	}

	@Override
	public String[] getRequiredColumns() {
		return this.RequiredColumns;
	}

    @Override
    public int compareTo(TableData o)
    {
        if (o == null) return 0;
        return o.toString().compareTo(this.toString());
    }

    @Override
    public boolean containsError()
    {
        return this.b;
    }

    @Override
    public boolean containsWarning()
    {
        return (this.f < 1);
    }

    @Override
    public ColumnType[] getColumnTypes()
    {
        return ColumnTypes;
    }
    /*
    @Override
    public boolean rowChanged() {
        boolean changed = (b == oldb);
        oldb = b;
        return changed;
    }
    */

    @Override
    public void addRowColorChangedListener(RowColorChangedListener l)
    {
        rowColorChangedListeners.add(l);
    }
    
}
