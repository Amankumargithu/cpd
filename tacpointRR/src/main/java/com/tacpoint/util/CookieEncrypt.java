package com.tacpoint.util;

import java.io.*;
import java.lang.*;

public class CookieEncrypt
{
   public static String encrypt(String inputLine, String publicKey)
    {
		String tempKey = publicKey;

		while(inputLine.length() > tempKey.length())
		{
			tempKey += tempKey;
		}

		char[] inputArray = inputLine.toCharArray();
		char[] keyArray = tempKey.toCharArray();

		int inputTemp = 0;
		int keyTemp = 0;
		int resultInt = 0;
		String resultString = "";
		String tempString ="";

		for(int k =0; k < inputArray.length; k++)
		{
			inputTemp = inputArray[k];
			keyTemp = keyArray[k];
			resultInt = inputTemp + keyTemp;

			if(resultInt < 99)
				tempString = "0" + resultInt;
			else
				tempString = ""+ resultInt;

			resultString += tempString;
		}
		return resultString;
    }

   public static String decrypt(String inputLine, String publicKey)
   {
		int inputLineSize = (inputLine.length())/3;

		String [] inputArray = new String[inputLineSize];
		int index = 0;
		for(int k = 0; k < inputLineSize ; k++)
		{
			inputArray[k]=inputLine.substring(index,index+3);
			index = index + 3;
		}

		String tempKey = publicKey;

		while(inputLineSize > tempKey.length())
		{
			tempKey+= tempKey;
		}

		char[] KeyArray = tempKey.toCharArray();
		int [] publicArray = new int[inputLineSize];
		int counter = 0;
		int tempChar = 0;

		for(int i =0; i < inputLineSize ; i++)
		{
			tempChar = KeyArray[i];
			publicArray[i] = tempChar;
		}

		int tempInt = 0;
		String resultArray = "";

		for(int j =0; j < inputLineSize ; j++)
		{
		tempInt = Integer.parseInt(inputArray[j]) - publicArray[j];
		resultArray += (char)tempInt;
		}
		return resultArray;
	}
}