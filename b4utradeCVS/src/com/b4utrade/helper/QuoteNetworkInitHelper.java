package com.b4utrade.helper;

import com.tacpoint.util.*;
import java.util.*;
import com.tacpoint.network.*;



public class QuoteNetworkInitHelper implements IPostNetworkRequestHandler
{

   public void execute(Hashtable hashtable) throws Exception
   {
       Enumeration  it = hashtable.elements();

       if (hashtable.size() == 0)
         return;
       while (it.hasMoreElements())
       {
           Object o = it.nextElement();
           if (o instanceof StockActivityHelper)
           {
             StockActivityHelper hashItem = (StockActivityHelper) o;
             hashItem.init();
	       }

       }
   }

}