package com.tacpoint.network;


/** NetworkConnection.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999.
* All rights reserved.
* @author Kim Gentes
* @author kimg@tacpoint.com
* @version 1.0
* Date created:  12/23/1999
* - 01/05/2000
*	  - Use Logger classs (KDN)
*
*
* NetworkConnection provides the specification for a network connection.
* i.e. sockets, pipes, etc.
*
*/

import java.util.ArrayList;

import com.tacpoint.util.*;


public abstract class NetworkConnection
{
	NetworkConnection()
	{
		try
		{
			Logger.init();
			Logger.log("NetworkConnection constructor.");
		}
		catch(Exception e)
		{
			String vMsg;
			vMsg = "NetworkConnection constructor: Unable to init Logger.";
			System.out.println(vMsg);
		}
	}

	/**
	 * Establishes a network connection.
	 */
	public abstract boolean connect();


	/**
	 * Terminates a network connection.
	 */
	public abstract boolean disconnect();


	/**
	 * Sends data to the network connection.
	 *
	 * @param  A string containing the data to send.
	 *
	 * @return	true if data was sent; false, otherwise.
	 *
	 */
	public abstract boolean sendData(String aData);


	/**
	 * Receives data from the network connection.
	 *
	 * @return A string containing the data received.
	 *
	 */
	public abstract String receiveData();
	public abstract byte[] receiveDataAsBytes();
	public abstract ArrayList receiveParsedMessages(byte msgDelim);

	public void setRetrieveFilenamePath(String aFilename, String aDirectory)
	{
	}

	public boolean setDirectory(String aDirectory) { return false; }
	public String getDirectoryList() { return null; }
	public boolean setTimeout(int aTimeout) { return false; }
   public void startConnectionCheck() {};
   public void stopConnectionCheck() {};

}


