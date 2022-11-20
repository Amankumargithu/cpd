/******************************************************************************
*
*  Level2Dump.java
*     Level2 update dump
*
*  REVISION HISTORY:
*     22 APR 2012 jcs  Created (from Level2.java).
*     16 MAY 2012 jcs  Build 20: v0.19: OnOpenComplete()
*     18 JUN 2012 jcs  Build 23: v0.20: Log EQTrade
*     14 OCT 2012 jcs  Build 42: v0.24: One-step Subscribe and Register
*
*  (c) 1994-2012 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.MD;

import java.lang.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
// 
//                c l a s s    L e v e l 2
//
/////////////////////////////////////////////////////////////////
/**
 * Level2Dump is an higher-order QuoddFeed.MD class for consuming and 
 * processing update messages for Level 2 market makers and market centers.
 * and dumps to stdout
 */
public class Level2Dump extends IUpdate
{
   ////////////////////
   // Instance Members
   ////////////////////
   protected UltraChan _uChan;
   protected String    _tkr;
   protected boolean   _bLvl2Only;
   protected String    _arg;
   private   int       _streamID;

   //////////////////////
   // Constructor
   //////////////////////
   public Level2Dump( UltraChan uChan, 
                      String    tkr, 
                      boolean   bLvl2Only )
   {
      // Guts

      _uChan     = uChan;
      _tkr       = tkr;
      _bLvl2Only = bLvl2Only;
      _arg       = "User-defined argument";
      Open();
   }


   //////////////////////
   // Open / Close
   //////////////////////
   public void Open()
   {
      // Subscribe

      _streamID  = _uChan.SubscribeLevel2( _tkr, _arg, _bLvl2Only, this );
      System.out.printf( ANSI_CLEAR );
   }

   public void Close()
   {
      _uChan.UnsubscribeLevel2( _streamID );
   }


   //////////////////////
   // IUpdate Interface
   //////////////////////
   /**
    * Callback invoked when an unknown QuoddMsg is received.
    *
    * @param StreamName Name of this Data Stream
    * @param qm QuoddMsg
    */
   public void OnQuoddMsg( String StreamName, QuoddMsg qm )
   {
      char mt, mt2;

      // Open Complete?

      if  ( (mt=qm.mt()) == _mtSTREAM_OPN ) {
         System.out.printf( "Open Complete\n" );
         return;
      }
      mt2 = qm.mtSub();
      _OnLvl2Msg( StreamName, "QUODDMSG", qm );
      System.out.printf( "mt=%c; mtSub=%c; Len=%d\n", qm.mt(), mt2, qm.len() );
   }

   /**
    * Callback invoked when status received on a data stream
    *
    * @param StreamName Name of this Data Stream 
    * @param sts Status
    */
   public void OnStatus( String StreamName, Status sts )
   {
      _OnLvl2Msg( StreamName, "DEAD", sts );
      System.out.printf( "%s\n", sts.Text() );
   }

   /**
    * Callback invoked when an Initial Image is received
    *
    * @param StreamName Name of this Data Stream
    * @param img Image
    */
   public void OnImage( String StreamName, Image img )
   {
      EQQuoteMM mm;

      // Populate to look like EQQuoteMM

      mm          = new EQQuoteMM();
      mm._bid     = img._bid;
      mm._bidSize = img._bidSize;
      mm._ask     = img._ask;
      mm._askSize = img._askSize;
      mm._mmid    = img._bidMktCtr;
      _OnLvl2Msg( StreamName, "IMAGE", img );
      _Dump ( mm );
   }

   /**
    * Callback invoked when an Short- or Long-Form Equity Quote message is 
    * received
    *
    * @param StreamName Name of this Data Stream
    * @param q EQQuote
    */
   public void OnUpdate( String StreamName, EQQuote q )
   {
      EQQuoteMM mm;

      // Now populate to look like EQQuoteMM

      mm          = new EQQuoteMM();
      mm._bid     = q._bid;
      mm._bidSize = q._bidSize;
      mm._ask     = q._ask;
      mm._askSize = q._askSize;
      mm._mmid    = q._mktCtr;
      _OnLvl2Msg( StreamName, "EQQuote", q );
      _Dump( mm );
   }

   /**
    * Callback invoked when an Short-or Long-Form Equity BBO Quote message
    * is received
    *
    * @param StreamName Name of this Data Stream
    * @param q EQBbo
    */
   public void OnUpdate( String StreamName, EQBbo q )
   {
      EQQuoteMM mm;

      // Populate to look like EQQuoteMM

      mm          = new EQQuoteMM();
      mm._bid     = q._bid;
      mm._bidSize = q._bidSize;
      mm._ask     = q._ask;
      mm._askSize = q._askSize;
      mm._mmid    = q._bidMktCtr;
      _OnLvl2Msg( StreamName, "EQBbo", q );
      _Dump ( mm );
   }

   /**
    * Callback invoked when an Short- or Long-Form Equity Market Maker Quote 
    * message is received
    *
    * @param StreamName Name of this Data Stream
    * @param q EQQuoteMM
    */
   public void OnUpdate( String StreamName, EQQuoteMM q )
   {
      _OnLvl2Msg( StreamName, "EQQteMM", q );
      _Dump ( q );
   }

   /**
    * Callback invoked when an Short-or Long-Form Equity Market Maker BBO 
    * Quote message is received
    *
    * @param StreamName Name of this Data Stream
    * @param q EQBboMM
    */
   public void OnUpdate( String StreamName, EQBboMM q )
   {
      EQQuoteMM mm;

      // Populate to look like EQQuoteMM

      mm          = new EQQuoteMM();
      mm._bid     = q._bid;
      mm._bidSize = q._bidSize;
      mm._ask     = q._ask;
      mm._askSize = q._askSize;
      mm._mmid    = q._bidMmid;
      _OnLvl2Msg( StreamName, "EQBboMM", q );
      _Dump ( mm );
   }

   /**
    * Callback invoked when an Equity Trade message is received
    *
    * @param StreamName Name of this Data Stream
    * @param trd EQTrade
    */
   public void OnUpdate( String StreamName, EQTrade trd )
   {
      _OnLvl2Msg( StreamName, "EQTrade", trd );
      System.out.printf( "\n" );
   }


   //////////////////////
   // Helpers
   //////////////////////
   private void _OnLvl2Msg( String StreamName, String mt, QuoddMsg m )
   {
      String  pt, ps;
      boolean bRTL;

      if ( (pt=m.tkr()) == null )
         pt = StreamName;
      bRTL = ( m.RTL() != -1 );
      ps   = m.pTimeMs();
      System.out.printf( "%s %-7s {%03d} %-10s ", ps, mt, m.tag(), pt );
      if ( bRTL )
         System.out.printf( "<%06d> ", m.RTL() );
   }

   private void _Dump( EQQuoteMM q )
   {
      System.out.printf( "{%-4s} %6.2f x %6.2f {%-4s}; %dx%d\n",
         q._mmid, q._bid, q._ask, q._mmid, q._bidSize, q._askSize );
   }
}
