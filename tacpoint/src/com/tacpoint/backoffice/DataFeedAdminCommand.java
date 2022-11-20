/** DataFeedAdminCommand.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created:  02/03/2000
*
*/
package com.tacpoint.backoffice;

import java.io.*;
import java.net.*;
import java.util.*;
import com.tacpoint.util.*;
import com.tacpoint.configuration.*;

/**
 * DataFeedAdminCommand class is used to control the data feed server.
 */
public class DataFeedAdminCommand
{
   ////////////////////////////////////////////////////////////////////////////
   // D A T A    M E M B E R S
   ////////////////////////////////////////////////////////////////////////////
   public final static String QUIT_APP = "QUIT_APP";
   public final static String CLOSE = "CLOSE";
   public final static String READ_CONFIG = "READ_CONFIG";
   public final static String PARSER_COUNT = "PARSER_COUNT";
   public final static String QUEUE_1_COUNT = "QUEUE_1_COUNT";
   public final static String QUEUE_2_COUNT = "QUEUE_2_COUNT";

   // each thread should check this variable to see if they should shut down.
   public static volatile boolean gQuit;
   public static volatile boolean gReadConfig;
   public static volatile boolean gDoGetDatafeed;

   ////////////////////////////////////////////////////////////////////////////
   // C O N S T R U C T O R S
   ////////////////////////////////////////////////////////////////////////////
   public DataFeedAdminCommand()
   {
      // do nothing
   }
}
