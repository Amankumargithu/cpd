/******************************************************************************
*
*  OnSession.java

*     _OnSession() re-subscribe
*
*  REVISION HISTORY:
*      8 JUL 2013 jcs  Created.
*
*  (c) 2011-2013 Quodd Financial
*******************************************************************************/
package com.quodd.tools;
import java.io.*;
import java.text.*;
import java.lang.*;
import java.util.*;

public class OnSession
{
   private int         _n;
   private PrintStream cout;

   /////////////////////
   // Constructor
   /////////////////////
   public OnSession( int n )
   {
      _n   = n;
      cout = System.out;
   }


   /////////////////////
   // Operations
   /////////////////////
   public void Run( boolean bSlow )
   {
      if ( bSlow )
         Slow();
      else
         Fast();
   }

   /////////////////////
   // Helpers
   /////////////////////
   private void Slow()
   {
      int    i;
      String qry;

      cout.printf( "%s START-SLOW : %d tkrs\n", Now(), _n );
      qry = "";
      for ( i=0; i<_n; i++ )
         qry += qry.format( "<OPN Name=\"%06d\"/>\n", i );
      cout.printf( "%s END-SLOW : %d bytes\n", Now(), qry.length() );
   }

   private void Fast()
   {
      int      i;
      String   qry, fmt;
      String[] qdb;

      cout.printf( "%s START-FAST : %d tkrs\n", Now(), _n );
      qdb = new String[_n];
      qry = "";
      fmt = "<OPN Name=\"%06d\"/> ";
      for ( i=0; i<_n; qdb[i] = qry.format( fmt, i++ ) );
      qry = Arrays.toString( qdb ).replace( " , <", " <" );
      cout.printf( "%s END-FAST : %d bytes\n", Now(), qry.length() );
      if ( _n < 100 )
         cout.printf( qry );
   }

   private String Now()
   {
      long   t0;
      String s;

      t0 = System.currentTimeMillis();
      s  = "[" + pDateTimeMs( t0 ) + "] ";
      return s;
   }

   private String pDateTimeMs( long tm )
   {
      Date   dt;
      String s, fmt;

      dt  = new Date( tm );
      fmt = "yyyy-MM-dd HH:mm:ss.SSS";
      s   = new SimpleDateFormat( fmt ).format( dt );
      return s;
   }


   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      OnSession sub;
      int     n, argc;
      boolean bSlow;

      argc  = args.length;
      n     = ( argc > 0 ) ? Integer.parseInt( args[0] ) : 35000;
      bSlow = ( argc > 1 ) ? args[1].equals( "SLOW" ) : true;
      sub  = new OnSession( n );
      sub.Run( bSlow );
      System.out.printf( "\nDone!!\n" );
   }
}
