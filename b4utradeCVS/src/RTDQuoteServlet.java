
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.b4utrade.helper.QuoteServerHelper;
import com.b4utrade.servlet.B4UTServlet;
import com.b4utrade.stock.Quotes;
import com.tacpoint.util.Logger;

public class RTDQuoteServlet extends B4UTServlet {

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	public void processRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (OutputStream os = res.getOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			String stocks = req.getParameter("COMPANYID");
			HashMap<String, LinkedList<byte[]>> result = new HashMap<>();
			if (stocks != null && stocks.length() > 0)
				result = getQuotesForRTD(stocks);
			oos.writeObject(result);
			oos.flush();
			byte[] bytes = baos.toByteArray();
			os.write(bytes);
		} catch (Exception e) {
			Logger.log("Exception in RTDQuoteServlet.processRequest " + e.getMessage());
		}
	}

	private HashMap<String, LinkedList<byte[]>> getQuotesForRTD(String inTickers) {
		final String DELAYED_EQUITY_INDICATOR = ".D";
		HashMap<String, LinkedList<byte[]>> result = new HashMap<>();
		try {
			Quotes tempQuote = null;
			StringBuffer equityTickers = null;
			StringBuffer optionTickers = null;
			StringBuffer futureTickers = null;
			StringBuffer optionRegionalTickers = null;
			StringBuffer equityMontageTickers = null;

			String[] tokens = inTickers.split(",");
			for (String tempTicker : tokens) {
				if (QuoteServerHelper.isOptionRegionalSymbol(tempTicker)) {
					if (optionRegionalTickers == null) {
						optionRegionalTickers = new StringBuffer();
					} else {
						optionRegionalTickers.append(",");
					}
					optionRegionalTickers.append(tempTicker);
				} else if (QuoteServerHelper.isOptionSymbol(tempTicker)) {
					if (optionTickers == null) {
						optionTickers = new StringBuffer();
					} else {
						optionTickers.append(",");
					}
					optionTickers.append(tempTicker);
				} else if (tempTicker.startsWith(QuoteServerHelper.FUTURE_INDICATOR)) {
					if (futureTickers == null) {
						futureTickers = new StringBuffer();
					} else {
						futureTickers.append(",");
					}
					futureTickers.append(tempTicker);
				}
				// For Equity Montage Only
				else if (QuoteServerHelper.isMontageSymbol(tempTicker)) {
					if (equityMontageTickers == null) {
						equityMontageTickers = new StringBuffer();
					} else {
						equityMontageTickers.append(",");
					}
					equityMontageTickers.append(tempTicker);
				} else {
					if (equityTickers == null) {
						equityTickers = new StringBuffer();
					} else {
						equityTickers.append(",");
					}
					if (tempTicker.endsWith(DELAYED_EQUITY_INDICATOR)) {
						tempTicker = tempTicker.substring(0, tempTicker.length() - DELAYED_EQUITY_INDICATOR.length());
					}
					equityTickers.append(tempTicker);
				}
			}
			// get the equity Quotes
			if (equityTickers != null && equityTickers.length() > 0) {
				tempQuote = Quotes.getInstance();
				LinkedList<byte[]> returnList = tempQuote.getStockQuote(equityTickers.toString());
				if (returnList != null && returnList.size() > 0)
					result.put(QuoteServerHelper.TICKER_TYPE_EQUITY, returnList);
			}
			// get the Option Quotes
			if (optionTickers != null && optionTickers.length() > 0) {
				LinkedList<byte[]> returnList = QuoteServerHelper.getOptionDataList(optionTickers.toString());
				if (returnList != null && returnList.size() > 0)
					result.put(QuoteServerHelper.TICKER_TYPE_OPTIONS, returnList);
			}
			// handle future data
			if (futureTickers != null && futureTickers.length() > 0) {
				LinkedList<byte[]> returnList = QuoteServerHelper.getFutureDataList(futureTickers.toString());
				if (returnList != null && returnList.size() > 0)
					result.put(QuoteServerHelper.TICKER_TYPE_FUTURES, returnList);
			}
			if (equityMontageTickers != null && equityMontageTickers.length() > 0) {
				LinkedList<byte[]> returnList = QuoteServerHelper
						.getEquityMontageDataList(equityMontageTickers.toString());
				if (returnList != null && returnList.size() > 0)
					result.put(QuoteServerHelper.TICKER_TYPE_EQUITY_MONTAGE, returnList);
			}
			if (optionRegionalTickers != null && optionRegionalTickers.length() > 0) {
				LinkedList<byte[]> returnList = QuoteServerHelper
						.getOptionRegionalDataList(optionRegionalTickers.toString());
				if (returnList != null && returnList.size() > 0)
					result.put(QuoteServerHelper.TICKER_TYPE_OPTIONS_MONTAGE, returnList);
			}
		} catch (Exception e) {
			Logger.log("QuoteServerHelper.getQuote() - Exception occurred.", e);
		}
		return result;
	}

	protected boolean doSaveSession() {
		return false;
	}

	@Override
	public String getServletInfo() {
		return "Copyright(c) 2015, Paxcel Technologies Inc.";
	}

	@Override
	public void destroy() {
		super.destroy();
	}

}