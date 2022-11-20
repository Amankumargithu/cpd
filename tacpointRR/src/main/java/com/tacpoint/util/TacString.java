package com.tacpoint.util;

/** TacString.java
* Copyright: Tacpoint Technologies, Inc. (c), 1999.
* All rights reserved.
* @author Kim Gentes
* @author kimg@tacpoint.com
* @version 1.0
* Date created:  4/10/2000
*/

import java.util.*;


/**
 * String functions
 */
public class TacString
{
	////////////////////////////////////////////////////////////////////////////
	// D A T A	 M E M B E R S
	////////////////////////////////////////////////////////////////////////////


	////////////////////////////////////////////////////////////////////////////
	// C O N S T R U C T O R S
	////////////////////////////////////////////////////////////////////////////

	/** Constructor:
	*
	* @exception Exception Passes exception encountered up to the calling
	*						method.
	*/
	TacString() throws Exception
	{
		Logger.init();

		// Delete when testing is done!
		Logger.log("TacString constructor.");

	}

	/**
	* Takes a string and inserts 'aInserChar' before every 'aSearchChar'
	*
	* @return String   a new formatted string
	*/
	public static String insert(String aOldString,
										char aSearchChar, char aInsertChar)
	{
		if (aOldString == null || aOldString.length() == 0)
			return new String();

		if (aOldString.indexOf(aSearchChar) < 0)
			return aOldString;

		StringBuffer vNewString = new StringBuffer(aOldString);
		for (int i=0; i < vNewString.length(); i++)
		{
			if (vNewString.charAt(i) == aSearchChar)
				vNewString.insert(i++, (char)aInsertChar);
		}

		return vNewString.toString();
	}

	/**
	* Takes a string and replaces all occurances of 'aSearchString' with 
	* 'aReplaceString'.  If 'aReplaceString' is null, then all occurances
	* of 'aSearchString' will be deleted.
	*
	* @return String   a new formatted string
	*/
	public static String replace(String aOldString,
					String aSearchString, String aReplaceString)
	{
		if (aOldString == null || aOldString.length() == 0)
			return new String();
		if (aSearchString == null || aSearchString.length() == 0)
			return new String();

		if (aOldString.indexOf(aSearchString) < 0)
			return aOldString;

		int vIndex=0, vOldIndex=0;
		int vLen = aOldString.length();
		StringBuffer vNewString = new StringBuffer();

		while (vOldIndex < vLen)
		{
			vIndex = aOldString.indexOf(aSearchString, vOldIndex);
			if (vIndex < 0)
				vIndex = vLen;
			for (; vOldIndex < vIndex; vOldIndex++)
				vNewString.append((char)aOldString.charAt(vOldIndex));

			if (vIndex < vLen)
			{
				vOldIndex = vIndex + aSearchString.length();
				if (aReplaceString != null && aReplaceString.length() > 0)
					vNewString.append(aReplaceString);
			}

		}

		return vNewString.toString();
	}

	/**
	* Takes a string and replaces all occurances of 'aSearchChar' with 
	* 'aReplaceString'.  If 'aReplaceString' is null, then all occurances
	* of 'aSearchChar' will be deleted.
	*
	* @return String   a new formatted string
	*/
	public static String replace(String aOldString,
							char aSearchChar, String aReplaceString)
	{
		if (aOldString == null || aOldString.length() == 0)
			return new String();

		if (aOldString.indexOf(aSearchChar) < 0)
			return aOldString;

		int vIndex=0, vOldIndex=0;
		int vLen = aOldString.length();
		StringBuffer vNewString = new StringBuffer();

		while (vOldIndex < vLen)
		{
			vIndex = aOldString.indexOf(aSearchChar, vOldIndex);
			if (vIndex < 0)
				vIndex = vLen;
			for (; vOldIndex < vIndex; vOldIndex++)
				vNewString.append((char)aOldString.charAt(vOldIndex));

			if (vIndex < vLen)
			{
				vOldIndex = vIndex + 1;
				if (aReplaceString != null && aReplaceString.length() > 0)
					vNewString.append(aReplaceString);
			}

		}

		return vNewString.toString();
	}

	// Pad 'aValue' with blanks to 'aValueLen'.
	public static void AppendPadRight(StringBuffer aBuffer,
													String aValue, int aValueLen)
	{
		if (aBuffer == null)
		{
			String vMsg = "TacString.AppendPadRight(): ";
			vMsg += "parameter [aBuffer] was null.";
			Logger.log(vMsg);
			return;
		}
		if ((aValueLen <= 0) && (aValue == null || aValue.length() == 0))
		{
			String vMsg = "TacString.AppendPadRight(): parameters";
			vMsg += "[aValueLen] and [aValue] cannot both be null.";
			Logger.log(vMsg);
			return;
		}

		if (aValue != null)
		{
			if (aValueLen <= 0 || aValue.length() == aValueLen)
			{
				aBuffer.append(aValue);
				return;
			}
		}

		int vLen = aBuffer.length();
		int vMaxLen = vLen + aValueLen;
		if (aValue != null && aValue.length() > 0)
		{
			aBuffer.append(aValue);
			vLen = aBuffer.length();
		}

		for (int i=vLen; i < vMaxLen; i++)
			aBuffer.append(' ');
   }

	public static void AppendPadLeft(StringBuffer aBuffer, 
												String aValue, int aValueLen)
	{
		if (aBuffer == null)
		{
			String vMsg = "TacString.AppendPadLeft(): ";
			vMsg += "parameter [aBuffer] was null.";
			Logger.log(vMsg);
			return;
		}
		if ((aValueLen <= 0) && (aValue == null || aValue.length() == 0))
		{
			String vMsg = "TacString.AppendPadLeft(): parameters";
			vMsg += "[aValueLen] and [aValue] cannot both be null.";
			Logger.log(vMsg);
			return;
		}

		if (aValue != null)
		{
			if (aValueLen <= 0 || aValue.length() == aValueLen)
			{
				aBuffer.append(aValue);
				return;
			}
		}

		int vLen = aBuffer.length();
		int vPadLen = vLen + aValueLen;
		if (aValue != null && aValue.length() > 0)
			vPadLen -= aValue.length();

		for (int i=vLen; i < vPadLen; i++)
			aBuffer.append(' ');

		if (aValue != null && aValue.length() > 0)
			aBuffer.append(aValue);
   }

   public static Vector toVector(String aDelimitedString,
                                 String aDelimitor)
   {
      Vector v = new Vector();
      if (aDelimitedString == null || aDelimitedString.trim().length() == 0)
         return v;
      if (aDelimitor == null || aDelimitor.length() == 0)
         return v;

       StringTokenizer st = new StringTokenizer(aDelimitedString.trim(), aDelimitor);
       while (st.hasMoreTokens())
       {
          String token = st.nextToken().trim();
          v.addElement(token);
       }

      return v;
   }

   public static String vectorToString(Vector aVectorOfStrings,
                                       String aDelimitor)
   {
      if (aVectorOfStrings == null || aVectorOfStrings.size() == 0)
         return new String();
      if (aDelimitor == null || aDelimitor.length() == 0)
         return new String();

      StringBuffer sb = new StringBuffer();
      for (int j=0; j < aVectorOfStrings.size(); j++)
      {
	      String token = (String)aVectorOfStrings.elementAt(j);
	      if (j == 0)
	      {
	         if (token != null && token.length() != 0)
	            sb.append(token);
	      }
	      else
	      {
	         sb.append(',');
	         if (token != null && token.length() != 0)
	            sb.append(token);
	      }
	   }

      return sb.toString();
   }

   public static Vector toVectorIncludeBlanks(String aDelimitedString,
                                              String aDelimitor)
   {
      Vector v = new Vector();
      if (aDelimitedString == null || aDelimitedString.trim().length() == 0)
         return v;
      if (aDelimitor == null || aDelimitor.length() == 0)
         return v;

       boolean vPreviousIsDelimitor = true;
       StringTokenizer st = new StringTokenizer(aDelimitedString.trim(), aDelimitor, true);
       while (st.hasMoreTokens())
       {
          String token = st.nextToken();
          if (aDelimitor.equals(token))
          {
             if (vPreviousIsDelimitor)
                v.addElement("");
             vPreviousIsDelimitor = true;
          }
          else
          {
             v.addElement(token);
             vPreviousIsDelimitor = false;
          }
       }

      return v;
   }

}
