package com.b4utrade.web.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.EquityMessageBean;
import com.b4utrade.bean.UserLoginStatusBean;
import com.b4utrade.bean.UserStreamerManagementBean;
import com.b4utrade.cache.EntitlementManager;
import com.b4utrade.cache.SymbolUpdaterCache;
import com.b4utrade.cache.TickerListManager;
import com.b4utrade.helper.RTDSessionValidator;
import com.google.gson.Gson;
import com.tacpoint.util.Environment;

public class RTDTickerValidationAndSnapAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(RTDTickerValidationAndSnapAction.class);
	private static final String DATA_QUALITY_REAL = "R";
	private static final String DATA_QUALITY_DELAYED = "D";
	private static final String DATA_QUALITY_UNSUBSCRIBED = "NE";
	private static final SymbolUpdaterCache symbolUpdaterCache = SymbolUpdaterCache.getInstance();
	private static final EntitlementManager entitlementManager = EntitlementManager.getInstance();
	private static final TickerListManager listManager = TickerListManager.getInstance();
	private static final TimeZone zone = TimeZone.getTimeZone("America/New_York");
	private static final Gson gson = new Gson();

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		this.doCheckUser = false;
		this.doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		HashMap<String, String> updatedTickerMap = new HashMap<>();
		HashMap<String, ArrayList<String>> mappedTickerMap = new HashMap<>();
		HashMap<String, EquityMessageBean> futureBeanMap = new HashMap<>();
		ArrayList<EquityMessageBean> snapDataList = new ArrayList<>();
		List<EquityMessageBean> beanList = new ArrayList<>();
		ArrayList<String> tickerList = new ArrayList<>();
		UserLoginStatusBean ulsb = null;
		String versionId = null;
		String status = null;
		boolean maxTickersSubscribed = false;
		String userIdAsString = null;
		try {
			response.setContentType("text/html");
			userIdAsString = request.getHeader("USERID");
			versionId = request.getHeader("VERSION");
			log.info("Requested version: " + versionId + " userId: " + userIdAsString);
			String tickers = null;
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream()));) {
				StringBuilder buffer = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					buffer.append(line);
				}
				tickers = buffer.toString();
			}
			Cookie[] cookies = request.getCookies();
			ulsb = RTDSessionValidator.handleCheckUserSession(cookies);
			if (ulsb.getStatus() == 1) {
				int userID = Integer.parseInt(userIdAsString);
				UserStreamerManagementBean userStreamerBean = listManager.getUserStreamingBean(userID);
				if (userStreamerBean != null && userStreamerBean.isLoggedIn()) {
					String modifiedTickers = null;
					String[] tickerArray = tickers.split(",");
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < tickerArray.length; i++) {
						String ticker = tickerArray[i];
						ticker = ticker.trim().toUpperCase();
						if (ticker.length() > 0) {
							String newTicker = symbolUpdaterCache.getUpdatedTicker(ticker);
							updatedTickerMap.put(ticker, newTicker);
							String mappedTicker = symbolUpdaterCache.getMappedSymbol(newTicker);
							String mappedTickerArray[] = mappedTicker.split(",");
							for (int j = 0; j < mappedTickerArray.length; j++) {
								String temp = mappedTickerArray[j];
								ArrayList<String> list = mappedTickerMap.get(temp);
								if (list == null)
									list = new ArrayList<>();
								sb.append(",");
								sb.append(temp);
								list.add(ticker);
								mappedTickerMap.put(temp, list);
								tickerList.add(temp);
							}
						}
					}
					UserStreamerManagementBean streamerBean = listManager.getUserStreamingBean(userID);
					if (streamerBean == null)
						return null;
					streamerBean.addAllTickers(UserStreamerManagementBean.PENDING_LIST, tickerList);
					modifiedTickers = sb.toString();
					if (modifiedTickers.length() > 1) {
						modifiedTickers = modifiedTickers.substring(1);
						snapDataList = getSnapData(modifiedTickers, userID);
						log.info("SnapListSize: " + snapDataList.size() + " userId: " + userIdAsString);
					}
					if (entitlementManager.isMyTrackBasicEntitled(userID)
							|| entitlementManager.isMyTrackSilverEntitled(userID)) {
						streamerBean.setMaxSymbols(25);
					} else if (entitlementManager.isMyTrackGoldEntitled(userID)) {
						streamerBean.setMaxSymbols(150);
					} else {
						streamerBean.setMaxSymbols(3500);
					}
					log.info("MaxSymbols: " + streamerBean.getMaxSymbols() + " userId: " + userIdAsString
							+ " SubscribeymbolCount " + streamerBean.getSubscribeSymbolCount());
					for (EquityMessageBean bean : snapDataList) {
						if (streamerBean.getSubscribeSymbolCount() > streamerBean.getMaxSymbols()) {
							maxTickersSubscribed = true;
							continue;
						}
						boolean moreThanOneOriginalTicker = false;
						String tickerType = bean.getTYPE();
						String mappedSymbol = bean.getMAPPED_TICKER();
						ArrayList<String> userInputTickerList = mappedTickerMap.get(mappedSymbol);
						if (userInputTickerList.size() > 1) {
							moreThanOneOriginalTicker = true;
						}
						String userInputTicker = userInputTickerList.get(0);
						bean.setTICKER(userInputTicker);
						String updatedTicker = updatedTickerMap.get(userInputTicker);
						bean.setUPDATED_TICKER(updatedTicker);
						log.info("OriginalTicker: " + userInputTicker + " UpdatedTicker: " + updatedTicker
								+ " ModifiedTicker: " + mappedSymbol + " tickerType: " + tickerType + " userId: "
								+ userIdAsString);
						streamerBean.removeTicker(UserStreamerManagementBean.PENDING_LIST, mappedSymbol);
						switch (tickerType) {
						case SymbolUpdaterCache.TICKER_TYPE_EQUITY: {
							if (symbolUpdaterCache.isVolTicker(mappedSymbol)) {
								beanList.add(bean);
								streamerBean.addTicker(UserStreamerManagementBean.SUBSCRIBE_EQUITY_LIST, mappedSymbol);
								continue;
							}
							String exchangeID = bean.getPROTOCOL();
							if (entitlementManager.isRealTimeEntitled(userID, exchangeID)) {
								if (entitlementManager.isLastOnlyEntitled(userID, exchangeID)) {
									setLastOnlyFields(bean);
									beanList.add(bean);
									streamerBean.addTicker(UserStreamerManagementBean.LAST_ONLY_LIST, mappedSymbol);
								} else {
									beanList.add(bean);
									streamerBean.addTicker(UserStreamerManagementBean.SUBSCRIBE_EQUITY_LIST,
											mappedSymbol);
								}
							} else if (entitlementManager.isDelayedEntitled(userID, exchangeID)) {
								ArrayList<EquityMessageBean> delayedSnapList = getDelayedSnapData(
										bean.getMAPPED_TICKER());
								log.info("userId: " + userID + " DelayedEntitled " + bean.getMAPPED_TICKER());
								if (delayedSnapList != null && !delayedSnapList.isEmpty()) {
									bean = delayedSnapList.get(0);
									bean.setTICKER(userInputTicker);
									if (entitlementManager.isLastOnlyEntitled(userID, exchangeID)) {
										setLastOnlyFields(bean);
										beanList.add(bean);
										streamerBean.addTicker(UserStreamerManagementBean.DELAYED_LAST_ONLY_LIST,
												mappedSymbol);
									} else {
										beanList.add(bean);
										streamerBean.addTicker(UserStreamerManagementBean.DELAYED_LIST, mappedSymbol);
									}
								}
							} else {
								// no entitlement
								setNotEntitledField(bean);
								beanList.add(bean);
								streamerBean.addTicker(UserStreamerManagementBean.UNSUBSCRIBE_LIST, mappedSymbol);
							}
						}
							break;
						case SymbolUpdaterCache.TICKER_TYPE_EQUITY_MONTAGE:
							if (entitlementManager.isEquityMontageEntitled(userID)) {
								beanList.add(bean);
								streamerBean.addTicker(UserStreamerManagementBean.SUBSCRIBE_EQUITY_REG_LIST,
										mappedSymbol);
							} else {
								setNotEntitledField(bean);
								beanList.add(bean);
								streamerBean.addTicker(UserStreamerManagementBean.UNSUBSCRIBE_LIST, mappedSymbol);
							}
							break;
						case SymbolUpdaterCache.TICKER_TYPE_FUTURES:
							String ticker = bean.getMAPPED_TICKER();
							String originalTicker = bean.getTICKER();
							if (entitlementManager.isFutureEntitled(userID, ticker)) {
								String exchange = symbolUpdaterCache.getFuturesExchange(ticker);
								if (entitlementManager.isRealTimeEntitled(userID, exchange)) {
									if (entitlementManager.isLastOnlyEntitled(userID, exchange)) {
										setLastOnlyFields(bean);
										bean = getLatestFutureBean(bean, futureBeanMap.get(originalTicker));
										futureBeanMap.put(originalTicker, bean);
										streamerBean.addTicker(UserStreamerManagementBean.LAST_ONLY_LIST, mappedSymbol);
									} else {
										bean = getLatestFutureBean(bean, futureBeanMap.get(originalTicker));
										futureBeanMap.put(originalTicker, bean);
										streamerBean.addTicker(UserStreamerManagementBean.SUBSCRIBE_EQUITY_LIST,
												mappedSymbol);
									}
								} else if (entitlementManager.isDelayedEntitled(userID, exchange)) {
									// getdataFromDelayedFuturesSnap
									ArrayList<EquityMessageBean> delayedSnapList = getDelayedSnapData(
											bean.getMAPPED_TICKER());
									log.info("userId: " + userID + " DelayedEntitled " + bean.getMAPPED_TICKER());
									if (delayedSnapList != null && !delayedSnapList.isEmpty()) {
										bean = delayedSnapList.get(0);
										bean.setTICKER(originalTicker);
										bean.setUPDATED_TICKER(originalTicker);
										if (entitlementManager.isLastOnlyEntitled(userID, exchange)) {
											setLastOnlyFields(bean);
											bean = getLatestFutureBean(bean, futureBeanMap.get(originalTicker));
											futureBeanMap.put(originalTicker, bean);
											streamerBean.addTicker(UserStreamerManagementBean.DELAYED_LAST_ONLY_LIST,
													mappedSymbol);
										} else {
											bean = getLatestFutureBean(bean, futureBeanMap.get(originalTicker));
											futureBeanMap.put(originalTicker, bean);
											streamerBean.addTicker(UserStreamerManagementBean.DELAYED_LIST,
													mappedSymbol);
										}
									}
								} else {
									// no entitlement
									setNotEntitledField(bean);
									futureBeanMap.put(originalTicker, bean);
									streamerBean.addTicker(UserStreamerManagementBean.UNSUBSCRIBE_LIST, mappedSymbol);
								}
							}
							break;
						case SymbolUpdaterCache.TICKER_TYPE_OPTIONS:
							if (entitlementManager.isOptionEntitled(userID)) {
								beanList.add(bean);
								streamerBean.addTicker(UserStreamerManagementBean.SUBSCRIBE_OPTIONS_LIST, mappedSymbol);
							} else {
								setNotEntitledField(bean);
								beanList.add(bean);
								streamerBean.addTicker(UserStreamerManagementBean.UNSUBSCRIBE_LIST, mappedSymbol);
							}
							break;
						case SymbolUpdaterCache.TICKER_TYPE_OPTIONS_MONTAGE:
							if (entitlementManager.isOptionMontageEntitled(userID)) {
								beanList.add(bean);
								streamerBean.addTicker(UserStreamerManagementBean.SUBSCRIBE_OPTIONS_REG_LIST,
										mappedSymbol);
							} else {
								setNotEntitledField(bean);
								beanList.add(bean);
								streamerBean.addTicker(UserStreamerManagementBean.UNSUBSCRIBE_LIST, mappedSymbol);
							}
							break;
						}
						if (moreThanOneOriginalTicker) {
							for (int k = 1; k < userInputTickerList.size(); k++) {
								String ticker = userInputTickerList.get(k);
								EquityMessageBean duplicateBean = createDuplicateBean(bean, ticker);
								duplicateBean.setUPDATED_TICKER(updatedTickerMap.get(ticker));
								beanList.add(duplicateBean);
							}
						}
					}
					Iterator<Entry<String, EquityMessageBean>> futureBeanIterator = futureBeanMap.entrySet().iterator();
					while (futureBeanIterator.hasNext()) {
						Map.Entry<String, EquityMessageBean> entry = futureBeanIterator.next();
						EquityMessageBean b = entry.getValue();
						beanList.add(b);
					}
				}
				if (maxTickersSubscribed) {
					status = "Maximum Symbol Count allowed have been subscribed already.";
				} else {
					status = "OK";
				}
			} else {
				status = "Authentication Failed";
				response.setStatus(401);
			}
		} catch (Exception e) {
			log.error("userId: " + userIdAsString + " " + e.getMessage(), e);
		}
		log.info("resultSize: " + beanList.size() + " userId: " + userIdAsString);
		HashMap<String, Object> resultMap = new HashMap<>();
		String currentRtdVersion = Environment.get("CURRENT_RTD_VERSION");
		double rtdVersion = Double.parseDouble(currentRtdVersion);
		String isBuildUpdated = "false";
		String rtdLink = null;
		if (rtdVersion > Double.parseDouble(versionId)) {
			isBuildUpdated = "true";
			rtdLink = Environment.get("RTD_LINK");
		}
		resultMap.put("is_updated", isBuildUpdated);
		resultMap.put("build_link", "" + rtdLink);
		resultMap.put("status", "" + status);
		resultMap.put("data", beanList);
		String output = gson.toJson(resultMap);
		try (PrintWriter sos = response.getWriter();) {
			sos.write(output);
			sos.flush();
		} catch (Exception e) {
			log.error("userId: " + userIdAsString + " " + e.getMessage(), e);
		}
		return null;
	}

	private EquityMessageBean createDuplicateBean(EquityMessageBean bean, String ticker) {
		EquityMessageBean duplicateBean = new EquityMessageBean();
		duplicateBean.setASK_EXCH(bean.getASK_EXCH());
		duplicateBean.setASK(bean.getASK());
		duplicateBean.setASK_SIZE(bean.getASK_SIZE());
		duplicateBean.setBID_EXCH(bean.getBID_EXCH());
		duplicateBean.setBID(bean.getBID());
		duplicateBean.setBID_SIZE(bean.getBID_SIZE());
		duplicateBean.setCHANGE_PRICE(bean.getCHANGE_PRICE());
		duplicateBean.setDAY_HIGH(bean.getDAY_HIGH());
		duplicateBean.setDAY_LOW(bean.getDAY_LOW());
		duplicateBean.setPROTOCOL(bean.getPROTOCOL());
		duplicateBean.setPREVIOUS_PRICE(bean.getPREVIOUS_PRICE());
		duplicateBean.setLAST(bean.getLAST());
		duplicateBean.setTRADE_SIZE(bean.getTRADE_SIZE());
		duplicateBean.setMAPPED_TICKER(bean.getMAPPED_TICKER());
		duplicateBean.setOPEN(bean.getOPEN());
		duplicateBean.setTICKER(ticker);
		duplicateBean.setTYPE(bean.getTYPE());
		duplicateBean.setVOLUME(bean.getVOLUME());
		duplicateBean.setVWAP(bean.getVWAP());
		duplicateBean.setLAST_TRADE_TIME(bean.getLAST_TRADE_TIME());
		duplicateBean.setDATA_QUALITY(bean.getDATA_QUALITY());
		return duplicateBean;
	}

	private ArrayList<EquityMessageBean> getSnapData(String modifiesTickers, int userId) {
		ArrayList<EquityMessageBean> result = new ArrayList<>();
		HashMap<String, LinkedList<byte[]>> snapData = null;
		entitlementManager.reloadUserEntitlements(userId);
		try {
			String snapUrl = null;
			if (entitlementManager.isNasdaqEntitled(userId)) {
				log.info("userId: " + userId + "  Nasdaqbasic entitled ");
				snapUrl = Environment.get("RTD_SNAP_NASDAQ_BASIC_URL");
				if (snapUrl == null) {
					snapUrl = "http://199.96.250.203/servlet/RTDQuoteServlet?COMPANYID=";
				}
			} else {
				log.info("userId: " + userId + "  Nasdaqbasic not entitled ");
				snapUrl = Environment.get("RTD_SNAP_URL");
				if (snapUrl == null) {
					snapUrl = "http://www.quodd.com/servlet/RTDQuoteServlet?COMPANYID=";
				}
			}
			StringBuilder updatedUrl = new StringBuilder(snapUrl);
			updatedUrl.append(URLEncoder.encode(modifiesTickers, "UTF-8"));
			URL url = new URL(updatedUrl.toString());
			URLConnection urlConnection = url.openConnection();
			Object o = null;
			try (ObjectInputStream objectStream = new ObjectInputStream(urlConnection.getInputStream());) {
				o = objectStream.readObject();
			} catch (ClassNotFoundException e) {
				log.error("userId: " + userId + " " + e.getMessage(), e);
			}
			if (o != null) {
				snapData = (HashMap<String, LinkedList<byte[]>>) o;
				Set<String> keys = snapData.keySet();
				for (String type : keys) {
					LinkedList<byte[]> list = snapData.get(type);
					if (list != null && !list.isEmpty()) {
						for (byte[] stream : list) {
							EquityMessageBean bean = new EquityMessageBean(new String(stream), type, DATA_QUALITY_REAL);
							bean.setLAST_TRADE_TIME(
									formatLastTradeTime(bean.getLAST_TRADE_TIME(), bean.getTRADE_DATE()));
							result.add(bean);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("userId: " + userId + " " + e.getMessage(), e);
		}
		return result;
	}

	private ArrayList<EquityMessageBean> getDelayedSnapData(String modifiesTickers) {
		ArrayList<EquityMessageBean> result = new ArrayList<>();
		HashMap<String, LinkedList<byte[]>> snapData = null;
		try {
			String snapUrl = Environment.get("RTD_DELAYED_SNAP_URL");
			if (snapUrl == null) {
				snapUrl = "http://snapdly.quodd.com/servlet/RTDQuoteServlet?COMPANYID=";
			}
			StringBuilder updatedUrl = new StringBuilder(snapUrl);
			updatedUrl.append(URLEncoder.encode(modifiesTickers, "UTF-8"));
			URL url = new URL(updatedUrl.toString());
			URLConnection urlConnection = url.openConnection();
			Object o = null;
			try (ObjectInputStream objectStream = new ObjectInputStream(urlConnection.getInputStream());) {
				o = objectStream.readObject();
			} catch (ClassNotFoundException e) {
				log.error(e.getMessage(), e);
			}
			if (o != null) {
				snapData = (HashMap<String, LinkedList<byte[]>>) o;
				Set<String> keys = snapData.keySet();
				for (String type : keys) {
					LinkedList<byte[]> list = snapData.get(type);
					if (list != null && !list.isEmpty()) {
						for (byte[] stream : list) {
							EquityMessageBean bean = new EquityMessageBean(new String(stream), type,
									DATA_QUALITY_DELAYED);
							bean.setLAST_TRADE_TIME(
									formatLastTradeTime(bean.getLAST_TRADE_TIME(), bean.getTRADE_DATE()));
							result.add(bean);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	private EquityMessageBean getLatestFutureBean(EquityMessageBean currentBean, EquityMessageBean previousBean) {
		String currentBeanLttStr = null;
		String previousBeanLttStr = null;
		if (currentBean != null) {
			currentBeanLttStr = currentBean.getTRADE_DATE() + formatFutureLtt(currentBean.getLAST_TRADE_TIME());
		}
		if (previousBean != null) {
			previousBeanLttStr = previousBean.getTRADE_DATE() + formatFutureLtt(previousBean.getLAST_TRADE_TIME());
		}
		long currentLTT = 0;
		long previousLTT = 0;
		try {
			if (currentBeanLttStr != null && !currentBeanLttStr.isEmpty()
					&& !currentBeanLttStr.equalsIgnoreCase("null")) {
				currentLTT = Long.parseLong(currentBeanLttStr);
			}
		} catch (NumberFormatException e) {
			log.error(e.getMessage(), e);
			currentLTT = 0;
		}
		try {
			if (previousBeanLttStr != null && !previousBeanLttStr.isEmpty()
					&& !previousBeanLttStr.equalsIgnoreCase("null")) {
				previousLTT = Long.parseLong(previousBeanLttStr);
			}
		} catch (NumberFormatException e) {
			log.error(e.getMessage(), e);
			previousLTT = 0;
		}
		if (previousLTT < currentLTT) {
			return currentBean;
		}
		return previousBean;
	}

	private String formatFutureLtt(String ltt) {
		if (ltt.indexOf(":") > -1) {
			while (ltt.indexOf(":") != -1) {
				int index = ltt.indexOf(":");
				String temp = ltt.substring(0, index);
				ltt = temp + ltt.substring(index + 1, ltt.length());
			}
		} else if (ltt.indexOf("/") > -1) {
			while (ltt.indexOf("/") != -1) {
				int index = ltt.indexOf("/");
				String temp = ltt.substring(0, index);
				ltt = temp + ltt.substring(index + 1, ltt.length());
			}
		}
		return ltt;
	}

	private String formatLastTradeTime(String time, String dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		sdf.setTimeZone(zone);
		String curdate = sdf.format(new Date());
		if (dt != null && dt.equalsIgnoreCase(curdate)) {
			if ((time != null) && (time.length() > 0) && (!time.equalsIgnoreCase("null"))) {
				time = formatTime(time);
			}
		} else if ((dt != null) && (dt.length() > 0) && (!dt.equalsIgnoreCase("null"))) {
			time = formatDate(dt);
		} else
			time = "";
		return time;
	}

	private String formatTime(String val) {
		try {
			if (val.length() == 6) {
				String hr = val.substring(0, 2);
				String min = val.substring(2, 4);
				String sec = val.substring(4, 6);
				val = hr + ":" + min + ":" + sec;
			} else if (val.length() == 5) {
				String hr = val.substring(0, 1);
				String min = val.substring(1, 3);
				String sec = val.substring(3, 5);
				val = hr + ":" + min + ":" + sec;
			} else if (val.length() == 9) {
				String hr = val.substring(0, 2);
				String min = val.substring(2, 4);
				String sec = val.substring(4, 6);
				String mil = val.substring(6, 9);
				val = hr + ":" + min + ":" + sec + "." + mil;
			} else if (val.length() == 8) {
				String hr = val.substring(0, 1);
				String min = val.substring(1, 3);
				String sec = val.substring(3, 5);
				String mil = val.substring(5, 8);
				val = hr + ":" + min + ":" + sec + "." + mil;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return val;
	}

	private String formatDate(String val) {
		try {
			if (val.length() == 8) {
				String yyyy = val.substring(0, 4);
				String mm = val.substring(4, 6);
				String dd = val.substring(6, 8);
				val = mm + "/" + dd + "/" + yyyy.substring(2, 4);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return val;
	}

	private void setNotEntitledField(EquityMessageBean bean) {
		bean.setASK_EXCH(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setASK(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setASK_SIZE(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setBID_EXCH(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setBID(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setBID_SIZE(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setCHANGE_PRICE(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setDAY_HIGH(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setDAY_LOW(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setPREVIOUS_PRICE(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setLAST(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setTRADE_SIZE(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setOPEN(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setTYPE(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setVOLUME(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setVWAP(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setLAST_TRADE_TIME(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setDATA_QUALITY(DATA_QUALITY_UNSUBSCRIBED);
	}

	private void setLastOnlyFields(EquityMessageBean bean) {
		bean.setASK_EXCH(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setASK(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setASK_SIZE(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setBID_EXCH(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setBID(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
		bean.setBID_SIZE(EntitlementManager.DEFAULT_NOT_ENTITLED_VALUE);
	}
}