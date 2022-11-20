/******************************************************************************
*
*  MktStatsDisplay.java
*     QuoddFeed.MD.MktStats example
*
*  REVISION HISTORY:
*     11 JUL 2012 jcs  Created.
*
*  (c) 2011-2012, Quodd Financial
*******************************************************************************/
package com.quodd.examples;

import java.util.*;
import QuoddFeed.util.*;
import QuoddFeed.MD.*;

public class MktStatsDisplay
{
   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      UltraChan uChan;
		MktStats  chn;
      Scanner   sc;
      int       nRow, argc;

      argc  = args.length;
      nRow  = ( argc > 0 ) ? Integer.parseInt( args[0] ) : 1000;
      uChan = new UltraChan( UCconfig.Hostname(),
                             UCconfig.Port(), 
                             "username", 
                             "password", 
                             false ); 
      uChan.Start();
      chn   = new MktStats( uChan, nRow );
      System.out.printf( "Hit <ENTER> to terminate...\n" );
      sc = new Scanner( System.in );
      sc.nextLine();
      System.out.printf( "Shutting down ...\n" );
      uChan.Stop();
      System.out.printf( QuoddFeed.msg.Cmd.ANSI_CLEAR );
      System.out.printf( "Done!!\n" );
   }
}
