/** Tokernizer.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999. 
* All rights reserved.
* @author Paul Kim
* @author pkim@tacpoint.com
* @version 1.0
* Date created:  2/9/1999
*
* Provides parsing for strings
*
*/
package com.tacpoint.util;
public class Tokenizer
{
	String	mLine;
	int		mCount;
	int		mLength;
	String mTok;
	
	//member variable that keeps track of the current position
	int		mCurLoc;
	
	public Tokenizer()
	{
		mLength = mLine.length();
		mCurLoc = 0;
	}
	public Tokenizer(String aLine)
	{
		mLine = aLine;
		mLength = mLine.length();
		mCurLoc = 0;
	}
	
	public void setToken(String aTok)
	{
	    if (aTok!= null && aTok.length()!=0)
	        mTok = aTok;
	}
	
	public Tokenizer(String aLine, String aTok)
	{
		mLine = aLine;
		mTok = aTok;
		mLength = mLine.length();
		mCurLoc = 0;
	}
	
	public String nextToken(String aTok)
	{
		String	ch="",
				stBuff = "";
		do
		{	
			ch = mLine.substring(mCurLoc,mCurLoc + 1);
			if (ch.compareTo(aTok)!=0)
				stBuff= stBuff + ch;
			mCurLoc++;
		}while (ch.compareTo(aTok)!=0 && (mCurLoc < mLength));
		return new String(stBuff);
	}
	
	public String nextToken()
	{
		String	ch="",
				stBuff = "";
		do
		{	
			ch = mLine.substring(mCurLoc,mCurLoc + 1);
			if (ch.compareTo(mTok)!=0)
				stBuff= stBuff + ch;
			mCurLoc++;
		}while (ch.compareTo(mTok)!=0 && (mCurLoc < mLength));
		return new String(stBuff);
	}
		
	public String nextMultiToken(int tokenLength)
	{
		String	check="",
		        checked="", 
	          stBuff = "";
		do
		{	
			check = mLine.substring(mCurLoc,mCurLoc + tokenLength);
			checked = mLine.substring(mCurLoc,mCurLoc + 1);
			if (check.compareTo(mTok)!=0)
			{
				stBuff= stBuff + checked;
				mCurLoc ++ ;
		  }
		  else
		  {
			   mCurLoc = mCurLoc + tokenLength;
			}
		}while (check.compareTo(mTok)!=0 && (mCurLoc < mLength));
		return new String(stBuff);
	}	
	
	public void setString(String aLine)
	{
		mLine = aLine;
		mLength = mLine.length();
		mCurLoc = 0;
	}
	
	public boolean hasMoreElements()
	{
		if (mCurLoc < mLength)
			return true;
		else
			return false;
	}
	
	public boolean isEndOfString()
	{
		if (mCurLoc == mLength)
			return true;
		else
			return false;
	}
}
