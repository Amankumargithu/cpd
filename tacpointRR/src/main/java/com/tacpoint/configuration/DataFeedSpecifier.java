package com.tacpoint.configuration;


/** DataFeedSpecifier.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999.
* All rights reserved.
* @author Kim Gentes
* @author kimg@tacpoint.com
* @version 1.0
*/


import com.tacpoint.util.*;
import java.io.*;
import java.util.*;


/**
* Singleton helper class to access a configuration file.
* All configuration files are in the following style:
* [Header 1]
* Field name 1 = value
* Field name 2 = value
* [Header 2]
* Field name 1 = value
* etc.
*/
public class DataFeedSpecifier
{
   ////////////////////////////////////////////////////////////////////////////
   // D A T A    M E M B E R S
   ////////////////////////////////////////////////////////////////////////////

   private String mFilename = "/datafeedjob.properties";
   private DataFeedConfiguration mDataFeedConfig = null;


   ////////////////////////////////////////////////////////////////////////////
   // C O N S T R U C T O R S
   ////////////////////////////////////////////////////////////////////////////

   /**
    * Constructor: accepts a file name, and sets member variable mFileName.
    *
    * @param aFileName  String containing the configuration file name.
    *
    */
   private DataFeedSpecifier()
   {
      try
      {
         Logger.init();
         mDataFeedConfig = new DataFeedConfiguration();
      }
      catch(Exception e)
      {
         String vMsg;
         vMsg  = "DataFeedSpecifier constructor: ";
         vMsg += "Unable to init Logger.";
         System.out.println(vMsg);
      }

   }



   ////////////////////////////////////////////////////////////////////////////
   // M E T H O D S
   ////////////////////////////////////////////////////////////////////////////

   /**
    * Create a new DataFeedSpecifier object, with 'datafeedjob.properties'
    * as the DataFeedSpecifier file name.
    *
    */
   private static DataFeedSpecifier
                  mConfigFile = new DataFeedSpecifier();


   /**
    * Retrieve the only DataFeedSpecifier object (singleton model).
    *
    */
   public static DataFeedSpecifier getDataFeedSpecifier()
   {
      return mConfigFile;
   }

   synchronized public void setFilename(String aFilename)
   {
      if (aFilename == null || aFilename.length() == 0)
         return;
      char vFirstChar = aFilename.charAt(0);
      if (vFirstChar != '/')
         mFilename = '/' + aFilename;
      else
         mFilename = aFilename;
   }

   /**
    * Retrieves the field value which matches the Header name and field name.
    *
    * @param aHeader  Header name to look for.
    * @param aField   Field name whose value is retrieved.
    *
    * @return A string with the value of the field retrieved (can be empty).
    *
    */
   synchronized public String getFieldValue(String aHeader, String aField)
                                             throws Exception
   {
      return mDataFeedConfig.getFieldValue(aHeader, aField, mFilename);
   }

   synchronized public String[] getFieldValueList(String aHeader,
                                    String aField) throws Exception
   {
      return mDataFeedConfig.getFieldValueList(aHeader, aField, mFilename);
   }


   ////////////////////////////////////////////////////////////////////////////
   // T E S T I N G
   ////////////////////////////////////////////////////////////////////////////

   public static void main(String argv[])
   {
      try
      {
         DataFeedSpecifier vConfigFile =
                              DataFeedSpecifier.getDataFeedSpecifier();

         String[] vFieldList = vConfigFile.getFieldValueList("DATAFEED", "WEB_SERVERS");
         String vLog = new String();
         for (int i=0; i < vFieldList.length; i++)
            vLog += vFieldList[i] + " ";
         Logger.log("WEB_SERVERS = " + vLog);

         DataFeedSpecifier df =
                              DataFeedSpecifier.getDataFeedSpecifier();

         String vFieldValue = df.getFieldValue("DATAFEED", "OPTION_PORT");
         Logger.log("OPTION_PORT = " + vFieldValue);

         DataFeedSpecifier df2 =
                              DataFeedSpecifier.getDataFeedSpecifier();

         vFieldValue = df2.getFieldValue("ADMIN", "PORT");
         Logger.log("PORT = " + vFieldValue);
      }
      catch(Exception e)
      {
         String vMsg;
         vMsg  = "DataFeedSpecifier.getFieldValue(): ";
         vMsg += e.getMessage();
         Logger.log(vMsg);
      }
   }
}
