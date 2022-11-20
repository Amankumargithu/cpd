/**
 * 
 */

package com.b4utrade.helper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class MPidListCache implements Runnable
{
   /**
    * The hashtable contain the news ticket and news description.
    */
   private static HashMap mpidMap = null;
   /**
    * The instance of MPidListCache.
    */
   private static MPidListCache instance = null;

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
    * A private constructor since this is a Singleton
    */
   
   final static int size=1024;
  
   private MPidListCache() {
      try {
         delay = Integer.parseInt(Environment.get("MPIDLIST_CACHE_DELAY"));
      }
      catch (NumberFormatException nfe) {
         // 10 seconds...
         delay = 360000L; 
      }
      doRun = true;
      thread = new Thread(this);
      thread.start();
   }

   /**
    * Perform any initialization tasks.
    */
   public static synchronized void init() 
   {
      if (instance == null)
         instance = new MPidListCache();
      
      Logger.log("MPidListCache: init");
      
   }

   /**
    * The overridden run method which queries the database for the latest
    * news stories.
    */
   public void run() {

      while (doRun) {

         try {
        	 String fAddress = Environment.get("MPIDLIST_FILE_ADDRESS");
        	 String localFileName = Environment.get("MPIDLIST_LOCAL_FILENAME");
        	 String localDirectory = Environment.get("MPIDLIST_LOCAL_DIR");
        	 fileUrl(fAddress,localFileName, localDirectory);
        	 parseFile(localFileName, localDirectory);          	 
         }
         catch (Exception e) {
            Logger.log("MPidListCache.run() - Error encountered while selecting MPIDLIST FILE.",e);
         }      
         try {
            Thread.sleep(delay);
         }
         catch (InterruptedException ie) {}
      }
   }

   private static void fileUrl(String fAddress, String  localFileName, String destinationDir) {
     OutputStream outStream = null;
     URLConnection  uCon = null;

     InputStream is = null;
     try {
        URL Url;
        byte[] buf;
        int ByteRead,ByteWritten=0;
        Url= new URL(fAddress);
        outStream = new BufferedOutputStream(new
        FileOutputStream(destinationDir+"/"+localFileName));

        uCon = Url.openConnection();
        is = uCon.getInputStream();
        buf = new byte[size];
        while ((ByteRead = is.read(buf)) != -1) {
           outStream.write(buf, 0, ByteRead);
           ByteWritten += ByteRead;
        }
        Logger.log("MPidListCache  File name:\""+localFileName+ "\"\nNo ofbytes :" + ByteWritten);
        doRun = false;
     }
     catch (Exception e) {
       e.printStackTrace();
       doRun = true;
     }
     finally {
        try {
           is.close();
           outStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
     }
   }
   private static void parseFile(String  localFileName, String destinationDir) throws IOException{
	   BufferedReader br = new BufferedReader(new FileReader(new File(destinationDir+"/"+localFileName)));
	   String line;
	   mpidMap = new HashMap();
	   while ((line = br.readLine()) != null) {
		   line.replaceAll("||", "| |");
		   StringTokenizer st = new StringTokenizer(line,"|");
		   String mpid, description, firna;
		   //first field contain the mpid
		   if (st.hasMoreTokens())
			   mpid = st.nextToken();
		   else 
			   continue;
		   //second field MP name ignore
		   if (st.hasMoreTokens())
			   st.nextToken();
		   else
			   continue;
		   //third field contain the description
		   if (st.hasMoreTokens())
			   description = st.nextToken();
		   else 
			   continue; 
		   //4th  field Location ignore
		   if (st.hasMoreTokens())
			   st.nextToken();
		   else
			   continue;	        
		   //5th  field telephone ignore
		   if (st.hasMoreTokens())
			   st.nextToken();
		   else
			   continue;    
		   //6th  field NASDAQ Member ignore
		   if (st.hasMoreTokens())
			   st.nextToken();
		   else
			   continue;             
		   //7th  field FINRA Member check if in the list
		   if (st.hasMoreTokens())
			   firna =st.nextToken();
		   else
			   continue;
		   if ((firna != null) && (firna.equalsIgnoreCase("Y"))){
			   mpidMap.put(mpid, description);
		   }
	   }
	   br.close();  
   }
   
   /**
    * Return the mpidMap.
    */
   public static HashMap getMpidMap() {
      return mpidMap;
   }   
   
   /**
    * Perform any necessary cleanup. 
    */
   public void finalize() {
      doRun = false;
      thread = null;
   }

}
