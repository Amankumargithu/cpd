package com.b4utrade.cache;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.b4utrade.bean.EntitlementPropertyBean;
import com.b4utrade.bean.QuoddUserEntitlementBean;
import com.b4utrade.bean.UserEntitlementBean;
import com.b4utrade.bo.UserBO;

public class EntitlementManager {

	static Log log = LogFactory.getLog(EntitlementManager.class);
	private static EntitlementManager instance = null;
	private static final String ENTITLEMENT_EQUITY_MONTAGE = "EMontage_access";
	private static final String ENTITLEMENT_CUSIP = "CUSIP_access";
	private static final String ENTITLEMENT_OPTION_MONTAGE = "OMontage_access";
	// MyTrack Entitlements
	private static final String ENTITLEMENT_MYTRACK_BASIC = "mytrackbasic_access";
	private static final String ENTITLEMENT_MYTRACK_SILVER = "mytracksilv_access";
	private static final String ENTITLEMENT_MYTRACK_GOLD = "mytrackgold_access";
	private static final String ENTITLEMENT_MYTRACK_PLATINUM = "mytrackplatinum_access";
	public static final String ENTITLEMENT_NONPROAGREEMENT = "nonprostatus_access";
	private static final String OPTIONS_SYMBOL_PREFIX = "O:";
	private static final String FUTURES_SYMBOL_PREFIX = "/";
	private static final String MONTAGE_IDENTIFIER = "/";
	public static final String DEFAULT_NOT_ENTITLED_VALUE = "N.E.";
	private ConcurrentHashMap<Integer, QuoddUserEntitlementBean> entitlementsMap = new ConcurrentHashMap<>();
	private boolean pinkEntitlement = false;
	private boolean nonPinkQuotesEntitlement = false;

	private EntitlementManager() {
	}

	public static EntitlementManager getInstance() {
		if (instance == null) {
			synchronized (EntitlementManager.class) {
				if (instance == null) {
					instance = new EntitlementManager();
				}
			}
		}
		return instance;
	}

	public QuoddUserEntitlementBean get(Integer userId) {
		QuoddUserEntitlementBean bean = null;
		if (userId != null) {
			bean = entitlementsMap.get(userId);
			if (bean == null) {
				bean = getUserEntitlementBean(userId);
				if (bean != null)
					entitlementsMap.put(userId, bean);
			}
		}
		return bean;
	}

	private QuoddUserEntitlementBean getUserEntitlementBean(Integer userId) {
		QuoddUserEntitlementBean entitlementbean = null;
		try {
			entitlementbean = new QuoddUserEntitlementBean();
			entitlementbean.setMarketMakerEntitlementFlag(false);
			entitlementbean.setOptionEntitlementFlag(false);
			entitlementbean.setDowJonesNewsFlag(false);
			entitlementbean.setBlockTradeEntitlementFlag(false);
			entitlementbean.setVWAPEntitlementFlag(false);
			entitlementbean.setUserID(userId);
			getExchangeEntitlements(userId, entitlementbean);
		} catch (Exception e) {
			log.error("Unable to retrieve entitlements data for user " + userId, e);
		}
		return entitlementbean;
	}

	private void getExchangeEntitlements(Integer userID, QuoddUserEntitlementBean entitlementbean) {
		try {

			String username = UserIdCache.getInstance().getUserNameFromQss4Id(String.valueOf(userID));
			log.info(
					"EntitlementManager.getExchangeEntilements() : fetching exchange entitlement for user with userId : "
							+ userID + " and userName : " + username);
			UserBO user = new UserBO();
			user.setUserID(userID);
			user.setUserName(username);
			user.setNoAuthNeeded(true); // for processing entitlement without entitlement
			Vector entitlementV = null;
			if (username == null || username.isEmpty())
				entitlementV = user.selectUserEntitlementsByID();
			else
				entitlementV = user.selectUserEntitlements();
			if (entitlementV == null || entitlementV.size() == 0)
				return;
			Hashtable ht = entitlementbean.getExchangeHash();
			if (ht == null) {
				ht = new Hashtable();
				entitlementbean.setExchangeHash(ht);
			}
			for (int i = 0; i < entitlementV.size(); i++) {
				Object bean = entitlementV.elementAt(i);
				if (bean instanceof QuoddUserEntitlementBean) {
					QuoddUserEntitlementBean accessLevelBean = (QuoddUserEntitlementBean) bean;
					entitlementbean.setMarketMakerEntitlementFlag(accessLevelBean.getMarketMakerEntitlementFlag());
					entitlementbean.setBlockTradeEntitlementFlag(accessLevelBean.getBlockTradeEntitlementFlag());
					entitlementbean.setDowJonesNewsFlag(accessLevelBean.getDowJonesNewsFlag());
					entitlementbean.setOptionEntitlementFlag(accessLevelBean.getOptionEntitlementFlag());
					entitlementbean.setVWAPEntitlementFlag(accessLevelBean.getVWAPEntitlementFlag());
					if (entitlementbean.getUsername() == null || entitlementbean.getUsername().isEmpty()) {
						entitlementbean.setUsername(accessLevelBean.getUsername());
					}
				} else {
					UserEntitlementBean uebean = (UserEntitlementBean) bean;
					if (uebean == null)
						continue;
					if (uebean.getExchangeID() == null || uebean.getExchangeID().length() == 0)
						continue;
					entitlementbean.setUserID(uebean.getUserID());
					Hashtable exchangeHash = (Hashtable) ht.get(uebean.getExchangeID());
					if (exchangeHash == null) {
						exchangeHash = new Hashtable();
						ht.put(uebean.getExchangeID(), exchangeHash);
					}
					Vector propV = uebean.getEntitlementVector();
					if (propV == null || propV.size() == 0)
						continue;
					for (int j = 0; j < propV.size(); j++) {
						EntitlementPropertyBean propbean = (EntitlementPropertyBean) propV.elementAt(j);
						if (propbean == null)
							continue;
						// skip exchange property code
						if (uebean.getExchangeID().equals(propbean.getPropertyCode()))
							continue;
						exchangeHash.put(propbean.getPropertyCode(), propbean.getPropertyCode());
					}
				}
			}
		} catch (Exception dne) {
			String msg = "Exception encountered while attempting to retrieve exchange entitlements for user [" + userID
					+ "]. " + dne.getMessage();
			log.error("EntitlementManager - " + msg, dne);
			entitlementbean.setDisableAllExchanges(true);
		}
	}

	public QuoddUserEntitlementBean reloadUserEntitlements(Integer userID) {
		QuoddUserEntitlementBean bean = null;
		if (userID != null) {
			entitlementsMap.remove(userID);
			bean = get(userID);
		}
		return bean;
	}

	public boolean isExchangeEntitled(Integer userID, String ex) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isExhangeEntitled(ex));
	}

	public boolean isLastOnlyEntitled(Integer userID, String ex) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isLastOnlyEntitled(ex));
	}

	public boolean isRealTimeEntitled(Integer userID, String ex) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isRealTimeEntitled(ex));
	}

	public boolean isDelayedEntitled(Integer userID, String ex) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isDelayedEntitled(ex));
	}

	public boolean isPinkSheetEntitled(Integer userID) {
		pinkEntitlement = isExchangeEntitled(userID, "1010");
		if (pinkEntitlement && isLastOnlyEntitled(userID, "1010"))
			pinkEntitlement = false;
		if (isOTCEntitled(userID) && isLastOnlyEntitled(userID, "10"))
			pinkEntitlement = false;
		return pinkEntitlement;
	}

	public boolean isNonPinkQuotesExchangeEntitled(Integer userID) {
		nonPinkQuotesEntitlement = isExchangeEntitled(userID, "9");
		if (nonPinkQuotesEntitlement && isLastOnlyEntitled(userID, "9"))
			nonPinkQuotesEntitlement = false;
		if (isOTCEntitled(userID) && isLastOnlyEntitled(userID, "10"))
			nonPinkQuotesEntitlement = false;
		return nonPinkQuotesEntitlement;
	}

	public boolean isOTCEntitled(Integer userID) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isExhangeEntitled("10"));
	}

	public boolean isOptionEntitled(int userID) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isOptionEntitled());
	}

	public boolean isNasdaqEntitled(int userID) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		boolean entitled = false;
		if (entitlementbean != null) {
			if (entitlementbean.isExhangeEntitled("tsnasdaqbasic_access")
					|| entitlementbean.isExhangeEntitled("tsqnb_access"))
				entitled = true;
		}
		return (entitlementbean == null ? false : entitled);
	}

	public boolean isEquityMontageEntitled(int userID) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isRealTimeEntitled(ENTITLEMENT_EQUITY_MONTAGE));
	}

	public boolean isOptionMontageEntitled(int userID) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isRealTimeEntitled(ENTITLEMENT_OPTION_MONTAGE));
	}

	public boolean isCusipEntitled(int userID) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isExhangeEntitled(ENTITLEMENT_CUSIP));
	}

	public boolean isOptionSymbol(String s) {
		if (s.startsWith(OPTIONS_SYMBOL_PREFIX) && (!isMontageSymbol(s)))
			return (true);
		return (false);
	}

	public boolean isOptionMontageSymbol(String s) {
		if (s.startsWith(OPTIONS_SYMBOL_PREFIX) && isMontageSymbol(s))
			return (true);
		return (false);
	}

	public boolean isFutureSymbol(String s) {
		if (s.startsWith(FUTURES_SYMBOL_PREFIX))
			return (true);
		return (false);
	}

	private static boolean isMontageSymbol(String ticker) {
		ticker = ticker.trim();
		if (ticker == null)
			return false;
		if (ticker.length() < 3)
			return false;
		if (!("" + ticker.charAt(ticker.length() - 2)).equalsIgnoreCase(MONTAGE_IDENTIFIER)) {
			return false;
		}
		return true;
	}

	public boolean isFutureEntitled(int userID, String ticker) {
		boolean entitlement = false;
		SymbolUpdaterCache cache = SymbolUpdaterCache.getInstance();
		String exchange = cache.getFuturesExchange(ticker);
		if (exchange != null) {
			entitlement = isExchangeEntitled(userID, exchange);
		}
		return entitlement;
	}

	public boolean isMyTrackBasicEntitled(int userID) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isRealTimeEntitled(ENTITLEMENT_MYTRACK_BASIC));
	}

	public boolean isMyTrackSilverEntitled(int userID) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isRealTimeEntitled(ENTITLEMENT_MYTRACK_SILVER));
	}

	public boolean isMyTrackGoldEntitled(int userID) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isRealTimeEntitled(ENTITLEMENT_MYTRACK_GOLD));
	}

	public boolean isMyTrackPlatinumEntitled(int userID) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isRealTimeEntitled(ENTITLEMENT_MYTRACK_PLATINUM));
	}

	public boolean isNonProAgreementEntitled(int userID) {
		QuoddUserEntitlementBean entitlementbean = get(userID);
		return (entitlementbean == null ? false : entitlementbean.isRealTimeEntitled(ENTITLEMENT_NONPROAGREEMENT));
	}
}
