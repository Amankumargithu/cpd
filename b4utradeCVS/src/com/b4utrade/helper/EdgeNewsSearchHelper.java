package com.b4utrade.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.b4utrade.bean.EdgeNewsCriteriaBean;
import com.b4utrade.bean.NewsBean;
import com.b4utrade.ejb.EdgeNewsData;
import com.b4utrade.ejb.EdgeNewsDataHandler;
import com.b4utrade.util.B4UConstants;
import com.tacpoint.common.DefaultObject;
import com.tacpoint.objectpool.ObjectPoolManager;
import com.tacpoint.util.Logger;

public class EdgeNewsSearchHelper extends DefaultObject {
	private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");

	public static ArrayList<NewsBean> searchNews(EdgeNewsCriteriaBean newsCriteria) {
		return service(newsCriteria, 501);
	}

	public static ArrayList<NewsBean> searchNews(EdgeNewsCriteriaBean newsCriteria, int maxRows) {
		return service(newsCriteria, maxRows);
	}

	public static HashMap<String, ArrayList<NewsBean>> selectGeneralNews(int maxRows) {
		HashMap<String, ArrayList<NewsBean>> map = new HashMap<>();
		EdgeNewsCriteriaBean bean = new EdgeNewsCriteriaBean();
		Calendar cal = Calendar.getInstance();
		bean.setEndDate(sdf.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_MONTH, -400);
		bean.setStartDate(sdf.format(cal.getTime()));
		bean.setSources(B4UConstants.EDGE_SOURCE);
		map.put(B4UConstants.EDGE_SOURCE, service(bean, maxRows));
		bean.setSources(B4UConstants.BENZINGA_SOURCE);
		map.put(B4UConstants.BENZINGA_SOURCE, service(bean, maxRows));
		bean.setSources(B4UConstants.MIDNIGHT_SOURCE);
		map.put(B4UConstants.MIDNIGHT_SOURCE, service(bean, maxRows));
		bean.setSources(B4UConstants.STREET_SOURCE);
		map.put(B4UConstants.STREET_SOURCE, service(bean, maxRows));
		bean.setSources(B4UConstants.FLY_ON_THE_WALL_SOURCE);
		map.put(B4UConstants.FLY_ON_THE_WALL_SOURCE, service(bean, maxRows));
		return map;
	}

	public static HashMap<String, ArrayList<NewsBean>> getLatestNewsByTickers(EdgeNewsCriteriaBean newsCriteria) {
		return getNewsByTickers(newsCriteria, 1);
	}

	public static ArrayList<NewsBean> getLatestNewsByTickerSnap(EdgeNewsCriteriaBean newsCriteria) {
		return getCompressedNewsByTickers(newsCriteria, 1);
	}

	public static byte[] searchCompressedNews(EdgeNewsCriteriaBean newsCriteria) {
		return serviceCompressed(newsCriteria, 501);
	}

	protected static byte[] serviceCompressed(EdgeNewsCriteriaBean newsCriteria, int maxRows) {
		byte[] newsBytes = null;
		if (newsCriteria == null)
			return (null);
		ObjectPoolManager opm = null;
		EdgeNewsDataHandler ndh = null;
		int numTries = 0;
		while (numTries < 1) {
			try {
				opm = ObjectPoolManager.getInstance();
				ndh = (EdgeNewsDataHandler) opm.getObject("EdgeNewsDataHandler", 1000);
				if (ndh != null) {
					EdgeNewsData nd = ndh.getRemoteInterface();
					if (nd != null) {
						newsBytes = nd.getCompressedNews(newsCriteria, maxRows);
						numTries++;
					} else {
						Logger.log("EdgeNewsSearchHelper.serviceCompressed - EdgeNewsData object is null.");
						numTries++;
					}
				} else {
					Logger.log(
							"Exception EdgeNewsSearchHelper.serviceCompressed - Not able to obtain Edge News Handler");
					numTries++;
				}
			} catch (Exception exc) {
				Logger.log(
						"EdgeNewsSearchHelper.serviceCompressed - Exception encountered while trying to get News Data.",
						exc);
				numTries++;
				if (ndh != null)
					ndh.init();
			} finally {
				if (ndh != null)
					opm.freeObject("EdgeNewsDataHandler", ndh);
			}
		}
		return newsBytes;
	}

	protected static ArrayList<NewsBean> service(EdgeNewsCriteriaBean newsCriteria, int maxRows) {
		ArrayList<NewsBean> newsBeans = null;
		if (newsCriteria == null)
			return (null);
		ObjectPoolManager opm = null;
		EdgeNewsDataHandler ndh = null;
		int numTries = 0;
		while (numTries < 1) {
			try {
				opm = ObjectPoolManager.getInstance();
				ndh = (EdgeNewsDataHandler) opm.getObject("EdgeNewsDataHandler", 1000);
				if (ndh != null) {
					EdgeNewsData nd = ndh.getRemoteInterface();
					if (nd != null) {
						newsBeans = nd.getNews(newsCriteria, maxRows);
						numTries++;
					} else {
						Logger.log("EdgeNewsSearchHelper.service - EdgeNewsData object is null.");
						numTries++;
					}
				} else {
					Logger.log("Exception EdgeNewsSearchHelper.service - Not able to obtain Edge News Handler");
					numTries++;
				}
			} catch (Exception exc) {
				Logger.log("EdgeNewsSearchHelper.service - Exception encountered while trying to get News Data.", exc);
				numTries++;
				if (ndh != null)
					ndh.init();
			} finally {
				if (ndh != null)
					opm.freeObject("EdgeNewsDataHandler", ndh);
			}
		}
		return newsBeans;
	}

	protected static ArrayList<NewsBean> getCompressedNewsByTickers(EdgeNewsCriteriaBean newsCriteria, int maxRows) {
		ArrayList<NewsBean> list = null;
		if (newsCriteria == null)
			return (null);
		ObjectPoolManager opm = null;
		EdgeNewsDataHandler ndh = null;
		int numTries = 0;
		while (numTries < 1) {
			try {
				opm = ObjectPoolManager.getInstance();
				ndh = (EdgeNewsDataHandler) opm.getObject("EdgeNewsDataHandler", 1000);
				if (ndh != null) {
					EdgeNewsData nd = ndh.getRemoteInterface();
					if (nd != null) {
						byte[] newsBytes = nd.getCompressedNewsByTickers(newsCriteria, maxRows);
						list = (ArrayList<NewsBean>) com.b4utrade.helper.NewsCompressor.decompress(newsBytes);
						System.out.println("EdgeNewsSearchHelper.getNewsByTickers: got results " + list.size());
						numTries++;
					} else {
						Logger.log("EdgeNewsSearchHelper.getNewsByTickers - EdgeNewsData object is null.");
						numTries++;
					}
				} else {
					System.out.println(
							"Exception EdgeNewsSearchHelper.getNewsByTickers - Not able to obtain Edge News Handler");
					numTries++;
				}
			} catch (Exception exc) {
				Logger.log(
						"EdgeNewsSearchHelper.getNewsByTickers - Exception encountered while trying to get News Data.",
						exc);
				numTries++;
				if (ndh != null)
					ndh.init();
			} finally {
				if (ndh != null)
					opm.freeObject("EdgeNewsDataHandler", ndh);
			}
		}
		return list;
	}

	protected static HashMap<String, ArrayList<NewsBean>> getNewsByTickers(EdgeNewsCriteriaBean newsCriteria,
			int maxRows) {
		HashMap<String, ArrayList<NewsBean>> map = null;
		if (newsCriteria == null)
			return (null);
		ObjectPoolManager opm = null;
		EdgeNewsDataHandler ndh = null;
		int numTries = 0;
		while (numTries < 1) {
			try {
				opm = ObjectPoolManager.getInstance();
				ndh = (EdgeNewsDataHandler) opm.getObject("EdgeNewsDataHandler", 1000);
				if (ndh != null) {
					EdgeNewsData nd = ndh.getRemoteInterface();
					if (nd != null) {
						map = nd.getNewsByTickers(newsCriteria, maxRows);
						System.out.println("EdgeNewsSearchHelper.getNewsByTickers: got results " + map.size());
						numTries++;
					} else {
						Logger.log("EdgeNewsSearchHelper.getNewsByTickers - EdgeNewsData object is null.");
						numTries++;
					}
				} else {
					System.out.println(
							"Exception EdgeNewsSearchHelper.getNewsByTickers - Not able to obtain Edge News Handler");
					numTries++;
				}
			} catch (Exception exc) {
				Logger.log(
						"EdgeNewsSearchHelper.getNewsByTickers - Exception encountered while trying to get News Data.",
						exc);
				numTries++;
				if (ndh != null)
					ndh.init();
			} finally {
				if (ndh != null)
					opm.freeObject("EdgeNewsDataHandler", ndh);
			}
		}
		return map;
	}

	public static NewsBean getNewsByID(long newsID) {
		NewsBean newsBean = null;
		if (newsID == 0)
			return (null);
		ObjectPoolManager opm = null;
		EdgeNewsDataHandler ndh = null;
		int numTries = 0;
		while (numTries < 1) {
			try {
				opm = ObjectPoolManager.getInstance();
				ndh = (EdgeNewsDataHandler) opm.getObject("EdgeNewsDataHandler", 1000);
				if (ndh != null) {
					EdgeNewsData nd = ndh.getRemoteInterface();
					if (nd != null) {
						newsBean = nd.getNewsByID(newsID);
						numTries++;
					} else {
						Logger.log("EdgeNewsSearchHelper.getNewsByID - EdgeNewsData object is null.");
						numTries++;
					}
				} else {
					System.out.println(
							"Exception EdgeNewsSearchHelper.getNewsByID - Not able to obtain Edge News Handler");
					numTries++;
				}
			} catch (Exception exc) {
				Logger.log("EdgeNewsSearchHelper.getNewsByID - Exception encountered while trying to get News Data.",
						exc);
				numTries++;
				if (ndh != null)
					ndh.init();
			} finally {
				if (ndh != null)
					opm.freeObject("EdgeNewsDataHandler", ndh);
			}
		}
		return newsBean;
	}

	public static HashMap<String, NewsBean> getLatestNewsByTickers(String tickers, String sources) {
		HashMap<String, NewsBean> map = null;
		if (tickers == null)
			return (null);
		ObjectPoolManager opm = null;
		EdgeNewsDataHandler ndh = null;
		int numTries = 0;
		while (numTries < 1) {
			try {
				opm = ObjectPoolManager.getInstance();
				ndh = (EdgeNewsDataHandler) opm.getObject("EdgeNewsDataHandler", 1000);
				if (ndh != null) {
					EdgeNewsData nd = ndh.getRemoteInterface();
					if (nd != null) {
						map = nd.getLatestNewsByTickers(tickers, sources);
						System.out.println("EdgeNewsSearchHelper.getLatestNewsByTickers: got results " + map.size());
						numTries++;
					} else {
						Logger.log("EdgeNewsSearchHelper.getLatestNewsByTickers - EdgeNewsData object is null.");
						numTries++;
					}
				} else {
					System.out.println(
							"Exception EdgeNewsSearchHelper.getLatestNewsByTickers - Not able to obtain Edge News Handler");
					numTries++;
				}
			} catch (Exception exc) {
				Logger.log(
						"EdgeNewsSearchHelper.getLatestNewsByTickers - Exception encountered while trying to get News Data.",
						exc);
				numTries++;
				if (ndh != null)
					ndh.init();
			} finally {
				if (ndh != null)
					opm.freeObject("EdgeNewsDataHandler", ndh);
			}
		}
		return map;
	}
}
