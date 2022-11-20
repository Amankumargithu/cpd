package com.equitymontage.ejb;



import java.util.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.tacpoint.util.Environment;

public class EquityMontageDataHandler 
{

   private InitialContext jndiContext = null;
   private Object reference = null;
   private EquityMontageDataHome home = null;
   private EquityMontageData regionalData = null;

   public EquityMontageDataHandler() {
      
	   init();
   }

   public void init() {
      try {
         Hashtable env = new Hashtable();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
         env.put(Context.PROVIDER_URL, Environment.get("JBOSS_EQUITY_MONTAGE_RMI_URL"));
         
         System.out.println("URL--- " + env.get(Context.PROVIDER_URL));
         
         env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
         jndiContext = new InitialContext(env);
         reference  = jndiContext.lookup("ejbCache/EquityMontageData");
         
         regionalData = (EquityMontageData) PortableRemoteObject.narrow(reference, EquityMontageData.class);
         
         System.out.println("Got regional data fromserver");
      }
      catch(Exception e) {
	     e.printStackTrace();
         
      }
   }

   public EquityMontageData getRemoteInterface() {
      return regionalData;
   }

}
