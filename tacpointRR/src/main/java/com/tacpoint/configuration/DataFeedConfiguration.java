package com.tacpoint.configuration;


/** DataFeedConfiguration.java
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
public class DataFeedConfiguration
{
   ////////////////////////////////////////////////////////////////////////////
   // D A T A    M E M B E R S
   ////////////////////////////////////////////////////////////////////////////



   ////////////////////////////////////////////////////////////////////////////
   // C O N S T R U C T O R S
   ////////////////////////////////////////////////////////////////////////////

   public DataFeedConfiguration()
   {
      try
      {
         Logger.init();
         Logger.log("DataFeedConfiguration contructor");
      }
      catch(Exception e)
      {
         String vMsg;
         vMsg  = "DataFeedConfiguration constructor: ";
         vMsg += "Unable to init Logger.";
         System.out.println(vMsg);
      }
   }



   ////////////////////////////////////////////////////////////////////////////
   // M E T H O D S
   ////////////////////////////////////////////////////////////////////////////


   /**
    * Retrieves the field value which matches the Header name and field name.
    *
    * @param aHeader  Header name to look for.
    * @param aField   Field name whose value is retrieved.
    *
    * @return A string with the value of the field retrieved (can be empty).
    *
    */
   public String getFieldValue(String aHeader, String aField,
                                             String aFilename)
                                             throws Exception
   {
      String  vHeader = "";
      String  vField  = "";
      String  vLine   = "";
      String  vFinal  = "";
      int     vIndex  = 1;
      boolean vResult  = false;


      if (aHeader == null)
         throw new Exception("Header string cannot be NULL.");
      if (aHeader.length() == 0)
         throw new Exception("Must specify a Header to search for.");

      if (aField == null)
         throw new Exception("Field name string cannot be NULL.");
      if (aField.length() == 0)
         throw new Exception("Must specify a field name to search for.");

      if (aFilename == null)
         throw new Exception("Filename string cannot be NULL.");
      if (aFilename.length() == 0)
         throw new Exception("Must specify a file name.");

      try
      {
         InputStream is = getClass().getResourceAsStream(aFilename);
         BufferedReader vFile = new BufferedReader(new InputStreamReader(is));

         ///// 2. Look for section header
         vHeader = new String("[" + aHeader + "]");

         do
         {
            vLine = vFile.readLine();
            if (vLine != null)
            {
               vResult = vHeader.equalsIgnoreCase(vLine);
            }
         }
         while(vResult == false && vLine != null);

         ///// 3. If header is found, look for field.
         if (vResult == true)
         {
            do
            {
               vLine  = vFile.readLine();
               if (vLine != null)
               {
                  vIndex = vLine.indexOf('=');
                  if (vIndex >= 0)
                  {
                     vField = vLine.substring(0, vIndex);
                     vField = vField.trim();
                  }
               }
            }
            while((vField.equalsIgnoreCase(aField) == false) &&
                  (vLine != null));
         }

         // 3.a Extract the string right of the equal (=) sign.
         // 3.b Set 'vFinal' to extracted string.
         if (vField.equalsIgnoreCase(aField) == true)
         {
            vIndex = vLine.indexOf('=');
            vFinal = vLine.substring(vIndex+1);
            vFinal = vFinal.trim();
         }

         vFile.close();
         vFile = null;
      }
      catch(IOException e)
      {
         String vMsg;
         vMsg  = "DataFeedConfiguration.getFieldValue(): ";
         vMsg += "[" + aHeader + "] " + aField + ": ";
         vMsg += "Could not get value.";
         Logger.log(vMsg);
      }

      return new String(vFinal);
   }

   public String[] getFieldValueList(String aHeader,
                   String aField, String aFilename) throws Exception
   {

      if (aHeader == null)
         throw new Exception("Header string cannot be NULL.");
      if (aHeader.length() == 0)
         throw new Exception("Must specify a Header to search for.");

      if (aField == null)
         throw new Exception("Field name string cannot be NULL.");
      if (aField.length() == 0)
         throw new Exception("Must specify a field name to search for.");

      if (aFilename == null)
         throw new Exception("Filename string cannot be NULL.");
      if (aFilename.length() == 0)
         throw new Exception("Must specify a file name.");

      String vValuesList = getFieldValue(aHeader, aField, aFilename);
      if (vValuesList == null || vValuesList.length() == 0)
         return new String[0];

      int vIndex = -1;
      List vTypeList = new ArrayList();
      String vField = null;

      // Parse out all fields separated by comma.
      while (vValuesList.length() > 0 &&
               (vIndex = vValuesList.indexOf(',')) >= 0)
      {
         if (vIndex > 0)
         {
            vField = vValuesList.substring(0, vIndex);
            vField = vField.trim();
            if (vField.length() > 0)
               vTypeList.add(vField);
         }

         if (vIndex+1 < vValuesList.length())
            vValuesList = vValuesList.substring(vIndex+1);
         else
            vValuesList = new String();
      }

      // Add last field.
      if (vValuesList.length() > 0)
      {
         vField = vValuesList.trim();
         if (vField.length() > 0)
            vTypeList.add(vField);
      }

      String[] vList = null;
      if (vTypeList.size() > 0)
      {
         vList = new String[vTypeList.size()];
         for (int i=0; i < vTypeList.size(); i++)
         {
            vList[i] = (String)vTypeList.get(i);
         }
      }

      return vList;
   }


   ////////////////////////////////////////////////////////////////////////////
   // T E S T I N G
   ////////////////////////////////////////////////////////////////////////////

   public static void main(String argv[])
   {
      try
      {
         DataFeedConfiguration vConfigFile =
                              new DataFeedConfiguration();

         String vFieldValue = vConfigFile.getFieldValue("Comstock", "IP Address", "/tacpoint.ini");
         Logger.log("IP Address = " + vFieldValue);
      }
      catch(Exception e)
      {
         String vMsg;
         vMsg  = "DataFeedConfiguration.getFieldValue(): ";
         vMsg += e.getMessage();
         Logger.log(vMsg);
      }
   }
}
