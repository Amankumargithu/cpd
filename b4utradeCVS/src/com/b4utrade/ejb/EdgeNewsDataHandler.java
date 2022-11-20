package com.b4utrade.ejb;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.tacpoint.util.Environment;

public class EdgeNewsDataHandler
{

   private InitialContext jndiContext = null;
   private Object reference = null;
   private EdgeNewsData newsData = null;

   public EdgeNewsDataHandler() {
      init();
   }

   public void init() {
      try {
         Hashtable env = new Hashtable();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
         env.put(Context.PROVIDER_URL, Environment.get("JBOSS_EDGE_NEWS_RMI_URL"));
         env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
         jndiContext = new InitialContext(env);
         reference  = jndiContext.lookup("ejbCache/EdgeNewsData");
         newsData = (EdgeNewsData) PortableRemoteObject.narrow(reference, EdgeNewsData.class);
         System.out.println("Got Edge News Remote OBJ");
      }
      catch(Exception e) {
	     e.printStackTrace();
         System.out.println(e.toString());
      }
   }

   public EdgeNewsData getRemoteInterface() {
      return newsData;
   }

}
