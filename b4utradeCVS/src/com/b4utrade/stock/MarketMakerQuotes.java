/** MarketMakerQuotes.java
 * Copyright: Tacpoint Technologies, Inc. (c) 2000.
 * All rights reserved.
 * @author Kim Gentes
 * @author kimg@tacpoint.com
 * @version 1.0
 * Date created:  4/30/2000
 */

package com.b4utrade.stock;

import java.util.*;
import com.tacpoint.exception.BusinessException;
import com.tacpoint.util.*;
import com.b4utrade.stockutil.*;
import com.b4utrade.util.*;

/**
 * MarketMakerQuotes class The Quotes class will have the be managing all of the
 * individual stock instances. This is a singleton class so the servlets can
 * have easy access to this class at anytime.
 */
public final class MarketMakerQuotes implements DefaultQuotes {
	private boolean mDebug = true;

	private static MarketMakerQuotes mMMQuotes = new MarketMakerQuotes();

	private static Hashtable mMMStocks = new Hashtable(); // MarketMakerStockTopLists
															// objects

	private int mMaxTopBidsAsks = 10;

	private boolean mLogger = false;

	// Constructor
	private MarketMakerQuotes() {
		try {
			Logger.init();
			mLogger = true;

			Environment.init();
			try {
				String vMaxBids = Environment
						.get(B4UConstants.NASDAQMM2_MAX_TOP_BIDS_ASKS);
				if (vMaxBids != null && vMaxBids.length() > 0)
					mMaxTopBidsAsks = Integer.parseInt(vMaxBids);
			} catch (Exception e) {
			}
		} catch (Exception e) {
		}
	}

	public static MarketMakerQuotes getInstance() {
		return mMMQuotes;
	}

	public DefaultStockObject getStockQuote(String aTicker) {

		if (mMMStocks.containsKey(aTicker)) {
			return ((MarketMakerOrderManager) mMMStocks.get(aTicker))
					.getMarketMakerStockTopList();
		}

		MarketMakerStockTopLists list = new MarketMakerStockTopLists(
				mMaxTopBidsAsks);
		list.setTicker(aTicker);
		return list;

	}

	public void setStockQuote(DefaultStockObject aMMStock) {
		
		MarketMakerActivity vStock = (MarketMakerActivity) aMMStock;
		
		if (mMMStocks.containsKey(vStock.mTicker)) {
			// update stock
			((MarketMakerOrderManager)mMMStocks.get(vStock.mTicker)).updateStock((MarketMakerActivity)aMMStock);
			
		} else {
			MarketMakerOrderManager mmom = new MarketMakerOrderManager(mMaxTopBidsAsks);
			mmom.updateStock(vStock);
			// add new stock into hash table
			mMMStocks.put(vStock.mTicker, mmom);
		}
	}

	public Enumeration getEnumeration() {
		return mMMStocks.elements();
	}

}
