package com.b4utrade.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.b4utrade.bean.UserStreamerManagementBean;
import com.tacpoint.util.Environment;

public class TickerListManager {

	private static TickerListManager instance = null;
	public static final String SUBSCRIBE = "SUBSCRIBE";
	public static final String UNSUBSCRIBE = "UNSUBSCRIBE";
	public static final String PENDING = "PENDING";
	public static final String DELAYED = "DELAYED";
	public static final String LAST_AND_DELAYED = "LAST_DELAYED";
	public static final String LAST_AND_REALTIME = "LAST_REALTIME";
	static final ConcurrentHashMap<Integer, UserStreamerManagementBean> userBean = new ConcurrentHashMap<>();
	static Log log = LogFactory.getLog(TickerListManager.class);
	static String filename = Environment.get("RTD_TICKER_FILE");

	public static TickerListManager getInstance() {
		if (instance == null) {
			synchronized (TickerListManager.class) {
				if (instance == null) {
					instance = new TickerListManager();
				}
			}
		}
		return instance;
	}

	private TickerListManager() {
		if (filename == null)
			filename = "/usr/user_logs/rtd_tickerList.log";
		File file = new File(filename);
		if (file.exists() && file.length() > 0) {
			try (FileInputStream reader = new FileInputStream(file);
					ObjectInputStream inputStream = new ObjectInputStream(reader);) {
				userBean.putAll((ConcurrentHashMap<Integer, UserStreamerManagementBean>) inputStream.readObject());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try (FileOutputStream writer = new FileOutputStream(filename);
						ObjectOutputStream stream = new ObjectOutputStream(writer);) {
					stream.writeObject(userBean);
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	public void addUserStreamingBean(UserStreamerManagementBean bean) {
		if (bean != null)
			userBean.put(bean.getUserId(), bean);
	}

	public UserStreamerManagementBean getUserStreamingBean(int userId) {
		return userBean.get(userId);
	}
}
