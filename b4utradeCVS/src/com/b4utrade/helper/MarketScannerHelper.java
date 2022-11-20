package com.b4utrade.helper;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.b4utrade.bo.MarketScannerBO;
import com.b4utrade.bo.PriceCodeBO;
import com.tacpoint.cache.CacheManager;
import com.tacpoint.common.DefaultObject;

public class MarketScannerHelper extends DefaultObject {
	private static final HashMap<String, PriceCodeBO> priceCodeMap = new HashMap<>();
	static Log log = LogFactory.getLog(MarketScannerHelper.class);
	static {
		priceCodeMap.put("1", new PriceCodeBO(1, 0d, 9999d));
		priceCodeMap.put("2", new PriceCodeBO(2, 0d, 5d));
		priceCodeMap.put("3", new PriceCodeBO(3, 5d, 15d));
		priceCodeMap.put("4", new PriceCodeBO(4, 15d, 25d));
		priceCodeMap.put("5", new PriceCodeBO(5, 25d, 50d));
		priceCodeMap.put("6", new PriceCodeBO(6, 50d, 100d));
		priceCodeMap.put("7", new PriceCodeBO(7, 100d, 9999d));
		priceCodeMap.put("8", new PriceCodeBO(8, 25d, 9999d));
	}
	private String mIndustry;
	private String mPriceRange;
	private String mMktCaps;
	private String mTopTen;
	private String mExchange;
	private int mktCapsKey;
	private int exchangeKey;
	private double highPriceKey;
	private double lowPriceKey;

	public MarketScannerHelper() {
	}

	/**
	 * Set the default values for variables
	 *
	 */
	public void setDefaultData() {
		mIndustry = "All Industries";
		mPriceRange = "PRICE_0";
		mMktCaps = "MKTCAP_0";
		mTopTen = "TOPTEN_1";
		mExchange = "EXCHANGE_0";
	}

	/**
	 * sets the Industry Id.
	 *
	 * @param String inIndustry
	 */
	public void setIndustry(String inIndustry) {
		mIndustry = inIndustry;
	}

	/**
	 * gets the Industry Id.
	 *
	 * @return String mIndustry
	 */
	public String getIndustry() {
		return mIndustry;
	}

	/**
	 * sets the Exchage ID
	 *
	 * @param String inExchange
	 */
	public void setExchange(String inExchange) {
		mExchange = inExchange;
	}

	/**
	 * gets the Exchage Id.
	 *
	 * @return String mExchage
	 */
	public String getExchange() {
		return mExchange;
	}

	/**
	 * sets the Price Range
	 *
	 * @param String inPriceRange
	 */
	public void setPriceRange(String inPriceRange) {
		mPriceRange = inPriceRange;
	}

	/**
	 * gets the Price Range.
	 *
	 * @return String mPriceRange
	 */
	public String getPriceRange() {
		return mPriceRange;
	}

	/**
	 * sets the Top ten critiria
	 *
	 * @param String inTopTen
	 */

	public void setTopTen(String inTopTen) {
		mTopTen = inTopTen;
	}

	/**
	 * gets the top ten Criteria
	 *
	 * @return String mTopTen
	 */
	public String getTopTen() {
		return mTopTen;
	}

	/**
	 * sets the Market Cap ID
	 *
	 * @param String inMarketCapID
	 */
	public void setMarketCap(String inMarketCapID) {
		mMktCaps = inMarketCapID;
	}

	/**
	 * gets the Market Cap ID
	 *
	 * @return String mMktCaps
	 */
	public String getMarketCap() {
		return mMktCaps;
	}

	/**
	 * select the first 10 stocks that marches all the selected market scanner
	 * critiria.
	 *
	 * @param Vector tickerNames, Vector companyNames
	 */
	public void selectStocks(Vector tickerNames, Vector companyNames) {
		log.info("Requested " + mTopTen);
		Hashtable h = (Hashtable) CacheManager.get("MARKET_SCANNER_CACHE");

		if (h != null) {
			// get the correct array from cache
			MarketScannerBO[] msArray = null;

			if (mTopTen.equals("TOPTEN_1")) {/* || mTopTen.equals("TOPTEN_2")) { */
				msArray = (MarketScannerBO[]) h.get("VOLUME_DESC");
			} else if (mTopTen.equals("TOPTEN_3")) {
				msArray = (MarketScannerBO[]) h.get("DOLLAR_CHANGE_DESC");
			} else if (mTopTen.equals("TOPTEN_4")) {
				msArray = (MarketScannerBO[]) h.get("DOLLAR_CHANGE_ASC");
			} else if (mTopTen.equals("TOPTEN_5")) {
				msArray = (MarketScannerBO[]) h.get("PERCENT_CHANGE_DESC");
			} else if (mTopTen.equals("TOPTEN_6")) {
				msArray = (MarketScannerBO[]) h.get("PERCENT_CHANGE_ASC");
			} else if (mTopTen.equals("TOPTEN_8")) {
				msArray = (MarketScannerBO[]) h.get("LAST_PRICE_DESC");
			} else if (mTopTen.equals("TOPTEN_9")) {
				msArray = (MarketScannerBO[]) h.get("LAST_PRICE_ASC");
			}
			// iterator to select out the first 10 stocks to meet the criterias
			int count = 0;
			createKeys();
			MarketScannerBO msBo = new MarketScannerBO();

			if (msArray != null) {
				for (int i = 0; i < msArray.length; i++) {
					msBo = msArray[i];
					if (validStock(msBo)) {
						tickerNames.addElement(msBo.getTicker());
						companyNames.addElement(msBo.getCompanyName());
						count++;
					}
					if (count >= 100)
						break;
				}
			}
		}
	}

	/**
	 * Check the specific stock from the array is matched all the Keys(critiria)
	 *
	 * @param MarketScannerBO
	 * @return boolen if the stock matched every thing then true, else false
	 */
	private boolean validStock(MarketScannerBO msBO) {
		boolean industryCheck = false;
		if (mIndustry != null) {
			if (mIndustry.equals("All Industries")) {
				industryCheck = true;
			}
			String stockSector = msBO.getSector();
			if (stockSector != null) {
				if (stockSector.equals(mIndustry)) {
					industryCheck = true;
				}
			}
			if (!industryCheck)
				return false;
		}

		// Check Market Caps
		if (mktCapsKey > 0) {
			if (msBO.getMarketCapID() != mktCapsKey)
				return false;
		}
		// Check exchange
		if (exchangeKey > 0) {
			if (msBO.getExchange() != exchangeKey)
				return false;
		} else {
			if (msBO.getExchange() == 5) {// OTC exchange check
				return false;
			}
		}
		// Check price range
		if (!mPriceRange.equals("PRICE_0")) {
			if ((msBO.getPrice() > highPriceKey) || (msBO.getPrice() < lowPriceKey))
				return false;
		}
		if (mTopTen.equals("TOPTEN_1")) {
			if (msBO.getVolume() < 0)
				return false;
		}
		// Check fifty-two week High
		if (mTopTen.equals("TOPTEN_8")) {
			if (!msBO.getFiftyTwoWeekHi())
				return false;
		}
		// Check fifty-two week Low
		if (mTopTen.equals("TOPTEN_9")) {
			if (!msBO.getFiftyTwoWeekLow())
				return false;
		}
		if ((mTopTen.equals("TOPTEN_5")) || (mTopTen.equals("TOPTEN_6")) || (mTopTen.equals("TOPTEN_8"))) {
			if ((msBO.getPrice() <= 2.00) && (!mPriceRange.equals("PRICE_2")))
				return false;
		}
		return true;
	}

	/**
	 * base on the criaria the user selected, create the keys wgich are easier to
	 * compare.
	 *
	 */

	private void createKeys() {
		// set priceKeys
		PriceCodeBO pcBO = priceCodeMap.get(mPriceRange.substring(mPriceRange.indexOf('_') + 1));
		if (pcBO != null) {
			highPriceKey = pcBO.getHigh();
			lowPriceKey = pcBO.getLow();
		}
		// set exchange Keys
		try {
			exchangeKey = Integer.parseInt(mExchange.substring(mExchange.indexOf('_') + 1));
		} catch (NumberFormatException ne) {
			exchangeKey = 0;
		}
		// set MarketCaps Keys
		try {
			mktCapsKey = Integer.parseInt(mMktCaps.substring(mMktCaps.indexOf('_') + 1));
		} catch (NumberFormatException ne) {
			mktCapsKey = 0;
		}
	}
}
