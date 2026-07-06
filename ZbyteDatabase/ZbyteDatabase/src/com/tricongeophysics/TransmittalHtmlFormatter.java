package com.tricongeophysics;

import java.awt.Container;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class TransmittalHtmlFormatter {

	private TransmittalEditDbRowPane editDbRowPane;
	private Object date;
	private Object clientName;
	private Object streetAddress;
	private Object city;
	private Object zip;
	private Object state;
	private Object country;
	private Object phone;
	private String contactName;
	private Object transmittalID;
	private int itemCount;
	private Object transmittedBy;
	private Object shippedVia;
	private Object comment;
	private SpreadsheetPanel sheet;
	private ArrayList<Object> mediaTypes = new ArrayList<Object>();
	private ArrayList<Integer> mediaTotals = new ArrayList<Integer>();

	public TransmittalHtmlFormatter(EditDbRowPane editDbRowPane) {
		this.editDbRowPane = new TransmittalEditDbRowPane(editDbRowPane);
	}

	public String getHtml() {
		//return editDbRowPane.getHtml();
		getFields();
		getTotals();
		 String zbytePath = "";
	        try {
	        	zbytePath  = TransmittalHtmlFormatter.class.getResource("images/zbyte_logo.png").toExternalForm();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		
		String html = "<html>";
		html += "<HEAD></HEAD>" + "\n";
		html += "<BODY LANG=\"en-US\" DIR=\"LTR\">" + "\n";
		html += "<FONT SIZE=4>" + "\n";
		html += "<table  width=100%> " + "\n";
		html += "<tr><td><FONT FACE=\"Times New Roman\"><FONT SIZE=6><B> " + "\n";
		html += "Z BYTE DATA SERVICES INC  </B></FONT></FONT> " + "\n";
		html += "</td> <td rowspan=5>" + "\n";
		html += "<IMG SRC=\""+zbytePath+"\" ALIGN=BOTTOM WIDTH=175 HEIGHT=120 BORDER=0>" + "\n" ;
		html += "</td></tr><tr><td>" + "\n";
		html += "475 17th Street, Suite 460 Denver, CO 80202 " + "\n";
		html += "</td></tr><tr><td>" + "\n";
		html += "Phone: 303.292.9222  " + "\n";
		html += "</td></tr><tr><td>" + "\n";
		html += "FAX: 303.292.4222 " + "\n";
		html += "</td></tr></table>" + "\n";
		html += "<P></P>"+ "\n";
		html += "<table width=100%>" + "\n";
		html += "<tr><td rowspan=3>"+ "\n";
		html += "<FONT SIZE=5>"+ "\n";
		html += "<B>Date: "+date+"</B>"+ "\n";
		html += "</td><td>" + "\n";
		html += "<FONT SIZE=5>"+ "\n";
		html += "Transmittal ID: " + transmittalID + "\n";
		html += "</td></tr><tr><td>" + "\n";
		html += "<FONT SIZE=5>"+ "\n";
		html += "Transmitted By: " + transmittedBy + "\n";
		html += "</td></tr><tr><td>" + "\n";
		html += "<FONT SIZE=5>"+ "\n";
		html += "Shipped Via: " + shippedVia + "\n";
		html += "</td></tr></table>" + "\n";
		html += "<P></P>"+ "\n";
		html += "<FONT SIZE=5>" + "\n";
		html += "<FONT SIZE=5>" + "\n";
		html += "<B>Ship to:</B><br>"+ "\n";
		html += "<table border=1 width=100%><tr><td>" + "\n";
		html += "Attention: "+contactName+"<br>"+ "\n";
		html += clientName+"<br>"+ "\n";
		html += streetAddress+"<br>"+ "\n";
		html += city+", "+state+" "+zip+" "+country+"<br>"+ "\n";
		html += phone + "\n";
		html += "</td></tr></table>" + "\n";
		html += "</FONT>"+ "\n";
		html += "<P></P>"+ "\n";
		html += "<FONT SIZE=5>"+ "\n";
		html += "<P><B>TOTAL OF "+itemCount+" ITEMS </FONT>(See Attached):</B></P>"+ "\n";
		html += getItemTotalsHtml();
		html += "<P></P>"+ "\n";
		html += "Comment: " + comment + "\n";
		html += "<P></P>"+ "\n";
		html += "<FONT SIZE=5>"+ "\n";
		html += "<P><B>Please Sign and Return One Copy</B></P>"+ "\n";
		html += "<P></P>"+ "\n";
		html += "<P>Received By: _____________________________________________ Date: ___________________________ </P>"+ "\n";
		html += "<P><BR></P>"+ "\n";
		html += ""+ "\n";
		html += "</body>"+ "\n";
		html += "</html>";
		return html;
	}

	private String getItemTotalsHtml() {
		String html = "";
		html += "<table border=1 width=100%>" + "\n";
		for (int i=0; i< mediaTypes.size(); i++) {
			html += "<tr><td>" + mediaTypes.get(i) + "(s)</td><td>" + mediaTotals.get(i) + "</td></tr>\n";
		}
		html += "</table>";
		return html;
	}

	private void getTotals() {
		itemCount = sheet.getRowCount();
		mediaTypes.clear();
		mediaTotals.clear();
		for (int i=0; i< itemCount; i++) {
			Object media = sheet.getValueAt(i, "media");
			int index = mediaTypes.indexOf(media);
			if (index < 0) {
				mediaTypes.add(media);
				mediaTotals.add(1);
			}
			else {
				int total = mediaTotals.get(index);
				mediaTotals.set(index, total + 1);
			}
		}
	}

	private void getFields() {
		contactName = editDbRowPane.getContactName();
		clientName = editDbRowPane.getClientName();
		streetAddress = editDbRowPane.getStreetAddress();
		city = editDbRowPane.getCity();
		state = editDbRowPane.getState();
		zip = editDbRowPane.getZip();
		country = editDbRowPane.getCountry();
		phone = editDbRowPane.getPhone();
		transmittalID = editDbRowPane.getTransmittalID();
		transmittedBy = editDbRowPane.getTransmittedBy();
		date =  editDbRowPane.getShipDate();
		shippedVia = editDbRowPane.getShipMethod();
		comment = editDbRowPane.getComment();
	}

	public void setSheet(SpreadsheetPanel sheet) {
		this.sheet = sheet;
	}
}
