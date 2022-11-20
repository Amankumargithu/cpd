import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.b4utrade.bean.NewsBean;
import com.b4utrade.bean.NewsCriteriaDetailBean;
import com.b4utrade.helper.DJNewsSearchHelper;
import com.b4utrade.helper.EdgeNewsSearchHelper;
import com.b4utrade.helper.LatestDJNewsCache;
import com.b4utrade.helper.NewsDateComparator;
import com.b4utrade.helper.QuoteServerHelper;
import com.b4utrade.helper.StockNewsUpdateHelper;
import com.b4utrade.helper.StockNewsUpdateHelperComparator;
import com.b4utrade.servlet.B4UTServlet;
import com.b4utrade.util.B4UConstants;
import com.tacpoint.util.Logger;
import com.tacpoint.util.Utility;

public class QuoteAndDJNewsServlet extends B4UTServlet {

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	public void processRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		boolean showSnaps = false;
		boolean showGeneralMarketNews = false;
		boolean showGeneralMarketAndCompanyNews = false;
		boolean showSnapAndCompanyNews = false;
		boolean showCompanyNews = false;
		String stocks = req.getParameter("COMPANYID");
		String action = req.getParameter("ACTION");
		String source = req.getParameter("SOURCE");
		if (source == null)
			source = B4UConstants.DJ_NEWS;
		if (action != null) {
			if (action.equals("GENERAL")) {
				showGeneralMarketNews = true;
			} else if (action.equals("SNAP")) {
				showSnaps = true;
			} else if (action.equals("COMPANY")) {
				showCompanyNews = true;
			} else if (action.equals("SNAP_COMPANY")) {
				showSnapAndCompanyNews = true;
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (OutputStream os = res.getOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			Hashtable payload = new Hashtable<>();
			// Not in used
			if (showSnaps && stocks != null) {
				payload = QuoteServerHelper.getQuote(stocks);
			}
			// Not in used
			else if (showCompanyNews && stocks != null) {
				payload = getCompanyNews(stocks, source);
			}
			// Not in used
			else if (showGeneralMarketAndCompanyNews && stocks != null) {
				Logger.log("Pulling company and general news!");
				Hashtable generalHash = getGeneralNews(source);
				Hashtable companyHash = getCompanyNews(stocks, source);
				payload = mergeNewsResults(generalHash, companyHash);
			} else if (showSnapAndCompanyNews && stocks != null) {
				Logger.log("Pulling snap quotes and company news!");
				Hashtable snaps = QuoteServerHelper.getQuote(stocks);
				Hashtable companyHash = getCompanyNews(stocks, source);
				payload = mergeSnapAndNewsResults(snaps, companyHash);
			} else if (showGeneralMarketNews) {
				payload = getGeneralNews(source);
			}
			oos.writeObject(payload);
			oos.flush();
			byte[] bytes = baos.toByteArray();
			os.write(bytes);
		} catch (Exception e) {
			Logger.log("Exception in QuoteAndDJNewsServlet.processRequest " + e.getMessage(), e);
		}
	}

	private Hashtable getCompanyNews(String tickers, String source) {
		long tzOffset = 0L;
		if (tickers == null)
			return new Hashtable();
		HashMap newsMap = new HashMap();
		if (!source.equalsIgnoreCase(B4UConstants.DJ_NEWS)) {
			String premiumSource = "";
			StringBuffer sb = new StringBuffer();
			String sources[] = source.split(",");
			for (int i = 0; i < sources.length; i++) {
				if (!(sources[i].equalsIgnoreCase(B4UConstants.DJ_NEWS))) {
					sb.append(",");
					sb.append(sources[i]);
				}
			}
			if (sb.length() > 1) {
				premiumSource = sb.substring(1);
				Logger.log("QuoteAndDJNewsServlet.getCompanyNews source = " + premiumSource);
				newsMap = EdgeNewsSearchHelper.getLatestNewsByTickers(tickers, premiumSource);
				if (newsMap == null || newsMap.size() == 0)
					newsMap = new HashMap();
			}
		}
		ArrayList data = new ArrayList();
		StringTokenizer st = new StringTokenizer(tickers, ",");
		while (st.hasMoreTokens()) {
			String ticker = st.nextToken();
			data.add(ticker);
		}
		// evaluate dj news
		NewsCriteriaDetailBean bean = new NewsCriteriaDetailBean();
		bean.setOperationType(NewsCriteriaDetailBean.OPERATION_OR);
		bean.setTickers(data);
		long now = System.currentTimeMillis() + tzOffset;
		bean.setStartDate(new java.sql.Timestamp(now));
		List results = DJNewsSearchHelper.searchLatestCompanyNews(bean);
		if (results != null) {
			int size = results.size();
			for (int i = 0; i < size; i++) {
				NewsBean djBean = (NewsBean) results.get(i);
				djBean.setCategoryType(B4UConstants.DJ_NEWS);
				String ticker = djBean.getTickers();
				String[] djTicks = ticker.split(",");
				for (int j = 0; j < djTicks.length; j++) {
					if (data.contains(djTicks[j])) {
						NewsBean edgeBean = (NewsBean) newsMap.get(djTicks[j]);
						if (edgeBean != null) {
							if (djBean.getNewsDate().getTime() > edgeBean.getNewsDate().getTime())
								newsMap.put(djTicks[j], djBean);
						} else
							newsMap.put(djTicks[j], djBean);
					}
				}
			}
		}
		Hashtable payload = new Hashtable();
		Iterator itr = newsMap.values().iterator();
		while (itr.hasNext()) {
			NewsBean news = (NewsBean) itr.next();
			if (news == null)
				continue;
			Logger.log("LatestCompanyDJNewsCache.getLatestNews - News ID : " + news.getNewsID() + " Tickers : "
					+ news.getTickers() + " Date : " + news.getNewsDate());
			StockNewsUpdateHelper item = new StockNewsUpdateHelper();
			item.setLastNewsID(news.getNewsID());
			item.setTicker(news.getTickers());
			item.setCategoriesAsString(news.getCategories());
			item.setLastNewsDate(com.tacpoint.util.Utility.getTimeAsPattern(Utility.DATE_PATTERN_DDMMMYYYYHHMISS,
					news.getNewsDate()));
			item.setLastNews(news.getHeadline());
			item.setLastNewsSource(news.getNewsSource());
			Long id = new Long(news.getNewsID());
			if (news.getCategoryType().equals(B4UConstants.DJ_NEWS))
				item.setDowJones("");
			else
				item.setDowJones(B4UConstants.NEWS_EDGE_CODE);
			payload.put(id, item);
		}
		Logger.log("Company news hash size : " + payload.size());
		return payload;
	}

	private Hashtable mergeSnapAndNewsResults(Hashtable snapHash, Hashtable companyHash) {
		Vector snapVector = new Vector();
		Vector companyNewsVector = new Vector();
		Iterator it = snapHash.values().iterator();
		while (it.hasNext()) {
			snapVector.addElement(it.next());
		}
		it = companyHash.values().iterator();
		while (it.hasNext()) {
			companyNewsVector.addElement(it.next());
		}
		Logger.log("Company news vector size : " + companyNewsVector.size());
		Logger.log("Snap vector size : " + snapVector.size());
		Hashtable vhash = new Hashtable();
		vhash.put("SNAP_QUOTE", snapVector);
		vhash.put("COMPANY_NEWS", companyNewsVector);
		return vhash;
	}

	private Hashtable mergeNewsResults(Hashtable generalHash, Hashtable companyHash) {
		Iterator it = generalHash.keySet().iterator();
		while (it.hasNext() && companyHash.size() <= 500) {
			Long key = (Long) it.next();
			companyHash.put(key, generalHash.get(key));
		}
		// now sort all the values in descending order!
		Collection values = companyHash.values();
		StockNewsUpdateHelper[] news = (StockNewsUpdateHelper[]) companyHash.values()
				.toArray(new StockNewsUpdateHelper[0]);
		Arrays.sort(news, new StockNewsUpdateHelperComparator());
		Vector results = new Vector();
		for (int i = 0; i < news.length; i++)
			results.addElement(news[i]);
		Logger.log("Combined news results size : " + results.size());
		Hashtable vhash = new Hashtable();
		vhash.put("1", results);
		return vhash;
	}

	private Hashtable getGeneralNews(String source) {
		ArrayList premiumSource = new ArrayList();
		String sources[] = source.split(",");
		for (int i = 0; i < sources.length; i++) {
			if (!sources[i].equals(B4UConstants.DJ_NEWS)) {
				premiumSource.add(sources[i]);
			}
		}
		Hashtable results = new Hashtable();
		try {
			Vector newsVec = LatestDJNewsCache.getLatestNews();
			if (premiumSource.size() > 0) {
				HashMap premiumNewsMap = EdgeNewsSearchHelper.selectGeneralNews(100);
				for (int i = 0; i < premiumSource.size(); i++) {
					ArrayList premiumNewsList = (ArrayList) premiumNewsMap.get(premiumSource.get(i));
					if (premiumNewsList != null) {
						int size = premiumNewsList.size();
						for (int j = 0; j < size; j++) {
							NewsBean bean = (NewsBean) premiumNewsList.get(j);
							StockNewsUpdateHelper newsHelper = new StockNewsUpdateHelper();
							newsHelper.setTicker(bean.getTickers());
							newsHelper.setLastNews(bean.getHeadline());
							newsHelper.setCategoriesAsString(bean.getCategories());
							newsHelper.setLastNewsSource(bean.getNewsSource());
							newsHelper.setLastNewsID(bean.getNewsID());
							newsHelper.setDowJones(B4UConstants.NEWS_EDGE_CODE);
							newsHelper.setLastNewsDate(
									Utility.getTimeAsPattern(Utility.DATE_PATTERN_DDMMMYYYYHHMISS, bean.getNewsDate()));
							newsVec.addElement(newsHelper);
						}
					}
				}
			}
			if (newsVec != null && newsVec.size() > 0) {
				Collections.sort(newsVec, new NewsDateComparator());
			}
			if (newsVec.size() > 100) {
				newsVec.setSize(100);
			}
			Logger.log("General news size : " + newsVec.size());
			// reverse the order so oldest story is first ...
			Vector resultVec = new Vector();
			for (int i = newsVec.size() - 1; i >= 0; i--)
				resultVec.addElement(newsVec.elementAt(i));
			results.put("GENERAL_NEWS", resultVec);
		} catch (Exception e) {
			Logger.log("Exception in QuoteAndDJNewsServlet : " + e.getMessage());
			e.printStackTrace();
		}
		return results;
	}

	protected boolean doSaveSession() {
		return false;
	}

	@Override
	public String getServletInfo() {
		return "Copyright(c) 2000, Tacpoint Technologies Inc.";
	}

	@Override
	public void destroy() {
		super.destroy();
	}

}
