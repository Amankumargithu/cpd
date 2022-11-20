/**
 * Copyright 2000 Tacpoint Technologies, Inc.
 * All rights reserved.
 *
 * @author  andy katz
 * @author akatz@tacpoint.com
 * @version 1.0
 * Date created: 1/2/2000
 * Date modified:
 * - 1/2/2000 Initial version
 *
 * Static constants to be used throughout the application.
 *
 */

package com.b4utrade.util;

public class B4UConstants {
	// strings

	public static final String ECN_DB_IPADDRESS = "ECN_DB_IPADDRESS";
	public static final String NASDAQMM2_DB_IPADDRESS = "NASDAQMM2_DB_IPADDRESS";
	public static final String NASDAQMM2_MAX_TOP_BIDS_ASKS = "NASDAQMM2_MAX_TOP_BIDS_ASKS";

	public static final int    OPTION_TYPE_BOTH = 0;
	public static final int    OPTION_TYPE_CALL = 1;
	public static final int    OPTION_TYPE_PUT = 2;


	public static final int    OPTION_CHAIN_TYPE_ALL = 0;
	public static final int    OPTION_CHAIN_TYPE_IN_THE_MONEY = 1;
	public static final int    OPTION_CHAIN_TYPE_OUT_OF_MONEY = 2;


	public static final int    OPTION_STRIKE_COUNT_ALL = 0;
	public static final int    OPTION_STRIKE_COUNT_10 = 10;
	public static final int    OPTION_STRIKE_COUNT_20 = 20;
	public static final int    OPTION_STRIKE_COUNT_30 = 30;
	public final static String DJ_NEWS_TOPIC = "DJ.NEWS";

	public final static String EDGE_NEWS_TOPIC = "EDGE.NEWS";

	public final static String NEWS_EDGE_CODE = "N";

	public final static String BENZINGA_NEWS_TOPIC = "BEN.NEWS";
	public final static String MIDNIGHT_TRADER_NEWS_TOPIC = "MID.NEWS";
	public final static String STREET_INSIDER_NEWS_TOPIC = "STR.NEWS";

	public final static String THE_FLY_ON_THE_WALL_NEWS_TOPIC = "FLY.NEWS";


	public final static String DJ_NEWS = "DOWJONES";

	public final static String EDGE_SOURCE = "EDGE";
	public final static String BENZINGA_SOURCE = "BEN";
	public final static String MIDNIGHT_SOURCE = "MID";
	public final static String STREET_SOURCE = "STR";
	public final static String FLY_ON_THE_WALL_SOURCE = "FLY";
	
	public final static String TICKERNAME = "tickerName";
	public final static String COMPANYNAME ="companyName";
	public final static String EXCHANGE = "exchange";
	public final static String SECTOR = "sector";
	public final static String INDUSTRY = "industry";
	public final static String STOCKYEARHI ="stockYearHi";
	public final static String STOCKYEARLO ="stockYearLo";
	public final static String STOCKEPS = "stockEPS";
	public final static String STOCKPE = "stockPE";
	public final static String STOCKAVGDAYVOL = "stockAvgDayVol";
	public final static String STOCKDIV = "stockDiv";
	public final static String STOCKYIELD = "stockYield";
	public final static String STOCKMKTCAP = "stockMktCap";
	public final static String STOCKEXDIVDATE = "stockExDivDate";
	public final static String CUSIPID = "cusipId";
	public final static String CURRENCY = "currency";
	public final static String STOCKTYPE = "stockType";
	public final static String TRAILINGDIV = "trailingDiv";
	public final static String DIVFREQUENCY = "divFrequency";

	public final static String EARNINGREPORTDATE = "earningReportDate";
	public final static String ISIN = "isin";
	public final static String PRICEBOOKRATIO = "priceBookRatio";
	public final static String BETA = "beta";
	public final static String PRICEYTDPERCENT = "priceYTDPercent";
	public final static String YTDVSSPPERCENT = "YTDvsSPPercent";
	public final static String AV10DVOL = "av10DVol";
	public final static String AV20DVOL = "av20DVol";
	public final static String AV30DVOL = "av30DVol";
	public final static String AV50DVOL = "av50DVol";
	public final static String POSTPANEL = "postPanel";
	public final static String SPECNAME = "specName";
	public final static String DIVIDENDPAYDATE = "dividendPayDate";
	public final static String TPILOT = "Tpilot";
}
