package com.b4utrade.web.action;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.b4utrade.bean.InterestRateBean;
import com.b4utrade.bean.OptionItem;
import com.b4utrade.bean.OptionsResultBean;
import com.b4utrade.bean.StockOptionBean;
import com.b4utrade.ejb.FutureOptionDataHandler;
import com.b4utrade.ejb.OptionData;
import com.b4utrade.ejb.OptionDataHandler;
import com.b4utrade.helper.OptionDataBothComparator;
import com.b4utrade.helper.OptionExpirationDateComparator;
import com.b4utrade.helper.StockActivityHelper;
import com.b4utrade.util.B4UConstants;
import com.tacpoint.objectpool.ObjectPoolManager;
import com.tacpoint.util.Environment;
import com.tacpoint.util.Logger;

public class EquityPlusOptionsMontageAction extends B4UTradeDefaultAction {

	static Log log = LogFactory.getLog(EquityPlusOptionsMontageAction.class);
	private static final String[] monthArray = { "January", "February", "March", "April", "May", "June", "July",
			"August", "September", "October", "November", "December" };

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
			response.setContentType("text/xml");
			String type = request.getParameter("TYPE");
			int intType = B4UConstants.OPTION_TYPE_BOTH;
			String expDate = request.getParameter("EXP_DATE");
			String tickerName = request.getParameter("TICKERNAME");
			String chainType = request.getParameter("CHAIN_TYPE");
			int intChainType = B4UConstants.OPTION_CHAIN_TYPE_ALL;
			String strikeCount = request.getParameter("STRIKE_COUNT");
			int intStrikeCount = B4UConstants.OPTION_STRIKE_COUNT_ALL;
			String monthCount = request.getParameter("MONTHS");
			int noOfMonths = 1;

			if (monthCount != null && monthCount.equals("3"))
				noOfMonths = 3;
			if (chainType != null)
				intChainType = Integer.parseInt(chainType);
			if (strikeCount != null)
				intStrikeCount = Integer.parseInt(strikeCount);
			if (type != null)
				intType = Integer.parseInt(type);
			if (tickerName == null)
				tickerName = request.getParameter("NEW_TICKER");
			if (tickerName == null) {
				tickerName = "MSFT";
			}
			double volatility = 0.00;
			double[] interestRate = new double[3];
			double[] expireTime = new double[3];
			Vector displaylist = new Vector<>();
			Vector returnDisplayList = new Vector<>();
			ObjectPoolManager opm = null;
			HashMap temphash = null;
			HashMap expirationDateChain = null;
			InterestRateBean irb = null;

			if (tickerName.startsWith("/")) {
				FutureOptionDataHandler odh = null;
				int numTries = 0;
				while (numTries <= 1) {
					try {
						opm = ObjectPoolManager.getInstance();
						odh = (FutureOptionDataHandler) opm.getObject("FutureOptionDataHandler", 1000);
						if (odh != null) {
							OptionData od = odh.getRemoteInterface();
							if (od != null) {
								temphash = od.getOptionChain(tickerName.toUpperCase());
								expirationDateChain = od.getExpirationChain(tickerName.toUpperCase());
							} else {
								log.info(
										"EquityPlusOptionsMontageAction.execute - Future OptionData remote interface object is null.");
								throw new Exception("Option Data Remote interface is null.");
							}
						} else
							Logger.log("EquityPlusOptionsMontageAction.execute - FutureOptionDataHandler is null.");

						break;
					} catch (Exception exc) {
						Logger.log(
								"EquityPlusOptionsMontageAction.execute - Exception encountered while trying to get Future Option Data.",
								exc);
						numTries++;
						if (odh != null)
							odh.init();
					} finally {
						if (odh != null)
							opm.freeObject("FutureOptionDataHandler", odh);
					}
				}
			} else {
				OptionDataHandler odh = null;
				int numTries = 0;
				while (numTries <= 1) {
					try {
						opm = ObjectPoolManager.getInstance();
						odh = (OptionDataHandler) opm.getObject("OptionDataHandler", 1000);

						if (odh != null) {
							OptionData od = odh.getRemoteInterface();
							if (od != null) {
								Properties props = new Properties();

								try {
									InputStream istream = getClass()
											.getResourceAsStream("/indexticker_conversion.properties");
									props.load(istream);
									istream.close();
									String optIndexTicker = props.getProperty(tickerName.toUpperCase());
									if (optIndexTicker != null) {
										temphash = od.getOptionChain(optIndexTicker);
										expirationDateChain = od.getExpirationChain(optIndexTicker);
									} else {
										temphash = od.getOptionChain(tickerName.toUpperCase());
										expirationDateChain = od.getExpirationChain(tickerName.toUpperCase());
									}
								} catch (Exception e) {
									log.warn(
											"EquityPlusOptionsMontageAction.execute - exception encountered in looking up indexticker_conversion properties file");
									temphash = od.getOptionChain(tickerName.toUpperCase());
									expirationDateChain = od.getExpirationChain(tickerName.toUpperCase());
								}

								irb = od.getInterestRate();
							} else {
								log.info(
										"EquityPlusOptionsMontageAction.execute - OptionData remote interface object is null.");
								throw new Exception("Option Data Remote interface is null.");
							}
						} else
							Logger.log("EquityPlusOptionsMontageAction.execute - OptionDataHandler is null.");

						break;
					} catch (Exception exc) {
						Logger.log(
								"EquityPlusOptionsMontageAction.execute - Exception encountered while trying to get Option Data.",
								exc);
						numTries++;
						if (odh != null)
							odh.init();
					} finally {
						if (odh != null)
							opm.freeObject("OptionDataHandler", odh);
					}
				}
			}
			if ((temphash == null) || (expirationDateChain == null) || expirationDateChain.size() == 0) {
				if (expDate == null)
					expDate = "";
			} else {
				Collection c = temphash.values();
				Collection expirationC = expirationDateChain.values();
				Object soba[] = c.toArray();
				Object expirationArray[] = expirationC.toArray();
				Calendar expirationDate[] = new Calendar[noOfMonths];
				Arrays.sort(expirationArray, new OptionExpirationDateComparator());
				if (expDate == null || expDate.trim().length() < 1)
					expDate = convertDateToDisplayString(((StockOptionBean) expirationArray[0]).getExpirationDate());
				if (noOfMonths > 1)
					getFirstThreeExpirationMonths(expirationArray, expirationDate, expDate);
				else
					getFirstExpirationMonths(expirationArray, expirationDate, expDate);
				for (int i = 0; i < expirationDate.length; i++) {
					if (expirationDate[i] != null) {
						displaylist.add(convertDateToDisplayString(expirationDate[i]));
						log.info("EquityPlusOptionsMontageAction" + convertDateToDisplayString(expirationDate[i]));
						expireTime[i] = getTimeToExpire(expirationDate[i]);
					}
				}
				returnDisplayList.clear();
				for (int i = 0; i < expirationArray.length; i++) {
					String date = convertDateToDisplayString(
							((StockOptionBean) expirationArray[i]).getExpirationDate());
					if (!returnDisplayList.contains(date))
						returnDisplayList.add(date);
				}
				Logger.log("Option Type Requested " + intType);
				if (temphash.size() > 0) {
					Arrays.sort(soba, new OptionDataBothComparator());
				}
				log.info("EquityPlusOptionsMontageAction.execute(): Array option ticker for ticker " + tickerName);
				if (irb != null) {
					for (int i = 0; i < displaylist.size(); i++) {
						if (expireTime[i] >= 1)
							interestRate[i] = irb.get360DayRate();
						else if (expireTime[i] >= .5)
							interestRate[i] = irb.get180DayRate();
						else if (expireTime[i] >= .25)
							interestRate[i] = irb.get90DayRate();
						else
							interestRate[i] = irb.get30DayRate();
					}
				}
				Vector callVector = new Vector<>();
				Vector putVector = new Vector<>();
				for (int j = 0; j < displaylist.size(); j++) {
					String currentDate = (String) displaylist.elementAt(j);
					Vector openCallInterests = new Vector<>();
					Vector optionCallSymbols = new Vector<>();
					Vector optionCallExpirationDate = new Vector<>();
					Vector optionPutExpirationDate = new Vector<>();
					Vector optionCallStrikes = new Vector<>();
					Vector openPutInterests = new Vector<>();
					Vector optionPutSymbols = new Vector<>();
					Vector optionPutStrikes = new Vector<>();

					for (int i = 0; i < soba.length; i++) {
						StockOptionBean tempBean = (StockOptionBean) soba[i];
						if (currentDate.equals(convertDateToDisplayString(tempBean.getExpirationDate()))) {
							if (intType == B4UConstants.OPTION_TYPE_CALL || intType == B4UConstants.OPTION_TYPE_BOTH) {
								if (tempBean.getOptionType() == B4UConstants.OPTION_TYPE_CALL) {
									optionCallExpirationDate.addElement(tempBean.getExpirationDate());
									optionCallStrikes.addElement(tempBean.getStrikePrice());
									openCallInterests.addElement(String.valueOf(tempBean.getOpenInterest()));
									optionCallSymbols.addElement(tempBean.getTicker());
									if (volatility == 0.00)
										volatility = tempBean.getVolatility();
								}
							}
							if (intType == B4UConstants.OPTION_TYPE_PUT || intType == B4UConstants.OPTION_TYPE_BOTH) {
								if (tempBean.getOptionType() == B4UConstants.OPTION_TYPE_PUT) {
									optionPutExpirationDate.addElement(tempBean.getExpirationDate());
									optionPutStrikes.addElement(tempBean.getStrikePrice());
									openPutInterests.addElement(String.valueOf(tempBean.getOpenInterest()));
									optionPutSymbols.addElement(tempBean.getTicker());
									if (volatility == 0.00)
										volatility = tempBean.getVolatility();
								}
							}
						}
					}
					String last = getLastValue(tickerName);
					if (optionCallSymbols.size() > 0) {
						List uniqueStrikes = getStrikeList(optionCallStrikes, last, B4UConstants.OPTION_TYPE_CALL,
								intStrikeCount, intChainType);
						log.info("Call Unique Strikes size " + uniqueStrikes.size());
						OptionsResultBean bean = createResultBean(optionCallExpirationDate, optionCallSymbols,
								optionCallStrikes, openCallInterests, expireTime[j], interestRate[j],
								B4UConstants.OPTION_TYPE_CALL, uniqueStrikes);
						callVector.add(bean);
					}
					if (optionPutSymbols.size() > 0) {
						List uniqueStrikes = getStrikeList(optionPutStrikes, last, B4UConstants.OPTION_TYPE_PUT,
								intStrikeCount, intChainType);
						log.info("Put Unique Strikes size " + uniqueStrikes.size());
						OptionsResultBean bean = createResultBean(optionPutExpirationDate, optionPutSymbols,
								optionPutStrikes, openPutInterests, expireTime[j], interestRate[j],
								B4UConstants.OPTION_TYPE_PUT, uniqueStrikes);
						putVector.add(bean);
					}
				}
				OptionsResultBean mainOrb = new OptionsResultBean();
				mainOrb.setExpiryDateList(returnDisplayList);
				Vector callPuts = new Vector<>();
				callPuts.add(callVector);
				callPuts.add(putVector);
				mainOrb.setOptionList(callPuts);
				mainOrb.setVolatility(volatility);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try (XMLEncoder xmlenc = new XMLEncoder(baos);) {
					xmlenc.writeObject(mainOrb);
				}
				response.getOutputStream().write(baos.toByteArray());
			}
		} catch (Exception e) {
			String msg = "EquityPlusOptionsMontageAction Unable to retrieve data from the database.";
			log.error(msg, e);
		}
		return null;
	}

	private String getLastValue(String ticker) {
		String last = null;
		try {
			String snapUrl = Environment.get("SNAP_URL");
			StringBuffer updatedUrl = new StringBuffer(snapUrl);
			updatedUrl.append("COMPANYID" + "=");
			updatedUrl.append(URLEncoder.encode(ticker, "UTF-8"));
			URL url = new URL(updatedUrl.toString());
			URLConnection urlConnection = url.openConnection();
			Object o = null;
			try (ObjectInputStream objectStream = new ObjectInputStream(urlConnection.getInputStream());) {
				o = objectStream.readObject();
			} catch (ClassNotFoundException e) {
				log.error(e);
			}
			if (o != null) {
				Hashtable snapData = (Hashtable) o;
				StockActivityHelper abean = (StockActivityHelper) snapData.get(ticker);
				if (abean != null && abean.getStockStream() != null) {
					String stream = new String(abean.getStockStream());
					StringTokenizer st = new StringTokenizer(stream, "||");
					st.nextToken();
					st.nextToken();
					last = st.nextToken();
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		log.info("Options- ticker : " + ticker + " last: " + last);
		return last;
	}

	/**
	 * Format the given Calendar
	 *
	 * @param cal Calendar to be formatted.
	 * @return Formatted Calendar date String.
	 */
	private String convertDateToDisplayString(Calendar cal) {
		if (cal == null) {
			log.info("Cal passed was null");
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(monthArray[cal.get(Calendar.MONTH)]);
		sb.append(" ");
		sb.append(cal.get(Calendar.YEAR));
		return (sb.toString());
	}

	private double getTimeToExpire(Calendar expirationTime) {
		Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		double daysLeft = ((expirationTime.getTime().getTime() - currentTime.getTime().getTime()) / 86400000);
		if (daysLeft <= 0)
			daysLeft = (double) 1 / (365 * 24);
		else
			daysLeft /= 365;
		return daysLeft;
	}

	private List getStrikeList(Vector optionStrikes, String lastPrice, int type, int strikeCount, int chainType) {
		log.info("Option Strike size:" + optionStrikes.size() + " chain type " + chainType);
		// double lastVal = Double.parseDouble(last);
		Set set = new HashSet(optionStrikes);
		Vector uniqueStrikes = new Vector(set);
		List<Double> results = uniqueStrikes;
		log.info("Unique Strike entries: " + uniqueStrikes.size());
		if (lastPrice == null)
			return uniqueStrikes;
		Double last = Double.parseDouble(lastPrice);
		Collections.sort(results);
		Double[] arr = (Double[]) results.toArray(new Double[results.size()]);
		switch (chainType) {
		case B4UConstants.OPTION_CHAIN_TYPE_ALL:
			if (strikeCount == 0) {
				log.info("Get All results");
				return uniqueStrikes;
			}
			if (results.contains(last)) {
				log.info("Last in results");
				int lastIndex = results.indexOf(last);
				int startIndex = lastIndex - (strikeCount / 2) + 1;
				log.info("last index = " + lastIndex + " startIndex = " + startIndex + " arr length= " + arr.length
						+ " " + strikeCount);
				if (startIndex < 0)
					return results;
				if (startIndex + strikeCount > arr.length)
					return Arrays.asList(Arrays.copyOfRange(arr, startIndex, arr.length + 1));
				return Arrays.asList(Arrays.copyOfRange(arr, startIndex, startIndex + strikeCount));
			} else {
				log.info("Last not in result");
				int lastIndex = Arrays.binarySearch(arr, last);
				lastIndex = Math.abs(lastIndex);
				int startIndex = lastIndex - (strikeCount / 2);
				log.info("last index = " + lastIndex + " startIndex = " + startIndex + " arr length= " + arr.length
						+ " " + strikeCount);
				if (startIndex < 0)
					return results;
				if (startIndex + strikeCount > arr.length)
					return Arrays.asList(Arrays.copyOfRange(arr, startIndex, arr.length + 1));
				return Arrays.asList(Arrays.copyOfRange(arr, startIndex, startIndex + strikeCount));
			}
		case B4UConstants.OPTION_CHAIN_TYPE_IN_THE_MONEY:
			if (type == B4UConstants.OPTION_TYPE_CALL) {
				Double[] inMoneyCalls;
				if (results.contains(last)) {
					int index = results.indexOf(last);
					log.info("Index:" + index);
					inMoneyCalls = (Arrays.copyOf(arr, index + 1));
				} else {
					int index = Arrays.binarySearch(arr, last);
					index = Math.abs(index);
					log.info("Binary Search Index: " + index);
					inMoneyCalls = (Arrays.copyOf(arr, index - 1));
				}
				if (strikeCount == 0 || inMoneyCalls.length <= strikeCount)
					return Arrays.asList(inMoneyCalls);
				else
					return Arrays.asList(
							Arrays.copyOfRange(inMoneyCalls, inMoneyCalls.length - strikeCount, inMoneyCalls.length));
			} else {
				Double[] inMoneyPuts;
				if (results.contains(last)) {
					int index = results.indexOf(last);
					inMoneyPuts = (Arrays.copyOfRange(arr, index, arr.length));
				} else {
					int index = Arrays.binarySearch(arr, last);
					index = Math.abs(index);
					inMoneyPuts = (Arrays.copyOfRange(arr, index - 1, arr.length));
				}
				if (strikeCount == 0 || inMoneyPuts.length <= strikeCount)
					return Arrays.asList(inMoneyPuts);
				else
					return Arrays.asList(Arrays.copyOf(inMoneyPuts, strikeCount));
			}
		case B4UConstants.OPTION_CHAIN_TYPE_OUT_OF_MONEY:
			if (type == B4UConstants.OPTION_TYPE_CALL) {
				Double[] outMoneyCalls;
				if (results.contains(last)) {
					int index = results.indexOf(last);
					outMoneyCalls = (Arrays.copyOfRange(arr, index, arr.length));
				} else {
					int index = Arrays.binarySearch(arr, last);
					index = Math.abs(index);
					outMoneyCalls = (Arrays.copyOfRange(arr, index - 1, arr.length));
				}
				if (strikeCount == 0 || outMoneyCalls.length <= strikeCount)
					return Arrays.asList(outMoneyCalls);
				else
					return Arrays.asList(Arrays.copyOf(outMoneyCalls, strikeCount));
			} else {
				Double[] outMoneyPuts;
				if (results.contains(last)) {
					int index = results.indexOf(last);
					outMoneyPuts = (Arrays.copyOf(arr, index + 1));
				} else {
					int index = Arrays.binarySearch(arr, last);
					index = Math.abs(index);
					outMoneyPuts = (Arrays.copyOf(arr, index - 1));
				}
				if (strikeCount == 0 || outMoneyPuts.length <= strikeCount)
					return Arrays.asList(outMoneyPuts);
				else
					return Arrays.asList(
							Arrays.copyOfRange(outMoneyPuts, outMoneyPuts.length - strikeCount, outMoneyPuts.length));
			}
		default:
			return uniqueStrikes;
		}
	}

	private OptionsResultBean createResultBean(Vector expirations, Vector optionSymbols, Vector optionStrikes,
			Vector openInterests, double expireTime, double interestRate, int type, List uniqueStrikes) {
		Vector optionItems = new Vector();
		OptionsResultBean orb = new OptionsResultBean();
		int size = optionSymbols.size();
		for (int i = 0; i < size; i++) {
			if ((Double) optionStrikes.elementAt(i) == 0.0)
				continue;
			if (uniqueStrikes.contains(optionStrikes.elementAt(i))) {
				OptionItem oi = orb.makeOptionItem((String) optionSymbols.elementAt(i),
						(Double) optionStrikes.elementAt(i), Long.parseLong((String) openInterests.elementAt(i)));
				oi.setType(type);
				oi.setExpDate((Calendar) expirations.get(i));
				optionItems.addElement(oi);
			}
		}
		orb.setExpireTime(expireTime);
		orb.setInterestRate(interestRate);
		orb.setOptionList(optionItems);
		return orb;
	}

	private void getFirstThreeExpirationMonths(Object[] beans, Calendar c[], String startDate) {
		int j = 0;
		String oldDate = null;
		String oldDate1 = null;
		boolean foundStartDate = false;
		for (int i = 0; i < beans.length; i++) {
			if (!foundStartDate) {
				if (startDate == null || startDate.trim().length() < 1) {
					foundStartDate = true;
					continue;
				}
				if (!startDate.equalsIgnoreCase(
						convertDateToDisplayString(((StockOptionBean) beans[i]).getExpirationDate()))) {
					continue;
				} else {
					foundStartDate = true;
				}
			}
			if (oldDate == null) {
				c[j] = ((StockOptionBean) beans[i]).getExpirationDate();
				oldDate = convertDateToDisplayString(c[j]);
				j++;
			} else {
				if (oldDate1 == null) {
					if (!oldDate.equalsIgnoreCase(
							convertDateToDisplayString(((StockOptionBean) beans[i]).getExpirationDate()))) {
						c[j] = ((StockOptionBean) beans[i]).getExpirationDate();
						oldDate1 = convertDateToDisplayString(c[j]);
						j++;
					}
				} else if (!oldDate1.equalsIgnoreCase(
						convertDateToDisplayString(((StockOptionBean) beans[i]).getExpirationDate()))) {
					c[j] = ((StockOptionBean) beans[i]).getExpirationDate();
					j++;
				}
			}
			if (j > 2)
				break;
		}
	}

	private void getFirstExpirationMonths(Object[] beans, Calendar c[], String startDate) {
		if (startDate == null || startDate.trim().length() < 1) {
			c[0] = ((StockOptionBean) beans[0]).getExpirationDate();
		} else {
			for (int i = 0; i < beans.length; i++) {
				if (!startDate.equalsIgnoreCase(
						convertDateToDisplayString(((StockOptionBean) beans[i]).getExpirationDate()))) {
					continue;
				} else {
					c[0] = ((StockOptionBean) beans[i]).getExpirationDate();
					break;
				}
			}
		}
	}
}
