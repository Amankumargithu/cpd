/******************************************************************************
*
*  EquitySpec.java
*     UltraFeed Equity Appendices
*
*  REVISION HISTORY:
*     31 MAY 2012 jcs  Created.
*     28 JUN 2012 jcs  Build  27: Tier()
*     20 JUL 2012 jcs  Build  29: QuoddFeed.Enum.UltraFeed
*      9 AUG 2012 jcs  Build  32: Eligilbility
*     22 AUG 2012 jcs  Build  34: UltraFeed._IsSet()
*     17 SEP 2012 jcs  Build  37: IsOpen()
*     24 SEP 2012 jcs  Build  39: MktCategory()
*     25 OCT 2012 jcs  Build  44: IsEligible(); [%d] %s for flags / bitmasks
*      4 FEB 2013 jcs  Build  52: Tier() w/o prefix
*     11 MAR 2013 jcs  Build  55: LuldFlags()
*     26 MAR 2013 jcs  Build  56: EQTrade.IsCanadian()
*     17 APR 2013 jcs  Build  57: _LimitUpDown()
*     25 APR 2013 jcs  Build  58: _LimitUpDownJ()
*     30 MAY 2013 jcs  Build  61: UltraFeed._bBasic
*     15 JUN 2013 jcs  Build  64: CanDisplayBid() / CanDisplayAsk()
*      1 APR 2014 jcs  Build  76: _IsCachedXxx() : !_IsSet() - DUH
*      1 OCT 2014 jcs  Build  83: FinancialStatus()
*     24 NOV 2014 jcs  Build  87: IsOfficialXxxx()
*     27 APR 2015 jcs  Build  92: De-lint
*      3 APR 2017 jcs  Build 102: Tier() from UF 3.0 spec
*     19 JUL 2017 jcs  Build 103: bOld
*
*  (c) 1994-2017 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.Enum;

import java.util.*;

/////////////////////////////////////////////////////////////////
//
//               c l a s s    E q u i t y S p e c
//
/////////////////////////////////////////////////////////////////
/**
 * The EquitySpec class encapsulates the UltraFeed Equity appendices.
 */
public class EquitySpec extends QuoddFeed.Enum.UltraFeed
{
   //////////////////////////////////
   //
   //       E Q T r a d e
   //
   //////////////////////////////////
   static private final int eligibleConsVolume   = 0x0001;
   static private final int eligibleConsLast     = 0x0002;
   static private final int eligibleConsHighLow  = 0x0004;
   static private final int eligiblePartVolume   = 0x0008;
   static private final int eligiblePartLast     = 0x0010;
   static private final int eligiblePartHighLow  = 0x0020;
   static private final int eligiblePartOpen     = 0x0040;
   static private final int eligiblePartClose    = 0x0080;
   static private final int changedConsLast      = 0x0100;
   static private final int changedConsLow       = 0x0200;
   static private final int changedConsHigh      = 0x0400;
   static private final int changedPartLast      = 0x0800;
   static private final int changedPartLow       = 0x1000;
   static private final int changedPartHigh      = 0x2000;
   static private final int changedPartOpen      = 0x4000;
   /*
    * Appx D - Eligility Flags
    */
   static public String EligibilityFlags( QuoddFeed.msg.EQTrade trd, boolean bOld )
   {
      String            rtn;
      ArrayList<String> v;
      int               i, n, elg;

      // Bitmask

      v   = new ArrayList<String>();
      elg = bOld ? trd._old_eligFlags : trd._eligFlags;
      if ( _IsSet( elg, eligibleConsVolume ) )
         v.add( new String( "ConsVolume" ) );
      if ( _IsSet( elg, eligibleConsLast ) )
         v.add( new String( "ConsLast" ) );
      if ( _IsSet( elg, eligibleConsHighLow ) )
         v.add( new String( "ConsHighLow" ) );
      if ( _IsSet( elg, eligiblePartVolume ) )
         v.add( new String( "PartVolume" ) );
      if ( _IsSet( elg, eligiblePartLast ) )
         v.add( new String( "PartLast" ) );
      if ( _IsSet( elg, eligiblePartHighLow ) )
         v.add( new String( "PartHighLow" ) );
      if ( _IsSet( elg, eligiblePartOpen ) )
         v.add( new String( "PartOpen" ) );
      if ( _IsSet( elg, eligiblePartClose ) )
         v.add( new String( "PartClose" ) );
      if ( _IsSet( elg, changedConsLast ) )
         v.add( new String( "changedConsLast" ) );
      if ( _IsSet( elg, changedConsLow ) )
         v.add( new String( "changedConsLow" ) );
      if ( _IsSet( elg, changedConsHigh ) )
         v.add( new String( "changedConsHigh" ) );
      if ( _IsSet( elg, changedPartLast ) )
         v.add( new String( "changedPartLast" ) );
      if ( _IsSet( elg, changedPartLow ) )
         v.add( new String( "changedPartLow" ) );
      if ( _IsSet( elg, changedPartHigh ) )
         v.add( new String( "changedPartHigh" ) );
      if ( _IsSet( elg, changedPartOpen ) )
         v.add( new String( "changedPartOpen" ) );
      n   = v.size();
      rtn = "".format( "[0x%04x] ", elg );
      for ( i=0; i<n; i++ ) {
         rtn += ( i>0 ) ? "," : "";
         rtn += v.get( i );
      }
      return rtn;
   }
   /*
    * Appx G - Sales Condition Tables
    */
   static public String SettlementType( QuoddFeed.msg.EQTrade trd, boolean bOld )
   {
      String s;
      int    ty;

      s = "Undefined";
      ty = bOld ? trd._old_setlType : trd._setlType;
      if ( trd.IsCanadian() ) {
         switch( ty ) {
            case 1: s = "Regular"; break;
            case 2: s = "Cash"; break;
            case 3: s = "NonNet"; break;
            case 4: s = "MS"; break;
            case 5: s = "CashToday"; break;
            case 6: s = "DelayedDelivery"; break;
         }
      }         
      else {    
         switch( ty ) {
            case 1: s = "Regular"; break;
            case 2: s = "Cash"; break;
            case 3: s = "NextDay"; break;
            case 4: s = "Seller"; break;
         }    
      }
      s = s.format( "[%02d] %s", ty, s );
      return s;
   }

   static public String ReportType( QuoddFeed.msg.EQTrade trd, boolean bOld ) 
   {
      String s;
      int    rpt;

      s = "Undefined";
      rpt = bOld ? trd._old_rptType : trd._rptType;
      if ( trd.IsCanadian() ) {
         switch( rpt ) {
            case 1: s = "Regular"; break;
            case 2: s = "Opening"; break;
            case 3: s = "InternalCross"; break;
            case 4: s = "Basis"; break;
            case 5: s = "Contingent"; break;
            case 6: s = "STS"; break;
            case 7: s = "VWAP"; break;
         }
      }
      else {
         switch( rpt ) {
            case 1: s = "Regular"; break;
            case 2: s = "Opening"; break;
            case 3: s = "Closing"; break;
            case 4: s = "Reopening"; break;
            case 5: s = "ISO"; break;
            case 6: s = "DerivitivelyPriced"; break;
         }
      }
      s = s.format( "[%02d] %s", rpt, s );
      return s;
   }

   static public String ReportDetail( QuoddFeed.msg.EQTrade trd, boolean bOld )
   {
      String s;
      int    rpt;

      s = "Undefined";
      rpt = bOld ? trd._old_rptDetail : trd._rptDetail;
      if ( trd.IsCanadian() ) {
         switch( rpt ) {
            case  0: s = "None"; break;
            case  1: s = "Cross"; break;
         }
      }
      else {
         switch( rpt ) {
            case  0: s = "None"; break;
            case  1: s = "Cross"; break;
            case  2: s = "OfficialOpen"; break;
            case  3: s = "OfficialClose"; break;
            case  4: s = "Acquisition"; break;
            case  5: s = "Bunched"; break;
            case  6: s = "Distribution"; break;
            case  7: s = "PriceVariation"; break;
            case  8: s = "CAPElection"; break;
            case  9: s = "AveragePrice"; break;
            case 10: s = "AutomaticExecution"; break;
            case 11: s = "PriorReferencePrice"; break;
            case 12: s = "Rule155"; break;
            case 13: s = "OptionTrade"; break;
            case 14: s = "SplitTrade"; break;
            case 15: s = "StoppedStock"; break;
         }
      }
      s = s.format( "[%02d] %s", rpt, s );
      return s;
   }

   static public boolean IsOfficialOpen( QuoddFeed.msg.EQTrade trd )
   {
      int rpt;

      // 2 = OfficialOpen

      rpt = trd.IsCanadian() ? 0 : trd._rptDetail;
      return( rpt == 2 );
   }

   static public boolean IsOfficialClose( QuoddFeed.msg.EQTrade trd )
   {
      int rpt;

      // 3 = OfficialClose

      rpt = trd.IsCanadian() ? 0 : trd._rptDetail;
      return( rpt == 3 );
   }

   static private final int TRD_ODDLOT     = 0x0001;
   static private final int TRD_EXTENDED   = 0x0002;
   static private final int TRD_OUTOFSEQ   = 0x0004;
   static private final int TRD_TRADETHRU  = 0x0008;
   static private final int TRD_HELDTRADE  = 0x0010;
   static private final int TRD_LATETRADE  = 0x0030;
   static private final int TRD_ERROR      = 0x0040;
   static private final int TRD_YELLOWFLAG = 0x0080;

   static public String ReportFlags( QuoddFeed.msg.EQTrade trd, boolean bOld )
   {
      String            s;
      ArrayList<String> v;
      int               i, n, rpt;

      // Bitmask

      v   = new ArrayList<String>();
      rpt = bOld ? trd._old_rptFlags : trd._rptFlags;
      if ( trd.IsCanadian() ) {
         if ( IsOddLot( trd ) )       v.add( new String( "OddLot" ) );
         if ( IsExtended( trd ) )     v.add( new String( "ExtendedHours" ) );
         if ( IsOutOfSeq( trd ) )     v.add( new String( "OutOfSequence" ) );
         if ( IsTradeThru( trd ) )    v.add( new String( "ByPass" ) );
         if ( IsHeldTrade( trd ) )    v.add( new String( "MOC" ) );
         if ( IsLateTrade( trd ) )    v.add( new String( "Basket" ) );
         if ( IsError( trd ) )        v.add( new String( "Jitney" ) );
         if ( IsYellowFlag( trd ) )   v.add( new String( "NonResident" ) );
         if ( _IsSet( rpt, 0x0100 ) ) v.add( new String( "Principal" ) );
         if ( _IsSet( rpt, 0x0200 ) ) v.add( new String( "Wash" ) );
         if ( _IsSet( rpt, 0x0400 ) ) v.add( new String( "Anonymous" ) );
         if ( _IsSet( rpt, 0x0800 ) ) v.add( new String( "SpecialTerms" ) );
      }
      else {
         if ( IsOddLot( trd ) )     v.add( new String( "OddLot" ) );
         if ( IsExtended( trd ) )   v.add( new String( "ExtendedHours" ) );
         if ( IsOutOfSeq( trd ) )   v.add( new String( "OutOfSequence" ) );
         if ( IsTradeThru( trd ) )  v.add( new String( "TradeThruExempt" ) );
         if ( IsHeldTrade( trd ) )  v.add( new String( "HeldTrade" ) );
         if ( IsLateTrade( trd ) )  v.add( new String( "Late" ) );
         if ( IsError( trd ) )      v.add( new String( "Error" ) );
         if ( IsYellowFlag( trd ) ) v.add( new String( "YellowFlag" ) );
      }
      n = v.size();
      s = "Undefined";
      s = s.format( "[%02d] ", rpt );
      for ( i=0; i<n; i++ ) {
         s += ( i>0 ) ? "," : "";
         s += v.get( i );
      }
      return s;
   }

   static public boolean IsOddLot( QuoddFeed.msg.EQTrade trd )
   {
      return _IsSet( trd._rptFlags, TRD_ODDLOT );
   }

   static public boolean IsExtended( QuoddFeed.msg.EQTrade trd )
   {
      return _IsSet( trd._rptFlags, TRD_EXTENDED );
   }

   static public boolean IsOutOfSeq( QuoddFeed.msg.EQTrade trd )
   {
      return _IsSet( trd._rptFlags, TRD_OUTOFSEQ );
   }

   static public boolean IsTradeThru( QuoddFeed.msg.EQTrade trd )
   {
      return _IsSet( trd._rptFlags, TRD_TRADETHRU );
   }

   static public boolean IsHeldTrade( QuoddFeed.msg.EQTrade trd )
   {
      return _IsSet( trd._rptFlags, TRD_HELDTRADE );
   }

   static public boolean IsLateTrade( QuoddFeed.msg.EQTrade trd )
   {
      return _IsSet( trd._rptFlags, TRD_LATETRADE );
   }

   static public boolean IsError( QuoddFeed.msg.EQTrade trd )
   {
      return _IsSet( trd._rptFlags, TRD_ERROR );
   }

   static public boolean IsYellowFlag( QuoddFeed.msg.EQTrade trd )
   {
      return _IsSet( trd._rptFlags, TRD_YELLOWFLAG );
   }

   /*
    * UF 3.0 Appendix 4.29
    */
   static public String Tier( QuoddFeed.msg.Image img )
   {
      String s;
      int    tier;

      s    = "";
      tier = -1;
      if ( img.IsEquity() ) {
         switch( (tier=img._tier) ) {
            case  0: s = "No Tier"; break;
            case  1: s = "NASDAQ Global Select"; break;
            case  2: s = "NASDAQ Global"; break;
            case  3: s = "NASDAQ Capital"; break;
            case  4: s = "NYSE"; break;
            case  5: s = "NYSE Markets (AMEX)"; break;
            case  6: s = "NYSE Arca"; break;
            case  7: s = "BATS"; break;
            case  8: s = "OTC Markets - No Tier"; break;
            case  9: s = "OTCQX U.S. Premier"; break;
            case 10: s = "OTCQX U.S."; break;
            case 11: s = "OTCQX International Premier"; break;
            case 12: s = "OTCQX International"; break;
            case 13: s = "OTCQB"; break;
            case 14: s = "OTCBB"; break;
            case 15: s = "OTC Pink Current"; break;
            case 16: s = "OTC Pink Limited"; break;
            case 17: s = "OTC Pink No Information"; break;
            case 18: s = "Grey Market"; break;
            case 19: s = "OTC Yellow"; break;
            case 20: s = "OTC Bonds"; break;
            case 21: s = "Funds - News Media List"; break;
            case 22: s = "Funds - Supplemental List"; break;
         }
      }
/*
 * 13-02-04 jcs  Build 52 : Mr Zee displays this
 *
      s = s.format( "[%02d] %s", tier, s );
 */
      return s;
   }

   /*
    * From Richard's E-mail
    */
   static public String Tier_UF1( QuoddFeed.msg.Image img )
   {
      String s;
      int    tier;

      s    = "";
      tier = -1;
      if ( img.IsEquity() ) {
         switch( (tier=img._tier) ) {
            case  0: s = "No Tier"; break;
            case  1: s = "OTCQX U.S. Premier"; break;
            case  2: s = "OTCQX U.S."; break;
            case  5: s = "OTCQX International Premier"; break;
            case  6: s = "OTCQX International"; break;
            case 10: s = "OTCQB"; break;
            case 11: s = "OTCBB Only"; break;
            case 20: s = "OTC Pink Current"; break;
            case 21: s = "OTC Pink Limited"; break;
            case 22: s = "OTC Pink No Information"; break;
            case 30: s = "Grey Market"; break;
            case 50: s = "OTC Yellow"; break;
            case 51: s = "OTC Bonds"; break;
         }
      }
/*
 * 13-02-04 jcs  Build 52 : Mr Zee displays this
 *
      s = s.format( "[%02d] %s", tier, s );
 */
      return s;
   }


   static public String MktCategory( QuoddFeed.msg.Image img )
   {
      String s;
      char   mc;

      s = "";
      switch( (mc=img._mktCategory) ) {
         case 'N': s = "NYSE"; break;
         case 'A': s = "NYSE Markets (AMEX)"; break;
         case 'P': s = "NYSE Arca"; break;
         case 'Q': s = "NASDAQ Global Select"; break;
         case 'G': s = "NASDAQ Global"; break;
         case 'S': s = "NASDAQ Capital"; break;
         case 'Z': s = "BATS"; break;
         case ' ': s = "Not Available"; break;
      }
      s = s.format( "[0x%02x] %s", (int)mc, s );
      return s;
   }


   /////////////////////////
   // Angie / Chin Logic
   /////////////////////////
   static public boolean IsEligible( QuoddFeed.msg.EQTrade trd )
   {
      boolean bInElig;

      /*
       * 12-07-26 jcs  Build 34: From Angie's markup of Appx G w/ Red Pen
       */

      // 1) Settlement Type

      bInElig = false;
      switch( trd._setlType ) {
         case 2:  // Cash
         case 3:  // NextDay
         case 4:  // Seller
            bInElig = true;
            break;
      }

      // 2) Report Type

      switch( trd._rptType ) {
         case 6:  // Derivatively Prices
            bInElig = true;
            break;
      }

      // 3) Report Detail

      switch( trd._rptDetail ) {
         case  2: // Official Open
         case  3: // Official Close
         case  7: // Price Variation
         case  9: // Average Price
         case 11: // Prior Reference Price
            bInElig = true;
            break;
      }

      // 4) Report Flags

      bInElig |= IsOddLot( trd );
      bInElig |= IsOutOfSeq( trd );
      /*
       * 15-05-07 jcs  Build 95: From Angie / Chin E-mail:
       *    "We no longer want to filter the EQ_RPT_EXTENDED trade contidion
       *     for extended fields; Those are the trades we need to process but
       *     all other 'IsEligible' logic stays in place."
      if ( !bExtended )
         bInElig |= IsExtended( trd );
       */
      return !bInElig;
   }


   //////////////////////////////////
   //
   //       E Q Q u o t e
   //
   //////////////////////////////////

   static private final int QTE_OPEN          = 0x0002;
   static private final int QTE_BID_PROTECTED = 0x0004;
   static private final int QTE_ASK_PROTECTED = 0x0008;
   static private final int QTE_BID_UNPRICED  = 0x0010;
   static private final int QTE_ASK_UNPRICED  = 0x0020;
   static private final int QTE_BID_VALID     = 0x0040;
   static private final int QTE_ASK_VALID     = 0x0080;

   /*
    * Appx C - Quote Conditions
    */
   static public boolean IsOpen( QuoddFeed.msg.Image img )
   {
      return _IsOpen( img._qteFlags );
   }

   static public boolean IsOpen( QuoddFeed.msg.EQQuoteMM qte )
   {
      return _IsOpen( qte._qteFlags );
   }

   static public boolean CanDisplayBid( QuoddFeed.msg.Image img ) 
   {
      int flg = img._qteFlags;

      return _IsOpen( flg ) && _BidIsPriced( flg ); // && _IsValidBid( flg );
   }

   static public boolean CanDisplayBid( QuoddFeed.msg.EQQuoteMM qte ) 
   {
      int flg = qte._qteFlags;

      return _IsOpen( flg ) && _BidIsPriced( flg ); // && _IsValidBid( flg );
   }

   static public boolean CanDisplayAsk( QuoddFeed.msg.Image img )
   {
      int flg = img._qteFlags;

      return _IsOpen( flg ) && _AskIsPriced( flg ); // && _IsValidAsk( flg );
   }

   static public boolean CanDisplayAsk( QuoddFeed.msg.EQQuoteMM qte )
   {
      int flg = qte._qteFlags;

      return _IsOpen( flg ) && _AskIsPriced( flg ); // && _IsValidAsk( flg );
   }

   static public boolean IsOneSided( QuoddFeed.msg.EQQuote qte )
   {
      return IsCachedBid( qte ) || IsCachedAsk( qte );
   }

   static public boolean IsCachedBid( QuoddFeed.msg.EQQuote qte )
   {
      return !_IsSet( qte._qteFlags, QTE_BID_VALID );
   }

   static public boolean IsCachedAsk( QuoddFeed.msg.EQQuote qte )
   {
      return !_IsSet( qte._qteFlags, QTE_ASK_VALID );
   }


   /////////////////////////
   // Helpers
   /////////////////////////
   static private boolean _IsOpen( int flags )
   {
      return _IsSet( flags, QTE_OPEN );
   }

   static private boolean _BidIsPriced( int flags )
   {
      return !_IsSet( flags, QTE_BID_UNPRICED );
   }

   static private boolean _AskIsPriced( int flags )
   {
      return !_IsSet( flags, QTE_ASK_UNPRICED );
   }

   static private boolean _IsValidBid( int flags )
   {
      return _IsSet( flags, QTE_BID_VALID );
   }

   static private boolean _IsValidAsk( int flags )
   {
      return _IsSet( flags, QTE_ASK_VALID );
   }





   //////////////////////////////////
   //
   //         E Q B b o
   //
   //////////////////////////////////

   static private final int LULD_NOCOND     = 0x0000;
   static private final int LULD_EXECUTABLE = 0x0001;
   static private final int LULD_BID_NONEX  = 0x0002;
   static private final int LULD_ASK_NONEX  = 0x0004;
   static private final int LULD_BID_LIMIT  = 0x0008;
   static private final int LULD_ASK_LIMIT  = 0x0010;

   /*
    * Mar 8, 2013 E-mail from Alison Lubischer at Scot Trade
    */
   static public String LimitUpDown( QuoddFeed.msg.EQBbo bbo )
   {
      return _LimitUpDown( bbo._luldFlags );
   }

   static private final int R_LULD_BID_NONEX  = 0x0400;
   static private final int R_LULD_ASK_NONEX  = 0x0800;
   static private final int R_LULD_BID_LIMIT  = 0x1000;
   static private final int R_LULD_ASK_LIMIT  = 0x2000;

   static public String LimitUpDownR( QuoddFeed.msg.EQBbo bbo )
   {
      int flags;

      // Appx C

      flags = 0;
      if ( _IsSet( bbo._bboFlags, R_LULD_BID_NONEX ) )
         flags |= LULD_BID_NONEX;
      if ( _IsSet( bbo._bboFlags, R_LULD_BID_LIMIT ) )
         flags |= LULD_BID_LIMIT;
      if ( _IsSet( bbo._bboFlags, R_LULD_ASK_NONEX ) )
         flags |= LULD_ASK_NONEX;
      if ( _IsSet( bbo._bboFlags, R_LULD_ASK_LIMIT ) )
         flags |= LULD_ASK_LIMIT;
      if ( flags == 0 )
         flags = LULD_EXECUTABLE;
      return _LimitUpDown( flags );
   }

   static public String LimitUpDown( QuoddFeed.msg.Image img )
   {
      int flags;

      flags = (int)img._ETPSharesOut;
      return _LimitUpDown( flags );
   }


   /////////////////////////
   // Helpers
   /////////////////////////
   static public String _LimitUpDown( int flags )
   {
      boolean bExec, bBidLT, bBidEQ, bAskGT, bAskEQ;

      // Special case - BASIC

      if ( _bBasic )
         return " ";

      // OK to process

      bExec  = _IsSet( flags, LULD_EXECUTABLE );
      bBidLT = _IsSet( flags, LULD_BID_NONEX );
      bBidEQ = _IsSet( flags, LULD_BID_LIMIT );
      bAskGT = _IsSet( flags, LULD_ASK_NONEX );
      bAskEQ = _IsSet( flags, LULD_ASK_LIMIT );
      if ( bExec )
         return "A";  // BBO Executable
      else if ( bBidLT ) {
         if ( bAskGT )
            return "D";  // BBO Non-Ex
         else if ( bAskEQ )
            return "H";  // ASK Limit / BID Non-Ex
         return "B";     // BID Non-Ex
      }
      else if ( bBidEQ ) {
         if ( bAskGT )
            return "G";  // BID Limit / ASK Non-Ex
         else if ( bAskEQ )
            return "I";  // BBO Limit
         return "E";     // BID Limit
      }
      else if ( bAskGT )
         return "C";     // ASK Non-Ex
      else if ( bAskEQ )
         return "F";     // ASK Limit
      return " ";
   }


   //////////////////////////////////
   //
   //         I m a g e
   //
   //////////////////////////////////

   static public String FinancialStatus( QuoddFeed.msg.Image img )
   {
      String s;

      // For Nasdaq listed securities

      switch( img._financialSts ) {
         case 'C': 
            return "Creations and/or Redemptions Suspended";
         case 'D': 
            return "Deficient: Issuer Failed to Meet NASDAQ Listing Req'ts";
         case 'E': 
            return "Delinquent: Issuer Missed Regulatory Filing Deadline";
         case 'Q': 
            return "Bankrupt: Issuer Has Filed for Bankruptcy";
         case 'N': 
            return "Normal: Issuer Is NOT Deficient, Delinquent, or Bankrupt";
         case 'G': 
            return "Deficient and Bankrupt";
         case 'H': 
            return "Deficient and Delinquent";
         case 'J': 
            return "Delinquent and Bankrupt";
         case 'K': 
            return "Deficient, Delinquent, and Bankrupt";
      }

      // NYSE / SIAC

      switch( img._financialSts ) {
         case '0':
            s  = "Not Bankrupt / Not Below Continuing Listing Standards";
            s += " / Not Late Filing";
            return s;
         case '1':
            return "Bankrupt"; 
         case '2':
            s  = "Below Continuing Listing Standards";
            s += " (For NYSE, NYSE AMEX & Regional listed issues)";
            return s;
         case '3':
            s  = "Bankrupt & Below Continuing Listing Standards";
            s += " (For NYSE, NYSE AMEX & Regional listed issues)";
            return s;
         case '4':
            return "Late Filing"; 
         case '5':
            return "Bankrupt & Late Filing"; 
         case '6':
            s  = "Below Continuing Listing Standards & Late Filing";
            s += " (For NYSE, NYSE AMEX & Regional listed issues)";
            return s;
         case '7':
            s  = "Bankrupt, Below Continuing Listing Standards & ";
            s += "Late Filing (For NYSE, NYSE AMEX & Regional listed issues)";
            return s;
      }
      return "".format( "[0x%02x] : Unknown", (int)img._financialSts );
   }
}
