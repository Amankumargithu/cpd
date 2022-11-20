package com.b4utrade.bo;

import java.sql.Timestamp;

import com.b4utrade.bean.NewsBean;
import com.b4utrade.helper.DJNewsSearchHelper;
import com.b4utrade.helper.EdgeNewsSearchHelper;
import com.tacpoint.dataaccess.DefaultBusinessObject;
import com.tacpoint.exception.BusinessException;

// from stock up close
public class StockNewsBO extends DefaultBusinessObject {
	private String mTicker;
	private String mCompanyName;
	private String mNewsDescription;
	private String mNewsLink;
	private String mNewsDate;
	private String mNewsSource;
	private String mNewsType;
	private long djNewsID;

	public StockNewsBO() {
		mTicker = "";
		mCompanyName = "";
		mNewsDescription = "";
		mNewsLink = "";
		mNewsDate = "";
		mNewsSource = "";
		mNewsType = "";
	}

	public void setDJNewsID(long id) {
		this.djNewsID = id;
	}

	public long getDJNewsID() {
		return this.djNewsID;
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

	public void setNewsDescription(String inNewsDescription) {
		mNewsDescription = inNewsDescription;
	}

	public String getNewsDescription() {
		return mNewsDescription;
	}

	public void setNewsLink(String inNewsLink) {
		mNewsLink = inNewsLink;
	}

	public String getNewsLink() {
		return mNewsLink;
	}

	public void setNewsDate(String inNewsDate) {
		mNewsDate = inNewsDate;
	}

	public String getNewsDate() {
		if (mNewsDate != null) {
			return mNewsDate;
		}
		return "N/A";
	}

	public void setNewsSource(String inNewsSource) {
		mNewsSource = inNewsSource;
	}

	public String getNewsSource() {
		return mNewsSource;
	}

	public void setNewsType(String inType) {
		mNewsType = inType;
	}

	public String getNewsType() {
		return mNewsType;
	}

	public void selectCompanyNewsByID() throws BusinessException {
		NewsBean bean = EdgeNewsSearchHelper.getNewsByID(getID());
		if (bean != null) {
			setNewsDate(bean.getFormattedNewsDate());
			setNewsDescription(bean.getHeadline());
			setNewsLink(bean.getNewsLink());
			setNewsSource(bean.getNewsSource());
		} else
			System.out.println(new Timestamp(System.currentTimeMillis())
					+ " StockNewsBO.selectCompanyNewsByID(): Got null bean for " + getID());
	}

	public void selectDJNewsByID() throws BusinessException {
		NewsBean bean = DJNewsSearchHelper.getNewsByID(getDJNewsID());
		if (bean != null) {
			setNewsDate(bean.getFormattedNewsDate());
			setNewsDescription(bean.getHeadline());
			setNewsLink(bean.getNewsLink());
			setNewsSource(bean.getNewsSource());
		} else
			System.out.println(new Timestamp(System.currentTimeMillis())
					+ " StockNewsBO.selectCompanyNewsByID(): Got null bean for " + getDJNewsID());
	}
}
