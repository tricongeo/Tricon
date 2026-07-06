package com.tricongeophysics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class FocusProject
{
    private String name;
    private File projectPath = new File("/not set");
    private File geodepthPath = new File("/not set");
    private String geodepthName;
    private String focusPath;
    private File pgroot = new File("");
    private FocusSeismicLine[] seismicLines = new FocusSeismicLine[0];
    private FocusSeismicLine selectedLine;

    public enum Status { OK, Corrupt, Missing, Poor };
    private Status focusStatus;
    private Status geodepthStatus;

    public enum Version
    {
        pg20, epos3SE, epos3TE
    }

    ;
    private Version focusVersion;

    private File lineNamesFile;  //file containing list of seismic lines for project
    final static String LINENAMESLIST = "LineNames.list";
    private File geodepthLinkFile = new File("/not set");
    final static String GEODEPTHLINK = "GeoDepth.link";
    final static String HDS = "hds";
    final static String HAR = ".har";
    private FocusDataPath[] dataPaths;
    private File dataPathsFile;
    final static String DATAPATHLIST = "DataPath.list";
    final static String DATAPATHSTRING = "DaNfsOriginal";
    final static String DATAPATHALIASSTRING = "DaNalias";
    final static String FILETABLE = "FileTable.tbl";
    private File fileTableFile;

    private ArrayList<String> errorMessages = new ArrayList<String>();


    //create new project - if doesn't exist, status will be corrupt
    public FocusProject(String n, File dir)
    {
        pgroot = dir;
        name = n;
        focusStatus = Status.OK;
        geodepthStatus = Status.OK;
        try
        {
            if (!setProjectPath())
            {
                errorMessages.add("Directory \"" + projectPath + "\" could not be found");
                System.err.println("Directory \"" + projectPath + "\" could not be found");
                focusStatus = Status.Missing;
                geodepthStatus = Status.Corrupt;
                return;
            }
            loadProject();
        }
        catch (Exception e)
        {
            errorMessages.add(e.toString() + "\n" + e.getLocalizedMessage() + "\n" + e.getMessage() + "\n" + e.getCause());
        }
    }

    public FocusProject(String project, String pgRoot2) {
		this(project, new File(pgRoot2));
	}

	public String getName()
    {
        return name;
    }

    //method that sets project name and all other parts using that name.
    public void setName(String n)
    {
        name = n;
        loadProject();
    }

    public boolean loadProject()
    {
        if (!findGeodepthPath())
        {
            geodepthError("Unable to find GeoDepth portion of project \"" + name + "\"", Status.Corrupt);
            return false;
        }
        loadDataPaths();
        loadSeismicLines();
        return true;
    }

    public boolean loadDataPaths()
    {
        if (!setDataPathsFile())
        {
            geodepthError("Unable to find datapaths file \"" + dataPathsFile.getAbsolutePath() + "\"", Status.Corrupt);
            return false;
        }
        if (!processDataPathsFile())
        {
            geodepthError("Failed to load Data Paths for project \"" + name + "\"", Status.Corrupt);
            return false;
        }
        return true;
    }

    public boolean setDataPathsFile()
    {
        dataPathsFile = new File(geodepthPath + File.separator + DATAPATHLIST);
        return dataPathsFile.isFile();
    }

    public boolean processDataPathsFile()
    {
        String[] text;
        String[] fields;
        ArrayList<String> paths = new ArrayList<String>();
        ArrayList<String> aliases = new ArrayList<String>();
        //...First, read file into string array
        text = this.readFile(dataPathsFile);

        //...Next, step through lines of text, search for datapaths and aliases
        if (text.length > 0)
        {
            for (int i = 0; i < text.length; i++)
            {  //start after file header
                fields = text[i].trim().split(" ");
                if (text[i].contains(DATAPATHSTRING))
                {
                    paths.add(fields[4].replace('`', ' ').trim());
                }
                if (text[i].contains(DATAPATHALIASSTRING))
                {
                    aliases.add(fields[4].replace('`', ' ').trim());
                }
            }
        }
        else
        {
            geodepthError("Empty or missing Data Paths File - filename \"" + dataPathsFile.getAbsolutePath() + "\"", Status.Corrupt);
            return false;
        }
        //...Load aliases and datapaths into FocusDataPath objects
        if (!setDataPaths(paths.toArray(new String[0]), aliases.toArray(new String[0])))
        {
            geodepthError("Bad Data Paths found - filename\"" + dataPathsFile.getAbsolutePath() + "\"", Status.Corrupt);
            return false;
        }
        return true;
    }

    public boolean setDataPaths(String[] paths, String[] aliases)
    {
        int badpaths = 0;
        dataPaths = new FocusDataPath[paths.length];
        for (int i = 0; i < paths.length; i++)
        {
            dataPaths[i] = new FocusDataPath(new File(paths[i]), aliases[i]);
            if (!dataPaths[i].exists())
            {
                badpaths++;
                geodepthError("Couldn't find datapath \"" + dataPaths[i].toString() + "\"", Status.Poor);
            }
        }
        if (badpaths > 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean loadSeismicLines()
    {
        if (!setLineNamesFile())
        {
            geodepthError("Couldn't find file \"" + lineNamesFile + "\"", Status.Corrupt);
            return false;
        }
        if (!processLineNamesFile())
        {
            geodepthError("Failed to load seismic lines for project \"" + name + "\"", Status.Corrupt);
            return false;
        }
        return true;
    }

    public boolean processLineNamesFile()
    {
        String[] text;
        String[] fields;
        String userName;
        String focusName;
        String Separator = "|";
        //...First, read file into string array
        text = this.readFile(lineNamesFile);

        //...Next, step through lines of text, each one is a seismic line
        if (text.length > 0)
        {
            seismicLines = new FocusSeismicLine[text.length - 1];
            for (int i = 1; i < text.length; i++)
            {  //start after file header
                if (text[i].contains(Separator))
                {
                    fields = text[i].split("\\" + Separator); //have to escape "|" for this to work
                    focusName = fields[0];
                    userName = fields[1];
                    seismicLines[i - 1] = new FocusSeismicLine(userName, focusName);
                }
                else
                {
                    geodepthError("Invalid Line Names File format - filename \"" + lineNamesFile.getAbsolutePath() + "\"", Status.Corrupt);
                    geodepthError("text \"" + text[i] + "\" does not contain \"" + Separator + "\"", Status.Corrupt);
                    return false;
                }
            }
        }
        else
        {
            geodepthError("Empty or missing Line Names File - filename \"" + lineNamesFile.getAbsolutePath() + "\"", Status.Corrupt);
            return false;
        }

        Arrays.sort(seismicLines);
        return true;
    }

    public void geodepthError(String message, Status status)
    {
        errorMessages.add(message);
        geodepthStatus = status;
    }


    public File getProjectPath()
    {
        return projectPath;
    }

    public boolean setProjectPath()
    {
        projectPath = new File(pgroot + File.separator + name);
        return projectPath.isDirectory();
    }

    public boolean setGeodepthLinkFile()
    {
        geodepthLinkFile = new File(pgroot + File.separator + name + File.separator + GEODEPTHLINK);
        return geodepthLinkFile.isFile();
    }

    public boolean findGeodepthPath()
    {
        if (!setGeodepthLinkFile())
        {
            geodepthError("Couldn't find file " + geodepthLinkFile, Status.Corrupt);
            return false;
        }
        if (!processGeodepthLinkFile())
        {
            geodepthError("Processing of geodepth link file \"" + geodepthLinkFile + "\"failed", Status.Corrupt);
            return false;
        }
        return true;
    }

    public boolean processGeodepthLinkFile()
    {
        String[] text;
        String path = "";
        //...First, read link file
        text = readFile(geodepthLinkFile);

        //...Now, interpret file based on length and contents
        if (text.length == 0)
        {
            geodepthError("Empty or missing GeoDepth Link file - \"" + geodepthLinkFile.getAbsolutePath() + "\"", Status.Corrupt);
            return false;
        }
        else if (text.length == 1)
        {
            geodepthName = text[0];
            focusVersion = Version.pg20;
            path = pgroot + File.separator + HDS + File.separator + geodepthName + HAR;
        }
        else if (text.length >= 3)
        {
            geodepthName = text[0];
            String parentDir = text[2];
            String pnsServer = text[1].split("@")[1];
            path = parentDir + File.separator + geodepthName;
            if (text[3].split("@")[0].equals(pnsServer))
            {
                focusVersion = Version.epos3SE;
            }
            else
            {
                focusVersion = Version.epos3TE;
            }
        }
        else
        {
            geodepthError("Unrecognized GeoDepth Link File format:" + geodepthLinkFile.getAbsolutePath(), Status.Corrupt);
            return false;
        }

        //...After getting geodepth path, try it to see if it works
        if (!setGeodepthPath(path))
        {
            geodepthError("Couldn't find GeoDepth directory \"" + path + "\"", Status.Missing);
            return false;
        }
        return true;
    }

    public boolean setGeodepthPath(String path)
    {
        geodepthPath = new File(path);
        return geodepthPath.isDirectory();
    }

    public String getFocusPath()
    {
        return focusPath;
    }

    public FocusSeismicLine[] getSeismicLines()
    {
        return seismicLines;
    }

    public String[] readFile(File file)
    {
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader reader;
        try
        {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null)
            {
                lines.add(line);
            }
        }
        catch (Exception e)
        {
            System.err.println("error reading file " + file + "\n" + e.toString());
        }
        return lines.toArray(new String[0]);
    }

    public boolean setLineNamesFile()
    {
        lineNamesFile = new File(geodepthPath + File.separator + LINENAMESLIST);
        return lineNamesFile.isFile();
    }

    public String[] getSeismicLineNames()
    {
        String[] names = new String[seismicLines.length];
        for (int i = 0; i < seismicLines.length; i++)
        {
        	if (seismicLines[i] == null) continue;
            names[i] = seismicLines[i].getUserLineName();
        }
        return names;
    }


    public Status getFocusStatus()
    {
        return focusStatus;
    }

    public boolean isCorrupt()
    {
        if (focusStatus.equals(Status.Corrupt) || focusStatus.equals(Status.Missing))
        {
            return true;
        }
        if (geodepthStatus.equals(Status.Corrupt) || geodepthStatus.equals(Status.Missing))
        {
            return true;
        }
        return false;
    }

    public Status getGeodepthStatus()
    {
        return geodepthStatus;
    }

    public Version getFocusVersion()
    {
        return focusVersion;
    }

    public void setFocusVersion(Version focusVersion)
    {
        this.focusVersion = focusVersion;
    }

    public String getErrorMessages()
    {
        String allMessages = "";
        for (int i = 0; i < errorMessages.size(); i++)
        {
            allMessages = allMessages + errorMessages.get(i) + "\n";
        }
        return allMessages;
    }

    //method to retrieve seismic line by line name (user or focus name)
    // return null if name not found
    public FocusSeismicLine getLine(String lineName)
    {
        for (int i = 0; i < seismicLines.length; i++)
        {
            if (seismicLines[i].getFocusLineName().equals(lineName))
            {
                return seismicLines[i];
            }
            if (seismicLines[i].getUserLineName().equals(lineName))
            {
                return seismicLines[i];
            }
        }
        return null;
    }

    public String getDescription()
    {
        String string;
        string = "ProjectName.: " + name + " (" + focusVersion + ")" + "\n" +
            "focusPath...: " + projectPath + "\n" +
            "geodepthPath: " + geodepthPath.getAbsolutePath() + "\n" +
            "geodepth....: " + geodepthStatus + "\n" +
            "focus.......: " + focusStatus + "\n" +
            "\n" +
            "selected dataset:" + "\n";
        if (selectedLine != null)
        {
            if (selectedLine.getSelectedDataSet() != null)
            {
                string = string + selectedLine.getSelectedDataSet().getDescription();
            }
        }
        string = string + "\nerrors:\n" + getErrorMessages();
        return string;
    }

    public String getLongDescription()
    {
        String string;
        string = "ProjectName.: " + name + "\n" +
            "focusPath...: " + projectPath + "\n" +
            "geodepthPath: " + geodepthPath.getAbsolutePath() + "\n" +
            "FocusVersion: " + focusVersion + "\n" +
            "geodepth....: " + geodepthStatus + "\n" +
            "focus.......: " + focusStatus + "\n" +
            "lines:\n";
        for (int i = 0; i < seismicLines.length; i++)
        {
            if (seismicLines[i] != null)
            {
                string = string + seismicLines[i].toString() + "\n";
            }
        }
        string = string + "Data Paths:\n";
        for (int i = 0; i < dataPaths.length; i++)
        {
            if (dataPaths[i] != null)
            {
                string = string + dataPaths[i].toString() + "\n";
            }
        }

        if (selectedLine != null)
        {
            string = string + "selected line:\n";
            string = string + selectedLine.getDescription();
        }

        string = string + "errors:\n" + getErrorMessages();
        return string;
    }

    public String toString()
    {
        return name;
    }

    public FocusSeismicLine getSelectedLine()
    {
        return selectedLine;
    }

    public void setSelectedLine(String lineName)
    {
        this.selectedLine = getLine(lineName);
    }

    public static void main(String[] args)
    {
        
        FocusProject fp = new FocusProject("scott3d", new File("/seisdata"));
        fp.getSeismicLines()[0].loadDataSets();
        System.out.println(fp.getDescription());
        System.out.println(fp.getSeismicLines()[0].getDescription());

    }

    //Inner class FocusSeismicLine has to know things about it's parent FocusProject in order to work
    // makes more since to inner-class it than to pass it a FocusProject object.
    public class FocusSeismicLine implements Comparable<FocusSeismicLine>
    {

        private String userLineName;
        private String focusLineName;
        private DataSet[] dataSets;
        private DataSet selectedDataSet;

        public DataSet getSelectedDataSet()
        {
            return selectedDataSet;
        }

        /** set selected dataset using String name - returns false if name doesn't match */
        public boolean setSelectedDataSet(String name)
        {
            DataSet ds = getDataSet(name);
            if (ds == null)
            {
                return false;
            }
            selectedDataSet = ds;
            return true;
        }

        //method to retrieve dataset by user name
        // return null if name not found
        public DataSet getDataSet(String fileName)
        {
            for (int i = 0; i < dataSets.length; i++)
            {
                if (dataSets[i].getName().equals(fileName))
                {
                    return dataSets[i];
                }
            }
            return null;
        }

        public DataSet[] getDataSets()
        {
            return dataSets;
        }

        public FocusSeismicLine(String userName, String focusName)
        {
            userLineName = userName;
            focusLineName = focusName;
            loadDataSets();
        }

        public String getUserLineName()
        {
            return userLineName;
        }

        public String getFocusLineName()
        {
            return focusLineName;
        }

        public String toString()
        {
            return userLineName + " = " + focusLineName;
        }


        public boolean loadDataSets()
        {
            if (!setFileTableFile())
            {
                geodepthError("File Table not found - filename\"" + fileTableFile.getAbsolutePath() + "\"", Status.Poor);
                return false;
            }
            if (!processFileTableFile())
            {
                geodepthError("Errors encountered reading File Table file", Status.Corrupt);
            }
            return true;
        }

        public boolean setFileTableFile()
        {
            fileTableFile = new File(geodepthPath + File.separator + "Line_"
                + focusLineName + File.separator + FILETABLE);
            return fileTableFile.isFile();
        }

        public boolean processFileTableFile()
        {
            String[] text;
            final String Separator = "|";
            String[] fields;
            String path;
            String dsName;
            String convertedPath;
            String rootName;
            ArrayList<DataSet> dataSetArray = new ArrayList<DataSet>();
            //...First, read file
            text = readFile(fileTableFile);

            //...Next, step through lines of text, each one is a seismic line
            if (text.length > 0)
            {
                for (int i = 0; i < text.length - 1; i++)
                {  //start after file header
                    if (text[i + 1].contains(Separator))
                    {
                        fields = text[i + 1].split("\\" + Separator); //have to escape "|" for this to work
                        dsName = fields[6];
                        path = fields[7];
                        convertedPath = convertFilePath(path);
                        rootName = fields[0];
                        if (!rootName.equals("0000000000000000"))
                        { //ignored deleted files that are still in filetable
                            if (convertedPath != null)
                            {
                                dataSetArray.add(new DataSet(name, userLineName, dsName, convertedPath, pgroot.getAbsolutePath(), geodepthPath.getParent()));
                            }
                            else
                            {
                                geodepthError("Failed to interpret PDS file path - \"" + path + "\"", Status.Poor);
                            }
                            if (!dataSetArray.get(dataSetArray.size() - 1).pdsFileOK())
                            {
                                geodepthError("Can't find PDS file \"" + dataSetArray.get(dataSetArray.size() - 1).getPdsFile() + "\"", Status.Poor);
                            }
                        }
                    }
                }
            }
            else
            {
                geodepthError("Empty or missing Line Names File - filename \"" + lineNamesFile.getAbsolutePath() + "\"", Status.Corrupt);
                return false;
            }
            dataSets = dataSetArray.toArray(new DataSet[0]);
            Arrays.sort(dataSets);
            return true;
        }

        //takes string from filetable as input
        //if datapath alias is found, replaces with actual datapath
        public String convertFilePath(String dp)
        {
            for (int i = 0; i < dataPaths.length; i++)
            {
                if (dp.contains(File.separator) && dp.split(File.separator)[0].equals(dataPaths[i].getPathAlias()))
                {
                    return dp.replace(dataPaths[i].getPathAlias(), dataPaths[i].name());
                }
            }
            return null;
        }

        public String getDescription()
        {
            String string;
            string = "user name: " + userLineName + "\n" +
                "focus name: " + focusLineName + "\n" +
                "total datasets: " + dataSets.length + "\n" +
                "Selected dataset:\n";
            if (selectedDataSet != null)
            {
                //dataSets[0].initializeDataSet();
                string = string + selectedDataSet.getDescription();
            }
            return string;
        }

        public int compareTo(FocusSeismicLine otherLine)
        {
            return this.getUserLineName().toUpperCase().compareTo(otherLine.getUserLineName().toUpperCase());
        }
    }

	public static String[] getProjectList(String pgRoot2) {
		if (pgRoot2 == null) return null;
		File dir = new File(pgRoot2);
		String[] list = dir.list();
		return list;
	}
}
