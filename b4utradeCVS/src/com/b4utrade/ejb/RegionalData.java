package com.b4utrade.ejb;

import java.util.*;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;



public interface RegionalData extends EJBObject
{

	public Vector getRegionalExchangeListForTicker(String ticker) throws RemoteException;
   
}

