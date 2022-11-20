/**
 * EntitlementPropertyBean.java
 *
 * @author Copyright (c) 2007 by Tacpoint Technologies, Inc. All Rights Reserved.
 * @version 1.0
 */
package com.b4utrade.bean;


import com.tacpoint.common.DefaultObject;


/**
 * This bean holds an entitlement property.
 */
public class EntitlementPropertyBean extends DefaultObject
{

   public static final String REAL_TIME_ENTITLEMENT = "RT";
   public static final String DELAYED_ENTITLEMENT = "DELAY";
   public static final String LAST_ONLY_ENTITLEMENT = "LAST";
   public static final String LAST_BID_ASK_ENTITLEMENT = "LBA";

   public static final String[] ALL_ENTITLEMENTS = {REAL_TIME_ENTITLEMENT,
                                                    DELAYED_ENTITLEMENT,
                                                    LAST_ONLY_ENTITLEMENT,
                                                    LAST_BID_ASK_ENTITLEMENT};

   private String propertyCode = "";
   private String name = "";
   private String description = "";
   private boolean exchangeFlag = false;


   /**
    * @param propertyCode  The propertyCode to set.
    */
   public void setPropertyCode(String propertyCode) {
      this.propertyCode = propertyCode;
   }

   /**
    * @return Returns the propertyCode.
    */
   public String getPropertyCode() {
      return propertyCode;
   }

   /**
    * @param name  The name to set.
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * @return Returns the name.
    */
   public String getName() {
      return name;
   }

   /**
    * @param description  The description to set.
    */
   public void setDescription(String description) {
      this.description = description;
   }

   /**
    * @return Returns the description.
    */
   public String getDescription() {
      return description;
   }

   /**
    * @param exchangeFlag  The exchangeFlag to set.
    */
   public void setExchangeFlag(boolean exchangeFlag) {
      this.exchangeFlag = exchangeFlag;
   }

   /**
    * @return Returns the exchangeFlag.
    */
   public boolean getExchangeFlag() {
      return exchangeFlag;
   }

   /**
    * @return Returns the exchangeFlag.
    */
   public boolean isExchange() {
      return exchangeFlag;
   }


}
