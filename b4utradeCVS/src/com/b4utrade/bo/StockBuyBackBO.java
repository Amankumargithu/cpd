
package com.b4utrade.bo;

import com.tacpoint.dataaccess.DefaultBusinessObject;
import com.tacpoint.util.Utility;

public class StockBuyBackBO extends DefaultBusinessObject {

	private String mTicker;
	private String mCompanyName;
	private String mBuybackAnnounceDate;
	private String mPriceAtBuyBack;
	private String mQuantity;
	private String mPercentBuyback;
	private String mDetail;

	public StockBuyBackBO() {
		mTicker = "&nbsp;";
		mCompanyName = "&nbsp;";
		mBuybackAnnounceDate = "&nbsp;";
		mPriceAtBuyBack = "&nbsp;";
		mQuantity = "&nbsp;";
		mPercentBuyback = "&nbsp;";
		mDetail = "&nbsp;";

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
		return (getDisplayString(mCompanyName));
	}

	private String getDisplayString(String inString) {
		if (inString == null)
			return ("N/A");
		if ((inString.trim().length()) <= 15)
			return (inString.trim());

		int firstspace = inString.indexOf(" ");

		StringBuffer displayname = new StringBuffer(inString.substring(0, firstspace) + "<BR>");
		String secondPart = inString.substring(firstspace);
		if ((secondPart.trim()).length() > 15) {
			displayname.append(secondPart.substring(0, 15) + "..");
		} else
			displayname.append(secondPart);

		return (displayname.toString());
	}

	public void setBuybackAnnounceDate(String inDate) {
		mBuybackAnnounceDate = inDate;
	}

	public String getBuybackAnnounceDate() {
		if (mBuybackAnnounceDate == null) {
			return "N/A";
		}

		return mBuybackAnnounceDate;
	}

	public void setPriceAtBuyBack(String inPrice) {
		mPriceAtBuyBack = inPrice;
	}

	public String getPriceAtBuyBack() {
		if (mPriceAtBuyBack == null) {
			return "N/A";
		}
		return (Utility.formatPrice(mPriceAtBuyBack));
	}

	public void setQuantity(String inQuantity) {
		mQuantity = inQuantity;
	}

	public String getQuantity() {
		if (mQuantity == null) {
			return "N/A";
		}

		return mQuantity;
	}

	public String getDisplayQuantity() {
		return (Utility.formatVolume(mQuantity));
	}

	public void setPercentBuyback(String inBuyback) {
		mPercentBuyback = inBuyback;
	}

	public String getPercentBuyback() {
		if (mPercentBuyback == null) {
			return "N/A";
		}

		return mPercentBuyback;
	}

	public void setDetail(String inDetail) {
		mDetail = inDetail;
	}

	public String getDetail() {
		if (mDetail == null) {
			return "N/A";
		}

		return mDetail;
	}

}
