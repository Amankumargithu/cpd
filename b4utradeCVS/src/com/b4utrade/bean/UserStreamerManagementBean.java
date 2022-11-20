package com.b4utrade.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.b4utrade.cache.SymbolUpdaterCache;

public class UserStreamerManagementBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final transient Log log = LogFactory.getLog(UserStreamerManagementBean.class);
	public static final int SUBSCRIBE_EQUITY_LIST = 1;
	public static final int UNSUBSCRIBE_LIST = 2;
	public static final int PENDING_LIST = 3;
	public static final int LAST_ONLY_LIST = 4;
	public static final int DELAYED_LIST = 5;
	public static final int SUBSCRIBE_OPTIONS_LIST = 6;
	public static final int SUBSCRIBE_EQUITY_REG_LIST = 7;
	public static final int SUBSCRIBE_OPTIONS_REG_LIST = 8;
	public static final int DELAYED_LAST_ONLY_LIST = 9;

	private int userId;
	private String sessionId;
	private String equitySubscriptionUrl;
	private String optionsSubscriptionUrl;
	private String delayedSubscriptionUrl;
	private String equityRegionalSubscriptionUrl;
	private String optionsRegionalSubscriptionUrl;
	private boolean loggedIn = false;

	private String equityStreamerId;
	private String optionsStreamerId;
	private String equityRegionalStreamerId;
	private String optionsRegionalStreamerId;
	private String delayedStreamerId;

	private HashSet<String> subscribeList = new HashSet<>(); // equity and futures list - real-time
	private HashSet<String> unsubscribeList = new HashSet<>(); // all tickers not entitled
	private HashSet<String> pendingList = new HashSet<>(); // tickers which are not decided - no snap
	private HashSet<String> lastOnlyList = new HashSet<>(); // equity and future tickers, no quotes
	private HashSet<String> delayedList = new HashSet<>(); // equity and future tickers - delayed
	private HashSet<String> delayedLastOnlyList = new HashSet<>(); // equity and future tickers - delayed, no quotes
	private HashSet<String> subscribeEquityRegionalList = new HashSet<>(); // equity regional - real-time
	private HashSet<String> subscribeOptionRegionalList = new HashSet<>(); // options regional - real-time
	private HashSet<String> subscribeOptionList = new HashSet<>(); // options real-time
	private HashMap<HashSet<String>, String> setToUrlMap = new HashMap<>();
	private HashMap<HashSet<String>, String> setToIdMap = new HashMap<>();

	private int maxSymbols = 5000;
	private int subscribeSymbolCount = 0;

	public UserStreamerManagementBean() {
		this.setToUrlMap.put(this.subscribeList, this.equitySubscriptionUrl);
		this.setToUrlMap.put(this.unsubscribeList, null);
		this.setToUrlMap.put(this.pendingList, null);
		this.setToUrlMap.put(this.lastOnlyList, this.equitySubscriptionUrl);
		this.setToUrlMap.put(this.delayedList, this.delayedSubscriptionUrl);
		this.setToUrlMap.put(this.delayedLastOnlyList, this.delayedSubscriptionUrl);
		this.setToUrlMap.put(this.subscribeEquityRegionalList, this.equityRegionalSubscriptionUrl);
		this.setToUrlMap.put(this.subscribeOptionRegionalList, this.optionsRegionalSubscriptionUrl);
		this.setToUrlMap.put(this.subscribeOptionList, this.optionsSubscriptionUrl);

		this.setToIdMap.put(this.subscribeList, this.equityStreamerId);
		this.setToIdMap.put(this.unsubscribeList, null);
		this.setToIdMap.put(this.pendingList, null);
		this.setToIdMap.put(this.lastOnlyList, this.equityStreamerId);
		this.setToIdMap.put(this.delayedList, this.delayedStreamerId);
		this.setToIdMap.put(this.delayedLastOnlyList, this.delayedStreamerId);
		this.setToIdMap.put(this.subscribeEquityRegionalList, this.equityRegionalStreamerId);
		this.setToIdMap.put(this.subscribeOptionRegionalList, this.optionsRegionalStreamerId);
		this.setToIdMap.put(this.subscribeOptionList, this.optionsStreamerId);
	}

	public int getUserId() {
		return this.userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getEquitySubscriptionUrl() {
		return this.equitySubscriptionUrl;
	}

	public void setEquitySubscriptionUrl(String equitySubscriptionUrl) {
		this.equitySubscriptionUrl = equitySubscriptionUrl;
	}

	public String getOptionsSubscriptionUrl() {
		return this.optionsSubscriptionUrl;
	}

	public void setOptionsSubscriptionUrl(String optionsSubscriptionUrl) {
		this.optionsSubscriptionUrl = optionsSubscriptionUrl;
	}

	public String getDelayedSubscriptionUrl() {
		return this.delayedSubscriptionUrl;
	}

	public void setDelayedSubscriptionUrl(String delayedSubscriptionUrl) {
		this.delayedSubscriptionUrl = delayedSubscriptionUrl;
	}

	public String getEquityRegionalSubscriptionUrl() {
		return this.equityRegionalSubscriptionUrl;
	}

	public void setEquityRegionalSubscriptionUrl(String equityRegionalSubscriptionUrl) {
		this.equityRegionalSubscriptionUrl = equityRegionalSubscriptionUrl;
	}

	public String getOptionsRegionalSubscriptionUrl() {
		return this.optionsRegionalSubscriptionUrl;
	}

	public void setOptionsRegionalSubscriptionUrl(String optionsRegionalSubscriptionUrl) {
		this.optionsRegionalSubscriptionUrl = optionsRegionalSubscriptionUrl;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getEquityStreamerId() {
		return this.equityStreamerId;
	}

	public void setEquityStreamerId(String equityStreamerId) {
		this.equityStreamerId = equityStreamerId;
	}

	public String getOptionsStreamerId() {
		return this.optionsStreamerId;
	}

	public void setOptionsStreamerId(String optionsStreamerId) {
		this.optionsStreamerId = optionsStreamerId;
	}

	public String getEquityRegionalStreamerId() {
		return this.equityRegionalStreamerId;
	}

	public void setEquityRegionalStreamerId(String equityRegionalStreamerId) {
		this.equityRegionalStreamerId = equityRegionalStreamerId;
	}

	public String getOptionsRegionalStreamerId() {
		return this.optionsRegionalStreamerId;
	}

	public void setOptionsRegionalStreamerId(String optionsRegionalStreamerId) {
		this.optionsRegionalStreamerId = optionsRegionalStreamerId;
	}

	public String getDelayedStreamerId() {
		return this.delayedStreamerId;
	}

	public void setDelayedStreamerId(String delayedStreamerId) {
		this.delayedStreamerId = delayedStreamerId;
	}

	public boolean isLoggedIn() {
		return this.loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public int getMaxSymbols() {
		return this.maxSymbols;
	}

	public void setMaxSymbols(int maxSymbols) {
		this.maxSymbols = maxSymbols;
	}

	public int getSubscribeSymbolCount() {
		return this.subscribeSymbolCount;
	}

	private void addToSubscribeSymbolCount() {
		this.subscribeSymbolCount = this.subscribeList.size() + this.lastOnlyList.size() + this.delayedList.size()
				+ this.delayedLastOnlyList.size() + this.subscribeEquityRegionalList.size();
	}

	public void addTicker(int map, String ticker) {
		log.info("userId: " + userId + " ticker: " + ticker + " map: " + map);
		if (this.subscribeList.size() > this.maxSymbols) {
			log.info("userId: " + userId + " SubscribeList size greater than " + this.maxSymbols);
		} else {
			switch (map) {
			case SUBSCRIBE_EQUITY_LIST: {
				this.subscribeList.add(ticker);
				subscribeTickers(ticker, this.equitySubscriptionUrl, this.equityStreamerId);
				addToSubscribeSymbolCount();
			}
				break;
			case UNSUBSCRIBE_LIST:
				this.unsubscribeList.add(ticker);
				break;
			case PENDING_LIST:
				this.pendingList.add(ticker);
				break;
			case LAST_ONLY_LIST: {
				this.lastOnlyList.add(ticker);
				subscribeTickers(ticker, this.equitySubscriptionUrl, this.equityStreamerId);
				addToSubscribeSymbolCount();
			}
				break;
			case DELAYED_LIST: {
				this.delayedList.add(ticker);
				subscribeTickers(ticker, this.delayedSubscriptionUrl, this.delayedStreamerId);
				addToSubscribeSymbolCount();
			}
				break;
			case SUBSCRIBE_OPTIONS_LIST: {
				this.subscribeOptionList.add(ticker);
				subscribeTickers(ticker, this.optionsSubscriptionUrl, this.optionsStreamerId);
			}
				break;
			case SUBSCRIBE_EQUITY_REG_LIST: {
				this.subscribeEquityRegionalList.add(ticker);
				subscribeTickers(ticker, this.equityRegionalSubscriptionUrl, this.equityRegionalStreamerId);
				addToSubscribeSymbolCount();
			}
				break;
			case SUBSCRIBE_OPTIONS_REG_LIST: {
				this.subscribeOptionRegionalList.add(ticker);
				subscribeTickers(ticker, this.optionsRegionalSubscriptionUrl, this.optionsRegionalStreamerId);
			}
				break;
			case DELAYED_LAST_ONLY_LIST: {
				this.delayedLastOnlyList.add(ticker);
				subscribeTickers(ticker, this.delayedSubscriptionUrl, this.delayedStreamerId);
				addToSubscribeSymbolCount();
			}
				break;
			default:
				log.info("UNKNOWN type " + map + " ticker: " + ticker + " userId: " + userId);
			}
		}
	}

	public void removeTicker(Integer map, String ticker) {
		switch (map) {
		case SUBSCRIBE_EQUITY_LIST: {
			this.subscribeList.remove(ticker);
			unsubscribeTickers(ticker, this.equitySubscriptionUrl, this.equityStreamerId);
		}
			break;
		case UNSUBSCRIBE_LIST: {
			this.unsubscribeList.remove(ticker);
		}
			break;
		case PENDING_LIST: {
			this.pendingList.remove(ticker);
		}
			break;
		case LAST_ONLY_LIST: {
			this.lastOnlyList.remove(ticker);
			unsubscribeTickers(ticker, this.equitySubscriptionUrl, this.equityStreamerId);
		}
			break;
		case DELAYED_LIST: {
			this.delayedList.remove(ticker);
			unsubscribeTickers(ticker, this.delayedSubscriptionUrl, this.delayedStreamerId);
		}
			break;
		case SUBSCRIBE_OPTIONS_LIST: {
			this.subscribeOptionList.remove(ticker);
			unsubscribeTickers(ticker, this.optionsSubscriptionUrl, this.optionsStreamerId);
		}
			break;
		case SUBSCRIBE_EQUITY_REG_LIST: {
			this.subscribeEquityRegionalList.remove(ticker);
			unsubscribeTickers(ticker, this.equityRegionalSubscriptionUrl, this.equityRegionalStreamerId);
		}
			break;
		case SUBSCRIBE_OPTIONS_REG_LIST: {
			this.subscribeOptionRegionalList.remove(ticker);
			unsubscribeTickers(ticker, this.optionsRegionalSubscriptionUrl, this.optionsRegionalStreamerId);
		}
			break;
		case DELAYED_LAST_ONLY_LIST: {
			this.delayedLastOnlyList.remove(ticker);
			unsubscribeTickers(ticker, this.delayedSubscriptionUrl, this.delayedStreamerId);
		}
			break;
		default:
			log.info("UNKNOWN type " + map + " ticker: " + ticker + " userId: " + userId);
		}
	}

	public void addAllTickers(Integer map, List<String> tickers) {
		switch (map) {
		case PENDING_LIST: {
			this.pendingList.addAll(tickers);
		}
			break;
		}
	}

	public void clearAllList() {
		unsubscribeTickers(convertSetToString(this.subscribeList), this.equitySubscriptionUrl, this.equityStreamerId);
		unsubscribeTickers(convertSetToString(this.lastOnlyList), this.equitySubscriptionUrl, this.equityStreamerId);
		unsubscribeTickers(convertSetToString(this.delayedList), this.delayedSubscriptionUrl, this.delayedStreamerId);
		unsubscribeTickers(convertSetToString(this.delayedLastOnlyList), this.delayedSubscriptionUrl,
				this.delayedStreamerId);
		this.subscribeList.clear();
		this.subscribeSymbolCount = 0;
		this.unsubscribeList.clear();
		this.pendingList.clear();
		this.lastOnlyList.clear();
		this.delayedList.clear();
		this.delayedLastOnlyList.clear();
		unsubscribeTickers(convertSetToString(this.subscribeOptionList), this.optionsSubscriptionUrl,
				this.optionsStreamerId);
		this.subscribeOptionList.clear();
		unsubscribeTickers(convertSetToString(this.subscribeEquityRegionalList), this.equityRegionalSubscriptionUrl,
				this.equityRegionalStreamerId);
		this.subscribeEquityRegionalList.clear();
		unsubscribeTickers(convertSetToString(this.subscribeOptionRegionalList), this.optionsRegionalSubscriptionUrl,
				this.optionsRegionalStreamerId);
		this.subscribeOptionRegionalList.clear();
	}

	public void clearAllList(String streamType) {
		switch (streamType) {
		case SymbolUpdaterCache.TICKER_TYPE_EQUITY:
			log.info("unsubscribing for " + SymbolUpdaterCache.TICKER_TYPE_EQUITY + " userId: " + userId);
			unsubscribeTickers(convertSetToString(this.subscribeList), this.equitySubscriptionUrl,
					this.equityStreamerId);
			unsubscribeTickers(convertSetToString(this.lastOnlyList), this.equitySubscriptionUrl,
					this.equityStreamerId);
			break;
		case SymbolUpdaterCache.TICKER_TYPE_OPTIONS:
			log.info("unsubscribing for " + SymbolUpdaterCache.TICKER_TYPE_OPTIONS + " userId: " + userId);
			unsubscribeTickers(convertSetToString(this.subscribeOptionList), this.optionsSubscriptionUrl,
					this.optionsStreamerId);
			break;
		case SymbolUpdaterCache.TICKER_TYPE_OPTIONS_MONTAGE:
			log.info("unsubscribing for " + SymbolUpdaterCache.TICKER_TYPE_OPTIONS_MONTAGE + " userId: " + userId);
			unsubscribeTickers(convertSetToString(this.subscribeOptionRegionalList),
					this.optionsRegionalSubscriptionUrl, this.optionsRegionalStreamerId);
			break;
		case SymbolUpdaterCache.TICKER_TYPE_EQUITY_MONTAGE:
			log.info("unsubscribing for " + SymbolUpdaterCache.TICKER_TYPE_EQUITY_MONTAGE + " userId: " + userId);
			unsubscribeTickers(convertSetToString(this.subscribeEquityRegionalList), this.equityRegionalSubscriptionUrl,
					this.equityRegionalStreamerId);
			break;
		case SymbolUpdaterCache.TICKER_TYPE_DELAYED:
			log.info("unsubscribing for " + SymbolUpdaterCache.TICKER_TYPE_DELAYED + " userId: " + userId);
			unsubscribeTickers(convertSetToString(this.delayedList), this.delayedSubscriptionUrl,
					this.delayedStreamerId);
			unsubscribeTickers(convertSetToString(this.delayedLastOnlyList), this.delayedSubscriptionUrl,
					this.delayedStreamerId);
			break;
		case SymbolUpdaterCache.ALL:
			log.info("unsubscribing for " + SymbolUpdaterCache.ALL + " userId: " + userId);
			clearAllList();
			break;
		default:
			log.error("Unknown " + streamType + " userId: " + userId);
			break;
		}
	}

	public void reSubscribe(String streamType) {
		switch (streamType) {
		case SymbolUpdaterCache.TICKER_TYPE_EQUITY:
			subscribeTickers(convertSetToString(this.subscribeList), this.equitySubscriptionUrl, this.equityStreamerId);
			subscribeTickers(convertSetToString(this.lastOnlyList), this.equitySubscriptionUrl, this.equityStreamerId);
			break;
		case SymbolUpdaterCache.TICKER_TYPE_OPTIONS:
			subscribeTickers(convertSetToString(this.subscribeOptionList), this.optionsSubscriptionUrl,
					this.optionsStreamerId);
			break;
		case SymbolUpdaterCache.TICKER_TYPE_OPTIONS_MONTAGE:
			subscribeTickers(convertSetToString(this.subscribeOptionRegionalList), this.optionsRegionalSubscriptionUrl,
					this.optionsRegionalStreamerId);
			break;
		case SymbolUpdaterCache.TICKER_TYPE_EQUITY_MONTAGE:
			subscribeTickers(convertSetToString(this.subscribeEquityRegionalList), this.equityRegionalSubscriptionUrl,
					this.equityRegionalStreamerId);
			break;
		case SymbolUpdaterCache.TICKER_TYPE_DELAYED:
			subscribeTickers(convertSetToString(this.delayedList), this.delayedSubscriptionUrl, this.delayedStreamerId);
			subscribeTickers(convertSetToString(this.delayedLastOnlyList), this.delayedSubscriptionUrl,
					this.delayedStreamerId);
			break;
		default:
			break;
		}
	}

	public void unsubscribe(Set<String> inputTickers) {
		Set<HashSet<String>> keys = this.setToUrlMap.keySet();
		for (HashSet<String> set : keys) {
			if (inputTickers.isEmpty())
				break;
			Set<String> intersection = new HashSet<>(set); // use the copy constructor
			intersection.retainAll(inputTickers);
			if (!intersection.isEmpty()) {
				String setString = convertSetToString(intersection);
				unsubscribeTickers(setString, this.setToUrlMap.get(set), this.setToIdMap.get(set));
				inputTickers.removeAll(intersection);
				set.removeAll(intersection);
			}
		}
	}

	private String convertSetToString(Set<String> abc) {
		if (abc != null && !abc.isEmpty()) {
			String separator = ",";
			StringBuilder sb = new StringBuilder();
			for (String s : abc)
				sb.append(separator).append(s);
			return sb.substring(separator.length());
		}
		return null;
	}

	private void subscribeTickers(String ticker, String subscriptionURL, String streamerID) {
		log.info("Requesting url: " + subscriptionURL + " ticker: " + ticker + " streamerId: " + streamerID
				+ " userId: " + userId);
		if (subscriptionURL != null && ticker != null) {
			try {
				subscriptionURL += "&OLD_TOPICS=&NEW_TOPICS=" + URLEncoder.encode(ticker, "UTF-8") + "&SUBEND=true";
				URL url = new URL(subscriptionURL);
				URLConnection urlConnection = url.openConnection();
				String myCookie = "StreamerID=" + streamerID;
				urlConnection.setRequestProperty("Cookie", myCookie);
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
						InputStream is = urlConnection.getInputStream();) {
					int bytesRead = 0;
					byte[] buffer = new byte[1024];
					while ((bytesRead = is.read(buffer)) != -1) {
						baos.write(buffer, 0, bytesRead);
					}
					byte[] results = baos.toByteArray();
					log.info("result: " + new String(results) + " streamerId: " + streamerID + " userId: " + userId);
				}
			} catch (IOException e) {
				log.error("streamerId: " + streamerID + " userId: " + userId + " " + e.getMessage(), e);
			}
		}
	}

	private void unsubscribeTickers(final String ticker, final String subscriptionURL, final String streamerID) {
		new Thread(() -> {
			String unsubscribeURL = subscriptionURL;
			log.info("Requesting url: " + subscriptionURL + " ticker: " + ticker + " streamerId: " + streamerID
					+ " userId: " + userId);
			if (unsubscribeURL != null && ticker != null) {
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
					byte[] buffer = new byte[1024];
					unsubscribeURL += "&OLD_TOPICS=" + URLEncoder.encode(ticker, "UTF-8") + "&NEW_TOPICS=&SUBEND=true";
					URL url;
					url = new URL(unsubscribeURL);
					URLConnection urlConnection = url.openConnection();
					String myCookie = "StreamerID=" + streamerID;
					urlConnection.setRequestProperty("Cookie", myCookie);
					try (InputStream is = urlConnection.getInputStream();) {
						int bytesRead = 0;
						while ((bytesRead = is.read(buffer)) != -1) {
							baos.write(buffer, 0, bytesRead);
						}
					}
					byte[] results = baos.toByteArray();
					log.info("result: " + new String(results) + " streamerId: " + streamerID + " userId: " + userId);
				} catch (IOException e) {
					log.error("streamerId: " + streamerID + " userId: " + userId + " " + e.getMessage(), e);
				}
			}
			log.info("Completed url: " + subscriptionURL + " ticker: " + ticker + " streamerId: " + streamerID
					+ " userId: " + userId);
		}).start();
	}

	private String makeTickersString(String[] tickers, int startIndex, int endIndex) {
		StringBuilder sb = new StringBuilder();
		for (int i = startIndex; i < endIndex; i++) {
			sb.append(",");
			sb.append(tickers[i]);
		}
		return sb.toString().substring(1);
	}

}
