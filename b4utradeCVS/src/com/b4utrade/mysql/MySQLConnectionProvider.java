package com.b4utrade.mysql;

public class MySQLConnectionProvider {

	public static IDBConnectionManager createConnection(String type){
		
		if(type.equalsIgnoreCase("single"))
		{			
			return  SingleDatabaseConnectionManager.getInstance();
		}
		else if(type.equalsIgnoreCase("mysql1"))
		{
			return  MySQL1DatabaseConnectionManager.getInstance();
		}
		else
		{
			return  PooledDatabaseConnectionManager.getInstance();
		}	
	}
}
