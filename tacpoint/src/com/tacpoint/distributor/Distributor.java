/** MessageDistributor.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000.
* All rights reserved.
* @author John Tong
* @author jtong@tacpoint.com
* @version 1.0
* Date created:  02/03/2000
*
* - 04/17/2000 KMG
*    - Implemented distribution system.
*/

package com.tacpoint.distributor;


import java.util.*;
import java.io.*;

import com.tacpoint.util.*;
import com.tacpoint.configuration.*;
import com.tacpoint.workflow.*;


/**
 * This class will take the parsed data and distribute it to the database and
 * the web servers.
 */
public abstract class Distributor extends WorkFlowTask
{
   ////////////////////////////////////////////////////////////////////////////
   // D A T A     M E M B E R S
   ////////////////////////////////////////////////////////////////////////////

   private boolean mDebug = false;
   private int mLastPort = 0;
   protected List mWebClients;


   ////////////////////////////////////////////////////////////////////////////
   // C O N S T R U C T O R S
   ////////////////////////////////////////////////////////////////////////////
   public Distributor()
   {
      try
      {
         Logger.init();
         Logger.log("MessageDistributor constructor.");
      }
      catch(Exception e)
      {
         String vMsg;
         vMsg = "MessageDistributor constructor: Unable to init Logger.";
         Logger.log(vMsg);
      }
   }


   ////////////////////////////////////////////////////////////////////////////
   // M E T H O D S
   ////////////////////////////////////////////////////////////////////////////

   public abstract void distributeRecord(List aRecord);

   synchronized protected void connectToWebClients() throws Exception
   {
      // get all the web server ip address from configuration file
      DataFeedSpecifier vDataFeedJob = DataFeedSpecifier.getDataFeedSpecifier();
      String vIPAddresses = vDataFeedJob.getFieldValue("DATAFEED", "WEB_SERVERS");
      if (vIPAddresses == null || vIPAddresses.length() == 0)
         throw new Exception("Unable to retrieve WebServers IP address.");

      // get port number
      if (mLastPort > 0)
         mLastPort++;
      else
      {
         NetworkUtility vUtil = NetworkUtility.getInstance();
         int vPort = vUtil.getPortNumber("DATAFEED", "PORT");
         mLastPort = vPort;
      }

      // parse out the ip addresses
      StringTokenizer vToken = new StringTokenizer(vIPAddresses, ",\n");
      String vIPAddress = null;
      while (vToken.hasMoreTokens())
      {
         vIPAddress = vToken.nextToken();
         if ( (vIPAddress != null) && (vIPAddress.length() > 0) )
         {
            // connect to web server
            WebClient vWebClient = new WebClient(vIPAddress, mLastPort);
            // add web servers ip address to vector
            synchronized (mWebClients)
            {
               mWebClients.add(vWebClient);
            }
         }
      }
   }

   protected void sendToWebClients(byte[] aByteStream)
   {
      if (aByteStream == null || aByteStream.length == 0)
      {
         Logger.debug("Distributor.sendToWebClients(): parameter ["
                      + "aByteStream] was blank.", mDebug);
         return;
      }
      if (mWebClients == null || mWebClients.size() == 0)
      {
         Logger.debug("Distributor.sendToWebClients(): member variable ["
                      + mWebClients + "] was empty", mDebug);
         return;
      }

      synchronized (mWebClients)
      {

         for (int i=0; i < mWebClients.size(); i++)
         {
            WebClient vClient = (WebClient)mWebClients.get(i);
            if (vClient == null)
            {
               continue;
            }

            OutputStream vOutput = vClient.getOutputStream();
            if (vOutput == null)
            {
//             Logger.debug("Distributor.sendToWebClients(): web client ["
//                       + i + "] has a null output stream", mDebug);

               vClient.resetSocket();
               continue;
            }
            try
            {
               vOutput.write(aByteStream);
               vOutput.flush();
            }
            catch (Exception e)
            {
               vClient.resetSocket();

//             Logger.debug("MessageDistributor.sendToWebClients(): unable to send stock data", mDebug);
            }
         }
      }
   }


}
