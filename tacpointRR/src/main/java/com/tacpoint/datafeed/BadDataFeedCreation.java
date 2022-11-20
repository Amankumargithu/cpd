/** BadDataFeedCreation.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 01.24.2000
* Date modified:
*/

package com.tacpoint.datafeed;

import java.lang.String;

/** BadDataFeedCreation class
* Exception class that's thrown if can't create a data feed sub class.
*/
public class BadDataFeedCreation extends Exception
{
   private final static String CANNOT_CREATE = "Error in creating data feed.";
   
   public BadDataFeedCreation()
   {
      super(CANNOT_CREATE);
   }
   
   public BadDataFeedCreation(String aMsg)
   {
      super(aMsg);
   }
}