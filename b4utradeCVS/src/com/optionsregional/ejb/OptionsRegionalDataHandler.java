package com.optionsregional.ejb;



import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.tacpoint.util.Environment;

public class OptionsRegionalDataHandler 
{

   private InitialContext jndiContext = null;
   private Object reference = null;
   private OptionsRegionalData regionalData = null;

   public OptionsRegionalDataHandler() {
      
	   init();
   }

   public void init() {
      try {
         Hashtable env = new Hashtable();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
         env.put(Context.PROVIDER_URL, Environment.get("JBOSS_OPTIONS_REGIONAL_RMI_URL"));
         
         env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
         jndiContext = new InitialContext(env);
         reference  = jndiContext.lookup("ejbCache/OptionsRegionalData");
         
         regionalData = (OptionsRegionalData) PortableRemoteObject.narrow(reference, OptionsRegionalData.class);
         
          
         
      }
      catch(Exception e) {
	     e.printStackTrace();
         
      }
   }

   public OptionsRegionalData getRemoteInterface() {
      return regionalData;
   }

}
