package com.tacpoint.network;

/** SocketClient.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999.
* All rights reserved.
* @author Kim Gentes
* @author kimg@tacpoint.com
* @version 1.0
*
* Date created:  12/23/1999
* - 01/05/2000
*     - Use Logger class (KDN)
*     - Make the code work (KDN)
*
*
* The SocketClient class provides client-side socket connections.
*
*/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import com.tacpoint.util.Logger;




/**
 * The SocketClient class allows for connection to, writing to,
 * and reading from a socket.
 *
 */
public class SocketClient extends NetworkConnection
{

    String BLANK = " ";

    ////////////////////////////////////////////////////////////////////////////
    // D A T A    M E M B E R S
    ////////////////////////////////////////////////////////////////////////////

    byte[]  vBuffer   = new byte[8192];
    String EMPTY = "";
    long beginTime = 0;
    long mBytesRead = 0;



    /**
     * A string used to hold the IP Address of the machine we're
     * connecting to.
     */
    private String mIPAddress;


    /**
     * An integer used to hold the port number of the machine we're
     * connecting to.
     */
    private int mPortNum;


    /**
     * A Socket instance used to maintaining a reference to the open socket.
     */
    private Socket mSocket;


    /**
     * An input stream used to read from the socket.
     */
    private BufferedInputStream mInputStream;


    /**
     * An output stream used to write to the socket.
     */
    private BufferedOutputStream mOutputStream;
    
    
    private byte[] readBuffer = new byte[51*1024*1024];
    private ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 10);
    private byte[] msgBuffer = new byte[1024 * 1024];
    private int msgBufferIndex = 0;



    ////////////////////////////////////////////////////////////////////////////
    // C O N S T R U C T O R S
    ////////////////////////////////////////////////////////////////////////////

    /**
     * SocketClient constructor that just initializes member data.
     *
     * @param aIPAddress  String containing IP Address.
     * @param aPortNum   Integer containing the port number.
     *
     */
    public SocketClient(String aIPAddress, int aPortNum) throws Exception
    {
        try
        {
            Logger.init();
//          Logger.log("SocketClient constructor.");
        }
        catch(Exception e)
        {
            String vMsg;
            vMsg = "SocketClient constructor: Unable to init Logger.";
            System.out.println(vMsg);
            throw new Exception(vMsg);
        }

        if (aIPAddress == null || aIPAddress.length() == 0)
        {
            String vMsg = "SocketClient constructor: ";
            vMsg += "parameter [aIPAddress] was blank.";
            throw new Exception(vMsg);
        }
        if (aPortNum <= 0)
        {
            String vMsg = "SocketClient constructor: ";
            vMsg += "parameter [aPortNum] was invalid.";
            throw new Exception(vMsg);
        }

        mIPAddress  = aIPAddress;
        mPortNum    = aPortNum;

        mSocket       = null;
        mInputStream  = null;
        mOutputStream = null;
    }



    ////////////////////////////////////////////////////////////////////////////
    // M E T H O D S
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a stream socket and connects it to the port number
     * and IP address specified in the constructor.
     *
     * @return True if successfully connected to socket.
     *
     */
    public boolean connect()
    {
//      Logger.log("SocketClient.connect()");

        boolean vOK = false;
        try
        {
            mSocket       = new Socket(mIPAddress, mPortNum);
            mSocket.setReceiveBufferSize(1024 * 1024);
            mInputStream  = new BufferedInputStream(mSocket.getInputStream(),50*1024*1024);
            mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
            vOK = true;
        }
        catch (InterruptedIOException e)
        {
            Logger.log("SocketClient.connect() timed out. " + e.getMessage());
        }
        catch (IOException e)
        {
            Logger.log("SocketClient.connect() failed: " + e.getMessage());
        }

        return vOK;
    }


    /**
     * Closes the socket and the streams associated with the socket.
     *
     * @return True if successfully disconnected from socket.
     *
     */
    public boolean disconnect()
    {
      boolean vOK = false;

        try
        {
           if (mInputStream != null)
           {
              mInputStream.close();
              mInputStream = null;
           }
           if (mOutputStream != null)
           {
              mOutputStream.close();
              mOutputStream = null;
           }
           if (mSocket != null)
           {
               mSocket.close();
            mSocket = null;
        }

            vOK = true;
        }
        catch (IOException e)
        {
            Logger.log("SocketClient.disconnect() failed.");
        }

        return vOK;
    }

    public boolean setTimeout(int aTimeout)
    {
        boolean vOK = false;
        try
        {
            mSocket.setSoTimeout(aTimeout);
            vOK = true;
        }
        catch (SocketException e)
        {
            Logger.log("SocketClient.setTimeout(): " + e.getMessage());
        }
        return vOK;
    }

    /**
     * Write data (a string) to the stream associated with the open socket.
     *
     * @param aData
     *
     * @return True if the data was successfully written to the socket.
     *
     */
    public boolean sendData(String aData)
    {
        boolean vReturn = false;

      if (mOutputStream == null)
         return false;

        // TO DO!!
        if (aData != null)
        {
            try
            {
                byte     vToSend[];
                int  vLen;

                vToSend = aData.getBytes();
                vLen = aData.length();

                // Write to the socket.
                mOutputStream.write(vToSend, // Bytes to write.
                                          0,          // Offset.
                                          vLen);   // Length of data.

                // Flush to indicate no more data for now.
                mOutputStream.flush();

                vReturn = true;
            }
            catch (IOException e)
            {
                Logger.log("SocketClient.sendData() failed.");
            }
        }

        return vReturn;
    }



    /**
     * Reads data from the stream associated with the socket.
     *
     * @return A string containing the data read from the socket.
     *
     */
    public String receiveData()
    {

        //String vMsg;
        int  vBytesRead = 0;
        int  vAvailable = 0;

        try
        {
            vBytesRead = mInputStream.read(vBuffer, 0, 8192);

            /*
            do
            {
                //mOutputStream.flush();

                // Find out what's available on the input stream.
                vAvailable = mInputStream.available();

                

                //if (vAvailable > 10240)
                //vMsg    = "SocketClient.receiveData(): ";
                //vMsg += "Available = " + vAvailable;
                //System.out.println(vMsg);

                if (vAvailable > 0)
                {
                    //System.out.println("Bytes available from socket: "+vAvailable);
                    //vBuffer = new byte[vAvailable];

                        // Read the data from the input stream.
                        vBytesRead = mInputStream.read(vBuffer, 0, vBytesRead);

                        //vMsg    = "SocketClient.receiveData(): ";
                        //vMsg += "Bytes Read = " + vBytesRead;
                }
            }
            while (vAvailable < 1);
            */
        }
        catch (InterruptedIOException e)
        {
            Logger.log("SocketClient.receiveData() timed out. " + e.getMessage());
        }
        catch (IOException e)
        {
            Logger.log("SocketClient.receiveData() failed. " + e.getMessage());
        }

        //if (vAvailable < 1)
        //    //return new String("");
        //    return null;


        if (vBytesRead > 0) {
           return new String(vBuffer,0,vBytesRead);
        }

        return null;
    }

   
    public ArrayList receiveParsedMessages(byte msgDelim) {
    	
    	ArrayList results = null;
    	
    	try {    
    		
    		int bytesRead = mInputStream.read(readBuffer);
    		
    		// AK 8/16/11 - use average size of a CSP message to set the ArrayList size ...
    		
    		int arraySize = (bytesRead / 50) + 25;
    		
    		results = new ArrayList(arraySize);
    		
    		//System.out.println("Est array size : "+arraySize);
    		
    		
    		mBytesRead += bytesRead;

    		if (bytesRead > 0) {
    		   for (int i=0; i<bytesRead; i++) {
    			   if (readBuffer[i] == msgDelim) {
    				   
    				   byte[] completeMsg = new byte[msgBufferIndex];
    				   System.arraycopy(msgBuffer,0,completeMsg,0,msgBufferIndex);    				   
    				   msgBufferIndex = 0;
    				   
    				   results.add(completeMsg);
    			   }
    			   else {
    				   msgBuffer[msgBufferIndex++] = readBuffer[i];
    			   }
    		   }
    		}
    		
    		/*
    		if (bytesRead > 0) {
     		   for (int i=0; i<bytesRead; i++) {
     			   if (readBuffer[i] == msgDelim) {
     				   //results.add(baos.toByteArray());
     				   baos.reset();
     			   }
     			   else {
     				   baos.write(readBuffer[i]);
     			   }
     		   }
     		}
     		*/
        		
    		long currentTime = System.currentTimeMillis();
    		if ((currentTime - beginTime) > 1000) {
    			Logger.log("SocketClient - bytes read : "+(mBytesRead / 1024)+" kb");
    			mBytesRead = 0;
    			beginTime = currentTime;
    		}
    	}
    	catch (Exception e) {
    		Logger.log("SocketClient.receiveParsedMessages() failed. " + e.getMessage());
    	}
    	
    	//System.out.println("Actual array size : "+results.size());
    	
    	return results;  
    }
    
    public byte[] receiveDataAsBytes()  {
    	
    	try {    		
    		int bytesRead = mInputStream.read(readBuffer);
    		
    		//mBytesRead += bytesRead;
    		
    		if (bytesRead > 0) {
    		   byte[] results = new byte[bytesRead];
    		   for (int i=0; i<bytesRead; i++)
    			   results[i] = readBuffer[i];
    		   
    		    /*
       			long currentTime = System.currentTimeMillis();
       			if ((currentTime - beginTime) > 1000) {
       				Logger.log("SocketClient - bytes read : "+(mBytesRead / 1024)+" kb");
       				mBytesRead = 0;
       				beginTime = currentTime;
       			}
       			*/
    		
    		   return results;
    		}
    	}
    	catch (Exception e) {
    		Logger.log("SocketClient.receiveDataAsBytes() failed. " + e.getMessage());
    	}
    	return null;    	
    }
    
    
    /*

    public byte[] receiveDataAsBytes()
    {
        int  vBytesRead = 0;
        int  vAvailable = 0;
        byte[] buffer = null;


        try
        {
            do
            {
                vAvailable = mInputStream.available();
                if (vAvailable > 0)
                {
                    buffer = new byte[vAvailable];
                    vBytesRead = mInputStream.read(buffer, 0, vAvailable);
                }
            }
            while (vAvailable < 1);
        }
        catch (InterruptedIOException e)
        {
            Logger.log("SocketClient.receiveData() timed out. " + e.getMessage());
        }
        catch (IOException e)
        {
            Logger.log("SocketClient.receiveData() failed. " + e.getMessage());
        }

        if (vAvailable > 0) {
           return buffer;
        }

        return null;
    }
    */



    ////////////////////////////////////////////////////////////////////////////
    // T E S T I N G
    ////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args)
    {
        boolean vOK  = false;
        byte[]  vRet = null;

        try
        {
            Logger.init();
            Logger.log("SocketClient.main()");
        }
        catch(Exception e)
        {
            System.out.println("SocketClient.main(): Failed to init Logger");
        }

        try
        {
        NetworkConnection sc = new SocketClient("4.20.80.98", 4000);
//      NetworkConnection sc = new SocketClient("207.251.72.54", 26471);
        if (!sc.connect())
            return;

/*      PrintWriter vFile = new PrintWriter(new BufferedWriter(
                                            new FileWriter("Island.txt")));

        /////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////
        String vMsg = "LD01301ykjkhh    " + "          " + "         0";
        vMsg += (char)10;
        byte[] vTest = vMsg.getBytes();
        vOK = sc.sendData(vMsg);
        if (!vOK)
        {
            sc.disconnect();
            return;
        }
        vRet = sc.receiveByteData();
        String vReturn = new String(vRet);
        if (vReturn == null || !(vReturn.charAt(0) == 'A'))
        {
            sc.disconnect();
            return;
        }
        vFile.println("line: " + vReturn);
        for (int i=0; i < 10; i++)
        {
            Thread.sleep(3000);
            vRet = sc.receiveByteData();
            if (vRet == null)
                continue;
            vReturn = new String(vRet);
            vFile.println("line: " + vReturn);
        }
        vMsg = "\n";
        vTest = vMsg.getBytes();
        vOK = sc.sendData(vMsg);
        vFile.close();*/

        vOK = sc.sendData("Set_User{b4utrade{b4utrade}");
        if (vOK == true)
        {
            String vStrRet = sc.receiveData();
            Logger.log("Return Data: " + vRet);
        }

        vOK = sc.sendData("Set_User{exit{}");
        /////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////

        sc.disconnect();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}

