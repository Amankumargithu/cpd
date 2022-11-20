package ntp.eod.metadata;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import ntp.logger.NTPLogger;
import ntp.util.MDProperty;

public class MetaDataProcessor 
{
  private final static String newIssues="NEW_ISSUES";
  private final static String symbolAdded="SYMBOL_ADDED";
  private static final  String symbolRemoved="SYMBOL_REMOVED";
  private static final  String symbolSet="SYMBOL_SET";
  private static final  String listingMarket="ListingMarket";
  private static final String error="ERROR";
  private static final String columnHeadings="COLUMN_HEADINGS";
  private static final String UTP="UTP";
  private static final String CTAA="CTAA";
  private static final String CTAB="CTAB";
  private static final String OTC="OTC";
  
	  
  private ConcurrentHashMap<String, Object> resultMapUTP=null;
  private ConcurrentHashMap<String, Object> resultMapCTAA=null;
  private ConcurrentHashMap<String, Object> resultMapCTAB=null;
  private ConcurrentHashMap<String, Object> resultMapOTC=null;
  
  private ConcurrentHashMap<String, String> exchangeChangeMap=new ConcurrentHashMap<String, String>();
  private Integer[] columnToCompareUTP=null;
  private Integer[] columnToCompareCTAA=null;
  private Integer[] columnToCompareCTAB=null;
  private Integer[] columnToCompareOTC=null;
  int exchangeColumnUTP=0;
  int exchangeColumnCTAA=0;
  int exchangeColumnCTAB=0;
  private SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd");
  
// private Date currentDate= new Date(116, 11, 29);
  
  public void compareUTPFiles(String sourceDir,String currentDay,String previousDay,String extention,HashSet<String> deadSymbolSet,boolean isNewIssue) throws Error
  {
	  String  filePrefix = MDProperty.getInstance().getProperty("UTP.FILE_PREFIX");
	  String newFile = null;
	  String oldFile =  null;
	  String primaryColumn =null;
	  String columnCompare =null;
	  String[] columnCompareArray = null;
	  String[] columnHeading=null;
	 
		try
		{
		   primaryColumn =  MDProperty.getInstance().getProperty("UTP.PRIMARY_COLUMN");
		   columnCompare =  MDProperty.getInstance().getProperty("UTP.COLUMN_TO_COMPARE");		
	      
	      if (filePrefix==null)
	      {
	    	  filePrefix="meta_UTP";
	    	  NTPLogger.missingProperty("UTP.FILE_PREFIX");
	    	  NTPLogger.defaultSetting("UTP.FILE_PREFIX",filePrefix);
	      }
	      
	      if (primaryColumn==null)
	      {
	    	  primaryColumn="1";
	    	  NTPLogger.missingProperty("UTP.PRIMARY_COLUMN");
	    	  NTPLogger.defaultSetting("UTP.PRIMARY_COLUMN",filePrefix);
	      }
	      
	      if (columnCompare==null)
	      {
	    	  columnCompare="999";
	    	  NTPLogger.missingProperty("UTP.COLUMN_TO_COMPARE");
	    	  NTPLogger.defaultSetting("UTP.COLUMN_TO_COMPARE",columnCompare);
	      }
	      

	      
		   columnCompareArray = columnCompare.split(",");
			 
		      columnToCompareUTP=new Integer[columnCompareArray.length];
	      newFile=sourceDir+File.separator+"eod_"+currentDay+File.separator+filePrefix+"_"+currentDay+"."+extention;
	      oldFile=sourceDir+File.separator+"eod_"+previousDay+File.separator+filePrefix+"_"+previousDay+"."+extention;
	      
		  int i=0;
		  for (String str : columnCompareArray)
			  columnToCompareUTP[i++] = Integer.parseInt(str);
		  
		   resultMapUTP=new CSVFileComparator().readAndCompare(newFile,oldFile, Integer.parseInt(primaryColumn),columnToCompareUTP,isNewIssue,exchangeColumnUTP);

		   if(resultMapUTP==null)
			   return;
		   MetaDataDBpopulator dBpopulator= new MetaDataDBpopulator();
		    
		   if(resultMapUTP.containsKey(newIssues)) 
		   {
			  dBpopulator.insertUTPMetaData((ConcurrentHashMap<String, String[]>) resultMapUTP.get(newIssues), columnToCompareUTP,convertStringToSQLDate(currentDay),deadSymbolSet);    	
			  return;
		   }
		    if(resultMapUTP.get(error)!=null)
		    {
		    	System.out.println("Error " +resultMapUTP.get(error));
		    	return;
		    }
		    columnHeading=(String[]) resultMapUTP.get(columnHeadings);
			} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
  }
  
  public void compareCTAAFiles(String sourceDir,String currentDay,String previousDay,String extention,HashSet<String> deadSymbolSet,boolean isNewIssue) throws Error
  {
	  String  filePrefix = MDProperty.getInstance().getProperty("CTAA.FILE_PREFIX");
	  String newFile = null;
	  String oldFile =  null;
	  String primaryColumn =null;
	  String columnCompare =null;
	  String[] columnCompareArray = null;
	  String[] columnHeading=null;

		try
		{
		   primaryColumn =  MDProperty.getInstance().getProperty("CTAA.PRIMARY_COLUMN");
		   columnCompare =  MDProperty.getInstance().getProperty("CTAA.COLUMN_TO_COMPARE");		
	      
	      if (filePrefix==null)
	      {
	    	  filePrefix="meta_UTP";
	    	  NTPLogger.missingProperty("CTAA.FILE_PREFIX");
	    	  NTPLogger.defaultSetting("CTAA.FILE_PREFIX",filePrefix);
	      }
	      
	      if (primaryColumn==null)
	      {
	    	  primaryColumn="1";
	    	  NTPLogger.missingProperty("CTAA.PRIMARY_COLUMN");
	    	  NTPLogger.defaultSetting("CTAA.PRIMARY_COLUMN",filePrefix);
	      }
	      
	      if (columnCompare==null)
	      {
	    	  columnCompare="999";
	    	  NTPLogger.missingProperty("CTAA.COLUMN_TO_COMPARE");
	    	  NTPLogger.defaultSetting("CTAA.COLUMN_TO_COMPARE",columnCompare);
	      }
			try 
			{
				exchangeColumnCTAA= Integer.parseInt(MDProperty.getInstance().getProperty("UTP.EXCHANGE_COLUMN"));
			}
			catch (Exception e) 
			{
				exchangeColumnCTAA=0;
				NTPLogger.missingProperty("UTP.EXCHANGE_COLUMN");
				NTPLogger.defaultSetting("UTP.EXCHANGE_COLUMN",exchangeColumnCTAA+"");
				NTPLogger.error("Error is parsing UTP.EXCHANGE_COLUMN , Error is : "+e.getMessage());
			}
	      
		   columnCompareArray = columnCompare.split(",");
			 
		   columnToCompareCTAA=new Integer[columnCompareArray.length];
	      newFile=sourceDir+File.separator+"eod_"+currentDay+File.separator+filePrefix+"_"+currentDay+"."+extention;
	      oldFile=sourceDir+File.separator+"eod_"+previousDay+File.separator+filePrefix+"_"+previousDay+"."+extention;
	      
		  int i=0;
		  for (String str : columnCompareArray)
			  columnToCompareCTAA[i++] = Integer.parseInt(str);

	  resultMapCTAA=new CSVFileComparator().readAndCompare(newFile,oldFile, Integer.parseInt(primaryColumn),columnToCompareCTAA,isNewIssue,exchangeColumnCTAA);

	   if(resultMapCTAA==null)
		   return;
		   MetaDataDBpopulator dBpopulator= new MetaDataDBpopulator();
		    
		   if(resultMapCTAA.containsKey(newIssues)) 
		   {
//			  dBpopulator.insertCTAAMetaData((ConcurrentHashMap<String, String[]>) resultMapCTAA.get(newIssues), columnToCompareCTAA,null,deadSymbolSet);    	
			  return;
		   }
		   
		    if(resultMapCTAA.get(error)!=null)
		    {
		    	System.out.println("Error " +resultMapCTAA.get(error));
		    	return;
		    }
		    columnHeading=(String[]) resultMapCTAA.get(columnHeadings);
		} 
	catch (Exception e) 
	{
		e.printStackTrace();
	}
	  
  }
  
  public void compareCTABFiles(String sourceDir,String currentDay,String  previousDay,String extention,HashSet<String> deadSymbolSet,boolean isNewIssue)  throws Error
  {
	  String  filePrefix = MDProperty.getInstance().getProperty("CTAB.FILE_PREFIX");
	  String newFile = null;
	  String oldFile =  null;
	  String primaryColumn =null;
	  String columnCompare =null;
	  String[] columnCompareArray = null;
	  String[] columnHeading=null;
		try
		{
		   primaryColumn =  MDProperty.getInstance().getProperty("CTAB.PRIMARY_COLUMN");
		   columnCompare =  MDProperty.getInstance().getProperty("CTAB.COLUMN_TO_COMPARE");		
	      
	      if (filePrefix==null)
	      {
	    	  filePrefix="meta_UTP";
	    	  NTPLogger.missingProperty("CTAB.FILE_PREFIX");
	    	  NTPLogger.defaultSetting("CTAB.FILE_PREFIX",filePrefix);
	      }
	      
	      if (primaryColumn==null)
	      {
	    	  primaryColumn="1";
	    	  NTPLogger.missingProperty("CTAB.PRIMARY_COLUMN");
	    	  NTPLogger.defaultSetting("CTAB.PRIMARY_COLUMN",filePrefix);
	      }
	      
	      if (columnCompare==null)
	      {
	    	  columnCompare="999";
	    	  NTPLogger.missingProperty("CTAB.COLUMN_TO_COMPARE");
	    	  NTPLogger.defaultSetting("CTAB.COLUMN_TO_COMPARE",columnCompare);
	      }
			try 
			{
				exchangeColumnCTAB= Integer.parseInt(MDProperty.getInstance().getProperty("UTP.EXCHANGE_COLUMN"));
			}
			catch (Exception e) 
			{
				exchangeColumnCTAB=0;
				NTPLogger.missingProperty("UTP.EXCHANGE_COLUMN");
				NTPLogger.defaultSetting("UTP.EXCHANGE_COLUMN",exchangeColumnCTAB+"");
				NTPLogger.error("Error is parsing UTP.EXCHANGE_COLUMN , Error is : "+e.getMessage());
			}
	      
		   columnCompareArray = columnCompare.split(",");
			 
		   columnToCompareCTAB=new Integer[columnCompareArray.length];
	      newFile=sourceDir+File.separator+"eod_"+currentDay+File.separator+filePrefix+"_"+currentDay+"."+extention;
	      oldFile=sourceDir+File.separator+"eod_"+previousDay+File.separator+filePrefix+"_"+previousDay+"."+extention;
	      
		  int i=0;
		  for (String str : columnCompareArray)
			  columnToCompareCTAB[i++] = Integer.parseInt(str);	  
	   resultMapCTAB =new CSVFileComparator().readAndCompare(newFile,oldFile, Integer.parseInt(primaryColumn),columnToCompareCTAB,isNewIssue,exchangeColumnCTAB);
	   
	   if(resultMapCTAB==null)
		   return;
	   MetaDataDBpopulator dBpopulator= new MetaDataDBpopulator();
	    
	   if(resultMapCTAB.containsKey(newIssues)) 
	   {
//		  dBpopulator.insertCTABMetaData((ConcurrentHashMap<String, String[]>) resultMapCTAB.get(newIssues), columnToCompareCTAB,null,deadSymbolSet);    	
		  return;
	   }
	   
	    if(resultMapCTAB.get(error)!=null)
	    {
	    	System.out.println("Error " +resultMapCTAB.get(error));
	    	return;
	    }
	    columnHeading=(String[]) resultMapCTAB.get(columnHeadings);
		} 
	catch (Exception e) 
	{
		e.printStackTrace();
	}
  
	  
  }
  
  
  public void compareOTCFiles(String sourceDir,String currentDay,String  previousDay,String extention,HashSet<String> deadSymbolSet,boolean isNewIssue)  throws Error
  {
	  String  filePrefix = MDProperty.getInstance().getProperty("OTC.FILE_PREFIX");
	  String newFile = null;
	  String oldFile =  null;
	  String primaryColumn =null;
	  String columnCompare =null;
	  String[] columnCompareArray = null;
	  String[] columnHeading=null;
		try
		{
		   primaryColumn =  MDProperty.getInstance().getProperty("OTC.PRIMARY_COLUMN");
		   columnCompare =  MDProperty.getInstance().getProperty("OTC.COLUMN_TO_COMPARE");		
	      
	      if (filePrefix==null)
	      {
	    	  filePrefix="meta_OTC";
	    	  NTPLogger.missingProperty("OTC.FILE_PREFIX");
	    	  NTPLogger.defaultSetting("OTC.FILE_PREFIX",filePrefix);
	      }
	      
	      if (primaryColumn==null)
	      {
	    	  primaryColumn="1";
	    	  NTPLogger.missingProperty("OTC.PRIMARY_COLUMN");
	    	  NTPLogger.defaultSetting("OTC.PRIMARY_COLUMN",filePrefix);
	      }
	      
	      if (columnCompare==null)
	      {
	    	  columnCompare="999";
	    	  NTPLogger.missingProperty("OTC.COLUMN_TO_COMPARE");
	    	  NTPLogger.defaultSetting("OTC.COLUMN_TO_COMPARE",columnCompare);
	      }
	      
		   columnCompareArray = columnCompare.split(",");
			 
		   columnToCompareOTC=new Integer[columnCompareArray.length];
	      newFile=sourceDir+File.separator+"eod_"+currentDay+File.separator+filePrefix+"_"+currentDay+"."+extention;
	      oldFile=sourceDir+File.separator+"eod_"+previousDay+File.separator+filePrefix+"_"+previousDay+"."+extention;
	      
		  int i=0;
		  for (String str : columnCompareArray)
			  columnToCompareOTC[i++] = Integer.parseInt(str);	  
	   resultMapOTC =new CSVFileComparator().readAndCompare(newFile,oldFile, Integer.parseInt(primaryColumn),columnToCompareOTC,isNewIssue,0);
	   
	   if(resultMapOTC==null)
		   return;
	   MetaDataDBpopulator dBpopulator= new MetaDataDBpopulator();
	    
	   if(resultMapOTC.containsKey(newIssues)) 
	   {
			  ConcurrentHashMap<String,String[]>  newIssueMapUTP =(ConcurrentHashMap<String, String[]>) resultMapUTP.get(newIssues);	
			  ConcurrentHashMap<String,String[]>  newIssueMapCTAA =(ConcurrentHashMap<String, String[]>) resultMapCTAA.get(newIssues);
			  ConcurrentHashMap<String,String[]>  newIssueMapCTAB =(ConcurrentHashMap<String, String[]>) resultMapCTAB.get(newIssues);;
			  ConcurrentHashMap<String,String[]>  newIssueMapOTC=(ConcurrentHashMap<String, String[]>) resultMapOTC.get(newIssues);
			  ConcurrentHashMap<String,String[]>  newIssueEquity = new  ConcurrentHashMap<String ,String[]>();
			  
			  newIssueEquity.putAll(newIssueMapUTP);
			  newIssueEquity.putAll(newIssueMapCTAA);
			  newIssueEquity.putAll(newIssueMapCTAB);
			  
			  for(String symbol:newIssueMapOTC.keySet())
			  {
				  if (newIssueEquity.containsKey(symbol))
				  {
					  newIssueMapOTC.remove(symbol);
					  NTPLogger.warning("OTC symbol found in equity for new issues , Symbol is : " +symbol);
				  }
			  }
			  
		  dBpopulator.insertOTCMetaData(newIssueMapOTC,columnToCompareOTC,null,deadSymbolSet);    	
		  return;
	   }
	   
	    if(resultMapOTC.get(error)!=null)
	    {
	    	 NTPLogger.error("Error " +resultMapOTC.get(error));
	    	return;
	    }
	    columnHeading=(String[]) resultMapOTC.get(columnHeadings);
		} 
	catch (Exception e) 
	{
		e.printStackTrace();
	}
  
	  
  }
  public void compareOTCandOTCBBFiles(String sourceDir,String currentDay,String extention)  throws Error
  {
	  String  filePrefixOTC = MDProperty.getInstance().getProperty("OTC.FILE_PREFIX");
	  String  filePrefixOTCBB = MDProperty.getInstance().getProperty("OTCBB.FILE_PREFIX");
	  String fileOTC = null;
	  String fileOTCBB =  null;
	  String finalOTC =  null;
	  String primaryColumnOTCBB =null;
	  String columnCompareOTCBB =null;
	  String[] columnCompareArray = null;
	  String[] columnHeading=null;
	   ConcurrentHashMap<String, Object> resultMapOTCandOTCBB=null;
		try
		{
		   primaryColumnOTCBB =  MDProperty.getInstance().getProperty("OTCBB.PRIMARY_COLUMN");
		   columnCompareOTCBB =  MDProperty.getInstance().getProperty("OTCBB.COLUMN_TO_COMPARE");		
	      
	      if (filePrefixOTC==null)
	      {
	    	  filePrefixOTC="meta_UTP";
	    	  NTPLogger.missingProperty("OTC.FILE_PREFIX");
	    	  NTPLogger.defaultSetting("OTC.FILE_PREFIX",filePrefixOTC);
	      }
	      if (filePrefixOTCBB==null)
	      {
	    	  filePrefixOTCBB="meta_UTP";
	    	  NTPLogger.missingProperty("OTCBB.FILE_PREFIX");
	    	  NTPLogger.defaultSetting("OTCBB.FILE_PREFIX",filePrefixOTCBB);
	      }
	      
	      if (primaryColumnOTCBB==null)
	      {
	    	  primaryColumnOTCBB="1";
	    	  NTPLogger.missingProperty("OTCBB.PRIMARY_COLUMN");
	    	  NTPLogger.defaultSetting("OTCBB.PRIMARY_COLUMN",primaryColumnOTCBB);
	      }
	      
	      if (columnCompareOTCBB==null)
	      {
	    	  NTPLogger.missingProperty("OTCBB.COLUMN_TO_COMPARE");
	    	  return;
	      }
	      
		   columnCompareArray = columnCompareOTCBB.split(",");
			 
		   columnToCompareCTAB=new Integer[columnCompareArray.length];
	      fileOTC=sourceDir+File.separator+"eod_"+currentDay+File.separator+filePrefixOTC+"_"+currentDay+"."+extention;
	      fileOTCBB=sourceDir+File.separator+"eod_"+currentDay+File.separator+filePrefixOTCBB+"_"+currentDay+"."+extention;
	      finalOTC=sourceDir+File.separator+"eod_"+currentDay+File.separator+"final_"+filePrefixOTC+"_"+currentDay+"."+extention;
		  int i=0;
		  for (String str : columnCompareArray)
			  columnToCompareCTAB[i++] = Integer.parseInt(str);	  
		  resultMapOTCandOTCBB =new CSVFileComparator().readAndCompareSubset(fileOTC,fileOTCBB, Integer.parseInt(primaryColumnOTCBB),columnToCompareCTAB);
	   
		  createCorrectOtcFile(fileOTC, finalOTC, resultMapOTCandOTCBB);
	   if(resultMapCTAB==null)
		   return;
	   
	    if(resultMapCTAB.get(error)!=null)
	    {
	    	System.out.println("Error " +resultMapCTAB.get(error));
	    	return;
	    }
	    columnHeading=(String[]) resultMapCTAB.get(columnHeadings);
		} 
	catch (Exception e) 
	{
		e.printStackTrace();
	}
  
	  
  }
  public void createCorrectOtcFile(String sourcefile,String outputFile,ConcurrentHashMap<String, Object> resultMapOTCandOTCBB)
  {
	  
	  try 
	  {
		  CsvReader reader= new CsvReader(sourcefile);
		  CsvWriter writer=new CsvWriter(outputFile);
		  ConcurrentHashMap<String, Integer> columnIdexMap=new ConcurrentHashMap<String, Integer>();
		  
		  reader.readRecord();
		  for(int columnCount=0;columnCount<reader.getColumnCount();columnCount++)
			  columnIdexMap.put(reader.get(columnCount),columnCount);
		  writer.writeRecord(reader.getValues());
		  
		  ConcurrentHashMap<String, String[]> cMap;
		  String[] compArray;
		  String[] recordValues;
		  
			while(reader.readRecord())
			{
				recordValues=reader.getValues();
				if(resultMapOTCandOTCBB.containsKey(recordValues[1]))
				{
					cMap=(ConcurrentHashMap<String, String[]>) resultMapOTCandOTCBB.get(recordValues[1]);
					
					for(String column :cMap.keySet())
					{
						compArray=cMap.get(column);
						
						if(compArray[0].trim().equals(""))
						{
							recordValues[columnIdexMap.get(column)]=compArray[1];
						}
					}				
				}
				writer.writeRecord(recordValues);
			}		
			
			reader.close();
			writer.close();
	  } 
	  catch (Exception e) 
	  {
		  e.printStackTrace();
	  }
	  
  }
  
  public void calculateExchangeChange(int exchangeColumn,ConcurrentHashMap<String,String[]> newIssueMap,Set<String> symbolSet) throws Error
  { 	
	  try 
	  {
		  for(String symbol: newIssueMap.keySet())
		  {
			  if (symbolSet.contains(symbol))
			  {   
				 exchangeChangeMap.put(symbol,newIssueMap.get(symbol)[exchangeColumn]);
				 newIssueMap.remove(symbol);
				 symbolSet.remove(symbol);
			  }
		  }
		} 
	  catch (Exception e) 
	  {
			e.printStackTrace();
	  }
 }
  
  public void calculateExchangeChange(int exchangeColumn,Set<String> symbolSet,ConcurrentHashMap<String,String[]> newIssueMap) throws Error
  { 	
	  try 
	  {
		  for(String symbol: newIssueMap.keySet())
		  {
			  if (symbolSet.contains(symbol))
			  {   
				 exchangeChangeMap.put(symbol,newIssueMap.get(symbol)[exchangeColumn]);
				 newIssueMap.remove(symbol);
				 symbolSet.remove(symbol);
			  }
		  }
		} 
	  catch (Exception e) 
	  {
			e.printStackTrace();
	  }
 }
  
  
  public void calculateExchangeChange(Set<String> symbolSet,ConcurrentHashMap<String,String> allSymbol,boolean isOTC) throws Error
  { 	
	  try 
	  {
		  for(String symbol: allSymbol.keySet())
		  {
			  if (symbolSet.contains(symbol))
			  {   
				  if(isOTC)
					  exchangeChangeMap.put(symbol,OTC);
				  else
					  exchangeChangeMap.put(symbol,allSymbol.get(symbol)); 
				 symbolSet.remove(symbol);
			  }
		  }
		} 
	  catch (Exception e) 
	  {
			e.printStackTrace();
	  }
 }
  
  public void calculateExchangeChange(int exchangeColumn,ConcurrentHashMap<String,String[]> newIssueMap,Set<String> symbolSet1,Set<String> symbolSet2) throws Error
  { 	
	  try 
	  {
		  for(String symbol: newIssueMap.keySet())
		  {
			  if (symbolSet1.contains(symbol))
			  {   
				 exchangeChangeMap.put(symbol,newIssueMap.get(symbol)[exchangeColumn]);
				 newIssueMap.remove(symbol);
				 symbolSet1.remove(symbol);
			  }
			  else if (symbolSet2.contains(symbol))
			  { 

				 exchangeChangeMap.put(symbol,newIssueMap.get(symbol)[exchangeColumn]);
				 newIssueMap.remove(symbol);
				 symbolSet2.remove(symbol);
			  }
		  }
		} 
	  catch (Exception e) 
	  {
			e.printStackTrace();
	  }
 }
  
//  public void calculateDelistIssueExchangeChange(String delistIssueExchnge,Set<String>dilistedSymbolSet,String exchnge1,ConcurrentHashMap<String,String[]>symbolMap1,String exchnge2,ConcurrentHashMap<String,String[]>symbolMap2)
//  { 	  
//	  for(String symbol: dilistedSymbolSet)
//	  {
//
//		  if (symbolMap1.containsKey(symbol))
//		  {   
//			 exchangeChangeMap.put(symbol, exchnge1+"-"+delistIssueExchnge);
//			 symbolMap1.remove(symbol);
//		  }
//		  else if (symbolMap2.containsKey(symbol))
//		  {   
//			 exchangeChangeMap.put(symbol, exchnge2+"-"+delistIssueExchnge);
//			 symbolMap2.remove(symbol);
//		  }
//	  }
//  }

  public void calculateExchangeChange() throws Error
  {  
	  ConcurrentHashMap<String,String[]>  newIssueMapUTP = null;
	  ConcurrentHashMap<String,String[]>  newIssueMapCTAA = null;
	  ConcurrentHashMap<String,String[]>  newIssueMapCTAB = null;
	  ConcurrentHashMap<String,String[]>  newIssueMapOTC = null;
	  Set<String>  removedSetUTP = null;
	  Set<String>  removedSetCTAA = null;
	  Set<String>  removedSetCTAB = null;
	  Set<String>  removedSetOTC = null;
	  ConcurrentHashMap<String,String>  allSymbolMapUTP = null;
	  ConcurrentHashMap<String,String>  allSymbolMapCTAA = null;
	  ConcurrentHashMap<String,String>  allSymbolMapCTAB = null;
	  ConcurrentHashMap<String,String>  allSymbolMapOTC = null;
	  try
	  {
			if(resultMapUTP.get(symbolAdded)!=null)
			  newIssueMapUTP=(ConcurrentHashMap<String, String[]>) resultMapUTP.get(symbolAdded);		
		    if(resultMapUTP.get(symbolRemoved)!=null)
		    	removedSetUTP =(Set<String>) resultMapUTP.get(symbolRemoved);
		    
			if(resultMapCTAA.get(symbolAdded)!=null)
		       newIssueMapCTAA=(ConcurrentHashMap<String, String[]>) resultMapCTAA.get(symbolAdded);		
			if(resultMapCTAA.get(symbolRemoved)!=null)
			   removedSetCTAA =(Set<String>) resultMapCTAA.get(symbolRemoved);
			    		    
			if(resultMapCTAB.get(symbolAdded)!=null)
					  newIssueMapCTAB=(ConcurrentHashMap<String, String[]>) resultMapCTAB.get(symbolAdded);		
		    if(resultMapCTAB.get(symbolRemoved)!=null)
				    	removedSetCTAB =(Set<String>) resultMapCTAB.get(symbolRemoved);
		    
			if(resultMapOTC.get(symbolAdded)!=null)
				  newIssueMapOTC=(ConcurrentHashMap<String, String[]>) resultMapOTC.get(symbolAdded);		
			if(resultMapOTC.get(symbolRemoved)!=null)
			    	removedSetOTC =(Set<String>) resultMapOTC.get(symbolRemoved);
			
			allSymbolMapUTP=(ConcurrentHashMap<String, String>) resultMapUTP.get(symbolSet);
			allSymbolMapCTAA=(ConcurrentHashMap<String, String>) resultMapCTAA.get(symbolSet);
			allSymbolMapCTAB=(ConcurrentHashMap<String, String>) resultMapCTAB.get(symbolSet);
			allSymbolMapOTC=(ConcurrentHashMap<String, String>) resultMapOTC.get(symbolSet);
		    
			//caclute exchangeChange with utp files  
			
		    calculateExchangeChange(exchangeColumnUTP, newIssueMapUTP,removedSetCTAA,removedSetCTAB);
		    calculateExchangeChange(exchangeColumnCTAA, newIssueMapCTAA,removedSetCTAB,removedSetUTP);
		    calculateExchangeChange(exchangeColumnCTAB, newIssueMapCTAB,removedSetCTAA,removedSetUTP);
		    
		   //Comparing OTC delisted symbol with OTC new Issue
		    
		    calculateExchangeChange(exchangeColumnUTP, newIssueMapUTP, removedSetOTC);
		    calculateExchangeChange(exchangeColumnCTAA, newIssueMapCTAA, removedSetOTC);
		    calculateExchangeChange(exchangeColumnCTAB, newIssueMapCTAB, removedSetOTC);
		    
		  //Comparing OTC new symbol with OTC delisted Issue
		    
		    calculateExchangeChange(exchangeColumnUTP,removedSetUTP, newIssueMapOTC);
		    calculateExchangeChange(exchangeColumnUTP,removedSetUTP, newIssueMapOTC);
		    calculateExchangeChange(exchangeColumnUTP,removedSetUTP, newIssueMapOTC);
		    
		  //compare Delsited equity with OTCSymbolSet  
		    
		    calculateExchangeChange(removedSetUTP,allSymbolMapOTC,false);
		    calculateExchangeChange(removedSetCTAA,allSymbolMapOTC,false);
		    calculateExchangeChange(removedSetUTP, allSymbolMapOTC,false);
		    
		  //compare Delsited OTC with EquitySymbolSet  
		    
		    calculateExchangeChange(removedSetOTC,allSymbolMapUTP,true);
		    calculateExchangeChange(removedSetOTC,allSymbolMapCTAA,true);
		    calculateExchangeChange(removedSetOTC, allSymbolMapCTAB,true);

	  }
	  catch(Exception ex)
	  {
		  ex.printStackTrace();
	  }
  }
  
  public void processNewIssues(HashSet<String>deadSymbolSet,String date)
  {
	  ConcurrentHashMap<String, String[]> equityNewIssues= new ConcurrentHashMap<String, String[]>();
	  
	  if(resultMapUTP.containsKey(newIssues))
		  equityNewIssues.putAll((ConcurrentHashMap<String, String[]>) resultMapUTP.get(newIssues));
	  else
		  equityNewIssues.putAll((ConcurrentHashMap<String, String[]>) resultMapUTP.get(symbolAdded));
	  if(resultMapCTAA.containsKey(newIssues))
		  equityNewIssues.putAll((ConcurrentHashMap<String, String[]>) resultMapCTAA.get(newIssues));
	  else
		  equityNewIssues.putAll((ConcurrentHashMap<String, String[]>) resultMapCTAA.get(symbolAdded));
	  if(resultMapCTAB.containsKey(newIssues))
		  equityNewIssues.putAll((ConcurrentHashMap<String, String[]>) resultMapCTAB.get(newIssues));
	  else
		  equityNewIssues.putAll((ConcurrentHashMap<String, String[]>) resultMapCTAB.get(symbolAdded));
	  
	  
  	  MetaDataDBpopulator dBpopulator= new MetaDataDBpopulator();
	  dBpopulator.insertUTPMetaData((ConcurrentHashMap<String, String[]>) resultMapUTP.get(symbolAdded), columnToCompareUTP,convertStringToSQLDate(date),deadSymbolSet);    	
	  dBpopulator.insertCTAAMetaData((ConcurrentHashMap<String, String[]>) resultMapCTAA.get(symbolAdded), columnToCompareCTAA,convertStringToSQLDate(date),deadSymbolSet);    	
	  dBpopulator.insertCTABMetaData((ConcurrentHashMap<String, String[]>) resultMapCTAB.get(symbolAdded), columnToCompareCTAB,convertStringToSQLDate(date),deadSymbolSet);    		  
  }
  
  public void processDelistedIssues(String date )
  {
  	  MetaDataDBpopulator dBpopulator= new MetaDataDBpopulator();
	  dBpopulator.updateDelistedSymbolMetaData((Set<String>) resultMapUTP.get(symbolRemoved),convertStringToSQLDate(date));    	
	  dBpopulator.updateDelistedSymbolMetaData((Set<String>) resultMapCTAA.get(symbolRemoved),convertStringToSQLDate(date));
	  dBpopulator.updateDelistedSymbolMetaData((Set<String>) resultMapCTAB.get(symbolRemoved),convertStringToSQLDate(date));	  
  }
  
  public void processExchangeChangeIssues(String date)
  {  
	  try 
	  {
  	  MetaDataDBpopulator dBpopulator= new MetaDataDBpopulator();
  	  if(exchangeChangeMap.size()>0)
  		  dBpopulator.updateExchangeChange1(exchangeChangeMap,convertStringToSQLDate(date)); 
      else
   	   NTPLogger.info("No exchnage Change Found");
      ConcurrentHashMap<String, String[]>  exchangeMap=new ConcurrentHashMap<String,String[]>();
  	  
  	  for(String value :resultMapCTAA.keySet())
  	  {
  		  if(value.equals(error)||value.equals(symbolAdded)||value.equals(symbolRemoved)||value.equals(newIssues)||value.equals(columnHeadings))
  			  continue;
  		ConcurrentHashMap<String, String[]> cMap=(ConcurrentHashMap<String, String[]>) resultMapCTAA.get(value);
  		for (String column: cMap.keySet())
  		{
  			if(column.equalsIgnoreCase(listingMarket))
  				exchangeMap.put(value, cMap.get(column));
  		}
  	  
  	  }
  	  
 	  for(String value :resultMapCTAB.keySet())
  	  {
  		  if(value.equals(error)||value.equals(symbolAdded)||value.equals(symbolRemoved)||value.equals(newIssues)||value.equals(columnHeadings))
  			  continue;
  		  
    		ConcurrentHashMap<String, String[]> cMap=(ConcurrentHashMap<String, String[]>) resultMapCTAA.get(value);
      		for (String column: cMap.keySet())
      		{
      			if(column.equalsIgnoreCase(listingMarket))
      				exchangeMap.put(value, cMap.get(column));
      		}
  	  
  	  }
       if(exchangeMap.size()>0)
		  dBpopulator.updateExchangeChange(exchangeMap,convertStringToSQLDate(date));
       else
    	   NTPLogger.info("No exchnage Change Found");
		} catch (Exception e) {
			// TODO: handle exception
		}
  }
 
  private java.sql.Date convertStringToSQLDate(String date) 
  {
	 java.sql.Date sqldate =null;
	 try 
	 {
		  SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	      java.util.Date parsed = format.parse(date);
	      sqldate = new java.sql.Date(parsed.getTime());
	 }
	 catch (Exception e) 
	 {
			// TODO: handle exception
	 }
	return sqldate;
  }
  
}
