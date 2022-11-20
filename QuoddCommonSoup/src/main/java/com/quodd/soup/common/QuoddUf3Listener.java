package com.quodd.soup.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quodd.bean.BaseBean;
import com.quodd.bean.BboBean;
import com.quodd.bean.InstrumentStatusBean;
import com.quodd.bean.QuoteBean;
import com.quodd.bean.TradeBean;
import com.quodd.bean.TradeUpdateBean;
import com.quodd.bean.ValueUpdateBean;
import com.quodd.common.logger.QuoddLogger;

public abstract class QuoddUf3Listener implements MessageListener {

	protected static final Logger logger = QuoddLogger.getInstance().getLogger();
	protected static final List<Integer> tradeMessageList = Collections
			.unmodifiableList(Arrays.asList(0x70, 0x71, 0x72));
	protected static final List<Integer> tradeUpdateMessageList = Collections
			.unmodifiableList(Arrays.asList(0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79));
	protected static final List<Integer> quoteMessageList = Collections
			.unmodifiableList(Arrays.asList(0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57));
	protected static final List<Integer> bboMessageList = Collections
			.unmodifiableList(Arrays.asList(0x60, 0x61, 0x62, 0x63, 0x64, 0x65));
	protected static final List<Integer> alwaysAllowedMessageList = Collections.unmodifiableList(
			Arrays.asList(0x20, 0x21, 0x22, 0x30, 0x31, 0x33, 0x80, 0x90, 0xB0, 0xB1, 0xB2, 0xC0, 0xC1, 0xC3));

	private static final String CHANNEL_EVENT_START_DAY = "*SOD";
	private static final String CHANNEL_EVENT_END_DAY = "*EOD";

	private final SimpleDateFormat timeformatter = new SimpleDateFormat("HH:mm:ss:SSS");
	protected QuoddSoupClient soupSession;

	protected final TreeMap<Integer, Long> byteMap = new TreeMap<>();
	protected final Set<Integer> allowedMessageSet = new HashSet<>();

	protected final Map<String, Integer> channelDayMap = new HashMap<>();
	protected final Map<String, Integer> channelTimeMap = new HashMap<>();
	protected final Map<String, Integer> channelTimeAsciiMap = new HashMap<>();
	protected final Map<Integer, String> marketCenterLocateMap = new TreeMap<>();
	protected final Map<Integer, String> marketMakerLocateMap = new TreeMap<>();
	protected final Map<Long, String> symbolLocateMap = new ConcurrentHashMap<>();
	protected final Map<Long, Map<Integer, String>> metaValueMap = new ConcurrentHashMap<>();
	protected final Set<Integer> metaType = new LinkedHashSet<>();

	protected final ConcurrentMap<String, Long> protocolChannelMap = new ConcurrentHashMap<>();
	protected boolean isallowedAll = false;

	protected final Map<Integer, TreeMap<Integer, Long>> messageAppandageMap = new HashMap<>();

	private final long datetime;
	protected String name = "listener";
	protected boolean doRun = true;
	private int messageType;

	public QuoddUf3Listener() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		this.datetime = cal.getTimeInMillis();
		this.doRun = true;
	}

	@Override
	public void message(ByteBuffer buffer) throws IOException {
		try {
			if (buffer.remaining() <= 0) {
				updateCountMap(this.byteMap, 0);
				return;
			}
			if (!this.doRun)
				return;
			this.messageType = Byte.toUnsignedInt(buffer.get());
			updateCountMap(this.byteMap, this.messageType);
			// Parse selectively for fast parsing
			if (!this.isallowedAll && !QuoddUf3Listener.alwaysAllowedMessageList.contains(this.messageType)
					&& !this.allowedMessageSet.contains(this.messageType))
				return;
			switch (this.messageType) {
			case 0x20: // System Event
				parseSystemEvent(buffer);
				break;
			case 0x21: // Channel Day
				parseChannelDay(buffer);
				break;
			case 0x22: // Channel Seconds
				parseChannelSeconds(buffer);
				break;
			case 0x30: // Market center Locate
				parseMarketCenterLocate(buffer);
				break;
			case 0x31: // Market maker Locate
				parseMarketMakerLocate(buffer);
				break;
			case 0x33: // Instrument System Locate
				parseInstrumentLocate(buffer);
				break;
			case 0x40: // Order Book Clear
				break;
			case 0x41: // Order Add- Short
				break;
			case 0x42: // Order Add- Long
				break;
			case 0x43: // Order Add- Extended
				break;
			case 0x44: // Order Executed
				break;
			case 0x45: // Order Executed at Price
				break;
			case 0x46: // Order Executed at Price/Size
				break;
			case 0x47: // Order Partial Cancel
				break;
			case 0x48: // Order Deleted
				break;
			case 0x49: // Order Modified
				break;
			case 0x50: // Two Sided Quote - Short
				parseQuote2SidedShort(buffer);
				break;
			case 0x51: // Two Sided Quote - Long
				parseQuote2SidedLong(buffer);
				break;
			case 0x52: // Two Sided Quote - Extended
				parseQuote2SidedExtended(buffer);
				break;
			case 0x53: // One Sided Quote - Short
				parseQuote1SidedShort(buffer);
				break;
			case 0x54: // One Sided Quote - Long
				parseQuote1SidedLong(buffer);
				break;
			case 0x55: // One Sided Quote - Extended
				parseQuote1SidedExtended(buffer);
				break;
			case 0x56: // Two Sided Quote - Fractional
				break;
			case 0x57: // One Sided Quote - Fractional
				break;
			case 0x60: // Two Sided NBBO - Short
				parseBbo2SidedShort(buffer);
				break;
			case 0x61: // Two Sided NBBO - Long
				parseBbo2SidedLong(buffer);
				break;
			case 0x62: // Two Sided NBBO - Extended
				parseBbo2SidedExtended(buffer);
				break;
			case 0x63: // One Sided NBBO - Short
				parseBbo1SidedShort(buffer);
				break;
			case 0x64: // One Sided NBBO - Long
				parseBbo1SidedLong(buffer);
				break;
			case 0x65: // One Sided NBBO - Extended
				parseBbo1SidedExtended(buffer);
				break;
			case 0x70: // Trade - short
				parseTradeShort(buffer);
				break;
			case 0x71: // Trade - long
				parseTradeLong(buffer);
				break;
			case 0x72: // Trade - extended
				parseTradeExtended(buffer);
				break;
			case 0x73: // Trade - Cancel
				parseTradeCancel(buffer);
				break;
			case 0x74: // Trade - Correction
				parseTradeCorrection(buffer);
				break;
			case 0x75: // Prior day Trade report
				parseTradePriorDay(buffer);
				break;
			case 0x76: // Prior day Trade Cancel
				parseTradePriorDayCancel(buffer);
				break;
			case 0x77: // Prior day Trade Correction
				parseTradePriorDayCorrection(buffer);
				break;
			case 0x78: // Trade Fractional Trade
				break;
			case 0x79: // Prior day Trade report - Fractional
				break;
			case 0x80: // Instrument Value Update
				parseValueUpdate(buffer);
				break;
			case 0x81: // Instrument As-of Value Update
				break;
			case 0x82: // Price range Indication
//				parsePriceRangeIndication(buffer);
				break;
			case 0x90: // Trading Action
				parseInstrumentStatus(buffer);
				break;
			case 0x92: // Instrument Imbalance Information
//				parseImbalanceInformation(buffer);
				break;
			case 0xA0: // Price Level Add - Short
				break;
			case 0xA1: // Price Level Add - Long
				break;
			case 0xA2: // Price Level Add - Extended
				break;
			case 0xA3: // Price Level Modify - Short
				break;
			case 0xA4: // Price Level Modify - Long
				break;
			case 0xA5: // Price Level Modify - Extended
				break;
			case 0xA6: // Price Level Delete - Short
				break;
			case 0xA7: // Price Level Delete - Long
				break;
			case 0xA8: // Price Level Delete - Extended
				break;
			case 0xA9: // Price Level Book Clear
				break;
			case 0xAA: // Price Level Add - Fractional
				break;
			case 0xAB: // Price Level Modify - Fractional
				break;
			case 0xB0: // Channel Event
				parseChannelEvent(buffer);
				break;
			case 0xB1: // Market Maker Status
				parseMarketMakerStatus(buffer);
				break;
			case 0xB2: // Administrative Text
				parseAdminText(buffer);
				break;
			case 0xB3: // News Update
				break;
			case 0xB4: // Request for Quote
				break;
			case 0xB5: // Corporate Action Dividend
				break;
			case 0xB6: // Corporate Action Capital Distributions
				break;
			case 0xC0: // Instrument Meta Data
				parseMetaData(buffer);
				break;
			case 0xC1: // Alternate Instrument Symbol
				parseAlternateSymbol(buffer);
				break;
			case 0xC3: // Option Delivery Component
				parseOptionDeliveryComponent(buffer);
				break;
			case 0xC4: // Index Symbol Participation
				break;
			case 0xC5: // Combination Leg
				parseCombinationLeg(buffer);
				break;
			case 0xFF: // End of Snapshot Message
				break;
			default:
				logger.warning(this.name + "," + Integer.toHexString(this.messageType));
				break;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, this.name + " " + e.getMessage(), e);
		}

	}

	private void parseSystemEvent(ByteBuffer buffer) {
		long timestamp = buffer.getLong();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		String eventCode = new String(bytes);
		Date date = new Date(this.datetime + (timestamp / 1_000_000));
		logger.info(() -> this.name + " " + this.timeformatter.format(date) + " " + eventCode + " " + timestamp);
	}

	private void parseChannelDay(ByteBuffer buffer) {
		int protocolId = Byte.toUnsignedInt(buffer.get());
		int channelIndex = Byte.toUnsignedInt(buffer.get());
		int dayOfMonth = Byte.toUnsignedInt(buffer.get());
		this.channelDayMap.put(channelIndex + "_" + protocolId, dayOfMonth);
		logger.info(() -> this.name + " " + protocolId + " " + channelIndex + " " + dayOfMonth);
	}

	private void parseChannelSeconds(ByteBuffer buffer) {
		int protocolId = Byte.toUnsignedInt(buffer.get());
		int channelIndex = Byte.toUnsignedInt(buffer.get());
		int seconds = buffer.getInt();
		this.channelTimeAsciiMap.put(channelIndex + "_" + protocolId, getTimeFromSeconds(seconds));
		this.channelTimeMap.put(channelIndex + "_" + protocolId, seconds);
	}

	private void parseMarketCenterLocate(ByteBuffer buffer) {
		int code = Short.toUnsignedInt(buffer.getShort());
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		String mic = new String(bytes);
		this.marketCenterLocateMap.put(code, mic);
	}

	private void parseMarketMakerLocate(ByteBuffer buffer) {
		int code = Short.toUnsignedInt(buffer.getShort());
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		String mmid = new String(bytes);
		this.marketMakerLocateMap.put(code, mmid);
	}

	private void parseInstrumentLocate(ByteBuffer buffer) {
		long locate = Integer.toUnsignedLong(buffer.getInt());
		byte[] bytes = new byte[2];
		buffer.get(bytes);
		String country = new String(bytes).trim();
		bytes = new byte[3];
		buffer.get(bytes);
		String currency = new String(bytes).trim();
		bytes = new byte[4];
		buffer.get(bytes);
		String mic = new String(bytes).trim();
		int productType = Byte.toUnsignedInt(buffer.get());
		int length = Byte.toUnsignedInt(buffer.get());
		bytes = new byte[length];
		buffer.get(bytes);
		String symbol = new String(bytes).trim();
		this.symbolLocateMap.put(locate, symbol);
		Map<Integer, String> metaMap = this.metaValueMap.get(locate);
		if (metaMap == null) {
			metaMap = new ConcurrentHashMap<>();
			this.metaValueMap.put(locate, metaMap);
		}
		if (!country.isEmpty()) {
			metaMap.put(0xfe, country);
			this.metaType.add(0xfe);
		}
		if (!currency.isEmpty()) {
			metaMap.put(0xfd, currency);
			this.metaType.add(0xfd);
		}
		if (!mic.trim().isEmpty()) {
			metaMap.put(0xfc, mic);
			this.metaType.add(0xfc);
		}
		this.metaType.add(0xfb);
		metaMap.put(0xfb, Integer.toString(productType));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		if (appMap != null && !appMap.isEmpty()) {
			this.metaType.addAll(appMap.keySet());
			metaMap.putAll(appMap);
		}
	}

	private void parseHeader(ByteBuffer buffer, BaseBean bean) throws IOException {
		bean.setProtocolId(Byte.toUnsignedInt(buffer.get()));
		bean.setChannelIndex(Byte.toUnsignedInt(buffer.get()));
		bean.setMessageFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setSequenceNumber(Integer.toUnsignedLong(buffer.getInt()));
		bean.setSeconds(Integer.toUnsignedLong(buffer.getInt()));
	}

	private void parseQuote2SidedShort(ByteBuffer buffer) throws IOException {
		QuoteBean bean = new QuoteBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setMarketMakerLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setBidPrice(Short.toUnsignedLong(buffer.getShort()));
		bean.setBidSize(Short.toUnsignedInt(buffer.getShort()));
		bean.setBidDenominator(2);
		bean.setAskPrice(Short.toUnsignedLong(buffer.getShort()));
		bean.setAskSize(Short.toUnsignedInt(buffer.getShort()));
		bean.setAskDenominator(2);
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setQuoteFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setBidFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setAskFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setQuoteType(QuoteBean.QUOTE_TYPE_COMPLETE);
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		onUpdateQuote(bean);
	}

	private void parseQuote2SidedLong(ByteBuffer buffer) throws IOException {
		QuoteBean bean = new QuoteBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setMarketMakerLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setBidPrice(Integer.toUnsignedLong(buffer.getInt()));
		bean.setBidSize(Short.toUnsignedInt(buffer.getShort()));
		bean.setBidDenominator(4);
		bean.setAskPrice(Integer.toUnsignedLong(buffer.getInt()));
		bean.setAskSize(Short.toUnsignedInt(buffer.getShort()));
		bean.setAskDenominator(4);
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setQuoteFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setBidFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setAskFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setQuoteType(QuoteBean.QUOTE_TYPE_COMPLETE);
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		onUpdateQuote(bean);
	}

	private void parseQuote2SidedExtended(ByteBuffer buffer) throws IOException {
		QuoteBean bean = new QuoteBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setMarketMakerLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setBidDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setBidPrice(buffer.getLong());
		bean.setBidSize(buffer.getInt());
		bean.setAskDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setAskPrice(buffer.getLong());
		bean.setAskSize(buffer.getInt());
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setQuoteFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setBidFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setAskFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setQuoteType(QuoteBean.QUOTE_TYPE_COMPLETE);
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		onUpdateQuote(bean);
	}

	private void parseQuote1SidedShort(ByteBuffer buffer) throws IOException {
		QuoteBean bean = new QuoteBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setMarketMakerLocate(Short.toUnsignedInt(buffer.getShort()));
		long price = Short.toUnsignedLong(buffer.getShort());
		int size = Short.toUnsignedInt(buffer.getShort());
		byte side = buffer.get();
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setQuoteFlag(Byte.toUnsignedInt(buffer.get()));
		int priceFlag = Byte.toUnsignedInt(buffer.get());
		if (side == 'B') {
			bean.setBidPrice(price);
			bean.setBidSize(size);
			bean.setBidDenominator(2);
			bean.setBidFlag(priceFlag);
			bean.setQuoteType(QuoteBean.QUOTE_TYPE_BID);
		} else if (side == 'A') {
			bean.setAskPrice(price);
			bean.setAskSize(size);
			bean.setAskDenominator(2);
			bean.setAskFlag(priceFlag);
			bean.setQuoteType(QuoteBean.QUOTE_TYPE_ASK);
		} else {
			logger.warning(() -> this.name + " Unknown side " + Integer.toHexString(side));
		}
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		onUpdateQuote(bean);
	}

	private void parseQuote1SidedLong(ByteBuffer buffer) throws IOException {
		QuoteBean bean = new QuoteBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setMarketMakerLocate(Short.toUnsignedInt(buffer.getShort()));
		long price = Integer.toUnsignedLong(buffer.getInt());
		int size = Short.toUnsignedInt(buffer.getShort());
		byte side = buffer.get();
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setQuoteFlag(Byte.toUnsignedInt(buffer.get()));
		int priceFlag = Byte.toUnsignedInt(buffer.get());
		if (side == 'B') {
			bean.setBidPrice(price);
			bean.setBidSize(size);
			bean.setBidDenominator(4);
			bean.setBidFlag(priceFlag);
			bean.setQuoteType(QuoteBean.QUOTE_TYPE_BID);
		} else if (side == 'A') {
			bean.setAskPrice(price);
			bean.setAskSize(size);
			bean.setAskDenominator(4);
			bean.setAskFlag(priceFlag);
			bean.setQuoteType(QuoteBean.QUOTE_TYPE_ASK);
		} else {
			logger.warning(() -> this.name + " Unknown side " + Integer.toHexString(side));
		}
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		onUpdateQuote(bean);
	}

	private void parseQuote1SidedExtended(ByteBuffer buffer) throws IOException {
		QuoteBean bean = new QuoteBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setMarketMakerLocate(Short.toUnsignedInt(buffer.getShort()));
		int denominator = Byte.toUnsignedInt(buffer.get());
		long price = buffer.getLong();
		int size = buffer.getInt();
		byte side = buffer.get();
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setQuoteFlag(Byte.toUnsignedInt(buffer.get()));
		int priceFlag = Byte.toUnsignedInt(buffer.get());
		if (side == 'B') {
			bean.setBidPrice(price);
			bean.setBidSize(size);
			bean.setBidDenominator(denominator);
			bean.setBidFlag(priceFlag);
			bean.setQuoteType(QuoteBean.QUOTE_TYPE_BID);
		} else if (side == 'A') {
			bean.setAskPrice(price);
			bean.setAskSize(size);
			bean.setAskDenominator(denominator);
			bean.setAskFlag(priceFlag);
			bean.setQuoteType(QuoteBean.QUOTE_TYPE_ASK);
		} else {
			logger.warning(() -> this.name + " Unknown side " + Integer.toHexString(side));
		}
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		onUpdateQuote(bean);
	}

	private void parseBbo2SidedShort(ByteBuffer buffer) throws IOException {
		BboBean bean = new BboBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setBidMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setBidPrice(Short.toUnsignedLong(buffer.getShort()));
		bean.setBidSize(Short.toUnsignedInt(buffer.getShort()));
		bean.setBidDenominator(2);
		bean.setAskMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setAskPrice(Short.toUnsignedLong(buffer.getShort()));
		bean.setAskSize(Short.toUnsignedInt(buffer.getShort()));
		bean.setAskDenominator(2);
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setBboType(BboBean.BBO_TYPE_COMPLETE);
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateBbo(bean);
	}

	private void parseBbo2SidedLong(ByteBuffer buffer) throws IOException {
		BboBean bean = new BboBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setBidMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setBidPrice(Integer.toUnsignedLong(buffer.getInt()));
		bean.setBidSize(Short.toUnsignedInt(buffer.getShort()));
		bean.setBidDenominator(4);
		bean.setAskMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setAskPrice(Integer.toUnsignedLong(buffer.getInt()));
		bean.setAskSize(Short.toUnsignedInt(buffer.getShort()));
		bean.setAskDenominator(4);
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setBboType(BboBean.BBO_TYPE_COMPLETE);
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateBbo(bean);
	}

	private void parseBbo2SidedExtended(ByteBuffer buffer) throws IOException {
		BboBean bean = new BboBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setBidMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setBidDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setBidPrice(buffer.getLong());
		bean.setBidSize(buffer.getInt());
		bean.setAskMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setAskDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setAskPrice(buffer.getLong());
		bean.setAskSize(buffer.getInt());
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setBboType(BboBean.BBO_TYPE_COMPLETE);
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateBbo(bean);
	}

	private void parseBbo1SidedShort(ByteBuffer buffer) throws IOException {
		BboBean bean = new BboBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		int mcLocate = Short.toUnsignedInt(buffer.getShort());
		long price = Short.toUnsignedLong(buffer.getShort());
		int size = Short.toUnsignedInt(buffer.getShort());
		byte side = buffer.get();
		if (side == 'B') {
			bean.setBidMarketCenterLocate(mcLocate);
			bean.setBidPrice(price);
			bean.setBidSize(size);
			bean.setBidDenominator(2);
			bean.setBboType(BboBean.BBO_TYPE_BID);
		} else if (side == 'A') {
			bean.setAskMarketCenterLocate(mcLocate);
			bean.setAskPrice(price);
			bean.setAskSize(size);
			bean.setAskDenominator(2);
			bean.setBboType(BboBean.BBO_TYPE_ASK);
		} else {
			logger.warning(() -> this.name + " Unknown side " + Integer.toHexString(side));
		}
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setFlag(Byte.toUnsignedInt(buffer.get()));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateBbo(bean);
	}

	private void parseBbo1SidedLong(ByteBuffer buffer) throws IOException {
		BboBean bean = new BboBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		int mcLocate = Short.toUnsignedInt(buffer.getShort());
		long price = Integer.toUnsignedLong(buffer.getInt());
		int size = Short.toUnsignedInt(buffer.getShort());
		byte side = buffer.get();
		if (side == 'B') {
			bean.setBidMarketCenterLocate(mcLocate);
			bean.setBidPrice(price);
			bean.setBidSize(size);
			bean.setBidDenominator(4);
			bean.setBboType(BboBean.BBO_TYPE_BID);
		} else if (side == 'A') {
			bean.setAskMarketCenterLocate(mcLocate);
			bean.setAskPrice(price);
			bean.setAskSize(size);
			bean.setAskDenominator(4);
			bean.setBboType(BboBean.BBO_TYPE_ASK);
		} else {
			logger.warning(() -> this.name + " Unknown side " + Integer.toHexString(side));
		}
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setFlag(Byte.toUnsignedInt(buffer.get()));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateBbo(bean);
	}

	private void parseBbo1SidedExtended(ByteBuffer buffer) throws IOException {
		BboBean bean = new BboBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		int mcLocate = Short.toUnsignedInt(buffer.getShort());
		int denominator = Byte.toUnsignedInt(buffer.get());
		long price = buffer.getLong();
		int size = buffer.getInt();
		byte side = buffer.get();
		if (side == 'B') {
			bean.setBidMarketCenterLocate(mcLocate);
			bean.setBidPrice(price);
			bean.setBidSize(size);
			bean.setBidDenominator(denominator);
			bean.setBboType(BboBean.BBO_TYPE_BID);
		} else if (side == 'A') {
			bean.setAskMarketCenterLocate(mcLocate);
			bean.setAskPrice(price);
			bean.setAskSize(size);
			bean.setAskDenominator(denominator);
			bean.setBboType(BboBean.BBO_TYPE_ASK);
		} else {
			logger.warning(() -> this.name + " Unknown side " + Integer.toHexString(side));
		}
		bean.setCondition(Byte.toUnsignedInt(buffer.get()));
		bean.setFlag(Byte.toUnsignedInt(buffer.get()));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateBbo(bean);
	}

	private void parseTradeShort(ByteBuffer buffer) throws IOException {
		TradeBean bean = new TradeBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradeMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setTradeId(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradePrice(Short.toUnsignedLong(buffer.getShort()));
		bean.setTradeDenominator(2);
		bean.setTradeSize(Short.toUnsignedLong(buffer.getShort()));
		bean.setPriceFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setEligibilityFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setReportFlag(Short.toUnsignedInt(buffer.getShort()));
		bean.setChangeFlag(Byte.toUnsignedInt(buffer.get()));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateTrade(bean);
	}

	private void parseTradeLong(ByteBuffer buffer) throws IOException {
		TradeBean bean = new TradeBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradeMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setTradeId(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradePrice(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradeDenominator(4);
		bean.setTradeSize(Short.toUnsignedLong(buffer.getShort()));
		bean.setPriceFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setEligibilityFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setReportFlag(Short.toUnsignedInt(buffer.getShort()));
		bean.setChangeFlag(Byte.toUnsignedInt(buffer.get()));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateTrade(bean);
	}

	private void parseTradeExtended(ByteBuffer buffer) throws IOException {
		TradeBean bean = new TradeBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradeMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setTradeId(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradeDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setTradePrice(buffer.getLong());
		bean.setTradeSize(buffer.getLong());
		bean.setPriceFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setEligibilityFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setReportFlag(Short.toUnsignedInt(buffer.getShort()));
		bean.setChangeFlag(Byte.toUnsignedInt(buffer.get()));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateTrade(bean);
	}

	private void parseTradeCancel(ByteBuffer buffer) throws IOException {
		TradeUpdateBean bean = new TradeUpdateBean(TradeUpdateBean.TRADE_UPDATE_TYPE_CANCEL);
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradeMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setOriginalTradeId(Integer.toUnsignedLong(buffer.getInt()));
		bean.setOriginalTradeDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalTradePrice(buffer.getLong());
		bean.setOriginalTradeSize(buffer.getLong());
		bean.setOriginalPriceFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalEligibilityFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalReportFlag(Short.toUnsignedInt(buffer.getShort()));
		bean.setCancelFlag(Byte.toUnsignedInt(buffer.get()));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateTradeUpdate(bean);
	}

	private void parseTradeCorrection(ByteBuffer buffer) throws IOException {
		TradeUpdateBean bean = new TradeUpdateBean(TradeUpdateBean.TRADE_UPDATE_TYPE_CORRECTION);
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradeMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setOriginalTradeId(Integer.toUnsignedLong(buffer.getInt()));
		bean.setOriginalTradeDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalTradePrice(buffer.getLong());
		bean.setOriginalTradeSize(buffer.getLong());
		bean.setOriginalPriceFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalEligibilityFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalReportFlag(Short.toUnsignedInt(buffer.getShort()));
		bean.setCorrectedTradeId(Integer.toUnsignedLong(buffer.getInt()));
		bean.setCorrectedTradeDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setCorrectedTradePrice(buffer.getLong());
		bean.setCorrectedTradeSize(buffer.getLong());
		bean.setCorrectedPriceFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setCorrectedEligibilityFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setCorrectedReportFlag(Short.toUnsignedInt(buffer.getShort()));
		bean.setCorrectionFlag(Byte.toUnsignedInt(buffer.get()));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		onUpdateTradeUpdate(bean);
	}

	private void parseTradePriorDay(ByteBuffer buffer) throws IOException {
		TradeUpdateBean bean = new TradeUpdateBean(TradeUpdateBean.TRADE_UPDATE_TYPE_PRIOR_DAY_TRADE);
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradeMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setOriginalTradeId(Integer.toUnsignedLong(buffer.getInt()));
		bean.setOriginalTradeDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalTradePrice(buffer.getLong());
		bean.setOriginalTradeSize(buffer.getLong());
		bean.setOriginalPriceFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalEligibilityFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalReportFlag(Short.toUnsignedInt(buffer.getShort()));
		bean.setPriorTradeMonth(Byte.toUnsignedInt(buffer.get()));
		bean.setPriorTradeDay(Byte.toUnsignedInt(buffer.get()));
		bean.setPriorTradeYear(Short.toUnsignedInt(buffer.getShort()));
		bean.setPriorTradeTimestamp(buffer.getLong());
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		onUpdateTradeUpdate(bean);
	}

	private void parseTradePriorDayCancel(ByteBuffer buffer) throws IOException {
		TradeUpdateBean bean = new TradeUpdateBean(TradeUpdateBean.TRADE_UPDATE_TYPE_PRIOR_DAY_CANCEL);
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradeMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setOriginalTradeId(Integer.toUnsignedLong(buffer.getInt()));
		bean.setOriginalTradeDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalTradePrice(buffer.getLong());
		bean.setOriginalTradeSize(buffer.getLong());
		bean.setOriginalPriceFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalEligibilityFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalReportFlag(Short.toUnsignedInt(buffer.getShort()));
		bean.setPriorTradeMonth(Byte.toUnsignedInt(buffer.get()));
		bean.setPriorTradeDay(Byte.toUnsignedInt(buffer.get()));
		bean.setPriorTradeYear(Short.toUnsignedInt(buffer.getShort()));
		bean.setPriorTradeTimestamp(buffer.getLong());
		bean.setCancelFlag(Byte.toUnsignedInt(buffer.get()));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		onUpdateTradeUpdate(bean);
	}

	private void parseTradePriorDayCorrection(ByteBuffer buffer) throws IOException {
		TradeUpdateBean bean = new TradeUpdateBean(TradeUpdateBean.TRADE_UPDATE_TYPE_PRIOR_DAY_CORRECTION);
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setTradeMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setOriginalTradeId(Integer.toUnsignedLong(buffer.getInt()));
		bean.setOriginalTradeDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalTradePrice(buffer.getLong());
		bean.setOriginalTradeSize(buffer.getLong());
		bean.setOriginalPriceFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalEligibilityFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setOriginalReportFlag(Short.toUnsignedInt(buffer.getShort()));
		bean.setPriorTradeMonth(Byte.toUnsignedInt(buffer.get()));
		bean.setPriorTradeDay(Byte.toUnsignedInt(buffer.get()));
		bean.setPriorTradeYear(Short.toUnsignedInt(buffer.getShort()));
		bean.setPriorTradeTimestamp(buffer.getLong());
		bean.setCorrectedTradeId(Integer.toUnsignedLong(buffer.getInt()));
		bean.setCorrectedTradeDenominator(Byte.toUnsignedInt(buffer.get()));
		bean.setCorrectedTradePrice(buffer.getLong());
		bean.setCorrectedTradeSize(buffer.getLong());
		bean.setCorrectedPriceFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setCorrectedEligibilityFlag(Byte.toUnsignedInt(buffer.get()));
		bean.setCorrectedReportFlag(Short.toUnsignedInt(buffer.getShort()));
		bean.setPriorCorrectedTradeMonth(Byte.toUnsignedInt(buffer.get()));
		bean.setPriorCorrectedTradeDay(Byte.toUnsignedInt(buffer.get()));
		bean.setPriorCorrectedTradeYear(Short.toUnsignedInt(buffer.getShort()));
		bean.setPriorCorrectedTradeTimestamp(buffer.getLong());
		bean.setCorrectionFlag(Byte.toUnsignedInt(buffer.get()));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		onUpdateTradeUpdate(bean);
	}

	private void parseValueUpdate(ByteBuffer buffer) throws IOException {
		ValueUpdateBean bean = new ValueUpdateBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setUpdateFlag(Integer.toUnsignedLong(buffer.getInt()));
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateValueUpdate(bean);
	}

	private void parseInstrumentStatus(ByteBuffer buffer) throws IOException {
		InstrumentStatusBean bean = new InstrumentStatusBean();
		parseHeader(buffer, bean);
		bean.setLocateCode(Integer.toUnsignedLong(buffer.getInt()));
		bean.setMarketCenterLocate(Short.toUnsignedInt(buffer.getShort()));
		bean.setStatusType(Byte.toUnsignedInt(buffer.get()));
		bean.setStatusCode(Byte.toUnsignedInt(buffer.get()));
		bean.setReasonCode(Byte.toUnsignedInt(buffer.get()));
		bean.setStatusFlag(Byte.toUnsignedInt(buffer.get()));
		int length = Byte.toUnsignedInt(buffer.get());
		String reasonDetail = "";
		if (length > 0) {
			byte[] bytes = new byte[length];
			buffer.get(bytes);
			reasonDetail = new String(bytes);
		}
		bean.setReasonDetail(reasonDetail);
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		bean.setAppendageMap(appMap);
		onUpdateInstrumentStatus(bean);
	}

	private void parseChannelEvent(ByteBuffer buffer) throws IOException {
		BaseBean bean = new BaseBean();
		parseHeader(buffer, bean);
		long timestamp = this.channelTimeAsciiMap.get(bean.getChannelIndex() + "_" + bean.getProtocolId());
		timestamp = timestamp * 1_000_000_000 + bean.getSeconds();
		byte[] bytes = new byte[4];
		buffer.get(bytes);
		String eventCode = new String(bytes);
		int mcLoc = Short.toUnsignedInt(buffer.getShort());
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		logger.info(this.name + "," + timestamp + "," + bean.getProtocolId() + "," + bean.getChannelIndex() + ","
				+ bean.getSeconds() + "," + eventCode + "," + mcLoc + "," + appMap);
		if (CHANNEL_EVENT_START_DAY.equals(eventCode)) {
			this.protocolChannelMap.put(bean.getProtocolId() + "." + bean.getChannelIndex(), bean.getSeconds());
		} else if (CHANNEL_EVENT_END_DAY.equals(eventCode)) {
			this.protocolChannelMap.remove(bean.getProtocolId() + "." + bean.getChannelIndex());
			if (this.protocolChannelMap.isEmpty()) {
				this.stopListener(QuoddSoupClient.REASON_FOR_CLOSE_SESSION_END);
			}
		}
	}

	private void parseMarketMakerStatus(ByteBuffer buffer) throws IOException {
		BaseBean bean = new BaseBean();
		parseHeader(buffer, bean);
		long timestamp = this.channelTimeAsciiMap.get(bean.getChannelIndex() + "_" + bean.getProtocolId());
		timestamp = timestamp * 1_000_000_000 + bean.getSeconds();
		long locate = Integer.toUnsignedLong(buffer.getInt());
		int mcLocate = Short.toUnsignedInt(buffer.getShort());
		int mmLocate = Short.toUnsignedInt(buffer.getShort());
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		logger.info(this.name + "," + timestamp + "," + locate + "," + mcLocate + "," + mmLocate + "," + appMap + ","
				+ this.symbolLocateMap.get(locate) + "," + this.marketCenterLocateMap.get(mcLocate) + ","
				+ this.marketMakerLocateMap.get(mmLocate));
	}

	private void parseAdminText(ByteBuffer buffer) throws IOException {
		BaseBean bean = new BaseBean();
		parseHeader(buffer, bean);
		int length = Short.toUnsignedInt(buffer.getShort());
		byte[] bytes = new byte[length];
		buffer.get(bytes);
		String text = new String(bytes);
		logger.info(() -> this.name + " " + bean.getProtocolId() + " " + bean.getChannelIndex() + " "
				+ bean.getSeconds() + " " + text);
	}

	private void parseMetaData(ByteBuffer buffer) {
		long locate = Integer.toUnsignedLong(buffer.getInt());
		Map<Integer, String> metaMap = this.metaValueMap.get(locate);
		if (metaMap == null) {
			metaMap = new HashMap<>();
			this.metaValueMap.put(locate, metaMap);
		}
		Map<Integer, String> appMap = parseAppendageElements(buffer);
		if (appMap != null && !appMap.isEmpty()) {
			this.metaType.addAll(appMap.keySet());
			metaMap.putAll(appMap);
		}
	}

	private void parseAlternateSymbol(ByteBuffer buffer) {
		long locate = Integer.toUnsignedLong(buffer.getInt());
		int symbolType = Byte.toUnsignedInt(buffer.get());
		int length = Byte.toUnsignedInt(buffer.get());
		byte[] bytes = new byte[length];
		buffer.get(bytes);
		String alternateSymbol = new String(bytes);
		Map<Integer, String> metaMap = this.metaValueMap.get(locate);
		if (metaMap == null) {
			metaMap = new HashMap<>();
			this.metaValueMap.put(locate, metaMap);
		}
		this.metaType.add(0xf9);
		this.metaType.add(0xfa);
		metaMap.put(0xf9, Integer.toString(symbolType));
		metaMap.put(0xfa, alternateSymbol);

	}

	private void parseOptionDeliveryComponent(ByteBuffer buffer) {
		long rootCodeLocate = Integer.toUnsignedLong(buffer.getInt());
		long componentIndex = Integer.toUnsignedLong(buffer.getInt());
		long componentTotal = Integer.toUnsignedLong(buffer.getInt());
		long deliverableUnits = Integer.toUnsignedLong(buffer.getInt());
		int settlementMethod = Byte.toUnsignedInt(buffer.get());
		int fixedAmontDenominator = Byte.toUnsignedInt(buffer.get());
		long fixedAmountNumerator = buffer.getLong();
		byte[] bytes = new byte[3];
		buffer.get(bytes);
		String currencyCode = new String(bytes);
		int strikePercent = Short.toUnsignedInt(buffer.getShort());
		long componentSymbolLocate = Integer.toUnsignedLong(buffer.getInt());
		logger.info(this.name + " " + this.symbolLocateMap.get(rootCodeLocate) + "," + componentIndex + ","
				+ componentTotal + "," + deliverableUnits + "," + settlementMethod + "," + fixedAmontDenominator + ","
				+ fixedAmountNumerator + "," + currencyCode + "," + strikePercent + "," + componentSymbolLocate);
	}

	private void parseCombinationLeg(ByteBuffer buffer) {
		int protocol = Byte.toUnsignedInt(buffer.get());
		long locate = Integer.toUnsignedLong(buffer.getInt());
		long legLocate = Integer.toUnsignedLong(buffer.getInt());
		char side = (char) buffer.get();
		int ratioDen = Byte.toUnsignedInt(buffer.get());
		long ratio = buffer.getLong();
		Map<Integer, String> appMap = parseAppendageElements(buffer);
//		logger.info(protocol + "," + symbolLocateMap.get(locate) + "," + symbolLocateMap.get(legLocate) + "," + side
//				+ "," + ratioDen + "," + ratio);
	}

	public abstract void onUpdateQuote(QuoteBean quoteBean);

	public abstract void onUpdateBbo(BboBean bboBean);

	public abstract void onUpdateTrade(TradeBean tradeBean);

	public abstract void onUpdateTradeUpdate(TradeUpdateBean tradeUpdateBean);

	public abstract void onUpdateValueUpdate(ValueUpdateBean valueUpdateBean);

	public abstract void onUpdateInstrumentStatus(InstrumentStatusBean instrumentStatusBean);

	private Map<Integer, String> parseAppendageElements(ByteBuffer buffer) {
		Map<Integer, String> map = new HashMap<>();

		String val = null;
		while (buffer.hasRemaining()) {
			int length = Byte.toUnsignedInt(buffer.get());
			int code = Byte.toUnsignedInt(buffer.get());
			if (code == 0)
				continue;
			int typeCode = Byte.toUnsignedInt(buffer.get());
			switch (code) {
			case 1: // Decimal Short
				int value = Short.toUnsignedInt(buffer.getShort());
				val = setDecimal("" + value, 2);
				map.put(typeCode, val);
				break;
			case 2: // Decimal Long
				long l = Integer.toUnsignedLong(buffer.getInt());
				val = setDecimal("" + l, 4);
				map.put(typeCode, val);
				break;
			case 3: // Decimal Extended
				int den = Byte.toUnsignedInt(buffer.get());
				long num = buffer.getLong();
				val = setDecimal("" + num, den);
				map.put(typeCode, val);
				break;
			case 7: // Byte Value
				int num1 = Byte.toUnsignedInt(buffer.get());
				map.put(typeCode, "" + num1);
				break;
			case 8: // Short Value
				int sht = Short.toUnsignedInt(buffer.getShort());
				map.put(typeCode, "" + sht);
				break;
			case 9: // Int32 Value
				long numint = Integer.toUnsignedLong(buffer.getInt());
				map.put(typeCode, "" + numint);
				break;
			case 10: // Int64 Value
				long ln = buffer.getLong();
				map.put(typeCode, "" + ln);
				break;
			case 15: // String
				byte[] bytes = new byte[length];
				buffer.get(bytes);
				map.put(typeCode, new String(bytes));
				break;
			case 16: // Date
				int month = Byte.toUnsignedInt(buffer.get());
				int day = Byte.toUnsignedInt(buffer.get());
				int year = Short.toUnsignedInt(buffer.getShort());
				long date = (year * 10000) + (month * 100) + day;
				map.put(typeCode, "" + date);
				break;
			case 20: // Fractional Price Need to check pricing calculation
				int den1 = Byte.toUnsignedInt(buffer.get());
				long num2 = buffer.getLong();
				long frac = Integer.toUnsignedLong(buffer.getInt());
				val = setDecimal("" + num2, den1);
				logger.info(typeCode + "," + frac + "," + den1 + "," + num2);
				map.put(typeCode, "" + val);
				break;
			case 21: // Boolean Value
				byte bol = buffer.get();
				val = bol == 0x01 ? "1" : "0";
				map.put(typeCode, val);
				break;
			case 22: // Char Value
				int chr = Byte.toUnsignedInt(buffer.get());
				map.put(typeCode, "" + chr);
				break;
			case 23:
				if (typeCode == 255) {
//					map.put(typeCode, "");
//					logger.info(this.name + "," + typeCode);
					Map<Integer, String> extAppMap = parseAppendageElements(buffer);
					if (extAppMap != null && !extAppMap.isEmpty()) {
						map.putAll(extAppMap);
//						for (Integer key : extAppMap.keySet()) {
//							map.put(key + 255, extAppMap.get(key));
//						}
					}
				} else {
					bytes = new byte[length];
					buffer.get(bytes);
					map.put(typeCode, new String(bytes));
					logger.info(this.name + "," + typeCode + "," + new String(bytes));
				}
				break;
			default:
				logger.warning(this.name + "," + length + "," + code + "," + typeCode);
				for (int i = 0; i < length - 3; i++) {
					if (buffer.hasRemaining())
						buffer.get();
				}
				break;
			}
		}
		Map<Integer, Long> countMap = this.messageAppandageMap.computeIfAbsent(this.messageType, k -> new TreeMap<>());
		for (Integer key : map.keySet())
			countMap.merge(key, 1l, (oldValue, one) -> oldValue + one);
		return map;
	}

	private String setDecimal(String value, int place) {
		StringBuilder sb = new StringBuilder();
		int size = value.length();
		if (place >= size) {
			sb.append("0.");
			while (place > size) {
				sb.append('0');
				place--;
			}
			sb.append(value);
			return sb.toString();
		}
		int t = size - place;
		sb.append(value.substring(0, t));
		sb.append('.');
		sb.append(value.substring(t));
		return sb.toString();
	}

	@Override
	public void setSoupSession(QuoddSoupClient session) {
		this.soupSession = session;
		if (this.soupSession != null)
			this.name = this.soupSession.getName() + "_lis";
	}

	private Integer getTimeFromSeconds(int seconds) {
		int hh = seconds / 3600;
		seconds = seconds - (hh * 3600);
		int mm = seconds / 60;
		seconds = seconds - (mm * 60);
		return ((hh * 10000) + (mm * 100) + seconds);
	}

	@Override
	public void stopListener(String reasonForClose) {
		logger.info("Requested");
		this.doRun = false;
		if (this.soupSession != null)
			this.soupSession.stop(reasonForClose);
	}

	private void updateCountMap(Map<Integer, Long> countMap, int key) {
		countMap.merge(key, 1l, (oldValue, one) -> oldValue + one);
	}

	protected void appendDecimal(StringBuilder builder, long d, int place) {
		long factor = 10;
		long loopF = place;
		while (--loopF > 0)
			factor *= 10;
		int scale = place + 1;
		while (factor * 10 <= d) {
			factor *= 10;
			scale++;
		}
		int decimalZero = 0;
		while (scale > 0) {
			if (scale == place) {
				builder.append('.');
				decimalZero = scale;
			}
			long c = d / factor % 10;
			factor /= 10;
			builder.append((char) ('0' + c));
			scale--;
		}
		decimalZero = decimalZero - 4;
		while (decimalZero < 0) {
			builder.append("0");
			decimalZero++;
		}
	}

	public Map<Integer, Long> getChannelStat() {
		return this.byteMap;
	}

	public Map<Integer, TreeMap<Integer, Long>> getMessageAppendages() {
		return this.messageAppandageMap;
	}

	public Map<Integer, String> getMarketCenter() {
		return this.marketCenterLocateMap;
	}

	public Map<Integer, String> getMarketMaker() {
		return this.marketMakerLocateMap;
	}

	public Map<Long, String> getSymbols() {
		return this.symbolLocateMap;
	}

	public Map<Long, Map<Integer, String>> getMetaData() {
		return this.metaValueMap;
	}
}
