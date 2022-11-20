 /**
  * StockDetail.java
  * Copyright: Tacpoint Technologies, Inc. (c), 1999.  All rights reserved.
  * @author Kim Gentes
  * @author kimg@tacpoint.com
  * @version 1.0
  * Date created:  5/25/2000
  */
package com.b4utrade.stockutil;

import com.tacpoint.common.DefaultObject;

public class StockDetail extends DefaultObject
{
    private StockActivity mStock = null;
    private StockNewsActivity mNews = null;

    public StockDetail() {}

    public StockActivity getStock()
    {
        return mStock;
    }

    public StockNewsActivity getNews()
    {
        return mNews;
    }

    public void setStock(StockActivity inStock)
    {
        mStock = inStock;
    }

    public void setNews(StockNewsActivity inNews)
    {
        mNews = inNews;
    }
}
