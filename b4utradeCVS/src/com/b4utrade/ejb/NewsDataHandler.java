package com.b4utrade.ejb;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.tacpoint.util.Environment;

public class NewsDataHandler
{

   private InitialContext jndiContext = null;
   private Object reference = null;
   private NewsData newsData = null;

   public NewsDataHandler() {
      init();
   }

   public void init() {
      try {
         Hashtable env = new Hashtable();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
         env.put(Context.PROVIDER_URL, Environment.get("JBOSS_NEWS_RMI_URL"));
         env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
         jndiContext = new InitialContext(env);
         reference  = jndiContext.lookup("ejbCache/NewsData");
         newsData = (NewsData) PortableRemoteObject.narrow(reference, NewsData.class);
         System.out.println("Got News Remote OBJ");
      }
      catch(Exception e) {
	     e.printStackTrace();
         System.out.println(e.toString());
      }
   }

   public NewsData getRemoteInterface() {
      return newsData;
   }

}
