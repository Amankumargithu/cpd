package com.b4utrade.web.action;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.QTMessageBean;
import com.b4utrade.bean.StockOptionBean;
import com.b4utrade.ejb.OptionData;
import com.b4utrade.ejb.OptionDataHandler;
import com.b4utrade.util.B4UConstants;
import com.b4utrade.web.form.OptionSnapsForm;
import com.tacpoint.objectpool.ObjectPoolManager;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class DelayedOptionSnapsAction extends Action {

	static Log log = LogFactory.getLog(DelayedOptionSnapsAction.class);

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
		try {
			Environment.init();
			OptionSnapsForm osForm = (OptionSnapsForm) form;
			if (osForm.getAction() != null && osForm.getAction().equals(OptionSnapsForm.INIT)) {
				GregorianCalendar today = new GregorianCalendar();
				osForm.setFromMonth(today.get(Calendar.MONTH));
				osForm.setFromYear(today.get(Calendar.YEAR));
				today.add(Calendar.MONTH, 1);
				osForm.setToMonth(today.get(Calendar.MONTH));
				osForm.setToYear(today.get(Calendar.YEAR));
				osForm.setAction(OptionSnapsForm.SEARCH);
				return (mapping.findForward("success"));
			}
			String tickerName = osForm.getTicker();
			if (tickerName == null)
				tickerName = "";
			HashMap results = getOptionStrikes(tickerName);
			if (results == null)
				results = new HashMap<>();
			Collection c = results.values();
			Object soba[] = c.toArray();
			double fromStrike = osForm.getFromStrike();
			double toStrike = osForm.getToStrike();
			if (toStrike == 0)
				toStrike = Double.MAX_VALUE;
			ArrayList qualifieds = new ArrayList<>();
			for (int i = 0; i < soba.length; i++) {
				// determine if expiry date falls within user defined date range ...
				StockOptionBean bean = (StockOptionBean) soba[i];
				log.debug("DelayedOptionSnapsAction - returned StockOptionBean : " + bean.toString());
				Calendar expiryDate = bean.getExpirationDate();
				int mo = expiryDate.get(Calendar.MONTH);
				int yr = expiryDate.get(Calendar.YEAR);
				if (osForm.getFromYear() > yr)
					continue;
				if (osForm.getToYear() < yr)
					continue;
				if (osForm.getFromYear() == yr)
					if (osForm.getFromMonth() > mo)
						continue;
				if (osForm.getToYear() == yr)
					if (osForm.getToMonth() < mo)
						continue;
				if (osForm.getType() == B4UConstants.OPTION_TYPE_BOTH || osForm.getType() == bean.getOptionType()) {
					if (fromStrike <= bean.getStrikePrice() && toStrike >= bean.getStrikePrice()) {
						BigDecimal aDecimal = new BigDecimal(bean.getLastPrice());
						BigDecimal bDecimal = new BigDecimal(bean.getLastClosedPrice());
						bean.setIntrinsicValue((aDecimal.subtract(bDecimal)).doubleValue());
						String sym = bean.getTickerInDB();
						// remove the "O:"
						sym = sym.substring(2);
						// find 1st backslash
						int bs1Index = sym.indexOf("\\");
						// find 2nd backslash
						int bs2Index = sym.indexOf("\\", bs1Index + 1);
						String optionSym = sym.substring(0, bs1Index);
						String expirationYear = sym.substring(bs1Index + 1, bs1Index + 3);
						String expirationMonth = sym.substring(bs1Index + 3, bs1Index + 4);
						String expirationDay = sym.substring(bs1Index + 4, bs1Index + 6);
						String strike = sym.substring(bs2Index + 1);
						String t1Symbol = optionSym + expirationYear + expirationDay + expirationMonth + strike;
						bean.setTickerInDB(t1Symbol);
						qualifieds.add(bean);
						log.debug("DelayedOptionSnapsAction - adding option sym : " + bean.getTicker() + " strike : "
								+ bean.getStrikePrice());
					}
				}
			}
			pullOptionSnaps(qualifieds);
			osForm.setOptionSnaps(qualifieds);
			return (mapping.findForward("success"));
		} catch (Exception e) {
			String msg = "DelayedOptionSnapsAction - unable to retrieve option data.";
			log.error(msg, e);
			return null;
		}
	}

	private HashMap getOptionStrikes(String symbol) throws Exception {
		ObjectPoolManager opm = null;
		OptionDataHandler odh = null;
		HashMap results = null;
		log.debug("DelayedOptionSnapsAction - attempting to pull option data for root symbol : " + symbol);
		int numTries = 0;
		while (numTries <= 1) {
			try {
				opm = ObjectPoolManager.getInstance();
				odh = (OptionDataHandler) opm.getObject("OptionDataHandler", 1000);
				if (odh != null) {
					OptionData od = odh.getRemoteInterface();
					if (od != null) {
						try {
							results = od.getOptionChain(symbol.toUpperCase());
						} catch (Exception e) {
							log.warn("DelayedOptionSnapsAction.execute - "
									+ "exception encountered in looking up indexticker_conversion properties file", e);
							results = od.getOptionChain(symbol.toUpperCase());
						}
					} else {
						log.error("DelayedOptionSnapsAction.execute - OptionData remote interface object is null.");
						throw new Exception("Option Data Remote interface is null.");
					}
				} else
					log.error("DelayedOptionSnapsAction.execute - OptionDataHandler is null.");
				break;
			} catch (Exception exc) {
				Logger.log("DelayedOptionSnapsAction.execute - Exception encountered while trying to get Option Data.",
						exc);
				numTries++;
				if (odh != null)
					odh.init();
			} finally {
				if (odh != null)
					opm.freeObject("OptionDataHandler", odh);
			}
		}
		return results;
	}

	private void pullOptionSnaps(ArrayList options) throws Exception {
		ObjectPoolManager opm = null;
		OptionDataHandler dodh = null;
		LinkedList results = null;
		// format ticker string as param to OptionData object ...
		Iterator it = options.iterator();
		StringBuffer sb = new StringBuffer();
		while (it.hasNext()) {
			StockOptionBean sob = (StockOptionBean) it.next();
			sb.append(sob.getTicker() + ",");
		}
		int numTries = 0;
		while (numTries <= 1) {
			try {
				opm = ObjectPoolManager.getInstance();
				dodh = (OptionDataHandler) opm.getObject("DelayedOptionDataHandler", 1000);

				if (dodh != null) {
					OptionData od = dodh.getRemoteInterface();
					if (od != null) {
						results = od.getOptionQuote(sb.toString());
					} else {
						log.error("DelayedOptionSnapsAction.execute - OptionData remote interface object is null.");
						throw new Exception("Option Data Remote interface is null.");
					}
				} else
					log.error("DelayedOptionSnapsAction.execute - OptionDataHandler is null.");
				break;
			} catch (Exception exc) {
				Logger.log("DelayedOptionSnapsAction.execute - Exception encountered while trying to get Option Data.",
						exc);
				numTries++;
				if (dodh != null)
					dodh.init();
			} finally {
				if (dodh != null)
					opm.freeObject("DelayedOptionDataHandler", dodh);
			}
		}
		// now we merge the snap quotes with the StockOptionBeans ...
		it = results.iterator();
		HashMap quotes = new HashMap<>();
		while (it.hasNext()) {
			byte[] quoteBytes = (byte[]) it.next();
			QTMessageBean quote = QTMessageBean.getQTMessageBean(quoteBytes);
			if (quote != null) {
				log.debug("Adding QTMessageBean to map : " + quote.getSystemTicker());
				quotes.put(quote.getSystemTicker(), quote);
			}
		}
		it = options.iterator();
		while (it.hasNext()) {
			StockOptionBean sob = (StockOptionBean) it.next();
			log.debug("Looking up QTMessageBean in map with ticker :  " + sob.getTicker());
			QTMessageBean quote = (QTMessageBean) quotes.get(sob.getTicker());
			if (quote != null)
				merge(sob, quote);
			else
				log.error("No QTMessage bean found for ticker : " + sob.getTicker());
		}
	}

	private void merge(StockOptionBean stockOptionBean, QTMessageBean qtMessageBean) {
		stockOptionBean.setBidPrice(qtMessageBean.getBidPrice());
		stockOptionBean.setAskPrice(qtMessageBean.getAskPrice());
		stockOptionBean.setLastPrice(qtMessageBean.getLastPrice());
		stockOptionBean.setVolume(qtMessageBean.getVolume());
	}
}
