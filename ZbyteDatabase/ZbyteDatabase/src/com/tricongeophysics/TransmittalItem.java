package com.tricongeophysics;

public class TransmittalItem extends AbstractTableData implements ModelTransferItem {
	
	//public static final String[] RequiredColumns = { "id", "barcode", "item", "media", "media_id", "type", "line_number" };
	public static final String[] RequiredColumns = { "id", "barcode", "box", "media", "media_id", "type", "line_number" };
	Object id;
	Object barcode;
	Object item;
	Object line_number;
	Object state;
	Object county;
	Object comment;
	Object media;
	Object media_id;
	Object type;
	Object box;
	
	@Override
	public boolean containsError() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsWarning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addRowColorChangedListener(RowColorChangedListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(TableData o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setFromModel(AbstractSpreadsheetModel model, int row) {
		id = model.getValueAt(row, "id"); 
		item = model.getTableName();
		comment = model.getValueAt(row, "Comment"); 
	}

	@Override
	public void setModelToItem(DatabaseModel model, int row) {
		model.setValueAt(id, row, "id");
	}

	@Override
	public String[] getRequiredColumns() {
		return RequiredColumns;
	}

	@Override
	public boolean[] getEditableCols() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ColumnType[] getColumnTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Object getBarcode() {
		return barcode;
	}

	public void setBarcode(Object barcode) {
		this.barcode = barcode;
	}

	public Object getItem() {
		return item;
	}

	public void setItem(Object item) {
		this.item = item;
	}

	public Object getLine_number() {
		return line_number;
	}

	public void setLine_number(Object line_number) {
		this.line_number = line_number;
	}

	public Object getState() {
		return state;
	}

	public void setState(Object state) {
		this.state = state;
	}

	public Object getCounty() {
		return county;
	}

	public void setCounty(Object county) {
		this.county = county;
	}

	public Object getComment() {
		return comment;
	}

	public void setComment(Object comment) {
		this.comment = comment;
	}

	public Object getMedia() {
		return media;
	}

	public void setMedia(Object media) {
		this.media = media;
	}

	public Object getMedia_id() {
		return media_id;
	}

	public void setMedia_id(Object media_id) {
		this.media_id = media_id;
	}

	public Object getType() {
		return type;
	}

	public void setType(Object type) {
		this.type = type;
	}
	
	public Object getBox() {
		return box;
	}

	public void setBox(Object box) {
		this.box = box;
	}
}
