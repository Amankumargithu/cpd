package com.b4utrade.helper;

import java.util.Comparator;

import com.b4utrade.bo.MarketScannerBO;

/**
 * LastPriceAscComparator is used by a cache to order MarketScannerBO objects.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2001.  All rights reserved.
 * @version 1.0
 */
public class LastPriceAscComparator implements Comparator {

  /**
   * Called by sort function to determine ordering of two objects.
   *
   * @param o1 Object
   * @param o2 Object
   * @returns int comparison result
   */
  public int compare(Object o1, Object o2)  {

     MarketScannerBO b1 = (MarketScannerBO)o1;
     MarketScannerBO b2 = (MarketScannerBO)o2;

     if (b1==null && b2==null) return 0;
     if (b1==null) return 1;
     if (b2==null) return -1;
     Double wkLw1 = 0.0;
     Double wkLw2 = 0.0;
     try
     {
    	 wkLw1 = Double.parseDouble(b1.getFiftyTwoWeekLowValue());
     }
     catch(Exception e)
     {
    	 /*e.printStackTrace();
    	 return 1;*/
    	 wkLw1 = 0.0;
     }
     try
     {
    	 wkLw2 = Double.parseDouble(b2.getFiftyTwoWeekLowValue());
     }
     catch(Exception e)
     {
    	 /*e.printStackTrace();
    	 return -1;*/
    	 wkLw2 = 0.0;
     }
     return wkLw1.compareTo(wkLw2);
     /*if (b1.getPrice() == b2.getPrice()) return 0;

     if (b1.getPrice() < b2.getPrice())  return -1;

     return 1;*/

  }

}
