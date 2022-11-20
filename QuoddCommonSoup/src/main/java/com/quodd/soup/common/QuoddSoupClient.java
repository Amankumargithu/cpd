package com.quodd.soup.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.common.logger.QuoddLogger;
import com.quodd.exception.QuoddException;
import com.quodd.soup.common.SoupBinTCP.LoginAccepted;
import com.quodd.soup.common.SoupBinTCP.LoginRejected;
import com.quodd.soup.common.SoupBinTCP.LoginRequest;

public class QuoddSoupClient implements Runnable, SoupBinTCPClientStatusListener {

	private static final Logger logger = QuoddLogger.getInstance().getLogger();
	public static final String REASON_FOR_CLOSE_MAX_LIMIT = "Max retries completed";
	public static final String REASON_FOR_CLOSE_LOGIN_REJECTION = "Login Rejection";
	public static final String REASON_FOR_CLOSE_SESSION_END = "Session end";
	private static final int MAX_RETRY_COUNT = 10;
	private SoupBinTCPSession tcpClient = null;
	private boolean doRun = false;
	private boolean isConnected = false;
	private final String name;
	private SocketChannel channel;
	private final int maxPayload = 1024 * 1024;
	private final InetSocketAddress address;
	private final MessageListener messageListener;
	private final LoginRequest request = new LoginRequest();
	private long totalByteCount = 0;
	private int retryCount = 0;
	private String reasonForClose = "";

	public QuoddSoupClient(String host, int port, String threadName, String username, String password,
			MessageListener listener) throws QuoddException {
		this.name = threadName;
		this.address = new InetSocketAddress(host, port);
		logger.info(this.name + " Address " + host + ":" + port);
		this.messageListener = listener;
		this.request.setUsername(username);
		this.request.setPassword(password);
		this.request.setRequestedSequenceNumber(1);
		if (this.address.isUnresolved())
			throw new QuoddException("Cannot resolve hostname " + host);
	}

	public void connectClient() {
		if (this.isConnected)
			return;
		try {
			this.channel = SocketChannel.open();
			this.channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
			this.channel.connect(this.address);
			logger.info(() -> this.name + " Socket connected " + this.retryCount);
			this.tcpClient = new SoupBinTCPSession(System::currentTimeMillis, this.channel, this.maxPayload,
					this.messageListener, this);
			this.doRun = true;
			logger.info(() -> this.name + " Sending login request " + this.request.getRequestedSequenceNumber());
			this.tcpClient.login(this.request);
			logger.info(() -> this.name + " Login request completed");
			this.isConnected = true;
		} catch (IOException e) {
			logger.log(Level.WARNING, this.name + " " + e.getMessage(), e);
			this.doRun = false;
		}
	}

	public void stop(String reasonForClose) {
		if (this.doRun) {
			this.doRun = false;
			this.reasonForClose = reasonForClose;
			logger.info(() -> this.name + " Requested thread close");
		}
	}

	public void reconnect() {
		if (this.retryCount > MAX_RETRY_COUNT) {
			logger.warning(() -> this.name + " MAX_RETRY_turning off");
			// We can add switching t secondary data source here as well
			stop(REASON_FOR_CLOSE_MAX_LIMIT);
			return;
		}
		long expectedSeqNum = this.tcpClient.getSequenceNumber();
		closeUnderlyingResources();
		// try to wait for input server to rebound
		try {
			TimeUnit.MILLISECONDS.sleep(200);
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, this.name + " " + e.getMessage(), e);
		}
		// set correct sequence number to start reading from
		this.setSequenceNumber(expectedSeqNum);
		this.isConnected = false;
		connectClient();
		this.tcpClient.setSequenceNumber(expectedSeqNum);
		this.retryCount++;
	}

	public void closeUnderlyingResources() {
		try {
			this.tcpClient.logout();
		} catch (IOException e) {
			logger.log(Level.WARNING, this.name + " " + e.getMessage(), e);
		}
		try {
			this.tcpClient.close();
		} catch (IOException e1) {
			logger.log(Level.WARNING, this.name + " " + e1.getMessage(), e1);
		}
		try {
			this.channel.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, this.name + " " + e.getMessage(), e);
		}
		this.isConnected = false;
		if (!this.doRun)
			this.messageListener.stopListener(REASON_FOR_CLOSE_MAX_LIMIT);
	}

	@Override
	public void run() {
		int byteCount = 0;
		if (!this.isConnected)
			connectClient();
		this.totalByteCount = 0;
		while (this.doRun) {
			try {
				byteCount = this.tcpClient.receive();
				if (byteCount <= 0) {
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
					}
				} else {
					this.totalByteCount += byteCount;
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, this.name + " " + e.getMessage(), e);
				reconnect();
			}
		}
		logger.info(() -> this.name + " thread loop close");
		closeUnderlyingResources();
		this.isConnected = false;
		logger.info(() -> this.name + " thread closed");
	}

	@Override
	public void heartbeatTimeout(SoupBinTCPSession session) {
		logger.info(this.name + " heartbeatTimeout");
	}

	@Override
	public void loginAccepted(SoupBinTCPSession session, LoginAccepted payload) {
		logger.info(this.name + " loginAccepted session- " + payload.getSession() + " seqNum "
				+ payload.getSequenceNumber());
		this.doRun = true;
	}

	@Override
	public void loginRejected(SoupBinTCPSession session, LoginRejected payload) {
		logger.info(this.name + " loginRejected reason- " + payload.rejectReasonCode);
		stop(REASON_FOR_CLOSE_LOGIN_REJECTION);
	}

	@Override
	public void endOfSession(SoupBinTCPSession session) {
		logger.info(this.name + " endOfSession");
		stop(REASON_FOR_CLOSE_SESSION_END);
	}

	@Override
	public void unexceptedPacket(byte packetType, ByteBuffer payload) {
		logger.warning("Unexpected packet: " + packetType);
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.request.setRequestedSequenceNumber(sequenceNumber);
	}

	public boolean isConnected() {
		return this.isConnected;
	}

	public boolean isRunning() {
		return this.doRun;
	}

	public String getName() {
		return this.name;
	}

	public SoupBinTCPSession getSession() {
		return this.tcpClient;
	}

	public Map<String, Object> getClientStat() {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("bytes_received", totalByteCount);
		resultMap.put("sequence_number", this.tcpClient.getSequenceNumber());
		resultMap.put("retry_count", retryCount);
		resultMap.put("is_connected", isConnected);
		resultMap.put("last_received_time", this.tcpClient.getlastReceivedTime());
		resultMap.put("reason_for_close", this.reasonForClose);
		return resultMap;
	}
}
