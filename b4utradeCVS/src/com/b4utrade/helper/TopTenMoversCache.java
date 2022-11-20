package com.b4utrade.helper;

import java.util.Vector;

import com.b4utrade.bo.StockMoverBO;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

/**
 * This Singleton class allows users to retrieve the latest news story (stock
 * specific) from a continuously updated news cache.
 */
public class TopTenMoversCache implements Runnable {

	private static Vector<StockMoverBO> upMoversVector = null;
	private static Vector<StockMoverBO> downMoversVector = null;
	private static Vector<StockMoverBO> volumeMoversVector = null;
	private static TopTenMoversCache instance = null;
	private static Thread thread = null;
	private static boolean doRun = false;
	private static long delay = 0L;

	private TopTenMoversCache() {
		try {
			delay = Integer.parseInt(Environment.get("TOP_TEN_MOVER_CACHE_DELAY"));
		} catch (NumberFormatException nfe) {
			delay = 80_000L;
		}
		try {
			upMoversVector = StockMoverBO.selectStockMovers(StockMoverBO.TYPE_UP);
			downMoversVector = StockMoverBO.selectStockMovers(StockMoverBO.TYPE_DOWN);
			volumeMoversVector = StockMoverBO.selectStockMovers(StockMoverBO.TYPE_VOL);
		} catch (Exception e) {
			upMoversVector = new Vector<>();
			downMoversVector = new Vector<>();
			volumeMoversVector = new Vector<>();
		}
		doRun = true;
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * Perform any initialization tasks.
	 */
	public static synchronized void init() {
		if (instance == null)
			instance = new TopTenMoversCache();
	}

	@Override
	public void run() {
		while (doRun) {
			try {
				upMoversVector = StockMoverBO.selectStockMovers(StockMoverBO.TYPE_UP);
				downMoversVector = StockMoverBO.selectStockMovers(StockMoverBO.TYPE_DOWN);
				volumeMoversVector = StockMoverBO.selectStockMovers(StockMoverBO.TYPE_VOL);
				if (upMoversVector == null) {
					upMoversVector = new Vector<>();
				}
				if (downMoversVector == null) {
					downMoversVector = new Vector<>();
				}
				if (volumeMoversVector == null) {
					volumeMoversVector = new Vector<>();
				}
			} catch (Exception e) {
				Logger.log("TopTenMoversCache.run() - Error encountered " + "while selecting latest movers.", e);
				upMoversVector = new Vector<>();
				downMoversVector = new Vector<>();
				volumeMoversVector = new Vector<>();
			}
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ie) {
			}
		}
	}

	public static Vector<StockMoverBO> getUpMovers() {
		return upMoversVector;
	}

	public static Vector<StockMoverBO> getDownMovers() {
		return downMoversVector;
	}

	public static Vector<StockMoverBO> getVolumeMovers() {
		return volumeMoversVector;
	}

	/**
	 * Perform any necessary cleanup.
	 */
	public void finalize() {
		doRun = false;
		thread = null;
	}

}
