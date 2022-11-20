package com.b4utrade.network;

import com.tacpoint.common.*;
import com.tacpoint.util.SystemOutLogger;
import com.b4utrade.helper.StockActivityHelper;
import com.b4utrade.network.QTMessageKeys;
import com.tacpoint.jms.MessageReducer;

import java.util.*;
import java.io.*;

/**
 * Responsible for reducing and inflating quote and trade messages.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2005.  All rights reserved.
 * @version 1.0
 */

public class QTMessageReducer extends DefaultObject implements MessageReducer {

	private StockActivityHelper sah = new StockActivityHelper();

   public MessageReducer cloneMe() {
	   return new QTMessageReducer();
   }
   
   private HashMap compactionCache = new HashMap();

   public HashMap parseKeyValues(ByteArrayOutputStream baos) {
      HashMap map = new HashMap();

      StringTokenizer st = new StringTokenizer(new String(baos.toByteArray()),QTMessageKeys.TUPLE_SEP);
      while (st.hasMoreTokens()) {
         String tuple = st.nextToken();
         int index = tuple.indexOf(QTMessageKeys.FIELD_SEP);
         map.put(tuple.substring(0,index),tuple.substring(index+1));
      }
      return map;
   }

   public void conflateReducedMessages(ByteArrayOutputStream baos, Map existing, Map additions) {

      Iterator it = additions.keySet().iterator();
      while (it.hasNext()) {
         Object key = it.next();
         existing.put(key,additions.get(key));
      }

      try {
         baos.reset();
         
         baos.write(QTMessageKeys.TICKER.getBytes());
         baos.write(QTMessageKeys.FIELD_SEP.getBytes());
         baos.write(((String)existing.get(QTMessageKeys.TICKER)).getBytes());

         existing.remove(QTMessageKeys.TICKER);

         it = existing.keySet().iterator();

         while (it.hasNext()) {
            String key   = (String)it.next();
            String value = (String)existing.get(key);
            baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
            baos.write(key.getBytes());
            baos.write(QTMessageKeys.FIELD_SEP.getBytes());
            baos.write(value.getBytes());
         }
 
         baos.write(com.tacpoint.publisher.TConstants.TERMINATOR_BYTE);

         // **AK **
         /*
         String ticker = (String)additions.get(QTMessageKeys.TICKER);
         if (ticker != null) {
            if (ticker.equals("EGN") || 
                ticker.equals("END") || 
                ticker.equals("EP")  || 
                ticker.equals("EPD") || 
                ticker.equals("EPE") ) {

               SystemOutLogger.debug(new java.util.Date()+" QTMessageReducer conflated stream - "+new String(baos.toByteArray()));
            }
         }
         */

      }
      catch (Exception e) {
         SystemOutLogger.error("Unable to conflate messages - "+e.getMessage());
         e.printStackTrace();
         baos = null;
      }
   }


   public ByteArrayOutputStream reduceMessage(String ticker, byte[] message) {

      if (compactionCache == null) {
         SystemOutLogger.debug("Compaction cache map is null.");
         return null;
      }

      StockActivityHelper sah = (StockActivityHelper)compactionCache.get(ticker);
      
      return buildCompactedMessage(ticker, message, sah);
      
   }

   private ByteArrayOutputStream buildCompactedMessage(String ticker, byte[] message, StockActivityHelper prevMessage) {

      try {


         boolean doSend = false;

         ByteArrayOutputStream baos = new ByteArrayOutputStream();

//         StockActivityHelper sah = new StockActivityHelper();

         sah.setStockStream(message);
//         if (ticker.indexOf(QTMessageKeys.OPTIONS_MASK) > -1 || ticker.indexOf(QTMessageKeys.FUTURES_MASK) > -1 || ticker.indexOf("/") > -1) {
// 
//            sah.setIsOption(true);
//         }

         //Set true in case u are getting feed from CTF CPD
         sah.setIsOption(true);
         sah.init();

         // start comparisons ...

         if (prevMessage == null) {
            doSend = true;
			prevMessage = new StockActivityHelper();
       	  	compactionCache.put(ticker,prevMessage);
           // prevMessage = sah;
         }
       

         baos.write(QTMessageKeys.TICKER.getBytes());
         baos.write(QTMessageKeys.FIELD_SEP.getBytes());
         baos.write(ticker.getBytes());
         
         if ( prevMessage.getBidSize() == null || !prevMessage.getBidSize().equals(sah.getBidSize())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.BID_SIZE.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getBidSize().getBytes());
             prevMessage.setBidSize(sah.getBidSize());
             doSend = true;
          }

          if ( prevMessage.getAskSize() == null || !prevMessage.getAskSize().equals(sah.getAskSize())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.ASK_SIZE.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getAskSize().getBytes());
             prevMessage.setAskSize(sah.getAskSize());
             doSend = true;
          }

          if ( prevMessage.getAskPrice() == null || !prevMessage.getAskPrice().equals(sah.getAskPrice())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.ASK_PRICE.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getAskPrice().getBytes());
             prevMessage.setAskPrice(sah.getAskPrice());
             doSend = true;
          }

          if ( prevMessage.getBidPrice() == null || !prevMessage.getBidPrice().equals(sah.getBidPrice())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.BID_PRICE.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getBidPrice().getBytes());
             prevMessage.setBidPrice(sah.getBidPrice());
             doSend = true;
          }

          if ( prevMessage.getAskExchangeCode() == null || !prevMessage.getAskExchangeCode().equals(sah.getAskExchangeCode())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.ASK_EXCHANGE_CODE.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getAskExchangeCode().getBytes());
             prevMessage.setAskExchangeCode(sah.getAskExchangeCode());
             doSend = true;
          }

          if ( prevMessage.getBidExchangeCode() == null || !prevMessage.getBidExchangeCode().equals(sah.getBidExchangeCode())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.BID_EXCHANGE_CODE.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getBidExchangeCode().getBytes());
             prevMessage.setBidExchangeCode(sah.getBidExchangeCode());
             doSend = true;
          }

          if ( prevMessage.getDayLow() == null || !prevMessage.getDayLow().equals(sah.getDayLow())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.DAY_LOW.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getDayLow().getBytes());
             prevMessage.setDayLow(sah.getDayLow());
             doSend = true;
          }

          if ( prevMessage.getDayHigh() == null || !prevMessage.getDayHigh().equals(sah.getDayHigh())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.DAY_HIGH.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getDayHigh().getBytes());
             prevMessage.setDayHigh(sah.getDayHigh());
             doSend = true;
          }

          if ( prevMessage.getChangePrice() == null || !prevMessage.getChangePrice().equals(sah.getChangePrice())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.CHANGE.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getChangePrice().getBytes());
             prevMessage.setChangePrice(sah.getChangePrice());
             doSend = true;
          }

          if ( prevMessage.getLastPrice() == null || !prevMessage.getLastPrice().equals(sah.getLastPrice())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.LAST.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getLastPrice().getBytes());
             prevMessage.setLastPrice(sah.getLastPrice());
             doSend = true;
          }

          if ( prevMessage.getVolume() == null || !prevMessage.getVolume().equals(sah.getVolume())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.VOLUME.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getVolume().getBytes());
             prevMessage.setVolume(sah.getVolume());
             doSend = true;
          }

          if ( prevMessage.getLastTradeVolume() == null || !prevMessage.getLastTradeVolume().equals(sah.getLastTradeVolume())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.TRADE_VOLUME.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getLastTradeVolume().getBytes());
             prevMessage.setLastTradeVolume(sah.getLastTradeVolume());
             doSend = true;
          }

          if ( prevMessage.getOpenPrice() == null || !prevMessage.getOpenPrice().equals(sah.getOpenPrice())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.OPEN_PRICE.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getOpenPrice().getBytes());
             prevMessage.setOpenPrice(sah.getOpenPrice());
             doSend = true;
          }

          if ( prevMessage.getMarketCenter() == null || !prevMessage.getMarketCenter().equals(sah.getMarketCenter())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.MARKET_CENTER_CODE.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getMarketCenter().getBytes());
             prevMessage.setMarketCenter(sah.getMarketCenter());
             doSend = true;
          }

          if ( prevMessage.getVWAP() == null || !prevMessage.getVWAP().equals(sah.getVWAP())) {
             baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
             baos.write(QTMessageKeys.VWAP_CODE.getBytes());
             baos.write(QTMessageKeys.FIELD_SEP.getBytes());
             baos.write(sah.getVWAP().getBytes());
             prevMessage.setVWAP(sah.getVWAP());
             doSend = true;
          }

          if ( prevMessage.getLastTradeDateGMT() == null || !prevMessage.getLastTradeDateGMT().equals(sah.getLastTradeDateGMT())) {
              baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
              baos.write(QTMessageKeys.TRADE_DATE.getBytes());
              baos.write(QTMessageKeys.FIELD_SEP.getBytes());
              baos.write(sah.getLastTradeDateGMT().getBytes());
              prevMessage.setLastTradeDateGMT(sah.getLastTradeDateGMT());
              doSend = true;
          }
          
          if ( prevMessage.getLastTradeTimeGMT() == null || !prevMessage.getLastTradeTimeGMT().equals(sah.getLastTradeTimeGMT())) {
              baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
              baos.write(QTMessageKeys.TRADE_TIME.getBytes());
              baos.write(QTMessageKeys.FIELD_SEP.getBytes());
              baos.write(sah.getLastTradeTimeGMT().getBytes());
              prevMessage.setLastTradeTimeGMT(sah.getLastTradeTimeGMT());
              doSend = true;
          }
   
          if ( prevMessage.getPreviousPrice() == null || !prevMessage.getPreviousPrice().equals(sah.getPreviousPrice())) {
              baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
              baos.write(QTMessageKeys.LAST_CLOSED_PRICE.getBytes());
              baos.write(QTMessageKeys.FIELD_SEP.getBytes());
              baos.write(sah.getPreviousPrice().getBytes());
              prevMessage.setPreviousPrice(sah.getPreviousPrice());
              doSend = true;
          }
          
          if ( prevMessage.getSettlementPrice() == null || !prevMessage.getSettlementPrice().equals(sah.getSettlementPrice())) {
              baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
              baos.write(QTMessageKeys.SETTLEMENT_PRICE.getBytes());
              baos.write(QTMessageKeys.FIELD_SEP.getBytes());
              baos.write(sah.getSettlementPrice().getBytes());
              prevMessage.setSettlementPrice(sah.getSettlementPrice());
              doSend = true;
          }
          
  
          if ( prevMessage == null || prevMessage.getRestricted() != sah.getRestricted()) {
              baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
              baos.write(QTMessageKeys.SHORT_SALE_RESTRICTION_IND.getBytes());
              baos.write(QTMessageKeys.FIELD_SEP.getBytes());
              baos.write(Boolean.toString(sah.getRestricted()).getBytes());
              prevMessage.setRestricted(sah.getRestricted());
              doSend = true;
          }
          
          //Used for Halt flag
          if ( prevMessage == null || prevMessage.isParseOk() != sah.isParseOk()) {
              baos.write(QTMessageKeys.TUPLE_SEP.getBytes());
              baos.write(QTMessageKeys.SHORT_SALE_RESTRICTION_IND.getBytes());
              baos.write(QTMessageKeys.FIELD_SEP.getBytes());
              baos.write(Boolean.toString(sah.getRestricted()).getBytes());
              prevMessage.setIsParseOk(sah.isParseOk());
              doSend = true;
          }
         
//         compactionCache.put(ticker,sah);

         // **AK **
         /*
         if (sah.getTicker() != null) {
            if (sah.getTicker().equals("EGN") || 
                sah.getTicker().equals("END") || 
                sah.getTicker().equals("EP")  || 
                sah.getTicker().equals("EPD") || 
                sah.getTicker().equals("EPE") ) {

               SystemOutLogger.debug(new java.util.Date()+" QTMessageReducer - ticker : "+ sah.getTicker()+
                                  " last : "+sah.getLastPrice()+
                                  " volume : "+sah.getVolume()+
                                  " doSend : "+doSend);
               SystemOutLogger.debug(new java.util.Date()+" QTMessageReducer stream - "+new String(baos.toByteArray()));
            }
         }
         */

         if (doSend)  
            return baos;
         else
            return null;

      }
      catch (Exception e) {
         SystemOutLogger.error("Unable to build compaction string.");
         e.printStackTrace();
         return null;
      }
   }

   public void resetAttribute(String attributeKey, String attributeValue) {

      if (attributeKey == null) return;

      Iterator it = compactionCache.values().iterator();
      while (it.hasNext()) {
         StockActivityHelper sah = (StockActivityHelper)it.next();

         // currently, only the open price would need to be reset
         // but if other resets are needed, this is where the 
         // reset should be placed.
         
         if (attributeKey.equals(QTMessageKeys.OPEN_PRICE))
            sah.setOpenPrice(attributeValue);
         if (attributeKey.equals(QTMessageKeys.LAST_CLOSED_PRICE))
             sah.setPreviousPrice(attributeValue);
         if (attributeKey.equals(QTMessageKeys.SETTLEMENT_PRICE))
             sah.setSettlementPrice(attributeValue);
            
      }
   }
}
