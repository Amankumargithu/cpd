package com.equitymontage.ejb;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface EquityMontageDataHome extends EJBHome {
	public EquityMontageData create()throws RemoteException, CreateException; 

}
