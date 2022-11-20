package com.b4utrade.stockutil;

/** StockItems.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000.
* All rights reserved.
* @author Kim Gentes
* @author kgentes@tacpoint.com
* @version 1.0
* Date created:   04/17/2000
*
*/

public final class StockItems
{
   public static final String NOACTIVITY = "N/A";
   public static final String PUT_OPTION = "P";
   public static final String CALL_OPTION = "C";

   public static final String BBO_REGIONAL_SEP = "|";
   public static final String TRADING_MARKET_CENTER_SEP = "|";

   public static final String MM2_UPC_RESET = "O";
   public static final String MM2_UPC_CLOSED = "L";

   public static final String GMT_DATE_PATTERN = "yyyyMMdd";
   public static final String GMT_TIME_PATTERN = "HHmmss";
   public static final String GMT_TIMEZONE_NAME = "GMT";
   public static final String EDT_TIMEZONE_NAME = "America/New_York";

   public static final int ASK = 1;
   public static final int BID = 2;
   public static final int PRICE = 3;        // current price
   public static final int OPENINTEREST = 4;
   public static final int DAYHIGH = 5;
   public static final int VOLUME = 6;
   public static final int BIDSIZE = 7;
   public static final int ASKSIZE = 8;
   public static final int DAYLOW = 9;
   public static final int CHANGE = 10;
   public static final int OPENPRICE = 11;
   public static final int LASTPRICE = 12;
   public static final int TOTALVOLUME = 13;
   public static final int PREVDAYCLOSEPRICE = 14;
   public static final int REGION = 15;
   public static final int STRIKEPRICE = 21;
   public static final int EXPIRATIONDATE = 22;
   public static final int TRADEDATE = 23;
   public static final int TRADETIME = 24;
   public static final int QUOTEDATE = 26;
   public static final int QUOTETIME = 27;

   public static final int ECN_TIMESTAMP = 41;
   public static final int ECN_ORDERNUMBER = 42;
   public static final int ECN_BUYSELL_FLAG = 43;
   public static final int ECN_MATCHNUMBER = 44;
   public static final int ECN_TICKER = 45;
   public static final int ECN_EXCHANGE = 46;
   public static final int ECN_SEQUENCE = 47;

   public static final int PE_RATIO = 51;
   public static final int AVERAGEDAILYVOL = 52;
   public static final int YEARHIGH_YEARLOW = 53;
   public static final int YIELD = 54;
   public static final int DIVIDEND = 55;
   public static final int DIVIDENDRATE = 56;
   public static final int EXDIVIDEND = 57;
   public static final int EPS = 58;
   public static final int OPTIONSYMBOLS = 59;
   public static final int INSTITUTIONPERCENT = 60;
   public static final int BETA = 61;
   public static final int LEAPS = 62;
   public static final int WRAPS = 63;
   public static final int LONGTERMDEBT = 64;
   public static final int COMMONSTOCK = 65;
   public static final int PREFERREDSTOCK = 66;
   public static final int EXCHANGEID = 67;
   public static final int COMPANY_NAME = 68;

   public static final int NASDAQRESTRICTED = 70;
   public static final int NASDAQUPTICK_RESTRICTED = 71;
   public static final int NASDAQUPTICK_NONRESTRICTED = 72;
   public static final int NASDAQDOWNTICK_RESTRICTED = 73;
   public static final int NASDAQDOWNTICK_NONRESTRICTED = 74;
   public static final int HALTED_STOCK = 75;
   public static final int CLEAR_SPECIAL_FLAGS = 80;
   public static final int START_OF_DAY_FLAG = 81;

   public static final int DECIMAL_CODE = 90;
   
   public static final int NASDAQ_MARKETMAKER_ID = 91;
   
   public static final int NASDAQ_MARKETMAKER_UPC_CODE = 93;
   public static final int NASDAQ_MARKETMAKER_BID_TICK_CODE = 94;
   
   public static final int NASDAQ_DELETE_MARKETMAKER_ID = 92;
   public static final int NASDAQ_MARKETMAKER_ORDER_ID = 96;
   public static final int NASDAQ_MARKETMAKER_PREV_ORDER_ID = 97;
   public static final int NASDAQ_MARKETMAKER_NEXT_ORDER_ID = 98;
   public static final int NASDAQ_MARKETMAKER_TOP_OF_THE_BOOK = 99;
   
   
   
   public static final int VWAP = 95;

   public static final int REG_BID_PRICE = 100;
   public static final int REG_ASK_PRICE = 101;
   public static final int REG_BID_SIZE  = 102;
   public static final int REG_ASK_SIZE  = 103;
   public static final int TRADE_DATETIME  = 104;
   public static final int QUOTE_DATETIME  = 105;
   
   public static final int MARKET_MAKER_BID_PRICE = 106;
   public static final int MARKET_MAKER_ASK_PRICE = 107;
   public static final int MARKET_MAKER_BID_SIZE  = 108;
   public static final int MARKET_MAKER_ASK_SIZE  = 109;
   
   public static final int MARKET_MAKER_F74_INDICATOR = 110;
   public static final int MARKET_MAKER_G43_INDICATOR = 111;
   
   public static final int MARKET_MAKER_U13_INDICATOR = 112;
   
   public static final int PINKSHEETS_F41_INDICATOR = 113;
   

}
