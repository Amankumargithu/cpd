package com.b4utrade.network;

import com.tacpoint.common.*;
import com.tacpoint.util.SystemOutLogger;
import com.b4utrade.helper.StockActivityHelper;
import com.b4utrade.bean.QTMessageBean;

import java.util.*;
import java.io.*;

/**
 * Responsible for inflating quote and trade messages.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2005.  All rights reserved.
 * @version 1.0
 */

public class QTReducedMessageInflator extends DefaultObject implements com.tacpoint.jms.ReducedMessageInflator {

   java.text.DecimalFormat df = new java.text.DecimalFormat("######0.00");

   public Hashtable parseKeyValues(ByteArrayOutputStream baos) {
      Hashtable map = new Hashtable();

      StringTokenizer st = new StringTokenizer(new String(baos.toByteArray()),QTMessageKeys.TUPLE_SEP);

      try {
         while (st.hasMoreTokens()) {
            String tuple = st.nextToken();
            int index = tuple.indexOf(QTMessageKeys.FIELD_SEP);
            map.put(tuple.substring(0,index),tuple.substring(index+1));
         }
      }
      catch (Exception ex) {
    	  System.out.println("Error parsing key values - "+new String(baos.toByteArray()));
      }

      return map;
   }

   public QTMessageBean merge(Hashtable map, QTMessageBean mergeTarget) {

      if (mergeTarget == null) mergeTarget = new QTMessageBean();

      mergeTarget.setSystemTicker((String)map.get(QTMessageKeys.TICKER));
      mergeTarget.setTicker((String)map.get(QTMessageKeys.TICKER));

      if (map.get(QTMessageKeys.BID_SIZE) != null) {
         String bidSize = (String)map.get(QTMessageKeys.BID_SIZE);
         try {
            mergeTarget.setBidSize(Long.parseLong(bidSize));
         }
         catch (Exception e) {}
      }

      if (map.get(QTMessageKeys.ASK_SIZE) != null) {
         String askSize = (String)map.get(QTMessageKeys.ASK_SIZE);
         try {
            mergeTarget.setAskSize(Long.parseLong(askSize));
         }
         catch (Exception e) {}
      }

      if (map.get(QTMessageKeys.BID_PRICE) != null) {
         String bidPrice = (String)map.get(QTMessageKeys.BID_PRICE);
         bidPrice = replaceComma(bidPrice);
         try {
            Double d = new Double(bidPrice);
            mergeTarget.setBidPrice(d.doubleValue());
         }
         catch (Exception e) {}
      }

      if (map.get(QTMessageKeys.ASK_PRICE) != null) {
         String askPrice = (String)map.get(QTMessageKeys.ASK_PRICE);
         askPrice = replaceComma(askPrice);
         try {
            Double d = new Double(askPrice);
            mergeTarget.setAskPrice(d.doubleValue());
         }
         catch (Exception e) {}
      }

      if (map.get(QTMessageKeys.BID_EXCHANGE_CODE) != null)
         mergeTarget.setBidExchangeCode((String)map.get(QTMessageKeys.BID_EXCHANGE_CODE));


      if (map.get(QTMessageKeys.ASK_EXCHANGE_CODE) != null)
         mergeTarget.setAskExchangeCode((String)map.get(QTMessageKeys.ASK_EXCHANGE_CODE));


      if (map.get(QTMessageKeys.DAY_LOW) != null) {
         String dayLow = (String)map.get(QTMessageKeys.DAY_LOW);
         dayLow = replaceComma(dayLow);

         try {
            Double d = new Double(dayLow);
            mergeTarget.setDayLow(d.doubleValue());
         }
         catch (Exception e) {}
      }

      if (map.get(QTMessageKeys.DAY_HIGH) != null) {
         String dayHigh = (String)map.get(QTMessageKeys.DAY_HIGH);
         dayHigh= replaceComma(dayHigh);
         try {
            Double d = new Double(dayHigh);
            mergeTarget.setDayHigh(d.doubleValue());
         }
         catch (Exception e) {}
      }

      if (map.get(QTMessageKeys.CHANGE) != null) {
         String changePrice = (String)map.get(QTMessageKeys.CHANGE);
         changePrice = replaceComma(changePrice);

         try {
            Double d = new Double(changePrice);
            mergeTarget.setChangePrice(d.doubleValue());
         }
         catch (Exception e) {
            System.out.println("in merge, change for ticker: " + map.get(QTMessageKeys.TICKER) + "  is " + changePrice);

	         e.printStackTrace();
	     }
      }

      if (map.get(QTMessageKeys.LAST) != null) {
         String lastPrice = (String)map.get(QTMessageKeys.LAST);
         lastPrice = replaceComma(lastPrice);
         try {
            Double d = new Double(lastPrice);
            String ticker = (String)map.get(QTMessageKeys.TICKER);
            if(ticker.startsWith(".") && !ticker.startsWith(".TIME"))
            	mergeTarget.setLastPrice(d.doubleValue());
            else if (d.doubleValue() > 0.00001d)
              mergeTarget.setLastPrice(d.doubleValue());
         }
         catch (Exception e) {
            System.out.println("in merge, Last price for ticker: " + map.get(QTMessageKeys.TICKER) + " is" + lastPrice);
	        e.printStackTrace();
	     }
      }

      if (map.get(QTMessageKeys.VOLUME) != null) {
         String volume = (String)map.get(QTMessageKeys.VOLUME);
         try {
            mergeTarget.setVolume(Long.parseLong(volume));
         }
         catch (Exception e) {}
      }

      if (map.get(QTMessageKeys.TRADE_VOLUME) != null) {
         String lastTradeVolume = (String)map.get(QTMessageKeys.TRADE_VOLUME);
         try {
            mergeTarget.setLastTradeVolume(Long.parseLong(lastTradeVolume));
         }
         catch (Exception e) {}
      }
      if (map.get(QTMessageKeys.OPEN_PRICE) != null){
         String openPrice = (String)map.get(QTMessageKeys.OPEN_PRICE);
         openPrice = replaceComma(openPrice);
         try {
            mergeTarget.setOpenPrice(Double.parseDouble(openPrice));
         }
         catch (Exception e) {}
      }

      if (map.get(QTMessageKeys.MARKET_CENTER_CODE) != null)
         mergeTarget.setMarketCenter((String)map.get(QTMessageKeys.MARKET_CENTER_CODE));

      if (map.get(QTMessageKeys.VWAP_CODE) != null)
         mergeTarget.setVWAP((String)map.get(QTMessageKeys.VWAP_CODE));     
      
      if (map.get(QTMessageKeys.LAST_CLOSED_PRICE) != null) {
    	  try {
    		  double lcp = Double.parseDouble((String)map.get(QTMessageKeys.LAST_CLOSED_PRICE));
    		  mergeTarget.setLastClosedPrice(lcp);
    	  }
    	  catch (Exception e) {}
      }

      try {
         double prevClose = mergeTarget.getLastClosedPrice();
         double change = mergeTarget.getChangePrice();

         if (prevClose > 0.0d)
           mergeTarget.setPercentChange(change / prevClose * 100);
      }
      catch (Exception e) {
//	     SystemOutLogger.error("Error in QTReducedMessageInflator - "+e.getMessage());
//	     e.printStackTrace();
      }

      if (map.get(QTMessageKeys.TRADE_DATE) != null)
         mergeTarget.setTradeDate((String)map.get(QTMessageKeys.TRADE_DATE));


      if (map.get(QTMessageKeys.TRADE_TIME) != null)
         mergeTarget.setLastTradeTime((String)map.get(QTMessageKeys.TRADE_TIME));

      
      if (map.get(QTMessageKeys.SETTLEMENT_PRICE) != null) {
    	  try {
    		  double sp = Double.parseDouble((String)map.get(QTMessageKeys.SETTLEMENT_PRICE));
    		  mergeTarget.setSettlementPrice(sp);
    	  }
    	  catch (Exception e) {}
      }
      
      if (map.get(QTMessageKeys.SHORT_SALE_RESTRICTION_IND) != null) {    	  
    	  try {
    		  String ssri = (String)map.get(QTMessageKeys.SHORT_SALE_RESTRICTION_IND);
    		  mergeTarget.setTickFlag(Boolean.parseBoolean(ssri));
    	  }
    	  catch (Exception e) {}    	  
      }
      
      if (map.get(QTMessageKeys.HALT_IND) != null) {    	  
    	  try {
    		  String halt = (String)map.get(QTMessageKeys.HALT_IND);
    		  mergeTarget.setUnabridged(Boolean.parseBoolean(halt));
    	  }
    	  catch (Exception e) {}    	  
      }
      
      if (map.get(QTMessageKeys.LIMIT_UP_DOWN) != null) {    	  
    	  try {
    		  String limitUpDown = (String)map.get(QTMessageKeys.LIMIT_UP_DOWN);
    		  mergeTarget.setLimitUpDown(limitUpDown);
    	  }
    	  catch (Exception e) {}    	  
      }
         
      return mergeTarget;
   }

   public StockActivityHelper merge(Hashtable map, StockActivityHelper mergeTarget) {

      if (mergeTarget == null) mergeTarget = new StockActivityHelper();

      mergeTarget.setTicker((String)map.get(QTMessageKeys.TICKER));

      if (map.get(QTMessageKeys.BID_SIZE) != null)
         mergeTarget.setBidSize((String)map.get(QTMessageKeys.BID_SIZE));

      if (map.get(QTMessageKeys.ASK_SIZE) != null)
         mergeTarget.setAskSize((String)map.get(QTMessageKeys.ASK_SIZE));

      if (map.get(QTMessageKeys.BID_PRICE) != null)
         mergeTarget.setBidPrice((String)map.get(QTMessageKeys.BID_PRICE));

      if (map.get(QTMessageKeys.ASK_PRICE) != null)
         mergeTarget.setAskPrice((String)map.get(QTMessageKeys.ASK_PRICE));

      if (map.get(QTMessageKeys.BID_EXCHANGE_CODE) != null)
         mergeTarget.setBidExchangeCode((String)map.get(QTMessageKeys.BID_EXCHANGE_CODE));

      if (map.get(QTMessageKeys.ASK_EXCHANGE_CODE) != null)
         mergeTarget.setAskExchangeCode((String)map.get(QTMessageKeys.ASK_EXCHANGE_CODE));

      if (map.get(QTMessageKeys.DAY_LOW) != null)
         mergeTarget.setDayLow((String)map.get(QTMessageKeys.DAY_LOW));

      if (map.get(QTMessageKeys.DAY_HIGH) != null)
         mergeTarget.setDayHigh((String)map.get(QTMessageKeys.DAY_HIGH));

      if (map.get(QTMessageKeys.CHANGE) != null)
         mergeTarget.setChangePrice((String)map.get(QTMessageKeys.CHANGE));

      if (map.get(QTMessageKeys.LAST) != null)
         mergeTarget.setLastPrice((String)map.get(QTMessageKeys.LAST));

      if (map.get(QTMessageKeys.VOLUME) != null)
         mergeTarget.setVolume((String)map.get(QTMessageKeys.VOLUME));

      if (map.get(QTMessageKeys.TRADE_VOLUME) != null)
         mergeTarget.setLastTradeVolume((String)map.get(QTMessageKeys.TRADE_VOLUME));

      if (map.get(QTMessageKeys.OPEN_PRICE) != null)
         mergeTarget.setOpenPrice((String)map.get(QTMessageKeys.OPEN_PRICE));

      if (map.get(QTMessageKeys.MARKET_CENTER_CODE) != null)
         mergeTarget.setMarketCenter((String)map.get(QTMessageKeys.MARKET_CENTER_CODE));

      if (map.get(QTMessageKeys.VWAP_CODE) != null)
         mergeTarget.setVWAP((String)map.get(QTMessageKeys.VWAP_CODE));     
      
      if (map.get(QTMessageKeys.LAST_CLOSED_PRICE) != null)
    	  mergeTarget.setPreviousPrice((String)map.get(QTMessageKeys.LAST_CLOSED_PRICE));
 
      if (map.get(QTMessageKeys.SETTLEMENT_PRICE) != null)
    	  mergeTarget.setPreviousPrice((String)map.get(QTMessageKeys.SETTLEMENT_PRICE));
      
      // need to calculate percent change ourselves!

      try {
         Double prevClose = new Double(mergeTarget.getPreviousPrice());
         Double change = new Double(mergeTarget.getChangePrice());

         mergeTarget.setPercentChange(df.format(change.doubleValue() / prevClose.doubleValue() * 100));
      }
      catch (Exception e) {
//	     SystemOutLogger.error("Error in QTReducedMessageInflator - "+e.getMessage());
//	     e.printStackTrace();
      }

      
      if (map.get(QTMessageKeys.TRADE_DATE) != null)
          mergeTarget.setLastTradeDateGMT((String)map.get(QTMessageKeys.TRADE_DATE));


       if (map.get(QTMessageKeys.TRADE_TIME) != null)
          mergeTarget.setLastTradeTimeGMT((String)map.get(QTMessageKeys.TRADE_TIME));
       
       if (map.get(QTMessageKeys.SHORT_SALE_RESTRICTION_IND) != null) {    	  
     	  try {
     		  String ssri = (String)map.get(QTMessageKeys.SHORT_SALE_RESTRICTION_IND);
     		  mergeTarget.setRestricted(Boolean.getBoolean(ssri));
     	  }
     	  catch (Exception e) {}    	  
       }       
       
       return mergeTarget;
   }

   public Object getTopic(ByteArrayOutputStream baos) {
      Hashtable map = parseKeyValues(baos);
      return map.get(QTMessageKeys.TICKER);
   }

   private String replaceComma(String data) {

	   char COMMA = ',';

	   if (data.indexOf(COMMA) < 0) return data;

	   int length = data.length();

	   char[] output = new char[length];
	   char[] input = data.toCharArray();

	   int j = 0;

	   for (int i=0; i<length; i++) {
		   if (input[i] != COMMA) {
			   output[j++] = input[i];
		   }
	   }

	   return new String(output,0,j);

   }
}
