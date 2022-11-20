package com.b4utrade.ejb;



import java.util.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.tacpoint.util.Environment;

public class RegionalDataHandler 
{

   private InitialContext jndiContext = null;
   private Object reference = null;
   private RegionalDataHome home = null;
   private RegionalData regionalData = null;

   public RegionalDataHandler() {
      
	   init();
   }

   public void init() {
      try {
         Hashtable env = new Hashtable();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
         env.put(Context.PROVIDER_URL, Environment.get("JBOSS_REGIONAL_RMI_URL"));
         
         env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
         jndiContext = new InitialContext(env);
         reference  = jndiContext.lookup("ejbCache/RegionalData");
         
         home = (RegionalDataHome) PortableRemoteObject.narrow(reference, RegionalDataHome.class);
         
         regionalData = home.create();
         
      }
      catch(Exception e) {
	     e.printStackTrace();
         
      }
   }

   public RegionalData getRemoteInterface() {
      return regionalData;
   }

}
