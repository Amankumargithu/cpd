package ntp.ejb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import ntp.bean.QTCPDMessageBean;
import ntp.logger.NTPLogger;
import ntp.options.cache.OptionsMemoryDB;
import ntp.options.cache.OptionsQTMessageQueue;
import ntp.options.snap.OptionsSnapController;
import ntp.options.streamer.OptionsStreamingController;
import ntp.util.DateTimeUtility;
import ntp.util.OptionsUtility;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.Image;

import com.b4utrade.bean.InterestRateBean;
import com.b4utrade.bean.StockOptionBean;
import com.b4utrade.ejb.OptionData;

@Remote(OptionData.class)
@Stateless(mappedName = "ejbCache/OptionData")
public class OptionsSnapEJB implements OptionData {

	public static final int OPTION_TYPE_CALL = 1;
	public static final int OPTION_TYPE_PUT = 2;
	private SimpleDateFormat expirationformatter = new SimpleDateFormat("yyyyMMdd");
	private Date currentDate = new Date();
	private OptionsUtility utility = OptionsUtility.getInstance();

	@Override
	public HashMap getOptionChain(String ticker) {
		NTPLogger.requestEJB("OpCh");
		long s = System.currentTimeMillis();
		HashMap<String, StockOptionBean> map = new HashMap<>();
		OptionsQTMessageQueue cache = OptionsQTMessageQueue.getInstance();
		String originalSymbol = ticker;
		ticker = utility.formatRootTicker(ticker);
		boolean doReferesh = cache.isMarkedSymbol(ticker);
		if (!doReferesh) {
			map = OptionsMemoryDB.getInstance().getOptionChain(ticker);
			if ((map != null && map.size() > 0)) {
				long t = System.currentTimeMillis();
				NTPLogger.responseEJB("OpCh", t - s);
				return map;
			}
		}
		cache.removeMarkedSymbol(ticker);
		map = new HashMap<>();
		BlobTable blobTable = OptionsSnapController.getInstance().getOptionChain(ticker);
		try {
			if (blobTable != null) {
				int rowCount = blobTable.nRow();
				for (int count = 0; count < rowCount; count++) {
					// For every iteration, create new bean for different contract/systemTicker. And
					// then update MAP
					String descp = blobTable.GetCell(count, 0);
					if (descp.contains("DESCRIPTION")) {
						continue;
					}
					String optionType = blobTable.GetCell(count, 5);
					String systemTicker = blobTable.GetCell(count, 1);
					if (systemTicker.startsWith("P:"))
						continue;
					systemTicker = utility.getEQPlusFormattedTicker(systemTicker);
					if (optionType == null) {
						NTPLogger.dropSymbol(systemTicker, "option type is null");
						continue;
					}
					StockOptionBean sob = new StockOptionBean();
					sob.setTicker(systemTicker);
					sob.setTickerInDB(systemTicker);
					sob.setStrikePrice(Double.parseDouble(blobTable.GetCell(count, 2)));
					sob.setVolatility(cache.getVolatility(originalSymbol));
					sob.setUnderlyingStockTicker(ticker);
					if ((optionType.trim()).equalsIgnoreCase("PUT"))
						sob.setOptionType(OPTION_TYPE_PUT);
					else
						sob.setOptionType(OPTION_TYPE_CALL);
					String expiryDate = blobTable.GetCell(count, 4);
					if (expiryDate != null && expiryDate.length() >= 6) {
						Date expiry = expirationformatter.parse(expiryDate);
						String cDate = expirationformatter.format(currentDate);
						currentDate = expirationformatter.parse(cDate);
						if (expiry.before(currentDate)) {
							NTPLogger.dropSymbol(systemTicker, "expiration date is wrong " + expiryDate);
							continue;
						}
						Calendar c = Calendar.getInstance();
						c.setTime(expiry);
						c.set(Calendar.HOUR_OF_DAY, 23);
						c.set(Calendar.MINUTE, 59);
						c.set(Calendar.SECOND, 0);
						c.set(Calendar.MILLISECOND, 0);
						sob.setExpirationDate(c);
					} else {
						NTPLogger.dropSymbol(systemTicker, "expiration date is null");
						continue;
					}
					sob.setOpenInterest(Long.parseLong(blobTable.GetCell(count, 24)));
					sob.setSecurityDesc(descp);
					OptionsMemoryDB.getInstance().add(sob);
					map.put(sob.getTicker(), sob);
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		long t = System.currentTimeMillis();
		NTPLogger.responseEJB("OpCh", t - s);
		return map;
	}

	@Override
	public HashMap getExpirationChain(String ticker) {
		NTPLogger.requestEJB("OpExp");
		long s = System.currentTimeMillis();
		String formattedTicker = utility.formatRootTicker(ticker);
		HashMap<String, StockOptionBean> conMap = OptionsMemoryDB.getInstance().getExpirationChain(formattedTicker);
		if (conMap == null)
			return new HashMap<String, StockOptionBean>();
		long t = System.currentTimeMillis();
		NTPLogger.responseEJB("OpExp", t - s);
		return conMap;
	}

	@Override
	public InterestRateBean getInterestRate() {
		NTPLogger.requestEJB("OpIr");
		NTPLogger.responseEJB("OpIr", 0);
		return OptionsQTMessageQueue.getInstance().getInterestRatebean();
	}

	@Override
	public LinkedList getOptionQuote(String optionTickers) {
		NTPLogger.requestEJB("OpSp");
		long s = System.currentTimeMillis();
		LinkedList<byte[]> stocks = new LinkedList<>();
		OptionsQTMessageQueue cache = OptionsQTMessageQueue.getInstance();
		try {
			HashSet<String> resubscribeSet = new HashSet<>();
			String[] tickerArray = optionTickers.split(",");
			for (int j = 0; j < tickerArray.length; j++) {
				String opTicker = tickerArray[j];
				if (!utility.validateOptionSymbol(opTicker)) {
					NTPLogger.dropSymbol(opTicker, "Invalid Option Symbol");
					continue;
				}
				if (!cache.isIncorrectSymbol(opTicker)) {
					QTCPDMessageBean bean = cache.getSubsData().get(utility.getEQPlusFormattedTicker(opTicker));
					if (bean != null) {
						bean = utility.evaluateGreeks(opTicker, bean);
						stocks.add(bean.toOptionByteArray());
					} else
						resubscribeSet.add(utility.getUCFormattedTicker(opTicker));
				} else
					NTPLogger.dropSymbol(opTicker, "Incorrect Option Symbol");
			}
			OptionsStreamingController.getInstance().addRequestedSymbols(resubscribeSet);
			if (!resubscribeSet.isEmpty()) {
				HashSet<String> dupSet = new HashSet<>();
				dupSet.addAll(resubscribeSet);
				HashMap<String, Image> images = OptionsSnapController.getInstance().getSyncSnapData(resubscribeSet,
						false);
				HashMap<String, Image> preImages = OptionsSnapController.getInstance().getSyncSnapData(dupSet, true);
				Set<String> tickers = images.keySet();
				for (String ticker : tickers) {
					QTCPDMessageBean qtMessageBean = new QTCPDMessageBean();
					Image image = images.get(ticker);
					Image preImage = preImages.get(ticker);
					qtMessageBean.setTicker(ticker);
					qtMessageBean.setSystemTicker(ticker);
					if (preImage != null) {
						qtMessageBean.setExtendedLastPrice(preImage._trdPrc);
						qtMessageBean.setExtendedLastTradeVolume(preImage._trdVol);
						qtMessageBean.setExtendedChangePrice(preImage._netChg);
						qtMessageBean.setExtendedPercentChange(preImage._pctChg);
						qtMessageBean.setExtendedMarketCenter(utility.getEquityPlusExchangeCode(preImage._trdMktCtr));
						DateTimeUtility.getDefaultInstance().processExtendedDate(qtMessageBean, preImage._trdTime);
						qtMessageBean.setExtendedTickUpDown(preImage._prcTck);
						qtMessageBean.setTradeTime(preImage._trdTime);
					}
					if (image != null) {
						qtMessageBean.setLastPrice(image._trdPrc);
						qtMessageBean.setLastTradeVolume(image._trdVol);
						qtMessageBean.setChangePrice(image._netChg);
						qtMessageBean.setPercentChange(image._pctChg);
						qtMessageBean.setMarketCenter(utility.getEquityPlusExchangeCode(image._trdMktCtr));
						DateTimeUtility.getDefaultInstance().processDate(qtMessageBean, image._trdTime);
						qtMessageBean.setTickUpDown(image._prcTck);
						qtMessageBean.setDayHigh(image._high);
						qtMessageBean.setDayLow(image._low);
						qtMessageBean.setLastClosedPrice(image._close);
						qtMessageBean.setOpenPrice(image._open);
						qtMessageBean.setOpenInterest(image._openVol);
						qtMessageBean.setTradeTime(image._trdTime);
						if (preImage == null) {
							NTPLogger.info("No pre image for " + ticker);
							qtMessageBean.setVolume(image._acVol);
							qtMessageBean.setAskPrice(image._ask);
							qtMessageBean.setAskSize(image._askSize);
							qtMessageBean.setBidPrice(image._bid);
							qtMessageBean.setBidSize(image._bidSize);
							qtMessageBean.setExchangeId("" + image.protocol());
							qtMessageBean.setAskExchangeCode(utility.getEquityPlusExchangeCode(image._askMktCtr));
							qtMessageBean.setBidExchangeCode(utility.getEquityPlusExchangeCode(image._bidMktCtr));
							qtMessageBean.setAskTime(image._askTime);
							qtMessageBean.setBidTime(image._bidTime);
							qtMessageBean.setTradeTime(image._trdTime);
						} else {
							qtMessageBean.setVolume(image._acVol + preImage._acVol);
							qtMessageBean.setTradeTime(image._trdTime);
							if (image._ask == 0 && image._askSize == 0) {
								qtMessageBean.setAskPrice(preImage._ask);
								qtMessageBean.setAskSize(preImage._askSize);
								qtMessageBean
										.setAskExchangeCode(utility.getEquityPlusExchangeCode(preImage._askMktCtr));
								qtMessageBean.setAskTime(preImage._askTime);
							} else {
								qtMessageBean.setAskPrice(image._ask);
								qtMessageBean.setAskSize(image._askSize);
								qtMessageBean.setAskExchangeCode(utility.getEquityPlusExchangeCode(image._askMktCtr));
								qtMessageBean.setAskTime(image._askTime);
							}
							if (image._bid == 0 && image._bidSize == 0) {
								qtMessageBean.setBidPrice(preImage._bid);
								qtMessageBean.setBidSize(preImage._bidSize);
								qtMessageBean
										.setBidExchangeCode(utility.getEquityPlusExchangeCode(preImage._bidMktCtr));
								qtMessageBean.setBidTime(preImage._bidTime);
							} else {
								qtMessageBean.setBidPrice(image._bid);
								qtMessageBean.setBidSize(image._bidSize);
								qtMessageBean.setBidExchangeCode(utility.getEquityPlusExchangeCode(image._bidMktCtr));
								qtMessageBean.setBidTime(image._bidTime);
							}
						}
					} else {
						NTPLogger.info("No image for " + ticker);
						qtMessageBean.setVolume(preImage._acVol);
						qtMessageBean.setAskPrice(preImage._ask);
						qtMessageBean.setAskSize(preImage._askSize);
						qtMessageBean.setBidPrice(preImage._bid);
						qtMessageBean.setBidSize(preImage._bidSize);
						qtMessageBean.setExchangeId("" + preImage.protocol());
						qtMessageBean.setAskExchangeCode(utility.getEquityPlusExchangeCode(preImage._askMktCtr));
						qtMessageBean.setBidExchangeCode(utility.getEquityPlusExchangeCode(preImage._bidMktCtr));
						qtMessageBean.setAskTime(preImage._askTime);
						qtMessageBean.setBidTime(preImage._bidTime);
						qtMessageBean.setTradeTime(preImage._trdTime);
					}
					qtMessageBean = utility.evaluateGreeks(ticker, qtMessageBean);
					stocks.add(qtMessageBean.toOptionByteArray());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long t = System.currentTimeMillis();
		NTPLogger.responseEJB("OpSp", t - s);
		return stocks;

	}

	@Override
	public StockOptionBean getStockOption(String ticker) {
		NTPLogger.requestEJB("OpFun");
		String originalSymbol = ticker;
		long s = System.currentTimeMillis();
		StockOptionBean bean = null;
		try {
			if (!utility.validateOptionSymbol(ticker)) {
				NTPLogger.dropSymbol(ticker, "invalid option format");
				return new StockOptionBean();
			}
			ticker = utility.getEQPlusFormattedTicker(ticker);
			bean = OptionsMemoryDB.getInstance().getFundamentalBean(ticker);
			if (bean == null) {
				bean = new StockOptionBean();
				Image img = OptionsSnapController.getInstance()
						.getOptionsFundamentalData(utility.getUCFormattedTicker(ticker));
				if (img != null) {
					String expiryDate = "" + img.OptionExpiration();
					Date expiry = expirationformatter.parse(expiryDate);
					Calendar c = Calendar.getInstance();
					c.setTime(expiry);
					c.set(Calendar.HOUR_OF_DAY, 23);
					c.set(Calendar.MINUTE, 59);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					bean.setExpirationDate(c);
					String type = img.OptionPutOrCall();
					if (type.equals("CALL"))
						bean.setOptionType(1);
					else
						bean.setOptionType(2);
					try {
						bean.setStrikePrice(img.OptionStrike());
					} catch (Exception e) {
						NTPLogger.warning("Cannot process Option Strike - John Issue");
					}
					bean.setOpenInterest(img.OptionOpenInterest());
					bean.setSecurityDesc(img.Description());
					bean.setTicker(originalSymbol);
					OptionsMemoryDB.getInstance().addStockFundamentalData(ticker, bean);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long t = System.currentTimeMillis();
		NTPLogger.responseEJB("OpFun", t - s);
		return bean;
	}

	@Override
	public HashMap getTSQOptionChain(String rootTicker) {
		NTPLogger.requestEJB("OpTSQ");
		long s = System.currentTimeMillis();
		String formattedTicker = utility.formatRootTicker(rootTicker);
		HashMap<String, String> chainMap = new HashMap<>();
		try {
			BlobTable bt = OptionsSnapController.getInstance().getOptionChain(formattedTicker);
			if (bt != null) {
				int rowCount = bt.nRow();
				for (int count = 0; count < rowCount; count++) {
					String systemTicker = bt.GetCell(count, 1);
					if (systemTicker.startsWith("P:"))
						continue;
					String price = bt.GetCell(count, 0);
					if (price.contains("DESCRIPTION")) {
						continue;
					}
					String expiryDate = bt.GetCell(count, 4);
					if (expiryDate != null && expiryDate.length() >= 6) {
						Date expiry = expirationformatter.parse(expiryDate);
						if (expiry.before(currentDate)) {
							NTPLogger.dropSymbol(systemTicker, "expiration date is wrong");
							continue;
						}
					}
					String ntrd = bt.GetCell(count, 21);
					String tradeCount = chainMap.get(systemTicker);
					try {
						if (tradeCount != null)
							ntrd = "" + (Integer.parseInt(ntrd) + Integer.parseInt(tradeCount));
					} catch (Exception e) {
						e.printStackTrace();
					}
					chainMap.put(systemTicker, ntrd);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			BlobTable bt = OptionsSnapController.getInstance().getPreOptionChain(formattedTicker);
			if (bt != null) {
				int rowCount = bt.nRow();
				for (int count = 0; count < rowCount; count++) {
					String systemTicker = bt.GetCell(count, 1);
					if (systemTicker.startsWith("O:"))
						continue;
					systemTicker = systemTicker.replace("P:", "O:");
					String price = bt.GetCell(count, 0);
					if (price.contains("DESCRIPTION")) {
						continue;
					}
					String expiryDate = bt.GetCell(count, 4);
					if (expiryDate != null && expiryDate.length() >= 6) {
						Date expiry = expirationformatter.parse(expiryDate);
						if (expiry.before(currentDate)) {
							NTPLogger.dropSymbol(systemTicker, "expiration date is wrong");
							continue;
						}
					}
					String ntrd = bt.GetCell(count, 21);
					String tradeCount = chainMap.get(systemTicker);
					try {
						if (tradeCount != null)
							ntrd = "" + (Integer.parseInt(ntrd) + Integer.parseInt(tradeCount));
					} catch (Exception e) {
						e.printStackTrace();
					}
					chainMap.put(systemTicker, ntrd);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long t = System.currentTimeMillis();
		NTPLogger.responseEJB("OpTSQ", t - s);
		return chainMap;
	}

	@Override
	public void subscribe(String id, Object[] keys) {
		NTPLogger.requestEJB("OpSub");
		long s = System.currentTimeMillis();
		HashSet<String> set = new HashSet<>();
		for (Object o : keys)
			set.add((String) o);
		OptionsStreamingController.getInstance().addRequestedSymbols(set);
		long t = System.currentTimeMillis();
		NTPLogger.responseEJB("OpSub", t - s);
	}

	@Override
	public HashMap getSpotSymbolMap() {
		// not applicable, to be deleted after futures separation
		return null;
	}

	@Override
	public ArrayList getFutureChainByDescription(String ticker, int pagingIndex, boolean fitlerOn) {
		// not applicable, to be deleted after futures separation
		return null;
	}

	@Override
	public ArrayList getFutureChainByBaseSymbol(String ticker) {
		// not applicable, to be deleted after futures separation
		return null;
	}

	@Override
	public String updateFutureMapping(String UITicker, String oldMappedTicker, String mappedTicker) {
		// TODO Auto-generated method stub
		return null;
	}

}
