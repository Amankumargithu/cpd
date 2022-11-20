/**
 * QuoteRequest.java
 * Copyright: Tacpoint Technologies, Inc. (c), 2000.  All rights reserved.
 * @author John Tong
 * @author jtong@tacpoint.com
 * @version 1.0
 * Date created:  06.09.2000
 */

package com.b4utrade.stockutil;

import java.io.Serializable;

public final class QuoteRequest implements Serializable
{
   public final static int GET_STOCK_QUOTE = 1;
   public final static int GET_ECNSTOCK_QUOTE = 2;
   public final static int GET_MARKET_MAKER_LISTS = 3;

   private String mTickers;
   private int mRequest;

   public QuoteRequest()
   {
   }
   public QuoteRequest(String aTickers, int aRequest)
   {
      mTickers = aTickers;
      mRequest = aRequest;
   }

   public void setTickers(String aTickers)
   {
      mTickers = aTickers;
   }

   public void setRequest(int aRequest)
   {
      mRequest = aRequest;
   }

   public String getTickers()
   {
      return(mTickers);
   }

   public int getRequest()
   {
      return(mRequest);
   }

/*	public static byte[] changeRequestToStream(QuoteRequest aRequest)
	{
		if (aRequest == null)
			return null;
		if (aRequest.mTicker == null || aRequest.mTicker.length() == 0)
			return null;

		StringBuffer vRequestStream = new StringBuffer();
		vRequestStream.append(aRequest.mTicker);
		vRequestStream.append(',');
		vRequestStream.append(aRequest.mRequest);

      // Append "||" (end of record indicator).
      vRequestStream.append("||");

		return vRequestStream.toString().getBytes();
	}

	public static QuoteRequest changeStreamToRequest(String aStream)
	{
      if (aStream == null || aStream.length() == 0)
         return null;

		// Get ticker
		int vOldIndex = 0;
		int vIndex = aStream.indexOf(',');
		if (vIndex <= 0)
			return null;

		QuoteRequest vRequest = new QuoteRequest("N/A", 1);
		vRequest.mTicker = aStream.substring(vOldIndex, vIndex);

		vOldIndex = vIndex + 1;
		if (vOldIndex >= aStream.length())
			return null;

      vRequest.mRequest = Integer.parseInt(aStream.substring(vOldIndex));

		return vRequest;
	}
*/

}
