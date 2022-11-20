package com.b4utrade.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface EdgeNewsDataHome extends EJBHome
{
   EdgeNewsData create() throws RemoteException, CreateException;
}

