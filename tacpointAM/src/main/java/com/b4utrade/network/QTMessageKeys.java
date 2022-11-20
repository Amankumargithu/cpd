package com.b4utrade.network;

import com.tacpoint.common.*;

import java.util.*;
import java.io.*;

/**
 * Constants used by the b4utrade message reducer and inflator classes
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2005.  All rights reserved.
 * @version 1.0
 */

public class QTMessageKeys extends DefaultObject {

   public static final String TUPLE_SEP     = "|";
   public static final String FIELD_SEP     = ":";

   public static final String TICKER        = "T";
   public static final String LAST          = "L";
   public static final String CHANGE        = "C";
   public static final String PERCENT_CHANGE= "P";

   public static final String DAY_HIGH      = "h";
   public static final String DAY_LOW       = "l";

   public static final String BID_PRICE     = "B";
   public static final String ASK_PRICE     = "A";
   public static final String BID_SIZE      = "b";
   public static final String ASK_SIZE      = "a";

   public static final String ASK_EXCHANGE_CODE  = "e";
   public static final String BID_EXCHANGE_CODE  = "E";

   public static final String VOLUME        = "V";
   public static final String TRADE_VOLUME  = "v";
   public static final String OPEN_PRICE    = "O";
   public static final String LAST_CLOSED_PRICE = "lp";
   public static final String SETTLEMENT_PRICE = "sp";

   public static final String MARKET_CENTER_CODE = "M";
   public static final String VWAP_CODE = "W";
   public static final String TRADE_DATE = "d";
   public static final String TRADE_TIME = "t";
   public static final String SHORT_SALE_RESTRICTION_IND = "ss";
   public static final String HALT_IND = "ht";
   
   public static final String LIMIT_UP_DOWN = "UD";

   public static final String LAST_EXTENDED          = "Le";
   public static final String CHANGE_EXTENDED        = "Ce";
   public static final String PERCENT_CHANGE_EXTENDED = "Pe";
   public static final String TRADE_VOLUME_EXTENDED  = "ve";
   public static final String MARKET_CENTER_EXTENDED = "Me";
   public static final String TRADE_DATE_EXTENDED = "de";
   public static final String TRADE_TIME_EXTENDED = "te";
   public static final String VOLUME_PLUS = "Vp";
   // change to O:
   public static final String OPTIONS_MASK  = "O:";
   public static final String FUTURES_MASK  = ".FT";

   public static final String RESET  = "N/A";

   public static final String NEWS_ID          = "NID";
   public static final String NEWS_HEADLINE    = "NHL";
   public static final String NEWS_TICKERS     = "NT";
   public static final String NEWS_CATEGORIES  = "NC";
   public static final String NEWS_DATE        = "ND";
   public static final String NEWS_SOURCE      = "NS";
   public static final String NEWS_VENDOR      = "NV";

   public static final String STREAMER_ID        = "SI";
   public static final String STREAMER_CLASS      = "SC";
   public static final String UDP_BIND_ADDRESS        = "UBA";
   public static final String UDP_PORT            = "UP";
   public static final String STREAMER_COOKIE_KEY      = "StreamerID";
   public static final String BASIC_AUTH_KEY = "Authorization";

}
