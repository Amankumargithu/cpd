package com.b4utrade.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import com.b4utrade.rmi.client.IRMIClient;
public interface IRMIServer extends Remote
{
	public void register(String processName) throws RemoteException;
	public void unregister(String processName) throws RemoteException;
	public void unregisterAll() throws RemoteException;
	public String showProcesses() throws RemoteException;
	public boolean killProcess(String id) throws RemoteException; 
}
