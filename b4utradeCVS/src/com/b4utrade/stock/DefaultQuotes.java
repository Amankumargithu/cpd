/** DefaultQuotes.java
* Copyright: Tacpoint Technologies, Inc. (c) 2000.
* All rights reserved.
* @author Kim Gentes
* @author kimg@tacpoint.com
* @version 1.0
* Date created:  4/30/2000
*/

package com.b4utrade.stock;

import java.util.*;
import com.b4utrade.stockutil.DefaultStockObject;


/** DefaultQuotes class
* The DefaultQuotes class is an interface to quote servers.
*/
public interface DefaultQuotes
{

   public DefaultStockObject getStockQuote(String aTicker);
   public void setStockQuote(DefaultStockObject aStock);
   public Enumeration getEnumeration();

}
