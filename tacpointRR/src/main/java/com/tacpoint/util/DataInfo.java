/** DataInfo.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999. 
* All rights reserved.
* @author Paul Kim
* @author pkim@tacpoint.com
* @version 1.0
* Date created:  2/9/2000
*
*	Description
* This class holds the field names and size of data feeds
*/
package com.tacpoint.util;
public class DataInfo
{
	private String mDataField;
	private int	mPutIntoQueue;
	private int mDataSize;
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Constructors
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public DataInfo()
	{
		mDataField = "";
		mDataSize=0;
	}
	public DataInfo(String aField, int aSize)
	{
		mDataField=aField;
		mDataSize= aSize;
	}
	
	public DataInfo(String aField, int aStatus, int aSize)
	{
		mDataField=aField;
		mPutIntoQueue = aStatus;
		mDataSize= aSize;
	}
	
	
	public void setData(String aField, int aStatus, int aSize)
	{
		mDataField=aField;
		mPutIntoQueue = aStatus;
		mDataSize= aSize;
	}
	
	public void setData(String aField,  int aSize)
	{
		mDataField=aField;
		mDataSize= aSize;
	}
	
	public int getSize()
	{
		return mDataSize;
	}
	
	public String getDataField()
	{
		return mDataField;
	}
	
	/**Description
	 * 1. Checks to see if the data field that is read should be put into queue.
	 * @return -true if the field is to put put into the messages
	 *				-false if nothing is to be done
	 */
	public boolean isToBeUsed()
	{
		if (mPutIntoQueue == 1)
			return true;
		else
			return false;
	}
	
}
