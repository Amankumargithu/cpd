/******************************************************************************
*
*  UCconfig.java
*     UltraCache config - From environment
*
*  REVISION HISTORY:
*     28 JUN 2012 jcs  Created.
*     20 JUL 2012 jcs  Build 33: UC_TCPNODELAY
*     31 JAN 2013 jcs  Build 51: v0.24: User-supplied defaults
*     10 JUN 2013 jcs  Build 63: v0.24: UC_READPOLL
*     15 JUL 2013 jcs  Build 67: v0.24: GetInt()
*     15 MAY 2014 jcs  Build 76: v0.24: GetInt( dflt ); GetEnv( delf )
*
*  (c) 1994-2014 Gatea Ltd.
*******************************************************************************/
package QuoddFeed.util;

import java.lang.*;
import java.util.*;


/////////////////////////////////////////////////////////////////
// 
//               c l a s s   U C c o n f i g
//
/////////////////////////////////////////////////////////////////
/**
 * UCconfig gets configuration from the environment.  Current configurable
 * values are:
 * <p> UC_HOST
 * <p> UC_PORT
 * <p> UC_USERNAME
 * <p> UC_PASSWORD
 * <p> UC_TCPNODELAY
 * <p> UC_READPOLL
 */
public class UCconfig
{
   ///////////////////////////
   // Environment
   ///////////////////////////
   static protected Map<String,String> _env = System.getenv();

   ///////////////////////////
   // Class-wide
   ///////////////////////////
   /**
    * Returns true if environment variable is set 
    * <p>
    * @param key Environment variable
    * @return true if key is defined in the environment
    */
   static public boolean HasKey( String key )
   {
      return _env.containsKey( key );
   }

   /**
    * Retrieves the variable from the environment
    * <p>
    * @param key Environment variable 
    * @return Value configured in environment; null if not defined
    */
   static public String GetEnv( String key ) 
   {
      return GetEnv( key, null );
   }

   /**
    * Retrieves the variable from the environment
    * <p>
    * @param key Environment variable
    * @param dflt Default value if not found
    * @return Value configured in environment; null if not defined
    */
   static public String GetEnv( String key, String dflt )
   {
      return HasKey( key ) ? _env.get( key ) : dflt;
   }

   /**
    * Retrieves the environment variable as int
    * <p>
    * @param key Environment variable 
    * @return Env variable as int; 0 if not found
    */
   static public int GetInt( String key )
   {
      String val;
      int    rtn;

      val = HasKey( key ) ? GetEnv( key ) : "0";
      try {
         rtn = Integer.valueOf( val ).intValue();
      } catch( NumberFormatException e ) {
         rtn = 0;
      }
      return rtn;
   }

   /**
    * Retrieves the environment variable as int w/ user-suppied default
    * <p>
    * @param key Environment variable
    * @param dflt Default value if not found
    * @return Env variable as int; User-supplied if not found
    */
   static public int GetInt( String key, int dflt )
   {
      String val;
      int    rtn;

      rtn = dflt;
      if ( HasKey( key ) ) {
         try {
            val = GetEnv( key );
            rtn = Integer.valueOf( val ).intValue();
         } catch( NumberFormatException e ) {
            rtn = dflt;
         }
      }
      return rtn;
   }

   /**
    * Retrieves the UC_HOST environment variable
    * <p>
    * @return UC_HOST environment variable; localhost if not defined
    */
   static public String Hostname()
   {
      return Hostname( "localhost" );
   }

   /**
    * Retrieves the UC_PORT environment variable 
    * <p>
    * @return UC_PORT env variable; 4321 if not defined
    */
   static public int Port()
   {   
      return Port( 4321 );
   }   

   /**
    * Retrieves the UC_USERNAME environment variable 
    * <p>
    * @return UC_USERNAME environment variable; null if not defined
    */
   static public String Username()
   {
      return Username( null );
   }

   /**
    * Retrieves the UC_PASSWORD environment variable
    * <p>
    * @return UC_PASSWORD environment variable; null if not defined
    */
   static public String Password()
   {
      return GetEnv( "UC_PASSWORD" );
   }

   /**
    * Retrieves the UC_TCPNODELAY environment variable
    * <p>
    * @return UC_TCPNODELAY environment variable; false if not defined
    */
   static public boolean TcpNoDelay()
   {
      String key = "UC_TCPNODELAY";
      String val;

      val = HasKey( key ) ? GetEnv( key ) : "false";
      return Boolean.valueOf( val ).booleanValue();
   }

   /**
    * Retrieves the UC_READPOLL environment variable
    * <p>
    * @return UC_READPOLL environment variable; 0 if not defined
    */
   static public int ReadPoll()
   {
      return GetInt( "UC_READPOLL" );
   }


   ///////////////////////////
   // Query w/ User-Supplied Default
   ///////////////////////////
   /**
    * Retrieves the UC_HOST environment variable
    * <p>
    * @return UC_HOST env variable; User-supplied default if not found
    */
   static public String Hostname( String dflt )
   {
      return GetEnv( "UC_HOST", dflt );
   }

   /**
    * Retrieves the UC_PORT environment variable
    * <p>
    * @return UC_PORT env variable; User-supplied default if not found
    */
   static public int Port( int dflt )
   {
      return GetInt( "UC_PORT", dflt );
   }

   /**
    * Retrieves the UC_USERNAME environment variable
    * <p>
    * @return UC_USERNAME env variable; User-supplied default if not found
    */
   static public String Username( String dflt )
   {
      return GetEnv( "UC_USERNAME", dflt );
   }

   /**
    * Retrieves the UC_PASSWORD environment variable
    * <p>
    * @return UC_PASSWORD env variable; User-supplied default if not found
    */
   static public String Password( String dflt )
   {
      return GetEnv( "UC_PASSWORD", dflt );
   }
}
