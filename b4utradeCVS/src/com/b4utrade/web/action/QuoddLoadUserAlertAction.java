package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
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

import com.b4utrade.bean.UserAlertBean;
import com.b4utrade.bean.UserAlertDetailBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class QuoddLoadUserAlertAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddLoadUserAlertAction.class);
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
			UserAlertBean alertBean = new UserAlertBean();
			if (userId > 0) {
				alertBean.setAlertDetail(getUserAlerts(userId));
				alertBean.setLoadingSuccessfulFlag();
			}
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try (XMLEncoder encoder = new XMLEncoder(byteArray);) {
				encoder.writeObject(alertBean);
			}
			try (ServletOutputStream sos = response.getOutputStream();) {
				sos.write(byteArray.toByteArray());
				sos.flush();
			} catch (Exception e) {
				log.error("QuoddLoadUserAlertAction.execute() : encountered exception from servlet output stream.", e);
			}
		} catch (Exception e) {
			String msg = "QuoddLoadUserAlertAction.execute() : Unable to retrieve data.";
			log.error(msg, e);
		}
		return null;
	}

	public static Vector<UserAlertDetailBean> getUserAlerts(long userId) {
		Vector<UserAlertDetailBean> detailBeanVector = new Vector<>();
		try {
			String urlString = alertCpdUrl + "/alerts/list?user_id=" + userId;
			Logger.log("QuoddLoadUserAlertAction: requesting " + urlString);
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				Logger.log("QuoddLoadUserAlertAction: Unable to get alert list for userId: " + userId);
			}
			String jsonString = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));) {
				jsonString = br.readLine();
			}
			urlConnection.disconnect();
			List<Map<String, Object>> dataList = gson.fromJson(jsonString, gsonListType);
			for (Map<String, Object> dataMap : dataList) {
				UserAlertDetailBean detailBean = new UserAlertDetailBean();
				detailBean = mapBeanData(detailBean, dataMap);
				detailBeanVector.addElement(detailBean);
			}
		} catch (Exception e) {
			Logger.log("QuoddLoadUserAlertAction.getUserAlerts -exception in api Call " + e.getMessage(), e);
		}
		return detailBeanVector;
	}

	private static UserAlertDetailBean mapBeanData(UserAlertDetailBean detailBean, Map<String, Object> dataMap) {
		detailBean.setWebFlag((String) dataMap.get("web_flag"));
		Object activity = dataMap.get("last_over_activity");
		detailBean.setLastOverAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setLastOverAlertValue((String) dataMap.get("last_over_value"));
		activity = dataMap.get("last_under_activity");
		detailBean.setLastUnderAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setLastUnderAlertValue((String) dataMap.get("last_under_value"));
		activity = dataMap.get("last_equal_activity");
		detailBean.setLastEqualAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setLastEqualAlertValue((String) dataMap.get("last_equal_value"));
		activity = dataMap.get("ask_over_activity");
		detailBean.setAskOverAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setAskOverAlertValue((String) dataMap.get("ask_over_value"));
		activity = dataMap.get("ask_under_activity");
		detailBean.setAskUnderAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setAskUnderAlertValue((String) dataMap.get("ask_under_value"));
		activity = dataMap.get("ask_equal_activity");
		detailBean.setAskEqualAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setAskEqualAlertValue((String) dataMap.get("ask_equal_value"));
		activity = dataMap.get("bid_over_activity");
		detailBean.setBidOverAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setBidOverAlertValue((String) dataMap.get("bid_over_value"));
		activity = dataMap.get("bid_under_activity");
		detailBean.setBidUnderAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setBidUnderAlertValue((String) dataMap.get("bid_under_value"));
		activity = dataMap.get("bid_equal_activity");
		detailBean.setBidEqualAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setBidEqualAlertValue((String) dataMap.get("bid_equal_value"));
		activity = dataMap.get("change_up_activity");
		detailBean.setPercentChangeUpAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setPercentChangeUpAlertValue((String) dataMap.get("change_up_value"));
		activity = dataMap.get("change_down_activity");
		detailBean.setPercentChangeDownAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setPercentChangeDownAlertValue((String) dataMap.get("change_down_value"));
		activity = dataMap.get("volume_over_equal_activity");
		detailBean.setVolumeOverAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setVolumeOverAlertValue((String) dataMap.get("volume_over_equal_value"));
		activity = dataMap.get("trade_vol_equal_activity");
		detailBean.setLastTradeVolumeEqualAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setLastTradeVolumeEqualAlertValue((String) dataMap.get("trade_vol_equal_value"));
		activity = dataMap.get("trade_vol_over_activity");
		detailBean.setLastTradeVolumeOverAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setLastTradeVolumeOverAlertValue((String) dataMap.get("trade_vol_over_value"));
		activity = dataMap.get("trade_vol_equal_over_activity");
		detailBean.setLastTradeVolumeEqualOverAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setLastTradeVolumeEqualOverAlertValue((String) dataMap.get("trade_vol_equal_over_value"));
		detailBean.setAlarmTime((String) dataMap.get("alertValue"));
		activity = dataMap.get("earnings_reported_activity");
		detailBean.setEarningsReportedAlertActivity(activity == null ? false : (boolean) activity);
		activity = dataMap.get("company_news_activity");
		detailBean.setCompanyNewsAlertActivity(activity == null ? false : (boolean) activity);
		activity = dataMap.get("fiftytwo_week_high_activity");
		detailBean.setFiftyTwoWeekHighAlertActivity(activity == null ? false : (boolean) activity);
		activity = dataMap.get("fiftytwo_week_low_activity");
		detailBean.setFiftyTwoWeekLowAlertActivity(activity == null ? false : (boolean) activity);
		detailBean.setTickerName((String) dataMap.get("ticker"));
		detailBean.setAlertName((String) dataMap.get("alert_name"));
		detailBean.setAlertComments((String) dataMap.get("comments"));
		if (detailBean.getAlertComments() == null)
			detailBean.setAlertComments("");
		Double tmp = (Double) dataMap.get("alert_frequency");
		if (tmp != null)
			detailBean.setAlertFrequency(tmp.intValue());
		return detailBean;
	}
}
