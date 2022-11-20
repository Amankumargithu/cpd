package ntp.util;

import java.util.ArrayList;

public class TSQConditionCodeGenerator 
{
	
	public static String generateConditionCode(int settlementType, int reportType, int reportDetail, int reportFlags)
	{
		String conditionCode = "";
		
		try 
		{
			conditionCode = getSettlementType(settlementType);
			conditionCode += getReportType(reportType);
			conditionCode += getReportDetail(reportDetail);
			conditionCode += getReportFlag(reportFlags) ;			
			conditionCode = reFormatConditionCode(conditionCode);
			
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		if(conditionCode != null &&conditionCode.length()<1)
		{
			conditionCode = " ";
		}
		
		return conditionCode;
	}
	
	public static String generateCanadianConditionCode(int settlementType, int reportType, int reportDetail, int reportFlags)
	{
		String conditionCode = "";
		
		try 
		{
			conditionCode = getCanadianSettlementType(settlementType);
			conditionCode += getCanadianReportType(reportType);
			conditionCode += getCanadianReportDetail(reportDetail);
			conditionCode += getCanadianReportFlag(reportFlags) ;			
			conditionCode = reFormatConditionCode(conditionCode);
			
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		if(conditionCode != null &&conditionCode.length()<1)
		{
			conditionCode = " ";
		}
		
		return conditionCode;
	}

	private static String reFormatConditionCode(String conditionCode) 
	{
		if(conditionCode.endsWith(","))
		{
			conditionCode = conditionCode.substring(0,conditionCode.length() - 1);// omit ','
		}
		return conditionCode;
	}

	private static String getSettlementType(int settlementType) 
	{
		
		switch (settlementType)
		{
			case 1: return "0,";  
			case 2: return "3,";
			case 3: return "14,";
			case 4: return "8,";			 
		}
		return "";
	}
	
	private static String getCanadianSettlementType(int settlementType) 
	{
		
		switch (settlementType)
		{
			case 1: return "0,";  
			case 2: return "3,";
			case 3: return "55,";
			case 4: return "56,";
			case 5: return "57,";
			case 6: return "58,";
		}
		return "";
	}
	
	private static String getReportType(int reportType) 
	{
		switch (reportType) 
		{
		case 1: return ""; // show nothing, can also omit this case
		case 2: return "15,";
		case 3: return "54,";
		case 4: return "53,";
		case 5: return "6,";
		case 6: return "52,";

		}
		return "";
	}
	
	private static String getCanadianReportType(int reportType) 
	{
		switch (reportType) 
		{
		case 1: return ""; // show nothing, can also omit this case
		case 2: return "15,";
		case 3: return "59,";
		case 4: return "60,";
		case 5: return "61,";
		case 6: return "62,";
		case 7: return "63,";

		}
		return "";
	}
	
	private static String getReportDetail(int reportDetail) 
	{
		switch (reportDetail)
		{
		case 0: return ""; // show nothing, can also omit this case
		case 1: return "24,";
		case 2: return "17,";
		case 3: return "13,";
		case 4: return "1,";
		case 5: return "7,";
		case 6: return "4,";
		case 7: return "8,";
		case 8: return "9,";
		case 9: return "23,";
		case 10: return "5,";
		case 11: return "16,";
		case 12: return "11,";
		case 13: return "22,";
		case 14: return "19,";
		case 15: return "49,";
						 
		}
		return "";
	}

	private static String getCanadianReportDetail(int reportDetail) 
	{
		switch (reportDetail)
		{
		case 0: return ""; // show nothing, can also omit this case
		case 1: return "24,";
		}
		return "";
	}
	
	private static String getReportFlag(int reportFlags) 
	{
		ArrayList<Integer> list = getBits(reportFlags);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			
			switch (list.get(i))
			{
				case 0x1:  builder.append("27,"); break;
				case 0x2:  builder.append("20,"); break;
				case 0x4:  builder.append("26,"); break;
				case 0x8:  builder.append("R6,"); break;
				case 0x10: builder.append("10,"); break;
				case 0x20: builder.append("21,"); break;
				case 0x40: builder.append("40,"); break;
				case 0x80: builder.append("25,"); break;			 
			}

		}
		
		
		return builder.toString();
	}

	private static String getCanadianReportFlag(int reportFlags) 
	{
		ArrayList<Integer> list = getBits(reportFlags);
		StringBuilder builder = new StringBuilder();
		
			for (int i = 0; i < list.size(); i++) 
			{
				switch (list.get(i))
				{
					case 0x1: builder.append("27,"); break;
					case 0x2: builder.append("20,"); break;
					case 0x4: builder.append("26,"); break;
					case 0x8: builder.append("64,"); break;
					case 0x10: builder.append("65,"); break;
					case 0x20: builder.append("66,"); break;
					case 0x40: builder.append("67,"); break;
					case 0x80: builder.append("68,"); break;
					case 0x100: builder.append("69,"); break;
					case 0x200: builder.append("70,"); break;
					case 0x400: builder.append("71,"); break;
					case 0x800: builder.append("72,"); break;
				}
		}

		return builder.toString();
	}
	
	private static ArrayList<Integer> getBits(int num) {
	    ArrayList<Integer> list = new ArrayList<Integer>();
	    for (int k = 0; k < Integer.SIZE; k++) {
	        if (((num >> k) & 1) == 1) {
	            list.add((int)Math.pow(2, k));
	        }
	    }
	    
	    return list;
	}
	
	public static void main(String[] args) {
		
		
		System.out.println(getReportFlag(256));
		System.out.println(getCanadianReportFlag(11));
	}
}
