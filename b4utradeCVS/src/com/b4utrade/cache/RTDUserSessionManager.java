package com.b4utrade.cache;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.b4utrade.bean.UserStreamerManagementBean;

public class RTDUserSessionManager {

	private static final Log log = LogFactory.getLog(RTDUserSessionManager.class);
	private static volatile RTDUserSessionManager instance = null;
	private HashMap<Integer, String> userSessionMap = new HashMap<>();
	private static final TickerListManager manager = TickerListManager.getInstance();

	private RTDUserSessionManager() {

	}

	public static RTDUserSessionManager getInstance() {
		if (instance == null) {
			synchronized (RTDUserSessionManager.class) {
				if (instance == null) {
					instance = new RTDUserSessionManager();
				}
			}
		}
		return instance;
	}

	public void storeUserSession(int userId, String session) {
		userSessionMap.put(userId, session);
		log.info("userSessionMap: " + userSessionMap);
	}

	public void deleteUserSession(int userId) {
		userSessionMap.remove(userId);
		log.info("userSessionMap: " + userSessionMap);
		UserStreamerManagementBean bean = manager.getUserStreamingBean(userId);
		if (bean != null) {
			bean.clearAllList();
			bean.setLoggedIn(false);
			log.info("unsubscribed all lists for userId: " + userId);
		}
	}
}
