package com.b4utrade.memorydb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import com.b4utrade.bean.StockProfileBean;
import com.tacpoint.util.Logger;

/**
 * Quotes class The Quotes class will have the be managing all of the individual
 * stock instances. This is a singleton class so the servlets can have easy
 * access to this class at anytime.
 */
public final class FutureExchangeMappingMemoryDB {

	private static FutureExchangeMappingMemoryDB mFEMappingDB = new FutureExchangeMappingMemoryDB();
	private Vector<StockProfileBean> futureExchangeList = new Vector<>();

	// Constructor
	private FutureExchangeMappingMemoryDB() {
		try {
			Logger.init();
			loadingFutureAndExchangeCodeMap();
		} catch (Exception e) {
		}
	}

	/**
	 * getInstance return the handle to the OptionQuoteServer
	 *
	 * @return OptionQuoteServer
	 */
	public static FutureExchangeMappingMemoryDB getInstance() {
		if (mFEMappingDB == null)
			mFEMappingDB = new FutureExchangeMappingMemoryDB();
		return mFEMappingDB;
	}

	public Vector<StockProfileBean> getFutureExchangeList() {
		return this.futureExchangeList;
	}

	private void loadingFutureAndExchangeCodeMap() {
		String indata = null;
		// get future and commodity list
		try (BufferedReader brdr = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("/quodd.future.exchange.properties")));) {
			StockProfileBean spbean = null;
			StringTokenizer str = null;
			String spotSymbol = null;
			Logger.log("Loading future room symbol: ");
			while ((indata = brdr.readLine()) != null) {
				str = new StringTokenizer(indata, "||");
				if (str.countTokens() >= 5) {
					spbean = new StockProfileBean();
					spbean.setSecurityDesc(str.nextToken());
					spbean.setExchange(str.nextToken());
					spbean.setUnderlyingStockTicker(str.nextToken().trim());
					spbean.setTopSecurityFlag("Y".equals(str.nextToken()));
					spotSymbol = str.nextToken(); // spot symbol
					spbean.setTickerInDB(spotSymbol);
					Logger.log("root Symbol=" + spbean.getUnderlyingStockTicker() + " desc=" + spbean.getSecurityDesc()
							+ " top Security Flag=" + spbean.isTopSecurityFlag());
					this.futureExchangeList.addElement(spbean);
				}
			}
		} catch (Exception e) {
			Logger.trace(Logger.SEVERE,
					"Unable to read the quodd.future.properties.  Make sure it exists in your CLASSPATH: ", e);
		}
	}
}
