package com.b4utrade.bo;

import com.tacpoint.dataaccess.DefaultBusinessObject;

public class PriceCodeBO extends DefaultBusinessObject {
	private int ID = -1;
	private double low;
	private double high;

	public PriceCodeBO(int id, double low, double high) {
		this.ID = id;
		this.high = high;
		this.low = low;
	}

	public int getID() {
		return ID;
	}

	public double getLow() {
		return low;
	}

	public double getHigh() {
		return high;
	}
}
