/** 
 * StockSplitBO.java
 * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
 * @author Ming Lau
 * @author mlau@tacpoint.com
 * @version 1.0
 * Date created: 02/04/2000
 * Date modified:
 * - 02/04/2000 Initial version
 *   The Stock Split business object.
 */
package com.b4utrade.bo;

import com.tacpoint.dataaccess.DefaultBusinessObject;
import com.tacpoint.util.Utility;

public class StockSplitBO extends DefaultBusinessObject {

	private String mTicker;
	private String mCompanyName;
	private String mSplitRatio;
	private String mPriceAtSplit;
	private String mPriceAfterSplit;
	private String mSplitAnnounceDate;
	private String mSplitEffectiveDate;
	private String mCurrentDate;

	public StockSplitBO() {
		mTicker = "&nbsp;";
		mCompanyName = "&nbsp;";
		mSplitRatio = "&nbsp;";
		mPriceAtSplit = "&nbsp;";
		mPriceAfterSplit = "&nbsp;";
		mSplitEffectiveDate = "&nbsp;";
		mSplitAnnounceDate = "&nbsp;";
		mCurrentDate = "&nbsp;";

	}

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

	public String getDisplayCompanyName() {
		if (mCompanyName == null)
			return ("N/A");

		if ((mCompanyName.length()) > 20) {
			return (mCompanyName.substring(0, 20) + "..");
		}

		return mCompanyName;
	}

	public void setSplitRatio(String inRatio) {
		mSplitRatio = inRatio;
	}

	public String getStockSplitRatio() {
		return mSplitRatio;
	}

	public String getDisplaySplitRatio() {
		if (mSplitRatio == null)
			return ("N/A");

		if ((mSplitRatio.length()) > 12) {
			return (mSplitRatio.substring(0, 12) + "..");
		}

		return mSplitRatio;
	}

	public void setPriceAtSplit(String inPrice) {
		mPriceAtSplit = inPrice;
	}

	public String getPriceAtSplit() {
		if (mPriceAtSplit == null) {
			return "N/A";
		}
		return (Utility.formatPrice(mPriceAtSplit));
	}

	public void setPriceAfterSplit(String inPrice) {
		mPriceAfterSplit = inPrice;
	}

	public String getPriceAfterSplit() {
		if (mPriceAfterSplit == null) {
			return "N/A";
		}

		return (Utility.formatPrice(mPriceAfterSplit));
	}

	public void setSplitEffectiveDate(String inDate) {
		mSplitEffectiveDate = inDate;
	}

	public String getSplitEffectiveDate() {
		if (mSplitEffectiveDate == null) {
			return "N/A";
		}

		return mSplitEffectiveDate;
	}

	public void setSplitAnnounceDate(String inDate) {

		mSplitAnnounceDate = inDate;
	}

	public String getSplitAnnounceDate() {
		if (mSplitAnnounceDate == null) {
			return "N/A";
		}

		return mSplitAnnounceDate;
	}

	public void setCurrentDate(String inDate) {
		mCurrentDate = inDate;
	}

	public String getCurrentDate() {
		return mCurrentDate;
	}

}
