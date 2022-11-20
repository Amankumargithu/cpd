/**
 * TickerExchangeBean.java
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2002.  All rights reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import com.tacpoint.common.DefaultObject;


/**
 *   The News bean object holding news data.
 */
public class TickerExchangeBean extends DefaultObject
{

  String ticker;
  String exchange;



   /**
    * Default constructor - does nothing.
    */
   public TickerExchangeBean()
   {
   }



public String getTicker() {
	return ticker;
}



public void setTicker(String ticker) {
	this.ticker = ticker;
}



public String getExchange() {
	return exchange;
}



public void setExchange(String exchange) {
	this.exchange = exchange;
}

}

