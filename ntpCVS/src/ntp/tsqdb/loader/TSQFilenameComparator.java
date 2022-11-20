package ntp.tsqdb.loader;

import java.util.Comparator;

public class TSQFilenameComparator implements Comparator<String> {

	public int compare(String arg0, String arg1) {
		String filename1 = arg0;
		String filename2 = arg1;
		int startIndex = 3;
		int endIndex   = filename1.indexOf(".");
		String time1 = null;
		String time2 = null;
		time1 = filename1.substring(startIndex,endIndex-2);
		time2 = filename2.substring(startIndex,endIndex-2);
		return time1.compareTo(time2) ;
	}
}
