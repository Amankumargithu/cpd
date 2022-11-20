/******************************************************************************
*
*  UltraChan.java
*     UltraCache channel handler
*
*  REVISION HISTORY:
*     13 OCT 2011 jcs  Created.
*      . . .
*      3 DEC 2013 jcs  Build  72: UC_SIGNAL
*     16 JAN 2014 jcs  Build  73: QueryTSQ()
*     11 FEB 2014 jcs  Build  75: _UC_LOG_WRBYTE; UC_TIERCHAN
*     15 MAY 2014 jcs  Build  76: QueryTSQ( bKid )
*      3 JUN 2014 jcs  Build  77: Dups in class MultiSnap
*     21 JUL 2014 jcs  Build  79: Cmd._xTranslate()
*     15 AUG 2014 jcs  Build  81: IUpdate.No mo OnRaw_OBSOLETE()
*      5 OCT 2014 jcs  Build  83: Heartbeat; _UC_LOG_BUFSIZ
*     17 OCT 2014 jcs  Build  84: Disconnect() : Free up _syncMsg
*     22 OCT 2014 jcs  Build  85: MultiSnap : Close all streams
*     12 NOV 2014 jcs  Build  86: UC_GUVNOR, UC_PROTOSWAP
*      1 DEC 2014 jcs  Build  87: Named threads; _UC_LOG_NUMUPD
*      3 FEB 2015 jcs  Build  90: _FUTOPT_CHAIN
*      7 MAR 2015 jcs  Build  91: Compression; Buffer
*     30 APR 2015 jcs  Build  92: DataStream._dMult; De-lint
*     24 MAY 2015 jcs  Build  93: _l2KO; _HashXxxFromFile()
*     12 JUN 2015 jcs  Build  94: SetTkr() before OnRaw()
*     17 AUG 2015 jcs  Build  96: SetByteLog()
*     17 DEC 2015 jcs  Build  98: ResubscribeZombieStreams()
*     18 APR 2016 jcs  Build 100: _blSubSORT; uc == null; cerr
*      8 DEC 2016 jcs  Build 101: _eqSubTRDASOF / _eqSubTRDASOFCXL
*
*  (c) 2011-2016 Quodd Financial
*******************************************************************************/
package QuoddFeed.util;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;
import java.util.zip.*;
import QuoddFeed.msg.*;
import QuoddFeed.sun.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
// 
//               c l a s s   U l t r a C h a n
//
/////////////////////////////////////////////////////////////////
/**
 * UltraChan is the connection to the UltraCache.  This class contains
 * methods to subscribe and unsubscribe data streams, send command 
 * queries - get field list, get options chain - and process the 
 * response.
 * <p>
 * Real-time updates are unpacked and delivered to your application via 
 * the polymorphic OnUpdate() methods.  Command response are delivered 
 * via the OnBlobXxxx() callback functions.
 */
public class UltraChan extends IUpdate 
                       implements Runnable
{
   ///////////////////////////
   // Enumerated types
   ///////////////////////////
   public enum AssetClass {
      EQUITY, INDEX, FUND, BOND
   };

   ///////////////////////////
   // SyncXxx() functions
   ///////////////////////////
   static protected final char _fcnSyncSnap                  = 'S';
   static protected final char _fcnSyncGetOptSnap            = 'O';
   static protected final char _fcnSyncGetOptChain           = 'C';
   static protected final char _fcnSyncGetExch               = 'E';
   static protected final char _fcnSyncGetExchTkrs           = 'T';
   static protected final char _fcnSyncGetFunds              = 'F';
   static protected final char _fcnSyncGetFuturesChain       = 'c';
   static protected final char _fcnSyncGetFuturesOptionChain = 'o';
   static protected final char _fcnSyncGetFuturesSnap        = 's';
   static protected final char _fcnSyncGetFutures            = 'f';
   static protected final char _fcnSyncGetBonds              = 'b';
   static protected final char _fcnSyncGetIndices            = 'I';
   static protected final char _fcnSyncGetFundy              = 'Y';
   static protected final char _fcnSyncGetMktCat             = 'M';
   static protected final char _fcnSyncDumpDb                = 'D';

   ///////////////////////////
   // SyncXxx() functions
   ///////////////////////////
   static private int       _nStat             =  0;
   static private final int _STAT_START        = _nStat++;
   static private final int _STAT_VERSION      = _nStat++;
   static private final int _STAT_CONN_STATE   = _nStat++;
   static private final int _STAT_CONN_COUNT   = _nStat++;
   static private final int _STAT_CONN_TIME    = _nStat++;
   static private final int _STAT_CONN_ADDR    = _nStat++;
   static private final int _STAT_RX_MSG       = _nStat++;
   static private final int _STAT_RX_BYTE      = _nStat++;
   static private final int _STAT_RX_TIME      = _nStat++;
   static private final int _STAT_RD_TIME      = _nStat++;
   static private final int _STAT_RD_WIN       = _nStat++;
   static private final int _STAT_TX_MSG       = _nStat++;
   static private final int _STAT_TX_BYTE      = _nStat++;
   static private final int _STAT_TX_TIME      = _nStat++;
   static private final int _STAT_TX_QSIZE     = _nStat++;
   static private final int _STAT_CPU_USED     = _nStat++;
   static private final int _STAT_NUM_SUBSCR   = _nStat++;
   static private final int _STAT_NUM_UNSUBSCR = _nStat++;
   static private final int _STAT_IN_READ      = _nStat++;
   static private final int _STAT_IN_PARSE     = _nStat++;
   static private final int _STAT_IN_SYNC      = _nStat++;
   static private final int _STAT_IN_NOTIFY    = _nStat++;
   static private final int _STAT_IN_APP       = _nStat++;
   static private final int _STAT_MSG_TYPE     = _nStat++;
   static private final int _STAT_MSG_SUBTYPE  = _nStat++;
   static private final int _STAT_IN_SESSHDB   = _nStat++;
   static private final int _STAT_IN_SEND      = _nStat++;
   static private final int _STAT_IN_SND1      = _nStat++;
   static private final int _STAT_GET_OUTSTR   = _nStat++;
   static private final int _STAT_IN_WRITE     = _nStat++;
   static private final int _STAT_WR_SIZ       = _nStat++;
   static private final int _STAT_IUPD_QSIZ    = _nStat++;
   static private final int _STAT_MAX          = _nStat++;

   ///////////////////////////
   // Logging from UC_LOG
   ///////////////////////////
   static protected final int _UC_LOG_BYTELOG   = 0x000001;
   static protected final int _UC_LOG_BUILD     = 0x000002;
   static protected final int _UC_LOG_SYNC      = 0x000004;
   static protected final int _UC_LOG_SUBSCR    = 0x000008;
   static protected final int _UC_LOG_SUBDUP    = 0x000010;
   static protected final int _UC_LOG_BLOB      = 0x000020;
   static protected final int _UC_LOG_DISCO     = 0x000040;
   static protected final int _UC_LOG_LVL2      = 0x000080;
   static protected final int _UC_LOG_SESS      = 0x000100;
   static protected final int _UC_LOG_PING      = 0x000200;
   static protected final int _UC_LOG_IMAGE     = 0x000400;
   static protected final int _UC_LOG_UPDATE    = 0x000800;
   static protected final int _UC_LOG_NUMUPD    = 0x001000;
   static protected final int _UC_LOG_WRBYTE    = 0x002000;
   static protected final int _UC_LOG_TIERDROP  = 0x004000;
   static protected final int _UC_LOG_BUFSIZ    = 0x008000;
   static protected final int _UC_LOG_MULTISNAP = 0x010000;
   static protected final int _UC_LOG_INFLATE   = 0x020000;
   static protected final int _UC_LOG_SYNCWL    = 0x040000;

   ////////////////////
   // Signal Handler
   ////////////////////
   static protected Object      _sigMtx    = new Object();
   static protected SigHandler  _sigHndlr  = null;
   static protected UltraChan[] _udb       = null;

   ////////////////////
   // Instance Members
   ////////////////////
   private   String[]                     _uHosts; // UltraCache hostname
   private   int                          _uPort;  // UltraCache port
   private   int                          _logLvl;
   private   String                       _conn;   // Current Connection
   private   String                       _usr;
   private   String                       _pwd;
   private   boolean                      _bByteLog;
   private   Factory                      _factory;
   private   Thread                       _thr;
   private   Object                       _readyMtx;
   private   Socket                       _sock;
   private   Buffer                       _buf;
   private   Buffer                       _bufZ;
   private   Inflater                     _zs;
   private   HashMap<Integer, DataStream> _hdb;
   private   HashMap<String, DataStream>  _sdb;  // Indexed by StreamID
   private   CPU                          _cpu;
   private   long                         _tot;
   private   boolean                      _bRun;
   private   boolean                      _bUP;
   private   int                          _uReqID;
   private   String                       _pendReq;
   private   int                          _syncFcn;
   private   int                          _syncIdx;
   private   QuoddMsg                     _syncMsg;
   private   Object                       _syncMtx;
   private   Governor                     _guvnor;
   private   ZombieDawg                   _zDawg;
   private   Heartbeat                    _hbDawg;
   private   RunTimeStats                 _stats;
   protected PrintStream                  cout;
   protected PrintStream                  cerr;
   private   boolean                      _bBasicBug;
   private   boolean                      _bAllowDup;
   private   boolean                      _bLvl2;
   private   boolean                      _bDiscoRd0;
   private   boolean                      _bSyncGet;
   private   boolean                      _bOnRaw;
   private   int                          _tierChan;
   private   int                          _tSlowMsg;
   private   long                         _tMsg0;
   private   long                         _nLogUpd;
   private   int                          _K;
   private   MultiSnap                    _multiSnap;
   private   HashSet<String>              _l2KO;
   private   HashMap<String, String>      _l2Req2nd;

   ////////////////////
   // Class-wide
   ////////////////////
   /**
    * Returns environment config info
    *
    * @return Environment config info
    */
   public static String Config()
   {
      String rtn = "";

      rtn += "Variable            Type  Description\n";
      rtn += "------------------- ----- ---------------------------\n";
      rtn += "UC_HOST             list  UltraCache hostname(s) - Comma-\n";
      rtn += "                          separated list; Default = localhost\n";
      rtn += "UC_PORT             int   UltraCache port; Default = 4321\n";
      rtn += "UC_USERNAME         str   UltraCache username\n";
      rtn += "UC_PASSWORD         str   UltraCache password\n";
      rtn += "UC_ALLOW_DUPLICATES bool  Set to allow duplicate Subscribe\n";
      rtn += "UC_DISCO_ON_READ0   bool  Set to disconnect if 0 bytes read\n";
      rtn += "UC_NOSYNC_QUERYWL   bool  Disable synchronized access to \n";
      rtn += "                          watchlist\n";
      rtn += "UC_BASICBUG         bool  Convert CSCO  CSCO,CSCO/T\n";
      rtn += "UC_LEVEL2           bool  Divide Quote Sizes by 100 if Level2\n";
      rtn += "UC_LEVEL2_NAME      bool  Ensure name and mktCtr's match\n";
      rtn += "UC_LEVEL2_2ND_REQ   str   Filename of L2 tickers - 2 comma-\n";
      rtn += "                          columns - to request BOTH in \n";
      rtn += "                          SubscribeLevel2(), formatted as:\n";
      rtn += "                             NSRGY,NSRGY/OB\n";
      rtn += "                             MRIC,MRIC/QB\n";
      rtn += "UC_LEVEL2_KO_LIST   str   Filename of L2 tickers to KO\n";
      rtn += "UC_TIERCHAN         int   Set tier chan for GetExch( TIER )\n";
      rtn += "UC_SLOW_MSG_MILLIS  int   Log msgs > this to process\n";
      rtn += "UC_EVENT_QUEUE      int   Force event queue w/ max size\n";
      rtn += "UC_SIGNAL           list  Signal-driven log dump of zombie \n";
      rtn += "                          streams (no response from UC) as:\n";
      rtn += "                          <signal>:<maxLog>, where <signal> \n";
      rtn += "                          is HUP, etc. not SIGHUP, etc.\n";
      rtn += "UC_ZOMBIE_DAWG      list  Timed log dump of zombie streams as \n";
      rtn += "                          <tDumpMs>:<maxLog>; <maxLog>=0 for \n";
      rtn += "                          auto-resubscribe every <tDumpMs>\n";
      rtn += "UC_GUVNOR           list  Subscribe Governor as <bias>:<paceMs>\n";
      rtn += "UC_PROTOSWAP        list  As <p1>:<p2> - Swap p2 for p1\n"; 
      rtn += "UC_ZLIB_COMPRESS    bool  Set to enable compression\n";
      rtn += "UC_LOGLVL           list  Comma-separated list of log levels:\n";
      rtn += "           BYTELOG     - Log **ALL** byte traffic in\n";
      rtn += "           BUILD       - Log build info\n";
      rtn += "           SYNC        - Log Enter / Leave SyncXxxx() call\n";
      rtn += "           SUBSCRIBE   - Log call to Subscribe()\n";
      rtn += "           DUPLICATES  - Log duplciate Subscribe()\n";
      rtn += "           BLOB        - Log full Blob List or Table\n";
      rtn += "           DISCONNECT  - Log Disconnect() and call stack\n";
      rtn += "           LEVEL2      - Log Level 2 Market Maker Image\n";
      rtn += "           SESSION     - Log Session handshake\n";
      rtn += "           PING        - Log Ping msg (if UC supports it)\n";
      rtn += "           IMAGE       - Log Image\n";
      rtn += "           NUMUPD      - Log every 100,000-th update\n";
      rtn += "           WRITEBYTES  - Log all bytes written to UC\n";
      rtn += "           TIERDROP    - Log tier drops (if UC_TIERCHAN is set)\n";
      rtn += "           SOCK_BUFSIZ - Log TCP buffer size on Connect()\n";
      rtn += "           MULTISNAP   - Log Multi Snap life cycle events\n";
      rtn += "           INFLATE     - Log inflate buffers if compressed\n";
      rtn += "           SYNCWL      - Log synchronized watchlist config\n";
      rtn += "           UPDATE      - UPDATE:10000 : Log every 10K update\n";
      return rtn;
   }

   //////////////////////
   // Constructor
   //////////////////////
   public UltraChan( String  uHost,
                     int     uPort, 
                     String  usr,
                     String  pwd,
                     boolean notUsed )
   {
      String pf;

      _K         = 1024;
      _uHosts    = UCconfig.Hostname( uHost ).split(",");
      _uPort     = UCconfig.Port( uPort );
      _logLvl    = 0;
      _conn      = "Not connected";
      _usr       = UCconfig.Username( usr );
      _pwd       = UCconfig.Password( pwd );
      _bByteLog  = false;
      _factory   = new Factory();
      _thr       = null;
      _readyMtx  = new Object();
      _sock      = null;
      _buf       = new Buffer( 10*_K*_K ); // 10 MB
      _bufZ      = null;
      _zs        = null;
      _hdb       = new HashMap<Integer, DataStream>();
      _sdb       = new HashMap<String, DataStream>();
      _cpu       = new CPU();
      _tot       = 0;
      _bRun      = true;
      _bUP       = false;
      _uReqID    = 1;
      _pendReq   = "";
      _syncFcn   = 0;
      _syncIdx   = 0;
      _syncMsg   = null;
      _syncMtx   = new Object();
      _guvnor    = null;
      _zDawg     = null;
      _hbDawg    = null;
      _stats     = null;
      cout       = System.out;
      cerr       = System.err;
      _bBasicBug = _factory.IsBasicBug();
      _bAllowDup = UCconfig.HasKey( "UC_ALLOW_DUPLICATES" );
      _bLvl2     = _factory.IsLvl2();
      _bDiscoRd0 = UCconfig.HasKey( "UC_DISCO_ON_READ0" );
      _bSyncGet  = !UCconfig.HasKey( "UC_NOSYNC_QUERYWL" );
      _bOnRaw    = false;
      _tierChan  = UCconfig.GetInt( "UC_TIERCHAN" );
      _tSlowMsg  = UCconfig.GetInt( "UC_SLOW_MSG_MILLIS" );
      _tMsg0     = 0;
      _nLogUpd   = 0;
      _multiSnap = null;
      pf         = UCconfig.GetEnv( "UC_LEVEL2_KO_LIST" );
      _l2KO      = _HashSetFromFile( pf, true );
      _l2Req2nd  = _HashMapFromFile( UCconfig.GetEnv( "UC_LEVEL2_2ND_REQ" ) );

      // Logging

      String   pl, ty, sig, dog, guv, pro, fmt;
      String[] kv, kv1;
      int      i, qSz, nk, nk1;

      if ( (pl=UCconfig.GetEnv( "UC_LOGLVL" )) != null ) {
         kv = pl.split(",");
         nk = kv.length;
         for ( i=0; i<nk; i++ ) {
            _logLvl |= kv[i].equals( "BYTELOG" )     ? _UC_LOG_BYTELOG   : 0;
            _logLvl |= kv[i].equals( "BUILD" )       ? _UC_LOG_BUILD     : 0;
            _logLvl |= kv[i].equals( "SYNC" )        ? _UC_LOG_SYNC      : 0;
            _logLvl |= kv[i].equals( "SUBSCRIBE" )   ? _UC_LOG_SUBSCR    : 0;
            _logLvl |= kv[i].equals( "DUPLICATES" )  ? _UC_LOG_SUBDUP    : 0;
            _logLvl |= kv[i].equals( "BLOB" )        ? _UC_LOG_BLOB      : 0;
            _logLvl |= kv[i].equals( "DISCONNECT" )  ? _UC_LOG_DISCO     : 0;
            _logLvl |= kv[i].equals( "LEVEL2" )      ? _UC_LOG_LVL2      : 0;
            _logLvl |= kv[i].equals( "SESSION" )     ? _UC_LOG_SESS      : 0;
            _logLvl |= kv[i].equals( "PING" )        ? _UC_LOG_PING      : 0;
            _logLvl |= kv[i].equals( "IMAGE" )       ? _UC_LOG_IMAGE     : 0;
            _logLvl |= kv[i].equals( "NUMUPD" )      ? _UC_LOG_NUMUPD    : 0;
            _logLvl |= kv[i].equals( "WRITEBYTES" )  ? _UC_LOG_WRBYTE    : 0;
            _logLvl |= kv[i].equals( "TIERDROP" )    ? _UC_LOG_TIERDROP  : 0;
            _logLvl |= kv[i].equals( "SOCK_BUFSIZ" ) ? _UC_LOG_BUFSIZ    : 0;
            _logLvl |= kv[i].equals( "MULTISNAP" )   ? _UC_LOG_MULTISNAP : 0;
            _logLvl |= kv[i].equals( "INFLATE" )     ? _UC_LOG_INFLATE   : 0;
            _logLvl |= kv[i].equals( "SYNCWL" )      ? _UC_LOG_SYNCWL    : 0;
            if ( kv[i].startsWith( "UPDATE" ) ) {
               kv1 = kv[i].split(":");
               nk1 = kv1.length;
               if ( nk1 <= 1 )
                  continue; // for-i
               try {
                  _nLogUpd = Integer.parseInt( kv1[1], 10 );
               } catch( NumberFormatException e ) {
                  _nLogUpd = 0;
               }
               _logLvl |= ( _nLogUpd > 0 ) ? _UC_LOG_UPDATE : 0;
            }
         }
      }
      if ( _CanLog( _UC_LOG_BUILD ) )
         cout.printf( "[%s] %s\n", Now(), QuoddMsg.Version() );
      if ( _bLvl2 )
         cout.printf( "[%s] LEVEL2 divide by 100\n", Now() );
      if ( _CanLog( _UC_LOG_SYNCWL ) ) {
         ty = _bSyncGet ? "" : "NOT ";
         cout.printf( "[%s] QueryWL %sSYNCHRONIZED\n", Now(), ty );
      }

      // Force an event queue?

      if ( (qSz=UCconfig.GetInt( "UC_EVENT_QUEUE" )) > 0 ) {
         StartQ( qSz );
         cout.printf( "[%s] EVENT QUEUE : Max = %d\n", Now(), qSz );
      }

      /*
       * ZOMBIE:
       *    1) Signal Handler
       *    2) Timed Dawg
       */
      UltraChan[] udb;
      int         nu, maxLog, tDumpMs, bias, pace, pro1, pro2;

      if ( (sig=UCconfig.GetEnv( "UC_SIGNAL" )) != null ) {
         synchronized( _sigMtx ) {
            if ( _sigHndlr == null ) {
               kv = sig.split(":");
               nk = kv.length;
               try {
                  maxLog = Integer.valueOf( kv[1] ).intValue();
               } catch( Exception e ) {
                  maxLog = 1000;
               }
               _sigHndlr = new SigHandler( kv[0], maxLog, this );
            }
            nu  = ( _udb != null ) ? _udb.length : 0;
            udb = new UltraChan[nu+1];
            for ( i=0; i<nu; udb[i]=_udb[i], i++ );
            udb[i] = this;
            _udb = udb;
            _sigHndlr.SetChans( _udb );
         }
      }
      if ( (dog=UCconfig.GetEnv( "UC_ZOMBIE_DAWG" )) != null ) {
         kv = dog.split(":");
         nk = kv.length;
         try {
            tDumpMs = Integer.valueOf( kv[0] ).intValue();
            maxLog  = Integer.valueOf( kv[1] ).intValue();
         } catch( Exception e ) {
            tDumpMs = 60000;
            maxLog  = 1000;
         }
         if ( tDumpMs > 0 ) {
            cout.printf( "[%s] ZOMBIE Dawg : %dmS\n", Now(), tDumpMs );
            _zDawg = new ZombieDawg( this, tDumpMs, maxLog );
         }
      }

      /*
       * UC_GUVNOR
       */
      if ( (guv=UCconfig.GetEnv( "UC_GUVNOR" )) != null ) {
         kv = guv.split(":");
         nk = kv.length;
         try {
            bias = Integer.valueOf( kv[0] ).intValue();
            pace = Integer.valueOf( kv[1] ).intValue();
         } catch( Exception e ) {
            bias = 0;
            pace = 0;
         }
         if ( ( bias > 0 ) && ( pace > 0 ) ) {
            fmt = "[%s] Governor : %d every %dmS\n";
            cout.printf( fmt, Now(), bias, pace );
            SetRequestRate( bias, pace );
         }
      }

      /*
       * UC_PROTOSWAP
       */
      if ( (pro=UCconfig.GetEnv( "UC_PROTOSWAP" )) != null ) {
         kv = pro.split(":");
         nk = kv.length;
         try {
            pro1 = Integer.valueOf( kv[0] ).intValue();
            pro2 = Integer.valueOf( kv[1] ).intValue();
         } catch( Exception e ) {
            pro1 = 0;
            pro2 = 0;
         }
         if ( ( pro1 > 0 ) && ( pro2 > 0 ) ) {
            fmt = "[%s] SWAP protocol %d for %d\n";
            cout.printf( fmt, Now(), pro2, pro1 );
            QuoddMsg.SwapProtocols( pro1, pro2 );
         }
      }

      // Compression?

      SetCompression( UCconfig.HasKey( "UC_ZLIB_COMPRESS" ) );
   }

   /**
    * Creates and starts the channel handling thread.
    */
   public void Start()
   {
      String err;

      // Once

      if ( _thr != null )
         return;
      _bRun = true;
      _thr  = new Thread( this, _usr + "_UltraChan" );

      // Start

      _thr.start();
      try {
         synchronized( _readyMtx ) {
            _readyMtx.wait();
         }
      } catch( InterruptedException e ) {
         err = e.getMessage();
      }
   }

   /**
    * Closes channel; Stops and destroys the channel handling thread.
    */
   public void Stop()
   {
      // Threads

      if ( _guvnor != null )
         _guvnor.Stop();
      if ( _zDawg != null )
         _zDawg.Stop();
      if ( _hbDawg != null )
         _hbDawg.Stop();
      _guvnor = null;
      _zDawg  = null;
      _hbDawg = null;

      _bRun = false;
      try {
         if ( _sock != null )
            _sock.close();
      } catch( IOException io ) {
         breakpoint();
      }
      _sock = null;
      _conn = "Not connected";
      if ( _thr != null )
         _thr.interrupt();
      _thr = null;
   }


   ////////////////////
   // Run-time Stats
   ////////////////////
   /**
    * Sets the run-time statistics for the channel.  Creates a 
    * RunTimeStats object with room for up to maxStat statistics.<p>
    * THE FILENAME MUST BE UNIQUE FOR EVERY UltraChan CHANNEL OBJECT THAT
    * YOU WISH TO MONITOR<p?
    * @param file Filename containing the run-time stats
    * @param maxStat Max number of statistics in file
    * @return {@link QuoddFeed.util.RunTimeStats#RunTimeStats(String,int, boolean)}
    */
   public RunTimeStats SetRunTimeStats( String file, int maxStat )
   {
      maxStat += _STAT_MAX;
      _stats   = new RunTimeStats( file, maxStat, false );
      _stats.InsertTime( "tStart" );
      _stats.InsertString( "Version" );
      _stats.InsertBool( "UltraChan.Connected?" );
      _stats.InsertLong( "UltraChan.NumConn" );
      _stats.InsertTime( "UltraChan.TimeConn" );
      _stats.InsertString( "UltraChan.Address" );
      _stats.InsertLong( "UltraChan.RXMsg" );
      _stats.InsertLong( "UltraChan.RXByte" );
      _stats.InsertTime( "UltraChan.RXTime" );
      _stats.InsertTime( "UltraChan.ReadTime" );
      _stats.InsertLong( "UltraChan.ReadWindow" );
      _stats.InsertLong( "UltraChan.TXMsg" );
      _stats.InsertLong( "UltraChan.TXByte" );
      _stats.InsertTime( "UltraChan.TXTime" );
      _stats.InsertLong( "UltraChan.TXQueue" );
      _stats.InsertDouble( "UltraChan.CPUused" );
      _stats.InsertLong( "UltraChan.NumSubscribe" );
      _stats.InsertLong( "UltraChan.NumUnsubscribe" );
      _stats.InsertBool( "UltraChan.Read()" );
      _stats.InsertBool( "UltraChan.Factory()" );
      _stats.InsertBool( "UltraChan.SyncWait()" );
      _stats.InsertBool( "UltraChan.SyncNotify()" );
      _stats.InsertBool( "UltraChan.OnUpdate()" );
      _stats.InsertHex( "USER_APP.MsgType" );
      _stats.InsertHex( "USER_APP.MsgSubType" );
      _stats.InsertBool( "Session.Lock()" );
      _stats.InsertBool( "Java.InSend()" );
      _stats.InsertBool( "Java.InSend2()" );
      _stats.InsertBool( "Java.GetOutStream()" );
      _stats.InsertBool( "Java.Write()" );
      _stats.InsertLong( "Java.WriteSize" );
      _stats.InsertLong( "IUpdate.QueueSize" );

      _stats.PutTimeNow( _STAT_START );
      _stats.PutString( _STAT_VERSION, QuoddMsg.Version() );
      return GetRunTimeStats();
   }

   /**
    * Returns the run-time statistics object associated with this channel.
    * <p>
    * @return {@link QuoddFeed.util.RunTimeStats#RunTimeStats(String,int, boolean)}
    */
   public RunTimeStats GetRunTimeStats()
   {
      return _stats;
   }


   ////////////////////
   // Logging / Compression
   ////////////////////
   /**
    * \brief Enable / disable verbose logging on this channel.
    *
    * THIS DRAMATICALLY SLOWS DOWN THE PERFORMANCE OF THIS CHANNEL.
    *
    * \param bByteLog - true to enable
    */
   void SetByteLog( boolean bByteLog )
   {
      _bByteLog = bByteLog;
   }

   /**
    * \brief Returns true if compressed stream channel.
    * <p>
    * \return true if compressed stream channel.
    */
   boolean IsCompressed()
   {
      return( _bufZ != null );
   }

   /**
    * \brief Enable / disable compression on this channel.
    *
    * You must set compression before calling Start().
    *
    * \param bCompress - true to compress
    */
   void SetCompression( boolean bCompress )
   {
      _bufZ = null;
      _zs   = null;
      if ( bCompress ) {
         _zs   = new Inflater();
         _bufZ = new Buffer( _buf.bSz() * 5 );
      }
   }


   ////////////////////
   // Zombie Streams
   ////////////////////
   /**
    * \brief 
    *
    * \return Number of zombie streams that were resubscribed
    */
   public int ResubscribeZombieStreams()
   {
      DataStream[] zdb;
      DataStream   ds;
      Object       obj;
      String       tkr;
      int          i, idOld, idNew;

      // Pre-condition

      if ( (zdb=GetZombieStreams()) == null )
         return 0;

      // Process ...

      for ( i=0; i<zdb.length; i++ ) {
         ds    = zdb[i];
         idOld = ds.StreamID();
         tkr   = ds.tkr();
         obj   = ds.obj();
         Unsubscribe( idOld );
         idNew = Subscribe( tkr, obj );
         OnResubscribe( tkr, idOld, idNew );
      }
      return zdb.length;
   }

   private DataStream[] GetZombieStreams()
   {
      DataStream            uc;
      DataStream[]          rtn;
      ArrayList<DataStream> zdb;
      int                   i, nd, sz;

      zdb = new ArrayList<DataStream>();
      synchronized( _hdb ) {
         sz = _hdb.size();
         for ( Map.Entry<Integer, DataStream> e : _hdb.entrySet() ) {
            uc = e.getValue();
            if ( uc._nUpd == 0 )
               zdb.add( uc );
         }
      }
      nd = zdb.size();
      if ( nd == 0 )
         return null;
      rtn = new DataStream[nd];
      for ( i=0; i<nd; rtn[i] = zdb.get( i ), i++ );
      return rtn;
   }

   private void dumpZombies( String sigName, int maxLog )
   {
      DataStream[] zdb;
      String       fmt, s, sep, tkr;
      int          i, nd, sz, id;

      // <maxLog> == 0 : Auto-resub

      if ( maxLog == 0 ) {
         ResubscribeZombieStreams();
         return;
      }

      // Pre-condition

      if ( (zdb=GetZombieStreams()) == null )
         return;

      // Process ...

      synchronized( _hdb ) {
         sz = _hdb.size();
      }
      nd  = zdb.length;
      s   = "";
      sep = "; ";
      for ( i=0; i<Math.min( maxLog,nd ); i++ ) {
         id  = zdb[i].StreamID();
         tkr = zdb[i].tkr();
         s   = s.format( "%s%s[%d,%s]", s, sep, id, tkr );
         sep = ", ";
      }
      if ( ( i > 0 ) && ( i != nd ) )
         s += ", ...";
      fmt = "[%s] dumpZombies( %s ) : %d of %d ZOMBIE%s\n";
      cout.printf( fmt, Now(), sigName, nd, sz, s );
   }


   ////////////////////
   // Paced Requests
   ////////////////////
   public void SetRequestRate( int bias, int pace )
   {
      /*
       * 1) Stop existing guv'nor
       * 2) 0 bias or pace = no guv'nor
       * 3) pace in millis
       */
      if ( _guvnor != null )
         _guvnor.Stop();
      _guvnor = null;
      if ( ( bias > 0 ) && ( pace > 0 ) )
         _guvnor = new Governor( this, bias, pace );
   }


   ////////////////////
   // OnRaw() Enable
   ////////////////////
   /**
    * Set to true to enable callout to OnRaw() 
    * <p>
    * @param bOnRaw Set to true to have UltraChan call out to 
    * {@link QuoddFeed.util.IUpdate#OnRaw(DataInputStream,QuoddMsg,ByteBuffer)}
    * on each message
    */
   public void SetOnRaw( boolean bOnRaw )
   {
      _bOnRaw = bOnRaw;
   }


   ////////////////////
   // Access
   ////////////////////
   /**
    * UltraCache server hostname (or IP address) we are connected to
    * <p>
    * @return UltraCache server hostname (or IP address) we are connected to
    */
   public String uHost()
   {
      return _conn;
   }

   /**
    * UltraCache server port number
    * <p>
    * @return UltraCache server port number
    */
   public int uPort()
   {
      return _uPort;
   }

   /**
    * Returns call stack at the current point in the code
    * <p>
    * @return String showing current call stack
    */
   public String CallStack()
   {
      StackTraceElement[] rsn;
      String              cs;
      int                 i, sz;

      // Don't include UltraChan.CallStack()

      rsn = new Exception().getStackTrace();
      cs  = "";
      sz  = rsn.length;
      for ( i=1; i<sz; cs += cs.format( "%s\n", rsn[i++].toString() ) );
      return cs;
   }


   //////////////////////
   // UltraChan Interface
   //////////////////////
   /**
    * Callback invoked when we connect to UltraCache
    */
   public void OnConnect()
   {
      // Derived Classes Implement
   }

   /**
    * Callback invoked when we disconnect from UltraCache 
    */
   public void OnDisconnect()
   {
      // Derived Classes Implement
   }

   /**
    * Callback invoked when session message is received
    *
    * @param txt Textual description
    * @param bUP true if UP; false if down
    */
   public void OnSession( String txt, boolean bUP )
   {
      // Derived Classes Implement
   }

   /**
    * Callback invoked when zombie stream - Subscribe(), but no response - 
    * is re-subscribe'ed via ResubscribeZombieStreams()
    *
    * @param tkr - Ticker
    * @param oldStreamID - Original StreamID from Subscribe(), now closed
    * @param newStreamID - New StreamID from Subscribe(), now open
    */
   public void OnResubscribe( String tkr, int oldStreamID, int newStreamID )
   {
      // Derived Classes Implement
   }


   //////////////////////
   // Stream Operations
   //////////////////////
   /**
    * Associates a list of allowable protocol ID's for a data stream. 
    * <p>
    * Updates from protocols that are not on this list are dropped 
    * by UltraChan.
    * <p>
    * @param StreamID Data Stream ID from Subscribe(), etc.
    * @param fltr List of allowable protocol ID's for this stream
    */
   public boolean SetProtocolFilter( int StreamID, int[] fltr )
   {
      DataStream uc;

      synchronized( _hdb ) {
         if ( _hdb.containsKey( StreamID ) ) {
            uc  = _hdb.get( StreamID );
            uc.SetProtocolFilter( fltr );
            return true;
         }
      }
      return false;
   }


   //////////////////////
   // Single-Ticker Stream
   //////////////////////
   /**
    * Queries time / sales / quotes for a ticker
    * <p>
    * @param tkr Ticker
    * @param tStart Start time in HH:MM:SS
    * @param tEnd End time in HH:MM:SS
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int QueryTSQ( String  tkr, 
                        String  tStart, 
                        String  tEnd, 
                        String  msgTy, 
                        boolean bKid,
                        int     numRec,
                        String  bboEx, 
                        Object  obj )
   {
      String arg;

      // <OPN Name="DELL" TAG="12345" Type="TSQDB" . . . /">

      arg  = "\" ";
      arg += arg.format( "Type=\"%s\" ", _TSQDB );
      arg += arg.format( "%s=\"%s\" ", _pStartTime, tStart );
      arg += arg.format( "%s=\"%s\" ", _pEndTime, tEnd );
      arg += arg.format( "%s=\"%s\" ", _pMsgType, msgTy );
      arg += arg.format( "%s=\"%s\" ", _pAttrKids, bKid ? _pYes : _pNo );
      if ( bboEx != null )
         arg += arg.format( "%s=\"%s\" ", _pAttrExch, bboEx );
      arg += arg.format( "%s=\"%d", _pAttrNum, numRec );
      return _Subscribe( _xTranslate( tkr ) + arg, obj, null, true );
   }

   /**
    * Snaps a ticker
    * <p>
    * @param tkr Ticker
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int Snap( String tkr, Object obj )
   {
      String xt;

      // <OPN Name="DELL" TAG="12345" Type="Snap"/>

      xt = _xTranslate( tkr );
      return _Subscribe( xt + "\" Type=\"Snap", obj, null, true );
   }

   /**
    * Snaps a ticker and registers IUpdate receiver
    * <p>
    * @param tkr Ticker
    * @param obj User-supplied Object
    * @param iUpd IUpdate object to receive Stream
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int Snap( String tkr, Object obj, IUpdate iUpd )
   {
      String xt;

      // <OPN Name="DELL" TAG="12345" Type="Snap"/>

      xt = _xTranslate( tkr );
      return _Subscribe( xt + "\" Type=\"Snap", obj, iUpd, true );
   }

   /**
    * Opens a subscription stream
    * <p>
    * @param tkr Ticker
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int Subscribe( String tkr, Object obj )
   {
      return _Subscribe( _xTranslate( tkr ), obj, null, true );
   }

   /**
    * Opens a subscription stream and streams to user-defined IUpdate object.
    * <p>
    * @param tkr Ticker
    * @param obj User-supplied Object
    * @param iUpd IUpdate object to receive Stream
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int Subscribe( String tkr, Object obj, IUpdate iUpd )
   {
      return _Subscribe( _xTranslate( tkr ), obj, iUpd, true );
   }

   /**
    * Closes a subscription stream
    * <p>
    * @param StreamID Unique data streamID returned from
    * {@link QuoddFeed.util.UltraChan#Subscribe(String,Object)}
    */
   public void Unsubscribe( int StreamID )
   {
      String[] tkrs;
      int      i, sz, sid;

      sid  = StreamID;
      tkrs = _CloseStream( sid );
      if ( _HasStats() )
         _stats.IncLong( _STAT_NUM_UNSUBSCR );
      sz = ( tkrs != null ) ? tkrs.length : 0;
      for ( i=0; i<sz; i++ ) {
         _Send( sid, fmtUnsubscribe( tkrs[i], sid ) );
         if ( _CanLog( _UC_LOG_SUBSCR ) )
            cout.printf( "[%s] UNSUBSCRIBE( %s )\n", Now(), tkrs[i] ); 
      }
   }


   //////////////////////
   // Multi-Ticker Stream 
   //////////////////////
   /**
    * Opens a subscription stream for entire exchange
    * <p>
    * @param exch Exchange
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int SubscribeExchange( String exch, Object obj )
   {
      return SubscribeExchange ( exch, obj, null );
   }

   /**
    * Opens a subscription stream for entire exchange and streams to 
    * user-defined IUpdate object.
    * <p>
    * @param exch Exchange
    * @param obj User-supplied Object
    * @param iUpd IUpdate object to receive Stream
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int SubscribeExchange( String exch, Object obj, IUpdate iUpd )
   {
      String tkr;

      // <OPN Name="ARCX" Type="EXCHANGE_LIST"/>

      tkr = _xTranslate( exch ) + "\" Type=\"" + _EXCH_LIST;
      return _Subscribe( tkr, obj, iUpd, false );
   }

   /**
    * Opens a subscription stream for an exchange - qualified by list
    * of alphanumeric range, e.g. A:D;M for A,B,C,D and M
    * <p>
    * @param exch Exchange
    * @param qual Qualifier, e.g. A:D;M for A,B,C,D and M
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int SubscribeExchange( String exch, String qual, Object obj )
   {
      String qualExch;

      // <OPN Name="ARCX-A:D;M" Type="EXCHANGE_LIST"/>

      qualExch = exch + "-" + qual;
      return SubscribeExchange( qualExch, obj, null );
   }

   /**
    * Opens a subscription stream for an exchange - qualified by list
    * of alphanumeric range, e.g. A:D;M for A,B,C,D and M
    * and streams to user-defined IUpdate object.
    * <p>
    * @param exch Exchange
    * @param qual Qualifier, e.g. A:D;M for A,B,C,D and M
    * @param obj User-supplied Object
    * @param iUpd IUpdate object to receive Stream
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int SubscribeExchange( String  exch, 
                                 String  qual, 
                                 Object  obj,
                                 IUpdate iUpd )
   {
      String qualExch;

      // <OPN Name="ARCX-A:D;M" Type="EXCHANGE_LIST"/>

      qualExch = exch + "-" + qual;
      return SubscribeExchange( qualExch, obj, iUpd );
   }


   /**
    * Closes a subscription stream for entire exchange
    * <p>
    * @param StreamID Unique data streamID returned from
    * {@link QuoddFeed.util.UltraChan#SubscribeExchange(String,Object)}
    */
   public void UnsubscribeExchange( int StreamID )
   {
      String[] exch;
      String   msg;

      // <CLS Name="ARCX" Type="EXCHANGE_LIST"/>

      exch = _CloseStream( StreamID );
      if ( exch != null ) {
         msg = fmtUnsubscribe(  exch[0] + "\" Type=\"" + _EXCH_LIST, StreamID );
         _Send( StreamID, msg );
      }
   }

   /**
    * Opens a subscription stream for all market makers
    * <p>
    * @param tkr Ticker
    * @param obj User-supplied Object
    * @param bLvl2Only Set to True to see only Level 2 market makers
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int SubscribeLevel2( String  tkr,
                               Object  obj,
                               boolean bLvl2Only )
   {
      return SubscribeLevel2( tkr, obj, bLvl2Only, null );
   }

   /**
    * Opens a subscription stream for all market makers
    * and streams to user-defined IUpdate object.
    * <p>
    * @param tkr Ticker
    * @param obj User-supplied Object
    * @param bLvl2Only Set to True to see only Level 2 market makers
    * @param iUpd IUpdate object to receive Stream
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int SubscribeLevel2( String  tkr,
                               Object  obj,
                               boolean bLvl2Only,
                               IUpdate iUpd )
   {
      String     sub, req2;
      DataStream uc;
      int        idx, idx2;

      // 2nd Request - MRIC/QB and MRIC

      req2 = null;
      if ( ( _l2Req2nd != null ) && _l2Req2nd.containsKey( tkr ) )
         req2 = _l2Req2nd.get( tkr );

      // <OPN Name="DELL" Type="MARKET_MAKERS"/>

      sub  = _xTranslate( tkr ) + "\" Type=\"" + _MMIDS;
      sub += "\" " + _pAttrLvl2 + "=\"";
      sub += bLvl2Only ? _pYes : _pNo;
      synchronized( _hdb ) {
         idx  = _Subscribe( sub, obj, iUpd, false );
         if ( (uc=_GetStream( idx )) != null )
            uc._bLevel2 = true;
         if ( req2 != null ) {
            _uReqID--; // Same index for MRIC and MRIC/QB
            idx2 = SubscribeLevel2( req2, obj, bLvl2Only, iUpd );
            if ( idx != idx2 )
               breakpoint();  // BIG TROUBLE ... SHOULD NEVER HAPPEN
         }
      }
      return idx;
   }

   /**
    * Closes a subscription stream for all market makers
    * <p>
    * @param StreamID Unique data streamID returned from
    * {@link #SubscribeLevel2(String,Object,boolean)}
    */
   public void UnsubscribeLevel2( int StreamID )
   {
      String[] tkr;
      String   msg;

      // <CLS Name="DELL" Type="MARKET_MAKERS"/>

      tkr = _CloseStream( StreamID );
      if ( tkr != null ) {
         msg = fmtUnsubscribe( tkr[0] + "\" Type=\"" + _MMIDS, StreamID );
         _Send( StreamID, msg );
      }
   }


   //////////////////////
   // All Level2 Stream
   //////////////////////
   /**
    * Opens a subscription stream for ALL Level2 (not market makers)
    * <p>
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int SubscribeAllLvl2( Object obj )
   {
      // <OPN Name="ALL_LEVEL2"/>

      return Subscribe( _ALL_LVL2, obj );
   }

   /**
    * Opens a subscription stream for ALL Level2 (not market makers)
    * and streams to user-defined IUpdate object.
    * <p>
    * @param obj User-supplied Object
    * @param iUpd IUpdate object to receive Stream
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int SubscribeAllLvl2( Object obj, IUpdate iUpd )
   {
      // <OPN Name="ALL_LEVEL2"/>

      return Subscribe( _ALL_LVL2, obj, iUpd );
   }

   /**
    * Closes a subscription stream for ALL Level2 (not market makers)
    * <p>
    * @param StreamID Unique data streamID returned from
    * {@link QuoddFeed.util.UltraChan#SubscribeAllLvl2(Object)}
    */
   public void UnsubscribeAllLvl2( int StreamID )
   {
      Unsubscribe( StreamID );
   }


   /////////////////////////////////////
   // Operation - Queries
   /////////////////////////////////////
   /**
    * \brief Queries UltraCache for list of all tickers for a specific 
    * channel.  Results returned in the {@link #OnBlobTable(String,BlobTable)} 
    * asynchronous callback.
    * <p>
    * @param obj User-supplied Object
    * @param chan Channel name to query; Empty for all
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetChannel( Object obj, String chan )
   {
      int id;

      id = _OpenStream( _DUMPDB, obj, false );
      _Send( id, fmtDumpDb( id, chan ) );
      return id;
   }

   /**
    * Retrieves the field list from UltraCache, which is returned in 
    * the {@link #OnBlobList(String,BlobList)} method.
    * <p>
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetFieldList( Object obj )
   {
      int id;

      id = _OpenStream( _FIELD_LIST, obj, false );
      _Send( id, fmtGetFieldList( id ) );
      return id;
   }

   /**
    * Retrieves all exchanges from UltraCache, which is returned in 
    * the {@link #OnBlobList(String,BlobList)} method.
    * <p>
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetExchanges( Object obj )
   {
      int id;

      id = _OpenStream( _EXCHANGES, obj, false );
      _Send( id, fmtGetExchanges( id ) );
      return id;
   }

   /**
    * Retrieves the list of tickers on a specific exchange from UltraCache, 
    * which is returned in the {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param exch Exchange
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetExchTickers( String exch, Object obj )
   {
      DataStream uc;
      int        id;

      id = _OpenStream( exch, obj, false );
      if ( (uc=_GetStream( id )) != null )
         uc.SetTierChan( _tierChan );
      _Send( id, fmtGetExch( exch, id ) );
      return id;
   }

   /**
    * Retrieves the list of NBBO options for a specific ticker from UltraCache, 
    * which is returned in the {@link #OnBlobList(String,BlobList)} method.
    * <p>
    * @param pUnd Underlying ticker (e.g., DELL)
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetOptionChain( String pUnd, Object obj )
   {
      int id;

      id = _OpenStream( pUnd, obj, false );
      _Send( id, fmtGetOptChain( pUnd, id ) );
      return id;
   }

   /**
    * Retrieves the list of NBBO options for a specific ticker from UltraCache,
    * - which is returned in the {@link #OnBlobList(String,BlobList)} method -
    * and streams response to user-defined IUpdate object.
    * <p>
    * @param pUnd Underlying ticker (e.g., DELL)
    * @param obj User-supplied Object
    * @param iUpd IUpdate object to receive Stream
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetOptionChain( String pUnd, Object obj, IUpdate iUpd )
   {
      int id;

      id = _OpenAndRegisterStream( pUnd, obj, iUpd, false );
      _Send( id, fmtGetOptChain( pUnd, id ) );
      return id;
   }

   /**
    * Retrieves the list of options for a specific ticker from a specific 
    * exchange from UltraCache, which is returned in the 
    * {@link #OnBlobList(String,BlobList)} method.
    * <p>
    * @param pUnd Underlying ticker (e.g., DELL)
    * @param exch Exchange - Either suffix, CTF number or name
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetOptionChainExch( String pUnd, String exch, Object obj )
   {
      int id;

      id = _OpenStream( pUnd, obj, false );
      _Send( id, fmtGetOptionChainExch( pUnd, exch, id ) );
      return id;
   }

   /**
    * Retrieves the list of all options underlyers, which is returned in 
    * the {@link #OnBlobList(String,BlobList)} method.
    * <p>
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetOptionsUnderlyers( Object obj )
   {
      int id;

      id = _OpenStream( _OPT_UNDLYER, obj, false );
      _Send( id, fmtGetOptionUnderlyer( id ) );
      return id;
   }

   /**
    * Retrieves the table of options and field values for a specific ticker 
    * from UltraCache, which is returned in the 
    * {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param pUnd Underlying ticker (e.g., DELL)
    * @param obj User-supplied Object
    * @param flds User-supplied list of fields; Empty for all.
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetOptionSnap( String pUnd, Object obj, String[] flds )
   {
      int id;

      id = _OpenStream( pUnd, obj, false );
      _Send( id, fmtGetOptionSnap( pUnd, id, flds ) );
      return id;
   }

   /**
    * Retrieves the list of index participants for a specifc index from the
    * UltraCache, which is returned in the
    * {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param pIdx Index namd (e.g. COMP, RUT ticker (e.g., DELL)
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetIndexParticipants( String pIdx, Object obj )
   {
      int id;

      id = _OpenStream( pIdx, obj, false );
      _Send( id, fmtGetIndexParticipants( pIdx, id ) );
      return id;
   }

   /**
    * Retrieves the list of index participants for a specifc index from the 
    * UltraCache - which is returned in the
    * {@link #OnBlobTable(String,BlobTable)} method - and streams response
    * to user-defined IUpdate object.
    * <p>
    * @param pIdx Index namd (e.g. COMP, RUT ticker (e.g., DELL)
    * @param obj User-supplied Object
    * @param iUpd IUpdate object to receive Stream
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetIndexParticipants( String  pIdx, 
                                    Object  obj, 
                                    IUpdate iUpd )
   {
      int id;

      id = _OpenAndRegisterStream( pIdx, obj, iUpd, false );
      _Send( id, fmtGetIndexParticipants( pIdx, id ) );
      return id;
   }

   /**
    * Retrieves the list of all indices from the UltraCache, which is returned
    * in the {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetIndices( Object obj )
   {
      int id;

      id = _OpenStream( _INDICES, obj, false );
      _Send( id, fmtGetIndices( id ) );
      return id;
   }

   /**
    * Retrieves the list of all funds from the UltraCache, which is returned 
    * in the {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetFunds( Object obj )
   {
      int id;

      id = _OpenStream( _FUNDS, obj, false );
      _Send( id, fmtGetFunds( id ) );
      return id;
   }

   /**
    * Retrieves the list of all bonds from the UltraCache, which is returned
    * in the {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetBonds( Object obj )
   {
      int id;

      id = _OpenStream( _BONDS, obj, false );
      _Send( id, fmtGetBonds( id ) );
      return id;
   }

   /**
    * Retrieves the list of all futures from the UltraCache, which is returned
    * in the {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetFutures( Object obj )
   {
      int id;

      id = _OpenStream( _FUTURES, obj, false );
      _Send( id, fmtGetFutures( id, false ) );
      return id;
   }

   /**
    * Retrieves the futures chain for a specific contract
    * such as CL from the UltraCache, which is returned in the
    * {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param pUnd Underlying future (e.g., CL)
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetFuturesChain( String pUnd, Object obj )
   {
      int id;

      id = _OpenStream( _FUT_CHAIN, obj, false );
      _Send( id, fmtGetFuturesChain( pUnd, id ) );
      return id;
   }

   /**
    * Retrieves the futures chain for a specific contract
    * such as CL from the UltraCache - which is returned in the
    * {@link #OnBlobTable(String,BlobTable)} method -
    * and streams response to user-defined IUpdate object.
    * <p>
    * @param pUnd Underlying future (e.g., CL)
    * @param obj User-supplied Object
    * @param iUpd IUpdate object to receive Stream
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetFuturesChain( String pUnd, Object obj, IUpdate iUpd )
   {
      int id;

      id = _OpenAndRegisterStream( _FUT_CHAIN, obj, iUpd, false );
      _Send( id, fmtGetFuturesChain( pUnd, id ) );
      return id;
   }

   /**
    * Retrieves the futures option chain for a specific contract 
    * such as /ESG15 from the UltraCache, which is returned in the 
    * {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param pUnd Underlying future (e.g., /ESG15)
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetFuturesOptionChain( String pUnd, Object obj )
   {
      int id;

      id = _OpenStream( _FUTOPT_CHAIN, obj, false );
      _Send( id, fmtGetFuturesOptionChain( pUnd, id ) );
      return id;
   }

   /**
    * Retrieves the future option chain for a specific contract
    * such as /ESG15 from the UltraCache - which is returned in the
    * {@link #OnBlobTable(String,BlobTable)} method -
    * and streams response to user-defined IUpdate object.
    * <p>
    * @param pUnd Underlying future (e.g., /ESG15)
    * @param obj User-supplied Object
    * @param iUpd IUpdate object to receive Stream
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetFuturesOptionChain( String pUnd, Object obj, IUpdate iUpd )
   {
      int id;

      id = _OpenAndRegisterStream( _FUTOPT_CHAIN, obj, iUpd, false );
      _Send( id, fmtGetFuturesOptionChain( pUnd, id ) );
      return id;
   }

   /**
    * Retrieves the futures chain snapshot for a specific contract
    * such as CL from the UltraCache, which is returned in the
    * {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param pUnd Underlying future (e.g., CL)
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetFuturesSnap( String pUnd, Object obj )
   {
      int id;

      id = _OpenStream( _FUT_SNAP, obj, false );
      _Send( id, fmtGetFuturesSnap( pUnd, id ) );
      return id;
   }

   /**
    * Retrieves the list of all futures - CL, ES, etc. - from the UltraCache
    * which is returned in the {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetAllFutures( Object obj )
   {
      int id;

      id = _OpenStream( _FUT_CHAIN, obj, false );
      _Send( id, fmtGetFuturesChain( _pAll, id ) );
      return id;
   }

   /**
    * Retrieves the list of all futures and options on futures from the 
    * UltraCache, which is returned in the
    * {@link #OnBlobTable(String,BlobTable)} method.
    * <p>
    * @param obj User-supplied Object
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetFuturesAll( Object obj )
   {
      int id;

      id = _OpenStream( _FUTURES_ALL, obj, false );
      _Send( id, fmtGetFutures( id, true ) );
      return id;
   }

   /**
    * Retrieves the list of market centers and market makers or market makers 
    * only (if bLvl2Only is true) for a specific ticker from UltraCache,
    * which is returned in the {@link #OnBlobList(String,BlobList)} method.
    * <p>
    * @param pTkr Ticker
    * @param obj User-supplied Object
    * @param bLvl2Only Set to True to see only Level 2 market makers
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetMktMkrs( String pTkr, Object obj, boolean bLvl2Only )
   {
      int id;

      id = _OpenStream( pTkr, obj, false );
      _Send( id, fmtGetMktMkrs( pTkr, id, !bLvl2Only ) );
      return id;
   }

   /**
    * Retrieves the table of fundamental data for a specific asset class - 
    * EQUITY, INDEX, FUND, or BOND - which is returnes in the 
    * {@link #OnBlobList(String,BlobList)} method.
    * <p>
    * @param obj User-supplied Object
    * @param assetClass Asset class
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetFundamentalData( Object     obj,  
                                  AssetClass assetClass )
   {
      char ch;

      // Which command?

      switch( assetClass ) {
         case EQUITY: ch = _mtEQUITY; break;
         case INDEX:  ch = _mtINDEX;  break;
         case FUND:   ch = _mtFUND;   break;
         case BOND:   ch = _mtBOND;   break;
         default:     ch = '?';       break;
      }
      return _GetFundy( obj, ch );
   }

   private int _GetFundy( Object obj, char ch )
   {
      int id;

      id = _OpenStream( _FUNDY, obj, false );
      _Send( id, fmtGetFundamental( id, ch ) );
      return id;
   }

  /**
    * Retrieves the list of tickers by market category - N, A, etc.
    * which is returnes in the {@link #OnBlobTable(String,BlobTable)} 
    * method.
    * <p>
    * @param obj User-supplied Object
    * @param cat Market Category - N, A, etc.
    * @return Unique data streamID returned as QuoddMsg.tag()
    */
   public int GetMktCategory( Object obj, String cat )
   {
      int id;

      id = _OpenStream( _MKT_CAT, obj, false );
      _Send( id, fmtGetMktCategory( id, cat ) );
      return id;
   }


   /////////////////////////////////////
   // Synchronized / Blocking Queries 
   /////////////////////////////////////
   /**
    * Synchronous (blocking) snapshot - single tickers
    *
    * @param tkr Ticker
    * @param obj User-supplied Object
    * @return QuoddMsg - Image or Status (check rtn.mt())
    */
   public QuoddMsg SyncSnap( String tkr, Object obj )
   {
      return _SyncWait( _fcnSyncSnap, tkr, obj, null );
   }

   /**
    * Synchronous (blocking) snapshot - multiple tickers
    *
    * @param tkrs List of tickers Ticker
    * @param obj User-supplied Object
    * @return QuoddMsg[] - List of Image or Status (check rtn.mt())
    */
   public QuoddMsg[] SyncMultiSnap( String[] tkrs, Object obj )
   {
      return WaitMultiSnap( tkrs, obj, 0 );
   }

   /**
    * Synchronous (blocking) snapshot - multiple tickers w/ timeout
    *
    * @param tkrs List of tickers Ticker
    * @param obj User-supplied Object
    * @param tmout Timeout in milliseconds
    * @return QuoddMsg[] - List of Image or Status (check rtn.mt())
    */
   public QuoddMsg[] WaitMultiSnap( String[] tkrs, Object obj, int tmout )
   {
      MultiSnap  snp;
      QuoddMsg[] rtn;

      _multiSnap = new MultiSnap( this, tkrs );
      rtn        = _multiSnap.Snap( tmout );
      _multiSnap = null;
      return rtn;
   }


   /**
    * Synchronous (blocking) snapshot of options table and field values.
    *
    * @param pUnd Underlying ticker (e.g., DELL)
    * @param obj User-supplied Object
    * @param flds User-supplied list of fields; Empty for all.
    */
   public BlobTable SyncGetOptionSnap( String   pUnd, 
                                       Object   obj,
                                       String[] flds )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncGetOptSnap, pUnd, obj, flds );
      return rtn;
   }

   /**
    * Synchronous (blocking) list of options for a ticker from UltraCache.
    *
    * @param pUnd Underlying ticker (e.g., DELL)
    * @param obj User-supplied Object
    */
   public BlobList SyncGetOptionChain( String pUnd, 
                                       Object obj )
   {
      BlobList rtn;

      rtn = (BlobList)_SyncWait( _fcnSyncGetOptChain, pUnd, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) list of all exchanges from UltraCache.
    * <p>
    * @param obj User-supplied Object
    * @return BlobTable
    */
   public BlobTable SyncGetExchanges( Object obj )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncGetExch, null, obj, null );
      return rtn; 
   }

   /**
    * Synchronous (blocking) list of all tickers from specific exchange.
    * <p>
    * @param exch Exchange
    * @param obj User-supplied Object
    * @return BlobTable
    */
   public BlobTable SyncGetExchTickers( String exch, Object obj )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncGetExchTkrs, exch, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) list of all mutual funds.
    * <p>
    * @param obj User-supplied Object
    * @return BlobTable
    */
   public BlobTable SyncGetFunds( Object obj )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncGetFunds, null, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) list of all bonds.
    * <p>
    * @param obj User-supplied Object
    * @return BlobTable
    */
   public BlobTable SyncGetBonds( Object obj )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncGetBonds, null, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) list of all futures.
    * <p>
    * @param obj User-supplied Object
    * @return BlobTable
    */
   public BlobTable SyncGetFutures( Object obj )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncGetFutures, null, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) list of futures for a contract from UltraCache.
    * <p>
    * @param pUnd Underlying future (e.g., CL)
    * @param obj User-supplied Object
    * @return BlobTable
    */
   public BlobTable SyncGetFuturesChain( String pUnd, Object obj )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncGetFuturesChain, pUnd, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) list of options for a give futures contract
    * from UltraCache.
    * <p>
    * @param pUnd Underlying future (e.g., /ESG15)
    * @param obj User-supplied Object
    * @return BlobList
    */
   public BlobTable SyncGetFuturesOptionChain( String pUnd, Object obj )
   {
      BlobTable rtn;
      char      fcn;

      fcn = _fcnSyncGetFuturesOptionChain;
      rtn = (BlobTable)_SyncWait( fcn, pUnd, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) futures snapshot for a contract from UltraCache.
    * <p>
    * @param pUnd Underlying future (e.g., CL)
    * @param obj User-supplied Object
    * @return BlobTable
    */
   public BlobTable SyncGetFuturesSnap( String pUnd, Object obj )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncGetFuturesSnap, pUnd, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) list of all futures - CL, etc. - from UltraCache.
    * <p>
    * @param obj User-supplied Object
    * @return BlobTable
    */
   public BlobTable SyncGetAllFutures( Object obj )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncGetFuturesChain, _pAll, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) list of all indices.
    * <p>
    * @param obj User-supplied Object
    * @return BlobTable
    */
   public BlobTable SyncGetIndices( Object obj )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncGetIndices, null, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) list of all indices.
    * <p>
    * @param obj User-supplied Object
    * @return BlobTable
    */
   public BlobTable SyncGetFundamentalData( Object     obj, 
                                            AssetClass assetClass )
   {
      BlobTable rtn;
      String    s;
      char      ch;

      // Which command?

      switch( assetClass ) {
         case EQUITY: ch = _mtEQUITY; break;
         case INDEX:  ch = _mtINDEX;  break;
         case FUND:   ch = _mtFUND;   break;
         case BOND:   ch = _mtBOND;   break;
         default:     ch = '?';       break;
      }

      char[] cc = { ch };

      s   = new String( cc );
      rtn = (BlobTable)_SyncWait( _fcnSyncGetFundy, s, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) list of tickers by market category.
    * <p>
    * @param obj User-supplied Object
    * @param cat Market Category - N, A, etc.
    * @return BlobTable
    */
   public BlobTable SyncGetMktCategory( Object obj, String cat )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncGetMktCat, cat, obj, null );
      return rtn;
   }

   /**
    * Synchronous (blocking) table of tickers by channel
    * <p>
    * @param obj User-supplied Object
    * @param chan Channel name; Empty for all
    * @return BlobTable
    */
   public BlobTable SyncGetChannel( Object obj, String chan )
   {
      BlobTable rtn;

      rtn = (BlobTable)_SyncWait( _fcnSyncDumpDb, chan, obj, null );
      return rtn;
   }


   //////////////////////
   // IUpdate Interface 
   //////////////////////
   protected void OnQueue()
   {
      if ( _HasStats() )
         _stats.PutLong( _STAT_IUPD_QSIZ, qSize() );
   }


   //////////////////////
   // Runnable Interface
   //////////////////////
   public void run()
   {
      DataInputStream in;
      double          dt;
      String          err, pl, rs;

      // Run-time stats

      pl = UCconfig.GetEnv( "UC_RUNTIMESTATS" );
      if ( ( pl != null ) && !_HasStats() ) {
         rs = "";
         rs = rs.format( "%s.%04d", pl, ThreadID() );
         SetRunTimeStats( rs, 0 );
      }
      synchronized( _readyMtx ) {
         _readyMtx.notify();
      }

      // Pump away ...

      while( _bRun ) {
         in = Connect();
         if ( in == null )
            continue; // while-loop
         try {
            Drain( in );
         } catch( Exception e ) { 
            err = e.getMessage();
            cerr.printf( "[%s] %s\n%s\n", Now(), err, CallStack() );
         } catch ( Error ee ) {
            err = ee.getMessage();
            cerr.printf( "[%s] %s\n%s\n", Now(), err, CallStack() );
         }     
         finally {
            try {
               if ( in != null )
                  in.close();
               if ( _sock != null )
                  _sock.close();
               _sock = null;
            } catch( IOException io ) {
               breakpoint();
            }
         }
      }

      // CPU Used

      dt = 1000.0 * _cpu.Get();
      if ( _CanLog( _UC_LOG_NUMUPD ) )
         cout.printf( "%d in %.1fmS\n", _tot, dt );
   }


   //////////////////////
   // Session Handler
   //////////////////////
   private void _OnSession( Status sts )
   {
      MessageDigest        md5;
      DataStream           uc;
      Governor             guv;
      char                 mt2, ch;
      boolean              tmp, trim;
      byte[]               bi, bo;
      String               inp, err, auth, qry, fmt;
      String[]             qdb;
      int                  i, nb, tHb, qSz, nc;

      // By Type

      mt2 = sts.mtSub();
      switch( mt2 ) {
         case _sessSubCHALLENGE:
            try {
               inp = _pwd + sts.Text();
               bi  = inp.getBytes( "UTF-8" );
               try {
                  md5  = MessageDigest.getInstance("MD5");
                  bo   = md5.digest( bi );
                  nb   = bo.length;
                  auth = "";
                  for ( i=0; i<nb; auth+=auth.format( "%02x", bo[i++] ) );
                  synchronized( _hdb ) {
                     tmp      = _bUP;
                     _bUP     = true;
                     guv      = _guvnor;
                     _guvnor  = null;
                     qry      = _pendReq;
                     _pendReq = "";  // Build 68 : Re-subscr in _sessSubSUCCESS
                     _Send( 0, fmtMount( _usr, auth ) );
                     _bUP     = tmp;
                     _guvnor  = guv;
                     _pendReq = qry;
                  }
               } catch( NoSuchAlgorithmException u ) {
                  err = u.getMessage();
               }
            } catch( UnsupportedEncodingException u ) {
               err = u.getMessage();
            }
            break;
         case _sessSubSUCCESS:
            tHb = sts.HeartbeatInterval();
            OnSession( sts.Text(), true );
            if ( _bUP ) {
               cout.printf( "[%s] _OnSession()-DUPLICATE\n", Now() );
               return;
            }
            if ( _CanLog( _UC_LOG_SESS ) ) {
               fmt = "[%s] _OnSession() FAST-START; tHbeat=%d\n";
               cout.printf( fmt, Now(), tHb );
            }
            if ( tHb > 0 ) {
               if ( _hbDawg != null ) {
                  _hbDawg.Stop();
                  _hbDawg = null;
               }
               _hbDawg = new Heartbeat( this, tHb );
            }
            if ( _HasStats() )
               _stats.PutBool( _STAT_IN_SESSHDB, true );
            qry = "";
            qSz = 0;
            synchronized( _hdb ) {
               _bUP = true;
               qSz  = _hdb.size();
               qdb  = new String[qSz];
/*
               c    = _hdb.values();
               it   = c.iterator();
               for ( i=0; it.hasNext(); i++ ) {
                  uc     = it.next();
 */
               i = 0;
               for ( Map.Entry<Integer, DataStream> e : _hdb.entrySet() ) {
                  uc       = e.getValue();
                  qdb[i++] = uc.Open();
               }

               // [ a, b, c, ] -> abc

               if ( _guvnor != null )
                  for ( i=0; i<qdb.length; _guvnor.Add( qdb[i++] ) );
               else {
                  qry = Arrays.toString( qdb );
                  qry = qry.replace( ", ,", "," );
                  qry = qry.replace( ", <", " <" );
                  nc  = qry.length()-1;
                  for ( trim=true, nc=qry.length()-1; trim; ) {
                     ch = qry.charAt( nc );
                     switch( ch ) {
                        case ' ':
                        case ',':
                        case ']':
                           nc -= 1;
                           break;
                        default:
                           trim = false;
                           break;
                     }
                  }
                  if ( nc > 1 ) {
                     qry = qry.substring( 1,nc );  // [ ... ]
                     _pendReq = "";  // Build 83
                     _Send( 0, qry );
                  }
               }
            }
            if ( _HasStats() )
               _stats.PutBool( _STAT_IN_SESSHDB, false );
            if ( _CanLog( _UC_LOG_SESS ) ) {
               fmt = "[%s] _OnSession() FAST-END; %d tkrs; %d bytes\n";
               cout.printf( fmt, Now(), qSz, qry.length() );
            }
            break;
         case _sessSubFAILURE:
            OnSession( sts.Text(), false );
            if ( _CanLog( _UC_LOG_SESS ) ) {
               fmt = "[%s] _OnSession FAILURE : %s\n";
               cout.printf( fmt, Now(), sts.Text() );
            }
            _bUP = false;
            break;
      }
   }


   //////////////////////
   // Synchronized dispatchers
   //////////////////////
   private synchronized QuoddMsg _SyncWait( char     fcn,
                                            String   pTkr,
                                            Object   obj,
                                            String[] flds )
   {
      QuoddMsg rtn;
      int      StreamID;
      String   fmt, err;
      long     t0, t1, dd;

      // Logging

      t0 = t1 = 0;
      if ( _CanLog( _UC_LOG_SYNC ) ) {
         t0 = System.currentTimeMillis();
         cout.printf( "[%s] ENTER _SyncWait( %c )\n", Now(), fcn );
      }
      if ( _HasStats() )
         _stats.PutBool( _STAT_IN_SYNC, true );

      // Do it

      _syncFcn = fcn;
      switch( _syncFcn ) {
         case _fcnSyncSnap:
            _syncIdx = Snap( pTkr, obj );
            break;
         case _fcnSyncGetOptSnap:
            _syncIdx = GetOptionSnap( pTkr, obj, flds );
            break;
         case _fcnSyncGetOptChain:
            _syncIdx = GetOptionChain( pTkr, obj );
            break;
         case _fcnSyncGetExch:
            _syncIdx = GetExchanges( obj );
            break;
         case _fcnSyncGetExchTkrs:
            _syncIdx = GetExchTickers( pTkr, obj );
            break;
         case _fcnSyncGetFunds:
            _syncIdx = GetFunds( obj );
            break;
         case _fcnSyncGetFuturesChain:
            _syncIdx = GetFuturesChain( pTkr, obj );
            break;
         case _fcnSyncGetFuturesOptionChain:
            _syncIdx = GetFuturesOptionChain( pTkr, obj );
            break;
         case _fcnSyncGetFuturesSnap:
            _syncIdx = GetFuturesSnap( pTkr, obj );
            break;
         case _fcnSyncGetFutures:
            _syncIdx = GetFutures( obj );
            break;
         case _fcnSyncGetBonds:
            _syncIdx = GetBonds( obj );
            break;
         case _fcnSyncGetIndices:
            _syncIdx = GetIndices( obj );
            break;
         case _fcnSyncGetFundy:
            _syncIdx = _GetFundy( obj, pTkr.charAt( 0 ) );
            break;
         case _fcnSyncGetMktCat:
            _syncIdx = GetMktCategory( obj, pTkr );
            break;
         case _fcnSyncDumpDb:
            _syncIdx = GetChannel( obj, pTkr );
            break;
         default:
            if ( _HasStats() )
               _stats.PutBool( _STAT_IN_SYNC, false );
            return null;
      }
      rtn = null;
      _Wait();
      rtn = _syncMsg;
      _CloseStream( _syncIdx );
      _syncFcn = 0;
      _syncIdx = 0;
      _syncMsg = null;

      // Logging / Return

      if ( _CanLog( _UC_LOG_SYNC ) ) {
         t1  = System.currentTimeMillis();
         dd  = ( t1 - t0 );
         fmt = "[%s] LEAVE _SyncWait( %c ) : %dmS\n";
         cout.printf( fmt, Now(), fcn, dd );
      }
      if ( _HasStats() ) 
         _stats.PutBool( _STAT_IN_SYNC, false );
      if ( rtn == null ) {
         fmt = "[%s] ERROR : _SyncWait( %c ) rtn == null\n";
         cout.printf( fmt, Now(), fcn );
      }
      return rtn;
   }

   private void _OnImage( IUpdate iUpd,
                          String  tkr,
                          Image   img,
                          int     nLeft )
   {
      int      iIdx, sIdx, nk;
      long     bidSz, askSz;
      boolean  bMktMkr;
      String[] kv;
      String   pt, fmt, pf;

      // Logging?

      if ( _CanLog( _UC_LOG_IMAGE ) ) {
         pf  = _IsFloater( img ) ? "FLOATER" : "";
         fmt = "[%s] IMAGE ( %s ) : RTL=%d; nChan=%d%s\n";
         cout.printf( fmt, Now(), img.tkr(), img.RTL(), img._nChan, pf );
      }

      // 13-06-10 jcs  4-char MMID = / 100

      if ( _bLvl2 ) {
         pt      = img.tkr();
         kv      = pt.split("/");
         nk      = kv.length;
         bMktMkr = ( ( nk > 1 ) && ( kv[nk-1].length() == 4 ) );
         bidSz   = img._bidSize;
         askSz   = img._askSize;
         if ( bMktMkr ) {
            img._bidSize = Math.max( bidSz / 100, 1 );
            img._askSize = Math.max( askSz / 100, 1 );
            if ( _CanLog( _UC_LOG_LVL2 ) ) {
               cout.printf( "[%s] LVL2 {%s} : ( %dx%d ) -> ( %dx%d )\n",
                  Now(), pt, 
                  bidSz, askSz, 
                  img._bidSize, img._askSize );
            }
         }

         // 13-06-17 jcs  Unpriced Bid / Ask

         if ( !img.IsNazzLevel2() ) {
            img._bid = img.QteCanDisplayBid() ? img._bid : 0.0;
            img._ask = img.QteCanDisplayAsk() ? img._ask : 0.0;
         }
      }

      // 15-01-12 jcs  MSFT/WEED {GSCO} ??

      if ( _factory.IsFixLvl2Name() )
         _factory.FixLevel2Name( img, img._bidMktCtr );

      // Synchronized Image?

      sIdx = _syncIdx;
      iIdx = img.tag();
      if ( ( sIdx != 0 ) && ( sIdx == iIdx ) ) {
         _syncMsg = new Image( _buf.buf(), _buf.bp(), nLeft );
         _Notify();
      }
      else
         iUpd.OnImage( tkr, img );
   }

   private void _OnStatus( IUpdate iUpd,
                           String  tkr, 
                           Status  sts,
                           int     nLeft )
   {
      int iIdx, sIdx;

      // Logging?

      if ( _CanLog( _UC_LOG_IMAGE ) )
         cout.printf( "[%s] DEAD ( %s ) : %s\n", Now(), sts.tkr(), sts.Text() );

      // Synchronized DEAD?

      sIdx = _syncIdx;
      iIdx = sts.tag();
      if ( ( sIdx != 0 ) && ( sIdx == iIdx ) ) {
         _syncMsg = new Status( _buf.buf(), _buf.bp(), nLeft );
         _Notify();
      }
      else {
         iUpd.OnMsg( tkr, sts, false );
/*
 * 14-01-30 jcs  Build 74 : TSQDB
 *
         iUpd.OnStatus( tkr, sts );
 */
      }
   }

   private void _OnBlobList( IUpdate  iUpd,
                             String   qry,
                             BlobList b,
                             int      off,
                             int      nLeft )
   {
      int iIdx, sIdx;
      int i, nr;

      // Dumpage??

      if ( _CanLog( _UC_LOG_BLOB ) ) {
         nr = (int)b.nLst();
         cout.printf( "[%s] BEG-BLOB-LIST : %s; %d rows\n", Now(), qry, nr );
         for ( i=0; i<nr; cout.printf( "%s\n", b.lst()[i++] ) );
         cout.printf( "[%s] END-BLOB-LIST : %s\n", Now(), qry );
      }

      // Synchronized Image?

      sIdx = _syncIdx;
      iIdx = b.tag();
      if ( ( sIdx != 0 ) && ( sIdx == iIdx ) ) {
         _syncMsg = new BlobList( b );
         _Notify();
      }
      else
         iUpd.OnBlobList( qry, b );
   }

   private void _OnBlobTable( IUpdate    iUpd, 
                              String     qry, 
                              BlobTable  b,
                              DataStream uc )
   {
      int     iIdx, sIdx;
      int     i, j, nr, nc, tc;
      boolean bSync;

      // Dumpage??

      if ( _CanLog( _UC_LOG_BLOB ) ) {
         nc = b.nCol();
         nr = b.nRow();
         cout.printf( "[%s] BEG-BLOB-TABLE : %s; %d rows\n", Now(), qry, nr );
         for ( i=0; i<nc; cout.printf( "%s,", b.GetColName( i++ ) ) );
         cout.printf( "\n" );
         for ( i=0; i<nr; i++ ) {
            for ( j=0; j<nc; cout.printf( "%s,", b.GetCell( i,j++ ) ) );
            cout.printf( "\n" );
         }
         cout.printf( "[%s] END-BLOB-TABLE : %s\n", Now(), qry );
      }

      // Synchronized Image?

      sIdx  = _syncIdx;
      iIdx  = b.tag();
      tc    = ( uc != null ) ? uc.TierChan() : 0;
      bSync = ( sIdx != 0 ) && ( sIdx == iIdx );
      if ( bSync && ( tc == 0 ) ) {
         _syncMsg = new BlobTable( b );
         _Notify();
         return;
      }

      // Un-synchronized; TIER Response??

      int                   ns, nh, nm, nk, row, nq;
      String[]              kv, tk;
      QuoddMsg[]            qm, ms;
      String                tkr, ckr, hh, fmt;
      HashMap<String, Long> hdb, req;
      Iterator              it;
      Map.Entry             et;
      UltraChan             chan;
      Image                 img;
      boolean               bOK;
      ArrayList<Long>       ddb;
      Comparator<Long>      cmp;

      if ( tc != 0 ) {
         cmp = Collections.reverseOrder();
         ddb = new ArrayList<Long>();
         hdb = new HashMap<String, Long>();
         req = new HashMap<String, Long>();
         nc = b.nCol();
         nr = b.nRow();
         for ( i=0; i<nr; i++ ) {
            tkr = b.GetCell( i,0 );
            kv  = tkr.split("/");
            try {
               row = hdb.get( kv[0] ).intValue();
               ckr = b.GetCell( row,0 );
               if ( !ckr.equals( tkr ) ) {
                  req.put( tkr, (long)i );
                  if ( !req.containsKey( ckr ) )
                     req.put( ckr, (long)row );
               }
            } catch( Exception e ) {
               hdb.put( kv[0], (long)i );
            }
         }
         if ( (ns=req.size()) > 0 ) {
            kv = new String[ns];
            i  = 0;
            it = req.keySet().iterator();
            for ( i=0; it.hasNext(); i++ ) {
               tkr   = (String)it.next();
               row   = req.get( tkr ).intValue();
               kv[i] = tkr;
            }
            if ( _CanLog( _UC_LOG_TIERDROP ) ) 
               cout.printf( "[%s] TIER-DUP: %d reqs\n", Now(), ns );
            hh = "";
            nh = _uHosts.length;
            for ( i=0; i<nh; hh += hh.format( "%s,", _uHosts[i++] ) );
            qm  = null;
            bOK = false;
            for ( i=0; !bOK && i<3; i++ ) {
               chan = new UltraChan( hh, _uPort, _usr, _pwd, false );
               chan.Start();
               ms  = chan.WaitMultiSnap( kv, null, 3000 );
               nm  = ms.length;
               nk  = kv.length;
               ns  = nk - nm;
// cout.printf( "[%s] WaitMultiSnap() : %d of %d tkrs done\n", Now(), nm, nk );
               bOK = ( ns == 0 );
               qm  = QuoddMsg.concat( qm, ms );
               if ( !bOK ) {
                  tk = new String[ns];
                  for ( j=0; j<ns; tk[j]=kv[nm+j], j++ );
                  kv = tk;
               }
               chan.Stop();
            }
            for ( i=0; i<qm.length; i++ ) {
               if ( qm[i].mtSub() == _mtSubIMG ) {
                  img = (Image)qm[i];
                  if ( img._nChan != tc ) {
                     ckr = img.tkr().split("\"")[0];
                     row = req.get( ckr ).intValue();
                     ddb.add( (long)row );
                     if ( _CanLog( _UC_LOG_TIERDROP ) ) { 
                        fmt = "[%s] DROP Chan=%d; Row=%d; Tkr=%s\n";
                        cout.printf( fmt, Now(), img._nChan, row, ckr );
                     }
                  }
               }
            }
            Collections.sort( ddb, cmp );
            for ( i=0; i<ddb.size(); i++ )
               b.DelRow( ddb.get( i ).intValue() );
            ns = ddb.size();
            nq = qm.length;
            if ( _CanLog( _UC_LOG_TIERDROP ) ) {
               fmt = "[%s] TIER-DUP : %d of %d rows dropped\n";
               cout.printf( fmt, Now(), ns, nq );
            }
         }
      }
      if ( bSync ) {
         _syncMsg = new BlobTable( b );
         _Notify();
      }
      else
         iUpd.OnBlobTable( qry, b );
   }


   //////////////////////
   // Synchronous
   //////////////////////
   protected void _Wait()
   {
      String err;

      try {
         synchronized( _syncMtx ) {
            _syncMtx.wait();
         }
      } catch( InterruptedException e ) {
         err = e.getMessage();
      }
   }

   protected void _Notify()
   {
      if ( _HasStats() )
         _stats.PutBool( _STAT_IN_NOTIFY, true );
      synchronized( _syncMtx ) {
         _syncMtx.notify();
      }
      if ( _HasStats() )
         _stats.PutBool( _STAT_IN_NOTIFY, false );
   }

   private int _IsSubscribed( String tkr )
   {
      DataStream uc;

      synchronized( _sdb ) {
         if ( _sdb.containsKey( tkr ) ) {
            uc = _sdb.get( tkr );
            return uc.StreamID();
         }
      }
      return 0;
   }

   private DataStream _GetStream( int idx )
   {
      DataStream uc;

      uc = null;
      if ( _bSyncGet ) {
         synchronized( _hdb ) {
            uc = _Query_hdb( idx );
         }
      }
      else
         uc = _Query_hdb( idx );
      return uc;
   }

   private DataStream _Query_hdb( int idx )
   {
      DataStream uc;

      uc = null;
      if ( _hdb.containsKey( idx ) )
         uc = _hdb.get( idx );
      return uc;
   }

   private boolean _HasStats()
   {
      return( _stats != null );
   }

   private long ThreadID()
   {
      return Thread.currentThread().getId();
   }

   public String Now()
   {
      long   t0, tid;
      String p0, s;

      tid = ThreadID();
      t0 = System.currentTimeMillis();
      p0 = QuoddMsg.pDateTimeMs( t0 );
      s  = "";
      s += s.format( "%s,%04x,QuoddFeed.jar", p0, tid );
      return s;
   }

   private boolean _IsFloater( Image img )
   {
      long ULT_NO_CHAN = 0x80000000;

      return( ( img._nChan & ULT_NO_CHAN ) == ULT_NO_CHAN );
   }

   private boolean _CanLog( int type )
   {
      return( ( _logLvl & type ) == type );
   }

   private String _SubName( String tkr )
   {
      String[] kv;
      int      nk;

      // Pre-condition

      if ( !_bBasicBug )
         return tkr;

      /*
       * CSCO -> CSCO,CSCO/T
       * Anything w/ space : Straight thru ...
       */
      kv = tkr.split( " " );
      nk = kv.length;
      if ( nk != 1 )
         return tkr;
      return tkr + "/T";
   }

   private int _Subscribe( String  tkr, 
                           Object  obj,
                           IUpdate iUpd,
                           boolean bSetTkr )
   {
      String  sub;
      boolean bBug;
      int     StreamID;

      // Pre-conditions

      if ( !_bAllowDup && (StreamID=_IsSubscribed( tkr )) != 0 ) {
         if ( _CanLog( _UC_LOG_SUBDUP ) )
            cout.printf( "[%s] SUBSCRIBE-DUPLICATE( %s )\n", Now(), tkr );
         return StreamID;
      }

      // Safe to Subscribe

      if ( iUpd != null ) 
         StreamID = _OpenAndRegisterStream( tkr, obj, iUpd, bSetTkr );
      else
         StreamID = _OpenStream( tkr, obj, bSetTkr );
      sub  = _SubName( tkr );
      bBug = !tkr.equals( sub );
      _Send( StreamID, fmtSubscribe( tkr, StreamID ) );
      if ( _CanLog( _UC_LOG_SUBSCR ) )
         cout.printf( "[%s] SUBSCRIBE( %s )\n", Now(), tkr );
      if ( bBug ) {
         _AddSubscription( StreamID, sub );
         _Send( StreamID, fmtSubscribe( sub, StreamID ) );
         if ( _CanLog( _UC_LOG_SUBSCR ) )
            cout.printf( "[%s] SUBSCRIBE( %s )\n", Now(), sub );
      }
      if ( _HasStats() )
         _stats.IncLong( _STAT_NUM_SUBSCR );
      return StreamID;
   }

   private int _OpenStream( String key, Object obj, boolean bSetTkr  )
   {
      DataStream uc;
      int        id;

      id = 0;
      synchronized( _hdb ) {
         uc = new DataStream( this, key, _uReqID++, obj, bSetTkr );
         id = uc.StreamID();
         if ( !_hdb.containsKey( id ) )
            _hdb.put( id, uc );
      }
      synchronized( _sdb ) {
         if ( !_sdb.containsKey( key ) )
            _sdb.put( key, uc );
      }
      return id;
   }

   private int _OpenAndRegisterStream( String  key, 
                                       Object  obj, 
                                       IUpdate iUpd,
                                       boolean bSetTkr )
   {
      int StreamID;

      StreamID = _OpenStream( key, obj, bSetTkr );
      _Register( StreamID, iUpd );
      return StreamID;
   }

   /**
    * Register an IUpdate object to receive updates for a data stream ID
    *
    * @param id Unique data stream ID returned from SubscribeXxx() methods
    * such as {@link #Subscribe(String,Object)} or GetXxx() query methods
    * such as {@link #GetMktMkrs(String,Object,boolean)}
    * @param iUpd IUpdate
    */
   private void _Register( int id, IUpdate iUpd )
   {
      DataStream uc;

      synchronized( _hdb ) {
         if ( _hdb.containsKey( id ) ) {
            uc = _hdb.get( id );
            uc.Register( iUpd );
         }
      }
   }

   private boolean _AddSubscription( int StreamID, String tkr )
   {
      DataStream uc;
      boolean    rtn;

      rtn = false;
      synchronized( _hdb ) {
         if ( _hdb.containsKey( StreamID ) ) {
            uc = _hdb.get( StreamID );
            uc.AddSubscription( tkr );
            rtn = true;
         }
      }
      return rtn;
   }

   private String[] _CloseStream( int id )
   {
      DataStream uc;
      String     tkr;
      String[]   rtn;

      rtn = null;
      synchronized( _hdb ) {
         if ( _hdb.containsKey( id ) ) {
            uc  = _hdb.get( id );
            rtn = uc.subs();
            _hdb.remove( id );
         }
      }
      synchronized( _sdb ) {
         if ( ( rtn != null ) && _sdb.containsKey( (tkr=rtn[0]) ) )
            _sdb.remove( tkr );
      }
      return rtn;
   }

   private void _Send( int StreamID, String qry )
   {
      DataStream uc;

      /*
       * 1) Store query string in DataStream if !StreamID
       * 2) Write to UC : From Aman, this must be sync'ed w/ _hdb
       */
      if ( _HasStats() )
         _stats.PutBool( _STAT_IN_SEND, true );
      synchronized( _hdb ) {
         if ( _HasStats() )
            _stats.PutBool( _STAT_IN_SND1, true );
         if ( ( StreamID != 0 ) && _hdb.containsKey( StreamID ) ) {
            uc  = _hdb.get( StreamID );
            uc.SetQuery( qry );
         }
         if ( _HasStats() )
            _stats.PutBool( _STAT_IN_SND1, false );
         if ( _guvnor != null )
            _guvnor.Add( qry );
         else
            _WriteBytes( qry );
      }
      if ( _HasStats() )
         _stats.PutBool( _STAT_IN_SEND, false );
   }  

   public void SyncWriteBytes( String qry )
   {
      synchronized( _hdb ) {
         _WriteBytes( qry );
      }
   }

   private void _WriteBytes( String qry )
   {
      DataOutputStream out;
      MultiSnap        mSnap;
      String           fmt, err;
      long             qLen, nWr;

      // Shove it out ...

      qLen  = nWr = 0;
      err   = null;
      mSnap = null;
      synchronized( _hdb ) {
         try {
            if ( _HasStats() )
               _stats.PutBool( _STAT_GET_OUTSTR, true );
            out = new DataOutputStream(  _sock.getOutputStream() );
            if ( _HasStats() )
               _stats.PutBool( _STAT_GET_OUTSTR, false );
            _pendReq += qry;
            qLen      = _pendReq.length();
            if ( _bUP && ( qLen > 0 ) ) {
               if ( _HasStats() ) {
                  _stats.PutBool( _STAT_IN_WRITE, true );
                  _stats.PutLong( _STAT_WR_SIZ, qLen );
               }
               out.writeBytes( _pendReq );
               if ( _CanLog( _UC_LOG_WRBYTE ) ) 
                  cout.printf( "[%s] WriteBytes( %s )\n", Now(), _pendReq );
               if ( _HasStats() ) {
                  _stats.PutBool( _STAT_IN_WRITE, false );
                  _stats.PutLong( _STAT_WR_SIZ, 0 );
               }
               _pendReq = "";
               nWr      = qLen;
               qLen     = 0;
            }
         } catch( Exception e ) {
            fmt = "[%s] WriteBytes(%d) ERR : %s\n";
            if ( qLen > 0 ) {
               cerr.printf( fmt, Now(), qLen, (err=e.getMessage()) );
               mSnap = _multiSnap;
               if ( ( mSnap != null ) && mSnap._bLog ) {
                  if ( err.equals( "Broken pipe" ) ) {
                     fmt = "_multiSnap : %d of %d tkrs\n";
                     cerr.printf( fmt, mSnap._nRes, mSnap._nTkr );
                     cerr.printf( CallStack() );
                  }
               }
            }
         } finally {
            if ( _HasStats() ) {
               _stats.PutBool( _STAT_GET_OUTSTR, false );
               _stats.PutBool( _STAT_IN_WRITE, false );
               _stats.PutLong( _STAT_WR_SIZ, 0 );
               _stats.PutLong( _STAT_TX_QSIZE, qLen );
               _stats.IncLong( _STAT_TX_MSG );
               _stats.AddLong( _STAT_TX_BYTE, nWr );
               _stats.PutTimeNow( _STAT_TX_TIME );
            }
         }
      }

      // Disconnect??

      if ( err != null )
         Disconnect( err );
   }

   private void _GrowSendBuf( int qLen )
   {
      int sz0, sz1;

      // Pre-condition

      if ( _sock == null )
         return;

      // OK to continue

      sz0 = _GetTXBufSz();
      if ( sz0 < qLen ) {
         sz1 = _SetTXBufSz( qLen );
         cout.printf( "SO_SNDBUF %d -> %d; Want %d\n", sz0, sz1, qLen );
      }
   }

   private int _SetTXBufSz( int sz )
   {
      String err;

      try {
         _sock.setSendBufferSize( sz );
      } catch( SocketException e ) {
         err = e.getMessage();
      }
      return _GetTXBufSz();
   }

   private int _GetTXBufSz()
   {
      int sz;

      try {
         sz = _sock.getSendBufferSize();
      } catch( SocketException e ) {
         sz = 0;
      } catch( NullPointerException e ) {
         sz = 0;
      }
      return sz;
   }


   //////////////////////
   // Channel Operations
   //////////////////////
   private DataInputStream Connect()
   {
      DataInputStream in;
      String          qry, del, uHost, lAddr;
      int             i, j, nh, tPoll, sz0, sz1;

      Disconnect( "Connect()" );
      for ( i=0,in=null; _bRun && _sock==null; i++ ) {
         nh = _uHosts.length;
         for ( j=0; _sock == null && j<nh; j++ ) {
            uHost = _uHosts[j];
            try {
               _sock = new Socket( uHost, _uPort );
               if ( UCconfig.TcpNoDelay() ) {
                  _sock.setTcpNoDelay( true );
                  del = _sock.getTcpNoDelay() ? "YES" : "NO";
                  cout.printf( "TCP_NODELAY = %s\n", del );
               }
               if ( (tPoll=UCconfig.ReadPoll()) != 0 ) {
                  _sock.setSoTimeout( tPoll );
                  cout.printf( "TCP_TIMEOUT = %d\n", _sock.getSoTimeout() );
               }
               sz0 = _GetTXBufSz();
               sz1 = _SetTXBufSz( 10*_K*_K );
               if ( _CanLog( _UC_LOG_BUFSIZ ) )
                  cout.printf( "[%s] TX BUFSIZ : %d -> %d\n", Now(), sz0, sz1 );
               in    = new DataInputStream( _sock.getInputStream() );
               _conn = uHost;
               if ( _HasStats() ) {
                  lAddr = _sock.getLocalSocketAddress().toString();
                  _stats.PutBool( _STAT_CONN_STATE, true );
                  _stats.IncLong( _STAT_CONN_COUNT );
                  _stats.PutTimeNow( _STAT_CONN_TIME );
                  _stats.PutString( _STAT_CONN_ADDR, lAddr );
               }
               OnConnect();
            } catch( UnknownHostException u ) {
               _sock = null;
               in    = null;
            } catch( IOException io ) {
               _sock = null;
               in    = null;
            }
         }
         if ( _sock == null )
            Sleep( 2.0 );
      }
      if ( in != null && _bUP )
         _Send( 0, " " );
      return in;
   }

   private void Disconnect( String err )
   {
      MultiSnap mSnap;

      if ( _hbDawg != null )
         _hbDawg.Stop();
      _hbDawg = null;
      try {
         if ( _sock != null ) {
            _sock.close();
            OnDisconnect();
         }
      } catch( IOException io ) {
         breakpoint();
      }
      synchronized( _hdb ) {
         _sock    = null;
         _conn    = "Not connected";
         _bUP     = false;
         _pendReq = "";  // Build 83
         mSnap    = _multiSnap;
      }
      if ( mSnap != null )
         mSnap.OnDisconnect( err );
      if ( _syncIdx != 0 ) {
         switch( _syncFcn ) {
            case _fcnSyncSnap:
               _syncMsg = new Status();
               break;
            case _fcnSyncGetOptChain:
               _syncMsg = new BlobList();
               break;
            case _fcnSyncGetOptSnap:
            case _fcnSyncGetExch:
            case _fcnSyncGetExchTkrs:
            case _fcnSyncGetFunds:
            case _fcnSyncGetFuturesChain:
            case _fcnSyncGetFuturesOptionChain:
            case _fcnSyncGetFuturesSnap:
            case _fcnSyncGetFutures:
            case _fcnSyncGetBonds:
            case _fcnSyncGetIndices:
            case _fcnSyncGetFundy:
            case _fcnSyncGetMktCat:
            case _fcnSyncDumpDb:
               _syncMsg = new BlobTable();
               break;
         }
         _Notify();
      }

      // Stats / Log

      if ( _HasStats() ) {
         _stats.PutBool( _STAT_CONN_STATE, false );
         _stats.PutString( _STAT_CONN_ADDR, "Disconnected" );
      }
      if ( _CanLog( _UC_LOG_DISCO ) ) {
         try {
            _LogDisco();
         } catch( Exception e ) {
            cerr.printf( "[%s] === DISCO [%s] ===\n", Now(), err );
            cerr.printf( "%s\n===\n", CallStack() );
         }
      }
   }

   private void Drain( DataInputStream in )
   {
      Buffer     inb = IsCompressed() ? _bufZ : _buf;
      byte[]     buf = _buf.buf();
      ByteBuffer bb;
      int        n, mSz, nR, bp, b0, nL, idx, sz, zn, zr;
      long       rtl;
      DataStream uc;
      IUpdate    iUpd;
      double     d0, dt, mult;
      QuoddMsg   qm;
      Blob       bl;
      String     tkr, fmt, err;
      char       mt, mt2;
      boolean    bClose, bPro;
      long       _mod = 100000;

      d0  = _cpu.Get();
      _buf.init();
      if ( IsCompressed() )
         _bufZ.init();
      for ( ; _bRun; ) {
         nR = 0;
         try {
            _EnterRead( inb.nRead() );
            nR = in.read( inb.buf(), inb.off(), inb.nRead() );
            _LeaveRead( nR );
            if ( nR <= 0 ) {
               if ( inb.nRead() != 0 ) {
                  Disconnect( "Zero READ" );
                  return;
               }
               else if ( _bDiscoRd0 ) {
                  fmt = "[%s] READ ERROR : len=%d; nR=%d\n";
                  cout.printf( fmt, Now(), nR, inb.nRead() );
                  cout.print( hexMsg( inb.buf(), 0, inb.off() ) );
                  Disconnect( "READ ERROR" );
                  return;
               }
            }     
            inb.readlen( nR );
            if ( _bByteLog || _CanLog( _UC_LOG_BYTELOG ) ) {
               fmt = "[%s] READ %05d bytes @ off=%d\n";
               cout.printf( fmt, Now(), nR, inb.off() );
               cout.print( hexMsg( inb.buf(), inb.off(), nR ) );
            }
         } catch( SocketTimeoutException  e ) {
            _LeaveRead( 0 );
            cerr.printf( "[%s] INFO : SocketTimeoutException\n", Now() );
            continue; // for-loop
         } catch( NullPointerException e ) {
            _LeaveRead( 0 );
            fmt = "[%s] READ ERROR : NullPointerException\n";
            cerr.printf( fmt, Now() );
            Disconnect( "READ ERROR : NullPointerException" );
            return;
         } catch( IndexOutOfBoundsException e ) {
            _LeaveRead( 0 );
            fmt = "[%s] READ ERROR : IndexOutOfBoundsException\n";
            cerr.printf( fmt, Now(), fmt );
            continue; // for-loop
         } catch( IOException e ) {
            _LeaveRead( 0 );
            Disconnect( "READ ERROR : IOException" );
            return;
         } catch( Exception e ) {
            err = e.getMessage();
            cerr.printf( "[%s] %s\n%s\n", Now(), err, CallStack() );
            _LeaveRead( 0 );
            Disconnect( "READ ERROR : Exception" );
            return;
         } catch ( Error ee ) {
            err = ee.getMessage();
            cerr.printf( "[%s] %s\n%s\n", Now(), err, CallStack() );
            _LeaveRead( 0 );
            Disconnect( "READ ERROR : Error" );
            return;
         }

         // Compressed??

         zn = 0;
         zr = 0;
         if ( ( IsCompressed() ) ) {
            _zs.reset();
            _zs.setInput( inb.buf(), inb.off(), nR );
            try {
               zn = _zs.inflate( _buf.buf(), _buf.off(), _buf.bSz() );
               zr = _zs.getRemaining();
               _buf.readlen( zn );
               if ( _bByteLog || _CanLog( _UC_LOG_BYTELOG ) ) {
                  fmt = "[%s] READ-Z %05d bytes; rem=%d\n";
                  cout.printf( fmt, Now(), zn, zr );
                  cout.print( hexMsg( _buf.buf(), _buf.off(), zn ) );
               }
            } catch( DataFormatException e ) {
               err = e.getMessage();
               fmt = "[%s] DataFormatException-Z : %s\n";
               cerr.printf( fmt, Now(), err );
            }
         }

         // Carry on ...

         b0  = _buf.bp();
         mSz = inb.off() + nR;
// cout.printf( "[%s] START _buf.chop() : nL=%d\n", Now(), _buf.nLeft() );
         for ( n=0; (nL=_buf.nLeft())>=QuoddMsg.MINSZ; n++ ) {
            bp = _buf.bp();
            if ( _HasStats() )
               _stats.PutBool( _STAT_IN_PARSE, true );
            qm = _factory.Parse( buf, bp, nL );
            if ( _HasStats() )
               _stats.PutBool( _STAT_IN_PARSE, false );
            if ( qm == null )
               break; // for-loop
            if ( qm.len() > nL )
               break; // for-loop
            sz   = qm.len();
            mt   = qm.mt();
            mt2  = qm.mtSub();
            idx  = qm.tag();
            tkr  = "Undefined";
            iUpd = this;
            bPro = true;
            uc   = null;
            mult = 1.0;
            try {
               if ( (uc=_GetStream( idx )) != null ) {
                  tkr   = uc.tkr();
                  iUpd  = uc.iUpd();
                  bPro  = uc.IsAllowableProtocol( qm );
                  bPro &= _bBasicBug ? uc.IsAllowableMsg( qm ) : true;
                  bPro &= !uc.IsKO( qm );
                  qm.SetUserObj( uc.obj() );
                  if ( uc.IsSetTkr() )
                     qm.SetTkr( uc.tkr() );
                  qm        = uc.Transform( qm );
                  mult      = uc._dMult;
                  uc._nUpd += 1;
               }
            } catch( Exception ee ) {
               tkr  = "Undefined";
               iUpd = this;
               bPro = true;
            }
            if ( _bOnRaw )
               OnRaw( in, qm, ByteBuffer.wrap( buf, bp, qm.len() ) );
            if ( _bByteLog || _CanLog( _UC_LOG_BYTELOG ) )
               cout.printf( "%c [%04d bytes]; bp=%05d\n", mt, sz, bp );
            bClose = false;

            /*
             * Special cases:
             *    1) Blob   - Synchronous, (except _blSubCONTRIB 
             *    2) Status - _OnSession()
             *    3) Image  - Synchronous
             *    4) Status - Synchronous
             */
            if ( _hbDawg != null )
               _hbDawg.Touch();
            qm.SetMultiplier( mult );
            _EnterOnMsg( qm );
            if ( bPro ) {
               try {
                  if ( mt == _mtBLOB ) {
                     bl = (Blob)qm;
                     if ( bl.IsDone() ) {
                        switch( mt2 ) {
                           case _blSubFIELDLIST:
                           case _blSubEXCHANGES:
                           case _blSubOPTIONS:
                           case _blSubIDXLIST:
                           case _blSubMMIDS:
                           case _blSubSORT:
                              _OnBlobList( iUpd, tkr, (BlobList)qm, bp, nL );
                              bClose = true;
                              break;
                           case _blSubEXCHLIST:
                           case _blSubSNAPSHOT:
                           case _blSubFUNDS:
                           case _blSubINDICES:
                           case _blSubFUT_CHAIN:
                           case _blSubFUTOPT_CHAIN:
                           case _blSubFUT_SNAP:
                           case _blSubFUTURES:
                           case _blSubBONDS:
                           case _blSubFUNDY:
                           case _blSubMKT_CAT:
                           case _blSubDUMPDB:
                           case _blSubUNDERLYERS:
                              _OnBlobTable( iUpd, tkr, (BlobTable)qm, uc );
                              break;
                           case _blSubCONTRIB:
                              iUpd.OnMsg( tkr, qm, false );
                              break;
                           default:
                              iUpd.OnQuoddMsg( tkr, qm ); // TODO : Queue
                              break;
                        }
                     }
                  }
                  else if ( mt == _mtPING ) {
                     if ( _CanLog( _UC_LOG_PING ) )
                        cout.printf( "[%s] PING\n", Now() );
                  }
                  else if ( mt == _mtSESSION )
                     _OnSession( (Status)qm );
                  else if ( mt2 == _mtSubIMG ) {
                     if ( uc != null ) {
                        uc.SetMultiplier( (Image)qm );
                        qm.SetMultiplier( uc._dMult );
                     }
                     _OnImage( iUpd, tkr, (Image)qm, nL );
                  }
                  else if ( mt == _mtDEAD )
                     _OnStatus( iUpd, tkr, (Status)qm, nL );
                  else {
                     if ( _CanLog( _UC_LOG_UPDATE ) ) {
                        if ( ( (rtl=qm.RTL()) % _nLogUpd ) == 0 ) {
                           fmt = "[%s] UPDATE {%s} : RTL=%d\n";
                           cout.printf( fmt, Now(), qm.tkr(), rtl );
                        }
                     }
                     iUpd.OnMsg( tkr, qm, false );
                  }
               } catch( Exception e ) {
                  err = e.getMessage();
                  cerr.printf( "[%s] %s\n%s\n", Now(), err, CallStack() );
               } catch ( Error ee ) {
                  err = ee.getMessage();
                  cerr.printf( "[%s] %s\n%s\n", Now(), err, CallStack() );
               }
            }
            _LeaveOnMsg( qm );
            _buf.inc( sz );
            if ( IsCompressed() && _CanLog( _UC_LOG_INFLATE ) )
               cout.printf( "_buf.inc( %d ); bp=%d\n", sz, _buf.bp() );
            _tot += 1;
            if ( _HasStats() ) {
               _stats.PutLong( _STAT_RX_MSG, _tot );
               _stats.PutTimeNow( _STAT_RX_TIME );
               if ( (_tot%1000) == 0 )
                  _stats.PutDouble( _STAT_CPU_USED, _cpu.Get() );
            }
            if ( ( (_tot%_mod) == 0 ) && _CanLog( _UC_LOG_NUMUPD ) ) {
               dt = 1000.0 * ( _cpu.Get() - d0 );
               cout.printf( "[%s] %d in %.1fmS\n", Now(), _tot, dt );
               d0 = _cpu.Get();
            }

            // Clean up data stream 

            qm.ClearUserObj();
            if ( bClose )
               _CloseStream( idx );
         }
// cout.printf( "[%s] END   _buf.chop() : nMsg=%d\n", Now(), n );

         // Reset input bufer

         if ( ( IsCompressed() ) ) {
            /*
             * 1) Handle compressed fragment at end of _bufZ
             */
            inb.inc( zn );
            if ( IsCompressed() && _CanLog( _UC_LOG_INFLATE ) )
               cout.printf( "inb.inc( %d ); bp=%d\n", zn, inb.bp() );
            inb.reset( n, zn /* nR */ );
            if ( IsCompressed() && _CanLog( _UC_LOG_INFLATE ) )
               cout.printf( "inb.reset( %d ); off=%d\n", zn, inb.off() );
            /*
             * 2) Partial inflated msg at end of _buf
             */
            _buf.reset( n+1, zn );  // n+1 : Always
            if ( IsCompressed() && _CanLog( _UC_LOG_INFLATE ) )
               cout.printf( "_buf.reset( %d ); off=%d\n", zn, _buf.off() );
         }
         else
            inb.reset( n, mSz );
         if ( _bByteLog || _CanLog( _UC_LOG_BYTELOG ) )
            cout.printf( "EOM : off=%d\n", inb.off() );
      }
   }


   //////////////////////
   // Helpers - Channel
   //////////////////////
   private void _LogDisco() throws Exception
   {
      throw new Exception();
   }

   protected void Sleep( double dSlp )
   {
      int tSlp;

      tSlp = (int)( dSlp * 1000.0 );
      try {
         Thread.sleep( tSlp );
      } catch( InterruptedException e ) {
         breakpoint();
      }
   }


   //////////////////////
   // Helpers - Load
   //////////////////////
   private HashSet<String> _HashSetFromFile( String pFile, boolean bLog )
   {
      String          line, err, fmt;
      BufferedReader  buf;
      HashSet<String> rtn;
      int              nt;

      // Pre-condition

      if ( pFile == null )
         return null;

      // Safe to read in ...

      if ( bLog ) {
         fmt = "[%s] Loading Level2 KO list from %s ...\n";
         cout.printf( fmt, Now(), pFile );
      }
      rtn = new HashSet<String>();
      try {
         buf = new BufferedReader( new FileReader( pFile ) );
         for ( ; (line=buf.readLine()) != null; ) {
            rtn.add( line );
            if ( bLog )
               cout.printf( "[%s] Level2 KO : %s\n", Now(), line );
         }
         buf.close();
      } catch( Exception e ) {
         err = e.getMessage();
         cerr.printf( "[%s] Error loading %s : %s\n", Now(), pFile, err );
      }
      if ( (nt=rtn.size()) == 0 )
         rtn = null;
      if ( bLog ) {
         fmt = "[%s] %d KO tickers loaded from %s ...\n";
         cout.printf( fmt, Now(), nt, pFile );
      }
      return rtn;
   }

   private HashMap<String, String> _HashMapFromFile( String pFile )
   {
      HashSet<String>         hset;
      HashMap<String, String> rtn;
      Iterator<String>        it;
      String[]                kv;
      String                  s, k, v, fmt;
      int                     nt, nk;

      // Pre-condition

      if ( (hset=_HashSetFromFile( pFile, false )) == null )
         return null;

      // Safe to read in ...

      fmt = "[%s] Loading Level2 2nd req list from %s ...\n";
      cout.printf( fmt, Now(), pFile );
      rtn = new HashMap<String, String>();
      it  = hset.iterator();
      while( it.hasNext() ) {
         s  = it.next();
         kv = s.split( "," );
         nk = kv.length;
         if ( nk == 2 ) {
            k = kv[0].trim();
            v = kv[1].trim();
            if ( !rtn.containsKey( k ) ) {
               rtn.put( k, v );
               fmt = "[%s] Level 2 2nd req : %s -> %s\n";
               cout.printf( fmt, Now(), k, v );
            }
         }
      }
      if ( (nt=rtn.size()) == 0 )
         rtn = null;
      fmt = "[%s] %d 2nd req tickers loaded from %s ...\n";
      cout.printf( fmt, Now(), nt, pFile );
      return rtn;
   }


   //////////////////////
   // Stats - Where are we?
   //////////////////////
   private void _EnterRead( int winSiz )
   {
      if ( _HasStats() ) {
         _stats.PutBool( _STAT_IN_READ, true );
         _stats.PutTimeNow( _STAT_RD_TIME );
         _stats.PutLong( _STAT_RD_WIN, winSiz );
      }
   }

   private void _LeaveRead( int len )
   {
      if ( _HasStats() ) {
         _stats.PutBool( _STAT_IN_READ, false );
         _stats.AddLong( _STAT_RX_BYTE, Math.max( 0,len ) );
         _stats.PutLong( _STAT_RD_WIN, 0 );
      }
   }

   private void _EnterOnMsg( QuoddMsg qm )
   {
      // SlowMsg calc

      _tMsg0 = ( _tSlowMsg > 0 ) ? System.currentTimeMillis() : 0;

      // Stats

      if ( _HasStats() ) {
         _stats.PutBool( _STAT_IN_APP, true );
         _stats.PutHex( _STAT_MSG_TYPE, (long)qm.mt() );
         _stats.PutHex( _STAT_MSG_SUBTYPE, (long)qm.mtSub() );
      }
   }

   private void _LeaveOnMsg( QuoddMsg qm )
   {
      String fmt, tkr;
      long   tMsg;
      int    mt, mt2;   

      // SlowMsg calc

      if ( _tSlowMsg > 0 ) {
         tMsg = System.currentTimeMillis() - _tMsg0;
         if ( tMsg > _tSlowMsg ) {
            fmt = "[%s] SLOW-MSG : [0x%02x,0x%02x,%s] in %dmS\n";
            mt  = (int)qm.mt(); 
            mt2 = (int)qm.mtSub();
            tkr = ( qm.tkr() != null ) ? qm.tkr() : "null";
            cout.printf( fmt, Now(), mt, mt2, tkr, tMsg );
         }
      }
      _tMsg0 = 0;

      // Stats

      if ( _HasStats() ) {
         _stats.PutBool( _STAT_IN_APP, false );
         _stats.PutHex( _STAT_MSG_TYPE, 0 );
         _stats.PutHex( _STAT_MSG_SUBTYPE, 0 );
      }
   }



   ///////////////////////////////////////////////
   //
   //     c l a s s   D a t a S t r e a m
   //
   ///////////////////////////////////////////////
   /**
    * DataStream is a collection that contains the infomration associated 
    * with an opened UltraCache data stream.  An UltraCache data stream is 
    * opened via {@link QuoddFeed.util.UltraChan#Subscribe(String,Object)} or
    * {@link QuoddFeed.util.UltraChan#GetOptionChain(String,Object)}-type
    * command requests.
    */
   class DataStream extends Cmd
   {
      private IUpdate           _iUpd;
      private int               _StreamID;
      private String            _tkr;
      private String            _qry;
      private Object            _obj;
      private boolean           _bSetTkr;
      private boolean           _bOpen;
      private int               _tierChan;
      private int[]             _fltr;
      private ArrayList<String> _subs;
      private int               _nUpd;
      private double            _dMult;
      private boolean           _bLevel2;
      private PrintStream       cout;

      ////////////////////
      // Constructor
      ////////////////////
      DataStream( IUpdate iUpd, 
                  String  tkr, 
                  int     StreamID, 
                  Object  obj,
                  boolean bSetTkr )
      {
         _iUpd      = iUpd;
         _StreamID  = StreamID;
         _tkr       = tkr;
         _bSetTkr   = bSetTkr;
         _qry       = "";
         _obj       = obj;
         _bOpen     = false;
         _tierChan  = 0;
         _fltr      = null;
         _subs      = new ArrayList<String>();
         _nUpd      = 0;
         _dMult     = 1.0;
         _bLevel2   = false;
         cout       = System.out;
         AddSubscription( tkr );
      }


      ////////////////////
      // Access
      ////////////////////
      boolean IsSetTkr()
      {
         return _bSetTkr;
      }

      boolean IsOpen()
      {
         return _bOpen;
      }

      int TierChan()
      {
         return _tierChan;
      }

      IUpdate iUpd()
      {
         return _iUpd;
      }

      int StreamID()
      {
         return _StreamID;
      }

      String tkr()
      {
         return _tkr;
      }

      String[] subs()
      {
         String[] rtn;
         String   req;
         int      i, sz;

         sz  = _subs.size();
         rtn = new String[sz];
         for ( i=0; i<sz; i++ )
            rtn[i] = _subs.get( i );
         return rtn;
      }

      String qry()
      {
         return _qry;
      }

      Object obj()
      {
         return _obj;
      }

      boolean IsAllowableProtocol( QuoddMsg m )
      {
         int     i, sz, pro;
         boolean bOK;

         // Pre-condition

         if ( _fltr == null )
            return true;

         // Search them all

         pro = m.protocol();
         bOK = false;
         sz  = _fltr.length;
         for ( i=0; !bOK && i<sz; bOK |= ( _fltr[i++] == pro ) );
         return bOK;
      }

      boolean IsAllowableMsg( QuoddMsg qm )
      {
         boolean bOK;
         char    mt;
         int     nt;

         // Drop EQTrade's from CSCO/T

         nt  = _subs.size();
         bOK = ( nt <= 1 );
         if ( !bOK ) {
            mt = qm.mtSub();
            switch( mt ) {
               case MsgTypes._mtSubIMG:
               case MsgTypes._eqSubTRDSHORT:
               case MsgTypes._eqSubSUMMARY:
               case MsgTypes._eqSubMKTCTRSUMM:
               case MsgTypes._eqSubTRDLONG:
               case MsgTypes._eqSubTRDASOF:
               case MsgTypes._eqSubTRDASOFCXL:
                  bOK = tkr().equals( qm.tkr() );
   // if ( !bOK ) cout.printf( "DROP TRADE from %s\n", qm.tkr() );
                  break;
               default:
                  bOK = true;
                  break;
            }
         }
         return bOK;
      }

      boolean IsKO( QuoddMsg qm )
      {
         boolean rtn;
         String  tkr;

         rtn = false;
         if ( _bLevel2 && ( _l2KO != null ) ) { 
            tkr = qm.tkr();
            rtn = _l2KO.contains( tkr );
         }
         return rtn;
      }


      ////////////////////
      // Mutator
      ////////////////////
      void SetMultiplier( Image img )
      {
         double den;

         // FUTUREs only (for now ...)

         switch( img.mt() ) {
            case _mtFUTURE: 
               break;
            default:
               return;
         }

         // Reverse of UltraCache

         den = 1.0;
         switch( img._multiplier ) {
            case  0:  den = 0.00001;  break;
            case  1:  den = 0.0001;   break;
            case  2:  den = 0.001;    break;
            case  3:  den = 0.01;     break;
            case  4:  den = 0.1;      break;
            case  5:  den = 1.0;      break;
            case  6:  den = 10.0;     break;
            case  7:  den = 100.0;    break;
            case  8:  den = 1000.0;   break;
            case  9:  den = 10000.0;  break;
            case 10:  den = 100000.0; break;
         }
         _dMult = 1.0 / den;
      }

      void AddSubscription( String tkr )
      {
         _subs.add( tkr );
      }

      void Register( IUpdate iUpd )
      {
         _iUpd = iUpd;
      }

      void SetQuery( String qry )
      {
         _bOpen = true;
         _qry  += qry;
      }

      String Open()
      {
         String rtn;

         rtn    = _bOpen ? "" : qry(); 
         _bOpen = true;
         return qry(); // rtn;
      }

      void Close()
      {
         _bOpen = false;
      }

      void SetProtocolFilter( int[] fltr )
      {
         _fltr = fltr;
      }

      void SetTierChan( int tierChan )
      {
         _tierChan = tierChan;
      }


      ////////////////////
      // Operations
      ////////////////////
      QuoddMsg Transform( QuoddMsg qm )
      {
         QuoddMsg rtn;

         rtn = qm;
         if ( _tkr.equals( _FUT_CHAIN ) )
            cout.printf( "GetFuturesChain()\n" );
         return rtn;
      }
   }   // class DataStream



   ///////////////////////////////////////////////
   //
   //     c l a s s   M u l t i S n a p
   //
   ///////////////////////////////////////////////
   /**
    * MultiSnap is an IUpdate derivitive that receives ...
    */
   class MultiSnap extends IUpdate
   {
      private UltraChan  _uChan;
      private int        _nTkr;
      private boolean    _bDone;
      private boolean    _bOpen;
      private boolean    _bNfy;
      private boolean    _bLog;
      private int        _nRes;
      private String     _disco;
      private String[]   _tkrs;
      private QuoddMsg[] _res;
      private int[]      _sids;   // Stream ID's

      ////////////////////
      // Constructor
      ////////////////////
      MultiSnap( UltraChan uChan, String[] tkrs )
      {
         int i;

         _uChan = uChan;
         _nTkr  = tkrs.length;
         _bOpen = false;
         _bDone = false;
         _bNfy  = false;
         _bLog  = _CanLog( _UC_LOG_MULTISNAP );
         _disco = null;
         _tkrs  = tkrs;
         _res   = new QuoddMsg[_nTkr];
         _sids  = new int[_nTkr];
         for ( i=0; i<_nTkr; _res[i++] = null );
      }


      ////////////////////
      // Operations
      ////////////////////
      QuoddMsg[] Snap( int tmout )
      {
         UltraChan       u = _uChan;
         Object          arg;
         String          err;
         QuoddMsg[]      qm;
         HashSet<String> rdb;
         int             i, nt;

         try {
            synchronized( this ) {
               arg = null;
               nt  = _nTkr;
               rdb = new HashSet<String>();
               for ( i=0; i<nt; i++ ) {
                  if ( rdb.contains( _tkrs[i] ) )
                     _res[i] = new Status( _tkrs[i], "DUPLICATE" );
                  else
                     rdb.add( _tkrs[i] );
               }
               if ( _bLog ) 
                  cout.printf( "[%s] ENTER Snap( %d )", Now(), nt );
               for ( i=0; ( _disco == null ) && i<nt; i++ ) {
                  _sids[i] = 0;
                  if ( _bLog )
                     cout.printf( "Snap( %d )\n", i );
                  if ( _res[i] == null )
                     _sids[i] = _uChan.Snap( _tkrs[i], arg, this );
               }
               if ( _bLog ) {
                  cout.printf( "[%s] LEAVE Snap( %d )", Now(), nt );
                  if ( _disco != null )
                     cout.printf( "; _disco = %s", _disco );
                  cout.printf( "\n" );
               }
               _bOpen = true;
               if ( _disco == null ) {
                  if ( _bLog )
                     cout.printf( "[%s] ENTER wait( %d )\n", Now(), tmout );
                  if ( tmout != 0 )
                     this.wait( tmout );
                  else
                     this.wait();
                  if ( _bLog )
                     cout.printf( "[%s] LEAVE wait( %d )\n", Now(), tmout );
               }
               else {
                  if ( _bLog )
                     cout.printf( "[%s] ENTER Snap.OnDisco()\n", Now() );
                  OnDisconnect( _disco );
                  if ( _bLog )
                     cout.printf( "[%s] LEAVE Snap.OnDisco()\n", Now() );
               }
            }
         } catch( InterruptedException e ) {
            err = e.getMessage();
         }

         /*
          * 1) Truncate to what is completed
          * 2) Release the hounds ... I mean streams
          */
         synchronized( this ) {
            _bDone = true;
            if ( _nRes < _nTkr ) {
               qm = new QuoddMsg[_nRes];
               for ( i=0; i<_nRes; qm[i]=_res[i],i++ );
               _res = qm;
            }
            for ( i=0; i<_sids.length; _uChan._CloseStream( _sids[i++] ) );
         }
         return _res;
      }


      ////////////////////
      // Disconnect Interface
      ////////////////////
      public void OnDisconnect( String err )
      {
         String tkr, fmt;
         Status sts;
         int    i;

         /*
          * synchronized because Snap() might be running in user thread:
          * 1) Stop all streams
          * 2) Blast status to all
          */
         synchronized( this ) {
            _disco = err;
            fmt    = "[%s] %s MultiSnap.OnDisco( %d,%d )\n";
            if ( _bLog )
               cout.printf( fmt, Now(), "ENTER", _nRes, _nTkr );
            if ( _bOpen ) {
               for ( i=0; i<_sids.length; _uChan._CloseStream( _sids[i++] ) );
               for ( i=_nRes; i<_nTkr; i++ ) {
                  tkr = _tkrs[i];
                  sts = new Status( tkr, err );
                  sts.SetIOException();
                  OnStatus( tkr, sts );
               }
               _bDone = true;
            }
            if ( _bLog )
               cout.printf( fmt, Now(), "LEAVE", _nRes, _nTkr );
         }
      }


      ////////////////////
      // IUpdate Interface
      ////////////////////
      public void OnImage( String StreamName, Image img )
      {
         _OnMsg( img );
      }

      public void OnStatus( String StreamName, Status sts )
      {
         _OnMsg( sts );
      }


      ////////////////////
      // Helpers
      ////////////////////
      void _OnMsg( QuoddMsg qm )
      {
         Status sts;
         int    nr;

         // Pre-condition

         if ( _nRes >= _nTkr )
            return;

         // Set ticker (DEAD)

         _FillInDuplicates();
         if ( qm.mt() == _mtDEAD ) {
            sts = (Status)qm;
            if ( sts.tkr() == null ) 
               sts.SetTkr( _tkrs[_nRes] );
         }

         // Safe to add

         synchronized( this ) {
            if ( !_bDone ) {
               _res[_nRes] = qm.clone();
               _nRes      += 1;
               _FillInDuplicates();
               if ( _nRes == _nTkr ) { 
                  this.notify();
                  _bNfy = true;
               }
            }
         }
      }

      private void _FillInDuplicates()
      {
         synchronized( this ) {
            while( ( _nRes < _nTkr ) && ( _res[_nRes] != null ) )
               _nRes += 1;
         }
      }
   }  // class MultiSnap



   ///////////////////////////////////////////////
   // 
   //          c l a s s   G o v e r n o r
   //
   ///////////////////////////////////////////////
   /**
    * Governor paces the requests - Subscribe, Unsubscribe, etc. - to 
    * the UltraCache.
    */
   class Governor implements Runnable
   {
      ////////////////////
      // Instance Members
      ////////////////////
      private UltraChan         _uChan;
      private int               _bias;
      private int               _pace;
      private Thread            _thr;
      private boolean           _bRun;
      private ArrayList<String> _reqs;

      //////////////////////
      // Constructor
      //////////////////////
      Governor( UltraChan uChan, int bias, int pace )
      {
         _uChan = uChan;
         _bias  = bias;
         _pace  = pace;
         _thr   = new Thread( this, _usr + "_Governor" );
         _bRun  = true;
         _reqs  = new ArrayList<String>();
         _thr.start();
      }

      void Stop()
      {
         _bRun = false;
         if ( _thr != null )
            _thr.interrupt();
         _thr = null;
      }


      //////////////////////
      // Operations
      //////////////////////
      void Add( String str )
      {
         synchronized( this ) {
            _reqs.add( str );
         }
      }


      //////////////////////
      // Runnable Interface
      //////////////////////
      public void run()
      {
         String qry, qAll;
         int    i, sz;

         while( _bRun ) {
            try {
               Thread.sleep( _pace );
               synchronized( this ) {
                  sz   = Math.min( _bias, _reqs.size() ); 
                  qAll = "";
                  for ( i=0; i<sz; i++ ) {
                     qry = (String)_reqs.get( i );
                     qAll += qry;
                  }
                  for ( i=sz-1; i>=0; _reqs.remove( i-- ) );
               }
               if ( qAll.length() > 0 )
                  _uChan.SyncWriteBytes( qAll );
            } catch( Exception e ) {
               _bRun = false;
            }
         }
      }
   }   // class Governor


   ///////////////////////////////////////////////
   // 
   //    c l a s s   Z o m b i e D a w g
   //
   ///////////////////////////////////////////////
   /**
    * ZombieDawg dumps ZOMBIE's regularly.
    */
   class ZombieDawg implements Runnable
   {
      ////////////////////
      // Instance Members
      ////////////////////
      private UltraChan _uChan;
      private int       _tDumpMs;
      private int       _maxLog;
      private Thread    _thr;
      private boolean   _bRun;

      //////////////////////
      // Constructor
      //////////////////////
      ZombieDawg( UltraChan uChan, int tDumpMs, int maxLog )
      {
         _uChan   = uChan;
         _tDumpMs = tDumpMs;
         _maxLog  = maxLog;
         _thr     = new Thread( this, _usr + "_ZombieDawg" );
         _bRun    = true;
         _thr.start();
      }

      void Stop()
      {
         _bRun = false;
         if ( _thr != null )
            _thr.interrupt();
         _thr = null;
      }


      //////////////////////
      // Runnable Interface
      //////////////////////
      public void run()
      {
         while( _bRun ) {
            try {
               Thread.sleep( _tDumpMs );
               _uChan.dumpZombies( "TIMER", _maxLog );
            } catch( Exception e ) {
               _bRun = false;
            }
         }
      }
   }  // class ZombieDawg


   ///////////////////////////////////////////////
   // 
   //    c l a s s   H e a r t b e a t
   //
   ///////////////////////////////////////////////
   /**
    * Heartbeat sends _mtPING every interval
    */
   class Heartbeat implements Runnable
   {
      ////////////////////
      // Instance Members
      ////////////////////
      private UltraChan _uChan;
      private int       _tHbeat;
      private long      _lastMsg;
      private Thread    _thr;
      private boolean   _bRun;

      //////////////////////
      // Constructor
      //////////////////////
      Heartbeat( UltraChan uChan, int tHbeat )
      {
         _uChan   = uChan;
         _tHbeat  = tHbeat;
         _lastMsg = 0;
         _thr     = new Thread( this, _usr + "_Heartbeat" );
         _bRun    = true;
         _thr.start();
      }

      void Stop()
      {
         _bRun = false;
         if ( _thr != null )
            _thr.interrupt();
         _thr = null;
      }


      //////////////////////
      // Operations
      //////////////////////
      public void Touch()
      {
         _lastMsg = System.currentTimeMillis();
      }


      //////////////////////
      // Runnable Interface
      //////////////////////
      public void run()
      {
         long dd;

         Touch();
         while( _bRun ) {
            try {
               Thread.sleep( _tHbeat*1000 );
               dd = ( System.currentTimeMillis() - _lastMsg ) / 1000;
               if ( dd > 2*_tHbeat ) {
                  if ( _CanLog( _UC_LOG_DISCO ) )
                     cout.printf( "[%s] HEARTBEAT DISCO\n", Now() );
                  Disconnect( "HEARTBEAT DISCO" );
               }
               else
                  _uChan._WriteBytes( "<Ping/>" );
            } catch( Exception e ) {
               _bRun = false;
            }
         }
      }
   }  // class Heartbeat


   ///////////////////////////////////////////////
   // 
   //        c l a s s   B u f f e r
   //
   ///////////////////////////////////////////////
   /**
    * Input buffer
    */
   class Buffer
   {
      ////////////////////
      // Instance Members
      ////////////////////
      /** \brief Allocated byte[] array */
      private byte[] _buf;
      /** \brief Current message pointer */
      private int    _bp;
      /** \brief Current read offset */
      private int    _off;
      /** \brief Length of chunk just read into _off */
      private int    _len;

      //////////////////////
      // Constructor
      //////////////////////
      Buffer( int bSz )
      {
         _buf = new byte[bSz];
         _bp  = 0;
         _off = 0;
         _len = 0;
      }


      //////////////////////
      // Access
      //////////////////////
      /** \brief byte[] buffer */
      byte[] buf()
      {
         return _buf;
      }

      /** \brief Current message pointer */
      int bp()
      {
         return _bp;
      }

      /** \brief Number of bytes available for reading */
      int nRead()
      {
         return bSz() - off();
      }

      /** \brief Number of bytes left for processing */
      int nLeft()
      {
         return ( _len + off() ) - bp();
      }

      /** \brief Max allocated size of buffer */
      int bSz()
      {
         return _buf.length;
      }

      /** \brief Read offset */
      int off()
      {
         return _off;
      }


      //////////////////////
      // Mutator
      //////////////////////
      /** \brief Set _len; Return nLeft() */
      int readlen( int len )
      {
         _len = len;
         _bp  = 0;
         return nLeft();
      }

      /** \brief Increment message pointer */
      void inc( int sz )
      {
         _bp += sz;
      }

      /** \brief Reset for next read */
      void reset( int n, int mSz )
      {
         _off = mSz - _bp;
         if ( ( n>0 ) && ( _off>0 ) )
            System.arraycopy( _buf, _bp, _buf, 0, _off );
         _bp  = 0;
      }

      /** \brief Re-initialize buffer state to 0 */
      void init()
      {
         _off = 0;
         _bp  = 0;
         _len = 0;
      }
   }  // class Buffer


   ///////////////////////////////////////////////
   //
   //      c l a s s   S i g H a n d l e r
   //
   ///////////////////////////////////////////////
   /**
    * Catches signal
    */
   class SigHandler extends SignalInterceptor
   {
      ////////////////////
      // Instance Members
      ////////////////////
      protected String      _sig;
      protected int         _maxLog;
      protected UltraChan[] _udb;
      protected PrintStream cout;

      //////////////////////
      // Constructor
      //////////////////////
      SigHandler( String sig, int maxLog, UltraChan uc )
      {
         String fmt;

         _sig    = sig;
         _maxLog = maxLog;
         _udb    = null;
         cout    = System.out;
         try {
            register( sig );
            fmt = "[%s] Sig.register( %s ) maxLog=%d SUCCESS\n";
            cout.printf( fmt, uc.Now(), sig, maxLog );
         } catch( SignalInterceptorException ex ) {
            fmt = "[%s] Sig.register( %s ) maxLog=%d FAILURE : %s\n";
            cout.printf( fmt, uc.Now(), sig, maxLog, ex.getMessage() );
         }
      }


      //////////////////////
      // Mutator
      //////////////////////
      void SetChans( UltraChan[] udb )
      {
         _udb = udb;
      }


      //////////////////////
      // Handler
      //////////////////////
      protected boolean handle( String sigName )
      {
         int i;

         for ( i=0; i<_udb.length; _udb[i++].dumpZombies( sigName, _maxLog ) );
         return false;
      }
   } // class SigHandler

} // class UltraChan
