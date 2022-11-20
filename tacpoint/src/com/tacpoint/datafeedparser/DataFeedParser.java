/** DataFeedParser.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000
* All rights reserved.
* @author Khoi Nguyen
* @author knguyen@tacpoint.com
* @version 1.0
* Date created: 01/18/2000
*
*
* This Parser class provides the specification for a parser.
* i.e. Comstock parser, Edgars, BestData, etc.
*
*/
package com.tacpoint.datafeedparser;

import com.tacpoint.util.*;
import com.tacpoint.workflow.*;

public abstract class DataFeedParser extends WorkFlowTask
{
   ////////////////////////////////////////////////////////////////////////////
   // C O N S T R U C T O R
   ////////////////////////////////////////////////////////////////////////////

   public DataFeedParser()
   {
      try
      {
         Logger.init();
         Logger.log("Parser constructor.");
      }
      catch(Exception e)
      {
         String vMsg;
         vMsg = "Parser constructor: Unable to init Logger.";
         System.out.println(vMsg);
      }
   }



   ////////////////////////////////////////////////////////////////////////////
   // M E T H O D S
   ////////////////////////////////////////////////////////////////////////////

   /**
    * Parses a data message.
    */
   public abstract void parseMessage(String aMessage);

}

