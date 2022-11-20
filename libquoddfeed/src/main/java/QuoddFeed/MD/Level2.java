/******************************************************************************
*
*  Level2.java
*     Higher-order Level 2 object
*
*  REVISION HISTORY:
*     14 JAN 2012 jcs  Created.
*     18 JAN 2012 jcs  Build 12: EQQuote for MktCtrs
*      1 FEB 2012 jcs  Build 13: Cmd.ANSI_CLEAR
*     21 FEB 2012 jcs  Build 14: OnLevel2()
*     21 MAR 2012 jcs  Build 15: SetTime()
*     17 APR 2012 jcs  Build 16: OnUpdate( StreamName, ... )
*     18 APR 2012 jcs  Build 16: v0.15: Image._bidMktCtr / Image._askMktCtr
*     22 APR 2012 jcs  Build 17: v0.15: _tSnap
*      7 MAY 2012 jcs  Build 18: v0.16: _UpdateL2() : DELL/NITE = NITE
*     16 MAY 2012 jcs  Build 20: v0.19: OnOpenComplete()
*      4 JUN 2012 jcs  Build 22: v0.20: _UpdateL2 : Divide all MMID's by 100
*     14 OCT 2012 jcs  Build 42: v0.24: One-step Subscribe and Register
*     15 JUN 2013 jcs  Build 64: v0.24: Shitcan !img.RTL(); CanDisplayBid()
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
//                c l a s s    L e v e l 2
//
/////////////////////////////////////////////////////////////////
/**
 * Level2 is an higher-order QuoddFeed.MD class for consuming and 
 * processing update messages for Level 2 market makers and market centers.
 * <p>
 * We store quotes in 2 collection:
 *   1) Unsorted collection key'ed by market maker / center
 *   2) Sorted collection key'ed by price; We stupidly rebuild this on
 *      every update
 */
public class Level2 extends IUpdate
{
   ////////////////////
   // Instance Members
   ////////////////////
   protected UltraChan                     _uChan;
   protected String                        _tkr;
   private   int                           _maxRow;
   private   double                        _tSnap;
   protected boolean                       _bLvl2Only;
   protected boolean                       _bCanDpy;
   protected String                        _arg;
   private   int                           _streamID;
   private   HashMap<String, Level2Record> _prcs;
   protected TreeSet<Level2Record>         _bids;
   protected TreeSet<Level2Record>         _asks;
   protected long                          _tPub;  // Time we last published

   //////////////////////
   // Constructor
   //////////////////////
   public Level2( UltraChan uChan, 
                  String    tkr, 
                  int       maxRow, 
                  double    tSnap, 
                  boolean   bLvl2Only )
   {
      // Guts

      _uChan     = uChan;
      _tkr       = tkr;
      _maxRow    = maxRow;
      _tSnap     = tSnap;
      _bLvl2Only = bLvl2Only;
      _bCanDpy   = !UCconfig.HasKey( "UC_L2CHK_CANDPY" );
      _arg       = "User-defined argument";
      _prcs      = new HashMap<String, Level2Record>();
      _bids      = new TreeSet<Level2Record>( new L2Comparator( true ) );
      _asks      = new TreeSet<Level2Record>( new L2Comparator( false ) );
      _tPub      = 0;
      Open();
   }


   //////////////////////
   // Access
   //////////////////////
   public double BestBid()
   {
      Iterator     it;
      Level2Record b;
      double       rtn;

      it = _bids.iterator();
      b  = it.hasNext() ? (Level2Record)it.next() : null;
      return ( b != null ) ? b._dBid : 0.0;
   }

   public double BestAsk()
   {
      Iterator     it;
      Level2Record b;
      double       rtn;

      it = _asks.iterator();
      b  = it.hasNext() ? (Level2Record)it.next() : null;
      return ( b != null ) ? b._dAsk : 0.0; 
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
      _prcs.clear();
      _bids.clear();
      _asks.clear();
   }


   //////////////////////
   // Level2 Interface
   //////////////////////
   /**
    * Callback invoked when the Level2 book updates
    */
   public void OnLevel2()
   {
      _ShowLvl2();
   }

   /**
    * Callback invoked when the Level2 book is completely opened
    */
   public void OnOpenComplete()
   {
System.out.printf( "Open Complete\n" );
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
         OnOpenComplete();
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
      String[]  kv;
      String    pt, mmid;
      int       iBidCtr, iAskCtr;
      boolean   bMktMkr, bPt, bMmid;
      double    d0;

      // Pre-condition

      if ( img.RTL() == 0 )
         return;

      // Market Center??

      pt  = img.tkr();
      bPt = ( pt != null );
      kv  = "".split("/");
      if ( bPt )
         kv = pt.split("/");
      bMmid  = ( img._closeDate == 19691231 );
      bMmid &= ( ( kv.length > 1 ) && ( kv[1].length() == 4 ) );

      // Figure out MMID; from name, if "????"

      mmid = img._bidMktCtr;
      if ( mmid.equals( "????" ) ) {
         iBidCtr = img._iBidMktCtr;
         iAskCtr = img._iAskMktCtr;
         bMktMkr = ( iBidCtr != 0 ) && ( iBidCtr == iAskCtr );
         if ( bPt && bMktMkr && ( kv.length > 1 ) )
            mmid = kv[1];
         else
            mmid = img._priMktCtr;
      }

      // Now populate to look like EQQuoteMM

      pt           = img.tkr();
      mm           = new EQQuoteMM();
      mm._qteFlags = img._qteFlags;
      mm._bid      = img._bid;
      mm._bidSize  = img._bidSize;
      mm._ask      = img._ask;
      mm._askSize  = img._askSize;
      mm._mmid     = mmid; 
      mm.SetTime( img._bidTime );

      // Allow Image's to go thru ...

      d0       = _tSnap;
      _tSnap   = 0.0;
      _UpdateL2( mm, mmid, bMmid );
      _tSnap   = d0;
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
      String[]  kv;
      String    pt, mmid;

      // Now populate to look like EQQuoteMM

      mm           = new EQQuoteMM();
      mm.SetTime( q );
      mm._qteFlags = q._qteFlags;
      mm._bid      = q._bid;
      mm._bidSize  = q._bidSize;
      mm._ask      = q._ask;
      mm._askSize  = q._askSize;
      mm._mmid     = q._mktCtr;
      _UpdateL2( mm, q._mktCtr, false );
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
      String bm, am;

      // Just log for now

      bm = q._bidMktCtr;
      am = q._askMktCtr;
      _OnLvl2Msg( StreamName, "BBO", q );
      System.out.printf( "{%s} %6.3f x %6.3f {%s}\n", bm, q._bid, q._ask, am );
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
      String[]  kv;
      String    pt, mmid;
      boolean   bMktMkr;

      mmid = q._mmid;
      pt   = q.tkr();
      kv   = pt.split("/");
      if ( kv.length > 1 )
         mmid = kv[1];
      _UpdateL2( q, mmid, true );
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
      String      bm, am;
      Level2Record l2;

      // Store on unsorted collection
/*
      bm = q._bidMmid;
      am = q._askMmid;
      synchronized( this ) {
         if ( !_prcs.containsKey( bm ) )
            _prcs.put( bm, new Level2Record( bm ) );
         l2 = (Level2Record)_prcs.get( bm );
      }
 */
      OnLevel2();
   }

   /**
    * Callback invoked when an Equity Trade message is received
    *
    * @param StreamName Name of this Data Stream
    * @param trd EQTrade
    */
   public void OnUpdate( String StreamName, EQTrade trd )
   {
      // Don't care
   }


   //////////////////////
   // Helpers
   //////////////////////
   private void _UpdateL2( EQQuoteMM q, String mm, boolean bMmid )
   {
      Level2Record l2;

      /*
       * Clean-up:
       * 1) MMID?  Divide by 100
       * 2) XNAS : Shitcan
       * 3) Displayable Bid / Ask
       */

      if ( bMmid ) {
         q._bidSize = Math.max( q._bidSize / 100, 1 );
         q._askSize = Math.max( q._askSize / 100, 1 );
      }
      if ( mm.equals( "XNAS" ) )
         return;
      q._bid = ( _bCanDpy || q.CanDisplayBid() ) ? q._bid : 0.0;
      q._ask = ( _bCanDpy || q.CanDisplayAsk() ) ? q._ask : 0.0;

      /*
       * 1) Find Level2Record on unsorted collection keyed by MMID
       * 2) Pull off sorted collection(s)
       * 3) Update, then put back on sorted collections
       */

      if ( mm.length() == 0 )
         mm = "++++";
      synchronized( this ) {
         /*
          * 1) Find Level2Record on unsorted collection keyed by MMID
          */
         if ( !_prcs.containsKey( mm ) )
            _prcs.put( mm, new Level2Record( mm ) );
         l2 = (Level2Record)_prcs.get( mm );
         /*
          * 2) Pull off sorted collection(s)
          */
         if ( _bids.contains( l2 ) )
            _bids.remove( l2 );
         if ( _asks.contains( l2 ) )
            _asks.remove( l2 );
         /*
          * 3) Update, then put back on sorted collections, if non-zero
          */
         l2.Set( q );
         _bids.add( l2 );
         _asks.add( l2 );
      }
      OnLevel2();
   }

   private boolean _CanPub()
   {
      double tAge;

      if ( _tSnap == 0.0 )
         return true;
      tAge = (double)( System.currentTimeMillis() - _tPub ) / 1000.0;
      return( tAge >= _tSnap );
   }

   private void _ShowLvl2()
   {
      Iterator     bt, at;
      Object       bm, am;
      Level2Record b, a;
      int          i;

      // Pre-condition

      if ( !_CanPub() )
         return;
      System.out.printf( ANSI_HOME );
      System.out.printf( "  Bid   MMID BidSize   BidTime    | " ); 
      System.out.printf( "  Ask   MMID AskSize   AskTime\n" );
      System.out.printf( "------- ---- ------- ------------ + " );
      System.out.printf( "------- ---- ------- ------------\n" );
      bt = _bids.iterator();
      at = _asks.iterator();
      for ( i=0; i<_maxRow && bt.hasNext() && at.hasNext(); i++ ) {
         bm = bt.next();
         am = at.next();
         b  = (Level2Record)bm;
         a  = (Level2Record)am;
         System.out.printf( "%7.3f %-4s %6d  %12s |", 
            b._dBid, b._mmid, b._bidSize, b._bidTime );
         System.out.printf( "%7.3f %-4s %6d  %12s\n",
            a._dAsk, a._mmid, a._askSize, a._askTime );
      }
      _tPub = System.currentTimeMillis();
   }

   private void _OnLvl2Msg( String StreamName, String mt, QuoddMsg m )
   {
      String  pt, ps;
      boolean bRTL;

      if ( (pt=m.tkr()) == null )
         pt = StreamName;
      bRTL = ( m.RTL() != -1 );
      ps   = m.pTimeMs();
      System.out.printf( "%s %-6s {%03d} %-6s ", ps, mt, m.tag(), pt );
      if ( bRTL )
         System.out.printf( "<%06d> ", m.RTL() );
   }
}


/////////////////////////////////////////////////////////////////
//
//              c l a s s    L e v e l 2 R e c o r d
//
/////////////////////////////////////////////////////////////////
/**
 * Level2Record is a container class storing the Market Maker (or Market 
 * Center)'s Level 2 quote.  This class is maintained on both an 
 * 1) unsorted HashMap collection keyed by Market Maker, and 2) sorted 
 * TreeMap keyed by price and MMID
 * <p>
 */
class Level2Record
{
   ///////////////////////////////
   // Instance Members
   ///////////////////////////////
   public String _mmid;
   public double _dBid;
   public long   _bidSize;
   public String _bidTime;
   public double _dAsk;
   public long   _askSize;
   public String _askTime;

   ///////////////////////////////
   // Constructor
   ///////////////////////////////
   public Level2Record( String mmid )
   {
      _mmid    = mmid;
      _dBid    = 0;
      _bidSize = 0;
      _bidTime = "00:00:00.000";
      _dAsk    = 0.0;
      _askSize = 0;
      _askTime = "00:00:00.000";
   }


   ///////////////////////////////
   // Mutator
   ///////////////////////////////
   public void Set( EQQuoteMM qte )
   {
      _dBid    = qte._bid;
      _bidSize = qte._bidSize;
      _bidTime = qte.pTimeMs().substring(1,12);
      _dAsk    = qte._ask;
      _askSize = qte._askSize;
      _askTime = _bidTime;
   }
}


/////////////////////////////////////////////////////////////////
//
//                c l a s s    L 2 C o m p a r a t o r
//
/////////////////////////////////////////////////////////////////
/**
 * L2Comparator is a comparator for Level2Record containers on the 
 * TreeSet collection.  Logic included to:<p>
 * 1) Keep 0.0 prices at the end of the collection<p>
 * 2) Sort BID in reverse order
 * <p>
 */
class L2Comparator implements Comparator<Level2Record>
{
   ///////////////////////////////
   // Instance Members
   ///////////////////////////////
   private boolean _bBid;

   //////////////////////
   // Constructor
   //////////////////////
   public L2Comparator( boolean bBid )
   {
      _bBid = bBid;
   }


   ///////////////////////////
   // Comparator Interface
   ///////////////////////////
   public int compare( Level2Record l1, Level2Record l2 )
   {
      return _bBid ? _cmpBid( l1, l2 ) : _cmpAsk( l1, l2 );
   }


   ///////////////////////////
   // Helpers Interface
   ///////////////////////////
   private int _cmpBid( Level2Record l1, Level2Record l2 )
   {
      if ( l1._dBid < l2._dBid )
         return 1;
      else if ( l1._dBid > l2._dBid )
         return -1;
      return l1._mmid.compareTo( l2._mmid );
   }

   private int _cmpAsk( Level2Record l1, Level2Record l2 )
   {
      if ( l1._dAsk == 0.0 )
         return 1;
      else if ( l2._dAsk == 0.0 )
         return -1;
      else if ( l1._dAsk > l2._dAsk )
         return 1;
      else if ( l1._dAsk < l2._dAsk )
         return -1;
      return l1._mmid.compareTo( l2._mmid );
   }
}
