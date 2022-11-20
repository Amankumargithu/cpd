package com.b4utrade.web.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.EquityMessageBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tacpoint.util.Environment;

public class QuoddStockRoverTsxQuoteAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(QuoddStockRoverTsxQuoteAction.class);
	private static Gson gson = new Gson();
	private static Type t = new TypeToken<ArrayList<HashMap>>() {
	}.getType();
	private static HashSet<String> allowedProtocols = new HashSet<>();
	static {
		allowedProtocols.add("25");
		allowedProtocols.add("24");
		allowedProtocols.add("27");
		allowedProtocols.add("28");
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		String params = null;
		try (InputStream in = request.getInputStream()) {
			if (in != null) {
				int ch = -1;
				StringBuilder buffer = new StringBuilder();
				while ((ch = in.read()) != -1) {
					char c = (char) ch;
					buffer.append(c);
				}
				params = buffer.toString();
				log.info("QuoddStockRoverTsxQuoteAction Request " + params);
			} else {
				log.warn("QuoddStockRoverTsxQuoteAction parameters are is null.");
			}
		}
		if (params == null || params.isEmpty())
			return null;
		ArrayList<HashMap> list = gson.fromJson(params, t);
		if (list.isEmpty())
			return null;
		HashMap paramMap = list.get(0);
		if (paramMap == null)
			return null;
		String username = (String) paramMap.get("UserName");
		String password = (String) paramMap.get("Password");
		ArrayList<String> symbolParams = (ArrayList<String>) paramMap.get("symbols");
		ArrayList<String> symbols = new ArrayList<>();
		for (String s : symbolParams) {
			if (s != null && s.length() > 0)
				symbols.add(s);
		}
		if (symbols.isEmpty())
			return null;
		SimpleDateFormat df = new SimpleDateFormat("HHmm");
		int time = Integer.parseInt(df.format(new Date()));
		if (time < 1615 || time > 2330)
			return null;
		if ("stockrover".equals(username) && "ondemandstockrover".equals(password)) {
			String snapUrl = Environment.get("DELAYED_SNAP_URL");
			if (snapUrl == null)
				response.setStatus(404);
			else {
				List<EquityMessageBean> delayedList = getDelayedSnapData(symbols, snapUrl);
				ArrayList<HashMap<String, String>> resultBeans = new ArrayList<>();
				for (EquityMessageBean bean : delayedList) {
					if (allowedProtocols.contains(bean.getPROTOCOL())) {
						HashMap<String, String> map = new HashMap<>();
						map.put("SYMBOL", bean.getTICKER());
						map.put("ASK", bean.getASK());
						map.put("BID", bean.getBID());
						map.put("PREVIOUS_CLOSE", bean.getPREVIOUS_PRICE());
						map.put("DAY_HIGH", bean.getDAY_HIGH());
						map.put("DAY_LOW", bean.getDAY_LOW());
						map.put("LAST", bean.getLAST());
						map.put("CHANGE", bean.getCHANGE_PRICE());
						map.put("OPEN", bean.getOPEN());
						map.put("VOLUME", bean.getVOLUME());
						map.put("BID_SIZE", bean.getBID_SIZE());
						map.put("ASK_SIZE", bean.getASK_SIZE());
						map.put("TRADE_SIZE", bean.getTRADE_SIZE());
						map.put("TRADE_DATE", bean.getTRADE_DATE());
						map.put("TRADE_TIME", bean.getLAST_TRADE_TIME());
						resultBeans.add(map);
					}
				}
				try (ServletOutputStream sos = response.getOutputStream()) {
					sos.write(gson.toJson(resultBeans).getBytes());
					sos.flush();
				}
			}
		} else {
			response.setStatus(401);
		}
		return null;
	}

	private static List<EquityMessageBean> getDelayedSnapData(List<String> modifiesTickers, String snapUrl) {
		List<EquityMessageBean> result = new ArrayList<>();
		HashMap<String, LinkedList<byte[]>> snapData = null;
		try {
			StringBuffer buf = new StringBuffer();
			for (String s : modifiesTickers) {
				if (s != null && s.trim().length() > 0)
					buf.append("," + s);
			}
			if (buf.length() == 0)
				return result;
			StringBuffer updatedUrl = new StringBuffer(snapUrl);
			updatedUrl.append(URLEncoder.encode(buf.substring(1), "UTF-8"));
			log.info("QuoddStockRoverTsxQuoteAction Delayed Snap url is : " + updatedUrl.toString());
			URL url = new URL(updatedUrl.toString());
			URLConnection urlConnection = url.openConnection();
			Object o = null;
			try (ObjectInputStream objectStream = new ObjectInputStream(urlConnection.getInputStream());) {
				o = objectStream.readObject();
			} catch (ClassNotFoundException e) {
				log.error("QuoddStockRoverTsxQuoteAction.getDelayedSnapData exception " + e.getMessage(), e);
			}
			if (o != null) {
				snapData = (HashMap<String, LinkedList<byte[]>>) o;
				Set<String> keys = snapData.keySet();
				for (String type : keys) {
					LinkedList<byte[]> list = snapData.get(type);
					if (list != null && list.size() > 0)
						for (byte[] stream : list) {
							EquityMessageBean bean = new EquityMessageBean(new String(stream), type, "D");
							result.add(bean);
						}
				}
			}
		} catch (Exception e) {
			log.error("QuoddStockRoverTsxQuoteAction.getDelayedSnapData exception " + e.getMessage(), e);
		}
		return result;
	}
}
