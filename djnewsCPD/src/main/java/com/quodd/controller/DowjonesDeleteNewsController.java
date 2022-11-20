package com.quodd.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;
import com.quodd.util.MySQLDBManager;

public class DowjonesDeleteNewsController {
	public static final Logger logger = QuoddLogger.getInstance("dowjones_cleaner").getLogger();

	public static void main(String[] args) {
		logger.info("PROCESS STARTED");
		int rows = 0;
		try {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, -400);
			Date d = cal.getTime();
			String oldDate = new SimpleDateFormat("yyyyMMdd").format(d);
			String query = "DELETE FROM DJ_NEWS_INFO WHERE NEWS_ID LIKE '" + oldDate + "%'";
			logger.info("QUERY " + query);
			try (Connection con = MySQLDBManager.getConnection();) {
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
