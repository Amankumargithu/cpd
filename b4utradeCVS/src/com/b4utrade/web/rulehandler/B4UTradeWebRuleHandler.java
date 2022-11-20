/*
 *  B4UTradeWebRuleHandler.java
 *
 *  Copyright (c) 2003 Tacpoint Technologies, Inc.
 *  All Rights Reserved.
 */
package com.b4utrade.web.rulehandler;

import com.tacpoint.common.DefaultObject;

/**
 * This class keeps the constants for default info.
 */
public class B4UTradeWebRuleHandler extends DefaultObject
{
 
   public static final String APPLICATION_ERROR_MSG = "APPLICATION_ERROR_MSG";
   public static final String LOGIN_ERROR_MSG = "LOGIN_ERROR_MSG";
   public static final String RELOGIN_MSG = "RELOGIN_MSG";
   public static final String HOME_PAGE_FLAG = "HOME_PAGE_FLAG";
   public static final String MAIN_PAGE_URL = "MAIN_PAGE_URL";
   public static final String MAIN_PAGE_TITLE = "MAIN_PAGE_TITLE";



   /**
    * The static method to access the defined constants
    *
    */

   public static String getApplicationErrorMsg()
   {
      return (B4UTradeWebRuleHandler.APPLICATION_ERROR_MSG);
   }

   public static String getReLoginMsg()
   {
      return B4UTradeWebRuleHandler.RELOGIN_MSG;
   }

   

}
