/*
 *  StockRetriever.java
 *
 *  Copyright (c) 2003 Tacpoint Technologies, Inc.
 *  All Rights Reserved.
 */
package com.b4utrade.web.helper;

import com.tacpoint.common.DefaultObject;
import com.tacpoint.http.HttpConfiguration;
import com.tacpoint.http.HttpRequestExecutor;
import com.tacpoint.exception.BusinessException;
import com.tacpoint.util.Environment;

import com.b4utrade.helper.StockActivityHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;


/**
 * This class retrieves stock quotes from B4Utrade's quote server.
 */
public class StockRetriever extends DefaultObject
{

   static Log log = LogFactory.getLog(StockRetriever.class);


   /**
    * Retrieves all the quotes for the given stock list from the quote server.
    *
    * @param stockList  Comma separated list of ticker symbols.
    * @return Hashtable of StockActivityHelper objects containing the stock quotes, keyed by ticker symbol.
    * @exception BusinessException
    */
   public static Hashtable getStockQuotes(String stockList) throws BusinessException
   {
      if (stockList == null || stockList.length() == 0)
         throw new BusinessException("StockRetriever.getStockQuotes(): stock list parameter may not be blank.");

      HttpRequestExecutor reqEx = new HttpRequestExecutor();
      ByteArrayInputStream bais = null;
      ObjectInputStream ois = null;
      Hashtable resultsHash = null;

      try {
         Environment.init();
         String quoteURL = (String)Environment.get("QUOTE_SERVER_URL");
         if (quoteURL == null || quoteURL.length() == 0)
            throw new Exception("Unable to retrieve the Quote Server URL from the properties file.");

         quoteURL += "?COMPANYID=";
         quoteURL += stockList;
         HttpConfiguration hc = new HttpConfiguration();
         hc.setURL(quoteURL);
         System.out.println("URL for exchange: "+quoteURL);
         reqEx.execute(hc);  // throws BusinessException
         byte[] results = reqEx.getResults();
         if (results == null) {
            log.info("StockRetriever.getStockQuotes(): no results retrieve for stock list="+stockList);
            return null;
         }

         bais = new ByteArrayInputStream(results);
         ois = new ObjectInputStream(bais);
         resultsHash = (Hashtable)ois.readObject();

         if (resultsHash != null && resultsHash.size() > 0) {
            for (Enumeration en = resultsHash.elements(); en.hasMoreElements(); ) {
               StockActivityHelper sa = (StockActivityHelper)en.nextElement();
               sa.init();
               log.debug("Ticker="+sa.getTicker()+", price="+sa.getLastPrice()+", previous day's price="+sa.getPreviousPrice()+", % change="+sa.getPercentChange());
//System.out.println("Ticker="+sa.getTicker()+", price="+sa.getLastPrice()+", previous day's price="+sa.getPreviousPrice()+", % change="+sa.getPercentChange());
              // System.out.println("Ticker="+sa.getExchange());
            }
         }

      }
      catch(BusinessException be) {
         log.error("StockRetriever.getStockQuotes(): exception occurred when trying to retrieve quotes from the quote server. " + be.getMessage());
         throw new BusinessException("StockRetriever.getStockQuotes(): Quote retrieval exception. ", be);
      }
      catch (Exception e) {
         log.error("StockRetriever.getStockQuotes(): " + e.getMessage());
         throw new BusinessException("StockRetriever.getStockQuotes(): exception occurred. ", e);
      }
      finally {
         try {
            if (bais != null) bais.close();
         } catch(Exception e) {}

         try {
            if (ois != null) ois.close();
         } catch(Exception e) {}
      }

      return resultsHash;
   }


	////////////////////////////////////////////////////////////////////////////
	// T E S T I N G
	////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		boolean vOK  = false;

		try
		{
		   StockRetriever.getStockQuotes("IBM,YHOO,ORCL");

		}
		catch(Exception e)
		{
			System.out.println("StockRetriever.main(): " + e.getMessage());
		}
	}

}
