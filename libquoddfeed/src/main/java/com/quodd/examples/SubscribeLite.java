/******************************************************************************
*
*  SubscribeLite.java
*     Stream equities
*
*  REVISION HISTORY:
*     16 JUL 2016 jcs  Created (from Subscribe.java).
*
*  (c) 2011-2016 Quodd Financial
*******************************************************************************/
package com.quodd.examples;

import java.io.*;
import java.util.*;
import QuoddFeed.msg.*;
import QuoddFeed.MD.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
//
//               c l a s s   S u b s c r i b e
//
/////////////////////////////////////////////////////////////////

/**
 * This example shows you how to open a subscription stream from UltraCache
 * via the {@link QuoddFeed.util.UltraChan#Subscribe(String,Object)} method
 * and proces streaming market data via the 
 * {@link QuoddFeed.util.UltraChan#OnUpdate} callbacks.
 */
public class SubscribeLite extends UltraChan
{
   private boolean     _bDump;
   private PrintStream cout;

   //////////////////////
   // Constructor
   //////////////////////
   SubscribeLite( String   uHost,
                  int      uPort,
                  String   uUsr,
                  String   uPwd,
                  boolean  bDump )
   {
      super( uHost, uPort, uUsr, uPwd, false );
      _bDump = bDump;
      cout   = System.out;
   }


   //////////////////////
   // Operations
   //////////////////////
   /**
    * This method subscribes to all tickers.
    */
   public void OpenAll()
   {
      int i;

   }


   //////////////////////
   // UltraChan Interface
   //////////////////////
   /**
    * Called when we connect to UltraCache.  We do nothing
    */
   public void OnConnect()
   {
      cout.printf( "CONNECT %s:%d\n", uHost(), uPort() );
   }

   /**
    * Called when we disconnect from UltraCache.  We do nothing here.
    */
   public void OnDisconnect()
   {
      cout.printf( "DISCONNECT %s:%d\n", uHost(), uPort() );
   }

   /**
    * Callback invoked when session message is received
    *
    * @param txt Textual string
    * @param bUP true if session is UP
    */
   public void OnSession( String txt, boolean bUP )
   {
      cout.printf( "OnSession( %s ) = %s\n", txt, bUP ? "UP" : "DOWN" );
   }


   //////////////////////
   // IUpdate Interface
   //////////////////////
   /**
    * Called when an unknown QuoddMsg is received.  Should never be called.
    */
   public void OnQuoddMsg( String StreamName, QuoddMsg qm )
   {
      if ( _bDump )
         cout.printf( qm.Dump() );
   }

   /**
    * Called when a data stream is closed.
    */
   public void OnStatus( String StreamName, Status sts )
   {
      if ( _bDump )
         cout.print( sts.Dump() );
   }

   /**
    * Called when an UltraCache Tracer msg arrives
    */
   public void OnTrace( String StreamName, Tracer trc )
   {
      cout.print( trc.Dump() );
   }

   /**
    * Called when an UltraCache status update arrives
    */
   public void OnStatus( UCStatus sts )
   {
      if ( _bDump )
         cout.print( sts.Dump() );
   }

   /**
    * Called when an Initial Image is received for a ticker.
    */
   public void OnImage( String StreamName, Image img )
   {
      if ( _bDump )
         cout.print( img.Dump() );
   }

   /**
    * Called when an EQBbo update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQBbo q )
   {
      if ( _bDump )
         cout.printf( q.Dump() );
   }

   /**
    * Called when an EQBboMM update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQBboMM q )
   {
      if ( _bDump )
         cout.printf( q.Dump() );
   }

   /**
    * Called when an EQQuote update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQQuote q )
   {
      if ( _bDump )
         cout.print( q.Dump() );
   }

   /**
    * Called when an EQQuoteMM update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQQuoteMM q )
   {
      if ( _bDump )
         cout.print( q.Dump() );
   }

   /**
    * Called when an EQTrade update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQTrade trd )
   {
      if ( _bDump )
         cout.print( trd.Dump() );
   }

   /**
    * Called when an EQTradeSts update is received for an equity ticker.
    */
   public void OnUpdate( String StreamName, EQTradeSts sts )
   {
      if ( _bDump )
         cout.print( sts.Dump() );
   }

   /**
    * Called when an EQMktStats update is received.
    */
   public void OnUpdate( String StreamName, EQMktStats st )
   {
      if ( _bDump )
         cout.print( st.Dump() );
   }

   /**
    * Called when an EQLimitUpDn update is received.
    */
   public void OnUpdate( String StreamName, EQLimitUpDn lim )
   {
      if ( _bDump )
         cout.print( lim.Dump() );
   }

   /**
    * Called when an OPQuote update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPQuote q )
   {
      if ( _bDump )
         cout.print( q.Dump() );
   }

   /**
    * Called when an OPBbo update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPBbo q )
   {
      if ( _bDump )
         cout.print( q.Dump() );
   }

   /**
    * Called when an OPTrade update is received for an option ticker.
    */
   public void OnUpdate( String StreamName, OPTrade trd )
   {
      if ( _bDump )
         cout.print( trd.Dump() );
   }

   /**
    * Called when an IDXValue update is received for an index ticker.
    */
   public void OnUpdate( String StreamName, IDXValue ix )
   {
      if ( _bDump )
         cout.print( ix.Dump() );
   }

   /**
    * Called when an IDXSummary update is received for an index ticker.
    */
   public void OnUpdate( String StreamName, IDXSummary q )
   {
      if ( _bDump )
         cout.print( q.Dump() );
   }

   /**
    * Called when FUNDnav message is received
    */
   public void OnUpdate( String StreamName, FUNDnav q )
   {
      if ( _bDump )
         cout.printf( q.Dump() );
   }

   /**
    * Called when FUTRTrade message is received
    */
   public void OnUpdate( String StreamName, FUTRTrade trd )
   {
      if ( _bDump )
         cout.print( trd.Dump() );
   }

   /**
    * Called when an Futures Summary is received.
    */
   public void OnUpdate( String StreamName, FUTRSumm q )
   {
      if ( _bDump )
         cout.print( q.Dump() );
   }

   /**
    * Called when an Futures HiLoLast or OpenInt is received.
    */
   public void OnUpdate( String StreamName, FUTRMisc q )
   {
      if ( _bDump )
         cout.print( q.Dump() );
   }

   /**
    * Called when FUTRQuote message is received
    */
   public void OnUpdate( String StreamName, FUTRQuote qte )
   {
      if ( _bDump )
         cout.print( qte.Dump() );
   }

   /**
    * Called when an BONDQuote update is received for an Bond ticker.
    */
   public void OnUpdate( String StreamName, BONDQuote q )
   {
      if ( _bDump )
         cout.print( q.Dump() );
   }

   /**
    * Called when an BONDTrade update is received for an Bond ticker
    */
   public void OnUpdate( String StreamName, BONDTrade trd )
   {
      if ( _bDump )
         cout.print( trd.Dump() );
   }

   /**
    * Called when we UltraCache sends us the options chain response formatted
    * as a BlobList.  We dump to cout.
    */
   public void OnBlobList( String exch, BlobList b )
   {
      cout.printf( b.Dump() );
   }

   /**
    * Called when we UltraCache sends us the options chain response formatted
    * as a BlobTable.  We dump to cout.
    */
   public void OnBlobTable( String exch, BlobTable b )
   {
      cout.printf( b.Dump() );
   }


   //////////////////////
   // Class-wide
   //////////////////////
   public static String[] TickersFromFile( String[] tkrs )
   {
      String[]        rtn;
      String          line;
      BufferedReader  buf;
      HashSet<String> hdb;
      int             i, n;

      /*
       * Allow user to pass comma-separated list of flat ASCII FILES
       * containing tickers.  If not, then return the array of tkrs.
       */
      try {
         buf = new BufferedReader( new FileReader( tkrs[0] ) );
         hdb = new HashSet<String>();
         for ( ; (line=buf.readLine()) != null; hdb.add( line ) );
         buf.close();
         n   = hdb.size();
         rtn = new String[n];
         i   = 0;
         for ( String tkr : hdb ) 
            rtn[i++] = tkr;
      } catch( Exception e ) {
         rtn = tkrs;
      }
      return rtn;
   }


   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      SubscribeLite cpd;
      String        host, usr, pwd;
      String[]      kv;
      Scanner       sc;
      int           i, argc, port;
      boolean       bDmp;

      // Quick check

      argc = args.length;
      if ( ( argc > 0 ) && args[0].equals( "--version" ) ) {
         System.out.printf( "%s\n", QuoddMsg.Version() );
         return;
      }
      if ( ( argc > 0 ) && args[0].equals( "--config" ) ) {
         System.out.printf( "%s\n", UltraChan.Config() );
         return;
      }

      /*
       * Defaults configuration:
       *    1) Tickers : 3 well-known names
       *    2) UC Host : UC_HOST environment variable, else "localhost"
       *    3) UC Port : UC_PORT environment variable, else 4321
       *    4) UC User : UC_USERNAME environment variable, else USER env varb
       *    5) UC Pwd  : UC_PASSWORD environment variable, else "no_password"
       *    6) Dump    : NO
       */
      String[] tkrs = { "AAPL", "IBM", "XLE" };

      host = UCconfig.Hostname( "localhost" );
      port = UCconfig.Port( 4321 );
      usr  = UCconfig.Username( UCconfig.GetEnv( "USER" ) );
      pwd  = UCconfig.Password( "no_password" );
      bDmp = false;

      /*
       * Command-line Arguments:
       *    The 1st argument is a comma-separated list of tickers or 
       *    FILES containing names of ticker to consume. 
       *
       *    The remaining (2nd+) arguments are:
       *    - bDump   : true to dump messages to console
       *    - SVR=val : UC host:port to connect to
       *    - USR=val : UC username
       *    - PWD=val : UC password
       */
      tkrs  = ( argc > 0 ) ? args[0].split(",") : tkrs;
      for ( i=1; i<argc; i++ ) {
         bDmp |= args[i].equals( "bDump" );
         try {
            kv = args[i].split("=");
            if ( kv[0] == "SVR" ) {
               host = kv[1].split(":")[0];
               port = Integer.parseInt( kv[1].split(":")[1] );
            }
            else if ( kv[0] == "USR" )
               usr = kv[1];
            else if ( kv[0] == "PWD" )
               pwd = kv[1];
         } catch( Exception e ) {
            System.out.printf( "Bad arg [%d] : %s\n", i, args[i] );
         }
      }
      /*
       * Do it:
       *    1) See if tickers are from file
       *    2) Instantiate object
       *    3) Start() UltraChan thread
       *    4) Subscribe() to all tickers
       */
      tkrs = TickersFromFile( tkrs );
      cpd  = new SubscribeLite( host, port, usr, pwd, bDmp );
      cpd.Start();
      for ( i=0; i<tkrs.length; cpd.Subscribe( tkrs[i++], "Hi Mom" ) );
      System.out.printf( "Hit <ENTER> to terminate...\n" );
      sc = new Scanner( System.in );
      sc.nextLine();
      System.out.printf( "Shutting down ...\n" );
      cpd.Stop();
      System.out.printf( "Done!!\n" );
   }
}
