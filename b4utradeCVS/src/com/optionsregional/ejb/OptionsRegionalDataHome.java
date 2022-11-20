package com.optionsregional.ejb;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface OptionsRegionalDataHome extends EJBHome {
	public OptionsRegionalData create()throws RemoteException, CreateException; 

}
