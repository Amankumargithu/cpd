package ntp.util;

import java.text.DecimalFormat;
import java.util.HashMap;

public class PumpUtil {

	private static HashMap<String, String> callMappingMonth = new HashMap<String, String>();
	private static HashMap<String, String> putMappingMonth = new HashMap<String, String>();
	private static DecimalFormat df = new DecimalFormat("0.0000");
    
	static
	{
		callMappingMonth.put("A", "01"); // call month jan
		callMappingMonth.put("B", "02");
		callMappingMonth.put("C", "03");
		callMappingMonth.put("D", "04");
		callMappingMonth.put("E", "05");
		callMappingMonth.put("F", "06");
		callMappingMonth.put("G", "07");
		callMappingMonth.put("H", "08");
		callMappingMonth.put("I", "09");
		callMappingMonth.put("J", "10");
		callMappingMonth.put("K", "11");
		callMappingMonth.put("L", "12");

		putMappingMonth.put("M", "01"); // put month jan ... so on
		putMappingMonth.put("N", "02");
		putMappingMonth.put("O", "03");
		putMappingMonth.put("P", "04");
		putMappingMonth.put("Q", "05");
		putMappingMonth.put("R", "06");
		putMappingMonth.put("S", "07");
		putMappingMonth.put("T", "08");
		putMappingMonth.put("U", "09");
		putMappingMonth.put("V", "10");
		putMappingMonth.put("W", "11");
		putMappingMonth.put("X", "12");
	}
	
	public static String getCallMap(String key)
	{
		return callMappingMonth.get(key);
	}
	
	public static String getPutMap(String key)
	{
		return putMappingMonth.get(key);
	}

	public static String formatFourDecimals(double val)
	   {
		
	      String value = "";
	      try
	      {      
	    	  value = df.format(val); 
	      }
	      catch(NumberFormatException e)
	      {
	         return(value);      
	      }      
	      catch(Exception e)
	      {
	         return(value);
	      }
	      return(value);
	   }
	 
}
