package com.b4utrade.ejb;

import com.tacpoint.util.*;

import java.util.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

public class OptionDataHandler 
{

   private InitialContext jndiContext = null;
   private Object reference = null;
   private OptionDataHome home = null;
   private OptionData optionData = null;

   public OptionDataHandler() {
      init();
   }

   public void init() {
      try {
         Hashtable env = new Hashtable();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
         System.out.println("URL: " + Environment.get("JBOSS_OPTION_RMI_URL") );
         env.put(Context.PROVIDER_URL, Environment.get("JBOSS_OPTION_RMI_URL"));
         env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
         jndiContext = new InitialContext(env);
         reference  = jndiContext.lookup("ejbCache/OptionData");
         
         
         /**
          *  env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
         jndiContext = new InitialContext(env);
         reference  = jndiContext.lookup("ejbCache/OptionSubscriber");
         optionSubscriber = (OptionSubscriber) PortableRemoteObject.narrow(reference, OptionSubscriber.class);
          */
         optionData = (OptionData) PortableRemoteObject.narrow(reference, OptionData.class);
         //optionData = home.create();
         System.out.println("got Option data " + optionData);
         // reference  = jndiContext.lookup("ejbCache/OptionSubscriber");
         //optionSubscriber = (OptionSubscriber) PortableRemoteObject.narrow(reference, OptionSubscriber.class);
         
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
