package com.quodd.util;

import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.Callable;

public class DbCallable implements Callable<QueryInfoBean>{

	private Connection con;
	private String query;

	public DbCallable(Connection con,String query) {
		this.con = con;
		this.query = query;
	}

	@Override
	public QueryInfoBean call() throws Exception {
		Statement stmt = null;
		int b = 0;
		QueryInfoBean bean = new QueryInfoBean();
		stmt = con.createStatement();
		bean.setQuery(query);
		try {
		b =stmt.executeUpdate(query);
		}
		catch(Exception e) {
			System.out.println("Excep: "+query);
			throw e;
		}
		bean.setResult(b);
		stmt.close();
		return bean;
	}
}