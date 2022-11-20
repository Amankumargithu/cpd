package com.b4utrade.stock;

import com.b4utrade.stockutil.*;

import com.tacpoint.jms.*;
import com.tacpoint.util.*;

import java.io.*;

import javax.jms.*;

public class MM2MessageHandler extends MessageHandler
{

    private byte[] bytes = new byte[1024];

    //private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private byte[] message = null;

    public void setMessage(byte[] message) {
       this.message = message;
    }

    public void run() {

        int bytesRead = 0;

        try {

           //baos.reset();

           //while ( (bytesRead = message.readBytes(bytes)) != -1 ) {
           //   baos.write(bytes,0,bytesRead);
           //}

           DefaultStockObject vStock = StockStreamer.changeStreamToStockObject(new String(message));
           DefaultQuotes vOtherQuotes = MarketMakerQuotes.getInstance();
           vOtherQuotes.setStockQuote(vStock);

           //Quotes.getInstance().setStock(ticker, baos.toByteArray());

        }
        catch (Exception e) {
           Logger.log("QuoteMessageHandler.run - Unable to retrieve message.",e);
        }
    }

    public Object clone()throws CloneNotSupportedException {
       return new MM2MessageHandler();
    }
}
