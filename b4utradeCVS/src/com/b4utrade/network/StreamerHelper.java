package com.b4utrade.network;

import com.tacpoint.common.*;
import com.tacpoint.util.SystemOutLogger;
import com.b4utrade.helper.StockActivityHelper;
import com.b4utrade.helper.StockNewsUpdateHelper;
import com.b4utrade.bean.QTMessageBean;

import java.util.*;
import java.io.*;

/**
 * Responsible for inflating quote and trade messages.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2005.  All rights reserved.
 * @version 1.0
 */

public class StreamerHelper extends DefaultObject {

   public Vector createNewsMsg(Hashtable map) {
      if (map == null)
        return null;
      SystemOutLogger.debug("In create news");
      StockNewsUpdateHelper snuh = new StockNewsUpdateHelper();
      if (map.get(QTMessageKeys.NEWS_ID) != null) {
         String newsId = (String)map.get(QTMessageKeys.NEWS_ID);
         try {
            snuh.setLastNewsID(Long.parseLong(newsId));
         }
         catch (Exception e) {
			SystemOutLogger.error("unable to parse news id");
	        e.printStackTrace();
	     }
      }
      if (map.get(QTMessageKeys.NEWS_HEADLINE) != null) {
         String newsHeadLine = (String)map.get(QTMessageKeys.NEWS_HEADLINE);
         try {
            snuh.setLastNews(newsHeadLine);
         }
         catch (Exception e){}
      }
      if (map.get(QTMessageKeys.NEWS_TICKERS) != null) {
         String newsTickers = (String)map.get(QTMessageKeys.NEWS_TICKERS);
         try {
         /* KC * comment out to parse the tickers and set to vectors of tickers

			StringTokenizer st = new StringTokenizer(newsTickers, ",");
            Vector tickersVec = new Vector();
            while (st.hasMoreTokens()) {
               String sym = st.nextToken();
               if (sym != null)
                 tickersVec.addElement(sym);

		    }
		    snuh.setTickers(tickersVec);
         * end comment */
		 snuh.setTicker(newsTickers);

         }
         catch (Exception e) {
			SystemOutLogger.error("unable to parse tickers");
	        e.printStackTrace();
	     }
      }
      if (map.get(QTMessageKeys.NEWS_CATEGORIES) != null) {
         String categories = (String)map.get(QTMessageKeys.NEWS_CATEGORIES);
         try {

		 snuh.setCategoriesAsString(categories);

         }
         catch (Exception e) {
			SystemOutLogger.error("unable to parse categories");
	        e.printStackTrace();
	     }
      }
      if (map.get(QTMessageKeys.NEWS_DATE) != null) {
         String date = (String)map.get(QTMessageKeys.NEWS_DATE);
         try {
            snuh.setLastNewsDate(date);

         }
         catch (Exception e) {}
      }
      if (map.get(QTMessageKeys.NEWS_SOURCE) != null) {
         String source = (String)map.get(QTMessageKeys.NEWS_SOURCE);
         try {
            snuh.setLastNewsSource(source);
         }
         catch (Exception e) {}
      }

       if (map.get(QTMessageKeys.NEWS_VENDOR) != null) {
          String source = (String)map.get(QTMessageKeys.NEWS_VENDOR);
          try {
             snuh.setDowJones(source);
          }
          catch (Exception e) {}
       }
      
      Vector v= new Vector();

      if (v.size() == 0)
         v.addElement(snuh);
      //SystemOutLogger.debug("List size(): " + v.size());

      return v;
   }

   //temp method
   public StockActivityHelper cloneStockActivityHelper(StockActivityHelper sah) {
       StockActivityHelper newStock = new StockActivityHelper();
       newStock.setTicker(sah.getTicker());
       newStock.setAlert(sah.getAlert());
       newStock.setAskPrice(sah.getAskPrice());
       newStock.setAskSize(sah.getAskSize());
       newStock.setBidPrice(sah.getBidPrice());
       newStock.setBidSize(sah.getBidSize());
       newStock.setChangePrice(sah.getChangePrice());
       newStock.setDayHigh(sah.getDayHigh());
       newStock.setDayLow(sah.getDayLow());
       newStock.setDownTick(sah.getDownTick());
       newStock.setExchange(sah.getExchange());
       newStock.setLastPrice(sah.getLastPrice());
       newStock.setLastTradeVolume(sah.getLastTradeVolume());
       newStock.setOpenPrice(sah.getOpenPrice());
       newStock.setPercentChange(sah.getPercentChange());
       newStock.setPreviousPrice(sah.getPreviousPrice());
       newStock.setRegion(sah.getRegion());
       newStock.setRestricted(sah.getRestricted());
       newStock.setUpdate(sah.getUpdate());
       newStock.setUpTick(sah.getUpTick());
       newStock.setVolume(sah.getVolume());

       newStock.setBidExchangeCode(sah.getBidExchangeCode());
       newStock.setAskExchangeCode(sah.getAskExchangeCode());
       newStock.setMarketCenter(sah.getMarketCenter());
       newStock.setVWAP(sah.getVWAP());

       return newStock;
   }


}
