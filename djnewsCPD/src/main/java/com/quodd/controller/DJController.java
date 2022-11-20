package com.quodd.controller;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.collector.DJCollector;
import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.util.QuoddProperty;
import com.quodd.parser.DJParser;
import com.quodd.populator.DJPopulator;
import com.quodd.util.MySQLDBManager;
import com.quodd.util.MySqlDBManager2;

public class DJController extends Thread {
	public static final Logger logger = QuoddLogger.getInstance("djCpdLog").getLogger();
	public static final QuoddProperty cpdProperties = new QuoddProperty("/cpd.properties");
	public static final QuoddProperty dbProperties = new QuoddProperty("/db.properties");
	public static long publishCount = 0;
	public static long dropCount = 0;
	public static long totalNews = 0;
	private boolean doRun = true;
	private ServerSocket serverSocket = null;
	private DJCollector collector = null;
	private DJParser parser = null;
	private DJPopulator distributor = null;
	private int collectorCounter = 0;
	private Connection mysqlConnection = null;
	private Connection mysqlConnection2 = null;
	private long logStartTime;
	private boolean doRunLog = true;

	public DJController() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			finish();
		}, "DJShutdownHookThread"));
	}

	@Override
	public void run() {
		logger.info(this.getName() + " Starting DJ News CPD");
		int port = cpdProperties.getIntProperty("PORT", 1234);
		String jmsClientID = cpdProperties.getStringProperty("JMS_CLIENT_ID", "djnewspub1new");
		String jmsTopic = cpdProperties.getStringProperty("JMS_TOPIC", "/news/dj");
		String filepath = cpdProperties.getStringProperty("FILE_PATH", "/home/djNews");
		try {
			this.mysqlConnection = MySQLDBManager.getConnection();
			this.mysqlConnection2 = MySqlDBManager2.getConnection();
			this.distributor = new DJPopulator(jmsClientID, jmsTopic, this.mysqlConnection);
			this.distributor.setName("DjDistributor");
			this.distributor.start();
			this.parser = new DJParser(this.distributor, this.mysqlConnection2);
			this.parser.setName("DjParser");
			this.parser.start();
			Socket socket = null;
			startLoggingThread();
			this.serverSocket = new ServerSocket(port);
			logger.info(this.getName() + " Waiting for connection on port " + port);
			while (this.doRun) {
				socket = this.serverSocket.accept();
				this.collectorCounter++;
				logger.info(this.getName() + " Socket connected to : " + socket.getRemoteSocketAddress().toString());
				if (this.collector != null) {
					this.collector.finish();
				}
				this.collector = new DJCollector(socket, this.parser, filepath);
				this.collector.setName("DjCollector_" + this.collectorCounter);
				this.collector.start();
			}
		} catch (Exception | Error e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			finish();
		}
	}

	public void finish() {
		this.doRun = false;
		this.doRunLog = false;
		try {
			if (this.serverSocket != null)
				this.serverSocket.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			this.serverSocket = null;
		}
		this.collector.finish();
		try {
			this.collector.join();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		this.parser.finish();
		try {
			this.parser.join();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		this.distributor.finish();
		try {
			this.distributor.join();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		try {
			this.mysqlConnection.close();
		} catch (SQLException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		try {
			this.mysqlConnection2.close();
		} catch (SQLException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public static void main(String[] args) {
		try {
			DJController controller = new DJController();
			controller.setName("DjController");
			controller.start();
		} catch (Exception | Error e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private void startLoggingThread() {
		new Thread(() -> {
			while (this.doRunLog) {
				long timeDiff = System.currentTimeMillis() - DJController.this.logStartTime;
				if (timeDiff > 5000) {
					logger.info("\n---------------\n PUBLISH COUNT : " + publishCount + "\n DROP COUNT : " + dropCount
							+ "\n TOTAL NEWS : " + totalNews + "\n---------------\n");
					DJController.this.logStartTime = System.currentTimeMillis();
				}
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
			}
		}, "StatLogger").start();
	}
}
