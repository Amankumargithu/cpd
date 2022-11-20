package com.b4utrade.helper;

import com.tacpoint.common.*;

import com.tacpoint.util.*;

import com.b4utrade.bean.NewsBean;


import java.util.*;
import java.util.zip.*;
import java.io.*;

/**
 * Responsible for serializing/compressing and de-serializing/de-compressing NewsBeans
 *
 * @author Copyright: Tacpoint Technologies, Inc. (c), 2005.  All rights reserved.
 * @version 1.0
 */

public class NewsCompressor extends DefaultObject {


   public static final int ATTR_DELIM = 124; // |
   public static final int REC_DELIM  = 63;  // ? 

   public static byte[] compress(Collection news) {

      ByteArrayOutputStream baos = null;
      GZIPOutputStream zbaos = null;

      try {

         long beginTime = System.currentTimeMillis();

         if (news == null || news.size() == 0) return null;

         baos = new ByteArrayOutputStream(40*news.size());
         zbaos = new GZIPOutputStream(baos);

         if (news == null) return null;
      
         Iterator it = news.iterator();

         int totalBytes = 0;
         byte[] bytes = null;

         while (it.hasNext()) {

            NewsBean bean = (NewsBean)it.next();

            // news ID
            bytes = String.valueOf(bean.getNewsID()).getBytes();
            totalBytes += bytes.length+1;
            zbaos.write(bytes);
            zbaos.write(ATTR_DELIM);

            // comma delim ticker list
            if (bean.getTickers() != null) {
               bytes = bean.getTickers().getBytes();
               totalBytes += bytes.length;
               zbaos.write(bytes);
            }
           
            totalBytes++;
            zbaos.write(ATTR_DELIM);
      
            // comma delim category list
            if (bean.getCategories() != null) {
               bytes = bean.getCategories().getBytes();
               totalBytes += bytes.length;
               zbaos.write(bytes);
            }

            totalBytes++;
            zbaos.write(ATTR_DELIM);

            // formatted news date
            bytes = bean.getFormattedNewsDate().getBytes();
            totalBytes += bytes.length+1;
            zbaos.write(bytes);
            zbaos.write(ATTR_DELIM);

            //  news date
            bytes = String.valueOf(bean.getNewsDate().getTime()).getBytes();
            totalBytes += bytes.length+1;
            zbaos.write(bytes);
            zbaos.write(ATTR_DELIM);

            // news headline
            if (bean.getHeadline() != null) {
               bytes = bean.getHeadline().getBytes();
               totalBytes += bytes.length;
               zbaos.write(bytes);
            }

            zbaos.write(ATTR_DELIM);

            // news source
            bytes = bean.getNewsSource().getBytes();
            totalBytes += bytes.length+1;
            zbaos.write(bytes);
            zbaos.write(ATTR_DELIM);

            totalBytes++;
            zbaos.write(REC_DELIM);

         }

         zbaos.finish();

         bytes = baos.toByteArray();

         long endTime = System.currentTimeMillis();

         Logger.log("NewsCompressor.compress - uncompressed byte count : "+totalBytes+" compressed byte count : "+bytes.length);
         SystemOutLogger.debug("********** NewsCompressor.compress - uncompressed byte count : "+totalBytes+" compressed byte count : "+bytes.length);
         Logger.log("NewsCompressor.compress - time to compress news beans : "+(endTime-beginTime));
         SystemOutLogger.debug("********** NewsCompressor.compress - time to compress news beans : "+(endTime-beginTime));

         return bytes;

      }
      catch (Exception e) {
         Logger.log("NewsCompressor.compress - exception encountered : "+e.getMessage(),e);
         return null;
      }
      finally {
         if (zbaos != null) 
            try {
               zbaos.close();
            }
            catch (Exception zex) {}
      }

   }

   private static void populate(NewsBean bean, byte[] data, int index) {

      switch (index) {

         case 0 : 
            bean.setNewsID(Long.parseLong(new String(data)));
            break;
         case 1 : 
            bean.setTickers(new String(data));
            break;
         case 2 : 
            bean.setCategories(new String(data));
            break;
         case 3 : 
            bean.setFormattedNewsDate(new String(data));
            break;
         case 4 : 
            bean.setNewsDate(new java.sql.Timestamp(Long.parseLong(new String(data))));
            break;
         case 5 : 
            bean.setHeadline(new String(data));
            break;
         case 6 : 
            bean.setNewsSource(new String(data));
            break;
      }
   }

   private static byte[] unzip(byte[] data) {

      ByteArrayInputStream bais = null;
      GZIPInputStream zbais = null;

      try {
         bais = new ByteArrayInputStream(data);
         zbais = new GZIPInputStream(bais);
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length * 2);

         byte[] results = new byte[1024];
      
         int len = 0;
         while ((len = zbais.read(results, 0, results.length)) >= 0) 
            baos.write(results,0,len);

         byte[] bytes = baos.toByteArray();

         Logger.log("NewsCompressor.unzip - compressed byte count : "+data.length+" uncompressed byte count : "+bytes.length);
         SystemOutLogger.debug("********** NewsCompressor.unzip - compressed byte count : "+data.length+" uncompressed byte count : "+bytes.length);

         return baos.toByteArray();
      }
      catch (Exception e) {
         Logger.log("NewsCompressor.unzip - exception encountered : "+e.getMessage(),e);
         return null;
      }
      finally {
         if (zbais != null) 
            try {
               zbais.close();
               bais.close();
            }
            catch (Exception zex) {}
      }
         
      
   }
   
   public static Collection decompress(byte[] news) {


      long beginTime = System.currentTimeMillis();

      ArrayList list = new ArrayList();

      if (news == null || news.length == 0) return list;

      news = unzip(news);

      long endTime = System.currentTimeMillis();

      Logger.log("NewsCompressor.decompress - time to decompress news bytes : "+(endTime-beginTime));
      SystemOutLogger.debug("********** NewsCompressor.decompress - time to decompress news bytes : "+(endTime-beginTime));

      beginTime = System.currentTimeMillis();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      
      int attributeCounter = 0;

      NewsBean bean = new NewsBean();

      byte[] bytes = null;

      //Logger.log("NEWS DATA : "+new String(news));

      for (int i=0; i<news.length; i++) {

         if (news[i] == ATTR_DELIM) {
            bytes = baos.toByteArray();
            if (bytes.length > 0)
               populate(bean, bytes, attributeCounter);
            baos.reset();
            attributeCounter++;
         }
         else if (news[i] == REC_DELIM) {
            bytes = baos.toByteArray();
            if (bytes.length > 0)
               populate(bean, bytes, attributeCounter);
            attributeCounter = 0;
            baos.reset();
            if (bean.getNewsID() > 0)
               list.add(bean);
            bean = new NewsBean();
         }
         else {
            baos.write(news[i]); 
         }
      }

      endTime = System.currentTimeMillis();

      Logger.log("NewsCompressor.decompress - time to inflate news beans : "+(endTime-beginTime));
      SystemOutLogger.debug("********** NewsCompressor.decompress - time to inflate news beans : "+(endTime-beginTime));

      /*
      Iterator it = list.iterator();
      while (it.hasNext()) {
         NewsBean nb = (NewsBean)it.next();
         SystemOutLogger.debug("********** News Bean ID : "+nb.getNewsID()+" Formatted news date : "+nb.getFormattedNewsDate());
      }
      */


      return list;

   }

}
