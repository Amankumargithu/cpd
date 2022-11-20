package com.b4utrade.bean;

public class QuoddUserEntitlementBean extends QuoddUserExchangeEntitlementBean {
	private String username;
	private boolean marketMakerEntitlementFlag = false;
	private boolean optionEntitlementFlag = false;
//	private boolean corporateEntitlementFlag = false;
	private boolean dowJonesNewsFlag = false;
//	private boolean firstCallEntitlementFlag = false;
//	private boolean lionShareEntitlementFlag = false;
//	private boolean tsqEntitlementFlag = false;
	private boolean blockTradeEntitlementFlag = false;
	private boolean vwapEntitlementFlag = false;
//	private boolean futuresCMEEntitlementFlag = false;
//	private boolean futuresNYMEXEntitlementFlag = false;
//	private boolean futuresNYBOTEntitlementFlag = false;
//	private boolean futuresCBOTEntitlementFlag = false;

	public QuoddUserEntitlementBean() {
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return (this.username);
	}

	public void setMarketMakerEntitlementFlag(boolean flag) {
		this.marketMakerEntitlementFlag = flag;
	}

	public boolean getMarketMakerEntitlementFlag() {
		return (this.marketMakerEntitlementFlag);
	}

	public void setDowJonesNewsFlag(boolean flag) {
		this.dowJonesNewsFlag = flag;
	}

	public boolean getDowJonesNewsFlag() {
		return (this.dowJonesNewsFlag);
	}

	public void setOptionEntitlementFlag(boolean flag) {
		this.optionEntitlementFlag = flag;
	}

	public boolean getOptionEntitlementFlag() {
		return (this.optionEntitlementFlag);
	}

//	public void setLionShareEntitlementFlag(boolean flag) {
//		this.lionShareEntitlementFlag = flag;
//	}
//
//	public void setFirstCallEntitlementFlag(boolean flag) {
//		this.firstCallEntitlementFlag = flag;
//	}
//
//	public void setTsqEntitlementFlag(boolean flag) {
//		this.tsqEntitlementFlag = flag;
//	}

	public void setBlockTradeEntitlementFlag(boolean flag) {
		this.blockTradeEntitlementFlag = flag;
	}

	public boolean getBlockTradeEntitlementFlag() {
		return (this.blockTradeEntitlementFlag);
	}

	public void setVWAPEntitlementFlag(boolean flag) {
		this.vwapEntitlementFlag = flag;
	}

	public boolean getVWAPEntitlementFlag() {
		return (this.vwapEntitlementFlag);
	}

//	public void setFuturesCMEEntitlementFlag(boolean flag) {
//		this.futuresCMEEntitlementFlag = flag;
//	}
//
//	public void setFuturesNYMEXEntitlementFlag(boolean flag) {
//		this.futuresNYMEXEntitlementFlag = flag;
//	}
//
//	public void setFuturesNYBOTEntitlementFlag(boolean flag) {
//		this.futuresNYBOTEntitlementFlag = flag;
//	}
//
//	public void setFuturesCBOTEntitlementFlag(boolean flag) {
//		this.futuresCBOTEntitlementFlag = flag;
//	}

	public boolean isMarketMakerEntitled() {
		return (this.marketMakerEntitlementFlag);
	}

	public boolean isOptionEntitled() {
		return (this.optionEntitlementFlag);
	}

	public boolean isDowJonesNewsFlag() {
		return (this.dowJonesNewsFlag);
	}

	public boolean isBlockTradeEntitled() {
		return (this.blockTradeEntitlementFlag);
	}

	public boolean isVWAPEntitled() {
		return (this.vwapEntitlementFlag);
	}

//	public void setCorporateEntitlementFlag(boolean corporateEntitlementFlag) {
//		this.corporateEntitlementFlag = corporateEntitlementFlag;
//	}

	public boolean isFutureEntitled(String futureProtocols) {
		if (futureProtocols == null)
			futureProtocols = "131,132,133";
		String[] exchanges = futureProtocols.split(",");
		for (int count = 0; count < exchanges.length; count++) {
			if (isRealTimeEntitled(exchanges[count]))
				return true;
		}
		return false;
	}
}
