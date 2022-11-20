/******************************************************************************
*
*  SunGardXML.java
*     SunGard Snap Server Emulator
*
*  REVISION HISTORY:
*     12 AUG 2014 jcs  Created (from SpryWareHTTP).
*
*  (c) 2011-2014 Quodd Financial
*******************************************************************************/
package com.quodd.proxy;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.QuoddMsg;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.MsgTypes;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;
import QuoddFeed.util.UCconfig;


/////////////////////////////////////////////////////////////////
//
//             c l a s s   _ S u n G a r d C l i e n t
//
/////////////////////////////////////////////////////////////////
class _SunGardClient implements Runnable
{
   ////////////////
   // Class-wide
   ////////////////
   static private String PDP_REQ   = "PDP_REQ";
   static private String PDP_RSP   = "PDP_RSP";
   static private String PDP_END   = "</" + PDP_REQ + ">";
   static private String ELEM_FMT  = "format";
   static private String ELEM_EXCH = "exch";
   static private String ELEM_CMD  = "command";
   static private String ELEM_SYM  = "symbol";
   static private String ELEM_MSG  = "errorMessage";
   static private String ELEM_ITEM = "item";
   static private String ELEM_ERR  = "error";
   static private String ATTR_STD  = "standard";
   static private int    _errno    = -1; // TODO : Map this ...

   /////////////////
   // Instance
   /////////////////
   private SunGardXML       _svr;
   private UltraChan         _uc;
   private Socket           _sock;
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
   _SunGardClient( SunGardXML svr, 
                   UltraChan  uc,
                   Socket     cli ) throws IOException
   {
      InetSocketAddress ip;
      String[]          kv;
      String            pl;
      int               i, nk;

      _svr  = svr;
      _uc   = uc;
      _sock = cli;
      ip    = (InetSocketAddress)cli.getRemoteSocketAddress();
      _dst  = ip.getAddress().getHostAddress();
      _in   = new DataInputStream( cli.getInputStream() );
      _out  = new DataOutputStream( cli.getOutputStream() );
      _thr  = new Thread( this );
      _bSz  = 16 * 1024;
      _buf  = new byte[_bSz];
      cout  = System.out;
      _uc.Start();
      _thr.start();
   }

   public void Stop()
   {
      synchronized( this ) {
         _uc.Stop();
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
      String  fmt, msg;
      boolean bRun;
      int     len, off, nR, nL, mSz;

      if ( _CanLog( _svr._SUNGARD_LOG_CONN ) )
         cout.printf( "[%s] CONNECT\n", Now() );
      for ( bRun=true,fmt=null,off=0; bRun; ) {
         try {
            nR   = _bSz - off;
            len  = _in.read( _buf, off, nR );
            if ( len <= 0 ) {
               fmt  = "[%s] READ ERROR : len <= 0\n";
               bRun = false;
               break; // for-i
            }
            if ( _CanLog( _svr._SUNGARD_LOG_RX_BYTES ) ) {
               cout.printf( "[%s] READ %d bytes\n", Now(), len );
               cout.printf( new String( _buf, off, len ) );
            }
            msg = null;
            nL  = off+len;
            off = 0;
            msg = ( nL > 0 ) ? GetNextMsg( _buf, off, nL ) : null;
            while( bRun && ( nL > 0 ) && ( msg != null ) ) {
               mSz = msg.length();
               if ( _CanLog( _svr._SUNGARD_LOG_RX_MSG ) ) {
                  cout.printf( "[%s] RX-MSG : %d bytes\n", Now(), mSz );
                  cout.printf( msg );
               }
               if ( Parse( msg ) ) {
                  off += mSz;
                  nL  -= mSz;
                  msg  = GetNextMsg( _buf, off, nL ); 
               }
               else {
                  fmt  = "[%s] XML ERROR : Malformed\n";
                  fmt += "   " + msg + "\n";
                  bRun = false;
                  msg  = null;
               }
            }
            if ( nL > 0 ) {
               System.arraycopy( _buf, off, _buf, 0, nL );
               off = nL;
            }
         } catch( NullPointerException e ) {
            fmt  = "[%s] READ ERROR : NullPointerException\n";
            bRun = false;
         } catch( IOException e ) {
            fmt  = "[%s] READ ERROR : IOException\n";
            bRun = false;
         }
      }
      if ( _CanLog( _svr._SUNGARD_LOG_CONN ) )
         cout.printf( "[%s] DISCONNECT\n", Now() );
      Stop();
/*
 * OBSOLETE
 *
      Disconnect();
      if ( fmt != null )
         cout.printf( fmt, Now() );
 */
      synchronized( this ) {
         _sock = null;
         _thr  = null;
      }
   }


   //////////////////////
   // Workhorse
   //////////////////////
   private String GetNextMsg( byte[] _buf, int off, int len )
   {
      String str, rtn;
      int    idx;

      // Just find PDP_END; Let parser do the rest ...

      str = new String( _buf, off, len );
      rtn = null;
      idx = str.indexOf( PDP_END );
      if ( idx != -1 ) {
         idx += PDP_END.length();
         rtn  = new String( _buf, off, idx );
      }
      return rtn;
   }

   private boolean Parse( String url )
   {
      PDP_REQ    pdp;
      String[]   tdb, fdb, rdb;
      String     rsp, res, tkr, val, err;
      QuoddMsg[] qdb;
      QuoddMsg   qm;
      Image      img;
      Status     sts;
      boolean    rtn, bEx, bStd;
      int        i, j, nr, nf;
      char       mt, mt2;

      // 1) Parse / Validate XML Query

      pdp = new PDP_REQ();
      if ( !pdp.Parse( url ) )
         return false;
      if ( (rsp=pdp.IsError()) != null ) {
         _SendResp( rsp );
         return true;
      }

      // 2) Query UltraCache for some love

      bStd = pdp.IsStdResp();
      rsp  = bStd ? "|0:" : "".format( "<%s>\n", PDP_RSP );
      bEx  = ( pdp._exch != null );
      tdb  = pdp.tkrs();
      fdb  = pdp.flds();
      nf   = fdb.length;
      if ( bEx )
         tdb = _QueryEx( pdp._exch );
      qdb = _uc.WaitMultiSnap( tdb, null, _svr.tmout() );
      nr  = ( qdb != null ) ? qdb.length : 0;
      rdb = ( nr > 0 ) ? new String[nr] : null;
      for ( i=0; i<nr; i++ ) {
         qm  = qdb[i];
         mt  = qm.mt();
         mt2 = qm.mtSub();
         tkr = tdb[i]; // qm.tkr();
         res = "";
         switch( mt2 ) {
            case MsgTypes._mtSubIMG:
               img  = (Image)qm;
               res += pdp.Build( tkr, fdb, img );
               break;
            default:
               if ( mt == MsgTypes._mtDEAD ) {
                  sts  = (Status)qm;
                  val  = sts.Text().replace( "<","" ).replace( ">","" );
                  res += res.format( "   <error type=\"%s\">\n", pdp._cmd );
                  res += "      <header>\n";
                  res += _AddXMLData( 9, ELEM_SYM, tkr );
                  res += _AddXMLData( 9, ELEM_MSG, val );
                  res += "      </header>\n";
                  res += res.format( "   </error>\n" );
               }
               break;
         }
         rdb[i] = res;
      }

      // [ a, b, c, ] -> abc

      if ( bStd )
         res = Arrays.toString( rdb ).replace( ", |", " |" );
      else
         res = Arrays.toString( rdb ).replace( ", <", " <" );
      res  = res.substring( 1,res.length()-1 );  // [ ... ]
      rsp += res;
      rsp += bStd ? "\n" : "".format( "</%s>\n", PDP_RSP );
      _SendResp( rsp );
      return true;
   }


   //////////////////////
   // Helpers
   //////////////////////
   private String[] _QueryEx( String ex )
   {
      BlobTable bt;
      String[]  rtn;
      int        r, nr;

      bt = _uc.SyncGetExchTickers( ex, null );
      if ( (nr=bt.nRow()) == 0 )
         return null;
      rtn = new String[nr];
      for ( r=0; r<nr; rtn[r] = bt.GetCell( r,0 ), r++ );
      if ( _CanLog( _svr._SUNGARD_LOG_EXCH ) )
         cout.printf( "[%s] %d tickers from exch %s\n", Now(), nr, ex );
      return rtn;
   }

   private void _SendResp( String rsp )
   {
      try {
         if ( _CanLog( _svr._SUNGARD_LOG_TX_MSG ) ) {
            cout.printf( "[%s] %d byte response\n", Now(), rsp.length() );
            if ( _CanLog( _svr._SUNGARD_LOG_TX_BYTES ) )
               cout.printf( rsp );
         }
         _out.writeBytes( rsp );
         _sock.shutdownOutput();
      } catch( Exception e ) {
         breakpoint();
      }
   }

   private String _AddXMLData( int nSp, String elem, String data )
   {
      String rtn, fmt;

      fmt = "".format( "%%%ds", nSp );
      rtn = ( nSp > 0 ) ? "".format( fmt, " " ) : "";
      rtn += rtn.format( "<%s> %s </%s>\n", elem, data, elem );
      return rtn;
   }

   private String _FieldData( String fld, Image img )
   {
      if ( fld.equals( "hi" ) )
         return Double.toString( img._high );
      else if ( fld.equals( "low" ) )
         return Double.toString( img._low );
      else if ( fld.equals( "opn" ) )
         return Double.toString( img._open );
      else if ( fld.equals( "vl" ) )
         return Long.toString( img._acVol );
      else if ( fld.equals( "last" ) )
         return Double.toString( img._trdPrc );
      else if ( fld.equals( "chg" ) )
         return Double.toString( img._netChg );
      else if ( fld.equals( "pchg" ) )
         return Double.toString( img._pctChg );
      else if ( fld.equals( "ask" ) )
         return Double.toString( img._ask );
      else if ( fld.equals( "bid" ) )
         return Double.toString( img._bid );
      else if ( fld.equals( "chg_sign" ) )
         return ( img._netChg > 0 ) ? "+" : "-";
      else if ( fld.equals( "name" ) )
         return img.Description();
//      else if ( fld.equals( "tradetick" ) )
      else if ( fld.equals( "vwap" ) )
         return Double.toString( img._vwap );
      else if ( fld.equals( "time" ) )
         return img.pDateTimeMs();
      return "Field not found";
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

   private boolean _CanLog( int type )
   {
      return _svr.CanLog( type );
   }

   private String Now()
   {
      return _svr.Now( _dst );
   }


   /////////////////////////////////////////////////////////////////
   //
   //          c l a s s   P D P _ R E Q
   //
   /////////////////////////////////////////////////////////////////
   private class PDP_REQ
   {
      public String            _fmt;
      public String            _cmd;
      public String            _exch;
      public ArrayList<String> _flds;
      public ArrayList<String> _tkrs;

      ////////////////////////////
      // Constructor
      ////////////////////////////
      private PDP_REQ()
      {
         _fmt  = "XML";
         _cmd  = "";
         _exch = null;
         _flds = new ArrayList<String>();
         _tkrs = new ArrayList<String>();
      }


      ////////////////////////////
      // Access
      ////////////////////////////
      private boolean IsStdResp()
      {
         return _fmt.equals( ATTR_STD );
      }

      private String[] tkrs()
      {
         String[] tdb;
         int      i, nt;

         // Pre-condition

         if ( (nt=_tkrs.size()) == 0 )
            return null;
         tdb = new String[nt];
         for ( i=0; i<nt; tdb[i] = _tkrs.get( i++ ) );
         return tdb;
      }

      private String[] flds() 
      {
         String[] fdb;
         int      i, nf; 

         // Pre-condition

         if ( (nf=_flds.size()) == 0 )
            return null;
         fdb = new String[nf];
         for ( i=0; i<nf; fdb[i] = _flds.get( i++ ) );
         return fdb; 
      } 

      ////////////////////////////
      // Operations
      ////////////////////////////
      private boolean Parse( String str )
      {
         DocumentBuilder db;
         InputSource     is;
         Document        doc;
         NodeList        ndb;
         Element         pdp, ex, fmt, cmd, fld;
         int             i, nn;

         /*
          * <PDP_REQ>
          *    . . .
          * </PDP_REQ>
          */
         pdp = null;
         try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            is = new InputSource();
            is.setCharacterStream( new StringReader( str ) );
            doc = db.parse( is );
            nn  = (ndb=doc.getElementsByTagName( PDP_REQ )).getLength();
            pdp = ( nn == 1 ) ? (Element)ndb.item( 0 ) : pdp;
         } catch( Exception e ) {
            cout.printf( "%s\n", e.getMessage() );
         }
         if ( pdp == null )
            return false;

         /*
          *   <exch> SC </exch>
          *   <format> standard </format>
          *   <command> quote </command>
          *   <symbol> MSFT </symbol>
          *   <item> last </item>
          *   <item> bibid </item>
          */
         nn  = (ndb=pdp.getElementsByTagName( ELEM_EXCH )).getLength();
         ex  = ( nn >= 1 ) ? (Element)ndb.item( 0 ) : null;
         nn  = (ndb=pdp.getElementsByTagName( ELEM_FMT )).getLength();
         fmt = ( nn >= 1 ) ? (Element)ndb.item( 0 ) : null;
         nn  = (ndb=pdp.getElementsByTagName( ELEM_CMD )).getLength();
         cmd = ( nn >= 1 ) ? (Element)ndb.item( 0 ) : null;
         nn  = (ndb=pdp.getElementsByTagName( ELEM_ITEM )).getLength();
         for ( i=0; i<nn; i++ )
            _flds.add( getCharData( (Element)ndb.item( i ), "" ) );
         nn  = (ndb=pdp.getElementsByTagName( ELEM_SYM )).getLength();
         for ( i=0; i<nn; i++ )
            _tkrs.add( getCharData( (Element)ndb.item( i ), "" ) );
         _exch = getCharData( ex, _exch );
         _fmt  = getCharData( fmt, _fmt );
         _cmd  = getCharData( cmd, _cmd );
         return( pdp != null );
      }

      private String getCharData( Element e, String dflt )
      {
         Node          kid;
         CharacterData cd;

         if ( e != null ) {
            kid = e.getFirstChild();
            if ( kid instanceof CharacterData ) {
               cd = (CharacterData)kid;
               return cd.getData().trim();
            }
         }
         return dflt;
      }

      private String IsError()
      {
         String[] tdb, fdb;
         String   rsp, err;
         boolean  bEx, bStd;

         // Pull out relevent stuff from query; Initialize response

         bStd = IsStdResp();
         bEx  = ( _exch != null );
         tdb  = tkrs();
         fdb  = flds();
         rsp = bStd ? "" : "".format( "<%s>\n", PDP_RSP );

         /*
          * 1) No <item>
          */
         if ( fdb == null ) {
            err = "No " + ELEM_ITEM + "s specified";
            if ( bStd ) 
               rsp += "".format( "|%d:%s\n", _errno, err );
            else {
               rsp += _AddXMLData( 3, ELEM_ERR, err );
               rsp += "".format( "</%s>\n", PDP_RSP );
            }
            return rsp;
         }
         /*
          * 2) No <exch> or <symbol>
          */
         if ( !bEx && ( tdb == null ) ) {
            err = "No " + ELEM_EXCH + " or " + ELEM_SYM + "(s) specified";
            if ( bStd )
               rsp += "".format( "|%d:%s\n", _errno, err );
            else {
               rsp += _AddXMLData( 3, ELEM_ERR, err );
               rsp += "".format( "</%s>\n", PDP_RSP );
            }
            return rsp;
         }
         /*
          * 3) <symbol> and <format> standard </format>
          */
         if ( bStd && !bEx ) {
            err  = "".format( "<%s> %s </%s> ", ELEM_FMT, ATTR_STD, ELEM_FMT );
            err += "".format( "supported with <%s> only, ", ELEM_EXCH );
            err += "".format( "not <%s>", ELEM_SYM );
            rsp += "".format( "|%d:%s\n", _errno, err );
            return rsp;
         }
         return null;
      }

      private String Build( String tkr, String[] fdb, Image img )
      {
         String res, fld;
         int    i, nf;

         res = "";
         nf  = fdb.length;
         if ( IsStdResp() ) {
            res += "".format( "|%s", tkr );
            for ( i=0; i<nf; i++ )
               res += "".format( "|%s", _FieldData( fdb[i], img ) );
         }
         else {
            res += res.format( "   <success type=\"%s\">\n", _cmd );
            res += "      <header>\n";
            res += _AddXMLData( 9, ELEM_SYM, tkr );
            res += "      </header>\n";
            res += "      <quotation>\n";
            for ( i=0; i<nf; i++ ) {
               fld  = fdb[i];
               res += _AddXMLData( 9, fld, _FieldData( fld, img ) );
            }
            res += "      </quotation>\n";
            res += "   </success>\n";
         }
         return res;
      }
   }
}



/////////////////////////////////////////////////////////////////
//
//               c l a s s   S u n G a r d X M L
//
/////////////////////////////////////////////////////////////////
class SunGardXML implements Runnable
{
   ////////////////
   // Class-wide
   ////////////////
   /*
    * Logging from SUNGARD_LOGLVL
    */
   static public final int _SUNGARD_LOG_CONN     = 0x0001;
   static public final int _SUNGARD_LOG_RX_MSG   = 0x0002;
   static public final int _SUNGARD_LOG_RX_BYTES = 0x0004;
   static public final int _SUNGARD_LOG_EXCH     = 0x0008;
   static public final int _SUNGARD_LOG_TX_MSG   = 0x0010;
   static public final int _SUNGARD_LOG_TX_BYTES = 0x0020;

   ////////////////
   // Instance
   ////////////////
   private int                       _port;
   private int                       _tmout;
   private ServerSocket              _lsn;
   private Thread                    _thr;
   private ArrayList<_SunGardClient> _cli;
   private PrintStream               cout;
   private int                       _logLvl;

   //////////////////////
   // Constructor
   //////////////////////
   SunGardXML( int port, int tmout ) throws IOException
   {
      String   pl;
      String[] kv;
      int      i, nk;

      // Basic Stuff

      _port  = port;
      _tmout = tmout;
      _lsn   = new ServerSocket( _port );
      _thr   = new Thread( this );
      _cli   = new ArrayList<_SunGardClient>();
      cout   = System.out;

      // Logging Level

      _logLvl = _SUNGARD_LOG_CONN;
      if ( (pl=UCconfig.GetEnv( "SUNGARD_LOGLVL" )) != null ) {
         kv = pl.split(",");
         nk = kv.length;
         for ( i=0; i<nk; i++ ) {
            _logLvl |= kv[i].equals( "CONN" )     ? _SUNGARD_LOG_CONN     : 0;
            _logLvl |= kv[i].equals( "RX_MSG" )   ? _SUNGARD_LOG_RX_MSG   : 0;
            _logLvl |= kv[i].equals( "RX_BYTES" ) ? _SUNGARD_LOG_RX_BYTES : 0;
            _logLvl |= kv[i].equals( "EXCH" )     ? _SUNGARD_LOG_EXCH     : 0;
            _logLvl |= kv[i].equals( "TX_MSG" )   ? _SUNGARD_LOG_TX_MSG   : 0;
            _logLvl |= kv[i].equals( "TX_BYTES" ) ? _SUNGARD_LOG_TX_BYTES : 0;
         }
      }

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
   // Access
   //////////////////////
   public int tmout()
   {
      return _tmout;
   }

   public boolean CanLog( int type )
   {
      return( ( _logLvl & type ) == type );
   }

   public static String Now( String dst )
   {
      long   t0, tid;
      String p0, s;

      tid = Thread.currentThread().getId();
      t0  = System.currentTimeMillis();
      p0  = QuoddMsg.pDateTimeMs( t0 );
      s   = p0;
      if ( dst != null )
         s += s.format( ",%04x,%s", tid, dst );
      return s;
   }


   //////////////////////
   // Runnable Interface
   //////////////////////
   public void run()
   {
      UltraChan uc;

      try {
         cout.printf( "SunGardXML listening on port %d\n", _port );
         while( true ) {
            uc  = new UltraChan( UCconfig.Hostname(),
                                 UCconfig.Port(),
                                 UCconfig.Username(),
                                 UCconfig.Password(),
                                 false );
            _cli.add( new _SunGardClient( this, uc, _lsn.accept() ) );
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
      int         argc, port, tmout;
      Scanner     sc;
      PrintStream cout;
      SunGardXML  sun;

      // Quick check

      argc = args.length;
      cout = System.out;
      if ( ( argc > 0 ) && args[0].equals( "--version" ) ) {
         cout.printf( "%s\n", QuoddMsg.Version() );
         return;
      }
      port  = ( argc > 0 ) ? Integer.parseInt( args[0] ) : 12345;
      tmout = ( argc > 1 ) ? Integer.parseInt( args[1] ) : 0;
      try {
         sun = new SunGardXML( port, tmout );
      } catch( IOException e ) {
         cout.printf( "Can't listen( %d ) : %s\n", port, e.getMessage() );
         return;
      }
      sc   = new Scanner( System.in );
      sc.nextLine();
      cout.printf( "Shutting down ...\n" );
      sun.Stop();
      cout.printf( "Done!!\n" );
   }
}
