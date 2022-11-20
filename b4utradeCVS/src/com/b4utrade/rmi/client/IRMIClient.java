package com.b4utrade.rmi.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import com.b4utrade.rmi.server.IRMIServer;
public interface IRMIClient extends Remote
{
	public void register(IRMIServer server) throws RemoteException;
	public void unregister() throws RemoteException;
}