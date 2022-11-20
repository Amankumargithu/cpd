/******************************************************************************
*
*  TSQ.java
*     Proxy for SpryWareHTTP server
*
*  REVISION HISTORY:
*      5 FEB 2014 jcs  Created.
*     15 MAY 2014 jcs  Build 76: _bKid; _numRec
*
*  (c) 2011-2014 Quodd Financial
*******************************************************************************/
package QuoddFeed.MD;

import java.io.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
//
//                c l a s s    T S Q
//
/////////////////////////////////////////////////////////////////
public class TSQ extends UltraChan
{
   private String              _tkr;
   private String              _tBeg;
   private String              _tEnd;
   private String              _fltr;
   private boolean             _bKid;
   private int                 _numRec;
   private String              _bboEx;
   private String              _arg;
   private Object              _eof;
   private ArrayList<QuoddMsg> _msgs;
   private PrintStream         cout;

   //////////////////////
   // Constructor
   //////////////////////
   public TSQ( String  tkr, 
               String  tBeg, 
               String  tEnd, 
               String  fltr,
               boolean bKid,
               int     numRec,
               String  bboEx )
   {
      super( UCconfig.Hostname(), 
             UCconfig.Port(),
             UCconfig.Username(),
             UCconfig.Password(),
             false );
      _tkr    = tkr;
      _tBeg   = tBeg;
      _tEnd   = tEnd;
      _fltr   = fltr;
      _bKid   = bKid;
      _numRec = numRec;
      _bboEx  = bboEx;
      _arg    = "User-defined closure for _TSQQuery";
      _eof    = new Object();
      _msgs   = new ArrayList<QuoddMsg>();
      cout    = System.out;
      Start();
   }


   //////////////////////
   // Operations
   //////////////////////
   /**
    * Query Time/Sales/Quote DB
    */
   public QuoddMsg[] Query()
   {
      QuoddMsg[] mdb;
      int        i, nm;

      // Query and Wait for EOF

      QueryTSQ( _tkr, _tBeg, _tEnd, _fltr, _bKid, _numRec, _bboEx, _arg );
      try {
         synchronized( _eof ) {
            _eof.wait();
         }
      } catch( InterruptedException e ) {
         cout.printf( e.getMessage() );
      }
      StopQ();

      // Build response

      nm  = _msgs.size();
      mdb = new QuoddMsg[nm];
      for ( i=0; i<nm; mdb[i] = _msgs.get( i++ ) );
      return mdb;
   }


   //////////////////////
   // UltraChan Interface
   //////////////////////
   /**
    * Called when we connect to UltraCache.  We do nothing
    */
   public void OnConnect()
   {
      return;
   }

   /**
    * Called when we disconnect from UltraCache.  We do nothing here.
    */
   public void OnDisconnect()
   {
      return;
   }


   //////////////////////
   // IUpdate Interface
   //////////////////////
   /**
    * Called when an unknown QuoddMsg is received.  Should never be called.
    */
   public void OnQuoddMsg( String StreamName, QuoddMsg qm )
   {
      _OnEOM( qm );
   }

   /**
    * Called when a data stream is closed.
    */
   public void OnStatus( String StreamName, Status sts )
   {
      _OnEOM( sts );
   }

   /**
    * Called when an EQQuote update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQQuote q )
   {
      _msgs.add( q.clone() );
   }

   /**
    * Called when an EQBbo update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQBbo q )
   {
      _msgs.add( q.clone() );
   }

   /**
    * Called when an EQQuoteMM update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQQuoteMM q )
   {
      _msgs.add( q.clone() );
   }

   /**
    * Called when an EQBboMM update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQBboMM q )
   {
      _msgs.add( q.clone() );
   }

   /**
    * Called when an EQTrade update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQTrade trd )
   {
      _msgs.add( trd.clone() );
   }

   /**
    * Called when an EQTradeSts update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQTradeSts sts )
   {
      _msgs.add( sts.clone() );
   }

   /**
    * Called when an EQLimitUpDn update is received.
    */
   public void OnUpdate( String StreamName, EQLimitUpDn lim )
   {
      _msgs.add( lim.clone() );
   }

   /**
    * Called when an OPQuote update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPQuote q )
   {
      _msgs.add( q.clone() );
   }

   /**
    * Called when an OPBbo update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPBbo q )
   {
      _msgs.add( q.clone() );
   }

   /**
    * Called when an OPTrade update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPTrade trd )
   {
      _msgs.add( trd.clone() );
   }


   //////////////////////
   // Helpers
   //////////////////////
   private void _OnEOM( QuoddMsg m )
   {
      _msgs.add( m );
      synchronized( _eof ) {
         _eof.notify();
      }
   }
}
