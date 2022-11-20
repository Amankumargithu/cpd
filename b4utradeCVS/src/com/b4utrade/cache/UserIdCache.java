package com.b4utrade.cache;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.csvreader.CsvReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

/*
 * Caching UserId and userName from Qss4 api
 */
public class UserIdCache implements Runnable {

	private static UserIdCache instance = null;

	private static Thread thread = null;

	private static long refreshCacheDelay = 0L;

	private static final Gson gson = new Gson();

	private static final String qss4_USER_ID = "user_id";
	private static final String qss4_USER_NAME = "user_name";
	private static final String qss4_STATUS = "status";

	private ConcurrentHashMap<String, String> qss4NameIdMap = new ConcurrentHashMap<>();

	private HashMap<String, String> qss4QsDbCodeProductIdMappingMap = new HashMap<>();
	private HashMap<String, List<String>> oracleQsDbCodeDdfMappingMap = new HashMap<>();

	private HashMap<String, String> qss4ServiceIdUpStreamIdMap = new HashMap<>();
	private HashMap<String, List<String>> qss4ProductIdComStockMappingMap = new HashMap<>();

	private boolean isQss4FailSafeInitiated = false;

	private UserIdCache() {
		try {
			refreshCacheDelay = Integer.parseInt(Environment.get("USERIDLIST_REFRESH_CACHE_DELAY"));
		} catch (NumberFormatException nfe) {
			refreshCacheDelay = 900000L;
		}
		thread = new Thread(this);
		thread.start();
	}

	public static UserIdCache getInstance() {
		if (instance == null) {
			init();
		}
		return instance;
	}

	public static synchronized void init() {
		if (instance == null)
			instance = new UserIdCache();
		Logger.log("UserIdCache: init");
		instance.fetchServiceIdEntitlementQss4();
		instance.processComStockMappings();
	}

	public HashMap<String, String> getQss4ServiceIdUpStreamIdMapp() {
		return this.qss4ServiceIdUpStreamIdMap;
	}

	public HashMap<String, List<String>> getQss4ProductIdComStockMappings() {
		return this.qss4ProductIdComStockMappingMap;
	}

	@Override
	public void run() {
		Logger.log("UserIdCache: Going to populate the cache");
		try {
			do {
				fetchQss4Users();
				Logger.log("UserIdCache : Caching is completed , Qss4DataMap Size :  " + qss4NameIdMap.size());
				try {
					Thread.sleep(refreshCacheDelay);
				} catch (InterruptedException ie) {
					Logger.log("Error While making thread sleep Refreshing userId cache.", ie);
				}
			} while (true);
		} catch (Exception e) {
			Logger.log("UserIdCache: - Unable to populate cache.", e);
		}
	}

	private void processComStockMappings() {
		Logger.log("UserIdCache: Going to populate the productId Comstock mapping map.");
		fetchProductIdQsDbCode();
		readQsDbCodeDdfCodeFile();

		qss4ProductIdComStockMappingMap.put(null, // by default always null
				Arrays.asList("0Bi", "11c", "11b", "11a", "10v", "0Az", "0Av", "07b", "07c", "07d", "18w", "10e", "07h",
						"10f", "10g", "11s", "11r", "07g", "07i", "18y", "10h", "07k", "07q", "07m", "10i", "10j",
						"11q", "07o", "07l", "07r", "1B", "08r", "11h", "NYBOT", "apnews_access", "OStreamer_access",
						"entitlement_property", "Ownership_access", "Bberry_access", "worden_access", "html5_access"));
		qss4ProductIdComStockMappingMap.put("correct_default", Arrays.asList("0Bz")); // default entitled

		if (oracleQsDbCodeDdfMappingMap.isEmpty() || qss4QsDbCodeProductIdMappingMap.isEmpty()) {
			Logger.log("UserIdCache: backend keys,ddf codes and products id Maps are empty ,Cant process Mapping");
		} else {
			qss4QsDbCodeProductIdMappingMap.entrySet().stream().forEach(entrySet -> {
				String qss4DbCode = entrySet.getKey();
				if (oracleQsDbCodeDdfMappingMap.containsKey(qss4DbCode)) {
					qss4ProductIdComStockMappingMap.put(entrySet.getValue(),
							oracleQsDbCodeDdfMappingMap.get(qss4DbCode));
				}
			});
		}
		Logger.log("UserIdCache: oracle  QSDbCode productId map populated successfully. Size : "
				+ qss4ProductIdComStockMappingMap.size());
		Logger.log("UserIdCache: oracle  QSDbCode productId map : " + qss4ProductIdComStockMappingMap);
	}

	private void readQsDbCodeDdfCodeFile() {
		Logger.log("UserIdCache: Going to populate the oracle QSDbCode productId map.");
		CsvReader reader = null;
		try {
			reader = new CsvReader(Environment.get("COMSTOCK_MAPPING_PATH"));
			reader.readHeaders();

			while (reader.readRecord()) {
				String qsDbCode = reader.get("db_name");
				String ddfCodes = reader.get("ddf");

				if (qsDbCode == null || ddfCodes == null)
					return;
				if (!ddfCodes.contains(",")) {
					oracleQsDbCodeDdfMappingMap.put(qsDbCode, Arrays.asList(ddfCodes));
				} else {
					String[] ddfCodeArr = ddfCodes.split(",");
					List<String> list = Arrays.asList(ddfCodeArr).stream().map(ddf -> ddf.trim())
							.collect(Collectors.toList());
					oracleQsDbCodeDdfMappingMap.put(qsDbCode, list);
				}
			}
			Logger.log("UserIdCache: oracle  QSDbCode productId map populated successfully. Size : "
					+ oracleQsDbCodeDdfMappingMap.size());
		} catch (Exception e) {
			Logger.log("UserIdCache: Exception While populating the oracle qsDbCodeDdf map.", e);
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	private void fetchProductIdQsDbCode() {
		Logger.log("UserIdCache: Going to populate the qss4 productId QS Db Code map.");

		String qss4ServiceListApi = Environment.get("QSS4_PRODUCTID_QSDBCODE_URL");
		try {
			URL url = new URL(qss4ServiceListApi);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Accept-Encoding", "gzip");
			urlConnection.addRequestProperty("firm_code", "eqplus");
			StringBuffer sb = new StringBuffer();
			ArrayList<HashMap<String, Object>> list = null;
			try (BufferedReader br1 = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				String line;
				while ((line = br1.readLine()) != null)
					sb.append(line);
				list = gson.fromJson(sb.toString(), new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType());
			} catch (Exception e) {
				Logger.log(
						"UserIdCache: Error while reading the input stream to populate the productId QS Db Code map.",
						e);
			}
			urlConnection.disconnect();
			if (list != null) {
				list.stream().forEach(map -> {
					if (map.get("product_id") == null || map.get("qs_db_code") == null)
						return;
					int productId = ((Double) map.get("product_id")).intValue();
					String qsDbCode = ((String) map.get("qs_db_code")).trim();

					if (productId < 1000) {
						qss4QsDbCodeProductIdMappingMap.put(qsDbCode, String.valueOf(productId));
					}
				});
				Logger.log("UserIdCache: qss4 productId QS Db Code map populated successfully. Size : "
						+ qss4QsDbCodeProductIdMappingMap.size());
			} else
				Logger.log("UserIdCache: qss4 productId QS Db Code map not populated. Size : "
						+ qss4QsDbCodeProductIdMappingMap.size());

		} catch (Exception e) {
			Logger.log("UserIdCache: Error while populating the qss4 productId QS Db Code map.", e);
		}

	}

	private void fetchServiceIdEntitlementQss4() {
		Logger.log("UserIdCache: Going to populate the qss4 service id entitlement map.");
		String qss4ServiceListApi = Environment.get("QSS4_SERVICE_LIST_URL");
		try {
			URL url = new URL(qss4ServiceListApi);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Accept-Encoding", "gzip");
			urlConnection.addRequestProperty("firm_code", "eqplus");
			StringBuffer sb = new StringBuffer();
			ArrayList<HashMap<String, Object>> list = null;
			try (BufferedReader br1 = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				String line;
				while ((line = br1.readLine()) != null)
					sb.append(line);
				list = gson.fromJson(sb.toString(), new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType());
			} catch (Exception e) {
				Logger.log(
						"UserIdCache: Error while reading the input stream to populate the qss4 service id entitlement map.",
						e);
			}
			if (list != null) {
				list.stream().forEach(map -> {
					if (map.get("qs_upstream_id") == null || map.get("service_id") == null)
						return;
					String serviceId = String.valueOf(((Double) map.get("service_id")).intValue());
					qss4ServiceIdUpStreamIdMap.put(serviceId, (String) map.get("qs_upstream_id"));
				});
			}
			Logger.log("UserIdCache: qss4 service id entitlement map populated successfully. Size : "
					+ qss4ServiceIdUpStreamIdMap.size());
			urlConnection.disconnect();
		} catch (Exception e) {
			Logger.log("UserIdCache: Error while populating the qss4 service id entitlement map.", e);
		}

	}

	private void fetchQss4Users() {
		Logger.log("UserIdCache: Going to fetch the users from qss4");
		String qss4UserListApi = Environment.get("QSS4_USERS_LIST_URL");
		try {
			URL url = new URL(qss4UserListApi);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Accept-Encoding", "gzip");
			urlConnection.addRequestProperty("firm_code", "eqplus");
			StringBuffer sb = new StringBuffer();
			ArrayList<HashMap<String, Object>> list = null;
			try (BufferedReader br1 = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				String line;
				while ((line = br1.readLine()) != null)
					sb.append(line);
				list = gson.fromJson(sb.toString(), new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType());
			} catch (Exception e) {
				Logger.log("UserIdCache: Error while reading the input Stream of qss4UserApi");
				throw new Exception(e);
			}
			urlConnection.disconnect();
			if (list != null) {
				list.stream().forEach(map -> {
					int status = ((Double) map.get(qss4_STATUS)).intValue();
					if (status == 4) {
						if (map.get(qss4_USER_ID) != null && map.get(qss4_USER_NAME) != null)
							qss4NameIdMap.put((String) map.get(qss4_USER_NAME),
									String.valueOf(((Double) map.get(qss4_USER_ID)).intValue()));
					}
				});
			}
			Logger.log("UserIdCache: Populated Qss4 users map " + qss4NameIdMap.size());
			isQss4FailSafeInitiated = false;
		} catch (Exception e) {
			Logger.log("UserIdCache: Error while populating the qss4DataMap", e);
			try {
				if (!isQss4FailSafeInitiated) {
					isQss4FailSafeInitiated = true;
					Logger.log("UserIdCache: Going to retry populating the qss4 data map");
					Thread.sleep(30000);
					fetchQss4Users();
				}
			} catch (InterruptedException ie) {
				Logger.log("UserIdCache: Interrupted Exception occured");
			} catch (Exception e1) {
				Logger.log("UserIdCache: try failed to repopulate the qss4 data map" + qss4NameIdMap.size(), e1);
			}
		}
	}

	public String getUserNameFromQss4Id(String qss4Id) {
		if (qss4NameIdMap.containsValue(qss4Id))
			return qss4NameIdMap.entrySet().stream().filter(x -> x.getValue().equalsIgnoreCase(qss4Id))
					.map(map -> map.getKey()).findFirst().orElse(null);
		else
			return null;
	}

	public String getQss4IdUsingUsername(String userName) {
		if (qss4NameIdMap.containsKey(userName))
			return qss4NameIdMap.get(userName);
		else
			return null;
	}
}
