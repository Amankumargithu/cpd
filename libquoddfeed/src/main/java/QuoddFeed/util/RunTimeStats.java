/******************************************************************************
*
*  RunTimeStats.java
*     UltraChan run-time statistics
*
*  REVISION HISTORY:
*      2 DEC 2012 jcs  Created.
*      5 DEC 2012 jcs  Build 48: size = max+1
*      8 MAY 2013 jcs  Build 59: AddLong()
*     23 MAY 2013 jcs  Build 60: InsertString(); sync all _map.position()
*     10 JUN 2013 jcs  Build 63: Hex()
*     17 JUN 2013 jcs  Build 64: _zz
*
*  (c) 2011-2013 Quodd Financial
*******************************************************************************/
package QuoddFeed.util;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import QuoddFeed.util.*;


/////////////////////////////////////////////////////////////////
//
//             c l a s s    R u n T i m e S t a t s
//
/////////////////////////////////////////////////////////////////
/**
 * RunTimeStats is a class that maintains run-time statistics in a 
 * memory-mapped file.
 * <p>
 * Stats are stored as a ( String name, int type, <NativeType> value ) 
 * tuple, where <NativeType> is bool, long, Time (long) or double 
 * depending on the type.  The tuple is stored in a 64-byte segment as 
 * follows:
 * <p>
 *    struct Stat
 *    {
 *         byte _name[63];
 *         byte _type;
 *         byte _value[64];
 *    }
 * <p>
 * Usage is as follows:<p>
 * 1) Define the max number of Stat segments when you construct the class.<p>
 * 2) Define a given statistic via one of the InsertXxx() methods -
 * {@link #InsertBool(boolean)}, {@link #InsertLong(long)}, 
 * {@link #InsertTime(long)}, or {@link #InsertDouble(double)}.  These return
 * an index that you use to update the run-time statistics<p>
 * 3) Update a statistics using the index returned from 2) above via the 
 * PutXxx() methods - {@link #PutBool(int,boolean)}, 
 * {@link #PutLong(int,long)}, {@link #PutTime(int,long)}, or
 * {@link #PutDouble(int,double)}<p>
 * 4) Increment long stats via {@link #IncLong(int)}<p>
 * 5) Add to long stats via {@link #AddLong(int,long)}<p>
 * 6) Query a stat via {@link #GetStat(int)}<p>
 */
public class RunTimeStats
{
   ///////////////////////////
   // Stat Type
   ///////////////////////////
   private static final int  _nmSz   =  63;
   private static final int  _tySz   =   1;
   private static final int  _hSz    = _nmSz + _tySz;
   private static final int  _strSz  =  64;
   private static final int  _statSz = _hSz + _strSz;
   public  static final char BOOL    = 'B';
   public  static final char LONG    = 'L';
   public  static final char HEX     = 'X';
   public  static final char TIME    = 'T';
   public  static final char DOUBLE  = 'D';
   public  static final char STRING  = 'S';
 
   ///////////////////////////
   // Stat Type
   ///////////////////////////
   public class Stat
   {
      public String _name;
      public char   _type;
   }

   public class BoolStat extends Stat
   {
      public boolean _val;

      public BoolStat( String name, boolean val )
      {
         super._name = name;
         super._type = BOOL;
         this._val   = val;
      }
   }

   public class LongStat extends Stat
   {
      public long _val;

      public LongStat( String name, long val )
      {
         super._name = name;
         super._type = LONG;
         this._val   = val;
      }
   }

   public class HexStat extends Stat
   {
      public long _val;

      public HexStat( String name, long val )
      {
         super._name = name;
         super._type = HEX;
         this._val   = val;
      }
   }

   public class TimeStat extends Stat
   {
      public long _val;

      public TimeStat( String name, long val )
      {
         super._name = name;
         super._type = TIME;
         this._val   = val;
      }
   }

   public class DoubleStat extends Stat
   {
      public double _val;

      public DoubleStat( String name, double val )
      {
         super._name = name;
         super._type = DOUBLE;
         this._val   = val;
      }
   }

   public class StringStat extends Stat
   {
      public String _val;

      public StringStat( String name, String val )
      {
         super._name = name;
         super._type = STRING;
         this._val   = val;
      }
   }


   ////////////////////
   // Instance Members
   ////////////////////
   String           _file;
   String           _flags;
   int              _pos;
   int              _max;
   FileChannel      _ch;
   RandomAccessFile _fp;
   MappedByteBuffer _map;
   byte[]           _zz;


   ///////////////////////////////
   // Constructor
   ///////////////////////////////
   public RunTimeStats( String file, int max, boolean bRdOnly )
   {
      PrintStream cout = System.out;
      ByteBuffer  buf;
      int         i, sz;

      // 1) Init guts

      _file  = file;
      _flags = bRdOnly ? "r" : "rw";
      _pos   = 0;
      _max   = max;
      _ch    = null;
      _fp    = null;
      _map   = null;

      // 2) Create file

      if ( !bRdOnly )
         new File( _file ).delete();
      try {
         _fp = new RandomAccessFile( _file, _flags );
      } catch( FileNotFoundException e ) {
         cout.printf( "Can't create %s : %s\n", _file, e.getMessage() );
         _fp = null;
         return;
      }
      _ch = _fp.getChannel();

      // 3) Write _max * _statSz, then map

      sz  = _statSz * ( _max+1 );
      buf = null;
      if ( !bRdOnly ) {
         buf = ByteBuffer.allocate( sz );
         for ( i=0; i<sz; buf.put( i++, (byte)0 ) );
         try {
            _ch.write( buf );
         } catch( IOException e ) {
            cout.printf( "write() failed : %s\n", e.getMessage() );
            _fp = null;
            _ch = null;
            return;
         }
      }
      buf = null;

      // 4) Map

      try {
         if ( bRdOnly )
            _map = _ch.map( FileChannel.MapMode.READ_ONLY, 0, sz );
         else
            _map = _ch.map( FileChannel.MapMode.READ_WRITE, 0, sz );
      } catch( IOException e ) {
         cout.printf( "map() failed : %s\n", e.getMessage() );
         _map = null;
         System.exit( 0 );
      }

      // 5) Zero-out string ...

      _zz = new byte[_strSz];
      for ( i=0; i<_zz.length; _zz[i++] = 0 ); 
   }

   ///////////////////////////////
   // Access
   ///////////////////////////////
   public int InsertBool( String name )
   {
      return _Insert( name, BOOL );
   }

   public int InsertLong( String name )
   {
      return _Insert( name, LONG );
   }

   public int InsertHex( String name )
   {
      return _Insert( name, HEX );
   }

   public int InsertTime( String name )
   {
      return _Insert( name, TIME );
   }

   public int InsertDouble( String name )
   {
      return _Insert( name, DOUBLE );
   }

   public int InsertString( String name )      
   {
      return _Insert( name, STRING );          
   }
   
   public void IncLong( int pos )
   {
      AddLong( pos, 1 );
   }

   public void AddLong( int pos, long lAdd )
   {
      Stat     st;
      LongStat ls;
      long     lv;

      // Defensive coding

      if ( (st=GetStat( pos )) == null )
         return;
      if ( ( st._type != LONG ) && ( st._type != HEX ) )
         return;
   
      // OK to increment

      ls = (LongStat)st;
      lv = ls._val + lAdd;
      PutLong( pos, lv );
   }

   public void PutBool( int pos, boolean val )
   {
      _PutLong( pos, val ? 1 : 0, BOOL );
   }

   public void PutLong( int pos, long val )
   {
      _PutLong( pos, val, LONG );
   }

   public void PutHex( int pos, long val )
   {
      _PutLong( pos, val, HEX );
   }

   public void PutString( int pos, String val )
   {
      _PutString( pos, val );
   }

   public void PutTimeNow( int pos )
   {
      PutTime( pos, System.currentTimeMillis() );
   }

   public void PutTime( int pos, long val )
   {
      _PutLong( pos, val, TIME );
   } 

   public synchronized void PutDouble( int pos, double val )
   { 
      int off;

      // Defensive coding

      if ( pos < _max ) {
         off = pos * _statSz;
         _map.position( off+_nmSz );
         _map.put( (byte)DOUBLE );
         _map.position( off+_hSz );
         _map.putDouble( val );
      } 
   } 

   public synchronized Stat GetStat( int pos )
   {
      Stat   rtn;
      String str, val;
      byte[] bp;
      char   ty;
      long   lv;
      int    off;

      // Pre-condition

      if ( ( pos < 0 ) || ( pos >= _max ) )
         return null;

      // Pull out

      off = pos * _statSz;
      bp  = new byte[_nmSz];
      _map.position( off );
      _map.get( bp );
      str = new String( bp ).trim();
      _map.position( off+_nmSz );
      ty  = (char)_map.get();
      rtn = null;
      _map.position( off+_hSz );
      switch( ty ) {
         case BOOL:
            lv  = _map.getLong();
            rtn = new BoolStat( str, ( lv != 0 ) ? true : false );
            break;
         case LONG:
            rtn = new LongStat( str, _map.getLong() );
            break;
         case HEX:
            rtn = new HexStat( str, _map.getLong() );
            break;
         case TIME: 
            rtn = new TimeStat( str, _map.getLong() );
            break; 
         case DOUBLE:
            rtn = new DoubleStat( str, _map.getDouble() );
            break;
         case STRING:
            _map.position( off+_hSz );
            _map.get( bp );
            val = new String( bp ).trim();
            rtn = new StringStat( str, val );
            break;
      }
      return rtn;
   }


   ///////////////////////////////
   // Helpers
   ///////////////////////////////
   private synchronized int _Insert( String name, char ty )
   {
      String str;
      byte[] np;
      int    off;

      // Pre-condition

      if ( _pos == _max )
         return -1;

      // Safe to add - Ensure < _nmSz bytes

      off = _pos * _statSz;
      str = name.substring( 0,Math.min( name.length(), _nmSz ) );
      np  = str.getBytes();
      _map.position( off );
      _map.put( np, 0, str.length() );
      _map.position( off+_nmSz );
      _map.put( (byte)ty );
      return _pos++;
   }

   private synchronized void _PutLong( int pos, long val, char ty )
   {
      int off;

      // Defensive coding

      if ( pos < _max ) {
         off = pos * _statSz;
         _map.position( off+_nmSz );
         _map.put( (byte)ty );
         _map.position( off+_hSz );
         _map.putLong( val );
      }
   }

   private synchronized void _PutString( int pos, String val )
   {
      String str;
      byte[] np;
      int    off;

      // Pre-condition

      if ( pos == _max )
         return;

      // Safe to add - Ensure < _strSz bytes

      off = pos * _statSz;
      str = val.substring( 0,Math.min( val.length(), _strSz ) );
      np  = str.getBytes();
      _map.position( off+_nmSz );
      _map.put( (byte)STRING );
      _map.position( off+_hSz );
      _map.put( _zz, 0, _zz.length );
      _map.position( off+_hSz );
      _map.put( np, 0, str.length() );
   }


   ///////////////////////////////
   // Class-wide
   ///////////////////////////////
   public static long size( String file )
   {
      long rtn;

      rtn = 0;
      try {
         rtn = new RandomAccessFile( file, "r" ).length();
      } catch( Exception e ) {
         rtn = 0;
      }
      return ( rtn / _statSz ) - 1;
   }
}
