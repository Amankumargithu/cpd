/** WorkFlowTask.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 01.28.2000
* Date modified:
*/

package com.tacpoint.workflow;

import com.tacpoint.util.*;
import com.tacpoint.messagequeue.*;


/** WorkFlowTask class
* The WorkFlowTask class is an abstract class representing all the steps
* within a work flow.
*/
public abstract class WorkFlowTask implements Runnable
{
   // Task Type
   public final static String RECORD_SPLIT = "SPLIT";
   public final static String PARSE        = "PARSE";
   public final static String DISTRIBUTE   = "DISTRIBUTE";
   public final static String OTHER_TASK   = "OTHER_TASK";
   
   // Constructor
   public WorkFlowTask()
   {
      // do nothing
   }
   
   public void setMessageQueue(MessageQueue vInQueue,
                               MessageQueue vOutQueue)
   {
      mInQueue = vInQueue;
      mOutQueue = vOutQueue;
   }

   /**
    * Main body which perform all the work.
    */
   public abstract void run();

   /**
    * Perform all the initializations needed to start receiving data feeds,
    *   i.e. connect, login, etc.
    */
   public abstract boolean init();

   /**
    * Perform all the cleanup process. ie. logout, reset, etc.
    */
   public abstract void cleanUp();


   protected MessageQueue mInQueue  = null;
   protected MessageQueue mOutQueue = null;
}
