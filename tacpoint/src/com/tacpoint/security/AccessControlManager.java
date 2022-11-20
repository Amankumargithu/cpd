/**
 * Copyright 2000 Tacpoint Technologies, Inc.
 * All rights reserved.
 *
 * @author  Ming Lau
 * @author mlau@tacpoint.com
 * @version 1.0
 * Date created: 1/4/2001
 * Date modified:
 * - 1/4/2001 Initial version
 *
 * Provide the access control to the url or other resource
 *
 */

package com.tacpoint.security;

import com.tacpoint.dataconnection.DBConnectionManager;
import com.tacpoint.util.*;

import java.util.*;
import java.sql.*;

/**
 * This Singleton class allows users to retrieve the access control information,
 * check the access, and refresh data for defined time interval.
 */
public class AccessControlManager implements Runnable
{
   /**
    * The Hash containing all the access urls.
    */
   private static Hashtable AccessHash = null;

   /**
    * The instance of AccessControlManager
    */
   private static AccessControlManager instance = null;

   /**
    * The allows for dynamic database queries.
    */
   private static Thread thread = null;

   /**
    * Determines whether or not to execute the run method.
    */
   private static boolean doRun = false;

   /**
    * The delay in milliseconds between database selects
    */
   private static long delay = 0L;

   /**
    * The delay in milliseconds between database selects
    */
   private static String currentHashDate = "";

   /**
    * Handle to the ConnectionManager
    */
   private DBConnectionManager connMgr = null;

   /**
    * A private constructor since this is a Singleton
    */
   private AccessControlManager() {
      try {
         delay = Integer.parseInt(Environment.get("ACCESS_CONTROL_MANAGER_DELAY"));
      }
      catch (NumberFormatException nfe) {
         // 30 seconds...
         delay = 300000L; 
      }
      connMgr = DBConnectionManager.getInstance();
      AccessHash = new Hashtable();
      
      doRun = true;
      //make sure the access hashtable has data
      selectLatestAccessControl();
      thread = new Thread(this);
      thread.start();
   }

   /**
    * Perform any initialization tasks.
    */
   public static synchronized void init() 
   {
      if (instance == null)
         instance = new AccessControlManager();
   }

   /**
    * The overridden run method which queries the database for the latest
    * Access Control Information.
    */
   public void run() {

      while (doRun) {
         try {
            selectLatestAccessControl();
         }
         catch (Exception e) {
            Logger.log("AccessControlManager.run() - Error encountered "+
                       "while selecting latest Access Control Information.",e);
         }
         try {
            Thread.sleep(delay);
         }
         catch (InterruptedException ie) {}
      }

   }

   /**
    * Return if the user has access to the url.
    */
   public static boolean checkAccess(int roleid, String url) {
      String s_roleid = ""+roleid;
      return(checkAccess(s_roleid, url));
   }

   /**
    * Return if the user has access to the url.  If there is not found, check if the url exists.
    */
   public static boolean checkAccess(String roleid, String url) {
    
      if (AccessHash == null)
      {
         return false;
      }
      
      String accessurl = url;
      if (accessurl == null)
      {
        accessurl = "";
      }
      
      String accessroleid = roleid;
      if (accessroleid == null)
      {
        accessroleid = "";
      }
      
      String akey = accessroleid.toUpperCase() +"_"+ accessurl.toUpperCase();
      boolean foundKey =  AccessHash.containsKey(akey);
      if (foundKey)
      {
        return foundKey;
      }
      else
      {
        //check if the url is in the access control list, if not return true.
        foundKey = AccessHash.containsValue(accessurl.toUpperCase());
        if (foundKey)
        {
            return false;
        }
        else
        {
            return true;
        }
      }
      
   }


   /**
    * Select the latest access control.
    */
   private void selectLatestAccessControl() {

      Connection c=connMgr.getConnection(Constants.DB_POOL,Constants.CONNECTION_WAIT_TIME);
      boolean debug = Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue(); 

      if (c==null) {
         Logger.log("AccessControlManager.selectLatestAccessControl() - "+
                    "Unable to obtain db connection.");
         return;
      }


      Statement timestmt = null;
      ResultSet timerset = null;

      // clean up the hash table daily - creating a new one and drop the old one.
      try {
         timestmt = c.createStatement();
         String timebuffer = "SELECT to_char(sysdate, 'YYYYMMDD')"
                       + "FROM     dual ";

         timerset = timestmt.executeQuery(timebuffer);

         while (timerset!=null && timerset.next()) {
            if ((timerset.getString(1)).equals(currentHashDate))
            {    continue;  }
            else
            {
                currentHashDate = timerset.getString(1);
                AccessHash = new Hashtable();
            }
                
         }
      }
      catch (Exception e) {
         Logger.log("AccessControlManager.selectLatestAccessControl() - "+
                    "Error encountered during select time.",e);
      }
      finally {
         if (timestmt!=null)
         {
            try {
               timestmt.close();
               if (timerset != null)
               {
                   timerset.close();
               }
            }
            catch (SQLException sqle) {}
         }
      }

      // selecting all access control information

      Statement astmt = null;

      try {
         astmt = c.createStatement();
         String buffer = "SELECT   ROLE_ACCESS.ROLE_ID, ACCESS_LEVEL.URL "
                       + "FROM     ROLE_ACCESS, ACCESS_LEVEL "
                       + "WHERE    ROLE_ACCESS.ACCESS_ID     = ACCESS_LEVEL.ACCESS_ID "
                       + "ORDER BY ROLE_ACCESS.ROLE_ID, ACCESS_LEVEL.URL";

         ResultSet rset = astmt.executeQuery(buffer);

         while (rset!=null && rset.next()) {
            AccessHash.put((rset.getString(1)).toUpperCase()+"_"+(rset.getString(2)).toUpperCase(),(rset.getString(2)).toUpperCase());
         }
      }
      catch (Exception e) {
         Logger.log("AccessControlManager.selectLatestAccessControl() - "+
                    "Error encountered during select.",e);
      }
      finally {
         if (astmt!=null)
         {
            try {
               astmt.close();
            }
            catch (SQLException sqle) {}
         }
         connMgr.freeConnection(Constants.DB_POOL,c);
      }
   }

   /**
    * Perform any necessary cleanup. 
    */
   public void finalize() {
      doRun = false;
      thread = null;
   }

}
