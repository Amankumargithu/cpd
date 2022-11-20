/**
 *  StockValueBean.java
 *
 *  Copyright (c) 2005 Tacpoint Technologies, Inc.
 *  All Rights Reserved.
 */
package com.b4utrade.bean;


import com.tacpoint.common.DefaultObject;


public class StockValueBean extends DefaultObject
{

   private String ticker = "";
   private long companyID;
   private long fieldID;
   private String fieldName = "";
   private String value = "";



   public void setTicker(String ticker) {
      this.ticker = ticker;
   }

   public String getTicker() {
      return ticker;
   }

   public void setCompanyID(long companyID) {
      this.companyID = companyID;
   }

   public long getCompanyID() {
      return companyID;
   }

   public void setFieldID(long fieldID) {
      this.fieldID = fieldID;
   }

   public long getFieldID() {
      return fieldID;
   }

   public void setFieldName(String fieldName) {
      this.fieldName = fieldName;
   }

   public String getFieldName() {
      return fieldName;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public String getValue() {
      return value;
   }


}
