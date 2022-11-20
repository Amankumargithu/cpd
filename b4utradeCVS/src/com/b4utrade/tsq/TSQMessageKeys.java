package com.b4utrade.tsq;

import com.tacpoint.common.*;

import java.util.*;
import java.io.*;

/**
 * Constants used by the b4utrade TSQSerializer
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2006.  All rights reserved.
 * @version 1.0
 */

public class TSQMessageKeys extends DefaultObject {

   public static final String TUPLE_SEP     = "|";
   public static final String FIELD_SEP     = ":";

   public static final String TICKER                  = "T";
   public static final String MESSAGE_SEQUENCE        = "MS";
   public static final String TRADE_SEQUENCE          = "TS";
   public static final String MESSAGE_TYPE            = "MT";
   public static final String TRADE_CANCEL_IND        = "X";
   public static final String TRADE_QUOTE_COND_CODE_1 = "C1";
   public static final String TRADE_QUOTE_COND_CODE_2 = "C2";
   public static final String TRADE_QUOTE_COND_CODE_3 = "C3";
   public static final String TRADE_QUOTE_COND_CODE_4 = "C4";
   public static final String TRADE_QUOTE_TIME        = "TQ";
   public static final String TRADE_PRICE             = "TP";
   public static final String TRADE_SIZE              = "TZ";
   public static final String BID_PRICE               = "BP";
   public static final String BID_SIZE                = "BZ";
   public static final String ASK_PRICE               = "AP";
   public static final String ASK_SIZE                = "AZ";
   public static final String EXCHANGE_ID             = "EI";
   public static final String TRADE_MARKET_CENTER     = "TC";
   public static final String BID_MARKET_CENTER       = "BC";
   public static final String ASK_MARKET_CENTER       = "AC";
   public static final String VWAP                    = "V";
   public static final String COMPUTED_VWAP           = "CV";
   public static final String CREATION_DATE           = "CD";
   public static final String TOTAL_VOLUME            = "TV";
   public static final String FILTERED_TOTAL_VOLUME   = "FV";
   public static final String MARKET_MAKER_ID         = "MM";
   public static final String UND_BID_PRICE           = "UBP";
   public static final String UND_ASK_PRICE           = "UAP";
   public static final String UND_BID_SIZE            = "UBS";
   public static final String UND_ASK_SIZE            = "UAS";
   public static final String UND_BID_EXCH            = "UBX";
   public static final String UND_ASK_EXCH            = "UAX";

}
