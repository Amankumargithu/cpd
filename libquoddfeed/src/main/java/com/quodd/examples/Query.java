/******************************************************************************
*
*  Query.java
*     Exercies Query commands to UltraCache
*
*  REVISION HISTORY:
*     13 OCT 2011 jcs  Created.
*      1 NOV 2011 jcs  Build  3: Query.java
*      9 NOV 2011 jcs  Build  3: oldMsg
*     17 NOV 2011 jcs  Build  5: BlobTable._bOldBlob
*     21 NOV 2011 jcs  Build  6: v0.7: _blSubEXCHLIST = BlobTable
*      2 DEC 2011 jcs  Build  7: v0.8: _blSubIDXLIST
*      2 JAN 2012 jcs  Build  9: v0.9: _blSubMMIDS; _blSubFUTURES
*     12 JAN 2012 jcs  Build 10: v0.11:GetMktMkrs( bLvl2Only )
*     30 JAN 2012 jcs  Build 13: UltraChan._usr / _pwd
*     18 APR 2012 jcs  Build 15: SyncGetExchanges()
*      3 MAY 2012 jcs  Build 17: GetExchTickers()
*     21 MAY 2012 jcs  Build 20: SyncGetFunds, Futures, Indices
*      8 JUN 2012 jcs  Build 23: GetFundamentalData()
*     19 JUN 2012 jcs  Build 24: GetFuturesChain()
*     28 JUN 2012 jcs  Build 27: UCconfig
*     10 JUL 2012 jcs  Build 28: GetFuturesSnap()
*     21 SEP 2012 jcs  Build 38: GetMktCategory()
*     31 JAN 2013 jcs  Build 51: 2nd constructor
*     25 FEB 2013 jcs  Build 54: GetBonds(); _blSubBONDS
*     12 FEB 2015 jcs  Build 90: GetFuturesOptionChain()
*     30 APR 2015 jcs  Build 92: No mo BlobTable._bOldBlob; _blSubDUMPDB
*     17 JUN 2015 jcs  Build 94: GetOptionUnderlyers(); BlobTable.Dump()
*
*  (c) 2011-2015 Quodd Financial
*******************************************************************************/
package com.quodd.examples;

import java.io.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
//
//                c l a s s   Q u e r y
//
/////////////////////////////////////////////////////////////////
/**
 * This example shows you how to query the UltraCache for any of the following:
 * <p>
 * 1) Schema via {@link QuoddFeed.util.UltraChan#GetFieldList(Object)} method<br>
 * 2) Exchange list via the
 *      {@link QuoddFeed.util.UltraChan#GetExchTickers(String,Object)} method.<br>
 * 3) Options chain via the
 *      {@link QuoddFeed.util.UltraChan#GetOptionChain(String,Object)} method<br>
 * 4) Options chain snapshot via the
 *      {@link QuoddFeed.util.UltraChan#GetOptionSnap(String,Object,String[])}
 *    method.
 */
public class Query extends UltraChan
{
   private String[]    _tkrs;
   private String      _arg;
   private boolean     _bMsgLog;
   private boolean     _bSync;
   private boolean     _bAllFut;
   private boolean     _bLvl2Only;
   private boolean     _bConn;
   private char        _qry;
   private PrintStream cout;

   //////////////////////
   // Constructor
   //////////////////////
   Query( String[] tkrs, 
          boolean  bMsgLog, 
          boolean  bByteLog, 
          boolean  bSync, 
          boolean  bAllFut, 
          char     qry )
   {
      this( UCconfig.Hostname(),
            UCconfig.Port(), 
            "username", 
            "password",
            tkrs,
            bMsgLog,
            bByteLog,
            bSync,
            bAllFut,
            qry );
   }

   Query( String   uHost,
          int      uPort,
          String   uUsr,
          String   uPwd,
          String[] tkrs,
          boolean  bMsgLog,
          boolean  bByteLog,
          boolean  bSync,
          boolean  bAllFut,
          char     qry )
   {
      super( uHost, uPort, uUsr, uPwd, bByteLog );
      _tkrs      = tkrs;
      _arg       = "User-defined closure for Query.java";
      _bMsgLog   = bMsgLog;
      _bSync     = bSync;
      _bAllFut   = bAllFut;
      _bLvl2Only = false;
      _bConn     = false;
      _qry       = qry;
      cout       = System.out;
   }


   //////////////////////
   // IUpdate Interface
   //////////////////////
   /**
    * Called when we connect to UltraCache.  Here, we query the UltraCache.
    */
   public void OnConnect()
   {
      int        i, nt;
      String[]   flds = {} ; // { "BID", "ASK", "STRIKE_PRICE" };
      AssetClass ass;

      cout.printf( "CONNECT %s:%d\n", uHost(), uPort() );

      // Free Synchronous (blocking) requests ...

      if ( _bSync ) {
         switch( _qry ) {
            case _blSubOPTIONS:
            case _blSubSNAPSHOT:
            case _blSubEXCHANGES:
            case _blSubEXCHLIST:
            case _blSubINDICES:
            case _blSubFUNDS:
            case _blSubBONDS:
            case _blSubFUT_CHAIN:
            case _blSubFUTOPT_CHAIN:
            case _blSubFUT_SNAP:
            case _blSubFUTURES:
            case _blSubFUNDY:
            case _blSubMKT_CAT:
            case _blSubDUMPDB:
               FreeSync();
               break;
         }
         return;
      }

      // Else, it is safe to post asynchronous (non-blocking) queries

      nt = _tkrs.length;
      switch( _qry ) {
         case _blSubFIELDLIST:
            GetFieldList( _arg );
            break;
         case _blSubEXCHANGES:
            GetExchanges( _arg );
            break;
         case _blSubEXCHLIST:
            for ( i=0; i<nt; GetExchTickers( _tkrs[i++], _arg ) );
            break;
         case _blSubOPTIONS:
            for ( i=0; i<nt; GetOptionChain( _tkrs[i++], _arg ) );
            break;
         case _blSubSNAPSHOT:
            for ( i=0; i<nt; GetOptionSnap( _tkrs[i++], _arg, flds ) );
            break;
         case _blSubIDXLIST:
            for ( i=0; i<nt; GetIndexParticipants( _tkrs[i++], _arg ) );
            break;
         case _blSubINDICES:
            GetIndices( _arg );
            break;
         case _blSubFUNDS:
            GetFunds( _arg );
            break;
         case _blSubBONDS:
            GetBonds( _arg );
            break;
         case _blSubFUT_CHAIN:
            for ( i=0; i<nt; GetFuturesChain( _tkrs[i++], _arg ) );
            break;
         case _blSubFUTOPT_CHAIN:
            for ( i=0; i<nt; GetFuturesOptionChain( _tkrs[i++], _arg ) );
            break;
         case _blSubFUT_SNAP:
            for ( i=0; i<nt; GetFuturesSnap( _tkrs[i++], _arg ) );
            break;
         case _blSubFUTURES:
            if ( _bAllFut ) GetFuturesAll( _arg );
            else            GetFutures( _arg );
            break;
         case _blSubMMIDS:
            for ( i=0; i<nt; GetMktMkrs( _tkrs[i++], _arg, _bLvl2Only ) );
            break;
         case _blSubFUNDY:
            for ( i=0; i<nt; i++ ) {
               ass = AssetClass.EQUITY;
// TODO : _tkrs[i].equal( EQUITY ) ...
               GetFundamentalData( _arg, ass );
            }
            break;
         case _blSubMKT_CAT:
            for ( i=0; i<nt; GetMktCategory( _arg, _tkrs[i++] ) );
            break;
         case _blSubDUMPDB:
            for ( i=0; i<nt; GetChannel( _arg, _tkrs[i++] ) );
            break;
         case _blSubUNDERLYERS:
            GetOptionsUnderlyers( _arg );
            break;
      }
   }

   /**
    * Called when we disconnect from UltraCache.  We do nothing here.
    */
   public void OnDisconnect()
   {
      cout.printf( "DISCONNECT %s:%d\n", uHost(), uPort() );
   }

   /**
    * Called when we UltraCache sends us the options chain response formatted
    * as a BlobList.  We dump to cout.
    */
   public void OnBlobList( String exch, BlobList b )
   {
      String  s, fmt;
      char    mt2;
      int     i, nt, nL;
      boolean bOpt, bFld;

      if ( (mt2=b.mtSub()) == _blSubFIELDLIST ) {
         _OnFieldList( b );
         return;
      }
      cout.print( b.Dump() );
   }

   /**
    * Called when we UltraCache sends us the options chain response formatted
    * as a BlobTable.  We dump to cout.
    */
   public void OnBlobTable( String exch, BlobTable b )
   {
      b.Dump( cout );
   }


   //////////////////////
   // Synchronous Query
   //////////////////////
   /**
    * Synchronous (blocking) query MUST be done from main thread.  If we 
    * try to do this from the UltraChan thread in OnConnect(), we lock up 
    * since OnConnect() tries to wait for itself to receive the response 
    * from UltraCache.  Since the thread can only do one thing at a time 
    * - i.e., wait() or UltraChain.Drain() - it never reads from the 
    * channel. 
    */
   public void WaitSync()
   {
      if ( !_bConn )
         _Wait();
   }

   public void PostSync()
   {
      int        i, nt, idx;
      String     pt, err;
      String[]   flds = {};
      AssetClass ass;
      BlobTable  bt;

      // Post blocking request

      nt = _tkrs.length;
      switch( _qry ) {
         case _blSubOPTIONS:
            for ( i=0; i<nt; i++ ) {
               pt  = _tkrs[i];
               idx = i+1;
               OnBlobList( pt, SyncGetOptionChain( pt, idx ) );
            }
            break;
         case _blSubSNAPSHOT:
            for ( i=0; i<nt; i++ ) {
               pt  = _tkrs[i];
               idx = i+1;
               OnBlobTable( pt, SyncGetOptionSnap( pt, idx, flds ) );
            }
            break;
         case _blSubEXCHANGES:
            OnBlobTable( _EXCHANGES, SyncGetExchanges( null ) );
            break;
         case _blSubEXCHLIST:
            for ( i=0; i<nt; i++ ) {
               pt  = _tkrs[i];
               idx = i+1;
               OnBlobTable( pt, SyncGetExchTickers( pt, idx ) );
            }
            break;
         case _blSubINDICES:
            OnBlobTable( _INDICES, SyncGetIndices( _arg ) );
            break;
         case _blSubFUNDS:
            OnBlobTable( _FUNDS, SyncGetFunds( _arg ) );
            break;
         case _blSubBONDS:
            OnBlobTable( _FUNDS, SyncGetBonds( _arg ) );
            break;
         case _blSubFUT_CHAIN:
            for ( i=0; i<nt; i++ )
               OnBlobTable( _FUT_CHAIN, SyncGetFuturesChain( _tkrs[i], _arg ) );
            break;
         case _blSubFUTOPT_CHAIN:
            for ( i=0; i<nt; i++ ) {
               bt = SyncGetFuturesOptionChain( _tkrs[i], _arg );
               OnBlobTable( _FUTOPT_CHAIN, bt );
            }
            break;
         case _blSubFUT_SNAP:
            for ( i=0; i<nt; i++ )
               OnBlobTable( _FUT_CHAIN, SyncGetFuturesSnap( _tkrs[i], _arg ) );
            break;
         case _blSubFUTURES:
            OnBlobTable( _FUTURES, SyncGetFutures( _arg ) );
            break;
         case _blSubFUNDY:
            for ( i=0; i<nt; i++ ) {
               ass = AssetClass.EQUITY;
// TODO : _tkrs[i].equal( EQUITY ) ...
               OnBlobTable( _FUNDY, SyncGetFundamentalData( _arg, ass ) );
            }
            break;
         case _blSubMKT_CAT:
            for ( i=0; i<nt; i++ ) {
               pt  = _tkrs[i];
               idx = i+1;
               OnBlobTable( _MKT_CAT, SyncGetMktCategory( _arg, pt ) );
            }
            break;
         case _blSubDUMPDB:
            for ( i=0; i<nt; i++ ) {
               pt  = _tkrs[i];
               idx = i+1;
               OnBlobTable( _DUMPDB, SyncGetChannel( _arg, pt ) );
            }
            break;
      }
   }

   private void FreeSync()
   {
      _bConn = true;
      _Notify();
   }


   //////////////////////
   // Helpers
   //////////////////////
   private void _OnMsg( String tkr, String mt, QuoddMsg m )
   {
      String pt = m.pTimeMs();

      cout.printf( "%s %-6s {%03d} %-6s ", pt, mt, m.tag(), tkr );
   }

   private void _OnFieldList( BlobList b )
   {
      String s;
      int    i, nt;

      _OnMsg( "", "FIELD LIST", b );
      nt   = b.lst().length;
      cout.printf( "nLst=%d\n", nt );
      for ( i=0; i<nt; i++ ) {
         s = b.lst()[i];
         cout.printf( "   %s\n", s );
      }
   }


   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      Query       cpd;
      Scanner     sc;
      String[]    tkrs = { "DELL", "XLE" };
      int         i, argc;
      boolean     bMsgLog, bByteLog, bSync, bAll;
      char        qry;
      PrintStream cout;

      cout     = System.out;
      argc     = args.length;
      tkrs     = ( argc > 0 ) ? args[0].split(",") : tkrs;
      bMsgLog  = false;
      bByteLog = false;
      bSync    = false;
      bAll     = false;
      qry      = _blSubFIELDLIST;
      for ( i=1; i<argc; i++ ) {
         bMsgLog  |= args[i].equals( "bMsgLog" );
         bByteLog |= args[i].equals( "bByteLog" );
         bSync    |= args[i].equals( "SYNCHRONOUS" );
         if ( args[i].equals( _EXCH_LIST ) )
            qry = _blSubEXCHLIST;
         else if ( args[i].equals( _OPT_CHAIN ) )
            qry = _blSubOPTIONS;
         else if ( args[i].equals( _OPT_SNAP ) )
            qry = _blSubSNAPSHOT;
         else if ( args[i].equals( _FIELD_LIST ) )
            qry = _blSubFIELDLIST;
         else if ( args[i].equals( _EXCHANGES ) )
            qry = _blSubEXCHANGES;
         else if ( args[i].equals( _IDX_LIST ) )
            qry = _blSubIDXLIST;
         else if ( args[i].equals( _INDICES ) )
            qry = _blSubINDICES;
         else if ( args[i].equals( _FUNDS ) )
            qry = _blSubFUNDS;
         else if ( args[i].equals( _BONDS ) )
            qry = _blSubBONDS;
         else if ( args[i].equals( _FUTURES ) )
            qry = _blSubFUTURES;
         else if ( args[i].equals( _FUT_CHAIN ) )
            qry = _blSubFUT_CHAIN;
         else if ( args[i].equals( _FUTOPT_CHAIN ) )
            qry = _blSubFUTOPT_CHAIN;
         else if ( args[i].equals( _FUT_SNAP ) )
            qry = _blSubFUT_SNAP;
         else if ( args[i].equals( _FUTURES_ALL ) ) {
            qry  = _blSubFUTURES;
            bAll = true;
         }
         else if ( args[i].equals( _MMIDS ) )
            qry = _blSubMMIDS;
         else if ( args[i].equals( _FUNDY ) )
            qry = _blSubFUNDY;
         else if ( args[i].equals( _MKT_CAT ) )
            qry = _blSubMKT_CAT;
         else if ( args[i].equals( _DUMPDB ) )
            qry = _blSubDUMPDB;
         else if ( args[i].equals( _OPT_UNDLYER ) )
            qry = _blSubUNDERLYERS;
      }
      cpd = new Query( tkrs, bMsgLog, bByteLog, bSync, bAll, qry );
      cpd.Start();
      cout.printf( "Hit <ENTER> to terminate...\n" );
      sc = new Scanner( System.in );
      if ( bSync ) {
         cpd.WaitSync();
         cpd.PostSync();
      }
      else
         sc.nextLine();
      cout.printf( "Shutting down ...\n" );
      cpd.Stop();
      cout.printf( "Done!!\n" );
   }
}
