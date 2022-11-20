package com.b4utrade.bean;

import java.io.Serializable;

public class InterestRateBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	private double rate30Day;
	private double rate90Day;
	private double rate180Day;
	private double rate360Day;

	public void set30DayRate(double inValue)
	{
		rate30Day = inValue;
	}	
	public void set90DayRate(double inValue)
	{
		rate90Day = inValue;
	}	
	public void set180DayRate(double inValue)
	{
		rate180Day = inValue;
	}	
	public void set360DayRate(double inValue)
	{
		rate360Day = inValue;
	}
	public double get30DayRate()
	{
		return rate30Day;
	}	
	public double get90DayRate()
	{
		return rate90Day;
	}	
	public double get180DayRate()
	{
		return rate180Day;
	}	
	public double get360DayRate()
	{
		return rate360Day;
	}
	public String toString()
	{
		StringBuffer sb = new StringBuffer();   
		sb.append("The rate information:");
		sb.append('\n');
		sb.append(" 30 day rate="+rate30Day);
		sb.append(" 90 day rate="+rate90Day);
		sb.append(" 180 day rate="+rate180Day);
		sb.append(" 360 day rate="+rate360Day);
		return(sb.toString());
	}	
}
