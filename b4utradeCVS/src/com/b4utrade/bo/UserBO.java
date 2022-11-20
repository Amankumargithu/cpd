package com.b4utrade.bo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.b4utrade.bean.EntitlementPropertyBean;
import com.b4utrade.bean.QuoddUserEntitlementBean;
import com.b4utrade.bean.UserEntitlementBean;
import com.b4utrade.bean.UserLoginStatusBean;
import com.b4utrade.cache.UserIdCache;
import com.b4utrade.helper.PasswordEncrypter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.dataaccess.DefaultBusinessObject;
import com.tacpoint.exception.BusinessException;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class UserBO extends DefaultBusinessObject {

	static Log log = LogFactory.getLog(UserBO.class);

	private int mUserID;
	private String mLastName;
	private String mFirstName;
	private String mUserName;
	private String mUserPassword;
	private String mEmailAddress;
//	private String mUserAccessLevel; // 0 = cancel, 1 = active, 2 = level two.
	private int mLevelIIAccessLevel; // 0 = cancel, 1 = monthly, 2 = annual.
	private int mOptionsAccessLevel; // 0 = cancel, 1 = monthly, 2 = annual.
	private int mDJNewsAccessLevel; // 0 = not DJ news, 1 = DJ news,.
	private int mBlockTradeAccessLevel; // 0 = cancel, 1 = monthly, 2 = annual.
	private int mVWAPAccessLevel; // 0 = cancel, 1 = monthly, 2 = annual.

	private boolean isNoAuthNeeded;

	private static final String ENTITLEMENT_LEVEL2 = "1349";
	private static final String ENTITLEMENT_OPTIONS = "1302";
	private static final String ENTITLEMENT_DOWJONES_NEWS = "1348";
	private static final String ENTITLEMENT_VWAP = "1327";
	private static final String ENTITLEMENT_BLOCK_TRADE = "1357";

	private static Set<String> realTimeServiceSet = null;
	private static Set<String> delayServiceSet = null;
	private static Set<String> lastOnlyServiceSet = null;
	private static Set<String> serviceSet = null;

	private static Set<String> realTimeProductSet = null;
	private static Set<String> delayProductSet = null;
	private static Set<String> lastOnlyProductSet = null;
	private static Set<String> productSet = null;

	private static final Gson gson = new Gson();
	private static final PasswordEncrypter encrypt = new PasswordEncrypter();

	private static final String nonExchangeEntitlements = "AUTHORIZED,nonpro_access,nonpro_stat_access,RT,DELAY,LBA,LAST,nonpro_stat";
	private static final Set<String> nonExchangeEntitlementsList = new HashSet<>(Arrays.asList("AUTHORIZED",
			"nonpro_access", "nonpro_stat_access", "nonpro_stat", "RT", "DELAY", "LBA", "LAST"));

	public void setUserID(int inUserID) {
		mUserID = inUserID;
	}

	public int getUserID() {
		return mUserID;
	}

	public void setLastName(String inLastName) {
		mLastName = inLastName;
	}

	public String getLastName() {
		return mLastName;
	}

	public void setFirstName(String inFirstName) {
		mFirstName = inFirstName;
	}

	public String getFirstName() {
		return mFirstName;
	}

	public void setUserName(String inUserName) {
		mUserName = inUserName;
	}

	public String getUserName() {
		return mUserName;
	}

	public void setUserPassword(String inPassword) {
		mUserPassword = inPassword;
	}

	public String getUserPassword() {
		return mUserPassword;
	}

	public void setEmailAddress(String inEmail) {
		mEmailAddress = inEmail;
	}

	public String getEmailAddress() {
		return mEmailAddress;
	}

//	public void setUserAccessLevel(String inLevel) {
//		mUserAccessLevel = inLevel;
//	}
//
//	public String getUserAccessLevel() {
//		return mUserAccessLevel;
//	}

	public void setLevelIIAccessLevel(int inLevel) {
		mLevelIIAccessLevel = inLevel;
	}

	public int getLevelIIAccessLevel() {
		return mLevelIIAccessLevel;
	}

	public void setDJNewsAccessLevel(int inLevel) {
		mDJNewsAccessLevel = inLevel;
	}

	public int getDJNewsAccessLevel() {
		return mDJNewsAccessLevel;
	}

	public void setOptionsAccessLevel(int inLevel) {
		mOptionsAccessLevel = inLevel;
	}

	public int getOptionsAccessLevel() {
		return mOptionsAccessLevel;
	}

	public void setBlockTradeAccessLevel(int inLevel) {
		mBlockTradeAccessLevel = inLevel;
	}

	public int getBlockTradeAccessLevel() {
		return mBlockTradeAccessLevel;
	}

	public void setVWAPAccessLevel(int inLevel) {
		mVWAPAccessLevel = inLevel;
	}

	public int getVWAPAccessLevel() {
		return mVWAPAccessLevel;
	}

	public boolean isNoAuthNeeded() {
		return isNoAuthNeeded;
	}

	public void setNoAuthNeeded(boolean isNoAuthNeeded) {
		this.isNoAuthNeeded = isNoAuthNeeded;
	}

	public Vector selectUserEntitlements() throws BusinessException {
		Vector resultVector = new Vector<>();
		processEntitlementsFromQss4(resultVector);
		// fail safe if qss4 logic doesnt work
		if (resultVector.isEmpty()) {
			Logger.log("UserBO :- .selectUserEntitlements() : resultVector(entitlementVector) is empty, Vector Size : "
					+ resultVector.size());
		}
		return resultVector;
	}

	public Vector selectUserEntitlementsByID() throws BusinessException {
		Vector resultVector = new Vector<>();
		processEntitlementsFromQss4(resultVector);
		// fail safe if qss4 logic doesnt work
		if (resultVector.isEmpty()) {
			Logger.log(
					"UserBO :- .selectUserEntitlementsByID() : resultVector(entitlementVector) is empty, Vector Size : "
							+ resultVector.size());
		}
		return resultVector;
	}

	private Vector processEntitlementsFromQss4(Vector entitlementVector) {
		Logger.log(
				"UserBO :- .processEntitlementsFromQss4() Going to process the entitlements from qss4. isNoAuth Flag : "
						+ isNoAuthNeeded);
		if ((!isNoAuthNeeded)) {
			if ((mUserPassword == null || mUserPassword.isEmpty())) {
				Logger.log("UserBO :- .processEntitlementsFromQss4() password is null or empty in HttpSession Obj : "
						+ mUserPassword);
				return entitlementVector;
			}
		}
		if (mUserName == null || mUserName.isEmpty()) { // to process authentication if username is null
			mUserName = UserIdCache.getInstance().getUserNameFromQss4Id(String.valueOf(mUserID));
		}

		HashMap<String, String> qss4ServiceIdUpStreamIdMap = UserIdCache.getInstance().getQss4ServiceIdUpStreamIdMapp();
		if (qss4ServiceIdUpStreamIdMap != null) {
			try {
				Map<String, Object> userEntitlementMap = authenticateUserFromQss4();
				if (userEntitlementMap != null && !userEntitlementMap.isEmpty()) {
					if ((String) userEntitlementMap.get("token") != null) {
						String servicePayload = ((String) userEntitlementMap.get("token")).split("\\.")[1];
						byte[] bytes = Base64.getUrlDecoder().decode(servicePayload);
						String payload = new String(bytes, "UTF-8");
						HashMap<String, Object> payloadMap = gson.fromJson(payload,
								new TypeToken<HashMap<String, Object>>() {
								}.getType());
						ArrayList<Double> listOfServices = (ArrayList<Double>) payloadMap.get("services");
						serviceSet = listOfServices.stream().map(i -> String.valueOf(Math.round(i)))
								.collect(Collectors.toSet());
						if (payloadMap.get("user_id") != null) {
							mUserID = ((Double) payloadMap.get("user_id")).intValue();
						}
						if (payloadMap.get("user_name") != null) {
							mUserName = (String) payloadMap.get("user_name");
						}
					} else
						Logger.log(
								"UserBO :- .processEntitlementsFromQss4() ,token is null ,not able to fetch entitlements for user with userName :"
										+ mUserName);

					realTimeServiceSet = ((ArrayList<Double>) userEntitlementMap.get("realtimeServices")).stream()
							.map(i -> String.valueOf(Math.round(i))).collect(Collectors.toSet());
					delayServiceSet = ((ArrayList<Double>) userEntitlementMap.get("delayedServices")).stream()
							.map(i -> String.valueOf(Math.round(i))).collect(Collectors.toSet());
					lastOnlyServiceSet = ((ArrayList<Double>) userEntitlementMap.get("lastOnlyServices")).stream()
							.map(i -> String.valueOf(Math.round(i))).collect(Collectors.toSet());

					productSet = ((ArrayList<Double>) userEntitlementMap.get("products")).stream()
							.map(i -> String.valueOf(Math.round(i))).collect(Collectors.toSet());
					realTimeProductSet = ((ArrayList<Double>) userEntitlementMap.get("realtimeProducts")).stream()
							.map(i -> String.valueOf(Math.round(i))).collect(Collectors.toSet());
					delayProductSet = ((ArrayList<Double>) userEntitlementMap.get("delayedProducts")).stream()
							.map(i -> String.valueOf(Math.round(i))).collect(Collectors.toSet());
					lastOnlyProductSet = ((ArrayList<Double>) userEntitlementMap.get("lastOnlyProducts")).stream()
							.map(i -> String.valueOf(Math.round(i))).collect(Collectors.toSet());

					HashMap<String, List<String>> qss4ProductIDComStockMappingMap = UserIdCache.getInstance()
							.getQss4ProductIdComStockMappings();
					HashMap<String, Set<String>> exchangeIdFeedEntitlementMap = new HashMap<>();
					// adding comstock entitlements
					qss4ProductIDComStockMappingMap.entrySet().stream().forEach(entrySet -> {
						String productId = entrySet.getKey();
						entrySet.getValue().stream().forEach(exchangeId -> {
							exchangeId = exchangeId.trim();
							try {
								if (exchangeIdFeedEntitlementMap.containsKey(exchangeId)) {
									Set<String> currentEntitlements = exchangeIdFeedEntitlementMap.get(exchangeId);
									Set<String> moreFeedEntitlement = fetchProductEntitlement(productId, exchangeId,
											true, currentEntitlements);
									exchangeIdFeedEntitlementMap.put(exchangeId, moreFeedEntitlement);
								} else {
									Set<String> feedEntitlement = fetchProductEntitlement(productId, exchangeId, false,
											null);
									exchangeIdFeedEntitlementMap.put(exchangeId, feedEntitlement);
								}
							} catch (Exception e) {
								Logger.log(
										"UserBO .processEntitlementFromQss4 : processing exchangeIdFeedEntitlementMap",
										e);
							}
						});
					});
					exchangeIdFeedEntitlementMap.entrySet().stream().forEach(exchangeFeedEntrySet -> {
						try {
							String exchangeId = exchangeFeedEntrySet.getKey();
							String entitlementString = exchangeFeedEntrySet.getValue().stream().map(Object::toString)
									.collect(Collectors.joining(","));
							UserEntitlementBean bean = new UserEntitlementBean();
							bean.setUserID(mUserID);
							bean.setExchangeID(exchangeId);
							bean.setEntitlementList(entitlementString);
							entitlementVector.add(bean);
						} catch (Exception e) {
							Logger.log("UserBO .processEntitlementFromQss4 : processing exchangeIdFeedEntitlementMap",
									e);
						}
					});

					QuoddUserEntitlementBean accessLevelBean = new QuoddUserEntitlementBean();
					qss4ServiceIdUpStreamIdMap.entrySet().stream().forEach(entrySet -> {
						try {
							UserEntitlementBean bean = new UserEntitlementBean();
							bean.setUserID(mUserID);
							String serviceId = entrySet.getKey();
							String qsUpstreamId = entrySet.getValue();

							if (qsUpstreamId.matches("[0-9]+")) { // checking if the upstreamid is int
								bean.setExchangeID(serviceId);
							} else {
								bean.setExchangeID(qsUpstreamId);
							}

							if (serviceSet.contains(serviceId)) {
								bean.setEntitlementList(addServiceEntitlementToString(serviceId));
								// loads access level flags using service ID
								if (serviceId.equalsIgnoreCase(ENTITLEMENT_LEVEL2))
									accessLevelBean.setMarketMakerEntitlementFlag(true);
								if (serviceId.equalsIgnoreCase(ENTITLEMENT_BLOCK_TRADE))
									accessLevelBean.setBlockTradeEntitlementFlag(true);
								if (serviceId.equalsIgnoreCase(ENTITLEMENT_DOWJONES_NEWS))
									accessLevelBean.setDowJonesNewsFlag(true);
								if (serviceId.equalsIgnoreCase(ENTITLEMENT_OPTIONS))
									accessLevelBean.setOptionEntitlementFlag(true);
								if (serviceId.equalsIgnoreCase(ENTITLEMENT_VWAP))
									accessLevelBean.setVWAPEntitlementFlag(true);
							}
							entitlementVector.add(bean);
						} catch (Exception e) {
							Logger.log("UserBO :- .processEntitlementsFromQss4(), Error while processing serviceId : "
									+ entrySet.getKey(), e);
						}
					});
					accessLevelBean.setUsername(mUserName);
					entitlementVector.add(accessLevelBean);
				} else
					Logger.log("UserBO :- .processEntitlementsFromQss4(), userEntitlement are null or empty.");
			} catch (Exception e) {
				Logger.log("UserBO :- .processEntitlementsFromQss4(), Error while processing entitlements to vector.",
						e);
			}
		} else
			Logger.log("UserBO :- .processEntitlementsFromQss4() serviceEntitlementMap is empty or null.");
		return entitlementVector;
	}

	private Set<String> fetchProductEntitlement(String productId, String exchangeId, boolean isAlreadyPresent,
			Set<String> currentEntitlements) {

		Set<String> entitlementSet = null;
		if (isAlreadyPresent && currentEntitlements != null && !currentEntitlements.isEmpty())
			entitlementSet = new HashSet<>(currentEntitlements);
		else
			entitlementSet = new HashSet<>(nonExchangeEntitlementsList);

		if (productId == null) {
			entitlementSet.clear();
			return entitlementSet;
		} else if (productSet.contains(productId)) {
			if (realTimeProductSet.contains(productId)) {
				entitlementSet.remove(EntitlementPropertyBean.REAL_TIME_ENTITLEMENT);
			}
			if (delayProductSet.contains(productId)) {
				entitlementSet.remove(EntitlementPropertyBean.DELAYED_ENTITLEMENT);
			}
			if (lastOnlyProductSet.contains(productId)) {
				entitlementSet.remove(EntitlementPropertyBean.LAST_ONLY_ENTITLEMENT);
			}
			if (!lastOnlyProductSet.contains(productId)
					&& (realTimeProductSet.contains(productId) || delayProductSet.contains(productId))) {
				entitlementSet.remove(EntitlementPropertyBean.LAST_BID_ASK_ENTITLEMENT);
			}
			return entitlementSet;
		} else if (productId.contains("correct_")) {
			entitlementSet = new HashSet<>(nonExchangeEntitlementsList);
//			 removing LBA entitlement from default entitlement if 556 is entitled
//			if ("0Au".equalsIgnoreCase(exchangeId) && productSet.contains("556")) {
//				entitlementSet.remove(EntitlementPropertyBean.LAST_BID_ASK_ENTITLEMENT);
//			}
			entitlementSet.remove(EntitlementPropertyBean.REAL_TIME_ENTITLEMENT);
			return entitlementSet;
		} else {
//			 adding default values of 1010 key mapping adding from QuoddSupport
			if ("507".equalsIgnoreCase(productId) && !productSet.contains(productId)) {
				entitlementSet.remove(EntitlementPropertyBean.REAL_TIME_ENTITLEMENT);
				entitlementSet.remove(EntitlementPropertyBean.LAST_ONLY_ENTITLEMENT);
				if ("0Bi".equalsIgnoreCase(exchangeId) && (!productSet.contains("511") && !productSet.contains("538")
						&& !productSet.contains("568"))) {
					entitlementSet.clear();
				}
				return entitlementSet;
			}
			if (!isAlreadyPresent || entitlementSet.size() == nonExchangeEntitlementsList.size())
				entitlementSet.clear();
			return entitlementSet;
		}
	}

	private String addServiceEntitlementToString(String serviceId) {
		Set<String> entitlementSet = new HashSet<>(nonExchangeEntitlementsList);
		int serviceIdInt = 0;
		try {
			serviceIdInt = Integer.parseInt(serviceId);
		} catch (Exception e) {
		}
		if (serviceId == null) {
			return "";
		} else if (serviceSet.contains(serviceId)) {
			if (realTimeServiceSet.contains(serviceId) || (serviceIdInt > 1300)) {
				entitlementSet.remove(EntitlementPropertyBean.REAL_TIME_ENTITLEMENT);
			}
			if (delayServiceSet.contains(serviceId)) {
				entitlementSet.remove(EntitlementPropertyBean.DELAYED_ENTITLEMENT);
			}
			if (lastOnlyServiceSet.contains(serviceId)) {
				entitlementSet.remove(EntitlementPropertyBean.LAST_ONLY_ENTITLEMENT);
			}
			if (!lastOnlyServiceSet.contains(serviceId)
					&& (realTimeServiceSet.contains(serviceId) || delayServiceSet.contains(serviceId))) {
				entitlementSet.remove(EntitlementPropertyBean.LAST_BID_ASK_ENTITLEMENT);
			}
			return entitlementSet.stream().map(Object::toString).collect(Collectors.joining(","));
		} else if (serviceId.contains("correct_")) {
			return nonExchangeEntitlements;
		} else {
			return "";
		}
	}

	public String handleLogin(UserLoginStatusBean ulsb) {
		String token = null;
		try {
			token = populateUserBean(authenticateUserFromQss4(), ulsb);
			if (mUserID == 0) {
				String userId = UserIdCache.getInstance().getQss4IdUsingUsername(mUserName);
				if (userId == null || userId.isEmpty()) {
					this.mUserID = 0;
				} else {
					this.mUserID = Integer.parseInt(userId);
				}
			}
			ulsb.setUserID(mUserID);
		} catch (Exception e) {
			log.error("UserBO.handleLogin : Error while handling login via qss4 ", e);
		}
		return token;
	}

	private Map<String, Object> authenticateUserFromQss4() {
		log.info("UserBO.authenticateUserFromQss4 : Going to do qss4 authentication of user : " + mUserName);
		String qss4Url = null;
		String basicAuth = null;
		if (isNoAuthNeeded) {
			qss4Url = Environment.get("QSS4_ENTITLEMENT_URL");
			basicAuth = Environment.get("QSS4_ENTITLEMENT_URL_BASIC_AUTH");
			log.info(
					"UserBO.authenticateUserFromQss4 : Going to fetch entitlement entitlments without auth, isNoAuth Flag : "
							+ isNoAuthNeeded);
		} else {
			qss4Url = Environment.get("QSS4_AUTHENTICATION_URL");
		}
		Map<String, Object> mapFetched = null;
		try {
			URL tokenurl = new URL(qss4Url);
			HttpURLConnection urlConnection = (HttpURLConnection) tokenurl.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("accept", "application/json");
			urlConnection.addRequestProperty("username", mUserName);
			if (isNoAuthNeeded) {
				urlConnection.addRequestProperty("Authorization", basicAuth);
			} else {
				urlConnection.addRequestProperty("password", encrypt.encrypt(mUserPassword.toCharArray()));
			}
			String jsonString;
			try (BufferedReader br1 = new BufferedReader(
					new InputStreamReader(urlConnection.getResponseCode() == 200 ? urlConnection.getInputStream()
							: urlConnection.getErrorStream()));) {
				jsonString = br1.readLine();
				if (urlConnection.getResponseCode() != 200) {
					log.error(
							"UserBO.authenticateUserFromQss4 : Error while authenticatin using qss4 auth api for user : "
									+ mUserName + " Response : " + jsonString);
				} else
					mapFetched = gson.fromJson(jsonString, Map.class);
			} catch (Exception e) {
				throw new Exception(e);
			}
			urlConnection.disconnect();
			log.info("UserBO.authenticateUserFromQss4 : Completed qss4 authentication of user : " + mUserName);
		} catch (Exception e) {
			log.error("UserBO.authenticateUserFromQss4 : Error while authentication user using qss4", e);
		}
		return mapFetched;
	}

	private String populateUserBean(Map<String, Object> qss4ResultMap, UserLoginStatusBean ulsb) {
		String token = null;
		ulsb.setUsername(mUserName);
		if (qss4ResultMap == null || qss4ResultMap.isEmpty()) {
			ulsb.setStatusLoginFailed();
		} else {
			log.info("UserBO.populateUserBean : Going to Populate userBean using qss4 auth map");
			token = (String) qss4ResultMap.get("token");

			if (qss4ResultMap.get("first_name") != null) {
				mFirstName = (String) qss4ResultMap.get("first_name");
				ulsb.setFirstName(mFirstName);
			}
			if (qss4ResultMap.get("first_name") != null) {
				mLastName = (String) qss4ResultMap.get("last_name");
				ulsb.setLastName(mLastName);
			}
			ulsb.setStatusLoginSuccess();
			try {
				if (token != null) {
					String servicePayload = ((String) qss4ResultMap.get("token")).split("\\.")[1];
					byte[] bytes = Base64.getUrlDecoder().decode(servicePayload);
					String payload = new String(bytes, "UTF-8");
					HashMap<String, Object> payloadMap = gson.fromJson(payload,
							new TypeToken<HashMap<String, Object>>() {
							}.getType());
					if (payloadMap.get("email") != null) {
						mEmailAddress = (String) payloadMap.get("email");
						ulsb.setEmailID(mEmailAddress);
					}
					if (payloadMap.get("user_id") != null) {
						mUserID = ((Double) payloadMap.get("user_id")).intValue();
						ulsb.setUserID(mUserID);
					}
				} else
					log.warn("UserBO.populateUserBean : token is null in qss4 api for user : " + mUserName);
				log.info("UserBO.populateUserBean : Populated userBean using qss4 auth map of user : " + mUserName);
			} catch (Exception e) {
				log.error("UserBO.populateUserBean : Exception while populating UserBean of User : " + mUserName);
			}
		}
		return token;
	}
}
