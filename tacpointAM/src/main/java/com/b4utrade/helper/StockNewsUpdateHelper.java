package com.b4utrade.helper;

import java.util.Vector;

import com.tacpoint.common.DefaultObject;

/**
 * StockNewsUpdateHelper.java Copyright: Tacpoint Technologies, Inc. (c), 1999.
 * All rights reserved.
 * 
 * @author BO Generator
 * @author mlau@tacpoint.com
 * @version 1.0 Date created: 06/07/2000 Date modified: - 06/07/2000 Initial
 *          version
 */

public class StockNewsUpdateHelper extends DefaultObject {
	private String mTicker;
	private String mDowJones;
	private int mDowJonesID;
	private String mReuters;
	private int mReutersID;
	private String mWhisper;
	private int mWhisperID;
	private String mEdgar;
	private int mEdgarID;
	private String mLastNews;
	private String mLastNewsSource;
	private long mLastNewsID;
	private String mLastNewsDate;
	private int mChatRumorID;
	private String mChatRumor;
	private String mChatRumorSource;
	private String mChatRumorDate;
	private String mLastNewsIDAsString;
	private String mChatRumorIDAsString;
	private String categoriesAsString;
	private Vector newsVector;
	private Vector tickers;
	private Vector categories;
	private boolean displayFlag = true;

	// setters

	public void setTicker(String inTicker) {
		mTicker = inTicker;
	}

	public void setCategoriesAsString(String inCategories) {
		categoriesAsString = inCategories;
	}

	public void setTickers(Vector inTickers) {
		tickers = inTickers;
	}

	public void setDisplayFlag(boolean inFlag) {
		displayFlag = inFlag;
	}

	public void setCategories(Vector inCategories) {
		categories = inCategories;
	}

	public void setDowJones(String inDowJones) {
		mDowJones = inDowJones;
	}

	public void setDowJonesID(int inDowJonesID) {
		mDowJonesID = inDowJonesID;
	}

	public void setReuters(String inReuters) {
		mReuters = inReuters;
	}

	public void setReutersID(int inReutersID) {
		mReutersID = inReutersID;
	}

	public void setWhisper(String inWhisper) {
		mWhisper = inWhisper;
	}

	public void setWhisperID(int inWhisperID) {
		mWhisperID = inWhisperID;
	}

	public void setEdgar(String inEdgar) {
		mEdgar = inEdgar;
	}

	public void setEdgarID(int inEdgarID) {
		mEdgarID = inEdgarID;
	}

	public void setLastNews(String inLastNews) {
		mLastNews = inLastNews;
	}

	public void setLastNewsSource(String inLastNewsSource) {
		mLastNewsSource = inLastNewsSource;
	}

	public void setLastNewsID(long inLastNewsID) {
		mLastNewsID = inLastNewsID;
		mLastNewsIDAsString = "" + inLastNewsID;
	}

	public void setLastNewsDate(String inLastNewsDate) {
		mLastNewsDate = inLastNewsDate;
	}

	public void setChatRumorID(int inChatRumorID) {
		mChatRumorID = inChatRumorID;
		mChatRumorIDAsString = "" + inChatRumorID;
	}

	public void setChatRumor(String inChatRumor) {
		mChatRumor = inChatRumor;
	}

	public void setChatRumorSource(String inSource) {
		mChatRumorSource = inSource;
	}

	public void setNewsVector(Vector inNewsVector) {
		newsVector = inNewsVector;
	}

	public void setAll(String inDJNews, int inDJNewsID, String inReuters, int inReutersID, String inWhisper,
			int inWhisperID, String inEdgar, int inEdgarID, String inLastNews, String inLastNewsSource,
			int inLastNewsID, String inLastNewsDate, int inChatRumorID, String inChatRumor, String inChatRumorSource,
			String inChatRumorDate) {

		mDowJones = inDJNews;
		mDowJonesID = inDJNewsID;
		mReuters = inReuters;
		mReutersID = inReutersID;
		mWhisper = inWhisper;
		mWhisperID = inWhisperID;
		mEdgar = inEdgar;
		mEdgarID = inEdgarID;
		mLastNews = inLastNews;
		mLastNewsSource = inLastNewsSource;
		mLastNewsID = inLastNewsID;
		mLastNewsDate = inLastNewsDate;
		mChatRumorID = inChatRumorID;
		mChatRumor = inChatRumor;
		mChatRumorSource = inChatRumorSource;
		mChatRumorDate = inChatRumorDate;

		mLastNewsIDAsString = "" + inLastNewsID;
		mChatRumorIDAsString = "" + inChatRumorID;

	}

	// getters

	public String getTicker() {
		return mTicker;
	}

	public Vector getTickers() {
		return tickers;
	}

	public String getCategoriesAsString() {
		return categoriesAsString;
	}

	public Vector getCategories() {
		return categories;
	}

	public boolean getDisplayFlag() {
		return displayFlag;
	}

	public String getDowJones() {
		return mDowJones;
	}

	public int getDowJonesID() {
		return mDowJonesID;
	}

	public String getReuters() {
		return mReuters;
	}

	public int getReutersID() {
		return mReutersID;
	}

	public String getWhisper() {
		return mWhisper;
	}

	public int getWhisperID() {
		return mWhisperID;
	}

	public String getEdgar() {
		return mEdgar;
	}

	public int getEdgarID() {
		return mEdgarID;
	}

	public String getLastNews() {
		return mLastNews;
	}

	public String getLastNewsSource() {
		return mLastNewsSource;
	}

	public long getLastNewsID() {
		return mLastNewsID;
	}

	public String getLastNewsDate() {
		return mLastNewsDate;
	}

	public int getChatRumorID() {
		return mChatRumorID;
	}

	public String getChatRumor() {
		return mChatRumor;
	}

	public String getChatRumorSource() {
		return mChatRumorSource;
	}

	public String getChatRumorDate() {
		return mChatRumorDate;
	}

	public String getLastNewsIDAsString() {
		return mLastNewsIDAsString;
	}

	public String getChatRumorIDAsString() {
		return mChatRumorIDAsString;
	}

	public Vector getNewsVector() {
		return newsVector;
	}

}
