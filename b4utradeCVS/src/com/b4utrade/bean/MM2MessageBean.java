package com.b4utrade.bean;

import com.tacpoint.common.*;
import com.tacpoint.util.Logger;
import com.b4utrade.stockutil.*;
import java.text.SimpleDateFormat;

import java.util.*;

/**
 * Bean class market Maker lever 2 messages.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2002.  All rights reserved.
 * @version 1.0
 */

public class MM2MessageBean extends DefaultObject {

    private transient static String tickerString = "ticker=";
    private transient static String restrictedString = "restricted=";
    private transient static String bidsBeginString = "bids=";
    private transient static String bidsEndString = "endbids";
    private transient static String asksBeginString = "asks=";
    private transient static String asksEndString = "endasks";
    private transient static String mmpDelim = "||";
    private transient static String mmpElementDelim = ",";

    static transient SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    //static transient TimeZone tz = TimeZone.getTimeZone("GMT");

    /*
    static {
       sdf.setTimeZone(tz);
    }
    */

    boolean isParse = false;
    private String ticker;
    private int numTopBids;
    private int numTopAsks;
    private double spread;
    private boolean stockRestricted = false;

    private Vector bidList;
    private Vector askList;

    byte[] bytedata = null;


    public MM2MessageBean(){

    }

    public void init()
    {

       numTopAsks = 0;
       numTopBids =0;
       double tempBid = 0;
       double tempAsk = 0;
       double tempSpread = 0.0;
       int i;
       MarketMakerPrice tempPrice = null;
       if ((askList != null) && (askList.size() > 0))
       {
          tempPrice = (MarketMakerPrice)askList.elementAt(0);
          tempAsk = getPriceInDecimal(tempPrice.getPrice());
          numTopAsks = 1;
          if (askList.size() > 1){
             i = 1;
             tempPrice = (MarketMakerPrice)askList.elementAt(1);
             while (((i+1) < askList.size()) && (tempAsk == getPriceInDecimal(tempPrice.getPrice()))){
                numTopAsks++;
                i++;
                tempPrice = (MarketMakerPrice)askList.elementAt(i);
             }
          }
       }
       if ((bidList != null) && (bidList.size() > 0))
       {
          tempPrice = (MarketMakerPrice)bidList.elementAt(0);
          tempBid = getPriceInDecimal(tempPrice.getPrice());
          numTopBids = 1;
          if (bidList.size() > 1){
             i = 1;
             tempPrice = (MarketMakerPrice)bidList.elementAt(1);
             while (((i+1) < bidList.size()) && (tempBid == getPriceInDecimal(tempPrice.getPrice())))
             {
                numTopBids++;
                i++;
                tempPrice = (MarketMakerPrice)bidList.elementAt(i);
             }
          }
       }
              tempSpread = tempAsk - tempBid;
//           Logger.log(stock+" Ask Price: "+ tempAsk + "Bid Price: " + tempBid + " spread: " + spread);
      if ((tempAsk > 0.0) && (tempBid > 0.0))
      {
         spread = tempSpread;
      }
    }

    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {

        this.ticker = ticker;
    }

    public int getNumTopBids() {
        return numTopBids;
    }
    public void setNumTopBids(int numTopBids) {

        this.numTopBids = numTopBids;
    }

    public int getNumTopAsks() {
        return numTopAsks;
    }
    public void setNumTopAsks(int numTopAsks) {

        this.numTopAsks = numTopAsks;
    }


    public double getSpread() {
        return spread;
    }
    public void setSpread(double spread) {

        this.spread = spread;
    }

    public boolean getStockRestricted()
    {
       return stockRestricted;
    }
    public void setStockRestricted(boolean stockRestricted)
    {
       this.stockRestricted = stockRestricted;
    }

    public Vector getBidList() {
        return bidList;
    }
    public void setBidList(Vector bidList) {

        this.bidList = bidList;
    }
    public Vector getAskList() {
        return askList;
    }
    public void setAskList(Vector askList) {

        this.askList = askList;
    }

    public byte[] getByteData() {
        return bytedata;
    }

    public void getByteData(byte[] byteData) {
        this.bytedata = byteData;
    }
    public boolean isParseOk() {
        return isParse;
    }

    public void setIsParse(boolean isParse) {
        this.isParse = isParse;
    }

    public static MM2MessageBean convertMM2MessageBean(byte[] bytes) {


        MM2MessageBean bean = new MM2MessageBean();
        bean.setIsParse(false);
        String beanstr = new String(bytes);

        //insert ticker string into bean
        int beginIndex = beanstr.indexOf(tickerString) + tickerString.length();
        int endIndex = beanstr.indexOf(restrictedString);
        if ((beginIndex <0) || (endIndex <0) || (endIndex < beginIndex))
           return bean;
        bean.setTicker(beanstr.substring(beginIndex, endIndex));

        //insert restricted value into bean
        beginIndex = beanstr.indexOf(restrictedString) + restrictedString.length();
        endIndex = beanstr.indexOf(bidsBeginString);
        if ((beginIndex <0) || (endIndex <0) || (endIndex < beginIndex))
           return bean;
        bean.setStockRestricted((new Boolean(beanstr.substring(beginIndex, endIndex))).booleanValue());

        //insert bidList into the bean
        beginIndex = beanstr.indexOf(bidsBeginString);
        endIndex = beanstr.indexOf(bidsEndString);
        if ((beginIndex <0) || (endIndex <0) || (endIndex < beginIndex))
           return bean;
        bean.setBidList(getMMPVector(beanstr.substring(beginIndex,endIndex)));

        //insert askList into the bean
        beginIndex = beanstr.indexOf(asksBeginString);
        endIndex = beanstr.indexOf(asksEndString);
        if ((beginIndex <0) || (endIndex <0) || (endIndex < beginIndex))
           return bean;
        bean.setAskList(getMMPVector(beanstr.substring(beginIndex,endIndex)));
        bean.setIsParse(true);

        return bean;
    }


    public static byte[] convertMM2MessageBeanToByte(MM2MessageBean bean)  throws Exception {
        //StringBuffer sb = new StringBuffer();
    	StringBuilder sb = new StringBuilder(500);
        if (bean == null)
          return sb.toString().getBytes() ;
        //append ticker
        sb.append(tickerString);
        sb.append(bean.getTicker());

        //append restricted
        sb.append(restrictedString);
        sb.append(String.valueOf(bean.getStockRestricted()));

        //append bids
        sb.append(bidsBeginString);
        Vector tempVector = bean.getBidList();
        if (tempVector != null) {
          Enumeration enu = tempVector.elements();
          while (enu.hasMoreElements()){
             sb.append(mmpDelim);
             MarketMakerPrice mmp = (MarketMakerPrice)enu.nextElement();
             sb.append(mmp.getUPC());
             sb.append(mmpElementDelim);
             sb.append(mmp.getMarketMakerID());
             sb.append(mmpElementDelim);
             sb.append(mmp.getPrice());
             sb.append(mmpElementDelim);
             sb.append(mmp.getSize());
             sb.append(mmpElementDelim);
             mmp.formatTime(sdf);
             sb.append(mmp.getTimeInString());

             //System.out.println("MMP Time - "+new Date(mmp.getTime()));

          }
        }
        sb.append(bidsEndString);

        //append asks
        sb.append(asksBeginString);
        tempVector = bean.getAskList();
        if (tempVector != null) {
          Enumeration enu = tempVector.elements();
          while (enu.hasMoreElements()){
             sb.append(mmpDelim);
             MarketMakerPrice mmp = (MarketMakerPrice)enu.nextElement();
             sb.append(mmp.getUPC());
             sb.append(mmpElementDelim);
             sb.append(mmp.getMarketMakerID());
             sb.append(mmpElementDelim);
             sb.append(mmp.getPrice());
             sb.append(mmpElementDelim);
             sb.append(mmp.getSize());
             sb.append(mmpElementDelim);
             mmp.formatTime(sdf);
             sb.append(mmp.getTimeInString());
          }
        }
        sb.append(asksEndString);
        
        char[] chars = new char[sb.length()];
        sb.getChars(0,chars.length,(char[])chars,0);
        
        // can only do this since we know all chars are ascii!
        byte[] bytes = new byte[chars.length];
        for (int i=0; i<chars.length; i++)
           bytes[i] = (byte)chars[i];

        return bytes;

        //return sb.toString().getBytes("UTF8");

    }
    public static Vector getMMPVector(String vectorString){

        Vector vector = new Vector();
        try {
          StringTokenizer st = new StringTokenizer(vectorString, mmpDelim);
          //skip the first element because it's the indicator string.
          st.nextElement();
          while (st.hasMoreTokens()){
            MarketMakerPrice mmp= getMarketMakerPrice(st.nextToken().trim());
            vector.addElement(mmp);
          }
          return vector;

        }catch(NoSuchElementException nsee) {
            return vector;
        }

    }

    public static MarketMakerPrice getMarketMakerPrice(String mmpString) {
        MarketMakerPrice mmp = new MarketMakerPrice();
        try {

          StringTokenizer st = new StringTokenizer(mmpString, mmpElementDelim);
          mmp.setUPC(st.nextToken());
          mmp.setMarketMakerID(st.nextToken());
          mmp.setPrice(st.nextToken());
          mmp.setSize(st.nextToken());
          mmp.setTimeInString(st.nextToken());
          return mmp;

        }catch(NoSuchElementException nsee) {
            return mmp;
        }

    }
    private double getPriceInDecimal(String price) {

       if (price == null || price.length() == 0)
         return 0.0;

       double decimalPrice = 0.0;

       try {
            decimalPrice = Double.valueOf(price).doubleValue();

       }
       catch(Exception e) { }

       return decimalPrice;
   }

   static public void main(String[] args)
   {
	   
	   String testString= "ticker=CSCO.MM2bids=||O,JANY,23.34,1,23:53||O,TMBR,23.34,1,23:53||O,JPMS,23.31,1,23:53||O,BEST,23.30,25,23:53||O,FBCO,23.30,1,23:53||O,GVRC,23.30,10,23:53||O,MADF,23.30,14,23:53||O,NEED,23.30,1,23:53||O,RBCM,23.30,1,23:53||O,SCHB,23.30,109,23:53||O,TDCM,23.30,13,23:53||O,UBSW,23.30,27,23:53||O,WCHV,23.30,1,23:53||O,BOFA,23.29,1,23:53||O,PIPR,23.29,1,23:53||O,COWN,23.28,1,23:53||O,FACT,23.28,1,23:53||O,LEHM,23.28,1,23:53||O,PRUS,23.28,1,23:53||O,LEER,23.27,1,23:53endbidsasks=||O,SBSH,23.34,1,N/A||O,JPMS,23.35,1,N/A||O,TDCM,23.35,1,N/A||O,WCHV,23.35,1,N/A||O,BOFA,23.37,1,N/A||O,FBCO,23.37,1,N/A||O,LEHM,23.38,1,N/A||O,RBCM,23.40,1,N/A||O,SIZE,23.40,40,N/A||O,COWN,23.43,1,N/A||O,OPCO,23.49,1,N/A||O,CANT,23.50,1,N/A||O,SCHB,23.52,10,N/A||O,FACT,23.53,1,N/A||O,GVRC,23.53,30,N/A||O,NITE,23.54,80,N/A||O,TMBR,23.54,50,N/A||O,DBAB,23.55,1,N/A||O,PIPR,23.55,1,N/A||O,UBSW,23.55,4,N/Aendasks";
       MM2MessageBean mm2mb = MM2MessageBean.convertMM2MessageBean(testString.getBytes());
       
       
       long startTime = System.currentTimeMillis();
       try {
    
           for (int i=0; i<1000000; i++) {
        	   MM2MessageBean.convertMM2MessageBeanToByte(mm2mb);
           }
           
           long endTime = System.currentTimeMillis();
           System.out.println("Time to exec 1MM conversions : "+(endTime - startTime));
       }
       catch (Exception e) {
    	   e.printStackTrace();
       }

	   
	   
	   /*
      try{
         String testString= "ticker=CSCO.MM2bids=||O,JANY,23.34,1,23:53||O,TMBR,23.34,1,23:53||O,JPMS,23.31,1,23:53||O,BEST,23.30,25,23:53||O,FBCO,23.30,1,23:53||O,GVRC,23.30,10,23:53||O,MADF,23.30,14,23:53||O,NEED,23.30,1,23:53||O,RBCM,23.30,1,23:53||O,SCHB,23.30,109,23:53||O,TDCM,23.30,13,23:53||O,UBSW,23.30,27,23:53||O,WCHV,23.30,1,23:53||O,BOFA,23.29,1,23:53||O,PIPR,23.29,1,23:53||O,COWN,23.28,1,23:53||O,FACT,23.28,1,23:53||O,LEHM,23.28,1,23:53||O,PRUS,23.28,1,23:53||O,LEER,23.27,1,23:53endbidsasks=||O,SBSH,23.34,1,N/A||O,JPMS,23.35,1,N/A||O,TDCM,23.35,1,N/A||O,WCHV,23.35,1,N/A||O,BOFA,23.37,1,N/A||O,FBCO,23.37,1,N/A||O,LEHM,23.38,1,N/A||O,RBCM,23.40,1,N/A||O,SIZE,23.40,40,N/A||O,COWN,23.43,1,N/A||O,OPCO,23.49,1,N/A||O,CANT,23.50,1,N/A||O,SCHB,23.52,10,N/A||O,FACT,23.53,1,N/A||O,GVRC,23.53,30,N/A||O,NITE,23.54,80,N/A||O,TMBR,23.54,50,N/A||O,DBAB,23.55,1,N/A||O,PIPR,23.55,1,N/A||O,UBSW,23.55,4,N/Aendasks";
         MM2MessageBean mm2mb = MM2MessageBean.convertMM2MessageBean(testString.getBytes());
         
         
        
         if (mm2mb.isParseOk()) {
            System.out.println("parse ok");
            Vector bidlist = mm2mb.getBidList();
            Vector asklist = mm2mb.getAskList();

            Enumeration e1 = bidlist.elements();
            System.out.println("bidList:");

            while (e1.hasMoreElements())
            {
               MarketMakerPrice mmp = (MarketMakerPrice)e1.nextElement();
               System.out.println(mmp.getMarketMakerID() + ": " + mmp.getPrice());
            }
            System.out.println("askList:");

            Enumeration e2 = asklist.elements();
            while (e2.hasMoreElements())
            {
               MarketMakerPrice mmp = (MarketMakerPrice)e2.nextElement();
               System.out.println(mmp.getMarketMakerID() + ": " + mmp.getPrice());
            }
            System.out.println("init:");
            mm2mb.init();
            System.out.println("# top bid: " + mm2mb.getNumTopBids());
            System.out.println("# top bid: " + mm2mb.getNumTopAsks());
            System.out.println("Spread: " + mm2mb.getSpread());
            
        
         }
         else
           System.out.println("parse not ok");
         //putHash(); //put hash info into file
         System.out.println("hash put int file");
      }
      catch(Exception e){
          e.printStackTrace();
         System.out.println("exception:"+ e.getMessage());
      }
      
      */
   }

}
