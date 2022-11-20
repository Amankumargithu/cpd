package com.b4utrade.network;

import java.io.*;
import java.net.*;
import java.util.*;

import com.b4utrade.helper.StockActivityHelper;
import com.b4utrade.bean.QTMessageBean;
import com.b4utrade.helper.StockNewsUpdateHelper;
import com.b4utrade.util.MessageQueue;
import com.tacpoint.network.NetworkConfiguration;
import com.tacpoint.util.SystemOutLogger;

public class UDPOptimizedEquityStreamer implements IStreamer, Runnable {

	private MessageQueue queue;

	private MessageQueue newsQueue;

	private NetworkStreamer networkStreamer;

	private NetworkConfiguration networkConfiguration;

	private InputStream inputStream;

	private OutputStream os = null;

	boolean doRun = true;

	boolean isBlocking = true;

	int readAttemptsBeforeTerminating = 5;

	String userId = null;

	private final String prefix = "TOPICS";

	private final String paramSeperator = "?";

	private Hashtable resultsCache = null;

	private HashMap tickerMap = new HashMap();

	private String streamerId = "";

	private int port = 0;

	private String udpBindAddress = null;
	
	private DatagramSocket sock = null;

	public UDPOptimizedEquityStreamer() {
	}

	public void init(MessageQueue queue, MessageQueue newsQueue,
			InputStream inputStream, NetworkConfiguration networkConfiguration,
			NetworkStreamer networkStreamer) {
		this.queue = queue;
		this.newsQueue = newsQueue;
		this.inputStream = inputStream;
		this.networkConfiguration = networkConfiguration;
		this.networkStreamer = networkStreamer;
		SystemOutLogger.debug("UDP Opt Equity Streamer - init()");

	}

	public void setUdpBindAddress(String bindingAddress) {
		this.udpBindAddress = bindingAddress;
	}

	public void setUdpPort(int port) {
		this.port = port;
	}

	public boolean getIsBlocking() {
		return isBlocking;
	}

	public void setIsBlocking(boolean isBlocking) {
		this.isBlocking = isBlocking;
	}

	public void setDoRun(boolean doRun) {
		this.doRun = doRun;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setStreamerId(String streamerId) {
		this.streamerId = streamerId;
	}

	public void setReadAttemptsBeforeTerminating(
			int readAttemptsBeforeTerminating) {
		if (readAttemptsBeforeTerminating > 0)
			this.readAttemptsBeforeTerminating = readAttemptsBeforeTerminating;
	}

	public void shutDownIO() {
		
		try {
			if (inputStream != null)
				inputStream.close();
		} catch (Exception ioCloseException) {
		}
		
		try {
			if (os != null)
				os.close();			
		} catch (Exception osCloseException) {
		}
		
		try {
			if (sock != null)
				sock.close();
		} catch (Exception sockClosException) {
		}

	}

	public void run() {

		networkStreamer.setIsStreamerConsuming(false);
		byte[] response = new byte[1024];

		try {

			URL url = new URL(networkConfiguration.getStreamingURL());

			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setAllowUserInteraction(false);
			conn.setUseCaches(false);
			if ((streamerId != null) && !(streamerId.trim().equals(""))) {
				String cookieValue = QTMessageKeys.STREAMER_COOKIE_KEY + "="
						+ streamerId + ";";
				conn.setRequestProperty("Cookie", cookieValue);
			}
			Hashtable queryHash = networkConfiguration.getQueryHash();
			Enumeration eKeys = queryHash.keys();
			StringBuffer query = new StringBuffer();
			// query.append(paramSeperator);
			if (userId != null) {
				query.append("USERID=");
				query.append(userId);
				query.append("&");

			}
			query.append(prefix);
			query.append("=");
			while (eKeys.hasMoreElements()) {
				String key = (String) eKeys.nextElement();
				Vector v = (Vector) queryHash.get(key);
				if (v == null)
					continue;
				updateTickerMap(v);
				for (int i = 0; i < v.size(); i++) {
					query.append(URLEncoder.encode((String) v.elementAt(i)));
					if (i + 1 < v.size())
						query.append(URLEncoder.encode(","));
				}
			}
			query.append("&SUBEND=true");
			SystemOutLogger
					.debug("streaming URL "
							+ networkConfiguration.getStreamingURL()
							+ query.toString());

			os = conn.getOutputStream();
			os.write(query.toString().getBytes());

			inputStream = conn.getInputStream();

			inputStream.read(response, 0, 1024);

			// once we've read the first byte, close the socket since we won't
			// be using it for anything else!

		} catch (Exception e) {
			SystemOutLogger
					.error("UDP Opt Equity Streamer - Unable to connect to URL: "
							+ networkConfiguration.getStreamingURL());
			e.printStackTrace();
			return;
		} finally {
			isBlocking = false;
			try {
				if (os != null)
					os.flush();
			} catch (Exception osFlushException) {
			}
			try {
				if (os != null)
					os.close();
			} catch (Exception osCloseException) {
			}
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (Exception isCloseException) {
			}

		}

		networkStreamer.setIsStreamerConsuming(true);

		ByteArrayOutputStream baos = new ByteArrayOutputStream(256);

		long beginTime = System.currentTimeMillis();
		long curTime = beginTime;

		long msgCount = 0;
		long procMsgCount = 0;
		long dropMsgCount = 0;

		byte terminatorByte = networkConfiguration.getTerminator();

		QTReducedMessageInflator messageInflator = new QTReducedMessageInflator();
		StreamerHelper sh = new StreamerHelper();
		InetAddress inetAddress = null;
		
		DatagramPacket packet = null;
		long prevPacketNbr = 0;
		long curPacketNbr = 0;
		byte[] buf = new byte[1024];

		try {
			sock = new DatagramSocket(port);
			// inetAddress = InetAddress.getByName(udpBindAddress);
			// sock.connect(inetAddress, port);
			sock.setBroadcast(true);

			while (doRun) {

				try {

					packet = new DatagramPacket(buf, buf.length);
					sock.receive(packet);

					baos.reset();
					baos.write(packet.getData(), 0, packet.getLength());

					// strip out the packet record number as well as the
					// terminator byte!

					String record = new String(baos.toByteArray());
					// SystemOutLogger.debug("Record: " + record);

					int beginRecordIndex = record.indexOf("T:");
					if (beginRecordIndex <= 0) {
						SystemOutLogger
								.debug("UDP Opt Equity Streamer - Unable to parse out record topic.");
						continue;
					}

					try {
						curPacketNbr = Long.parseLong(record.substring(0,
								beginRecordIndex));

					} catch (NumberFormatException nfe) {
						SystemOutLogger.error("Unable to parse packet number.");
						continue;
					}
					procMsgCount++;

					if (curPacketNbr > prevPacketNbr) {

						dropMsgCount = dropMsgCount
								+ (curPacketNbr - prevPacketNbr - 1);
						// SystemOutLogger.debug("packet number: " +
						// curPacketNbr+ " drop count: " + dropMsgCount);

						prevPacketNbr = curPacketNbr;

					} else {
						// SystemOutLogger.debug("Skipping message");

						continue;
					}

					record = record.substring(beginRecordIndex,
							record.length() - 1);

					baos.reset();
					baos.write(record.getBytes(), 0, record.length());

					curTime = System.currentTimeMillis();

					networkStreamer.setLastProcessTime(curTime);

					if ((curTime - beginTime) > 1000) {
						beginTime = curTime;
						SystemOutLogger
								.debug("UDP Opt Equity Streamer - Number of messages read: "
										+ procMsgCount);
						SystemOutLogger
								.debug("UDP Opt Equity Streamer - Number of messages processed: "
										+ msgCount);
						SystemOutLogger
								.debug("UDP Opt Equity Streamer - Number of messages drop: "
										+ dropMsgCount);
						SystemOutLogger
								.debug("UDP Opt Equity Streamer - % of messages get: "
										+ ((double) procMsgCount / ((double) dropMsgCount + (double) procMsgCount)));
						msgCount = 0l;
						procMsgCount = 0l;
						dropMsgCount = 0l;
						Hashtable queryHash = networkConfiguration
								.getQueryHash();
						updateTickerMap((Vector) queryHash.get("COMPANYID"));
					}

					Hashtable map = messageInflator.parseKeyValues(baos);

					String topic = (String) map.get(QTMessageKeys.TICKER);

					if (topic != null
							&& topic
									.equals(com.b4utrade.util.B4UConstants.DJ_NEWS_TOPIC)) {
						// SystemOutLogger.debug("before create news");
						Vector v = sh.createNewsMsg(map);
						Enumeration en = v.elements();
						while (en.hasMoreElements()) {
							StockNewsUpdateHelper newsItem = (StockNewsUpdateHelper) en
									.nextElement();
							newsQueue.add(newsItem);
							SystemOutLogger.debug("news ticker:"
									+ newsItem.getTicker() + " news id: "
									+ newsItem.getLastNewsID());

						}
						// SystemOutLogger.debug("after create news");

					} else {

						resultsCache = networkConfiguration.getResultsHash();

						if (resultsCache == null) {
							SystemOutLogger
									.debug("UDP Opt Equity Streamer - unable to obtain network configuration results hash!");
							baos.reset();
							continue;
						}

						if (!tickerMap.containsKey(map
								.get(QTMessageKeys.TICKER)))
							continue;

						QTMessageBean qtBean = (QTMessageBean) resultsCache
								.get(map.get(QTMessageKeys.TICKER));

						qtBean = messageInflator.merge(map, qtBean);
						// QTMessageBean newBean = new QTMessageBean();

						if (qtBean != null) {
							resultsCache.put(qtBean.getSystemTicker(), qtBean);
							queue.add(qtBean);
							msgCount++;
						}

					}

					baos.reset();

				} catch (Exception runEx) {} 
				
			}
		} catch (Exception ioe) {
			SystemOutLogger
					.error("UDP Opt Equity Streamer - Exception caught during input stream read.");
			ioe.printStackTrace();
			return;
		} finally {
			try {
				if (sock != null)
					sock.close();
			} catch (Exception sockClosException) {
			}
		}

		networkStreamer.setIsStreamerConsuming(false);

	}

	private void updateTickerMap(Vector list) {
		if (list == null)
			return;

		tickerMap.clear();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			tickerMap.put(it.next(), "");
		}
	}

}
