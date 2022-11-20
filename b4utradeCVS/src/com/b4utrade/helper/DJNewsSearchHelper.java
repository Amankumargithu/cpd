package com.b4utrade.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.b4utrade.bean.NewsBean;
import com.b4utrade.bean.NewsCriteriaDetailBean;
import com.b4utrade.ejb.NewsData;
import com.b4utrade.ejb.NewsDataHandler;
import com.tacpoint.common.DefaultObject;
import com.tacpoint.objectpool.ObjectPoolManager;
import com.tacpoint.util.Logger;

public class DJNewsSearchHelper extends DefaultObject {

	private static final Log log = LogFactory.getLog(DJNewsSearchHelper.class);

	public static ArrayList searchNews(NewsCriteriaDetailBean newsCriteria, int maxRows) {
		return service(newsCriteria, maxRows);
	}

	public static ArrayList searchNews(NewsCriteriaDetailBean newsCriteria) {
		return service(newsCriteria, 501);
	}

	public static ArrayList searchLatestCompanyNews(NewsCriteriaDetailBean newsCriteria) {
		return serviceLatestCompanyNews(newsCriteria);
	}

	public static byte[] searchCompressedNews(NewsCriteriaDetailBean newsCriteria, int maxRows) {
		return serviceCompressed(newsCriteria, maxRows);
	}

	public static byte[] searchCompressedNews(NewsCriteriaDetailBean newsCriteria) {
		return serviceCompressed(newsCriteria, 501);
	}

	protected static ArrayList serviceLatestCompanyNews(NewsCriteriaDetailBean newsCriteria) {
		if (newsCriteria == null)
			return (new ArrayList());
		ObjectPoolManager opm = null;
		NewsDataHandler ndh = null;
		int numTries = 0;
		while (numTries <= 2) {
			try {
				opm = ObjectPoolManager.getInstance();
				ndh = (NewsDataHandler) opm.getObject("NewsDataHandler", 1000);
				if (ndh != null) {
					NewsData nd = ndh.getRemoteInterface();
					if (nd != null) {
						byte[] newsBytes = nd.getCompressedLatestCompanyNews(newsCriteria);
						Collection list = NewsCompressor.decompress(newsBytes);
						if (list != null) {
							log.info("news results size : " + list.size());
							if (list == null)
								list = new ArrayList();
						}
						return (ArrayList) list;
					}
				} else {
					log.error("cannot obtain news handler");
				}
			} catch (Exception exc) {
				log.error(exc.getMessage(), exc);
			} finally {
				numTries++;
				if (ndh != null)
					opm.freeObject("NewsDataHandler", ndh);
			}
		}
		return new ArrayList();
	}

	protected static ArrayList service(NewsCriteriaDetailBean newsCriteria, int maxRows) {
		if (newsCriteria == null)
			return (new ArrayList());
		ObjectPoolManager opm = null;
		NewsDataHandler ndh = null;
		int numTries = 0;
		while (numTries <= 2) {
			try {
				opm = ObjectPoolManager.getInstance();
				ndh = (NewsDataHandler) opm.getObject("NewsDataHandler", 1000);
				if (ndh != null) {
					NewsData nd = ndh.getRemoteInterface();
					if (nd != null) {
						byte[] newsBytes = nd.getCompressedNews(newsCriteria, maxRows);
						Collection list = NewsCompressor.decompress(newsBytes);
						if (list != null) {
							log.info("news results size : " + list.size());
							if (list == null)
								list = new ArrayList();
						}
						return (ArrayList) list;
					} else {
						log.error("NewsData object is null.");
					}
				} else
					log.error("cannot obtain news handler");
			} catch (Exception exc) {
				log.error(exc.getMessage(), exc);
			} finally {
				numTries++;
				if (ndh != null)
					opm.freeObject("NewsDataHandler", ndh);
			}
		}
		return new ArrayList();
	}

	protected static byte[] serviceCompressed(NewsCriteriaDetailBean newsCriteria, int maxRows) {
		if (newsCriteria == null)
			return (null);
		ObjectPoolManager opm = null;
		NewsDataHandler ndh = null;
		int numTries = 0;
		while (numTries <= 2) {
			try {
				opm = ObjectPoolManager.getInstance();
				ndh = (NewsDataHandler) opm.getObject("NewsDataHandler", 1000);
				if (ndh != null) {
					NewsData nd = ndh.getRemoteInterface();
					if (nd != null) {
						byte[] newsBytes = nd.getCompressedNews(newsCriteria, maxRows);
						return newsBytes;
					}
				} else {
					log.error("cannot obtain news handler");
				}
			} catch (Exception exc) {
				log.error(exc.getMessage(), exc);
			} finally {
				numTries++;
				if (ndh != null)
					opm.freeObject("NewsDataHandler", ndh);
			}
		}
		return null;
	}

	public static List searchNews(String inTicker, int maxRows) {
		log.info("Start searching News by ticker.");
		if (inTicker == null || inTicker.length() == 0)
			return (new ArrayList());
		NewsCriteriaDetailBean ncdbean = new NewsCriteriaDetailBean();
		ArrayList tickerList = new ArrayList();
		tickerList.add(inTicker);
		ncdbean.setTickers(tickerList);
		List list = searchNews(ncdbean, maxRows);
		log.info("End searching News by ticker.");
		return (ArrayList) list;
	}

	public static ArrayList searchNews(String inTicker) {
		log.info("start searching News by ticker.");
		if (inTicker == null || inTicker.length() == 0)
			return (new ArrayList());
		NewsCriteriaDetailBean ncdbean = new NewsCriteriaDetailBean();
		ArrayList tickerList = new ArrayList();
		tickerList.add(inTicker);
		ncdbean.setTickers(tickerList);
		List list = searchNews(ncdbean);
		log.info("End searching News by ticker.");
		return (ArrayList) list;
	}

	public static byte[] searchCompressedNews(String inTicker) {
		log.info("Start searching News by ticker.");
		if (inTicker == null || inTicker.length() == 0)
			return null;
		NewsCriteriaDetailBean ncdbean = new NewsCriteriaDetailBean();
		ArrayList tickerList = new ArrayList();
		tickerList.add(inTicker);
		ncdbean.setTickers(tickerList);
		log.info("End searching News by ticker.");
		return searchCompressedNews(ncdbean);
	}

	public static NewsBean getNewsByID(long newsID) {
		NewsBean newsBean = null;
		if (newsID == 0)
			return (null);
		ObjectPoolManager opm = null;
		NewsDataHandler ndh = null;
		int numTries = 0;
		while (numTries < 1) {
			try {
				opm = ObjectPoolManager.getInstance();
				ndh = (NewsDataHandler) opm.getObject("NewsDataHandler", 1000);
				if (ndh != null) {
					NewsData nd = ndh.getRemoteInterface();
					if (nd != null) {
						newsBean = nd.getNewsByID(newsID);
						numTries++;
					} else {
						Logger.log("DJNewsSearchHelper.getNewsByID - NewsData object is null.");
						numTries++;
					}
				} else {
					System.out
							.println("Exception DJNewsSearchHelper.getNewsByID - Not able to obtain Edge News Handler");
					numTries++;
				}
			} catch (Exception exc) {
				Logger.log("DJNewsSearchHelper.getNewsByID - Exception encountered while trying to get News Data.",
						exc);
				numTries++;
				if (ndh != null)
					ndh.init();
			} finally {
				if (ndh != null)
					opm.freeObject("NewsDataHandler", ndh);
			}
		}
		return newsBean;
	}

}
