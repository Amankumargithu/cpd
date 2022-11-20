/**
 * OptionItem
 *
 * @author Copyright (c) 2004 by Tacpoint Technologies, Inc.
 *         All rights reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import java.util.*;


public class OptionItem implements java.io.Serializable {

   private String symbol;
   private double strikePrice;
   private long openInterest;
   private String expiryDate;
   private int type;
   private Calendar expDate;
   
   public OptionItem() {}

   public void setSymbol(String symbol) { this.symbol = symbol; }
   public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
   public void setType(int type) { this.type = type; }
   public void setStrikePrice(double strikePrice) { this.strikePrice = strikePrice; }
   public void setOpenInterest(long openInterest) { this.openInterest = openInterest; }

   public String getSymbol() { return symbol; }
   public String getExpiryDate() { return expiryDate; }
   public int getType() { return type; }
   public double getStrikePrice() { return strikePrice; }
   public long getOpenInterest() { return openInterest; }

   public String toString() {
      return "Symbol="+symbol+",Strike Price="+strikePrice+",Open Interest="+openInterest+",Expiry Date="+expiryDate+",Type = "+type + " exp date " + expDate;
   }

public Calendar getExpDate() {
	return expDate;
}

public void setExpDate(Calendar expDate) {
	this.expDate = expDate;
}

}
