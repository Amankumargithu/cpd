package com.quodd.populator;

import static com.quodd.controller.DJController.logger;

import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import com.quodd.bean.QueryInfoBean;

public class DbCallable implements Callable<QueryInfoBean> {

	private Connection con;
	private String query;

	public DbCallable(Connection con, String query) {
		this.con = con;
		this.query = query;
	}

	@Override
	public QueryInfoBean call() throws Exception {
		int b = 0;
		QueryInfoBean bean = new QueryInfoBean();
		try (Statement stmt = this.con.createStatement();) {
			bean.setQuery(this.query);
			b = stmt.executeUpdate(this.query);
		} catch (Exception e) {
			logger.log(Level.WARNING, this.query + " " + e.getMessage(), e);
			throw e;
		}
		bean.setResult(b);
		return bean;
	}
}
