package com.b4utrade.helper;

import java.util.Comparator;

import com.b4utrade.bo.MarketScannerBO;

/**
 * LastPriceDescComparator is used by a cache to order MarketScannerBO objects.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2001.  All rights reserved.
 * @version 1.0
 */
public class LastPriceDescComparator implements Comparator {

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
     Double wkHg1 = 0.0;
     Double wkHg2 = 0.0;
     try
     {
    	 wkHg1 = Double.parseDouble(b1.getFiftyTwoWeekHighValue());
     }
     catch(Exception e)
     {
    	 /*e.printStackTrace();
    	 return 1;*/
    	 wkHg1 = 0.0;
     }
     try
     {
    	 wkHg2 = Double.parseDouble(b2.getFiftyTwoWeekHighValue());
     }
     catch(Exception e)
     {
    	 /*e.printStackTrace();
    	 return -1;*/
    	 wkHg2 = 0.0;
     }
     return wkHg1.compareTo(wkHg2);
  }
}
