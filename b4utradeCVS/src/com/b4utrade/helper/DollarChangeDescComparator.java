package com.b4utrade.helper;

import java.util.Comparator;

import com.b4utrade.bo.MarketScannerBO;

/**
 * DollarChangeDescComparator is used by a cache to order MarketScannerBO objects.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2001.  All rights reserved.
 * @version 1.0
 */
public class DollarChangeDescComparator implements Comparator {

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

     if (b1.getDollarChange() == b2.getDollarChange()) return 0;

     if (b1.getDollarChange() < b2.getDollarChange())  return 1;

     return -1;

  }

}
