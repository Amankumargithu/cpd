package com.b4utrade.helper;

import com.b4utrade.stockutil.*;
import com.b4utrade.bean.*;

import com.tacpoint.util.*;

import java.util.*;

/**
 * MarketMakerBidComparator is used by MarketMakerStockTopLists to determine order.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2002.  All rights reserved.
 * @version 1.0
 */
public class OptionExpirationDateComparator implements Comparator {

  /**
   * Called by sort function to determine ordering of two objects.
   *
   * @param o1 Object
   * @param o2 Object
   * @returns int comparison result
   */
  public int compare(Object o1, Object o2)  {


     // ask data should be in ascending order...

     StockOptionBean b1 = (StockOptionBean)o1;
     StockOptionBean b2 = (StockOptionBean)o2;


       // compare expired date
       if ((b1.getExpirationDate()).after(b2.getExpirationDate()))
          return 1;
       else
       if ((b2.getExpirationDate()).after(b1.getExpirationDate()))
          return -1;
       else{
	     return 0;
       }

  }

}