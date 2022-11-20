
package com.b4utrade.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface TSQSubscriberHome extends EJBHome 
{
   TSQSubscriber create() throws RemoteException, CreateException;
}

