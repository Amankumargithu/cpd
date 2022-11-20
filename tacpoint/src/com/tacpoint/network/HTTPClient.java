package com.tacpoint.network;

/** HTTPClient.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999. 
* All rights reserved.
* @author Tushar Jain
* @author tushar@tacpoint.com
* @version 1.0
*
* Date created:  2/22/2000

*
*
* The HTTPClient class provides connections to HTML document using HTTP.
*
*/

import com.tacpoint.util.*;
import java.net.*;
import java.util.ArrayList;
import java.io.*;




/**
 * The HTTPClient class allows for connecting and reading from a HTML Server.
 *
 */
public class HTTPClient extends NetworkConnection
{
	////////////////////////////////////////////////////////////////////////////
	// D A T A	  M E M B E R S
	////////////////////////////////////////////////////////////////////////////

	/**
	 * A string used to hold the IP Address of the machine we're
	 * connecting to.
	 */
	private String mIPAddress;
	/**
	 * A URL used to hold the IP Address  contained in mIPAddress
	 */
	private URL mURL;
	/**
	 * A URLConnection used to open connection to the URL pointed by mURL
	 */
	private URLConnection mURLConn;

	/**
	 * User ID & password is set, then authentication is done.
	 */
	private String mUser = null;
	/**
	 * User ID & password is set, then authentication is done.
	 */
	private String mPassword = null;

	/**
	* Holds the text filename to hold data from http site.
	*/
	private String mFilename = "";
	/**
	* Holds the name of the directory to store the text file mentioned above.
	*/
	private String mDirectory = "";


	////////////////////////////////////////////////////////////////////////////
	// C O N S T R U C T O R S
	////////////////////////////////////////////////////////////////////////////

	/**
	 * HTTPClient constructor just initializes member data.
	 *
	 * @param aIPAddress  String containing IP Address.
	 *
	 */
	public HTTPClient(String aIPAddress) throws Exception
	{
		Logger.init();
//		Logger.log("HTTPClient constructor.");

		if (aIPAddress == null || aIPAddress.length() == 0)
		{
			String vMsg = "HTTPClient constructor: ";
			vMsg += "parameter [aIPAddress] was blank.";
			throw new Exception(vMsg);
		}

		mIPAddress	= aIPAddress;

	}

	public HTTPClient(String aIPAddress, String aUser, String aPassword)
								throws Exception
	{
		Logger.init();
//		Logger.log("HTTPClient constructor.");

		if (aIPAddress == null || aIPAddress.length() == 0)
		{
			String vMsg = "HTTPClient constructor: ";
			vMsg += "parameter [aIPAddress] was blank.";
			throw new Exception(vMsg);
		}

		mIPAddress	= aIPAddress;
		mUser = aUser;
		mPassword = aPassword;

	}

	////////////////////////////////////////////////////////////////////////////
	// M E T H O D S
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a stream to read the data from the IP address 
	 * specified in the constructor.
	 *
	 * @return True if successfully connected to HTTP Server.
	 *
	 */
	public boolean connect()
	{
//		Logger.log("HTTPClient.connect()");

		boolean vOK = false;

		try
		{
			mURL = new URL (mIPAddress + mFilename);
			if (mUser != null && mPassword != null)
				Authenticator.setDefault(new HttpAuthenticator(mUser, mPassword));
			mURLConn = mURL.openConnection();
			vOK = true;
		}
		catch (MalformedURLException e) 
		{
			Logger.log("HTTPClient.connect() failed: " + e.getMessage());
		}
		catch (IOException e)
		{
			Logger.log("HTTPClient.connect() failed: " + e.getMessage());
		}

		return vOK;
	}


	/**
	 * Closes the connection and the streams associated with the server.
	 *
	 * @return True if successfully disconnected from server.
	 *
	 */
	public boolean disconnect()
	{
//		Logger.log("HTTPClient.disconnect()");

		mURLConn = null;
		mURL = null;

		return true;
	}


	/**
	 * Write data 
	 *
	 * @param aData
	 *
	 * @return True if the data was successfully written to the socket.
	 *
	 */
	public boolean sendData(String aData)
	{
		boolean vReturn = true;

		// Delete when testing is done!
//		Logger.log("HTTPClient.sendData()");

		return vReturn;
	}


	 public ArrayList receiveParsedMessages(byte msgDelim) {
		 return null;
	 }
	 
	/**
	 * Reads data from the stream associated with the Server.
	 *
	 * @return A string containing the data read from the socket.
	 *
	 */
	public String receiveData()
	{
		if (mFilename == null || mFilename.length() == 0)
		{
			String vMsg = "HTTPClient.receiveData: ";
			vMsg += " member variable [mFilename] was not set.";
			Logger.log(vMsg);
			return new String("");
		}
		if (mDirectory == null || mDirectory.length() == 0)
		{
			String vMsg = "HTTPClient.receiveData: ";
			vMsg += "member variable [mDirectory] was not set.";
			Logger.log(vMsg);
			return new String("");
		}

//		Logger.log("HTTPClient.receiveData()");
		String vFilePath = null;
		try
		{
			BufferedReader vBuff = new BufferedReader(new InputStreamReader(
														mURLConn.getInputStream()));
			// write to a temp file for test
			if (mDirectory.charAt(mDirectory.length()-1) != File.separatorChar)
				vFilePath = mDirectory + File.separator + mFilename;
			else
				vFilePath = mDirectory + mFilename;
			File outputFile = new File(vFilePath);
			FileWriter out = new FileWriter(outputFile);

			String inputLine;
			inputLine = vBuff.readLine();

			while (inputLine != null)
			{
				out.write (inputLine + '\n');
				inputLine = vBuff.readLine();
			}
			vBuff.close();
			out.close ();
		}
		catch (IOException e) 
		{	 
			Logger.log("HTTPClient.receiveData() failed: " + e.getMessage());
			vFilePath = null;
		}		

		return vFilePath;
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
	* The filename to retrieve from a http site must be set before calling
	* 'receiveData()' function.  
	*/
	public void setRetrieveFilenamePath(String aFilename, String aDirectory)
	{
		if (aFilename == null || aFilename.length() == 0)
		{
			String vMsg = "HTTPClient.setRetrieveFilenamePath: ";
			vMsg += "parameter [aFilename] was blank.";
			Logger.log(vMsg);
			return;
		}
		if (aDirectory == null || aDirectory.length() == 0)
		{
			String vMsg = "HTTPClient.setRetrieveFilenamePath: ";
			vMsg += "parameter [aDirectory] was blank.";
			Logger.log(vMsg);
			return;
		}

		mFilename = aFilename;
		mDirectory = aDirectory;
	}

	////////////////////////////////////////////////////////////////////////////
	// T E S T I N G
	////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		boolean vOK  = false;
		String  vRet = null;

		try
		{
			Logger.init();
			Logger.log("HTTPClient.main()");

			HTTPClient http = new HTTPClient("http://www.briefing.com/intro_private/iupdown.htm");
			//HTTPClient http = new HTTPClient("http://www.briefing.com/intro_private/upgrades.txt");
			http.connect ();
			http.setRetrieveFilenamePath("briefings.txt", "C:\\Tacpoint\\B4UTrade\\Briefings");
			vRet = http.receiveData ();
			http.disconnect();
		}
		catch(Exception e)
		{
			System.out.println("HTTPClient.main(): " + e.getMessage());
		}
	}
}

