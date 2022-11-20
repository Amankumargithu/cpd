package com.b4utrade.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

import com.b4utrade.bo.MarketScannerBO;

public interface IMarketData extends Remote{

	public HashMap<String, MarketScannerBO> getLatestMarketScannerCache() throws RemoteException;
}
