package com.tacpoint.configuration;


/** FileConfiguration.java
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
public class FileConfiguration
{
   ////////////////////////////////////////////////////////////////////////////
   // D A T A    M E M B E R S
   ////////////////////////////////////////////////////////////////////////////

   private String mFilename = "/tacpoint.ini";
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
   private FileConfiguration()
   {
      try
      {
         Logger.init();
         mDataFeedConfig = new DataFeedConfiguration();
      }
      catch(Exception e)
      {
         String vMsg;
         vMsg  = "FileConfiguration constructor: ";
         vMsg += "Unable to init Logger.";
         System.out.println(vMsg);
      }

   }



   ////////////////////////////////////////////////////////////////////////////
   // M E T H O D S
   ////////////////////////////////////////////////////////////////////////////

   /**
    * Create a new FileConfiguration object, with 'tacpoint.ini'
    * as the configuration file name.
    *
    */
   private static FileConfiguration
                  mConfigFile = new FileConfiguration();


   /**
    * Retrieve the only FileConfiguration object (singleton model).
    *
    */
   public static FileConfiguration getConfigurationFile()
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
         FileConfiguration vConfigFile =
                              FileConfiguration.getConfigurationFile();
         String vFieldValue = vConfigFile.getFieldValue("CNBCFeed", "File Name");
         Logger.log("File Name = " + vFieldValue);

         FileConfiguration fc1 =
                              FileConfiguration.getConfigurationFile();
         vFieldValue = fc1.getFieldValue("Newsview", "NewsIP");
         Logger.log("NewsIP = " + vFieldValue);

         FileConfiguration fc2 =
                              FileConfiguration.getConfigurationFile();
         vFieldValue = fc1.getFieldValue("Newsview", "NewsIPDirectory");
         Logger.log("NewsIPDirectory = " + vFieldValue);

         FileConfiguration fc3 =
                              FileConfiguration.getConfigurationFile();
         String[] vFieldList = vConfigFile.getFieldValueList("EDGAR-IPO", "DataRetrievalTimes");
         String vLog = new String();
         for (int i=0; i < vFieldList.length; i++)
            vLog += vFieldList[i] + " ";
         Logger.log("DataRetrievalTimes = " + vLog);

      }
      catch(Exception e)
      {
         String vMsg;
         vMsg  = "FileConfiguration.getFieldValue(): ";
         vMsg += e.getMessage();
         Logger.log(vMsg);
      }
   }
}
