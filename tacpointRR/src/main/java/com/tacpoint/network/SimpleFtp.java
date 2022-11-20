/**
 * Copyright Tacpoint Technologies, Inc.
 * All rights reserved.
 *
 * @author  Leonid Ratgauz
 * @author leonidr@tacpoint.com
 * @version 1.0
 * Date created: 1/25/2000
 * Date modified:
 * - 1/25/2000 Initial version
 *
 * The ftp class, provides very simple ftp get file method only
 *
 * - 3/31/2000 KMG Changed the way retrieval socket is formed.
 *
 */

package com.tacpoint.network;


import java.net.*;
import java.io.*;
import java.util.*;

import com.tacpoint.util.*;

/**
 * This SimpleFtp class allows users to open a ftp connction
 * set type (ascii, binary), and get file
 */

public class SimpleFtp extends NetworkConnection
{
   ////////////////////////////////////////////////////////////////////////////
   // D A T A   M E M B E R S
   ////////////////////////////////////////////////////////////////////////////

   // FOR DEBUGGING: set the variable to "true"
   private boolean DEBUG = false;
   private static final String mServerOKCode = "220";

   /*
    * Stall (refuse further requests) till we get a reply back 
    * from server for the current request.
    */
   private boolean mPause = false;

   /**
   * A private <code>mHost</code> instance - host name
   */
   private String mHost;
   /**
   * A private <code>mUser</code> instance - user name
   */
   private String mUser;
   /**
   * A private <code>mPassword</code> instance - password
   */
   private String mPassword;
   /**
   * A private <code>mType</code> instance - transfer type.
   * A = ASCII or I = binary
   */
   private char mType;

   /**
   * A Socket instance used to maintain a reference to the open socket.
   */
   private Socket mControlSocket = null;

   /**
   * An input stream used to read from the socket.
   */
   private BufferedReader mInputStream = null;
   /**
   * A print writer used to write to the socket.
   */
   private PrintWriter mPrintWriter = null;

   /**
   * Holds the filename to retrieve from a ftp site.
   */
   private String mGetFilename = null;
   /**
   * Holds the name of the directory to store the retrieved file.
   */
   private String mStoreDirectory = null;


   ////////////////////////////////////////////////////////////////////////////
   // C O N S T R U C T O R S
   ////////////////////////////////////////////////////////////////////////////

   /**
   * SimpleFtp constructor that just initializes member data.
   *
   * @param aHost    String - host name
   * @param aUser    String - User name
   * @param aPassword String - User password
   * @param aType     String - transfer type (A = ASCII or I = binary)
   */
   public SimpleFtp(String aHost, String aUser, String aPassword, char aType)
                   throws Exception
   {
      Logger.init();
      
      if (aHost == null || aHost.length() == 0)
      {
         String vMsg = "SimpleFtp constructor: ";
         vMsg += "parameter [aHost] was blank.";
         throw new Exception(vMsg);
      }
      if (aUser == null || aUser.length() == 0)
      {
         String vMsg = "SimpleFtp constructor: ";
         vMsg += "parameter [aUser] was blank.";
         throw new Exception(vMsg);
      }
      if (aPassword == null || aPassword.length() == 0)
      {
         String vMsg = "SimpleFtp constructor: ";
         vMsg += "parameter [aPassword] was blank.";
         throw new Exception(vMsg);
      }

      mHost = aHost;
      mUser = aUser;
      mPassword = aPassword;
      mType = Character.toUpperCase(aType);
   }

   ////////////////////////////////////////////////////////////////////////////
   // M E T H O D S
   ////////////////////////////////////////////////////////////////////////////

   /**
   * Open a connection to a remote host that runs an FTP server.
   */
   public boolean connect()
   {
      boolean vOK = false;
      try
      {
         // Set up socket, control streams, connect to ftp server
         // Open socket to server control port 21
         mControlSocket = new Socket(mHost, 21);

         // Open control streams
         mInputStream = new BufferedReader(new InputStreamReader(
                              mControlSocket.getInputStream()));
         OutputStream vOutStream = mControlSocket.getOutputStream();
         // Set auto flush true.
         mPrintWriter = new PrintWriter(vOutStream, true);

         // See if server is alive or dead... 
         String vResponse = response(null);
         if (vResponse != null && vResponse.length() >= 3 &&
               vResponse.substring(0,3).equals(mServerOKCode))
         {
            // ftp server alive
            if (DEBUG)
               System.out.println("Connected to ftp server.");
         }
         else 
            throw new Exception ("Error in connecting to ftp server ["
                                 + mHost + "].");
         command("USER " + mUser);
         command("PASS " + mPassword);
         vOK = true;
      }
      catch(IOException e)
      {
         String vMsg = "SimpleFtp.connect(): " + e.getMessage();
         Logger.log(vMsg);
      }
      catch(Exception e)
      {
         String vMsg = "SimpleFtp.connect(): " + e.getMessage();
         Logger.log(vMsg);
      }
      return vOK;
   }

   /**
   * The filename to retrieve from a ftp site must be set before calling
   * 'receiveData()' function.  The directory where the file should be
   * stored must be set before calling 'receiveData()'.
   */
   public void setRetrieveFilenamePath(String aFilename, String aDirectory)
   {
      if (aFilename == null || aFilename.length() == 0)
      {
         String vMsg = "SimpleFtp.setRetrieveFilenamePath: ";
         vMsg += "parameter [aFilename] was blank.";
         Logger.log(vMsg);
         return;
      }
      if (aDirectory == null || aDirectory.length() == 0)
      {
         String vMsg = "SimpleFtp.setRetrieveFilenamePath: ";
         vMsg += "parameter [aDirectory] was blank.";
         Logger.log(vMsg);
         return;
      }

      mGetFilename = aFilename;
      mStoreDirectory = aDirectory;
   }

   public ArrayList receiveParsedMessages(byte msgDelim) {
	   return null;
   }
   
   /**
   * Get a file from a remote host and saves it to the specifield directory
   * NOTE: 'setRetrieveFilenamePath()' function must be called first.
   *
   * @return vPath    String - location to save the file
   */
   public String receiveData()
   {
      if (mGetFilename == null || mGetFilename.length() == 0)
      {
         String vMsg = "SimpleFtp.receiveData(): ";
         vMsg += "member [mGetFilename] is blank.";
         Logger.log(vMsg);
         return null;
      }

      String vPath = null;
      try
      {
         if (mType == 'I')
            setTransferType(false);
         else
            setTransferType(true);

         vPath = get("RETR " + mGetFilename);
      }
      catch(IOException e)
      {
         Logger.log("SimpleFtp.receiveData() IOException: "
                     + e.getMessage());
      }
      catch(Exception e)
      {
         Logger.log("SimpleFtp.receiveData() Exception: " 
                     + e.getMessage());
      }

      return vPath;
   }

   /**
    * This method is added temporary.
    * Due to the change made in Parent Class NetworkConnetion,
    * this class has to implement this mehtod.
    */
   public byte[] receiveDataAsBytes()
   {
       return null;
   }

   /**
   * Put a file to a remote host from the specifield directory.
   * NOTE: 'setRetrieveFilenamePath()' function must be called first.
   */
   public boolean sendData(String aData)
   {
      if (mGetFilename == null || mGetFilename.length() == 0)
      {
         String vMsg = "SimpleFtp.sendData(): ";
         vMsg += "member [mGetFilename] is blank.";
         Logger.log(vMsg);
         return false;
      }
      if (mStoreDirectory == null || mStoreDirectory.length() == 0)
      {
         String vMsg = "SimpleFtp.sendData(): ";
         vMsg += "member [mStoreDirectory] is blank.";
         Logger.log(vMsg);
         return false;
      }

      boolean vOK = setDirectory(mStoreDirectory);
      if (!vOK)
         return false;

      vOK = false;
      try
      {
         if (mType == 'I')
            setTransferType(false);
         else
            setTransferType(true);

         vOK = put("STOR " + mGetFilename, aData);
      }
      catch(IOException e)
      {
         Logger.log("SimpleFtp.sendData() IOException: "
                     + e.getMessage());
      }
      catch(Exception e)
      {
         Logger.log("SimpleFtp.sendData() Exception: " 
                     + e.getMessage());
      }

      return vOK;
   }

   /**
    * Changes directory to the directory passed in.
    *
    * @param aDirectory   directory to change to
    */
   public boolean setDirectory(String aDirectory) 
   { 
      boolean vOK = false;
      try
      {
         // cwd to directory
         if (aDirectory != null && aDirectory.length() > 0)
         {
            command("CWD " + aDirectory);
            vOK = true;
         }
         else
         {
            String vMsg = "SimpleFtp.setDirectory(): ";
            vMsg += "parameter [aDirectory] was blank.";
            Logger.log(vMsg);
         }
      }
      catch(IOException e)
      {
         Logger.log("SimpleFtp.setDirectory() IOException: "
                     + e.getMessage());
      }
      catch(Exception e)
      {
         Logger.log("SimpleFtp.setDirectory() Exception: " 
                     + e.getMessage());
      }
      return vOK;
   }

   /**
    * Gets the currect directory list.
    * NOTE: 'setRetrieveFilenamePath()' function must be called first.
    *
    * @return vPath    String - location to dump the directory listing.
    */
   public String getDirectoryList()
   {
      String vPath = null;
      try
      {
         setTransferType(true);
         vPath = get("LIST");
      }
      catch(IOException e)
      {
         Logger.log("SimpleFtp.getDirectoryList() IOException: "
                     + e.getMessage());
      }
      catch(Exception e)
      {
         Logger.log("SimpleFtp.getDirectoryList() Exception: " 
                     + e.getMessage());
      }
      return vPath;
   }

   /**
   * Closes a connection to a remote host
   *           
   */
   public boolean disconnect()
   {
      // logout, close streams
      boolean vOK = false;
      try 
      { 
         if(DEBUG)
            System.out.println("sending BYE");
         mPrintWriter.print("BYE" + "\r\n" );
         mPrintWriter.flush();
         mPrintWriter.close();
         mInputStream.close();
         mControlSocket.close();
         vOK = true;
      } 
      catch(IOException e)
      {
         Logger.log("SimpleFtp.disconnect(): " + e.getMessage());
      }
      catch(Exception e)
      {
         Logger.log("SimpleFtp.disconnect(): " + e.getMessage());
      }
      return vOK;
   }

   /**
   * Send a command to the host, and read the response.
   *
   * @param command   String - command
   */
   private String command(String aCommand) throws IOException, Exception
   { 
      if (aCommand == null || aCommand.length() < 3)
      {
         String vMsg = "SimpleFtp.command(): ";
         vMsg += "parameter [aCommand] was blank.";
         Logger.log(vMsg);
         throw new Exception(vMsg);
      }
      if (mPrintWriter == null)
      {
         String vMsg = "SimpleFtp.command(): ";
         vMsg += "member [mPrintWriter] has not been created.";
         throw new Exception(vMsg);
      }
      if (mInputStream == null)
      {
         String vMsg = "SimpleFtp.command(): ";
         vMsg += "member [mInputStream] has not been created.";
         throw new Exception(vMsg);
      }

      // This sends a dialog string to the server, returns reply
      // V2.0 Updated to parse multi-string responses a la RFC 959
      // Prints out only last response string of the lot.
      if (mPause) // i.e. we already issued a request, and are
                  // waiting for server to reply to it.  
      {
         if (mInputStream != null)
         {
            String vDiscard = mInputStream.readLine(); // will block here
            // preventing this further client request until server
            // responds to the already outstanding one.
            if (DEBUG)
            {
               System.out.println("keeping handler in sync" +
                            " by discarding next response: ");
               System.out.println(vDiscard);
            }
            mPause = false;
         }
      }

      mPrintWriter.print(aCommand + "\r\n" );
      mPrintWriter.flush(); 
      String vResponse = response(aCommand);
      char c = vResponse.charAt(0);
      if ((c != '1') && (c != '2') && (c != '3'))
      {
         String vMsg = "SimpleFtp.command(): command [";
         vMsg += aCommand + "] returned an error: " + vResponse;
         throw new Exception(vMsg);
      }

      return vResponse;
   }

   /**
    * New method to read multi-line responses from the host.
    * response: takes a String command or null and returns
    * all the lines of a possibly multi-line response.
    */
   private String response(String aCommand) throws IOException, Exception
   {

      String vResponse = mInputStream.readLine();
      if (vResponse == null || vResponse.length() == 0) // timed out!
         throw new Exception("SimpleFtp.response(): response timed out.");

      // handle more than one line returned
      String vReply = this.responseParser(vResponse);
      if (vReply == null || vReply.length() < 3)
      {
         String vMsg = "SimpleFtp.response(): ";
         vMsg += "unable to parse response [" + vResponse + "].";
         Logger.log(vMsg);
         throw new Exception(vMsg);
      }

      String vCode = vReply.substring(0, 3);
      String vHyphen = null;
      if (vReply.length() > 3)
         vHyphen = vReply.substring(3, 4);

      String vLine = null;
      if(vHyphen != null && vHyphen.equals("-"))
      {
         String vTestCode = vCode + " ";
         boolean vDone = false;
         while(!vDone)
         {
            // read lines til finds last line
            vLine = mInputStream.readLine();
            // Read "over" blank line responses
            while (vLine.length() < 3) 
            {
               if (vLine.length() > 0)
                  vReply += vLine;
               vLine = mInputStream.readLine();
            }

            vReply += vLine;
            if (vLine.length() == 3)
               vDone = true;
            // If next starts with vTestCode, we're done
            else if(vLine.substring(0,4).equals(vTestCode))
               vDone = true;
         }
      }

      if(DEBUG)
      {
         if(aCommand != null)
            System.out.println("Response to: "+aCommand+" was: "+vReply);
         else
            System.out.println("Response was: "+vReply);
      }

      return vReply;
   }

   /**
    * responseParser: check first digit of first line of response
    * and take action based on it; set up to read an extra line
    * if the response starts with "1"
    */
   private String responseParser(String aResponse) throws IOException
   {
      if (aResponse == null || aResponse.length() < 3)
      {
         String vMsg = "SimpleFtp.responseParser(): ";
         vMsg += "parameter [aResponse] was blank.";
         Logger.log(vMsg);
         return null;
      }

      // Check first digit of aResponse, take appropriate action.
      String vDigit1 = aResponse.substring(0, 1);
      if(vDigit1.equals("1"))
      {
         // server to act, then give response
         if(DEBUG)
            System.out.println("in 1 handler");
         // set mPause
         mPause = true;
         return aResponse;
      }
      else if(vDigit1.equals("2"))
      {
         // do usual handling
         if(DEBUG) 
            System.out.println("in 2 handler");
         // reset mPause
         mPause = false;
         return aResponse;
      }
      else if(vDigit1.equals("3"))
      {
         if(DEBUG)
            System.out.println("in 3 handler");
         return aResponse;
      }
      else if(vDigit1.equals("4") || vDigit1.equals("5"))
      {
         // error codes returned
         if(DEBUG)
            System.out.println("in 4-5 handler: error code");
         return aResponse;
      }

      // all other codes are not covered, so return null
      return null;
   }

   private String get(String aCommand) throws IOException, Exception
   {
      if (aCommand == null || aCommand.length() == 0)
      {
         String vMsg = "SimpleFtp.get(): ";
         vMsg += "parameter [aCommand] was blank.";
         return null;
      }
      if (mGetFilename == null || mGetFilename.length() == 0)
      {
         String vMsg = "SimpleFtp.get(): ";
         vMsg += "member [mGetFilename] is blank.";
         Logger.log(vMsg);
         return null;
      }
      if (mStoreDirectory == null || mStoreDirectory.length() == 0)
      {
         String vMsg = "SimpleFtp.get(): ";
         vMsg += "member [mStoreDirectory] is blank.";
         Logger.log(vMsg);
         return null;
      }

      Socket vSocket = getDataSock();
      InputStream vInputStream = vSocket.getInputStream();
      command(aCommand);

      // Create the pathname to store the file retrieved from a ftp site.
      String vFilename = mStoreDirectory.substring(0);
      if (vFilename.charAt(vFilename.length()-1) != File.separatorChar)
         vFilename += File.separator;
      vFilename += mGetFilename;
      RandomAccessFile vOutfile = new RandomAccessFile(vFilename, "rw");

      byte[] vByteBuffer = new byte[1024];
      int vLength = 0;
      while((vLength = vInputStream.read(vByteBuffer)) != -1)
         vOutfile.write(vByteBuffer, 0, vLength);

      vOutfile.close();
      vSocket.close();
      vSocket = null;

      return vFilename;
   }

   private boolean put(String aCommand, String aData)
                        throws IOException, Exception
   {
      if (aCommand == null || aCommand.length() == 0)
      {
         String vMsg = "SimpleFtp.put(): ";
         vMsg += "parameter [aCommand] was blank.";
         return false;
      }
      if (aData == null || aData.length() == 0)
      {
         String vMsg = "SimpleFtp.put(): ";
         vMsg += "parameter [aData] was blank.";
         return false;
      }

      Socket vSocket = getDataSock();
      OutputStream vOutputStream = vSocket.getOutputStream();
      byte[] vByteStream = aData.getBytes();
//    DataOutputStream vDataOS = new DataOutputStream(vOutputStream);
      command(aCommand);

      vOutputStream.write(vByteStream);
      vOutputStream.flush();
      vOutputStream.close();
/*
      vDataOS.writeBytes(aData);
      vDataOS.flush();
      vDataOS.close();
*/
      vSocket.close();
      vSocket = null;

      return true;
   }

   private void setTransferType(boolean aType) throws IOException, Exception
   {
      // set file transfer type
      String vTransType = (aType? "A" : "I");
      command("TYPE " + vTransType);
   }  

   private Socket getDataSock() throws IOException, Exception
   {
      // Go to PASV mode, capture server reply, parse for socket setup
      // V2.1: generalized port parsing, allows more server variations
      String vReply = command("PASV");

      // New technique: just find numbers before and after ","!
      StringTokenizer vTokenizer = new StringTokenizer(vReply, ",");
      String[] vParts = new String[6]; // parts, incl. some garbage
      int i = 0; // put tokens into String array
      while(i < vParts.length && vTokenizer.hasMoreElements()) 
      {
         // stick pieces of host, port in String array
         try
         {
            vParts[i] = vTokenizer.nextToken();
            i++;
         } 
         catch(NoSuchElementException nope)
         {
            String vMsg = "SimpleFtp.getDataSock(): unable to parse ";
            vMsg += "command [PASV] response [" + vReply + "].";
            throw new Exception(vMsg);
         }
      } // end getting parts of host, port

      // Get rid of everything before first "," except digits
      String[] vPositionNumber = new String[3];
      for(int j = 0; j < 3; j++) 
      {
         // Get 3 characters, inverse order, check if digit/character
         vPositionNumber[j] = vParts[0].substring(vParts[0].length() - (j + 1),
                     vParts[0].length() - j); // next: digit or character?
         if(!Character.isDigit(vPositionNumber[j].charAt(0)))
            vPositionNumber[j] = "";
      }
      vParts[0] = vPositionNumber[2] + vPositionNumber[1] + vPositionNumber[0];
      // Get only the digits after the last ","
      String[] porties = new String[3];
      for(int k = 0; k < 3; k++) 
      {
         // Get 3 characters, in order, check if digit/character
         // May be less than 3 characters
         if((k + 1) <= vParts[5].length())
            porties[k] = vParts[5].substring(k, k + 1);
         else
         {
            String vMsg = "SimpleFtp.getDataSock(): wrong format for ";
            vMsg += "response [" + vReply + "] to command [PASV].";
            throw new Exception(vMsg);
         }
         // next: digit or character?
         if(!Character.isDigit(porties[k].charAt(0)))
            porties[k] = "";
      }
      // Have to do this one in order, not inverse order
      vParts[5] = porties[0] + porties[1] + porties[2];
      // Get dotted quad IP number first
      String vIP = vParts[0] + "." + vParts[1] + "." + vParts[2]
                  + "." + vParts[3];

      // Determine port
      int vPort = -1;
      try 
      { 
         // Get first part of port, shift by 8 bits.
         int big = Integer.parseInt(vParts[4]) << 8;
         int small = Integer.parseInt(vParts[5]);
         vPort = big + small; // port number
      } 
      catch(NumberFormatException nfe) 
      {
         String vMsg = "SimpleFtp.getDataSock(): unable to convert ";
         vMsg += "a number from response [" + vReply + "].";
         throw new Exception(vMsg);
      }

      Socket vSocket = null;
      if((vIP != null) && (vPort != -1))
         vSocket = new Socket(vIP, vPort);
      else 
      {
         String vMsg = "SimpleFtp.getDataSock(): unable to extract a port";
         vMsg += " number or IP address from response [" + vReply + "].";
         throw new Exception(vMsg);
      }

      return vSocket;
   }

   public static void main(String[] args)
   {
      SimpleFtp ftp = null;
      boolean vConnected = false;
      try
      {
/*       String vHost = "ftp.edgar-online.com";
         String vPassword = "s3cd4t4";
         String vUser = "b4utrade";
         String vHost = "209.95.221.196";
         String vPassword = "hy45623";
         String vUser = "b4utrade";
         String vHost = "209.95.221.196";
         String vPassword = "10883ff";
         String vUser = "lb4utrade";*/
/*       String vHost = "netearnings.net";
         String vPassword = "b4u0120";
         String vUser = "netearn-b4utrade";
         String vHost = "axp.zacks.com";
         String vUser= "b4utradecom";
         String vPassword = "8A4t443CW0C";*/
         String vHost = "tacpoint.com";
         String vUser = "kgentes";
         String vPassword = "tacpo1nt";

         ftp = new SimpleFtp(vHost, vUser, vPassword, 'A');
         vConnected = ftp.connect();
         if (!vConnected)
            throw new Exception("Could not connect to [" + vHost + "].");
         System.out.println("main(): connected");
//       String vList;
/*       ftp.setDirectory("raw");
         ftp.setRetrieveFilenamePath("dir.txt", ".");
         vList = ftp.getDirectoryList();
         System.out.println(vList);
         ftp.setRetrieveFilenamePath("0001108965_0001108965_00_000002.txt", ".");
         vList = ftp.receiveData();
         System.out.println(vList);
         ftp.setRetrieveFilenamePath("0001095603_0001095603_00_000012.txt", ".");
         vList = ftp.receiveData();
         System.out.println(vList);
         ftp.setRetrieveFilenamePath("company_4107.txt", ".");
         vList = ftp.receiveData();
         System.out.println(vList);
         ftp.setRetrieveFilenamePath("company_4006.txt", ".");
         vList = ftp.receiveData();
         System.out.println(vList);
         ftp.setRetrieveFilenamePath("lockups.txt", ".");
         vList = ftp.receiveData();
         System.out.println(vList);
         ftp.setDirectory("/sys$client/download/brokcovr");
         ftp.setRetrieveFilenamePath("current.bcv", ".");
         vList = ftp.receiveData();
         System.out.println(vList);
         ftp.setDirectory("/web$user/investor/fund/ais/1");
         ftp.setRetrieveFilenamePath("0003ai01.zip", ".");
         vList = ftp.receiveData();
         System.out.println(vList);*/
/*       ftp.setDirectory("/sys$client/download/brokcovr");
         ftp.setRetrieveFilenamePath("dir.txt", ".");
         vList = ftp.getDirectoryList();
         ftp.setRetrieveFilenamePath("AVEARNINGS.TXT", ".");
         vList = ftp.receiveData();
         System.out.println(vList);*/

         ftp.setRetrieveFilenamePath("kimtest.txt", ".");
         BufferedReader vFile = new BufferedReader(new FileReader("C:\\Tacpoint\\com\\tacpoint\\stock\\StockItems.java"));
         String vLine, vAllLines = new String();
         while ((vLine = vFile.readLine()) != null)
         {
            vAllLines += vLine + "\n";
         }
         if (vAllLines.length() != 0)
         {
            ftp.sendData(vAllLines);
         }

      }
      catch(FileNotFoundException e)
      {
         System.out.println("Main: FileNotFoundException: " + e.getMessage());
      }
      catch(IOException e)
      {
         System.out.println("Main: IOException: " + e.getMessage());
      }
      catch(Exception e)
      {
         System.out.println("Main: Exception: " + e.getMessage());
      }
      finally
      {
         if (ftp != null && vConnected)
            ftp.disconnect();
      }

   }

}
