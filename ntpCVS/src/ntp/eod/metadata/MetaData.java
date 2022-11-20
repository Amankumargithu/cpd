package ntp.eod.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ntp.logger.NTPLogger;
import ntp.util.MDProperty;

public class MetaData 
{	
	private HashSet<String> getDataSetFromFile(String filePath) throws Error
	{
		File symbolFile;
		BufferedReader br=null;
		HashSet<String> dataSet=new HashSet<String>();
		try 
		{
			symbolFile=new File(filePath);
			if (symbolFile.exists())
			{
				 br = new BufferedReader(new FileReader(filePath));
			    String currentLine;
				while ((currentLine = br.readLine()) != null) 
				{
					if(!currentLine.trim().equals(""))
						dataSet.add(currentLine.trim());
				}
				dataSet.remove("");
			}
		}
		catch (Exception e) 
		{
		  e.printStackTrace();
			return null;
		}
		finally{
			try {
				br.close();
			} catch (IOException e) 
			{

				e.printStackTrace();
			}
		}
		return dataSet;
	}
	
	private String getpreviousDay(Set<String> holidaySet) throws Error
	{
		String previousDateST=null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		try
		{
			calendar.setTime(new Date());
			
			if (calendar.get(Calendar.DAY_OF_WEEK)==2)
				calendar.add(Calendar.DAY_OF_MONTH, -3);
			else
				calendar.add(Calendar.DAY_OF_MONTH, -1);
			
			previousDateST=dateFormat.format(calendar.getTime());
			
			while(holidaySet.contains(previousDateST))
			{
				calendar.add(Calendar.DAY_OF_MONTH, -1);
				
				if (calendar.get(Calendar.DAY_OF_WEEK)==1)
					calendar.add(Calendar.DAY_OF_MONTH, -2);
				else if (calendar.get(Calendar.DAY_OF_WEEK)==7)
					calendar.add(Calendar.DAY_OF_MONTH, -1);
				
				previousDateST=dateFormat.format(calendar.getTime());	
			}
		
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return previousDateST;
	}
	
	public static void main(String[] args) 
	{
		String sourceDirPath=null;
		String fileExt = null;
		String currentDay=null;
		String previousDay=null;
		MetaDataProcessor dataProcessor=null;
		File sourceDir=null;
		String deadSymbolFilePath=null;
		HashSet<String> deadSymbolSet=null;
		String holidayFilePath=null;
		HashSet<String> holidaySet=null;
		String newIssue=null;
		boolean isNewIssue=false;
		try 
		{
			 sourceDirPath= MDProperty.getInstance().getProperty("SOURCE_DIR");
			 fileExt =  MDProperty.getInstance().getProperty("FILE_EXT");
			 currentDay =  MDProperty.getInstance().getProperty("CURRENT_DAY");
			 previousDay =  MDProperty.getInstance().getProperty("PREVIOUS_DAY");
			 deadSymbolFilePath =  MDProperty.getInstance().getProperty("DEAD_SYMBOL.FILE_PATH");
			 holidayFilePath =  MDProperty.getInstance().getProperty("HOLIDAYS.FILE_PATH");
			 newIssue = MDProperty.getInstance().getProperty("NEW_ISSUE");
			 if(sourceDirPath==null)
				 NTPLogger.missingProperty("SOURCE_DIR"); 
			 else
			 {
				 if(fileExt==null) 
				 { 
					 fileExt="csv";
					 NTPLogger.missingProperty("FILE_EXT");
					 NTPLogger.defaultSetting("FILE_EXT",fileExt);
				 }
				 
				 if (deadSymbolFilePath!=null)
				 {
					 deadSymbolSet=new MetaData().getDataSetFromFile(deadSymbolFilePath);				 }
				 else 
					 NTPLogger.missingProperty("DEAD_SYMBOL.FILE_PATH");
				 if (holidayFilePath!=null)
				 {
					 holidaySet=new MetaData().getDataSetFromFile(holidayFilePath);				 }
				 else 
					 NTPLogger.missingProperty("HOLIDAYS.FILE_PATH");
				 
				 
				 if(currentDay==null)
				 {  
					 NTPLogger.missingProperty("CURRENT_DAY");
					 NTPLogger.defaultSetting("CURRENT_DAY","Date Today");
					 currentDay = new SimpleDateFormat("yyyyMMdd").format(new Date());
					 previousDay=new MetaData().getpreviousDay(holidaySet);
				 }
				 else if (previousDay==null)
				 {
					 NTPLogger.missingProperty("PREVIOUS_DAY");
					 NTPLogger.error("Previous day is null so terminating process");
					 return ;
				 }

				 if(newIssue==null)
				 {
					 newIssue="false";
					 NTPLogger.missingProperty("NEW_ISSUE");
					 NTPLogger.defaultSetting("NEW_ISSUE",newIssue);
					 
				 }
				 else if(newIssue.equalsIgnoreCase("true")) 
					 isNewIssue=true;
				 
				 sourceDir=new File(sourceDirPath);
				 if(sourceDir.exists())
				 {
					 
				    dataProcessor= new MetaDataProcessor();
				    
//				    dataProcessor.compareOTCandOTCBBFiles(sourceDirPath, currentDay, fileExt);
					dataProcessor.compareUTPFiles(sourceDirPath,currentDay,previousDay,fileExt,deadSymbolSet,isNewIssue);
					dataProcessor.compareCTAAFiles(sourceDirPath,currentDay,previousDay,fileExt,deadSymbolSet,isNewIssue);
					dataProcessor.compareCTABFiles(sourceDirPath,currentDay,previousDay,fileExt,deadSymbolSet,isNewIssue);
					dataProcessor.compareOTCFiles(sourceDirPath, currentDay, previousDay, fileExt, deadSymbolSet, isNewIssue);
				
					if(!isNewIssue)
					{
						dataProcessor.calculateExchangeChange();
						dataProcessor.processNewIssues(deadSymbolSet,currentDay);
						dataProcessor.processDelistedIssues(previousDay);
						dataProcessor.processExchangeChangeIssues(currentDay);
					}
				 }
				 else
					 NTPLogger.error("Source Directory doesn't exists , Directory path is :" +sourceDir.getAbsolutePath());
			 }
		} 
		catch (Exception ex) 
		{
			NTPLogger.error("Exception in main class , Error is " +ex.getMessage());
			
			ex.printStackTrace();
		}
		catch (Error e) 
		{
			NTPLogger.error("Error in main class , Error is " +e.getMessage());
			
			e.printStackTrace();
		}
	}
		
}
