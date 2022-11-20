package com.b4utrade.cache;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import com.b4utrade.bo.MarketScannerBO;
import com.b4utrade.helper.DollarChangeAscComparator;
import com.b4utrade.helper.DollarChangeDescComparator;
import com.b4utrade.helper.LastPriceAscComparator;
import com.b4utrade.helper.LastPriceDescComparator;
import com.b4utrade.helper.PercentChangeAscComparator;
import com.b4utrade.helper.PercentChangeDescComparator;
import com.b4utrade.helper.VolumeDescComparator;
import com.b4utrade.rmi.IMarketData;
import com.tacpoint.cache.ICacheController;
import com.tacpoint.util.Constants;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class MarketScannerCacheController implements ICacheController {

	private final static String VOLUME_DESC = "VOLUME_DESC";
	private final static String DOLLAR_CHANGE_DESC = "DOLLAR_CHANGE_DESC";
	private final static String DOLLAR_CHANGE_ASC = "DOLLAR_CHANGE_ASC";
	private final static String PERCENT_CHANGE_DESC = "PERCENT_CHANGE_DESC";
	private final static String PERCENT_CHANGE_ASC = "PERCENT_CHANGE_ASC";
	private final static String LAST_PRICE_DESC = "LAST_PRICE_DESC";
	private final static String LAST_PRICE_ASC = "LAST_PRICE_ASC";

	/**
	 * Container for all the MarketScannerBO(s)
	 */
	private Hashtable msHash;

	/**
	 * Used to track the last run time (in milliseconds)
	 */
	private long previousRunTime = 0L;

	/**
	 * Used to determine the amount of time between cache refresh invokations
	 */
	private long delay = 0L;

	/**
	 * Default Contructor
	 */
	public MarketScannerCacheController() {
		msHash = new Hashtable();
	}

	/**
	 * Sets the delay time.
	 *
	 * @param delay long
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}

	/**
	 * Gets the delay time.
	 *
	 * @return long delay
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * Get the cache object from the controller. The result will return to the
	 * CacheManager and return back to the requested client.
	 *
	 * @return Object the cache object, could be any kind of Container object, and
	 *         the requested class needs to cast to the right type.
	 */
	public Object get() {
		return msHash;
	}

	/**
	 * Get the cache object from the controller. The result will return to the
	 * CacheManager and return back to the requested client.
	 *
	 * @return Object the cache object, could be any kind of Container object, and
	 *         the requested class needs to cast to the right type.
	 */
	public Object get(Object inKey) {
		if (msHash != null && inKey != null)
			return msHash.get(inKey);
		return null;
	}

	/**
	 * Remove the cache in the controller.
	 *
	 * @return void
	 */
	public void remove() {
		msHash = null;
	}

	/**
	 * To check if the time is up for the controller to refresh its cache.
	 *
	 * @return boolean true = it is time to run, false = it is not the time.
	 */
	public boolean isTimeToRun() {
		long timediff = System.currentTimeMillis() - previousRunTime;
		if (timediff > delay)
			return true;
		return false;
	}

	/**
	 * To run and update the cache pool in the cache controller.
	 *
	 * @return void
	 */
	public synchronized void run() {
		try {
			Vector v = null;
			IMarketData marketData = (IMarketData) Naming
					.lookup("rmi://" + Environment.get("MARKETSCANNER_CPD_RMI_IP") + "/RMIServer");
			HashMap<String, MarketScannerBO> map = marketData.getLatestMarketScannerCache();
			Collection<MarketScannerBO> col = map.values();
			if (map.size() > 0 && map != null) {
				v = new Vector();
			}
			v.addAll(col);
			if (v != null && v.size() > 0) {
				// stuff contents into arrays and sort!
				MarketScannerBO[] vdArray = new MarketScannerBO[v.size()];
				MarketScannerBO[] dcdArray = new MarketScannerBO[v.size()];
				MarketScannerBO[] pcdArray = new MarketScannerBO[v.size()];
				MarketScannerBO[] pcaArray = new MarketScannerBO[v.size()];
				v.copyInto(vdArray);
				v.copyInto(dcdArray);
				v.copyInto(pcdArray);
				v.copyInto(pcaArray);
				ArrayList<MarketScannerBO> dollarChangeAscending = new ArrayList<>();
				ArrayList<MarketScannerBO> weekHighList = new ArrayList<>();
				ArrayList<MarketScannerBO> weekLowList = new ArrayList<>();
				for (MarketScannerBO bo : vdArray) {
					try {
						Double.parseDouble(bo.getFiftyTwoWeekHighValue());
						weekHighList.add(bo);
					} catch (Exception e) {
					}
					try {
						Double.parseDouble(bo.getFiftyTwoWeekLowValue());
						if (bo.getVolume() > 0)
							weekLowList.add(bo);
					} catch (Exception e) {
					}
					if (bo.getVolume() > 0)
						dollarChangeAscending.add(bo);
				}
				MarketScannerBO[] dcaArray = new MarketScannerBO[dollarChangeAscending.size()];
				MarketScannerBO[] lpdArray = new MarketScannerBO[weekHighList.size()];
				MarketScannerBO[] lpaArray = new MarketScannerBO[weekLowList.size()];
				for (int i = 0; i < dollarChangeAscending.size(); i++)
					dcaArray[i] = dollarChangeAscending.get(i);
				for (int i = 0; i < weekHighList.size(); i++)
					lpdArray[i] = weekHighList.get(i);
				for (int i = 0; i < weekLowList.size(); i++)
					lpaArray[i] = weekLowList.get(i);

				Arrays.sort(vdArray, new VolumeDescComparator());
				Arrays.sort(dcdArray, new DollarChangeDescComparator());
				Arrays.sort(dcaArray, new DollarChangeAscComparator());
				Arrays.sort(pcdArray, new PercentChangeDescComparator());
				Arrays.sort(pcaArray, new PercentChangeAscComparator());
				Arrays.sort(lpdArray, new LastPriceDescComparator());
				Arrays.sort(lpaArray, new LastPriceAscComparator());

				msHash.put(VOLUME_DESC, vdArray);
				msHash.put(DOLLAR_CHANGE_DESC, dcdArray);
				msHash.put(DOLLAR_CHANGE_ASC, dcaArray);
				msHash.put(PERCENT_CHANGE_DESC, pcdArray);
				msHash.put(PERCENT_CHANGE_ASC, pcaArray);
				msHash.put(LAST_PRICE_DESC, lpdArray);
				msHash.put(LAST_PRICE_ASC, lpaArray);
			}
		} catch (Exception e) {
			Logger.log("MarketScannerCacheController.run - Unable to refresh cache.", e);
		}
		previousRunTime = System.currentTimeMillis();
		boolean doLog = Boolean.valueOf(Environment.get(Constants.DEBUG)).booleanValue();
		Logger.debug("MarketScannerCacheController.run - refreshing the cache.", doLog);
	}

}
