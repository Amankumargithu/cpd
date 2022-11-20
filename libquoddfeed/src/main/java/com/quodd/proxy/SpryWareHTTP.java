/******************************************************************************
*
*  SpryWareHTTP.java
*     Proxy for SpryWareHTTP server
*
*  REVISION HISTORY:
*      5 FEB 2014 jcs  Created.
*     15 MAY 2014 jcs  Build 76: SPRY_TICKER; QuoddFeed.spry; bKid
*     16 JUN 2014 jcs  Build 78: UCMapping
*     30 JUN 2014 jcs  Build 79: RTLCache
*      6 AUG 2014 jcs  Build 80: UCLocCodeMap
*      2 JUN 2015 jcs  Build 93: bKid = !bTrdOnly; NEXT : Don't bump 1 sec
*     31 JUL 2015 jcs  Build 95: _opSubTRADE_ID; Duplicate Trades
*     10 AUG 2015 jcs  Build 96: _IsDuplicate()
*     10 SEP 2015 jcs  Build 97: _FilterResults() 
*     10 DEC 2015 jcs  Build 98: Don't IncTime() if KEY_PREV; OPTrade.HasQuote()
*      4 JAN 2016 jcs  Build 99: %f, not %.2f; _AddQuoteAppendage()
*     16 FEB 2016 jcs  Build 99a:bPre = Incomplete result set; MCRY
*
*  (c) 2011-2016 Quodd Financial
*******************************************************************************/
package com.quodd.proxy;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.text.*;
import java.util.*;
import QuoddFeed.Enum.*;
import QuoddFeed.msg.*;
import QuoddFeed.spry.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
//
//                c l a s s   _ S p r y C l i e n t
//
/////////////////////////////////////////////////////////////////
class _SpryClient implements Runnable
{
   /////////////////
   // Class-wide
   /////////////////
   /*
    * SpryWare Arguments
    */
   private static final String KEY_SYMBOL         = "s";
   private static final String KEY_NUM_REC        = "n";
   private static final String KEY_DATE           = "date";
   private static final String KEY_START_TIME     = "starttime";
   private static final String KEY_END_TIME       = "endtime";
   private static final String KEY_TYPE           = "type";
   private static final String KEY_MIN_TRADE_SIZE = "minsize";
   private static final String KEY_MAX_TRADE_SIZE = "maxsize";
   private static final String KEY_MIN_PRICE      = "minprice";
   private static final String KEY_MAX_PRICE      = "maxprice";
   private static final String KEY_NEXT           = "next";
   private static final String KEY_PREV           = "prev";
   private static final String KEY_EXCH           = "exch";
   /*
    * SpryWare KEY_TYPE Values
    */
   private static String TYPE_TRADE     =  "1";
   private static String TYPE_BID       =  "8";
   private static String TYPE_ASK       =  "9";
   private static String TYPE_BEST_BID  = "13";
   private static String TYPE_BEST_ASK  = "14";
   private static String TYPE_BEST_QTE  = "quote";
   private static int    _FLTR_BID      = 0x0001;
   private static int    _FLTR_ASK      = 0x0002;
   private static int    _FLTR_BEST_BID = 0x0004;
   private static int    _FLTR_BEST_ASK = 0x0008;
   /*
    * Hard-coded SpryWare conditions - WTF??
    */
   private static int    COND_MKTCTR    = 25;
   private static int    _MAX_MS        = 1000000;
   /*
    * Johnson
    */
   private static final String _KEY_SYMBOLOSY = "symbology";
   private static final String _KEY_REUTER    = "reuter";
   private static final String _KEY_SPRYWARE  = "spryware";
   private static final String _CR            = "\r\n";
   private static final String _BR            = "<br>";
   private static final String _BRCR          = _BR + _CR;
   private static final char   _NOEXCH        = 'U';
   private static final int    _MAX_RES       = 64*1024;
   /*
    * Ankit Fuckup
    */
   private static final String _ANKIT1        = "00:00:00";

   /////////////////
   // Instance
   /////////////////
   private SpryWareHTTP     _svr;
   private Socket           _sock;
   private boolean          _bChunked;
   private boolean          _bUseRTL;
   private boolean          _bSprySym;
   private int              _maxQryWin;
   private int              _maxPrevWin;
   private String           _tStart;
   private int              _maxResp;
   private String           _EOD;
   private String           _dst;
   private DataInputStream  _in;
   private DataOutputStream _out;
   private Thread           _thr;
   private int              _bSz;
   private byte[]           _buf;
   private PrintStream      cout;

   //////////////////////
   // Constructor
   //////////////////////
   _SpryClient( SpryWareHTTP svr, Socket cli ) throws IOException
   {
      InetSocketAddress ip;
      String[]          kv;
      String            pl;
      int               i, nk;

      _svr        = svr;
      _sock       = cli;
      _bChunked   = UCconfig.HasKey( "SPRY_CHUNKED" );
      _bUseRTL    = _svr.CacheRTL();
      _bSprySym   = UCconfig.HasKey( "SPRY_TICKER" );
      _maxQryWin  = UCconfig.GetInt( "SPRY_QRYWIN", 3600 );
      _maxPrevWin = UCconfig.GetInt( "SPRY_PREVWIN", 3600 );
      _tStart     = UCconfig.GetEnv( "SPRY_STARTTIME", null );
      _maxResp    = UCconfig.GetInt( "SPRY_MAXRSP", 255 );
      _EOD        = UCconfig.GetEnv( "SPRY_EOD", "16:00:00" );
      ip          = (InetSocketAddress)cli.getRemoteSocketAddress();
      _dst        = ip.getAddress().getHostAddress();
      _in         = new DataInputStream( cli.getInputStream() );
      _out        = new DataOutputStream( cli.getOutputStream() );
      _thr        = new Thread( this );
      _bSz        = 16 * 1024;
      _buf        = new byte[_bSz];
      cout        = System.out;
      _thr.start();
   }

   public void Stop()
   {
      synchronized( this ) {
         try {
            if ( _sock != null )
               _sock.close();
         } catch( IOException e ) { ; }
         if ( _thr != null )
            _thr.interrupt();
         _thr = null;
      }
   }


   //////////////////////
   // Runnable Interface
   //////////////////////
   public void run()
   {
      String  msg, fmt, s;
      boolean bRun;
      int     len;

      for ( bRun=true,fmt=null; bRun; ) {
         try {
            len = _in.read( _buf, 0, _bSz );
            if ( len > 0 )
               Parse( new String( _buf, 0, len ) );
            else
               bRun = false;
         } catch( NullPointerException e ) {
            fmt  = "[%s] READ ERROR : NullPointerException\n";
            bRun = false;
         } catch( IOException e ) {
            fmt  = "[%s] READ ERROR : IOException\n";
            bRun = false;
         }
      }
      Disconnect();
      if ( fmt != null )
         cout.printf( fmt, Now() );
      synchronized( this ) {
         _sock = null;
         _thr  = null;
      }
   }


   //////////////////////
   // Workhorse
   //////////////////////
   private void Parse( String url )
   {
      QuoddFeed.MD.TSQ qry;
      String           get;
      String[]         argv, kv, av, http;
      QuoddMsg[]       mdb, fdb, qdb, tdb;
      QuoddMsg         qm, q0, q1;
      EQQuote          eq;
      EQBbo            eb;
      EQTrade          et;
      OPBbo            ob;
      OPQuote          oq;
      OPTrade          ot;
      Status           sts;
      String           rtr, tkr, exch, dt, t0, t1, tp, eod, fltr;
      String           hdr, rsp, pt, flg, fmt;
      String           tNxt, tPrv, rtlNxt, rtlPrv, bboEx, srtl0;
      boolean          bKid, bRtr, bSpry, bNxt, bPrv, bMin, bTrd, bQte;
      boolean          bboBid, bboAsk, bBbo, b000, bTrdOnly, bDone;
      int              i, j, k, argc, na, nRec, nReqUsr, ix, n2get;
      _FilterCfg       cc;
      long             rtl, rtl0;
      double           dv;
      char             mt, mt2;

      // Parse HTTP Header

      if ( _CanLog( _svr._SPRY_LOG_URL ) )
         cout.printf( "[%s] %s\n", Now(), url );
      http   = url.split( "\\r?\\n" );
      get    = http[0].split("HTTP")[0].split( "\\?" )[1].trim();
      if ( _CanLog( _svr._SPRY_LOG_QUERY ) )
         cout.printf( "[%s] %s\n", Now(), get );
      cc      = new _FilterCfg();
      tkr     = exch = dt = t0 = t1 = eod = null;
      bKid    = bRtr = bNxt = bPrv = false;
      bSpry   = _bSprySym;
      rtl0    = 0;
      tNxt    = tPrv = rtlNxt = rtlPrv = null;
      nRec    = _maxResp;
      nReqUsr = nRec;
      bTrd    = bQte = bTrdOnly = false;
      bboBid  = bboAsk = false;
      argv    = get.split( "\\&" );
      argc    = argv.length;
      /*
       * Argument Parsing Pass 1 : All except NEXT / PREV
       */
      for ( i=0; i<argc; i++ ) {
         kv = argv[i].split( "=" );
         if ( kv.length != 2 )
            continue; // for-i
         kv[0] = kv[0].toLowerCase();
         av    = kv[1].split( "," );
         if ( kv[0].equals( KEY_SYMBOL ) )
            tkr = kv[1].replace( "%5C", "\\" ).replace( "%20", " " );
         else if ( kv[0].equals( KEY_EXCH ) )
            exch = kv[1];
         else if ( kv[0].equals( KEY_NUM_REC ) ) {
            try {
               nReqUsr = Integer.parseInt( kv[1] );
               nRec    = nRec;
            } catch( Exception e ) { ; }
         }
         else if ( kv[0].equals( KEY_DATE ) )
            dt = kv[1].replace( "%20", " " ) + " ";
         else if ( kv[0].equals( KEY_START_TIME ) )
            t0 = kv[1].replace( "%20", " " );
         else if ( kv[0].equals( KEY_END_TIME ) ) {
            t1  = kv[1].replace( "%20", " " );
            eod = t1;
         }
         else if ( kv[0].equals( KEY_TYPE ) ) {
            for ( j=0; j<av.length; j++ ) {
               if ( av[j].equals( TYPE_TRADE ) ) {
                  bTrd     = true;
                  bTrdOnly = bTrd;
               }
               else if ( av[j].equals( TYPE_BID ) )
                  cc._iFltr |= _FLTR_BID;
               else if ( av[j].equals( TYPE_ASK ) )
                  cc._iFltr |= _FLTR_ASK;
               else if ( av[j].equals( TYPE_BEST_BID ) ) {
                  cc._iFltr |= _FLTR_BEST_BID;
                  bboBid = true;
               }
               else if ( av[j].equals( TYPE_BEST_ASK ) ) {
                  cc._iFltr |= _FLTR_BEST_ASK;
                  bboAsk = true;
               }
               else if ( av[j].equals( TYPE_BEST_QTE ) ) {
                  cc._bQTE  = true;
                  nRec     += nRec;  // KLUDGE
/*
                  bTrd       = false;
                  cc._iFltr |= _FLTR_BID;
                  cc._iFltr |= _FLTR_ASK;
 */
               }
            }
         }
         else if ( kv[0].equals( KEY_MIN_TRADE_SIZE ) ||
                   kv[0].equals( KEY_MAX_TRADE_SIZE ) )
         {
            bMin = kv[0].equals( KEY_MIN_TRADE_SIZE );
            try {
               cc._szMin = bMin  ? Long.parseLong( kv[1] ) : cc._szMin;
               cc._szMax = !bMin ? Long.parseLong( kv[1] ) : cc._szMax;
            } catch( Exception e ) { ; }
         }
         else if ( kv[0].equals( KEY_MIN_PRICE ) || 
                   kv[0].equals( KEY_MAX_PRICE ) )
         {
            bMin = kv[0].equals( KEY_MIN_PRICE );
            try {
               cc._prcMin = bMin  ? Double.parseDouble( kv[1] ) : cc._prcMin;
               cc._prcMax = !bMin ? Double.parseDouble( kv[1] ) : cc._prcMax;
            } catch( Exception e ) { ; }
         }
         else if ( kv[0].equals( _KEY_SYMBOLOSY ) ) {
            kv[1]  = kv[1].toLowerCase();
            bRtr   = kv[1].equals( _KEY_REUTER );
            bSpry |= kv[1].equals( _KEY_SPRYWARE );
         }
      }
      /*
       * Pass 1 Eval : Filter, BBO, Ticker Name
       */
      bQte = ( cc._iFltr != 0 );
      if ( bTrd && !bQte )
         fltr = "TRADE";
      else if ( !bTrd && bQte )
         fltr = "QUOTE";
      else {
         fltr   =     "ALL";
         bTrd       = true;
         cc._iFltr |= _FLTR_BID;
         cc._iFltr |= _FLTR_ASK;
         cc._iFltr |= _FLTR_BEST_BID;
         cc._iFltr |= _FLTR_BEST_ASK;
      }
/*
 * 14-07-01 jcs
      bBbo  = bboBid && bboAsk && bTrd;
 */
      bBbo  = bboBid && bboAsk;
      bboEx = null;
      if ( bRtr ) {
         rtr = tkr;
         tkr = Rtr2Quodd( rtr );
      }
      else if ( bSpry ) {
         rtr = tkr;
         tkr = Spry2Quodd( rtr );
         if ( exch != null )
            tkr  += bBbo ? "" : "/" + exch;
         bboEx = bBbo ? exch : null;
      }
      /*
       * Argument Parsing Pass 2 : NEXT / PREV
       */
      b000 = false;
      for ( i=0; i<argc; i++ ) {
         kv = argv[i].split( "=" );
         if ( kv.length != 2 )
            continue; // for-i
         kv[0] = kv[0].toLowerCase();
         av    = kv[1].split( "," );
         if ( kv[0].equals( KEY_PREV ) /* && !b000 */ ) {
            b000   = av[0].startsWith( _ANKIT1 );
            bPrv   = true;
            na     = av.length;
            t0     = av[0];
            rtl0   = ( na > 1 ) ? _ToInt( av[1] ) : 0;
            srtl0  = Long.toString( rtl0 );
            rtlNxt = _bUseRTL ? _svr.GetTimeFromRTL( tkr, srtl0 ) : null;
            if ( _bUseRTL && _CanLog( _svr._SPRY_LOG_RTL ) ) {
               fmt = "PREV-GetTime( %s,%d ) = %s\n";
               cout.printf( fmt, tkr, rtl0, rtlNxt != null ? rtlNxt : "null" );
            }
            if ( rtlNxt != null )
               t0 = IncTime( rtlNxt, 0, 0 );
            else
/*
 * 15-09-22 Build 98: jcs
 *
               t0 = IncTime( av[0], 1, 0 );  // Next Second
 */
               t0 = IncTime( av[0], 0, 1 );  // Next millisecond
            tp = IncTime( t0, _maxPrevWin, 0 );
            if ( t1 == null )
               t1 = tp;
            else {
               ix = t1.compareTo( tp );
               t1 = ( ix > 0 ) ? tp : t1;
            }
            if ( _CanLog( _svr._SPRY_LOG_NEXT ) )
               cout.printf( "PREV {%s,%s}; RTL=%d\n", t0, t1, rtl0 );
         }
         else if ( kv[0].equals( KEY_NEXT ) /* && !b000 */ ) {
            b000   = av[0].startsWith( _ANKIT1 );
            bNxt   = true;
            na     = av.length;
            rtl0   = ( na > 1 ) ? _ToInt( av[1] ) : 0;
            srtl0  = Long.toString( rtl0 );
            rtlNxt = _bUseRTL ? _svr.GetTimeFromRTL( tkr, srtl0 ) : null;
            if ( _bUseRTL && _CanLog( _svr._SPRY_LOG_RTL ) ) {
               fmt = "NEXT-GetTime( %s,%d ) = %s\n";
               cout.printf( fmt, tkr, rtl0, rtlNxt != null ? rtlNxt : "null" );
            }
/*
 * 15-06-03 jcs  Build 93 
 * 
            if ( rtlNxt != null ) 
               t1 = IncTime( rtlNxt, 0, 0 );
            else
               t1 = IncTime( av[0], 1, 0 );  // Next Second
 */
t1 = av[0];
            eod = t1;
            t0 = IncTime( t1, -_maxQryWin, 0 );
            if ( _CanLog( _svr._SPRY_LOG_NEXT ) )
               cout.printf( "NEXT {%s,%s}; RTL=%d\n", t0, t1, rtl0 );
         }
      }

      // Hard-Coded Header

      hdr  = "HTTP/1.1 200 OK" + _CR;
      hdr += "Date: " + HttpTime() + _CR;
      hdr += "Server: El Hombre/1.0 (Johnson)" + _CR;
      hdr += "Connection: close" + _CR;
      hdr += _bChunked ? "Transfer-Encoding: chunked" + _CR : "";
      hdr += "Content-Type: text/html; charset=UTF-8" + _CR;
      hdr += _bChunked ? _CR : "";

      // Canned TSQ response (for now)

      ArrayList<String> arr, rar;
      String[]          rdb;
      String            oHdr, wip, bTy, aTy, pBid, pAsk, chk, T0, T1;
      String            tCnd, qCnd, ty, ex, qTkr, pt0, pt1;
      int               nf, nt, nq, nm, tot, flgs, n0, n1, nQ, nQry;
      long              tNow, tCls, rNow, d0, d1, d2;
      char              cnd;
      double            bid, ask;
      long              bSz, aSz;
      boolean           bEx, bPre;
      _SameSec          same;
      String            cT0, cT1, cr0, cr1;

      cT0  = cT1 = cr0 = cr1 = null;
      mdb  = null;
      q0   = q1 = null;
      arr  = new ArrayList<String>();
      oHdr = null;
      bEx  = ( exch != null );
/*
 * 15-06-02 jcs  Build 93
 *
      bKid = !bEx && !bBbo;
 */
      bKid = !bTrdOnly;
      if ( t1 == null ) {
         tNow  = System.currentTimeMillis();
         tCls  = MktCloseMillis();
         rNow  = Math.min( tNow, tCls );
         t1    = QuoddMsg.pDateTimeMs( rNow ).split( " " )[1];
         rNow -= _maxQryWin * 1000;
         if ( _tStart != null )
            t0 = _tStart;
         else
            t0 = QuoddMsg.pDateTimeMs( rNow ).split( " " )[1];
      }
      if ( t0 == null ) {
         tNow  = System.currentTimeMillis();
         tNow  = System.currentTimeMillis();
         tNow -= _maxQryWin * 1000;
         t0    = QuoddMsg.pDateTimeMs( tNow );
      }
      /*
       * Protect against fucked up queries
       */
      t1 = t0.equals( t1 ) ? IncTime( t0, 1, 0 ) : t1;
      nf = 0;
      if ( tkr == null )
         arr.add( "ERROR : Ticker not specified" + _BR );
      else {
         d0    = System.currentTimeMillis();
         T0    = ( dt == null ) ? "" : dt;
         T1    = ( dt == null ) ? "" : dt;
         T0   += t0;
         T1   += _IsEOD( T1.trim(), eod ) ? _EOD : t1;
         same  = new _SameSec();
         n2get = nRec;
         pt0   = T0.split( " " )[1];
         pt1   = T1.split( " " )[1];
         bPre  = ( pt0.compareTo( "09:30:00" ) < 0 );
//         bPre &= ( ( _tStart == null ) || !_tStart.equals( pt0 ) );
         bPre |= ( pt1.compareTo( "09:30:00" ) < 0 );
         nQry  = bPre ? 2 : 1;
nQry  = 2;
// cout.printf( "   ===>>> MD.TSQ( %s,%s ); nQry = %d\n", pt0, pt1, nQry );
         for ( nQ=0; nQ<nQry; nQ++ ) {
            if ( ( mdb != null ) && ( mdb.length >= nRec ) ) 
{
fmt = "   ===>>> MD.TSQ( %s,%s ); nRes = %d; No P:\n";
cout.printf( fmt, pt0, pt1, nRec );
               break; // for-i
}
            qTkr = ( nQ == 0 ) ? tkr : tkr.replace( "O:", "P:" );
            for ( i=0,bDone=false; !bDone; i++ ) {
               qry   = new QuoddFeed.MD.TSQ( qTkr, 
                                             T0, 
                                             T1, 
                                             fltr, 
                                             bKid, 
                                             nRec, 
                                             bboEx );
               qdb   = qry.Query();
               nq    = qdb.length;
               if ( i == 0 ) {
                  n2get = nq - 1; // Last one is Status
                  bDone = ( n2get < nRec );
               }
               qry.Stop();
               same         = _SameSecond( qdb, nRec );
               same._bSame &= ( same._nRes >= nRec );
               if ( same._bSame ) {
                  fmt = "[%s] ALL SAME SECOND ( %s, %s ) = %s; ";
                  cout.printf( fmt, Now(), qTkr, T1, same._sec );
                  cout.printf( "nRec=%d; nRes=%d\n", nRec, nq );
                  nRec *= 2;
                  nRec  = Math.min( nRec, _MAX_RES );
                  T0    = ( dt == null ) ? "" : dt;
                  T0   += IncTime( t1, -(i+2), 0 );
               }
               fdb    = _FilterResults( qdb, cc );
               tot    = ( fdb == null ) ? 0 : fdb.length;
               bDone |= ( tot == 0 );
               nf    += nq - tot;
               tdb    = mdb;
               nm     = ( tdb != null ) ? tdb.length : 0;
               tot   += nm;
               mdb    = new QuoddMsg[tot];
               for ( k=0; k<nm; mdb[k]=tdb[k], k++ );
               for ( j=0; k<tot; mdb[k++]=fdb[j++] );
               bDone |= ( mdb.length >= n2get );
               if ( !bDone ) {
                  T1 = ( dt == null ) ? "" : dt;
                  T1 += " " + qdb[nq-2].pTimeMs();
                  T1 = T1.replace("-","");
                  T1 = T1.replace("[","");
                  T1 = T1.replace("]","");
               }
            }
         }
         if ( mdb.length > nReqUsr ) {
            tdb = mdb;
            mdb = new QuoddMsg[nReqUsr];
            tot = mdb.length;
            for ( i=0; i<tot; mdb[i]=tdb[i],i++ );
         } 
         d1    = System.currentTimeMillis() - d0;
         d0    = System.currentTimeMillis();
         tot   = ( mdb == null ) ? 0 : mdb.length;
         oHdr  = "Time,Condition,ReferenceNumber,Type,";
         oHdr += "Exchange,Price,Size";
         if ( fltr.equals( "ALL" ) || fltr.equals( "QUOTE" ) )
            oHdr += "Type,Exchange,Price,Size";
/*
 * 15-11-16 jcs  Build 98: Not Available ...
 *
            oHdr += ",Net,Pct,Vol,VWAP,Flags";
 */
         oHdr += ",Bid,Ask,BidSize,AskSize,BidExchg,AskExchg";
         oHdr += ",BidTime,AskTime,Trade Id";
         /*
          * 15-12-18 jcs  Build 99: Underlying shit
          */
         oHdr += ",Underlying Bid,Underlying Ask";
         oHdr += ",Underlying Bid Size,Underlying Ask Size";
         oHdr += ",Underlying Bid Exchange,Underlying Ask Exchange";
         oHdr += _BR;
         sts = null;
         for ( i=tot,nt=nq=0; i>0; i-- ) {
            qm  = mdb[i-1];
            q1  = qm;
            pt  = qm.pTimeMs().replace("[","").replace("]","");
            mt  = qm.mt();
            mt2 = qm.mtSub();
            rtl = qm.RTL();
            q0  = ( ( q0 == null ) && ( rtl != 0 ) ) ? qm : q0;
            wip = "";
            /****************************
             * RTL Stuff : Cache / Allowable?
            if ( _bUseRTL && !_IsAllowableRTL( rtl, rtl0, bNxt, bPrv ) ) {
               fmt = "RTL not allowable : %s,%d vs %d\n";
               if ( _CanLog( _svr._SPRY_LOG_RTL ) )
                  cout.printf( fmt, tkr, rtl, rtl0 );
               nf += 1;
               continue; // for-i
            }
            if ( rtl != 0 ) {
               cT1 = pt;
               cr1 = Long.toString( rtl );
               if ( cT0 == null ) {
                  cT0 = cT1;
                  cr0 = cr1;
               }
            }
            ****************************/
            switch( mt ) {
               case MsgTypes._mtEQUITY:
                  switch( mt2 ) {
                     case MsgTypes._eqSubQTESHORT:
                     case MsgTypes._eqSubQTELONG:
                        eq   = (EQQuote)qm;
                        cnd  = eq._qteCond;
                        cnd  = _IsAscii( cnd ) ? cnd : _NOEXCH;
                        pBid = eq._mktCtr;
                        pAsk = eq._mktCtr;
                        dv   = _QtePrc( eq._bid, eq._ask, cc._iFltr, false );
                        wip += wip.format( "%s,%c,%d,", pt, cnd, rtl );
                        wip += wip.format( "%s,%s,", TYPE_BID, pBid );
                        wip += wip.format( "%.2f,%d,", eq._bid, eq._bidSize );
                        wip += wip.format( "%s,%s,", TYPE_ASK, pAsk );
                        wip += wip.format( "%.2f,%d,", eq._ask, eq._askSize );
                        wip += _BR;
                        nq  += 1;
                        arr.add( wip );
                        break;
                     case MsgTypes._eqSubBBOSHORT:
                     case MsgTypes._eqSubBBOLONG:
                        eb   = (EQBbo)qm;
                        cnd  = eb._bboCond;
                        cnd  = _IsAscii( cnd ) ? cnd : _NOEXCH;
                        pBid = eb._bidMktCtr;
                        pAsk = eb._askMktCtr;
                        dv   = _QtePrc( eb._bid, eb._ask, cc._iFltr, true );
                        wip += wip.format( "%s,%c,%d,", pt, cnd, rtl );
                        wip += wip.format( "%s,%s,", TYPE_BEST_BID, pBid );
                        wip += wip.format( "%.2f,%d,", eb._bid, eb._bidSize );
                        wip += wip.format( "%s,%s,", TYPE_BEST_ASK, pAsk );
                        wip += wip.format( "%.2f,%d,", eb._ask, eb._askSize );
                        wip += _BR;
                        nq  += 1;
                        arr.add( wip );
                        break;
                     case MsgTypes._eqSubTRDSHORT:
                     case MsgTypes._eqSubTRDLONG:
                     case MsgTypes._eqSubSUMMARY:
                     case MsgTypes._eqSubMKTCTRSUMM:
                        et   = (EQTrade)qm;
                        wip += wip.format( "%s,TRADE(TODO),%d,", pt, rtl );
                        wip += et._mktCtr + ",";
                        wip += wip.format( "%.2f,", et._trdPrc );
                        wip += wip.format( "%d,",  et._trdVol );
                        wip += wip.format( "%.2f,", et._netChg );
                        wip += wip.format( "%.2f,", et._pctChg );
                        wip += wip.format( "%d,", et._acVol );
                        wip += wip.format( "%.4f,", et._vwap );
                        flg  = "";
                        flg += flg.format( "0x%x ", et._eligFlags );
                        flg += flg.format( "0x%x ", et._rptType );
                        flg += flg.format( "0x%x ", et._rptDetail );
                        flg += flg.format( "0x%x", et._rptFlags );
                        wip += flg;
                        wip += _BR;
                        nt  += 1;
                        arr.add( wip );
                        break;
                  }
                  break;
               case MsgTypes._mtOPTION:
                  switch( mt2 ) {
                     case MsgTypes._opSubQTESHORT:
                     case MsgTypes._opSubQTELONG:
                        oq   = (OPQuote)qm;
                        qCnd = SpryWare.QuoteCondition( oq );
                        pBid = _svr.OptEX( oq._mktCtr );
                        pAsk = pBid;
                        dv   = _QtePrc( oq._bid, oq._ask, cc._iFltr, true );
                        wip += wip.format( "%s,%s,%d,", pt, qCnd, rtl );
                        wip += wip.format( "%s,%s,", TYPE_BID, pBid );
                        wip += wip.format( "%f,%d,", oq._bid, oq._bidSize );
                        wip += wip.format( "%s,%s,", TYPE_ASK, pAsk );
                        wip += wip.format( "%f,%d", oq._ask, oq._askSize );
                        wip += _BR;
                        nq  += 1;
                        arr.add( wip );
                        break;
                     case MsgTypes._opSubBBOSHORT:
                     case MsgTypes._opSubBBOLONG:
                        ob   = (OPBbo)qm;
                        qCnd = SpryWare.QuoteCondition( ob );
                        pBid = _svr.GetExch( T0, tkr, ob._iBidMktCtr );
                        pAsk = _svr.GetExch( T0, tkr, ob._iAskMktCtr );
                        bid  = ob._bid;
                        ask  = ob._ask;
                        bSz  = ob._bidSize;
                        aSz  = ob._askSize;
                        dv   = _QtePrc( bid, ask, cc._iFltr, true );
                        bTy  = bEx && ( bboEx == null ) ? TYPE_BID : TYPE_BEST_BID;
                        aTy  = bEx && ( bboEx == null ) ? TYPE_ASK : TYPE_BEST_ASK;
                        wip += wip.format( "%s,%s,%d,", pt, qCnd, rtl );
                        wip += wip.format( "%s,%s,", bTy, pBid );
                        wip += wip.format( "%f,%d,", bid, bSz );
                        wip += wip.format( "%s,%s,", aTy, pAsk );
                        wip += wip.format( "%f,%d", ask, aSz );
                        wip += _BR;
                        nq  += 1;
                        arr.add( wip );
                        break; 
                     case MsgTypes._opSubTRADE:
                     case MsgTypes._opSubTRDCXL:
                     case MsgTypes._opSubTRADE_ID:
                     case MsgTypes._opSubTRDCXL_ID:
                        ot   = (OPTrade)qm;
                        ty   = TYPE_TRADE;
                        tCnd = SpryWare.TradeCondition( ot );
                        ex   = _svr.GetExch( T0, tkr, ot._iMktCtr );
                        wip += wip.format( "%s,%s,%d,%s,", pt, tCnd, rtl, ty );
                        wip += wip.format( "%s,", ex );
                        wip += wip.format( "%f,", ot._trdPrc );
                        wip += wip.format( "%d",  ot._trdVol );
                        wip += _AddQuoteAppendage( tkr, T0, ot, false );
                        wip += wip.format( ",%d",  ot._trdID );
                        wip += _AddQuoteAppendage( tkr, T0, ot, true );
                        wip += ",";
                        wip += _BR;
                        nt  += 1;
                        arr.add( wip );
                        break; 
                  }
                  break;
               case MsgTypes._mtDEAD:
                  sts = (Status)qm;
                  break;
            }
         }
         if ( ( sts != null ) && _CanLog( _svr._SPRY_LOG_LATENCY ) ) {
            wip  = sts.Text();
            if ( !fltr.contains( "COUNT" ) )
               wip += wip.format( "; %d TRADE; %d QUOTE; %d Fltr", nt, nq, nf );
            cout.printf( "%s\n", wip );
         }
         d2 = System.currentTimeMillis() - d0;
         if ( _CanLog( _svr._SPRY_LOG_LATENCY ) )
            cout.printf( "Query in %dmS; Format in %dmS\n", d1, d2 );
      }
      na = arr.size();
      if ( na > nRec+1 ) {
         rar = new ArrayList<String>();
         if ( bPrv )
            n0 = na - ( nRec+1 );
         else
            n0 = 0;
         n1 = n0 + nRec+1;
         for ( i=n0; i<n1; rar.add( arr.get( i++ ) ) );
         arr = rar;
      }
      if ( !_bChunked )
         arr.add( _CR + _CR );

      /*
       * RTL Cache
       */
      if ( _svr.CacheRTL() && ( cT0 != null ) ) {
         _svr.AddRTL( tkr, cr0, cT0 );
         if ( _CanLog( _svr._SPRY_LOG_RTL ) )
            cout.printf( "RTLCache.Add( %s, %s ) = %s\n", tkr, cr0, cT0 );
      }
      if ( _svr.CacheRTL() && ( cT1 != null ) ) {
         _svr.AddRTL( tkr, cr1, cT1 );
         if ( _CanLog( _svr._SPRY_LOG_RTL ) )
            cout.printf( "RTLCache.Add( %s, %s ) = %s\n", tkr, cr1, cT1 );
      }

      // [ a, b, c, ] -> abc

      String pay;

      na   = arr.size();
      rdb  = new String[na];
      for  ( i=0; i<na; rdb[na-(i+1)] = arr.get( i ), i++ );
      pay  = Arrays.toString( rdb ).replace( ", ", "" );
      pay  = pay.substring( 1,pay.length()-1 );  // [ ... ]
      wip  = ( oHdr != null ) ? oHdr : "";
      wip += pay;
      tot  = wip.length();
      chk  = Integer.toHexString( tot );

      // Format response

      if ( !_bChunked ) {
         hdr += hdr.format( "Content-Length : %d%s", tot, _CR );
         hdr += _CR;
      }
      rsp  = hdr;
      rsp += _bChunked ? rsp.format( "%s%s", chk, _CR ) : "";
      rsp += wip;
      rsp += _bChunked ? rsp.format( "%s0%s%s", _CR, _CR, _CR ) : "";

      // Shove it out ...

      if ( _CanLog( _svr._SPRY_LOG_RANGE ) && ( q0 != null ) ) {
         pt  = q1.pTimeMs().replace("[","").replace("]","");
         rtl = q1.RTL();
         cout.printf( "[%s,%d] -> ", pt, rtl );
         pt  = q0.pTimeMs().replace("[","").replace("]","");
         rtl = q0.RTL();
         cout.printf( "[%s,%d]\n", pt, rtl );
      }
      try {
         cout.printf( "[%s] %d byte response\n", Now(), rsp.length() );
         if ( _CanLog( _svr._SPRY_LOG_RESPONSE ) )
            cout.printf( rsp.replaceAll( _BR, _BRCR ) );
         _out.writeBytes( rsp );
         _sock.shutdownOutput();
      } catch( Exception e ) {
         breakpoint();
      }
   }


   //////////////////////
   // Helpers
   //////////////////////
   QuoddMsg[] _FilterResults( QuoddMsg[] mdb, _FilterCfg cc )
   {
      ArrayList<QuoddMsg> arr;
      QuoddMsg[]          rtn;
      QuoddMsg            qm;
      EQQuote             eq;
      EQBbo               eb;
      EQTrade             et;
      OPBbo               ob;
      OPQuote             oq;
      OPTrade             ot, lwc;
      int                 i, tot, nr;
      char                mt, mt2;
      boolean             bOK, bOpTrd;
      double              bid, ask, dv;
      long                bSz, aSz;

      arr = new ArrayList<QuoddMsg>();
      tot = mdb.length;
      lwc = null;
      for ( i=tot; i>0; i-- ) {
         qm  = mdb[i-1];
         mt  = qm.mt();
         mt2 = qm.mtSub();
         switch( mt ) {
            case MsgTypes._mtEQUITY:
               lwc = null;
               switch( mt2 ) {
                  case MsgTypes._eqSubQTESHORT:
                  case MsgTypes._eqSubQTELONG:
                     eq   = (EQQuote)qm;
                     dv   = _QtePrc( eq._bid, eq._ask, cc._iFltr, false );
                     bOK  = _IsMktCtrQte( cc._iFltr );
                     bOK &= InRange( cc._prcMin, dv, cc._prcMax ); 
                     if ( !bOK )
                        continue; // for-i
                     arr.add( qm );
                     break;
                  case MsgTypes._eqSubBBOSHORT:
                  case MsgTypes._eqSubBBOLONG:
                     eb   = (EQBbo)qm;
                     dv   = _QtePrc( eb._bid, eb._ask, cc._iFltr, true );
                     bOK  = _IsBboQte( cc._iFltr );
                     bOK &= InRange( cc._prcMin, dv, cc._prcMax ); 
                     if ( !bOK )
                        continue; // for-i
                     arr.add( qm );
                     break;
                  case MsgTypes._eqSubTRDSHORT:
                  case MsgTypes._eqSubTRDLONG:
                  case MsgTypes._eqSubSUMMARY:
                  case MsgTypes._eqSubMKTCTRSUMM:
                     et   = (EQTrade)qm;
                     if ( !InRange( cc._prcMin, et._trdPrc, cc._prcMax ) ||
                          !InRange( cc._szMin, et._trdVol, cc._szMax ) || 
                          cc._bQTE )
                        continue; // for-i
                     arr.add( qm );
                     break;
               }
               break;
            case MsgTypes._mtOPTION:
               switch( mt2 ) {
                  case MsgTypes._opSubQTESHORT:
                  case MsgTypes._opSubQTELONG:
                     oq   = (OPQuote)qm;
                     lwc  = null;
                     dv   = _QtePrc( oq._bid, oq._ask, cc._iFltr, true );
                     bOK  = _IsMktCtrQte( cc._iFltr );
                     bOK &= InRange( cc._prcMin, dv, cc._prcMax );
                     bOK &= !_svr._IsZeroQuote( oq._bid, oq._ask );
                     if ( !bOK )
                        continue; // for-i
                     arr.add( qm );
                     break;
                  case MsgTypes._opSubBBOSHORT:
                  case MsgTypes._opSubBBOLONG:
                     ob   = (OPBbo)qm;
                     lwc  = null;
                     bid  = ob._bid;
                     ask  = ob._ask;
                     bSz  = ob._bidSize;
                     aSz  = ob._askSize;
                     dv   = _QtePrc( bid, ask, cc._iFltr, true );
                     bOK  = _IsBboQte( cc._iFltr );
                     bOK &= InRange( cc._prcMin, dv, cc._prcMax );
                     bOK &= !_svr._IsZeroQuote( ob._bid, ob._ask );
                     if ( !bOK )
                        continue; // for-i
                     arr.add( qm );
                     break; 
                  case MsgTypes._opSubTRADE:
                  case MsgTypes._opSubTRDCXL:
                  case MsgTypes._opSubTRADE_ID:
                  case MsgTypes._opSubTRDCXL_ID:
                     ot      = (OPTrade)qm;
                     bOpTrd  = ( mt2 == MsgTypes._opSubTRADE );
                     bOpTrd |= ( mt2 == MsgTypes._opSubTRADE_ID );
                     if ( bOpTrd && _IsDuplicate( ot, lwc ) )
                        continue; // for-i
                     if ( !InRange( cc._prcMin, ot._trdPrc, cc._prcMax ) ||
                          !InRange( cc._szMin, ot._trdVol, cc._szMax ) || 
                          cc._bQTE ) 
                        continue; // for-i
                     arr.add( qm );
                     lwc = null;
                     if ( bOpTrd )
                        lwc = ot.clone();
                     break; 
                  default:
                     lwc = null;
                     break; 
               }
               break;
         }
      }

      // Return as QuoddMsg[] array, but in order as it came in

      rtn = ( (nr=arr.size()) > 0 ) ? new QuoddMsg[nr] : null;
      for ( i=0; i<nr; rtn[i]=arr.get( (nr-1)-i ), i++ );
      return rtn;
   }

   private String _AddQuoteAppendage( String  tkr,
                                      String  T0,
                                      OPTrade ot, 
                                      boolean bUnderlyer )
   {
      String rtn;
      double bid, ask;
      long   bSz, aSz;
      String bTm, aTm, pBid, pAsk, ex;

      // Pre-condition

      rtn = "";
      if ( !ot.HasQuote() )
         return rtn;
      if ( bUnderlyer && !ot.EQ_HasQuote() )
         return rtn;

      // 1) Pull values

      ex = _svr.GetExch( T0, tkr, ot._iMktCtr );
      if ( bUnderlyer ) {
         bid  = ot.EQ_bid;
         bSz  = ot.EQ_bidSize;
         bTm  = ot.pTimeMs( ot.JavaTime( ot.EQ_bidTime ) );
         bTm  = bTm.replace("[","").replace("]","");
         pBid = _svr.OptExID( ot.EQ_bidMktCtr ); 
         ask  = ot.EQ_ask;
         aSz  = ot.EQ_askSize;
         aTm  = ot.pTimeMs( ot.JavaTime( ot.EQ_askTime ) );
         aTm  = aTm.replace("[","").replace("]","");
         pAsk = _svr.OptExID( ot.EQ_askMktCtr );
      }
      else {
         bid  = ot._bid;
         bSz  = ot._bidSize;
         bTm  = ot.pTimeMs( ot.JavaTime( ot._bidTime ) );
         bTm  = bTm.replace("[","").replace("]","");
         pBid = _svr.OptExID( ot._bidMktCtr );
         ask  = ot._ask;
         aSz  = ot._askSize;
         aTm  = ot.pTimeMs( ot.JavaTime( ot._askTime ) );
         aTm  = aTm.replace("[","").replace("]","");
         pAsk = _svr.OptExID( ot._askMktCtr ); 
      }

      // 2) Clean up; Format output

      pBid = pBid.equals( "????" ) ? ex : pBid;
      pAsk = pAsk.equals( "????" ) ? ex : pAsk;
      rtn += ",";
      rtn += rtn.format( "%f,%f,", bid, ask );
      rtn += rtn.format( "%d,%d,", bSz, aSz );
      rtn += rtn.format( "%s,%s", pBid, pAsk );
      rtn += bUnderlyer ? "" : rtn.format( ",%s,%s", bTm, aTm );
      return rtn;
   }

   private boolean _IsDuplicate( OPTrade t1, OPTrade lwc )
   {
      boolean bDup;

      bDup = false;
      if ( lwc != null ) {
         if ( t1._trdIDuniq != 0 )
            bDup = ( t1._trdIDuniq == lwc._trdIDuniq );
         else {
            bDup  = ( t1.MsgTime() == lwc.MsgTime() );
            bDup &= ( t1._acVol == lwc._acVol );
         }
      }
      return bDup;
   }

   private _SameSec _SameSecond( QuoddMsg[] mdb, int nRec )
   {
      _SameSec rtn;
      QuoddMsg qm;
      String[] kv;
      String   pt, ps, ms;
      long     rtl;
      char     mt, mt2;
      int      i;

      // Return true if results all from same second

      rtn = new _SameSec();
      for ( i=0; rtn._bSame && i<mdb.length; i++ ) {
         qm  = mdb[i];
         pt  = qm.pTimeMs().replace("[","").replace("]","");
         kv  = pt.split("\\.");
         ps  = kv[0];
         ms  = ( kv.length > 1 ) ? kv[1] : "xxx";
         mt  = qm.mt();
         mt2 = qm.mtSub();
         rtl = qm.RTL();
         if ( rtl == 0 )
            continue; // for-i
         if ( mt == MsgTypes._mtDEAD )
            continue; // for-i
         if ( ms.equals( "000" ) )
            continue; // for-i
         rtn._bSame = ( rtn._sec != null ) ? rtn._sec.equals( ps ) : rtn._bSame;
         rtn._sec   = ps;
         rtn._nRes += 1;
      }
      rtn._bSame &= ( rtn._nRes > 1 );
      return rtn;
   }

   private boolean _IsEOD( String dt, String tm )
   {
      String  today;
      boolean eod, bTime, bToday;

      today  = _svr.YYYYMMDD( System.currentTimeMillis() );
      bTime  = ( tm != null );
      bToday = dt.equals( "" ) || dt.equals( today );
      eod    = !bTime && !bToday;
      if ( _CanLog( _svr._SPRY_LOG_EOD ) ) {
         cout.printf( "IsEOD( %s, %s )=%s; today=%s; bToday=%s; bTime=%s\n", 
            dt, 
            bTime ? tm : "null",
            eod ? "true" : "false",
            today,
            bToday ? "true" : "false",
            bTime ? "true" : "false" );
      }
      return eod;
   }

   private boolean _IsAllowableRTL( long    rtl, 
                                    long    rtl0, 
                                    boolean bNxt, 
                                    boolean bPrv )
   {
      // Pre-condition

      if ( rtl0 == 0 )
         return true;

      // OK to check

      if ( bNxt )
         return( rtl < rtl0 );
      else if ( bPrv )
         return( rtl > rtl0 );
      return true;
   }

   private String HttpTime()
   {
      Calendar         cal;
      SimpleDateFormat sdf;
      String           fmt = "EEE, dd MMM yyyy HH:mm:ss z";

      cal = Calendar.getInstance();
      sdf = new SimpleDateFormat( fmt, Locale.US );
      sdf.setTimeZone( TimeZone.getTimeZone("GMT") );
      return sdf.format( cal.getTime() );
   }

   private String IncTime( String HMS, int tInc, int tIncMs )
   {
      String[] kv;
      String   hms;
      int      h, m, s, ms, nk, tm, sec;

      kv  = HMS.split("\\.");
      nk  = kv.length;
      hms = kv[0];
      ms  = _ToInt( ( nk > 1 ) ? kv[1] : "0" );
      kv  = hms.split(":");
      nk  = kv.length;
      h   = _ToInt( kv[0] );
      m   = _ToInt( ( nk > 1 ) ? kv[1] : "0" );
      s   = _ToInt( ( nk > 2 ) ? kv[2] : "0" );
      /*
       * 14-07-02 jcs  Handle Ankit bug - 00:00:00.000
       */
      if ( ( h == 0 ) && ( m == 0 ) && ( s == 0 ) && ( tInc < 0 ) )
         breakpoint();
      else {
         tm  = ( h* 3600 ) + ( m * 60 ) + s;
         tm += tInc;
         h   = tm / 3600;
         m   = ( tm / 60 ) % 60;
         s   = tm % 60;
      }
      ms  += tIncMs;
      sec  = ( 3600*h ) + ( 60*m ) + s;
      sec += ( ms / 1000 );
      ms   = ( ms+_MAX_MS ) % 1000;
      h    = sec / 3600;
      m    = ( sec / 60 ) % 60;
      s    = sec % 60;
      return hms.format( "%02d:%02d:%02d.%03d", h, m, s, ms );
   }

   private long MktCloseMillis()
   {
      SimpleDateFormat sdf;
      Date             dt, now;
      long             tm;
      String           ymd;
      String           fmt = "yyyy-MM-dd HH:mm:ss";

      sdf  = new SimpleDateFormat( "yyyy-MM-dd" );
      now  = new Date();
      ymd  = sdf.format( now );
      ymd += " 16:00:05";
      sdf  = new SimpleDateFormat( fmt, Locale.US );
      try {
         dt = sdf.parse( ymd );
         tm = dt.getTime();
      } catch( ParseException e ) {
         tm = 0;
      }
      return tm;
   }

   private int _ToInt( String s )
   {
      int rtn;

      try {
         rtn = Integer.parseInt( s );
      } catch( Exception e ) {
         rtn = 0;
      }
      return rtn;
   }

   private boolean _IsAscii( char ch )
   {
      return _svr._IsAscii( ch );
   }

   private boolean _CanLog( int type )
   {
      return _svr.CanLog( type );
   }

   private double _QtePrc( double  dBid, 
                           double  dAsk, 
                           int     iFltr, 
                           boolean bBbo )
   {
      double rtn;
      int    bMsk, aMsk;

      bMsk = bBbo ? _FLTR_BEST_BID : _FLTR_BID;
      aMsk = bBbo ? _FLTR_BEST_ASK : _FLTR_ASK;
      rtn  = ( dBid + dAsk ) / 2.0;
      if ( _BitIsSet( iFltr, bMsk ) )
         rtn = dBid;
      else if ( _BitIsSet( iFltr, aMsk ) )
         rtn = dAsk;
      return rtn;
   }

   private boolean _IsBboQte( int iFltr )
   {
      return _BitIsSet( iFltr, _FLTR_BEST_BID ) ||
             _BitIsSet( iFltr, _FLTR_BEST_ASK );
   }

   private boolean _IsMktCtrQte( int iFltr )
   {
      return _BitIsSet( iFltr, _FLTR_BID ) || _BitIsSet( iFltr, _FLTR_ASK );
   }

   private boolean _BitIsSet( int msk, int bit )
   {
      return( ( msk & bit ) == bit );
   }

   private boolean InRange( long a, long b, long c )
   {
      return( ( a <= b ) && ( b <= c ) );
   }

   private boolean InRange( double a, double b, double c )
   {
      return( ( a <= b ) && ( b <= c ) );
   }

   private void breakpoint() { ; }

   private void Disconnect()
   {
      try {
         if ( _sock != null ) {
            _sock.close();
         }
      } catch( IOException io ) {
         breakpoint();
      }
   }

   private String Now()
   {
      return _svr.Now( _dst );
   }

   private String Rtr2Quodd( String tkr )
   {
      boolean bSig2, bSig3;
      String  und, yr, mon, day, stk, prc;
      char[]  ca;
      char    ch;
      int     off, sz, iPrc;

      // Quick check : .U suffix

      ca = tkr.toCharArray();
      sz = tkr.length();
      if ( sz < 2 )
         return tkr;
      if ( tkr.charAt( sz-1 ) != 'U' )
         return tkr;
      if ( tkr.charAt( sz-2 ) != '.' )
         return tkr;
      if ( tkr.contains( "#" ) )
         return tkr;

      /*
       * IMPEDENCE MATCH : INazzCache.OptionName()
       *   "3 sig-fig, truncated to 2 if last is 0"
       *    AAPLB161326000.U -> O:AAPL\\13B16\\260.00
       */

      // Number of SigFig's in price

      ch    = tkr.charAt( sz-10 );
      bSig2 = ( '0' <= ch ) && ( ch <= '9' );
      ch    = tkr.charAt( sz-11 );
      bSig3 = ( '0' <= ch ) && ( ch <= '9' );
      if ( !bSig2 && !bSig3 )
         return tkr;
      off  = sz - 11;
      off -= bSig3 ? 1 : 0;

      //  Parse out all shit

      und  = new String( ca, 0, off );
      mon  = new String( ca, off, 1 );
      off += 1;
      day  = new String( ca, off, 2 );
      off += 2;
      yr   = new String( ca, off, 2 );
      off += 2;
      prc  = new String( ca, off, (sz-2)-off );
      try {
         iPrc = Integer.parseInt( prc );
      } catch( Exception e ) {
         return tkr;
      }
      stk = "";
      if ( bSig3 )
         stk = stk.format( "%.3f", 0.001 * iPrc );
      if ( bSig2 )
         stk = stk.format( "%.2f", 0.01 * iPrc );
      tkr = tkr.format( "O:%s\\%s%s%s\\%s", und, yr, mon, day, stk );
      return tkr;
   }

   private String Spry2Quodd( String tkr )
   {
      boolean  sig3;
      String[] kv;
      String   und, yr, mon, day, call, stk, prc, fmt;
      char[]   ca;
      char     ch;
      int      off, sz, iPrc, iMon, nk;
      String[] calls = { "A", "B", "C", "D", "E", "F",
                         "G", "H", "I", "J", "K", "L" };
      String[] puts  = { "M", "N", "O", "P", "Q", "R",
                         "S", "T", "U", "V", "W", "X" };

      // Quick check : .AAPL 140222C505000

      kv = tkr.split( " " );
      nk = kv.length;
      if ( nk != 2 )
         return tkr;
      if ( tkr.charAt( 0 ) != '.' )
         return tkr;

      // Parse out all shit ...

      ca   = kv[0].toCharArray();
      und  = new String( ca, 1, kv[0].length()-1 );
      ca   = kv[1].toCharArray();
      sz   = kv[1].length();
      off  = 0;
      yr   = new String( ca, off, 2 );
      off += 2;
      mon  = new String( ca, off, 2 );
      off += 2;
      day  = new String( ca, off, 2 );
      off += 2;
      call = new String( ca, off, 1 );
      off += 1;
      prc  = new String( ca, off, sz-off );
      try {
         iPrc = Integer.parseInt( prc );
      } catch( Exception e ) {
         return tkr;
      }
      try {
         iMon = Integer.parseInt( mon ) - 1;
      } catch( Exception e ) {
         return tkr;
      }

      // INazzCache.OptionName() : 3 sig-fig, truncated to 2 if last is 0

      stk  = "";
      ca   = prc.toCharArray();
      sz   = prc.length();
      sig3 = ( ca[sz-1] != '0' );
      fmt  = sig3 ? "%.3f" : "%.2f";
      stk  = stk.format( fmt, 0.001 * iPrc );
      if ( call.equals( "C" ) )
         mon = calls[iMon];
      else
         mon = puts[iMon];
      tkr = tkr.format( "O:%s\\%s%s%s\\%s", und, yr, mon, day, stk );
      return tkr;
   }

   class _FilterCfg
   {
      private int     _iFltr;
      private double  _prcMin;
      private double  _prcMax;
      private long    _szMin;
      private long    _szMax;
      private boolean _bQTE;

      _FilterCfg()
      {
         _iFltr  = 0;
         _prcMin = 0.0;
         _prcMax = Double.MAX_VALUE;
         _szMin  = 0;
         _szMax  = Long.MAX_VALUE;
         _bQTE   = false;
      }
   }

   class _SameSec
   {
      private String  _sec; 
      private boolean _bSame;
      private int     _nRes;

      _SameSec()
      {
         _sec   = null;
         _bSame = true;
         _nRes  = 0;
      }
   }
}


/////////////////////////////////////////////////////////////////
//
//                 c l a s s   R T L C a c h e
//
/////////////////////////////////////////////////////////////////
class RTLCache
{
   private SpryWareHTTP                 _svr;
   private Hashtable<String, RTLTicker> _cache;
   private PrintStream                  cout;

   //////////////////////
   // Constructor
   //////////////////////
   RTLCache( SpryWareHTTP svr )
   {
      _svr   = svr;
      _cache = new Hashtable<String, RTLTicker>();
      cout   = System.out;
   }


   //////////////////////
   // Access / Mutator
   //////////////////////
   public synchronized void AddRTL( String tkr, String rtl, String tm )
   {
      RTLTicker rt;

      if ( (rt=_cache.get( tkr )) == null ) {
         rt = new RTLTicker( tkr );
         _cache.put( tkr, rt );
      }
      rt.Add( rtl, tm );
   }

   public synchronized String GetTime( String tkr, String rtl )
   {
      RTLTicker rt;

      if ( (rt=_cache.get( tkr )) != null )
         return rt.Get( rtl );
      return null;
   }


   /////////////////////////////////////////////////////////////////
   //
   //               c l a s s   R T L T i c k e r
   //
   /////////////////////////////////////////////////////////////////
   private class RTLTicker
   {
      private String                    _tkr;
      private PrintStream                cout;
      private Hashtable<String, String> _timeByRTL;

      //////////////////
      // Constructor
      //////////////////
      RTLTicker( String tkr )
      {
         _tkr       = tkr;
         cout       = System.out;
         _timeByRTL = new Hashtable<String, String>();
      }


      //////////////////
      // Operations
      //////////////////
      String Get( String rtl )
      {
         return _timeByRTL.get( rtl );
      }

      void Add( String rtl, String tm )
      {
         if ( Get( rtl ) == null )
           _timeByRTL.put( rtl, tm );
      }

      void HiLo( String rtlQ )
      {
         ArrayList<String>   ddb;
         Comparator<String>  cmp;
         Enumeration<String> en;
         String              rtl, tm;

         cmp = Collections.reverseOrder();
         ddb = new ArrayList<String>();
         en  = _timeByRTL.keys();
         while( en.hasMoreElements() ) {
            rtl = en.nextElement();
            tm  = _timeByRTL.get( rtl );
            ddb.add( rtl );
         }
         Collections.sort( ddb, cmp );
      }
   }
}



/////////////////////////////////////////////////////////////////
//
//               c l a s s   S p r y W a r e H T T P
//
/////////////////////////////////////////////////////////////////
class SpryWareHTTP implements Runnable
{
   ////////////////
   // Class-wide
   ////////////////
   private static final long MILLIS_PER_DAY = 86400 * 1000;
   private static final int  NUM_DAY        = 40;
   /*
    * Logging from SPRY_LOGLVL
    */
   static public final int _SPRY_LOG_URL      = 0x0001;
   static public final int _SPRY_LOG_QUERY    = 0x0002;
   static public final int _SPRY_LOG_LATENCY  = 0x0004;
   static public final int _SPRY_LOG_RESPONSE = 0x0008;
   static public final int _SPRY_LOG_RANGE    = 0x0010;
   static public final int _SPRY_LOG_EOD      = 0x0020;
   static public final int _SPRY_LOG_RTL      = 0x0040;
   static public final int _SPRY_LOG_NEXT     = 0x0080;

   ////////////////
   // Instance
   ////////////////
   private int                       _port;
   private ServerSocket              _lsn;
   private Thread                    _thr;
   private ArrayList<_SpryClient>    _cli;
   private PrintStream               cout;
   private QuoddFeed.MD.UCLocCodeMap _ucMap;
   private boolean                   _bRtlCache;
   private RTLCache                  _rtlCache;
   private int                       _logLvl;
   private Hashtable<String, String> _exchID;

   //////////////////////
   // Constructor
   //////////////////////
   SpryWareHTTP( int     port, 
                 String  mcDef, 
                 String  optDef,
                 boolean bIsURL ) throws IOException
   {
      String   pl;
      String[] kv;
      int      i, nk, sz;

      // Basic Stuff

      _port  = port;
      _lsn   = new ServerSocket( _port );
      _thr   = new Thread( this );
      _cli   = new ArrayList<_SpryClient>();
      cout   = System.out;
      _ucMap = new QuoddFeed.MD.UCLocCodeMap();

      // Logging Level

      _logLvl = 0;
      if ( (pl=UCconfig.GetEnv( "SPRY_LOGLVL" )) != null ) {
         kv = pl.split(",");
         nk = kv.length;
         for ( i=0; i<nk; i++ ) {
            _logLvl |= kv[i].equals( "URL" )      ? _SPRY_LOG_URL      : 0;
            _logLvl |= kv[i].equals( "QUERY" )    ? _SPRY_LOG_QUERY    : 0;
            _logLvl |= kv[i].equals( "LATENCY" )  ? _SPRY_LOG_LATENCY  : 0;
            _logLvl |= kv[i].equals( "RESPONSE" ) ? _SPRY_LOG_RESPONSE : 0;
            _logLvl |= kv[i].equals( "RANGE" )    ? _SPRY_LOG_RANGE    : 0;
            _logLvl |= kv[i].equals( "EOD" )      ? _SPRY_LOG_EOD      : 0;
            _logLvl |= kv[i].equals( "RTL" )      ? _SPRY_LOG_RTL      : 0;
            _logLvl |= kv[i].equals( "NEXT" )     ? _SPRY_LOG_NEXT     : 0;
         }
      }
      if ( bIsURL )
         _ucMap.LoadFromURLs( mcDef, optDef );
      else
         _ucMap.LoadFromFiles( mcDef, optDef );

      // RTL Cache

      _bRtlCache = UCconfig.HasKey( "SPRY_RTL_CACHE" );
      cout.printf( "RTL Cache %s\n", _bRtlCache ? "ENABLED" : "DISABLED" );
      _rtlCache  = new RTLCache( this );

      // OPTrade Exch ID

      String[] mktCtr = { "AMXO", "ARCO", "BATO", "C2OX",
                          "XBOX", "XBXO", "XCBO", "XISX",
                          "XNDQ", "XPHO", "XMIO", "GMNI",
                          "EDGO", "MCRY", "MPRL"
                        };

      String[] mcID   = { "a", "n", "z", "w",
                          "b", "t", "c", "i",
                          "q", "x", "m", "h",
                          "e", "j", "p"
                        };

      _exchID = new Hashtable<String, String>();
      sz      = mktCtr.length;
      for ( i=0; i<sz; _exchID.put( mktCtr[i], mcID[i] ), i++ );

      // EQBbo Exch ID (underlyer ...)

      String[] EQ_mktCtr = { "XASE", "XBOS", "XCIS", "XADF",
                             "FINY", "XISE", "EDGA", "EDGX",
                             "XCHI", "XNYS", "ARCX", "XNAS",
                             "CBSX", "XPHL", "BATY", "BATS",
                             "XOTC", "OOTC", "OTCB", "OTCQ",
                             "PINX", "PSGM", "XTSE", "XTSX",
                             "FINN", "FINR" };
      String[] EQ_mcID   = { "A", "B", "C", "D",
                             "d", "I", "J", "K",
                             "M", "N", "P", "T",
                             "W", "X", "Y", "Z",
                             "BB", "OB", "QB", "QX",
                             "PK", "GM", "TO", "TV",
                             "D1", "D2" };

      sz = EQ_mktCtr.length;
      for ( i=0; i<sz; _exchID.put( EQ_mktCtr[i], EQ_mcID[i] ), i++ );

      // Fire when ready, Gridley

      _thr.start();
   }

   public void Stop()
   {
      int i, nc;

      synchronized( this ) { 
         try {
            if ( _lsn != null )
               _lsn.close();
         } catch( IOException e ) { ; }
         if ( _thr != null )
            _thr.interrupt();
         _thr = null;
      }
      nc   = _cli.size();
      for ( i=0; i<nc; _cli.get( i++ ).Stop() );
   }


   //////////////////////
   // RTL Cache
   //////////////////////
   public boolean CacheRTL()
   {
      return _bRtlCache;
   }

   public void AddRTL( String tkr, String rtl, String tm )
   {
      _rtlCache.AddRTL( tkr, rtl, tm );
   }

   public String GetTimeFromRTL( String tkr, String rtl )
   {
      return _rtlCache.GetTime( tkr, rtl );
   }


   //////////////////////
   // MktCtr Conversion
   //////////////////////
   public String OptExID( String exch )
   {
      String rtn;

      rtn = _exchID.get( exch );
      return ( rtn != null ) ? rtn : exch;
   }


   //////////////////////
   // UCLocCodeMap Access
   //////////////////////
   public String OptEX( String exch )
   {
      return _CleanExch( _ucMap.OptEX( exch ) );
   }

   public String GetExch( String T0, String tkr, int locCode )
   {
      String rtn;

      rtn = _ucMap.GetExch( T0, tkr, locCode );
      return _CleanExch( rtn );
   }

   private String _CleanExch( String ex )
   {
      return ex.replace( "??", "J" ).replace( "XXXX", "G" );
   }

   public boolean _IsZeroQuote( double bid, double ask )
   {
      return( ( bid == 0.0 ) && ( ask == 0.0 ) );
   }

   public boolean _IsAscii( char ch )
   {
      return _ucMap._IsAscii( ch );
   }

   public boolean CanLog( int type )
   {
      return( ( _logLvl & type ) == type );
   }

   public String Now( String dst )
   {
      return _ucMap.Now( dst );
   }

   public String YYYYMMDD( long tm )
   {
      return _ucMap.YYYYMMDD( tm );
   }


   //////////////////////
   // Runnable Interface
   //////////////////////
   public void run()
   {
      try {
         cout.printf( "SpryWareHTTP listening on port %d\n", _port );
         while( true ) {
            _cli.add( new _SpryClient( this, _lsn.accept() ) );
         }
      } catch( IOException e ) {
         cout.printf( "%s\n", e.getMessage() );
      }
      synchronized( this ) { 
         _lsn = null;
         _thr = null;
      }
   }

   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      String      mcDef, optDef;
      int          argc, port;
      Scanner      sc;
      PrintStream  cout;
      SpryWareHTTP spry;
      boolean      bIsURL;

      // Quick check

      argc = args.length;
      cout = System.out;
      if ( ( argc > 0 ) && args[0].equals( "--version" ) ) {
         cout.printf( "%s\n", QuoddMsg.Version() );
         return;
      }
      port   = ( argc > 0 ) ? Integer.parseInt( args[0] ) : 4040;
      mcDef  = ( argc > 1 ) ? args[1] : "./spry/MktCtr.opra";
      optDef = ( argc > 2 ) ? args[2] : "./spry/chan.opra";
      bIsURL = ( argc > 3 );
      try {
         spry = new SpryWareHTTP( port, mcDef, optDef, bIsURL );
      } catch( IOException e ) {
         cout.printf( "Can't listen() : %s\n", e.getMessage() );
         return;
      }
      sc   = new Scanner( System.in );
      sc.nextLine();
      cout.printf( "Shutting down ...\n" );
      spry.Stop();
      cout.printf( "Done!!\n" );
   }
}
