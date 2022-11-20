/******************************************************************************
*
*  OptionDump.java
*     Higher-order Option Chain object
*
*  REVISION HISTORY:
*     17 APR 2012 jcs  Created (from OptionChain).
*     15 AUG 2012 jcs  Build 32: No mo _bbo
*     14 OCT 2012 jcs  Build 42: One-step Subscribe and Register
*     27 APR 2015 jcs  Build 92: De-lint
*
*  (c) 1994-2015 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.MD;

import java.lang.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
// 
//                c l a s s    O p t i o n D u m p
//
/////////////////////////////////////////////////////////////////
/**
 * OptionDump is an higher-order QuoddFeed.MD class for consuming and 
 * processing update messages for all ticker of an Option Chain
 * <p>
 * We store quotes in an unsorted HashMap collection.
 */
public class OptionDump extends IUpdate
{
   ////////////////////
   // Instance Members
   ////////////////////
   protected UltraChan                _uChan;
   protected String                   _tkr;
   protected String                   _arg;
   private   int                      _streamID;
   private   HashMap<Integer, String> _recs;

   //////////////////////
   // Constructor
   //////////////////////
   public OptionDump( UltraChan uChan, String tkr )
   {
      // Guts

      _uChan  = uChan;
      _tkr    = tkr;
      _arg    = "User-defined argument";
      _recs   = new HashMap<Integer, String>();
      Open();
   }


   //////////////////////
   // Open / Close
   //////////////////////
   public void Open()
   {
      // Subscribe

      _streamID  = _uChan.GetOptionChain( _tkr, _arg, this );
   }

   public void Close()
   {
      Iterator it;
      int      sid;
/*
 * 12-04-18 jcs  Too lazy to figure this out ...
 *
      it = _recs.keySet().iterator();
      while( it.hasNext() ) {
         sid = (int)it.next();
         _uChan.Unsubscribe( sid );
      }
 */
      _recs.clear();
   }


   //////////////////////
   // IUpdate Interface - Blob
   //////////////////////
   /**
    * Callback invoked when BlobList containing list of participants is received.
    *
    * @param qry Query
    * @param b BlobTable
    */
   public void OnBlobList( String qry, BlobList b )
   {
      String tkr;
      int    i, nt, sid;

      nt = b.lst().length;
System.out.printf( "%d tickers\n", nt );
      for ( i=0; i<nt; i++ ) {
         tkr = b.lst()[i];
         sid = _uChan.Subscribe( tkr, null, this );
         _recs.put( sid, new String( tkr ) );
      }
   }


   //////////////////////
   // IUpdate Interface - Stream
   //////////////////////
   /**
    * Callback invoked when an unknown QuoddMsg is received.
    *
    * @param StreamName Name of this Data Stream
    * @param qm QuoddMsg
    */
   public void OnQuoddMsg( String StreamName, QuoddMsg qm )
   {
      // Don't care, for now
   }

   /**
    * Callback invoked when status received on a data stream
    *
    * @param StreamName Name of this Data Stream 
    * @param sts Status
    */
   public void OnStatus( String StreamName, Status sts )
   {
      // Don't care, for now
   }

   /**
    * Callback invoked when an Initial Image is received
    *
    * @param StreamName Name of this Data Stream
    * @param img Image
    */
   public void OnImage( String StreamName, Image img )
   {
      _OnMsg( StreamName, "IMAGE", img );
      System.out.printf( "\n" );
      System.out.print( img.Dump() );
   }

   /**
    * Callback invoked when Equity Quote BBO Message is received 
    *
    * @param StreamName Name of this Data Stream
    * @param q OPBbo
    */
   public void OnUpdate( String StreamName, OPBbo q )
   {
      _OnMsg( StreamName, "BBO  ", q );
      System.out.printf( "{%s} %6.2f x %6.2f {%s}; %dx%d\n",
         q._bidMktCtr, q._bid, q._ask, q._askMktCtr, q._bidSize, q._askSize );
   }

   /**
    * Callback invoked when Equity Quote Message is received
    *
    * @param StreamName Name of this Data Stream
    * @param q OPQuote
    */
   public void OnUpdate( String StreamName, OPQuote q )
   {
      _OnMsg( StreamName, "QUOTE", q );
      System.out.printf( "{%s} %6.2f x %6.2f\n", q._mktCtr, q._bid, q._ask );
   }

   /**
    * Callback invoked when an Option Trade message is received
    *
    * @param StreamName Name of this Data Stream
    * @param trd OPTrade
    */
   public void OnUpdate( String StreamName, OPTrade trd )
   {
      String pt;

      pt = trd.prcTck();
      _OnMsg( StreamName, "TRADE", trd );
      System.out.printf( "(%s) %d @ %6.2f", pt, trd._trdVol, trd._trdPrc );
      System.out.printf( " {%s}\n", trd._mktCtr );
      System.out.printf( "   _netChg = %.4f; ", trd._netChg );
      System.out.printf( "_pctChg = %.4f\n", trd._pctChg );
      System.out.printf( "   _tnOvr = %d; ", trd._tnOvr );
      System.out.printf( "_acVol = %d; ", trd._acVol );
      System.out.printf( "_vwap = %.4f\n", trd._vwap );
      System.out.printf( "   _openPrc = %.4f; ", trd._openPrc );
      System.out.printf( "_openVol = %d\n", trd._openVol );
   }


   //////////////////////
   // Helpers
   //////////////////////
   private void _OnMsg( String StreamName, String mt, QuoddMsg m )
   {
      String  pt;
      boolean bRTL;

      if ( (pt=m.tkr()) == null )
         pt = StreamName;
      bRTL = ( m.RTL() != -1 );
      System.out.printf( "%s %-6s {%03d} %-6s ", m.pTimeMs(), mt, m.tag(), pt );
      if ( bRTL )
         System.out.printf( "<%06d> ", m.RTL() );
   }
}
