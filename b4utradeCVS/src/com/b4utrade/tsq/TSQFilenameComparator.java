package com.b4utrade.tsq;

import java.util.Comparator;

public class TSQFilenameComparator implements Comparator<String> {

	public int compare(String arg0, String arg1) {
		String filename1 = arg0;
		String filename2 = arg1;
		
		// parse out the time in millis
		
		int startIndex = filename1.indexOf(".");
		int endIndex   = filename1.indexOf(".",startIndex+1);
		
		String time1 = null;
		String time2 = null;
		
		if (endIndex < startIndex)
			time1 = filename1.substring(startIndex+1);
		else
			time1 = filename1.substring(startIndex+1,endIndex);	
	
		startIndex = filename2.indexOf(".");
		endIndex   = filename2.indexOf(".",startIndex+1);	
		
		if (endIndex < startIndex)
			time2 = filename2.substring(startIndex+1);
		else
			time2 = filename2.substring(startIndex+1,endIndex);	
		
		try {
			long f1 = Long.parseLong(time1);
			long f2 = Long.parseLong(time2);
			
			if (f1 < f2) return -1;
			if (f2 < f1) return 1;	
			
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("TSQFileNameComparator - filename1 : "+filename1+" filename2 : "+filename2);
		}	
		return 0;
	}

}
