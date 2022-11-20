package com.b4utrade.rmi.client;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import com.b4utrade.rmi.client.IRMIClient;
import com.b4utrade.rmi.server.*;
import com.tacpoint.backoffice.DataFeedAdminCommand;
import com.tacpoint.util.Logger;
public class RMIClient extends UnicastRemoteObject implements IRMIClient
{
	IRMIServer server;
	
	public RMIClient() throws RemoteException
	{
		super();
	}
	public void unregister() 
	{
		DataFeedAdminCommand.gQuit = true;
		Logger.log("Setting gQuit to true"); 
	}

	public void register(IRMIServer server) throws RemoteException {
		Logger.log("Registering IRMIServer");
		this.server = server;
	} 
	
	public IRMIServer getServer()
	{
		return this.server;
	}
}
