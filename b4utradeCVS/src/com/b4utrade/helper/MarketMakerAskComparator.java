package com.b4utrade.helper;

import com.b4utrade.stockutil.*;

import com.tacpoint.util.*;

import java.util.*;

/**
 * MarketMakerAskComparator is used by MarketMakerStockTopLists to determine order.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2002.  All rights reserved.
 * @version 1.0
 */
public class MarketMakerAskComparator implements Comparator {

  /**
   * Called by sort function to determine ordering of two objects.
   *
   * @param o1 Object
   * @param o2 Object
   * @returns int comparison result
   */
  public int compare(Object o1, Object o2)  {


     // ask data should be in ascending order...

     MarketMakerPrice b1 = (MarketMakerPrice)o1;
     MarketMakerPrice b2 = (MarketMakerPrice)o2;

     if (b1==null && b2==null) return 0;
     if (b1==null) return -1;
     if (b2==null) return 1;

     // kluge to keep ask prices equaling zero from showing up at the top of the book!
     
     
     if (b1.getPriceAsDouble() < b2.getPriceAsDouble() && b1.getPriceAsDouble() > .00001)
        return -1;

     if (b1.getPriceAsDouble() > b2.getPriceAsDouble() && b2.getPriceAsDouble() > .00001)
        return 1;

     int b1Size = 0;
     int b2Size = 0;

     try {
        b1Size = Integer.parseInt(b1.getSize());
     }
     catch (NumberFormatException nfe) {}

     try {
        b2Size = Integer.parseInt(b2.getSize());
     }
     catch (NumberFormatException nfe) {}

     if (b1Size < b2Size)
        return 1;

     if (b1Size > b2Size)
        return -1;

     if (b1.getTime() < b2.getTime())
        return 1;

     if (b1.getTime() > b2.getTime())
        return -1;

     return 0;
  }

}
