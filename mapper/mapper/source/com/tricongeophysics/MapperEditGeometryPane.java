package com.tricongeophysics;

import java.awt.Container;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class MapperEditGeometryPane extends JTabbedPane implements TableModelListener, SelectionListener {
	
	//private ArrayList<TableData> receiverList;
//	private ArrayList<Station> shotPointList;
//	private ArrayList<OBRecord> shotRecordList
	
	private SpreadsheetPanel receiverSheet;
	private SpreadsheetPanel shotPointSheet;
	private SpreadsheetPanel shotRecordSheet;
	private ArrayList<TableModelListener> tableModelListeners;
	
	private QCGeomPanel qcGeomPanel;

	MapperEditGeometryPane() {
		this.setName("2. Edit");
		tableModelListeners = new ArrayList<TableModelListener>();
		
		SelectionChangedMonitor.addListener(this);
		
		//receiverSheet = new SpreadsheetPanel(null, new MapperTable());
		receiverSheet = new SpreadsheetPanel(null);
		receiverSheet.setName(StationPlotter.Receivers);
		//shotPointSheet = new SpreadsheetPanel(null, new MapperTable());
		shotPointSheet = new SpreadsheetPanel(null);
		shotPointSheet.setName(StationPlotter.ShotPoints);
		//shotRecordSheet = new SpreadsheetPanel(null, new MapperTable());
		shotRecordSheet = new SpreadsheetPanel(null);
		shotRecordSheet.setName(StationPlotter.ShotRecords);
		
		qcGeomPanel = new QCGeomPanel();
		
		setTabPlacement(JTabbedPane.LEFT);
		add(qcGeomPanel);
		add(shotRecordSheet);
		add(receiverSheet);
		add(shotPointSheet);
	}

	public void setReceivers(ReflectiveTableModel receiverList) {
		//this.receiverList = receiverList2;
		//ReflectiveTableModel model = new ReflectiveTableModel(receiverList2);
		//model.addTableModelListener(this);
		receiverList.addTableModelListener(this);
		receiverSheet.setModel(receiverList);
		qcGeomPanel.setReceivers(receiverList);
	}

	public void setShotPoints(ReflectiveTableModel spList) {
		//this.shotPointList = spList;
		//ReflectiveTableModel model = new ReflectiveTableModel(spList);
		//model.addTableModelListener(this);
		spList.addTableModelListener(this);
		shotPointSheet.setModel(spList);
		qcGeomPanel.setShotPoints(spList);
	}

	public void setShotRecords(ReflectiveTableModel obList) {
		//this.shotRecordList = obList;
		//ReflectiveTableModel model = new ReflectiveTableModel(obList);
		//model.addTableModelListener(this);
		obList.addTableModelListener(this);
		shotRecordSheet.setModel(obList);
		qcGeomPanel.setShotRecords(obList);
	}
	
	public void addTableModelListener(TableModelListener l) {
		tableModelListeners.add(l);
		/*
		receiverSheet.addTableModelListener(l);
		shotPointSheet.addTableModelListener(l);
		shotRecordSheet.addTableModelListener(l);
		*/
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		for (TableModelListener l: tableModelListeners) l.tableChanged(e);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
		int index = e.getIndex();
		Object source = e.getSource();
		JTable table = null;
		if (source instanceof Receiver) {
			table = receiverSheet.table;
		}
		else if (source instanceof SP) {
			table = shotPointSheet.table;
		}
		else if (source instanceof ShotRecord) {
			table = shotRecordSheet.table;
		}
		if (table == null) return;
		if (index >= 0 && index < table.getModel().getRowCount())
		    table.changeSelection(table.convertRowIndexToView(index), 0, false, false);
	}

    public boolean geomChanged()
    {
        if (receiverSheet.getChanged()) return true;
        if (shotPointSheet.getChanged()) return true;
        if (shotRecordSheet.getChanged()) return true;
        return false;
    }

    public void setGeomChanged(boolean b)
    {
        receiverSheet.setChanged(b);
        shotPointSheet.setChanged(b);
        shotRecordSheet.setChanged(b);
    }

    public void resetShots()
    {
        int rows = shotRecordSheet.getRowCount();
        if (rows < 2) {
            System.out.println("Insufficient rows in Shot Record sheet to reset shots");
            return;
        }
        shotRecordSheet.setValueAt(0, "Shot", 1);
        shotRecordSheet.setValueAt(1, "Shot", 2);
        shotRecordSheet.extrapDown("Shot", 0, rows - 1);
    }
}
