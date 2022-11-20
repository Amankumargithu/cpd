package com.b4utrade.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface OptionDataHome extends EJBHome 
{
   OptionData create() throws RemoteException, CreateException;
}

