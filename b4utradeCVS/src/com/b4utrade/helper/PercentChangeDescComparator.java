package com.b4utrade.helper;

import com.b4utrade.bo.*;

import com.tacpoint.util.*;

import java.util.*;

/**
 * PercentChangeDescComparator is used by a cache to order MarketScannerBO objects.
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2001.  All rights reserved.
 * @version 1.0
 */
public class PercentChangeDescComparator implements Comparator {

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

     if (b1.getPercentChange() == b2.getPercentChange()) return 0;

     if (b1.getPercentChange() < b2.getPercentChange())  return 1;

     return -1;

  }

}
