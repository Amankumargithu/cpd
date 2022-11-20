package com.quodd.task;

import static com.quodd.cpd.AlertCpd.alertCache;
import static com.quodd.cpd.AlertCpd.alertDataProcessorArray;
import static com.quodd.cpd.AlertCpd.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.quodd.bean.AlertActivityWrapper;
import com.quodd.bean.AlertFundamentalBean;
import com.quodd.bean.StockActivity;
import com.quodd.bean.UserAlertDetailBean;
import com.quodd.common.collection.MessageQueue;

public class DatafeedProcessor implements Runnable {
	private static final MessageQueue queue = new MessageQueue();
	private boolean doRun = false;
	private static final String[] mAlertTypes = { UserAlertDetailBean.LAST_OVER_ACTIVITY,
			UserAlertDetailBean.LAST_UNDER_ACTIVITY, UserAlertDetailBean.BID_OVER_ACTIVITY,
			UserAlertDetailBean.BID_UNDER_ACTIVITY, UserAlertDetailBean.ASK_OVER_ACTIVITY,
			UserAlertDetailBean.ASK_UNDER_ACTIVITY, UserAlertDetailBean.PERCENTCHANGE_UP_ACTIVITY,
			UserAlertDetailBean.PERCENTCHANGE_DOWN_ACTIVITY, UserAlertDetailBean.VOLUME_OVER_EQUAL_ACTIVITY,
			UserAlertDetailBean.FIFTYTWOWEEK_HIGH_ACTIVITY, UserAlertDetailBean.FIFTYTWOWEEK_LOW_ACTIVITY,
			UserAlertDetailBean.LAST_EQUAL_ACTIVITY, UserAlertDetailBean.BID_EQUAL_ACTIVITY,
			UserAlertDetailBean.ASK_EQUAL_ACTIVITY, UserAlertDetailBean.LAST_TRADE_VOLUME_EQUAL_ACTIVITY,
			UserAlertDetailBean.LAST_TRADE_VOLUME_OVER_ACTIVITY,
			UserAlertDetailBean.LAST_TRADE_VOLUME_EQUAL_OVER_ACTIVITY };

	private static final String CURRENT_DATE = new SimpleDateFormat("yyyyMMdd").format(new Date());

	public DatafeedProcessor() {
		doRun = true;
	}

	public void addMessage(byte[] message) {
		queue.add(message);
	}

	@Override
	public void run() {
		while (doRun) {
			try {
				List<Object> streamMessages = queue.removeAllWithoutWait();
				if (streamMessages != null && !streamMessages.isEmpty()) {
					streamMessages.forEach(m -> {
						byte[] message = (byte[]) m;
						String stockString = new String(message);
						StockActivity stock = changeStreamToStock(stockString);
						processStock(stock);
					});
				} else {
					TimeUnit.MILLISECONDS.sleep(10);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "exception encountered : " + e.getMessage(), e);
			}
		}
	}

	private void processStock(StockActivity stock) {
		if (stock == null) {
			logger.info("Stock is null");
			return;
		}
		String ticker = "";
		try {
			if ((stock.mTicker) != null) {
				ticker = stock.mTicker.trim().toUpperCase();
			}
			String yearHighPrice = "";
			String yearLowPrice = "";
			String averageDailyVol = "";
			AlertFundamentalBean fundamentalBean = alertCache.getFundamentalBeanByTicker(ticker);
			if (fundamentalBean != null) {
				yearHighPrice = fundamentalBean.getFiftyTwoWeekHigh();
				yearLowPrice = fundamentalBean.getFiftyTwoWeekLow();
				averageDailyVol = fundamentalBean.getAverageDailyVolume();
			}
			// Bid Under, Over, Equal
			if (!stock.mBidPrice.equalsIgnoreCase(StockActivity.NOACTIVITY)) {
				alert(ticker, mAlertTypes[3], stock.mBidPrice);
				alert(ticker, mAlertTypes[2], stock.mBidPrice);
				alert(ticker, mAlertTypes[12], stock.mBidPrice);
			}
			// Ask Under, Over, Equal
			if (!stock.mAskPrice.equalsIgnoreCase(StockActivity.NOACTIVITY)) {
				alert(ticker, mAlertTypes[5], stock.mAskPrice);
				alert(ticker, mAlertTypes[4], stock.mAskPrice);
				alert(ticker, mAlertTypes[13], stock.mAskPrice);
			}
			if (CURRENT_DATE.equalsIgnoreCase(stock.mLastTradeDateGMT)) {
				// Last Under, Over, Equal
				if (!stock.mLastPrice.equalsIgnoreCase(StockActivity.NOACTIVITY)) {
					double lastPrice = Double.parseDouble(stock.mLastPrice);
					alert(ticker, mAlertTypes[1], stock.mLastPrice);
					alert(ticker, mAlertTypes[0], stock.mLastPrice);
					alert(ticker, mAlertTypes[11], stock.mLastPrice);
					// 52 Week Low
					if (!stock.mDayLow.equalsIgnoreCase(StockActivity.NOACTIVITY)
							&& lastPrice <= Double.parseDouble(stock.mDayLow) && yearLowPrice != null
							&& yearLowPrice.length() > 0) {
						try {
							double vYearLowPrice = new Double(yearLowPrice).doubleValue();
							if (lastPrice <= vYearLowPrice) {
								alert(ticker, mAlertTypes[10], null);
							}
						} catch (Exception e) {
							logger.log(Level.WARNING,
									"Invalid double value for yearLowPrice:" + yearLowPrice + "for ticker: " + ticker);
						}
					}
					// 52 Week High
					if (!stock.mDayHigh.equalsIgnoreCase(StockActivity.NOACTIVITY)
							&& lastPrice >= Double.parseDouble(stock.mDayHigh) && yearHighPrice != null
							&& yearHighPrice.length() > 0) {
						try {
							Double vYearHighPriceDouble = new Double(yearHighPrice);
							double vYearHighPrice = vYearHighPriceDouble.doubleValue();
							if (lastPrice >= vYearHighPrice) {
								alert(ticker, mAlertTypes[9], null);
							}
						} catch (Exception e) {
							logger.log(Level.WARNING, "Invalid double value for yearHighPrice:" + " " + yearHighPrice
									+ "for ticker: " + ticker, e);
						}
					}
				}
				// % change
				if (!stock.mPercentChange.equalsIgnoreCase(StockActivity.NOACTIVITY)) {
					if (stock.mPercentChange.indexOf("-") == -1) {
						// % change up
						alert(ticker, mAlertTypes[6], stock.mPercentChange);
					} else {
						// % change down
						alert(ticker, mAlertTypes[7], stock.mPercentChange);
					}
				}

				// LastTradeVolume Over, Equal
				if (!stock.mLastTradeVolume.equalsIgnoreCase(StockActivity.NOACTIVITY)) {
//					alert(ticker, mAlertTypes[14], stock.mLastTradeVolume);
//					alert(ticker, mAlertTypes[15], stock.mLastTradeVolume);
					alert(ticker, mAlertTypes[16], stock.mLastTradeVolume);
				}
				if (!stock.mVolume.equalsIgnoreCase(StockActivity.NOACTIVITY)) {
					long volume = Long.parseLong(stock.mVolume);
					// Volume >20 Million
					if (volume > 20000000) {
						alert(ticker, mAlertTypes[8] + ">1 Million", stock.mVolume);
						alert(ticker, mAlertTypes[8] + ">5 Million", stock.mVolume);
						alert(ticker, mAlertTypes[8] + ">10 Million", stock.mVolume);
						alert(ticker, mAlertTypes[8] + ">20 Million", stock.mVolume);
					}
					// Volume >10 Million
					else if (volume > 10000000) {
						alert(ticker, mAlertTypes[8] + ">1 Million", stock.mVolume);
						alert(ticker, mAlertTypes[8] + ">5 Million", stock.mVolume);
						alert(ticker, mAlertTypes[8] + ">10 Million", stock.mVolume);
					}
					// Volume >5 Million
					else if (volume > 5000000) {
						alert(ticker, mAlertTypes[8] + ">1 Million", stock.mVolume);
						alert(ticker, mAlertTypes[8] + ">5 Million", stock.mVolume);
					}
					// Volume >1 Million
					else if (volume > 1000000) {
						alert(ticker, mAlertTypes[8] + ">1 Million", stock.mVolume);
					}
					if (volume > -1) {
						alert(ticker, mAlertTypes[8], stock.mVolume);
					}
					// Get the daily volume average and compute which alerts to send
					double vAvgDailyVolume = 0;
					if (averageDailyVol != null && averageDailyVol.length() > 0) {
						try {
							if (averageDailyVol.indexOf("K") != -1) {
								Double vAvgDailyVolumeDouble = new Double(
										averageDailyVol.substring(0, averageDailyVol.indexOf("K")));
								vAvgDailyVolume = vAvgDailyVolumeDouble.doubleValue() * 1000;
							} else if (averageDailyVol.indexOf("M") != -1) {
								Double vAvgDailyVolumeDouble = new Double(
										averageDailyVol.substring(0, averageDailyVol.indexOf("M")));
								vAvgDailyVolume = vAvgDailyVolumeDouble.doubleValue() * 1000000;
							} else {
								Double vAvgDailyVolumeDouble = Double.parseDouble(averageDailyVol);
								vAvgDailyVolume = vAvgDailyVolumeDouble.doubleValue();
							}
						} catch (Exception e) {
							logger.log(Level.WARNING, "Invalid double value for averageDailyVol:" + " "
									+ averageDailyVol + "for ticker: " + ticker, e);
						}
						// Volume >5 X AVG
						if (volume > (5 * vAvgDailyVolume)) {
							alert(ticker, mAlertTypes[8] + ">2X AVG", stock.mVolume);
							alert(ticker, mAlertTypes[8] + ">3X AVG", stock.mVolume);
							alert(ticker, mAlertTypes[8] + ">4X AVG", stock.mVolume);
							alert(ticker, mAlertTypes[8] + ">5X AVG", stock.mVolume);
						}
						// Volume >4 X AVG
						else if (volume > (4 * vAvgDailyVolume)) {
							alert(ticker, mAlertTypes[8] + ">2X AVG", stock.mVolume);
							alert(ticker, mAlertTypes[8] + ">3X AVG", stock.mVolume);
							alert(ticker, mAlertTypes[8] + ">4X AVG", stock.mVolume);
						}
						// Volume >3 X AVG
						else if (volume > (3 * vAvgDailyVolume)) {
							alert(ticker, mAlertTypes[8] + ">2X AVG", stock.mVolume);
							alert(ticker, mAlertTypes[8] + ">3X AVG", stock.mVolume);
						}
						// Volume >2 X AVG
						else if (volume > (2 * vAvgDailyVolume)) {
							alert(ticker, mAlertTypes[8] + ">2X AVG", stock.mVolume);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Ticker: " + stock.mTicker, e);
		}
	}

	private void alert(String ticker, String activity, String value) {
		int index = alertCache.findHelperIndex(ticker);
		if (index >= 0 && alertDataProcessorArray.length > index) {
			AlertDataProcessor alertProcessor = alertDataProcessorArray[index];
			if (alertProcessor != null)
				alertProcessor.addMessage(new AlertActivityWrapper(ticker, activity, value));
			else
				logger.log(Level.WARNING, "AlertDataProcessor is null " + index + "," + ticker);
		} else {
			logger.log(Level.WARNING, "Cannot find AlertDataProcessor " + index + "," + ticker);
		}
	}

	private StockActivity changeStreamToStock(String aStream) {
		if (aStream == null || aStream.length() == 0)
			return null;
		StockActivity vStock = new StockActivity();
		try {
			StringTokenizer st = new StringTokenizer(aStream, "||");
			// Don't bother to convert stock type string to an integer
			// since we should only be here if this is a stock stream.
			// skip ticker
			st.nextToken();
			vStock.mTicker = (st.nextToken()).trim();
			vStock.mLastPrice = (st.nextToken());
			vStock.mOpenPrice = (st.nextToken());
			vStock.mPercentChange = (st.nextToken()).trim();
			vStock.mChangePrice = (st.nextToken());
			vStock.mDayHigh = (st.nextToken());
			vStock.mDayLow = (st.nextToken());
			vStock.mBidSize = (st.nextToken()).trim();
			vStock.mAskSize = (st.nextToken()).trim();
			vStock.mVolume = (st.nextToken()).trim();
			vStock.mLastTradeVolume = (st.nextToken()).trim();
			vStock.mBidPrice = (st.nextToken());
			vStock.mAskPrice = (st.nextToken());
			// skip messageType
			st.nextToken();
			vStock.mPreviousPrice = st.nextToken();
			// skip lastTradeYear
			st.nextToken();
			// skip lastTradeMonth
			st.nextToken();
			// skip lastTradeDay
			st.nextToken();
			// skip lastTradeHour
			st.nextToken();
			// skip lastTradeMinute
			st.nextToken();
			// skip lastTradeSecond
			st.nextToken();
			vStock.mUptick = false;
			vStock.mDowntick = false;
			String flag = st.nextToken().trim();
			if (flag.equals("T")) {
				vStock.mUptick = true;
			} else {
				vStock.mDowntick = true;
			}
			// open price range 1
			st.nextToken();
			// open price range 2
			st.nextToken();
			// last close price range 1
			st.nextToken();
			// last close price range 2
			st.nextToken();
			vStock.mLastTradeDateGMT = st.nextToken();
			vStock.mLastTradeTimeGMT = st.nextToken();
			vStock.mExchange = st.nextToken();
			vStock.mAskExchangeCode = st.nextToken();
			vStock.mBidExchangeCode = st.nextToken();
			vStock.mMarketCenter = st.nextToken();
			vStock.mVWAP = st.nextToken();
			vStock.mExchangeID = st.nextToken();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		return vStock;
	}

}
