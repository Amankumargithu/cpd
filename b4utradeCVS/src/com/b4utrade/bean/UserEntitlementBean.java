/**
 * UserEntitlementBean.java
 *
 * @author Copyright (c) 2007 by Tacpoint Technologies, Inc. All Rights Reserved.
 * @version 1.0
 */
package com.b4utrade.bean;


import com.tacpoint.common.DefaultObject;
import java.util.Vector;
import java.util.StringTokenizer;


/**
 * This bean holds all the entitlement properties for a user.
 */
public class UserEntitlementBean extends DefaultObject
{

   public static final String ALL_EXCHANGES = "-99";

   private long userID;
   private String exchangeID = ALL_EXCHANGES;
   private String entitlementList = "";   // comma delimited list
   private Vector entitlementVector = new Vector();   // Vector of EntitlementPropertyBeans


   /**
    * @param userID  The userID to set.
    */
   public void setUserID(long userID) {
      this.userID = userID;
   }

   /**
    * @return Returns the userID.
    */
   public long getUserID() {
      return userID;
   }

   /**
    * @param exchangeID  The exchangeID to set.
    */
   public void setExchangeID(String exchangeID) {
      this.exchangeID = exchangeID;
      if (exchangeID == null || exchangeID.trim().length() == 0) this.exchangeID = ALL_EXCHANGES;
   }

   /**
    * @return Returns the exchangeID.
    */
   public String getExchangeID() {
      if (exchangeID != null && exchangeID.equals(ALL_EXCHANGES)) return "";
      return exchangeID;
   }

   /**
    * @param entitlementList  The entitlementList to set.
    */
   public void setEntitlementList(String entitlementList) {
      this.entitlementList = entitlementList;
      this.entitlementVector = parseEntitlementList(entitlementList);
   }

   /**
    * @return Returns the entitlementList.
    */
   public String getEntitlementList() {
      return entitlementList;
   }

   /**
    * @param entitlementVector  The entitlementVector to set.
    */
   public void setEntitlementVector(Vector entitlementVector) {
      this.entitlementVector = entitlementVector;
      this.entitlementList = concatProperties(entitlementVector);
   }

   /**
    * @return  Returns the entitlementVector.
    */
   public Vector getEntitlementVector() {
      return entitlementVector;
   }


   /**
    * Parses the given comma separated entitlement list into a Vector
    * of EntitlementPropertyBeans.
    *
    * @param entitlementList  Comma separated entitlement list.
    * @return Returns a Vector of EntitlementPropertyBeans.
    */
   private Vector parseEntitlementList(String entitlementList)
   {
      if (entitlementList == null || entitlementList.trim().length() == 0) {
         return new Vector();
      }

      Vector v = new Vector();
      StringTokenizer st = new StringTokenizer(entitlementList, ",");
      while (st.hasMoreTokens()) {
         String token = st.nextToken();
         if (token == null) continue;
         EntitlementPropertyBean epbean = new EntitlementPropertyBean();
         epbean.setPropertyCode(token);
         v.add(epbean);

      }

      return v;
   }

   /**
    * Concatenates the given Vector into a comma separated entitlement list.
    *
    * @param entitlementVector  Vector of EntitlementPropertyBeans.
    * @return Returns a comma separated entitlement list.
    */
   private String concatProperties(Vector entitlementVector)
   {
      if (entitlementVector == null || entitlementVector.size() == 0)
         return "";

      StringBuffer sb = new StringBuffer();
      for (int i=0; i < entitlementVector.size(); i++) {
         EntitlementPropertyBean epbean = (EntitlementPropertyBean)entitlementVector.elementAt(i);
         if (epbean == null) continue;
         if (sb.length() == 0) sb.append(epbean.getPropertyCode());
         else {
            sb.append(',');
            sb.append(epbean.getPropertyCode());
         }
      }

      return sb.toString();
   }


}
