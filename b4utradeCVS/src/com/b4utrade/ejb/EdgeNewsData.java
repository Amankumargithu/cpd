package com.b4utrade.ejb;

import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.Remote;

import com.b4utrade.bean.EdgeNewsCriteriaBean;
import com.b4utrade.bean.NewsBean;
@Remote
public interface EdgeNewsData
{
   public byte[] getCompressedNews(EdgeNewsCriteriaBean bean, int maxRows);
   public ArrayList getNews(EdgeNewsCriteriaBean bean, int maxRows);
   public HashMap getNewsByTickers(EdgeNewsCriteriaBean bean, int maxRows);
   public byte[] getCompressedNewsByTickers(EdgeNewsCriteriaBean bean, int maxRows);
   public HashMap getLatestNewsByTickers(String tickers, String sources);
   public NewsBean getNewsByID(long id);
}
