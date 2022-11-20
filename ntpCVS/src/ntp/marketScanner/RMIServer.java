package ntp.marketScanner;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import ntp.logger.NTPLogger;
import ntp.util.CPDProperty;

import com.b4utrade.bo.MarketScannerBO;
import com.b4utrade.rmi.IMarketData;

public class RMIServer extends UnicastRemoteObject implements IMarketData {

	private CPDProperty property = CPDProperty.getInstance();

	public RMIServer() throws RemoteException {
		super(1099);
	}

	public HashMap<String, MarketScannerBO> getLatestMarketScannerCache() throws RemoteException {
		HashMap<String, MarketScannerBO> tempMap = new HashMap<String, MarketScannerBO>();
		tempMap.putAll(MarketScannerCache.getInstance().getCacheMap());
		return tempMap;
	}

	public void startRMIServer() {
		try {
			String rmiIP = property.getProperty("RMI_IP");
			if(rmiIP == null)
			{
				NTPLogger.missingProperty("RMI_IP");
				rmiIP = "192.168.192.191";
				NTPLogger.defaultSetting("RMI_IP", rmiIP);
			}
			String rmiServer = property.getProperty("RMI_SERVER_NAME");
			if(rmiServer == null)
			{
				NTPLogger.missingProperty("RMI_SERVER_NAME");
				rmiServer = "RMIServer";
				NTPLogger.defaultSetting("RMI_SERVER_NAME", rmiServer);
			}
			int port = 1099;
			NTPLogger.info("RMI Port : " + port);
			LocateRegistry.createRegistry(port);
			NTPLogger.info("Started Registery of RMI Server");
			IMarketData server = new RMIServer();
			Naming.rebind("rmi://" + rmiIP + ":" + port + "/" + rmiServer, server);
			NTPLogger.info("MarketDataRMI Server Started...");
		} catch (Exception e) {
			NTPLogger.warning(e.getMessage());
			e.printStackTrace();
		}
	}
}
