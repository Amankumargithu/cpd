package com.b4utrade.tsq;

public class TSQIndex {
	
	private boolean forceIndex = false;
	
	public void forceIndex() {
		forceIndex = true;
	}
	
	public void noForceIndex() {
		forceIndex = false;
	}

	public boolean useForceIndex() {
		return forceIndex;
	}
}
