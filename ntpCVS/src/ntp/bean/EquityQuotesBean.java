package ntp.bean;

import java.io.Serializable;

public class EquityQuotesBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String ticker;
	private Double bidPrice;
	private Double askPrice;
	private Long bidSize;
	private Long askSize;
	private String bidMarketCenter;
	private String askMarketCenter;

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public void setBidPrice(Double bidPrice) {
		this.bidPrice = bidPrice;
	}
	public void setAskPrice(Double askPrice) {
		this.askPrice = askPrice;
	}
	public void setBidSize(Long bidSize) {
		this.bidSize = bidSize;
	}
	public void setAskSize(Long askSize) {
		this.askSize = askSize;
	}
	public void setBidMarketCenter(String bidMarketCenter) {
		this.bidMarketCenter = bidMarketCenter;
	}
	public void setAskMarketCenter(String askMarketCenter) {
		this.askMarketCenter = askMarketCenter;
	}
	public String getTicker() {
		return this.ticker;
	}
	public Double getBidPrice() {
		return this.bidPrice;
	}
	public Double getAskPrice() {
		return this.askPrice;
	}
	public Double setAskPrice() {
		return this.askPrice;
	}
	public Long getBidSize() {
		return this.bidSize;
	}
	public Long getAskSize() {
		return this.askSize;
	}
	public String getBidMarketCenter() {
		return this.bidMarketCenter;
	}
	public String getAskMarketCenter() {
		return this.askMarketCenter;
	}
}
