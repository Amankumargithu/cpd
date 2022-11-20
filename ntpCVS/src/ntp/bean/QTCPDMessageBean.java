package ntp.bean;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

public class QTCPDMessageBean implements Serializable,Cloneable  {

	private static final long serialVersionUID = 2L;

	private String ticker;
	private String systemTicker;
	private double lastPrice = 0.0;
	private double openPrice = 0.0;
	private double percentChange = 0.0;
	private double changePrice = 0.0;
	private double dayHigh = 0.0;
	private double dayLow = 0.0;
	private long volume;
	private long lastTradeVolume;
	private char tickUpDown = '-';
	private long bidSize;
	private long askSize;
	private double bidPrice = 0.0;
	private double askPrice = 0.0;
	private int messageType;
	private double lastClosedPrice = 0.0;
	private boolean isSHO;
	private double extendedLastPrice = 0.0;
	private double extendedPercentChange = 0.0;
	private double extendedChangePrice = 0.0;
	private long extendedLastTradeVolume = 0;
	private String extendedLastTradeTime;
	private String extendedTradeDate;
	private String extendedMarketCenter;
	private boolean extendedUpDownTick;
	private char extendedTickUpDown = '-';
	private int securityType;
	private double lastClosedPriceRange1 = 0.0;
	private double lastClosedPriceRange2 = 0.0;
	private double openPriceRange1 = 0.0;
	private double openPriceRange2 = 0.0;
	private String exchangeCode;
	private String mAskExchangeCode;
	private String mBidExchangeCode;
	private String mMarketCenter;
	private String mVWAP;
	private String tradeDate;
	private String lastTradeTime;
	private String exchangeId;
	private double settlementPrice = 0.0;
	private boolean isHalted = false;
	byte[] bytedata = null;
	private String outputString = null;
	private String limitUpDown;
	private long volumePlus;
	private long bidTime;
	private long askTime;
	private long tradeTime;
	private long preOptionVolume = 0;
	private long regularOptionVolume = 0;
	
	//Greeks Param
	
	private double intrinsicValue = 0d;
	private double timev = 0d;
	private double diff = 0d;
	private double theoV = 0d;
	private double delta = 0d;
	private Double gamma =0d;
	private double theta = 0d;
	private Double ivol = 0d;
	private Double ivolChg = 0d;
	private double equityLast = 0d;
	private int daysToExpire;
	private double strikePrice;
	private long openInterest;
	private String expirationDate;

	private DecimalFormat df = new DecimalFormat("0.0000");

	/**
	 * Constructor set processing flag to false.
	 */
	public QTCPDMessageBean() {}

	public byte[] getByteData() {
		return this.bytedata;
	}

	public String getOutputString() {
		return outputString;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public void setSystemTicker(String ticker) {
		this.systemTicker = ticker;
	}

	public void setHalted(boolean isHalted) {
		this.isHalted = isHalted;
	}

	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}

	public void setPercentChange(double percentChange) {
		this.percentChange = percentChange;
	}

	public void setChangePrice(double changePrice) {
		this.changePrice = changePrice;
	}

	public void setDayHigh(double dayHigh) {
		this.dayHigh = dayHigh;
	}

	public void setDayLow(double dayLow) {
		this.dayLow = dayLow;
	}

	public void setBidSize(long bidSize) {
		this.bidSize = bidSize;
	}

	public void setAskSize(long askSize) {
		this.askSize = askSize;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public void setLastTradeVolume(long lastTradeVolume) {
		this.lastTradeVolume = lastTradeVolume;
	}

	public void setBidPrice(double bidPrice) {
		this.bidPrice = bidPrice;
	}

	public void setAskPrice(double askPrice) {
		this.askPrice = askPrice;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public void setLastClosedPrice(double lastClosedPrice) {
		this.lastClosedPrice = lastClosedPrice;
	}

	public void setSHO(boolean isSHO) {
		this.isSHO = isSHO;
	}

	public void setSecurityType(int securityType) {
		this.securityType = securityType;
	}

	public void setLastClosedPriceRange2(double priceRange) {
		this.lastClosedPriceRange2 = priceRange;
	}

	public void setLastClosedPriceRange1(double priceRange) {
		this.lastClosedPriceRange1 = priceRange;
	}

	public void setOpenPriceRange1(double priceRange) {
		this.openPriceRange1 = priceRange;
	}

	public void setOpenPriceRange2(double priceRange) {
		this.openPriceRange2 = priceRange;
	}

	public void setExchangeCode(String exchangeCode) {
		this.exchangeCode = exchangeCode;
	}

	public void setAskExchangeCode(String askExchangeCode) {
		mAskExchangeCode = askExchangeCode;
	}

	public void setBidExchangeCode(String bidExchangeCode) {
		mBidExchangeCode = bidExchangeCode;
	}

	public void setMarketCenter(String marketCenter) {
		mMarketCenter = marketCenter;
	}

	public void setVWAP(String vwap) {
		mVWAP = vwap;
	}

	public void setLimitUpDown(String limitUpDown) {
		this.limitUpDown = limitUpDown;
	}

	public String getLimitUpDown() {
		return limitUpDown;
	}

	public String getTicker() {
		return ticker;
	}

	public String getSystemTicker() {
		return systemTicker;
	}

	public double getLastPrice() {
		return lastPrice;
	}

	public double getOpenPrice() {
		return openPrice;
	}

	public double getPercentChange() {
		return percentChange;
	}

	public double getChangePrice() {
		return changePrice;
	}

	public double getDayHigh() {
		return dayHigh;
	}

	public double getDayLow() {
		return dayLow;
	}

	public long getBidSize() {
		return bidSize;
	}

	public long getAskSize() {
		return askSize;
	}

	public long getVolume() {
		return volume;
	}

	public long getLastTradeVolume() {
		return lastTradeVolume;
	}

	public double getBidPrice() {
		return bidPrice;
	}

	public double getAskPrice() {
		return askPrice;
	}

	public int getMessageType() {
		return messageType;
	}

	public double getLastClosedPrice() {
		return lastClosedPrice;
	}

	public boolean isSHO() {
		return this.isSHO;
	}

	public int getSecurityType() {
		return (this.securityType);
	}

	public double getLastClosedPriceRange1() {
		return (this.lastClosedPriceRange1);
	}

	public double getLastClosedPriceRange2() {
		return (this.lastClosedPriceRange2);
	}

	public double getOpenPriceRange1() {
		return (this.openPriceRange1);
	}

	public double getOpenPriceRange2() {
		return (this.openPriceRange2);
	}

	public String getExchangeCode() {
		return (this.exchangeCode);
	}

	public String getAskExchangeCode() {
		return mAskExchangeCode;
	}

	public String getBidExchangeCode() {
		return mBidExchangeCode;
	}

	public String getMarketCenter() {
		return mMarketCenter;
	}

	public String getVWAP() {
		return mVWAP;
	}

	public boolean isHalted() {
		return this.isHalted;
	}

	public void createByteData() {
		bytedata = this.toByteArray();
	}

	/**
	 * toString To display the content of the bean as a string
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ticker=");
		sb.append(ticker);
		sb.append(" systemTicker=");
		sb.append(systemTicker);
		sb.append(" lastPrice=");
		sb.append(lastPrice);
		sb.append(" openPrice=");
		sb.append(openPrice);
		sb.append(" PercentChange=");
		sb.append(percentChange);
		sb.append("\n");
		sb.append("changePrice=");
		sb.append(changePrice);
		sb.append(" dayHigh=");
		sb.append(dayHigh);
		sb.append(" dayLow=");
		sb.append(dayLow);
		sb.append(" bidSize=");
		sb.append(bidSize);
		sb.append("\n");
		sb.append("askSize=");
		sb.append(askSize);
		sb.append(" volume=");
		sb.append(volume);
		sb.append(" lastTradeVolume=");
		sb.append(lastTradeVolume);
		sb.append(" bidPrice=");
		sb.append(bidPrice);
		sb.append(" askPrice=");
		sb.append(askPrice);
		sb.append("\n");
		sb.append("messageType=");
		sb.append(messageType);
		sb.append(" lastClosedPrice=");
		sb.append(lastClosedPrice);
		sb.append(" openPriceRange1 = " + openPriceRange1);
		sb.append(" openPriceRange2 = " + openPriceRange2);
		sb.append(" lastClosedPriceRange1 = " + lastClosedPriceRange1);
		sb.append(" lastClosedPriceRange2 = " + lastClosedPriceRange2);
		sb.append(" tradeDate = "+tradeDate);
		sb.append(" lastTradeTime = "+lastTradeTime);
		sb.append(" exchangeCode = "+exchangeCode);
		sb.append(" askExchangeCode = "+mAskExchangeCode);
		sb.append(" bidExchangeCode = "+mBidExchangeCode);
		sb.append(" marketCenter = "+mMarketCenter);
		sb.append(" vwap = "+mVWAP);
		sb.append(" exchangeId = "+exchangeId);
		sb.append(" settlementPrice = "+settlementPrice);

		return (sb.toString());
	}

	/**
	 * toByteArray create the byte array based on the QTMessageBean's content
	 * 
	 * @return byte[]
	 */
	public byte[] toByteArray() {
		createOutputString();
		return (outputString.getBytes());
	}

	/**
	 * createOutputString Create the output String that will be serialized and
	 * send to other part of the system. This is used by the message producer to
	 * create the output string.
	 */
	public void createOutputString() {
		StringBuffer sb = new StringBuffer();
		sb.append(ticker);
		sb.append("||");
		sb.append(systemTicker);
		sb.append("||");
		sb.append(formatFourDecimals(lastPrice));
		sb.append("||");
		sb.append(formatFourDecimals(openPrice));
		sb.append("||");
		sb.append(formatFourDecimals(percentChange));
		sb.append("||");
		sb.append(formatFourDecimals(changePrice));
		sb.append("||");
		sb.append(formatFourDecimals(dayHigh));
		sb.append("||");
		sb.append(formatFourDecimals(dayLow));
		sb.append("||");
		sb.append(bidSize);
		sb.append("||");
		sb.append(askSize);
		sb.append("||");
		sb.append(volume);
		sb.append("||");
		sb.append(lastTradeVolume);
		sb.append("||");
		sb.append(formatFourDecimals(bidPrice));
		sb.append("||");
		sb.append(formatFourDecimals(askPrice));
		sb.append("||");
		sb.append(messageType);
		sb.append("||");
		sb.append(formatFourDecimals(lastClosedPrice));
		sb.append("||");
		sb.append(formatFourDecimals(extendedLastPrice));
		sb.append("||");
		sb.append(formatFourDecimals(extendedPercentChange));
		sb.append("||");
		sb.append(formatFourDecimals(extendedChangePrice));
		sb.append("||");
		sb.append(extendedLastTradeVolume);
		sb.append("||");
		sb.append(extendedMarketCenter != null ? extendedMarketCenter : " ");
		sb.append("||");
		if (extendedUpDownTick) {
			sb.append("T"); // true
		} else {
			sb.append("F"); // false
		}
		sb.append("||");
		if (isSHO) {
			sb.append("T"); // true
		} else {
			sb.append("F"); // false
		}
		sb.append("||");
		sb.append(formatFourDecimals(openPriceRange1));

		sb.append("||");
		sb.append(formatFourDecimals(openPriceRange2));

		sb.append("||");
		sb.append(formatFourDecimals(lastClosedPriceRange1));

		sb.append("||");
		sb.append(formatFourDecimals(lastClosedPriceRange2));

		sb.append("||");
		sb.append(tradeDate != null ? tradeDate : " ");

		sb.append("||");
		sb.append(lastTradeTime != null ? lastTradeTime : " ");

		sb.append("||");
		sb.append(exchangeCode);

		sb.append("||");
		sb.append(mAskExchangeCode != null ? mAskExchangeCode : " ");

		sb.append("||");
		sb.append(mBidExchangeCode != null ? mBidExchangeCode : " ");

		sb.append("||");
		sb.append(mMarketCenter != null? mMarketCenter : " ");

		sb.append("||");
		sb.append(mVWAP != null ? mVWAP : " ");

		sb.append("||");
		sb.append(exchangeId);

		sb.append("||");
		sb.append(settlementPrice);

		//for Halted status
		sb.append("||");

		if (isHalted) {
			sb.append("T"); // true
		} else {
			sb.append("F"); // false
		}
		// For Limit up down
		sb.append("||");
		sb.append(limitUpDown != null ? limitUpDown : " ");
		sb.append("||");
		sb.append(extendedTradeDate != null ? extendedTradeDate : " ");
		sb.append("||");
		sb.append(extendedLastTradeTime != null ? extendedLastTradeTime : " ");
		sb.append("||");
		sb.append(volumePlus);
		sb.append("||");
		sb.append(tickUpDown);
		sb.append("||");
		sb.append(extendedTickUpDown);
		outputString = (sb.toString());
	}
	
	public byte[] toOptionByteArray() {

		StringBuffer sb = new StringBuffer();
		sb.append(ticker);
		sb.append("||");
		sb.append(systemTicker);
		sb.append("||");
		sb.append(formatFourDecimals(lastPrice));
		sb.append("||");
		sb.append(formatFourDecimals(openPrice));
		sb.append("||");
		sb.append(formatFourDecimals(percentChange));
		sb.append("||");
		sb.append(formatFourDecimals(changePrice));
		sb.append("||");
		sb.append(formatFourDecimals(dayHigh));
		sb.append("||");
		sb.append(formatFourDecimals(dayLow));
		sb.append("||");
		sb.append(bidSize);
		sb.append("||");
		sb.append(askSize);
		sb.append("||");
		sb.append(volume);
		sb.append("||");
		sb.append(lastTradeVolume);
		sb.append("||");
		sb.append(formatFourDecimals(bidPrice));
		sb.append("||");
		sb.append(formatFourDecimals(askPrice));
		sb.append("||");
		sb.append(messageType);
		sb.append("||");
		sb.append(formatFourDecimals(lastClosedPrice));
		sb.append("||");
		sb.append(formatFourDecimals(extendedLastPrice));
		sb.append("||");
		sb.append(formatFourDecimals(extendedPercentChange));
		sb.append("||");
		sb.append(formatFourDecimals(extendedChangePrice));
		sb.append("||");
		sb.append(extendedLastTradeVolume);
		sb.append("||");
		sb.append(extendedMarketCenter != null ? extendedMarketCenter : " ");
		sb.append("||");
		if (extendedUpDownTick) {
			sb.append("T"); // true
		} else {
			sb.append("F"); // false
		}
		sb.append("||");
		if (isSHO) {
			sb.append("T"); // true
		} else {
			sb.append("F"); // false
		}
		sb.append("||");
		sb.append(formatFourDecimals(openPriceRange1));

		sb.append("||");
		sb.append(formatFourDecimals(openPriceRange2));

		sb.append("||");
		sb.append(formatFourDecimals(lastClosedPriceRange1));

		sb.append("||");
		sb.append(formatFourDecimals(lastClosedPriceRange2));

		sb.append("||");
		sb.append(tradeDate != null ? tradeDate : " ");

		sb.append("||");
		sb.append(lastTradeTime != null ? lastTradeTime : " ");

		sb.append("||");
		sb.append(exchangeCode);

		sb.append("||");
		sb.append(mAskExchangeCode != null ? mAskExchangeCode : " ");

		sb.append("||");
		sb.append(mBidExchangeCode != null ? mBidExchangeCode : " ");

		sb.append("||");
		sb.append(mMarketCenter != null? mMarketCenter : " ");

		sb.append("||");
		sb.append(mVWAP != null ? mVWAP : " ");

		sb.append("||");
		sb.append(exchangeId);

		sb.append("||");
		sb.append(settlementPrice);

		//for Halted status
		sb.append("||");

		if (isHalted) {
			sb.append("T"); // true
		} else {
			sb.append("F"); // false
		}
		// For Limit up down
		sb.append("||");
		sb.append(limitUpDown != null ? limitUpDown : " ");
		sb.append("||");
		sb.append(extendedTradeDate != null ? extendedTradeDate : " ");
		sb.append("||");
		sb.append(extendedLastTradeTime != null ? extendedLastTradeTime : " ");
		sb.append("||");
		sb.append(volumePlus);
		sb.append("||");
		sb.append(tickUpDown);
		sb.append("||");
		sb.append(extendedTickUpDown);
		
		sb.append("||");
		sb.append(formatFourDecimals(intrinsicValue));
		sb.append("||");
		sb.append(formatFourDecimals(timev));
		sb.append("||");
		sb.append(formatFourDecimals(diff));
		sb.append("||");
		sb.append(formatFourDecimals(theoV));
		sb.append("||");
		sb.append(formatFourDecimals(delta));
		sb.append("||");
		sb.append(gamma == Double.NaN ? "0.0000" : formatFourDecimals(gamma));
		sb.append("||");
		sb.append(formatFourDecimals(theta));
		sb.append("||");
		sb.append(ivol == null ? "0.0000" : formatFourDecimals(ivol));
		sb.append("||");
		sb.append(formatFourDecimals(equityLast));
		sb.append("||");
		sb.append(expirationDate);
		sb.append("||");
		sb.append(daysToExpire);
		sb.append("||");
		sb.append(ivolChg == null ? "0.0000" : formatFourDecimals(ivolChg));
		sb.append("||");
		sb.append(openInterest);
		sb.append("||");
		sb.append(askTime);
		sb.append("||");
		sb.append(bidTime);
		sb.append("||");
		sb.append(tradeTime);

		outputString = (sb.toString());	
		return (outputString.getBytes());
	}

	/**
	 * getQTMessageBean This method is used by consumer to convert the byte
	 * array to the QTMessageBean.
	 * 
	 * @param byte[]
	 *            bytes the byte array of a QTMessageBean
	 * @return QTMessageBean resurrected QTMessageBean
	 */
	public static QTCPDMessageBean getQTMessageBean(byte[] bytes) {
		QTCPDMessageBean bean = new QTCPDMessageBean();
		String beanstr = new String(bytes);
		try {

			StringTokenizer st = new StringTokenizer(beanstr, "||");
			bean.ticker = (st.nextToken()).trim();
			bean.systemTicker = (st.nextToken()).trim();
			bean.lastPrice = Double.parseDouble((st.nextToken()).trim());
			bean.openPrice = Double.parseDouble((st.nextToken()).trim());
			bean.percentChange = Double.parseDouble((st.nextToken()).trim());
			bean.changePrice = Double.parseDouble((st.nextToken()).trim());
			bean.dayHigh = Double.parseDouble((st.nextToken()).trim());
			bean.dayLow = Double.parseDouble((st.nextToken()).trim());
			bean.bidSize = Long.parseLong((st.nextToken()).trim());
			bean.askSize = Long.parseLong((st.nextToken()).trim());
			bean.volume = Long.parseLong((st.nextToken()).trim());
			bean.lastTradeVolume = Long.parseLong((st.nextToken()).trim());
			bean.bidPrice = Double.parseDouble((st.nextToken()).trim());
			bean.askPrice = Double.parseDouble((st.nextToken()).trim());
			bean.messageType = Integer.parseInt((st.nextToken()).trim());
			bean.lastClosedPrice = Double.parseDouble((st.nextToken()).trim());
			bean.extendedLastPrice = Double.parseDouble((st.nextToken()).trim());
			bean.extendedPercentChange = Double.parseDouble((st.nextToken()).trim());
			bean.extendedChangePrice = Double.parseDouble((st.nextToken()).trim());
			bean.extendedLastTradeVolume = Long.parseLong((st.nextToken()).trim());
			bean.extendedMarketCenter =  st.nextToken().trim();
			bean.extendedUpDownTick = false;
			String flag = st.nextToken().trim();
			if (flag.equals("T")) {
				bean.extendedUpDownTick = true;
			}
			bean.isSHO = false;
			flag = st.nextToken().trim();
			if (flag.equals("T")) {
				bean.isSHO = true;
			}
			bean.openPriceRange1 = Double.parseDouble((st.nextToken()).trim());
			bean.openPriceRange2 = Double.parseDouble((st.nextToken()).trim());
			bean.lastClosedPriceRange1 = Double.parseDouble((st.nextToken()).trim());
			bean.lastClosedPriceRange2 = Double.parseDouble((st.nextToken()).trim());
			bean.tradeDate = st.nextToken().trim();
			bean.lastTradeTime = st.nextToken().trim();
			bean.exchangeCode = st.nextToken().trim();
			bean.mAskExchangeCode = st.nextToken().trim();
			bean.mBidExchangeCode = st.nextToken().trim();
			bean.mMarketCenter = st.nextToken().trim();
			bean.mVWAP = st.nextToken().trim();			
			String exId = st.nextToken();
			if (exId != null)
				bean.exchangeId = exId.trim();
			String price = st.nextToken();
			if(price != null)
				bean.setSettlementPrice(Double.parseDouble(price));
			bean.isHalted = false;
			String halted = st.nextToken().trim();
			if (halted.equalsIgnoreCase("T")) {
				bean.isHalted = true;
			}
			bean.limitUpDown = st.nextToken();
			bean.extendedTradeDate  = st.nextToken().trim();
			bean.extendedLastTradeTime = st.nextToken().trim();
			return bean;
		} catch (Exception e) {
			System.out.println(bean.getSystemTicker() + beanstr);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return Returns the lastTradeTime.
	 */
	public String getLastTradeTime() {
		return lastTradeTime;
	}

	/**
	 * @param lastTradeTime
	 *            The lastTradeTime to set.
	 */
	public void setLastTradeTime(String lastTradeTime) {
		this.lastTradeTime = lastTradeTime;
	}

	/**
	 * @return Returns the tradeDate.
	 */
	public String getTradeDate() {
		return tradeDate;
	}

	/**
	 * @param tradeDate
	 *            The tradeDate to set.
	 */
	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}

	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

	public double getSettlementPrice() {
		return settlementPrice;
	}

	public void setSettlementPrice(double settlementPrice) {
		this.settlementPrice = settlementPrice;
	}
	private String formatFourDecimals(double val)
	{
		String value = " ";
		try
		{      
			value = (String)df.format(val);
		}     
		catch(Exception e)
		{
			return(value);
		}
		return(value);
	}

	public double getExtendedLastPrice() {
		return extendedLastPrice;
	}

	public void setExtendedLastPrice(double extendedLastPrice) {
		this.extendedLastPrice = extendedLastPrice;
	}

	public double getExtendedPercentChange() {
		return extendedPercentChange;
	}

	public void setExtendedPercentChange(double extendedPercentChange) {
		this.extendedPercentChange = extendedPercentChange;
	}

	public double getExtendedChangePrice() {
		return extendedChangePrice;
	}

	public void setExtendedChangePrice(double extendedChangePrice) {
		this.extendedChangePrice = extendedChangePrice;
	}

	public long getExtendedLastTradeVolume() {
		return extendedLastTradeVolume;
	}

	public void setExtendedLastTradeVolume(long extendedLastTradeVolume) {
		this.extendedLastTradeVolume = extendedLastTradeVolume;
	}

	public String getExtendedLastTradeTime() {
		return extendedLastTradeTime;
	}

	public void setExtendedLastTradeTime(String extendedLastTradeTime) {
		this.extendedLastTradeTime = extendedLastTradeTime;
	}

	public String getExtendedMarketCenter() {
		return extendedMarketCenter;
	}

	public void setExtendedMarketCenter(String extendedMarketCenter) {
		this.extendedMarketCenter = extendedMarketCenter;
	}

	public Boolean getExtendedUpDownTick() {
		return extendedUpDownTick;
	}

	public void setExtendedUpDownTick(Boolean extendedUpDownTick) {
		this.extendedUpDownTick = extendedUpDownTick;
	}

	public String getExtendedTradeDate() {
		return extendedTradeDate;
	}

	public void setExtendedTradeDate(String extendedTradeDate) {
		this.extendedTradeDate = extendedTradeDate;
	}

	public long getVolumePlus() {
		return volumePlus;
	}

	public void setVolumePlus(long volumePlus) {
		this.volumePlus = volumePlus;
	}

	public char getTickUpDown() {
		return tickUpDown;
	}

	public void setTickUpDown(char tickUpDown) {
		this.tickUpDown = tickUpDown;
	}

	public char getExtendedTickUpDown() {
		return extendedTickUpDown;
	}

	public void setExtendedTickUpDown(char extendedTickUpDown) {
		this.extendedTickUpDown = extendedTickUpDown;
	}
	
	public Object clone()throws CloneNotSupportedException{  
		return super.clone();  
	}

	public long getBidTime() {
		return bidTime;
	}

	public void setBidTime(long bidTime) {
		this.bidTime = bidTime;
	}

	public long getAskTime() {
		return askTime;
	}

	public void setAskTime(long askTime) {
		this.askTime = askTime;
	}

	public long getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(long tradeTime) {
		this.tradeTime = tradeTime;
	}

	public long getPreOptionVolume() {
		return preOptionVolume;
	}

	public void setPreOptionVolume(long preOptionVolume) {
		this.preOptionVolume = preOptionVolume;
	}

	public long getRegularOptionVolume() {
		return regularOptionVolume;
	}

	public void setRegularOptionVolume(long regularOptionVolume) {
		this.regularOptionVolume = regularOptionVolume;
	}

	public double getIntrinsicValue() {
		return intrinsicValue;
	}

	public void setIntrinsicValue(double intrinsicValue) {
		this.intrinsicValue = intrinsicValue;
	}

	public double getTimev() {
		return timev;
	}

	public double getDiff() {
		return diff;
	}

	public double getTheoV() {
		return theoV;
	}

	public double getDelta() {
		return delta;
	}

	public Double getGamma() {
		return gamma;
	}

	public double getTheta() {
		return theta;
	}

	public void setTimev(double timev) {
		this.timev = timev;
	}

	public void setDiff(double diff) {
		this.diff = diff;
	}

	public void setTheoV(double theoV) {
		this.theoV = theoV;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public void setGamma(Double gamma) {
		this.gamma = gamma;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}

	public Double getIvol() {
		return ivol;
	}

	public void setIvol(Double ivol) {
		this.ivol = ivol;
	}

	public double getEquityLast() {
		return equityLast;
	}

	public void setEquityLast(double equityLast) {
		this.equityLast = equityLast;
	}

	public int getDaysToExpire() {
		return daysToExpire;
	}

	public void setDaysToExpire(int daysToExpire) {
		this.daysToExpire = daysToExpire;
	}

	public double getStrikePrice() {
		return strikePrice;
	}

	public void setStrikePrice(double strikePrice) {
		this.strikePrice = strikePrice;
	}

	public long getOpenInterest() {
		return openInterest;
	}

	public void setOpenInterest(long openInterest) {
		this.openInterest = openInterest;
	}

	public String getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Double getIvolChg() {
		return ivolChg;
	}

	public void setIvolChg(Double ivolChg) {
		this.ivolChg = ivolChg;
	} 
}
