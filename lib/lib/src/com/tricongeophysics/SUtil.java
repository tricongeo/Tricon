package com.tricongeophysics;

import java.io.*;
import java.lang.reflect.Type;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Class with a bunch of useful utilities in it
 * 
 * @author Scott Cook
 * Property of Tricon Geophysics
 *
 */
public class SUtil {
    
    private static final int SECOND = 1000; //second in milliseconds
    private static final int MINUTE = 60*SECOND;
    private static final int HOUR = 60*MINUTE;
    private static final int DAY = 24*HOUR;
    
    //	sval RETURNS NEXT INTEGER AFTER POINTER IN STRING
    static double sval(String str,int start)
    {
	int len=str.length(), x=0, y=0;
	char chars[];
	double ans=0;
	
	chars = new char[len+1]; // chars[len+1];
	
	if (start<0) start = 0;
	if (start>len) start = len;

	//send chopped string to overloaded sval(String)
	ans = sval(str.substring(start));
	return ans;
    }
    
    
    //	sval RETURNS NEXT INTEGER AFTER START AND BEFORE STOP IN STRING
    static double sval(StringBuilder str,int start, int stop)
    {
	int len = str.length();
	double ans=0;
	
	if (start<0) start = 0;
	if (stop>len) stop = len;
	if (start>stop) start = stop;
	//send chopped string to overloaded sval(String)
	ans = sval(str.substring(start,stop));
	
	return ans;
    }
    
    //	cval RETURNS NEXT INTEGER AFTER POINTER IN CHAR ARRAY
    static long cval(char string[])
    {
	int j=0,i=0,len=string.length,neg=1;
	char stack[],stack2[];
	long ans=0;
	stack = new char[len+1]; // stack[len+1];
	
	
	for(j=0;j<len && !Character.isDigit(string[j]);j++)  //skip non-digits at beginning
	    if (string[j]=='-') neg = -1;           //determine if negative number
	for(j=j;j<len && Character.isDigit(string[j]);j++)     //stop at end of 1st integer 
	    stack[i++]=string[j];     
	stack2 = new char[i];
	for(int k=0;k<i;k++) stack2[k] = stack[k];
	if (i>0) {
	    ans = Integer.parseInt(String.valueOf(stack2))*neg;
	}
	
	stack=null;stack2=null;
	return ans;
    }
    
    
    
    /**
     * 	sval RETURNS NEXT FLOAT AFTER START AND BEFORE STOP IN STRING<br>
     * Ignores text.
     * @param str
     * @param start
     * @param stop
     * @return
     */
    static double sval(String str,int start, int stop)
    {
	int len = str.length();
	double ans=0;
	
	stop=stop+1;
    //	+1 added because start=1,stop=1
	//                                         should return 1 char, not 0 chars
	if (start<0) start = 0;
	if (stop>len) stop = len;
	if (start>stop) start = stop;
	
	//chop string down to start,stop, sent to overloaded sval(String)
	ans = sval(str.substring(start,stop)); 
	
	return ans;
    }
    
    //	sval RETURNS NEXT FLOAT AFTER BEGINNING OF STRING
    static double sval(String str)
    {
	int j=0, //index of last digit
	    i=0, //number of characters that are digits
	    len=str.length(), //length of string
	    neg=1; //positive or negative number (-1 = negative)
	double ans=0;
	
	//skips nondigits at beginning(take neg sign), i counts digits
	int nPoints = 0;
	for (j=0;j<len && !(Character.isDigit(str.charAt(j)) || str.charAt(j)=='.');j++)
	if (str.charAt(j)=='-') neg = -1;        
	for (; j<len && (Character.isDigit(str.charAt(j)) || str.charAt(j)=='.'); j++) {
	    if (str.charAt(j)=='.') {
            nPoints++;
            if (nPoints > 1) break;
        }
	    i++;
	}
	if (i>0) {
	    ans = Double.parseDouble(str.substring(j-i,j))*neg;
	}
	
	return ans;
    }
    
    // distance calculates the distance between two points
    static double distance(double x1, double y1, double x2, double y2) {
    	double dx = x2-x1;
    	double dy = y2-y1;
    	return Math.sqrt(dx*dx+dy*dy);
    }
    static double distance(float x1, float y1, float x2, float y2) {
    	return distance(x1,y1,x2,y2) ;
    }
    static double distance(int x1, int y1, int x2, int y2) {
    	return distance(x1,y1,x2,y2) ;
    }
    
    /**
     *  calculate azimuth from point one to point two (counter-clockwise from positive x axis) in degrees
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    static double azimuth(double x1, double y1,double x2, double y2) { 
    	double dx = x2-x1;
    	double dy = y2-y1;
    	if ((dx == 0.0) && (dy == 0.0)) return 0;
    	else if ((dx > 0) && (dy >= 0))
    		return Math.toDegrees(Math.atan(dy/dx));
    	else if ((dx <= 0) && (dy >= 0)) {
    		if (dx == 0.0) return 90;
    		return 180+Math.toDegrees(Math.atan(dy/dx));
    	}
    	else if ((dx <= 0) && (dy <= 0)) {
    		if (dx == 0.0) return 270;
    		return Math.toDegrees(Math.atan(dy/dx))+180;
    	}
    	else if ((dx > 0) && (dy <= 0)) {
    		return 360+Math.toDegrees(Math.atan(dy/dx));
    	}
    	return 0;
    }    
    
    static double azimuth(float x1, float y1, float x2, float y2) {
    	return azimuth(x1,y1,x2,y2) ;
    }
    static double azimuth(int x1, int y1, int x2, int y2) {
    	return azimuth(x1,y1,x2,y2) ;
    }

    //File reader that reads a file 5,000,000 characters at a time and returns the file as a String
	public static String readFileFast (File file) {
		char[] charArray = new char[5000000]; //character array buffer
		int numChars = 0; //number of characters read per attempt
		String fileText = "";
		//Read text file and insert into text area
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			//System.out.println("reading characters..");
			while ((numChars=reader.read(charArray,0,charArray.length)) != -1) {
				//System.out.println("converting characters..");
				fileText = fileText+String.copyValueOf(charArray,0,numChars);
			}
			reader.close();			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return fileText;
	}
	
	public static void print(String string) {
		System.out.println(string);
	}
	
	public static void printErr(String string) {
		System.err.println(string);
	}


	public enum PrecisionLevels { INT, FLOAT, DOUBLE, NAN, BOOL }; 
	public static PrecisionLevels getPrecisionLevel(Object object) {
		Class<? extends Object> oClass = object.getClass();
		String className = oClass.getName();
		if (className.equals("java.lang.Integer")) return PrecisionLevels.INT;
		if (className.equals("java.lang.Float")) return PrecisionLevels.FLOAT;
		if (className.equals("java.lang.Double")) return PrecisionLevels.DOUBLE;
		if (className.equals("java.lang.Boolean")) return PrecisionLevels.BOOL;
		return PrecisionLevels.NAN;
		/*
		int tryInt = 0;
		float tryFloat = 0;
		double tryDouble = 0;Double.parseDouble(object.toString());
		
		try {
			tryInt = Integer.parseInt(object.toString());
		} catch (Exception e){};
		try {
			tryFloat = Float.parseFloat(object.toString());
		} catch (Exception e){};
		try {
			tryDouble = Double.parseDouble(object.toString());
		} catch (Exception e){};

		if (tryDouble - tryInt == 0) return PrecisionLevels.INT;
		if (tryDouble - tryFloat == 0) return PrecisionLevels.FLOAT;
		return PrecisionLevels.DOUBLE;
		*/
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(Class<?> objectClass, String path, String description) {
		java.net.URL imgURL = objectClass.getClassLoader().getResource(path);
		if (imgURL == null) {
			imgURL = objectClass.getResource(path);
		} if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Returns an ImageIcon, or null if the path was invalid.
	 * @param objectClass
	 * @param path
	 * @return
	 */
	public static Icon createImageIcon(Class<?> objectClass, String path) {
		return createImageIcon(objectClass, path, "");
	}


	public static int min(int[] array) {
		if (array == null || array.length == 0) return 0;
		int min = array[0];
		for (int num: array) {
			min = Math.min(min, num);
		}
		return min;
	}

	public static double getNumVal(Object o) {
		if (o instanceof Number) return ((Number) o).doubleValue();
		if (o instanceof Boolean) {
			if ((Boolean)o == false) return 0;
			return 1;
		}
		if (o instanceof String) return sval(o.toString()); //this is superfluous and could slow down code
		return 0;
	}

	/**
	 * returns the concatenation of arrays 1 and 2.
	 * If either are null, the other array is returned.
	 * If both are null, null is returned.
	 * 
	 * @param array1
	 * @param array2
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static <T> T[] arrayCat(T[] array1, T[] array2) {
		if (array1 == null) {
			if (array2 == null) return null;
			return array2.clone();
		}
		if (array2 == null) return array1.clone();

		Class<? extends Object[]> class1 = array1.getClass();
		Class<? extends Object[]> class2 = array2.getClass();
		if (class1 != class2) throw new ClassCastException("SUtil.arrayCat(): class types are different: " + class1.getCanonicalName() + " and " + class2.getCanonicalName());
		int newSize = array1.length + array2.length;
		//ArrayList newArray = new ArrayList(newSize);
		T[] newArray = (T[]) Arrays.copyOf(array1, newSize, array1.getClass());
		//System.arraycopy(array1, 0, newArray, 0, array1.length);
		System.arraycopy(array2, 0, newArray, array1.length, array2.length);
		/*
		for (int i=0; i<array1.length; i++) {
			newArray.add(array1[i]);
		}
		for (int i=0; i<array2.length; i++) {
			newArray.add(array2[i]);
		}
		*/
		return newArray;
	}

	/**
	 * returns an array of unique elements based on the Object.equal() method.
	 * Objects in array should be sorted before running this routine.
	 * @param array
	 */
	public static <T> T[] uniq(T[] array) {
		if (array == null || array.length == 0) return array;
		T o0 = array[0];
		T o1 = null;
		int uVals = 1;
		for (int i=1; i<array.length; i++) {
			o1 = array[i];
			if (!(o0 == null && o1 == null)) {
				if (o0 == null || o1 == null || !o1.equals(o0)) {
					array[uVals] = o1;
					uVals++;
				}
			}
			o0 = o1;
		}
		return (T[]) Arrays.copyOf(array, uVals, array.getClass());
	}

	/**
	 * returns string shortened or lengthened as needed to make it's length == size.
	 * spaces are padded at beginning of string as needed.
	 * @param string
	 * @param size
	 * @return
	 */
	public static String stringResize(String string, int size) {
		if (string == null) return null;
		if (string.length() > size) string = string.substring(0, size);
		while (string.length() < size)
		    string = " " + string; //right-justified
		return string;
	}

	/**
	 *  returns true for values greater than zero.
	 *  False otherwise.
	 * @param newVal
	 * @return
	 */
	public static Object getBoolVal(double val)
	{
	    if (val > 0 ) return true;
	    return false;
	}


    public static String formatTimeLong(long totalTime)
    {
        DecimalFormat df2 = new java.text.DecimalFormat("00");
        DecimalFormat df1 = new java.text.DecimalFormat("00.000");
        
        int days = (int) (totalTime/DAY);
        int hours = (int) ((totalTime - days*DAY)/HOUR);
        int minutes = (int) ((totalTime - days*DAY - hours*HOUR)/MINUTE);
        double seconds = ((totalTime - days*DAY - hours*HOUR - minutes*MINUTE - 0.0)/SECOND);
        return df2.format(days)+":"+df2.format(hours)+":"+df2.format(minutes)+":"+df1.format(seconds);
    }
    
    public static String formatTime(long totalTime)
    {
        DecimalFormat df2 = new java.text.DecimalFormat("00");
        
        //int days = (int) (totalTime/DAY);
        int hours = (int) ((totalTime)/HOUR);
        int minutes = (int) ((totalTime - hours*HOUR)/MINUTE);
        double seconds = ((totalTime - hours*HOUR - minutes*MINUTE - 0.0)/SECOND);
        return df2.format(hours)+":"+df2.format(minutes)+":"+df2.format(seconds);
    }
    
    /**
     * sorts an ArrayList of arbitray class type.
     * Objects in ArrayList should implement Comparable interface
     * @param <E>
     * @param objects
     */
    public static <E> void sort(ArrayList<E> objects)
    {
        if(objects == null || objects.size() == 0) return;
 //       Class<? extends Object> klass = objects.get(0).getClass();
        Object[] array = objects.toArray();
        Arrays.sort(array);
        for (int i=0; i<objects.size(); i++) {
            //objects.set(i, klass.cast(array[i]));
            objects.set(i, (E) array[i]);
        }
    }

/**
 * takes arbitrary input and converts it to Type type.
 * This is done by process Object -> String -> Double -> Type
 * (unless output is String, then just Object -> String
 * @param type
 * @param val
 * @return
 */
    public static Object ObjectToType(Type type, Object val)
    {
        String name = type.toString();
    	if (val == null) return null;
//        double doubleVal = sval(val.toString());
    	 double doubleVal = toDouble(val.toString());
        if (name.equals("double")) {
            return doubleVal;
        }
        if (name.equals("float")) {
            return (float)doubleVal;
        }
        if (name.equals("int")) {
            return (int)doubleVal;
        }
        if (name.equals("boolean")) {
            return getBoolVal(doubleVal);
        }
//        if (type == Double.class) {
//            return doubleVal;
//        }
//        if (type == Float.class) {
//            return (float)doubleVal;
//        }
//        if (type == Integer.class) {
//            return (int)doubleVal;
//        }
//        if (type == Boolean.class) {
//            return getBoolVal(doubleVal);
//        }
        return val.toString();
    }


public static <T> void print(T[] objects)
{
    if (objects == null) {
        print("null array");
        return;
    }
    for (Object o: objects) {
        if (o == null) {
            print("null item");
            continue;
        }
        print(o.toString());
    }
}


static java.sql.Date getTodaysDate() {
	java.util.Date date = new java.util.Date();
	Date date2 = new java.sql.Date(date.getTime());
	return date2;
}

/**
 * sort that can handle null objects.
 * uses compareTo() for comparison
 * @param <T>
 * @param array
 * @return
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public static <T> T[] sort(T[] array) {
	if (array == null) return null;
	int l = array.length;
	for (int i=0; i<l; i++) {
		for (int j=i+1; j<l; j++) {
			 Comparable a = (Comparable)array[i];
			 Comparable b = (Comparable)array[j];
			 //SUtil.print("sort a:" + a + " b:" + b + " i:" + i + " j:" + j + " class=" + a.getClass());
			if (b == null || (a != null && a.compareTo(b) > 0)) {
				array[i] = (T)b;
				array[j] = (T)a;
				//SUtil.print("sort switching a:" + a + " b:" + b + " i:" + i + " j:" + j);
			}
		}
	}
	return array;
}

public static void main(String[] args) {
	Object[] ints = new String[] {"1","0" , "564", "0" , "1"};
	Object[] sorted = sort(ints);
	SUtil.print("after sort");
	SUtil.print(sorted);
}

/**
 * calculate median value of an array
 * @param vals
 * @return
 */
public static <T> T median(T[] vals) {
	Arrays.sort(vals);
	return vals[vals.length/2];
}


public static double median(double[] vals) {
	Arrays.sort(vals);
	return vals[vals.length/2];
}

/**
 * Format string as double using Double.parseDouble(),
 * but first clean up text(get rid of blank spaces).
 * @param string
 * @param start
 * @param stop
 * @return
 */
public static double toDouble(String string, int start, int stop) {
	if (string == null) return 0;
	return toDouble(string.substring(start, stop));
}


private static double toDouble(String string) {
	if (string == null) return 0;
	String clean = string.trim();
	return Double.parseDouble(clean);
}


public static <T> int indexOf(T[] list, T object) {
	if (list == null) return -1;
	for (int i=0; i<list.length; i++) {
		if(list[i] != null && list[i].equals(object)) return i;
	}
	return -1;
}

/**
 * Removes crap from char array coming from JNI
 * @param dirtyChars
 * @return
 */
public static char[] cleanChars(char[] dirtyChars) {
	int i=0;
	for (i=0; i<dirtyChars.length; i++) {
		if (Character.isLetterOrDigit(dirtyChars[i])) continue;
		else break;
	}
	char[] cleanChars = new char[i];
	for (int j=0; j<i; j++) cleanChars[j] = dirtyChars[j];
	return cleanChars;
}


public static String getUserName() {
	return System.getProperty("user.name");
}

}
