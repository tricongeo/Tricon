package com.tricongeophysics;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class MapperProject implements Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    //Mapper mapper;
	private OBFileKey[] obFileKeyList;
	private FileKey[] receiverFileKeyList;
	private FileKey[] shotFileKeyList;
	private ArrayList<TableData> obList;
	private ArrayList<TableData> receiverList;
	private ArrayList<TableData> spList;
    private CdpModel cdpModel;
    private String notes;

	public MapperProject(Mapper mapper) {
		//this.mapper = mapper;
		this.setObList(mapper.obList.getTableData());
		this.setReceiverList(mapper.receiverList.getTableData());
		this.setSpList(mapper.spList.getTableData());
		
		this.setObFileKeyList(mapper.inputFilesPane.getObFileKeys());
		this.setReceiverFileKeyList(mapper.inputFilesPane.getReceiverFileKeys());
		this.setShotFileKeyList(mapper.inputFilesPane.getShotFileKeys());
		
		this.setCdpModel(mapper.cdpBinningPane.getCdpModel());
		
		this.setNotes(mapper.notesPane.getText());
	}
	
	public void setCdpModel(CdpModel cdpModel)
    {
        this.cdpModel = cdpModel;
    }

    public MapperProject(){}

	public static MapperProject projectFromXML(File file) {
		XMLDecoder d = null;
		try {
			d = new XMLDecoder(
					new BufferedInputStream(
							new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MapperProject project = (MapperProject) d.readObject();
		d.close();
		return project;
	}

	public void setObList(ArrayList<TableData> obList) {
		this.obList = obList;
	}

	public ArrayList<TableData> getObList() {
		return obList;
	}

	public void setReceiverList(ArrayList<TableData> receiverList) {
		this.receiverList = receiverList;
	}

	public ArrayList<TableData> getReceiverList() {
		return receiverList;
	}

	public void setObFileKeyList(OBFileKey[] obFileKeyList) {
		this.obFileKeyList = obFileKeyList;
	}

	public OBFileKey[] getObFileKeyList() {
		return obFileKeyList;
	}

	public void setReceiverFileKeyList(FileKey[] receiverFileKeyList) {
		this.receiverFileKeyList = receiverFileKeyList;
	}

	public FileKey[] getReceiverFileKeyList() {
		return receiverFileKeyList;
	}

	public void setShotFileKeyList(FileKey[] shotFileKeyList) {
		this.shotFileKeyList = shotFileKeyList;
	}

	public FileKey[] getShotFileKeyList() {
		return shotFileKeyList;
	}

	public void setSpList(ArrayList<TableData> spList) {
		this.spList = spList;
	}

	public ArrayList<TableData> getSpList() {
		return spList;
	}

	public void restoreProject(Mapper mapper) {
		mapper.receiverList = new ReflectiveTableModel(this.getReceiverList());
		mapper.spList = new ReflectiveTableModel(this.getSpList());
		fixObList(obList);
		mapper.obList = new ReflectiveTableModel(this.getObList());
		mapper.receiverList.setTableLoader(mapper);
		mapper.spList.setTableLoader(mapper);
		mapper.obList.setTableLoader(mapper);
		mapper.inputFilesPane.rebuildPane(this.getReceiverFileKeyList(),
				this.getShotFileKeyList(),
				this.getObFileKeyList());
		mapper.inputFilesPane.resetNumberInputFiles();
		mapper.stationPlotter.receivers = mapper.receiverList;
		mapper.stationPlotter.shots = mapper.spList;
		mapper.stationPlotter.obRecords = mapper.obList;
		mapper.stationPlotter.reset();
		mapper.stationPlotter.repaint();
		mapper.editGeomPane.setReceivers(mapper.receiverList);
		mapper.editGeomPane.setShotPoints(mapper.spList);
		mapper.editGeomPane.setShotRecords(mapper.obList);
		mapper.cdpBinningPane.setCdpModel(this.cdpModel);
		mapper.notesPane.setText(notes);
		mapper.setStatisticsLabel();
	}

	/**
	 * If FFID not found, set to Shot. This fixes old mapper projects
	 * that stored FFID in Shot
	 * @param obList2
	 */
	private void fixObList(ArrayList<TableData> obList2)
    {
	    if (obList2 == null) return;
	    if (obList2.size() < 1) return;
	    TableData first = obList2.get(0);
	    if (first == null) return;
	    String[] names = first.getColumnNames();
	    int ffidIndex = SUtil.indexOf(names, "FFID");
//        int ffidIndex = obList2.getColumnIndex("FFID");
	    
        if (ffidIndex < 0) {
//            obList2.addColumn("FFID", Integer.class);
//            obList2.addColumn("FFID", Double.class);
//            obList2.addColumn("FFID", String.class);
//            obList2.addColumn("FFID", Float.class);
//            int shotIndex = obList2.getColumnIndex("Shot");
            int shotIndex = SUtil.indexOf(names, "Shot");
            if (shotIndex < 0) return;
            for (int i=0; i<obList2.size(); i++) {
                OBRecord obr = (OBRecord) obList2.get(i);
//                obr.setFfid(obr.getShot());
//                obr.setValue("FFID", obr.getShot());
                obr.addOptionalColumn("FFID", Float.class);
                obr.setValue("FFID", obr.getShot());
            }
        }
        
    }

    public static void toXML(File file2, Mapper mapper) {
	    MapperProject project = new MapperProject(mapper);
	    XMLEncoder e = null;
	    try {
	        e = new XMLEncoder(
	                new BufferedOutputStream(
	                        new FileOutputStream(file2)));
	        e.writeObject(project);
	    } catch (Exception e1) {
	        e1.printStackTrace();
	    } finally {
	        e.close();
	    }
	}

	public static void serialize(TriconFile file2, Mapper mapper) {
	    File file = file2.setExtension(Mapper.Mapper3Suffix);
		MapperProject project = new MapperProject(mapper);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(file)));
			oos.writeObject(project);
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static MapperProject unSerialize(File file2) throws InvalidClassException {
		ObjectInputStream ois = null;
		MapperProject project = null;
		try {
			ois = new ObjectInputStream(
					new BufferedInputStream(
							new FileInputStream(file2)));
			project = (MapperProject) ois.readObject();
			ois.close();
		} catch (InvalidClassException e) {
		    throw e;
		} catch (FileNotFoundException e) {
		    JOptionPane.showMessageDialog(null, "Project file: " + file2 + " not found!");
		    return null;
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return project;
	}

    public String getVersion()
    {
        return Mapper.version;
    }

    public CdpModel getCdpModel()
    {
        return cdpModel;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

}
