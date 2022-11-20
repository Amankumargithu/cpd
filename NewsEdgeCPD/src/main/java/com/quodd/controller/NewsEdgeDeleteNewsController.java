package com.quodd.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;
import com.quodd.db.DatabaseConnectionManager;

public class NewsEdgeDeleteNewsController {
	public static final Logger logger = QuoddLogger.getInstance("edgenews_cleaner").getLogger();

	public static void main(String[] args) {
		logger.info("PROCESS STARTED");
		int rows = 0;
		long num = 0;
		try {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, -400);
			Date d = cal.getTime();
			String oldDate = new SimpleDateFormat("yyyyMMdd").format(d);
			String query = "DELETE FROM NEWSEDGE_INFO WHERE NEWS_ID LIKE '" + oldDate + "%'";
			logger.info("QUERY " + query);
			try (Connection con = DatabaseConnectionManager.getConnection();) {
				try (PreparedStatement preStmt = con.prepareStatement(query);) {
					rows = preStmt.executeUpdate();
					logger.info("Rows Updates " + rows);
				}
				query = "SELECT MIN(story_id) FROM NEWSEDGE_INFO";
				logger.info("QUERY " + query);
				try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query);) {
					while (rs.next()) {
						num = rs.getLong(1);
						logger.info("Minimun story id " + num);
					}
				}
				query = "DELETE FROM COMPANY_NEWS WHERE NEWS_ID < " + num;
				logger.info("QUERY " + query);
				try (PreparedStatement preStmt = con.prepareStatement(query);) {
					rows = preStmt.executeUpdate();
					logger.info("Rows Updates " + rows);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		logger.info("PROCESS STOPPED");
	}
}
