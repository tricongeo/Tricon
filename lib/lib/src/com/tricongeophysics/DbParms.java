package com.tricongeophysics;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessControlException;

import javax.swing.JLabel;

public class DbParms implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String XMLFile = "DbParms.xml";
	protected transient static DbParms dbParms;
	public static String test = "";
	protected String url;
	protected String db;
	//protected String dbTable = "jobs";
	protected String dbTable;
	protected String query;
	protected String user;
	protected String pword;
	protected int pkeyIndex;
	protected String adminPWord;
	protected String managerPWord;
	
	
	//private DbParms() {
	public DbParms() {
	}
	
	public static DbParms read(String applicationName) {
		URL xfile = getParmFilename(applicationName);
		if (xfile == null) {
			SUtil.printErr("Failed to find database parameter file.");
			return null;
		}

		//SUtil.print("Reading parameters from file: "+xfile);
		
		DbParms result = null;
		XMLDecoder d = null;
		try {
			d = new XMLDecoder(
					new BufferedInputStream(xfile.openStream()));
			result = (DbParms) d.readObject();
			
			if (test.equals("true")) {
				SUtil.print("test is true: " + test);
				result.url = "jdbc:mysql://192.9.200.48";
			} else {
				SUtil.print("test is false: " + test);
			}
			d.close();
		} catch (Exception e) {
			SUtil.printErr("Failed to read database parameters from file: "+xfile);
			e.printStackTrace();
		}
		return result;
	}

	public static URL getParmFilename(String applicationName) {
		String basedir = null;
		String homedir = null;
		String homepath = null;
		File apacheFile = new File("/var/www");

		try {
			Class<?> klass = Class.forName("com.tricongeophysics."+applicationName);
			//URL xmlurl = klass.getClassLoader().getResource(XMLFile);
			//System.out.println("using class " + klass);
			URL xmlurl = klass.getResource(XMLFile);
			if (xmlurl == null) {
				xmlurl = klass.getClassLoader().getResource(XMLFile);
			}
			if (xmlurl != null) {
				SUtil.print("Class resource url is " + xmlurl);
				return xmlurl;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			basedir = System.getenv("BASE_DIR");
			homedir = System.getenv("HOME");
			homepath = "C:\\" + System.getenv("HOMEPATH"); //for windows machines
		}
		catch (Exception ace) {
			ace.printStackTrace();
		}

		if (basedir != null) {
			basedir += "/java/"+applicationName; //if BASE_DIR not null, this is Linux
			System.out.println("Linux environment: Using BASE_DIR " + basedir);
		}
		else if (homedir != null) {
			basedir = homedir;
			SUtil.print("Windows environment: Using HOME = " + basedir);
		}
		else if (homepath != "C:\\") {
			basedir = homepath;
			SUtil.print("Windows environment: Using HOMEPATH = "+basedir);
		}
		else if(apacheFile.exists()) {
			basedir = apacheFile.getAbsolutePath();
		}

		String file = basedir + File.separator + XMLFile;
		File f = new File(file);
		if (!f.exists()) {
			SUtil.printErr("Database parameter file: " + file + " does not exist!!!!");
			return null;
		}
		
		URL u = null;
		try {
			u = new URL("file://"+file);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return u;
	}

	//private void write() {
	public void write() {
		//java.net.URL xmlurl = DbParms.class.getResource(XMLFile);
		//dbParms.dbParms = null;
		XMLEncoder e;
		try {
			e = new XMLEncoder(
					new BufferedOutputStream(
							//new FileOutputStream(xmlurl.getPath())));
							new FileOutputStream(XMLFile) ) );
			e.writeObject(this);
			e.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

	}

	public static DbParms getParms(String applicationName) {
		if (dbParms == null) {
			dbParms = read(applicationName);
		}
		return dbParms;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getDbTable() {
		return dbTable;
	}

	public void setDbTable(String dbTable) {
		this.dbTable = dbTable;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPword() {
		return pword;
	}

	public void setPword(String pword) {
		this.pword = pword;
	}
	
	public int getPkeyIndex() {
		return pkeyIndex;
	}

	public void setPkeyIndex(int pkeyIndex) {
		this.pkeyIndex = pkeyIndex;
	}

	public String getAdminPWord() {
		return adminPWord;
	}

	public void setAdminPWord(String adminPWord) {
		this.adminPWord = adminPWord;
	}

	public String getManagerPWord() {
		return managerPWord;
	}

	public void setManagerPWord(String managerPWord) {
		this.managerPWord = managerPWord;
	}
}
