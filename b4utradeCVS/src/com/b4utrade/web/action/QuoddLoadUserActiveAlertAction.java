package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.ActiveAlertBean;
import com.b4utrade.bean.EdgeNewsCriteriaBean;
import com.b4utrade.bean.NewsBean;
import com.b4utrade.bean.NewsCriteriaDetailBean;
import com.b4utrade.bean.QuoddUserOperationStatusBean;
import com.b4utrade.helper.DJNewsSearchHelper;
import com.b4utrade.helper.EdgeNewsSearchHelper;
import com.b4utrade.util.B4UConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class QuoddLoadUserActiveAlertAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddLoadUserActiveAlertAction.class);
	private static final String alertCpdUrl = Environment.get("ALERT_CPD_URL");
	private static Gson gson = new Gson();
	private static final Type gsonListType = new TypeToken<List<Map<String, Object>>>() {
	}.getType();

	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it). Return an
	 * <code>ActionForward</code> instance describing where and how control should
	 * be forwarded, or <code>null</code> if the response has already been
	 * completed.
	 *
	 * @param mapping    The ActionMapping used to select this instance
	 * @param actionForm The optional ActionForm bean for this request (if any)
	 * @param request    The HTTP request we are processing
	 * @param response   The HTTP response we are creating
	 *
	 * @exception IOException      if an input/output error occurs
	 * @exception ServletException if a servlet exception occurs
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		doCheckUser = false;
		doCheckReferalPartner = false;
		ActionForward forward = super.execute(mapping, form, request, response);
		if (forward != null)
			return forward;
		try {
			response.setContentType("text/xml");
			request.getSession(false);
			QuoddUserOperationStatusBean statusbean = new QuoddUserOperationStatusBean();
			Cookie[] cookies = request.getCookies();
			String userIdStr = "0";
			for (Cookie cook : cookies) {
				if ("USER_ID".equals(cook.getName()))
					userIdStr = cook.getValue();
			}
			long userId = 0;
			try {
				userId = Long.parseLong(userIdStr);
			} catch (Exception e) {
				userId = 0;
			}
			String newsSource = request.getParameter("NEWS_SOURCE");
			String isNews = request.getParameter("IS_NEWS"); // used so that mobile code does not fetch news
			if (isNews == null)
				isNews = "Y";
			Vector activeAlertBeanList = getActiveAlert(userId);
			try {
				if (!isNews.equalsIgnoreCase("N"))
					injectLatestNewsId(activeAlertBeanList, newsSource);
				log.info("Active Alerts retrieved :- " + activeAlertBeanList.size());
			} catch (Exception e) {
				log.error("QuoddLoadUserActiveAlertAction.execute(): exception from retrieving user alert for user="
						+ userId, e);
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(activeAlertBeanList);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddLoadUserActiveAlertAction.execute(): exception from servlet output stream.", e);
			}
		} catch (Exception e) {
			String msg = "QuoddLoadUserActiveAlertAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}

	public static Vector<ActiveAlertBean> getActiveAlert(long userId) {
		Vector<ActiveAlertBean> detailBeanVector = new Vector<>();
		try {
			String urlString = alertCpdUrl + "/alerts/list/active?user_id=" + userId;
			Logger.log("QuoddLoadUserActiveAlertAction: requesting " + urlString);
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				Logger.log("QuoddLoadUserActiveAlertAction: Unable to get alert list for userId: " + userId);
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			List<Map<String, Object>> dataList = gson.fromJson(jsonString, gsonListType);
			for (Map<String, Object> dataMap : dataList) {
				ActiveAlertBean bean = new ActiveAlertBean();
				Double tmp = (Double) dataMap.get("userId");
				if (tmp != null)
					bean.setUserId(tmp.intValue());
				else
					bean.setUserId(Long.valueOf(userId).intValue());
				bean.setTickerName((String) dataMap.get("ticker"));
				bean.setWebFlag((String) dataMap.get("web_flag"));
				bean.setAlertType((String) dataMap.get("alert_type"));
				bean.setAlertValue((String) dataMap.get("alert_value"));
				bean.setDateCreated((String) dataMap.get("date_created"));
				bean.setAlertName((String) dataMap.get("alert_name"));
				tmp = (Double) dataMap.get("alert_frequency");
				bean.setAlertFreq(tmp == null ? 0 : tmp.intValue());
				bean.setComments((String) dataMap.get("comments"));
				detailBeanVector.add(bean);
			}
		} catch (Exception e) {
			Logger.log("QuoddLoadUserActiveAlertAction -exception in api Call " + e.getMessage(), e);
		}
		return detailBeanVector;
	}

	private void injectLatestNewsId(Vector activeAlertBeanList, String newsSource) {
		try {
			if (activeAlertBeanList.size() == 0)
				return;
			StringBuffer buffer = new StringBuffer();
			for (Iterator iterator = activeAlertBeanList.iterator(); iterator.hasNext();) {
				ActiveAlertBean bean = (ActiveAlertBean) iterator.next();
				buffer.append(",");
				buffer.append(bean.getTickerName());
			}
			String tickers = buffer.substring(1); // removing leading comma separator
			HashMap<String, NewsBean> map = null;
			if (newsSource == null || !newsSource.contains(B4UConstants.DJ_NEWS)) {
				// fetch Edge news
				EdgeNewsCriteriaBean bean = new EdgeNewsCriteriaBean();
				bean.setTickers(tickers);
				bean.setSources(newsSource);
				map = EdgeNewsSearchHelper.getLatestNewsByTickers(tickers, newsSource);
			} else {
				map = getCompanyNews(tickers, newsSource);
			}
			if (map == null || map.size() == 0)
				return;
			for (Iterator iterator = activeAlertBeanList.iterator(); iterator.hasNext();) {
				ActiveAlertBean alertBean = (ActiveAlertBean) iterator.next();
				if (map.containsKey(alertBean.getTickerName())) {
					NewsBean newsBean = map.get(alertBean.getTickerName());
					if (newsBean != null) {
						alertBean.setNewsId(newsBean.getNewsID());
					} else
						alertBean.setNewsId(0);
				} else
					alertBean.setNewsId(0);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.log("Exception in injectLatestNewsId() : " + ex);
		}
	}

	private HashMap<String, NewsBean> getCompanyNews(String tickers, String source) {
		long tzOffset = 0L;
		if (tickers == null)
			return new HashMap<String, NewsBean>();
		HashMap<String, NewsBean> newsMap = new HashMap<String, NewsBean>();
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
				newsMap = EdgeNewsSearchHelper.getLatestNewsByTickers(tickers, premiumSource);
			}
		}
		ArrayList<String> data = new ArrayList<String>();
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
		List<NewsBean> results = DJNewsSearchHelper.searchLatestCompanyNews(bean);
		if (results != null) {
			int size = results.size();
			for (int i = 0; i < size; i++) {
				NewsBean djBean = results.get(i);
				djBean.setCategoryType(B4UConstants.DJ_NEWS);
				String ticker = djBean.getTickers();
				String[] djTicks = ticker.split(",");
				for (int j = 0; j < djTicks.length; j++) {
					if (data.contains(djTicks[j])) {
						NewsBean edgeBean = newsMap.get(djTicks[j]);
						if (edgeBean != null) {
							if (djBean.getNewsDate().getTime() > edgeBean.getNewsDate().getTime())
								newsMap.put(djTicks[j], djBean);
						} else
							newsMap.put(djTicks[j], djBean);
					}
				}
			}
		}
		return newsMap;
	}
}
