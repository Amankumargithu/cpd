package com.b4utrade.ejb;

import java.util.Vector;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;



public class RegionalDataBean implements SessionBean {
	
	   public Vector getRegionalExchangeListForTicker(String ticker) {

		     return(new Vector());
		   }

	   
	   public void ejbCreate() {}
	   public void ejbPostCreate() {}
	   public void ejbRemove() {}
	   public void ejbActivate() {}
	   public void ejbPassivate() {}
	   public void setSessionContext(SessionContext sc) {}

	
}
