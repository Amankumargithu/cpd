package com.quodd.distributor;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.util.Constants;
import com.quodd.common.util.QuoddProperty;
import com.quodd.util.StatExecutor;
import com.quodd.util.StatFileWriter;

public class SftpOneMinFutureOptionTsq {

	private static final Logger logger = QuoddLogger.getInstance("sftpFutureOptions").getLogger();

	private final QuoddProperty sftpProperties;
	private Session session = null;
	private Channel channel = null;
	private ChannelSftp channelSftp = null;
	private String tradeDate;
	private final int startTime;
	private final int endTime;
	private final int delayedMinutes;
	private final String localBaseDirectory;
//	private final int opraChannelStart;
//	private final int opraChannelEnd;
	private final boolean doTransferTrade;
	private final boolean doTransferQuote;
	private boolean isCustomDate = false;
	private final String sftpHostName;
	private final String sftpUsername;
	private final boolean isEndFileTransfer;
	private final String endFileName;

	private long sleepTime = 1000;
	private boolean doRun = false;
	private String serverName;
	private final String statUrl;

	private final ExecutorService executor = Executors.newFixedThreadPool(5);
	private StatFileWriter writer = null;
	private Thread statWriterThread = null;

	private static final List<String> futureChannelList = Collections
			.unmodifiableList(Arrays.asList("CBOT", "CME", "COMEX", "NYMEX"));

	public SftpOneMinFutureOptionTsq(String propertyName) {
		this.sftpProperties = new QuoddProperty("/" + propertyName);
		this.tradeDate = this.sftpProperties.getStringProperty("TRADE_DATE", null);
		if (this.tradeDate == null) {
			this.tradeDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
		} else {
			this.isCustomDate = true;
		}
		this.startTime = this.sftpProperties.getIntProperty("SFTP_START_TIME", 930);
		this.endTime = this.sftpProperties.getIntProperty("SFTP_END_TIME", 1630);
		this.delayedMinutes = this.sftpProperties.getIntProperty("SFTP_DELAYED_MINUTES", 15);
		this.localBaseDirectory = this.sftpProperties.getStringProperty("LOCAL_BASE_DIR", "/data1/tsqOptions_")
				+ this.tradeDate;
//		this.opraChannelStart = this.sftpProperties.getIntProperty("OPRA_CHANNEL_START", 1);
//		this.opraChannelEnd = this.sftpProperties.getIntProperty("OPRA_CHANNEL_END", 16);
		this.doTransferTrade = this.sftpProperties.getBooleanProperty("DO_TRANSFER_TRADE", true);
		this.doTransferQuote = this.sftpProperties.getBooleanProperty("DO_TRANSFER_QUOTE", true);
		this.sftpHostName = this.sftpProperties.getStringProperty("SFTP_SERVER", "qsec026");
		this.sftpUsername = this.sftpProperties.getStringProperty("SFTP_USER", "betalabs");
		this.statUrl = this.sftpProperties.getStringProperty("STAT_URL", "http://www6.quodd.com");
		this.isEndFileTransfer = this.sftpProperties.getBooleanProperty("IS_END_FILE_TRANSFER", false);
		this.endFileName = this.sftpProperties.getStringProperty("END_FILE_NAME", "/home/tsqone/resource/done.txt");
		try {
			this.writer = new StatFileWriter(this.sftpProperties.getStringProperty("STAT_FILE", this.sftpUsername));
			this.statWriterThread = new Thread(this.writer);
			this.statWriterThread.start();
		} catch (IOException e1) {
			logger.log(Level.WARNING, e1.getMessage(), e1);
		}
		logger.info("Set local directory " + this.localBaseDirectory);
		this.doRun = true;
		try {
			this.serverName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			this.serverName = "localhost";
		}
	}

	public void connectSftpServer() throws JSchException, SftpException {
		JSch jsch = new JSch();
		jsch.setKnownHosts(this.sftpProperties.getStringProperty("KNOWN_HOST", "/root/.ssh/known_hosts"));
		jsch.addIdentity(this.sftpProperties.getStringProperty("SFTP_PRIVATE_KEY", "/root/.ssh/id_rsa"));
		this.session = jsch.getSession(this.sftpUsername, this.sftpHostName,
				this.sftpProperties.getIntProperty("SFTP_PORT", 22));
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		this.session.setConfig(config);
		this.session.connect();
		this.channel = this.session.openChannel("sftp");
		this.channel.connect();
		this.channelSftp = (ChannelSftp) this.channel;
		this.channelSftp.cd(this.sftpProperties.getStringProperty("SFTP_BASE_DIRECTORY", "/files"));
	}

	public void disconnectSftpServer() {
		if (this.channel != null)
			this.channel.disconnect();
		if (this.session != null)
			this.session.disconnect();
		this.executor.shutdown();
		if (this.writer != null)
			try {
				this.writer.close();
				this.statWriterThread.join();
			} catch (IOException | InterruptedException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
	}

	public void createDirectories() throws SftpException {
		String dirName = "futureoptions_" + this.tradeDate;
		try {
			this.channelSftp.mkdir(dirName);
			logger.info("Created remote directory " + dirName);
		} catch (SftpException e) {
			logger.warning("Directory Already present " + dirName);
		}
		this.channelSftp.cd(dirName);
		for (String subDirName : futureChannelList) {
			try {
				this.channelSftp.mkdir(subDirName);
				logger.info("Created remote sub directory " + subDirName);
			} catch (SftpException e) {
				logger.warning("Directory Already present " + subDirName);
			}
		}
	}

	public void transferEndFile() {
		try {
			if (this.isEndFileTransfer) {
				File endFile = new File(this.endFileName);
				if (endFile.exists())
					this.channelSftp.put(endFile.getAbsolutePath(), endFile.getName());
			}
		} catch (SftpException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public void transferFiles() throws JSchException, SftpException {
		LocalDateTime dt = LocalDateTime.now().withHour(this.startTime / 100).withMinute(this.startTime % 100);
		// wait till current time is not equal or greater than start time
		int currentTime = updateCurrentTime(LocalDateTime.now());
		while (currentTime < this.startTime && currentTime > 1720) { // day 1
			try {
				TimeUnit.MILLISECONDS.sleep(this.sleepTime);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			currentTime = updateCurrentTime(LocalDateTime.now());
		}
		while (this.doRun) {
			currentTime = updateCurrentTime(dt);
			if (currentTime > this.endTime && currentTime <= 1720) {
				this.doRun = false;
			} else if (!this.isCustomDate
					&& (currentTime > updateCurrentTime(LocalDateTime.now().minusMinutes(this.delayedMinutes)))) {
				try {
					TimeUnit.MILLISECONDS.sleep(this.sleepTime);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			} else {
				// check and transfer files
				StringBuilder sb = new StringBuilder();
				String fileTime = Integer.toString(currentTime);
				for (int i = 4; i > fileTime.length(); i--) {
					sb.append("0");
				}
				sb.append(fileTime);
				fileTime = sb.toString();
				if (this.doTransferTrade) {
					for (String channelName : futureChannelList) {
						String fileName = channelName + "/" + channelName + "_OptionTrade_" + fileTime + ".csv";
						String gzipFileName = fileName + ".gz";
						File localFile = new File(this.localBaseDirectory, fileName);
						File localFileGz = new File(this.localBaseDirectory, gzipFileName);
						while (localFile.exists()) {
							try {
								TimeUnit.MILLISECONDS.sleep(this.sleepTime);
							} catch (InterruptedException e) {
								logger.log(Level.WARNING, e.getMessage(), e);
							}
						}
						if (localFileGz.exists()) {
							try {
								this.channelSftp.put(localFileGz.getAbsolutePath(), gzipFileName);
								logger.info("Moved " + localFileGz.getAbsolutePath());
								this.executor.execute(new StatExecutor(this.serverName, this.sftpUsername, gzipFileName,
										this.sftpHostName, this.statUrl, null));
								this.writer.addMessage(getStatMessage(gzipFileName, ""));
							} catch (SftpException e) {
								logger.log(Level.WARNING,
										localFileGz.getAbsolutePath() + " " + gzipFileName + " " + e.getMessage(), e);
								this.executor.execute(new StatExecutor(this.serverName, this.sftpUsername, gzipFileName,
										this.sftpHostName, this.statUrl, "Error " + e.getMessage()));
								this.writer.addMessage(getStatMessage(gzipFileName, "Error " + e.getMessage()));
								if (this.channelSftp == null || !this.channelSftp.isConnected()) {
									logger.info("trying to reconnect channel");
									connectSftpServer();
									createDirectories();
									this.channelSftp.put(localFileGz.getAbsolutePath(), gzipFileName);
									logger.info("Moved " + localFileGz.getAbsolutePath());
									this.executor.execute(new StatExecutor(this.serverName, this.sftpUsername, gzipFileName,
											this.sftpHostName, this.statUrl, null));
									this.writer.addMessage(getStatMessage(gzipFileName, ""));
								}
							}
						} else {
							logger.warning("No Gzip file " + localFileGz.getAbsolutePath());
						}
					}
				}
				if (this.doTransferQuote) {
					for (String channelName : futureChannelList) {
						String fileName = channelName + "/" + channelName + "_OptionQuote_" + fileTime + ".csv";
						String gzipFileName = fileName + ".gz";
						File localFile = new File(this.localBaseDirectory, fileName);
						File localFileGz = new File(this.localBaseDirectory, gzipFileName);
						while (localFile.exists()) {
							try {
								TimeUnit.MILLISECONDS.sleep(this.sleepTime);
							} catch (InterruptedException e) {
								logger.log(Level.WARNING, e.getMessage(), e);
							}
						}
						if (localFileGz.exists()) {
							try {
								this.channelSftp.put(localFileGz.getAbsolutePath(), gzipFileName);
								logger.info("Moved " + localFileGz.getAbsolutePath());
								this.executor.execute(new StatExecutor(this.serverName, this.sftpUsername, gzipFileName,
										this.sftpHostName, this.statUrl, null));
								this.writer.addMessage(getStatMessage(gzipFileName, ""));
							} catch (SftpException e) {
								logger.log(Level.WARNING,
										localFileGz.getAbsolutePath() + " " + gzipFileName + " " + e.getMessage(), e);
								this.executor.execute(new StatExecutor(this.serverName, this.sftpUsername, gzipFileName,
										this.sftpHostName, this.statUrl, "Error " + e.getMessage()));
								this.writer.addMessage(getStatMessage(gzipFileName, "Error " + e.getMessage()));
								if (this.channelSftp == null || !this.channelSftp.isConnected()) {
									logger.info("trying to reconnect channel");
									connectSftpServer();
									createDirectories();
									this.channelSftp.put(localFileGz.getAbsolutePath(), gzipFileName);
									logger.info("Moved " + localFileGz.getAbsolutePath());
									this.executor.execute(new StatExecutor(this.serverName, this.sftpUsername, gzipFileName,
											this.sftpHostName, this.statUrl, null));
									this.writer.addMessage(getStatMessage(gzipFileName, ""));
								}
							}
						} else {
							logger.warning("No Gzip file " + localFileGz.getAbsolutePath());
						}
					}
				}
				dt = dt.plusMinutes(1);
			}
		}
	}

	private String getStatMessage(String fileName, String errorMessage) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.serverName);
		sb.append(Constants.COMMA);
		sb.append(this.sftpUsername);
		sb.append(Constants.COMMA);
		sb.append(fileName);
		sb.append(Constants.COMMA);
		sb.append(this.sftpHostName);
		sb.append(Constants.COMMA);
		sb.append(errorMessage);
		sb.append(Constants.NEWLINE);
		return sb.toString();
	}

	private int updateCurrentTime(LocalDateTime dt) {
		return dt.getHour() * 100 + dt.getMinute();
	}

//	private int updatefutureCurrentTime(LocalDateTime dt) {
//		if (dt.getHour() == 0 && dt.getMinute() <= this.delayedMinutes)
//			return 24 * 100 + dt.getMinute();
//		return dt.getHour() * 100 + dt.getMinute();
//	}

	public static void main(String[] args) {
		logger.info("Start Process");
		if (args.length < 1) {
			logger.warning("Missing properties files as argument");
			System.exit(-1);
		}
		SftpOneMinFutureOptionTsq sftpTrade = new SftpOneMinFutureOptionTsq(args[0]);
		try {
			sftpTrade.connectSftpServer();
			sftpTrade.createDirectories();
			sftpTrade.transferFiles();
			sftpTrade.transferEndFile();
			sftpTrade.disconnectSftpServer();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		logger.info("End Process");
	}
}
