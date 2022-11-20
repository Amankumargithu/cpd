/******************************************************************************
*
*  Notify.java
*     Verify notify() blocks
*
*  REVISION HISTORY:
*     18 JUL 2013 jcs  Created.
*
*  (c) 2011-2013 Quodd Financial
*******************************************************************************/
package com.quodd.tools;
import java.io.*;
import java.lang.*;
import java.util.*;

public class Notify
{
   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      PrintStream cout = System.out;
      Scanner     sc;
      int         i;
      long        t0, td;
      double      dd;

      sc = new Scanner( System.in );
      cout.printf( "Hit <ENTER> to notify()...\n" );
      sc.nextLine();
t0 = System.currentTimeMillis();
      for ( i=0; i<10000000; i++ ) {
         synchronized( sc ) {
            sc.notify();
         }
/*
         if ( i%100000 == 0 )
            cout.printf( "." );
 */
      }
td = System.currentTimeMillis() - t0;
dd = ( 1000000.0 * td ) / i;
      cout.printf( "\n" );
      cout.printf( "%d in %dmS = %.1fnS each\n", i, td, dd );
      cout.printf( "Done!!\n" );
   }
}
