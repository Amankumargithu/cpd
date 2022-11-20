/******************************************************************************
*
*  UCLocCodeMapDB.java
*     UC Locate Code Map
*
*  REVISION HISTORY:
*     28 JUL 2014 jcs  Created (from SpryWareHTTP.java)
*     28 AUG 2014 jcs  Build  81: TSQ_LOG_TKR
*     18 AUG 2015 jcs  Build  82: GMNI = H
*     18 NOV 2015 jcs  Build  98: EDGO = E
*     16 FEB 2016 jcs  Build  99a:MRCY = J; MCRY not MRCY
*     21 APR 2017 jcs  Build 102: MIAX = P; Not it's MRPL
*
*  (c) 2011-2017 Quodd Financial
*******************************************************************************/
package QuoddFeed.MD;

import java.io.*;
import java.lang.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


/////////////////////////////////////////////////////////////////
//
//              c l a s s   U C L o c C o d e M a p
//
/////////////////////////////////////////////////////////////////
public class UCLocCodeMap
{
   ////////////////
   // Class-wide
   ////////////////
   static private String     ELEM_ROW       = "row";
   static private String     ELEM_FIELD     = "field";
   static private String     ATTR_NAME      = "name";
   static private String     ATTR_DAY       = "day";
   static private final long MILLIS_PER_DAY = 86400 * 1000;
   static private final int  NUM_DAY        = 40;
   /*
    * Logging from TSQ_LOGLVL
    */
   static public final int             _TSQ_LOG_LOAD = 0x0001;
   static public final HashSet<String> _TSQ_LOG_TKR  = new HashSet<String>();

   ////////////////
   // Instance
   ////////////////
   private HashMap<String, String>    _exOpt;
   private HashMap<String, UCMapping> _ucMap;
   private int                        _logLvl;
   private PrintStream                cout;

   //////////////////////
   // Constructor
   //////////////////////
   public UCLocCodeMap()
   {
      String[] sfx  = { "A", "N", "Z", "W",
                        "B", "T", "C", "I",
                        "Q", "X", "M", "H",
                        "E", "J", "P" };
      String[] code = { "AMXO", "ARCO", "BATO", "C2OX",
                        "XBOX", "XBXO", "XCBO", "XISX",
                        "XNDQ", "XPHO", "XMIO", "GMNI",
                        "EDGO", "MCRY", "MPRL" };
      String[] kv;
      String   pl;
      int      i, nk;

      // (Hard-Coded) Exchange Code Lookup

      cout   = System.out;
      _exOpt = new HashMap<String, String>();
      for ( i=0; i<sfx.length; _exOpt.put( code[i], sfx[i] ), i++ );

      // Locate Code Map

      _ucMap = new HashMap<String, UCMapping>();

      // Logging Level

      _logLvl = 0;
      if ( (pl=QuoddFeed.util.UCconfig.GetEnv( "TSQ_LOGLVL" )) != null ) {
         kv = pl.split(",");
         nk = kv.length;
         for ( i=0; i<nk; i++ ) {
            _logLvl |= kv[i].equals( "LOAD" )     ? _TSQ_LOG_LOAD     : 0;
         }
      }
      if ( (pl=QuoddFeed.util.UCconfig.GetEnv( "TSQ_LOGTKR" )) != null ) {
         kv = pl.split(",");
         nk = kv.length;
         for ( i=0; i<nk; _TSQ_LOG_TKR.add( kv[i++] ) );
      }
   }

   public void LoadFromURLs( String urlMktCtr, String urlOpts )
   {
      LoadXMLFromURL mx, ox;
      String[]       dt, mcData, optData;
      String         pt;
      UCMapping      map;
      int            i, n;

      mx = new LoadXMLFromURL();
      ox = new LoadXMLFromURL();
      if ( CanLog( _TSQ_LOG_LOAD ) )
         cout.printf( "[%s] %s ...\n", _Now(), urlMktCtr );
      n = mx.Load( urlMktCtr );
      if ( CanLog( _TSQ_LOG_LOAD ) )
         cout.printf( "[%s] ... %d MktCtrs pulled via http\n", _Now(), n );
      if ( CanLog( _TSQ_LOG_LOAD ) )
         cout.printf( "[%s] %s ...\n", _Now(), urlOpts );
      n = ox.Load( urlOpts );
      if ( CanLog( _TSQ_LOG_LOAD ) ) {
         cout.printf( "[%s] Building ...\n", _Now() );
         cout.printf( "[%s] ... %d OptChans pulled via http\n", _Now(), n );
      }
      dt = mx.Dates();
      for ( i=0; i<dt.length; i++ ) {
         mcData  = mx.DataByDate( dt[i] );
         optData = ox.DataByDate( dt[i] );
         map     = new UCMapping( dt[i] );
         map.LoadFromData( urlMktCtr + "?" + dt[i],
                           urlOpts + "?" + dt[i],
                           mcData,
                           optData,
                           true );
         _ucMap.put( dt[i], map );
      }
   }

   public void LoadFromFiles( String mcDef,
                              String optDef ) throws IOException
   {
      String    mDef1, oDef1, pt, pT;
      UCMapping map;
      File      fm, fo;
      boolean   bOK;
      long      tm;
      int       i;

      // Today + Previous Days Files

      tm  = System.currentTimeMillis();
      pt  = UCLocCodeMap.YYYYMMDD( tm );
      map = new UCMapping( pt );
      map.LoadFromFile( mcDef, optDef );
      _ucMap.put( pt, map );
      for ( i=0; i<NUM_DAY; i++ ) {
         pt    = UCLocCodeMap.YYYYMMDD( tm );
         mDef1 = mcDef  + "." + pt;
         oDef1 = optDef + "." + pt;
         fm    = new File( mDef1 );
         fo    = new File( oDef1 );
         bOK   = fm.exists() && !fm.isDirectory();
         bOK  &= fo.exists() && !fo.isDirectory();
         if ( bOK ) {
            pT  = UCLocCodeMap.YYYYMMDD( fm.lastModified() );
            map = new UCMapping( pT );
            map.LoadFromFile( mDef1, oDef1 );
            _ucMap.put( pT, map );
         }
         tm   -= MILLIS_PER_DAY;
      }
   }


   //////////////////////
   // Access
   //////////////////////
   public String OptEX( String exch )
   {
      String  rtn;
      char[]  ch = exch.toCharArray();
      boolean bOK;
      int     i;

      // Lookup

      if ( (rtn=_exOpt.get( exch )) != null )
         return rtn;

      // Else ensure ASCII

      for ( i=0,bOK=true; bOK && i<ch.length; i++ )
         bOK &= _IsAscii( ch[i] );
      if ( bOK )
         rtn = exch;
      if ( !bOK ) {
         rtn = "";
         for ( i=0; i<ch.length; rtn += rtn.format( "[%02x]", (int)ch[i++] ) );
      }
      return rtn;
   }

   public String GetExch( String T0, String tkr, int locCode )
   {
      String    date = T0.split( " " )[0];
      UCMapping uc;

      // T0 = "20140616 12:34:56.789"

      uc = _ucMap.get( date );
      if ( uc != null )
         return uc.GetExch( tkr, locCode );
      if ( _CanLogTkr( tkr ) )
         cout.printf( "  Can't find date %s for ( %s, %s )\n", T0, date, tkr );
      return "?"; // "????";
   }

   public boolean CanLog( int type )
   {
      return( ( _logLvl & type ) == type );
   }

   private String _Now()
   {
      return pDateTimeMs( System.currentTimeMillis() );
   }


   //////////////////////
   // Class-wide
   //////////////////////
   public static boolean _CanLogTkr( String tkr )
   {
      return _TSQ_LOG_TKR.contains( tkr );
   }

   public static boolean _IsAscii( char ch )
   {
      char c0 = 0x21; // ' ';
      char c1 = '~';

      // No spaces - Arrays.toString() ...

      return( ( c0<=ch ) && ( ch<=c1 ) );
   }


   public static String Now( String dst )
   {
      long   t0, tid;
      String p0, s;

      tid = Thread.currentThread().getId();
      t0  = System.currentTimeMillis();
      p0  = pDateTimeMs( t0 );
      s   = p0;
      if ( dst != null )
         s += s.format( ",%04x,%s", tid, dst );
      return s;
   }

   public static String YYYYMMDD( long tm )
   {
      String pt;

      pt = pDateTimeMs( tm );    // 2014-06-16 12:34:56.789
      pt = pt.split( " " )[0];   // 2014-06-16
      return pt.replace( "-", "" );
   }

   public static String pDateTimeMs( long tm )
   {
      // 2014-06-16 12:34:56.789

      return QuoddFeed.msg.QuoddMsg.pDateTimeMs( tm );
   }


   /////////////////////////////////////////////////////////////////
   //
   //                 c l a s s   U C M a p p i n g
   //
   /////////////////////////////////////////////////////////////////
   private class UCMapping
   {
      private String                   _mcDef;
      private String                   _optDef;
      private String                   _YMD;
      private HashMap<String, Integer> _und2chan;
      private HashMap<Integer, UFChan> _UFchans;
   
      //////////////////////
      // Constructor
      //////////////////////
      UCMapping( String YMD )
      {
         _mcDef    = null;
         _optDef   = null;
         _YMD      = YMD;
         _und2chan = new HashMap<String, Integer>();
         _UFchans  = new HashMap<Integer, UFChan>();
      }

      void LoadFromFile( String mcFile, String optFile )
      {
         LoadFromData( mcFile,
                       optFile,
                       ReadFile( mcFile ),
                       ReadFile( optFile ),
                       false );
      }

      void LoadFromData( String   mcFile, 
                         String   optFile,
                         String[] mcData,
                         String[] optData,
                         boolean  bTruncCol1 )
      {
         _mcDef  = mcFile;
         _optDef = optFile;
         LoadMktCtrs( mcData, bTruncCol1 );
         LoadOptChans( optData, bTruncCol1 );
      }
   
   
      //////////////////////
      // Access
      //////////////////////
      private String GetExch( String tkr, int locCode )
      {
         String  rtn, hdr, err;
         UFChan  uf;
         MktCtr  mc;
         int     nc;
         boolean bLog;
   
         rtn  = "XXXX";
         bLog = _CanLogTkr( tkr );
         hdr  = "".format( "%s.GetExch( %s", _YMD, tkr );
         err  = "No channel : " + hdr;
         if ( (nc=GetChan( tkr )) != -1 ) {
            if ( (uf=_UFchans.get( nc )) != null ) {
               if ( (mc=uf.GetMktCtr( locCode )) != null ) {
                  rtn = mc._mnemonic; // mc._exch;
                  if ( bLog )
                     cout.printf( "%s,%d,%d ) = %s\n", hdr, nc, locCode, rtn );
               }
               else if ( bLog )
                  cout.printf( "%s, %d, %d )\n", err, nc, locCode );
            }
            else if ( bLog )
               cout.printf( "%s, %d )\n", err, nc );
         }
         return rtn;
      }
   
   
      //////////////////////
      // Loaders
      //////////////////////
      private void LoadMktCtrs( String[] mcDef, boolean bTruncCol1 )
      {
         int      i, ix, n, ch, nc, tot;
         String[] cels;
         UFChan   uf;
         MktCtr   mc;
         String   row, s, fmt;
   
         /*
          * <nChan>,<ChType>,<mcLocIdx>,<mcLocToday>,<Exch>,<Mnemonic>,
          * 1,OPRA1,10,11,XMIO,
          */
         mc  = new MktCtr();
         ix  = bTruncCol1 ? 1 : 0;
         tot = 0;
         for ( i=0; i<mcDef.length; i++ ) {
            row  = mcDef[i];
            cels = row.split( "," );
            if ( row.length() == 0 )
               continue; // for-i
            nc = cels.length - ix;
            for ( n=0; n<nc; n++ ) {
               s = cels[n+ix].replace( " ","" ).replace( "\n","" );
               switch( n ) {
                  case 0: // nChan
                     mc._nChan = Integer.parseInt( s );
                     break;
                  case 1: // Channel Type
                     mc._type = s;
                     break;
                  case 2: // locCode index
                     break;
                  case 3: // locCode today
                     mc._locCode = Integer.parseInt( s );
                     break;
                  case 4: // EXCH
                     mc._exch = s;
                     break;
                  case 5: // Mnemonic
                     ch           = mc._nChan;
                     mc._mnemonic = s;
                     if ( (uf=_UFchans.get( ch )) == null ) {
                        uf = new UFChan( mc._nChan, s );
                        _UFchans.put( ch, uf );
                     }
                     uf.Add( mc );
                     tot += 1;
                     mc   = new MktCtr();
                     break;
               }
            }
         }
         fmt  = "[%s] MktCtrs( %s ) : %d Chans and ";
         fmt += "%d MktCtrs loaded from %s\n";
         if ( CanLog( _TSQ_LOG_LOAD ) )
            cout.printf( fmt, _Now(), _YMD, _UFchans.size(), tot, _mcDef );
      }

      private void LoadOptChans( String[] optDef, boolean bTruncCol1 )
      {
         int      i, ix, n, ch, nc;
         String[] cels;
         String   s, row, und, fmt;
   
         /*
          * <nChan>,<Underlyer>,
          * 1,AAPL,
          */
         ch = -1;
         ix = bTruncCol1 ? 1 : 0;
         for ( i=0; i<optDef.length; i++ ) {
            row  = optDef[i];
            cels = row.split( "," );
            if ( row.length() == 0 )
               continue; // for-i
            nc = cels.length - ix;
            for ( n=0; n<nc; n++ ) {
               s = cels[n+ix].replace( " ","" ).replace( "\n","" );
               switch( n ) {
                  case 0: // nChan
                     ch = Integer.decode( s ).intValue();
                     break;
                  case 1: // Underlyer
                     _und2chan.put( s, ch );
                     if ( _CanLogTkr( s ) )
                        cout.printf( "   %s( %s ) : Chan=%d\n", _YMD, s, ch );
                     break;
               }
            }
         }
         fmt = "[%s] OptChans( %s ) : %d tickers loaded from %s\n";
         if ( CanLog( _TSQ_LOG_LOAD ) )
            cout.printf( fmt, _Now(), _YMD, _und2chan.size(), _optDef );
      }
   
   
      //////////////////////
      // Helpers 
      //////////////////////
      private int GetChan( String opt )
      {
         String  und;
         Integer ch;
   
         // O:AAPL\\13B16\\260.00
   
         und = opt.split( "\\\\" )[0];
         und = und.split(":")[1];
         if ( (ch=_und2chan.get( und )) != null ) 
            return ch;
         else
         return -1;
      }

      private String[] ReadFile( String filename )
      {
         String     content;
         File       file;
         FileReader rdr;
         char[]     ch;
         long       fSz;

         content = "";
         try {
            file = new File( filename );
            fSz  = file.length();
            rdr  = new FileReader( file );
            ch   = new char[(int)fSz];
            rdr.read( ch );
            content = new String( ch );
            rdr.close();
         } catch( IOException ex ) {
            cout.printf( ex.getMessage() );
         }
         return content.split( "\n" );
      }


      /////////////////////////////////////////////////////////////////
      //
      //               c l a s s   U F C h a n
      //
      /////////////////////////////////////////////////////////////////
      private class UFChan
      {
         private int                      _nChan;
         private String                   _name;
         private HashMap<Integer, MktCtr> _loc2MktCtr;
   
         //////////////////
         // Constructor
         //////////////////
         UFChan( int nChan, String name )
         {
            _nChan      = nChan;
            _name       = name;
            _loc2MktCtr = new HashMap<Integer, MktCtr>(); 
         }
   
         //////////////////
         // Operations
         //////////////////
         MktCtr GetMktCtr( int locCode )
         {
            return _loc2MktCtr.get( locCode );
         }
   
         void Add( MktCtr mc )
         {
            _loc2MktCtr.put( mc._locCode, mc );
         }
      }
   
   
      /////////////////////////////////////////////////////////////////
      //
      //               c l a s s   M k t C t r
      //
      /////////////////////////////////////////////////////////////////
      private class MktCtr
      {
         private int    _nChan;
         private String _type;
         private int    _locCode;
         private String _exch;
         private String _mnemonic;
   
         //////////////////
         // Constructor
         //////////////////
         MktCtr()
         {
            _nChan    = -1;
            _type     = null;
            _locCode  = -1;
            _exch     = null;
            _mnemonic = null;
         }
   
         //////////////////
         // Operations
         //////////////////
         boolean IsOPRA()
         {
            return _exch.startsWith( "OPRA" );
         }
      }
   }


   /////////////////////////////////////////////////////////////////
   //
   //          c l a s s   L o a d X M L F r o m U R L
   //
   /////////////////////////////////////////////////////////////////
   private class LoadXMLFromURL extends DefaultHandler
   {
      private SAXParserFactory        _spf;
      private SAXParser               _sax;
      private XMLReader               _xml;
      private String                  _name;
      private Row                     _hdr;
      private Row                     _wip;
      private ArrayList<Row>          _rows;
      private HashMap<String, String> _byDate;
      private ArrayDeque<String>      _elems;
   
      ////////////////////////////
      // Constructor
      ////////////////////////////
      private LoadXMLFromURL()
      {
         // Guts
   
         _name   = null;
         _hdr    = new Row();
         _wip    = null;
         _rows   = new ArrayList<Row>();
         _byDate = new HashMap<String, String>();
         _elems  = new ArrayDeque<String>();
    
         // Parsing
   
         _spf  = SAXParserFactory.newInstance();
         _spf.setNamespaceAware(true);
         try {
            _sax = _spf.newSAXParser();
            _xml = _sax.getXMLReader();
            _xml.setContentHandler( this );
         } catch( Exception ex ) {
            cout.printf( "ERROR : %s\n", ex.getMessage() );
         }
      }
   
   
      ////////////////////////////
      // Operations
      ////////////////////////////
      private int Load( String url )
      {
         try {
            if ( _xml != null )
               _xml.parse( url );
         } catch( Exception ex ) {
            cout.printf( "ERROR-Load() : %s\n", ex.getMessage() );
         }
         return _rows.size();
      }

      private String[] Dates()
      {
         String[] dt;
         int      i, nd;

         nd = _byDate.size();
         dt = new String[nd];
         i  = 0;
         for ( String key : _byDate.keySet() )
            dt[i++] = key;
         return dt;
      }

      private String[] DataByDate( String dt )
      {
         String content;

         content = _byDate.containsKey( dt ) ? _byDate.get( dt ) : "";
         return content.split( "\n" );
      }
   
   
      ////////////////////////////
      // DefaultHandler
      ////////////////////////////
      public void startDocument() throws SAXException
      {
         _elems.clear();
      }
   
      public void startElement( String     namespaceURI,
                                String     localName,
                                String     qName, 
                                Attributes atts ) throws SAXException
      {
         // Column Headers or New Row??
   
         _elems.push( localName );
         _name = atts.getValue( ATTR_NAME );
         if ( IsRow() )
            _wip = new Row();
         if ( IsField() && !HasColHdr() )
            _hdr.Add( _name );
      }
   
      public void characters( char[] ch,
                              int    start,
                              int    length ) throws SAXException
      {
         String data;
   
         data = new String( ch, start, length ).trim();
         data = IsDay() ? data.replace( "-","" ) : data;
         if ( data.length() > 0 )
            _wip.Add( data );
      }
   
      public void endElement( String namespaceURI,
                              String localName,
                              String qName ) throws SAXException
      {
         String dt, lwc;

         if ( IsRow() ) {
            _rows.add( _wip );
            dt   = _wip.Date();
            lwc  = _byDate.containsKey( dt ) ? _byDate.get( dt ) : "";
            lwc += "\n";
            lwc += _wip.ToCSV();
            _byDate.put( dt, lwc );
            _wip = null;
         }
         _elems.pop();
         _name = null;
      }
   
      public void endDocument() throws SAXException
      {
         int nc, nr;
   
         nc = _hdr.Size();
         nr = _rows.size();
      }
   
   
      /////////////////////
      // Helpers
      /////////////////////
      private boolean HasColHdr()
      {
         return( _rows.size() > 0 );
      }
   
      private boolean IsRow()
      {
         return elemName().equals( ELEM_ROW );
      }
   
      private boolean IsField()
      {
         return elemName().equals( ELEM_FIELD );
      }
   
      private boolean IsDay()
      {
         return ( _name != null ) && _name.equals( ATTR_DAY );
      }
   
      private String elemName()
      {
         return _elems.getFirst();
      }
   
   
      ///////////////////////////////
      //
      //   c l a s s   R o w
      //
      ///////////////////////////////
      private class Row
      {
         private ArrayList<String> _cels;
   
         //////////////
         // Constructor
         //////////////
         Row()
         {
            _cels = new ArrayList<String>();
         }
   
         //////////////
         // Operations
         //////////////
         private String Date()
         {
            return Get( 0 );
         }

         private int Size()
         {
            return _cels.size();
         }
   
         private void Add( String str )
         {
            _cels.add( str );
         }
   
         private String Get( int idx )
         {
            String rtn;
   
            try {
               rtn = _cels.get( idx );
            } catch( Exception ex ) {
               rtn = ex.getMessage();
            }
            return rtn;
         }
   
         private String ToCSV()
         {
            String r;
            int    i, nc;
   
            r  = "";
            nc = Size();
            for ( i=0; i<nc; r+="".format( "%s,", Get( i++ ) ) );
            return r;
         }
      }
   }
}
