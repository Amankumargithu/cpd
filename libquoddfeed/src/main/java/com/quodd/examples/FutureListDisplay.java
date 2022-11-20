/******************************************************************************
*
*  FutureListDisplay.java
*     QuoddFeed.MD.FutureList example
*
*  REVISION HISTORY:
*     18 JAN 2012 jcs  Created.
*     30 JAN 2012 jcs  Build 13: UltraChan._usr / _pwd
*     28 JUN 2012 jcs  Build 28: UCconfig
*
*  (c) 2011-2012, Quodd Financial
*******************************************************************************/
package com.quodd.examples;

import java.util.*;
import QuoddFeed.util.*;

public class FutureListDisplay
{
   /////////////////////////
   //
   //  main()
   //
   /////////////////////////
   public static void main( String args[] )
   {
      UltraChan               uChan;
      QuoddFeed.MD.FutureList chn;
      Scanner                 sc;
      String                  tkr;
      int                     i, nRow, argc;

      argc  = args.length;
      tkr   = ( argc > 0 ) ? args[0] : "CL";
      nRow  = ( argc > 1 ) ? Integer.parseInt( args[1] ) : 1000;
      uChan = new UltraChan( UCconfig.Hostname(),
                             UCconfig.Port(), 
                             "username", 
                             "password", 
                             false ); 
      uChan.Start();
      chn   = new QuoddFeed.MD.FutureList( uChan, tkr, nRow );
      System.out.printf( "Hit <ENTER> to terminate...\n" );
      sc = new Scanner( System.in );
      sc.nextLine();
      System.out.printf( "Shutting down ...\n" );
      uChan.Stop();
      System.out.printf( QuoddFeed.msg.Cmd.ANSI_CLEAR );
      System.out.printf( "Done!!\n" );
   }
}
