package com.quodd.bean;

import java.util.ArrayList;
import java.util.List;

public class NewsBean {

	private String product; // needed for sequencing and retransmission
	private String docdate; // date of publish of article, needed for re-transmission
	private String seq; // seq id
	private String msgType; // N = news, R = replace, D = delete
	private String publisher; // NEWS_TYPE in db
	private String tempPerm; // if T, news is to be deleted in 2 days
	private Long accessionNumber; // for story chaining
	private String displayDate; // NEWS_DATE in db
	private String displayTime;
	private String headline;
	private String body;
	private String newsLink;
	private String newsId;
	private String actionCode;
	private String retention;
	private String newsSource;
	private String branding;
	private String hotInd;
	private String origSource;
	private String pageCitation;
	private String gmtDate;
	private String gmtTime;
	private String miliSeconds;
	private List<List<String>> categories = new ArrayList<>();
	private List<String> expireList = new ArrayList<>();

	public String getProduct() {
		return this.product;
	}

	public String getDocdate() {
		return this.docdate;
	}

	public String getSeq() {
		return this.seq;
	}

	public String getMsgType() {
		return this.msgType;
	}

	public String getPublisher() {
		return this.publisher;
	}

	public String getTempPerm() {
		return this.tempPerm;
	}

	public Long getAccessionNumber() {
		return this.accessionNumber;
	}

	public String getDisplayDate() {
		return this.displayDate;
	}

	public String getDisplayTime() {
		return this.displayTime;
	}

	public String getHeadline() {
		return this.headline;
	}

	public String getBody() {
		return this.body;
	}

	public List<List<String>> getCategories() {
		return this.categories;
	}

	public String getNewsLink() {
		return this.newsLink;
	}

	public String getNewsId() {
		return this.newsId;
	}

	public String getActionCode() {
		return this.actionCode;
	}

	public List<String> getExpireList() {
		return this.expireList;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public void setDocdate(String docdate) {
		this.docdate = docdate;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public void setTempPerm(String tempPerm) {
		this.tempPerm = tempPerm;
	}

	public void setAccessionNumber(Long accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public void setDisplayDate(String displayDate) {
		this.displayDate = displayDate;
	}

	public void setDisplayTime(String displayTime) {
		this.displayTime = displayTime;
	}

	public void setHeadline(String headline) {
		this.headline = headline;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setCategories(List<List<String>> categories) {
		this.categories = categories;
	}

	public void setNewsLink(String newsLink) {
		this.newsLink = newsLink;
	}

	public void setNewsId(String newsId) {
		this.newsId = newsId;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public void setExpireList(List<String> expireList) {
		this.expireList = expireList;
	}

	public String getRetention() {
		return this.retention;
	}

	public void setRetention(String retention) {
		this.retention = retention;
	}

	public String getNewsSource() {
		return this.newsSource;
	}

	public String getBranding() {
		return this.branding;
	}

	public void setNewsSource(String newsSource) {
		this.newsSource = newsSource;
	}

	public void setBranding(String branding) {
		this.branding = branding;
	}

	public String getHotInd() {
		return this.hotInd;
	}

	public String getOrigSource() {
		return this.origSource;
	}

	public String getPageCitation() {
		return this.pageCitation;
	}

	public String getGmtDate() {
		return this.gmtDate;
	}

	public String getGmtTime() {
		return this.gmtTime;
	}

	public String getMiliSeconds() {
		return this.miliSeconds;
	}

	public void setHotInd(String hotInd) {
		this.hotInd = hotInd;
	}

	public void setOrigSource(String origSource) {
		this.origSource = origSource;
	}

	public void setPageCitation(String pageCitation) {
		this.pageCitation = pageCitation;
	}

	public void setGmtDate(String gmtDate) {
		this.gmtDate = gmtDate;
	}

	public void setGmtTime(String gmtTime) {
		this.gmtTime = gmtTime;
	}

	public void setMiliSeconds(String miliSeconds) {
		this.miliSeconds = miliSeconds;
	}

	@Override
	public String toString() {
		return "product : " + this.product + " docdate : " + this.docdate + " seq : " + this.seq + " msgType : "
				+ this.msgType + " publisher : " + this.publisher + " tempPerm : " + this.tempPerm
				+ " accessionNumber : " + this.accessionNumber + " displayDate : " + this.displayDate
				+ " displayTime : " + this.displayTime + " headline : " + this.headline + " body : " + this.body
				+ " newsLink : " + this.newsLink + " newsId : " + this.newsId + " actionCode : " + this.actionCode
				+ " retention : " + this.retention + " newsSource : " + this.newsSource + " branding : " + this.branding
				+ " hotInd : " + this.hotInd + " origSource : " + this.origSource + " pageCitation : "
				+ this.pageCitation + " gmtDate : " + this.gmtDate + " gmtTime : " + this.gmtTime + " miliSeconds : "
				+ this.miliSeconds + " categories : " + this.categories;
	}
}
