/******************************************************************************
*
*  OptionTrade.java
*     Just dump trades from Options Chain
*
*  REVISION HISTORY:
*      9 APR 2012 jcs  Created.
*     17 APR 2012 jcs  Build 16: OnUpdate( StreamName, ... )
*     14 OCT 2012 jcs  Build 42: One-step Subscribe and Register
*
*  (c) 2011-2012 Quodd Financial
*******************************************************************************/
package QuoddFeed.MD;

import java.lang.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
// 
//                c l a s s    O p t i o n T r a d e
//
/////////////////////////////////////////////////////////////////
/**
 * OptionTrade is an higher-order QuoddFeed.MD class for showing all 
 * TRADES from an Option Chain
 */
public class OptionTrade extends IUpdate
{
   ////////////////////
   // Instance Members
   ////////////////////
   protected UltraChan _uChan;
   protected String    _tkr;
   protected String    _arg;
   private   int       _streamID;

   //////////////////////
   // Constructor
   //////////////////////
   public OptionTrade( UltraChan uChan, String tkr )
   {
      // Guts

      _uChan  = uChan;
      _tkr    = tkr;
      _arg    = "User-defined argument";
      Open();
   }


   //////////////////////
   // Open / Close
   //////////////////////
   public void Open()
   {
      // Subscribe

      System.out.printf( ANSI_CLEAR );
      _streamID  = _uChan.GetOptionChain( _tkr, _arg, this );

      // Clear Screen

      System.out.printf( ANSI_HOME );
   }


   //////////////////////
   // IUpdate Interface - Blob
   //////////////////////
   /**
    * Callback invoked when BlobList containing list of options is received.
    *
    * @param qry Query
    * @param b BlobTable
    */
   public void OnBlobList( String qry, BlobList b )
   {
      String tkr;
      int    i, nt, sid;

      nt = b.lst().length;
      for ( i=0; i<nt; i++ ) {
         tkr = b.lst()[i];
         sid = _uChan.Subscribe( tkr, null, this );
      }
   }


   //////////////////////
   // IUpdate Interface - Stream
   //////////////////////
   /**
    * Called when an OPTrade update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPTrade m )
   {
      String  pt;
      boolean bRTL;

      if ( (pt=m.tkr()) == null )
         pt = StreamName;
      bRTL = ( m.RTL() != -1 );
      System.out.printf( "%s TRADE {%03d} %-6s ", m.pTimeMs(), m.tag(), pt );
      if ( bRTL )
         System.out.printf( "<%06d> ", m.RTL() );

      // Just log for now

      pt = m.prcTck();
      System.out.printf( "(%s) %d @ %6.2f", pt, m._trdVol, m._trdPrc );
      System.out.printf( " {%s}\n", m._mktCtr );
      System.out.printf( "   _netChg = %.4f; ", m._netChg );
      System.out.printf( "_pctChg = %.4f\n", m._pctChg );
      System.out.printf( "   _tnOvr = %d; ", m._tnOvr );
      System.out.printf( "_acVol = %d; ", m._acVol );
      System.out.printf( "_vwap = %.4f\n", m._vwap );
   }
}
