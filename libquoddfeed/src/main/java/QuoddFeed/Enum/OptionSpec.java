/******************************************************************************
*
*  OptionSpec.java
*     UltraFeed Option Appendices
*
*  REVISION HISTORY:
*     31 MAY 2012 jcs  Created.
*     20 JUL 2012 jcs  Build 29: QuoddFeed.Enum.UltraFeed
*     22 AUG 2012 jcs  Build 34: TradeFlags
*      6 SEP 2012 jcs  Build 35: XxxIsValid()
*     25 OCT 2012 jcs  Build 44: [%d] %s for flags / bitmasks
*     28 NOV 2012 jcs  Build 46: Month code A thru X; Mod 12
*     14 NOV 2013 jcs  Build 71: Strike() / Expiration() / PutOrCall()
*     20 NOV 2013 jcs  Build 71a:Strike() : bbo (MktCtrs)
*      1 APR 2014 jcs  Build 76: IsOneSided()
*     16 JUN 2014 jcs  Build 78: Contract Size from OptionRoot()
*     27 APR 2015 jcs  Build 92: De-lint
*
*  (c) 1994-2015 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.Enum;

import java.util.*;

/////////////////////////////////////////////////////////////////
//
//                c l a s s    O p t i o n S p e c
//
/////////////////////////////////////////////////////////////////
/**
 * The OptionSpec class encapsulates the UltraFeed Equity appendices.
 */
public class OptionSpec extends QuoddFeed.Enum.UltraFeed
{
   //////////////////////////////////
   //
   //         O P B B o
   //
   //////////////////////////////////
   static private final int BestBidValid  = 0x0002;
   static private final int BestAskValid  = 0x0004;
   /*
    * Appx A - Quote Flags
    */
   static public boolean IsOneSided( QuoddFeed.msg.OPBbo bbo )
   {
      return IsCachedBid( bbo ) || IsCachedAsk( bbo );
   }

   static public boolean IsCachedBid( QuoddFeed.msg.OPBbo bbo )
   {
      return !_IsSet( bbo._qteFlags, BestBidValid );
   }

   static public boolean IsCachedAsk( QuoddFeed.msg.OPBbo bbo )
   {
      return !_IsSet( bbo._qteFlags, BestAskValid );
   }


   //////////////////////////////////
   //
   //       O P T r a d e
   //
   //////////////////////////////////
   static private final int eligibleLast    = 0x0001;
   static private final int eligibleVolume  = 0x0002;
   static private final int late            = 0x0004;
   static private final int outOfSequence   = 0x0008;
   static private final int resumed         = 0x0010;
   /*
    * Appx B - Trade / Trade Cxl Flags
    */
   static public String TradeFlags( QuoddFeed.msg.OPTrade trd )
   {
      String            s;
      ArrayList<String> v;
      int               i, n, flg;

      // Bitmask

      v   = new ArrayList<String>();
      flg = trd._trdFlags;
      if ( PriceIsValid( trd ) )
         v.add( new String( "Last" ) );
      if ( VolumeIsValid( trd ) )
         v.add( new String( "Volume" ) );
      if ( _IsSet( flg, late ) )
         v.add( new String( "Late" ) );
      if ( _IsSet( flg, outOfSequence ) )
         v.add( new String( "OutOfSequence" ) );
      if ( _IsSet( flg, resumed ) )
         v.add( new String( "Resumed" ) );
      n = v.size();
      s = "Undefined";
      s = s.format( "[0x%04x] ", flg );
      for ( i=0; i<n; i++ ) {
         s += ( i>0 ) ? "," : "";
         s += v.get( i );
      }
      return s;
   }

   static public boolean PriceIsValid( QuoddFeed.msg.OPTrade trd )
   {
      return _IsSet( trd._trdFlags, eligibleLast );
   }

   static public boolean VolumeIsValid( QuoddFeed.msg.OPTrade trd )
   {
      return _IsSet( trd._trdFlags, eligibleVolume );
   }



   //////////////////////////////////
   //                                
   //       N a m i n g
   //
   //////////////////////////////////
   static private String[] _mons = { "JAN", "FEB", "MAR", "APR",
                                     "MAY", "JUN", "JUL", "AUG",
                                     "SEP", "OCT", "NOV", "DEC" };

   /**
    * UltraCache AIG2 Naming : <CONTRACT_SIZE=100, ... >
    */
   static public String Description( String tkr, String aig2 )
   {
      String desc, und, shr, s, re1, re2, root;
      char   ch;
      int    cSz;

      /*
       * 14-06-16 jcs  Build 78 : Contract Size from root.
       *               From Hanji - AAPL7 / GOGL8 : Size = 10
       */

      root = OptionRoot( tkr );
      ch   = root.charAt( root.length()-1 );
      cSz  = 100;
      switch( ch ) {
         case '7':
         case '8': cSz = 10; break;
      }

      /*
       * 100<U>, 6.1<C>, 53<S>AIGWS ->
       *    (100 shares of AIG, 6.1 cash, 53 shares of AIGWS)
       */

      desc = DescriptionFromName( tkr );
      und  = OptionRoot( tkr ).replaceAll( "[0-9]", "" );
      shr  = " shares of " + und;
      re1  = und + "WS";
      re2  = und + "+";
      s    = aig2.replaceAll( "<U>", shr );
      s    = s.replaceAll( "<C>", " cash" );
      s    = s.replaceAll( "<S>", " shares of " );
      s    = s.replaceAll( re1, re2 );
      return s.format( "%s (CONTRACT_SIZE=%d, %s)", desc, cSz, s );
   }


   /**
    * UltraCache Naming Convention : O:<root>\\...
    */
   static public String DescriptionFromName( String tkr )
   {
      String   wip, und, rtn, exp, prc, pm, pc;
      String[] kv;
      char     y0, y1, mon;
      int      m, nk;

      /*
       * O:AIG\14M18\32.00 -> AIG Jan 2014 32.00 PUT 
       *    (The 18th day is included - Expires 18 Jan 2014)
       */

      und = OptionRoot( tkr );
      wip = new String( tkr ).replace( '\\', '-' );
      kv  = wip.split("-");
      nk  = kv.length;
      rtn = und;
      if ( nk > 2 ) {
         exp = kv[1];
         prc = kv[2];
         y0  = exp.charAt( 0 );
         y1  = exp.charAt( 1 );
         mon = exp.charAt( 2 );
         m   = (int)( mon - 'A' );
         pc  = ( mon < 'M' ) ? "CALL" : "PUT";
         m  %= 12; 
         pm  = ( m <= 11 ) ? _mons[m] : "???";
         rtn = rtn.format( "%s %s 20%c%c %s %s", und, pm, y0,y1, prc, pc );
      }
      return rtn;
   }

   /**
    * UltraCache Naming Convention : O:<root>\\...
    */
   static public String OptionRoot( String tkr )
   {
      String   wip;
      String[] kv;

      // O:AIG\14M18\32.00 -> AIG

      wip = new String( tkr ).replace( '\\', '-' );
      kv  = wip.split("-");
      return kv[0].substring( 2 );
   }

   static public int Expiration( String tkr )
   {
      String   wip, exp;
      String[] kv;
      int      y0, y1, d0, d1, m, nk, rtn;

      // O:AIG\14M18\32.00 -> 20140118

      wip = new String( tkr ).replace( '\\', '-' );
      kv  = wip.split("-");
      nk  = kv.length;
      rtn = 0;
      if ( nk > 2 ) {
         exp = kv[1];
         y0   = (int)( exp.charAt( 0 ) - '0' );
         y1   = (int)( exp.charAt( 1 ) - '0' );
         m    = (int)( exp.charAt( 2 ) - 'A' );
         d0   = (int)( exp.charAt( 3 ) - '0' );
         d1   = (int)( exp.charAt( 4 ) - '0' );
         m   %= 12;
         rtn  = ( ( y0*10 ) + y1 );
         rtn += 2000;
         rtn *= 10000;
         rtn += ( (m+1) * 100 );
         rtn += ( ( d0*10 ) + d1 );
      }
      return rtn;
   }

   static public double Strike( String tkr )
   {
      String   wip, prc, bbo;
      String[] kv;
      double   rtn;
      int      nk;

      // O:AIG\14M18\32.00 -> 32.00

      bbo = tkr.split( "/" )[0];
      wip = new String( bbo ).replace( '\\', '-' );
      kv  = wip.split("-");
      nk  = kv.length;
      rtn = 0.0;
      if ( nk > 2 ) {
         prc = kv[2];
         rtn = Double.parseDouble( prc );
      }
      return rtn;
   }

   static public String PutOrCall( String tkr )
   {
      String   wip, exp, rtn;
      String[] kv;
      int      m, nk;

      // O:AIG\14M18\32.00 -> PUT

      wip = new String( tkr ).replace( '\\', '-' );
      kv  = wip.split("-");
      nk  = kv.length;
      rtn = "Undefined";
      if ( nk > 2 ) {
         exp = kv[1];
         m   = (int)( exp.charAt( 2 ) - 'A' );
         rtn  = ( m < 'M' ) ? "CALL" : "PUT";
      }
      return rtn;
   }
}
