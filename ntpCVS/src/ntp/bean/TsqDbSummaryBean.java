package ntp.bean;

public class TsqDbSummaryBean {

	private String ticker;
	private long tradeId;
	private double openPrice;
	private double dayHigh; 
	private double dayLow;
	private double closePrice;
	private long volume;
	private double vwap;
	
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public long getTradeId() {
		return tradeId;
	}
	public void setTradeId(long tradeId) {
		this.tradeId = tradeId;
	}
	public double getOpenPrice() {
		return openPrice;
	}
	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}
	public double getDayHigh() {
		return dayHigh;
	}
	public void setDayHigh(double dayHigh) {
		this.dayHigh = dayHigh;
	}
	public double getDayLow() {
		return dayLow;
	}
	public void setDayLow(double dayLow) {
		this.dayLow = dayLow;
	}
	public double getClosePrice() {
		return closePrice;
	}
	public void setClosePrice(double closePrice) {
		this.closePrice = closePrice;
	}
	public long getVolume() {
		return volume;
	}
	public void setVolume(long volume) {
		this.volume = volume;
	}
	public double getVwap() {
		return vwap;
	}
	public void setVwap(double vwap) {
		this.vwap = vwap;
	}
}
