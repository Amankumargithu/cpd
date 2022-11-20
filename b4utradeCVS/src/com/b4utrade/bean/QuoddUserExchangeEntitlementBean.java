/**
 * QuoddUserExchangeEntitlementBean.java
 *
 * @author Copyright (c) 2007 by Tacpoint Technologies, Inc. All Rights Reserved.
 * @version 1.0
 */
package com.b4utrade.bean;


import com.tacpoint.common.DefaultObject;
import java.util.Hashtable;


/**
 * This bean holds all the entitlement properties for a user.
 */
public class QuoddUserExchangeEntitlementBean extends DefaultObject
{

   private long userID;
   private boolean disableAllExchanges = false;
   private Hashtable exchangeHash = new Hashtable();  // Hashtable of Hashtables, keyed by exchange ID. Inner Hashtables: Hashtable of excluded entitlement codes, keyed by entitlement code


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
    * @param disableAllExchanges  The disableAllExchanges flag to set.
    */
   public void setDisableAllExchanges(boolean disableAllExchanges) {
      this.disableAllExchanges = disableAllExchanges;
   }

   /**
    * @return Returns the disableAllExchanges flag.
    */
   public boolean isDisableAllExchanges() {
      return disableAllExchanges;
   }

   /**
    * @param exchangeHash  The exchangeHash to set.
    */
   public void setExchangeHash(Hashtable exchangeHash) {
      this.exchangeHash = exchangeHash;
   }

   /**
    * @return Returns the exchangeHash.
    */
   public Hashtable getExchangeHash() {
      return exchangeHash;
   }


   /**
    * Checks if the given exchange is entitled.
    *
    * @param exchangeID  Exchange ID code.
    * @return Returns true, if the exchange is entitled.
    */
   public boolean isExhangeEntitled(String exchangeID)
   {
      if (exchangeID == null || exchangeID.length() == 0)
         return false;

      if (disableAllExchanges)
         return false;

      if (this.exchangeHash == null || this.exchangeHash.size() == 0)
         return true;

      Hashtable ht = (Hashtable)this.exchangeHash.get(exchangeID);
      if (ht == null)
         return true;
      if (ht.size() == 0)
         return false;

      // check if both real-time and delayed entitlements are excluded.
      if (ht.get(EntitlementPropertyBean.REAL_TIME_ENTITLEMENT) != null && ht.get(EntitlementPropertyBean.DELAYED_ENTITLEMENT) != null)
         return false;

      return true;
   }

   /**
    * Checks if the given exchange is entitled to real-time prices.
    *
    * @param exchangeID  Exchange ID code.
    * @return Returns true, if the exchange is entitled to real-time prices.
    */
   public boolean isRealTimeEntitled(String exchangeID)
   {
      if (exchangeID == null || exchangeID.length() == 0)
         return false;

      if (!isExhangeEntitled(exchangeID))
         return false;

      if (this.exchangeHash == null || this.exchangeHash.size() == 0)
         return true;

      Hashtable ht = (Hashtable)this.exchangeHash.get(exchangeID);
      if (ht == null)
         return true;

      // check if both real-time and delayed entitlements are missing.
      if (ht.get(EntitlementPropertyBean.REAL_TIME_ENTITLEMENT) == null && ht.get(EntitlementPropertyBean.DELAYED_ENTITLEMENT) == null)
         return true;

      if (ht.get(EntitlementPropertyBean.REAL_TIME_ENTITLEMENT) != null)
         return false;

      return true;
   }

// JohnBall: TODO: need to rethink logic - just copy and paste
   /**
    * Checks if the given exchange is entitled to delayed prices.
    *
    * @param exchangeID  Exchange ID code.
    * @return Returns true, if the exchange is entitled to delayed prices.
    */
   public boolean isDelayedEntitled(String exchangeID)
   {
      if (exchangeID == null || exchangeID.length() == 0)
         return false;

      if (!isExhangeEntitled(exchangeID))
         return false;

      if (this.exchangeHash == null || this.exchangeHash.size() == 0)
         return true;

      Hashtable ht = (Hashtable)this.exchangeHash.get(exchangeID);
      if (ht == null)
         return true;

      // check if both real-time and delayed entitlements are missing.
      if (ht.get(EntitlementPropertyBean.REAL_TIME_ENTITLEMENT) == null && ht.get(EntitlementPropertyBean.DELAYED_ENTITLEMENT) == null)
         return true;

      if (ht.get(EntitlementPropertyBean.DELAYED_ENTITLEMENT) != null)
         return false;

      return true;
   }
// JohnBall
   
   /**
    * Checks if the given exchange is entitled to only the last price.
    *
    * @param exchangeID  Exchange ID code.
    * @return Returns true, if the exchange is entitled to only the last price.
    */
   public boolean isLastOnlyEntitled(String exchangeID)
   {
      if (exchangeID == null || exchangeID.length() == 0)
         return false;

      if (!isExhangeEntitled(exchangeID))
         return false;

      if (this.exchangeHash == null || this.exchangeHash.size() == 0)
         return false;

      Hashtable ht = (Hashtable)this.exchangeHash.get(exchangeID);
      if (ht == null)
         return false;

      // check if both last-only and last/bid/ask entitlements are missing.
      if (ht.get(EntitlementPropertyBean.LAST_ONLY_ENTITLEMENT) == null && ht.get(EntitlementPropertyBean.LAST_BID_ASK_ENTITLEMENT) == null)
         return false;

      if (ht.get(EntitlementPropertyBean.LAST_ONLY_ENTITLEMENT) != null)
         return false;

      return true;
   }

   /**
    * Checks if the given exchange is entitled to last, bid, and ask prices.
    *
    * @param exchangeID  Exchange ID code.
    * @return Returns true, if the exchange is entitled to last, bid, and ask prices.
    */
   public boolean isLastBidAskEntitled(String exchangeID)
   {
      if (exchangeID == null || exchangeID.length() == 0)
         return false;

      if (!isExhangeEntitled(exchangeID))
         return false;

      if (this.exchangeHash == null || this.exchangeHash.size() == 0)
         return true;

      Hashtable ht = (Hashtable)this.exchangeHash.get(exchangeID);
      if (ht == null)
         return true;

      // check if both last-only and last/bid/ask entitlements are missing.
      if (ht.get(EntitlementPropertyBean.LAST_ONLY_ENTITLEMENT) == null && ht.get(EntitlementPropertyBean.LAST_BID_ASK_ENTITLEMENT) == null)
         return true;

      if (ht.get(EntitlementPropertyBean.LAST_BID_ASK_ENTITLEMENT) != null)
         return false;

      return true;
   }
}
