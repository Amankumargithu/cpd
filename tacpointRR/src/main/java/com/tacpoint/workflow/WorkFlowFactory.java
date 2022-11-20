/** WorkFlowFactory.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 01.23.2000
* Date modified:
*/

package com.tacpoint.workflow;

import com.tacpoint.util.*;
import java.util.HashMap;
import com.tacpoint.configuration.*;
import com.tacpoint.datafeed.*;

/** WorkFlowFactory class
* The WorkFlowFactory class will contain all the code necessary to create
* a new workflow process.
*/
abstract public class WorkFlowFactory
{
   protected abstract WorkFlowTask create();

   public static HashMap gFactories = new HashMap();

   public static WorkFlowTask createTask(String vWorkFlowStep)
      throws BadDataFeedCreation, Exception
   {
      Logger.init();

      // Delete when testing is done!
      Logger.log("WorkFlowFactory createWorkFlow");

      // Retrieve which app to run
      DataFeedSpecifier vDataFeedJob = DataFeedSpecifier.getDataFeedSpecifier();
      
      

      String vDataFeedType = vDataFeedJob.getFieldValue("DATAFEED", vWorkFlowStep);
      if (vDataFeedType == null || vDataFeedType.length() == 0)
         throw new Exception("Unable to retrieve Data Feed run type for work flow step : "+vWorkFlowStep);

      if (! gFactories.containsKey(vDataFeedType))
      {
         try
         {
            Class.forName(vDataFeedType); // load dynamically
         }
         catch (ClassNotFoundException e)
         {
            throw new BadDataFeedCreation();
         }
         // see if it was put in
         if (! gFactories.containsKey(vDataFeedType))
            throw new BadDataFeedCreation();
      }

      WorkFlowTask vTask = 
            ((WorkFlowFactory)gFactories.get(vDataFeedType)).create();
      if (vTask == null)
         throw new BadDataFeedCreation();

      return vTask;
   }
}
