/******************************************************************************
*
*  UCfileConfig.java
*     UltraCache config - From key = value file
*
*  REVISION HISTORY:
*     23 JUN 2015 jcs  Created (from UCconfig.java)
*
*  (c) 1994-2015 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.util;

import java.lang.*;
import java.io.*;
import java.util.*;
import QuoddFeed.msg.QuoddMsg;


/////////////////////////////////////////////////////////////////
// 
//             c l a s s   U C f i l e C o n f i g
//
/////////////////////////////////////////////////////////////////
/**
 * UCfileConfig gets configuration from a key = value file.
 */
public class UCfileConfig
{
   ////////////////////
   // Instance Members
   ////////////////////
   private HashMap<String, String> _cfg;


   //////////////////////
   // Constructor
   //////////////////////
   public UCfileConfig()
   {
      _cfg = new HashMap<String, String>();
   }

   public UCfileConfig( String file )
   {
      _cfg = new HashMap<String, String>();
      Load( file );
   }

   public void Load( String file )
   {
      BufferedReader buf;
      String         line, s, fmt;
      String[]       kv;

      try {
         buf = new BufferedReader( new FileReader( file ) );
         for ( ; (line=buf.readLine()) != null; ) {
            s  = line.split( "#" )[0].trim();
            kv = s.split("=");
            if ( kv.length == 2 )
               AddString( kv[0].trim(), kv[1].trim() );
         }
         buf.close();
      } catch( Exception e ) {
         fmt = "[%s] ERROR opening %s : %s\n";
         System.out.printf( fmt, Now(), file, e.getMessage() );
      }
   }


   //////////////////////
   // Access
   //////////////////////
   public String Now()
   {
      long   t0;
      String p0;

      t0 = System.currentTimeMillis();
      p0 = QuoddMsg.pDateTimeMs( t0 );
      return p0;
   }

   public String Dump()
   {
      String s;

      s = "";
      for ( Map.Entry<String, String> e : _cfg.entrySet() )
         s += s.format( "%-20s = %s\n", e.getKey(), e.getValue() );
      return s;
   }

   public String GetString( String key )
   {
      String val;

      val = "NO";
      if ( _cfg.containsKey( key ) )
         val = _cfg.get( key );
      return val;
   }

   public boolean GetBool( String key )
   {
      return GetString( key ).equals( "true" );
   }

   public int GetInt( String key )
   {
      try {
         return Integer.parseInt( GetString( key ) );
      } catch( Exception e ) { ; }
      return 0;
   }

   public double GetDouble( String key )
   {
      try {
         return Double.parseDouble( GetString( key ) );
      } catch( Exception e ) { ; }
      return 0.0;
   }


   //////////////////////
   // Mutator
   //////////////////////
   public void AddString( String key, String val )
   {
      _cfg.put( key, val );
   }

   public void AddInt( String key, int iv )
   {
      _cfg.put( key, Integer.toString( iv ) );
   }

   public void AddDouble( String key, double dv )
   {
      _cfg.put( key, Double.toString( dv ) );
   }

   public void AddBool( String key, boolean bv )
   {
      _cfg.put( key, Boolean.toString( bv ) );
   } 
}
