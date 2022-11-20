package com.b4utrade.helper;

import java.sql.Timestamp;

public class ConsoleTimestampLogger 
{
	public static void println(String statement)
	{
		System.out.println(new Timestamp(System.currentTimeMillis()) + " "+ statement);
	}
	
	public static void print(String statement)
	{
		System.out.print(new Timestamp(System.currentTimeMillis()) + " " + statement);
	}
}
