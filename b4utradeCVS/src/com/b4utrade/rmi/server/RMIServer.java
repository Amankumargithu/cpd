package com.b4utrade.rmi.server;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.io.InputStream;
//import javax.rmi.PortableRemoteObject;

import com.b4utrade.rmi.client.IRMIClient;
import com.tacpoint.util.Logger;
public class RMIServer extends UnicastRemoteObject implements IRMIServer
{
	/**
	 * A static Map that will have :-
	 * <code>processName</code>, A string of client name ( or process name)
	 * <code>IRMIClient</code>, A reference to client stub
	 */
	static Map processMap = new HashMap();
	
	public RMIServer() throws RemoteException
	{
		super();
		try
		{
			Logger.init();
		}
		catch(Exception e)
		{
			System.out.println("Unable To Initialize Logger...");
		}
	}
	
	public void register(String processName)
			throws RemoteException 
	{
		Logger.log("Found Client : "+processName);
		System.out.println("Found Client : "+processName);
		/**
		 * Put the processName in <code>processMap</code> before Naming.lookup
		 * ,Because the client start is independent to this server,so even if 
		 * client doesnt get register here client is going to be started.
		 * So, if we keep the processName in <code>processMap</code> then it
		 * will help us to remove this process too.
		 */
		processMap.put(processName, null);
		try
		{
			/**
			 * RMI lookup for the client stub reference.
			 */
			IRMIClient client = (IRMIClient)Naming.lookup("rmi://localhost:1097/"+processName);
			/**
			 * Got the stub reference , so register me to the client
			 */
			client.register(this);
			/**
			 * put this client in <code>processMap</code>
			 */
			processMap.put(processName, client);
			Logger.log("Successfully Registered Client : "+processName);
		}
		catch(Exception e)
		{
			Logger.log("ERROR : ",e);
			e.printStackTrace();
		}
	}

	public void unregister(String processName) throws RemoteException
	{
		/**
		 * Get the client stub reference for this <code>processName</code>
		 */
		IRMIClient client = (IRMIClient)processMap.get(processName);
		/**
		 * Check if the stub reference is null ( possible , if registration failed )
		 */
		if(client == null)
		{
			try
			{
				/**
				 * RMI lookup for the client stub reference.
				 */
				IRMIClient c = (IRMIClient)Naming.lookup("rmi://localhost:1097/"+processName);
				/**
				 * Got the stub reference , so register me to the client
				 */
				c.unregister();
				Logger.log("Unregistered : "+processName);
				return;
			}
			catch(Exception e)
			{
				Logger.log("ERROR while Unregistering process : "+processName,e);
				e.printStackTrace();
			}
		}
			//throw new RemoteException(processName + "Could Not be found");
		/**
		 * We have an actual stub reference for this <code>processName</code>
		 * So get that reference.
		 */
		IRMIClient process = (IRMIClient)processMap.get(processName);
		/**
		 * Call <code>unregister()</code> to shutdown the clients.
		 * I called it thrice to make the shutdown sure. (Client waits for 10 seconds
		 * before shutdown , so it is safe to call this method 3 times.
		 */
		process.unregister();
		process.unregister();
		process.unregister();
		Logger.log("Unregistered : "+processName);
	}
	
	public void unregisterAll() throws RemoteException
	{
		/**
		 * Iterate the processMap
		 */
		Iterator processes = processMap.keySet().iterator();
		if(processes != null)
		{
			while(processes.hasNext())
			{
				String processName = null;
				try
				{
					/**
					 * Get the processName
					 */
					processName = (String)processes.next();
					/**
					 * Unregister the processName ( shutdown the client)
					 */
					unregister(processName);
				}
				catch(RemoteException e)
				{
					Logger.log("ERROR while Unregistering pocess : "+processName, e);
					e.printStackTrace();
				}
				catch(Exception e)
				{
					throw new RemoteException("ERROR : "+processName ,e);
				}
			}
		}
	}
	          
	public static void main(String[] args)
	{  
		try
		{	
			System.out.println("Length : "+args.length);
			int port = 1099;
			try
			{
				if(args.length != 0)
				{
					port = Integer.parseInt(args[0]);
					System.out.println("Port : "+port);
					LocateRegistry.createRegistry(port);
					System.out.println("Started Registery");
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			IRMIServer rmiServer = new RMIServer();
			Logger.log("Trying To Start RMIServer...");
			if(args.length != 0 )
				Naming.rebind("//localhost:"+port+"/RMIServer", rmiServer);
			else
				Naming.rebind("RMIServer", rmiServer);
			Logger.log("RMIServer Started...");
			System.out.println("RMIServer Started...");
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	public boolean killProcess(String id) throws RemoteException {
		try
		{
			if(id.indexOf(":") != -1)
			{
				// We are going to restart the server now.
				id = id.substring(0, id.indexOf(":"));
				String command = "kill -9 "+id;
				//System.out.println("id : "+id);
				String[] cmd = { "/bin/sh", "-c", command };
				System.out.println(cmd[2]);
				Process p =  Runtime.getRuntime().exec(cmd);
				
				command = "cd /usr/jboss-3.0.0_tomcat-4.0.3/bin |sh /usr/jboss-3.0.0_tomcat-4.0.3/bin/runCustom.sh ";
				String[] restartCmd = { "/bin/sh", "-c", command };
				System.out.println(restartCmd[2]);
				p =  Runtime.getRuntime().exec(restartCmd);
			}
			else
			{
				String command = "kill -9 "+id;
				//System.out.println("id : "+id);
				String[] cmd = { "/bin/sh", "-c", command };
				System.out.println(cmd[2]);
				Process p =  Runtime.getRuntime().exec(cmd);
			}
		}
		catch(Exception e)
		{
			return false;
		} 
		return true;
	}

	public String showProcesses() throws RemoteException {
		
		try
		{
			String[] cmd = { "/bin/sh", "-c", "ps -ef | grep java" };
			Process p =  Runtime.getRuntime().exec(cmd);
			InputStream o = p.getInputStream();
			//System.out.println(o);
			int i = 0;
			int token = 0;
			boolean process = false;
			StringBuffer sb = new StringBuffer();
			while((i=o.read()) != -1 )
			{
			        sb.append((char)i);
			}
			return sb.toString();
		}
		catch(Exception e)
		{
			return "ERROR";
		}
		
	}

}
