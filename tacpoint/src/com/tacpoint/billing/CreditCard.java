/**
* CreditCard.java
* Copyright: Tacpoint Technologies, Inc. (c), 2000.  All rights reserved.
* @author jtong@tacpoint.com
* @version 1.0
* Date created: 12/01/2000
* Date modified:
*/

package com.tacpoint.billing;

public abstract class CreditCard
{
   protected String mErrorMessage;

   //METHODS
   public abstract void init();

   public abstract boolean authorizeCharge(String aCharge);

   public abstract boolean chargeAccount(String aCharge);

   public abstract String getErrorMessage();
}
