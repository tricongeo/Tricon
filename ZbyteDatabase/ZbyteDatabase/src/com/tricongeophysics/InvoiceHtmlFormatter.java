package com.tricongeophysics;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class InvoiceHtmlFormatter {

	private InvoiceEditDbRowPane editDbRowPane;
	private Object date;
	private Object clientName;
	private Object streetAddress;
	private Object city;
	private Object zip;
	private Object state;
	private Object country;
	private Object phone;
	private String contactName;
	private Object invoiceID;
	private double invoiceTotal;
	private Object comment;
	private SpreadsheetPanel sheet;
	private ArrayList<Object> BillCodes = new ArrayList<Object>();
	private ArrayList<Double> billTotals = new ArrayList<Double>();
	private ArrayList<Integer> billQtys = new ArrayList<Integer>();
	private ArrayList<Double> billRates = new ArrayList<Double>();
	private DecimalFormat df;
	private DateFormat dfmt;

	public InvoiceHtmlFormatter(EditDbRowPane editDbRowPane) {
		this.editDbRowPane = new InvoiceEditDbRowPane(editDbRowPane);
		df = new DecimalFormat("#####.00");
		dfmt = new SimpleDateFormat("MMMMMMMMM dd, yyyy");
	}

	public String getHtml() {
		//return editDbRowPane.getHtml();
		
		getFields();
		getTotals();
		String zbytePath = "";
		try {
			zbytePath  = InvoiceHtmlFormatter.class.getResource("images/zbyte_logo.png").toExternalForm();
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
		html += "Z byte Denver, 475 17th Street, Suite 600, Denver, CO  80202" + "\n";
		html += "</td></tr><tr><td>" + "\n";
		html += "Z byte Golden, 700 corporate Circle, #K, Golden, CO 80401" + "\n";
		html += "</td></tr><tr><td>" + "\n";
		html += "Phone: 303.292.9222  " + "\n";
		html += "</td></tr><tr><td>" + "\n";
		html += "FAX: 303.292.4222 " + "\n";
		html += "</td></tr></table>" + "\n";
		html += "<P></P>"+ "\n";
		html += "<table width=100%>" + "\n";
		html += "<tr><td>"+ "\n";
		html += "<B>"+dfmt.format(date)+"</B>"+ "\n";
		html += "</td><td>" + "\n";
		html += "Invoice ID: " + invoiceID + "\n";
		html += "</td></tr>" + "\n";
		html += "</table> \n";
		html += "<FONT SIZE=5>"+ "\n";
		html += "<P></P>"+ "\n";
		html += "<FONT SIZE=5>" + "\n";
		html += "<FONT SIZE=5>" + "\n";
		html += "<B>Bill to:</B><br>"+ "\n";
		html += "<table border=1 width=100%><tr><td>" + "\n";
		html += contactName+"<br>"+ "\n";
		html += clientName+"<br>"+ "\n";
		html += streetAddress+"<br>"+ "\n";
		html += city+", "+state+" "+zip+" "+country+"<br>"+ "\n";
		html += phone + "\n";
		html += "</td></tr></table>" + "\n";
		html += "</FONT>"+ "\n";
		html += "<P></P>"+ "\n";
		html += "<FONT SIZE=5>"+ "\n";
		html += "<B>Bill Summary: (See Attached)</B><br>"+ "\n";
		html += getItemTotalsHtml();
		html += "<P></P>"+ "\n";
		html += "Comment: " + comment + "\n";
		html += "<P></P>"+ "\n";
		html += "For Questions:  Call 303-292-9222 or email us at roger.balogh@zbytedata.com, sandra.cerovski@zbytedata.com<br>"+ "\n";
		html += "<P></P>"+ "\n";
		html += "<b><i>THANK YOU FOR CHOOSING Z BYTE DATA!</b></i><br>";
		html += "<P></P>"+ "\n";
		html += "</body>"+ "\n";
		html += "</html>";
		return html;
	}

	private String getItemTotalsHtml() {
		String html = "";
		html += "<table border=1 width=100%>" + "\n";
		html += "<tr>";
		html += "<th> Bill Code </th>";
		html += "<th> Quantity </th>";
		html += "<th> Rate </th>";
		html += "<th> Sub-Total </th>";
		html += "</tr>\n";
		for (int i=0; i< BillCodes.size(); i++) {
			html += "<tr>\n";
			html += "<td>" + BillCodes.get(i) + "</td>";
			html += "<td>" + billQtys.get(i) + "</td>";
			html += "<td>$" + df.format(billRates.get(i)) + "</td>";
			html += "<td>$" + df.format(billTotals.get(i)) + "</td>\n";
			html += "</tr>\n";
		}
		html += "<tr><td></td><td></td><td><b>TOTAL</b></td><td>$" + df.format(invoiceTotal) + "</td></tr>";
		html += "</table>";
		return html;
	}

	private void getTotals() {
		int itemCount = sheet.getRowCount();
		BillCodes.clear();
		billTotals.clear();
		for (int i=0; i< itemCount; i++) {
			Object bc = sheet.getValueAt(i, "Bill Code:");
			Object total = sheet.getValueAt(i, "Total");
			Object qty = sheet.getValueAt(i, "Bill Qty");
			if (bc == null || total == null || qty == null) continue;
			double tt = SUtil.sval(total.toString());
			int qq =  (int) SUtil.sval(qty.toString());
			double rate = tt/qq;
			
			int index = BillCodes.indexOf(bc);
			if (index < 0) {
				BillCodes.add(bc);
				billTotals.add(tt);
				billQtys.add(qq);
				billRates.add(rate);
			}
			else {
				double t = billTotals.get(index);
				billTotals.set(index, t + tt);
				Integer q = billQtys.get(index);
				billQtys.set(index, q + qq);
			}
			invoiceTotal += tt;
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
		invoiceID = editDbRowPane.getInvoiceID();
		date =  editDbRowPane.getInvoiceDate();
		comment = editDbRowPane.getComment();
		
		if (date == null) date = SUtil.getTodaysDate();
	}

	public void setSheet(SpreadsheetPanel sheet) {
		this.sheet = sheet;
	}
}
