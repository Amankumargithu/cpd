/**
 * TSQMessageMerger.java
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2006.  All rights reserved.
 * @version 1.0
 */

package com.b4utrade.tsq;

import com.b4utrade.bean.TSQBean;

import java.util.*;

/**
 *   The object for associating ticker symbols with the respective table names.
 */
public class TSQMessageMerger
{

   public static List<TSQBean> merge(List dailyMessages, List correctedMessages) {

	  TSQMessageSequenceComparator msc = new TSQMessageSequenceComparator();
	  msc.setDirection(TSQMessageSequenceComparator.DESCENDING);
	  
	  if (dailyMessages != null) {
		  Collections.sort(dailyMessages, msc);  
	  }
	   	   
      if (correctedMessages == null || correctedMessages.size() == 0) return dailyMessages;
      
      msc.setDirection(TSQMessageSequenceComparator.ASCENDING);

      Collections.sort(correctedMessages, msc);

      Map dailyMap = mapDailyTradeMessages(dailyMessages);

      Iterator it = correctedMessages.iterator();
      while (it.hasNext()) {

         TSQBean cbean = (TSQBean)it.next();

         /**AK**/
    
         System.out.println("TSQMessageMerger.merge - cancelled bean ticker : "+cbean.getTicker()+
                            " trade market center : "+cbean.getTradeMarketCenter()+
                            " trade sequence : "+cbean.getTradeSequence());

        
         /**AK**/
         TSQBean dbean = (TSQBean)dailyMap.get(buildKey(cbean.getTradeMarketCenter(),cbean.getTradeSequence()));

       
         // check to see if the daily bean is contained in our list ...
         if (dbean == null) {
            //System.out.println("TSQMessageMerger.merge - search for daily bean returned null.");
            continue;
         }

         dbean = findYoungestChild(dbean);


         // cancelled trade
         if (cbean.getTradeCancelIndicator().equals(TSQBean.CANCELLED_TRADE)) {
            dbean.setTradeCancelIndicator(TSQBean.CANCELLED_TRADE);

            System.out.println("TSQMessageMerger.merge - cancelling orig record - "+dbean.getTicker()+
                               " trade market center : "+dbean.getTradeMarketCenter()+
                               " trade sequence : "+dbean.getTradeSequence()+
                               " cancel ind : "+dbean.getTradeCancelIndicator());
         }   

         // corrected trade
         else {
            cbean.setTradeQuoteTime(dbean.getTradeQuoteTime());
            dbean.setChild(cbean);
            System.out.println("TSQMessageMerger.merge - correcting orig record - "+dbean.getTicker()+
                               " trade market center : "+dbean.getTradeMarketCenter()+
                               " trade sequence : "+dbean.getTradeSequence()+
                               " cancel ind : "+dbean.getTradeCancelIndicator());
         }
      }

      // finally we need to flatten out any added children ...

      ArrayList results = new ArrayList();
      it = dailyMessages.iterator();
      while (it.hasNext()) {
         TSQBean bean  = (TSQBean)it.next();
         results.add(bean);
         while (bean.getChild() != null) {
            bean = bean.getChild();
            results.add(bean);
         }
      }

      return results;
   }

   private static TSQBean findYoungestChild(TSQBean bean) {

      while (bean.getChild() != null) {
         bean = bean.getChild();
      }

      return bean;
   }
      
   private static String buildKey(String tradeMarketCenter, Long tradeSequence) {
      return new String(tradeMarketCenter+tradeSequence.toString());
   }

   private static Map mapDailyTradeMessages(List dailyMessages) {
      HashMap results = new HashMap();
      Iterator it = dailyMessages.iterator();
      while (it.hasNext()) {
         TSQBean bean = (TSQBean)it.next();

         if (bean.getMessageType().equals(TSQBean.TYPE_TRADE)) {

            String key = buildKey(bean.getTradeMarketCenter(),bean.getTradeSequence());
            results.put(key,bean);
         }
      }
      return results;
   }
      

   public static void main(String[] args) {
 
      // for unit testing only!

      ArrayList dlist = new ArrayList();
      
      TSQBean bean1 = new TSQBean();
      bean1.setTicker("TACPOINT");
      bean1.setTradeCancelIndicator(TSQBean.NON_CANCELLED_TRADE);
      bean1.setMsgSequence(new Long(100));
      bean1.setMessageType(TSQBean.TYPE_TRADE);
      bean1.setTradeMarketCenter("p");
      bean1.setTradeSequence(new Long(10));
      bean1.setTradePrice(new Double(25.6700));
      bean1.setTradeSize(new Long(200));
      bean1.setTradeQuoteTime(new Integer(140012));
      dlist.add(bean1);

      TSQBean bean2 = new TSQBean();
      bean2.setTicker("TACPOINT");
      bean2.setTradeCancelIndicator(TSQBean.NON_CANCELLED_TRADE);
      bean2.setMsgSequence(new Long(101));
      bean2.setMessageType(TSQBean.TYPE_TRADE);
      bean2.setTradeMarketCenter("m");
      bean2.setTradeSequence(new Long(10));
      bean2.setTradePrice(new Double(25.6701));
      bean2.setTradeSize(new Long(300));
      bean2.setTradeQuoteTime(new Integer(140012));
      dlist.add(bean2);

      TSQBean bean3 = new TSQBean();
      bean3.setTicker("TACPOINT");
      //bean3.setTradeCancelIndicator(TSQBean.NON_CANCELLED_TRADE);
      bean3.setMsgSequence(new Long(104));
      bean3.setMessageType(TSQBean.TYPE_COMPOSITE_QUOTE);
      bean3.setBidMarketCenter("b");
      //bean3.setTradeSequence(10);
      bean3.setBidPrice(new Double(25.6600));
      bean3.setBidSize(new Long(500));
      bean3.setTradeQuoteTime(new Integer(140012));
      dlist.add(bean3);

      TSQBean bean4 = new TSQBean();
      bean4.setTicker("TACPOINT");
      bean4.setTradeCancelIndicator(TSQBean.NON_CANCELLED_TRADE);
      bean4.setMsgSequence(new Long(105));
      bean4.setMessageType(TSQBean.TYPE_TRADE);
      bean4.setTradeMarketCenter("c");
      bean4.setTradeSequence(new Long(11));
      bean4.setTradePrice(new Double(25.6800));
      bean4.setTradeSize(new Long(700));
      bean4.setTradeQuoteTime(new Integer(140012));
      dlist.add(bean4);

      ArrayList clist = new ArrayList();
      
      TSQBean bean5 = new TSQBean();
      bean5.setTicker("TACPOINT");
      bean5.setTradeCancelIndicator(TSQBean.CANCELLED_TRADE);
      bean5.setMsgSequence(new Long(102));
      bean5.setMessageType(TSQBean.TYPE_TRADE);
      bean5.setTradeMarketCenter("m");
      bean5.setTradeSequence(new Long(10));
      bean5.setTradePrice(new Double(25.6701));
      bean5.setTradeSize(new Long(300));
      //bean5.setTradeQuoteTime(new java.sql.Timestamp(System.currentTimeMillis()));
      clist.add(bean5);

      TSQBean bean6 = new TSQBean();
      bean6.setTicker("TACPOINT");
      bean6.setTradeCancelIndicator(TSQBean.NON_CANCELLED_TRADE);
      bean6.setMsgSequence(new Long(103));
      bean6.setMessageType(TSQBean.TYPE_TRADE);
      bean6.setTradeMarketCenter("m");
      bean6.setTradeSequence(new Long(10));
      bean6.setTradePrice(new Double(25.6601));
      bean6.setTradeSize(new Long(400));
      //bean6.setTradeQuoteTime(new java.sql.Timestamp(System.currentTimeMillis()));
      clist.add(bean6);

      TSQBean bean7 = new TSQBean();
      bean7.setTicker("TACPOINT");
      bean7.setTradeCancelIndicator(TSQBean.CANCELLED_TRADE);
      bean7.setMsgSequence(new Long(106));
      bean7.setMessageType(TSQBean.TYPE_TRADE);
      bean7.setTradeMarketCenter("m");
      bean7.setTradeSequence(new Long(10));
      bean7.setTradePrice(new Double(25.6601));
      bean7.setTradeSize(new Long(400));
      //bean7.setTradeQuoteTime(new java.sql.Timestamp(System.currentTimeMillis()));
      clist.add(bean7);

      List results = TSQMessageMerger.merge(dlist,clist);
      Iterator it = results.iterator();
      while (it.hasNext()) {
         TSQBean b = (TSQBean)it.next();
         System.out.println("#################################");
         System.out.println("Ticker     : "+b.getTicker());
         System.out.println("Time       : "+b.getTradeQuoteTime());
         System.out.println("Cancel Ind : "+b.getTradeCancelIndicator());
         System.out.println("Msg Seq    : "+b.getMsgSequence());
         System.out.println("Trade Seq  : "+b.getTradeSequence());
         System.out.println("Msg Type   : "+b.getMessageType());
         System.out.println("Trade mc   : "+b.getTradeMarketCenter());
         System.out.println("Trade price: "+b.getTradePrice());
         System.out.println("Trade size : "+b.getTradeSize());
         System.out.println("Bid price  : "+b.getBidPrice());
         System.out.println("Bid size   : "+b.getBidSize());
         System.out.println("Bid mc     : "+b.getBidMarketCenter());
     }


     byte[] bytes = TSQMessageSerializer.deflate(bean6);
     System.out.println("Bean 6 after deflation : "+new String(bytes));

     TSQBean b7 = TSQMessageSerializer.inflate(bytes);
     System.out.println("Bean 6 after inflation ...");
     System.out.println("#################################");
     System.out.println("Ticker     : "+b7.getTicker());
     System.out.println("Time       : "+b7.getTradeQuoteTime());
     System.out.println("Cancel Ind : "+b7.getTradeCancelIndicator());
     System.out.println("Msg Seq    : "+b7.getMsgSequence());
     System.out.println("Trade Seq  : "+b7.getTradeSequence());
     System.out.println("Msg Type   : "+b7.getMessageType());
     System.out.println("Trade mc   : "+b7.getTradeMarketCenter());
     System.out.println("Trade price: "+b7.getTradePrice());
     System.out.println("Trade size : "+b7.getTradeSize());
     System.out.println("Bid price  : "+b7.getBidPrice());
     System.out.println("Bid size   : "+b7.getBidSize());
     System.out.println("Bid mc     : "+b7.getBidMarketCenter());

   }



}


