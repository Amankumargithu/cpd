/******************************************************************************
*
*  BondSpec.java
*     UltraFeed Bond Appendices
*
*  REVISION HISTORY:
*     16 MAR 2013 jcs  Created.
*     27 APR 2015 jcs  Build 92: De-lint
*
*  (c) 1994-2015 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.Enum;

import java.util.*;

/////////////////////////////////////////////////////////////////
//
//                c l a s s    B o n d S p e c
//
/////////////////////////////////////////////////////////////////
/**
 * The BondSpec class encapsulates the UltraFeed Bond appendices.
 */
public class BondSpec extends QuoddFeed.Enum.UltraFeed
{

   //////////////////////////////////
   //
   //      B O N D Q u o t e
   //
   //////////////////////////////////

   static private final int QTE_BENCHMARK = 0x0001;
   static private final int QTE_DELETED   = 0x0002;
   static private final int QTE_NONTRADE  = 0x0004;
   static private final int QTE_CLOSING   = 0x0008;
   static private final int QTE_OPENING   = 0x0010;
   static private final int QTE_SLOWBID   = 0x0020;
   static private final int QTE_SLOWASK   = 0x0040;
   static private final int QTE_LRP       = 0x0080;
   static private final int QTE_FLATPRICE = 0x0100;
   static private final int QTE_NONFIRM   = 0x0200;
   static private final int QTE_SLOWLIST  = 0x0400;

   /*
    * Appx C - Quote Conditions
    */
   static public String QuoteCond( QuoddFeed.msg.BONDQuote qte )
   {
      String            s;
      ArrayList<String> v;
      int               i, n, flg;

      // Bitmask

      v   = new ArrayList<String>();
      flg = qte._qteFlags;
      if ( _IsSet( flg, QTE_BENCHMARK ) ) v.add( new String( "Benchmark" ) );
      if ( _IsSet( flg, QTE_DELETED ) )   v.add( new String( "Deleted" ) );
      if ( _IsSet( flg, QTE_NONTRADE ) )  v.add( new String( "NonTradable" ) );
      if ( _IsSet( flg, QTE_CLOSING ) )   v.add( new String( "Closing" ) );
      if ( _IsSet( flg, QTE_OPENING ) )   v.add( new String( "Opening" ) );
      if ( _IsSet( flg, QTE_SLOWBID ) )   v.add( new String( "SlowBid" ) );
      if ( _IsSet( flg, QTE_SLOWASK ) )   v.add( new String( "SlowAsk" ) );
      if ( _IsSet( flg, QTE_LRP ) )       v.add( new String( "LRP" ) );
      if ( _IsSet( flg, QTE_FLATPRICE ) ) v.add( new String( "FlatPricing" ) );
      if ( _IsSet( flg, QTE_NONFIRM ) )   v.add( new String( "NonFirm" ) );
      if ( _IsSet( flg, QTE_SLOWLIST ) )  v.add( new String( "SlowList" ) );
      n = v.size();
      s = "Undefined";
      s = s.format( "[%02d] ", flg );
      for ( i=0; i<n; i++ ) {
         s += ( i>0 ) ? "," : "";
         s += v.get( i );
      }
      return s;
   }
}
