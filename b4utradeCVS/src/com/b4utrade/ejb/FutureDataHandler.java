package com.b4utrade.ejb;

import com.tacpoint.util.*;

import java.util.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

public class FutureDataHandler 
{

   private InitialContext jndiContext = null;
   private Object reference = null;
   private OptionData optionData = null;

   public FutureDataHandler() {
      init();
   }

   public void init() {
      try {
         Hashtable env = new Hashtable();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
         env.put(Context.PROVIDER_URL, Environment.get("JBOSS_FUTURE_RMI_URL"));
         env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
         jndiContext = new InitialContext(env);
         reference  = jndiContext.lookup("ejbCache/OptionData");
         
         optionData = (OptionData) PortableRemoteObject.narrow(reference, OptionData.class); 
      }
      catch(Exception e) {
	 e.printStackTrace();
         System.out.println(e.toString());
      }
   }

   public OptionData getRemoteInterface() {
      return optionData;
   }

}
