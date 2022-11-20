/** NetworkUtility.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 02.07.2000
* Date modified:
*/

package com.tacpoint.util;

import java.io.*;
import com.tacpoint.configuration.*;


/** NetworkUtility class
* The NetworkUtility class is used to store all misc functions.  Don't just put any
* function here.  This is a singleton class so any class can quickly access it
* from anywhere.
*/
public final class NetworkUtility
{
   private static NetworkUtility mUtilityInstance = new NetworkUtility();
   
   // Constructor
   private NetworkUtility()
   {
      // do nothing
   }

   public static NetworkUtility getInstance()
   {
      return mUtilityInstance;
   }
   
   public int getPortNumber(String aCategory, String aValue) throws Exception
   {
      DataFeedSpecifier vDataFeedJob = DataFeedSpecifier.getDataFeedSpecifier();
      String vPortNumberStr = vDataFeedJob.getFieldValue(aCategory, aValue);
      if (vPortNumberStr == null || vPortNumberStr.length() == 0)
         throw new Exception("Unable to retrieve port number.");
         
      int vPortNumber = Integer.parseInt(vPortNumberStr);
      
      return (vPortNumber);
   }
}
