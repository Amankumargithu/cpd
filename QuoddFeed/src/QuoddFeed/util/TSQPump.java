/******************************************************************************
*
*  TSQPump.java
*     TSQ Pump
*
*  REVISION HISTORY:
*     28 APR 2015 jcs  Created.
*     24 MAY 2015 jcs  Build  93: DataFile.Pump()
*      8 JUN 2015 jcs  Build  94: GetTickers()
*     22 JUL 2015 jcs  Build  95: long PumpXxx()
*     31 AUG 2015 jcs  Build  96: Destroy(); InRange() checks 
*     14 SEP 2015 jcs  Build  97: PumpTickerTo() : offset bug
*     19 SEP 2016 jcs  Build 101: get( dst ) : Copy into dst if _getSlice()
*
*  (c) 2011-2016 Quodd Financial
*******************************************************************************/
package QuoddFeed.util;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import QuoddFeed.msg.QuoddMsg;
import QuoddFeed.msg.Factory;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
//
//              c l a s s    T S Q P u m p
//
/////////////////////////////////////////////////////////////////
public class TSQPump extends IUpdate
{
   ////////////////////
   // Constants
   ////////////////////
   protected static final int  _K         = 1024;
   protected static final long _MB        = _K * _K;
   protected static final int  _MAX_MSGSZ = 10224;

   ////////////////////
   // Class-wide
   public static boolean _bLogRoll        = false;
   /*
    * Logging from PUMP_LOGLVL
    */
   static public final int _PUMP_LOG_PUMP     = 0x0001;
   static public final int _PUMP_LOG_NEXT     = 0x0002;

   ////////////////////
   // Instance Members
   ////////////////////
   private String                       _idxFile;
   private String                       _dataFile;
   private PrintStream                  cout;
   private IndexFile                    _idx;
   private DataFile                     _data;
   private HashSet<String>              _wl;
   private HashMap<Integer, PumpThread> _thrDb;
   private int                          _thrID;
   private int                          _logLvl;

   ////////////////////
   // Constructor
   ////////////////////
   public TSQPump( String idxFile, String dataFile )
   {
      String   pl;
      String[] kv;
      int      i, nk;

      _idxFile  = idxFile;
      _dataFile = dataFile;
      cout      = System.out;
      _idx      = null;
      _data     = null;
      _wl       = new HashSet<String>();
      _thrDb    = new HashMap<Integer, PumpThread>();
      _thrID    = 1;
      _logLvl   = 1;

      // Logging Level

      _logLvl = 0;
      if ( (pl=UCconfig.GetEnv( "PUMP_LOGLVL" )) != null ) {
         kv = pl.split(",");
         nk = kv.length;
         for ( i=0; i<nk; i++ ) {
            _logLvl |= kv[i].equals( "PUMP" )    ? _PUMP_LOG_PUMP    : 0;
            _logLvl |= kv[i].equals( "NEXT" )    ? _PUMP_LOG_NEXT    : 0;
         }
      }
   }

   public void Destroy()
   {
      if ( _idx != null )
         _idx.Destroy();
      if ( _data != null )
         _data.Destroy();
      _idx  = null;
      _data = null;
   }


   ///////////////////////////////
   // Access / Operations
   ///////////////////////////////
   public void Start()
   {
      // Pre-condition

      if ( _idx != null )
         return;
      OnIndexFileStart( _idxFile );
      _idx  = new IndexFile( _idxFile );
      OnIndexFileLoaded( _idxFile );
      OnDataFileStart( _dataFile );
      _data = new DataFile( _dataFile, _idx );
      SetMapRange( "00:00", "23:59" );
      OnDataFileLoaded( _dataFile );
   }

   /**
    * \brief Return array of tickers; Position in array is index
    * in index file
    * 
    * \return Array of tickers
    */
   public String[] GetTickers()
   {
      return _idx.GetTickers();
   }

   public void Dump( PrintStream ps )
   {
      _idx.Dump( ps );
   }


   //////////////////////
   // Watchlist / Msg Filter
   //////////////////////
   public boolean IsWatched( String tkr )
   {
      return ( _wl.size() > 0 ) ? _wl.contains( tkr ) : true;
   }

   public int NumWatch()
   {
      return _wl.size();
   }

   public int SetWatchlist( String[] tdb )
   {
      int i, nt;

      nt = ( tdb != null ) ? tdb.length : 0;
      for ( i=0; i<nt; AddWatchlist( tdb[i++] ) );
      return NumWatch();
   }

   public void AddWatchlist( String tkr )
   {
      if ( !_wl.contains( tkr ) )
         _wl.add( tkr );
   }

   public void ClearWatchlist()
   {
      _wl.clear();
   }




   ///////////////////////////////
   // Mapping Range
   ///////////////////////////////
   /**
    * \brief Maps the data file for a specific time range.  When pumping 
    * large number of tickers - Or ALL tickers - it is more efficient to 
    * map once rather than once per ticker.
    *
    * \param t0 - Start time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param tt - End time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    */
   public void SetMapRange( String t0, String t1 )
   {
      int ix0, ix1;

      ix0  = _idx.GetIdx( t0, true );
      ix1  = _idx.GetIdx( t1, false );
      _data.SetMapRange( Math.min( ix0, ix1 ), Math.max( ix0, ix1 ) );
   }

   /**
    * \brief Clear the map range set in SetMapRange().
    */
   public void ClearMapRange()
   {
      _data.ClearMapRange();
   }


   ///////////////////////////////
   // Pump One Ticker Operations
   ///////////////////////////////
   /**
    * \brief Pump messages for specified ticker between time range to this
    * TSQPump receiver.
    *
    * \param t0 - Start time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param tt - End time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param tkr - Ticker name (CSCO, O:AAPL\15A15\90.00, etc.)
    * \return Number of messages pumped.
    */
   public long PumpTicker( String t0, String t1, String tkr )
   {
      return PumpTickerTo( t0, t1, tkr, this );
   }

   /**
    * \brief Pump messages for specified ticker between time range 
    * to a user-supplied IUpdate receiver.
    *
    * \param t0 - Start time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param tt - End time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param tkr - Ticker name (CSCO, O:AAPL\15A15\90.00, etc.)
    * \param iUpd - IUpdate receiver to pump messages to.
    * \return Number of messages pumped.
    */
   public long PumpTickerTo( String t0, String t1, String tkr, IUpdate iUpd )
   {
      int     ix0, ix1, px0, px1, tMs;
      boolean swap;

      Start();
      ix0  = _idx.GetIdx( t0, true );
      ix0 -= ( ix0 > -1 ) ? 1 : 0;
      ix1  = _idx.GetIdx( t1, false );
      px0  = Math.min( ix0, ix1 );
      px1  = Math.max( ix0, ix1 );
      px1 += ( px0 == px1 ) ? 1 : 0;
      swap = ( t0.compareTo( t1 ) > 0 );
      ClearWatchlist();
      AddWatchlist( tkr );
      return _data._Pump( this,
                          swap ? t1 : t0,
                          swap ? t0 : t1,
                          px0,
                          px1,
                          false,
                          iUpd );
   }


   ///////////////////////////////
   // Pump ALL Ticker Operations
   ///////////////////////////////
   /**
    * \brief Pump messages for ALL tickers between time range to this 
    * TSPump receiver.
    *
    * \param t0 - Start time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param tt - End time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param tkr - Ticker name (CSCO, O:AAPL\15A15\90.00, etc.)
    * \param iUpd - IUpdate receiver to pump messages to.
    * \return Number of messages pumped.
    */
   public long PumpAll( String t0, String t1, boolean bSort )
   {
      return PumpAllTo( t0, t1, bSort, this );
   }

   /**
    * \brief Pump messages for ALL tickers between time range
    * to an IUpdate receiver other than  this TSQPump.
    *
    * \param t0 - Start time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param tt - End time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param iUpd - IUpdate receiver to pump messages to.
    * \return Number of messages pumped.
    */
   public long PumpAllTo( String t0, String t1, boolean bSort, IUpdate iUpd )
   {
      int     ix0, ix1, px0, px1;
      boolean swap;

      Start();
      ix0  = _idx.GetIdx( t0, true );
      ix0 -= ( ix0 > -1 ) ? 1 : 0;
      ix1  = _idx.GetIdx( t1, false );
      px0  = Math.min( ix0, ix1 );
      px1  = Math.max( ix0, ix1 );
      px1 += ( px0 == px1 ) ? 1 : 0; 
      swap = ( t0.compareTo( t1 ) > 0 );
      return _data._Pump( this,
                          swap ? t1 : t0,
                          swap ? t0 : t1,
                          px0,
                          px1,
                          bSort,
                          iUpd );
   }


   ///////////////////////////////
   // Threaded Pump Operations
   ///////////////////////////////
   /**
    * \brief Pump messages for specified ticker between time range to 
    * a user-specified IUpdate receiver.  
    *
    * The messages are pumped in a new thread created by the library.  You
    * may wait for the results via WaitPumpThread() using the unique
    * thread ID returned from this call.
    *
    * \param t0 - Start time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param t1 - End time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param tkr - Ticker name (CSCO, O:AAPL\15A15\90.00, etc.)
    * \param iUpd - IUpdate receiver to pump messages to.
    * \return Unique Thread identifier you can use for WaitPumpThread()
    */
   public int PumpTickerThreaded( String  t0,
                                  String  t1,
                                  String  tkr,
                                  IUpdate iUpd )
   {
      PumpThread thr;
      int        ix0, ix1, px0, px1, tid;
      boolean    swap;

      Start();
      ix0  = _idx.GetIdx( t0, true );
      ix1  = _idx.GetIdx( t1, false );
      px0  = Math.min( ix0, ix1 );
      px1  = Math.max( ix0, ix1 );
      px1 += ( px0 == px1 ) ? 1 : 0; 
      swap = ( t0.compareTo( t1 ) > 0 );
      synchronized( _thrDb ) {
         tid = _thrID++;
         AddWatchlist( tkr );
         thr = new PumpThread( this,
                               swap ? t1 : t0,
                               swap ? t0 : t1,
                               px0,
                               px1,
                               false,
                               iUpd,
                               tid );
         _thrDb.put( tid, thr );
         thr.Start();
      }
      return tid;
   }

   /**
    * \brief Pump messages for specified ticker between time range to
    * a user-specified IUpdate receiver.
    *
    * The messages are pumped in a new thread created by the library.  You
    * may wait for the results via WaitPumpThread() using the unique
    * thread ID returned from this call.
    *
    * \param t0 - Start time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param t1 - End time in HH:MM:SS, HHMMSS, HH:MM or HHMM
    * \param iUpd - IUpdate receiver to pump messages to.
    * \return Unique Thread identifier you can use for WaitPumpThread()
    */
   public int PumpAllThreaded( String  t0,
                               String  t1,
                               IUpdate iUpd )
   {
      return PumpTickerThreaded( t0, t1, null, iUpd );
   }

   /**
    * \brief Wait for a specific thread to end.
    *
    * \param thrID - Thread ID from PumpTickerThreaded() or PumpAllThreaded()
    * \return Number of messages pumped
    */
   public long WaitPumpThread( int thrID )
   {
      PumpThread thr;

      // 1) Pull off HashMap

      thr = null;
      synchronized( _thrDb ) {
         if ( _thrDb.containsKey( thrID ) ) {
            thr = _thrDb.get( thrID );
            _thrDb.remove( thrID );
         }
      }

      // 2) Wait and harvest results

      if ( thr != null )
         return thr.Wait();
      return 0;
   }


   //////////////////////
   // TSQPump Interface
   //////////////////////
   /**
    * \brief Callback invoked when Index File is loading
    *
    * @param idxFile Index filename
    */
   public void OnIndexFileStart( String idxFile )
   {
      // Derived classes implement
   }

   /**
    * \brief Callback invoked when Index File has been loaded
    *
    * @param idxFile Index filename
    */
   public void OnIndexFileLoaded( String idxFile )
   {
      // Derived classes implement
   }

   /**
    * \brief Callback invoked when Data File is loading
    *
    * @param dataFile Index filename
    */
   public void OnDataFileStart( String dataFile )
   {
      // Derived classes implement
   }

   /**
    * \brief Callback invoked when Data File has been loaded 
    *
    * @param dataFile Index filename 
    */
   public void OnDataFileLoaded( String dataFile )
   {
      // Derived classes implement
   }

   /**
    * \brief Callback invoked when query begins
    *
    * @param bSorted true if sorted query
    */
   public void OnQueryStart( boolean bSorted )
   {
      // Derived classses implement
   }

   /**
    * \brief Callback invoked when data file is mapped
    *
    * @param dFile Name of file just mapped in
    * @param tm Time slice mapped in HH:MM:SS 
    * @param off Mapped file window offset
    * @param len Mapped file window len
    */
   public void OnFileMapped( String dFile, String tm, long off, long len )
   {
      // Derived classses implement
   }

   /**
    * \brief Callback invoked when TSQ query is complete
    *
    * @param total Total msgs queried
    */
   public void OnQueryComplete( long total )
   {
      // Derived classes implement
   }

   /**
    * \brief Callback invoked when TSQ query sort is complete  
    *
    * @param total Total msgs queried and sorted
    */
   public void OnSortComplete( int total )
   {
      // Derived classes implement
   }

   /**
    * \brief Callback invoked when TSQ query for specified ticker starts
    *
    * @param tkr - Ticker name
    * @param nTkr - Ticker name
    * @param totTkr - Total tickers in TSQ File
    */
   public void OnTickerStart( String tkr, int nTkr, int totTkr )
   {
      // Derived classes implement
   }

   /**
    * \brief Callback invoked when TSQ query for specified ticker ends
    *
    * @param tkr - Ticker name
    * @param total - Total msgs pumped for ticker
    */
   public void OnTickerEnd( String tkr, int total )
   {
      // Derived classes implement
   }

   /**
    * \brief Callback invoked when TSQ error occurs
    *
    * @param txt Textual description of error
    */
   public void OnError( String txt )
   {
      // Derived classes implement
   }


   ////////////////////
   // Helpers
   ////////////////////
   private boolean _CanLog( int type )
   {
      return( ( _logLvl & type ) == type );
   }


   ////////////////////
   // Class-wide
   ////////////////////
   public static String Now()
   {
      long t0;

      t0 = System.currentTimeMillis();
      return QuoddFeed.msg.QuoddMsg.pDateTimeMs( t0 );
   }


   //////////////////////////////////////////////////
   //
   //        c l a s s    I n d e x F i l e
   //
   //////////////////////////////////////////////////
   class IndexFile
   {
      ///////////////////////////
      // Class-wide
      ///////////////////////////
      private static final int _IDX_HDR_SZ = 208;
      private static final int _REC_HDR_SZ =  64;
      private static final int _ENDIAN_OFF = 148;

      ////////////////////
      // Instance Members
      ////////////////////
      String           _file;
      String           _flags;
      int              _pos;
      FileChannel      _ch;
      RandomAccessFile _fp;
      MappedByteBuffer _vw;
      Position         _cur;
      TsqDbHdr         _hdr;
      TsqRecHdr[]      _recByIdx;

      ///////////////////////////////
      // Constructor
      ///////////////////////////////
      IndexFile( String file )
      {
         long sz;
         int  i, nr;

         // 1) Init guts

         _file     = file;
         _flags    = "r";
         _pos      = 0;
         _ch       = null;
         _fp       = null;
         _vw       = null;
         _cur      = null;
         _hdr      = null;
         _recByIdx = null;

         // 2) Read File

         try {
            _fp = new RandomAccessFile( _file, _flags );
         } catch( FileNotFoundException e ) {
            cout.printf( "Can't open %s : %s\n", _file, e.getMessage() );
            _fp = null;
            return;
         }

         // 2) Map File

         _ch = _fp.getChannel();
         sz  = 0;
         try {
            sz  = _fp.length();
            _vw = _ch.map( FileChannel.MapMode.READ_ONLY, 0, sz );
         } catch( IOException e ) {
            cout.printf( "map() failed : %s\n", e.getMessage() );
            _vw = null;
            System.exit( 0 );
         }

         // 3) Rock on 

         _hdr = new TsqDbHdr();
         nr   = _hdr._numTkr;
         _recByIdx  = new TsqRecHdr[nr];
         for ( i=0; i<_recByIdx.length; i++ )
            _recByIdx[i] = new TsqRecHdr( i );
         _cur = new Position( -1 );
         _hdr.Snap();
      }

      void Destroy()
      {
         try {
            if ( _fp != null )
               _fp.close();
         } catch( IOException e ) {
            cout.printf( "Can't close %s : %s\n", _file, e.getMessage() );
         } finally {
            _fp = null;
            _vw = null;
            _ch = null;
         }
      }


      ///////////////////////////////
      // Access
      ///////////////////////////////
      /**
       * \brief Return true if TradeAccel is configured
       *
       * \return true if TradeAccel is configured
       */
      boolean HasTradeAccel()
      {
         return _hdr.HasTradeAccel();
      }

      /**
       * \brief Return array of tickers; Position in array is index
       * in index file
       *
       * \return Array of tickers
       */
      String[] GetTickers()
      {
         String[] rtn;
         int      i, nt;

         nt  = _recByIdx.length;
         rtn = new String[nt];
         for ( i=0; i<nt; i++ )
            rtn[i] = new String( _recByIdx[i]._tkr );
         return rtn;
      }

      /**
       * \brief Return HH:MM:SS since midnight for index
       */
      String pGetTime( int idx )
      {
         int tm, h, m;

         tm = GetTime( idx );
         h  = tm / 60;
         m  = tm % 60;
         return "".format( "%02d:%02d:00", h, m );
      }

      /**
       * \brief Return index associated w/ HH:MM:SS
       */
      int GetIdx( String tm, boolean bT0 )
      {
         int nMs, cnt, cntMs, nMin, idx, iBeg, iLen;

         nMs   = TimeMs( tm );
         nMin  = ( nMs / 1000 ) / 60;
         iBeg  = _hdr._idxBeg;
         iLen  = _hdr._idxLen;
         cnt   = iBeg * iLen;
         cntMs = cnt * 60 * 1000;
         for ( idx=0; cnt<nMin; idx++, cnt+=iLen );
         if ( bT0 )
            idx -= ( nMin < cnt ) ? 1 : 0;
         else
            idx += ( nMs > cntMs ) ? 1 : 0;
         return idx;
      }

      /**
       * \brief Return millis since midnight
       */
      int TimeMs( String tm )
      {
         String[] kv, hms;
         String   sH, sM, sS, sMs;
         int      h, m, s, ms, nSec;

         /*
          * Support the following:
          *    HH:MM:SS.mmm
          *    HH:MM.mmm
          *    HHMM.mmm
          *    HHMMSS.mmm
          */
         sH  = "0";
         sM  = "0";
         sS  = "0";
         sMs = "0";
         hms = tm.split("\\.");
         kv  = hms[0].split(":");
         switch( kv.length ) {
            case 3:
               sS = kv[2];
               // Fall-through
            case 2:
               sM = kv[1];
               sH = kv[0];
               break;
            default:
               switch( tm.length() ) {
                  case 6:
                     sS = tm.substring( 4,6 );
                     // Fall-through
                  case 4:
                     sM = tm.substring( 2,4 );
                     sH = tm.substring( 0,2 );
                     break;
               }
               break;
         }
         sMs = ( hms.length > 1 ) ? hms[1] : sMs;
         h = m = s = ms = 0;
         try {
            h  = Integer.valueOf( sH  ).intValue();
            m  = Integer.valueOf( sM  ).intValue();
            s  = Integer.valueOf( sS  ).intValue();
            ms = Integer.valueOf( sMs ).intValue();
         } catch( IllegalArgumentException e ) {
            cout.printf( "[%s] ERROR : %s\n", Now(), e.getMessage() );
         }

         // Num seconds

         nSec = ( h * 3600 ) + ( m * 60 ) + s;
         return ( nSec * 1000 ) + ms;
      }

      /**
       * \brief Return number of minutes since midnight for index
       */
      int GetTime( int idx )
      {
         int nMin;

         nMin = ( _hdr._idxBeg + idx ) * _hdr._idxLen;
         return nMin;
      } 

      public void Dump( PrintStream ps )
      {
         TsqRecHdr[] rdb = _recByIdx;
         int         i, nr;

         ps.printf( _hdr.Dump() );
         nr = rdb.length;
         ps.printf( "=====>>>>> %d TsqRecHdr's <<<<<=====\n", nr );
/*
 * OBSOLETE
         ps.printf( "Idx,Ticker,begPos,insPos,trdBeg,trdPos,nUpd,tUpd\n" );
         for ( i=0; i<nr; ps.printf( rdb[i++].Dump() ) );
 */
      }


      ///////////////////////////////
      // Helpers
      ///////////////////////////////
      int _AlignLong()
      {
         int i, pos;

         for ( i=0; ((pos=_vw.position())%8) != 0; _vw.get(), i++ );
         return i;
      }

      String _GetString( int len )
      {
         byte[] bb;

         bb  = new byte[len];
         _vw.get( bb ); 
         return new String( bb ).trim();
      }

      /**
       * \brief struct timeval -> java time 
       */
      long _timeval2javaTime()
      {
         int  tm_sec, tm_usec;
         long jt;

         // Java Time = millis since Jan 1, 1970

         tm_sec  = _vw.getInt();
         tm_usec = _vw.getInt();
         jt  = tm_sec;
         jt *= 1000;
         jt += ( tm_usec / 1000 );
         return jt;
      }


      //////////////////////////////////////////////////
      //
      //     c l a s s   T s q D b H d r
      //
      //////////////////////////////////////////////////
      class TsqDbHdr
      {
         ///////////////////////////
         // Class-wide
         ///////////////////////////
         private static final String ULT_TSQ_001 = "001 UltraTsqDB";
         private static final String ULT_TSQ_002 = "002 UltraTsqDB";

         //////////////////////
         // Instance Members
         //////////////////////
         long    _fileSiz;
         long    _insLoc;
         long    _winPtr;
         String  _version;    // byte[80]
         String  _signature;  // byte[16]
         String  _login;      // byte[16]
         long    _tStart;
         int     _pid;
         boolean _bBigEndian;
         boolean _bPack;
         long    _winSiz;
         int     _pageSize;
         int     _idxBeg;    // Minutes
         int     _idxEnd;    // Minutes
         int     _idxLen;    // Minutes
         int     _numIdx;    // ( _idxEnd - _idxBeg )
         int     _numTkr;
         int     _maxTkr;
         int     _minFree;   // x pageSize
         long    _nUpd;
         long    _tUpd;      // struct timeval
         long[]  _offBeg;    // length = _numIdx : FIRST MSG IN PERIOD
         long[]  _offMax;

         ////////////////////
         // Constructor
         ////////////////////
         TsqDbHdr()
         {
            ByteOrder ord;
            boolean   bBig;

            // Order 1st, then Snap

            _vw.position( _ENDIAN_OFF );
            bBig = ( _vw.get() != 0 ) ? true : false;
            ord  = bBig ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
            _vw.order( ord );
            _offBeg = null;
            _offMax = null;
            Snap();
         }


         ////////////////////
         // Access
         ////////////////////
         boolean HasTradeAccel()
         {
            return !_signature.equals( ULT_TSQ_001 );
         }

         String Dump()
         {
            TsqRecHdr[] rdb = _recByIdx;
            String      s, ps, pu, tkr;
            TsqRecHdr   rec;
            Position    pdb;
            int         i, idx;
            long        pos;

            // Formatted Times

            ps = QuoddFeed.msg.QuoddMsg.pDateTimeMs( _tStart );
            pu = QuoddFeed.msg.QuoddMsg.pDateTimeMs( _tUpd );

            // Formatted Output

            s  = "=====>>>>> TsqDbHdr <<<<<=====\n";
            s += s.format( "   _fileSiz    = %d\n", _fileSiz );
            s += s.format( "   _insLoc     = %d\n", _insLoc );
            s += s.format( "   _winPtr     = %d\n", _winPtr );
            s += s.format( "   _version    = %s\n", _version );
            s += s.format( "   _signature  = %s\n", _signature );
            s += s.format( "   _login      = %s\n", _login );
            s += s.format( "   _tStart     = %s\n", ps );
            s += s.format( "   _pid        = %d\n", _pid );
            s += s.format( "   _bBigEndian = %s\n", s.valueOf( _bBigEndian ) );
            s += s.format( "   _bPack      = %s\n", s.valueOf( _bPack ) );
            s += s.format( "   _winSiz     = %d\n", _winSiz );
            s += s.format( "   _pageSize   = %d\n", _pageSize );
            s += s.format( "   _idxBeg     = %d\n", _idxBeg );
            s += s.format( "   _idxEnd     = %d\n", _idxEnd );
            s += s.format( "   _idxLen     = %d\n", _idxLen );
            s += s.format( "   _numIdx     = %d\n", _numIdx );
            s += s.format( "   _numTkr     = %d\n", _numTkr );
            s += s.format( "   _maxTkr     = %d\n", _maxTkr );
            s += s.format( "   _minFree    = %d\n", _minFree );
            s += s.format( "   _nUpd       = %d\n", _nUpd );
            s += s.format( "   _tUpd       = %s\n", pu );

            // Dump offset up to current time; Find ticker ...

            s += s.format( "   --->>> Offsets <<<---\n" );
            for ( idx=0; idx<_numIdx; idx++ ) {
               if ( (pos=_offBeg[idx]) == 0 )
                  break; 
               pdb  = new Position( idx );
               tkr = "Undefined";
               if ( (rec=pdb.Get( pos )) != null )
                  tkr = rec._tkr;
               ps  = pGetTime( idx );
               s  += s.format( "+++%s = %d {%s}\n", ps, pos, tkr );
            }
            return s;
         }


         ////////////////////
         // Mutator
         ////////////////////
         void Snap()
         {
            int i;

            _vw.position( 0 );
            _fileSiz    = _vw.getLong();
            _insLoc     = _vw.getLong();
            _winPtr     = _vw.getLong();
            _version    = _GetString( 80 );
            _signature  = _GetString( 16 );
            _login      = _GetString( 16 );
            _tStart     = 1000;
            _tStart    *= _vw.getInt();
            _pid        = _vw.getInt();
            _bBigEndian = ( _vw.get() != 0 ) ? true : false;
            _bPack      = ( _vw.get() != 0 ) ? true : false;
            _AlignLong();
            _winSiz     = _vw.getLong();
            _pageSize   = _vw.getInt();
            _idxBeg     = _vw.getInt();
            _idxEnd     = _vw.getInt();
            _idxLen     = _vw.getInt();
            _numIdx     = _vw.getInt();
            _numTkr     = _vw.getInt();
            _maxTkr     = _vw.getInt();
            _minFree    = _vw.getInt();
            _nUpd       = _vw.getLong();
            _tUpd       = _timeval2javaTime();

            // File-wide Index; Do once ...

            if ( _offBeg == null ) {
               _offBeg = new long[_numIdx];
               _offMax = new long[_numIdx];
            }
         }
      }
      // class TsqDbHdr


      //////////////////////////////////////////////////
      //
      //       c l a s s   T s q R e c H d r
      //
      //////////////////////////////////////////////////
      class TsqRecHdr
      {
         //////////////////////
         // Instance Members
         //////////////////////
         int     _idx;
         String  _tkr;    // byte[24]
         int     _dbIdx;
         long    _begPos;
         long    _insPos; // Current insert (WRITE) position
         long    _nUpd;
         long    _tUpd;   // struct timeval
         long    _trdBeg; // Trade Accelerator : GLultTsqDbTrdRec
         long    _trdPos; // Trade Accelerator : GLultTsqDbTrdRec
         int     _nTrade; // Trade Accelerator : GLultTsqDbTrdRec
         long[]  _off;    // length = _numIdx
         long    _curPos; // Current READ position

         ////////////////////
         // Constructor
         ////////////////////
         TsqRecHdr( int idx )
         {
            String fmt;
            int    i;
            long   pos0, pos;

            pos0    = _vw.position();
            _idx    = idx;
            _tkr    = _GetString( 24 );
            _dbIdx  = _vw.getInt();
            _AlignLong();
            _begPos = _vw.getLong();
            _insPos = _vw.getLong();
            _nUpd   = _vw.getLong();
            _tUpd   = _timeval2javaTime();
            _trdBeg = HasTradeAccel() ? _vw.getLong() : 0;
            _trdPos = HasTradeAccel() ? _vw.getLong() : 0;
            _nTrade = HasTradeAccel() ? _vw.getInt()  : 0;
            _AlignLong();
            _off    = new long[_hdr._numIdx];
            pos     = _vw.position();
            for ( i=0; i<_hdr._numIdx; i++ ) {
               _off[i] = _vw.getLong();
               if ( _off[i] == 0 )
                  continue; // for-i
               if ( _hdr._offBeg[i] == 0 )
                  _hdr._offBeg[i] = _off[i];
               _hdr._offBeg[i] = Math.min( _hdr._offBeg[i], _off[i] );
               _hdr._offMax[i] = Math.max( _hdr._offMax[i], _off[i] );
            }
            _curPos = 0;
         }


         ////////////////////
         // Access
         ////////////////////
         String Dump()
         {
            String s, pu;

            // Idx,Ticker,begPos,insPos,trdBeg,trdPos,nUpd,tUpd

            pu = QuoddFeed.msg.QuoddMsg.pDateTimeMs( _tUpd );
            s  = "";
            s += s.format( "%d,%s,", _idx, _tkr );
            s += s.format( "%d,%d,", _begPos, _insPos );
            s += s.format( "%d,%d,", _trdBeg, _trdPos );
            s += s.format( "%d,%s,\n", _nUpd, pu );
            return s;
         }
      }
      // class TsqRecHdr


      //////////////////////////////////////////////////
      //
      //        c l a s s   P o s i t i o n
      //
      //////////////////////////////////////////////////
      class Position
      {
         //////////////////////
         // Instance Members
         //////////////////////
         HashMap<Long,TsqRecHdr> _posDb;
         long                    _off; // Current position

         ////////////////////
         // Constructor
         ////////////////////
         Position( int idx )
         {
            TsqRecHdr[] rdb = _recByIdx;
            TsqRecHdr   rec;
            long        pos;
            int         i;

            // idx == -1 implies _curPos

            _posDb = new HashMap<Long,TsqRecHdr>();
            for ( i=0; i<rdb.length; i++ ) {
               rec = rdb[i];
               pos = ( idx == -1 ) ? rec._curPos : rec._off[idx];
               _posDb.put( pos, rec );
            }
         }

         ////////////////////
         // Access
         ////////////////////
         TsqRecHdr Get( long off )
         {
            if ( _posDb.containsKey( off ) )
               return _posDb.get( off );
            return null;
         }


         ////////////////////
         // Mutator
         ////////////////////
         void Update( TsqRecHdr rec, long oldPos, long newPos )
         {
            TsqRecHdr dup;
            String    fmt;

            // Remove old

            if ( _posDb.containsKey( oldPos ) )
               _posDb.remove( oldPos );
            else {
               fmt = "[%s] NO OFFSET %d : %s\n";
               cout.printf( fmt, Now(), oldPos, rec._tkr );
            }

            // Add new

            if ( _posDb.containsKey( newPos ) ) {
               dup = _posDb.get( newPos );
               fmt = "[%s] DUPLICATE OFFSET %d : %s and %s\n";
               cout.printf( fmt, Now(), newPos, dup._tkr, rec._tkr );
            }
            else
               _posDb.put( newPos, rec );
         }
      }
      // class Position
   }
   // class IndexFile


   //////////////////////////////////////////////////
   //
   //     c l a s s    T S Q F i l e V i e w
   //
   //////////////////////////////////////////////////
   class TSQFileView
   {
      private TSQByteBuffer[] _tdb;
      private int             _lwc;
      private long            _pos;
      private ByteOrder       _ord;

      ////////////////////
      // Constructor
      ////////////////////
      TSQFileView( DataFile dataFile, long off, long len )
      {
         _tdb = dataFile.Map( off, len );
         _lwc = 0;
         _pos = off;
         _ord = _tdb[0]._vw.order();
      }

      void Destroy()
      {
         int i;

         for ( i=0; i<NumBuf(); _tdb[i++].Destroy() );
         _tdb = null;
      }


      ////////////////////
      // Access / Operations
      ////////////////////
      int NumBuf()
      {
         return ( _tdb != null ) ? _tdb.length : 0;
      }

      long position()
      {
         return _pos;
      }

      long length()
      {
         int n;

         n = NumBuf();
         return ( n>0 ) ? _tdb[n-1]._off + _tdb[n-1]._len : 0;
      }

      TSQFileView position( long pos )
      {
         _pos = pos;
         return this;
      }

      byte get() throws IndexOutOfBoundsException
      {
         TSQByteBuffer buf;
         ByteBuffer    vw;
         byte          rtn;
         long          rPos;
         String        err;

         rtn = 0;
         if ( (buf=_buf()) == null ) {
            err = "".format( "No buffer at pos %d", _pos );
            throw new IndexOutOfBoundsException( err );
         }
         vw    = buf._vw;
         rPos  = _pos - buf._off;
         rtn   = vw.get( (int)rPos );
         _pos += 1;
         return rtn;
      }

      short getShort() throws IndexOutOfBoundsException
      {
         TSQByteBuffer buf;
         short         rtn;
         long          rPos, fSz, nL;
         ByteBuffer    bb;
         String        err;

         if ( (buf=_buf()) == null ) {
            err = "".format( "No buffer at pos %d", _pos );
            throw new IndexOutOfBoundsException( err );
         }
         fSz  = 2;
         rPos = _pos - buf._off;
         nL   = buf._len - rPos;
         if ( fSz <= nL ) {
            rtn   = buf._vw.getShort( (int)rPos );
            _pos += fSz;
         }
         else {
            if ( (bb=_getSlice( buf, fSz, nL )) == null ) {
               err = "".format( "No buffer at pos %d", _pos );
               throw new IndexOutOfBoundsException( err );
            }
            rtn = bb.order( _ord ).getShort( 0 );
         }
         return rtn;
      }

      int getInt() throws IndexOutOfBoundsException
      {
         TSQByteBuffer buf;
         int           rtn;
         long          rPos, fSz, nL;
         ByteBuffer    bb;
         String        err;

         if ( (buf=_buf()) == null ) {
            err = "".format( "No buffer at pos %d", _pos );
            throw new IndexOutOfBoundsException( err );
         }
         fSz  = 4;
         rPos = _pos - buf._off;
         nL   = buf._len - rPos;
         if ( fSz <= nL ) {
            rtn   = buf._vw.getInt( (int)rPos );
            _pos += fSz;
         }
         else {
            if ( (bb=_getSlice( buf, fSz, nL )) == null ) {
               err = "".format( "No buffer at pos %d", _pos );
               throw new IndexOutOfBoundsException( err );
            }
            rtn = bb.order( _ord ).getInt( 0 );
         }
         return rtn;
      }

      long getLong() throws IndexOutOfBoundsException
      {
         TSQByteBuffer buf;
         long          rtn;
         long          rPos, fSz, nL;
         ByteBuffer    bb;
         String        err;

         if ( (buf=_buf()) == null ) {
            err = "".format( "No buffer at pos %d", _pos );
            throw new IndexOutOfBoundsException( err );
         }
         fSz  = 8;
         rPos = _pos - buf._off;
         nL   = buf._len - rPos;
         if ( fSz <= nL ) {
            rtn   = buf._vw.getLong( (int)rPos );
            _pos += fSz;
         }
         else {
            if ( (bb=_getSlice( buf, fSz, nL )) == null ) {
               err = "".format( "No buffer at pos %d", _pos );
               throw new IndexOutOfBoundsException( err );
            }
            rtn = bb.order( _ord ).getLong( 0 );
         }
         return rtn;
      }

      ByteBuffer get( byte[] dst ) throws IndexOutOfBoundsException
      {
         TSQByteBuffer buf;
         long          rPos, fSz, nL;
         ByteBuffer    rtn;
         String        err;

         if ( (buf=_buf()) == null ) {
            err = "".format( "No buffer at pos %d", _pos );
            throw new IndexOutOfBoundsException( err );
         }
         fSz  = dst.length;
         rPos = _pos - buf._off;
         nL   = buf._len - rPos;
         if ( fSz <= nL ) {
            buf._vw.position( (int)rPos );
            rtn   = buf._vw.get( dst );
// cout.printf( "get( %d,%d ) nL=%d from len=%d\n", rPos, fSz, nL, buf._len );
            _pos += fSz;
         }
         else {
            if ( (rtn=_getSlice( buf, fSz, nL )) == null ) {
               err = "".format( "No buffer at pos %d", _pos );
               throw new IndexOutOfBoundsException( err );
            }
            rtn.get( dst );
         }
         return rtn;
      }

      TSQFileView order( ByteOrder bo )
      {
         int i;

         for ( i=0; i<_tdb.length; _tdb[i++]._vw.order( bo ) );
         _ord = bo;
         return this;
      }


      ////////////////////
      // Helpers
      ////////////////////
      TSQByteBuffer _buf()
      {
         int i, nt;

         nt  = _tdb.length;
         for ( i=_lwc; i<nt; i++ ) {
            if ( _tdb[i].Contains( _pos ) ) {
               _lwc = i;
               return _tdb[i];
            }
         }
         for ( i=0; i<_lwc; i++ ) {
            if ( _tdb[i].Contains( _pos ) ) {
               _lwc = i;
               return _tdb[i];
            }
         }
         return null;
      }

      ByteBuffer _getSlice( TSQByteBuffer buf, long fSz, long nL )
      {
         long       rPos, i;
         ByteBuffer bb;

         rPos = _pos - buf._off;
         nL   = buf._len - rPos;
         bb = ByteBuffer.allocate( (int)fSz );
         for ( i=0; i<nL; bb.put( (int)i++, buf._vw.get( (int)rPos++ ) ) );
         _pos += nL;
         if ( (buf=_buf()) != null ) {
            rPos = 0;
            for ( ; i<fSz; bb.put( (int)i++, buf._vw.get( (int)rPos++ ) ) );
            _pos += ( fSz-nL );
            return bb;
         }
         return null;
      }
   }
   // class TSQFileView


   //////////////////////////////////////////////////
   //
   //     c l a s s    T S Q B y t e B u f f e r
   //
   //////////////////////////////////////////////////
   class TSQByteBuffer
   {
      private MappedByteBuffer _vw;
      private long             _off;
      private long             _len;
      private long             _eob;

      ////////////////////
      // Constructor
      ////////////////////
      TSQByteBuffer( MappedByteBuffer vw, long off, long len )
      {
         _vw  = vw;
         _off = off;
         _len = len;
         _eob = _off + _len;
      }

      void Destroy()
      {
         _vw = null;
         System.gc();
      }


      ////////////////////
      // Operations
      ////////////////////
      boolean Contains( long pos )
      {
         return InRange( _off, pos, _eob-1 );
      }
   }
   // class TSQByteBuffer


   //////////////////////////////////////////////////
   //
   //        c l a s s    D a t a F i l e
   //
   //////////////////////////////////////////////////
   class DataFile
   {
      ///////////////////////////
      // Class-wide
      ///////////////////////////
      private static final byte _mtTSQ_TRADE = 0x7e; // ~
      private static final int  _PACK_BYTE   = 0x80;
      private static final int  _PACK_MASK   = 0x7fffffff;
      private static final int  _TQS_TRD_SIZ = 9;
      private static final int  _TQS_TRD_OFF = 18;

      ////////////////////
      // Instance Members
      ////////////////////
      String           _file;
      IndexFile        _idx;
      String           _flags;
      FileChannel      _ch;
      RandomAccessFile _fp;
      long             _fpSz;
      TSQFileView      _vw;

      ///////////////////////////////
      // Constructor
      ///////////////////////////////
      DataFile( String file, IndexFile idx )
      {
         // 1) Init guts

         _file  = file;
         _idx   = idx;
         _flags = "r";
         _ch    = null;
         _fp    = null;
         _fpSz  = 0;
         _vw    = null;

         // 2) Open File

         try {
            _fp   = new RandomAccessFile( _file, _flags );
         } catch( FileNotFoundException e ) {
            cout.printf( "Can't open %s : %s\n", _file, e.getMessage() );
            _fp = null;
            return;
         }
         try {
            _fpSz = _fp.length();
         } catch( IOException e ) {
            cout.printf( "Can't stat %s : %s\n", _file, e.getMessage() );
            _fp = null;
            return;
         }
         _ch = _fp.getChannel();
      }

      void Destroy()
      {
         try {
            _vw.Destroy();
            if ( _fp != null )
               _fp.close();
         } catch( IOException e ) {
            cout.printf( "Can't close %s : %s\n", _file, e.getMessage() );
         } finally {
            _vw = null;
            _fp = null;
            _ch = null;
         }
      }


      ///////////////////////////////
      // Mapping Range 
      ///////////////////////////////
      /**
       * \brief Maps the data file for a specific time range.  When pumping
       * large number of tickers - Or ALL tickers - it is more efficient to
       * map once rather than once per ticker.
       *
       * \param ix0 - Starting index
       * \param ix1 - Starting index
       */
      public void SetMapRange( int ix0, int ix1 )
      {
         long off0, off1;
         int  iMax;

         // Offset / Length

         iMax = _idx._hdr._numIdx;
         off0 = InRange( 0, ix0, iMax-1 ) ? _idx._hdr._offBeg[ix0] : 0;
         off1 = _idx._hdr._insLoc;
         if  ( InRange( 0, ix1, iMax-1 ) ) {
            off1 = _idx._hdr._offMax[ix1];
            off1 = ( off1 == 0 ) ? _idx._hdr._insLoc : off1;
         }
         off1 = WithinRange( 0, off1, _fpSz );

         // Map / Store

         ClearMapRange();
         _vw = new TSQFileView( this, off0, off1-off0 );
      }

      /**
       * \brief Clear the map range set in SetMapRange().
       */
      public void ClearMapRange()
      {
         _vw = null;
      }


      ///////////////////////////////
      // Operations
      ///////////////////////////////
      TSQByteBuffer[] Map( long off, long len )
      {
         FileChannel.MapMode mode;
         TSQByteBuffer[]     rtn;
         MappedByteBuffer    vw;
         String              fmt, exc;
         long                mSz, o0, o1;
         int                 i, n;

         // Quick Check

         if ( ( _vw != null ) && ( (n=_vw.NumBuf()) > 0 ) ) {
            rtn = _vw._tdb;
            o0  = rtn[0]._off;
            o1  = rtn[n-1]._off + rtn[n-1]._len;
            if ( ( o0 == off ) && ( (o1-o0) >= len ) ) 
               return rtn;
         }

         // OK to map it in ...

         mode = FileChannel.MapMode.READ_ONLY;
         rtn  = null;
         System.gc();
         n   = (int)( len / (long)Integer.MAX_VALUE );
         n  += 1;
         rtn = new TSQByteBuffer[n];
         for ( i=0; i<n; i++ ) {
            mSz = Math.min( Integer.MAX_VALUE, len );
            try {
               vw = _ch.map( mode, off, mSz );
            } catch( IOException e ) {
               exc = e.getMessage();
               fmt = "map[%d]( %d,%d ) IOException : %s";
               OnError( "".format( fmt, i, off, len, exc ) );
               return rtn;
            } catch( IllegalArgumentException e ) {
               exc = e.getMessage();
               fmt = "map[%d]( %d,%d ) IllegalArgumentException : %s";
               OnError( "".format( fmt, i, off, len, exc ) );
               return rtn;
            }
            rtn[i] = new TSQByteBuffer( vw, off, mSz );
            off   += mSz;
            len   -= mSz;
         }
         return rtn;
      }

      long _Pump( TSQPump    pmp, 
                  String     t0,
                  String     t1,
                  int        idx0, 
                  int        idx1, 
                  boolean    bSort, 
                  IUpdate    iUpd )
      {
         HashMap<Long,QuoddMsg> map;
         TreeMap<Long,QuoddMsg> srt;
         TSQFileView            vw;
         QuoddMsg               qm;
         String                 fmt;
         long                   off0, off1, mSz, fSz, tot;
         int                    ix0, ix1, iMax;

         // Collections

         map = bSort ? new HashMap<Long,QuoddMsg>() : null;
         srt = bSort ? new TreeMap<Long,QuoddMsg>() : null;
         if ( _CanLog( _PUMP_LOG_PUMP ) ) {
            fmt = "[%s] _Pump( %d,%d ) of %d\n";
            cout.printf( fmt, Now(), idx0, idx1, _idx._hdr._numIdx );
         }
         /*
          * Map 1 index at a time
          *    ASSUME : Each index < Integer.MAX_VALUE
          */
         pmp.OnQueryStart( bSort );
         iMax = _idx._hdr._numIdx;
         ix0  = WithinRange( -1, idx0, idx1 );
//         ix1  = WithinRange( ix0, idx1, iMax-1 );
         ix1  = idx1;
         off0 = ( ix0 >= 0 ) ? _idx._hdr._offBeg[ix0] : 0;
         off1 = _idx._hdr._insLoc;
         if  ( InRange( 0, ix1, iMax-1 ) ) {
            off1 = _idx._hdr._offMax[ix1];
            off1 = ( off1 == 0 ) ? _idx._hdr._insLoc : off1;
         }
         mSz = ( off1 - off0 ) + _MAX_MSGSZ;
         try {
            fSz = _fp.length();
         } catch( IOException e ) {
            return 0;
         }
         if ( ( off0 > fSz ) || ( off0+mSz > fSz ) )
            return 0;
         vw = new TSQFileView( this, off0, mSz );
         if ( vw.NumBuf() == 0 )
            return 0;
         pmp.OnFileMapped( _file, _idx.pGetTime( ix0 ), off0, mSz );

         // Build ...

         tot = _Build( vw, 
                       (long)_idx.TimeMs( t0 ), 
                       (long)_idx.TimeMs( t1 ), 
                       off0, 
                       ix1, 
                       map, 
                       iUpd );
         pmp.OnQueryComplete( tot );
         if ( tot == 0 || !bSort )
            return tot;

         // Build sorted TreeMap / Dispatch

         for ( Map.Entry<Long,QuoddMsg> et : map.entrySet() ) {
            srt.put( et.getKey(), et.getValue() );
         }
         pmp.OnSortComplete( srt.size() );
         for ( Map.Entry<Long,QuoddMsg> et : srt.entrySet() ) {
            qm = et.getValue();
            iUpd.OnMsg( qm.tkr(), qm, false );
         }
         tot = map.size();
         return tot;
      }


      ///////////////////////////////
      // Helpers
      ///////////////////////////////
      long _Build( TSQFileView            vw,
                   long                   tMs0,
                   long                   tMs1,
                   long                   off0, 
                   int                    ix1,
                   HashMap<Long,QuoddMsg> map,
                   IUpdate                iUpd )
      {
         String   tkr, fmt, pt, pb;
         long     rtn, mSz, p0, p1, pos, mOff, qt;
         int      i, j, nr, nm, iMax;
         TsqDbMsg tQte, msg;
         TsqDbTrd tTrd;
         Factory  fct;
         QuoddMsg qm, qz;
         char     mt, mt2;
         boolean  bOK, bSort;

         // Pump away ...

         bSort = ( map != null );
         fct   = new Factory();
         qz    = new QuoddMsg();
         nr    = _idx._recByIdx.length;
         iMax  = _idx._hdr._numIdx;
         qm    = null;
         tQte  = new TsqDbMsg( vw );
         tTrd  = new TsqDbTrd( vw );
         for ( i=0,rtn=0; i<nr; i++ ) {
            tkr = _idx._recByIdx[i]._tkr;
            if ( !IsWatched( tkr ) )
               continue; // for-i
            p0  = off0;
            try {
               if  ( InRange( 0, ix1, iMax-1 ) )
                  p1  = _idx._recByIdx[i]._off[ix1];
               else
                  p1  = _idx._recByIdx[i]._insPos;
            } catch( ArrayIndexOutOfBoundsException ex ) {
               p1  = _idx._recByIdx[i]._insPos;
               fmt = "i=%d; p0=%d; ix1=%d; numIdx=%d\n";
               cout.printf( fmt, i, p0, ix1, iMax );
            }
            pos = p1;
            bOK = ( pos > p0 );
            qm  = null;
            /*
             * Walk thru TSQFileView with each ticker
             */
            OnTickerStart( tkr, i, nr );
            for ( j=0,nm=0; bOK; j++ ) {
               vw.position( pos );
               tQte.Set( tkr, i, nr, qm );
               if ( tQte.IsValid() && tQte.IsTrade() ) {
                  vw.position( pos );
                  tTrd.Set( tkr, i, nr, qm );
                  msg = tTrd;
               }
               else
                  msg = tQte;
               mOff = msg._last;
               pos -= mOff;
               bOK  = ( pos > p0 ) && ( mOff != 0 );
               if ( _CanLog( _PUMP_LOG_NEXT ) ) {
                  fmt = "   %s pos=%d; last=%d; nPos=%d; Valid=%s\n";
                  pt  = ( qm != null ) ? qm.pTimeMs() : "00:00:00.000";
                  pb  = msg.IsValid() ? "true" : "false";
                  cout.printf( fmt, pt, pos+mOff, mOff, pos, pb );
               }
               if ( !msg.IsValid() )
                  continue; // for-j
               qm  = fct.Parse( msg._msg, 0, msg._len );
               qt  = qm.MsgTime();
               if ( ( qt != 0 ) && ( qt < tMs0 ) ) {
                  if ( _CanLog( _PUMP_LOG_NEXT ) )
                     cout.printf( "   qt=%d < tMs0=%d\n", qt, tMs0 );
                  break; // for-j
               }
               if ( !InRange( tMs0, qm.MsgTime(), tMs1 ) )
                  continue; // for-j
               mt  = qm.mt();
               mt2 = qm.mtSub();
               qm.SetStreamID( qz );
               qm.SetTkr( tkr );
               if ( rtn > 0 ) {  // Drop 1st one ...
                  if ( bSort )
                     map.put( pos+mOff, qm.clone() );
                  else 
                     iUpd.OnMsg( qm.tkr(), qm, false );
               }
               rtn += 1;
               nm  += 1;
            }
            OnTickerEnd( tkr, nm );
         }
         return ( rtn > 0 ) ? rtn-1 : rtn;
      }


      //////////////////////////////////////////////////
      //
      //         c l a s s   T s q D b M s g
      //
      //////////////////////////////////////////////////
      class TsqDbMsg
      {
         //////////////////////
         // Instance Members
         //////////////////////
         TSQFileView _vw;
         long        _pos;
         int         _1st;
         boolean     _bPack;
         long        _last;
         int         _len;
         byte        _mt;
         byte        _mtSub;
         long        _mPos;
         byte[]      _msg;

         ///////////////////////////////
         // Constructor
         ///////////////////////////////
         TsqDbMsg( TSQFileView vw )
         {
            _vw    = vw;
            _pos   = 0;
            _1st   = 0;
            _bPack = false;
            _last  = 0;
            _len   = 0;
            _mt    = 0;
            _mtSub = 0;
            _mPos  = 0;
            _msg   = null;
         }


         ///////////////////////////////
         // Access
         ///////////////////////////////
         byte mt()
         {
            return _mt;
         }

         boolean IsPacked()
         {
            return _bPack;
         }

         boolean IsTrade()
         {
            return( _mt == _mtTSQ_TRADE );
         }

         boolean IsValid()
         {
            return( _len > 0 );
         }

         ///////////////////////////////
         // Operations
         ///////////////////////////////
         void Set( String tkr, int n, int tot, QuoddMsg qm )
         {
            /*
             * Polymorphic : _last[0] & _PACK_BYTE : int; Else long
             */
            _pos   = _vw.position();
            _1st   = (int)_vw.get();
            _bPack = ( ( _1st & _PACK_BYTE ) == _PACK_BYTE );
            _vw.position( _pos );
            if ( _bPack )
               _last = (long)( _vw.getInt() & _PACK_MASK );
            else
               _last = _vw.getLong();
            /*
             * QuoddMsg
             */
            _mPos  = _vw.position();
            _len   = (int)_vw.getShort();
            _mt    = _vw.get();
            _mtSub = _vw.get();
            if ( !IsValid() ) 
               return;
            _msg   = new byte[_len];
            _vw.position( _mPos );
            _vw.get( _msg );
         }
      }
      // class TsqDbMsg


      //////////////////////////////////////////////////
      //
      //       c l a s s   T s q D b T r d
      //
      //////////////////////////////////////////////////
      class TsqDbTrd extends TsqDbMsg
      {
         //////////////////////
         // Instance Members
         //////////////////////
         long _lastTrade;
         byte _mtReal;  /* TsqDbMsg._mt == _mtTSQ_TRADE */

         ///////////////////////////////
         // Constructor
         ///////////////////////////////
         TsqDbTrd( TSQFileView vw )
         {
            super( vw );
            _lastTrade = 0;
            _mtReal    = 0;
         }


         ///////////////////////////////
         // Access
         ///////////////////////////////
         @Override
         byte mt()
         {
            return _mtReal;
         }

         ///////////////////////////////
         // Operations
         ///////////////////////////////
         void Set( String tkr, int n, int tot, QuoddMsg qm )
         {
            ByteBuffer bb;
            byte[]     payload;
            int        i, pLen;

            super.Set( tkr, n, tot, qm );
            /*
             * Full TSQ message layout:
             *    + GLultTsqDbMsg hdr
             *    + QuoddMsg header (18 bytes)
             *    + GLultTsqDbTrd hdr : _lastTrade  + _mtReal
             *    + UltraFeed business payload
             * Soooooo, _msg is sized properly, but bytes 18-26 are
             * _lastTrade and _mtReal; Plus we need to read in 9 
             * bytes from EOM and shift _msg appropriately
             */
            bb         = ByteBuffer.wrap( _msg );
            _lastTrade = bb.getLong( _TQS_TRD_OFF );
            _mtReal    = bb.get( _TQS_TRD_OFF+8 );
            _msg[2]    = _mtReal;

            // Splice 'er in ...

            pLen    = _len + _TQS_TRD_SIZ;
            payload = new byte[pLen];
            _vw.position( _mPos );
            _vw.get( payload );
            for ( i=_TQS_TRD_OFF; i<_len; i++ )
               _msg[i] = payload[i+_TQS_TRD_SIZ];
         }

      }
      // class TsqDbTrd8
   }
   // class DataFile


   //////////////////////////////////////////////////
   //
   //       c l a s s   P u m p T h r e a d
   //
   //////////////////////////////////////////////////
   class PumpThread implements Runnable
   {
      //////////////////////
      // Instance Members
      //////////////////////
      TSQPump _pmp;
      String  _t0;
      String  _t1;
      int     _idx0;
      int     _idx1;
      boolean _bSort;
      IUpdate _iUpd;
      int     _thrID;
      Thread  _thr;
      long    _nPump;

      ///////////////////////////////
      // Constructor
      ///////////////////////////////
      PumpThread( TSQPump pmp,
                  String  t0,
                  String  t1,
                  int     idx0,
                  int     idx1,
                  boolean bSort,
                  IUpdate iUpd,
                  int     thrID ) 
      {
         _pmp      = pmp;
         _t0       = t0;
         _t1       = t1;
         _idx0     = idx0;
         _idx1     = idx1;
         _bSort    = bSort;
         _iUpd     = iUpd;
         _thrID    = thrID;
         _thr      = null;
         _nPump    = -1;
      }


      ///////////////////////////////
      // Operations
      ///////////////////////////////
      public void Start()
      {
         if ( _thr == null ) {
            _thr  = new Thread( this, "PumpThread" );
            _thr.start();
         }
      }

      public long Wait()
      {
         String err;

         try {
            synchronized( this ) {
               if ( _nPump == -1 )
                  this.wait();
            }
         } catch( InterruptedException e ) {
            err = e.getMessage();
         }
         return _nPump;
      }


      ///////////////////////////////
      // Runnable Interface
      ///////////////////////////////
      public void run()
      {
         long nPmp;

         // 1) Kick it

         nPmp = _pmp._data._Pump( _pmp, 
                                  _t0,
                                  _t1,
                                  _idx0, 
                                  _idx1, 
                                  _bSort, 
                                  _iUpd );

         // 2) Free waiting thread

         synchronized( this ) {
            _nPump = nPmp;
            this.notify();
         }
      }
   }
   // class PumpThread
}
