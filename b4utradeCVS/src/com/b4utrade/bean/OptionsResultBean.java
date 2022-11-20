/**
 * OptionsResultBean
 *
 * @author Copyright (c) 2004 by Tacpoint Technologies, Inc.
 *         All rights reserved.
 * @version 1.0
 */

package com.b4utrade.bean;

import java.util.*;

public  class OptionsResultBean implements java.io.Serializable
{

   private double volatility;
   private double interestRate;
   private double expireTime;
   private Vector expiryDateList;
   private Vector optionList;

   public OptionsResultBean() {}

   public void setVolatility(double volatility) { this.volatility = volatility; }
   public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
   public void setExpireTime(double expireTime) { this.expireTime = expireTime; }
   public void setExpiryDateList(Vector expiryDateList) { this.expiryDateList = expiryDateList; }
   public void setOptionList(Vector optionList) { this.optionList = optionList; }

   public double getVolatility() { return volatility; }
   public double getInterestRate() { return interestRate; }
   public double getExpireTime() { return expireTime; }
   public Vector getExpiryDateList() { return expiryDateList; }
   public Vector getOptionList() { return optionList; }

   public OptionItem makeOptionItem(String symbol, double strikePrice, long openInterest) {
      OptionItem oi = new OptionItem();
      oi.setSymbol(symbol);
      oi.setStrikePrice(strikePrice);
      oi.setOpenInterest(openInterest);
      return oi;
   }
}

