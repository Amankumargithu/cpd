package com.b4utrade.bo;

import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Vector;

import com.tacpoint.cache.CacheManager;
import com.tacpoint.dataaccess.DefaultBusinessObject;
import com.tacpoint.exception.BusinessException;
import com.tacpoint.util.Utility;

public class StockMoverBO extends DefaultBusinessObject {

	private static final int NUMBER_OF_RECORDS_RETURN = 10;
	private String mTicker;
	private String mCompanyName;
	private String mChangeValue;

	public static final String TYPE_UP = "UP";
	public static final String TYPE_DOWN = "DOWN";
	public static final String TYPE_VOL = "VOL";

	public void setTicker(String inTicker) {
		mTicker = inTicker;
	}

	public String getTicker() {
		return mTicker;
	}

	public void setCompanyName(String inCompanyName) {
		mCompanyName = inCompanyName;
	}

	public String getCompanyName() {
		return mCompanyName;
	}

	public String getFormattedCompanyName() {
		if (mCompanyName == null)
			return "N/A";
		return mCompanyName;
	}

	public void setChangeValue(String inValue) {
		mChangeValue = inValue;
	}

	public String getChangeValue() {
		return mChangeValue;
	}

	public String getFormattedChangeValue() {
		if (mChangeValue == null)
			return "N/A";
		else if (mChangeValue.equalsIgnoreCase("NA"))
			return "N/A";
		return Utility.formatPrice(mChangeValue);
	}

	public String getFormattedChangeVolume() {
		if (mChangeValue == null)
			return "N/A";
		else if (mChangeValue.equalsIgnoreCase("NA"))
			return "N/A";
		return Utility.formatVolume(mChangeValue);
	}

	public static Vector<StockMoverBO> selectStockMovers(String inMoverType) throws BusinessException {
		Hashtable h = (Hashtable) CacheManager.get("MARKET_SCANNER_CACHE");
		Vector<StockMoverBO> resultVector = new Vector<>();
		MarketScannerBO[] msArray = null;
		MarketScannerBO msBo = new MarketScannerBO();
		if (inMoverType.equals(TYPE_UP)) {
			msArray = (MarketScannerBO[]) h.get("PERCENT_CHANGE_DESC");
		} else if (inMoverType.equals(TYPE_DOWN)) {
			msArray = (MarketScannerBO[]) h.get("PERCENT_CHANGE_ASC");
		} else if (inMoverType.equals(TYPE_VOL)) {
			msArray = (MarketScannerBO[]) h.get("VOLUME_DESC");
		}
		if (msArray != null) {
			for (int i = 0; i < msArray.length; i++) {
				msBo = msArray[i];
				if (msBo.getPrice() > 2.00) {
					StockMoverBO tempBO = new StockMoverBO();
					tempBO.setTicker(msBo.getTicker());
					tempBO.setCompanyName(msBo.getCompanyName());
					if (inMoverType.equals(TYPE_VOL)) {
						DecimalFormat df = new DecimalFormat("############");
						tempBO.setChangeValue(df.format(msBo.getVolume()));
					} else {
						tempBO.setChangeValue(String.valueOf(msBo.getPercentChange()));
					}
					resultVector.addElement(tempBO);
				}
				if (resultVector.size() >= NUMBER_OF_RECORDS_RETURN)
					break;
			}
		}
		return resultVector;
	}
}
