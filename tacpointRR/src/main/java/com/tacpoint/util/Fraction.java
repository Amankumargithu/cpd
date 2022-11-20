package com.tacpoint.util;

/** Fraction.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000.
* All rights reserved.
* @author Kim Gentes
* @author kgentes@tacpoint.com
* @version 1.0
* Date created:	04/17/2000
*
*/

import java.util.*;

public class Fraction
{
	public long mWholeNumber = 0;
	public long mNumerator = 0;
	public long mDenominator = 1;

	public Fraction() {}

	public Fraction(String aFraction) throws Exception
	{
		long[] vFractionList = parseFraction(aFraction);
		mWholeNumber = vFractionList[0];
		mNumerator = vFractionList[1];
		mDenominator = vFractionList[2];
//		ReduceFractions();
   }

	public Fraction subtract(Fraction aFraction)
	{
		if (aFraction == null)
			return null;

		long vNumerator1, vNumerator2, vNewNumerator, vCarryOut;

		Fraction vFraction = new Fraction();
		vFraction.mWholeNumber = mWholeNumber - aFraction.mWholeNumber;
		vFraction.mDenominator = mDenominator * aFraction.mDenominator;
		vNumerator1 = mNumerator * aFraction.mDenominator;
		vNumerator2 = aFraction.mNumerator * mDenominator;
		vNewNumerator = vNumerator1 - vNumerator2;
		if ( (vFraction.mWholeNumber < 0 && vNewNumerator > 0) ||
				(vFraction.mWholeNumber > 0 && vNewNumerator < 0) )
		{
			if (vFraction.mWholeNumber > 0)
			{
				vNewNumerator += vFraction.mDenominator;
				vFraction.mWholeNumber--;
			}
			else
			{
				vNewNumerator -= vFraction.mDenominator;
				vFraction.mWholeNumber++;
			}
		}

		vCarryOut = vNewNumerator / vFraction.mDenominator;
		vFraction.mWholeNumber += vCarryOut;
		vFraction.mNumerator = vNewNumerator % vFraction.mDenominator;
		vFraction.ReduceFractions();

		return (vFraction);
	}

	private void ReduceFractions()
	{
		long vFirst = Math.abs(mNumerator);
		long vSecond = Math.abs(mDenominator);
		long vTemp;

		if (mNumerator == 0) // Check for 0 numerator
		{
			mDenominator = 1;
			return;
		}

		// Find greatest common denominator
		while (vFirst != vSecond)
		{
			if (vFirst < vSecond)   // First is not larger
			{
				vTemp = vFirst;
				vFirst = vSecond;
				vSecond = vTemp;
			}
			vFirst -= vSecond;
		}

		mNumerator /= vFirst;
		mDenominator /= vFirst;
	}

	public double getDecimal() throws Exception
	{
		return getDecimal(mWholeNumber, mNumerator, mDenominator);
	}

	public String toString()
	{
		boolean vNegative = false;
		long vWholeNumber = mWholeNumber;
		long vNumerator = mNumerator;

		if (vWholeNumber < 0)
		{
			vNegative = true;
			vWholeNumber *= -1;
		}
		if (vNumerator < 0)
		{
			vNegative = true;
			vNumerator *= -1;
		}

		if (vWholeNumber == 0 && vNumerator == 0)
			return ("0");

		StringBuffer vFraction = new StringBuffer();
		if (vNegative)
			vFraction.append((char)'-');
		if (vWholeNumber > 0)
			vFraction.append(vWholeNumber);
		if (vNumerator > 0)
		{
			if (vWholeNumber > 0)
				vFraction.append((char)' ');
			vFraction.append(vNumerator);
			vFraction.append((char)'/');
			vFraction.append(mDenominator);
		}

		return vFraction.toString();
	}

	private static long[] parseFraction(String aFraction) throws Exception
	{
		if (aFraction == null)
			throw new Exception("Fraction.parseFraction(): null fraction string.");
		aFraction = aFraction.trim();
		if (aFraction.length() == 0)
			throw new Exception("Fraction.parseFraction(): empty fraction string.");

		boolean vNegative = false;
		int vLen = aFraction.length();
		if (aFraction.charAt(0) == '-')
		{
			if (vLen <= 1)
				throw new Exception("Fraction.parseFraction(): empty fraction string.");
			aFraction = aFraction.substring(1);
			aFraction = aFraction.trim();
			// Check to make sure there isn't a negative at the end too.
			if (aFraction.indexOf('-') >=0)
				throw new Exception("Fraction.parseFraction(): empty fraction string.");
			vNegative = true;
		}
		// Check if there is a negative at the end.
		else if (aFraction.charAt(vLen-1) == '-')
		{
			aFraction = aFraction.substring(0, vLen-1);
			aFraction = aFraction.trim();
			vNegative = true;
		}

		int vSlashIndex = aFraction.indexOf('/');
		if (vSlashIndex == 0)
			throw new Exception("Incorrect fraction format: " + aFraction);
		if (vSlashIndex+1 >= aFraction.length())
			throw new Exception("Incorrect fraction format: " + aFraction);

		String vWhole = null;
		long vDenomValue = 1;
		long vNumeratorValue = 0;
		if (vSlashIndex > 0)
		{
			// Get the denominator
			String vDenominator = aFraction.substring(vSlashIndex+1);
			vDenominator = vDenominator.trim();
			if (vDenominator.length() == 0)
				throw new Exception("Incorrect fraction format: " + aFraction);
			vDenomValue = Long.parseLong(vDenominator);
			if (vDenomValue == 0)
				throw new Exception("Incorrect fraction format: " + aFraction);

			aFraction = aFraction.substring(0, vSlashIndex);
			aFraction = aFraction.trim();
			String vNumerator = null;
			int vSpaceIndex = aFraction.indexOf(' ');
			// Get the numerator
			if (vSpaceIndex > 0)
			{
				// Numerator & whole number
				vNumerator = aFraction.substring(vSpaceIndex+1);
				vNumerator = vNumerator.trim();
				vNumeratorValue = Long.parseLong(vNumerator);
				vWhole = aFraction.substring(0, vSpaceIndex);
			}
			else
			{
				// Just numerator
				vNumerator = aFraction;
				vNumeratorValue = Long.parseLong(vNumerator);
			}
		}
		else
			vWhole = aFraction;

		long vWholeNumber = 0;
		if (vWhole != null && vWhole.length() > 0)
			vWholeNumber = Long.parseLong(vWhole);

		// Make sure numerator is not bigger than the denominator.
		if (vNumeratorValue >= vDenomValue)
		{
			vWholeNumber += vNumeratorValue / vDenomValue;
			vNumeratorValue = vNumeratorValue % vDenomValue;
		}

		// Apply negative
		if (vNegative)
		{
			vWholeNumber *= -1;
			vNumeratorValue *= -1;
		}

		long[] vFractionList = new long[3];
		vFractionList[0] = vWholeNumber;
		vFractionList[1] = vNumeratorValue;
		vFractionList[2] = vDenomValue;

		return vFractionList;
	}

	private static double getDecimal(long aWholeNumber, long aNumerator,
												long aDenominator) throws Exception
	{
		if (aDenominator == 0)
			throw new Exception("Fraction.getDecimal(): denominator cannot be zero.");

		double vNumber = 0;

		if (aWholeNumber != 0)
			vNumber = aWholeNumber;

		if (aNumerator != 0)
		{
			double vDecimal = (double)aNumerator / (double)aDenominator;
			vNumber += vDecimal;
		}

		return vNumber;
	}

	public static double convertToDecimal(String aFraction) throws Exception
	{
		if (aFraction.indexOf(".") >= 0){
	       Double vStockValueDouble = new Double(aFraction);
		   double vStockValue = vStockValueDouble.doubleValue();
		   return vStockValue;
	    }
	    else{
		long[] vFractionList = parseFraction(aFraction);
		double vNumber = getDecimal(vFractionList[0], vFractionList[1],
												vFractionList[2]);
		return vNumber;
	    }

	}


}
