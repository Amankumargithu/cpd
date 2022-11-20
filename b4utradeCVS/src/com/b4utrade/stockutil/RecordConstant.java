 /**
  * RecordConstant.java
  *   The class contains all constants used to retrieve Accessors.
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Kim Gentes
  * @author kgentes@tacpoint.com
  * @version 1.0
  * Date created: 4/17/2000
  */
package com.b4utrade.stockutil;


public final class RecordConstant
{
	public static final int VALUECOLUMN = 0;
	public static final int STORAGETYPECOLUMN = 1;
	public static final int NAMECOLUMN = 2;

	public static final String AMBIGUOUSSTORAGE = "0";
	public static final int STORAGE = 0;
	public static final int MEMORY = 1;
	public static final int DATABASE = 2;
//	public static final int OPTIONMEMORY = 3;
//	public static final int FUNDAMENTAL_DATA_MEMORY = 4;
	public static final int ECN_MEMORY = 5;
//	public static final int MARKET_MAKER_MEMORY = 6;
//	public static final int CSD_MEMORY = 7;

	public static final int TICKERROW = 0;
	public static final int EXCHANGEROW = 1;
	public static final int LAST_TRADE_MARKET_CENTER_ROW = 2;
	public static final int TRADE_SEQUENCE_ROW = 3;
	public static final int CANCELLED_TRADE_ROW = 4;
	public static final int CORRECTED_TRADE_PRICE_ROW = 5;
	public static final int CORRECTED_TRADE_SIZE_ROW = 6;
	public static final int QUOTE_TRADE_COND_CODE_1_ROW = 7;
	public static final int QUOTE_TRADE_COND_CODE_2_ROW= 8;
	public static final int QUOTE_TRADE_COND_CODE_3_ROW= 9;
	public static final int QUOTE_TRADE_COND_CODE_4_ROW= 10;
	public static final int EXCHANGE_ID_ROW= 11;
	public static final int CANCELLED_TRADE_PRICE_ROW = 12;
	public static final int CANCELLED_TRADE_SIZE_ROW = 13;

   public static final String INDEX_TICKER_CODE = "$";

}
