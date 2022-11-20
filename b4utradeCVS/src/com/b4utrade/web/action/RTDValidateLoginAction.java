package com.b4utrade.web.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.UserLoginStatusBean;
import com.b4utrade.bean.UserStreamerManagementBean;
import com.b4utrade.bo.UserBO;
import com.b4utrade.cache.EntitlementManager;
import com.b4utrade.cache.RTDUserSessionManager;
import com.b4utrade.cache.SymbolUpdaterCache;
import com.b4utrade.cache.TickerListManager;
import com.b4utrade.helper.RTDUsageLogManager;
import com.b4utrade.helper.SingleLoginHelper;
import com.b4utrade.network.QTMessageKeys;
import com.google.gson.Gson;
import com.tacpoint.util.Environment;

/**
 * The class manages session check for RTD users. Provide URL info along with
 * session Check
 */

public class RTDValidateLoginAction extends B4UTradeDefaultAction {

	private static final Log log = LogFactory.getLog(RTDValidateLoginAction.class);
	private static final EntitlementManager enManager = EntitlementManager.getInstance();
	private static final TickerListManager tickerManager = TickerListManager.getInstance();
	private static final Gson gson = new Gson();
	private static final String ALL = "ALL";
	private static final RTDUsageLogManager logManager = RTDUsageLogManager.getInstance();

	public RTDValidateLoginAction() {
	}

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		UserLoginStatusBean ulsb = new UserLoginStatusBean();
		String sesionId = null;
		try {
			response.setContentType("text/x-json;charset=UTF-8");
			String userIdAsString = request.getHeader("USERID");
			String versionId = request.getHeader("VERSION");
			String environment = request.getHeader("ENVIRONMENT");
			String officeVersion = request.getHeader("OFFICEVERSION");
			String streamerType = request.getParameter("STREAMERTYPE");
			String rtdLogin = request.getParameter("RTD_LOGIN");
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					String name = cookie.getName();
					if (name.equals(Environment.get("SESSION_COOKIE"))) {
						sesionId = cookie.getValue();
					}
				}
			}
			log.info("Requested Version: " + versionId + "  streamerType: " + streamerType + " userId: "
					+ userIdAsString + " environment: " + environment + " office_version: " + officeVersion
					+ " rtdLogin: " + rtdLogin + " session: " + sesionId);
			if (sesionId == null) {
				ulsb.setStatusSessionCheckFailed();
			} else {
				try {
					SingleLoginHelper slh = new SingleLoginHelper();
					slh.setSessionID(sesionId);
					if (slh.doesEQPlusSessionExist()) {
						ulsb.setUserID(slh.getUserID());
						ulsb.setStatusLoginSuccess();
						ulsb.setMessage("Session Authenticated");
						log.info("AUTHENTICATED  userId: " + userIdAsString + " session: " + sesionId + " slh_userId: "
								+ slh.getUserID());
					} else {
						ulsb.setStatusSessionCheckFailed();
						ulsb.setMessage("Invalid Session");
						if (rtdLogin != null && rtdLogin.equalsIgnoreCase("TRUE")) {
							log.info("Requested RtdLogin username: "
									+ request.getHeader(UserLoginStatusBean.USERNAME_KEY) + " password: "
									+ request.getHeader(UserLoginStatusBean.PASSWORD_KEY) + " source: " + environment);
							sesionId = handleLogin(request, environment, ulsb);
						}
					}
				} catch (Exception e) {
					log.error("userId: " + ulsb.getUserID() + " session: " + sesionId + " " + e.getMessage(), e);
					ulsb.setStatusSessionCheckFailed();
				}
			}
			Map<String, String> result = new HashMap<>();
			result.put("userID", "" + ulsb.getUserID());
			result.put("status", "" + ulsb.getStatus());
			result.put("message", "" + ulsb.getMessage());
			log.info("userId: " + ulsb.getUserID() + " session: " + sesionId + " " + ulsb.getStatus() + " "
					+ ulsb.getMessage());
			if (ulsb.getStatus() == 1) {
				result.putAll(loadRTDProperties(ulsb.getUserID(), sesionId, streamerType));
				String currentRtdVersion = Environment.get("CURRENT_RTD_VERSION");
				double rtdVersion = Double.parseDouble(currentRtdVersion);
				String isBuildUpdated = "false";
				String rtdLink = null;
				if (rtdVersion > Double.parseDouble(versionId)) {
					isBuildUpdated = "true";
					rtdLink = Environment.get("RTD_LINK");
				}
				result.put("is_updated", isBuildUpdated);
				result.put("build_link", rtdLink);
				RTDUserSessionManager.getInstance().storeUserSession(ulsb.getUserID(), sesionId);
				if (streamerType.equals(ALL)) {
					logManager.addQuery(String.valueOf(ulsb.getUserID()), "1", versionId);
				}
			}
			String output = gson.toJson(result);
			log.info("RESPONSE " + output + " userId: " + ulsb.getUserID());
			try (PrintWriter sos = response.getWriter();) {
				sos.write(output);
				sos.flush();
			} catch (Exception e) {
				log.error("userId: " + ulsb.getUserID() + " session: " + sesionId + " " + e.getMessage(), e);
			}
		} catch (Exception e) {
			log.error("userId: " + ulsb.getUserID() + " session: " + sesionId + " " + e.getMessage(), e);
		}
		return null;
	}

	private Map<String, String> loadRTDProperties(int userId, String sessionId, String streamerType) {
		Map<String, String> result = new HashMap<>();
		Properties props = new Properties();
		try (InputStream is = getClass().getResourceAsStream("/rtd.startup.properties");) {
			props.load(is);
			enManager.reloadUserEntitlements(userId);
			UserStreamerManagementBean bean = tickerManager.getUserStreamingBean(userId);
			log.info("userId: " + userId + " session: " + sessionId + " type: " + streamerType);
			if (bean == null) {
				log.info("Null UserStreamerManagementBean bean userId: " + userId + " session: " + sessionId);
				bean = new UserStreamerManagementBean();
				bean.setUserId(userId);
				tickerManager.addUserStreamingBean(bean);
			} else {
				bean.clearAllList(streamerType);
			}
			bean.setSessionId(sessionId);
			bean.setLoggedIn(true);
			// It required snap call from User after this to obtain a new list - cleared to
			// apply new entitlements
			String streamingUrl = null;
			String subscriptionUrl = null;
			if (streamerType.equals(ALL) || streamerType.equals(SymbolUpdaterCache.TICKER_TYPE_EQUITY)) {
				if (enManager.isNasdaqEntitled(userId)) {
					streamingUrl = props.getProperty("nasdaqBasicStreamingURL");
					subscriptionUrl = props.getProperty("nasdaqBasicSubscriptionURL");
				} else {
					streamingUrl = props.getProperty("equityStreamingURL");
					subscriptionUrl = props.getProperty("equitySubscriptionURL");
				}
				String streamerID = getStreamerId(subscriptionUrl, userId, sessionId);
				if (streamerID != null) {
					String equityStreamingURL = streamingUrl + "?USERID=" + userId
							+ "_Equity_QuoddRTD&TOPICS=&SUBEND=true";
					String equitySubscribeURL = subscriptionUrl + "?USERID=" + userId
							+ "_Equity_QuoddRTD&ACTION=MODIFY";
					if (streamerType.equals(SymbolUpdaterCache.TICKER_TYPE_EQUITY)) {
						result.put("streamingURL", equityStreamingURL);
						result.put("streamerId", streamerID);
					} else {
						result.put("equityStreamingURL", equityStreamingURL);
						result.put("equityStreamerId", streamerID);
					}
					bean.setEquitySubscriptionUrl(equitySubscribeURL);
					bean.setEquityStreamerId(streamerID);
				}
			}
			// remove all in order to keep end loop working
			props.remove("equityStreamingURL");
			props.remove("equitySubscriptionURL");
			props.remove("nasdaqBasicStreamingURL");
			props.remove("nasdaqBasicSubscriptionURL");
			if (streamerType.equals(ALL) || streamerType.equals(SymbolUpdaterCache.TICKER_TYPE_OPTIONS)) {
				if (enManager.isOptionEntitled(userId)) {
					String streamerID = getStreamerId(props.getProperty("optionSubscriptionURL"), userId, sessionId);
					if (streamerID != null) {
						String optionsStreamingURL = props.getProperty("optionStreamingURL") + "?USERID=" + userId
								+ "_Options_QuoddRTD&TOPICS=&SUBEND=true";
						String optionsSubscribeURL = props.getProperty("optionSubscriptionURL") + "?USERID=" + userId
								+ "_Options_QuoddRTD&ACTION=MODIFY";
						if (streamerType.equals(SymbolUpdaterCache.TICKER_TYPE_OPTIONS)) {
							result.put("streamingURL", optionsStreamingURL);
							result.put("streamerId", streamerID);
						} else {
							result.put("optionStreamingURL", optionsStreamingURL);
							result.put("optionsStreamerId", streamerID);
						}
						bean.setOptionsSubscriptionUrl(optionsSubscribeURL);
						bean.setOptionsStreamerId(streamerID);
					}
				} else {
					bean.setOptionsSubscriptionUrl(null);
					bean.setOptionsStreamerId(null);
				}
			}
			props.remove("optionStreamingURL");
			props.remove("optionSubscriptionURL");
			if (streamerType.equals(ALL) || streamerType.equals(SymbolUpdaterCache.TICKER_TYPE_OPTIONS_MONTAGE)) {
				if (enManager.isOptionMontageEntitled(userId)) {
					String streamerID = getStreamerId(props.getProperty("optionRegSubscriptionURL"), userId, sessionId);
					if (streamerID != null) {
						String optionsRegStreamingURL = props.getProperty("optionRegStreamingURL") + "?USERID=" + userId
								+ "_OptionsReg_QuoddRTD&TOPICS=&SUBEND=true";
						String optionsRegSubscribeURL = props.getProperty("optionRegSubscriptionURL") + "?USERID="
								+ userId + "_OptionsReg_QuoddRTD&ACTION=MODIFY";
						if (streamerType.equals(SymbolUpdaterCache.TICKER_TYPE_OPTIONS_MONTAGE)) {
							result.put("streamingURL", optionsRegStreamingURL);
							result.put("streamerId", streamerID);
						} else {
							result.put("optionRegStreamingURL", optionsRegStreamingURL);
							result.put("optionRegStreamerId", streamerID);
						}
						bean.setOptionsRegionalSubscriptionUrl(optionsRegSubscribeURL);
						bean.setOptionsRegionalStreamerId(streamerID);
					}
				} else {
					bean.setOptionsRegionalSubscriptionUrl(null);
					bean.setOptionsRegionalStreamerId(null);
				}
			}
			props.remove("optionRegStreamingURL");
			props.remove("optionRegSubscriptionURL");
			if (streamerType.equals(ALL) || streamerType.equals(SymbolUpdaterCache.TICKER_TYPE_EQUITY_MONTAGE)) {
				if (enManager.isEquityMontageEntitled(userId)) {
					String streamerID = getStreamerId(props.getProperty("equityRegSubscriptionURL"), userId, sessionId);
					if (streamerID != null) {
						String equityRegStreamingURL = props.getProperty("equityRegStreamingURL") + "?USERID=" + userId
								+ "_EquityReg_QuoddRTD&TOPICS=&SUBEND=true";
						String equityRegSubscribeURL = props.getProperty("equityRegSubscriptionURL") + "?USERID="
								+ userId + "_EquityReg_QuoddRTD&ACTION=MODIFY";
						if (streamerType.equals(SymbolUpdaterCache.TICKER_TYPE_EQUITY_MONTAGE)) {
							result.put("streamingURL", equityRegStreamingURL);
							result.put("streamerId", streamerID);
						} else {
							result.put("equityRegStreamingURL", equityRegStreamingURL);
							result.put("equityRegStreamerId", streamerID);
						}
						bean.setEquityRegionalSubscriptionUrl(equityRegSubscribeURL);
						bean.setEquityRegionalStreamerId(streamerID);
					}
				} else {
					bean.setEquityRegionalSubscriptionUrl(null);
					bean.setEquityRegionalStreamerId(null);
				}
			}
			props.remove("equityRegStreamingURL");
			props.remove("equityRegSubscriptionURL");
			if (streamerType.equals(ALL) || streamerType.equals(SymbolUpdaterCache.TICKER_TYPE_DELAYED)) {
				String streamerID = getStreamerId(props.getProperty("delayedSubscriptionURL"), userId, sessionId);
				if (streamerID != null) {
					String delayedStreamingURL = props.getProperty("delayedStreamingURL") + "?USERID=" + userId
							+ "_Delayed_QuoddRTD&TOPICS=&SUBEND=true";
					String delayedSubscribeURL = props.getProperty("delayedSubscriptionURL") + "?USERID=" + userId
							+ "_Delayed_QuoddRTD&ACTION=MODIFY";
					if (streamerType.equals(SymbolUpdaterCache.TICKER_TYPE_DELAYED)) {
						result.put("streamingURL", delayedStreamingURL);
						result.put("streamerId", streamerID);
					} else {
						result.put("delayedStreamingURL", delayedStreamingURL);
						result.put("delayedStreamerId", streamerID);
					}
					bean.setDelayedSubscriptionUrl(delayedSubscribeURL);
					bean.setDelayedStreamerId(streamerID);
				}
			}
			props.remove("delayedStreamingURL");
			props.remove("delayedySubscriptionURL");
			if (streamerType.equals(ALL)) {
				Enumeration<Object> keys = props.keys();
				while (keys.hasMoreElements()) {
					String key = (String) keys.nextElement();
					String value = props.getProperty(key);
					result.put(key, value);
				}
			}
		} catch (Exception e) {
			log.error("userId: " + userId + " session: " + sessionId + " " + e.getMessage(), e);
		}
		return result;
	}

	private String getStreamerId(String streamerCheckUrl, int userId, String sessionId) {
		String streamerId = null;
		if (streamerCheckUrl == null) {
			log.info("NULL_URL userId: " + userId + " session: " + sessionId + " url: " + streamerCheckUrl);
			return streamerId;
		}
		StringBuilder updatedUrl = new StringBuilder(streamerCheckUrl);
		updatedUrl.append("?ACTION=CHECK&Authorization=Basic%20c3RyZWFtZXI6c3RyM2FtZXIh&SUBEND=true");
		try {
			URL url = new URL(updatedUrl.toString());
			URLConnection urlConnection = url.openConnection();
			try (InputStream inputStream = urlConnection.getInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
				int bytesRead = -1;
				byte[] buffer = new byte[1024];
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					baos.write(buffer, 0, bytesRead);
				}
				Map<String, String> map = parseKeyValues(baos.toByteArray());
				if (map.containsKey(QTMessageKeys.STREAMER_ID))
					streamerId = map.get(QTMessageKeys.STREAMER_ID);
			}
		} catch (Exception e) {
			log.error(
					"userId: " + userId + " session: " + sessionId + " url: " + streamerCheckUrl + " " + e.getMessage(),
					e);
		}
		return streamerId;
	}

	private Map<String, String> parseKeyValues(byte[] bytes) {
		HashMap<String, String> map = new HashMap<>();
		StringTokenizer st = new StringTokenizer(new String(bytes), QTMessageKeys.TUPLE_SEP);
		while (st.hasMoreTokens()) {
			String tuple = st.nextToken();
			int index = tuple.indexOf(QTMessageKeys.FIELD_SEP);
			map.put(tuple.substring(0, index), tuple.substring(index + 1));
		}
		return map;
	}

	private String handleLogin(HttpServletRequest request, String environment, UserLoginStatusBean ulsb) {
		String sessionIdAfterLogin = null;
		String userName = null;
		try {
			userName = request.getHeader(UserLoginStatusBean.USERNAME_KEY);
			String password = request.getHeader(UserLoginStatusBean.PASSWORD_KEY);
			if (userName == null || userName.isEmpty() || password == null || password.isEmpty()) {
				log.error("RTDValidateLoginAction.handleLogin - null parameter " + userName + "," + password);
				ulsb.setStatusLoginFailed();
				return sessionIdAfterLogin;
			}

			UserBO user = new UserBO();
			user.setUserName(userName);
			user.setUserPassword(password);
			String token = user.handleLogin(ulsb);

			if (ulsb.isLoginFailed()) {
				return sessionIdAfterLogin;
			} else {
				int userId = ulsb.getUserID(); // qss4 id
				try {
					request.getSession(false).invalidate();
				} catch (Exception ex) {
					log.error("RTDValidateLoginAction.handleLogin(): username: " + userName + " userId: " + userId
							+ " session: " + sessionIdAfterLogin + " " + ex.getMessage(), ex);
				}
				HttpSession hs = request.getSession(true);
				log.info("RTDValidateLoginAction.handleLogin(): LOGIN_SUCCESFUL username: " + userName + " userId: "
						+ userId + " session: " + hs.getId());

				SingleLoginHelper slh = new SingleLoginHelper();
				slh.setUserID(userId);
				slh.setSessionID(hs.getId());
				slh.setSource(environment);
				sessionIdAfterLogin = slh.getSessionID();

				if (slh.isCurrentUserLoggedOn()) {
					String userID = user.getID() + "";
					log.info("ALREAY_LOGGED_IN username: " + userName + " userId: " + user.getUserID() + " session: "
							+ hs.getId());
					UserLoginStatusBean expireUserObj = handleExpireSession(userID);
					if (expireUserObj.isExpireOtherFailed()) {
						log.info("EXPIRATION_FAILED username: " + userName + " userId: " + userId + " session: "
								+ hs.getId());
						ulsb.setStatusLoginDuplicate();
						return sessionIdAfterLogin;
					}
				}
				// NEED THESE VALUES, SO THAT WE CAN RETAIN ORIGINAL SESSION ID
				slh.setUserID(userId);
				slh.setSessionID(hs.getId());
				slh.setSource(environment);
				hs.putValue("SLH", slh);
				hs.putValue("USERNAME", userName);
				hs.putValue("PASSWORD", password);
				hs.putValue("USERFIRSTNAME", user.getFirstName());
				hs.putValue("USERID", "" + userId);
				hs.putValue("QUODD_JWT", token);
				hs.setAttribute("SLH", slh);
				hs.setAttribute("USERID", "" + userId);
				hs.setAttribute("USERNAME", "" + userName);
				hs.setAttribute("PASSWORD", "" + password);
				hs.setAttribute("USERFIRSTNAME", "" + ulsb.getFirstName());
				hs.setAttribute("QUODD_JWT", token);
				ulsb.setStatusLoginSuccess();
			}
		} catch (Exception e) {
			log.error("username: " + userName + " session: " + sessionIdAfterLogin + " " + e.getMessage(), e);
			ulsb.setStatusLoginFailed();
		}
		return sessionIdAfterLogin;
	}

	private UserLoginStatusBean handleExpireSession(String userId) {
		log.info("Start Session Expiration userId: " + userId);
		UserLoginStatusBean ulsb = new UserLoginStatusBean();
		try {
			if (userId != null && !userId.isEmpty()) {
				SingleLoginHelper sh = new SingleLoginHelper(Integer.parseInt(userId));
				sh.deleteUser();
				ulsb.setStatusExpireOtherSuccess();
			} else {
				ulsb.setStatusExpireOtherFail();
			}
			log.info("End Session Expiration userId: " + userId + " status: " + ulsb.getStatus());
		} catch (Exception e) {
			log.error("userId: " + userId + " " + e.getMessage(), e);
			ulsb.setStatusExpireOtherFail();
		}
		return ulsb;
	}
}
