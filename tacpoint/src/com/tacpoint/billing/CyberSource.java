/**
* CyberSource.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000.  All rights reserved.
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 07/24/2000
* Date modified:
*  - 8/25/00 - change input/output stream to bufferedReader and printWriter
*/

package com.tacpoint.billing;

import com.cybersource.ics.base.message.*;
import com.cybersource.ics.base.exception.*;
import com.cybersource.ics.client.message.*;
import com.cybersource.ics.client.*;

import java.io.*;
import java.util.*;
import java.net.*;

import com.tacpoint.util.*;

public final class CyberSource extends CreditCard
{
   //CONSTANTS
   public static final String ICS_DAV      = "ics_dav";
   public static final String ICS_TAX      = "ics_tax";
   public static final String ICS_EXPORT   = "ics_export";
   public static final String ICS_AUTH     = "ics_auth";
   public static final String ICS_SCORE    = "ics_score";
   public static final String ICS_BILL     = "ics_bill";
   public static final String ICS_NOTIFY   = "ics_notify";
   public static final String ICS_CREDIT   = "ics_credit";
   public static final String ICS_ECP      = "ics_ecp";

   //Credit info
   private ICSClient mICSClient;
   private ICSClientOffer mOffer;          //offer information
   private ICSClientRequest mICSrequest;   //Request information
   private ICSReply mReply;

   private String mReplyFlag;

   //CONSTRUCTOR
   public CyberSource()
   {
      try
      {
         Logger.init();
      }
      catch(Exception e)
      {
         Logger.log("CyberSource.default constructor() " + e.getMessage());
      }

      init();
   }

   //METHODS
   public void init()
   {
      ResourceBundle vResource = null;

      try
      {
         vResource = ResourceBundle.getBundle("credit");
         mErrorMessage = new String();
      }
      catch(Exception e)
      {
         Logger.log("CyberSource.init() - " + e.getMessage());
      }

      try
      {
         mOffer = new ICSClientOffer();
         if (vResource != null)
         {
            mICSClient = new ICSClient(
               vResource.getString("credit.MerchantID"),
               vResource.getString("credit.PrivateKey"),
               vResource.getString("credit.Certificate"),
               vResource.getString("credit.ServerID"),
               vResource.getString("credit.ServerCertificate"),
               new URL(vResource.getString("credit.URL")));

            mICSrequest = new ICSClientRequest(mICSClient);
         }
         else
         {
            Logger.log("CyberSource.init() - vResource null");
         }

         if ( (vResource != null) && (mICSrequest != null) )
         {
            mICSrequest.setCurrency(vResource.getString("credit.Currency"));
            //mICSrequest.setMerchantRefNo(vResource.getString("request.MerchantRefNo"));
            mICSrequest.setMerchantID(vResource.getString("credit.MerchantID"));
         }
         else
         {
            Logger.log("CyberSource.init() - mICSrequest null");
         }
      }
      catch(Exception e)
      {
         Logger.log("CyberSource.initCyberSource() - " + e.getMessage());
      }
   }

   public boolean authorizeCharge(String aCharge)
   {
      boolean vOk = false;

      try
      {
         mICSrequest.addApplication(ICS_AUTH);	//request a credit

         mOffer.setAmount(aCharge);

         mICSrequest.addOffer(mOffer);
         mReply = mICSClient.send(mICSrequest);

         if ( mReply.getReplyCode() > 0 )
         {
            //Authorization SUCCEEDED!!!
            vOk = true;
            String vMember = new String("SUCCESS: " + mICSrequest.getCustomerFirstName()
               + " " + mICSrequest.getCustomerLastName());
            Logger.log(vMember);
         }
         else
         {
            //Authorization FAILED!!!
            String vErrorUser = new String("FAILED: " + mICSrequest.getCustomerFirstName()
               + " " + mICSrequest.getCustomerLastName()
               + " - " + mReply.getErrorMessage());
            Logger.log(vErrorUser);
            mErrorMessage = mReply.getErrorMessage();
            Hashtable vReplyHash = mReply.getHashtable();
            ICSClientReply vClientReply = new ICSClientReply(vReplyHash);
            mReplyFlag = vClientReply.getReplyFlag();
            vOk = false;
         }
      }
      catch (ICSException icse)
      {
         mErrorMessage = icse.getMessage();
      }
      catch (Exception e)
      {
         Logger.log("CyberSource.authorizeCharge exception " + e.getMessage());
      }
      return vOk;
   }

   public boolean chargeAccount(String aCharge)
   {
      boolean vOk = false;

      try
      {
         mICSrequest.addApplication(ICS_AUTH + "," + ICS_BILL);	//request a credit

         mOffer.setAmount(aCharge);
         mICSrequest.addOffer(mOffer);
         mReply = mICSClient.send(mICSrequest);

         if ( mReply.getReplyCode() > 0 )
         {
            //Charge SUCCEEDED!!!
            vOk = true;
            String vMember = new String("SUCCESS: " + mICSrequest.getCustomerFirstName()
               + " " + mICSrequest.getCustomerLastName() + " " + aCharge);
            Logger.log(vMember);
         }
         else
         {
            //Charge FAILED!!!
            String vErrorUser = new String("FAILED: " + mICSrequest.getCustomerFirstName()
               + " " + mICSrequest.getCustomerLastName() + " " + aCharge
               + " - " + mReply.getErrorMessage());
            Logger.log(vErrorUser);
            mErrorMessage = mReply.getErrorMessage();
            Hashtable vReplyHash = mReply.getHashtable();
            ICSClientReply vClientReply = new ICSClientReply(vReplyHash);
            mReplyFlag = vClientReply.getReplyFlag();
            vOk = false;
         }
      }
      catch (ICSException icse)
      {
         mErrorMessage = icse.getMessage();
      }
      catch (Exception e)
      {
         Logger.log("CyberSource.creditCharge exception " + e.getMessage());
      }

      return vOk;
   }

   public String getErrorMessage()
   {
      if (mErrorMessage != null)
      {
         return mErrorMessage;
      }
      return ("N/A");
   }

   public String getReplyFlag()
   {
      if (mReplyFlag != null)
      {
         return mReplyFlag;
      }
      return ("N/A");
   }

   public void setUserInfo(
      String aRefNumber,
      String aCCNumber,
      String aCCExpMonth,
      String aCCExpYear,
      String aFirstName,
      String aLastName,
      String aEmailAddress,
      String aPhone,
      String aBillAddress1,
      String aBillAddress2,
      String aBillCity,
      String aBillState,
      String aBillZip,
      String aBillCountry)
   {
      try
      {
         if (aRefNumber != null)
            mICSrequest.setMerchantRefNo(aRefNumber);
         if (aCCNumber != null)
            mICSrequest.setCustomerCreditCardNumber(aCCNumber);
         if (aCCExpMonth != null)
            mICSrequest.setCustomerCreditCardExpirationMonth(aCCExpMonth);
         if (aCCExpYear != null)
            mICSrequest.setCustomerCreditCardExpirationYear(aCCExpYear);
         if (aFirstName != null)
            mICSrequest.setCustomerFirstName(aFirstName);
         if (aLastName != null)
            mICSrequest.setCustomerLastName(aLastName);
         if (aEmailAddress != null)
            mICSrequest.setCustomerEmailAddress(aEmailAddress);
         if (aPhone != null)
            mICSrequest.setCustomerPhone(aPhone);
         if (aBillAddress1 != null)
            mICSrequest.setBillAddress1(aBillAddress1);
         if (aBillAddress2 != null)
            mICSrequest.setBillAddress2(aBillAddress2);
         if (aBillCity != null)
            mICSrequest.setBillCity(aBillCity);
         if (aBillState != null)
            mICSrequest.setBillState(aBillState);
         if (aBillZip != null)
            mICSrequest.setBillZip(aBillZip);
         if (aBillCountry != null)
            mICSrequest.setBillCountry(aBillCountry);
      }
      catch (Exception e)
      {
         Logger.log("CyberSource.setUserInfo exception " + e.getMessage());
      }
   }

   // main function for testing
   public static void main(String[] args)
   {
      try
      {
      }
      catch(Exception e)
      {
         System.out.println("CyberSource.main() Exception " + e.getMessage());
      }
   }

}
