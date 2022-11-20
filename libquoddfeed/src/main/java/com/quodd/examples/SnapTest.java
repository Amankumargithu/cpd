package com.quodd.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;

import QuoddFeed.msg.BlobTable;
import QuoddFeed.msg.Image;
import QuoddFeed.msg.QuoddMsg;
import QuoddFeed.msg.Status;
import QuoddFeed.util.UltraChan;


public class SnapTest {

   public static void main(String[] args) {
      String ip = "quodd175";
      int port = 4321;
      PrintStream cout = System.out;

      String exchange = null;
      String tkr, dTy;
      HashSet<String> tickerSet = new HashSet<String>();
      HashSet<String> dead = new HashSet<String>();
      if(exchange == null)
      {
         try
         {
         BufferedReader bufReader = new BufferedReader(new FileReader("./tickers.txt"));
         String line = null;
         while((line = bufReader.readLine()) != null)
         {
            tickerSet.add(line);
         }
         bufReader.close();
         }catch(Exception e)
         {
            e.printStackTrace();
         }
      }
      else if(exchange.equalsIgnoreCase("I"))
      {
         UltraChan uc = new UltraChan(ip, port, "GetIndices", "GetIndices", false);
         uc.Start();
         BlobTable table = uc.SyncGetIndices(new Object());
         if(table != null)
         {
            int rowCount = table.nRow();
            cout.println("parse Blob Table rowCount: " + rowCount);
            for (int count = 0; count < rowCount; count++)
            {
               String ticker = table.GetCell(count, 0);         
               cout.println(count + ") " + ticker);
               tickerSet.add(ticker);
            }

         }
         else
            cout.println("Exception: Indices list is null");   
         uc.Stop();
      }
      if(tickerSet.size() <= 0)
      {
         cout.println("ERROR: No tickers provided");
         return;
      }
      UltraChan uc = new UltraChan(ip, port, "SyncTest", "SyncTest", false);
      uc.Start();   
      cout.println(new Timestamp(System.currentTimeMillis()) + " SyncTest: - Sending request to UC " + ip + " " + port);
      int size = tickerSet.size();
      String[] stockArr = new String[size];
      int count = 0;
      for(String ticker : tickerSet)
      {
         stockArr[count] = ticker;
         count++;
      }
//      cout.println(new Timestamp(System.currentTimeMillis()) + " UCServletHelper: requested tickers - " + Arrays.toString(stockArr));
      cout.println(new Timestamp(System.currentTimeMillis()) + " Sending SyncMultiSnap() to UC - ticker count: " + tickerSet.size());
      QuoddMsg[] tickerSnaps = uc.SyncMultiSnap(stockArr, null);
      cout.println(new Timestamp(System.currentTimeMillis()) + " Got records from UC " + tickerSnaps.length);
      char mt, mt2;
      for(int j = 0; j < tickerSnaps.length ; j++)
      {
         mt = tickerSnaps[j].mt();
         mt2 = tickerSnaps[j].mtSub();
         if( mt2 == UltraChan._mtSubIMG)
         {
            Image img = (Image)tickerSnaps[j];

            cout.printf( "[%s] IMAGE : %s\n", uc.Now(), img.tkr() );

            tkr = img.tkr().substring(0,img.tkr().indexOf("\""));   
            tickerSet.remove(tkr);            
         }
         else if(mt== UltraChan._mtDEAD)
         {
            Status sts = (Status)tickerSnaps[j];

            try {
               tkr = sts.tkr().substring(0,sts.tkr().indexOf("\""));   
            } catch( StringIndexOutOfBoundsException e ) {
               tkr = sts.tkr(); // + e.getMessage();
            }
            dTy = sts.IsIOException() ? "DEAD-IO" : "DEAD   ";
            cout.printf( "[%s] %s : %s = %s\n", uc.Now(), dTy, tkr, sts.Text() );
//            tickerSet.remove( tkr );            
            dead.add( tkr );
         }
         else if(tickerSnaps[j] == null)
         {
            cout.println("UCServletHelper : - Got null element from UC");
         }
         else
         {
            cout.println(" doesnt gets the image " + tickerSnaps[j].getClass());
         }
      }
      cout.println("Total Missing tickers from UC " + tickerSet.size());
/*
      for(String ticker: tickerSet)
         cout.println("Missing " + ticker);
 */
      for(String ticker: dead)
         cout.println("Missing-DEAD " + ticker);
      uc.Stop();
      if ( tickerSet.containsAll( dead ) && dead.containsAll( tickerSet ) )
cout.printf( "DEAD SUCCESS : %d tkrs\n", dead.size() );
      else 
cout.printf( "**** FAILURE : %d vs %d tkrs\n", tickerSet.size(), dead.size() );
      cout.println("Process complete");
   }
}
