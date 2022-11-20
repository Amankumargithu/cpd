package ntp.eod.metadata;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ntp.logger.NTPLogger;
import ntp.util.MDDBManager;

public class MetaDataDBpopulator 
{
//  private Date curreDate= new Date(216, 11, 29);
  Connection aConnection = null;
  String nullValue="NA";
	
	public void connectToDatabase() throws Exception
	{
		try
		{
			aConnection = MDDBManager.getConnection();
		}
		catch(Exception exception){
			exception.printStackTrace();
			throw new Exception(exception.getMessage());
		}
	

	}

  
	private String validateStringValue(String symbol, String field,String value)
	{
       if(value.trim().equals(""))
       {   
    	   if(field.equals("roundLot"))
    		   value="0";
    	   else
    	     value=nullValue;
    	   NTPLogger.warning(field+" value is blank for symbol : " +symbol);
       }
		return value;
	}
	private int validateIntegerValue(String symbol, String field,String value)
	{  
		try 
		{
		  return Integer.parseInt(value);
		} 
		catch (Exception e) 
		{
			 NTPLogger.warning(field+" value is Invalid  for symbol : " +symbol +" , Invalid value is :" +value);
			 return 0;
		}
	}
	private boolean validateBooleanValue(String symbol, String field,String value)
	{
		boolean returnValue=false;
		
       if(value.trim().equals(""))
    	   NTPLogger.warning(field+" value is blank for symbol : " +symbol);
       else if (value.equals("1"))
    	   returnValue=true;
       else if (value.equals("0"))
    	   returnValue=false;
       else
    	   NTPLogger.warning(field+" value is  invalid for symbol : " +symbol +" , Invalid value is :" +value);
       return returnValue;
	}
  
  public void insertUTPMetaData(ConcurrentHashMap<String, String[]> map,Integer[] columnToCompare,Date ipoDate,HashSet<String> deadSymbolSet)
  {  
	  String[] symbolMetaData =null;
	  String listingExchange=null;
	  String productType=null;
	  String currency=null;
	  String country=null;
	  String marketTier=null;
	  String authenticity=null;
	  int roundLot;
	  String issueName=null;
	  String oldSymbol=null;
	  String issueType=null;
	  String primaryMarketCode=null;
	  try 
	  {
		  String insertQuery ="INSERT INTO STOCK (symbol,product_type,currency,country,market_tier,listing_exchange,primary_market_code," +
		  		     "authenticity,round_lot,issue_name,old_symbol,issue_type,ipo_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		  aConnection= MDDBManager.getConnection();
		  
		  String seleteSql="DELETE FROM STOCK WHERE symbol =?";  
		  String updateQuery="Update STOCK Set is_dead = true where symbol =?";
		  
		  PreparedStatement insertStatement = aConnection.prepareStatement(insertQuery);
		  PreparedStatement deleteStatement = aConnection.prepareStatement(insertQuery);
		  PreparedStatement updateStatement = aConnection.prepareStatement(updateQuery);

		  final int batchSize = 100;
		  int count = 0;
		  
    	  for(String symbol :map.keySet())
    	  { 
    		  try
    		  {
	            symbolMetaData = map.get(symbol);
	            productType=validateStringValue(symbol,"productType",symbolMetaData[2]);
	            currency=validateStringValue(symbol,"currency",symbolMetaData[3]);
	            country=validateStringValue(symbol,"country",symbolMetaData[4]);
	            marketTier=validateStringValue(symbol,"marketTier",symbolMetaData[5]);
	            listingExchange=validateStringValue(symbol,"listingExchange",symbolMetaData[6]);
	            primaryMarketCode=validateStringValue(symbol,"primaryMarketCode",symbolMetaData[7]);
	            authenticity=validateStringValue(symbol,"authenticity",symbolMetaData[8]);
	            roundLot=validateIntegerValue(symbol,"roundLot",symbolMetaData[9]);
	            issueName=validateStringValue(symbol,"issueName",symbolMetaData[10]);
	            oldSymbol=validateStringValue(symbol,"oldSymbol",symbolMetaData[11]);
	            issueType=validateStringValue(symbol,"issueType",symbolMetaData[12]);

  	            if(deadSymbolSet!=null&&deadSymbolSet.contains(symbol))
  		        {
  		           if(marketTier.equals(nullValue)&&listingExchange.equals(nullValue))
  		           {
		        	   updateStatement.setString(1,symbol);
		        	   if(updateStatement.executeUpdate()==0)
		        	     NTPLogger.warning("Symbol present In Dead List , Both Market_tier and Listing_exchange values are null , Droped symbol is :"+symbol);
		        	    else
		        	    	NTPLogger.warning("Symbol present In Dead List ,Both Market_tier and Listing_exchange values are null , Upated as dead symbol in data base"+symbol);
		        	   continue;
  		           }
  		        }
  	            else if(marketTier.equals(nullValue)&&listingExchange.equals(nullValue))
		        {
		        	   updateStatement.setString(1,symbol);
		        	   if(updateStatement.executeUpdate()==0)
		        	     NTPLogger.warning("Both Market_tier and Listing_exchange values are null , Droped symbol is :"+symbol);
		        	    else
		        	    	NTPLogger.warning("Both Market_tier and Listing_exchange values are null , Upated as dead symbol in data base"+symbol);
		        	   continue;
		        }
  	            
	            insertStatement.setString(1, symbol);
			  	insertStatement.setString(2, productType);
			  	insertStatement.setString(3, currency);
			  	insertStatement.setString(4, country);
			  	insertStatement.setString(5, marketTier);
			  	insertStatement.setString(6, listingExchange);
			  	insertStatement.setString(7, primaryMarketCode);
			  	insertStatement.setString(8, authenticity);
			  	insertStatement.setInt(9, roundLot);
			  	insertStatement.setString(10, issueName);
			  	insertStatement.setString(11, oldSymbol);
			  	insertStatement.setString(12, issueType);
			  	insertStatement.setDate(13, ipoDate);
			  	insertStatement.addBatch();
			  	
    		  }
    		  catch(Exception ex)
    		  {  
    	 		  NTPLogger.error("Unable to insert redord for "+symbol);
    			  ex.printStackTrace();
    		  }
		  	if(++count % batchSize == 0) {
		  		insertStatement.executeBatch();
		  	}
		  }
		  insertStatement.executeBatch(); // insert remaining records
		  insertStatement.close();
		  updateStatement.close();
		  aConnection.close();  
		  
	  } 
	  catch (Exception e) 
	  {
			e.printStackTrace();
	  }
  }
  
  public void insertCTAAMetaData(ConcurrentHashMap<String, String[]> map,Integer[] columnToCompare,Date ipoDate,HashSet<String> deadSymbolSet)
  {    
	  String[] symbolMetaData =null;
	  String productType=null;
	  String currency=null;
	  String country=null;
	  String marketTier=null;
	  String listingExchange=null;
	  String primaryMarketCode=null;
	  String symbology=null;
	  String alternateSymbol=null;
	  String financialStatus=null;
	  try 
	  {
		  String sql ="INSERT INTO STOCK (symbol,product_type,currency,country,market_tier,listing_exchange,primary_market_code" +
		  		     ",symbology,alternate_symbol,financial_status,ipo_date) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		  String updateQuery="Update STOCK Set is_dead = true where symbol =?";
		  
		  aConnection= MDDBManager.getConnection();
		  
		  PreparedStatement insertStatement = aConnection.prepareStatement(sql);
		  PreparedStatement updateStatement = aConnection.prepareStatement(updateQuery);
		  final int batchSize = 100;
		  int count = 0;
		  
		  
    	  for(String symbol :map.keySet())
    	  { 
    		  try
    		  {
    			symbolMetaData = map.get(symbol);
  	            productType=validateStringValue(symbol,"productType",symbolMetaData[2]);
  	            currency=validateStringValue(symbol,"currency",symbolMetaData[3]);
  	            country=validateStringValue(symbol,"country",symbolMetaData[4]);
  	            marketTier=validateStringValue(symbol,"marketTier",symbolMetaData[5]);
  	            listingExchange=validateStringValue(symbol,"listingExchange",symbolMetaData[6]);
  	            primaryMarketCode=validateStringValue(symbol,"primaryMarketCode",symbolMetaData[7]);
  	            symbology=validateStringValue(symbol,"symbology",symbolMetaData[8]);
  	            alternateSymbol=validateStringValue(symbol,"alternateSymbol",symbolMetaData[9]);
  	            financialStatus=validateStringValue(symbol,"financialStatus",symbolMetaData[10]);
  	            
  	            if(deadSymbolSet!=null&&deadSymbolSet.contains(symbol))
  		        {
  		           if(marketTier.equals(nullValue)&&listingExchange.equals(nullValue))
  		           {
		        	   updateStatement.setString(1,symbol);
		        	   if(updateStatement.executeUpdate()==0)
		        	     NTPLogger.warning("Symbol present In Dead List , Both Market_tier and Listing_exchange values are null , Droped symbol is :"+symbol);
		        	    else
		        	    	NTPLogger.warning("Symbol present In Dead List ,Both Market_tier and Listing_exchange values are null , Upated as dead symbol in data base"+symbol);
		        	   continue;
  		           }
  		        }
  	            else if(marketTier.equals(nullValue)&&listingExchange.equals(nullValue))
		        {
		        	   updateStatement.setString(1,symbol);
		        	   if(updateStatement.executeUpdate()==0)
		        	     NTPLogger.warning("Both Market_tier and Listing_exchange values are null , Droped symbol is :"+symbol);
		        	    else
		        	    	NTPLogger.warning("Both Market_tier and Listing_exchange values are null , Upated as dead symbol in data base"+symbol);
		        	   continue;
		        }
  	            
  	            insertStatement.setString(1, symbol);
  			  	insertStatement.setString(2, productType);
  			  	insertStatement.setString(3, currency);
  			  	insertStatement.setString(4, country);
  			  	insertStatement.setString(5, marketTier);
  			  	insertStatement.setString(6, listingExchange);
  			  	insertStatement.setString(7, primaryMarketCode);
  			  	insertStatement.setString(8, symbology);
  			  	insertStatement.setString(9, alternateSymbol);
  			  	insertStatement.setString(10, financialStatus);
  			  	insertStatement.setDate(11, ipoDate);
  			  	insertStatement.addBatch();
			  	
    		  }catch(Exception ex)
    		  {  
    	 		 NTPLogger.error("Unable to insert redord for "+symbol);
    			  ex.printStackTrace();
    		  }
		  	if(++count % batchSize == 0) {
		  		insertStatement.executeBatch();
		  	}
		  }
		  insertStatement.executeBatch(); // insert remaining records
		  insertStatement.close();
		  updateStatement.close();
		  aConnection.close();  
		  
	  } 
	  catch (Exception e) 
	  {
//		  System.out.println(recordArray1);
			e.printStackTrace();
	  }
  }
  
  public void insertCTABMetaData(ConcurrentHashMap<String, String[]> map,Integer[] columnToCompare ,Date ipoDate,HashSet<String> deadSymbolSet)
  {  
	  String[] symbolMetaData =null;
	  String productType=null;
	  String currency=null;
	  String country=null;
	  String marketTier=null;
	  String listingExchange=null;
	  String primaryMarketCode=null;
	  String symbology=null;
	  String alternateSymbol=null;
	  String financialStatus=null;
	  try 
	  {
		  String sql ="INSERT INTO STOCK (symbol,product_type,currency,country, market_tier,listing_exchange,primary_market_code" +
		  		     ",symbology,alternate_symbol,financial_status,ipo_date) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		  
		  String updateQuery="Update STOCK Set is_dead = true where symbol =?";
		  aConnection= MDDBManager.getConnection();
		  PreparedStatement insertStatement = aConnection.prepareStatement(sql);
		  PreparedStatement updateStatement = aConnection.prepareStatement(updateQuery);
		  final int batchSize = 100;
		  int count = 0;
		  
		  
 	  for(String symbol :map.keySet())
 	  { 
 		  try
 		  {
 			  	symbolMetaData = map.get(symbol);
	            productType=validateStringValue(symbol,"productType",symbolMetaData[2]);
	            currency=validateStringValue(symbol,"currency",symbolMetaData[3]);
	            country=validateStringValue(symbol,"country",symbolMetaData[4]);
	            marketTier=validateStringValue(symbol,"marketTier",symbolMetaData[5]);
	            listingExchange=validateStringValue(symbol,"listingExchange",symbolMetaData[6]);
	            primaryMarketCode=validateStringValue(symbol,"primaryMarketCode",symbolMetaData[7]);
	            symbology=validateStringValue(symbol,"symbology",symbolMetaData[8]);
	            alternateSymbol=validateStringValue(symbol,"alternateSymbol",symbolMetaData[9]);
	            financialStatus=validateStringValue(symbol,"financialStatus",symbolMetaData[10]);
	            
  	            if(deadSymbolSet!=null&&deadSymbolSet.contains(symbol))
  		        {
  		           if(marketTier.equals(nullValue)&&listingExchange.equals(nullValue))
  		           {
		        	   updateStatement.setString(1,symbol);
		        	   if(updateStatement.executeUpdate()==0)
		        	     NTPLogger.warning("Symbol present In Dead List , Both Market_tier and Listing_exchange values are null , Droped symbol is :"+symbol);
		        	    else
		        	    	NTPLogger.warning("Symbol present In Dead List ,Both Market_tier and Listing_exchange values are null , Upated as dead symbol in data base"+symbol);
		        	   continue;
  		           }
  		        }
  	            else if(marketTier.equals(nullValue)&&listingExchange.equals(nullValue))
		        {
		        	   updateStatement.setString(1,symbol);
		        	   if(updateStatement.executeUpdate()==0)
		        	     NTPLogger.warning("Both Market_tier and Listing_exchange values are null , Droped symbol is :"+symbol);
		        	    else
		        	    	NTPLogger.warning("Both Market_tier and Listing_exchange values are null , Upated as dead symbol in data base"+symbol);
		        	   continue;
		        }
  	            
	            
	            insertStatement.setString(1, symbol);
			  	insertStatement.setString(2, productType);
			  	insertStatement.setString(3, currency);
			  	insertStatement.setString(4, country);
			  	insertStatement.setString(5, marketTier);
			  	insertStatement.setString(6, listingExchange);
			  	insertStatement.setString(7, primaryMarketCode);
			  	insertStatement.setString(8, symbology);
			  	insertStatement.setString(9, alternateSymbol);
			  	insertStatement.setString(10, financialStatus);
			  	insertStatement.setDate(11, ipoDate);
			  	insertStatement.addBatch();
			  	
 		  }catch(Exception ex)
 		  {  
 			 NTPLogger.error("Unable to process record for "+symbol);
 			  ex.printStackTrace();
 		  }
		  	if(++count % batchSize == 0) {
		  		insertStatement.executeBatch();
		  	}
		  }
		  insertStatement.executeBatch(); // insert remaining records
		  insertStatement.close();
		  updateStatement.close();
		  aConnection.close();  
		  
	  } 
	  catch (Exception e) 
	  {
//		  System.out.println(recordArray1);
			e.printStackTrace();
	  }
  }
        
  public void insertOTCMetaData(ConcurrentHashMap<String, String[]> map,Integer[] columnToCompare ,Date ipoDate,HashSet<String> deadSymbolSet)
  {  
	  String[] symbolMetaData =null;
	  String productType=null;
	  String currency=null;
	  String country=null;
	  String marketTier=null;
	  String listingExchange=null;
	  String primaryMarketCode=null;
	  boolean caveatEmptor;
	  boolean  BBQuoted;
	  String issueName=null;
	  boolean shortSaleThresholdIndicator;
	  boolean piggybackExempt;
	  boolean unsolicitedOnly;
	  boolean messagingDisabled;
	  boolean saturationEligibleFlag;
	  boolean rule144aFlag;
	  try 
	  {
		  String sql ="INSERT INTO STOCK (symbol,product_type,currency,country, market_tier,listing_exchange,primary_market_code" +
		  		     ",caveat_emptor,bb_quoted,issue_name,shortsale_threshold_indicator,piggyback_exempt,unsolicited_only,messaging_disabled,saturation_eligibleflag,rule144aflag,ipo_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		  
		  String updateQuery="Update STOCK Set is_dead = true where symbol =?";
		  aConnection= MDDBManager.getConnection();
		  PreparedStatement insertStatement = aConnection.prepareStatement(sql);
		  PreparedStatement updateStatement = aConnection.prepareStatement(updateQuery);
		  final int batchSize = 100;
		  int count = 0;
		  
		  
 	  for(String symbol :map.keySet())
 	  { 
 		  try
 		  {
 			  	symbolMetaData = map.get(symbol);
	            productType=validateStringValue(symbol,"productType",symbolMetaData[2]);
	            currency=validateStringValue(symbol,"currency",symbolMetaData[3]);
	            country=validateStringValue(symbol,"country",symbolMetaData[4]);
	            marketTier=validateStringValue(symbol,"marketTier",symbolMetaData[5]);
	            listingExchange=validateStringValue(symbol,"listingExchange",symbolMetaData[6]);
	            primaryMarketCode=validateStringValue(symbol,"primaryMarketCode",symbolMetaData[7]);
	            caveatEmptor=validateBooleanValue(symbol, "caveatEmptor", symbolMetaData[8]);
	            BBQuoted=validateBooleanValue(symbol, "BBQuoted", symbolMetaData[9]);
	            issueName=validateStringValue(symbol,"issueName",symbolMetaData[10]);
	            shortSaleThresholdIndicator=validateBooleanValue(symbol, "shortSaleThresholdIndicator", symbolMetaData[11]);
	            piggybackExempt=validateBooleanValue(symbol, "piggybackExempt", symbolMetaData[12]);
	            unsolicitedOnly=validateBooleanValue(symbol, "unsolicitedOnly", symbolMetaData[13]);
	            messagingDisabled=validateBooleanValue(symbol, "messagingDisabled", symbolMetaData[14]);
	            saturationEligibleFlag=validateBooleanValue(symbol, "saturationEligibleFlag", symbolMetaData[15]);
	            rule144aFlag=validateBooleanValue(symbol, "rule144aFlag", symbolMetaData[16]);
	            
  	            if(deadSymbolSet!=null&&deadSymbolSet.contains(symbol))
  		        {
  		           if(marketTier.equals(nullValue)&&listingExchange.equals(nullValue))
  		           {
		        	   updateStatement.setString(1,symbol);
		        	   if(updateStatement.executeUpdate()==0)
		        	     NTPLogger.warning("Symbol present In Dead List , Both Market_tier and Listing_exchange values are null , Droped symbol is :"+symbol);
		        	    else
		        	    	NTPLogger.warning("Symbol present In Dead List ,Both Market_tier and Listing_exchange values are null , Upated as dead symbol in data base"+symbol);
		        	   continue;
  		           }
  		        }
  	            else if(marketTier.equals(nullValue)&&listingExchange.equals(nullValue))
		        {
		        	   updateStatement.setString(1,symbol);
		        	   if(updateStatement.executeUpdate()==0)
		        	     NTPLogger.warning("Both Market_tier and Listing_exchange values are null , Droped symbol is :"+symbol);
		        	    else
		        	    	NTPLogger.warning("Both Market_tier and Listing_exchange values are null , Upated as dead symbol in data base"+symbol);
		        	   continue;
		        }
	            
	            insertStatement.setString(1, symbol);
			  	insertStatement.setString(2, productType);
			  	insertStatement.setString(3, currency);
			  	insertStatement.setString(4, country);
			  	insertStatement.setString(5, marketTier);
			  	insertStatement.setString(6, listingExchange);
			  	insertStatement.setString(7, primaryMarketCode);
			  	insertStatement.setBoolean(8, caveatEmptor);
			  	insertStatement.setBoolean(9, BBQuoted);
			  	insertStatement.setString(10, issueName);
			  	insertStatement.setBoolean(11, shortSaleThresholdIndicator);
			  	insertStatement.setBoolean(12, piggybackExempt);
			  	insertStatement.setBoolean(13, unsolicitedOnly);
			  	insertStatement.setBoolean(14, messagingDisabled);
			  	insertStatement.setBoolean(15, saturationEligibleFlag);
			  	insertStatement.setBoolean(16, rule144aFlag);
			  	insertStatement.setDate(17, ipoDate);
			  	insertStatement.addBatch();
			  	
 		  }catch(Exception ex)
 		  {  
 			 NTPLogger.error("Unable to process record for "+symbol);
 			  ex.printStackTrace();
 		  }
		  	if(++count % batchSize == 0) {
		  		insertStatement.executeBatch();
		  	}
		  }
		  insertStatement.executeBatch(); // insert remaining records
		  insertStatement.close();
		  updateStatement.close();
		  aConnection.close();  
		  
	  } 
	  catch (Exception e) 
	  {
//		  System.out.println(recordArray1);
			e.printStackTrace();
	  }
  }
     

  
  public void  updateExchangeChange(ConcurrentHashMap<String, String[]> exchangeChangeMap,Date date)
  {  
	  try 
	  {
		  String sql ="UPDATE STOCK set old_exchange =? ,exchange_change_date=? WHERE symbol=?";
		  aConnection= MDDBManager.getConnection();
		  PreparedStatement ps = aConnection.prepareStatement(sql);

		  final int batchSize = 100;
		  int count = 0;
		  
    	  for(String symbol :exchangeChangeMap.keySet())
    	  { 
    		  
    		  String[] exchanges=exchangeChangeMap.get(symbol);
    		  
    		  try
    		  {  	
    			
    			ps.setString(1, exchanges[1]);  
			  	ps.setDate(2,date);
			  	ps.setString(3, symbol);
			  	
			  	ps.addBatch();
			  	
    		  }catch(Exception ex)
    		  {  
    			  NTPLogger.error("Unable to update exchange change for "+symbol);
    			  ex.printStackTrace();
    		  }
		  	if(++count % batchSize == 0) {
		  		ps.executeBatch();
		  	}
		  	
		  	NTPLogger.info("Updating exchange change for symbol : "+symbol+" Old exchange is : "+exchanges[1]);
		  }
          ps.executeBatch(); // insert remaining records
		  ps.close();
		  aConnection.close();  
		  
	  } 
	  catch (Exception e) 
	  {
			e.printStackTrace();
	  }
  }
  
  public void  updateExchangeChange1(ConcurrentHashMap<String, String> exchangeChangeMap,Date date)
  {  
	  try 
	  {
		  String sql ="UPDATE STOCK set old_exchange =(SELECT ) ,exchange_change_date=? WHERE symbol=?";
		  aConnection= MDDBManager.getConnection();
		  PreparedStatement ps = aConnection.prepareStatement(sql);

		  final int batchSize = 100;
		  int count = 0;
		  
    	  for(String symbol :exchangeChangeMap.keySet())
    	  { 
    		  
//    		  String[] exchanges=exchangeChangeMap.get(symbol);
    		  
    		  try
    		  {  	
    			
//    			ps.setString(1, exchanges[1]);  
			  	ps.setDate(2,date);
			  	ps.setString(3, symbol);
			  	
			  	ps.addBatch();
			  	
    		  }catch(Exception ex)
    		  {  
    			  NTPLogger.error("Unable to update exchange change for "+symbol);
    			  ex.printStackTrace();
    		  }
		  	if(++count % batchSize == 0) {
		  		ps.executeBatch();
		  	}
		  	
//		  	NTPLogger.info("Updating exchange change for symbol : "+symbol+" Old exchange is : "+exchanges[1]);
		  }
          ps.executeBatch(); // insert remaining records
		  ps.close();
		  aConnection.close();  
		  
	  } 
	  catch (Exception e) 
	  {
			e.printStackTrace();
	  }
  }
  
  public void updateDelistedSymbolMetaData(Set<String> set,Date date)
  {  
	  try 
	  {  
		  if(set.size()==0)
			  return;
		  String sql ="UPDATE STOCK set delist_date =? WHERE symbol=?";
		  aConnection= MDDBManager.getConnection();
		  PreparedStatement ps = aConnection.prepareStatement(sql);

		  final int batchSize = 100;
		  int count = 0;
		  
    	  for(String symbol :set)
    	  { 
    		  try
    		  {
			  	ps.setDate(1, date);
			  	ps.setString(2, symbol);
			  	ps.addBatch();
			  	
    		  }catch(Exception ex)
    		  {  
    			 NTPLogger.error("Unable to update delist date for "+symbol);
    			  ex.printStackTrace();
    		  }
		  	if(++count % batchSize == 0) 
		  	{
		  		ps.executeBatch();
		  	}
		  	NTPLogger.info("Updating delist date for symbol : "+symbol);
		  }
    	  ps.executeBatch(); 
		  ps.close();
		  aConnection.close();  
		  
	  } 
	  catch (Exception e) 
	  {
			e.printStackTrace();
	  }
  }
}
