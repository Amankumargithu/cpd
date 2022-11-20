/**
 * DateColumnDecorator.java
 *
 * @author Copyright (c) 2004 by Tacpoint Technologies, Inc.
 *         All rights reserved.
 * @version 1.0
 */
package com.b4utrade.web.decorator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

import org.displaytag.decorator.ColumnDecorator;
import org.displaytag.exception.DecoratorException;

public class DateColumnDecorator implements ColumnDecorator
{
   public String decorate(Object columnValue) throws DecoratorException
   {
      try
      {
		 Calendar cal = (Calendar)columnValue;
		 if (cal == null) return "N/A";

         Date colDate = cal.getTime();
         if (colDate == null)
         {
            return "N/A";
         }

         SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
         String newDate = formatter.format(colDate);
         return (newDate);
      }
      catch (Exception e)
      {
         return "";
      }
   }
}
